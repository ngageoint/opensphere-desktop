package io.opensphere.merge;

import static org.junit.Assert.assertNotNull;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.sun.javafx.application.PlatformImpl;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.datafilter.DataFilterRegistry;
import io.opensphere.core.datafilter.columns.ColumnMappingController;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.ref.WeakReference;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;
import io.opensphere.mantle.data.DataGroupInfo.MultiDataGroupContextKey;
import io.opensphere.mantle.data.cache.DataElementCache;
import io.opensphere.merge.controller.MergeController;
import io.opensphere.merge.ui.MergeContextMenuProvider;
import io.opensphere.merge.ui.MergeContextSingleSelectionMenuProvider;

/**
 * Unit test for {@link MergePlugin}.
 */
public class MergePluginTestDisplay
{
    /**
     * Mimics the weak reference to the context provider, ensures plugin does it correctly.
     */
    private WeakReference<MergeContextMenuProvider> myMenuProvider;

    /**
     * Mimics the weak reference toe the context provider, ensures plugin does it correctly.
     */
    private WeakReference<MergeContextSingleSelectionMenuProvider> mySingleMenuProvider;

    /**
     * Unit test.
     */
    @Test
    public void test()
    {
        PlatformImpl.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support);

        support.replayAll();

        MergePlugin plugin = new MergePlugin();
        plugin.initialize(null, toolbox);

        System.gc();

        assertNotNull(myMenuProvider.get());
        assertNotNull(mySingleMenuProvider.get());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked toolbox.
     *
     * @param support Used to create the mock.
     * @return The system toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support)
    {
        ContextActionManager manager = support.createMock(ContextActionManager.class);
        manager.registerContextMenuItemProvider(EasyMock.cmpEq(DataGroupInfo.ACTIVE_DATA_CONTEXT),
                EasyMock.eq(MultiDataGroupContextKey.class), EasyMock.isA(MergeContextMenuProvider.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myMenuProvider = new WeakReference<>((MergeContextMenuProvider)EasyMock.getCurrentArguments()[2]);
            return null;
        });

        manager.registerContextMenuItemProvider(EasyMock.cmpEq(DataGroupInfo.ACTIVE_DATA_CONTEXT),
                EasyMock.eq(DataGroupContextKey.class), EasyMock.isA(MergeContextSingleSelectionMenuProvider.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            mySingleMenuProvider = new WeakReference<>(
                    (MergeContextSingleSelectionMenuProvider)EasyMock.getCurrentArguments()[2]);
            return null;
        });

        UIRegistry uiRegistry = support.createMock(UIRegistry.class);
        EasyMock.expect(uiRegistry.getContextActionManager()).andReturn(manager).atLeastOnce();

        Toolbox toolbox = support.createNiceMock(Toolbox.class);
        EasyMock.expect(toolbox.getUIRegistry()).andReturn(uiRegistry).atLeastOnce();

        DataGroupController groupController = support.createNiceMock(DataGroupController.class);

        EasyMock.expect(Boolean.valueOf(
                groupController.addRootDataGroupInfo(EasyMock.isA(DataGroupInfo.class), EasyMock.isA(MergeController.class))))
                .andReturn(Boolean.TRUE);

        DataElementCache cache = support.createNiceMock(DataElementCache.class);
        MantleToolbox mantle = support.createNiceMock(MantleToolbox.class);
        EasyMock.expect(mantle.getDataElementCache()).andReturn(cache);
        EasyMock.expect(mantle.getDataGroupController()).andReturn(groupController).atLeastOnce();

        PluginToolboxRegistry toolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(EasyMock.eq(MantleToolbox.class))).andReturn(mantle).atLeastOnce();

        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(toolboxRegistry).atLeastOnce();

        EventManager eventManager = support.createNiceMock(EventManager.class);
        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager).anyTimes();

        ColumnMappingController mapper = support.createMock(ColumnMappingController.class);

        DataFilterRegistry filterRegistry = support.createMock(DataFilterRegistry.class);
        EasyMock.expect(filterRegistry.getColumnMappingController()).andReturn(mapper).atLeastOnce();
        EasyMock.expect(toolbox.getDataFilterRegistry()).andReturn(filterRegistry).atLeastOnce();

        DataRegistry dataRegistry = support.createMock(DataRegistry.class);
        EasyMock.expect(toolbox.getDataRegistry()).andReturn(dataRegistry);

        PreferencesRegistry prefsRegistry = support.createNiceMock(PreferencesRegistry.class);
        EasyMock.expect(toolbox.getPreferencesRegistry()).andReturn(prefsRegistry);

        return toolbox;
    }
}
