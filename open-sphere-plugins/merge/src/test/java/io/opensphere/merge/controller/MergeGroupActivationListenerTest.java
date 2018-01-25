package io.opensphere.merge.controller;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.PropertyValueReceiver;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.merge.model.MergedDataRow;

/**
 * Unit test for {@link MergeGroupActivationListener}.
 */
public class MergeGroupActivationListenerTest
{
    /**
     * The layer id.
     */
    private static final String ourLayerId = "iamlayerid";

    /**
     * Tests when a group is activated.
     */
    @Test
    public void testHandleCommit()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<MergedDataRow> data = New.list();
        Map<String, Serializable> row1Map = New.map();
        row1Map.put("column1", "value1");
        MergedDataRow row1 = new MergedDataRow(row1Map, null, null);
        data.add(row1);

        Map<String, Serializable> row2Map = New.map();
        row2Map.put("column1", "value2");
        MergedDataRow row2 = new MergedDataRow(row2Map, null, null);
        data.add(row2);

        DataRegistry registry = createRegistry(support, data);
        List<DataElement> elements = New.list();
        DataTypeController typeController = createController(support, elements);
        DataGroupInfo dgi = createGroupInfo(support);

        support.replayAll();

        MergeGroupActivationListener listener = new MergeGroupActivationListener(registry, typeController);
        listener.handleCommit(true, dgi, null);

        assertEquals(2, elements.size());
        assertEquals("value1", elements.get(0).getMetaData().getValue("column1"));
        assertEquals("value2", elements.get(1).getMetaData().getValue("column1"));

        support.verifyAll();
    }

    /**
     * Tests when a group is activated.
     */
    @Test
    public void testHandleCommitDeactivate()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry registry = support.createMock(DataRegistry.class);
        DataTypeController typeController = support.createMock(DataTypeController.class);
        DataGroupInfo dgi = support.createMock(DataGroupInfo.class);

        support.replayAll();

        MergeGroupActivationListener listener = new MergeGroupActivationListener(registry, typeController);
        listener.handleCommit(false, dgi, null);

        support.verifyAll();
    }

    /**
     * Tests when a group is activated.
     */
    @Test
    public void testHandleCommitEmptyQuery()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry registry = createRegistry(support, New.list());
        DataTypeController typeController = support.createMock(DataTypeController.class);
        DataGroupInfo dgi = createGroupInfo(support);

        support.replayAll();

        MergeGroupActivationListener listener = new MergeGroupActivationListener(registry, typeController);
        listener.handleCommit(true, dgi, null);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked type controller.
     *
     * @param support Used to create the mock.
     * @param elements The list to add elements to.
     * @return The type controller.
     */
    private DataTypeController createController(EasyMockSupport support, List<DataElement> elements)
    {
        DataTypeController typeController = support.createMock(DataTypeController.class);

        EasyMock.expect(typeController.addDataElements(EasyMock.isA(MergeDataElementProvider.class), EasyMock.isNull(),
                EasyMock.isNull(), EasyMock.isA(MergeGroupActivationListener.class))).andAnswer(() ->
                {
                    MergeDataElementProvider provider = (MergeDataElementProvider)EasyMock.getCurrentArguments()[0];
                    List<Long> ids = New.list();
                    long id = 0;
                    while (provider.hasNext())
                    {
                        elements.add(provider.next());
                        ids.add(Long.valueOf(id));
                        id++;
                    }

                    return New.list(Long.valueOf(0), Long.valueOf(1));
                });

        return typeController;
    }

    /**
     * Creates a mocked group info.
     *
     * @param support Used to create the mock.
     * @return The mocked group info.
     */
    private DataGroupInfo createGroupInfo(EasyMockSupport support)
    {
        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(dataType.getTypeKey()).andReturn(ourLayerId);

        DataGroupInfo groupInfo = support.createMock(DataGroupInfo.class);

        EasyMock.expect(groupInfo.getMembers(EasyMock.eq(false))).andReturn(New.set(dataType));

        return groupInfo;
    }

    /**
     * Creates a mocked {@link DataRegistry}.
     *
     * @param support Used to create the mock.
     * @param data The merged test data.
     * @return The mocked registry.
     */
    private DataRegistry createRegistry(EasyMockSupport support, List<MergedDataRow> data)
    {
        DataRegistry registry = support.createMock(DataRegistry.class);

        EasyMock.expect(registry.performLocalQuery(EasyMock.isA(SimpleQuery.class))).andAnswer(() -> queryAnswer(data));

        return registry;
    }

    /**
     * The answer to the mocked query.
     *
     * @param data The merged test data.
     * @return The ids.
     */
    @SuppressWarnings("unchecked")
    private long[] queryAnswer(List<MergedDataRow> data)
    {
        SimpleQuery<MergedDataRow> query = (SimpleQuery<MergedDataRow>)EasyMock.getCurrentArguments()[0];
        assertEquals(DataRegistryUtils.getInstance().getMergeDataCategory(ourLayerId), query.getDataModelCategory());
        PropertyValueReceiver<MergedDataRow> valueReceiver = (PropertyValueReceiver<MergedDataRow>)query
                .getPropertyValueReceivers().iterator().next();
        assertEquals(DataRegistryUtils.MERGED_PROP_DESCRIPTOR, valueReceiver.getPropertyDescriptor());
        valueReceiver.receive(data);
        return new long[] { 0, 1 };
    }
}
