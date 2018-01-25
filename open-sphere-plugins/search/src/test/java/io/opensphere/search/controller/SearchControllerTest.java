package io.opensphere.search.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javafx.collections.ListChangeListener;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.MapManager;
import io.opensphere.core.TimeManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultMouseBinding;
import io.opensphere.core.control.DiscreteEventListener;
import io.opensphere.core.control.PickListener;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.search.ResultsSearchProvider;
import io.opensphere.core.search.ResultsViewSearchProvider;
import io.opensphere.core.search.SearchProvider;
import io.opensphere.core.search.SearchRegistry;
import io.opensphere.core.search.SearchResult;
import io.opensphere.core.util.ObservableList;
import io.opensphere.core.util.collections.New;
import io.opensphere.search.model.SearchModel;

/**
 * Unit test for the SearchController.
 */
public class SearchControllerTest
{
    /**
     * The test basic search type.
     */
    private static final String ourBasicType = "Basic";

    /**
     * The test keyword.
     */
    private static final String ourKeyWord = "I am keyword";

    /**
     * The test layers search type.
     */
    private static final String ourLayersType = "Layers";

    /**
     * The test place name search type.
     */
    private static final String ourPlaceNameType = "Place Names";

    /**
     * The geometries published during the test.
     */
    private final List<Geometry> myPublishedGeometries = Collections.synchronizedList(New.list());

