package io.opensphere.mantle.data.tile;

import io.opensphere.mantle.data.VisualizationSupport;

/**
 * Base interface for a TileVisualizationSupport that will help the
 * visualization styles alter tile data that is going to be displayed on the
 * map.
 */
public interface TileVisualizationSupport extends VisualizationSupport
{
    /**
     * Gets the description.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();
}
