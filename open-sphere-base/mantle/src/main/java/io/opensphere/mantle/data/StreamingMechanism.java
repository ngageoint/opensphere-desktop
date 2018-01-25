package io.opensphere.mantle.data;

/**
 * An enumeration over the set of available streaming types supported by the
 * remote server.
 */
public enum StreamingMechanism
{
    /**
     * The enum type corresponding to the client repeatedly polling the server
     * for new data.
     */
    CLIENT_POLL,

    /**
     * The enum type corresponding to a remote connection to a persistent
     * WebSocket to do event-based receipt of remote data.
     */
    WEBSOCKET;
}
