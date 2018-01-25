package io.opensphere.core.server;

/**
 * Interface to an object interested in being notified when a server is added to
 * a {@link ServerProvider}.
 *
 * @param <T> The type of the server.
 */
public interface ServerChangedListener<T>
{
    /**
     * Called when a server is added to the server provider.
     *
     * @param server The server that was added.
     * @return True if activation was successful, false otherwise.
     */
    boolean serverAdded(T server);

    /**
     * Called when a server is removed from the server provider.
     *
     * @param server The server that was removed.
     */
    void serverRemoved(T server);
}
