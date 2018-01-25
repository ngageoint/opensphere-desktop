package io.opensphere.stkterrain.mantle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfoPreferenceAssistant;
import io.opensphere.stkterrain.model.TileSet;
import io.opensphere.stkterrain.util.Constants;

/**
 * Unit test for {@link STKDataGroupController}.
 */
public class STKDataGroupControllerTest
{
    /**
     * The test server name.
     */
    private static final String ourServerName = "I am server";

    /**
     * The test server url.
     */
    private static final String ourServerUrl = "http://somehost/terrain";

    /**
     * The test tile set name.
     */
    private static final String ourTileSetName1 = "world";

    /**
     * Another test tile set name.
     */
    private static final String ourTileSetName2 = "Fodar";

    /**
     * The list of test tile set names.
     */
    private static final List<String> ourTileSetNames = New.list(ourTileSetName1, ourTileSetName2);

    /**
     * Tests close.
     */
    @Test
    public void testClose()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<DataGroupInfo> rootGroups = New.list();

        DataRegistry dataRegistry = createDataRegistryForClose(support);
        DataGroupController groupController = createGroupControllerForClose(support, rootGroups);
        MantleToolbox mantleBox = createMantleToolbox(support, groupController);
        Toolbox toolbox = createToolbox(support, dataRegistry, mantleBox);

        support.replayAll();

        STKDataGroupController controller = new STKDataGroupController(toolbox, ourServerName, ourServerUrl);

        assertEquals(ourServerName, rootGroups.get(0).getDisplayName());

        assertFalse(controller.isIdArrayNeeded());
        controller.close();

        assertTrue(rootGroups.isEmpty());

