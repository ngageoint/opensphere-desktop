package io.opensphere.server.serverprovider;

import io.opensphere.core.common.connection.ServerConfiguration;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.server.services.ServerConfigEvent;
import io.opensphere.server.services.ServerConfigEvent.ServerEventAction;

/**
 * Responsds to ServerConfigEvents, and makes the appropriate changes.
 *
 */
public class ServerConfigEventHandler implements EventListener<ServerConfigEvent>
{
    /**
     * The server provider.
     */
    private final ServerProvider<HttpServer> myProvider;

    /**
     * The event manager.
     */
    private final EventManager myEventManager;

    /**
     * Constructs a new server config event handler.
     *
     * @param eventManager The event manager.
     * @param provider The server provider.
     */
    public ServerConfigEventHandler(EventManager eventManager, ServerProvider<HttpServer> provider)
    {
        myProvider = provider;
        myEventManager = eventManager;
        myEventManager.subscribe(ServerConfigEvent.class, this);
    }

    @Override
    public void notify(ServerConfigEvent event)
    {
        if (event.getEventAction() == ServerEventAction.ACTIVATE)
        {
            handleActivate(event);
        }
    }

    /**
     * Sets the timeouts when a server is activated.
     *
     * @param event The server activated timeout.
     */
    private void handleActivate(ServerConfigEvent event)
    {
        ServerConfiguration configuration = event.getServer().getServerConfiguration();

        HttpServer server = myProvider.getServer(configuration.getHost(), configuration.getProtocol(), configuration.getPort());
        server.setTimeouts(configuration.getReadTimeout(), configuration.getConnectTimeout());
    }

    /**
     * Unsubscribes from the event.
     */
    public void close()
    {
        myEventManager.unsubscribe(ServerConfigEvent.class, this);
    }
}
