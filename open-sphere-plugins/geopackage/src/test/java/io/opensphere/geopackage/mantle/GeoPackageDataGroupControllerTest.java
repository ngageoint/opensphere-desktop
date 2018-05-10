package io.opensphere.geopackage.mantle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.MapManager;
import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.PropertyValueReceiver;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.event.Event;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.order.impl.DefaultOrderParticipantKey;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.model.GeoPackageFeatureLayer;
import io.opensphere.geopackage.model.GeoPackageLayer;
import io.opensphere.geopackage.model.GeoPackagePropertyDescriptors;
import io.opensphere.geopackage.model.GeoPackageTileLayer;
import io.opensphere.geopackage.model.LayerType;
import io.opensphere.geopackage.util.Constants;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.controller.event.impl.DataTypeRemovedEvent;
import io.opensphere.mantle.data.ActivationListener;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoPreferenceAssistant;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.event.DataGroupInfoChildAddedEvent;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;
import io.opensphere.mantle.data.impl.DefaultDeletableDataGroupInfo;

/**
 * Tests the {@link GeoPackageDataGroupController} class.
 */
@SuppressWarnings({ "PMD.GodClass", "boxing" })
public class GeoPackageDataGroupControllerTest
{
    /**
     * Test loading already imported geopackage layers.
     */
    @Test
    public void testDataGroupCreator()
    {
        EasyMockSupport support = new EasyMockSupport();

        GeoPackageTileLayer tilePackage1 = new GeoPackageTileLayer("one", "c:\\one.pkg", "tilePackage1", 100);
        tilePackage1.getExtensions().put(Constants.TERRAIN_EXTENSION, "something");
        GeoPackageLayer tilePackage2 = new GeoPackageTileLayer("two", "c:\\two.pkg", "tilePackage2", 101);
        GeoPackageLayer featurePackage1 = new GeoPackageFeatureLayer("one", "c:\\one.pkg", "featurePackage1", 10000);
        GeoPackageLayer featurePackage2 = new GeoPackageFeatureLayer("two", "c:\\two.pkg", "featurePackage2", 10001);
        List<GeoPackageLayer> layers = New.list(tilePackage1, featurePackage1, tilePackage2, featurePackage2);

        DataRegistry registry = createRegistry(support, layers);
        List<DataGroupInfo> rootGroups = New.list();
        DataGroupController controller = createGroupController(support, rootGroups);

        Set<String> imports = New.set();
        PreferencesRegistry prefsRegistry = createPrefsRegistry(support, imports);

        OrderManager orderManager = createOrderManager(support, layers);

        Toolbox toolbox = createToolbox(support, registry, controller, prefsRegistry, orderManager);

        Map<String, DataGroupInfo> children = New.map();

        LayerActivationListener tileListener = createNewLayerListener(support);

        support.replayAll();

        GeoPackageDataGroupController groupCreator = new GeoPackageDataGroupController(toolbox, tileListener);

        assertEquals(1, rootGroups.size());
        DataGroupInfo rootGroup = rootGroups.get(0);
        assertEquals("GPKG Files", rootGroup.getDisplayName());
        assertTrue(rootGroup.isRootNode());
        for (DataGroupInfo child : rootGroup.getChildren())
        {
            children.put(child.getId(), child);
        }

        assertEquals(2, children.size());

        Map<String, DataGroupInfo> actualGroups1 = New.map();
        Map<String, DataGroupInfo> actualGroups2 = New.map();

        DataGroupInfo packageGroup1 = children.get(tilePackage1.getPackageFile());

        assertNotNull(packageGroup1);
        assertEquals(tilePackage1.getPackageName(), packageGroup1.getDisplayName());
        assertTrue(imports.contains(tilePackage1.getPackageFile()));
        assertTrue(packageGroup1 instanceof DefaultDeletableDataGroupInfo);
        assertTrue(packageGroup1.getAssistant() instanceof GeoPackageDeleter);
        for (DataGroupInfo group : packageGroup1.getChildren())
        {
            actualGroups1.put(group.getId(), group);
        }

        DataGroupInfo packageGroup2 = children.get(tilePackage2.getPackageFile());
        assertEquals(tilePackage2.getPackageName(), packageGroup2.getDisplayName());
        assertTrue(imports.contains(tilePackage2.getPackageFile()));
        assertTrue(packageGroup2 instanceof DefaultDeletableDataGroupInfo);
        assertTrue(packageGroup2.getAssistant() instanceof GeoPackageDeleter);
        for (DataGroupInfo group : packageGroup2.getChildren())
        {
            actualGroups2.put(group.getId(), group);
        }

        assertEquals(2, actualGroups1.size());
        assertEquals(2, actualGroups2.size());

        for (GeoPackageLayer layer : layers)
        {
            Map<String, DataGroupInfo> actuals = actualGroups2;
            if (layer.getName().endsWith("1"))
            {
                actuals = actualGroups1;
            }

            DataGroupInfo group = actuals.get(layer.getPackageFile() + layer.getName());
            List<ActivationListener> listeners = group.activationProperty().getListeners();
            assertEquals(1, listeners.size());
            GeoPackageLayerActivationHandler provider = (GeoPackageLayerActivationHandler)listeners.get(0);
            assertEquals(groupCreator.getDataGroupBuilder().getActivationListener(), provider);
            assertEquals(provider.getTileLayerListener(), tileListener);
            assertEquals(layer.getName(), group.getDisplayName());
            Collection<DataTypeInfo> members = group.getMembers(false);
            assertEquals(1, members.size());

            DataTypeInfo dataType = members.iterator().next();

            assertTrue(dataType instanceof GeoPackageDataTypeInfo);
            assertEquals(layer.getName(), dataType.getDisplayName());
            assertEquals(layer.getPackageFile() + layer.getName() + layer.getLayerType(), dataType.getTypeKey());

            if (layer.getLayerType() == LayerType.FEATURE)
            {
                assertEquals(DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY, dataType.getOrderKey().getFamily());
                assertEquals(DefaultOrderCategory.FEATURE_CATEGORY, dataType.getOrderKey().getCategory());
                assertEquals(layer.getPackageFile() + layer.getName(), dataType.getOrderKey().getId());
                assertEquals(MapVisualizationType.MIXED_ELEMENTS, dataType.getMapVisualizationInfo().getVisualizationType());
                assertEquals(LoadsTo.TIMELINE, dataType.getBasicVisualizationInfo().getLoadsTo());
                assertEquals(New.<LoadsTo>set(LoadsTo.STATIC, LoadsTo.TIMELINE, LoadsTo.BASE),
                        dataType.getBasicVisualizationInfo().getSupportedLoadsToTypes());
            }
            else
            {
                assertEquals(layer.getPackageFile() + layer.getName(), dataType.getOrderKey().getId());
                if (layer.equals(tilePackage1))
                {
                    assertEquals(DefaultOrderCategory.DEFAULT_ELEVATION_FAMILY, dataType.getOrderKey().getFamily());
                    assertEquals(DefaultOrderCategory.EARTH_ELEVATION_CATEGORY, dataType.getOrderKey().getCategory());
                    assertEquals(MapVisualizationType.TERRAIN_TILE, dataType.getMapVisualizationInfo().getVisualizationType());
                }
                else
                {
                    assertEquals(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY, dataType.getOrderKey().getFamily());
                    assertEquals(DefaultOrderCategory.IMAGE_OVERLAY_CATEGORY, dataType.getOrderKey().getCategory());
                    assertEquals(MapVisualizationType.IMAGE_TILE, dataType.getMapVisualizationInfo().getVisualizationType());
                }
                assertNotNull(dataType.getMapVisualizationInfo().getTileRenderProperties());
                assertEquals(10, dataType.getMapVisualizationInfo().getTileRenderProperties().getZOrder());
                assertNotNull(dataType.getMapVisualizationInfo().getTileLevelController());
                assertEquals(LoadsTo.BASE, dataType.getBasicVisualizationInfo().getLoadsTo());
                assertEquals(New.<LoadsTo>set(LoadsTo.BASE), dataType.getBasicVisualizationInfo().getSupportedLoadsToTypes());
            }
        }

        GeoPackageDeleter deleter = (GeoPackageDeleter)packageGroup1.getAssistant();
        deleter.getDeleteListener().packageDeleted();
        assertFalse(imports.contains(tilePackage1.getPackageFile()));

        deleter = (GeoPackageDeleter)packageGroup2.getAssistant();
        deleter.getDeleteListener().packageDeleted();
        assertFalse(imports.contains(tilePackage2.getPackageFile()));

        groupCreator.close();

        assertTrue(rootGroups.isEmpty());

        support.verifyAll();
    }

