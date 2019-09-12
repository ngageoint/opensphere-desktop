package io.opensphere.featureactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.modulestate.ModuleStateManager;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesImpl;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.ref.WeakReference;
import io.opensphere.featureactions.controller.FeatureActionsController;
import io.opensphere.featureactions.editor.ui.FeatureActionsMenuProvider;
import io.opensphere.featureactions.registry.FeatureActionsRegistry;
import io.opensphere.featureactions.toolbox.FeatureActionsToolbox;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;
import io.opensphere.mantle.data.geom.style.dialog.StyleManagerController;

/**
 * Unit test for {@link FeatureActionsPlugin}.
 */
public class FeatureActionsPluginTest
{
    /**
     * A weak reference to the context menu provider.
     */
    private WeakReference<ContextMenuProvider<DataGroupContextKey>> myListener;

    /**
     * Tests the getServices call.
     */
    @Test
    public void testGetServices()
    {
        EasyMockSupport support = new EasyMockSupport();

        Service pluginService = support.createMock(Service.class);
        Toolbox toolbox = createToolbox(support, pluginService);

        support.replayAll();

        FeatureActionsPlugin plugin = new FeatureActionsPlugin();
        Collection<Service> services = plugin.getServices(null, toolbox);

        assertEquals(2, services.size());
        Iterator<Service> iterator = services.iterator();

        assertEquals(pluginService, iterator.next());
        assertTrue(iterator.next() instanceof FeatureActionsController);

        System.gc();

        assertNotNull(myListener.get());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the toolbox.
     * @param registrationService The service to return for plugin toolbox
     *            registration.
     * @return The mocked toolbox.
     */
    @SuppressWarnings("unchecked")
    private Toolbox createToolbox(EasyMockSupport support, Service registrationService)
    {
        Preferences stylePrefs = support.createNiceMock(Preferences.class);
        PreferencesImpl registryPrefs = new PreferencesImpl(FeatureActionsRegistry.class.getName());

        PreferencesRegistry prefsRegistry = support.createMock(PreferencesRegistry.class);
        EasyMock.expect(prefsRegistry.getPreferences(FeatureActionsRegistry.class)).andReturn(registryPrefs);
        EasyMock.expect(prefsRegistry.getPreferences(StyleManagerController.class)).andReturn(stylePrefs);

        EventManager eventManager = support.createMock(EventManager.class);

        ContextActionManager contextManager = support.createMock(ContextActionManager.class);
        contextManager.registerContextMenuItemProvider(EasyMock.eq(DataGroupInfo.ACTIVE_DATA_CONTEXT),
                EasyMock.eq(DataGroupContextKey.class), EasyMock.isA(FeatureActionsMenuProvider.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myListener = new WeakReference<>((ContextMenuProvider<DataGroupContextKey>)EasyMock.getCurrentArguments()[2]);
            return null;
        });
        UIRegistry uiRegistry = support.createMock(UIRegistry.class);
        EasyMock.expect(uiRegistry.getContextActionManager()).andReturn(contextManager).atLeastOnce();
        contextManager.registerContextMenuItemProvider(EasyMock.eq(ContextIdentifiers.DELETE_CONTEXT),
                EasyMock.eq(Void.class), EasyMock.isA(ContextMenuProvider.class));
        EasyMock.expectLastCall().atLeastOnce();

        MantleToolbox mantle = support.createMock(MantleToolbox.class);

        PluginToolboxRegistry toolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolboxRegistry.getRegistrationService(EasyMock.isA(FeatureActionsToolbox.class)))
                .andReturn(registrationService);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(MantleToolbox.class)).andReturn(mantle).atLeastOnce();
        DataGroupController dataGroupController = support.createMock(DataGroupController.class);
        EasyMock.expect(mantle.getDataGroupController()).andReturn(dataGroupController);
        ModuleStateManager stateManager = support.createNiceMock(ModuleStateManager.class);

        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getPreferencesRegistry()).andReturn(prefsRegistry).atLeastOnce();
        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager);
        EasyMock.expect(toolbox.getUIRegistry()).andReturn(uiRegistry).atLeastOnce();
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(toolboxRegistry).atLeastOnce();
        EasyMock.expect(toolbox.getModuleStateManager()).andReturn(stateManager);

        return toolbox;
    }
}
