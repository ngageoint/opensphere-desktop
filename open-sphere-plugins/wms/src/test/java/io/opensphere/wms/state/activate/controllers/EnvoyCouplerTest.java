package io.opensphere.wms.state.activate.controllers;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.easymock.EasyMock;
import org.junit.Test;

import io.opensphere.core.Plugin;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.registry.GenericRegistry;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.wms.envoy.WMSEnvoy;
import io.opensphere.wms.layer.WMSDataType;
import io.opensphere.wms.state.model.WMSEnvoyAndState;
import io.opensphere.wms.state.model.WMSLayerState;

/**
 * Tests the EnvoyCoupler.
 *
 */
public class EnvoyCouplerTest
{
    /**
     * The server url.
     */
    private static final String ourServerUrl = "http://ourserver/ogc/wms";

    /**
     * The layer.
     */
    private static final String ourLayer = "testLayer";

    /**
     * The states created by the test.
     */
    private List<WMSLayerState> myStates;

    /**
     * Tests getting related envoys.
     */
    @Test
    public void testRetrieveRelatedEnvoys()
    {
        Plugin wmsPlugin = createPlugin();
        List<ServerConnectionParams> params = createServerConnectionParams(ourServerUrl);
        List<WMSEnvoyAndEnvoy> envoys = createEnvoys(params);
        GenericRegistry<Envoy> envoyRegistry = createEnvoyRegistry(wmsPlugin, envoys);

        WMSDataType dataType = createDataType();
        DataGroupController dataController = createDataController(dataType);

        EasyMock.replay(dataType);

        List<WMSEnvoyAndState> envoyAndStates = performTest(params, envoys, wmsPlugin, dataController, envoyRegistry);

        assertEquals(1, envoyAndStates.size());
        WMSEnvoyAndState envoyAndState = envoyAndStates.get(0);

        assertEquals(myStates.get(1), envoyAndState.getState());
        assertEquals(envoys.get(1), envoyAndState.getEnvoy());
        assertEquals(dataType, envoyAndState.getTypeInfo());

        EasyMock.verify(dataType);
    }

    /**
     * Tests getting related envoys when the server isn't loaded.
     */
    @Test
    public void testRetrieveRelatedEnvoysServerNotLoaded()
    {
        Plugin wmsPlugin = createPlugin();
        List<ServerConnectionParams> params = createServerConnectionParams("http://wrongserver");
        List<WMSEnvoyAndEnvoy> envoys = createEnvoys(params);
        GenericRegistry<Envoy> envoyRegistry = createEnvoyRegistry(wmsPlugin, envoys);

        DataGroupController dataController = EasyMock.createMock(DataGroupController.class);

        List<WMSEnvoyAndState> envoyAndStates = performTest(params, envoys, wmsPlugin, dataController, envoyRegistry);

        assertEquals(0, envoyAndStates.size());
    }

    /**
     * Creates the easy mocked data controller.
     *
     * @param dataType The data type the controller returns.
     * @return The controller.
     */
    private DataGroupController createDataController(WMSDataType dataType)
    {
        DataGroupController controller = EasyMock.createMock(DataGroupController.class);
        controller.findMemberById(ourServerUrl + "!!nonExistingLayer");
        EasyMock.expectLastCall().andReturn(null);
        controller.findMemberById(EasyMock.eq(ourServerUrl + "!!" + ourLayer));
        EasyMock.expectLastCall().andReturn(dataType);

        return controller;
    }

    /**
     * Creates the easy mocked data type.
     *
     * @return The data type.
     */
    private WMSDataType createDataType()
    {
        WMSDataType dataType = EasyMock.createMock(WMSDataType.class);

        return dataType;
    }

    /**
     * Create envoy registry.
     *
     * @param wmsPlugin The test plugin.
     * @param envoys The envoys associated with the plugin.
     * @return The envoy registry.
     */
    private GenericRegistry<Envoy> createEnvoyRegistry(Plugin wmsPlugin, List<WMSEnvoyAndEnvoy> envoys)
    {
        GenericRegistry<Envoy> registry = new GenericRegistry<>();
        registry.addObjectsForSource(wmsPlugin, envoys);

        return registry;
    }

