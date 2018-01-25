package io.opensphere.wps.streaming;

import java.net.URL;

import io.opensphere.core.server.ModifiableServerProvider;
import io.opensphere.core.server.ServerChangedListener;
import io.opensphere.core.server.StreamingServer;

/**
 * A mock server provider used for testing.
 */
public class MockServerProvider implements ModifiableServerProvider<StreamingServer>
{
    /**
     * The added server.
     */
    private StreamingServer myAddedServer;

    @Override
    public void addServer(StreamingServer server)
    {
        myAddedServer = server;
    }

    @Override
    public void addServerChangedListener(ServerChangedListener<StreamingServer> listener)
    {
    }

    @Override
    public void clearServers()
    {
    }

    /**
     * Gets the added server.
     *
     * @return The added server.
     */
    public StreamingServer getAddedServer()
    {
        return myAddedServer;
    }

    @Override
    public StreamingServer getServer(String host, String protocol, int port)
    {
        return null;
    }

    @Override
    public StreamingServer getServer(URL url)
    {
        return null;
    }

    @Override
    public void removeServer(StreamingServer server)
    {
        myAddedServer = null;
    }

    @Override
    public void removeServerChangedListener(ServerChangedListener<StreamingServer> listener)
    {
    }
}
