package io.opensphere.search.controller;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.MapManager;
import io.opensphere.core.TimeManager;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.search.ClearableResultsSearchProvider;
import io.opensphere.core.search.ResultsSearchProvider;
import io.opensphere.core.search.SearchProvider;
import io.opensphere.core.search.SearchRegistry;
import io.opensphere.core.search.SearchResult;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.search.model.SearchModel;

/**
 * Executes searches for all {@link ResultsSearchProvider} installed in the
 * system.
 */
public class SearchExecutor
{
    /**
     * Used to get the viewport boundaries.
     */
    private final MapManager myMapManager;

    /**
     * The model of the search, contains the keyword to search on.
     */
    private final SearchModel myModel;

    /**
     * Contains all of the installed {@link SearchProvider}.
     */
    private final SearchRegistry mySearchRegistry;

    /**
     * Gets the time spans to perform the search for.
     */
    private final TimeManager myTimeManager;

    /**
     * Constructs a new search executor.
     *
     * @param model The model used by the search.
     * @param searchRegistry Contains all of the installed
     *            {@link SearchProvider}.
     * @param timeManager Contains the time spans to perform the search for.
     * @param mapManager Used to get the viewport boundaries.
     */
    public SearchExecutor(SearchModel model, SearchRegistry searchRegistry, TimeManager timeManager, MapManager mapManager)
    {
        myModel = model;
        mySearchRegistry = searchRegistry;
        myTimeManager = timeManager;
        myMapManager = mapManager;
    }

    /**
     * Performs a search against all {@link ResultsSearchProvider} that are
     * enabled.
     */
    public void performSearch()
    {
        performSearch(myModel.getSelectedSearchTypes());
    }

    /**
     * Performs a search against all {@link ResultsSearchProvider} that are
     * enabled.
     *
     * @param types The search types to search for.
     */
    public void performSearch(Collection<String> types)
    {
        String keyword = myModel.getKeyword().get();
        Pair<LatLonAlt, LatLonAlt> boundingBox = getBoundingBox();
        TimeSpan span = getTimeSpan();

        List<SearchProvider> providers = mySearchRegistry.getProviders(types);
        for (SearchProvider provider : providers)
        {
            if (provider instanceof ResultsSearchProvider)
            {
                ThreadUtilities.runBackground(() -> performSearch((ResultsSearchProvider)provider, keyword,
                        boundingBox.getFirstObject(), boundingBox.getSecondObject(), span));
            }
        }
    }

    /**
     * Clears all the results from the previous search.
     */
    public void clearSearch()
    {
        List<SearchProvider> providers = mySearchRegistry.getProviders(myModel.getSelectedSearchTypes());
        for (SearchProvider provider : providers)
        {
            if (provider instanceof ClearableResultsSearchProvider)
            {
                ClearableResultsSearchProvider clearableProvider = (ClearableResultsSearchProvider)provider;
                ThreadUtilities.runBackground(() -> clearableProvider.clearResults());
            }
        }
        myModel.getResultCount().clear();
        myModel.getTotalResultCount().clear();
    }

    /**
     * Gets the viewports bounding box to be used by the search providers.
     *
     * @return The viewports bounding box.
     */
    private Pair<LatLonAlt, LatLonAlt> getBoundingBox()
    {
        GeographicBoundingBox box = myMapManager.getVisibleBoundingBox();
        return new Pair<>(box.getLowerLeft().getLatLonAlt(), box.getUpperRight().getLatLonAlt());
    }

    /**
     * Gets the timespan to be used by the search providers.
     *
     * @return The time span.
     */
    private TimeSpan getTimeSpan()
    {
        List<TimeSpan> loadSpans = New.list(myTimeManager.getLoadTimeSpans());
        TimeSpan span = null;
        for (TimeSpan loadSpan : loadSpans)
        {
            if (span == null)
            {
                span = loadSpan;
            }
            else
            {
                span = span.simpleUnion(loadSpan);
            }
        }

        return span;
    }

    /**
     * Performs a search using the provider given.
     *
     * @param provider The provider to use for the search.
     * @param keyword The keyword to search on.
     * @param lowerLeft The lower left of the viewport bounding box.
     * @param upperRight The upper right of the viewport bounding box.
     * @param span The timespan to perform a search for.
     */
    private void performSearch(ResultsSearchProvider provider, String keyword, LatLonAlt lowerLeft, LatLonAlt upperRight,
            TimeSpan span)
    {
        List<SearchResult> results = provider.performSearch(keyword, lowerLeft, upperRight, span);
        for (SearchResult result : results)
        {
            result.setSearchType(provider.getType());
        }
        myModel.getResultCount().put(provider.getType(), Integer.valueOf(results.size()));
        myModel.getTotalResultCount().put(provider.getType(), Integer.valueOf(provider.getTotalResultCount()));
        myModel.getAllResults().addAll(results);
    }
}
