package io.opensphere.search.mapzen;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.options.OptionsProvider;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.search.SearchOptionsProvider;
import io.opensphere.search.mapzen.model.MapZenSettingsModel;
import io.opensphere.search.mapzen.view.MapZenOptionsProvider;

/** The plugin that provides a MapZen GeoCode search. */
public class MapZenPlugin extends PluginAdapter
{
    /** The class the does the actual searching. */
    private MapZenSearchProvider myGeoQuerySearcher;

    /** The toolbox. */
    private Toolbox myToolbox;

    /** The model in which plugin state is maintained. */
    private MapZenSettingsModel myModel;

    /** The options provider in which settings are modified. */
    private MapZenOptionsProvider myOptionsProvider;

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.api.adapter.PluginAdapter#close()
     */
    @Override
    public void close()
    {
        if (myToolbox != null)
        {
            if (myToolbox.getSearchRegistry() != null && myGeoQuerySearcher != null)
            {
                myToolbox.getSearchRegistry().removeSearchProvider(myGeoQuerySearcher);
            }

            OptionsProvider searchProvider = myToolbox.getUIRegistry().getOptionsRegistry()
                    .getRootProviderByTopic(SearchOptionsProvider.PROVIDER_NAME);
            if (searchProvider != null)
            {
                searchProvider.removeSubTopic(searchProvider);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.api.adapter.PluginAdapter#initialize(io.opensphere.core.PluginLoaderData,
     *      io.opensphere.core.Toolbox)
     */
    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        myToolbox = toolbox;

        Preferences preferences = toolbox.getPreferencesRegistry().getPreferences(MapZenPlugin.class);
        myModel = new MapZenSettingsModel(preferences);

        myOptionsProvider = new MapZenOptionsProvider(myToolbox, myModel);
        OptionsProvider parentSearchProvider = myToolbox.getUIRegistry().getOptionsRegistry()
                .getRootProviderByTopic(SearchOptionsProvider.PROVIDER_NAME);
        if (parentSearchProvider != null)
        {
            parentSearchProvider.addSubTopic(myOptionsProvider);
        }

        myGeoQuerySearcher = new MapZenSearchProvider(myToolbox, myModel);
        myToolbox.getSearchRegistry().addSearchProvider(myGeoQuerySearcher);
    }
}
