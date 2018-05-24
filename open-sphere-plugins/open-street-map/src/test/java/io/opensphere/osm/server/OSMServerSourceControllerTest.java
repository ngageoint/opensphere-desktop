package io.opensphere.osm.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ServiceLoader;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Ignore;
import org.junit.Test;

import io.opensphere.core.NetworkConfigurationManager;
import io.opensphere.core.SystemToolbox;
import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.SimpleSessionOnlyCacheDeposit;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.mantle.datasources.impl.UrlDataSource;
import io.opensphere.mantle.datasources.impl.UrlSourceConfig;
import io.opensphere.osm.OSMPlugin;
import io.opensphere.osm.util.OSMUtil;
import io.opensphere.server.customization.ServerCustomization;
import io.opensphere.server.toolbox.ServerSourceController;
import io.opensphere.xyztile.model.Projection;
import io.opensphere.xyztile.model.XYZTileLayerInfo;
import io.opensphere.xyztile.util.XYZTileUtils;

/**
 * Unit test for {@link OSMServerSourceController}.
 */
public class OSMServerSourceControllerTest
{
    /**
     * The test server url.
     */
    private static final String ourServer = "http://osm.geointservices.io/osm_tiles_pc/{z}/{x}/{y}.png";

    /**
     * Tests creating a new source.
     */
    @Test
    public void testCreateNewSource()
    {
        OSMServerSourceController controller = new OSMServerSourceController();
        UrlDataSource source = (UrlDataSource)controller.createNewSource("New Server");

        assertEquals("Open Street Map", source.getName());
        assertEquals(ourServer, source.getURL());
    }

    /**
     * Tests the example url.
     */
    @Test
    public void testGetExampleUrl()
    {
        OSMServerSourceController controller = new OSMServerSourceController();

        assertEquals(ourServer, controller.getExampleUrl());
    }

    /**
     * Tests the server customization.
     */
    @Test
    public void testGetServerCustomization()
    {
        OSMServerSourceController controller = new OSMServerSourceController();
        ServerCustomization customization = controller.getServerCustomization();

        assertEquals("Open Street Map Server", customization.getServerType());
    }

    /**
     * Tests get validator.
     */
    @Test
    public void testGetValidator()
    {
        OSMServerSourceController controller = new OSMServerSourceController();

        assertTrue(controller.getValidator(null) instanceof OSMServerSourceValidator);
    }

    /**
     * Tests handling server activating.
     */
    @Ignore("Ignore while new developer learns JUnit and EasyMock")
    @Test
    public void testHandleActivateSource()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry registry = createDataRegistry(support);
        Toolbox toolbox = createToolbox(support, registry);

        support.replayAll();

        OSMServerSourceController controller = new OSMServerSourceController();
        controller.open(toolbox, OSMPlugin.class);
        UrlDataSource source = new UrlDataSource("Open Street Map", ourServer);
        controller.handleActivateSource(source);

        support.verifyAll();
    }

    /**
     * Tests handling server deactivation.
     */
    @Test
    public void testHandleDeactivateSource()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry registry = support.createMock(DataRegistry.class);
        EasyMock.expect(registry.removeModels(EasyMock.eq(XYZTileUtils.newLayersCategory(ourServer, OSMUtil.PROVIDER)),
                EasyMock.eq(false))).andReturn(new long[] {});
        Toolbox toolbox = createToolbox(support, registry);

        support.replayAll();

        OSMServerSourceController controller = new OSMServerSourceController();
        controller.open(toolbox, OSMPlugin.class);
        UrlDataSource source = new UrlDataSource("Open Street Map", ourServer);
        controller.handleDeactivateSource(source);

        support.verifyAll();
    }

    /**
     * Tests that it can be loaded via service loader.
     */
    @Test
    public void testServiceLoader()
    {
        boolean hasIt = false;
        ServiceLoader<ServerSourceController> loader = ServiceLoader.load(ServerSourceController.class);
        for (ServerSourceController controller : loader)
        {
            if (controller instanceof OSMServerSourceController)
            {
                hasIt = true;
                break;
            }
        }

        assertTrue(hasIt);
    }

    /**
     * The add models answer.
     *
     * @return The model id.
     */
    private long[] addModelAnswer()
    {
        @SuppressWarnings("unchecked")
        SimpleSessionOnlyCacheDeposit<XYZTileLayerInfo> deposit = (SimpleSessionOnlyCacheDeposit<XYZTileLayerInfo>)EasyMock
                .getCurrentArguments()[0];
        DataModelCategory expectedCat = XYZTileUtils.newLayersCategory(ourServer, OSMUtil.PROVIDER);
        assertEquals(expectedCat, deposit.getCategory());
        assertEquals(XYZTileUtils.LAYERS_DESCRIPTOR, deposit.getAccessors().iterator().next().getPropertyDescriptor());
        XYZTileLayerInfo layer = deposit.getInput().iterator().next();

        assertEquals(OSMUtil.PROVIDER, layer.getName());
        assertEquals("Open Street Map", layer.getDisplayName());
        assertEquals(Projection.EPSG_4326, layer.getProjection());
        assertEquals(2, layer.getNumberOfTopLevels());
        assertFalse(layer.isTms());
        assertEquals(2, layer.getMinZoomLevel());
        assertEquals(ourServer, layer.getServerUrl());
        assertEquals("Open Street Map", layer.getServerInfo().getServerName());

        return new long[] { 0 };
    }

    /**
     * Creates an easy mocked {@link DataRegistry}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link DataRegistry}.
     */
    @SuppressWarnings("unchecked")
    private DataRegistry createDataRegistry(EasyMockSupport support)
    {
        DataRegistry registry = support.createMock(DataRegistry.class);

        EasyMock.expect(registry.addModels(EasyMock.isA(SimpleSessionOnlyCacheDeposit.class))).andAnswer(this::addModelAnswer);

        return registry;
    }

    /**
     * Creates an easy mocked toolbox.
     *
     * @param support Used to create the mock.
     * @param registry The registry to return.
     * @return The mocked toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support, DataRegistry registry)
    {
        Preferences prefs = support.createMock(Preferences.class);
        EasyMock.expect(prefs.getJAXBObject(EasyMock.eq(UrlSourceConfig.class), EasyMock.cmpEq("serverConfig"),
                EasyMock.isA(UrlSourceConfig.class))).andReturn(new UrlSourceConfig());
        PreferencesRegistry prefsRegistry = support.createMock(PreferencesRegistry.class);
        EasyMock.expect(prefsRegistry.getPreferences(EasyMock.eq(OSMPlugin.class))).andReturn(prefs);

        Toolbox toolbox = support.createMock(Toolbox.class);

        NetworkConfigurationManager networkConfigManager = support.createMock(NetworkConfigurationManager.class);
        networkConfigManager
                .addChangeListener(EasyMock.isA(NetworkConfigurationManager.NetworkConfigurationChangeListener.class));
        SystemToolbox systemToolbox = support.createMock(SystemToolbox.class);
        EasyMock.expect(toolbox.getSystemToolbox()).andReturn(systemToolbox);
        EasyMock.expect(systemToolbox.getNetworkConfigurationManager()).andReturn(networkConfigManager);
        EasyMock.expect(toolbox.getDataRegistry()).andReturn(registry);
        EasyMock.expect(toolbox.getPreferencesRegistry()).andReturn(prefsRegistry);

        return toolbox;
    }
}
