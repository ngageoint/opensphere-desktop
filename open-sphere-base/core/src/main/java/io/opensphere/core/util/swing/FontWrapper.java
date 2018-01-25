package io.opensphere.core.util.swing;

import java.awt.Font;

import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.HashCodeHelper;

/**
 * Wrapper class for Font to allow it in a combo box.
 */
public class FontWrapper
{
    /** The font. */
    private final Font myFont;

    /**
     * Constructor.
     *
     * @param font The font
     */
    public FontWrapper(Font font)
    {
        myFont = font;
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
        FontWrapper other = (FontWrapper)obj;
        return EqualsHelper.equals(myFont.getName(), other.getFont().getName());
    }

    /**
     * Getter for font.
     *
     * @return the font
     */
    public Font getFont()
    {
        return myFont;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myFont.getName());
        return result;
    }

    @Override
    public String toString()
    {
        return myFont.getName();
    }
}
