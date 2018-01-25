package io.opensphere.core.geometry.renderproperties;

import java.awt.Color;

/** Render properties for geometries which are colored. */
public interface ColorRenderProperties extends BaseRenderProperties
{
    /** Default color used for highlighting. */
    int DEFAULT_HIGHLIGHT_COLOR = new Color(1f, 1f, 1f, .5f).getRGB();

    @Override
    ColorRenderProperties clone();

    /**
     * Get the color.
     *
     * @return the color
     */
    Color getColor();

    /**
     * Get the color as an ARGB-packed int.
     *
     * @return The color.
     */
    int getColorARGB();

    /**
     * Get the color to use when the geometry is highlighted.
     *
     * @return the color
     */
    Color getHighlightColor();

    /**
     * Get the color to use when the geometry is highlighted as an ARGB-packed
     * int.
     *
     * @return The color.
     */
    int getHighlightColorARGB();

    /**
     * Set the color to the current color with a different opacity.
     *
     * @param opacity The opacity.
     */
    void opacitizeColor(float opacity);

    /**
     * Set the color.
     *
     * @param color the color to set
     */
    void setColor(Color color);

    /**
     * Set the color as an ARGB-packed int.
     *
     * @param color the color to set
     */
    void setColorARGB(int color);

    /**
     * Set the color to use when the geometry is highlighted.
     *
     * @param color the color to set
     */
    void setHighlightColor(Color color);

    /**
     * Set the color to use when the geometry is highlighted as an ARGB-packed
     * int.
     *
     * @param color the color to set
     */
    void setHighlightColorARGB(int color);
}
