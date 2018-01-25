package io.opensphere.server.state.activate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.w3c.dom.Node;

import io.opensphere.core.modulestate.ModuleStateController;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.state.activate.serversource.IActivationListener;
import io.opensphere.server.state.activate.serversource.ServerSourceFilterer;
import io.opensphere.server.state.activate.serversource.ServerSourceProvider;
import io.opensphere.server.toolbox.ServerSourceController;
import io.opensphere.server.toolbox.ServerSourceControllerManager;

/**
 * Utility class for server activator tests.
 */
public final class ServerActivatorUtils
{
    /**
     * the wfs host.
     */
    private static final String ourWfsHost = "wfsHost";

    /**
     * Asserts the list of servers.
     *
     * @param servers The servers to assert.
     */
    public static void assertActivatingServers(List<IDataSource> servers)
    {
        assertEquals(2, servers.size());
        boolean somehostFound = false;
        boolean wfsHostFound = false;

        for (IDataSource server : servers)
        {
            if (server.getName().equals("somehost"))
            {
                somehostFound = true;
            }
            else if (server.getName().equals(ourWfsHost))
            {
                wfsHostFound = true;
            }
        }

        assertTrue(somehostFound);
        assertTrue(wfsHostFound);
    }

    /**
     * Creates an easy mocked ServerSourceControllerManager.
     *
     * @param controller The server controller returned by the manager.
     * @return The server controller.
     */
    public static ServerSourceControllerManager createControllerManager(ServerSourceController controller)
    {
        ServerSourceControllerManager controllerManager = EasyMock.createMock(ServerSourceControllerManager.class);
        controllerManager.getControllers();
        EasyMock.expectLastCall().andReturn(New.list(controller));

        return controllerManager;
    }

    /**
     * Creates a list of data sources for the source provider to return.
     *
     * @return The data sources.
     */
    public static List<IDataSource> createDataSources()
    {
        List<IDataSource> dataSources = New.list();

        OGCServerSource server = new OGCServerSource();
        server.setName("somehost");

        dataSources.add(server);

        server = new OGCServerSource();
        server.setName(ourWfsHost);

        dataSources.add(server);

        return dataSources;
    }

    /**
     * Creates an easy mocked server controller.
     *
     * @param provider The provider returned by the controller.
     * @param filterer The filterer returned by the controller.
     * @param activateAnswer The answer to use for the activate call.
     * @return The server controller.
     */
    public static ServerSourceController createServerController(ServerSourceProvider provider, ServerSourceFilterer filterer,
            IAnswer<Void> activateAnswer)
    {
        ServerSourceStateController controller = EasyMock.createMock(ServerSourceStateController.class);
        controller.getStateServerProvider();
        EasyMock.expectLastCall().andReturn(provider);

        if (filterer != null)
        {
            controller.getServerSourceFilterer();
            EasyMock.expectLastCall().andReturn(filterer);
            controller.addSource(EasyMock.isA(IDataSource.class));
            EasyMock.expectLastCall().andAnswer(new IAnswer<Void>()
            {
                @Override
                public Void answer()
                {
                    IDataSource source = (IDataSource)EasyMock.getCurrentArguments()[0];

                    assertEquals(ourWfsHost, source.getName());

                    return null;
                }
            });

            controller.activateSource(EasyMock.isA(IDataSource.class));

            if (activateAnswer != null)
            {
                EasyMock.expectLastCall().andAnswer(activateAnswer);
            }

            EasyMock.expectLastCall().times(2);
        }

        return controller;
    }

    /**
     * Creates an easy mocked ServerSourceFilterer.
     *
     * @return The source filterer.
     */
    @SuppressWarnings("unchecked")
    public static ServerSourceFilterer createSourceFilterer()
    {
        ServerSourceFilterer filterer = EasyMock.createMock(ServerSourceFilterer.class);
        filterer.getNonActiveServers(EasyMock.isA(List.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<List<IDataSource>>()
        {
            @Override
            public List<IDataSource> answer()
            {
                List<IDataSource> servers = (List<IDataSource>)EasyMock.getCurrentArguments()[0];

                IDataSource nonActive = null;

                assertActivatingServers(servers);

                for (IDataSource server : servers)
                {
                    if (server.getName().equals("somehost"))
                    {
                        nonActive = server;
                        break;
                    }
                }

                List<IDataSource> nonActives = New.list(nonActive);
                return nonActives;
            }
        });

        filterer.getNonAddedServers(EasyMock.isA(List.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<List<IDataSource>>()
        {
            @Override
            public List<IDataSource> answer()
            {
                List<IDataSource> servers = (List<IDataSource>)EasyMock.getCurrentArguments()[0];

                IDataSource nonAdded = null;

                assertActivatingServers(servers);

                for (IDataSource server : servers)
                {
                    if (server.getName().equals(ourWfsHost))
                    {
                        nonAdded = server;
                        break;
                    }
                }

                List<IDataSource> nonAddeds = New.list(nonAdded);
                return nonAddeds;
            }
        });

        filterer.findBusyServers(EasyMock.isA(IActivationListener.class), EasyMock.isA(List.class));

        return filterer;
    }

    /**
     * Creates an easy mocked ServerSourceProvider.
     *
     * @param node The state node expected to be passed to the provider.
     * @return The server source provider.
     * @throws XPathExpressionException Bad path.
     */
    public static ServerSourceProvider createSourceProvider(Node node) throws XPathExpressionException
    {
        ServerSourceProvider provider = EasyMock.createMock(ServerSourceProvider.class);
        provider.getServersInNode(EasyMock.eq(node));

        Node layerNode = StateXML.getChildNode(node, "/" + ModuleStateController.STATE_QNAME + "/:layers/:layer");

        if (layerNode != null)
        {
            EasyMock.expectLastCall().andReturn(createDataSources());
        }
        else
        {
            EasyMock.expectLastCall().andReturn(new ArrayList<IDataSource>());
        }

        return provider;
    }

    /**
     * Not constructible.
     */
    private ServerActivatorUtils()
    {
    }
}
