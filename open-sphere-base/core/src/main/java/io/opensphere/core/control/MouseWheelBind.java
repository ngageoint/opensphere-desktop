package io.opensphere.core.control;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Internal binding used to translate mouse wheel events to external bindings.
 */
public class MouseWheelBind extends DiscreteMouseBind
{
    /** The wheel direction. */
    private final int myWheelDirection;

    /**
     * Construct the internal binding.
     *
     * @param binding The external binding.
     * @param listener The listener.
     * @param controlContext The control context.
     */
    MouseWheelBind(DefaultMouseWheelBinding binding, DiscreteEventListener listener, ControlContextImpl controlContext)
    {
        super(binding, listener, controlContext);
        myWheelDirection = binding.getWheelDirection().getDirection();
    }

    /**
     * Construct the binding.
     *
     * @param e The mouse wheel event.
     * @param listener The listener.
     */
    MouseWheelBind(MouseWheelEvent e, DiscreteEventListener listener)
    {
        super(e, listener);
        myWheelDirection = e.getWheelRotation();
    }

    @Override
    public String getIDString()
    {
        StringBuilder str = new StringBuilder();
        switch (getEventId())
        {
            case MouseEvent.MOUSE_WHEEL:
            {
                str.append("Mouse Wheel ");
                str.append(myWheelDirection < 0 ? "Up" : "Down");
                break;
            }
            default:
            {
                str.append("Unknown type");
                break;
            }
        }

        return str.toString();
    }

    /**
     * Get the mouse wheel direction.
     *
     * @return The mouse wheel direction.
     */
    public int getWheelDirection()
    {
        return myWheelDirection;
    }

    /**
     * Method called when the mouse wheel moves.
     *
     * @param e The event.
     */
    void mouseWheelMoved(MouseEvent e)
    {
        getListener().eventOccurred(e);
    }
}
