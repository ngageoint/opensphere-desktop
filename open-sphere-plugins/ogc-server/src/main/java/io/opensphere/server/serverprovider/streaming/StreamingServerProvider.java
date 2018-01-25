package io.opensphere.server.serverprovider.streaming;

import java.net.URL;
import java.util.Collections;
import java.util.Map;

import io.opensphere.core.server.ModifiableServerProvider;
import io.opensphere.core.server.ServerChangedListener;
import io.opensphere.core.server.StreamingServer;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ThreeTuple;
import io.opensphere.core.util.net.UrlUtilities;

/**
 * Provides {@link StreamingServer}s when available and notifies listeners when
 * streaming servers are added.
 */
public class StreamingServerProvider implements ModifiableServerProvider<StreamingServer>
{
    /**
     * The delimiter that separates individual parts to make a key uniquely
     * identifying a server.
     */
    private static final char ourKeyDelimiter = ':';

    /**
     * Helps in maintaining and notifying the added listeners.
     */
    private final WeakChangeSupport<ServerChangedListener<StreamingServer>> myChangeSupport = new WeakChangeSupport<>();

    /**
     * The collection of active servers who can stream data.
     */
    private final Map<String, StreamingServer> myServers = Collections.synchronizedMap(New.<String, StreamingServer>map());

    @Override
    public void addServer(StreamingServer server)
    {
        String url = server.getURL();
        ThreeTuple<String, String, Integer> protoHostPort = UrlUtilities.getProtocolHostPort(url, 80);
        String key = createServerKey(protoHostPort.getSecondObject(), protoHostPort.getThirdObject().intValue());

        myServers.put(key, server);

        notifyServerAdded(server);
    }

    @Override
    public void addServerChangedListener(ServerChangedListener<StreamingServer> listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public void clearServers()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public StreamingServer getServer(String host, String protocol, int port)
    {
        String key = createServerKey(host, port);

        return myServers.get(key);
    }

    @Override
    public StreamingServer getServer(URL url)
    {
        int port = url.getPort();
        if (port < 0)
        {
            port = 80;
        }

        return getServer(url.getHost(), url.getProtocol(), port);
    }

    @Override
    public void removeServer(StreamingServer server)
    {
        String url = server.getURL();
        ThreeTuple<String, String, Integer> protoHostPort = UrlUtilities.getProtocolHostPort(url, 80);
        String key = createServerKey(protoHostPort.getSecondObject(), protoHostPort.getThirdObject().intValue());

        StreamingServer removedServer = myServers.remove(key);
        if (removedServer != null)
        {
            notifyServerRemoved(removedServer);
        }
    }

    @Override
    public void removeServerChangedListener(ServerChangedListener<StreamingServer> listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Creates a server key used to access security configuration stuff.
     *
     * @param host The host to connect to.
     * @param port The port to connect to.
     * @return The server key used to access security configurations.
     */
    private String createServerKey(String host, int port)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(host).append(ourKeyDelimiter).append(port);

        return builder.toString();
    }

    /**
     * Notifies listeners of added server.
     *
     * @param server The added server.
     */
    private void notifyServerAdded(final StreamingServer server)
    {
        myChangeSupport.notifyListeners(new Callback<ServerChangedListener<StreamingServer>>()
        {
            @Override
            public void notify(ServerChangedListener<StreamingServer> listener)
            {
                listener.serverAdded(server);
            }
        });
    }

    /**
     * Notifies listeners of removed server.
     *
     * @param server The removed server.
     */
    private void notifyServerRemoved(final StreamingServer server)
    {
        myChangeSupport.notifyListeners(new Callback<ServerChangedListener<StreamingServer>>()
        {
            @Override
            public void notify(ServerChangedListener<StreamingServer> listener)
            {
                listener.serverRemoved(server);
            }
        });
    }
}
