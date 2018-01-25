package io.opensphere.xyztile.mantle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.order.OrderChangeListener;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.order.impl.DefaultOrderParticipantKey;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoPreferenceAssistant;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.impl.DefaultBasicVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.xyztile.model.Projection;
import io.opensphere.xyztile.model.XYZDataTypeInfo;
import io.opensphere.xyztile.model.XYZServerInfo;
import io.opensphere.xyztile.model.XYZSettings;
import io.opensphere.xyztile.model.XYZTileLayerInfo;
import io.opensphere.xyztile.util.XYZTileUtils;

/**
 * Unit test for the {@link XYZMantleController}.
 */
public class XYZMantleControllerTest
{
    /**
     * Our test layer info.
     */
    private static final XYZTileLayerInfo ourLayerInfo1 = new XYZTileLayerInfo("name1", "displayName1", Projection.EPSG_4326, 1,
            false, 0, new XYZServerInfo("serverName", "http://somehost"));

    /**
     * Our test layer info.
     */
    private static final XYZTileLayerInfo ourLayerInfo2 = new XYZTileLayerInfo("name2", "displayName2", Projection.EPSG_4326, 1,
            false, 0, new XYZServerInfo("serverName", "http://somehost"));

    /**
     * The test parent id.
     */
    private static final String ourParentId = "parentId";

    /**
     * The added root group.
     */
    private DataGroupInfo myAddedRootGroup;

    /**
     * Tests when all layers are removed.
     */
    @Test
    public void testAllValuesRemoved()
    {
        myAddedRootGroup = null;
        EasyMockSupport support = new EasyMockSupport();

        DataGroupController groupController = createGroupController(support, true, null);
        DataRegistry dataRegistry = createDataRegistry(support);
        OrderManagerRegistry orderRegistry = createOrderRegistry(support, 2);
        Toolbox toolbox = createToolbox(support, groupController, dataRegistry, orderRegistry);

        support.replayAll();

        XYZMantleController controller = new XYZMantleController(toolbox);
        controller.valuesAdded(new DataModelCategory(null, XYZTileUtils.LAYERS_FAMILY, null), new long[] { 0, 1 },
                New.list(ourLayerInfo1, ourLayerInfo2), this);

        assertNotNull(myAddedRootGroup);
        Collection<DataGroupInfo> children = myAddedRootGroup.getChildren();
        assertEquals(2, children.size());

        controller.allValuesRemoved(this);

        assertEquals(0, myAddedRootGroup.getChildren().size());

        controller.close();

        support.verifyAll();
    }

    /**
     * Tests layers being added.
     */
    @Test
    public void testLayersAdded()
    {
        ourLayerInfo1.setDescription("I am description");
        myAddedRootGroup = null;
        EasyMockSupport support = new EasyMockSupport();

        DataGroupController groupController = createGroupController(support, false, null);
        DataRegistry dataRegistry = createDataRegistry(support);
        OrderManagerRegistry orderRegistry = createOrderRegistry(support, 0);
        Toolbox toolbox = createToolbox(support, groupController, dataRegistry, orderRegistry);

        support.replayAll();

        XYZMantleController controller = new XYZMantleController(toolbox);
        controller.valuesAdded(new DataModelCategory(null, XYZTileUtils.LAYERS_FAMILY, null), new long[] { 0, 1 },
                New.list(ourLayerInfo1, ourLayerInfo2), this);

        assertNotNull(myAddedRootGroup);
        Collection<DataGroupInfo> children = myAddedRootGroup.getChildren();
        assertEquals(2, children.size());

        for (DataGroupInfo child : children)
        {
            if (child.getDisplayName().endsWith("1"))
            {
                assertGroup(ourLayerInfo1, child);
            }
            else
            {
                assertGroup(ourLayerInfo2, child);
            }
        }

        controller.close();

        support.verifyAll();
    }

