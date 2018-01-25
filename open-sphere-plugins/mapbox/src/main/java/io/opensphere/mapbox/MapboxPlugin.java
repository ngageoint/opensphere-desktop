package io.opensphere.mapbox;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.util.collections.New;
import io.opensphere.mapbox.envoy.MapboxLayersEnvoy;
import io.opensphere.mapbox.envoy.MapboxTileEnvoy;
import io.opensphere.mapbox.server.MapboxServerSourceController;
import io.opensphere.server.toolbox.ServerToolboxUtils;

/** Mapbox plugin. */
public class MapboxPlugin extends PluginAdapter
{
    /**
     * The set of active mapbox servers.
     */
    private final Set<String> myActiveUrls = Collections.synchronizedSet(New.set());

    /** The toolbox. */
    private Toolbox myToolbox;

    @Override
    public Collection<? extends Envoy> getEnvoys()
    {
        return New.list(new MapboxLayersEnvoy(myToolbox, myActiveUrls), new MapboxTileEnvoy(myToolbox, myActiveUrls));
    }

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        myToolbox = toolbox;
        super.initialize(plugindata, toolbox);
        ServerToolboxUtils.getServerToolbox(toolbox).getServerSourceControllerManager()
                .setPreferencesTopic(MapboxServerSourceController.class, MapboxPlugin.class);
    }
}
