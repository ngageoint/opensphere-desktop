package io.opensphere.server.services;

import io.opensphere.core.event.AbstractSingleStateEvent;

/**
 * An event on a layer.
 */
public class ServerConfigEvent extends AbstractSingleStateEvent
{
    /** Action to take for this event. */
    private final ServerEventAction myEventAction;

    /** The server to configure. */
    private final ServerConnectionParams myServerConfig;

    /** The name of the source. */
    private final String mySourceName;

    /** Whether the action was successful. */
    private final boolean mySuccess;

    /**
     * Construct me.
     *
     * @param source The name of the source.
     * @param server Server configuration.
     * @param action action to take for this event.
     */
    public ServerConfigEvent(String source, ServerConnectionParams server, ServerEventAction action)
    {
        this(source, server, action, true);
    }

    /**
     * Construct me.
     *
     * @param source The name of the source.
     * @param server Server configuration.
     * @param action action to take for this event.
     * @param success Whether the action was successful
     */
    public ServerConfigEvent(String source, ServerConnectionParams server, ServerEventAction action, boolean success)
    {
        mySourceName = source;
        myServerConfig = server;
        myEventAction = action;
        mySuccess = success;
    }

    @Override
    public String getDescription()
    {
        return "An event indicating that a server has been added, activated, or deactivated.";
    }

    /**
     * Get the eventAction.
     *
     * @return the eventAction
     */
    public ServerEventAction getEventAction()
    {
        return myEventAction;
    }

    /**
     * Get the Server connection parameters.
     *
     * @return the server parameters
     */
    public ServerConnectionParams getServer()
    {
        return myServerConfig;
    }

    /**
     * Get the source name.
     *
     * @return The source name.
     */
    public String getSourceName()
    {
        return mySourceName;
    }

    /**
     * Gets the success.
     *
     * @return the success
     */
    public boolean isSuccess()
    {
        return mySuccess;
    }

    /** Type of action to take. */
    public enum ServerEventAction
    {
        /** Validate Server, but do not Activate. */
        VALIDATE,

        /** Activate server. */
        ACTIVATE,

        /** Deactivate server. */
        DEACTIVATE,

        /** Server load is complete. */
        LOADCOMPLETE,

        /** Remove a server. */
        REMOVE,

        ;
    }
}
