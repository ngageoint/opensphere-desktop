package io.opensphere.core.search;

/**
 * This is a search provider that does not provide any results in the results
 * view. Instead it will put the results on the globe by some means.
 */
public interface BasicSearchProvider extends SearchProvider
{
    /**
     * Clear any results displayed on the map.
     *
     * @return True if successful, false otherwise.
     */
    boolean clearSearchResults();

    /**
     * Perform the actual search and display any results on the map.
     *
     * @param searchString The string to search for.
     * @return True if successful, false otherwise.
     */
    boolean performSearch(String searchString);
}
