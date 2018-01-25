package io.opensphere.xyztile.transformer;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.controller.event.impl.ActiveDataGroupsChangedEvent;
import io.opensphere.mantle.data.DataGroupActivationProperty;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.event.DataGroupInfoChildRemovedEvent;
import io.opensphere.mantle.data.event.DataGroupInfoMemberAddedEvent;
import io.opensphere.mantle.data.event.DataGroupInfoMemberRemovedEvent;
import io.opensphere.mantle.data.event.DataTypeInfoZOrderChangeEvent;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;
import io.opensphere.xyztile.model.Projection;
import io.opensphere.xyztile.model.XYZDataTypeInfo;
import io.opensphere.xyztile.model.XYZServerInfo;
import io.opensphere.xyztile.model.XYZTileLayerInfo;
import io.opensphere.xyztile.util.XYZTileUtils;

/**
 * Unit test for {@link LayerActivationHandler}.
 */
public class LayerActivationHandlerTest
{
    /**
     * Our test layer info.
     */
    private static final XYZTileLayerInfo ourLayerInfo = new XYZTileLayerInfo("name", "displayName", Projection.EPSG_4326, 1,
            false, 0, new XYZServerInfo("serverName", "http://somehost"));

    /**
     * The active listener.
     */
    private EventListener<ActiveDataGroupsChangedEvent> myActiveListener;

    /**
     * The data type removed event listener.
     */
    private EventListener<DataGroupInfoMemberRemovedEvent> myDataTypeRemovedListener;

    /**
     * The member added listener.
     */
    private EventListener<DataGroupInfoMemberAddedEvent> myMemberAddedListener;

    /**
     * The removed listener.
     */
    private EventListener<DataGroupInfoChildRemovedEvent> myRemovedListener;

    /**
     * The visibility listener.
     */
    private EventListener<DataTypeVisibilityChangeEvent> myVisibilityListener;

    /**
     * The zorder listener.
     */
    private EventListener<DataTypeInfoZOrderChangeEvent> myZOrderListener;

    /**
     * Tests when the layer is deactivated.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testActivateDeactivate() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        XYZDataTypeInfo dataType = createType(true);
        CountDownLatch active = new CountDownLatch(1);
        CountDownLatch deactive = new CountDownLatch(1);
        LayerActivationListener listener = createListener(support, dataType, deactive, active);
        DataGroupInfo mapBoxGroup = createGroup(support, XYZTileUtils.XYZ_PROVIDER, dataType);
        DataGroupInfo otherGroup = createGroup(support, null, null);
        EasyMock.expect(otherGroup.getMembers(EasyMock.eq(false))).andReturn(New.set()).anyTimes();

        support.replayAll();

        LayerActivationHandler handler = new LayerActivationHandler(eventManager, listener);
        ActiveDataGroupsChangedEvent event = new ActiveDataGroupsChangedEvent(this, New.set(otherGroup, mapBoxGroup), New.set());
        myActiveListener.notify(event);
        active.await(1, TimeUnit.SECONDS);
        event = new ActiveDataGroupsChangedEvent(this, New.set(), New.set(otherGroup, mapBoxGroup));
        myActiveListener.notify(event);
        deactive.await(1, TimeUnit.SECONDS);
        handler.close();

        support.verifyAll();
    }

    /**
     * Tests when the layer is activated and deactivated and the layer belongs
     * to a none xyz group.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testActivateDeactivateXYZType() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        XYZDataTypeInfo dataType = createType(true);
        CountDownLatch active = new CountDownLatch(1);
        CountDownLatch deactive = new CountDownLatch(1);
        LayerActivationListener listener = createListener(support, dataType, deactive, active);
        DataGroupInfo otherGroup = createGroup(support, null, null);
        EasyMock.expect(otherGroup.getMembers(EasyMock.eq(false))).andReturn(New.set(dataType)).anyTimes();

        support.replayAll();

        LayerActivationHandler handler = new LayerActivationHandler(eventManager, listener);
        ActiveDataGroupsChangedEvent event = new ActiveDataGroupsChangedEvent(this, New.set(otherGroup), New.set());
        myActiveListener.notify(event);
        active.await(1, TimeUnit.SECONDS);
        event = new ActiveDataGroupsChangedEvent(this, New.set(), New.set(otherGroup));
        myActiveListener.notify(event);
        deactive.await(1, TimeUnit.SECONDS);
        handler.close();

        support.verifyAll();
    }

    /**
     * Tests when the layer is activated but not visible.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testActiveButNotVisible() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        XYZDataTypeInfo dataType = createType(false);
        CountDownLatch latch = new CountDownLatch(1);
        LayerActivationListener listener = createListenerDeactivate(support, dataType, latch);

        DataGroupInfo mapBoxGroup = createGroup(support, XYZTileUtils.XYZ_PROVIDER, dataType);
        DataGroupInfo otherGroup = createGroup(support, null, null);
        EasyMock.expect(otherGroup.getMembers(EasyMock.eq(false))).andReturn(New.set()).anyTimes();

        support.replayAll();

        LayerActivationHandler handler = new LayerActivationHandler(eventManager, listener);
        ActiveDataGroupsChangedEvent event = new ActiveDataGroupsChangedEvent(this, New.set(otherGroup, mapBoxGroup), New.set());
        myActiveListener.notify(event);
        event = new ActiveDataGroupsChangedEvent(this, New.set(), New.set(otherGroup, mapBoxGroup));
        myActiveListener.notify(event);
        latch.await(1, TimeUnit.SECONDS);
        handler.close();

        Thread.sleep(250L);

        support.verifyAll();
    }

    /**
     * Tests the when a layer is removed.
     */
    @Test
    public void testDataTypeRemoved()
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        XYZDataTypeInfo dataType = createType(true);
        LayerActivationListener listener = createListenerDeactivate(support, dataType);
        DataGroupInfo dgi = support.createMock(DataGroupInfo.class);
        DataTypeInfo otherType = support.createMock(DataTypeInfo.class);

