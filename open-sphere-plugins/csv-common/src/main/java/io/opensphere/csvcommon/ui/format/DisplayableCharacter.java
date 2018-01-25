package io.opensphere.csvcommon.ui.format;

import io.opensphere.core.util.lang.HashCodeHelper;

/**
 * Wrapper for a character and display text.
 */
public class DisplayableCharacter
{
    /** Custom option. */
    public static final DisplayableCharacter CUSTOM = new DisplayableCharacter((char)1, "Custom");

    /** None option. */
    public static final DisplayableCharacter NONE = new DisplayableCharacter((char)0, "None");

    /** The character. */
    private final char myCharacter;

    /** The display text. */
    private final String myDisplayText;

    /**
     * Constructor.
     *
     * @param character the character
     */
    public DisplayableCharacter(char character)
    {
        this(character, String.valueOf(character));
    }

    /**
     * Constructor.
     *
     * @param character the character
     * @param displayText the display text
     */
    public DisplayableCharacter(char character, String displayText)
    {
        myCharacter = character;
        myDisplayText = displayText;
    }

    /**
     * Gets the character.
     *
     * @return the character
     */
    public char charValue()
    {
        return myCharacter;
    }

    /**
     * Returns the Character representation of this value.
     *
     * @return the Character representation
     */
    public Character toCharacter()
    {
        return this == DisplayableCharacter.NONE ? null : Character.valueOf(myCharacter);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myCharacter);
        return result;
    }

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
        DisplayableCharacter other = (DisplayableCharacter)obj;
        return myCharacter == other.myCharacter;
    }

    @Override
    public String toString()
    {
        return myDisplayText;
    }
}
