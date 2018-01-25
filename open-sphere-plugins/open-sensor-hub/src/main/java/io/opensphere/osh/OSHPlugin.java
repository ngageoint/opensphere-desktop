package io.opensphere.osh;

import java.util.Collection;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.util.collections.New;
import io.opensphere.osh.envoy.OSHDescribeSensorEnvoy;
import io.opensphere.osh.envoy.OSHGetCapabilitiesEnvoy;
import io.opensphere.osh.envoy.OSHGetResultEnvoy;
import io.opensphere.osh.envoy.OSHGetResultTemplateEnvoy;
import io.opensphere.osh.server.OSHServerSourceController;
import io.opensphere.server.toolbox.ServerToolboxUtils;

/** The OpenSensorHub plugin. */
public class OSHPlugin extends PluginAdapter
{
    /** The toolbox. */
    private Toolbox myToolbox;

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        myToolbox = toolbox;
        ServerToolboxUtils.getServerToolbox(myToolbox).getServerSourceControllerManager()
                .setPreferencesTopic(OSHServerSourceController.class, OSHPlugin.class);
        super.initialize(plugindata, toolbox);
    }

    @Override
    public Collection<? extends Envoy> getEnvoys()
    {
        return New.list(new OSHGetCapabilitiesEnvoy(myToolbox), new OSHDescribeSensorEnvoy(myToolbox),
                new OSHGetResultTemplateEnvoy(myToolbox), new OSHGetResultEnvoy(myToolbox));
    }

    @Override
    public Collection<? extends Transformer> getTransformers()
    {
        return null;
    }
}
