package io.opensphere.mantle.data.element.mdfilter;

/**
 * Interface to a custom filter object used to in state files.
 */
public interface CustomFilter
{
    /**
     * Gets the filter description.
     *
     * @return the filter description
     */
    String getFilterDescription();

    /**
     * Gets the filter type.
     *
     * @return the filter type
     */
    String getFilterType();

    /**
     * Get the id of the filter.
     *
     * @return The id.
     */
    String getFilterId();

    /**
     * Checks the match state. TRUE is match all(filters are anded).
     *
     * @return true, if is match
     */
    String getMatch();

    /**
     * Gets the server name.
     *
     * @return the server name
     */
    String getServerName();

    /**
     * Gets the title.
     *
     * @return the title
     */
    String getTitle();

    /**
     * Gets the url key.
     *
     * @return the url key
     */
    String getUrlKey();

    /**
     * Checks if is active.
     *
     * @return true, if is active
     */
    boolean isActive();

    /**
     * True if this is from a state.
     *
     * @return true, if is from a state
     */
    boolean isFromState();

    /**
     * Sets if the filter is from a state.
     *
     * @param isFromState if the filter is from a state
     */
    void setFromState(boolean isFromState);

    /**
     * Sets the active.
     *
     * @param isActive the new active
     */
    void setActive(boolean isActive);

    /**
     * Sets the filter description.
     *
     * @param filterDescription the new filter description
     */
    void setFilterDescription(String filterDescription);

    /**
     * Sets the filter type.
     *
     * @param filterType the new filter type
     */
    void setFilterType(String filterType);

    /**
     * Sets the match. TRUE is match all(filters are anded).
     *
     * @param match the new match
     */
    void setMatch(String match);

    /**
     * Sets the server name.
     *
     * @param serverName the new server name
     */
    void setServerName(String serverName);

    /**
     * Sets the title.
     *
     * @param title the new title
     */
    void setTitle(String title);

    /**
     * Sets the url key.
     *
     * @param urlKey the new url key
     */
    void setUrlKey(String urlKey);
}
