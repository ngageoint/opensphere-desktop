package io.opensphere.wms.state.model;

import io.opensphere.wms.envoy.WMSEnvoy;
import io.opensphere.wms.envoy.WMSLayerEnvoy;

/**
 * Contains the get capabilities envoy and the layer envoy it should be
 * associated with.
 */
public class WMSEnvoyAndLayerEnvoy
{
    /**
     * The get capabilities envoy.
     */
    private final WMSEnvoy myEnvoy;

    /**
     * The new layer envoy.
     */
    private final WMSLayerEnvoy myLayerEnvoy;

    /**
     * Constructs a new class.
     *
     * @param envoy The get capabilities envoy.
     * @param layerEnvoy The layer envoy.
     */
    public WMSEnvoyAndLayerEnvoy(WMSEnvoy envoy, WMSLayerEnvoy layerEnvoy)
    {
        myEnvoy = envoy;
        myLayerEnvoy = layerEnvoy;
    }

    /**
     * Gets the capabilities envoy.
     *
     * @return The envoy.
     */
    public WMSEnvoy getEnvoy()
    {
        return myEnvoy;
    }

    /**
     * Gets the layer envoy.
     *
     * @return The layer envoy.
     */
    public WMSLayerEnvoy getLayerEnvoy()
    {
        return myLayerEnvoy;
    }
}
