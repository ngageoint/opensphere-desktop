package io.opensphere.geopackage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.MapManager;
import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.DefaultQuery;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.event.Event;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.importer.ImporterRegistry;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.registry.GenericRegistry;
import io.opensphere.geopackage.importer.GeoPackageImporter;
import io.opensphere.geopackage.mantle.GeoPackageDataGroupController;
import io.opensphere.geopackage.model.GeoPackageLayer;
import io.opensphere.geopackage.model.GeoPackagePropertyDescriptors;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.controller.event.impl.DataTypeRemovedEvent;
import io.opensphere.mantle.data.DataTypeInfoPreferenceAssistant;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;

/**
 * Tests the {@link GeoPackagePlugin}.
 */
@SuppressWarnings("boxing")
public class GeoPackagePluginTest
{
    /**
     * Tests initializing the plugin and closing it.
     *
     * @throws IOException Bad IO.
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void test() throws IOException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        File testFile = File.createTempFile("test", ".gpkg");
        testFile.deleteOnExit();

        CountDownLatch latch = new CountDownLatch(1);

        DataRegistry dataRegistry = createDataRegistry(support, latch, testFile);
        PreferencesRegistry prefsRegistry = createPreferencesRegistry(support, testFile);

        Toolbox toolbox = createToolbox(support, dataRegistry, prefsRegistry);

        support.replayAll();

        GeoPackagePlugin plugin = new GeoPackagePlugin();
        plugin.initialize(null, toolbox);

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        plugin.close();

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link DataRegistry}.
     *
     * @param support Used to create the mock.
     * @param latch Latch to countdown so we can wait for multithread call.
     * @param existingFile The existing geopackage file.
     * @return The mocked {@link DataRegistry}.
     */
    private DataRegistry createDataRegistry(EasyMockSupport support, CountDownLatch latch, File existingFile)
    {
        DataRegistry registry = support.createMock(DataRegistry.class);

        registry.addChangeListener(EasyMock.isA(GeoPackageDataGroupController.class),
                EasyMock.eq(new DataModelCategory(null, null, GeoPackageLayer.class.getName())),
                EasyMock.eq(GeoPackagePropertyDescriptors.GEOPACKAGE_LAYER_PROPERTY_DESCRIPTOR));
        EasyMock.expect(registry.performLocalQuery(EasyMock.isA(SimpleQuery.class))).andReturn(new long[0]);
        EasyMock.expect(registry.performLocalQuery(EasyMock.isA(DefaultQuery.class))).andAnswer(() ->
        {
            DefaultQuery query = (DefaultQuery)EasyMock.getCurrentArguments()[0];
            assertEquals(new DataModelCategory(existingFile.toString(), null, null), query.getDataModelCategory());
            latch.countDown();
            return new long[] { 1 };
        });
        registry.removeChangeListener(EasyMock.isA(GeoPackageDataGroupController.class));

        return registry;
    }