    /**
     * Tests create data groups and data types for a new import.
     */
    @Test
    public void testNewImport()
    {
        EasyMockSupport support = new EasyMockSupport();

        GeoPackageLayer tilePackage1 = new GeoPackageTileLayer("won", "c:\\won.pkg", "layer1", 100);
        GeoPackageLayer tilePackage2 = new GeoPackageTileLayer("won", "c:\\won.pkg", "layer2", 101);
        GeoPackageLayer featurePackage1 = new GeoPackageFeatureLayer("won", "c:\\won.pkg", "layer1", 10000);
        GeoPackageLayer featurePackage2 = new GeoPackageFeatureLayer("won", "c:\\won.pkg", "layer2", 10000);
        List<GeoPackageLayer> layers = New.list(tilePackage1, featurePackage1, tilePackage2, featurePackage2);

        DataRegistry registry = createRegistry(support, New.list());
        List<DataGroupInfo> rootGroups = New.list();
        DataGroupController controller = createGroupController(support, rootGroups);

        Set<String> imports = New.set();
        PreferencesRegistry prefsRegistry = createPrefsRegistry(support, imports);

        OrderManager orderManager = createOrderManager(support, layers);

        Toolbox toolbox = createToolbox(support, registry, controller, prefsRegistry, orderManager);

        Map<String, DataGroupInfo> children = New.map();

        LayerActivationListener tileListener = createNewLayerListener(support);

        support.replayAll();

        GeoPackageDataGroupController creator = new GeoPackageDataGroupController(toolbox, tileListener);

        creator.valuesAdded(new DataModelCategory(tilePackage1.getPackageFile(), tilePackage1.getPackageName(),
                GeoPackageLayer.class.getName()), new long[] { 1, 2, 3, 4 }, layers, this);

        assertEquals(1, rootGroups.size());
        DataGroupInfo rootGroup = rootGroups.get(0);
        assertEquals("GPKG Files", rootGroup.getDisplayName());
        assertTrue(rootGroup.isRootNode());
        for (DataGroupInfo child : rootGroup.getChildren())
        {
            children.put(child.getId(), child);
        }

        assertEquals(1, children.size());

        Map<String, DataGroupInfo> actualGroups = New.map();

        DataGroupInfo packageGroup = children.get(tilePackage1.getPackageFile());
        assertEquals(tilePackage1.getPackageName(), packageGroup.getDisplayName());
        assertTrue(imports.contains(tilePackage1.getPackageFile()));
        assertTrue(packageGroup instanceof DefaultDeletableDataGroupInfo);
        assertTrue(packageGroup.getAssistant() instanceof GeoPackageDeleter);
        for (DataGroupInfo group : packageGroup.getChildren())
        {
            actualGroups.put(group.getId(), group);
        }

        assertEquals(2, actualGroups.size());

        DataGroupInfo group1 = actualGroups.get(tilePackage1.getPackageFile() + tilePackage1.getName());
        assertGroup(group1, featurePackage1, tilePackage1, tileListener, creator.getDataGroupBuilder());

        DataGroupInfo group2 = actualGroups.get(tilePackage1.getPackageFile() + tilePackage2.getName());
        assertGroup(group2, featurePackage2, tilePackage2, tileListener, creator.getDataGroupBuilder());

        GeoPackageDeleter deleter = (GeoPackageDeleter)packageGroup.getAssistant();
        deleter.getDeleteListener().packageDeleted();
        assertFalse(imports.contains(tilePackage1.getPackageFile()));

        creator.close();

        assertTrue(rootGroups.isEmpty());

        support.verifyAll();
    }

