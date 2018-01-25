package io.opensphere.wms.envoy;

import io.opensphere.wms.layer.WMSDataType;
import io.opensphere.wms.layer.WMSLayer;

/**
 * Creates a new WMSLayer.
 */
@FunctionalInterface
public interface WMSLayerCreator
{
    /**
     * Creates a new layer.
     *
     * @param dataType The data type representing the layer.
     * @param minimumDisplaySize The minimun display size.
     * @param maximumDisplaySize The maximum display size.
     * @return The WMS Layer.
     */
    WMSLayer createLayer(WMSDataType dataType, int minimumDisplaySize, int maximumDisplaySize);
}
