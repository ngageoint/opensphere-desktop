package io.opensphere.infinity;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.adapter.AbstractServicePlugin;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.Service;
import io.opensphere.infinity.envoy.InfinityEnvoy;
import io.opensphere.infinity.model.InfinitySettingsModel;
import io.opensphere.infinity.view.InfinityOptionsProvider;

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
        Preferences preferences = toolbox.getPreferencesRegistry().getPreferences(InfinityPlugin.class);
        InfinitySettingsModel settingsModel = new InfinitySettingsModel(preferences);
        Service optionsProviderService = toolbox.getUIRegistry().getOptionsRegistry()
                .getOptionsProviderService(new InfinityOptionsProvider(settingsModel));
        return List.of(settingsModel, new InfinityLayerController(toolbox, settingsModel), optionsProviderService);
    }
}
