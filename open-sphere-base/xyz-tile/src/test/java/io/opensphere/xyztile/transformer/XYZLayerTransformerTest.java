package io.opensphere.xyztile.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.controller.event.impl.ActiveDataGroupsChangedEvent;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.event.DataGroupInfoChildRemovedEvent;
import io.opensphere.mantle.data.event.DataGroupInfoMemberAddedEvent;
import io.opensphere.mantle.data.event.DataGroupInfoMemberRemovedEvent;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;
import io.opensphere.mantle.data.event.DataTypeInfoZOrderChangeEvent;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;
import io.opensphere.mantle.data.impl.DefaultMapTileVisualizationInfo;
import io.opensphere.xyztile.model.Projection;
import io.opensphere.xyztile.model.XYZDataTypeInfo;
import io.opensphere.xyztile.model.XYZServerInfo;
import io.opensphere.xyztile.model.XYZTileLayerInfo;
import io.opensphere.xyztile.util.XYZTileUtils;

/**
 * Unit test for {@link XYZLayerTransformer}.
 */
public class XYZLayerTransformerTest
{
    /**
     * The active listener.
     */
    private EventListener<ActiveDataGroupsChangedEvent> myActiveListener;

    /**
     * The color listener.
     */
    private EventListener<DataTypeInfoColorChangeEvent> myColorListener;

    /**
     * Tests activating and deactivating a layer.
     *
     * @throws InterruptedException Don't interrupt
     */
    @Test
    public void testLayerActivated() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        CountDownLatch latch = new CountDownLatch(256);
        XYZDataTypeInfo dataType = createType(support, latch);
        DataRegistry dataRegistry = createDataRegistry(support);
        UIRegistry uiRegistry = createUIRegistry(support);
        EventManager eventManager = createEventManager(support);
        List<Geometry> publishedGeometries = New.list();
        CountDownLatch addLatch = new CountDownLatch(1);
        CountDownLatch removeLatch = new CountDownLatch(1);
        GenericSubscriber<Geometry> subscriber = createSubscriber(support, publishedGeometries, addLatch, removeLatch);
        DataGroupInfo mapBoxGroup = createGroup(support, XYZTileUtils.XYZ_PROVIDER, dataType);

        support.replayAll();

        XYZLayerTransformer transformer = new XYZLayerTransformer(dataRegistry, uiRegistry, eventManager);
        transformer.addSubscriber(subscriber);
        transformer.open();

        ActiveDataGroupsChangedEvent event = new ActiveDataGroupsChangedEvent(this, New.set(mapBoxGroup), New.set());
        myActiveListener.notify(event);
        addLatch.await(1, TimeUnit.SECONDS);
        Thread.sleep(500);
        assertEquals(256, publishedGeometries.size());
        assertEquals(1, dataType.getLayerInfo().countObservers());

        DataTypeInfoColorChangeEvent colorEvent = new DataTypeInfoColorChangeEvent(dataType, new Color(127, 127, 127, 127), true,
                this);
        myColorListener.notify(colorEvent);

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        event = new ActiveDataGroupsChangedEvent(this, New.set(), New.set(mapBoxGroup));
        myActiveListener.notify(event);
        removeLatch.await(1, TimeUnit.SECONDS);
        Thread.sleep(500);
        assertTrue(publishedGeometries.isEmpty());
        assertEquals(0, dataType.getLayerInfo().countObservers());

        transformer.close();

