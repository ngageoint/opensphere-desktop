package io.opensphere.arcgis2.envoy.tile;

import java.util.Date;

import org.apache.log4j.Logger;

import io.opensphere.arcgis2.model.ArcGISDataGroupInfo;
import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.xyztile.envoy.XYZTileEnvoy;

/**
 * Envoy that goes out and gets tile images from a ArcGIS server.
 */
public class ArcGISTileEnvoy extends XYZTileEnvoy
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ArcGISTileEnvoy.class);

    /**
     * Gets the {@link ArcGISDataGroupInfo} that we are trying to download tile
     * images for.
     */
    private final ArcGISDataGroupProvider myProvider;

    /**
     * Constructs a new tile envoy for map box.
     *
     * @param toolbox The system toolbox.
     */
    public ArcGISTileEnvoy(Toolbox toolbox)
    {
        super(toolbox);
        myProvider = new ArcGISDataGroupProvider(MantleToolboxUtils.getMantleToolbox(toolbox).getDataGroupController());
    }

    @Override
    public boolean providesDataFor(DataModelCategory category)
    {
        return super.providesDataFor(category) && category.getSource() != null
                && category.getSource().toLowerCase().contains("/rest/services");
    }

    @Override
    protected String buildImageUrlString(DataModelCategory category, ZYXImageKey key)
    {
        String urlString = null;
        ArcGISDataGroupInfo group = myProvider.getDataGroup(category);
        if (group != null)
        {
            TileUrlBuilder builder = TileUrlBuilderFactory.getInstance().createBuilder(group.getLayer());
            urlString = builder.buildUrl(category, key);
        }
        else
        {
            LOGGER.error("Unable to find data group for category " + category);
        }
        return urlString;
    }

    @Override
    protected Date getExpirationTime(DataModelCategory category)
    {
        ArcGISDataGroupInfo group = myProvider.getDataGroup(category);

        Date expiration = super.getExpirationTime(category);

        if (group != null && !group.getLayer().isSingleFusedMapCache())
        {
            expiration = CacheDeposit.SESSION_END;
        }

        return expiration;
    }
}
