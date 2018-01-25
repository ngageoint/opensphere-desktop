package io.opensphere.core.control;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

/** Default binding for a mouse wheel event. */
public class DefaultMouseWheelBinding extends DefaultMouseBinding
{
    /** The wheel direction, 1 or -1. */
    private final WheelDirection myWheelDirection;

    /**
     * Construct a default mouse wheel binding with no modifiers.
     *
     * @param wheelDirection The wheel direction that triggers the binding.
     */
    public DefaultMouseWheelBinding(WheelDirection wheelDirection)
    {
        super(MouseEvent.MOUSE_WHEEL);
        myWheelDirection = wheelDirection;
    }

    /**
     * Construct a default mouse wheel binding.
     *
     * @param wheelDirection The wheel direction that triggers the binding.
     * @param modifiersEx The modifier bitmask. (See {@link InputEvent})
     */
    public DefaultMouseWheelBinding(WheelDirection wheelDirection, int modifiersEx)
    {
        super(MouseEvent.MOUSE_WHEEL, modifiersEx);
        myWheelDirection = wheelDirection;
    }

    /**
     * Get the mouse wheel direction associated with this binding.
     *
     * @return The mouse wheel direction.
     */
    public WheelDirection getWheelDirection()
    {
        return myWheelDirection;
    }

    /** Enumeration of mouse wheel directions. */
    public enum WheelDirection
    {
        /** Mouse wheel up. */
        UP(-1),

        /** Mouse wheel down. */
        DOWN(1),

        ;

        /** An integer indicating the direction. */
        private final int myDirection;

        /**
         * Construct the enum constant.
         *
         * @param direction The direction.
         */
        WheelDirection(int direction)
        {
            myDirection = direction;
        }

        /**
         * Accessor for the direction.
         *
         * @return The direction.
         */
        public int getDirection()
        {
            return myDirection;
        }
    }
}
