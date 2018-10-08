package io.opensphere.core.util.concurrent;

import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ThreadControl;

/**
 * A special {@link ThreadPoolExecutor} that stops execution if the core thread
 * pool size is set to zero.
 */
public class PausingThreadPoolExecutor extends ThreadPoolExecutor
{
    /** Collection of active worker threads. */
    private final Collection<Thread> myActiveThreads = Collections.synchronizedCollection(New.<Thread>linkedList());

    /** Collection of tasks waiting to be added to the run queue. */
    private final Queue<Runnable> myWaiting = New.queue();

    /**
     * Creates a new <tt>ThreadPoolExecutor</tt> with the given initial
     * parameters and default thread factory and rejected execution handler. It
     * may be more convenient to use one of the {@link Executors} factory
     * methods instead of this general purpose constructor.
     *
     * @param corePoolSize the number of threads to keep in the pool, even if
     *            they are idle.
     * @param maximumPoolSize the maximum number of threads to allow in the
     *            pool.
     * @param keepAliveTime when the number of threads is greater than the core,
     *            this is the maximum time that excess idle threads will wait
     *            for new tasks before terminating.
     * @param unit the time unit for the keepAliveTime argument.
     * @throws IllegalArgumentException if corePoolSize or keepAliveTime less
     *             than zero, or if maximumPoolSize less than or equal to zero,
     *             or if corePoolSize greater than maximumPoolSize.
     * @throws NullPointerException if <tt>workQueue</tt> is null
     */
    public PausingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new SpecialQueue());
        setThreadFactory(getThreadFactory());
    }

    /**
     * Creates a new <tt>ThreadPoolExecutor</tt> with the given initial
     * parameters and default thread factory.
     *
     * @param corePoolSize the number of threads to keep in the pool, even if
     *            they are idle.
     * @param maximumPoolSize the maximum number of threads to allow in the
     *            pool.
     * @param keepAliveTime when the number of threads is greater than the core,
     *            this is the maximum time that excess idle threads will wait
     *            for new tasks before terminating.
     * @param unit the time unit for the keepAliveTime argument.
     * @param handler the handler to use when execution is blocked because the
     *            thread bounds and queue capacities are reached.
     * @throws IllegalArgumentException if corePoolSize or keepAliveTime less
     *             than zero, or if maximumPoolSize less than or equal to zero,
     *             or if corePoolSize greater than maximumPoolSize.
     * @throws NullPointerException if <tt>workQueue</tt> or <tt>handler</tt>
     *             are null.
     */
    public PausingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            RejectedExecutionHandler handler)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new SpecialQueue(), handler);
        setThreadFactory(getThreadFactory());
    }

    /**
     * Creates a new <tt>ThreadPoolExecutor</tt> with the given initial
     * parameters and default rejected execution handler.
     *
     * @param corePoolSize the number of threads to keep in the pool, even if
     *            they are idle.
     * @param maximumPoolSize the maximum number of threads to allow in the
     *            pool.
     * @param keepAliveTime when the number of threads is greater than the core,
     *            this is the maximum time that excess idle threads will wait
     *            for new tasks before terminating.
     * @param unit the time unit for the keepAliveTime argument.
     * @param threadFactory the factory to use when the executor creates a new
     *            thread.
     * @throws IllegalArgumentException if corePoolSize or keepAliveTime less
     *             than zero, or if maximumPoolSize less than or equal to zero,
     *             or if corePoolSize greater than maximumPoolSize.
     * @throws NullPointerException if <tt>workQueue</tt> or
     *             <tt>threadFactory</tt> are null.
     */
    public PausingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            ThreadFactory threadFactory)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new SpecialQueue(), threadFactory);
        setThreadFactory(getThreadFactory());
    }

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
    public PausingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            ThreadFactory threadFactory, RejectedExecutionHandler handler)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new SpecialQueue(), threadFactory, handler);
        setThreadFactory(getThreadFactory());
    }

    @Override
    public void execute(Runnable command)
    {
        if (getPoolSize() > getCorePoolSize() || getCorePoolSize() == 0)
        {
            synchronized (myWaiting)
            {
                myWaiting.add(command);
            }
        }
        else
        {
            moveWaitingToQueue();
            super.execute(command);
        }
    }

    @Override
    public void setCorePoolSize(int corePoolSize)
    {
        if (getPoolSize() > corePoolSize)
        {
            ((SpecialQueue)getQueue()).setAllowCapacity(false);
            synchronized (myWaiting)
            {
                getQueue().drainTo(myWaiting);
            }
        }
        else
        {
            moveWaitingToQueue();
        }
        synchronized (myActiveThreads)
        {
            int count = 0;
            for (Thread worker : myActiveThreads)
            {
                if (++count > corePoolSize)
                {
                    ThreadControl.pauseThread(worker);
                }
                else
                {
                    ThreadControl.unpauseThread(worker);
                }
            }
        }
        super.setCorePoolSize(corePoolSize);
    }

    @Override
    public final void setThreadFactory(ThreadFactory threadFactory)
    {
        super.setThreadFactory(new ThreadFactoryProxy(threadFactory));
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t)
    {
        myActiveThreads.remove(Thread.currentThread());
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r)
    {
        ThreadControl.clearState(t);
        myActiveThreads.add(t);
    }

    /**
     * Move any tasks from {@link #myWaiting} to {@link #getQueue()}.
     */
    private void moveWaitingToQueue()
    {
        ((SpecialQueue)getQueue()).setAllowCapacity(true);
        synchronized (myWaiting)
        {
            if (!myWaiting.isEmpty())
            {
                getQueue().addAll(myWaiting);
                myWaiting.clear();
            }
        }
    }

    /**
     * Wrapper for the queue that allows overriding the remaining capacity.
     */
    private static final class SpecialQueue extends LinkedBlockingQueue<Runnable>
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** Flag indicating if this queue allows any capacity. */
        private volatile boolean myAllowCapacity = true;

        @Override
        public int remainingCapacity()
        {
            if (myAllowCapacity)
            {
                return Integer.MAX_VALUE;
            }
            return 0;
        }

        /**
         * Set if the queue allows any capacity.
         *
         * @param allowCapacity If the queue allows any capacity.
         */
        public void setAllowCapacity(boolean allowCapacity)
        {
            myAllowCapacity = allowCapacity;
        }
    }

    /** Thread factory proxy that lets us to install the {@link WorkerProxy}. */
    private final class ThreadFactoryProxy implements ThreadFactory
    {
        /**
         * The nested factory that I delegate to to actually create the thread.
         */
        private final ThreadFactory myNestedFactory;

        /**
         * Constructor.
         *
         * @param nestedFactory The nested factory.
         */
        public ThreadFactoryProxy(ThreadFactory nestedFactory)
        {
            myNestedFactory = nestedFactory;
        }

        @Override
        public Thread newThread(Runnable r)
        {
            return myNestedFactory.newThread(new WorkerProxy(r));
        }
    }

    /**
     * Wrapper for the superclass' worker that lets us know when the worker
     * exits.
     */
    private final class WorkerProxy implements Runnable
    {
        /** The nested runnable. */
        private final Runnable myNestedRunnable;

        /**
         * Constructor.
         *
         * @param nestedRunnable The nested runnable.
         */
        public WorkerProxy(Runnable nestedRunnable)
        {
            myNestedRunnable = Utilities.checkNull(nestedRunnable, "nestedRunnable");
        }

        @Override
        public void run()
        {
            try
            {
                myNestedRunnable.run();
            }
            finally
            {
                setCorePoolSize(getCorePoolSize());
            }
        }
    }
}
