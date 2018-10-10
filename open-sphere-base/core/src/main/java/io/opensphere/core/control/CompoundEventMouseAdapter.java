package io.opensphere.core.control;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * This class is the intended event handler for most compound events, since most
 * compound events seem to involve mouse movement of some kind. Compound events
 * are those that require two separate events to occur to function properly,
 * such as panning the earth with the mouse. This requires the left press,
 * (event started), mouse drag, and left release, (event ended), to work. If
 * your event is something that can be invoked on only one event and doesn't
 * need more information, such as mouse drags, etc, then you should use the
 * <code>DiscreteEventAdapter</code> instead.
 *
 * Intended usage:
 *
 * <pre>
 * // context is a ControlContext
 * context.addListener(new CompoundEventMouseAdapter(string, "Smooth Yaw View",
 *         "Rotates the camera around its up/down axis with mouse movement")
 * {
 *      {@code @Override}
 *      public void eventEnded(InputEvent event)
 *      {
 *          getCurrentControlTranslator().compoundViewYawEnd(event);
 *      }
 *
 *      {@code @Override}
 *      public void eventStarted(InputEvent event)
 *      {
 *          getCurrentControlTranslator().compoundViewYawStart(event);
 *      }
 *
 *      {@code @Override}
 *      public void mouseDragged(MouseEvent event)
 *      {
 *          getCurrentControlTranslator().compoundViewYawDrag(event);
 *      }
 *
 *      {@code @Override}
 *      public void mouseMoved(MouseEvent event)
 *      {
 *          getCurrentControlTranslator().compoundViewYawDrag(event);
 *      }
 *  }, new DefaultMouseBinding(MouseEvent.MOUSE_PRESSED, MouseEvent.BUTTON3_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK));
 * </pre>
 *
 */
public class CompoundEventMouseAdapter extends CompoundEventAdapter
implements MouseListener, MouseMotionListener, MouseWheelListener
{
    /**
     * Construct the listener.
     *
     * @param category The category to present to the user.
     * @param title The title to present to the user.
     * @param description The description to present to the user.
     */
    public CompoundEventMouseAdapter(String category, String title, String description)
    {
        super(category, title, description);
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
    }
}