    /**
     * The answer for adding a child to the mocked root group.
     *
     * @param rootGroups The list to add the root to.
     * @return Null.
     */
    private boolean addChildAnswer(List<DataGroupInfo> rootGroups)
    {
        DataGroupInfo child = (DataGroupInfo)EasyMock.getCurrentArguments()[0];

        return rootGroups.add(child);
    }

    /**
     * Asserts the layer group.
     *
     * @param group The group to assert.
     * @param featureLayer The feature layer.
     * @param tileLayer The tile layer.
     * @param tileListener The expected tile listener.
     * @param builder The data group builder.
     */
    private void assertGroup(DataGroupInfo group, GeoPackageLayer featureLayer, GeoPackageLayer tileLayer,
            LayerActivationListener tileListener, DataGroupBuilder builder)
    {
        assertEquals(featureLayer.getName(), group.getDisplayName());
        List<ActivationListener> listeners = group.activationProperty().getListeners();
        assertEquals(1, listeners.size());
        GeoPackageLayerActivationHandler provider = (GeoPackageLayerActivationHandler)listeners.get(0);
        assertEquals(builder.getActivationListener(), provider);
        assertEquals(provider.getTileLayerListener(), tileListener);

        Collection<DataTypeInfo> members = group.getMembers(false);
        assertEquals(2, members.size());

        Iterator<DataTypeInfo> iterator = members.iterator();

        while (iterator.hasNext())
        {
            DataTypeInfo dataType = iterator.next();

            assertTrue(dataType instanceof GeoPackageDataTypeInfo);

            GeoPackageDataTypeInfo geopackageDataType = (GeoPackageDataTypeInfo)dataType;

            if (geopackageDataType.getLayer().getLayerType() == LayerType.TILE)
            {
                assertEquals(tileLayer.getName(), dataType.getDisplayName());
                assertEquals(tileLayer.getPackageFile() + tileLayer.getName() + tileLayer.getLayerType(), dataType.getTypeKey());
                assertEquals(LoadsTo.BASE, dataType.getBasicVisualizationInfo().getLoadsTo());
                assertEquals(New.<LoadsTo>set(LoadsTo.BASE), dataType.getBasicVisualizationInfo().getSupportedLoadsToTypes());

                assertEquals(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY, dataType.getOrderKey().getFamily());
                assertEquals(DefaultOrderCategory.IMAGE_OVERLAY_CATEGORY, dataType.getOrderKey().getCategory());
                assertEquals(tileLayer.getPackageFile() + tileLayer.getName(), dataType.getOrderKey().getId());
                assertEquals(MapVisualizationType.IMAGE_TILE, dataType.getMapVisualizationInfo().getVisualizationType());
                assertNotNull(dataType.getMapVisualizationInfo().getTileRenderProperties());
                assertEquals(10, dataType.getMapVisualizationInfo().getTileRenderProperties().getZOrder());
                assertNotNull(dataType.getMapVisualizationInfo().getTileLevelController());
            }
            else
            {
                assertEquals(featureLayer.getName(), dataType.getDisplayName());
                assertEquals(featureLayer.getPackageFile() + featureLayer.getName() + featureLayer.getLayerType(),
                        dataType.getTypeKey());
                assertEquals(LoadsTo.TIMELINE, dataType.getBasicVisualizationInfo().getLoadsTo());
                assertEquals(New.<LoadsTo>set(LoadsTo.STATIC, LoadsTo.TIMELINE, LoadsTo.BASE),
                        dataType.getBasicVisualizationInfo().getSupportedLoadsToTypes());

                assertEquals(DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY, dataType.getOrderKey().getFamily());
                assertEquals(DefaultOrderCategory.FEATURE_CATEGORY, dataType.getOrderKey().getCategory());
                assertEquals(featureLayer.getPackageFile() + featureLayer.getName(), dataType.getOrderKey().getId());
                assertEquals(MapVisualizationType.MIXED_ELEMENTS, dataType.getMapVisualizationInfo().getVisualizationType());
            }
        }
    }

