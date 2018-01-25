package io.opensphere.osm;

import java.util.Collection;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.adapter.AbstractServicePlugin;
import io.opensphere.core.util.collections.New;
import io.opensphere.osm.envoy.OSMTileEnvoy;
import io.opensphere.osm.server.OSMServerSourceController;
import io.opensphere.server.toolbox.ServerToolboxUtils;

/** The OSM plugin. */
public class OSMPlugin extends AbstractServicePlugin
{
    /**
     * The system toolbox.
     */
    private Toolbox myToolbox;

    @Override
    public Collection<? extends Envoy> getEnvoys()
    {
        return New.list(new OSMTileEnvoy(myToolbox));
    }

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        myToolbox = toolbox;
        super.initialize(plugindata, toolbox);
        ServerToolboxUtils.getServerToolbox(toolbox).getServerSourceControllerManager()
                .setPreferencesTopic(OSMServerSourceController.class, OSMPlugin.class);
    }
}
