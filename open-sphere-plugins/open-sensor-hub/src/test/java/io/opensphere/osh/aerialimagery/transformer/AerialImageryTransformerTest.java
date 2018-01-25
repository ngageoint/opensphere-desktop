package io.opensphere.osh.aerialimagery.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.AnimationManager;
import io.opensphere.core.MapManager;
import io.opensphere.core.TimeManager;
import io.opensphere.core.TimeManager.ActiveTimeSpanChangeListener;
import io.opensphere.core.TimeManager.ActiveTimeSpans;
import io.opensphere.core.Toolbox;
import io.opensphere.core.animationhelper.TimeRefreshNotifier;
import io.opensphere.core.cache.matcher.NumberPropertyMatcher;
import io.opensphere.core.cache.matcher.NumberPropertyMatcher.OperatorType;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.PropertyValueReceiver;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.PolygonMeshGeometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.model.GeographicConvexQuadrilateral;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.image.ImageUtil;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;
import io.opensphere.osh.aerialimagery.util.Constants;
import io.opensphere.osh.util.OSHImageQuerier;

/**
 * Unit test for {@link AerialImageryTransformer} class.
 */
public class AerialImageryTransformerTest
{
    /**
     * The color.
     */
    private static final Color ourColor = Color.red;

    /**
     * The test zorder.
     */
    private static final int ourImageryZOrder = 121;

    /**
     * The test opacity.
     */
    private static final int ourOpacity = 150;

    /**
     * The test server url.
     */
    private static final String ourServer = "http://somehost";

    /**
     * The test type key.
     */
    private static final String ourTypeKey = "Iamatypekey";

    /**
     * The test zorder.
     */
    private static final int ourUAVZOrder = 121;

    /**
     * The time span change listener.
     */
    private ActiveTimeSpanChangeListener myListener;

    /**
     * The query time.
     */
    private long myQueryTime = System.currentTimeMillis();

    /**
     * Tests drawing all the geometries to the globe on time change and verifies
     * that it doesn't republish the geometries for the same time.
     *
     * @throws IOException Bad IO.
     * @throws QueryException Bad query.
     */
    @Test
    public void test() throws QueryException, IOException
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        TimeManager timeManager = createTimeManager(support);
        AnimationManager animationManager = createAnimationManager(support);
        List<PlatformMetadata> testData = New.list(createMetadata());
        OrderParticipantKey expectedUAVKey = support.createMock(OrderParticipantKey.class);
        OrderParticipantKey expectedVideoKey = support.createMock(OrderParticipantKey.class);
        OrderManagerRegistry orderRegistry = createOrderRegistry(support, expectedUAVKey, expectedVideoKey, 1);
        Toolbox toolbox = createToolbox(support, eventManager, timeManager, animationManager, testData, orderRegistry, 1);

        DataTypeInfo videoLayer = createVideoLayer(support, expectedVideoKey);
        OSHImageQuerier querier = createQuerier(support, videoLayer, 1);
        List<DataTypeInfo> linkedLayer = New.list(videoLayer);

        DataTypeInfo uavLayer = createUAVDataType(support, expectedUAVKey, 1);

        GenericSubscriber<Geometry> receiver = createReceiver(support);

        support.replayAll();

        AerialImageryTransformer transformer = new AerialImageryTransformer(toolbox, querier, uavLayer, linkedLayer);
        transformer.addSubscriber(receiver);
        transformer.open();

        myListener.activeTimeSpansChanged(null);

