package io.opensphere.server.event;

import io.opensphere.core.event.AbstractSingleStateEvent;

/**
 * An event to describe if an individual OGC server connection was successful or
 * not.
 */
public class OverallServerStatusEvent extends AbstractSingleStateEvent
{
    /** The server status. */
    private final OverallServerStatus myStatus;

    /**
     * Constructor.
     *
     * @param status The overall server status.
     */
    public OverallServerStatusEvent(OverallServerStatus status)
    {
        myStatus = status;
    }

    @Override
    public String getDescription()
    {
        return "An event indicating that server plugins have completed loading or timed out.";
    }

    /**
     * Get the overall status of the active servers.
     *
     * @return The server status.
     */
    public OverallServerStatus getStatus()
    {
        return myStatus;
    }

    /** Enum indicating what the overall server status is. */
    public enum OverallServerStatus
    {
        /** All active servers are reporting good status. */
        GOOD,

        /** Some servers failed, but some are okay. */
        DEGRADED,

        /** All server activations failed. */
        FAILED,

        /** Server status is unknown. */
        UNKNOWN,
    }
}
