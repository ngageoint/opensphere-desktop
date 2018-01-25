package io.opensphere.mapbox.envoy;

import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.xyztile.envoy.XYZTileEnvoy;

/**
 * Envoy that goes out and gets tile images from a Mapbox server.
 */
public class MapboxTileEnvoy extends XYZTileEnvoy
{
    /**
     * The set of active mapbox urls.
     */
    private final Set<String> myActiveUrls;

    /**
     * Constructs a new tile envoy for map box.
     *
     * @param toolbox The system toolbox.
     * @param availableServers The set of active mapbox servers.
     */
    public MapboxTileEnvoy(Toolbox toolbox, Set<String> availableServers)
    {
        super(toolbox);
        myActiveUrls = availableServers;
    }

    @Override
    public boolean providesDataFor(DataModelCategory category)
    {
        return super.providesDataFor(category) && myActiveUrls.contains(category.getSource());
    }

    @Override
    protected String buildImageUrlString(DataModelCategory category, ZYXImageKey key)
    {
        String urlString = StringUtilities.concat(category.getSource(), "/v4/", category.getCategory(), "/",
                Integer.valueOf(key.getZ()), "/", Integer.valueOf(key.getX()), "/", Integer.valueOf(key.getY()), "@2x.png");
        return urlString;
    }
}
