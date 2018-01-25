package io.opensphere.stkterrain.mantle;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.stkterrain.model.TileSetMetadata;
import io.opensphere.stkterrain.util.Constants;

/**
 * Unit test for {@link STKDataGroupActivationListener}.
 */
public class STKDataGroupActivationListenerTest
{
    /**
     * The test layer name.
     */
    private static final String ourLayerName = "world";

    /**
     * The test server url.
     */
    private static final String ourServerUrl = "http://somehost/terrain";

    /**
     * Tests when a group is activated.
     */
    @Test
    public void testHandleCommitActivated()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataGroupInfo dataGroup = createDataGroup(support);

        DataRegistry dataRegistry = support.createMock(DataRegistry.class);
        EasyMock.expect(dataRegistry.submitQuery(EasyMock.isA(SimpleQuery.class))).andAnswer(this::queryAnswer);

        support.replayAll();

        STKDataGroupActivationListener listener = new STKDataGroupActivationListener(dataRegistry);
        listener.handleCommit(true, dataGroup, null);

        support.verifyAll();
    }

    /**
     * Tests when a group is deactivated.
     */
    @Test
    public void testHandleCommitDeactivated()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataGroupInfo dataGroup = createDataGroup(support);

        DataRegistry dataRegistry = support.createMock(DataRegistry.class);
        EasyMock.expect(dataRegistry.removeModels(
                EasyMock.eq(new DataModelCategory(ourServerUrl, TileSetMetadata.class.getName(), ourLayerName)),
                EasyMock.eq(false))).andReturn(new long[] {});

        support.replayAll();

        STKDataGroupActivationListener listener = new STKDataGroupActivationListener(dataRegistry);
        listener.handleCommit(false, dataGroup, null);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link DataGroupInfo} representing an STK terrain
     * layer.
     *
     * @param support Used to create the mock.
     * @return The {@link DataGroupInfo}.
     */
    private DataGroupInfo createDataGroup(EasyMockSupport support)
    {
        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(dataType.getSourcePrefix()).andReturn(ourServerUrl);

        DataGroupInfo group = support.createMock(DataGroupInfo.class);

        EasyMock.expect(group.getMembers(EasyMock.eq(false))).andReturn(New.set(dataType));
        EasyMock.expect(group.getDisplayName()).andReturn(ourLayerName);

        return group;
    }

    /**
     * The answer for the query call.
     *
     * @return Null.
     */
    private QueryTracker queryAnswer()
    {
        @SuppressWarnings("unchecked")
        SimpleQuery<TileSetMetadata> query = (SimpleQuery<TileSetMetadata>)EasyMock.getCurrentArguments()[0];

        DataModelCategory expected = new DataModelCategory(ourServerUrl, TileSetMetadata.class.getName(), ourLayerName);

        assertEquals(expected, query.getDataModelCategory());
        assertEquals(Constants.TILESET_METADATA_PROPERTY_DESCRIPTOR,
                query.getPropertyValueReceivers().get(0).getPropertyDescriptor());

        return null;
    }
}