        support.verifyAll();
    }

    /**
     * Tests when tile sets are added.
     */
    @Test
    public void testValuesAddedDataModelCategoryLongArrayIterableOfQextendsTileSetObject()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<DataGroupInfo> rootGroups = New.list();

        DataRegistry dataRegistry = createDataRegistry(support);
        DataGroupController groupController = createGroupController(support, rootGroups);
        MantleToolbox mantleBox = createMantleToolboxForAdd(support, groupController);
        Toolbox toolbox = createToolbox(support, dataRegistry, mantleBox);

        support.replayAll();

        STKDataGroupController controller = new STKDataGroupController(toolbox, ourServerName, ourServerUrl);

        List<TileSet> tileSets = createTileSets();
        controller.valuesAdded(new DataModelCategory(ourServerName, TileSet.class.getName(), TileSet.class.getName()),
                new long[] { 0, 1 }, tileSets, this);

        assertEquals(1, rootGroups.size());
        DataGroupInfo rootGroup = rootGroups.get(0);

        assertTrue(rootGroup.isRootNode());
        assertEquals(ourServerUrl, rootGroup.getId());
        assertEquals(ourServerName, rootGroup.getDisplayName());

        Collection<DataGroupInfo> dataGroups = rootGroup.getChildren();

        assertEquals(ourTileSetNames.size(), dataGroups.size());
        for (DataGroupInfo dataGroup : dataGroups)
        {
            assertTrue(ourTileSetNames.contains(dataGroup.getDisplayName()));
        }

        support.verifyAll();
    }

    /**
     * Creates a mocked {@link DataRegistry}.
     *
     * @param support Used to create the mock.
     * @return The mocked data registry.
     */
    private DataRegistry createDataRegistry(EasyMockSupport support)
    {
        DataRegistry dataRegistry = support.createMock(DataRegistry.class);
        dataRegistry.addChangeListener(EasyMock.isA(STKDataGroupController.class),
                EasyMock.eq(new DataModelCategory(ourServerUrl, TileSet.class.getName(), null)),
                EasyMock.eq(Constants.TILESET_PROPERTY_DESCRIPTOR));

        return dataRegistry;
    }

    /**
     * Creates a mocked {@link DataRegistry} used for the close test.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link DataRegistry}.
     */
    private DataRegistry createDataRegistryForClose(EasyMockSupport support)
    {
        DataRegistry dataRegistry = createDataRegistry(support);

        dataRegistry.removeChangeListener(EasyMock.isA(STKDataGroupController.class));

        return dataRegistry;
    }

    /**
     * Creates a mocked {@link DataGroupController}.
     *
     * @param support Used to create the mock.
     * @param rootGroups The list to add new root groups to.
     * @return The mocked {@link DataGroupController}.
     */
    private DataGroupController createGroupController(EasyMockSupport support, List<DataGroupInfo> rootGroups)
    {
        DataGroupController controller = support.createMock(DataGroupController.class);

        EasyMock.expect(
                controller.addRootDataGroupInfo(EasyMock.isA(DataGroupInfo.class), EasyMock.isA(STKDataGroupController.class)))
                .andAnswer(() ->
                {
                    return rootGroups.add((DataGroupInfo)EasyMock.getCurrentArguments()[0]);
                });

        return controller;
    }

    /**
     * Creates a {@link DataGroupController} for close.
     *
     * @param support Used to create the mock.
     * @param rootGroups The list of root groups to remove from.
     * @return The mocked {@link DataGroupController}.
     */
    private DataGroupController createGroupControllerForClose(EasyMockSupport support, List<DataGroupInfo> rootGroups)
    {
        DataGroupController controller = createGroupController(support, rootGroups);

        EasyMock.expect(
                controller.removeDataGroupInfo(EasyMock.isA(DataGroupInfo.class), EasyMock.isA(STKDataGroupController.class)))
                .andAnswer(() ->
                {
                    return rootGroups.remove(EasyMock.getCurrentArguments()[0]);
                });

        return controller;
    }

    /**
     * Creates a mocked {@link MantleToolbox}.
     *
     * @param support Used to create the mock.
     * @param groupController The {@link DataGroupController} to return.
     * @return The mocked {@link MantleToolbox}.
     */
    private MantleToolbox createMantleToolbox(EasyMockSupport support, DataGroupController groupController)
    {
        MantleToolbox mantleToolbox = support.createMock(MantleToolbox.class);

        EasyMock.expect(mantleToolbox.getDataGroupController()).andReturn(groupController);

        return mantleToolbox;
    }

    /**
     * Create a {@link MantleToolbox} for add.
     *
     * @param support Used to create the mock.
     * @param groupController The mocked {@link DataGroupController} to return.
     * @return The mantle toolbox.
     */
    private MantleToolbox createMantleToolboxForAdd(EasyMockSupport support, DataGroupController groupController)
    {
        MantleToolbox mantleToolbox = createMantleToolbox(support, groupController);

        DataTypeInfoPreferenceAssistant assistant = support.createNiceMock(DataTypeInfoPreferenceAssistant.class);

        EasyMock.expect(mantleToolbox.getDataTypeInfoPreferenceAssistant()).andReturn(assistant).atLeastOnce();

        return mantleToolbox;
    }

    /**
     * Creates a list of test tile sets.
     *
     * @return The list of tile sets.
     */
    private List<TileSet> createTileSets()
    {
        List<TileSet> tileSets = New.list();

        for (String tileName : ourTileSetNames)
        {
            TileSet tileSet = new TileSet();
            tileSet.setName(tileName);
            tileSets.add(tileSet);
        }

        return tileSets;
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @param dataRegistry A mocked {@link DataRegistry} to return.
     * @param mantleBox A mocked {@link MantleToolbox} to return.
     * @return The toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support, DataRegistry dataRegistry, MantleToolbox mantleBox)
    {
        OrderManager orderManager = support.createNiceMock(OrderManager.class);

        OrderManagerRegistry orderRegistry = support.createMock(OrderManagerRegistry.class);
        EasyMock.expect(orderRegistry.getOrderManager(EasyMock.eq(DefaultOrderCategory.DEFAULT_ELEVATION_FAMILY),
                EasyMock.eq(DefaultOrderCategory.EARTH_ELEVATION_CATEGORY))).andReturn(orderManager);

        PluginToolboxRegistry toolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(EasyMock.eq(MantleToolbox.class))).andReturn(mantleBox).atLeastOnce();

        EventManager eventManager = support.createNiceMock(EventManager.class);

        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getOrderManagerRegistry()).andReturn(orderRegistry);
        EasyMock.expect(toolbox.getDataRegistry()).andReturn(dataRegistry).atLeastOnce();
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(toolboxRegistry).atLeastOnce();
        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager).anyTimes();

        return toolbox;
    }
}
