package io.opensphere.core.search;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.opensphere.core.util.collections.New;

/**
 * Interface for the search registry. The search registry will allow plugins to
 * add themselves as a search provider.
 */
public interface SearchRegistry
{
    /**
     * Add a new search provider.
     *
     * @param provider The new search provider to add.
     */
    void addSearchProvider(SearchProvider provider);

    /**
     * Clear any search results currently displayed on the map for all the
     * search providers.
     */
    void clearSearchResults();

    /**
     * Start a search for the given string. This will go through and perform a
     * search for each of the current search providers until results are found.
     *
     * @param searchTerm What to search for.
     * @param useBbox use a bounding box or not for the search
     */
    void initiateSearch(String searchTerm, boolean useBbox);

    /**
     * Remove the search provider.
     *
     * @param provider The search provider to remove.
     */
    void removeSearchProvider(SearchProvider provider);

    /**
     * The list of all registered search providers.
     *
     * @return searchProviders.
     */
    Map<String, Map<String, SearchProvider>> getProviders();

    /**
     * Sets whether the user wants to search in view. Search providers may choose to ignore this setting.
     *
     * @param inView whether the user wants to search in view
     */
    void setSearchInView(boolean inView);

    /**
     * Gets whether the user wants to search in view. Search providers may choose to ignore this setting.
     *
     * @return whether the user wants to search in view
     */
    boolean isSearchInView();

    /**
     * Gets the search providers for the given search types.
     *
     * @param searchTypes the search types
     * @return the search providers
     */
    default List<SearchProvider> getProviders(Collection<String> searchTypes)
    {
        Map<String, Map<String, SearchProvider>> providers = getProviders();
        return searchTypes.stream().filter(t -> providers.containsKey(t)).flatMap(t -> providers.get(t).values().stream())
                .collect(Collectors.toList());
    }

    /** The types of searches that can be performed. */
    enum SearchType
    {
        /** BE Number type. */
        BE_NUMBER,

        /** Geographic query type. */
        GEO_QUERY,

        /** Latitude and Longitude type. */
        LAT_LON,

        /** MGRS type. */
        MGRS,

        /** Parsley geographic name type. */
        PARSLEY_GEO_NAMES,

        /** Place name type. */
        PLACE_NAME,

        /** Recommended layers. */
        RECOMMENDED_LAYERS;

        /**
         * Gets the values as strings.
         *
         * @return The values of this enum as strings.
         */
        public static List<String> valuesAsStrings()
        {
            List<String> values = New.list();
            for (SearchType searchType : values())
            {
                values.add(searchType.toString());
            }
            return values;
        }
    }
}
