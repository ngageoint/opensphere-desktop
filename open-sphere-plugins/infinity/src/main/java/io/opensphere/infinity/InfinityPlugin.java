package io.opensphere.infinity;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.adapter.AbstractServicePlugin;
import io.opensphere.core.util.Service;
import io.opensphere.infinity.envoy.InfinityEnvoy;

/** Infinity (Elasticsearch) plugin. */
public class InfinityPlugin extends AbstractServicePlugin
{
    /** The toolbox. */
    private Toolbox myToolbox;

    @Override
    public Collection<? extends Envoy> getEnvoys()
    {
        return List.of(new InfinityEnvoy(myToolbox));
    }

    @Override
    protected Collection<Service> getServices(PluginLoaderData plugindata, Toolbox toolbox)
    {
        myToolbox = toolbox;
        return List.of(new InfinityLayerController(toolbox));
    }
}
