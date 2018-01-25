package io.opensphere.mantle.plugin.queryregion;

/**
 * The listener interface for receiving queryRegion events.
 */
public interface QueryRegionListener
{
    /**
     * All queries removed.
     *
     * @param animationPlanCancelled true if the animation plan was cancelled,
     *            causing this remove all queries.
     */
    void allQueriesRemoved(boolean animationPlanCancelled);

    /**
     * Query region added.
     *
     * @param region the region that was added
     */
    void queryRegionAdded(QueryRegion region);

    /**
     * Query region removed.
     *
     * @param region the region that was removed
     */
    void queryRegionRemoved(QueryRegion region);
}
