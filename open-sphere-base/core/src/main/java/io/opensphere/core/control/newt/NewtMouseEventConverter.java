package io.opensphere.core.control.newt;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Container;

import com.jogamp.newt.event.MouseListener;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.swing.EventQueueUtilities;

/** Convert NEWT mouse events to AWT mouse events and dispatch the event. */
public class NewtMouseEventConverter implements MouseListener
{
    /** The canvas which is used by JOGL for rendering. */
    private final Component myCanvas;

    /** The dispatcher for dispatching the AWT events. */
    private AWTEventDispatcher myDispatcher;

    /** The factory which will do the event translation from NEWT to AWT. */
    private final NewtAWTEventFactory myEventFactory;

    /** The main application frame which is an ancestor of the OpenGL canvas. */
    private Container myMainFrame;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox.
     * @param canvas The canvas which is used by JOGL for rendering.
     */
    public NewtMouseEventConverter(Toolbox toolbox, Canvas canvas)
    {
        myToolbox = toolbox;
        myEventFactory = new NewtAWTEventFactory(canvas);
        myCanvas = canvas;
    }

    /**
     * When true the converter is ready to dispatch events. When false, no event
     * translation will occur.
     *
     * @return true when the converter is operational.
     */
    private boolean canDispatch()
    {
        if (myDispatcher == null)
        {
            if (myToolbox.getUIRegistry() != null)
            {
                myMainFrame = myToolbox.getUIRegistry().getMainFrameProvider().get();
                myDispatcher = new AWTEventDispatcher(myMainFrame, myCanvas);
            }
            else
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public void mouseClicked(com.jogamp.newt.event.MouseEvent e)
    {
        dispatchEvent(e);
    }

    @Override
    public void mouseDragged(com.jogamp.newt.event.MouseEvent e)
    {
        dispatchEvent(e);
    }

    @Override
    public void mouseEntered(com.jogamp.newt.event.MouseEvent e)
    {
        // Do not dispatch enter and exit events. The swing dispatcher will
        // handle this.
    }

    @Override
    public void mouseExited(com.jogamp.newt.event.MouseEvent e)
    {
        // Do not dispatch enter and exit events. The swing dispatcher will
        // handle this.
    }

    @Override
    public void mouseMoved(com.jogamp.newt.event.MouseEvent e)
    {
        dispatchEvent(e);
    }

    @Override
    public void mousePressed(com.jogamp.newt.event.MouseEvent e)
    {
        dispatchEvent(e);
    }

    @Override
    public void mouseReleased(com.jogamp.newt.event.MouseEvent e)
    {
        dispatchEvent(e);
    }

    @Override
    public void mouseWheelMoved(com.jogamp.newt.event.MouseEvent e)
    {
        dispatchEvent(e);
    }

    /**
     * If this converter is operational, convert the event and dispatch it.
     *
     * @param e The event to be converted and dispatched.
     */
    private void dispatchEvent(final com.jogamp.newt.event.MouseEvent e)
    {
        if (canDispatch())
        {
            EventQueueUtilities.runOnEDT(() -> myDispatcher.dispatchEvent(myEventFactory.createMouseEvent(e)));
        }
    }
}
