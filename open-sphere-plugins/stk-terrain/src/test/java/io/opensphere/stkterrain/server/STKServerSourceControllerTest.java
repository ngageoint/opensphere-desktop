package io.opensphere.stkterrain.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.registry.GenericRegistry;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.IDataSourceConfig;
import io.opensphere.server.config.v1.OGCServerConfig;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.toolbox.ServerSourceController;
import io.opensphere.stkterrain.envoy.QuantizedMeshEnvoy;
import io.opensphere.stkterrain.envoy.TileSetEnvoy;
import io.opensphere.stkterrain.envoy.TileSetMetadataEnvoy;
import io.opensphere.stkterrain.mantle.STKDataGroupController;
import io.opensphere.test.core.matchers.EasyMockHelper;

/**
 * Unit test for {@link STKServerSourceController}.
 */
public class STKServerSourceControllerTest extends GenericRegistry<Envoy>
{
    /**
     * The expected test server url.
     */
    private static final String ourTestServer = "http://somehost/stk-terrain";

    /**
     * Tests activating an STK server.
     *
     * @throws IOException Bad IO.
     * @throws URISyntaxException Bad URI.
     * @throws InterruptedException Don't interrupt.
     */
    @SuppressWarnings("unchecked")
//    @Test
    public void testActivate() throws IOException, URISyntaxException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        CountDownLatch latch = new CountDownLatch(1);

        OGCServerSource source = new OGCServerSource();
        source.setWMSServerURL(ourTestServer);
        source.addDataSourceChangeListener((evt) ->
        {
            if (!source.isBusy())
            {
                latch.countDown();
            }
        });

        ServerProviderRegistry serverRegistry = createServerProviderRegistry(support, 200, source);
        IDataSourceConfig config = createConfig(support);
        DataRegistry dataRegistry = support.createMock(DataRegistry.class);
        dataRegistry.addChangeListener(EasyMock.isA(STKDataGroupController.class), EasyMock.isA(DataModelCategory.class),
                EasyMock.isA(PropertyDescriptor.class));
        EasyMock.expect(
                dataRegistry.removeModels(EasyMock.eq(new DataModelCategory(ourTestServer, null, null)), EasyMock.eq(false)))
                .andReturn(new long[] {});
        dataRegistry.removeChangeListener(EasyMock.isA(STKDataGroupController.class));
        DataGroupController groupController = support.createMock(DataGroupController.class);
        EasyMock.expect(groupController.addRootDataGroupInfo(EasyMock.isA(DataGroupInfo.class),
                EasyMock.isA(STKDataGroupController.class))).andReturn(true);
        EasyMock.expect(groupController.removeDataGroupInfo(EasyMock.isA(DataGroupInfo.class),
                EasyMock.isA(STKDataGroupController.class))).andReturn(true);
        Toolbox toolbox = createToolboxForActivate(support, config, serverRegistry, null, dataRegistry, groupController);
        EasyMock.expect(toolbox.getEnvoyRegistry()).andReturn(this);

        support.replayAll();

        STKServerSourceController controller = new STKServerSourceController();
        controller.open(toolbox, this.getClass());
        controller.activateSource(source);

        assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));
        assertTrue(source.isActive());
        assertFalse(source.loadError());
        assertFalse(source.isBusy());

        List<Envoy> envoys = getObjects();

        assertEquals(3, envoys.size());

        TileSetEnvoy tileSetEnvoy = null;
        TileSetMetadataEnvoy metadataEnvoy = null;
        QuantizedMeshEnvoy meshEnvoy = null;

        for (Envoy envoy : envoys)
        {
            if (envoy instanceof TileSetEnvoy)
            {
                tileSetEnvoy = (TileSetEnvoy)envoy;
            }
            else if (envoy instanceof TileSetMetadataEnvoy)
            {
                metadataEnvoy = (TileSetMetadataEnvoy)envoy;
            }
            else if (envoy instanceof QuantizedMeshEnvoy)
            {
                meshEnvoy = (QuantizedMeshEnvoy)envoy;
            }
        }

        assertNotNull(tileSetEnvoy);
        assertTrue(tileSetEnvoy.getValidatorSupport() instanceof STKValidatorSupport);
        assertNotNull(metadataEnvoy);
        assertNotNull(meshEnvoy);

        controller.deactivateSource(source);

        support.verifyAll();
    }

    /**
     * Tests activating an invalid server.
     *
     * @throws IOException Bad IO.
     * @throws URISyntaxException Bad uri.
     * @throws InterruptedException Don't interrupt.
     */
