package io.opensphere.server.serverprovider.http.factory;

import com.bitsys.common.http.client.HttpClient;

/**
 * Configures the connection pool settings of the HttpClient object.
 *
 */
public class ConnectionPoolConfigurer
{
    /**
     * The number of connections per route.
     */
    static final int ourConnectionsPerRoute = 100;

    /**
     * The max number of connections.
     */
    static final int ourMaxConnections = 300;

    /**
     * Whether to allow circular redirects.
     */
    static final boolean ourAllowCircularRedirects = true;

    /**
     * Configures the connection pool settings of the HttpClient object.
     *
     * @param client The client to configure.
     */
    public void configure(HttpClient client)
    {
        client.getOptions().setMaxConnectionsPerRoute(ourConnectionsPerRoute);
        client.getOptions().setMaxConnections(ourMaxConnections);
        client.getOptions().setAllowCircularRedirects(ourAllowCircularRedirects);
    }
}
