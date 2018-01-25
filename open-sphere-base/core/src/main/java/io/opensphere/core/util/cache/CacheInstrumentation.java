package io.opensphere.core.util.cache;

import org.apache.log4j.Logger;

/**
 * Cache instrumentation.
 */
class CacheInstrumentation
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(CacheInstrumentation.class);

    /** A start time in nanoseconds since epoch. */
    private long myStartTime;

    /** The total duration in microseconds. */
    private long myTotalDuration;

    /** The hit count. */
    private long myHitCount;

    /** The miss count. */
    private long myMissCount;

    /**
     * Starts timing.
     */
    public void start()
    {
        myStartTime = System.nanoTime();
    }

    /**
     * Records a hit.
     */
    public void hit()
    {
        stop();
        myHitCount++;
        logMessage();
    }

    /**
     * Records a miss.
     */
    public void miss()
    {
        stop();
        myMissCount++;
        logMessage();
    }

    /**
     * Logs a message.
     */
    public void logMessage()
    {
        if (getCount() % 1000 == 0)
        {
            long totalCount = getCount();
            double hitRatio = (double)myHitCount / totalCount;
            double averageDuration = (double)myTotalDuration / totalCount;

            StringBuilder msg = new StringBuilder(64);
            msg.append("Hit ratio: ").append(hitRatio);
            msg.append("  Average duration: ").append(averageDuration);
            LOGGER.info(msg.toString());
        }
    }

    /**
     * Stops timing.
     */
    private void stop()
    {
        long duration = System.nanoTime() - myStartTime;
        myTotalDuration += duration / 1000;
    }

    /**
     * Gets the count.
     *
     * @return the count
     */
    private long getCount()
    {
        return myHitCount + myMissCount;
    }
}