//    @Test
    public void testActivateInvalidServer() throws IOException, URISyntaxException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        CountDownLatch latch = new CountDownLatch(1);

        EventManager eventManager = createEventManager(support);
        OGCServerSource source = new OGCServerSource();
        source.setWMSServerURL(ourTestServer);
        source.addDataSourceChangeListener((evt) ->
        {
            if (!source.isBusy())
            {
                latch.countDown();
            }
        });
        ServerProviderRegistry serverRegistry = createServerProviderRegistry(support, 503, source);
        IDataSourceConfig config = createConfig(support);
        DataRegistry dataRegistry = support.createNiceMock(DataRegistry.class);
        DataGroupController groupController = support.createNiceMock(DataGroupController.class);
        Toolbox toolbox = createToolboxForActivate(support, config, serverRegistry, eventManager, dataRegistry, groupController);

        support.replayAll();

        STKServerSourceController controller = new STKServerSourceController();
        controller.open(toolbox, this.getClass());
        controller.activateSource(source);

        assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));
        assertFalse(source.isActive());
        assertTrue(source.loadError());
        assertFalse(source.isBusy());

        support.verifyAll();
    }

    /**
     * Tests creating a new source.
     */
//    @Test
    public void testCreateNewSource()
    {
        STKServerSourceController controller = new STKServerSourceController();

        IDataSource source = controller.createNewSource(null);

        assertTrue(source instanceof OGCServerSource);
        OGCServerSource newSource = (OGCServerSource)source;

        assertEquals("STK Terrain Server", newSource.getServerType());
        assertTrue(newSource.getName().isEmpty());
    }

    /**
     * Tests deactivating the server.
     */
//    @Test
    public void testDeactivateNullServer()
    {
        EasyMockSupport support = new EasyMockSupport();

        IDataSourceConfig config = createConfig(support);
        Toolbox toolbox = createToolbox(support, config, false);
        DataRegistry dataRegistry = support.createMock(DataRegistry.class);
        EasyMock.expect(
                dataRegistry.removeModels(EasyMock.eq(new DataModelCategory(ourTestServer, null, null)), EasyMock.eq(false)))
                .andReturn(new long[] {});
        EasyMock.expect(toolbox.getDataRegistry()).andReturn(dataRegistry);

        support.replayAll();

        STKServerSourceController controller = new STKServerSourceController();
        controller.open(toolbox, this.getClass());

        OGCServerSource source = new OGCServerSource();
        source.setWMSServerURL(ourTestServer);
        source.setActive(true);

        controller.deactivateSource(source);

        assertFalse(source.isActive());

        support.verifyAll();
    }

    /**
     * Tests getting the source description.
     */
//    @Test
    public void testGetSourceDescription()
    {
        STKServerSourceController controller = new STKServerSourceController();

        OGCServerSource source = new OGCServerSource();
        source.setWMSServerURL("http://terrainserver");

        assertTrue(controller.getSourceDescription(source).contains("http://terrainserver"));
    }

    /**
     * Tests the type name of the server.
     */
//    @Test
    public void testGetTypeName()
    {
        STKServerSourceController controller = new STKServerSourceController();

        OGCServerSource source = new OGCServerSource();
        assertEquals("STK Terrain Server", controller.getTypeName(source));
    }

    /**
     * Tests opening the controller.
     */
