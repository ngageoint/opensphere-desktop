package io.opensphere.server.serverprovider;

import java.net.MalformedURLException;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.common.connection.ServerConfiguration;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.server.services.ServerConfigEvent;
import io.opensphere.server.services.ServerConfigEvent.ServerEventAction;
import io.opensphere.server.services.ServerConnectionParams;

/**
 * Tests the ServerConfigEventHandler.
 *
 */
public class ServerConfigEventHandlerTest
{
    /**
     * The expected host.
     */
    private static final String ourHost = "host";

    /**
     * The expected protocol.
     */
    private static final String ourProtocol = "protocol";

    /**
     * The expected port.
     */
    private static final int ourPort = 10;

    /**
     * The expected read timeout.
     */
    private static final int ourReadTimeout = 100;

    /**
     * The expected connection timeout.
     */
    private static final int ourConnectTimeout = 200;

    /**
     * Tests handling the activate event.
     *
     * @throws MalformedURLException Exception.
     */
    @Test
    public void test() throws MalformedURLException
    {
        EasyMockSupport support = new EasyMockSupport();

        EventManager eventManager = createEventManager(support);
        ServerProvider<HttpServer> provider = createServerProvider(support);
        ServerConfigEvent event = createEvent(support, ServerEventAction.ACTIVATE);
        ServerConfigEvent nonActivateEvent = createEvent(support, ServerEventAction.LOADCOMPLETE);

        support.replayAll();

        ServerConfigEventHandler handler = new ServerConfigEventHandler(eventManager, provider);

        handler.notify(event);
        handler.notify(nonActivateEvent);

        support.verifyAll();
    }

    /**
     * Creates the server provider.
     *
     * @param support Used to create the mock.
     * @return The server provider.
     * @throws MalformedURLException Exception.
     */
    private ServerProvider<HttpServer> createServerProvider(EasyMockSupport support) throws MalformedURLException
    {
        HttpServer server = support.createMock(HttpServer.class);
        server.setTimeouts(EasyMock.eq(ourReadTimeout), EasyMock.eq(ourConnectTimeout));

        @SuppressWarnings("unchecked")
        ServerProvider<HttpServer> provider = support.createMock(ServerProvider.class);
        provider.getServer(EasyMock.cmpEq(ourHost), EasyMock.cmpEq(ourProtocol), EasyMock.eq(ourPort));
        EasyMock.expectLastCall().andReturn(server);

        return provider;
    }

    /**
     * Creates the event manager.
     *
     * @param support Used to create the mock.
     * @return The event manager.
     */
    private EventManager createEventManager(EasyMockSupport support)
    {
        EventManager manager = support.createMock(EventManager.class);
        manager.subscribe(EasyMock.eq(ServerConfigEvent.class), EasyMock.isA(ServerConfigEventHandler.class));

        return manager;
    }

    /**
     * Creates the event.
     *
     * @param support Used to create the ServerConnectionParams within the
     *            event.
     * @param action The event action.
     * @return The event.
     */
    private ServerConfigEvent createEvent(EasyMockSupport support, ServerEventAction action)
    {
        ServerConnectionParams params = null;
        if (action == ServerEventAction.ACTIVATE)
        {
            ServerConfiguration configuration = new ServerConfiguration();
            configuration.setHost(ourHost);
            configuration.setProtocol(ourProtocol);
            configuration.setPort(ourPort);
            configuration.setReadTimeout(ourReadTimeout);
            configuration.setConnectTimeout(ourConnectTimeout);

            params = support.createMock(ServerConnectionParams.class);
            params.getServerConfiguration();
            EasyMock.expectLastCall().andReturn(configuration);
        }

        ServerConfigEvent event = new ServerConfigEvent(toString(), params, action);

        return event;
    }
}
