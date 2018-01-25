package io.opensphere.server.serverprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.NetworkConfigurationManager;
import io.opensphere.core.SystemToolbox;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.server.HttpServer;
import io.opensphere.server.services.ServerConfigEvent;

/**
 * Tests the HttpServerProvider class.
 *
 */
public class HttpServerProviderTest
{
    /**
     * The expected host.
     */
    private static final String ourHost = "host";

    /**
     * The expected protocol.
     */
    private static final String ourProtocol = "https";

    /**
     * Tests the createServer function.
     *
     * @throws IOException Bad IO.
     * @throws GeneralSecurityException Bad security.
     */
    @Test
    public void testCreateServer() throws GeneralSecurityException, IOException
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support);

        HttpServer server = support.createMock(HttpServer.class);
        HttpServer storedServer = support.createMock(HttpServer.class);

        int port = 1000;
        String serverKey = ourHost + ':' + port;

        ServerFactory factory = createFactory(support, port, serverKey, toolbox, server);
        EasyMock.expect(factory.createServer(EasyMock.isA(SecurityComponentsProviderImpl.class), EasyMock.cmpEq(ourProtocol),
                EasyMock.cmpEq(ourHost), EasyMock.eq(port), EasyMock.cmpEq(serverKey), EasyMock.eq(toolbox)))
                .andReturn(storedServer);

        support.replayAll();

        URL url = new URL(ourProtocol + "://" + ourHost + ":" + port);

        HttpServerProvider provider = new HttpServerProvider(toolbox, factory);
        HttpServer actualServer = provider.createServer(url);

        assertEquals(server, actualServer);

        HttpServer actualStoredServer = provider.getServer(url);
        assertEquals(storedServer, actualStoredServer);

        support.verifyAll();
    }

    /**
     * Tests getting a server that is already created.
     *
     * @throws IOException Bad IO.
     * @throws GeneralSecurityException Bad security.
     */
    @Test
    public void testGetServerAlreadyCreated() throws GeneralSecurityException, IOException
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support);
        HttpServer server = support.createMock(HttpServer.class);

        int port = 1000;
        String serverKey = ourHost + ':' + port;

        ServerFactory factory = createFactory(support, port, serverKey, toolbox, server);

        support.replayAll();

        URL url = new URL(ourProtocol + "://" + ourHost + ":" + port);

        HttpServerProvider provider = new HttpServerProvider(toolbox, factory);
        HttpServer actualServer = provider.getServer(url);

        assertEquals(server, actualServer);
        HttpServer sameServer = provider.getServer(url);

        assertSame(actualServer, sameServer);

        support.verifyAll();
    }

    /**
     * Tests getting the server with a url.
     *
     * @throws IOException Bad IO.
     * @throws GeneralSecurityException Bad security.
     */
    @Test
    public void testGetServerURL() throws GeneralSecurityException, IOException
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support);

        HttpServer server = support.createMock(HttpServer.class);

        int port = 1000;
        String serverKey = ourHost + ':' + port;

        ServerFactory factory = createFactory(support, port, serverKey, toolbox, server);

        support.replayAll();

        URL url = new URL(ourProtocol + "://" + ourHost + ":" + port);

        HttpServerProvider provider = new HttpServerProvider(toolbox, factory);
        HttpServer actualServer = provider.getServer(url);

        assertEquals(server, actualServer);

        support.verifyAll();
    }

    /**
     * Tests getting the server with a url and no port.
     *
     * @throws IOException Bad IO.
     * @throws GeneralSecurityException Bad security.
     */
    @Test
    public void testGetServerURLNoPort() throws GeneralSecurityException, IOException
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support);
        HttpServer server = support.createMock(HttpServer.class);

        URL url = new URL(ourProtocol + "://" + ourHost);
        int port = url.getDefaultPort();
        String serverKey = ourHost + ":" + port;

        ServerFactory factory = createFactory(support, port, serverKey, toolbox, server);

        support.replayAll();

        HttpServerProvider provider = new HttpServerProvider(toolbox, factory);
        HttpServer actualServer = provider.getServer(url);

        assertEquals(server, actualServer);
        assertTrue(-1 != port);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked server factory.
     *
     * @param support Used to create the mock.
     * @param port The expected port.
     * @param serverKey The expected server key.
     * @param toolbox The expected toolbox.
     * @param server The server to return.
     * @return The server factory.
     * @throws GeneralSecurityException Bad security.
     * @throws IOException Bad IO.
     */
    private ServerFactory createFactory(EasyMockSupport support, int port, String serverKey, Toolbox toolbox, HttpServer server)
        throws GeneralSecurityException, IOException
    {
        ServerFactory factory = support.createMock(ServerFactory.class);

        factory.createServer(EasyMock.isA(SecurityComponentsProviderImpl.class), EasyMock.cmpEq(ourProtocol),
                EasyMock.cmpEq(ourHost), EasyMock.eq(port), EasyMock.cmpEq(serverKey), EasyMock.eq(toolbox));
        EasyMock.expectLastCall().andReturn(server);

        return factory;
    }

    /**
     * Creates the system toolbox.
     *
     * @param support Used to create the mock.
     * @return The toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support)
    {
        NetworkConfigurationManager netconfigManager = support.createMock(NetworkConfigurationManager.class);
        netconfigManager.addChangeListener(EasyMock.isA(NetworkConfigurationManager.NetworkConfigurationChangeListener.class));

        SystemToolbox systemToolbox = support.createMock(SystemToolbox.class);
        EasyMock.expect(systemToolbox.getNetworkConfigurationManager()).andReturn(netconfigManager);

        EventManager eventManager = support.createMock(EventManager.class);
        eventManager.subscribe(EasyMock.eq(ServerConfigEvent.class), EasyMock.isA(ServerConfigEventHandler.class));

        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getSystemToolbox()).andReturn(systemToolbox);

        toolbox.getEventManager();
        EasyMock.expectLastCall().andReturn(eventManager);

        return toolbox;
    }
}
