package io.opensphere.core.control;

import java.awt.event.InputEvent;

/**
 * A default binding for a key press.
 */
public class DefaultKeyPressedBinding implements DefaultBinding
{
    /** The key code that triggers this binding. */
    private final int myKeyCode;

    /** The modifier bitmask that triggers this binding. */
    private final int myModifiersEx;

    /**
     * Construct the binding.
     *
     * @param keyCode The key code that triggers the binding.
     */
    public DefaultKeyPressedBinding(int keyCode)
    {
        this(keyCode, 0);
    }

    /**
     * Construct the binding.
     *
     * @param keyCode The key code that triggers the binding.
     * @param modifiersEx The modifier bitmask, to match those in
     *            {@link InputEvent}.
     */
    public DefaultKeyPressedBinding(int keyCode, int modifiersEx)
    {
        myKeyCode = keyCode;
        myModifiersEx = modifiersEx;
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
        DefaultKeyPressedBinding other = (DefaultKeyPressedBinding)obj;
        if (myKeyCode != other.myKeyCode)
        {
            return false;
        }
        if (myModifiersEx != other.myModifiersEx)
        {
            return false;
        }
        return true;
    }

    /**
     * Get the key code that triggers the binding.
     *
     * @return The key code.
     */
    public int getKeyCode()
    {
        return myKeyCode;
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

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + myKeyCode;
        result = prime * result + myModifiersEx;
        return result;
    }
}
