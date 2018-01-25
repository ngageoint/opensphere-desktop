package io.opensphere.core.control;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Internal binding used to translate key typed events to external bindings.
 */
class KeyTypedBind extends KeyBindingAbs
{
    /** The key character that activates the binding. */
    private final char myKeyChar;

    /**
     * Construct the binding.
     *
     * @param keyChar The key character that activates the binding.
     * @param listener The listener.
     * @param context The control context.
     */
    public KeyTypedBind(char keyChar, DiscreteEventListener listener, ControlContext context)
    {
        this(keyChar, 0, listener, context);
    }

    /**
     * Construct the binding.
     *
     * @param keyChar The key character that activates the binding.
     * @param modifiersEx The modifier bitmask that activates the binding. (See
     *            {@link InputEvent})
     * @param listener The listener.
     * @param context The control context.
     */
    public KeyTypedBind(char keyChar, int modifiersEx, DiscreteEventListener listener, ControlContext context)
    {
        super(modifiersEx, listener, context);
        myKeyChar = keyChar;
    }

    /**
     * Construct the binding from an external binding.
     *
     * @param defaultBinding The external binding.
     * @param listener The listener.
     * @param context The control context.
     */
    public KeyTypedBind(DefaultKeyTypedBinding defaultBinding, DiscreteEventListener listener, ControlContext context)
    {
        this(defaultBinding.getKeyChar(), defaultBinding.getModifiersEx(), listener, context);
    }

    /**
     * Construct the binding from a key event.
     *
     * @param e The key event.
     * @param listener The listener.
     */
    public KeyTypedBind(KeyEvent e, DiscreteEventListener listener)
    {
        this(e.getKeyChar(), e.getModifiersEx(), listener, null);
    }

    @Override
    public DiscreteEventListener getListener()
    {
        return (DiscreteEventListener)super.getListener();
    }

    /**
     * Method called when a key is typed.
     *
     * @param e The key event.
     */
    public void keyTyped(KeyEvent e)
    {
        getListener().eventOccurred(e);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (getModifiersEx() != 0)
        {
            sb.append(InputEvent.getModifiersExText(getModifiersEx()));
            sb.append('+');
        }
        sb.append(myKeyChar);
        return sb.toString();
    }

    /**
     * Get the key character.
     *
     * @return The key character.
     */
    char getKeyChar()
    {
        return myKeyChar;
    }
}