    /**
     * Creates a mocked {@link DataTypeController}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link DataTypeController}.
     */
    private DataTypeController createController(EasyMockSupport support)
    {
        DataTypeController controller = support.createMock(DataTypeController.class);

        return controller;
    }

    /**
     * Creates an easy mocked {@link DataGroupController}.
     *
     * @param support Used to create the mock.
     * @param rootGroups The list to add the root group to.
     * @return The mocked controller.
     */
    private DataGroupController createGroupController(EasyMockSupport support, List<DataGroupInfo> rootGroups)
    {
        DataGroupController controller = support.createMock(DataGroupController.class);

        EasyMock.expect(controller.addRootDataGroupInfo(EasyMock.isA(DataGroupInfo.class), EasyMock.isA(Object.class)))
                .andAnswer(() -> addChildAnswer(rootGroups));
        EasyMock.expect(controller.removeDataGroupInfo(EasyMock.isA(DataGroupInfo.class), EasyMock.isA(Object.class)))
                .andAnswer(() -> removeAnswer(rootGroups));

        return controller;
    }

    /**
     * Creates and easy mocked {@link LayerActivationListener}.
     *
     * @param support Used to create the mock.
     * @return The {@link LayerActivationListener}.
     */
    private LayerActivationListener createNewLayerListener(EasyMockSupport support)
    {
        LayerActivationListener listener = support.createMock(LayerActivationListener.class);

        return listener;
    }

