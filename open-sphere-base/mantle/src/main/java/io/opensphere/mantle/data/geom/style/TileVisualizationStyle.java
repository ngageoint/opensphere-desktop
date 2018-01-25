package io.opensphere.mantle.data.geom.style;

import io.opensphere.core.geometry.renderproperties.TileRenderProperties;

/**
 * The Interface TileVisualizationStyle.
 */
public interface TileVisualizationStyle extends VisualizationStyle
{
    /**
     * Gets the shader resource specifier. Like: "/GLSL/DarkenBrighten.glsl".
     *
     * @return the resource locator.
     */
    String getShaderResourceLocation();

    /**
     * Install/update tile render properties.
     *
     * @param trp the {@link TileRenderProperties}
     */
    void updateTileRenderProperties(TileRenderProperties trp);
}