    /**
     * Creates envoys.
     *
     * @param serverConnectionParams the params the envoys returns.
     * @return The envoys.
     */
    private List<WMSEnvoyAndEnvoy> createEnvoys(List<ServerConnectionParams> serverConnectionParams)
    {
        List<WMSEnvoyAndEnvoy> envoys = New.list();

        for (ServerConnectionParams param : serverConnectionParams)
        {
            WMSEnvoyAndEnvoy envoy = EasyMock.createMock(WMSEnvoyAndEnvoy.class);
            envoy.getServerConnectionConfig();
            EasyMock.expectLastCall().andReturn(param);
            EasyMock.expectLastCall().atLeastOnce();
            envoys.add(envoy);
        }

        return envoys;
    }

    /**
     * Creates the test plugin.
     *
     * @return The plugin.
     */
    private Plugin createPlugin()
    {
        Plugin plugin = EasyMock.createMock(Plugin.class);

        return plugin;
    }

    /**
     * Creates a server connection params.
     *
     * @param server The server url.
     * @return The connection params.
     */
    private List<ServerConnectionParams> createServerConnectionParams(String server)
    {
        ServerConnectionParams param1 = EasyMock.createMock(ServerConnectionParams.class);
        param1.getWmsUrl();
        EasyMock.expectLastCall().andReturn("http://someotherhost/ogc/wms");

        ServerConnectionParams param2 = EasyMock.createMock(ServerConnectionParams.class);
        param2.getWmsUrl();
        EasyMock.expectLastCall().andReturn(server);

        if (ourServerUrl.equals(server))
        {
            param2.getServerId(EasyMock.eq(OGCServerSource.WMS_GETMAP_SERVICE));
            EasyMock.expectLastCall().andReturn(server);
            EasyMock.expectLastCall().atLeastOnce();
        }

        return New.list(param1, param2);
    }

    /**
     * Creates the test states.
     *
     * @return The test states.
     */
    private List<WMSLayerState> createStates()
    {
        WMSLayerState state1 = new WMSLayerState();
        state1.setUrl(ourServerUrl);
        state1.setId("nonExistingLayer");
        state1.getParameters().setLayerName("nonExistingLayer");

        WMSLayerState state2 = new WMSLayerState();
        state2.setUrl(ourServerUrl);
        state2.setId(ourLayer);
        state2.getParameters().setLayerName(ourLayer);

        return New.list(state1, state2);
    }

    /**
     * Performs the test.
     *
     * @param params The params.
     * @param envoys the envoys.
     * @param wmsPlugin The plugin.
     * @param dataController The data controller.
     * @param envoyRegistry The envoy registry.
     * @return The returned envoys, states and data types.
     */
    private List<WMSEnvoyAndState> performTest(List<ServerConnectionParams> params, List<WMSEnvoyAndEnvoy> envoys,
            Plugin wmsPlugin, DataGroupController dataController, GenericRegistry<Envoy> envoyRegistry)
    {
        myStates = createStates();

        for (ServerConnectionParams param : params)
        {
            EasyMock.replay(param);
        }

        for (WMSEnvoy envoy : envoys)
        {
            EasyMock.replay(envoy);
        }

        EasyMock.replay(wmsPlugin, dataController);

        EnvoyCoupler coupler = new EnvoyCoupler(wmsPlugin, envoyRegistry, dataController);
        List<WMSEnvoyAndState> envoyAndStates = coupler.retrieveRelatedEnvoys(myStates);

        for (ServerConnectionParams param : params)
        {
            EasyMock.verify(param);
        }

        for (WMSEnvoy envoy : envoys)
        {
            EasyMock.verify(envoy);
        }

        EasyMock.verify(wmsPlugin, dataController);

        return envoyAndStates;
    }
}