    /**
     * Tests clearing the search results.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testClearSearch() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        myPublishedGeometries.clear();

        TimeSpan expectedSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        SearchResult result1 = new SearchResult();
        result1.setText("I am a place name");
        result1.setConfidence(.4f);
        result1.getLocations().add(LatLonAlt.createFromDegrees(10, 11));
        SearchResult result2 = new SearchResult();
        result2.setText("I am a layer");
        result2.setConfidence(.9f);
        result2.getLocations().add(LatLonAlt.createFromDegrees(12, 13));
        Set<String> selectedSearchTypes = New.set(ourLayersType, ourPlaceNameType);

        Map<String, Map<String, SearchProvider>> providers = createProviders(support, ourKeyWord, expectedSpan, result1, result2,
                selectedSearchTypes);
        TimeManager timeManager = createTimeManager(support, expectedSpan);
        SearchRegistry searchRegistry = createSearchRegistry(support, providers);
        Toolbox toolbox = createToolbox(support, searchRegistry, timeManager);

        CountDownLatch latch = new CountDownLatch(2);
        SearchModel model = new SearchModel();
        model.getShownResults().addListener((ListChangeListener<SearchResult>)(c) ->
        {
            while (c.next())
            {
                for (int i = 0; i < c.getAddedSize(); i++)
                {
                    latch.countDown();
                }
            }
        });

        support.replayAll();

        SearchController controller = new SearchController(toolbox, model);

        // This now happens in SearchPlugin so we have to do it here in the test
        model.getSearchTypes().add(ourLayersType);
        model.getSearchTypes().add(ourPlaceNameType);
        model.getSelectedSearchTypes().add(ourLayersType);
        model.getSelectedSearchTypes().add(ourPlaceNameType);

        model.getKeyword().set(ourKeyWord);

        assertEquals(ourLayersType, model.getSearchTypes().get(0));
        assertEquals(ourPlaceNameType, model.getSearchTypes().get(1));

        assertEquals(ourLayersType, model.getSelectedSearchTypes().get(0));
        assertEquals(ourPlaceNameType, model.getSelectedSearchTypes().get(1));

        assertTrue(latch.await(1, TimeUnit.SECONDS));

        assertTrue(model.getAllResults().contains(result1));
        assertEquals(result1, model.getShownResults().get(1));
        assertTrue(model.getAllResults().contains(result2));
        assertEquals(result2, model.getShownResults().get(0));

        assertEquals(4, myPublishedGeometries.size());

        Set<LatLonAlt> points = New.set();
        points.add(((GeographicPosition)((PointGeometry)myPublishedGeometries.get(0)).getPosition()).getLatLonAlt());
        points.add(((GeographicPosition)((PointGeometry)myPublishedGeometries.get(2)).getPosition()).getLatLonAlt());

        assertTrue(points.contains(result1.getLocations().get(0)));
        assertTrue(points.contains(result2.getLocations().get(0)));

        model.getKeyword().set(null);

        assertTrue(model.getAllResults().isEmpty());
        assertTrue(model.getShownResults().isEmpty());
        assertTrue(myPublishedGeometries.isEmpty());

        controller.close();

        support.verifyAll();
    }

    /**
     * Tests performing a search.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testPerformSearch() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        myPublishedGeometries.clear();

        TimeSpan expectedSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        SearchResult result1 = new SearchResult();
        result1.setText("I am a place name");
        result1.setConfidence(.4f);
        result1.getLocations().add(LatLonAlt.createFromDegrees(10, 11));
        SearchResult result2 = new SearchResult();
        result2.setText("I am a layer");
        result2.setConfidence(.9f);
        result2.getLocations().add(LatLonAlt.createFromDegrees(12, 13));
        Set<String> selectedSearchTypes = New.set(ourLayersType, ourPlaceNameType);

        Map<String, Map<String, SearchProvider>> providers = createProviders(support, ourKeyWord, expectedSpan, result1, result2,
                selectedSearchTypes);
        TimeManager timeManager = createTimeManager(support, expectedSpan);
        SearchRegistry searchRegistry = createSearchRegistry(support, providers);
        Toolbox toolbox = createToolbox(support, searchRegistry, timeManager);

        CountDownLatch latch = new CountDownLatch(2);
        final SearchModel model = new SearchModel();
        model.getShownResults().addListener((ListChangeListener<SearchResult>)(c) ->
        {
            while (c.next())
            {
                for (int i = 0; i < c.getAddedSize(); i++)
                {
                    latch.countDown();
                }
            }
        });

        support.replayAll();

        SearchController controller = new SearchController(toolbox, model);

        // This now happens in SearchPlugin so we have to do it here in the test
        model.getSearchTypes().add(ourLayersType);
        model.getSearchTypes().add(ourPlaceNameType);
        model.getSelectedSearchTypes().add(ourLayersType);
        model.getSelectedSearchTypes().add(ourPlaceNameType);

        model.getSearchTypes().addListener((ListChangeListener<String>)(change) ->
        {
            assertTrue(model.getSelectedSearchTypes().containsAll(model.getSearchTypes()));
        });

        model.getKeyword().set(ourKeyWord);

        assertEquals(ourLayersType, model.getSearchTypes().get(0));
        assertEquals(ourPlaceNameType, model.getSearchTypes().get(1));

        assertEquals(ourLayersType, model.getSelectedSearchTypes().get(0));
        assertEquals(ourPlaceNameType, model.getSelectedSearchTypes().get(1));

        assertTrue(latch.await(1, TimeUnit.SECONDS));

        assertTrue(model.getAllResults().contains(result1));
        assertEquals(result1, model.getShownResults().get(1));
        assertTrue(model.getAllResults().contains(result2));
        assertEquals(result2, model.getShownResults().get(0));

        assertEquals(4, myPublishedGeometries.size());

        Set<LatLonAlt> points = New.set();
        points.add(((GeographicPosition)((PointGeometry)myPublishedGeometries.get(0)).getPosition()).getLatLonAlt());
        points.add(((GeographicPosition)((PointGeometry)myPublishedGeometries.get(2)).getPosition()).getLatLonAlt());

        assertTrue(points.contains(result1.getLocations().get(0)));
        assertTrue(points.contains(result2.getLocations().get(0)));

        controller.close();

        support.verifyAll();
    }

    /**
     * Tests performing a search and only a few are selected.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testPerformSearchSomeTypes() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        myPublishedGeometries.clear();

        TimeSpan expectedSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        SearchResult result1 = new SearchResult();
        result1.setText("I am a place name");
        result1.setConfidence(.4f);
        result1.getLocations().add(LatLonAlt.createFromDegrees(10, 11));
        SearchResult result2 = new SearchResult();
        result2.setText("I am a layer");
        result2.setConfidence(.9f);
        result2.getLocations().add(LatLonAlt.createFromDegrees(12, 13));
        Set<String> selectedSearchTypes = New.set(ourPlaceNameType);

        Map<String, Map<String, SearchProvider>> providers = createProviders(support, ourKeyWord, expectedSpan, result1, result2,
                selectedSearchTypes);
        TimeManager timeManager = createTimeManager(support, expectedSpan);
        SearchRegistry searchRegistry = createSearchRegistry(support, providers);
        Toolbox toolbox = createToolbox(support, searchRegistry, timeManager);

        CountDownLatch latch = new CountDownLatch(1);
        SearchModel model = new SearchModel();
        model.getShownResults().addListener((ListChangeListener<SearchResult>)(c) ->
        {
            while (c.next())
            {
                for (int i = 0; i < c.getAddedSize(); i++)
                {
                    latch.countDown();
                }
            }
        });

        support.replayAll();

        model.getSearchTypes().add(ourLayersType);
        model.getSearchTypes().add(ourPlaceNameType);
        model.getSelectedSearchTypes().add(ourLayersType);
        model.getSelectedSearchTypes().add(ourPlaceNameType);

        SearchController controller = new SearchController(toolbox, model);

        assertEquals(ourLayersType, model.getSearchTypes().get(0));
        assertEquals(ourPlaceNameType, model.getSearchTypes().get(1));

        assertEquals(ourLayersType, model.getSelectedSearchTypes().get(0));
        assertEquals(ourPlaceNameType, model.getSelectedSearchTypes().get(1));

        model.getSelectedSearchTypes().remove(0);

        model.getKeyword().set(ourKeyWord);

        assertTrue(latch.await(1, TimeUnit.SECONDS));

        assertEquals(result1, model.getAllResults().get(0));
        assertEquals(result1, model.getShownResults().get(0));

        assertEquals(2, myPublishedGeometries.size());

        Set<LatLonAlt> points = New.set();
        points.add(((GeographicPosition)((PointGeometry)myPublishedGeometries.get(0)).getPosition()).getLatLonAlt());

        assertTrue(points.contains(result1.getLocations().get(0)));

        controller.close();

        support.verifyAll();
    }

    /**
     * Tests when a new search type is selected.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testSearchTypeAdded() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        myPublishedGeometries.clear();

        TimeSpan expectedSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        SearchResult result1 = new SearchResult();
        result1.setText("I am a place name");
        result1.setConfidence(.4f);
        result1.getLocations().add(LatLonAlt.createFromDegrees(10, 11));
        SearchResult result2 = new SearchResult();
        result2.setText("I am a layer");
        result2.setConfidence(.9f);
        result2.getLocations().add(LatLonAlt.createFromDegrees(12, 13));
        Set<String> selectedSearchTypes = New.set(ourLayersType, ourPlaceNameType);

        Map<String, Map<String, SearchProvider>> providers = createProviders(support, ourKeyWord, expectedSpan, result1, result2,
                selectedSearchTypes);
        TimeManager timeManager = createTimeManager(support, expectedSpan);
        SearchRegistry searchRegistry = createSearchRegistry(support, providers);
        Toolbox toolbox = createToolbox(support, searchRegistry, timeManager);

        CountDownLatch latch = new CountDownLatch(2);
        SearchModel model = new SearchModel();
        model.getShownResults().addListener((ListChangeListener<SearchResult>)(c) ->
        {
            while (c.next())
            {
                for (int i = 0; i < c.getAddedSize(); i++)
                {
                    latch.countDown();
                }
            }
        });

        support.replayAll();

        SearchController controller = new SearchController(toolbox, model);

        // This now happens in SearchPlugin so we have to do it here in the test
        model.getSearchTypes().add(ourLayersType);
        model.getSearchTypes().add(ourPlaceNameType);
        model.getSelectedSearchTypes().add(ourLayersType);
        model.getSelectedSearchTypes().add(ourPlaceNameType);

        model.getKeyword().set(ourKeyWord);

        assertEquals(ourLayersType, model.getSearchTypes().get(0));
        assertEquals(ourPlaceNameType, model.getSearchTypes().get(1));

        assertEquals(ourLayersType, model.getSelectedSearchTypes().get(0));
        assertEquals(ourPlaceNameType, model.getSelectedSearchTypes().get(1));

        assertTrue(latch.await(1, TimeUnit.SECONDS));

        assertTrue(model.getAllResults().contains(result1));
        assertEquals(result1, model.getShownResults().get(1));
        assertTrue(model.getAllResults().contains(result2));
        assertEquals(result2, model.getShownResults().get(0));

        assertEquals(4, myPublishedGeometries.size());

        Set<LatLonAlt> points = New.set();
        points.add(((GeographicPosition)((PointGeometry)myPublishedGeometries.get(0)).getPosition()).getLatLonAlt());
        points.add(((GeographicPosition)((PointGeometry)myPublishedGeometries.get(2)).getPosition()).getLatLonAlt());

        assertTrue(points.contains(result1.getLocations().get(0)));
        assertTrue(points.contains(result2.getLocations().get(0)));

        model.getSelectedSearchTypes().remove(0);

        assertTrue(model.getAllResults().contains(result1));
        assertEquals(result1, model.getShownResults().get(0));
        assertTrue(model.getAllResults().contains(result2));
        assertFalse(model.getShownResults().contains(result2));

        assertEquals(2, myPublishedGeometries.size());

        points = New.set();
        points.add(((GeographicPosition)((PointGeometry)myPublishedGeometries.get(0)).getPosition()).getLatLonAlt());

        assertTrue(points.contains(result1.getLocations().get(0)));

        model.getSelectedSearchTypes().add(ourLayersType);

        assertTrue(model.getAllResults().contains(result1));
        assertEquals(result1, model.getShownResults().get(1));
        assertTrue(model.getAllResults().contains(result2));
        assertEquals(result2, model.getShownResults().get(0));

        assertEquals(4, myPublishedGeometries.size());

        points = New.set();
        points.add(((GeographicPosition)((PointGeometry)myPublishedGeometries.get(0)).getPosition()).getLatLonAlt());
        points.add(((GeographicPosition)((PointGeometry)myPublishedGeometries.get(2)).getPosition()).getLatLonAlt());

        assertTrue(points.contains(result1.getLocations().get(0)));
        assertTrue(points.contains(result2.getLocations().get(0)));

        controller.close();

        support.verifyAll();
    }

    /**
     * Tests when a new search type is selected and that new type needs a search
     * performed.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testSearchTypeAddedNeedSearch() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        myPublishedGeometries.clear();

        TimeSpan expectedSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        SearchResult result1 = new SearchResult();
        result1.setText("I am a place name");
        result1.setConfidence(.4f);
        result1.getLocations().add(LatLonAlt.createFromDegrees(10, 11));
        SearchResult result2 = new SearchResult();
        result2.setText("I am a layer");
        result2.setConfidence(.9f);
        result2.getLocations().add(LatLonAlt.createFromDegrees(12, 13));
        Set<String> selectedSearchTypes = New.set(ourPlaceNameType, ourLayersType);

        Map<String, Map<String, SearchProvider>> providers = createProviders(support, ourKeyWord, expectedSpan, result1, result2,
                selectedSearchTypes);
        TimeManager timeManager = createTimeManager(support, expectedSpan);
        SearchRegistry searchRegistry = createSearchRegistry(support, providers);
        Toolbox toolbox = createToolbox(support, searchRegistry, timeManager);

        CountDownLatch latch = new CountDownLatch(1);
        SearchModel model = new SearchModel();
        model.getShownResults().addListener((ListChangeListener<SearchResult>)(c) ->
        {
            while (c.next())
            {
                for (int i = 0; i < c.getAddedSize(); i++)
                {
                    latch.countDown();
                }
            }
        });

        support.replayAll();

        model.getSearchTypes().add(ourLayersType);
        model.getSearchTypes().add(ourPlaceNameType);
        model.getSelectedSearchTypes().add(ourLayersType);
        model.getSelectedSearchTypes().add(ourPlaceNameType);

        SearchController controller = new SearchController(toolbox, model);

        assertEquals(ourLayersType, model.getSearchTypes().get(0));
        assertEquals(ourPlaceNameType, model.getSearchTypes().get(1));

        assertEquals(ourLayersType, model.getSelectedSearchTypes().get(0));
        assertEquals(ourPlaceNameType, model.getSelectedSearchTypes().get(1));

        model.getSelectedSearchTypes().remove(0);

        model.getKeyword().set(ourKeyWord);

        assertTrue(latch.await(1, TimeUnit.SECONDS));

        assertEquals(result1, model.getAllResults().get(0));
        assertEquals(result1, model.getShownResults().get(0));

        assertEquals(2, myPublishedGeometries.size());

        Set<LatLonAlt> points = New.set();
        points.add(((GeographicPosition)((PointGeometry)myPublishedGeometries.get(0)).getPosition()).getLatLonAlt());

        assertTrue(points.contains(result1.getLocations().get(0)));

        CountDownLatch latch2 = new CountDownLatch(1);
        model.getShownResults().addListener((ListChangeListener<SearchResult>)(c) ->
        {
            while (c.next())
            {
                for (int i = 0; i < c.getAddedSize(); i++)
                {
                    latch2.countDown();
                }
            }
        });

        model.getSelectedSearchTypes().add(ourLayersType);

        assertTrue(latch2.await(1, TimeUnit.SECONDS));

        assertTrue(model.getAllResults().contains(result1));
        assertEquals(result1, model.getShownResults().get(1));
        assertTrue(model.getAllResults().contains(result2));
        assertEquals(result2, model.getShownResults().get(0));

        assertEquals(4, myPublishedGeometries.size());

        points = New.set();
        points.add(((GeographicPosition)((PointGeometry)myPublishedGeometries.get(0)).getPosition()).getLatLonAlt());
        points.add(((GeographicPosition)((PointGeometry)myPublishedGeometries.get(2)).getPosition()).getLatLonAlt());

        assertTrue(points.contains(result1.getLocations().get(0)));
        assertTrue(points.contains(result2.getLocations().get(0)));

        controller.close();

        support.verifyAll();
    }

    /**
     * Creates a {@link ControlRegistry} mock.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link ControlRegistry}.
     */
    private ControlRegistry createControlRegistry(EasyMockSupport support)
    {
        ControlContext context = support.createMock(ControlContext.class);
        context.addPickListener(EasyMock.isA(PickListener.class));
        context.addListener(EasyMock.isA(DiscreteEventListener.class),
                EasyMock.eq(new DefaultMouseBinding(MouseEvent.MOUSE_CLICKED)));

        context.removePickListener(EasyMock.isA(PickListener.class));
        context.removeListener(EasyMock.isA(DiscreteEventListener.class));

        ControlRegistry controlRegistry = support.createMock(ControlRegistry.class);

        EasyMock.expect(controlRegistry.getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT)).andReturn(context).times(2);
        EasyMock.expect(controlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT)).andReturn(context).times(2);

        return controlRegistry;
    }

    /**
     * Creates an easy mocked {@link GeometryRegistry}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link GeometryRegistry}.
     */
    @SuppressWarnings("unchecked")
    private GeometryRegistry createGeometryRegistry(EasyMockSupport support)
    {
        GeometryRegistry registry = support.createMock(GeometryRegistry.class);

        registry.addGeometriesForSource(EasyMock.isA(SearchTransformer.class), EasyMock.isA(Collection.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myPublishedGeometries.addAll((Collection<? extends Geometry>)EasyMock.getCurrentArguments()[1]);
            return null;
        }).anyTimes();
        registry.removeGeometriesForSource(EasyMock.isA(SearchTransformer.class), EasyMock.isA(Collection.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myPublishedGeometries.removeAll((Collection<? extends Geometry>)EasyMock.getCurrentArguments()[1]);
            return null;
        }).anyTimes();

        return registry;
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
        EasyMock.expect(mapManager.getVisibleBoundingBox()).andReturn(boundingBox).atLeastOnce();

        return mapManager;
    }

    /**
     * Creates the test search providers.
     *
     * @param support Used to create the mock.
     * @param keyword The expected keyword.
     * @param expectedSpan The expected span.
     * @param result1 The result to return for the placename provider.
     * @param result2 The result to return for the layer provider.
     * @param selectedSearchTypes The currently selected search types.
     * @return The mocked providers.
     */
    private Map<String, Map<String, SearchProvider>> createProviders(EasyMockSupport support, String keyword,
            TimeSpan expectedSpan, SearchResult result1, SearchResult result2, Set<String> selectedSearchTypes)
    {
        SearchProvider basic = support.createMock(SearchProvider.class);

        ResultsSearchProvider placeNames = support.createMock(ResultsSearchProvider.class);
        if (selectedSearchTypes.contains(ourPlaceNameType))
        {
            EasyMock.expect(placeNames.getType()).andReturn(ourPlaceNameType).atLeastOnce();
            EasyMock.expect(placeNames.performSearch(keyword, LatLonAlt.createFromDegrees(10, 11),
                    LatLonAlt.createFromDegrees(12, 13), expectedSpan)).andReturn(New.list(result1));
        }

        ResultsViewSearchProvider layers = support.createMock(ResultsViewSearchProvider.class);
        if (selectedSearchTypes.contains(ourLayersType))
        {
            EasyMock.expect(layers.getType()).andReturn(ourLayersType).atLeastOnce();
            EasyMock.expect(layers.performSearch(keyword, LatLonAlt.createFromDegrees(10, 11),
                    LatLonAlt.createFromDegrees(12, 13), expectedSpan)).andReturn(New.list(result2));
        }

        Map<String, Map<String, SearchProvider>> providers = New.map();

        Map<String, SearchProvider> basicMap = New.map();
        basicMap.put(ourBasicType, basic);
        providers.put(ourBasicType, basicMap);

        Map<String, SearchProvider> placeNameMap = New.map();
        placeNameMap.put(ourPlaceNameType, placeNames);
        providers.put(ourPlaceNameType, placeNameMap);

        Map<String, SearchProvider> layerMap = New.map();
        layerMap.put(ourLayersType, layers);
        providers.put(ourLayersType, layerMap);

        return providers;
    }

    /**
     * Creates an easy mocked {@link SearchRegistry}.
     *
     * @param support Used to create the mock.
     * @param providers The expected providers to return.
     * @return The mocked search registry.
     */
    @SuppressWarnings("unchecked")
    private SearchRegistry createSearchRegistry(EasyMockSupport support, Map<String, Map<String, SearchProvider>> providers)
    {
        SearchRegistry registry = support.createMock(SearchRegistry.class);

        EasyMock.expect(registry.getProviders()).andReturn(providers).anyTimes();
        EasyMock.expect(registry.getProviders(EasyMock.isA(Collection.class))).andAnswer(() ->
        {
            List<SearchProvider> toReturn = New.list();
            Collection<String> types = (Collection<String>)EasyMock.getCurrentArguments()[0];
            for (String type : types)
            {
                toReturn.addAll(providers.get(type).values());
            }

            return toReturn;
        }).atLeastOnce();

        return registry;
    }

    /**
     * Creates an easy mocked {@link TimeManager}.
     *
     * @param support Used to create the mock.
     * @param loadSpan The load spans to return.
     * @return The mocked time manager.
     */
    private TimeManager createTimeManager(EasyMockSupport support, TimeSpan loadSpan)
    {
        TimeManager timeManager = support.createMock(TimeManager.class);

        ObservableList<TimeSpan> loadSpans = new ObservableList<>();
        loadSpans.add(loadSpan);
        EasyMock.expect(timeManager.getLoadTimeSpans()).andReturn(loadSpans).atLeastOnce();

        return timeManager;
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @param searchRegistry The mocked {@link SearchRegistry}.
     * @param timeManager The mocked {@link TimeManager}.
     * @return The mocked toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support, SearchRegistry searchRegistry, TimeManager timeManager)
    {
        MapManager mapManager = createMapManager(support);
        ControlRegistry controlRegistry = createControlRegistry(support);
        GeometryRegistry geometryRegistry = createGeometryRegistry(support);

        Toolbox toolbox = support.createMock(Toolbox.class);

        EasyMock.expect(toolbox.getSearchRegistry()).andReturn(searchRegistry);
        EasyMock.expect(toolbox.getMapManager()).andReturn(mapManager).anyTimes();
        EasyMock.expect(toolbox.getTimeManager()).andReturn(timeManager);
        EasyMock.expect(toolbox.getControlRegistry()).andReturn(controlRegistry);
        EasyMock.expect(toolbox.getGeometryRegistry()).andReturn(geometryRegistry);

        return toolbox;
    }
}
