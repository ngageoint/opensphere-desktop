package io.opensphere.wms.envoy;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Weeks;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.wms.config.v1.WMSLayerConfig;
import io.opensphere.wms.layer.WMSDataType;
import io.opensphere.wms.layer.WMSDataTypeInfo;
import io.opensphere.wms.layer.WMSLayer;

/**
 * Creates a WMS layer.
 *
 */
public class WMSLayerCreatorImpl implements WMSLayerCreator
{
    /**
     * The instance of this class.
     */
    private static WMSLayerCreatorImpl ourInstance = new WMSLayerCreatorImpl();

    /**
     * Gets the instance of this class.
     *
     * @return The instance of this class.
     */
    public static WMSLayerCreatorImpl getInstance()
    {
        return ourInstance;
    }

    @Override
    public WMSLayer createLayer(WMSDataType wmsDataType, int minimumDisplaySize, int maximumDisplaySize)
    {
        WMSLayer newLayer = null;

        if (wmsDataType instanceof WMSDataTypeInfo)
        {
            WMSDataTypeInfo dataType = (WMSDataTypeInfo)wmsDataType;

            WMSLayerConfig layerConfig = dataType.getWmsConfig().getLayerConfig();
            int resolveLevels = layerConfig.getDisplayConfig().getResolveLevels().intValue();
            final double largestTileSize = layerConfig.getDisplayConfig().getLargestTileSize();
            WMSLayer.Builder builder = new WMSLayer.Builder(dataType);
            builder.setLayerCellDimensions(WMSLayer.createQuadSplitLayerCellDimensions(resolveLevels, largestTileSize));
            builder.setMinimumDisplaySize(minimumDisplaySize);
            builder.setMaximumDisplaySize(maximumDisplaySize);

            DataRegistry dataRegistry = dataType.getToolbox().getDataRegistry();
            // All current terrain layer types need to read the original input stream from the image
            if (layerConfig.getLayerType() != null
                    && layerConfig.getLayerType().getMapVisualizationType() == MapVisualizationType.TERRAIN_TILE)
            {
                int height = layerConfig.getGetMapConfig().getTextureHeight().intValue();
                int width = layerConfig.getGetMapConfig().getTextureWidth().intValue();
                builder.setImageProvider(new LayerStreamingImageProvider(layerConfig.getLayerName(), layerConfig.getLayerKey(),
                        layerConfig.getGetMapConfig().getUsableGetMapURL(), height, width, dataRegistry));
            }
            else
            {
                builder.setImageProvider(new LayerImageProvider(layerConfig.getLayerName(), layerConfig.getLayerKey(),
                        layerConfig.getGetMapConfig().getUsableGetMapURL(), dataRegistry));
            }

            if (builder.getDataTypeInfo().getWmsConfig().getLayerConfig().getTimeExtent() != TimeSpan.TIMELESS)
            {
                builder.setValidDuration(Weeks.ONE);
            }
            else
            {
                builder.setValidDuration(new Weeks(12));
            }

            newLayer = new WMSLayer(builder);
        }

        return newLayer;
    }
}
