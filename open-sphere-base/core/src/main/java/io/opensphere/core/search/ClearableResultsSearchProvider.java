package io.opensphere.core.search;

/**
 * A search provider that will return results of the search.
 */
public interface ClearableResultsSearchProvider extends ResultsSearchProvider
{
    /**
     * Clears the search results.
     */
    void clearResults();
}
