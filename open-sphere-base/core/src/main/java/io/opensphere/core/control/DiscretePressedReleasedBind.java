package io.opensphere.core.control;

import java.awt.event.KeyEvent;

/**
 * Internal binding used to translate key events to external bindings.
 */
class DiscretePressedReleasedBind extends PressedReleasedKeyBindAbs
{
    /**
     * Construct the binding from a default binding.
     *
     * @param binding The default binding.
     * @param listener The event listener.
     * @param context The control context.
     */
    public DiscretePressedReleasedBind(DefaultKeyPressedBinding binding, DiscreteEventListener listener,
            ControlContextImpl context)
    {
        this(binding.getKeyCode(), binding.getModifiersEx(), listener, context);
    }

    /**
     * Construct the binding.
     *
     * @param keyCode The key code.
     * @param listener The modifier bitmask.
     * @param context The control context.
     */
    public DiscretePressedReleasedBind(int keyCode, DiscreteEventListener listener, ControlContextImpl context)
    {
        this(keyCode, 0, listener, context);
    }

    /**
     * Construct the binding.
     *
     * @param keyCode The key code.
     * @param modifiersEx The modifier bitmask.
     * @param listener The event listener.
     * @param context The control context.
     */
    public DiscretePressedReleasedBind(int keyCode, int modifiersEx, DiscreteEventListener listener, ControlContextImpl context)
    {
        super(listener, context, keyCode, modifiersEx);
    }

    /**
     * Construct the binding from a key event.
     *
     * @param e The key event.
     */
    public DiscretePressedReleasedBind(KeyEvent e)
    {
        this(e.getKeyCode(), e.getModifiersEx(), null, null);
    }

    /**
     * Construct the binding from a key event.
     *
     * @param e The key event.
     * @param listener The listener.
     */
    public DiscretePressedReleasedBind(KeyEvent e, DiscreteEventListener listener)
    {
        this(e.getKeyCode(), e.getModifiersEx(), listener, null);
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        ((DiscreteEventListener)getListener()).eventOccurred(e);
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        // This method does nothing, since the listener is discrete. So, the
        // listener only takes the
        // single event. No need to do anything on key release.
    }
}
