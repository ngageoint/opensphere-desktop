package io.opensphere.core.util;

import java.awt.Font;

/** Interface allowing grouping for all FontIcon enumerable classes. */
public interface FontIconEnum
{
    /**
     * Retrieves the font code.
     *
     * @return the font code
     */
    String getFontCode();

    /**
     * Retrieves the font.
     *
     * @return the font
     */
    Font getFont();
}
