package io.opensphere.core.server;

import java.net.URL;

/**
 * Creates an object of type T that is used to communicate with a specified
 * server.
 *
 * @param <T> The type of the object used to communicate with a server.
 */
public interface ServerProvider<T>
{
    /**
     * Adds a server changed listener.
     *
     * @param listener The listener to add.
     */
    void addServerChangedListener(ServerChangedListener<T> listener);

    /**
     * Clear the cached servers in the provider.
     */
    void clearServers();

    /**
     * Gets the object to communicate with the specified server.
     *
     * @param host The host.
     * @param protocol The protocol to use.
     * @param port The port to connect to.
     * @return The object to communicate with the server.
     */
    T getServer(String host, String protocol, int port);

    /**
     * Gets the object to communicate with the server at the specified url.
     *
     * @param url The url to the server.
     * @return The object to communicate with the server.
     */
    T getServer(URL url);

    /**
     * Removes the server changed listener.
     *
     * @param listener The listener to remove.
     */
    void removeServerChangedListener(ServerChangedListener<T> listener);
}
