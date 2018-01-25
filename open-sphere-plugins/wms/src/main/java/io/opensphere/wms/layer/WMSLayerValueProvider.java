package io.opensphere.wms.layer;

import java.util.List;

import io.opensphere.core.math.Vector2d;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.wms.config.v1.WMSLayerConfig;

/**
 * Provides certain values from a WMSLayer.
 */
public interface WMSLayerValueProvider
{
    /**
     * Gets the configuration for the layer.
     *
     * @return The configuration.
     */
    WMSLayerConfig getConfiguration();

    /**
     * Gets the layer associated with the data type.
     *
     * @return The wms layer.
     */
    WMSLayer getLayer();

    /**
     * Get the list containing the cell dimensions of each level of detail,
     * starting with level 0.
     *
     * @return The cell dimensions.
     */
    List<Vector2d> getLayerCellDimensions();

    /**
     * Get the approximate maximum number of pixels this geometry should occupy
     * before it is split.
     *
     * @return The maximum number of pixels before split.
     */
    int getMaximumDisplaySize();

    /**
     * Get the approximate minimum number of pixels this geometry should occupy
     * before it is joined.
     *
     * @return The minimum number of pixels before split.
     */
    int getMinimumDisplaySize();

    /**
     * Gets the data type info representing the layer.
     *
     * @return The layer's data type info.
     */
    DataTypeInfo getTypeInfo();
}