    /**
     * Tests layers being added when the server group already exists.
     */
    @Test
    public void testLayersAddedGroupExists()
    {
        ourLayerInfo1.setParentId(ourParentId);
        ourLayerInfo2.setParentId(ourParentId);

        try
        {
            myAddedRootGroup = null;
            EasyMockSupport support = new EasyMockSupport();

            DataGroupInfo serverGroup = createServerGroupWithChildren(support);
            DataGroupController groupController = createGroupController(support, false, serverGroup);
            DataRegistry dataRegistry = createDataRegistry(support);
            OrderManagerRegistry orderRegistry = createOrderRegistry(support, 0);
            Toolbox toolbox = createToolbox(support, groupController, dataRegistry, orderRegistry);

            support.replayAll();

            XYZMantleController controller = new XYZMantleController(toolbox);
            controller.valuesAdded(new DataModelCategory(null, XYZTileUtils.LAYERS_FAMILY, null), new long[] { 0, 1 },
                    New.list(ourLayerInfo1, ourLayerInfo2), this);

            assertNull(myAddedRootGroup);

            controller.close();

            support.verifyAll();
        }
        finally
        {
            ourLayerInfo1.setParentId(null);
            ourLayerInfo2.setParentId(null);
        }
    }

    /**
     * Tests layers being added when the server group already exists.
     */
    @Test
    public void testLayersAddedServerExists()
    {
        myAddedRootGroup = null;
        EasyMockSupport support = new EasyMockSupport();

        DataGroupInfo serverGroup = createServerGroup(support);
        DataGroupController groupController = createGroupController(support, false, serverGroup);
        DataRegistry dataRegistry = createDataRegistry(support);
        OrderManagerRegistry orderRegistry = createOrderRegistry(support, 0);
        Toolbox toolbox = createToolbox(support, groupController, dataRegistry, orderRegistry);

        support.replayAll();

        XYZMantleController controller = new XYZMantleController(toolbox);
        controller.valuesAdded(new DataModelCategory(null, XYZTileUtils.LAYERS_FAMILY, null), new long[] { 0, 1 },
                New.list(ourLayerInfo1, ourLayerInfo2), this);

        assertNull(myAddedRootGroup);

        controller.close();

        support.verifyAll();
    }

    /**
     * Tests layers being removed.
     */
    @Test
    public void testLayersRemoved()
    {
        myAddedRootGroup = null;
        EasyMockSupport support = new EasyMockSupport();

        DataGroupController groupController = createGroupController(support, false, null);
        DataRegistry dataRegistry = createDataRegistry(support);
        OrderManagerRegistry orderRegistry = createOrderRegistry(support, 1);
        Toolbox toolbox = createToolbox(support, groupController, dataRegistry, orderRegistry);

        support.replayAll();

        XYZMantleController controller = new XYZMantleController(toolbox);
        controller.valuesAdded(new DataModelCategory(null, XYZTileUtils.LAYERS_FAMILY, null), new long[] { 0, 1 },
                New.list(ourLayerInfo1, ourLayerInfo2), this);

        assertNotNull(myAddedRootGroup);
        Collection<DataGroupInfo> children = myAddedRootGroup.getChildren();
        assertEquals(2, children.size());

        controller.valuesRemoved(new DataModelCategory(null, XYZTileUtils.LAYERS_FAMILY, null), new long[] { 1 }, this);

        children = myAddedRootGroup.getChildren();
        assertEquals(1, children.size());
        Iterator<DataGroupInfo> iterator = children.iterator();
        DataGroupInfo group1 = iterator.next();
        assertGroup(ourLayerInfo1, group1);

        controller.close();

        support.verifyAll();
    }

    /**
     * Asserts the added children.
     *
     * @return Null.
     */
    private Void addChildAnswer()
    {
        DataGroupInfo child = (DataGroupInfo)EasyMock.getCurrentArguments()[0];
        if (child.getDisplayName().endsWith("1"))
        {
            assertGroup(ourLayerInfo1, child);
        }
        else
        {
            assertGroup(ourLayerInfo2, child);
        }
        return null;
    }

