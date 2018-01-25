package io.opensphere.xyztile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.order.OrderChangeListener;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.xyztile.mantle.XYZMantleController;
import io.opensphere.xyztile.model.XYZSettings;
import io.opensphere.xyztile.transformer.XYZLayerTransformer;
import io.opensphere.xyztile.util.XYZTileUtils;

/**
 * Unit test {@link XYZTilePlugin}.
 */
public class XYZTilePluginTest
{
    /**
     * Unit test.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support);

        support.replayAll();

        XYZTilePlugin plugin = new XYZTilePlugin();
        plugin.initialize(null, toolbox);

        Collection<? extends Transformer> transformers = plugin.getTransformers();

        assertEquals(1, transformers.size());
        assertTrue(transformers.iterator().next() instanceof XYZLayerTransformer);

        plugin.close();

        support.verifyAll();
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
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @return The mocked toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support)
    {
        Toolbox toolbox = support.createMock(Toolbox.class);

        DataRegistry dataRegistry = createDataRegistry(support);

        EasyMock.expect(toolbox.getDataRegistry()).andReturn(dataRegistry).atLeastOnce();

        DataGroupController groupController = support.createMock(DataGroupController.class);

        MantleToolbox mantleToolbox = support.createMock(MantleToolbox.class);
        EasyMock.expect(mantleToolbox.getDataGroupController()).andReturn(groupController).atLeastOnce();

        PluginToolboxRegistry toolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(EasyMock.eq(MantleToolbox.class))).andReturn(mantleToolbox)
                .atLeastOnce();
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(toolboxRegistry).atLeastOnce();

        EventManager eventManager = support.createNiceMock(EventManager.class);
        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager).atLeastOnce();

        UIRegistry uiRegistry = support.createMock(UIRegistry.class);
        EasyMock.expect(toolbox.getUIRegistry()).andReturn(uiRegistry);

        OrderManager orderManager = support.createMock(OrderManager.class);
        orderManager.addParticipantChangeListener(EasyMock.isA(OrderChangeListener.class));
        orderManager.removeParticipantChangeListener(EasyMock.isA(OrderChangeListener.class));

        OrderManager dataOrderManager = support.createMock(OrderManager.class);
        dataOrderManager.addParticipantChangeListener(EasyMock.isA(OrderChangeListener.class));
        dataOrderManager.removeParticipantChangeListener(EasyMock.isA(OrderChangeListener.class));

        OrderManagerRegistry orderRegistry = support.createMock(OrderManagerRegistry.class);
        EasyMock.expect(orderRegistry.getOrderManager(EasyMock.cmpEq(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY),
                EasyMock.eq(DefaultOrderCategory.IMAGE_BASE_MAP_CATEGORY))).andReturn(orderManager);
        EasyMock.expect(orderRegistry.getOrderManager(EasyMock.cmpEq(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY),
                EasyMock.eq(DefaultOrderCategory.IMAGE_DATA_CATEGORY))).andReturn(dataOrderManager);
        EasyMock.expect(toolbox.getOrderManagerRegistry()).andReturn(orderRegistry).times(2);

        Preferences prefs = support.createMock(Preferences.class);
        PreferencesRegistry prefsRegistry = support.createMock(PreferencesRegistry.class);
        EasyMock.expect(prefsRegistry.getPreferences(EasyMock.eq(XYZSettings.class))).andReturn(prefs);
        EasyMock.expect(toolbox.getPreferencesRegistry()).andReturn(prefsRegistry);

        return toolbox;
    }
}