//    @Test
    public void testOpen()
    {
        EasyMockSupport support = new EasyMockSupport();

        IDataSourceConfig config = createConfig(support);
        Toolbox toolbox = createToolbox(support, config, false);

        support.replayAll();

        STKServerSourceController controller = new STKServerSourceController();
        controller.open(toolbox, this.getClass());

        support.verifyAll();
    }

    /**
     * Verifies the {@link STKServerSourceController} class can be loaded via
     * {@link ServiceLoader}.
     */
    @Test
    public void testServiceLoader()
    {
        ServiceLoader<ServerSourceController> controllers = ServiceLoader.load(ServerSourceController.class);

        boolean isThere = false;
        for (ServerSourceController controller : controllers)
        {
            if (controller instanceof STKServerSourceController)
            {
                isThere = true;
                break;
            }
        }

        assertTrue(isThere);
    }

    /**
     * Creates a mocked server config.
     *
     * @param support Used to create the mock.
     * @return The mocked server config.
     */
    private IDataSourceConfig createConfig(EasyMockSupport support)
    {
        IDataSourceConfig config = support.createMock(IDataSourceConfig.class);
        EasyMock.expect(config.getSourceList()).andReturn(New.list());

        return config;
    }

    /**
     * Creates an easy mocked {@link EventManager}.
     *
     * @param support Use to create the mock.
     * @return The mocked {@link EventManager}.
     */
    private EventManager createEventManager(EasyMockSupport support)
    {
        EventManager eventManager = support.createMock(EventManager.class);

        eventManager.publishEvent(EasyMock.isA(UserMessageEvent.class));

        return eventManager;
    }

    /**
     * Creates an easy mocked {@link ServerProviderRegistry}.
     *
     * @param support Used to create the mock.
     * @param responseCode The http response code to return.
     * @param source The server being activated in the test..
     * @return The mocked {@link ServerProviderRegistry}.
     * @throws IOException Bad IO.
     * @throws URISyntaxException Bad uri.
     */
    private ServerProviderRegistry createServerProviderRegistry(EasyMockSupport support, int responseCode, OGCServerSource source)
        throws IOException, URISyntaxException
    {
        URL expectedUrl = new URL(ourTestServer + "/v1/tilesets");

        HttpServer server = support.createMock(HttpServer.class);
        EasyMock.expect(server.sendGet(EasyMockHelper.eq(expectedUrl), EasyMock.isA(ResponseValues.class)))
                .andAnswer(() -> sendGetAnswer(responseCode, source));

        @SuppressWarnings("unchecked")
        ServerProvider<HttpServer> provider = support.createMock(ServerProvider.class);
        EasyMock.expect(provider.getServer(EasyMockHelper.eq(expectedUrl))).andReturn(server);

        ServerProviderRegistry registry = support.createMock(ServerProviderRegistry.class);
        EasyMock.expect(registry.getProvider(EasyMock.eq(HttpServer.class))).andReturn(provider);

        return registry;
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @param config The config to return from the {@link Preferences} mock.
     * @param expectPut True if we should expect a put call on preferences,
     *            false otherwise.
     * @return The mocked toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support, IDataSourceConfig config, boolean expectPut)
    {
        Preferences preferences = support.createMock(Preferences.class);
        preferences.getJAXBObject(EasyMock.eq(OGCServerConfig.class), EasyMock.cmpEq("serverConfig"),
                EasyMock.isA(OGCServerConfig.class));
        EasyMock.expectLastCall().andReturn(config).atLeastOnce();

        if (expectPut)
        {
            EasyMock.expect(preferences.putJAXBObject(EasyMock.cmpEq("serverConfig"), EasyMock.eq(config), EasyMock.anyBoolean(),
                    EasyMock.anyObject())).andReturn(config);
        }

        PreferencesRegistry registry = support.createMock(PreferencesRegistry.class);
        EasyMock.expect(registry.getPreferences(EasyMock.eq(this.getClass()))).andReturn(preferences).atLeastOnce();

        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getPreferencesRegistry()).andReturn(registry).atLeastOnce();

        return toolbox;
    }

    /**
     * Creates an easy mocked {@link Toolbox} used for activate server tests.
     *
     * @param support Used to create the mock.
     * @param config The test config to use.
     * @param serverRegistry The mocked server registry.
     * @param eventManager The mocked event manager or null if event manager is
     *            not expected to be called.
     * @param dataRegistry A mocked data registry to return.
     * @param groupController A mocked group controller to return.
     * @return The mocked {@link Toolbox}.
     */
    private Toolbox createToolboxForActivate(EasyMockSupport support, IDataSourceConfig config,
            ServerProviderRegistry serverRegistry, EventManager eventManager, DataRegistry dataRegistry,
            DataGroupController groupController)
    {
        Toolbox toolbox = createToolbox(support, config, false);

        EasyMock.expect(toolbox.getDataRegistry()).andReturn(dataRegistry).atLeastOnce();

        EasyMock.expect(toolbox.getServerProviderRegistry()).andReturn(serverRegistry);
        if (eventManager != null)
        {
            EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager);
        }

        OrderManager orderManager = support.createNiceMock(OrderManager.class);
        OrderManagerRegistry orderRegistry = support.createMock(OrderManagerRegistry.class);
        EasyMock.expect(orderRegistry.getOrderManager(EasyMock.eq(DefaultOrderCategory.DEFAULT_ELEVATION_FAMILY),
                EasyMock.eq(DefaultOrderCategory.EARTH_ELEVATION_CATEGORY))).andReturn(orderManager).anyTimes();
        EasyMock.expect(toolbox.getOrderManagerRegistry()).andReturn(orderRegistry).anyTimes();

        MantleToolbox mantleToolbox = support.createMock(MantleToolbox.class);
        EasyMock.expect(mantleToolbox.getDataGroupController()).andReturn(groupController).anyTimes();
        PluginToolboxRegistry toolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(EasyMock.eq(MantleToolbox.class))).andReturn(mantleToolbox).anyTimes();
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(toolboxRegistry).anyTimes();

        return toolbox;
    }

    /**
     * The answer to the mocked sendGet call.
     *
     * @param responseCode The http response code to return.
     * @param source The server being activated, check to make sure its busy
     *            flag is set to true.
     * @return Null.
     */
    private CancellableInputStream sendGetAnswer(int responseCode, OGCServerSource source)
    {
        ResponseValues response = (ResponseValues)EasyMock.getCurrentArguments()[1];
        response.setResponseCode(responseCode);
        assertTrue(source.isBusy());

        return null;
    }
}
