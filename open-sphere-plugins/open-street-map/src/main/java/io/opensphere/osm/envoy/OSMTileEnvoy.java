package io.opensphere.osm.envoy;

import io.opensphere.core.Toolbox;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.osm.util.OSMUtil;
import io.opensphere.xyztile.envoy.XYZTileEnvoy;

/**
 * Envoy that goes out and gets tile images from a Mapbox server.
 */
public class OSMTileEnvoy extends XYZTileEnvoy
{
    /**
     * Constructs a new tile envoy for open street map.
     *
     * @param toolbox The system toolbox.
     */
    public OSMTileEnvoy(Toolbox toolbox)
    {
        super(toolbox);
    }

    @Override
    public boolean providesDataFor(DataModelCategory category)
    {
        return super.providesDataFor(category) && OSMUtil.PROVIDER.equals(category.getCategory());
    }

    @Override
    protected String buildImageUrlString(DataModelCategory category, ZYXImageKey key)
    {
        return OSMUtil.getInstance().buildImageUrlString(category.getSource(), key);
    }
}
