package io.opensphere.search.googleplaces;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.options.OptionsProvider;
import io.opensphere.search.SearchOptionsProvider;

/** The plugin that provides a Google Places search. */
public class GooglePlacesPlugin extends PluginAdapter
{
    /** The toolbox. */
    private volatile Toolbox myToolbox;

    /** The class the does the actual searching. */
    private volatile GooglePlacesSearch myGeoQuerySearcher;

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
        myGeoQuerySearcher = new GooglePlacesSearch(toolbox);

        OptionsProvider optionsProvider = new GooglePlacesOptionsProvider(toolbox.getPreferencesRegistry(), myGeoQuerySearcher);
        OptionsProvider parentSearchProvider = myToolbox.getUIRegistry().getOptionsRegistry()
                .getRootProviderByTopic(SearchOptionsProvider.PROVIDER_NAME);
        if (parentSearchProvider != null)
        {
            parentSearchProvider.addSubTopic(optionsProvider);
        }

        toolbox.getSearchRegistry().addSearchProvider(myGeoQuerySearcher);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.api.adapter.PluginAdapter#close()
     */
    @Override
    public void close()
    {
        if (myToolbox != null && myToolbox.getSearchRegistry() != null && myGeoQuerySearcher != null)
        {
            myToolbox.getSearchRegistry().removeSearchProvider(myGeoQuerySearcher);
        }
    }
}
