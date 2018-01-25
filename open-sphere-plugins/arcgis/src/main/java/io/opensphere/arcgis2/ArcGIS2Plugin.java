package io.opensphere.arcgis2;

import java.util.Collection;

import io.opensphere.arcgis2.controller.ArcGISQueryController;
import io.opensphere.arcgis2.envoy.ArcGISDescribeLayerEnvoy;
import io.opensphere.arcgis2.envoy.ArcGISLayerListEnvoy;
import io.opensphere.arcgis2.envoy.ArcRestEnvoy;
import io.opensphere.arcgis2.envoy.tile.ArcGISTileEnvoy;
import io.opensphere.arcgis2.mantle.ArcGISMantleController;
import io.opensphere.arcgis2.mantle.ArcGISToolbox;
import io.opensphere.arcgis2.server.ArcGISServerSourceController;
import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.adapter.AbstractServicePlugin;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.collections.New;
import io.opensphere.server.toolbox.ServerToolboxUtils;

/** The ArcGIS2 plugin. */
public class ArcGIS2Plugin extends AbstractServicePlugin
{
    /** The toolbox. */
    private Toolbox myToolbox;

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        myToolbox = toolbox;
        ServerToolboxUtils.getServerToolbox(toolbox).getServerSourceControllerManager()
                .setPreferencesTopic(ArcGISServerSourceController.class, ArcGIS2Plugin.class);
        super.initialize(plugindata, toolbox);
    }

    @Override
    public Collection<? extends Envoy> getEnvoys()
    {
        return New.list(new ArcGISLayerListEnvoy(myToolbox), new ArcGISDescribeLayerEnvoy(myToolbox),
                new ArcGISTileEnvoy(myToolbox), new ArcRestEnvoy(myToolbox));
    }

    @Override
    protected Collection<Service> getServices(PluginLoaderData plugindata, Toolbox toolbox)
    {
        ArcGISMantleController mantleController = new ArcGISMantleController(toolbox);
        toolbox.getPluginToolboxRegistry().registerPluginToolbox(new ArcGISToolbox(mantleController));

        return New.list(mantleController, new ArcGISQueryController(toolbox, mantleController));
    }
}
