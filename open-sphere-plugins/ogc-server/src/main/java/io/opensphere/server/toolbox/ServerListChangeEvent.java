package io.opensphere.server.toolbox;

import io.opensphere.server.services.ServerConnectionParams;

/**
 * The Interface ServerListChangeEvent to inform clients when a ServerSource has
 * been added or removed.
 */
public class ServerListChangeEvent
{
    /** Payload defining the server's connection information. */
    private ServerConnectionParams myPayload;

    /** The change state of the server. */
    private ServerState myState;

    /**
     * Instantiates a new server list change event.
     *
     * @param payload the connection payload
     * @param state the server's state
     */
    public ServerListChangeEvent(ServerConnectionParams payload, ServerState state)
    {
        myPayload = payload;
        myState = state;
    }

    /**
     * Gets the payload.
     *
     * @return the payload
     */
    public ServerConnectionParams getPayload()
    {
        return myPayload;
    }

    /**
     * Gets the state.
     *
     * @return the state
     */
    public ServerState getState()
    {
        return myState;
    }

    /**
     * Sets the payload.
     *
     * @param payload the new payload
     */
    public void setPayload(ServerConnectionParams payload)
    {
        myPayload = payload;
    }

    /**
     * Sets the state.
     *
     * @param state the new state
     */
    public void setState(ServerState state)
    {
        myState = state;
    }

    /**
     * Enum defining the ServerState.
     */
    public enum ServerState
    {
        /** The specified Server was added. */
        ADDED,

        /** The specified server's layer list was updated. */
        UPDATED,

        /** The specified server was removed from the configuration. */
        REMOVED,
    }
}
