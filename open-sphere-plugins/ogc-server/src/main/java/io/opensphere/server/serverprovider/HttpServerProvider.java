package io.opensphere.server.serverprovider;

import java.net.URL;
import java.util.Map;

import io.opensphere.core.NetworkConfigurationManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ServerChangedListener;
import io.opensphere.core.server.ServerCreator;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;

/**
 * A server provider that provides HttpServer objects to the system.
 *
 */
public class HttpServerProvider implements ServerProvider<HttpServer>, ServerCreator<HttpServer>
{
    /**
     * The delimiter used to seperate unique values of an HttpServer.
     */
    private static final char ourKeyDelimiter = ':';

    /**
     * The factory to use to create the HttpServer objects.
     */
    private final ServerFactory myFactory;

    /**
     * Handles server configuration events.
     */
    private final ServerConfigEventHandler myHandler;

    /**
     * Contains the registered server added listeners.
     */
    private final WeakChangeSupport<ServerChangedListener<HttpServer>> myServerAddedChangeSupport = new WeakChangeSupport<>();

    /**
     * The map of servers that have already been created and configured.
     */
    private final Map<String, HttpServer> myServers = New.map();

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Listener for changes to the network configuration.
     */
    private final NetworkConfigurationManager.NetworkConfigurationChangeListener myNetworkConfigurationChangeListener = this::clearServers;

    /**
     * Constructs a new HttpServerProvider.
     *
     * @param toolbox The system toolbox.
     * @param factory The server factory to use to create HttpServers.
     */
    public HttpServerProvider(Toolbox toolbox, ServerFactory factory)
    {
        myToolbox = toolbox;
        myFactory = factory;
        myHandler = new ServerConfigEventHandler(toolbox.getEventManager(), this);

        myToolbox.getSystemToolbox().getNetworkConfigurationManager().addChangeListener(myNetworkConfigurationChangeListener);
    }

    @Override
    public void addServerChangedListener(ServerChangedListener<HttpServer> listener)
    {
        myServerAddedChangeSupport.addListener(listener);
    }

    @Override
    public synchronized void clearServers()
    {
        myServers.clear();
    }

    /**
     * Closes the provider.
     */
    public void close()
    {
        myHandler.close();
        myToolbox.getSystemToolbox().getNetworkConfigurationManager().removeChangeListener(myNetworkConfigurationChangeListener);
    }

    @Override
    public HttpServer createServer(URL url)
    {
        int port = url.getPort() <= 0 ? url.getDefaultPort() : url.getPort();
        return createServer(url.getHost(), url.getProtocol(), port);
    }

    @Override
    public synchronized HttpServer getServer(String host, String protocol, int port)
    {
        String connectionKey = createConnectionKey(protocol, host, port);

        if (!myServers.containsKey(connectionKey))
        {
            HttpServer server = createServer(host, protocol, port);
            myServers.put(connectionKey, server);
            notifyListeners(server);
        }

        return myServers.get(connectionKey);
    }

    @Override
    public HttpServer getServer(URL url)
    {
        // Extract port for post
        int port = url.getPort() <= 0 ? url.getDefaultPort() : url.getPort();
        return getServer(url.getHost(), url.getProtocol(), port);
    }

    @Override
    public void removeServerChangedListener(ServerChangedListener<HttpServer> listener)
    {
        myServerAddedChangeSupport.removeListener(listener);
    }

    /**
     * Creates a connection key to map the newly created HttpServer.
     *
     * @param protocol The protocol being used to connect to the server.
     * @param host The host.
     * @param port The port.
     * @return The unique key for an HttpServer.
     */
    private String createConnectionKey(String protocol, String host, int port)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(protocol).append(ourKeyDelimiter).append(host).append(ourKeyDelimiter).append(port);

        return sb.toString();
    }

    /**
     * Creates an {@link HttpServer}.
     *
     * @param host The host to create the server for.
     * @param protocol The protocol to use.
     * @param port The port.
     * @return The newly created server.
     */
    private HttpServer createServer(String host, String protocol, int port)
    {
        SecurityComponentsProviderImpl securityProvider = new SecurityComponentsProviderImpl();
        String serverKey = createServerKey(host, port);
        HttpServer server = myFactory.createServer(securityProvider, protocol, host, port, serverKey, myToolbox);
        return server;
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
     * Notifies the listeners of the added server.
     *
     * @param server The server that was added.
     */
    private void notifyListeners(final HttpServer server)
    {
        myServerAddedChangeSupport.notifyListeners(new Callback<ServerChangedListener<HttpServer>>()
        {
            @Override
            public void notify(ServerChangedListener<HttpServer> listener)
            {
                listener.serverAdded(server);
            }
        });
    }
}
