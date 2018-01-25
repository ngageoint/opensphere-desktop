package io.opensphere.hud.glswing;

import java.awt.AWTEvent;
import java.awt.Container;
import java.awt.Toolkit;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import io.opensphere.core.control.ui.InternalComponentRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.ref.WeakReference;

/**
 * A Singleton class to handle AWTEvents and make sure that the panels with the
 * highest Z-Order which can use the events gets the events.
 */
public final class GLSwingEventManager
{
    /** Events which need to be forwarded for GLSwing interaction. */
    private static long ourEventMask = AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK
            | AWTEvent.MOUSE_WHEEL_EVENT_MASK;

    /** The singleton instance. */
    private static GLSwingEventManager ourInstance = new GLSwingEventManager();

    /** Listener for AWT events. */
    private final GLSwingAWTEventListener myAWTListener;

    /** A list of frames registered with me for forwarded AWT events. */
    private final List<WeakReference<GLSwingInternalFrame>> myFrames = New.list();

    /**
     * Get the singleton instance.
     *
     * @return The event manager.
     */
    public static GLSwingEventManager getInstance()
    {
        return ourInstance;
    }

    /** Disallow instantiation. */
    private GLSwingEventManager()
    {
        final String newtString = System.getProperty("opensphere.pipeline.jogl.nativeWindows");
        final boolean newt = Boolean.getBoolean(newtString);
        if (newt)
        {
            myAWTListener = null;
        }
        else
        {
            myAWTListener = new GLSwingAWTEventListener(this);
        }
        final Toolkit kit = Toolkit.getDefaultToolkit();
        kit.addAWTEventListener(myAWTListener, ourEventMask);
    }

    /**
     * De-register for forwarded AWT events.
     *
     * @param frame The frame no longer interested in events.
     */
    public void deregisterFrame(GLSwingInternalFrame frame)
    {
        synchronized (myFrames)
        {
            final Collection<WeakReference<GLSwingInternalFrame>> removes = New.set();
            for (final WeakReference<GLSwingInternalFrame> ref : myFrames)
            {
                if (frame == ref.get())
                {
                    removes.add(ref);
                }
            }
            myFrames.removeAll(removes);
        }
        if (myAWTListener != null)
        {
            myAWTListener.clearHeld();
        }
    }

    /** Handle any necessary cleanup for when a frame has been popped. */
    public void framePopped()
    {
        if (myAWTListener != null)
        {
            myAWTListener.clearHeld();
        }
    }

    /**
     * Perform any required initialization including setting the event
     * listener's component registry.
     *
     * @param registry The component registry to be used by my event listener.
     */
    public void init(InternalComponentRegistry registry)
    {
        myAWTListener.setComponentRegistry(registry);
    }

    /**
     * Register for forwarded AWT events.
     *
     * @param frame The frame interested in events.
     */
    public void registerFrame(GLSwingInternalFrame frame)
    {
        synchronized (myFrames)
        {
            myFrames.add(new WeakReference<>(frame));
        }
    }

    /** Tell all active frames to validate their render order. */
    public void validateRenderOrders()
    {
        final List<WeakReference<GLSwingInternalFrame>> framesCopy = New.list(myFrames.size());
        synchronized (myFrames)
        {
            framesCopy.addAll(myFrames);
        }

        for (final WeakReference<GLSwingInternalFrame> frame : framesCopy)
        {
            final GLSwingInternalFrame glFrame = frame.get();
            if (glFrame != null)
            {
                glFrame.validateRenderOrder();
            }
        }
    }

    /**
     * Get a copy of all valid frames which I manage.
     *
     * @return Currently managed frames.
     */
    Set<GLSwingInternalFrame> getFrames()
    {
        final List<WeakReference<GLSwingInternalFrame>> removes = New.list();
        final Set<GLSwingInternalFrame> frames = new TreeSet<>(new FrameZOrderComparator());
        final List<WeakReference<GLSwingInternalFrame>> framesCopy = New.list(myFrames.size());
        synchronized (myFrames)
        {
            framesCopy.addAll(myFrames);
        }

        for (final WeakReference<GLSwingInternalFrame> frame : framesCopy)
        {
            final GLSwingInternalFrame glFrame = frame.get();
            if (glFrame == null)
            {
                removes.add(frame);
            }
            else
            {
                frames.add(glFrame);
            }
        }

        if (!removes.isEmpty())
        {
            synchronized (myFrames)
            {
                myFrames.removeAll(removes);
            }
        }

        return frames;
    }

    /**
     * Comparator for internal frames which orders the frames by their relative
     * z-order. This is used to ensure that frames which are in front will get
     * events before frames which are behind.
     */
    private static class FrameZOrderComparator implements Serializable, Comparator<GLSwingInternalFrame>
    {
        /** serialVersionUID. */
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(GLSwingInternalFrame frame1, GLSwingInternalFrame frame2)
        {
            final Container frameParent = frame1.getHUDFrame().getInternalFrame().getParent();
            if (frameParent != null)
            {
                final int z1 = frameParent.getComponentZOrder(frame1.getHUDFrame().getInternalFrame());
                final int z2 = frameParent.getComponentZOrder(frame2.getHUDFrame().getInternalFrame());

                if (z1 < z2)
                {
                    return -1;
                }
                else if (z2 < z1)
                {
                    return 1;
                }
            }
            return 0;
        }
    }
}
