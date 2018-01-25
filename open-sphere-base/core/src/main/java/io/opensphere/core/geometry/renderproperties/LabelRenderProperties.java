package io.opensphere.core.geometry.renderproperties;

import java.awt.Color;
import java.util.function.Function;

import io.opensphere.core.units.length.Kilometers;

/** Render properties for label geometries. */
public interface LabelRenderProperties extends ColorRenderProperties
{
    /**
     * Clone this set of render properties.
     *
     * @return The cloned render properties.
     */
    @Override
    LabelRenderProperties clone();

    /**
     * Get the font for the label.
     *
     * @return The font.
     */
    String getFont();

    /**
     * Gets the scale function.
     *
     * @return the scale function, or null
     */
    Function<Kilometers, Float> getScaleFunction();

    /**
     * Get the shadow color.
     *
     * @return The shadow color, or {@code null} if there is none.
     */
    Color getShadowColor();

    /**
     * Get the shadow color.
     *
     * @return The shadow color in ARGB bytes. Zero indicates no shadow.
     */
    int getShadowColorARGB();

    /**
     * Get the shadow offset X.
     *
     * @return The shadow offset X coordinate, positive being toward the right
     *         of the screen.
     */
    float getShadowOffsetX();

    /**
     * Get the shadow offset Y.
     *
     * @return The shadow offset Y coordinate, positive being toward the top of
     *         the screen.
     */
    float getShadowOffsetY();

    /**
     * Set the font for the label.
     *
     * @param font The font.
     */
    void setFont(String font);

    /**
     * Sets the scale function. The first argument is the viewer altitude, the
     * return value is the scale.
     *
     * @param scaleFunction the scale function
     */
    void setScaleFunction(Function<Kilometers, Float> scaleFunction);

    /**
     * Set the shadow color.
     *
     * @param color The shadow color, which may be {@code null}.
     */
    void setShadowColor(Color color);

    /**
     * Set the shadow color in ARGB bytes. A value of 0 indicates no shadow.
     *
     * @param color The color.
     */
    void setShadowColorARGB(int color);

    /**
     * Set the shadow offset.
     *
     * @param x The horizontal coordinate, right being positive.
     * @param y The vertical coordinate, up being positive.
     */
    void setShadowOffset(float x, float y);
}
