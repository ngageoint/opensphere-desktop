package io.opensphere.kml.common.model;

import net.jcip.annotations.ThreadSafe;

/**
 * Tracks outcomes. This uses synchronization instead of atomic classes in order
 * to save memory.
 */
@ThreadSafe
public class OutcomeTracker
{
    /** The success count. */
    private int mySuccessCount;

    /** The consecutive failure count. */
    private int myConsecutiveFailureCount;

    /**
     * Increments the success count.
     */
    public synchronized void success()
    {
        mySuccessCount++;
        myConsecutiveFailureCount = 0;
    }

    /**
     * Increments the failure count.
     */
    public synchronized void failure()
    {
        myConsecutiveFailureCount++;
    }

    /**
     * Gets the success count.
     *
     * @return the success count
     */
    public synchronized int getSuccessCount()
    {
        return mySuccessCount;
    }

    /**
     * Gets the consecutive failure count.
     *
     * @return the consecutive failure count
     */
    public synchronized int getConsecutiveFailureCount()
    {
        return myConsecutiveFailureCount;
    }
}
