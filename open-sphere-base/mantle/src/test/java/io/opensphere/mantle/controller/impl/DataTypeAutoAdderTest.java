package io.opensphere.mantle.controller.impl;

import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Set;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.InlineExecutor;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.controller.event.impl.ActiveDataGroupsChangedEvent;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;

/**
 * Unit test for {@link DataTypeAutoAdder}.
 */
public class DataTypeAutoAdderTest
{
    /**
     * The existing data type key, the one to expect to be added.
     */
    private static final String ourExistingDtiKey = "iamexisting";

    /**
     * The new data type key, the one to expect to be added.
     */
    private static final String ourNewDtiKey = "iamkey";

    /**
     * The registered listener to the mocked {@link EventManager}.
     */
    private EventListener<ActiveDataGroupsChangedEvent> myListener;

    /**
     * Tests adding an activated {@link DataTypeInfo} to the controller.
     */
    @Test
    public void testActivate()
    {
        myListener = null;

        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo activate = createDataType(support, ourNewDtiKey, New.list("Column"));
        DataTypeInfo alreadyThere = createDataType(support, ourExistingDtiKey, New.list("Column"));

        Set<DataGroupInfo> groups = createGroups(support, alreadyThere, activate);
        DataTypeController typeController = createControllerActivate(support, activate);
        EventManager eventManager = createEventManager(support);

        support.replayAll();

        DataTypeAutoAdder autoAdder = new DataTypeAutoAdder(eventManager, typeController);
        autoAdder.setExecutor(new InlineExecutor());

        ActiveDataGroupsChangedEvent event = new ActiveDataGroupsChangedEvent(this, groups, New.set());
        myListener.notify(event);

        autoAdder.close();
        assertNull(myListener);

        support.verifyAll();
    }

    /**
     * Tests that tile layers do not get added to the controller.
     */
    @Test
    public void testActivateNoMetadata()
    {
        myListener = null;

        EasyMockSupport support = new EasyMockSupport();

        MetaDataInfo metadatainfo = support.createMock(MetaDataInfo.class);
        EasyMock.expect(metadatainfo.getKeyNames()).andReturn(New.list());

        DataTypeInfo notThere = support.createMock(DataTypeInfo.class);
        EasyMock.expect(notThere.getMetaDataInfo()).andReturn(metadatainfo).times(2);

        Set<DataGroupInfo> groups = createGroups(support, notThere);
        DataTypeController typeController = support.createMock(DataTypeController.class);
        EventManager eventManager = createEventManager(support);

        support.replayAll();

        DataTypeAutoAdder autoAdder = new DataTypeAutoAdder(eventManager, typeController);
        autoAdder.setExecutor(new InlineExecutor());

        ActiveDataGroupsChangedEvent event = new ActiveDataGroupsChangedEvent(this, groups, New.set());
        myListener.notify(event);

        autoAdder.close();
        assertNull(myListener);

        support.verifyAll();
    }

    /**
     * Tests that tile layers do not get added to the controller.
     */
    @Test
    public void testActivateTileLayer()
    {
        myListener = null;

        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo notThere = support.createMock(DataTypeInfo.class);
        EasyMock.expect(notThere.getMetaDataInfo()).andReturn(null);

        Set<DataGroupInfo> groups = createGroups(support, notThere);
        DataTypeController typeController = support.createMock(DataTypeController.class);
        EventManager eventManager = createEventManager(support);

        support.replayAll();

        DataTypeAutoAdder autoAdder = new DataTypeAutoAdder(eventManager, typeController);
        autoAdder.setExecutor(new InlineExecutor());

        ActiveDataGroupsChangedEvent event = new ActiveDataGroupsChangedEvent(this, groups, New.set());
        myListener.notify(event);

        autoAdder.close();
        assertNull(myListener);

        support.verifyAll();
    }

