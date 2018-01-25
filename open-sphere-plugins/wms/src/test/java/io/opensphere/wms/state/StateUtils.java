package io.opensphere.wms.state;

import org.easymock.EasyMock;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.wms.envoy.WMSEnvoy;
import io.opensphere.wms.envoy.WMSLayerEnvoy;
import io.opensphere.wms.layer.WMSLayerValueProvider;
import io.opensphere.wms.state.activate.controllers.WMSLayerEnvoyAndEnvoy;
import io.opensphere.wms.state.model.WMSEnvoyAndLayerEnvoy;

/**
 * Contains utility test methods.
 *
 */
public final class StateUtils
{
    /**
     * Creates the envoy.
     *
     * @return The envoy.
     */
    public static WMSEnvoy createEnvoy()
    {
        WMSEnvoy envoy = EasyMock.createMock(WMSEnvoy.class);

        return envoy;
    }

    /**
     * Creates the envoy and layer envoy.
     *
     * @param envoy The envoy.
     * @param layerEnvoy The layer envoy.
     * @return The envoy and layer envoy.
     */
    public static WMSEnvoyAndLayerEnvoy createEnvoyAndLayerEnvoy(WMSEnvoy envoy, WMSLayerEnvoy layerEnvoy)
    {
        WMSEnvoyAndLayerEnvoy envoyAndLayer = new WMSEnvoyAndLayerEnvoy(envoy, layerEnvoy);

        return envoyAndLayer;
    }

    /**
     * Creates the layer envoy.
     *
     * @param layer The layer.
     * @return The layer envoy.
     */
    public static WMSLayerEnvoy createLayerEnvoy(WMSLayerValueProvider layer)
    {
        WMSLayerEnvoy layerEnvoy = EasyMock.createMock(WMSLayerEnvoyAndEnvoy.class);

        return layerEnvoy;
    }

    /**
     * Creates the toolbox.
     *
     * @param toolboxRegistry The toolbox registry.
     * @return The toolbox.
     */
    public static Toolbox createToolbox(PluginToolboxRegistry toolboxRegistry)
    {
        Toolbox toolbox = EasyMock.createMock(Toolbox.class);
        toolbox.getPluginToolboxRegistry();
        EasyMock.expectLastCall().andReturn(toolboxRegistry);

        return toolbox;
    }

    /**
     * Not constructible.
     */
    private StateUtils()
    {
    }
}
