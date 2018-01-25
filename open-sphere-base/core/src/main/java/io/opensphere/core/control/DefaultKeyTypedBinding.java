package io.opensphere.core.control;

import java.awt.event.InputEvent;

/** Default binding for a key typed. */
public class DefaultKeyTypedBinding implements DefaultBinding
{
    /** The key character that triggers this binding. */
    private final char myKeyChar;

    /** The modifier bitmask that triggers this binding. */
    private final int myModifiersEx;

    /**
     * Construct a key typed binding with a key character and no modifiers.
     *
     * @param keyChar The key character.
     */
    public DefaultKeyTypedBinding(char keyChar)
    {
        this(keyChar, 0);
    }

    /**
     * Construct a key typed binding with a key character and modifier bitmask.
     *
     * @param keyChar The key character.
     * @param modifiersEx The modifier bitmask, to match those in
     *            {@link InputEvent}.
     */
    public DefaultKeyTypedBinding(char keyChar, int modifiersEx)
    {
        myKeyChar = keyChar;
        myModifiersEx = modifiersEx;
    }

    /**
     * Get the key character that triggers the binding.
     *
     * @return The key character.
     */
    public char getKeyChar()
    {
        return myKeyChar;
    }

    /**
     * Get the modifier bitmask that triggers the binding.
     *
     * @return The modifier bitmask.
     */
    public int getModifiersEx()
    {
        return myModifiersEx;
    }
}
