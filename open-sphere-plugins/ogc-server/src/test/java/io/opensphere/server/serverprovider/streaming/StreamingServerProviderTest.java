package io.opensphere.server.serverprovider.streaming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.MalformedURLException;
import java.net.URL;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.server.ServerChangedListener;
import io.opensphere.core.server.StreamingServer;

/**
 * Tests the {@link StreamingServerProvider} class.
 */
public class StreamingServerProviderTest
{
    /**
     * The expected server url.
     */
    private static String ourUrl = "http://somehost";

    /**
     * Tests adding a server, getting the server and removing the server, and
     * checks to make sure listeners are notified.
     *
     * @throws MalformedURLException Bad URL.
     */
    @Test
    public void testAddServer() throws MalformedURLException
    {
        EasyMockSupport support = new EasyMockSupport();

        StreamingServer server = createServer(support);
        ServerChangedListener<StreamingServer> listener = createListener(support, server);

        support.replayAll();

        StreamingServerProvider provider = new StreamingServerProvider();
        provider.addServerChangedListener(listener);
        provider.addServer(server);

        assertEquals(server, provider.getServer(new URL(ourUrl)));

        provider.removeServer(server);

        assertNull(provider.getServer(new URL(ourUrl)));

        provider.removeServer(server);

        support.verifyAll();
    }

    /**
     * Tests removing listener.
     *
     * @throws MalformedURLException Bad Url.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveListener() throws MalformedURLException
    {
        EasyMockSupport support = new EasyMockSupport();

        StreamingServer server = createServer(support);
        ServerChangedListener<StreamingServer> listener = support.createMock(ServerChangedListener.class);

        support.replayAll();

        StreamingServerProvider provider = new StreamingServerProvider();
        provider.addServerChangedListener(listener);
        provider.removeServerChangedListener(listener);

        provider.addServer(server);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link ServerChangedListener}.
     *
     * @param support Used to create the mock.
     * @param server The expected server to be passed to the listener.
     * @return The {@link ServerChangedListener}.
     */
    private ServerChangedListener<StreamingServer> createListener(EasyMockSupport support, StreamingServer server)
    {
        @SuppressWarnings("unchecked")
        ServerChangedListener<StreamingServer> listener = support.createMock(ServerChangedListener.class);

        EasyMock.expect(Boolean.valueOf(listener.serverAdded(EasyMock.eq(server)))).andReturn(Boolean.TRUE);
        listener.serverRemoved(EasyMock.eq(server));

        return listener;
    }

    /**
     * Creates an easy mocked {@link StreamingServer}.
     *
     * @param support Used to create the mock.
     * @return The {@link StreamingServer}.
     * @throws MalformedURLException Bad URL.
     */
    private StreamingServer createServer(EasyMockSupport support) throws MalformedURLException
    {
        StreamingServer server = support.createMock(StreamingServer.class);

        EasyMock.expect(server.getURL()).andReturn(ourUrl).atLeastOnce();

        return server;
    }
}
