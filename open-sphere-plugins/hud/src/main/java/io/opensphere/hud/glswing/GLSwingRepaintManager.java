package io.opensphere.hud.glswing;

import java.awt.Component;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import io.opensphere.core.hud.awt.AbstractInternalFrame;
import io.opensphere.core.util.collections.New;

/** Manager for repainting swing components. */
public class GLSwingRepaintManager extends RepaintManager
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GLSwingRepaintManager.class);

    /**
     * Components which require repainting and their associated frames. This
     * collection may be modified off of the EDT.
     */
    private Map<JComponent, AbstractInternalFrame> myDirtyComponents = New.map();

    /**
     * Lock access to the map of dirty components. The map of dirty components
     * being processed need not also be locked with this since it is only used
     * while on the EDT.
     */
    private final Lock myDirtyComponentsLock = new ReentrantLock();

    /**
     * Components which require repainting and their associated frames. This
     * collection is used only while processing a repaint and may only be
     * modified on the EDT.
     */
    private Map<JComponent, AbstractInternalFrame> myDirtyComponentsProcessing = New.map();

    /**
     * A map of GLSwingInternalFrame to the regions in that frame which are
     * dirty.
     */
    private final Map<GLSwingInternalFrame, Collection<Rectangle>> myFrameDirtyRegions = New.map();

    /**
     * A map of AbstractInternalFrame to the associated GLSwingInternalFrame.
     */
    private final Map<AbstractInternalFrame, GLSwingInternalFrame> myFrames = New.map();

    @Override
    public void addDirtyRegion(JComponent comp, int x, int y, int width, int height)
    {
        if (width == 0 || height == 0)
        {
            return;
        }

        myDirtyComponentsLock.lock();
        try
        {
            if (!myDirtyComponents.containsKey(comp))
            {
                AbstractInternalFrame ancestor = internalFrameAncestor(comp);
                if (ancestor != null)
                {
                    myDirtyComponents.put(comp, ancestor);
                }
            }
        }
        finally
        {
            myDirtyComponentsLock.unlock();
        }

        super.addDirtyRegion(comp, x, y, width, height);
    }

    /**
     * Add a frame to notify when repainting dirty regions.
     *
     * @param frame frame to add.
     */
    public void addFrame(GLSwingInternalFrame frame)
    {
        synchronized (myFrames)
        {
            myFrames.put(frame.getHUDFrame().getInternalFrame(), frame);
            myFrameDirtyRegions.put(frame, new ArrayList<Rectangle>());
        }
    }

    @Override
    public void paintDirtyRegions()
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            LOGGER.error("Repainting must be done on the EDT.");
            return;
        }

        myDirtyComponentsLock.lock();
        try
        {
            // Switch the map for collecting dirty components with the one for
            // processing
            Map<JComponent, AbstractInternalFrame> swap = myDirtyComponentsProcessing;
            myDirtyComponentsProcessing = myDirtyComponents;
            myDirtyComponents = swap;
        }
        finally
        {
            myDirtyComponentsLock.unlock();
        }

        synchronized (myFrames)
        {
            for (Entry<JComponent, AbstractInternalFrame> entry : myDirtyComponentsProcessing.entrySet())
            {
                JComponent comp = entry.getKey();
                AbstractInternalFrame intFrame = entry.getValue();
                GLSwingInternalFrame frame = myFrames.get(intFrame);

                // Only process the regions if this is a frame for which I am
                // responsible.
                if (frame != null)
                {
                    // The dirty region for the component is in the component's
                    // coordinates.
                    Rectangle dirtyComp = getDirtyRegion(comp);

                    if (dirtyComp.width > 0 && dirtyComp.height > 0)
                    {
                        // Convert the dirty region to the internal frame's
                        // coordinates and make sure that it is contained within
                        // the frame's bounds.
                        Rectangle dirtyFrame = SwingUtilities.convertRectangle(comp, dirtyComp, intFrame);
                        dirtyFrame = SwingUtilities.computeIntersection(0, 0, intFrame.getWidth(), intFrame.getHeight(),
                                dirtyFrame);
                        if (dirtyFrame.width > 0 && dirtyFrame.height > 0)
                        {
                            myFrameDirtyRegions.get(frame).add(dirtyFrame);
                        }
                    }
                }
            }

            super.paintDirtyRegions();

            for (Entry<GLSwingInternalFrame, Collection<Rectangle>> entry : myFrameDirtyRegions.entrySet())
            {
                if (!entry.getValue().isEmpty())
                {
                    entry.getKey().handleImageDirty(entry.getValue());
                    entry.getValue().clear();
                }
            }
        }
        myDirtyComponentsProcessing.clear();
    }

    /**
     * Remove a frame to notify when repainting dirty regions.
     *
     * @param frame frame to remove.
     */
    public void removeFrame(GLSwingInternalFrame frame)
    {
        synchronized (myFrames)
        {
            myFrames.remove(frame.getHUDFrame().getInternalFrame());
            myFrameDirtyRegions.remove(frame);
        }
    }

    /**
     * Tell whether this component or one of its ancestors is an
     * {@link AbstractInternalFrame}.
     *
     * @param comp The component which may have an internal frame parent.
     * @return true when this component or one of its ancestors is an
     *         {@link AbstractInternalFrame}.
     */
    private AbstractInternalFrame internalFrameAncestor(Component comp)
    {
        if (comp == null)
        {
            return null;
        }

        if (comp instanceof AbstractInternalFrame)
        {
            if (comp.getParent() != null)
            {
                // Check to make sure that this isn't a frame within a frame,
                // because sometimes when you have a class which is called
                // panel, it's really a frame when it could have been a panel,
                // but it isn't; it's a frame.
                AbstractInternalFrame realFrame = internalFrameAncestor(comp.getParent());
                if (realFrame != null)
                {
                    return realFrame;
                }
            }
            return (AbstractInternalFrame)comp;
        }

        return internalFrameAncestor(comp.getParent());
    }
}
