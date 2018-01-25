package io.opensphere.core.util.lang;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * Collection of thread utilities.
 */
public final class ThreadUtilities
{
    /**
     * The CPU executor service. If this ever gets heavily used, it may be
     * better to use a regular fixed thread pool.
     */
    private static final ExecutorService CPU_EXECUTOR_SERVICE = newTerminatingFixedThreadPool(
            new NamedThreadFactory("CPU-Worker"));

    /** The I/O executor service. */
    private static final ExecutorService IO_EXECUTOR_SERVICE = Executors.newCachedThreadPool(new NamedThreadFactory("IO-Worker"));

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ThreadUtilities.class);

    /**
     * Get all active threads in the system.
     *
     * @return The active threads.
     */
    public static List<Thread> getActiveThreads()
    {
        ThreadGroup tg = getTopThreadGroup();
        Thread[] arr;

        int estimatedCount = tg.activeCount();
        int count = estimatedCount;
        do
        {
            estimatedCount = count + 1;
            arr = new Thread[estimatedCount];
            count = tg.enumerate(arr);
        }
        while (count == estimatedCount);

        return Arrays.asList(arr);
    }

    /**
     * Get the top-level thread group that contains the current thread.
     *
     * @return The top-level parent thread group.
     */
    public static ThreadGroup getTopThreadGroup()
    {
        return getTopThreadGroup(Thread.currentThread());
    }

    /**
     * Get the top-level thread group that contains the given thread.
     *
     * @param thread The thread.
     * @return The top-level parent thread group.
     */
    public static ThreadGroup getTopThreadGroup(Thread thread)
    {
        return getTopThreadGroup(thread.getThreadGroup());
    }

    /**
     * Get the top-level thread group that contains the given thread group.
     *
     * @param group The thread group.
     * @return The top-level parent thread group.
     */
    public static ThreadGroup getTopThreadGroup(ThreadGroup group)
    {
        ThreadGroup result = group;
        while (result.getParent() != null)
        {
            result = result.getParent();
        }
        return result;
    }

    /**
     * Gets the cpuExecutorService.
     *
     * @return the cpuExecutorService
     */
    public static ExecutorService getCpuExecutorService()
    {
        return CPU_EXECUTOR_SERVICE;
    }

    /**
     * Gets the ioExecutorService.
     *
     * @return the ioExecutorService
     */
    public static ExecutorService getIoExecutorService()
    {
        return IO_EXECUTOR_SERVICE;
    }

    /**
     * Run a task on a background thread. This is best suited for I/O or
     * long-lived operations.
     *
     * @param <T> The type returned by the future.
     * @param r The runnable.
     * @return A future representing the task.
     */
    public static <T> Future<T> runBackground(Callable<T> r)
    {
        return IO_EXECUTOR_SERVICE.submit(r);
    }

    /**
     * Run a task on a background thread. This is best suited for I/O or
     * long-lived operations.
     *
     * @param r The runnable.
     */
    public static void runBackground(Runnable r)
    {
        IO_EXECUTOR_SERVICE.execute(r);
    }

    /**
     * Run a task on a background thread. This is best suited for I/O or
     * long-lived operations.
     *
     * @param <T> The type returned by the future.
     * @param r The runnable.
     * @param result The result to return.
     * @return A future representing the task.
     */
    public static <T> Future<T> runBackground(Runnable r, T result)
    {
        return IO_EXECUTOR_SERVICE.submit(r, result);
    }

    /**
     * Run a short-lived CPU-bound task.
     *
     * @param <T> The type returned by the future.
     * @param r The runnable.
     * @return A future representing the task.
     */
    public static <T> Future<T> runCpu(Callable<T> r)
    {
        return CPU_EXECUTOR_SERVICE.submit(r);
    }

    /**
     * Run a short-lived CPU-bound task.
     *
     * @param r The runnable.
     */
    public static void runCpu(Runnable r)
    {
        CPU_EXECUTOR_SERVICE.execute(r);
    }

    /**
     * Run a short-lived CPU-bound task.
     *
     * @param <T> The type returned by the future.
     * @param r The runnable.
     * @param result The result to return.
     * @return A future representing the task.
     */
    public static <T> Future<T> runCpu(Runnable r, T result)
    {
        return CPU_EXECUTOR_SERVICE.submit(r, result);
    }

    /**
     * Suspend the current thread for some amount of time.
     *
     * @param sleepTime The sleep time in milliseconds.
     */
    public static void sleep(long sleepTime)
    {
        try
        {
            Thread.sleep(sleepTime);
        }
        catch (InterruptedException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Interrupted during sleep: " + e, e);
            }
        }
    }

    /**
     * Creates a new fixed thread pool, where the threads will eventually
     * terminate if not used.
     *
     * @param threadFactory the thread factory to use
     * @return the executor service
     */
    private static ExecutorService newTerminatingFixedThreadPool(ThreadFactory threadFactory)
    {
        int poolSize = Math.max(Runtime.getRuntime().availableProcessors(), 2);
        return newTerminatingFixedThreadPool(threadFactory, poolSize);
    }

    /**
     * Creates a new fixed thread pool, where the threads will eventually
     * terminate if not used.
     *
     * @param name The name used for the thread group
     * @param poolSize the pool size
     * @return the executor service
     */
    public static ExecutorService newTerminatingFixedThreadPool(String name, int poolSize)
    {
        return newTerminatingFixedThreadPool(new NamedThreadFactory(name), poolSize);
    }

    /**
     * Creates a new fixed thread pool, where the threads will eventually
     * terminate if not used.
     *
     * @param threadFactory the thread factory to use
     * @param poolSize the pool size
     * @return the executor service
     */
    public static ExecutorService newTerminatingFixedThreadPool(ThreadFactory threadFactory, int poolSize)
    {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(poolSize, poolSize, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), threadFactory);
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    /**
     * Disabled constructor.
     */
    private ThreadUtilities()
    {
    }
}
