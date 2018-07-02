package io.opensphere.core.util;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.apache.log4j.Logger;

/**
 * Utility for measuring a rate.
 */
@ThreadSafe
public class RateMeter implements AutoCloseable
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(RateMeter.class);

    /** The callback to be notified when a sample is taken, with the results. */
    private final Callback myCallback;

    /** The count since the last sample. */
    @GuardedBy("this")
    private long myCountSinceLastSample;

    /** Nanoseconds since Java epoch when the count was first incremented. */
    @GuardedBy("this")
    private long myFirstTime;

    /**
     * The future used to cancel a repeating task used to update the rate, only
     * used if {@link #open(ScheduledExecutorService)} is called.
     */
    private volatile ScheduledFuture<?> myFuture;

    /** Nanoseconds since Java epoch when the last sample was taken. */
    @GuardedBy("this")
    private long myLastTime;

    /** How often samples should be taken. */
    private final long myMinTimeBetweenSamples;

    /** The number of frames drawn since the meter was started. */
    @GuardedBy("this")
    private long myTotalCount;

    /**
     * Create the meter.
     *
     * @param minSecondsBetweenSamples The minimum amount of time between
     *            samples.
     * @param cb The object to be called when the frame rate is measured.
     */
    public RateMeter(double minSecondsBetweenSamples, Callback cb)
    {
        myMinTimeBetweenSamples = (long)(minSecondsBetweenSamples * Constants.NANO_PER_UNIT);
        myCallback = cb;
    }

    @Override
    public void close()
    {
        ScheduledFuture<?> task = myFuture;
        if (task != null)
        {
            task.cancel(false);
        }
    }

    /** Increment the count by one. */
    public void increment()
    {
        increment(1);
    }

    /**
     * Increment the count.
     *
     * @param amount The amount to increment the count.
     */
    public synchronized void increment(int amount)
    {
        if (myFirstTime == 0)
        {
            long now = System.nanoTime();
            myFirstTime = now;
            myLastTime = now;
        }
        else if (myFuture == null)
        {
            long now = System.nanoTime();
            if (now - myLastTime > myMinTimeBetweenSamples)
            {
                report(now);
            }
        }
        myCountSinceLastSample += amount;
        myTotalCount += amount;
    }

    /**
     * Schedule a repeating task on the given executor to report the rate.
     *
     * @param executor The executor.
     */
    public void open(ScheduledExecutorService executor)
    {
        myFuture = executor.scheduleAtFixedRate(() -> report(System.nanoTime()), 0L, myMinTimeBetweenSamples,
                TimeUnit.NANOSECONDS);
    }

    /**
     * Report the rate to the callback.
     *
     * @param now The current time in nanos.
     */
    private synchronized void report(long now)
    {
        double instant = (double)myCountSinceLastSample * Constants.NANO_PER_UNIT / (now - myLastTime);
        double average = (double)myTotalCount * Constants.NANO_PER_UNIT / (now - myFirstTime);
        myCountSinceLastSample = 0;
        myLastTime = now;
        try
        {
            myCallback.rateSampled(instant, average);
        }
        catch (RuntimeException | Error e)
        {
            LOGGER.error(e, e);
        }
    }

    /**
     * Interface called when the frame rate is sampled.
     */
    @FunctionalInterface
    public interface Callback
    {
        /**
         * Method called when the rate is sampled.
         *
         * @param instant The instant rate (Hertz).
         * @param average The average rate (Hertz) since the meter was started.
         */
        void rateSampled(double instant, double average);
    }
}
