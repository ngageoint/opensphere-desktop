package io.opensphere.wms.envoy;

import io.opensphere.wms.layer.WMSLayerValueProvider;

/**
 * Interface to a WMS layer envoy.
 *
 */
@FunctionalInterface
public interface WMSLayerEnvoy
{
    /**
     * Gets the layer value provider for the layer contained in the envoy.
     *
     * @return The layer value provider.
     */
    WMSLayerValueProvider getLayer();
}
