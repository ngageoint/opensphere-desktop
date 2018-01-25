package io.opensphere.wfs.mantle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.TimeManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.accessor.GeometryAccessor;
import io.opensphere.core.cache.accessor.TimeSpanAccessor;
import io.opensphere.core.cache.matcher.GeometryMatcher;
import io.opensphere.core.cache.matcher.TimeSpanMatcher;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.DefaultQuery;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.QueryTracker.QueryStatus;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ObservableList;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.controller.event.impl.ActiveDataGroupsChangedEvent;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.cache.DataElementCache;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.factory.MapGeometrySupportConverterRegistry;
import io.opensphere.mantle.data.geom.factory.impl.MapPointGeometryConverter;
import io.opensphere.mantle.data.geom.impl.SimpleMapPointGeometrySupport;
import io.opensphere.mantle.data.impl.DefaultBasicVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultMapFeatureVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultTimeExtents;
import io.opensphere.mantle.data.util.DataElementLookupException;
import io.opensphere.mantle.data.util.DataElementLookupUtils;
import io.opensphere.mantle.plugin.queryregion.QueryRegion;
import io.opensphere.mantle.plugin.queryregion.QueryRegionListener;
import io.opensphere.mantle.plugin.queryregion.QueryRegionManager;
import io.opensphere.wfs.layer.WFSDataType;

/**
 * Unit test for {@link WFSQueryController}.
 */
public class WFSQueryControllerTest
{
    /**
     * The test type key.
     */
    private static final String ourTypeKey = "i am key";

    /**
     * The subscribed event listener.
     */
    private EventListener<ActiveDataGroupsChangedEvent> myEventListener;

    /**
     * The region listener.
     */
    private QueryRegionListener myRegionListener;

    /**
     * Tests removing all queries.
     *
     * @throws InterruptedException Don't interrupt.
     * @throws DataElementLookupException Bad lookup.
     */
    @Test
    public void testAllQueriesRemoved() throws InterruptedException, DataElementLookupException
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        TimeManager timeManager = createTimeManager(support);
        TimeSpan loadSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        CountDownLatch latch = new CountDownLatch(1);
        DataRegistry dataRegistry = createRegistry(support, New.list(loadSpan), New.list(latch));

        QueryRegionManager queryManager = createRegionManager(support);
        List<DataTypeInfo> dataTypes = createTypes(support, TimeSpan.get(1000, System.currentTimeMillis()));
        DataTypeInfo wfsType = dataTypes.get(dataTypes.size() - 1);
        DataTypeController typeController = createTypeControllerRemove(support, dataTypes, wfsType, new long[] { 0, 1 });

        List<Long> allIds = New.list(Long.valueOf(0), Long.valueOf(1));
        DataElementCache cache = createCache(support, wfsType, allIds, null);

        List<MapGeometrySupport> geometrySupports = New.list();
        List<DataElement> elements = createElements(loadSpan, geometrySupports);
        DataElementLookupUtils lookupUtils = createLookup(support, wfsType, allIds, elements);

        MapGeometrySupportConverterRegistry converter = createConverter(support, geometrySupports);

        MantleToolbox mantle = createMantle(support, queryManager, typeController, cache, lookupUtils, converter);
        Toolbox toolbox = createToolbox(support, eventManager, timeManager, dataRegistry, mantle);
        QueryRegion region = createRegion(support);

        support.replayAll();

        timeManager.getLoadTimeSpans().add(loadSpan);
        WFSQueryController controller = new WFSQueryController(toolbox);

        myRegionListener.queryRegionAdded(region);

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        myRegionListener.allQueriesRemoved(false);

        assertTrue(controller.getGovernorManager().findGovernors(p -> true).isEmpty());

        controller.close();

        assertNull(myEventListener);
        assertNull(myRegionListener);