        support.verifyAll();
    }

    /**
     * Tests closing the transformer.
     *
     * @throws IOException Bad IO.
     * @throws QueryException Bad query.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testClose() throws QueryException, IOException
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        eventManager.unsubscribe(EasyMock.eq(DataTypeInfoColorChangeEvent.class), EasyMock.isA(AerialImageryTransformer.class));
        TimeManager timeManager = createTimeManager(support);
        timeManager.removeActiveTimeSpanChangeListener(EasyMock.isA(TimeRefreshNotifier.class));
        AnimationManager animationManager = createAnimationManager(support);
        animationManager.removeAnimationChangeListener(EasyMock.isA(TimeRefreshNotifier.class));
        List<PlatformMetadata> testData = New.list(createMetadata());
        OrderParticipantKey expectedUAVKey = support.createMock(OrderParticipantKey.class);
        OrderParticipantKey expectedVideoKey = support.createMock(OrderParticipantKey.class);
        OrderManagerRegistry orderRegistry = createOrderRegistry(support, expectedUAVKey, expectedVideoKey, 1);
        Toolbox toolbox = createToolbox(support, eventManager, timeManager, animationManager, testData, orderRegistry, 1);

        DataTypeInfo videoLayer = createVideoLayer(support, expectedVideoKey);
        OSHImageQuerier querier = createQuerier(support, videoLayer, 1);
        List<DataTypeInfo> linkedLayer = New.list(videoLayer);

        DataTypeInfo uavLayer = createUAVDataType(support, expectedUAVKey, 1);

        GenericSubscriber<Geometry> receiver = createReceiver(support);
        receiver.receiveObjects(EasyMock.isA(AerialImageryTransformer.class), EasyMock.isA(List.class), EasyMock.isA(List.class));
        EasyMock.expectLastCall().andAnswer(this::removeGeometriesAnswer);

        support.replayAll();

        AerialImageryTransformer transformer = new AerialImageryTransformer(toolbox, querier, uavLayer, linkedLayer);
        transformer.addSubscriber(receiver);
        transformer.open();

        myListener.activeTimeSpansChanged(null);

        transformer.close();

        support.verifyAll();
    }

    /**
     * Verifies geometries are republished on color change for the linked layer.
     *
     * @throws IOException Bad IO.
     * @throws QueryException Bad query.
     */
    @Test
    public void testNotifyDataTypeInfoColorChangeEvent() throws QueryException, IOException
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        TimeManager timeManager = createTimeManager(support);
        AnimationManager animationManager = createAnimationManager(support);
        List<PlatformMetadata> testData = New.list(createMetadata());
        OrderParticipantKey expectedUAVKey = support.createMock(OrderParticipantKey.class);
        OrderParticipantKey expectedVideoKey = support.createMock(OrderParticipantKey.class);
        OrderManagerRegistry orderRegistry = createOrderRegistry(support, expectedUAVKey, expectedVideoKey, 1);
        Toolbox toolbox = createToolbox(support, eventManager, timeManager, animationManager, testData, orderRegistry, 2);

        DataTypeInfo videoLayer = createVideoLayer(support, expectedVideoKey);
        OSHImageQuerier querier = createQuerier(support, videoLayer, 2);
        List<DataTypeInfo> linkedLayer = New.list(videoLayer);

        DataTypeInfo uavLayer = createUAVDataType(support, expectedUAVKey, 2);

        GenericSubscriber<Geometry> receiver = createReceiver(support);

        support.replayAll();

        AerialImageryTransformer transformer = new AerialImageryTransformer(toolbox, querier, uavLayer, linkedLayer);
        transformer.addSubscriber(receiver);
        transformer.open();

        DataTypeInfoColorChangeEvent event = new DataTypeInfoColorChangeEvent(uavLayer, Color.blue, true, this);
        transformer.notify(event);

        event = new DataTypeInfoColorChangeEvent(videoLayer, Color.black, true, this);
        transformer.notify(event);

        support.verifyAll();
    }

    /**
     * Tests removing old geometries and adding new ones.
     *
     * @throws IOException Bad IO.
     * @throws QueryException Bad query.
     */
    @Test
    public void testSecondRefresh() throws QueryException, IOException
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        TimeManager timeManager = createTimeManager(support);
        AnimationManager animationManager = createAnimationManager(support);
        List<PlatformMetadata> testData = New.list(createMetadata());
        OrderParticipantKey expectedUAVKey = support.createMock(OrderParticipantKey.class);
        OrderParticipantKey expectedVideoKey = support.createMock(OrderParticipantKey.class);
        OrderManagerRegistry orderRegistry = createOrderRegistry(support, expectedUAVKey, expectedVideoKey, 2);
        Toolbox toolbox = createToolbox(support, eventManager, timeManager, animationManager, testData, orderRegistry, 2);

        DataTypeInfo videoLayer = createVideoLayer(support, expectedVideoKey);
        OSHImageQuerier querier = createQuerier(support, videoLayer, 2);
        List<DataTypeInfo> linkedLayer = New.list(videoLayer);

        DataTypeInfo uavLayer = createUAVDataType(support, expectedUAVKey, 2);

        GenericSubscriber<Geometry> receiver = createReceiver(support);
        EasyMock.expectLastCall().andAnswer(this::receiveObjectsAnswerImageryRemoves);
        EasyMock.expectLastCall().andAnswer(this::receiveObjectsAnswerFootprint);

        support.replayAll();

        AerialImageryTransformer transformer = new AerialImageryTransformer(toolbox, querier, uavLayer, linkedLayer);
        transformer.addSubscriber(receiver);
        transformer.open();

        myListener.activeTimeSpansChanged(null);

        myQueryTime += 500;
        GeographicConvexQuadrilateral footprint = new GeographicConvexQuadrilateral(
                new GeographicPosition(LatLonAlt.createFromDegrees(10.1, 0.1)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0.1, 0.1)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0.1, 10.1)),
                new GeographicPosition(LatLonAlt.createFromDegrees(10.1, 10.1)));
        testData.get(0).setTime(new Date(myQueryTime));
        testData.get(0).setFootprint(footprint);

        myListener.activeTimeSpansChanged(null);

        support.verifyAll();
    }

    /**
     * The answer for the mocked active time span call.
     *
     * @return The current active time span.
     */
    private TimeSpanList activeTimeAnswer()
    {
        return TimeSpanList.singleton(TimeSpan.get(myQueryTime));
    }

    /**
     * The answer to the mocked convertToModel call.
     *
     * @return The position as a vector.
     */
    private Vector3d convertToModelAnswer()
    {
        GeographicPosition pos = (GeographicPosition)EasyMock.getCurrentArguments()[0];

        return pos.asVector3d();
    }

    /**
     * Creates a mocked {@link AnimationManager}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link AnimationManager}.
     */
    private AnimationManager createAnimationManager(EasyMockSupport support)
    {
        AnimationManager animationManager = support.createMock(AnimationManager.class);

        animationManager.addAnimationChangeListener(EasyMock.isA(TimeRefreshNotifier.class));

        return animationManager;
    }

    /**
     * Creates a mocked {@link EventManager}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link EventManager}.
     */
    private EventManager createEventManager(EasyMockSupport support)
    {
        EventManager eventManager = support.createMock(EventManager.class);

        eventManager.subscribe(EasyMock.eq(DataTypeInfoColorChangeEvent.class), EasyMock.isA(AerialImageryTransformer.class));

        return eventManager;
    }

    /**
     * Creates an easy mocked map manager.
     *
     * @param support Used to create the mock.
     * @return The mocked map manager.
     */
    private MapManager createMapManager(EasyMockSupport support)
    {
        Projection projection = support.createMock(Projection.class);
        EasyMock.expect(projection.convertToModel(EasyMock.isA(GeographicPosition.class), EasyMock.eq(Vector3d.ORIGIN)))
                .andAnswer(this::convertToModelAnswer).atLeastOnce();

        MapManager mapManager = support.createMock(MapManager.class);
        EasyMock.expect(mapManager.getProjection()).andReturn(projection).atLeastOnce();

        return mapManager;
    }

    /**
     * Creates test metadata.
     *
     * @return The test metadata.
     */
    private PlatformMetadata createMetadata()
    {
        PlatformMetadata metadata = new PlatformMetadata();

        metadata.setCameraPitchAngle(-15.443460464477539);
        metadata.setCameraRollAngle(-2.876760482788086);
        metadata.setCameraYawAngle(0.10008019953966141);
        metadata.setPitchAngle(1.3307557106018066);
        metadata.setRollAngle(1.6258397102355957);
        metadata.setYawAngle(73.36104583740234);
        metadata.setLocation(LatLonAlt.createFromDegrees(34.6905037, -86.5819168));
        metadata.setTime(new Date(System.currentTimeMillis() - 1000));
        GeographicConvexQuadrilateral footprint = new GeographicConvexQuadrilateral(
                new GeographicPosition(LatLonAlt.createFromDegrees(10, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 10)),
                new GeographicPosition(LatLonAlt.createFromDegrees(10, 10)));
        metadata.setFootprint(footprint);

        return metadata;
    }

    /**
     * Creates an easy mocked {@link OrderManagerRegistry}.
     *
     * @param support Used to create the mock.
     * @param expectedUAVKey The expected order key to be passed to it.
     * @param expectedVideoKey The expected video key.
     * @param times The number of times to be expected to be called.
     * @return The mocked {@link OrderManagerRegistry}.
     */
    private OrderManagerRegistry createOrderRegistry(EasyMockSupport support, OrderParticipantKey expectedUAVKey,
            OrderParticipantKey expectedVideoKey, int times)
    {
        OrderManager orderManager = support.createMock(OrderManager.class);
        EasyMock.expect(Integer.valueOf(orderManager.getOrder(EasyMock.eq(expectedUAVKey))))
                .andReturn(Integer.valueOf(ourUAVZOrder)).times(times);
        EasyMock.expect(Integer.valueOf(orderManager.getOrder(EasyMock.eq(expectedVideoKey))))
                .andReturn(Integer.valueOf(ourImageryZOrder)).times(times);

        OrderManagerRegistry orderRegistry = support.createMock(OrderManagerRegistry.class);
        EasyMock.expect(orderRegistry.getOrderManager(expectedUAVKey)).andReturn(orderManager).times(times);
        EasyMock.expect(orderRegistry.getOrderManager(expectedVideoKey)).andReturn(orderManager).times(times);

        return orderRegistry;
    }

    /**
     * Creates a mocked querier.
     *
     * @param support Used to create the mock.
     * @param videoLayer The mocked video layer.
     * @param times The number of times expected for geometries.
     * @return The mocked querier.
     * @throws QueryException Bad query.
     * @throws IOException Bad IO.
     */
    private OSHImageQuerier createQuerier(EasyMockSupport support, DataTypeInfo videoLayer, int times)
        throws QueryException, IOException
    {
        OSHImageQuerier querier = support.createMock(OSHImageQuerier.class);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(ImageUtil.LOADING_IMAGE, "jpg", out);
        EasyMock.expect(querier.queryImage(EasyMock.cmpEq(ourTypeKey), EasyMock.isA(TimeSpan.class)))
                .andAnswer(this::queryImageAnswer).times(times);

        return querier;
    }

    /**
     * Creates an easy mocked {@link GenericSubscriber}.
     *
     * @param support Used to create the mock.
     * @return The receiver.
     */
    @SuppressWarnings("unchecked")
    private GenericSubscriber<Geometry> createReceiver(EasyMockSupport support)
    {
        GenericSubscriber<Geometry> receiver = support.createMock(GenericSubscriber.class);

        receiver.receiveObjects(EasyMock.isA(AerialImageryTransformer.class), EasyMock.isA(List.class), EasyMock.isA(List.class));

        EasyMock.expectLastCall().andAnswer(this::receiveObjectsAnswerVehicle);
        EasyMock.expectLastCall().andAnswer(this::receiveObjectsAnswerImagery);
        EasyMock.expectLastCall().andAnswer(this::receiveObjectsAnswerFootprint);

        return receiver;
    }

    /**
     * Creates an easy mocked {@link DataRegistry}.
     *
     * @param support Used to create the mock.
     * @param testData The test data to return.
     * @param times The expected number of times for geometry publishing.
     * @return The mocked {@link DataRegistry}.
     */
    private DataRegistry createRegistry(EasyMockSupport support, List<PlatformMetadata> testData, int times)
    {
        DataRegistry dataRegistry = support.createMock(DataRegistry.class);

        EasyMock.expect(dataRegistry.performQuery(EasyMock.isA(SimpleQuery.class))).andAnswer(() -> queryAnswer(testData))
                .times(times);

        return dataRegistry;
    }

    /**
     * Creates an easy mocked {@link TimeManager}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link TimeManager}.
     */
    private TimeManager createTimeManager(EasyMockSupport support)
    {
        ActiveTimeSpans activeSpan = support.createMock(ActiveTimeSpans.class);
        EasyMock.expect(activeSpan.getPrimary()).andAnswer(this::activeTimeAnswer).atLeastOnce();

        TimeManager timeManager = support.createMock(TimeManager.class);

        timeManager.addActiveTimeSpanChangeListener(EasyMock.isA(TimeRefreshNotifier.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myListener = (ActiveTimeSpanChangeListener)EasyMock.getCurrentArguments()[0];
            return null;
        });
        EasyMock.expect(timeManager.getActiveTimeSpans()).andReturn(activeSpan).atLeastOnce();

        return timeManager;
    }

    /**
     * Creates an easy mocked toolbox.
     *
     * @param support Used to create the mock.
     * @param eventManager A mocked {@link EventManager}.
     * @param timeManager A mocked {@link TimeManager}.
     * @param animationManager A mocked {@link AnimationManager}.
     * @param testData The test data.
     * @param orderRegistry A mocked {@link OrderManagerRegistry}.
     * @param times The expected number of times for geometry publishing.
     * @return The mocked {@link Toolbox}.
     */
    private Toolbox createToolbox(EasyMockSupport support, EventManager eventManager, TimeManager timeManager,
            AnimationManager animationManager, List<PlatformMetadata> testData, OrderManagerRegistry orderRegistry, int times)
    {
        DataRegistry dataRegistry = createRegistry(support, testData, times);
        MapManager mapManager = createMapManager(support);

        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getDataRegistry()).andReturn(dataRegistry).atLeastOnce();
        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager);
        EasyMock.expect(toolbox.getTimeManager()).andReturn(timeManager);
        EasyMock.expect(toolbox.getAnimationManager()).andReturn(animationManager);
        EasyMock.expect(toolbox.getOrderManagerRegistry()).andReturn(orderRegistry);
        EasyMock.expect(toolbox.getMapManager()).andReturn(mapManager);

        return toolbox;
    }

    /**
     * Creates an easy mocked data type.
     *
     * @param support Used to create the mock.
     * @param orderKey The order key.
     * @param expectedTimes for geometry publishing.
     * @return The mocked {@link DataTypeInfo}.
     */
    private DataTypeInfo createUAVDataType(EasyMockSupport support, OrderParticipantKey orderKey, int expectedTimes)
    {
        BasicVisualizationInfo visInfo = support.createMock(BasicVisualizationInfo.class);
        EasyMock.expect(visInfo.getTypeColor()).andReturn(ourColor).atLeastOnce();
        EasyMock.expect(Integer.valueOf(visInfo.getTypeOpacity())).andReturn(Integer.valueOf(ourOpacity)).atLeastOnce();

        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);

        EasyMock.expect(dataType.getOrderKey()).andReturn(orderKey).atLeastOnce();
        EasyMock.expect(dataType.getBasicVisualizationInfo()).andReturn(visInfo).anyTimes();
        EasyMock.expect(dataType.getTypeKey()).andReturn(ourTypeKey).atLeastOnce();
        EasyMock.expect(Boolean.valueOf(dataType.isVisible())).andReturn(Boolean.TRUE).anyTimes();
        EasyMock.expect(dataType.getUrl()).andReturn(ourServer).times(expectedTimes);

        return dataType;
    }

    /**
     * Creates the video layer.
     *
     * @param support Used to create the mock.
     * @param key The expected order key.
     * @return The mocked video layer.
     */
    private DataTypeInfo createVideoLayer(EasyMockSupport support, OrderParticipantKey key)
    {
        BasicVisualizationInfo visInfo = support.createMock(BasicVisualizationInfo.class);

        DataTypeInfo videoLayer = support.createMock(DataTypeInfo.class);

        EasyMock.expect(videoLayer.getTypeKey()).andReturn(ourTypeKey).atLeastOnce();
        EasyMock.expect(Integer.valueOf(visInfo.getTypeOpacity())).andReturn(Integer.valueOf(ourOpacity)).atLeastOnce();
        EasyMock.expect(videoLayer.getBasicVisualizationInfo()).andReturn(visInfo).atLeastOnce();
        EasyMock.expect(videoLayer.getOrderKey()).andReturn(key).atLeastOnce();

        return videoLayer;
    }

    /**
     * The answer to the mocked performQuery call.
     *
     * @param testData The test data to return.
     * @return Null.
     */
    @SuppressWarnings("unchecked")
    private QueryTracker queryAnswer(List<PlatformMetadata> testData)
    {
        SimpleQuery<PlatformMetadata> query = (SimpleQuery<PlatformMetadata>)EasyMock.getCurrentArguments()[0];

        DataModelCategory expected = new DataModelCategory(ourServer, Constants.PLATFORM_METADATA_FAMILY, ourTypeKey);
        assertEquals(expected, query.getDataModelCategory());
        assertEquals(Constants.PLATFORM_METADATA_DESCRIPTOR, query.getPropertyValueReceivers().get(0).getPropertyDescriptor());

        List<? extends PropertyMatcher<?>> matchers = query.getParameters();

        assertEquals(2, matchers.size());

        NumberPropertyMatcher<Long> lte = (NumberPropertyMatcher<Long>)matchers.get(0);
        NumberPropertyMatcher<Long> gte = (NumberPropertyMatcher<Long>)matchers.get(1);

        assertEquals(Constants.METADATA_TIMESTAMP_PROPERTY_DESCRIPTOR, lte.getPropertyDescriptor());
        assertEquals(OperatorType.LTE, lte.getOperator());
        assertEquals(myQueryTime, lte.getOperand().longValue());

        assertEquals(Constants.METADATA_TIMESTAMP_PROPERTY_DESCRIPTOR, gte.getPropertyDescriptor());
        assertEquals(OperatorType.GTE, gte.getOperator());
        assertEquals(myQueryTime - 1000, gte.getOperand().longValue());

        ((PropertyValueReceiver<PlatformMetadata>)query.getPropertyValueReceivers().get(0)).receive(testData);

        return null;
    }

    /**
     * The answer to the mocked query image call.
     *
     * @return The image.
     * @throws IOException Bad IO.
     */
    private byte[] queryImageAnswer() throws IOException
    {
        assertEquals(TimeSpan.get(myQueryTime, Milliseconds.ONE), EasyMock.getCurrentArguments()[1]);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(ImageUtil.LOADING_IMAGE, "jpg", out);

        return out.toByteArray();
    }

    /**
     * Answer to the first receiveObjectsAnswer mocked call, expecting a
     * footprint geometry.
     *
     * @return Null.
     */
    @SuppressWarnings("unchecked")
    private Void receiveObjectsAnswerFootprint()
    {
        List<Geometry> adds = (List<Geometry>)EasyMock.getCurrentArguments()[1];
        List<Geometry> removes = (List<Geometry>)EasyMock.getCurrentArguments()[2];

        assertTrue(removes.isEmpty());
        assertEquals(1, adds.size());
        assertTrue(adds.get(0) instanceof PolygonGeometry);

        return null;
    }

    /**
     * Answer to the third receiveObjectAnswer mocked call, expecting imagery
     * geometry.
     *
     * @return Null.
     */
    @SuppressWarnings("unchecked")
    private Void receiveObjectsAnswerImagery()
    {
        List<Geometry> adds = (List<Geometry>)EasyMock.getCurrentArguments()[1];
        List<Geometry> removes = (List<Geometry>)EasyMock.getCurrentArguments()[2];

        assertTrue(removes.isEmpty());
        assertEquals(1, adds.size());
        assertTrue(adds.get(0) instanceof TileGeometry);

        return null;
    }

    /**
     * Answer to the third receiveObjectAnswer mocked call, expecting imagery
     * geometry.
     *
     * @return Null.
     */
    @SuppressWarnings("unchecked")
    private Void receiveObjectsAnswerImageryRemoves()
    {
        List<Geometry> adds = (List<Geometry>)EasyMock.getCurrentArguments()[1];
        List<Geometry> removes = (List<Geometry>)EasyMock.getCurrentArguments()[2];

        assertEquals(1, removes.size());
        assertEquals(1, adds.size());
        assertTrue(adds.get(0) instanceof TileGeometry);

        return null;
    }

    /**
     * Answer to the second receiveObjectAnswer mocked call, expecting a vehicle
     * geometry.
     *
     * @return Null.
     */
    @SuppressWarnings("unchecked")
    private Void receiveObjectsAnswerVehicle()
    {
        List<Geometry> adds = (List<Geometry>)EasyMock.getCurrentArguments()[1];
        List<Geometry> removes = (List<Geometry>)EasyMock.getCurrentArguments()[2];

        assertTrue(removes.isEmpty());
        // 119
        assertEquals(138, adds.size());
        boolean containsMeshes = false;
        boolean containsLines = false;

        for (Geometry geom : adds)
        {
            if (geom instanceof PolygonMeshGeometry)
            {
                containsMeshes = true;
            }
            else if (geom instanceof PolylineGeometry)
            {
                containsLines = true;
            }
        }

        assertTrue(containsMeshes);
        assertTrue(containsLines);

        return null;
    }

    /**
     * The answer to the remove geometries mocked call during the close test.
     *
     * @return Null.
     */
    @SuppressWarnings("unchecked")
    private Void removeGeometriesAnswer()
    {
        List<Geometry> adds = (List<Geometry>)EasyMock.getCurrentArguments()[1];
        List<Geometry> removes = (List<Geometry>)EasyMock.getCurrentArguments()[2];

        assertTrue(adds.isEmpty());

        // 121
        assertEquals(140, removes.size());
        boolean hasLines = false;
        boolean hasMeshes = false;
        boolean hasFootprint = false;
        boolean hasImage = false;

        for (Geometry geom : removes)
        {
            if (geom instanceof PolygonGeometry)
            {
                hasFootprint = true;
            }
            else if (geom instanceof PolylineGeometry)
            {
                hasLines = true;
            }
            else if (geom instanceof PolygonMeshGeometry)
            {
                hasMeshes = true;
            }
            else if (geom instanceof TileGeometry)
            {
                hasImage = true;
            }
        }

        assertTrue(hasLines);
        assertTrue(hasMeshes);
        assertTrue(hasFootprint);
        assertTrue(hasImage);

        return null;
    }
}