    /**
     * Creates the easy mocked order manager.
     *
     * @param support Used to create the mock.
     * @param layers The test layers.
     * @return The mocked {@link OrderManager}.
     */
    private OrderManager createOrderManager(EasyMockSupport support, List<GeoPackageLayer> layers)
    {
        OrderManager orderManager = support.createMock(OrderManager.class);

        for (GeoPackageLayer layer : layers)
        {
            if (layer.getLayerType() == LayerType.TILE)
            {
                DefaultOrderParticipantKey orderKey = new DefaultOrderParticipantKey(
                        DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY, DefaultOrderCategory.IMAGE_OVERLAY_CATEGORY,
                        layer.getPackageFile() + layer.getName());
                if ("tilePackage1".equals(layer.getName()))
                {
                    orderKey = new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_ELEVATION_FAMILY,
                            DefaultOrderCategory.EARTH_ELEVATION_CATEGORY, layer.getPackageFile() + layer.getName());
                    EasyMock.expect(orderManager.activateParticipant(EasyMock.eq(orderKey))).andReturn(10);
                }
                else
                {
                    EasyMock.expect(orderManager.activateParticipant(EasyMock.eq(orderKey))).andReturn(10);
                }
            }
            else
            {
                DefaultOrderParticipantKey orderKey = new DefaultOrderParticipantKey(
                        DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY, DefaultOrderCategory.FEATURE_CATEGORY,
                        layer.getPackageFile() + layer.getName());
                EasyMock.expect(orderManager.activateParticipant(EasyMock.eq(orderKey))).andReturn(10);
            }
        }