        support.verifyAll();
    }

    /**
     * Tests when load spans are added.
     *
     * @throws DataElementLookupException Bad lookup.
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testElementsAdded() throws DataElementLookupException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        TimeManager timeManager = createTimeManager(support);
        TimeSpan loadSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        CountDownLatch latch = new CountDownLatch(1);
        TimeSpan loadSpan2 = TimeSpan.get(System.currentTimeMillis() - 20000, System.currentTimeMillis() - 15000);
        CountDownLatch latch2 = new CountDownLatch(1);
        DataRegistry dataRegistry = createRegistry(support, New.list(loadSpan, loadSpan2), New.list(latch, latch2));

        QueryRegionManager queryManager = createRegionManager(support);
        List<DataTypeInfo> dataTypes = createTypes(support, TimeSpan.get(1000, System.currentTimeMillis()));
        DataTypeInfo wfsType = dataTypes.get(dataTypes.size() - 1);
        DataTypeController typeController = createTypeControllerRemove(support, dataTypes, wfsType, new long[] { 0, 1 });

        List<Long> allIds = New.list(Long.valueOf(0), Long.valueOf(1));
        DataElementCache cache = createCache(support, wfsType, allIds, null);

        List<MapGeometrySupport> geometrySupports = New.list();
        List<DataElement> elements = createElements(loadSpan, geometrySupports);
        DataElementLookupUtils lookupUtils = createLookup(support, wfsType, allIds, elements);

        MapGeometrySupportConverterRegistry converter = createConverter(support, geometrySupports);

        MantleToolbox mantle = createMantle(support, queryManager, typeController, cache, lookupUtils, converter);
        Toolbox toolbox = createToolbox(support, eventManager, timeManager, dataRegistry, mantle);
        QueryRegion region = createRegion(support);

        support.replayAll();

        timeManager.getLoadTimeSpans().add(loadSpan);
        WFSQueryController controller = new WFSQueryController(toolbox);

        myRegionListener.queryRegionAdded(region);

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        timeManager.getLoadTimeSpans().add(loadSpan2);

        assertTrue(latch2.await(5, TimeUnit.SECONDS));

        controller.close();

        assertNull(myEventListener);
        assertNull(myRegionListener);

        support.verifyAll();
    }

    /**
     * Tests when a load span is changed to a completely new span.
     *
     * @throws InterruptedException Don't interrupt.
     * @throws DataElementLookupException Bad lookup.
     */
    @Test
    public void testElementsChangedComplete() throws InterruptedException, DataElementLookupException
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        TimeManager timeManager = createTimeManager(support);
        TimeSpan loadSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        CountDownLatch latch = new CountDownLatch(1);
        TimeSpan loadSpan2 = TimeSpan.get(System.currentTimeMillis() - 20000, System.currentTimeMillis() - 15000);
        CountDownLatch latch2 = new CountDownLatch(1);
        DataRegistry dataRegistry = createRegistry(support, New.list(loadSpan, loadSpan2), New.list(latch, latch2));

        QueryRegionManager queryManager = createRegionManager(support);
        List<DataTypeInfo> dataTypes = createTypes(support, TimeSpan.get(1000, System.currentTimeMillis()));
        DataTypeInfo wfsType = dataTypes.get(dataTypes.size() - 1);
        DataTypeController typeController = createTypeControllerRemove(support, dataTypes, wfsType, new long[] { 0, 1 });
        EasyMock.expectLastCall().times(2);

        List<Long> allIds = New.list(Long.valueOf(0), Long.valueOf(1));
        DataElementCache cache = createCache(support, wfsType, allIds, New.list(loadSpan, loadSpan));
        EasyMock.expect(cache.getElementIdsForTypeAsList(wfsType)).andReturn(allIds);

        List<MapGeometrySupport> geometrySupports = New.list();
        List<DataElement> elements = createElements(loadSpan, geometrySupports);
        DataElementLookupUtils lookupUtils = createLookup(support, wfsType, allIds, elements);
        EasyMock.expectLastCall().times(2);

        List<MapGeometrySupport> allSupports = New.list();
        allSupports.addAll(geometrySupports);
        allSupports.addAll(geometrySupports);
        MapGeometrySupportConverterRegistry converter = createConverter(support, allSupports);

        MantleToolbox mantle = createMantle(support, queryManager, typeController, cache, lookupUtils, converter);
        Toolbox toolbox = createToolbox(support, eventManager, timeManager, dataRegistry, mantle);
        QueryRegion region = createRegion(support);

        support.replayAll();

        timeManager.getLoadTimeSpans().add(loadSpan);
        WFSQueryController controller = new WFSQueryController(toolbox);

        myRegionListener.queryRegionAdded(region);

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        timeManager.getLoadTimeSpans().set(0, loadSpan2);

        assertTrue(latch2.await(5, TimeUnit.SECONDS));

        controller.close();

        assertNull(myEventListener);
        assertNull(myRegionListener);

        support.verifyAll();
    }

    /**
     * Tests when a load span is changed to a larger span.
     *
     * @throws InterruptedException Don't interrupt.
     * @throws DataElementLookupException Bad lookup.
     */
    @Test
    public void testElementsChangedLargerSpan() throws InterruptedException, DataElementLookupException
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        TimeManager timeManager = createTimeManager(support);
        TimeSpan loadSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        CountDownLatch latch = new CountDownLatch(1);
        TimeSpan loadSpan2 = TimeSpan.get(System.currentTimeMillis() - 20000, loadSpan.getStart());
        CountDownLatch latch2 = new CountDownLatch(1);
        TimeSpan loadSpan3 = TimeSpan.get(loadSpan.getEnd(), System.currentTimeMillis() + 10000);
        CountDownLatch latch3 = new CountDownLatch(1);
        TimeSpan theLoadSpan = TimeSpan.get(loadSpan2.getStart(), loadSpan3.getEnd());
        DataRegistry dataRegistry = createRegistry(support, New.list(loadSpan, loadSpan2, loadSpan3),
                New.list(latch, latch2, latch3));

        QueryRegionManager queryManager = createRegionManager(support);
        List<DataTypeInfo> dataTypes = createTypes(support, TimeSpan.get(1000, System.currentTimeMillis()));
        DataTypeInfo wfsType = dataTypes.get(dataTypes.size() - 1);
        DataTypeController typeController = createTypeControllerRemove(support, dataTypes, wfsType, new long[] { 0, 1 });

        List<Long> allIds = New.list(Long.valueOf(0), Long.valueOf(1));
        DataElementCache cache = createCache(support, wfsType, allIds, null);

        List<MapGeometrySupport> geometrySupports = New.list();
        List<DataElement> elements = createElements(loadSpan, geometrySupports);
        DataElementLookupUtils lookupUtils = createLookup(support, wfsType, allIds, elements);

        MapGeometrySupportConverterRegistry converter = createConverter(support, geometrySupports);

        MantleToolbox mantle = createMantle(support, queryManager, typeController, cache, lookupUtils, converter);
        Toolbox toolbox = createToolbox(support, eventManager, timeManager, dataRegistry, mantle);
        QueryRegion region = createRegion(support);

        support.replayAll();

        timeManager.getLoadTimeSpans().add(loadSpan);
        WFSQueryController controller = new WFSQueryController(toolbox);

        myRegionListener.queryRegionAdded(region);

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        timeManager.getLoadTimeSpans().set(0, theLoadSpan);

        assertTrue(latch2.await(5, TimeUnit.SECONDS));
        assertTrue(latch3.await(5, TimeUnit.SECONDS));

        controller.close();

        assertNull(myEventListener);
        assertNull(myRegionListener);

        support.verifyAll();
    }

    /**
     * Tests when a load span is changed to a smaller time.
     *
     * @throws DataElementLookupException Bad lookup.
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testElementsChangedSmallerSpan() throws DataElementLookupException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        TimeManager timeManager = createTimeManager(support);
        TimeSpan loadSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        CountDownLatch latch = new CountDownLatch(1);
        TimeSpan loadSpan2 = TimeSpan.get(System.currentTimeMillis() - 5000, System.currentTimeMillis() - 1000);
        DataRegistry dataRegistry = createRegistry(support, New.list(loadSpan), New.list(latch));

        QueryRegionManager queryManager = createRegionManager(support);
        List<DataTypeInfo> dataTypes = createTypes(support, TimeSpan.get(1000, System.currentTimeMillis()));
        DataTypeInfo wfsType = dataTypes.get(dataTypes.size() - 1);
        DataTypeController typeController = createTypeControllerRemove(support, dataTypes, wfsType, new long[] { 0 });
        typeController.removeDataElements(EasyMock.eq(wfsType), EasyMock.aryEq(new long[] { 1 }));
        typeController.removeDataElements(EasyMock.eq(wfsType), EasyMock.aryEq(new long[] { 2, 3 }));

        List<Long> allIds = New.list(Long.valueOf(0), Long.valueOf(1), Long.valueOf(2), Long.valueOf(3));
        DataElementCache cache = createCache(support, wfsType, allIds,
                New.list(TimeSpan.get(loadSpan.getStart() + 2000), TimeSpan.get(loadSpan.getEnd() - 500), loadSpan2, loadSpan2));
        EasyMock.expect(cache.getElementIdsForTypeAsList(wfsType))
                .andReturn(New.list(Long.valueOf(1), Long.valueOf(2), Long.valueOf(3)));
        EasyMock.expect(cache.getTimeSpans(EasyMock.eq(New.list(Long.valueOf(1), Long.valueOf(2), Long.valueOf(3)))))
                .andReturn(New.list(TimeSpan.get(loadSpan.getEnd() - 500), loadSpan2, loadSpan2));
        EasyMock.expect(cache.getElementIdsForTypeAsList(wfsType)).andReturn(New.list(Long.valueOf(2), Long.valueOf(3)));

        List<MapGeometrySupport> geometrySupports = New.list();

        List<DataElement> elements = createElements(loadSpan, geometrySupports);
        SimpleMapPointGeometrySupport point = new SimpleMapPointGeometrySupport(LatLonAlt.createFromDegrees(10.1, 10.1));
        DataElement element = new DefaultMapDataElement(2, loadSpan, point);
        elements.add(element);
        geometrySupports.add(point);

        point = new SimpleMapPointGeometrySupport(LatLonAlt.createFromDegrees(10.8, 10.8));
        element = new DefaultMapDataElement(3, loadSpan, point);
        elements.add(element);
        geometrySupports.add(point);

        DataElementLookupUtils lookupUtils = createLookup(support, wfsType, New.list(allIds.get(0)), New.list(elements.get(0)));
        EasyMock.expect(lookupUtils.getDataElements(New.list(allIds.get(1)), wfsType, null, false))
                .andReturn(New.list(elements.get(1)));
        EasyMock.expect(lookupUtils.getDataElements(New.list(allIds.get(2), allIds.get(3)), wfsType, null, false))
                .andReturn(New.list(elements.get(2), elements.get(3)));

        List<MapGeometrySupport> allSupports = New.list();
        allSupports.addAll(geometrySupports);
        MapGeometrySupportConverterRegistry converter = createConverter(support, allSupports);

        MantleToolbox mantle = createMantle(support, queryManager, typeController, cache, lookupUtils, converter);
        Toolbox toolbox = createToolbox(support, eventManager, timeManager, dataRegistry, mantle);
        QueryRegion region = createRegion(support);

        support.replayAll();

        timeManager.getLoadTimeSpans().add(loadSpan);
        WFSQueryController controller = new WFSQueryController(toolbox);

        myRegionListener.queryRegionAdded(region);

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        timeManager.getLoadTimeSpans().set(0, loadSpan2);

        controller.close();

        assertNull(myEventListener);
        assertNull(myRegionListener);

        support.verifyAll();
    }

    /**
     * Tests when a load span is removed.
     *
     * @throws InterruptedException Don't interrupt.
     * @throws DataElementLookupException Bad lookup.
     */
    @Test
    public void testElementsRemoved() throws InterruptedException, DataElementLookupException
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        TimeManager timeManager = createTimeManager(support);
        TimeSpan loadSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        CountDownLatch latch = new CountDownLatch(1);
        DataRegistry dataRegistry = createRegistry(support, New.list(loadSpan), New.list(latch));

        QueryRegionManager queryManager = createRegionManager(support);
        List<DataTypeInfo> dataTypes = createTypes(support, TimeSpan.get(1000, System.currentTimeMillis()));
        DataTypeInfo wfsType = dataTypes.get(dataTypes.size() - 1);
        DataTypeController typeController = createTypeControllerRemove(support, dataTypes, wfsType, new long[] { 0, 1 });
        EasyMock.expectLastCall().times(2);

        List<Long> allIds = New.list(Long.valueOf(0), Long.valueOf(1));
        DataElementCache cache = createCache(support, wfsType, allIds, New.list(loadSpan, loadSpan));
        EasyMock.expect(cache.getElementIdsForTypeAsList(wfsType)).andReturn(allIds);

        List<MapGeometrySupport> geometrySupports = New.list();
        List<DataElement> elements = createElements(loadSpan, geometrySupports);
        DataElementLookupUtils lookupUtils = createLookup(support, wfsType, allIds, elements);
        EasyMock.expectLastCall().times(2);

        List<MapGeometrySupport> allSupports = New.list();
        allSupports.addAll(geometrySupports);
        allSupports.addAll(geometrySupports);
        MapGeometrySupportConverterRegistry converter = createConverter(support, allSupports);

        MantleToolbox mantle = createMantle(support, queryManager, typeController, cache, lookupUtils, converter);
        Toolbox toolbox = createToolbox(support, eventManager, timeManager, dataRegistry, mantle);
        QueryRegion region = createRegion(support);

        support.replayAll();

        timeManager.getLoadTimeSpans().add(loadSpan);
        WFSQueryController controller = new WFSQueryController(toolbox);

        myRegionListener.queryRegionAdded(region);

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        timeManager.getLoadTimeSpans().remove(0);

        controller.close();

        assertNull(myEventListener);
        assertNull(myRegionListener);

        support.verifyAll();
    }

    /**
     * Tests when a wfs layer is deactivated.
     *
     * @throws DataElementLookupException Bad lookup.
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testNotifyActiveDataGroupsChangedEvent() throws DataElementLookupException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        TimeManager timeManager = createTimeManager(support);
        TimeSpan loadSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        CountDownLatch latch = new CountDownLatch(1);
        DataRegistry dataRegistry = createRegistry(support, New.list(loadSpan), New.list(latch));

        QueryRegionManager queryManager = createRegionManager(support);
        List<DataTypeInfo> dataTypes = createTypes(support, TimeSpan.get(1000, System.currentTimeMillis()));
        DataTypeInfo wfsType = dataTypes.get(dataTypes.size() - 1);
        DataTypeController typeController = createTypeControllerRemove(support, dataTypes, wfsType, new long[] { 0, 1 });

        List<Long> allIds = New.list(Long.valueOf(0), Long.valueOf(1));
        DataElementCache cache = createCache(support, wfsType, allIds, null);

        List<MapGeometrySupport> geometrySupports = New.list();
        List<DataElement> elements = createElements(loadSpan, geometrySupports);
        DataElementLookupUtils lookupUtils = createLookup(support, wfsType, allIds, elements);

        MapGeometrySupportConverterRegistry converter = createConverter(support, geometrySupports);

        MantleToolbox mantle = createMantle(support, queryManager, typeController, cache, lookupUtils, converter);
        Toolbox toolbox = createToolbox(support, eventManager, timeManager, dataRegistry, mantle);
        QueryRegion region = createRegion(support);

        DataGroupInfo group = support.createMock(DataGroupInfo.class);
        EasyMock.expect(group.getMembers(false)).andReturn(New.set(dataTypes));

        support.replayAll();

        timeManager.getLoadTimeSpans().add(loadSpan);
        WFSQueryController controller = new WFSQueryController(toolbox);

        myRegionListener.queryRegionAdded(region);

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        ActiveDataGroupsChangedEvent event = new ActiveDataGroupsChangedEvent(this, New.set(), New.set(group));
        myEventListener.notify(event);

        assertTrue(controller.getGovernorManager().findGovernors(p -> true).isEmpty());

        controller.close();

        assertNull(myEventListener);
        assertNull(myRegionListener);

        support.verifyAll();
    }

    /**
     * Tests when a new query region is added.
     *
     * @throws DataElementLookupException Bad lookup.
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testQueryRegionAdded() throws DataElementLookupException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        TimeManager timeManager = createTimeManager(support);
        TimeSpan loadSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        CountDownLatch latch = new CountDownLatch(1);
        DataRegistry dataRegistry = createRegistry(support, New.list(loadSpan), New.list(latch));

        QueryRegionManager queryManager = createRegionManager(support);
        List<DataTypeInfo> dataTypes = createTypes(support, TimeSpan.get(1000, System.currentTimeMillis()));
        DataTypeInfo wfsType = dataTypes.get(dataTypes.size() - 1);
        DataTypeController typeController = createTypeControllerRemove(support, dataTypes, wfsType, new long[] { 0, 1 });

        List<Long> allIds = New.list(Long.valueOf(0), Long.valueOf(1));
        DataElementCache cache = createCache(support, wfsType, allIds, null);

        List<MapGeometrySupport> geometrySupports = New.list();
        List<DataElement> elements = createElements(loadSpan, geometrySupports);
        DataElementLookupUtils lookupUtils = createLookup(support, wfsType, allIds, elements);

        MapGeometrySupportConverterRegistry converter = createConverter(support, geometrySupports);

        MantleToolbox mantle = createMantle(support, queryManager, typeController, cache, lookupUtils, converter);
        Toolbox toolbox = createToolbox(support, eventManager, timeManager, dataRegistry, mantle);
        QueryRegion region = createRegion(support);

        support.replayAll();

        timeManager.getLoadTimeSpans().add(loadSpan);
        WFSQueryController controller = new WFSQueryController(toolbox);

        myRegionListener.queryRegionAdded(region);

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        controller.close();

        assertNull(myEventListener);
        assertNull(myRegionListener);

        support.verifyAll();
    }

    /**
     * Tests when a new query region is added and the layer is timeless.
     *
     * @throws DataElementLookupException Bad lookup.
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testQueryRegionAddedTimelessLayer() throws DataElementLookupException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        TimeManager timeManager = createTimeManager(support);
        TimeSpan loadSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        CountDownLatch latch = new CountDownLatch(1);
        DataRegistry dataRegistry = createRegistry(support, New.list(TimeSpan.TIMELESS), New.list(latch));

        QueryRegionManager queryManager = createRegionManager(support);
        List<DataTypeInfo> dataTypes = createTypes(support, null);
        DataTypeInfo wfsType = dataTypes.get(dataTypes.size() - 1);
        DataTypeController typeController = createTypeControllerRemove(support, dataTypes, wfsType, new long[] { 0, 1 });

        List<Long> allIds = New.list(Long.valueOf(0), Long.valueOf(1));
        DataElementCache cache = createCache(support, wfsType, allIds, null);

        List<MapGeometrySupport> geometrySupports = New.list();
        List<DataElement> elements = createElements(TimeSpan.TIMELESS, geometrySupports);
        DataElementLookupUtils lookupUtils = createLookup(support, wfsType, allIds, elements);

        MapGeometrySupportConverterRegistry converter = createConverter(support, geometrySupports);

        MantleToolbox mantle = createMantle(support, queryManager, typeController, cache, lookupUtils, converter);
        Toolbox toolbox = createToolbox(support, eventManager, timeManager, dataRegistry, mantle);
        QueryRegion region = createRegion(support);

        support.replayAll();

        timeManager.getLoadTimeSpans().add(loadSpan);
        WFSQueryController controller = new WFSQueryController(toolbox);

        myRegionListener.queryRegionAdded(region);

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        controller.close();

        assertNull(myEventListener);
        assertNull(myRegionListener);

        support.verifyAll();
    }

    /**
     * Tests when a query region is removed.
     *
     * @throws DataElementLookupException Bad lookup.
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testQueryRegionRemoved() throws DataElementLookupException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        TimeManager timeManager = createTimeManager(support);
        TimeSpan loadSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        CountDownLatch latch = new CountDownLatch(1);
        DataRegistry dataRegistry = createRegistry(support, New.list(loadSpan), New.list(latch));

        QueryRegionManager queryManager = createRegionManager(support);
        List<DataTypeInfo> dataTypes = createTypes(support, TimeSpan.get(1000, System.currentTimeMillis()));
        DataTypeInfo wfsType = dataTypes.get(dataTypes.size() - 1);
        DataTypeController typeController = createTypeControllerRemove(support, dataTypes, wfsType, new long[] { 0, 1 });

        List<Long> allIds = New.list(Long.valueOf(0), Long.valueOf(1));
        DataElementCache cache = createCache(support, wfsType, allIds, null);

        List<MapGeometrySupport> geometrySupports = New.list();
        List<DataElement> elements = createElements(loadSpan, geometrySupports);
        DataElementLookupUtils lookupUtils = createLookup(support, wfsType, allIds, elements);

        MapGeometrySupportConverterRegistry converter = createConverter(support, geometrySupports);

        MantleToolbox mantle = createMantle(support, queryManager, typeController, cache, lookupUtils, converter);
        Toolbox toolbox = createToolbox(support, eventManager, timeManager, dataRegistry, mantle);
        QueryRegion region = createRegion(support);

        support.replayAll();

        timeManager.getLoadTimeSpans().add(loadSpan);
        WFSQueryController controller = new WFSQueryController(toolbox);

        myRegionListener.queryRegionAdded(region);

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        myRegionListener.queryRegionRemoved(region);

        assertTrue(controller.getGovernorManager().findGovernors(p -> true).isEmpty());

        controller.close();

        assertNull(myEventListener);
        assertNull(myRegionListener);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link DataElementCache}.
     *
     * @param support Used to create the mock.
     * @param layer The layer to expect.
     * @param elementIds The element ids to expect.
     * @param spans The time spans to return or null if not expected the time
     *            span call.
     * @return The {@link DataElementCache}.
     */
    private DataElementCache createCache(EasyMockSupport support, DataTypeInfo layer, List<Long> elementIds, List<TimeSpan> spans)
    {
        DataElementCache cache = support.createMock(DataElementCache.class);

        EasyMock.expect(cache.getElementIdsForTypeAsList(layer)).andReturn(elementIds);
        if (spans != null)
        {
            EasyMock.expect(cache.getTimeSpans(EasyMock.eq(elementIds))).andReturn(spans);
        }

        return cache;
    }

    /**
     * Creates a mocked {@link MapGeometrySupportConverterRegistry}.
     *
     * @param support Used to create the mock.
     * @param geometrySupports The test mantle geometries.
     * @return Returns a mocked {@link MapGeometrySupportConverterRegistry}.
     */
    private MapGeometrySupportConverterRegistry createConverter(EasyMockSupport support,
            List<MapGeometrySupport> geometrySupports)
    {
        MapGeometrySupportConverterRegistry converter = support.createMock(MapGeometrySupportConverterRegistry.class);

        Toolbox toolbox = support.createMock(Toolbox.class);
        MapPointGeometryConverter pointConverter = new MapPointGeometryConverter(toolbox);

        for (MapGeometrySupport geometrySupport : geometrySupports)
        {
            EasyMock.expect(converter.getConverter(geometrySupport)).andReturn(pointConverter);
        }

        return converter;
    }

    /**
     * Creates the test elements.
     *
     * @param span The first load span.
     * @param supports Adds the map geometry support to that list.
     * @return Returns the test elements.
     */
    private List<DataElement> createElements(TimeSpan span, List<MapGeometrySupport> supports)
    {
        List<DataElement> elements = New.list();

        SimpleMapPointGeometrySupport point = new SimpleMapPointGeometrySupport(LatLonAlt.createFromDegrees(10.1, 10.1));
        DataElement element = new DefaultMapDataElement(0, span, point);
        elements.add(element);
        supports.add(point);

        point = new SimpleMapPointGeometrySupport(LatLonAlt.createFromDegrees(10.8, 10.8));
        element = new DefaultMapDataElement(1, span, point);
        elements.add(element);
        supports.add(point);

        return elements;
    }

    /**
     * Creates an easy mocked {@link EventManager}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link EventManager}.
     */
    @SuppressWarnings("unchecked")
    private EventManager createEventManager(EasyMockSupport support)
    {
        EventManager eventManager = support.createMock(EventManager.class);

        eventManager.subscribe(EasyMock.eq(ActiveDataGroupsChangedEvent.class), EasyMock.isA(EventListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myEventListener = (EventListener<ActiveDataGroupsChangedEvent>)EasyMock.getCurrentArguments()[1];
            return null;
        });

        eventManager.unsubscribe(EasyMock.eq(ActiveDataGroupsChangedEvent.class), EasyMock.isA(EventListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            if (myEventListener.equals(EasyMock.getCurrentArguments()[1]))
            {
                myEventListener = null;
            }

            return null;
        });

        return eventManager;
    }

    /**
     * Creates a mocked {@link DataElementLookupUtils}.
     *
     * @param support Used to create the mock.
     * @param layer The layer.
     * @param ids The ids to expect.
     * @param elements The elements to return.
     * @return The mocked {@link DataElementLookupUtils}.
     * @throws DataElementLookupException Bad lookup.
     */
    private DataElementLookupUtils createLookup(EasyMockSupport support, DataTypeInfo layer, List<Long> ids,
            List<DataElement> elements)
        throws DataElementLookupException
    {
        DataElementLookupUtils lookup = support.createMock(DataElementLookupUtils.class);

        EasyMock.expect(lookup.getDataElements(ids, layer, null, false)).andReturn(elements);

        return lookup;
    }

    /**
     * Creates an easy mocked {@link MantleToolbox}.
     *
     * @param support Used to create the mocked.
     * @param queryManager A mocked {@link QueryRegionManager} to return from
     *            mantle.
     * @param typeController A mocked {@link DataTypeController} to return from
     *            mantle.
     * @param cache A mocked {@link DataElementCache} to return from mantle.
     * @param lookupUtils A mocked {@link DataElementLookupUtils} to return from
     *            mantle.
     * @param converter A mocked {@link MapGeometrySupportConverterRegistry} to
     *            return from mantle.
     * @return A mocked {@link MantleToolbox}.
     */
    private MantleToolbox createMantle(EasyMockSupport support, QueryRegionManager queryManager,
            DataTypeController typeController, DataElementCache cache, DataElementLookupUtils lookupUtils,
            MapGeometrySupportConverterRegistry converter)
    {
        MantleToolbox mantle = support.createMock(MantleToolbox.class);

        EasyMock.expect(mantle.getQueryRegionManager()).andReturn(queryManager);
        EasyMock.expect(mantle.getDataTypeController()).andReturn(typeController).atLeastOnce();
        EasyMock.expect(mantle.getDataElementCache()).andReturn(cache);
        EasyMock.expect(mantle.getDataElementLookupUtils()).andReturn(lookupUtils);
        EasyMock.expect(mantle.getMapGeometrySupportConverterRegistry()).andReturn(converter);

        return mantle;
    }

    /**
     * Creates the region to test with.
     *
     * @param support Used to create the mock.
     * @return The mocked query region.
     */
    private QueryRegion createRegion(EasyMockSupport support)
    {
        QueryRegion region = support.createMock(QueryRegion.class);

        EasyMock.expect(region.getTypeKeyToFilterMap()).andReturn(New.map()).anyTimes();

        PolygonGeometry.Builder<GeographicPosition> builder = new PolygonGeometry.Builder<>();
        builder.setVertices(New.list(new GeographicPosition(LatLonAlt.createFromDegrees(10, 10)),
                new GeographicPosition(LatLonAlt.createFromDegrees(10, 11)),
                new GeographicPosition(LatLonAlt.createFromDegrees(11, 11)),
                new GeographicPosition(LatLonAlt.createFromDegrees(11, 10))));
        DefaultPolygonRenderProperties props = new DefaultPolygonRenderProperties(0, true, false);
        PolygonGeometry geometry = new PolygonGeometry(builder, props, null);

        region.getGeometries();
        EasyMock.expectLastCall().andReturn(New.list(geometry)).atLeastOnce();

        return region;
    }

    /**
     * Creates an easy mocked {@link QueryRegionManager}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link QueryRegionManager}.
     */
    private QueryRegionManager createRegionManager(EasyMockSupport support)
    {
        QueryRegionManager regionManager = support.createMock(QueryRegionManager.class);

        regionManager.addQueryRegionListener(EasyMock.isA(QueryRegionListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myRegionListener = (QueryRegionListener)EasyMock.getCurrentArguments()[0];
            return null;
        });

        regionManager.removeQueryRegionListener(EasyMock.isA(QueryRegionListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            if (myRegionListener.equals(EasyMock.getCurrentArguments()[0]))
            {
                myRegionListener = null;
            }
            return null;
        });

        return regionManager;
    }

    /**
     * Creates an easy mocked {@link DataRegistry}.
     *
     * @param support Used to create the mock.
     * @param spans The query's expected time span.
     * @param latches Used to synchronize tests.
     * @return The mocked {@link DataRegistry}.
     */
    private DataRegistry createRegistry(EasyMockSupport support, List<TimeSpan> spans, List<CountDownLatch> latches)
    {
        DataRegistry registry = support.createMock(DataRegistry.class);

        int index = 0;
        for (TimeSpan span : spans)
        {
            CountDownLatch latch = latches.get(index);
            QueryTracker tracker = support.createMock(QueryTracker.class);
            tracker.logException();
            tracker.addListener(EasyMock.isA(WFSGovernor.class));
            EasyMock.expectLastCall().andAnswer(() ->
            {
                ((QueryTracker.QueryTrackerListener)EasyMock.getCurrentArguments()[0]).statusChanged(tracker,
                        QueryStatus.SUCCESS);
                latch.countDown();
                return null;
            });

            EasyMock.expect(registry.submitQuery(EasyMock.isA(DefaultQuery.class))).andAnswer(() ->
            {
                DefaultQuery query = (DefaultQuery)EasyMock.getCurrentArguments()[0];

                DataModelCategory expected = new DataModelCategory(null, MapDataElement.class.getName(), ourTypeKey);

                assertEquals(expected, query.getDataModelCategory());

                assertEquals(2, query.getParameters().size());

                assertEquals(span, query.getParameters().get(1).getOperand());
                assertEquals(TimeSpanAccessor.PROPERTY_DESCRIPTOR, query.getParameters().get(1).getPropertyDescriptor());
                assertTrue(query.getParameters().get(1) instanceof TimeSpanMatcher);

                GeometryMatcher geomMatcher = (GeometryMatcher)query.getParameters().get(0);
                assertEquals(GeometryAccessor.PROPERTY_DESCRIPTOR, geomMatcher.getPropertyDescriptor());
                assertEquals(GeometryMatcher.OperatorType.INTERSECTS, geomMatcher.getOperator());

                Polygon polygon = (Polygon)geomMatcher.getOperand();

                assertEquals(5, polygon.getCoordinates().length);

                assertEquals(10, polygon.getCoordinates()[0].x, 0d);
                assertEquals(10, polygon.getCoordinates()[0].y, 0d);

                assertEquals(11, polygon.getCoordinates()[1].x, 0d);
                assertEquals(10, polygon.getCoordinates()[1].y, 0d);

                assertEquals(11, polygon.getCoordinates()[2].x, 0d);
                assertEquals(11, polygon.getCoordinates()[2].y, 0d);

                assertEquals(10, polygon.getCoordinates()[3].x, 0d);
                assertEquals(11, polygon.getCoordinates()[3].y, 0d);

                assertEquals(10, polygon.getCoordinates()[4].x, 0d);
                assertEquals(10, polygon.getCoordinates()[4].y, 0d);

                return tracker;
            });

            index++;
        }

        return registry;
    }

    /**
     * Creates an easy mocked {@link TimeManager}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link TimeManager}.
     */
    private TimeManager createTimeManager(EasyMockSupport support)
    {
        TimeManager timeManager = support.createMock(TimeManager.class);

        EasyMock.expect(timeManager.getLoadTimeSpans()).andReturn(new ObservableList<>()).anyTimes();

        return timeManager;
    }

    /**
     * Creates a mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @param eventManager A mocked {@link EventManager} to return from toolbox.
     * @param timeManager A mocked {@link TimeManager} to return from toolbox.
     * @param dataRegistry A mocked {@link DataRegistry} to return from toolbox.
     * @param mantle A mocked {@link MantleToolbox} to return from toolbox.
     * @return A mocked {@link Toolbox}.
     */
    private Toolbox createToolbox(EasyMockSupport support, EventManager eventManager, TimeManager timeManager,
            DataRegistry dataRegistry, MantleToolbox mantle)
    {
        Toolbox toolbox = support.createMock(Toolbox.class);

        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager);
        EasyMock.expect(toolbox.getTimeManager()).andReturn(timeManager);
        EasyMock.expect(toolbox.getDataRegistry()).andReturn(dataRegistry);

        PluginToolboxRegistry pluginToolbox = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(pluginToolbox.getPluginToolbox(MantleToolbox.class)).andReturn(mantle);
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(pluginToolbox);

        return toolbox;
    }

    /**
     * Creates an easy mocked {@link DataTypeController}.
     *
     * @param support Used to create the mock.
     * @param dataTypes The data types to return.
     * @return The mocked {@link DataTypeController}.
     */
    private DataTypeController createTypeController(EasyMockSupport support, List<DataTypeInfo> dataTypes)
    {
        DataTypeController typeController = support.createMock(DataTypeController.class);

        EasyMock.expect(typeController.getDataTypeInfo()).andReturn(dataTypes);

        return typeController;
    }

    /**
     * Creates a mocked {@link DataTypeController}.
     *
     * @param support Used to create the mock.
     * @param allTypes The the layers the type controller will return.
     * @param layer The test layer.
     * @param ids The ids to expect to be removed.
     * @return The mocked {@link DataTypeController}.
     */
    private DataTypeController createTypeControllerRemove(EasyMockSupport support, List<DataTypeInfo> allTypes,
            DataTypeInfo layer, long[] ids)
    {
        DataTypeController controller = createTypeController(support, allTypes);

        controller.removeDataElements(EasyMock.eq(layer), EasyMock.aryEq(ids));

        return controller;
    }

    /**
     * Creates the test layers.
     *
     * @param support Used to create the layers.
     * @param extents The time extents of the wfs layer.
     * @return The test layers.
     */
    private List<DataTypeInfo> createTypes(EasyMockSupport support, TimeSpan extents)
    {
        List<DataTypeInfo> types = New.list();

        DataTypeInfo other = support.createMock(DataTypeInfo.class);
        types.add(other);

        WFSDataType nonVisible = support.createMock(WFSDataType.class);
        EasyMock.expect(Boolean.valueOf(nonVisible.isVisible())).andReturn(Boolean.FALSE);
        types.add(nonVisible);

        WFSDataType wfsType = support.createMock(WFSDataType.class);
        EasyMock.expect(Boolean.valueOf(wfsType.isVisible())).andReturn(Boolean.TRUE);
        EasyMock.expect(wfsType.getTypeKey()).andReturn(ourTypeKey).atLeastOnce();
        DefaultMapFeatureVisualizationInfo visInfo = new DefaultMapFeatureVisualizationInfo(MapVisualizationType.POINT_ELEMENTS);
        EasyMock.expect(wfsType.getMapVisualizationInfo()).andReturn(visInfo).anyTimes();
        DefaultTimeExtents timeExtents = null;
        if (extents != null)
        {
            timeExtents = new DefaultTimeExtents(extents);
        }
        EasyMock.expect(wfsType.getTimeExtents()).andReturn(timeExtents).anyTimes();

        DefaultBasicVisualizationInfo basicVisInfo = new DefaultBasicVisualizationInfo(LoadsTo.TIMELINE, Color.RED, true);
        EasyMock.expect(wfsType.getBasicVisualizationInfo()).andReturn(basicVisInfo).anyTimes();
        types.add(wfsType);

        return types;
    }
}
