package io.opensphere.server.toolbox;

/**
 * The Interface ServerRefreshController.
 */
public interface ServerRefreshController
{
    /**
     * Gets the interval in minutes between refresh commands being sent.
     *
     * @return the refresh interval in minutes
     */
    int getRefreshInterval();

    /**
     * Checks if server refreshes are enabled.
     *
     * @return true, if refreshes are enabled
     */
    boolean isRefreshEnabled();

    /**
     * Restore defaults.
     */
    void restoreDefaults();

    /**
     * Enable/Disable server refreshes.
     *
     * @param isEnabled true to enable server refreshes, false otherwise
     */
    void setRefreshEnabled(boolean isEnabled);

    /**
     * Set the interval in minutes between sending refresh commands.
     *
     * @param interval the new refresh interval in minutes
     */
    void setRefreshInterval(int interval);
}
