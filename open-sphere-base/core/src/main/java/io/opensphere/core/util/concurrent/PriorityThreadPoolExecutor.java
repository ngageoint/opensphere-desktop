package io.opensphere.core.util.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A {@link ThreadPoolExecutor} that supports using a
 * {@link PriorityBlockingQueue}.
 */
public class PriorityThreadPoolExecutor extends ThreadPoolExecutor
{
    /**
     * Creates a new <tt>ThreadPoolExecutor</tt> with the given initial
     * parameters.
     *
     * @param corePoolSize the number of threads to keep in the pool, even if
     *            they are idle.
     * @param maximumPoolSize the maximum number of threads to allow in the
     *            pool.
     * @param keepAliveTime when the number of threads is greater than the core,
     *            this is the maximum time that excess idle threads will wait
     *            for new tasks before terminating.
     * @param unit the time unit for the keepAliveTime argument.
     * @param workQueue the queue to use for holding tasks before they are
     *            executed. This queue will hold only the <tt>Runnable</tt>
     *            tasks submitted by the <tt>execute</tt> method.
     * @param threadFactory the factory to use when the executor creates a new
     *            thread.
     * @param handler the handler to use when execution is blocked because the
     *            thread bounds and queue capacities are reached.
     * @throws IllegalArgumentException if corePoolSize or keepAliveTime less
     *             than zero, or if maximumPoolSize less than or equal to zero,
     *             or if corePoolSize greater than maximumPoolSize.
     * @throws NullPointerException if <tt>workQueue</tt> or
     *             <tt>threadFactory</tt> or <tt>handler</tt> are null.
     */
    public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable)
    {
        if (callable instanceof Comparable<?>)
        {
            return new ComparableFutureTask<T>(callable);
        }
        else
        {
            return super.newTaskFor(callable);
        }
    }

    @Override
    protected <T extends Object> RunnableFuture<T> newTaskFor(Runnable runnable, T value)
    {
        if (runnable instanceof Comparable<?>)
        {
            return new ComparableFutureTask<T>(runnable, value);
        }
        else
        {
            return super.newTaskFor(runnable, value);
        }
    }
}
