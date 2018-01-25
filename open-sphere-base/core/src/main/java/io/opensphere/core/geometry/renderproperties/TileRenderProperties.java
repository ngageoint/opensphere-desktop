package io.opensphere.core.geometry.renderproperties;

import java.awt.Color;

/** Render properties specific to tile geometries. */
public interface TileRenderProperties extends ColorRenderProperties
{
    /** Default tile color. */
    int DEFAULT_COLOR = 0xff000000;

    /** Default color used for highlighting. */
    int DEFAULT_HIGHLIGHT_COLOR = new Color(51, 51, 51, 255).getRGB();

    @Override
    TileRenderProperties clone();

    /**
     * Get the opacity.
     *
     * @return the opacity
     */
    float getOpacity();

    /**
     * Get the shaderProperties.
     *
     * @return the shaderProperties
     */
    FragmentShaderProperties getShaderProperties();

    /** Reset the shader properties to default behavior. */
    void resetShaderPropertiesToDefault();

    /**
     * Set the color of the tile. This color will be added to the texture color.
     * The alpha channel of this color will become the opacity of the tile.
     *
     * @param color The color of the tile as an ARGB packed int.
     */
    @Override
    void setColor(Color color);

    /**
     * Set the color of the tile. This color will be added to the texture color.
     * The alpha channel of the color will become the opacity of the tile.
     *
     * @param color The color of the tile as an ARGB packed int.
     */
    @Override
    void setColorARGB(int color);

    /**
     * Set the opacity. This will not change the RGB channels of the tile color.
     *
     * @param opacity the opacity to set
     */
    void setOpacity(float opacity);
}
