package io.opensphere.core.control;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

/** Default binding for a mouse event. */
public class DefaultMouseBinding implements DefaultBinding
{
    /** The modifier bitmask that triggers the binding. */
    private final int myModifiersEx;

    /** The mouse event code that triggers the binding. */
    private final int myMouseEvent;

    /**
     * Construct a default mouse binding with mouse event code and no modifiers.
     *
     * @param mouseEvent The mouse event code. (See {@link MouseEvent})
     */
    public DefaultMouseBinding(int mouseEvent)
    {
        this(mouseEvent, 0);
    }

    /**
     * Construct a default mouse binding with a mouse event code and modifier
     * bitmask.
     *
     * @param mouseEvent The mouse event code. (See {@link MouseEvent})
     * @param modifiersEx The modifier bitmask. (See {@link InputEvent})
     */
    public DefaultMouseBinding(int mouseEvent, int modifiersEx)
    {
        if (mouseEvent == MouseEvent.MOUSE_WHEEL && this.getClass() == DefaultMouseBinding.class)
        {
            throw new IllegalArgumentException("Default Mouse Wheel Events need to be defined using the"
                    + "DefaultMouseWheelBinding class, not the DefaultMouseBinding class.");
        }
        myModifiersEx = modifiersEx;
        myMouseEvent = mouseEvent;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        DefaultMouseBinding other = (DefaultMouseBinding)obj;
        if (myModifiersEx != other.myModifiersEx)
        {
            return false;
        }
        if (myMouseEvent != other.myMouseEvent)
        {
            return false;
        }
        return true;
    }

    /**
     * Get the modifier bitmask for the event.
     *
     * @return The modifier bitmask.
     */
    public int getModifiersEx()
    {
        return myModifiersEx;
    }

    /**
     * Get the mouse event code.
     *
     * @return The mouse event code.
     * @see MouseEvent
     */
    public int getMouseEvent()
    {
        return myMouseEvent;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + myModifiersEx;
        result = prime * result + myMouseEvent;
        return result;
    }
}
