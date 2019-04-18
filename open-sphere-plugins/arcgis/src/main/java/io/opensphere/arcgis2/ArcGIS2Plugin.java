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
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;
import io.opensphere.server.toolbox.ServerToolboxUtils;

/** The ArcGIS2 plugin. */
public class ArcGIS2Plugin extends AbstractServicePlugin
{
    /** The toolbox through which application state is accessed. */
    private Toolbox myToolbox;

    /** The menu provider for ArcGIS layers. */
    private ContextMenuProvider<DataGroupContextKey> myContextMenuProvider;

    /**
     * {@inheritDoc}
     * 
     * @see io.opensphere.core.api.adapter.AbstractServicePlugin#initialize(io.opensphere.core.PluginLoaderData,
     *      io.opensphere.core.Toolbox)
     */
    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        myToolbox = toolbox;
        ServerToolboxUtils.getServerToolbox(toolbox).getServerSourceControllerManager()
                .setPreferencesTopic(ArcGISServerSourceController.class, ArcGIS2Plugin.class);

        ContextActionManager manager = toolbox.getUIRegistry().getContextActionManager();
        if (manager != null)
        {
            myContextMenuProvider = new ArcGISContextMenuProvider(toolbox);
            manager.registerContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT, DataGroupContextKey.class,
                    myContextMenuProvider);
        }

        super.initialize(plugindata, toolbox);
    }

    /**
     * {@inheritDoc}
     * 
     * @see io.opensphere.core.api.adapter.PluginAdapter#getEnvoys()
     */
    @Override
    public Collection<? extends Envoy> getEnvoys()
    {
        return New.list(new ArcGISLayerListEnvoy(myToolbox), new ArcGISDescribeLayerEnvoy(myToolbox),
                new ArcGISTileEnvoy(myToolbox), new ArcRestEnvoy(myToolbox));
    }

    /**
     * {@inheritDoc}
     * 
     * @see io.opensphere.core.api.adapter.AbstractServicePlugin#getServices(io.opensphere.core.PluginLoaderData,
     *      io.opensphere.core.Toolbox)
     */
    @Override
    protected Collection<Service> getServices(PluginLoaderData plugindata, Toolbox toolbox)
    {
        ArcGISMantleController mantleController = new ArcGISMantleController(toolbox);
        toolbox.getPluginToolboxRegistry().registerPluginToolbox(new ArcGISToolbox(mantleController));

        return New.list(mantleController, new ArcGISQueryController(toolbox, mantleController));
    }
}
