package io.opensphere.wms;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.UnserializableAccessor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.MimeType;
import io.opensphere.wms.config.v1.WMSLayerConfig;
import io.opensphere.wms.config.v1.WMSLayerConfigurationSet;
import io.opensphere.wms.layer.WMSDataTypeInfo;
import io.opensphere.wms.layer.WMSLayer;

/**
 * This class handles a single image file that covers the entire earth.
 */
public class SingleFileWMSEnvoy extends FileWMSEnvoy
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(SingleFileWMSEnvoy.class);

    /**
     * Construct the envoy.
     *
     * @param toolbox The toolbox.
     */
    public SingleFileWMSEnvoy(Toolbox toolbox)
    {
        super(toolbox);
    }

    @Override
    public void open()
    {
        // TODO configuration should be read, not created
        WMSLayerConfig conf = new WMSLayerConfig();
        conf.setLayerTitle("BlueMarble");
        conf.setTimeExtent(TimeSpan.TIMELESS);
        String layerName = FileWMSLayer.class.getName() + ":" + getBaseDir();
        conf.setLayerName(layerName);
        conf.setLayerKey(layerName);
        conf.setCacheImageFormat(MimeType.DDS.getMimeType());

        WMSDataTypeInfo info = new WMSDataTypeInfo(getToolbox(),
                getToolbox().getPreferencesRegistry().getPreferences(WMSPlugin.class), "SingleFileWMS",
                new WMSLayerConfigurationSet(null, conf, null), conf.getLayerKey(), conf.getLayerTitle());
        FileWMSLayer.Builder builder = new FileWMSLayer.Builder(info);
        builder.setLRCImageProvider(getFileSystemImageProvider());
        // We are a single layer that encompasses the entire earth.
        builder.setLayerCellDimensions(createSingleLayerCellDimensions(180, 360));
        WMSLayer blueMarbleLayer = new FileWMSLayer(builder);

        String source = toString();
        String family = WMSLayer.class.getName();
        String category = "";
        Date expiration = CacheDeposit.SESSION_END;

        Collection<? extends PropertyAccessor<WMSLayer, ?>> accessors = Collections
                .singleton(UnserializableAccessor.getHomogeneousAccessor(WMSLayer.PROPERTY_DESCRIPTOR));
        CacheDeposit<WMSLayer> deposit = new DefaultCacheDeposit<WMSLayer>(new DataModelCategory(source, family, category),
                accessors, Collections.singleton(blueMarbleLayer), true, expiration, true);
        getDataRegistry().addModels(deposit);
    }

    /**
     * Create a list of lat/lon coordinates that define how large the cell is in
     * the single layer's grid. (list will contain only one set of coordinates)
     *
     * @param latSizeDeg The size of the latitude grid size (degrees)
     * @param lonSizeDeg The size of the longitude grid size (degrees)
     * @return LatLonAlt - The size of single layer described by parameters.
     */
    protected List<Vector2d> createSingleLayerCellDimensions(float latSizeDeg, float lonSizeDeg)
    {
        List<Vector2d> layerCellDimensions = new ArrayList<>();
        layerCellDimensions.add(new Vector2d(lonSizeDeg, latSizeDeg));
        return layerCellDimensions;
    }

    @Override
    protected URL getImageURL(LevelRowCol coords)
    {
        // TODO: n.a.a. rather than hard-code this, make file name configurable
        String fileName = "/images/BMNG_world.topo.bathy.200405.3.2048x1024.jpg";
        URL url = getClass().getResource(fileName);

        if (url == null)
        {
            LOGGER.error("The single BMNG file " + fileName + " could not be found.  Check that it is in the classpath");
            return null;
        }

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("The path to BMNG file = " + url);
        }
        return url;
    }
}
