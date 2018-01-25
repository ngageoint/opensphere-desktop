package io.opensphere.core.server;

import java.net.URL;

/**
 * Creates an object of type T that is used to communicate with a specified
 * server.
 *
 * @param <T> The type of the object used to communicate with a server.
 */
public interface ServerCreator<T>
{
    /**
     * Gets the object to communicate with the server at the specified url.
     *
     * @param url The url to the server.
     * @return The object to communicate with the server.
     */
    T createServer(URL url);
}
