package io.opensphere.core.util.concurrent;

import java.util.Timer;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.opensphere.core.util.lang.NamedThreadFactory;

/**
 * Singleton that allows disparate classes to schedule future tasks using a
 * single thread.
 */
public final class CommonTimer
{
    /** An executor to use for scheduling tasks. */
    private static final ScheduledExecutorService EXECUTOR = new ReportingScheduledExecutorService(
            new ScheduledThreadPoolExecutor(2, new NamedThreadFactory("CommonTimer"),
                    SuppressableRejectedExecutionHandler.getInstance()));

    /**
     * Create a new procrastinating executor that uses the common timer thread.
     *
     * @param delayMilliseconds How long the executor procrastinates.
     * @return A procrastinating executor.
     */
    public static Executor createProcrastinatingExecutor(int delayMilliseconds)
    {
        return new ProcrastinatingExecutor(EXECUTOR, delayMilliseconds);
    }

    /**
     * Create a new procrastinating executor that uses the common timer thread.
     *
     * @param minDelayMS How long the executor procrastinates in milliseconds.
     * @param maxDelayMS The maximum procrastination in milliseconds.
     * @return A procrastinating executor.
     */
    public static Executor createProcrastinatingExecutor(int minDelayMS, int maxDelayMS)
    {
        return new ProcrastinatingExecutor(EXECUTOR, minDelayMS, maxDelayMS);
    }

    /**
     * Get the common timer executor.
     *
     * @return The executor.
     */
    public static ScheduledExecutorService getExecutor()
    {
        return EXECUTOR;
    }

    /** Initialize the timer. */
    public static void init()
    {
    }

    /**
     * Schedule a one-time task.
     *
     * @param task The task to be run.
     * @param delayMilliseconds The delay in milliseconds before the invocation.
     * @return A future.
     * @see Timer#schedule(java.util.TimerTask, long)
     */
    public static ScheduledFuture<?> schedule(Runnable task, long delayMilliseconds)
    {
        return EXECUTOR.schedule(task, delayMilliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedule a repeating task with a fixed delay between executions.
     *
     * @param task The task to be run.
     * @param delayMilliseconds The delay in milliseconds before the invocation.
     * @param periodMilliseconds The delay between executions.
     * @return A future.
     * @see Timer#schedule(java.util.TimerTask, long)
     */
    public static ScheduledFuture<?> schedule(Runnable task, long delayMilliseconds, long periodMilliseconds)
    {
        return EXECUTOR.scheduleWithFixedDelay(task, delayMilliseconds, periodMilliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedule a repeating task with a fixed rate. This version will attempt to
     * keep throughput constant by executing multiple tasks in a row to catch up
     * if it falls behind.
     *
     * @param task The task.
     * @param delayMilliseconds The delay in milliseconds before the first
     *            invocation.
     * @param periodMilliseconds The period between invocations.
     * @return A future.
     * @see Timer#scheduleAtFixedRate(java.util.TimerTask, long, long)
     */
    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long delayMilliseconds, long periodMilliseconds)
    {
        return EXECUTOR.scheduleAtFixedRate(task, delayMilliseconds, periodMilliseconds, TimeUnit.MILLISECONDS);
    }

    /** Disallow instantiation. */
    private CommonTimer()
    {
    }
}