        return orderManager;
    }

    /**
     * Creates a mocked preferences registry.
     *
     * @param support Used to create the mock.
     * @param imports The set to add and remove to when add and removed are
     *            called on the mock.
     * @return The mocked {@link PreferencesRegistry}.
     */
    private PreferencesRegistry createPrefsRegistry(EasyMockSupport support, Set<String> imports)
    {
        Preferences prefs = support.createMock(Preferences.class);
        EasyMock.expect(prefs.getStringSet(EasyMock.cmpEq(GeoPackageDataGroupController.ourImportsKey), EasyMock.eq(New.set())))
                .andReturn(imports).anyTimes();
        EasyMock.expect(prefs.addElementToSet(EasyMock.cmpEq(GeoPackageDataGroupController.ourImportsKey),
                EasyMock.isA(String.class), EasyMock.isA(GeoPackageDataGroupController.class))).andAnswer(() ->
                {
                    return imports.add(EasyMock.getCurrentArguments()[1].toString());
                }).anyTimes();
        EasyMock.expect(prefs.removeElementFromSet(EasyMock.cmpEq(GeoPackageDataGroupController.ourImportsKey),
                EasyMock.isA(String.class), EasyMock.isA(GeoPackageDataGroupController.class))).andAnswer(() ->
                {
                    return imports.remove(EasyMock.getCurrentArguments()[1].toString());
                }).anyTimes();

        PreferencesRegistry registry = support.createMock(PreferencesRegistry.class);

        EasyMock.expect(registry.getPreferences(EasyMock.eq(GeoPackageDataGroupController.class))).andReturn(prefs).anyTimes();

        return registry;
    }

    /**
     * Creates an easy mocked {@link DataRegistry}.
     *
     * @param support Used to create the mock.
     * @param layers The layer to return in a query.
     * @return The mocked data registry.
     */
    private DataRegistry createRegistry(EasyMockSupport support, List<GeoPackageLayer> layers)
    {
        DataRegistry registry = support.createMock(DataRegistry.class);

        registry.addChangeListener(EasyMock.isA(GeoPackageDataGroupController.class),
                EasyMock.eq(new DataModelCategory(null, null, GeoPackageLayer.class.getName())),
                EasyMock.eq(GeoPackagePropertyDescriptors.GEOPACKAGE_LAYER_PROPERTY_DESCRIPTOR));
        EasyMock.expect(registry.performLocalQuery(EasyMock.isA(SimpleQuery.class))).andAnswer(() -> queryAnswer(layers));
        registry.removeChangeListener(EasyMock.isA(GeoPackageDataGroupController.class));

        return registry;
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @param registry The data registry to return.
     * @param controller The {@link DataGroupController} for the mock to return.
     * @param prefsRegistry The mocked {@link PreferencesRegistry} to return.
     * @param orderManager The mocked {@link OrderManager} to return.
     * @return The mocked toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support, DataRegistry registry, DataGroupController controller,
            PreferencesRegistry prefsRegistry, OrderManager orderManager)
    {
        Toolbox toolbox = support.createMock(Toolbox.class);

        EasyMock.expect(toolbox.getDataRegistry()).andReturn(registry);

        DataTypeInfoPreferenceAssistant assistant = support.createMock(DataTypeInfoPreferenceAssistant.class);
        EasyMock.expect(assistant.isVisiblePreference(EasyMock.isA(String.class))).andReturn(true).anyTimes();
        EasyMock.expect(assistant.getColorPreference(EasyMock.isA(String.class), EasyMock.anyInt())).andReturn(0).anyTimes();
        EasyMock.expect(assistant.getOpacityPreference(EasyMock.isA(String.class), EasyMock.anyInt())).andReturn(50).anyTimes();

        MantleToolbox mantleToolbox = support.createMock(MantleToolbox.class);
        EasyMock.expect(mantleToolbox.getDataTypeInfoPreferenceAssistant()).andReturn(assistant).anyTimes();
        EasyMock.expect(mantleToolbox.getDataGroupController()).andReturn(controller);
        DataTypeController typeController = createController(support);
        EasyMock.expect(mantleToolbox.getDataTypeController()).andReturn(typeController);

        PluginToolboxRegistry pluginToolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(pluginToolboxRegistry.getPluginToolbox(EasyMock.eq(MantleToolbox.class))).andReturn(mantleToolbox)
                .anyTimes();

        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(pluginToolboxRegistry).anyTimes();

        EventManager eventManager = support.createMock(EventManager.class);
        eventManager.publishEvent(EasyMock.isA(Event.class));
        EasyMock.expectLastCall().andAnswer(this::publishEventAnswer).anyTimes();
        eventManager.subscribe(EasyMock.eq(DataTypeVisibilityChangeEvent.class), EasyMock.notNull());
        eventManager.unsubscribe(EasyMock.eq(DataTypeVisibilityChangeEvent.class), EasyMock.notNull());
        eventManager.subscribe(EasyMock.eq(DataTypeInfoColorChangeEvent.class), EasyMock.notNull());
        eventManager.unsubscribe(EasyMock.eq(DataTypeInfoColorChangeEvent.class), EasyMock.notNull());
        eventManager.subscribe(EasyMock.eq(DataTypeRemovedEvent.class), EasyMock.notNull());
        eventManager.unsubscribe(EasyMock.eq(DataTypeRemovedEvent.class), EasyMock.notNull());

        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager).anyTimes();

        EasyMock.expect(toolbox.getPreferencesRegistry()).andReturn(prefsRegistry).anyTimes();

        OrderManagerRegistry orderRegistry = support.createMock(OrderManagerRegistry.class);
        EasyMock.expect(orderRegistry.getOrderManager(EasyMock.eq(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY),
                EasyMock.eq(DefaultOrderCategory.IMAGE_OVERLAY_CATEGORY))).andReturn(orderManager);
        EasyMock.expect(toolbox.getOrderManagerRegistry()).andReturn(orderRegistry);
        EasyMock.expect(orderRegistry.getOrderManager(EasyMock.eq(DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY),
                EasyMock.eq(DefaultOrderCategory.FEATURE_CATEGORY))).andReturn(orderManager);
        EasyMock.expect(toolbox.getOrderManagerRegistry()).andReturn(orderRegistry);
        EasyMock.expect(orderRegistry.getOrderManager(EasyMock.eq(DefaultOrderCategory.DEFAULT_ELEVATION_FAMILY),
                EasyMock.eq(DefaultOrderCategory.EARTH_ELEVATION_CATEGORY))).andReturn(orderManager);
        EasyMock.expect(toolbox.getOrderManagerRegistry()).andReturn(orderRegistry);

        MapManager mapManager = support.createMock(MapManager.class);
        EasyMock.expect(toolbox.getMapManager()).andReturn(mapManager);

        return toolbox;
    }

    /**
     * Ensures that when groups are added they have members.
     *
     * @return Null.
     */
    private Void publishEventAnswer()
    {
        Event event = (Event)EasyMock.getCurrentArguments()[0];
        if (event instanceof DataGroupInfoChildAddedEvent)
        {
            DataGroupInfoChildAddedEvent childAdded = (DataGroupInfoChildAddedEvent)event;
            if (!(childAdded.getAdded() instanceof DefaultDeletableDataGroupInfo))
            {
                assertTrue(childAdded.getAdded().hasMembers(false));
            }
        }
        return null;
    }

    /**
     * The answer to a query call on the mocked {@link DataRegistry}.
     *
     * @param layers The layers to return.
     * @return The model ids.
     */
    private long[] queryAnswer(List<GeoPackageLayer> layers)
    {
        @SuppressWarnings("unchecked")
        SimpleQuery<GeoPackageLayer> query = (SimpleQuery<GeoPackageLayer>)EasyMock.getCurrentArguments()[0];

        if (query.getDataModelCategory().equals(new DataModelCategory(null, null, GeoPackageLayer.class.getName()))
                && !layers.isEmpty())
        {
            @SuppressWarnings("unchecked")
            PropertyValueReceiver<GeoPackageLayer> receiver = (PropertyValueReceiver<GeoPackageLayer>)query
                    .getPropertyValueReceivers().get(0);
            if (GeoPackagePropertyDescriptors.GEOPACKAGE_LAYER_PROPERTY_DESCRIPTOR.equals(receiver.getPropertyDescriptor()))
            {
                receiver.receive(layers);
            }
        }

        long[] ids = new long[layers.size()];
        for (int i = 0; i < layers.size(); i++)
        {
            ids[i] = i;
        }

        return ids;
    }

    /**
     * Remove the group from the specified list.
     *
     * @param rootGroups The list to remove the data group from.
     * @return The removal success.
     */
    private boolean removeAnswer(List<DataGroupInfo> rootGroups)
    {
        DataGroupInfo group = (DataGroupInfo)EasyMock.getCurrentArguments()[0];

        return rootGroups.remove(group);
    }
}
