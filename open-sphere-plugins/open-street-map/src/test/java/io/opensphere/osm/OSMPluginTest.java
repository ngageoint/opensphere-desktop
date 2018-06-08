package io.opensphere.osm;

import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.osm.envoy.OSMTileEnvoy;
import io.opensphere.osm.server.OSMServerSourceController;
import io.opensphere.server.toolbox.ServerSourceControllerManager;
import io.opensphere.server.toolbox.ServerToolbox;

/**
 * Unit test for the {@link OSMPlugin}.
 */
public class OSMPluginTest
{
    /**
     * Tests the plugin.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support);

        support.replayAll();

        OSMPlugin plugin = new OSMPlugin();

        plugin.initialize(null, toolbox);
        assertTrue(plugin.getEnvoys().iterator().next() instanceof OSMTileEnvoy);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked toolbox.
     *
     * @param support Used to create the mock.
     * @return The mocked toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support)
    {
        ServerSourceControllerManager controllerManager = support.createMock(ServerSourceControllerManager.class);
        controllerManager.setPreferencesTopic(EasyMock.eq(OSMServerSourceController.class), EasyMock.eq(OSMPlugin.class));

        ServerToolbox serverBox = support.createMock(ServerToolbox.class);
        EasyMock.expect(serverBox.getServerSourceControllerManager()).andReturn(controllerManager);

        PluginToolboxRegistry toolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(EasyMock.eq(ServerToolbox.class))).andReturn(serverBox);

        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(toolboxRegistry).anyTimes();

        return toolbox;
    }
}