    /**
     * The answer for the mocked addMember call.
     *
     * @return Null.
     */
    private Void addMemberAnswer()
    {
        XYZDataTypeInfo child = (XYZDataTypeInfo)EasyMock.getCurrentArguments()[0];
        if (child.getDisplayName().endsWith("1"))
        {
            assertDataType(ourLayerInfo1, child);
        }
        else
        {
            assertDataType(ourLayerInfo2, child);
        }
        return null;
    }

    /**
     * The answer to the add root mock call.
     *
     * @return true.
     */
    private boolean addRootAnswer()
    {
        DataGroupInfo dataGroup = (DataGroupInfo)EasyMock.getCurrentArguments()[0];
        assertEquals(ourLayerInfo1.getServerUrl(), dataGroup.getId());
        assertEquals(ourLayerInfo1.getServerInfo().getServerName(), dataGroup.getDisplayName());
        assertTrue(dataGroup.isRootNode());

        myAddedRootGroup = dataGroup;

        return true;
    }

    /**
     * Asserts the data type.
     *
     * @param layerInfo The layer info the data type represents.
     * @param dataType The data type to assert.
     */
    private void assertDataType(XYZTileLayerInfo layerInfo, XYZDataTypeInfo dataType)
    {
        assertEquals(layerInfo, dataType.getLayerInfo());
        assertEquals(DefaultBasicVisualizationInfo.LOADS_TO_BASE_ONLY,
                dataType.getBasicVisualizationInfo().getSupportedLoadsToTypes());
        assertEquals(LoadsTo.BASE, dataType.getBasicVisualizationInfo().getLoadsTo());
        if (layerInfo.getDisplayName().endsWith("2"))
        {
            assertEquals(158f, dataType.getMapVisualizationInfo().getTileRenderProperties().getOpacity(), 0f);
            assertEquals(2, dataType.getMapVisualizationInfo().getTileRenderProperties().getZOrder());
        }
        else
        {
            assertEquals(157f, dataType.getMapVisualizationInfo().getTileRenderProperties().getOpacity(), 0f);
            assertEquals(1, dataType.getMapVisualizationInfo().getTileRenderProperties().getZOrder());
        }
        assertEquals(MapVisualizationType.IMAGE_TILE, dataType.getMapVisualizationInfo().getVisualizationType());
        assertTrue(dataType.getMapVisualizationInfo().usesVisualizationStyles());
        assertTrue(dataType.getMapVisualizationInfo().getTileRenderProperties().isDrawable());
        assertFalse(dataType.getMapVisualizationInfo().getTileRenderProperties().isHidden());
        assertEquals(new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY,
                DefaultOrderCategory.IMAGE_BASE_MAP_CATEGORY, dataType.getTypeKey()), dataType.getOrderKey());

