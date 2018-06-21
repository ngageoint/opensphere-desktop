package io.opensphere.core.search;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jcip.annotations.GuardedBy;

import org.apache.log4j.Logger;

import io.opensphere.core.util.collections.New;

/** The implementation for the search registry. */
public class SearchRegistryImpl implements SearchRegistry
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(SearchRegistryImpl.class);

    /** The search types. */
    private static final List<String> SEARCH_TYPES = Collections.unmodifiableList(Arrays.asList(SearchType.PLACE_NAME.toString(),
            SearchType.GEO_QUERY.toString(), SearchType.BE_NUMBER.toString(), SearchType.PARSLEY_GEO_NAMES.toString()));

    /**
     * The data structure that holds all the search providers that have
     * registered themselves. This is a map of search type to another map of
     * name to the actual provider.
     */
    @GuardedBy("myProviders")
    private final Map<String, Map<String, SearchProvider>> myProviders = new HashMap<>();

    @Override
    public void addSearchProvider(SearchProvider provider)
    {
        synchronized (myProviders)
        {
            if (myProviders.get(provider.getType()) == null)
            {
                myProviders.put(provider.getType(), new HashMap<String, SearchProvider>());
            }
            myProviders.get(provider.getType()).put(provider.getName(), provider);
        }
    }

    @Override
    public void clearSearchResults()
    {
        for (SearchProvider searchProv : getProviders(SEARCH_TYPES))
        {
            if (searchProv instanceof BasicSearchProvider)
            {
                ((BasicSearchProvider)searchProv).clearSearchResults();
            }
        }
    }

    @Override
    public void initiateSearch(String searchTerm)
    {
        // The order we want are
        // 1. Place Names
        // 2. GeoQuery
        // 3. BENumSearch
        // 4. ParsleyGeoNamesSearch
        for (SearchProvider searchProv : getProviders(SEARCH_TYPES))
        {
            if (searchProv instanceof BasicSearchProvider && ((BasicSearchProvider)searchProv).performSearch(searchTerm))
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(searchProv.getName() + " search successful.");
                }
                return;
            }
        }
    }

    @Override
    public void removeSearchProvider(SearchProvider provider)
    {
        synchronized (myProviders)
        {
            myProviders.get(provider.getType()).remove(provider.getName());
            if (myProviders.get(provider.getType()).isEmpty())
            {
                myProviders.remove(provider.getType());
            }
        }
    }

    /**
     * A map of all registered search Providers.
     *
     * @return myProviders.
     */
    @Override
    public Map<String, Map<String, SearchProvider>> getProviders()
    {
        synchronized (myProviders)
        {
            return New.map(myProviders);
        }
    }
}
