package io.opensphere.core.control.newt;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Container;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.swing.EventQueueUtilities;

/** Convert NEWT mouse events to AWT mouse events and dispatch the event. */
public class NewtKeyEventConverter implements KeyListener
{
    /** The canvas which is used by JOGL for rendering. */
    private final Component myCanvas;

    /** The dispatcher for dispatching the AWT events. */
    private AWTEventDispatcher myDispatcher;

    /** The factory which will do the event translation from NEWT to AWT. */
    private final NewtAWTEventFactory myEventFactory;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox.
     * @param canvas The canvas which is used by JOGL for rendering.
     */
    public NewtKeyEventConverter(Toolbox toolbox, Canvas canvas)
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
    public boolean canDispatch()
    {
        if (myDispatcher == null)
        {
            if (myToolbox.getUIRegistry() != null)
            {
                EventQueueUtilities.runOnEDTAndWait(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Container mainFrame = myToolbox.getUIRegistry().getMainFrameProvider().get();
                        myDispatcher = new AWTEventDispatcher(mainFrame, myCanvas);
                    }
                });
            }
            else
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        dispatchEvent(e);
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        dispatchEvent(e);
    }

    /**
     * If this converter is operational, convert the event and dispatch it.
     *
     * @param event The event to be converted and dispatched.
     */
    private void dispatchEvent(final com.jogamp.newt.event.KeyEvent event)
    {
        if (canDispatch())
        {
            EventQueueUtilities.runOnEDT(new Runnable()
            {
                @Override
                public void run()
                {
                    // Because there is no NEWT KEY_TYPED event, we need to
                    // generate it ourselves.
                    if (event.getEventType() == com.jogamp.newt.event.KeyEvent.EVENT_KEY_RELEASED)
                    {
                        myDispatcher.dispatchEvent(myEventFactory.createSyntheticTypedEvent(event));
                    }
                    myDispatcher.dispatchEvent(myEventFactory.createKeyEvent(event));
                }
            });
        }
    }
}
