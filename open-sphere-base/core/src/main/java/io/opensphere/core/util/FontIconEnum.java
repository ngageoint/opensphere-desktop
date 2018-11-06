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

    /**
     * A Y offset, in pixels, of the font glyph. Allows for corrections of
     * specific icons. May be zero.
     *
     * @return a drawing offset in the y direction. Positive values indicate a
     *         downward direction, negative up.
     */
    float getYDrawingOffset();

    /**
     * An X offset, in pixels, of the font glyph. Allows for corrections of
     * specific icons. May be zero.
     *
     * @return a drawing offset in the y direction. Positive values indicate a
     *         rightward direction, negative leftward.
     */
    float getXDrawingOffset();

    String getGlyphName();
}
