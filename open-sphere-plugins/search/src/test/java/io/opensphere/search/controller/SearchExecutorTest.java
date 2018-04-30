package io.opensphere.search.controller;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javafx.collections.ListChangeListener;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.MapManager;
import io.opensphere.core.TimeManager;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.search.ResultsSearchProvider;
import io.opensphere.core.search.SearchProvider;
import io.opensphere.core.search.SearchRegistry;
import io.opensphere.core.search.SearchResult;
import io.opensphere.core.util.ObservableList;
import io.opensphere.core.util.collections.New;
import io.opensphere.search.model.SearchModel;

/**
 * Unit test for {@link SearchExecutor}.
 */
public class SearchExecutorTest
{
    /**
     * Tests performing a search.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testPerformSearch() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        List<String> searchTypes = New.list("Place Names", "Layers");
        TimeSpan expectedSpan = TimeSpan.get(System.currentTimeMillis() - 1000, System.currentTimeMillis());
        SearchResult result1 = support.createMock(SearchResult.class);
        result1.setSearchType(searchTypes.get(0));
        SearchResult result2 = support.createMock(SearchResult.class);
        result2.setSearchType(searchTypes.get(0));

        SearchRegistry searchRegistry = createSearchRegistry(support, "acka acka", searchTypes, expectedSpan,
                New.list(result1, result2));

        ObservableList<TimeSpan> loadSpans = new ObservableList<>();
        loadSpans.add(expectedSpan);
        TimeManager timeManager = createTimeManager(support, loadSpans);

        MapManager mapManager = createMapManager(support);

        CountDownLatch latch = new CountDownLatch(1);
        SearchModel model = new SearchModel();
        model.getKeyword().set("acka acka");
        model.getSelectedSearchTypes().addAll(searchTypes);
        model.getAllResults().addListener((ListChangeListener<SearchResult>)(e) ->
        {
            latch.countDown();
        });

        support.replayAll();

        SearchExecutor executor = new SearchExecutor(model, searchRegistry, timeManager, mapManager);
        executor.performSearch();

        assertTrue(latch.await(1, TimeUnit.SECONDS));

        assertTrue(model.getAllResults().contains(result1));
        assertTrue(model.getAllResults().contains(result2));

        support.verifyAll();
    }

    /**
     * Tests performing a search.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testPerformSearchNullDoubleSpan() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        List<String> searchTypes = New.list("Place Names", "Layers");
        TimeSpan loadSpan1 = TimeSpan.get(System.currentTimeMillis() - 1000, System.currentTimeMillis());
        TimeSpan loadSpan2 = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis() - 9000);
        TimeSpan expectedSpan = TimeSpan.get(loadSpan2.getStart(), loadSpan1.getEnd());
        SearchResult result1 = support.createMock(SearchResult.class);
        result1.setSearchType(searchTypes.get(0));
        SearchResult result2 = support.createMock(SearchResult.class);
        result2.setSearchType(searchTypes.get(0));
        SearchRegistry searchRegistry = createSearchRegistry(support, null, searchTypes, expectedSpan,
                New.list(result1, result2));

        ObservableList<TimeSpan> loadSpans = new ObservableList<>();
        loadSpans.add(loadSpan1);
        loadSpans.add(loadSpan2);
        TimeManager timeManager = createTimeManager(support, loadSpans);

        MapManager mapManager = createMapManager(support);

        CountDownLatch latch = new CountDownLatch(1);
        SearchModel model = new SearchModel();
        model.getSelectedSearchTypes().addAll(searchTypes);
        model.getAllResults().addListener((ListChangeListener<SearchResult>)(e) ->
        {
            latch.countDown();
        });

        support.replayAll();

        SearchExecutor executor = new SearchExecutor(model, searchRegistry, timeManager, mapManager);
        executor.performSearch();

        assertTrue(latch.await(1, TimeUnit.SECONDS));

        assertTrue(model.getAllResults().contains(result1));
        assertTrue(model.getAllResults().contains(result2));

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link SearchRegistry}.
     *
     * @param support Used to create the mock.
     * @param keyword The keyword to expect in search.
     * @param searchTypes The search types to expect when getting providers.
     * @param expectedSpan The expected time span for the search.
     * @param results The results to return from search.
     * @return The mocked registry.
     */
    private SearchRegistry createSearchRegistry(EasyMockSupport support, String keyword, Collection<String> searchTypes,
            TimeSpan expectedSpan, List<SearchResult> results)
    {
        SearchProvider provider = support.createMock(SearchProvider.class);

        ResultsSearchProvider resultProvider = support.createMock(ResultsSearchProvider.class);
        EasyMock.expect(resultProvider.getType()).andReturn("Place Names").atLeastOnce();
        EasyMock.expect(resultProvider.performSearch(keyword, LatLonAlt.createFromDegrees(10, 11),
                LatLonAlt.createFromDegrees(12, 13), expectedSpan)).andReturn(results);
        EasyMock.expect(resultProvider.getTotalResultCount()).andReturn(-1).anyTimes();

        SearchRegistry registry = support.createMock(SearchRegistry.class);

        EasyMock.expect(registry.getProviders(searchTypes)).andReturn(New.list(provider, resultProvider));

        return registry;
    }

    /**
     * Creates an easy mocked {@link TimeManager}.
     *
     * @param support Used to create the mock.
     * @param loadSpans The load spans to return.
     * @return The mocked time manager.
     */
    private TimeManager createTimeManager(EasyMockSupport support, ObservableList<TimeSpan> loadSpans)
    {
        TimeManager timeManager = support.createMock(TimeManager.class);

        EasyMock.expect(timeManager.getLoadTimeSpans()).andReturn(loadSpans);

        return timeManager;
    }

    /**
     * Creates an easy mocked {@link MapManager}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link MapManager}.
     */
    private MapManager createMapManager(EasyMockSupport support)
    {
        MapManager mapManager = support.createMock(MapManager.class);

        GeographicBoundingBox boundingBox = new GeographicBoundingBox(new GeographicPosition(LatLonAlt.createFromDegrees(10, 11)),
                new GeographicPosition(LatLonAlt.createFromDegrees(12, 13)));
        EasyMock.expect(mapManager.getVisibleBoundingBox()).andReturn(boundingBox);

        return mapManager;
    }
}
