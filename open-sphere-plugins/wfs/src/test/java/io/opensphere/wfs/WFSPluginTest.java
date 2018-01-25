package io.opensphere.wfs;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.easymock.EasyMock;
import org.junit.Test;

import io.opensphere.core.AnimationManager;
import io.opensphere.core.MapManager;
import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.TimeManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.metrics.MetricsRegistry;
import io.opensphere.core.modulestate.ModuleStateManager;
import io.opensphere.core.options.OptionsRegistry;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.impl.OrderManagerRegistryImpl;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.ObservableList;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.plugin.queryregion.QueryRegionManager;
import io.opensphere.server.toolbox.ServerToolbox;
import io.opensphere.server.toolbox.ServerValidatorRegistry;
import io.opensphere.wfs.envoy.WFSToolbox;
import io.opensphere.wfs.util.WFSConstants;

/**
 * Test {@link WFSPlugin}.
 */
public class WFSPluginTest
{
    /**
     * Perform some tests on the plug-in. Test that:
     *
     * <ul>
     * <li>The envoys are created and configured correctly.</li>
     * <li>The transformers are created and configured correctly.</li>
     * </ul>
     */
    @Test
    public void testPlugin()
    {
        WFSPlugin plugin = new WFSPlugin();
        PluginLoaderData data = new PluginLoaderData();

        OptionsRegistry optReg = EasyMock.createNiceMock(OptionsRegistry.class);
        EasyMock.expect(optReg.getRootProviderByTopic("OGC Server")).andReturn(null).anyTimes();
        EasyMock.replay(optReg);

        UIRegistry uiRegistry = EasyMock.createNiceMock(UIRegistry.class);
        EasyMock.expect(uiRegistry.getMenuBarRegistry()).andReturn(EasyMock.createNiceMock(MenuBarRegistry.class)).anyTimes();
        EasyMock.expect(uiRegistry.getOptionsRegistry()).andReturn(optReg).anyTimes();
        EasyMock.replay(uiRegistry);

        Preferences prefs = EasyMock.createNiceMock(Preferences.class);
        prefs.getInt(EasyMock.eq(WFSConstants.MAX_FEATURES_PREFERENCE), EasyMock.anyInt());
        EasyMock.expectLastCall().andReturn(Integer.valueOf(0));
        EasyMock.replay(prefs);

        PreferencesRegistry prefsRegistry = EasyMock.createNiceMock(PreferencesRegistry.class);
        EasyMock.expect(prefsRegistry.getPreferences(WFSPlugin.class)).andReturn(prefs).anyTimes();
        EasyMock.replay(prefsRegistry);

        ModuleStateManager msm = EasyMock.createNiceMock(ModuleStateManager.class);

        Toolbox toolbox = EasyMock.createNiceMock(Toolbox.class);
        EasyMock.expect(toolbox.getDataRegistry()).andReturn(EasyMock.createNiceMock(DataRegistry.class)).anyTimes();
        EasyMock.expect(toolbox.getMetricsRegistry()).andReturn(EasyMock.createNiceMock(MetricsRegistry.class)).anyTimes();
        EasyMock.expect(toolbox.getEventManager()).andReturn(EasyMock.createNiceMock(EventManager.class)).anyTimes();
        EasyMock.expect(toolbox.getMapManager()).andReturn(EasyMock.createNiceMock(MapManager.class)).anyTimes();
        EasyMock.expect(toolbox.getAnimationManager()).andReturn(EasyMock.createNiceMock(AnimationManager.class)).anyTimes();

        TimeManager timeManager = EasyMock.createMock(TimeManager.class);
        EasyMock.expect(timeManager.getLoadTimeSpans()).andReturn(new ObservableList<>());
        EasyMock.expect(toolbox.getTimeManager()).andReturn(timeManager).anyTimes();
        EasyMock.expect(toolbox.getPreferencesRegistry()).andReturn(prefsRegistry).anyTimes();
        EasyMock.expect(toolbox.getUIRegistry()).andReturn(uiRegistry).anyTimes();
        EasyMock.expect(toolbox.getModuleStateManager()).andReturn(msm);

        PluginToolboxRegistry toolboxRegistry = EasyMock.createNiceMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(toolboxRegistry).anyTimes();

        MantleToolbox mantleToolbox = EasyMock.createNiceMock(MantleToolbox.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(EasyMock.eq(MantleToolbox.class))).andReturn(mantleToolbox).anyTimes();

        ServerToolbox serverToolbox = EasyMock.createNiceMock(ServerToolbox.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(EasyMock.eq(ServerToolbox.class))).andReturn(serverToolbox).anyTimes();

        WFSToolbox wfsToolbox = new WFSToolbox();
        EasyMock.expect(toolboxRegistry.getPluginToolbox(WFSToolbox.class)).andReturn(wfsToolbox).anyTimes();

        ServerValidatorRegistry serverValidatorRegistry = EasyMock.createNiceMock(ServerValidatorRegistry.class);
        EasyMock.expect(serverToolbox.getServerValidatorRegistry()).andReturn(serverValidatorRegistry).anyTimes();

        QueryRegionManager qrm = EasyMock.createNiceMock(QueryRegionManager.class);
        EasyMock.expect(mantleToolbox.getQueryRegionManager()).andReturn(qrm).anyTimes();
        DataGroupController dataGroupController = EasyMock.createNiceMock(DataGroupController.class);
        EasyMock.expect(mantleToolbox.getDataGroupController()).andReturn(dataGroupController).anyTimes();
        EasyMock.replay(dataGroupController);
        EasyMock.replay(toolboxRegistry);
        EasyMock.replay(mantleToolbox);
        EasyMock.replay(serverToolbox);
        EasyMock.replay(qrm);

        OrderManagerRegistry omReg = new OrderManagerRegistryImpl(null);
        EasyMock.expect(toolbox.getOrderManagerRegistry()).andReturn(omReg).anyTimes();
        EasyMock.replay(toolbox, timeManager);

        plugin.initialize(data, toolbox);

        Collection<? extends Transformer> transformers = plugin.getTransformers();
        assertEquals(1, transformers.size());
    }
}
