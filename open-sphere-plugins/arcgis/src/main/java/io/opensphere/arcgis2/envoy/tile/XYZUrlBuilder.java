package io.opensphere.arcgis2.envoy.tile;

import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * The URL builder that builds the appropriate url to get tile imagery for a
 * layer that has a tile cache.
 */
public class XYZUrlBuilder implements TileUrlBuilder
{
    @Override
    public String buildUrl(DataModelCategory category, ZYXImageKey key)
    {
        String layerUrl = category.getCategory();
        String parentUrl = layerUrl.substring(0, layerUrl.lastIndexOf('/'));
        String urlString = StringUtilities.concat(parentUrl, "/tile/", Integer.valueOf(key.getZ()), "/",
                Integer.valueOf(key.getY()), "/", Integer.valueOf(key.getX()));
        return urlString;
    }
}
