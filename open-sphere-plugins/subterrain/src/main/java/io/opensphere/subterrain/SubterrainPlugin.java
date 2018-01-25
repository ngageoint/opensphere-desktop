package io.opensphere.subterrain;

import java.util.Collection;
import java.util.Collections;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.util.collections.New;
import io.opensphere.subterrain.xraygoggles.XrayGoggles;

/**
 * Subterrain plugin that provides the user the ability to see features
 * underneath the surface either by making the globe transparent or displaying a
 * two dimensional "fish finder" type UI.
 */
public class SubterrainPlugin extends PluginAdapter
{
    /**
     * Allows the user to see below earths surface.
     */
    private XrayGoggles myGoggles;

    @Override
    public Collection<? extends Envoy> getEnvoys()
    {
        return Collections.emptyList();
    }

    @Override
    public Collection<? extends Transformer> getTransformers()
    {
        return New.unmodifiableList(myGoggles.getTransformer());
    }

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        super.initialize(plugindata, toolbox);
        myGoggles = new XrayGoggles(toolbox);
    }
}
