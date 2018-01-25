package io.opensphere.server.serverprovider;

import io.opensphere.core.Toolbox;
import io.opensphere.core.server.HttpServer;

/**
 * Interface to a server factory that builds HttpServers.
 *
 */
@FunctionalInterface
public interface ServerFactory
{
    /**
     * Creates an HttpServer that communicates with the specified host and port.
     *
     * @param securityProvider Provides different security components used to
     *            build the HttpServer.
     * @param protocol The protocol to use.
     * @param host The host.
     * @param port The port.
     * @param serverKey The server key.
     * @param toolbox The system toolbox.
     * @return The newly create HttpServer.
     */
    HttpServer createServer(SecurityComponentsProvider securityProvider, String protocol, String host, int port, String serverKey,
            Toolbox toolbox);
}
