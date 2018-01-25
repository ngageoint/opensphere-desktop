package io.opensphere.arcgis2.envoy.tile;

import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.model.ZYXImageKey;

/**
 * Interface to an object that knows how to build the appropriate get url in
 * order to receive imagery for the given layer and tile.
 */
public interface TileUrlBuilder
{
    /**
     * Builds the url for the given layer and tile.
     *
     * @param category The layer to retrieve imagery for.
     * @param key The tile to retrieve imagery for.
     * @return The get url to use in order to retrieve imagery.
     */
    String buildUrl(DataModelCategory category, ZYXImageKey key);
}