    /**
     * Creates an easy mocked {@link PreferencesRegistry}.
     *
     * @param support Used to create the mock.
     * @param existingFile The existing geopackage file to return as an already
     *            imported file.
     * @return The mocked {@link PreferencesRegistry}.
     */
    private PreferencesRegistry createPreferencesRegistry(EasyMockSupport support, File existingFile)
    {
        Preferences prefs = support.createMock(Preferences.class);
        EasyMock.expect(prefs.getStringSet(EasyMock.eq("imports"), EasyMock.eq(New.set())))
                .andReturn(New.set(existingFile.toString()));

        PreferencesRegistry registry = support.createMock(PreferencesRegistry.class);
        EasyMock.expect(registry.getPreferences(EasyMock.eq(GeoPackageDataGroupController.class))).andReturn(prefs);

        return registry;
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @param registry The data registry to return.
     * @param prefsRegistry The mocked {@link PreferencesRegistry} to return.
     * @return The mocked toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support, DataRegistry registry, PreferencesRegistry prefsRegistry)
    {
        Toolbox toolbox = support.createMock(Toolbox.class);

        EasyMock.expect(toolbox.getDataRegistry()).andReturn(registry).atLeastOnce();

        DataTypeInfoPreferenceAssistant assistant = support.createMock(DataTypeInfoPreferenceAssistant.class);
        EasyMock.expect(assistant.isVisiblePreference(EasyMock.isA(String.class))).andReturn(true).anyTimes();
        EasyMock.expect(assistant.getColorPreference(EasyMock.isA(String.class), EasyMock.anyInt())).andReturn(0).anyTimes();
        EasyMock.expect(assistant.getOpacityPreference(EasyMock.isA(String.class), EasyMock.anyInt())).andReturn(50).anyTimes();

        DataGroupController controller = support.createMock(DataGroupController.class);
        EasyMock.expect(controller.addRootDataGroupInfo(EasyMock.isA(DefaultDataGroupInfo.class),
                EasyMock.isA(GeoPackageDataGroupController.class))).andReturn(true);
        EasyMock.expect(controller.removeDataGroupInfo(EasyMock.isA(DefaultDataGroupInfo.class),
                EasyMock.isA(GeoPackageDataGroupController.class))).andReturn(true);

        MantleToolbox mantleToolbox = support.createMock(MantleToolbox.class);
        EasyMock.expect(mantleToolbox.getDataTypeInfoPreferenceAssistant()).andReturn(assistant).anyTimes();
        EasyMock.expect(mantleToolbox.getDataGroupController()).andReturn(controller);

        DataTypeController typeController = support.createNiceMock(DataTypeController.class);
        EasyMock.expect(mantleToolbox.getDataTypeController()).andReturn(typeController);

        PluginToolboxRegistry pluginToolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(pluginToolboxRegistry.getPluginToolbox(EasyMock.eq(MantleToolbox.class))).andReturn(mantleToolbox)
                .anyTimes();

        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(pluginToolboxRegistry).anyTimes();

        EventManager eventManager = support.createMock(EventManager.class);
        eventManager.publishEvent(EasyMock.isA(Event.class));
        EasyMock.expectLastCall().anyTimes();
        eventManager.subscribe(EasyMock.eq(DataTypeVisibilityChangeEvent.class), EasyMock.notNull());
        eventManager.unsubscribe(EasyMock.eq(DataTypeVisibilityChangeEvent.class), EasyMock.notNull());
        eventManager.subscribe(EasyMock.eq(DataTypeInfoColorChangeEvent.class), EasyMock.notNull());
        eventManager.unsubscribe(EasyMock.eq(DataTypeInfoColorChangeEvent.class), EasyMock.notNull());
        eventManager.subscribe(EasyMock.eq(DataTypeRemovedEvent.class), EasyMock.notNull());
        eventManager.unsubscribe(EasyMock.eq(DataTypeRemovedEvent.class), EasyMock.notNull());

        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager).anyTimes();

        EasyMock.expect(toolbox.getPreferencesRegistry()).andReturn(prefsRegistry);

        UIRegistry uiRegistry = support.createMock(UIRegistry.class);
        EasyMock.expect(toolbox.getUIRegistry()).andReturn(uiRegistry).atLeastOnce();

        Service service = support.createMock(Service.class);
        service.open();
        service.close();

        ImporterRegistry importerRegistry = support.createMock(ImporterRegistry.class);
        EasyMock.expect(importerRegistry.createImporterService(EasyMock.isA(GeoPackageImporter.class))).andReturn(service);

        EasyMock.expect(toolbox.getImporterRegistry()).andReturn(importerRegistry);

        OrderManager orderManager = support.createNiceMock(OrderManager.class);
        OrderManagerRegistry orderRegistry = support.createMock(OrderManagerRegistry.class);
        EasyMock.expect(orderRegistry.getOrderManager(EasyMock.eq(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY),
                EasyMock.eq(DefaultOrderCategory.IMAGE_OVERLAY_CATEGORY))).andReturn(orderManager);
        EasyMock.expect(orderRegistry.getOrderManager(EasyMock.eq(DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY),
                EasyMock.eq(DefaultOrderCategory.FEATURE_CATEGORY))).andReturn(orderManager);
        EasyMock.expect(orderRegistry.getOrderManager(EasyMock.eq(DefaultOrderCategory.DEFAULT_ELEVATION_FAMILY),
                EasyMock.eq(DefaultOrderCategory.EARTH_ELEVATION_CATEGORY))).andReturn(orderManager);
        EasyMock.expect(toolbox.getOrderManagerRegistry()).andReturn(orderRegistry).times(3);

        MapManager mapManager = support.createMock(MapManager.class);
        EasyMock.expect(toolbox.getMapManager()).andReturn(mapManager);

        EasyMock.expect(toolbox.getEnvoyRegistry()).andReturn(new GenericRegistry<>());

        return toolbox;
    }
}
