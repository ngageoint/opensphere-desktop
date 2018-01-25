package io.opensphere.core.control;

import java.awt.event.InputEvent;

/**
 * Internal binding used to translate key typed events to external bindings.
 */
class KeyBindingAbs implements Binding
{
    /** The listener. */
    private final BoundEventListener myListener;

    /** The control context. */
    private final ControlContext myContext;

    /** The modifier bitmask. */
    private final int myModifiersEx;

    /**
     * Construct the binding.
     *
     * @param modifiersEx The modifier bitmask that activates the binding. (See
     *            {@link InputEvent})
     * @param listener The listener.
     * @param context The control context.
     */
    public KeyBindingAbs(int modifiersEx, BoundEventListener listener, ControlContext context)
    {
        myModifiersEx = modifiersEx;
        myListener = listener;
        myContext = context;
    }

    @Override
    public BoundEventListener getListener()
    {
        return myListener;
    }

    /**
     * Get the control context.
     *
     * @return The control context.
     */
    ControlContext getContext()
    {
        return myContext;
    }

    /**
     * Get the modifier bitmask.
     *
     * @return The modifier bitmask.
     */
    int getModifiersEx()
    {
        return myModifiersEx;
    }
}
