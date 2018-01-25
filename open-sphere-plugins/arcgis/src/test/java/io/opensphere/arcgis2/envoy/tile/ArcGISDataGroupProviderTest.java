package io.opensphere.arcgis2.envoy.tile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.arcgis2.model.ArcGISDataGroupInfo;
import io.opensphere.core.Toolbox;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * Unit test for {@link ArcGISDataGroupProvider}.
 */
public class ArcGISDataGroupProviderTest
{
    /**
     * The id of the layer's group.
     */
    private static final String ourGroupId = "http://somehost/layer1/3";

    /**
     * The xyz layer id.
     */
    private static final String ourLayer = "http://somehost/layer1/MapServer";

    /**
     * Tests finding the group.
     */
    @Test
    public void testGetDataGroup()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support);
        List<DataGroupInfo> dataGroups = createDataGroups(support, toolbox);
        ArcGISDataGroupInfo theGroup = new ArcGISDataGroupInfo(toolbox, "The Group", ourGroupId);
        dataGroups.add(theGroup);

        DataGroupController dataGroupController = createController(support, dataGroups);

        support.replayAll();

        ArcGISDataGroupProvider provider = new ArcGISDataGroupProvider(dataGroupController);

        DataModelCategory category = new DataModelCategory(null, null, ourLayer);
        ArcGISDataGroupInfo found = provider.getDataGroup(category);

        assertEquals(theGroup, found);

        support.verifyAll();
    }

    /**
     * Tests when the group isn't found.
     */
    @Test
    public void testGetDataGroupNotFound()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support);
        List<DataGroupInfo> dataGroups = createDataGroups(support, toolbox);

        DataGroupController dataGroupController = createController(support, dataGroups);

        support.replayAll();

        ArcGISDataGroupProvider provider = new ArcGISDataGroupProvider(dataGroupController);

        DataModelCategory category = new DataModelCategory(null, null, ourLayer);
        ArcGISDataGroupInfo found = provider.getDataGroup(category);

        assertNull(found);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link DataGroupController}.
     *
     * @param support Used to create the mock.
     * @param dataGroupInfos The test active data groups.
     * @return The mocked controller.
     */
    @SuppressWarnings("unchecked")
    private DataGroupController createController(EasyMockSupport support, List<DataGroupInfo> dataGroupInfos)
    {
        DataGroupController controller = support.createMock(DataGroupController.class);

        EasyMock.expect(controller.findActiveDataGroupInfo(EasyMock.isA(Predicate.class), EasyMock.eq(true)))
                .andAnswer(() -> findActiveAnswer(dataGroupInfos));

        return controller;
    }

    /**
     * Creates a list of other active data groups.
     *
     * @param support Used to create mocks.
     * @param toolbox A mocked toolbox.
     * @return The list of other active groups.
     */
    private List<DataGroupInfo> createDataGroups(EasyMockSupport support, Toolbox toolbox)
    {
        DataGroupInfo dataGroup = support.createMock(DataGroupInfo.class);

        ArcGISDataGroupInfo otherGroup = new ArcGISDataGroupInfo(toolbox, "dispalyName", "http://somehost/layer3/3");

        return New.list(dataGroup, otherGroup);
    }

    /**
     * Creates an easy mocked toolbox.
     *
     * @param support Used to create the mock.
     * @return The toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support)
    {
        Toolbox toolbox = support.createMock(Toolbox.class);

        return toolbox;
    }

    /**
     * The answer to the mocked findActiveDataGroupInfo call.
     *
     * @param dataGroupInfos The active data groups.
     * @return The group that passed the predicates test.
     */
    private Set<DataGroupInfo> findActiveAnswer(List<DataGroupInfo> dataGroupInfos)
    {
        Set<DataGroupInfo> dataGroups = New.set();

        @SuppressWarnings("unchecked")
        Predicate<DataGroupInfo> predicate = (Predicate<DataGroupInfo>)EasyMock.getCurrentArguments()[0];
        for (DataGroupInfo dataGroupInfo : dataGroupInfos)
        {
            if (predicate.test(dataGroupInfo))
            {
                dataGroups.add(dataGroupInfo);
                break;
            }
        }

        return dataGroups;
    }
}