        support.verifyAll();
    }

    /**
     * Tests activating and deactivating a layer.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testLayerActivatedBeforeOpen() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        CountDownLatch latch = new CountDownLatch(256);
        XYZDataTypeInfo dataType = createType(support, latch);
        DataRegistry dataRegistry = createDataRegistry(support);
        UIRegistry uiRegistry = createUIRegistry(support);
        EventManager eventManager = createEventManager(support);
        List<Geometry> publishedGeometries = New.list();
        CountDownLatch addLatch = new CountDownLatch(1);
        CountDownLatch removeLatch = new CountDownLatch(1);
        GenericSubscriber<Geometry> subscriber = createSubscriber(support, publishedGeometries, addLatch, removeLatch);
        DataGroupInfo mapBoxGroup = createGroup(support, XYZTileUtils.XYZ_PROVIDER, dataType);

        support.replayAll();

        XYZLayerTransformer transformer = new XYZLayerTransformer(dataRegistry, uiRegistry, eventManager);
        transformer.addSubscriber(subscriber);

        ActiveDataGroupsChangedEvent event = new ActiveDataGroupsChangedEvent(this, New.set(mapBoxGroup), New.set());
        myActiveListener.notify(event);
        addLatch.await(1, TimeUnit.SECONDS);
        assertTrue(publishedGeometries.isEmpty());
        transformer.open();
        assertEquals(256, publishedGeometries.size());

        DataTypeInfoColorChangeEvent colorEvent = new DataTypeInfoColorChangeEvent(dataType, new Color(127, 127, 127, 127), true,
                this);
        myColorListener.notify(colorEvent);

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        event = new ActiveDataGroupsChangedEvent(this, New.set(), New.set(mapBoxGroup));
        myActiveListener.notify(event);
        removeLatch.await(1, TimeUnit.SECONDS);
        assertTrue(publishedGeometries.isEmpty());

        transformer.close();

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link DataRegistry}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link DataRegistry}.
     */
    private DataRegistry createDataRegistry(EasyMockSupport support)
    {
        DataRegistry registry = support.createMock(DataRegistry.class);

        return registry;
    }

    /**
     * Creates an easy mocked {@link EventManager}.
     *
     * @param support Used to create the mock.
     * @return The {@link EventManager}.
     */
    @SuppressWarnings("unchecked")
    private EventManager createEventManager(EasyMockSupport support)
    {
        EventManager eventManager = support.createMock(EventManager.class);

        eventManager.subscribe(EasyMock.eq(DataTypeVisibilityChangeEvent.class), EasyMock.isA(EventListener.class));
        eventManager.subscribe(EasyMock.eq(DataGroupInfoChildRemovedEvent.class), EasyMock.isA(EventListener.class));
        eventManager.subscribe(EasyMock.eq(ActiveDataGroupsChangedEvent.class), EasyMock.isA(EventListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myActiveListener = (EventListener<ActiveDataGroupsChangedEvent>)EasyMock.getCurrentArguments()[1];
            return null;
        });
        eventManager.subscribe(EasyMock.eq(DataTypeInfoColorChangeEvent.class), EasyMock.isA(EventListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myColorListener = (EventListener<DataTypeInfoColorChangeEvent>)EasyMock.getCurrentArguments()[1];
            return null;
        });
        eventManager.subscribe(EasyMock.eq(DataTypeInfoZOrderChangeEvent.class), EasyMock.isA(EventListener.class));
        eventManager.subscribe(EasyMock.eq(DataGroupInfoMemberAddedEvent.class), EasyMock.isA(EventListener.class));
        eventManager.subscribe(EasyMock.eq(DataGroupInfoMemberRemovedEvent.class), EasyMock.isA(EventListener.class));

        eventManager.unsubscribe(EasyMock.eq(DataTypeVisibilityChangeEvent.class), EasyMock.isA(EventListener.class));
        eventManager.unsubscribe(EasyMock.eq(DataGroupInfoChildRemovedEvent.class), EasyMock.isA(EventListener.class));
        eventManager.unsubscribe(EasyMock.eq(ActiveDataGroupsChangedEvent.class), EasyMock.isA(EventListener.class));
        eventManager.unsubscribe(EasyMock.eq(DataTypeInfoColorChangeEvent.class), EasyMock.isA(EventListener.class));
        eventManager.unsubscribe(EasyMock.eq(DataTypeInfoZOrderChangeEvent.class), EasyMock.isA(EventListener.class));
        eventManager.unsubscribe(EasyMock.eq(DataGroupInfoMemberAddedEvent.class), EasyMock.isA(EventListener.class));
        eventManager.unsubscribe(EasyMock.eq(DataGroupInfoMemberRemovedEvent.class), EasyMock.isA(EventListener.class));

        return eventManager;
    }

    /**
     * Creates an easy mocked {@link DataGroupInfo}.
     *
     * @param support Used to create the mock.
     * @param providerType The provider type to return.
     * @param dataType The data type to return or null if getMembers will not be
     *            called.
     * @return The easy mocked {@link DataGroupInfo}.
     */
    private DataGroupInfo createGroup(EasyMockSupport support, String providerType, XYZDataTypeInfo dataType)
    {
        DataGroupInfo group = support.createMock(DataGroupInfo.class);
        EasyMock.expect(group.getProviderType()).andReturn(providerType).atLeastOnce();
        if (dataType != null)
        {
            EasyMock.expect(group.getMembers(EasyMock.eq(false))).andReturn(New.set(dataType)).atLeastOnce();
        }

        return group;
    }

    /**
     * Creates an easy mocked subscriber.
     *
     * @param support Used to create the mock.
     * @param publishedGeometries The list to add/remove published geometries to
     *            and from.
     * @param addLatch The latch to countdown when geometries are published.
     * @param removeLatch The latch to countdown when geometries are removed.
     * @return The mocked subscriber.
     */
    private GenericSubscriber<Geometry> createSubscriber(EasyMockSupport support, List<Geometry> publishedGeometries,
            CountDownLatch addLatch, CountDownLatch removeLatch)
    {
        @SuppressWarnings("unchecked")
        GenericSubscriber<Geometry> subscriber = support.createMock(GenericSubscriber.class);

        subscriber.receiveObjects(EasyMock.isA(Object.class), EasyMock.notNull(), EasyMock.notNull());
        EasyMock.expectLastCall().andAnswer(() -> receiveAnswer(publishedGeometries, addLatch, removeLatch)).times(2);

        return subscriber;
    }

    /**
     * Creates an easy mocked {@link DataTypeInfo}.
     *
     * @param support Used to create the mock.
     * @param latch Used to synchronize the threaded calls.
     * @return The mocked {@link DataTypeInfo}.
     */
    private XYZDataTypeInfo createType(EasyMockSupport support, CountDownLatch latch)
    {
        TileRenderProperties props = support.createMock(TileRenderProperties.class);
        EasyMock.expect(Boolean.valueOf(props.isDrawable())).andReturn(Boolean.TRUE).anyTimes();
        props.setOpacity(EasyMock.eq(0.49803922f));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            latch.countDown();
            return null;
        }).atLeastOnce();

        String serverUrl = "http://mapboxserver";
        String layerName = "dark";
        String layerDisplayName = "Dark";

        XYZTileLayerInfo layerInfo = new XYZTileLayerInfo(layerName, layerDisplayName, Projection.EPSG_3857, 1, false, 4,
                new XYZServerInfo("mapbox", serverUrl));
        XYZDataTypeInfo dataType = new XYZDataTypeInfo(null, layerInfo);
        dataType.setVisible(true, this);

        DefaultMapTileVisualizationInfo visInfo = new DefaultMapTileVisualizationInfo(MapVisualizationType.IMAGE_TILE, props,
                true);
        dataType.setMapVisualizationInfo(visInfo);

        return dataType;
    }

    /**
     * Creates an easy mocked {@link UIRegistry}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link UIRegistry}.
     */
    private UIRegistry createUIRegistry(EasyMockSupport support)
    {
        UIRegistry uiRegistry = support.createMock(UIRegistry.class);

        return uiRegistry;
    }

    /**
     * The answer for the generic subscriber.
     *
     * @param publishedGeometries The list to add/remove published geometries to
     *            and from.
     * @param addLatch The latch to countdown when geometries are published.
     * @param removeLatch The latch to countdown when geometries are removed.
     * @return Null.
     */
    @SuppressWarnings("unchecked")
    private Void receiveAnswer(List<Geometry> publishedGeometries, CountDownLatch addLatch, CountDownLatch removeLatch)
    {
        Collection<Geometry> adds = (Collection<Geometry>)EasyMock.getCurrentArguments()[1];
        Collection<Geometry> removes = (Collection<Geometry>)EasyMock.getCurrentArguments()[2];
        publishedGeometries.addAll(adds);
        if (!adds.isEmpty())
        {
            addLatch.countDown();
        }
        publishedGeometries.removeAll(removes);
        if (!removes.isEmpty())
        {
            removeLatch.countDown();
        }
        return null;
    }
}