    /**
     * Tests removing a deactivated {@link DataTypeInfo} from the controller.
     */
    @Test
    public void testDeactivate()
    {
        myListener = null;

        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo notThere = createDataTypeDeactivate(support, ourNewDtiKey);
        DataTypeInfo alreadyThere = createDataTypeDeactivate(support, ourExistingDtiKey);

        Set<DataGroupInfo> groups = createGroups(support, alreadyThere, notThere);
        DataTypeController typeController = createControllerDeactivate(support, alreadyThere);
        EventManager eventManager = createEventManager(support);

        support.replayAll();

        DataTypeAutoAdder autoAdder = new DataTypeAutoAdder(eventManager, typeController);
        autoAdder.setExecutor(new InlineExecutor());

        ActiveDataGroupsChangedEvent event = new ActiveDataGroupsChangedEvent(this, New.set(), groups);
        myListener.notify(event);

        autoAdder.close();
        assertNull(myListener);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link DataTypeController}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link DataTypeController}.
     */
    private DataTypeController createController(EasyMockSupport support)
    {
        DataTypeController controller = support.createMock(DataTypeController.class);

        EasyMock.expect(Boolean.valueOf(controller.hasDataTypeInfoForTypeKey(EasyMock.cmpEq(ourNewDtiKey))))
                .andReturn(Boolean.FALSE).anyTimes();
        EasyMock.expect(Boolean.valueOf(controller.hasDataTypeInfoForTypeKey(EasyMock.cmpEq(ourExistingDtiKey))))
                .andReturn(Boolean.TRUE).anyTimes();

        return controller;
    }

    /**
     * Creates a mocked {@link DataTypeController} expected and data type to be
     * added.
     *
     * @param support Used to create the mock.
     * @param expected The expected {@link DataTypeInfo} to be added.
     * @return The mocked {@link DataTypeController}.
     */
    private DataTypeController createControllerActivate(EasyMockSupport support, DataTypeInfo expected)
    {
        DataTypeController controller = createController(support);

        controller.addDataType(EasyMock.cmpEq(DataTypeAutoAdder.class.getName()),
                EasyMock.cmpEq(DataTypeAutoAdder.class.getName()), EasyMock.eq(expected), EasyMock.isA(DataTypeAutoAdder.class));

        return controller;
    }

    /**
     * Creates a mocked {@link DataTypeController} expecting a data type to be
     * removed.
     *
     * @param support Used to create the mock.
     * @param expected The expected {@link DataTypeInfo} to be removed.
     * @return The mocked {@link DataTypeController}.
     */
    private DataTypeController createControllerDeactivate(EasyMockSupport support, DataTypeInfo expected)
    {
        DataTypeController controller = createController(support);

        EasyMock.expect(Boolean.valueOf(controller.removeDataType(EasyMock.eq(expected), EasyMock.isA(DataTypeAutoAdder.class))))
                .andReturn(Boolean.TRUE);

        return controller;
    }

    /**
     * Creates a mocked data type info.
     *
     * @param support Used to create the mock.
     * @param id The id of the type.
     * @param keys The column names.
     * @return The type info.
     */
    private DataTypeInfo createDataType(EasyMockSupport support, String id, List<String> keys)
    {
        MetaDataInfo metadatainfo = support.createMock(MetaDataInfo.class);
        EasyMock.expect(metadatainfo.getKeyNames()).andReturn(keys);

        DataTypeInfo dataType = createDataTypeDeactivate(support, id);
        EasyMock.expect(dataType.getMetaDataInfo()).andReturn(metadatainfo).times(2);

        return dataType;
    }

    /**
     * Creates a mocked data type info.
     *
     * @param support Used to create the mock.
     * @param id The id of the type.
     * @return The type info.
     */
    private DataTypeInfo createDataTypeDeactivate(EasyMockSupport support, String id)
    {
        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(dataType.getTypeKey()).andReturn(id);

        return dataType;
    }

    /**
     * Creates a mocked {@link EventManager}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link EventManager}.
     */
    @SuppressWarnings("unchecked")
    private EventManager createEventManager(EasyMockSupport support)
    {
        EventManager eventManager = support.createMock(EventManager.class);

        eventManager.subscribe(EasyMock.eq(ActiveDataGroupsChangedEvent.class), EasyMock.isA(DataTypeAutoAdder.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myListener = (EventListener<ActiveDataGroupsChangedEvent>)EasyMock.getCurrentArguments()[1];
            return null;
        });
        eventManager.unsubscribe(EasyMock.eq(ActiveDataGroupsChangedEvent.class), EasyMock.isA(DataTypeAutoAdder.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            EventListener<ActiveDataGroupsChangedEvent> listener = (EventListener<ActiveDataGroupsChangedEvent>)EasyMock
                    .getCurrentArguments()[1];
            if (myListener.equals(listener))
            {
                myListener = null;
            }
            return null;
        });

        return eventManager;
    }

    /**
     * Creates a mocked group for each passed in {@link DataTypeInfo}.
     *
     * @param support Used to create the mock.
     * @param types The types to create groups for.
     * @return The mocked groups.
     */
    private Set<DataGroupInfo> createGroups(EasyMockSupport support, DataTypeInfo... types)
    {
        Set<DataGroupInfo> groups = New.set();

        for (DataTypeInfo type : types)
        {
            DataGroupInfo group = support.createMock(DataGroupInfo.class);
            EasyMock.expect(Boolean.valueOf(group.hasMembers(EasyMock.eq(false)))).andReturn(Boolean.TRUE);
            EasyMock.expect(group.getMembers(EasyMock.eq(false))).andReturn(New.set(type));

            groups.add(group);
        }

        return groups;
    }
}
