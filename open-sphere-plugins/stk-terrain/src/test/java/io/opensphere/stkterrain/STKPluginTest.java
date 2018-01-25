package io.opensphere.stkterrain;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.event.EventManager;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.server.toolbox.ServerSourceControllerManager;
import io.opensphere.server.toolbox.ServerToolbox;
import io.opensphere.stkterrain.server.STKServerSourceController;

/**
 * Unit test for {@link STKPlugin}.
 */
public class STKPluginTest
{
    /**
     * Tests initializing the plugin.
     */
    @Test
    public void testInitialize()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support);
        PluginLoaderData loaderData = new PluginLoaderData();

        support.replayAll();

        STKPlugin plugin = new STKPlugin();
        plugin.initialize(loaderData, toolbox);

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
        DataRegistry dataRegistry = support.createNiceMock(DataRegistry.class);

        ServerSourceControllerManager sourceManager = support.createMock(ServerSourceControllerManager.class);
        sourceManager.setPreferencesTopic(EasyMock.eq(STKServerSourceController.class), EasyMock.eq(STKPlugin.class));

        DataGroupController groupController = support.createMock(DataGroupController.class);

        MantleToolbox mantleToolbox = support.createMock(MantleToolbox.class);
        EasyMock.expect(mantleToolbox.getDataGroupController()).andReturn(groupController);

        ServerToolbox serverBox = support.createMock(ServerToolbox.class);
        EasyMock.expect(serverBox.getServerSourceControllerManager()).andReturn(sourceManager);

        PluginToolboxRegistry pluginToolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(pluginToolboxRegistry.getPluginToolbox(EasyMock.eq(ServerToolbox.class))).andReturn(serverBox);
        EasyMock.expect(pluginToolboxRegistry.getPluginToolbox(EasyMock.eq(MantleToolbox.class))).andReturn(mantleToolbox);

        EventManager eventManager = support.createNiceMock(EventManager.class);

        Toolbox toolbox = support.createNiceMock(Toolbox.class);
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(pluginToolboxRegistry).atLeastOnce();
        EasyMock.expect(toolbox.getDataRegistry()).andReturn(dataRegistry).atLeastOnce();
        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager);

        return toolbox;
    }
}
