package io.opensphere.server.state.activate.serversource.genericserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.easymock.EasyMock;
import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.toolbox.ServerSourceController;

/**
 * Tests the ServerSourceFilterer class.
 *
 */
public class GenericServerSourceFiltererTest
{
    /**
     * The wfs non active but added server.
     */
    private static final String ourWfsNonActiveServer = "http://nonactive/ogc/wfs";

    /**
     * The wms non active but added server.
     */
    private static final String ourWmsNonActiveServer = "http://wmsnonactive/ogc/wms";

    /**
     * The wfs non added server.
     */
    private static final String ourWfsNonAddedServer = "http://wfsnonadded/ogc/wfs";

    /**
     * The wms non added server.
     */
    private static final String ourWmsNonAddedServer = "http://wmsnonadded/ogc/wms";

    /**
     * The wfs active server.
     */
    private static final String ourWfsActiveServer = "http://wfsactive/ogc/wfs";

    /**
     * The wms active server.
     */
    private static final String ourWmsActiveServer = "http://wmsactive/ogc/wms";

    /**
     * Tests getting servers added to the system but not active.
     */
    @Test
    public void testGetNonActiveServers()
    {
        List<IDataSource> registeredServers = createRegisteredServers();
        ServerSourceController sourceController = createSourceController(registeredServers);
        List<IDataSource> servers = createStateServers();

        EasyMock.replay(sourceController);

        GenericServerSourceFilterer filterer = new GenericServerSourceFilterer(sourceController);
        List<IDataSource> nonActiveServers = filterer.getNonActiveServers(servers);

        assertEquals(2, nonActiveServers.size());

        boolean wfsServerFound = false;
        boolean wmsServerFound = false;

        for (IDataSource dataSource : nonActiveServers)
        {
            OGCServerSource server = (OGCServerSource)dataSource;

            if (ourWfsNonActiveServer.equals(server.getWFSServerURL()))
            {
                wfsServerFound = true;
            }
            else if (ourWmsNonActiveServer.equals(server.getWMSServerURL()))
            {
                wmsServerFound = true;
            }
        }

        assertTrue(wfsServerFound);
        assertTrue(wmsServerFound);

        EasyMock.verify(sourceController);
    }

    /**
     * Tests getting server not added to the system.
     */
    @Test
    public void testGetNonAddedServers()
    {
        List<IDataSource> registeredServers = createRegisteredServers();
        ServerSourceController sourceController = createSourceController(registeredServers);
        List<IDataSource> servers = createStateServers();

        EasyMock.replay(sourceController);

        GenericServerSourceFilterer filterer = new GenericServerSourceFilterer(sourceController);
        List<IDataSource> nonAddedServers = filterer.getNonAddedServers(servers);

        assertEquals(2, nonAddedServers.size());

        boolean wfsServerFound = false;
        boolean wmsServerFound = false;

        for (IDataSource dataSource : nonAddedServers)
        {
            OGCServerSource server = (OGCServerSource)dataSource;
            if (ourWfsNonAddedServer.equals(server.getWFSServerURL()))
            {
                wfsServerFound = true;
            }
            else if (ourWmsNonAddedServer.equals(server.getWMSServerURL()))
            {
                wmsServerFound = true;
            }
        }

        assertTrue(wfsServerFound);
        assertTrue(wmsServerFound);

        EasyMock.verify(sourceController);
    }

    /**
     * Creates a list of the system registered servers.
     *
     * @return The system registered servers.
     */
    private List<IDataSource> createRegisteredServers()
    {
        List<IDataSource> servers = New.list();

        OGCServerSource server = new OGCServerSource();
        server.setWFSServerURL("http://random/ogc/wfs");
        server.setWMSServerURL("http://random/ogc/wms");

        servers.add(server);

        server = new OGCServerSource();
        server.setWFSServerURL(ourWfsNonActiveServer);
        server.setActive(false);

        servers.add(server);

        server = new OGCServerSource();
        server.setWMSServerURL(ourWmsNonActiveServer);
        server.setActive(false);

        servers.add(server);

        server = new OGCServerSource();
        server.setWFSServerURL(ourWfsActiveServer);
        server.setActive(true);

        servers.add(server);

        server = new OGCServerSource();
        server.setWMSServerURL(ourWmsActiveServer);
        server.setActive(true);

        servers.add(server);

        return servers;
    }

    /**
     * Creates the source controller.
     *
     * @param sourceList The list of sources the controller returns.
     * @return The source controller.
     */
    private ServerSourceController createSourceController(List<IDataSource> sourceList)
    {
        ServerSourceController controller = EasyMock.createMock(ServerSourceController.class);
        controller.getSourceList();
        EasyMock.expectLastCall().andReturn(sourceList);

        return controller;
    }

    /**
     * Creates the server source read from a state file.
     *
     * @return The list of server sources.
     */
    private List<IDataSource> createStateServers()
    {
        List<IDataSource> stateServers = New.list();

        OGCServerSource stateServer = new OGCServerSource();
        stateServer.setWFSServerURL(ourWfsNonActiveServer);

        stateServers.add(stateServer);

        stateServer = new OGCServerSource();
        stateServer.setWMSServerURL(ourWmsNonActiveServer);

        stateServers.add(stateServer);

        stateServer = new OGCServerSource();
        stateServer.setWFSServerURL(ourWfsActiveServer);

        stateServers.add(stateServer);

        stateServer = new OGCServerSource();
        stateServer.setWMSServerURL(ourWmsActiveServer);

        stateServers.add(stateServer);

        stateServer = new OGCServerSource();
        stateServer.setWFSServerURL(ourWfsNonAddedServer);

        stateServers.add(stateServer);

        stateServer = new OGCServerSource();
        stateServer.setWMSServerURL(ourWmsNonAddedServer);

        stateServers.add(stateServer);

        return stateServers;
    }
}
