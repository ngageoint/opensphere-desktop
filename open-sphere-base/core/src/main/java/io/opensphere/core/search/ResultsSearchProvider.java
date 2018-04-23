package io.opensphere.core.search;

import java.util.List;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;

/**
 * A search provider that will return results of the search.
 */
public interface ResultsSearchProvider extends SearchProvider
{
    /**
     * Performs the search given a keyword, an area, and a time span. Providers
     * can choose to ignore any of the parameters they wish. Keyword is the only
     * parameter that may be null.
     *
     * @param keyword The keyword to search on, or null if no keyword was given
     *            by the user.
     * @param lowerLeft The lower left corner of the area to search in.
     * @param upperRight The upper right corner of the area to search in.
     * @param span The time span to search for.
     * @return The search results or an empty list if nothing was found in
     *         search.
     */
    List<SearchResult> performSearch(String keyword, LatLonAlt lowerLeft, LatLonAlt upperRight, TimeSpan span);

    /**
     * Gets the total result count including skipped results.
     *
     * @return the total result count
     */
    default int getTotalResultCount()
    {
        return -1;
    }
}
