package io.opensphere.core.server;

/**
 * Interface to a server provider that allows the adding and removal of servers
 * from an outside component.
 *
 * @param <T> The type of the server.
 */
public interface ModifiableServerProvider<T> extends ServerProvider<T>
{
    /**
     * Adds the server.
     *
     * @param server The server to add.
     */
    void addServer(T server);

    /**
     * Removes the server.
     *
     * @param server The server to add.
     */
    void removeServer(T server);
}
