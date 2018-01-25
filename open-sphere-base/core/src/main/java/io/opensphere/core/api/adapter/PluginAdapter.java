package io.opensphere.core.api.adapter;

import java.util.Collection;

import io.opensphere.core.Plugin;
import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.Transformer;

/**
 * Adapter with empty implementations of methods in the {@link Plugin}
 * interface.
 */
public abstract class PluginAdapter implements Plugin
{
    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
    }

    @Override
    public void close()
    {
    }

    @Override
    public Collection<? extends Envoy> getEnvoys()
    {
        return null;
    }

    @Override
    public Collection<? extends Transformer> getTransformers()
    {
        return null;
    }
}
