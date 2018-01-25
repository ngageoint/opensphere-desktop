package io.opensphere.wps.request;

/**
 * An enumeration over the set of execution modes available to the user.
 */
public enum WpsExecutionMode
{
    /**
     * The execution mode used when a user wants to execute the process without saving the configuration.
     */
    RUN_ONCE,

    /**
     * The execution mode used when a user wants to both execute the process and save the configuration.
     */
    SAVE_AND_RUN,

    /**
     * The execution mode used when a user only wants to save the configuration.
     */
    SAVE;
}