        assertEquals(layerInfo.getServerUrl() + layerInfo.getName(), dataType.getTypeKey());
    }

    /**
     * Asserts the data group and its member.
     *
     * @param layerInfo The layer info the group represents.
     * @param dataGroup The group to assert.
     */
    private void assertGroup(XYZTileLayerInfo layerInfo, DataGroupInfo dataGroup)
    {
        assertEquals(layerInfo.getDisplayName(), dataGroup.getDisplayName());
        assertEquals(XYZTileUtils.XYZ_PROVIDER, dataGroup.getProviderType());
        assertFalse(dataGroup.isRootNode());

        Collection<DataTypeInfo> members = dataGroup.getMembers(true);
        assertEquals(1, members.size());

        XYZDataTypeInfo dataType = (XYZDataTypeInfo)members.iterator().next();

        assertDataType(layerInfo, dataType);

        assertEquals(dataType.getTypeKey(), dataGroup.getId());
        assertEquals(layerInfo.getDescription(), dataGroup.getGroupDescription());
        assertTrue(dataGroup.getAssistant() instanceof XYZDataGroupInfoAssistant);
    }

    /**
     * Creates an easy mocked {@link DataRegistry}.
     *
     * @param support Used to create the mock.
     * @return The mocked data registry.
     */
    private DataRegistry createDataRegistry(EasyMockSupport support)
    {
        DataRegistry registry = support.createMock(DataRegistry.class);

        DataModelCategory category = new DataModelCategory(null, XYZTileUtils.LAYERS_FAMILY, null);
        registry.addChangeListener(EasyMock.isA(XYZMantleController.class), EasyMock.eq(category),
                EasyMock.eq(XYZTileUtils.LAYERS_DESCRIPTOR));
        registry.removeChangeListener(EasyMock.isA(XYZMantleController.class));

        return registry;
    }

    /**
     * Creates an easy mocked {@link DataGroupController}.
     *
     * @param support Used to create the mock.
     * @param expectRemove True if the mock should expect the root group to be
     *            removed.
     * @param serverGroup An already existing server group, or null if we want
     *            the mantle controller to create its own.
     * @return The mocked data group controller.
     */
    private DataGroupController createGroupController(EasyMockSupport support, boolean expectRemove, DataGroupInfo serverGroup)
    {
        DataGroupController controller = support.createMock(DataGroupController.class);

        Set<DataGroupInfo> rootGroups = New.set();
        if (serverGroup != null)
        {
            rootGroups.add(serverGroup);
        }

        EasyMock.expect(controller.getDataGroupInfoSet()).andAnswer(() -> getDataGroupInfoSetAnswer(rootGroups)).atLeastOnce();
        if (serverGroup == null)
        {
            EasyMock.expect(Boolean.valueOf(controller.addRootDataGroupInfo(EasyMock.isA(DefaultDataGroupInfo.class),
                    EasyMock.isA(XYZMantleController.class)))).andAnswer(this::addRootAnswer);
        }
        if (expectRemove)
        {
            EasyMock.expect(Boolean.valueOf(controller.removeDataGroupInfo(EasyMock.isA(DefaultDataGroupInfo.class),
                    EasyMock.isA(XYZMantleController.class)))).andAnswer(this::removeGroupAnswer);
        }

        return controller;
    }

    /**
     * Creates an easy mocked {@link OrderManagerRegistry}.
     *
     * @param support Used to create the mock.
     * @param removeCount The number of expected deactivates.
     * @return The mocked {@link OrderManagerRegistry}.
     */
    private OrderManagerRegistry createOrderRegistry(EasyMockSupport support, int removeCount)
    {
        DefaultOrderParticipantKey key1 = new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY,
                DefaultOrderCategory.IMAGE_BASE_MAP_CATEGORY, ourLayerInfo1.getServerUrl() + ourLayerInfo1.getName());
        DefaultOrderParticipantKey key2 = new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY,
                DefaultOrderCategory.IMAGE_BASE_MAP_CATEGORY, ourLayerInfo2.getServerUrl() + ourLayerInfo2.getName());
        OrderManager orderManager = support.createMock(OrderManager.class);
        EasyMock.expect(Integer.valueOf(orderManager.activateParticipant(EasyMock.eq(key1)))).andReturn(Integer.valueOf(1));
        EasyMock.expect(Integer.valueOf(orderManager.activateParticipant(EasyMock.eq(key2)))).andReturn(Integer.valueOf(2));

        EasyMock.expect(Integer.valueOf(orderManager.deactivateParticipant(EasyMock.eq(key2)))).andReturn(Integer.valueOf(2));
        EasyMock.expect(Integer.valueOf(orderManager.deactivateParticipant(EasyMock.eq(key1)))).andReturn(Integer.valueOf(1));
        orderManager.addParticipantChangeListener(EasyMock.isA(OrderChangeListener.class));
        orderManager.removeParticipantChangeListener(EasyMock.isA(OrderChangeListener.class));

        OrderManager dataOrderManager = support.createMock(OrderManager.class);
        dataOrderManager.addParticipantChangeListener(EasyMock.isA(OrderChangeListener.class));
        dataOrderManager.removeParticipantChangeListener(EasyMock.isA(OrderChangeListener.class));

        EasyMock.expect(orderManager.getFamily()).andReturn(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY).atLeastOnce();
        EasyMock.expect(orderManager.getCategory()).andReturn(DefaultOrderCategory.IMAGE_BASE_MAP_CATEGORY).atLeastOnce();
        OrderManagerRegistry orderRegistry = support.createMock(OrderManagerRegistry.class);
        EasyMock.expect(orderRegistry.getOrderManager(EasyMock.eq(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY),
                EasyMock.eq(DefaultOrderCategory.IMAGE_BASE_MAP_CATEGORY))).andReturn(orderManager).atLeastOnce();
        EasyMock.expect(orderRegistry.getOrderManager(EasyMock.eq(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY),
                EasyMock.eq(DefaultOrderCategory.IMAGE_DATA_CATEGORY))).andReturn(dataOrderManager);

        return orderRegistry;
    }

    /**
     * Creates an easy mocked {@link PreferencesRegistry}.
     *
     * @param support Used to create the mock.
     * @return The mock.
     */
    private PreferencesRegistry createPrefsRegistry(EasyMockSupport support)
    {
        Preferences prefs = support.createMock(Preferences.class);

        XYZSettings settings = new XYZSettings();
        settings.setMaxZoomLevelCurrent(14);

        EasyMock.expect(prefs.getJAXBObject(EasyMock.eq(XYZSettings.class), EasyMock.cmpEq(ourLayerInfo1.getName()),
                EasyMock.isA(XYZSettings.class))).andReturn(settings);
        EasyMock.expect(prefs.getJAXBObject(EasyMock.eq(XYZSettings.class), EasyMock.cmpEq(ourLayerInfo2.getName()),
                EasyMock.isA(XYZSettings.class))).andReturn(settings);

        PreferencesRegistry prefsRegistry = support.createMock(PreferencesRegistry.class);

        EasyMock.expect(prefsRegistry.getPreferences(EasyMock.eq(XYZSettings.class))).andReturn(prefs);

        return prefsRegistry;
    }

    /**
     * Creates an easy mocked {@link DataGroupInfo} representing an already
     * existing server group.
     *
     * @param support Used to create the mock.
     * @return The mock.
     */
    private DataGroupInfo createServerGroup(EasyMockSupport support)
    {
        DataGroupInfo group = support.createMock(DataGroupInfo.class);

        EasyMock.expect(group.getDisplayName()).andReturn("serverName").atLeastOnce();
        group.addChild(EasyMock.isA(DataGroupInfo.class), EasyMock.isA(XYZMantleController.class));
        EasyMock.expectLastCall().andAnswer(this::addChildAnswer).times(2);
        EasyMock.expect(Boolean.valueOf(group.isRootNode())).andReturn(Boolean.TRUE).atLeastOnce();
        return group;
    }

    /**
     * Creates an easy mocked {@link DataGroupInfo} representing an already
     * existing server group.
     *
     * @param support Used to create the mock.
     * @return The mock.
     */
    private DataGroupInfo createServerGroupWithChildren(EasyMockSupport support)
    {
        DataGroupInfo child1 = support.createMock(DataGroupInfo.class);
        EasyMock.expect(child1.getId()).andReturn("some other id").anyTimes();
        EasyMock.expect(child1.getChildren()).andReturn(New.list()).anyTimes();

        DataGroupInfo child2 = support.createMock(DataGroupInfo.class);
        EasyMock.expect(child2.getId()).andReturn(ourParentId).atLeastOnce();
        EasyMock.expect(Boolean.valueOf(child2.hasMembers(EasyMock.eq(false)))).andReturn(Boolean.FALSE).atLeastOnce();
        EasyMock.expect(Boolean.valueOf(child2.isRootNode())).andReturn(Boolean.FALSE).atLeastOnce();

        DataGroupInfo group = support.createMock(DataGroupInfo.class);

        EasyMock.expect(group.getDisplayName()).andReturn("serverName").atLeastOnce();
        child2.addMember(EasyMock.isA(XYZDataTypeInfo.class), EasyMock.isA(XYZMantleController.class));
        EasyMock.expectLastCall().andAnswer(this::addMemberAnswer).times(2);
        EasyMock.expect(group.getChildren()).andReturn(New.list(child1, child2)).atLeastOnce();

        return group;
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @param groupController A mocked {@link DataGroupController}.
     * @param dataRegistry A mocked {@link DataRegistry}.
     * @param orderRegistry A mocked {@link OrderManagerRegistry}.
     * @return The mocked toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support, DataGroupController groupController, DataRegistry dataRegistry,
            OrderManagerRegistry orderRegistry)
    {
        Toolbox toolbox = support.createMock(Toolbox.class);

        EasyMock.expect(toolbox.getDataRegistry()).andReturn(dataRegistry);

        DataTypeInfoPreferenceAssistant prefAssistant = support.createMock(DataTypeInfoPreferenceAssistant.class);
        EasyMock.expect(Integer.valueOf(prefAssistant
                .getOpacityPreference(EasyMock.cmpEq(ourLayerInfo1.getServerUrl() + ourLayerInfo1.getName()), EasyMock.eq(255))))
                .andReturn(Integer.valueOf(157)).atLeastOnce();
        EasyMock.expect(Integer.valueOf(prefAssistant
                .getOpacityPreference(EasyMock.cmpEq(ourLayerInfo2.getServerUrl() + ourLayerInfo2.getName()), EasyMock.eq(255))))
                .andReturn(Integer.valueOf(158)).atLeastOnce();
        EasyMock.expect(Boolean.valueOf(prefAssistant.isVisiblePreference(EasyMock.anyObject()))).andReturn(Boolean.TRUE)
                .atLeastOnce();
        EasyMock.expect(Integer.valueOf(prefAssistant.getColorPreference(
                EasyMock.cmpEq(ourLayerInfo1.getServerUrl() + ourLayerInfo1.getName()), EasyMock.eq(Color.white.getRGB()))))
                .andReturn(Integer.valueOf(Color.white.getRGB()));
        EasyMock.expect(Integer.valueOf(prefAssistant.getColorPreference(
                EasyMock.cmpEq(ourLayerInfo2.getServerUrl() + ourLayerInfo2.getName()), EasyMock.eq(Color.white.getRGB()))))
                .andReturn(Integer.valueOf(Color.white.getRGB()));

        MantleToolbox mantleToolbox = support.createMock(MantleToolbox.class);
        EasyMock.expect(mantleToolbox.getDataGroupController()).andReturn(groupController).atLeastOnce();
        EasyMock.expect(mantleToolbox.getDataTypeInfoPreferenceAssistant()).andReturn(prefAssistant).atLeastOnce();

        PluginToolboxRegistry toolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(EasyMock.eq(MantleToolbox.class))).andReturn(mantleToolbox)
                .atLeastOnce();
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(toolboxRegistry).atLeastOnce();

        EventManager eventManager = support.createNiceMock(EventManager.class);
        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager).atLeastOnce();

        EasyMock.expect(toolbox.getOrderManagerRegistry()).andReturn(orderRegistry).atLeastOnce();

        PreferencesRegistry prefsRegistry = createPrefsRegistry(support);
        EasyMock.expect(toolbox.getPreferencesRegistry()).andReturn(prefsRegistry);

        return toolbox;
    }

    /**
     * The answer for the getDataGroupInfoSet mock call.
     *
     * @param rootGroups The root groups to return.
     * @return The root groups.
     */
    private Set<DataGroupInfo> getDataGroupInfoSetAnswer(Set<DataGroupInfo> rootGroups)
    {
        Set<DataGroupInfo> returnGroups = New.set();

        returnGroups.addAll(rootGroups);
        if (myAddedRootGroup != null)
        {
            returnGroups.add(myAddedRootGroup);
        }

        return returnGroups;
    }

    /**
     * The answer to the mocked removedGroup call.
     *
     * @return True.
     */
    private boolean removeGroupAnswer()
    {
        assertEquals(myAddedRootGroup, EasyMock.getCurrentArguments()[0]);

        return true;
    }
}
