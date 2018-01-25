package io.opensphere.core.data;

import io.opensphere.core.data.util.QueryTracker;

/**
 * A query tracker that is mutable.
 */
public interface MutableQueryTracker extends QueryTracker
{
    /**
     * Set the result ids.
     *
     * @param ids The result ids.
     */
    void addIds(long[] ids);

    /**
     * Set the fraction complete.
     *
     * @param fractionComplete The fraction complete.
     */
    void setFractionComplete(float fractionComplete);

    /**
     * Set the query status. If the query status is not
     * {@link io.opensphere.core.data.util.QueryTracker.QueryStatus#RUNNING},
     * the fraction complete will also be set to 1.
     *
     * @param queryStatus The query status.
     * @param e An exception if one was encountered during the query, or
     *            {@code null}.
     */
    void setQueryStatus(QueryStatus queryStatus, Throwable e);

    /**
     * Get a {@link Runnable} that will wrap the input {@link Runnable}, and
     * also notifies this tracker that the runnable may be running on a new
     * thread. This should always be used when passing a tracker to a new
     * thread.
     *
     * @param r The wrapped runnable.
     * @return The result.
     */
    Runnable wrapRunnable(Runnable r);
}
