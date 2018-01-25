package io.opensphere.core.geometry.renderproperties;

import java.awt.Color;

/** Render properties specific to geometries used for visualization. */
public interface ScalableRenderProperties extends BaseAltitudeRenderProperties, PolygonMeshRenderProperties
{
    /**
     * Clone this set of render properties.
     *
     * @return The cloned render properties.
     */
    @Override
    ScalableRenderProperties clone();

    /**
     * Get the color at base.
     *
     * @return The color at the base.
     */
    Color getBaseColor();

    /**
     * Get the width.
     *
     * @return the width
     */
    float getWidth();

    /**
     * Set the color at base.
     *
     * @param baseColor The color at the base.
     */
    void setBaseColor(Color baseColor);

    /**
     * Set the width.
     *
     * @param width the width to set
     */
    void setWidth(float width);
}