        support.replayAll();

        LayerActivationHandler handler = new LayerActivationHandler(eventManager, listener);
        DataGroupInfoMemberRemovedEvent event = new DataGroupInfoMemberRemovedEvent(dgi, dataType, this);
        myDataTypeRemovedListener.notify(event);
        event = new DataGroupInfoMemberRemovedEvent(dgi, otherType, this);
        myDataTypeRemovedListener.notify(event);
        handler.close();

        support.verifyAll();
    }

    /**
     * Tests when a member is added.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testMemberAdded() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        XYZDataTypeInfo dataType = createType(true);
        CountDownLatch deactivate = new CountDownLatch(1);
        CountDownLatch activate = new CountDownLatch(1);
        LayerActivationListener listener = createListener(support, dataType, deactivate, activate);

        DataTypeInfo otherType = support.createMock(DataTypeInfo.class);
        DataGroupInfo activeGroup = support.createMock(DataGroupInfo.class);
        DataGroupActivationProperty activationProperty = new DataGroupActivationProperty(activeGroup);
        activationProperty.setActive(false);
        EasyMock.expect(activeGroup.activationProperty()).andReturn(activationProperty).times(2);
        dataType.setParent(activeGroup);

        DataGroupInfo otherGroup = support.createMock(DataGroupInfo.class);
        DataGroupActivationProperty otherActivation = new DataGroupActivationProperty(activeGroup);
        otherActivation.setActive(true);
        EasyMock.expect(otherGroup.activationProperty()).andReturn(otherActivation);

        support.replayAll();

        LayerActivationHandler handler = new LayerActivationHandler(eventManager, listener);
        DataGroupInfoMemberAddedEvent event = new DataGroupInfoMemberAddedEvent(otherGroup, otherType, this);
        myMemberAddedListener.notify(event);
        event = new DataGroupInfoMemberAddedEvent(activeGroup, dataType, this);
        myMemberAddedListener.notify(event);
        deactivate.await(1, TimeUnit.SECONDS);
        activationProperty.setActive(true);
        myMemberAddedListener.notify(event);
        deactivate.await(1, TimeUnit.SECONDS);

        Thread.sleep(500);

        handler.close();

        support.verifyAll();
    }

    /**
     * Tests the when a layer is removed.
     */
    @Test
    public void testRemoved()
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        XYZDataTypeInfo dataType = createType(true);
        LayerActivationListener listener = createListenerDeactivate(support, dataType);
        DataGroupInfo mapBoxGroup = createGroup(support, XYZTileUtils.XYZ_PROVIDER, dataType);
        DataGroupInfo otherGroup = createGroup(support, null, null);
        EasyMock.expect(otherGroup.getMembers(EasyMock.eq(false))).andReturn(New.set());

        support.replayAll();

        LayerActivationHandler handler = new LayerActivationHandler(eventManager, listener);
        DataGroupInfoChildRemovedEvent event = new DataGroupInfoChildRemovedEvent(mapBoxGroup, mapBoxGroup, this);
        myRemovedListener.notify(event);
        event = new DataGroupInfoChildRemovedEvent(otherGroup, otherGroup, this);
        myRemovedListener.notify(event);
        handler.close();

        support.verifyAll();
    }

    /**
     * Tests the when a layer is removed that is not an xyz group but contains
     * xyz layers.
     */
    @Test
    public void testRemovedNotXYZGroup()
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        XYZDataTypeInfo dataType = createType(true);
        LayerActivationListener listener = createListenerDeactivate(support, dataType);
        DataGroupInfo mapBoxGroup = createGroup(support, "Some other group", dataType);
        DataGroupInfo otherGroup = createGroup(support, null, null);
        EasyMock.expect(otherGroup.getMembers(EasyMock.eq(false))).andReturn(New.set());

        support.replayAll();

        LayerActivationHandler handler = new LayerActivationHandler(eventManager, listener);
        DataGroupInfoChildRemovedEvent event = new DataGroupInfoChildRemovedEvent(mapBoxGroup, mapBoxGroup, this);
        myRemovedListener.notify(event);
        event = new DataGroupInfoChildRemovedEvent(otherGroup, otherGroup, this);
        myRemovedListener.notify(event);
        handler.close();

        support.verifyAll();
    }

    /**
     * Tests the visibility changed.
     */
    @Test
    public void testVisibilityChanged()
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        XYZDataTypeInfo dataType = createType(true);
        LayerActivationListener listener = createListener(support, dataType);
        DataTypeInfo otherType = support.createMock(DataTypeInfo.class);

        support.replayAll();

        LayerActivationHandler handler = new LayerActivationHandler(eventManager, listener);
        DataTypeVisibilityChangeEvent event = new DataTypeVisibilityChangeEvent(dataType, true, true, this);
        myVisibilityListener.notify(event);
        event = new DataTypeVisibilityChangeEvent(dataType, false, true, this);
        myVisibilityListener.notify(event);
        event = new DataTypeVisibilityChangeEvent(otherType, true, true, this);
        myVisibilityListener.notify(event);

        handler.close();

        support.verifyAll();
    }

    /**
     * Tests when the layer is deactivated.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testZorder() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        XYZDataTypeInfo dataType = createType(true);
        CountDownLatch latch = new CountDownLatch(1);
        LayerActivationListener listener = createListener(support, dataType);
        EasyMock.expectLastCall().andAnswer(() ->
        {
            latch.countDown();
            return null;
        });
        DataGroupInfo mapBoxGroup = support.createMock(DataGroupInfo.class);
        DataGroupActivationProperty property = new DataGroupActivationProperty(mapBoxGroup);
        property.setActive(true);
        EasyMock.expect(mapBoxGroup.activationProperty()).andReturn(property);
        dataType.setParent(mapBoxGroup);
        DataTypeInfo otherType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(otherType.getParent()).andReturn(mapBoxGroup);

        support.replayAll();

        LayerActivationHandler handler = new LayerActivationHandler(eventManager, listener);
        DataTypeInfoZOrderChangeEvent zEvent = new DataTypeInfoZOrderChangeEvent(dataType, 2, this);
        myZOrderListener.notify(zEvent);

        zEvent = new DataTypeInfoZOrderChangeEvent(otherType, 3, this);
        myZOrderListener.notify(zEvent);

        latch.await(5, TimeUnit.SECONDS);

        handler.close();

        support.verifyAll();
    }

    /**
     * Tests when the layer is zordered but not active.
     */
    @Test
    public void testZorderNotActive()
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        XYZDataTypeInfo dataType = createType(true);
        LayerActivationListener listener = support.createMock(LayerActivationListener.class);
        DataGroupInfo mapBoxGroup = support.createMock(DataGroupInfo.class);
        DataGroupActivationProperty property = new DataGroupActivationProperty(mapBoxGroup);
        property.setActive(false);
        EasyMock.expect(mapBoxGroup.activationProperty()).andReturn(property);
        dataType.setParent(mapBoxGroup);
        DataTypeInfo otherType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(otherType.getParent()).andReturn(mapBoxGroup);

        support.replayAll();

        LayerActivationHandler handler = new LayerActivationHandler(eventManager, listener);
        DataTypeInfoZOrderChangeEvent zEvent = new DataTypeInfoZOrderChangeEvent(dataType, 2, this);
        myZOrderListener.notify(zEvent);

        zEvent = new DataTypeInfoZOrderChangeEvent(otherType, 3, this);
        myZOrderListener.notify(zEvent);

        handler.close();

        support.verifyAll();
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
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myVisibilityListener = (EventListener<DataTypeVisibilityChangeEvent>)EasyMock.getCurrentArguments()[1];
            return null;
        });
        eventManager.subscribe(EasyMock.eq(DataGroupInfoChildRemovedEvent.class), EasyMock.isA(EventListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myRemovedListener = (EventListener<DataGroupInfoChildRemovedEvent>)EasyMock.getCurrentArguments()[1];
            return null;
        });
        eventManager.subscribe(EasyMock.eq(ActiveDataGroupsChangedEvent.class), EasyMock.isA(EventListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myActiveListener = (EventListener<ActiveDataGroupsChangedEvent>)EasyMock.getCurrentArguments()[1];
            return null;
        });
        eventManager.subscribe(EasyMock.eq(DataTypeInfoZOrderChangeEvent.class), EasyMock.isA(EventListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myZOrderListener = (EventListener<DataTypeInfoZOrderChangeEvent>)EasyMock.getCurrentArguments()[1];
            return null;
        });
        eventManager.subscribe(EasyMock.eq(DataGroupInfoMemberAddedEvent.class), EasyMock.isA(EventListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myMemberAddedListener = (EventListener<DataGroupInfoMemberAddedEvent>)EasyMock.getCurrentArguments()[1];
            return null;
        });
        eventManager.subscribe(EasyMock.eq(DataGroupInfoMemberRemovedEvent.class), EasyMock.isA(EventListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myDataTypeRemovedListener = (EventListener<DataGroupInfoMemberRemovedEvent>)EasyMock.getCurrentArguments()[1];
            return null;
        });

        eventManager.unsubscribe(EasyMock.eq(DataTypeVisibilityChangeEvent.class), EasyMock.isA(EventListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            assertEquals(myVisibilityListener, EasyMock.getCurrentArguments()[1]);
            return null;
        });

        eventManager.unsubscribe(EasyMock.eq(DataGroupInfoChildRemovedEvent.class), EasyMock.isA(EventListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            assertEquals(myRemovedListener, EasyMock.getCurrentArguments()[1]);
            return null;
        });

        eventManager.unsubscribe(EasyMock.eq(ActiveDataGroupsChangedEvent.class), EasyMock.isA(EventListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            assertEquals(myActiveListener, EasyMock.getCurrentArguments()[1]);
            return null;
        });

        eventManager.unsubscribe(EasyMock.eq(DataTypeInfoZOrderChangeEvent.class), EasyMock.isA(EventListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            assertEquals(myZOrderListener, EasyMock.getCurrentArguments()[1]);
            return null;
        });

        eventManager.unsubscribe(EasyMock.eq(DataGroupInfoMemberAddedEvent.class), EasyMock.isA(EventListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            assertEquals(myMemberAddedListener, EasyMock.getCurrentArguments()[1]);
            return null;
        });

        eventManager.unsubscribe(EasyMock.eq(DataGroupInfoMemberRemovedEvent.class), EasyMock.isA(EventListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            assertEquals(myDataTypeRemovedListener, EasyMock.getCurrentArguments()[1]);
            return null;
        });

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
    private DataGroupInfo createGroup(EasyMockSupport support, String providerType, DataTypeInfo dataType)
    {
        DataGroupInfo group = support.createMock(DataGroupInfo.class);

        EasyMock.expect(group.getProviderType()).andReturn(providerType).atLeastOnce();
        if (dataType != null)
        {
            EasyMock.expect(group.getId()).andReturn(dataType.getTypeKey()).anyTimes();
            EasyMock.expect(group.getMembers(EasyMock.eq(false))).andReturn(New.set(dataType)).atLeastOnce();
        }

        return group;
    }

    /**
     * Creates an easy mocked {@link LayerActivationListener}.
     *
     * @param support Used to create the mock.
     * @param dataType The data type to expect.
     * @return The mocked {@link LayerActivationListener}.
     */
    private LayerActivationListener createListener(EasyMockSupport support, XYZDataTypeInfo dataType)
    {
        return createListener(support, dataType, null, null);
    }

    /**
     * Creates an easy mocked {@link LayerActivationListener}.
     *
     * @param support Used to create the mock.
     * @param dataType The data type to expect.
     * @param activate Used to synchronize threads.
     * @param deactivate Used to synchronize threads.
     * @return The mocked {@link LayerActivationListener}.
     */
    private LayerActivationListener createListener(EasyMockSupport support, XYZDataTypeInfo dataType, CountDownLatch deactivate,
            CountDownLatch activate)
    {
        LayerActivationListener listener = support.createMock(LayerActivationListener.class);

        listener.layerDeactivated(EasyMock.eq(dataType));
        if (deactivate != null)
        {
            EasyMock.expectLastCall().andAnswer(() ->
            {
                deactivate.countDown();
                return null;
            });
        }
        listener.layerActivated(EasyMock.eq(dataType));
        if (activate != null)
        {
            EasyMock.expectLastCall().andAnswer(() ->
            {
                activate.countDown();
                return null;
            });
        }

        return listener;
    }

    /**
     * Creates an easy mocked {@link LayerActivationListener} only expected for
     * a deactivate call.
     *
     * @param support Used to create the mock.
     * @param dataType The data type to expect.
     * @return The mocked {@link LayerActivationListener}.
     */
    private LayerActivationListener createListenerDeactivate(EasyMockSupport support, XYZDataTypeInfo dataType)
    {
        return createListenerDeactivate(support, dataType, null);
    }

    /**
     * Creates an easy mocked {@link LayerActivationListener} only expected for
     * a deactivate call.
     *
     * @param support Used to create the mock.
     * @param dataType The data type to expect.
     * @param latch Used to synchronize threads.
     * @return The mocked {@link LayerActivationListener}.
     */
    private LayerActivationListener createListenerDeactivate(EasyMockSupport support, XYZDataTypeInfo dataType,
            CountDownLatch latch)
    {
        LayerActivationListener listener = support.createMock(LayerActivationListener.class);

        listener.layerDeactivated(EasyMock.eq(dataType));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            if (latch != null)
            {
                latch.countDown();
            }
            return null;
        }).atLeastOnce();

        return listener;
    }

    /**
     * Creates an easy mocked {@link DataTypeInfo}.
     *
     * @param isVisible Indicates if the type is visible.
     * @return The mocked {@link DataTypeInfo}.
     */
    private XYZDataTypeInfo createType(boolean isVisible)
    {
        XYZDataTypeInfo dataType = new XYZDataTypeInfo(null, ourLayerInfo);
        dataType.setVisible(isVisible, this);

        return dataType;
    }
}
