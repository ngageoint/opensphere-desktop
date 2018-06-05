package io.opensphere.osm.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.ServiceLoader;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.NetworkConfigurationManager;
import io.opensphere.core.SystemToolbox;
import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.SimpleSessionOnlyCacheDeposit;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.matchers.EasyMockHelper;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.taskactivity.TaskActivity;
import io.opensphere.mantle.datasources.impl.UrlDataSource;
import io.opensphere.mantle.datasources.impl.UrlSourceConfig;
import io.opensphere.osm.OSMPlugin;
import io.opensphere.osm.util.OSMUtil;
import io.opensphere.server.customization.ServerCustomization;
import io.opensphere.server.toolbox.ServerSourceController;
import io.opensphere.xyztile.envoy.XYZTileEnvoy;
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
    private static final String OUR_SERVER = "http://osm.geointservices.io/osm_tiles_pc/{z}/{x}/{y}.png";

    /**
     * The url used in the server ping.
     */
    private static final String PING_URL_STRING = "http://osm.geointservices.io/osm_tiles_pc/0/0/0.png";

    /**
     * Tests creating a new source.
     */
    @Test
    public void testCreateNewSource()
    {
        OSMServerSourceController controller = new OSMServerSourceController();
        UrlDataSource source = (UrlDataSource)controller.createNewSource("New Server");

        assertEquals("Open Street Map", source.getName());
        assertEquals(OUR_SERVER, source.getURL());
    }

    /**
     * Tests the example url.
     */
    @Test
    public void testGetExampleUrl()
    {
        OSMServerSourceController controller = new OSMServerSourceController();

        assertEquals(OUR_SERVER, controller.getExampleUrl());
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
    @Test
    @SuppressWarnings("unchecked")
    public void testHandleActivateSource()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry registry = createDataRegistry(support);
        Toolbox toolbox = createToolbox(support, registry);
        createTaskActivity(support, toolbox);
        try
        {
            createServerPing(support, toolbox, true);
        }
        catch (IOException | URISyntaxException e)
        {
            fail("Unexpected exception during simulated server ping mock setup");
        }

        EasyMock.expect(toolbox.getDataRegistry()).andReturn(registry);
        EasyMock.expect(registry.addModels(EasyMock.isA(SimpleSessionOnlyCacheDeposit.class))).andAnswer(this::addModelAnswer);

        support.replayAll();

        OSMServerSourceController controller = new OSMServerSourceController();
        controller.open(toolbox, OSMPlugin.class);
        UrlDataSource source = new UrlDataSource("Open Street Map", OUR_SERVER);
        controller.handleActivateSource(source);

        support.verifyAll();
    }

    /**
     * Tests handling server activating with no server connection.
     *
     * Note: The absence of the addModels method call is the verification.
     */
    @Test
    public void testActivationWhileDisconnected()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry registry = createDataRegistry(support);
        Toolbox toolbox = createToolbox(support, registry);
        createTaskActivity(support, toolbox);
        try
        {
            createServerPing(support, toolbox, false);
        }
        catch (IOException | URISyntaxException e)
        {
            fail("Unexpected exception during simulated server ping mock setup");
        }

        support.replayAll();

        OSMServerSourceController controller = new OSMServerSourceController();
        controller.open(toolbox, OSMPlugin.class);
        UrlDataSource source = new UrlDataSource("Open Street Map", OUR_SERVER);
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
        Toolbox toolbox = createToolbox(support, registry);

        EasyMock.expect(toolbox.getDataRegistry()).andReturn(registry);
        EasyMock.expect(registry.removeModels(EasyMock.eq(XYZTileUtils.newLayersCategory(OUR_SERVER, OSMUtil.PROVIDER)),
                EasyMock.eq(false))).andReturn(new long[] {});

        support.replayAll();

        OSMServerSourceController controller = new OSMServerSourceController();
        controller.open(toolbox, OSMPlugin.class);
        UrlDataSource source = new UrlDataSource("Open Street Map", OUR_SERVER);
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
        DataModelCategory expectedCat = XYZTileUtils.newLayersCategory(OUR_SERVER, OSMUtil.PROVIDER);
        assertEquals(expectedCat, deposit.getCategory());
        assertEquals(XYZTileUtils.LAYERS_DESCRIPTOR, deposit.getAccessors().iterator().next().getPropertyDescriptor());
        XYZTileLayerInfo layer = deposit.getInput().iterator().next();

        assertEquals(OSMUtil.PROVIDER, layer.getName());
        assertEquals("Open Street Map", layer.getDisplayName());
        assertEquals(Projection.EPSG_4326, layer.getProjection());
        assertEquals(2, layer.getNumberOfTopLevels());
        assertFalse(layer.isTms());
        assertEquals(2, layer.getMinZoomLevel());
        assertEquals(OUR_SERVER, layer.getServerUrl());
        assertEquals("Open Street Map", layer.getServerInfo().getServerName());

        return new long[] { 0 };
    }

    /**
     * Creates an easy mocked {@link DataRegistry}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link DataRegistry}.
     */
    private DataRegistry createDataRegistry(EasyMockSupport support)
    {
        DataRegistry registry = support.createMock(DataRegistry.class);

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
        EasyMock.expect(toolbox.getPreferencesRegistry()).andReturn(prefsRegistry);

        return toolbox;
    }

    /**
     * Creates an easy mocked {@link TaskActivity}.
     *
     * @param support Used to create the mock.
     * @param toolbox Mocked system toolbox
     */
    private void createTaskActivity(EasyMockSupport support, Toolbox toolbox)
    {
        UIRegistry uiReg = support.createMock(UIRegistry.class);
        MenuBarRegistry menuBarReg = support.createMock(MenuBarRegistry.class);
        List<TaskActivity> activities = New.list();

        EasyMock.expect(toolbox.getUIRegistry()).andReturn(uiReg);
        EasyMock.expect(uiReg.getMenuBarRegistry()).andReturn(menuBarReg);
        menuBarReg.addTaskActivity(EasyMock.isA(TaskActivity.class));
        EasyMock.expectLastCall().andAnswer(() -> addTaskActivityAnswer(activities));
    }

    /**
     * Answer for the addTaskActivity call on the mocked {@link MenuBarRegistry}.
     *
     * @param activities The list for which to add the {@link TaskActivity}.
     * @return Null
     */
    private Void addTaskActivityAnswer(List<TaskActivity> activities)
    {
        TaskActivity activity = (TaskActivity)EasyMock.getCurrentArguments()[0];
        assertTrue(activity.isActive());
        activities.add(activity);

        return null;
    }

    /**
     * Creates an easy mock of the {@link XYZTileEnvoy} ping method.
     *
     * @param support Used to create the mock
     * @param toolbox Mocked system toolbox
     * @param success Controls whether to simulate a successful or failed server ping
     * @throws IOException exception during server communication
     * @throws URISyntaxException exception while converting URL to URI
     */
    private void createServerPing(EasyMockSupport support, Toolbox toolbox, boolean success) throws IOException, URISyntaxException
    {
        ServerProviderRegistry serverRegistry = support.createMock(ServerProviderRegistry.class);
        HttpServer server = support.createMock(HttpServer.class);
        @SuppressWarnings("unchecked")
        ServerProvider<HttpServer> serverProvider = support.createMock(ServerProvider.class);
        URL url = new URL(PING_URL_STRING);

        EasyMock.expect(toolbox.getServerProviderRegistry()).andReturn(serverRegistry);
        EasyMock.expect(serverRegistry.getProvider(HttpServer.class)).andReturn(serverProvider);
        EasyMock.expect(serverProvider.getServer(EasyMockHelper.eq(url))).andReturn(server);
        server.sendHead(EasyMock.isA(URL.class), EasyMock.isA(ResponseValues.class));
        EasyMock.expectLastCall().andAnswer(() -> sendHeadAnswer(success));
    }

    /**
     * Creates a mocked server response during the ping.
     *
     * @param success Controls whether to simulate a successful or failed server ping
     * @return Null
     * @throws IOException exception during server communication
     */
    private Void sendHeadAnswer(boolean success) throws IOException
    {
        ResponseValues respVal = (ResponseValues)EasyMock.getCurrentArguments()[1];

        if (success)
        {
            respVal.setResponseCode(HttpURLConnection.HTTP_OK);
        }
        else
        {
            throw new IOException();
        }

        return null;
    }
}
