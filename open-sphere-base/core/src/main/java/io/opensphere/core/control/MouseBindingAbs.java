package io.opensphere.core.control;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

/**
 * Abstract class for internal mouse bindings.
 */
abstract class MouseBindingAbs implements Binding
{
    /** Mask of compatible mouse events. */
    private static final int COMPATIBLE_FIELDS = MouseEvent.MOUSE_PRESSED | MouseEvent.MOUSE_RELEASED | MouseEvent.MOUSE_WHEEL
            | MouseEvent.MOUSE_MOVED;

    /** The control context. */
    private ControlContext myContext;

    /** The mouse event code. */
    private final int myEventId;

    /** The listener. */
    private BoundEventListener myListener;

    /** The modifier bitmask. */
    private final int myModifiersEx;

    /**
     * Construct the mouse binding.
     *
     * @param binding The external binding.
     * @param listener The listener.
     * @param controlContext The control context.
     */
    public MouseBindingAbs(DefaultMouseBinding binding, BoundEventListener listener, ControlContext controlContext)
    {
        myListener = listener;
        myContext = controlContext;

        myModifiersEx = binding.getModifiersEx();
        myEventId = binding.getMouseEvent();
    }

    /**
     * Construct the binding from a mouse event.
     *
     * @param e The mouse event.
     * @param listener The listener.
     * @param controlContext The control context.
     */
    public MouseBindingAbs(MouseEvent e, BoundEventListener listener, ControlContext controlContext)
    {
        myListener = listener;
        myContext = controlContext;

        myEventId = e.getID();
        myModifiersEx = e.getModifiersEx();
    }

    /**
     * Get the eventId.
     *
     * @return the eventId
     */
    public int getEventId()
    {
        return myEventId;
    }

    /**
     * Get a string that represents the mouse event code.
     *
     * @return The string.
     */
    public String getIDString()
    {
        StringBuilder str = new StringBuilder();
        switch (myEventId)
        {
            case MouseEvent.MOUSE_PRESSED:
                str.append("MOUSE_PRESSED");
                break;
            case MouseEvent.MOUSE_RELEASED:
                str.append("MOUSE_RELEASED");
                break;
            case MouseEvent.MOUSE_CLICKED:
                str.append("MOUSE_CLICKED");
                break;
            case MouseEvent.MOUSE_ENTERED:
                str.append("MOUSE_ENTERED");
                break;
            case MouseEvent.MOUSE_EXITED:
                str.append("MOUSE_EXITED");
                break;
            case MouseEvent.MOUSE_MOVED:
                str.append("MOUSE_MOVED");
                break;
            case MouseEvent.MOUSE_DRAGGED:
                str.append("MOUSE_DRAGGED");
                break;
            case MouseEvent.MOUSE_WHEEL:
                str.append("MOUSE_WHEEL");
                break;
            default:
                str.append("unknown type");
                break;
        }
        return str.toString();
    }

    @Override
    public BoundEventListener getListener()
    {
        return myListener;
    }

    /**
     * Get the modifiersEx.
     *
     * @return the modifiersEx
     */
    public int getModifiersEx()
    {
        return myModifiersEx;
    }

    /**
     * Set the context.
     *
     * @param context the context to set
     */
    public void setContext(ControlContext context)
    {
        myContext = context;
    }

    /**
     * Set the listener.
     *
     * @param listener the listener to set
     */
    public void setListener(BoundEventListener listener)
    {
        myListener = listener;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (myModifiersEx == 0)
        {
            if (myEventId == MouseEvent.MOUSE_WHEEL)
            {
                sb.append(getIDString());
            }
        }
        else
        {
            sb.append(InputEvent.getModifiersExText(myModifiersEx));
            if (myEventId == MouseEvent.MOUSE_WHEEL)
            {
                sb.append('+');
                sb.append(getIDString());
            }
        }

        return sb.toString();
    }

    /**
     * Get the context.
     *
     * @return the context
     */
    protected ControlContextImpl getContext()
    {
        return (ControlContextImpl)myContext;
    }

    /**
     * Send the mouse clicked event to my listener.
     *
     * @param e The mouse event.
     */
    void mouseClicked(MouseEvent e)
    {
        throw new UnsupportedOperationException("MOUSE_CLICKED not supported for binding " + this.getClass().getName());
    }

    /**
     * Send the mouse dragged event to my listener.
     *
     * @param e The mouse event.
     */
    void mouseDragged(MouseEvent e)
    {
        throw new UnsupportedOperationException("MOUSE_DRAGGED not supported for binding " + this.getClass().getName());
    }

    /**
     * Send the mouse moved event to my listener.
     *
     * @param e The mouse event.
     */
    void mouseEntered(MouseEvent e)
    {
        throw new UnsupportedOperationException("MOUSE_ENTERED not supported for binding " + this.getClass().getName());
    }

    /**
     * Send the mouse moved event to my listener.
     *
     * @param e The mouse event.
     */
    void mouseExited(MouseEvent e)
    {
        throw new UnsupportedOperationException("MOUSE_EXITED not supported for binding " + this.getClass().getName());
    }

    /**
     * Send the mouse moved event to my listener.
     *
     * @param e The mouse event.
     */
    void mouseMoved(MouseEvent e)
    {
        throw new UnsupportedOperationException("MOUSE_MOVED not supported for binding " + this.getClass().getName());
    }

    /**
     * Send the mouse pressed event to my listener.
     *
     * @param e The mouse event.
     */
    void mousePressed(MouseEvent e)
    {
        throw new UnsupportedOperationException("MOUSE_PRESSED not supported for binding " + this.getClass().getName());
    }

    /**
     * Send the mouse released event to my listener.
     *
     * @param e The mouse event.
     */
    void mouseReleased(MouseEvent e)
    {
        throw new UnsupportedOperationException("MOUSE_RELEASED not supported for binding " + this.getClass().getName());
    }

    /**
     * Check if a mouse event is compatible with this binding.
     *
     * @param eventNum The event code.
     * @return <code>true</code> if the event is compatible.
     */
    @SuppressWarnings("unused")
    private boolean verifyCompatibleEvent(int eventNum)
    {
        return (eventNum & COMPATIBLE_FIELDS) != 0;
    }
}
