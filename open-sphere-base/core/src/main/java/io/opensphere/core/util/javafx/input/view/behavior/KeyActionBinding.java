package io.opensphere.core.util.javafx.input.view.behavior;

import java.util.HashSet;
import java.util.Set;

import javafx.scene.input.KeyCode;

/**
 * A binding between a key event and an action.
 */
public class KeyActionBinding
{
    /**
     * The set of modifier key codes bound with the action. These include Shift,
     * Control, Alt, Meta, Command, etc.
     */
    private final Set<KeyCode> myModifiers;

    /**
     * The code of the key to which the action is bound.
     */
    private final KeyCode myCode;

    /**
     * The action bound to the key code.
     */
    private final String myAction;

    /**
     * Creates a new binding between the key code and the action. The binding
     * will not include any of the modifier keys (shift, control, alt, or meta).
     *
     * @param pCode the code of the key to which the action is bound.
     * @param pAction the action to bind to the key code.
     * @param pModifiers the set of modifier keys to apply to the binding.
     * @throws IllegalArgumentException if one or more of the supplied modifiers
     *             is represented by a {@link KeyCode} that returns false for a
     *             call to the {@link KeyCode#isModifierKey()} method.
     */
    public KeyActionBinding(KeyCode pCode, String pAction, KeyCode... pModifiers)
    {
        myCode = pCode;
        myAction = pAction;
        myModifiers = new HashSet<>();
        if (pModifiers != null)
        {
            for (KeyCode modifier : pModifiers)
            {
                if (!modifier.isModifierKey())
                {
                    throw new IllegalArgumentException(
                            "The key binding supplied as a modifier (" + modifier.getName() + " is not a modifier key!");
                }
                myModifiers.add(modifier);
            }
        }
    }

    /**
     * Gets the value of the {@link #myAction} field.
     *
     * @return the value stored in the {@link #myAction} field.
     */
    public String getAction()
    {
        return myAction;
    }

    /**
     * Gets the value of the {@link #myCode} field.
     *
     * @return the value stored in the {@link #myCode} field.
     */
    public KeyCode getCode()
    {
        return myCode;
    }

    /**
     * Tests to determine if the Alt key is included as part of the action
     * binding.
     *
     * @return true if the Alt key is included as part of the binding.
     */
    public boolean isAltBound()
    {
        return myModifiers.contains(KeyCode.ALT);
    }

    /**
     * Tests to determine if the Ctrl key is included as part of the action
     * binding.
     *
     * @return true if the Alt key is included as part of the binding.
     */
    public boolean isControlRequired()
    {
        return myModifiers.contains(KeyCode.CONTROL);
    }

    /**
     * Tests to determine if the Meta key is included as part of the action
     * binding.
     *
     * @return true if the Alt key is included as part of the binding.
     */
    public boolean isMetaRequired()
    {
        return myModifiers.contains(KeyCode.META);
    }

    /**
     * Tests to determine if the Shift key is included as part of the action
     * binding.
     *
     * @return true if the Alt key is included as part of the binding.
     */
    public boolean isShiftRequired()
    {
        return myModifiers.contains(KeyCode.SHIFT);
    }

    /**
     * Gets the value of the {@link #myModifiers} field.
     *
     * @return the value stored in the {@link #myModifiers} field.
     */
    public Set<KeyCode> getModifiers()
    {
        return myModifiers;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(45);
        for (KeyCode modifier : myModifiers)
        {
            builder.append(modifier.getName());
            builder.append('+');
        }

        builder.append(myCode.getName());
        builder.append("=>'");
        builder.append(myAction);
        builder.append('\'');

        return builder.toString();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((myAction == null) ? 0 : myAction.hashCode());
        result = prime * result + ((myCode == null) ? 0 : myCode.hashCode());
        result = prime * result + ((myModifiers == null) ? 0 : myModifiers.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        KeyActionBinding other = (KeyActionBinding)obj;
        if (myAction == null)
        {
            if (other.myAction != null)
            {
                return false;
            }
        }
        else if (!myAction.equals(other.myAction) || myCode != other.myCode)
        {
            return false;
        }

        if (myModifiers == null)
        {
            if (other.myModifiers != null)
            {
                return false;
            }
        }
        else if (!myModifiers.equals(other.myModifiers))
        {
            return false;
        }
        return true;
    }
}
