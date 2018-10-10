package io.opensphere.core.util.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.lang.NamedThreadFactory;

/**
 * Optimized (simple) executor that runs a fixed number of threads that run
 * tasks in an indeterminate order. This does not support thread timeout, and as
 * a result, has shorter execution latency than {@link ThreadPoolExecutor}
 */
public final class FixedThreadPoolExecutor extends AbstractExecutorService
{
    /** Default rejection handler. */
    private static final RejectedExecutionHandler DEFAULT_HANDLER = new AbortPolicy();

    /** Lock used to synchronize shutting down and adding new tasks. */
    private final Lock myAddLock = new ReentrantLock();

    /** The handler for un-executed tasks. */
    private final RejectedExecutionHandler myHandler;

    /** The backlog of tasks. */
    private final BlockingQueue<Runnable> myQueue;

    /** Flag indicating if the executor is shutting down. */
    private volatile boolean myShuttingDown;

    /** Flag indicating that the executor should not execute any more tasks. */
    private volatile boolean myStopExecuting;

    /** The collection of workers. */
    private final Collection<Worker> myWorkers;

    /**
     * Construct a fixed thread pool executor. This will immediately start the
     * specified number of daemon threads.
     *
     * @param nThreads The number of threads.
     */
    public FixedThreadPoolExecutor(int nThreads)
    {
        this(nThreads, Executors.defaultThreadFactory());
    }

    /**
     * Construct a fixed thread pool executor. This will immediately start the
     * specified number of daemon threads using the supplied factory.
     *
     * @param nThreads The number of threads.
     * @param factory The thread factory.
     * @param handler The handler to be used if a task cannot be handled due to
     *            queue bounds or the executor shutting down. The
     *            {@link ThreadPoolExecutor} passed to the handler will be
     *            {@code null}.
     */
    public FixedThreadPoolExecutor(int nThreads, NamedThreadFactory factory, SuppressableRejectedExecutionHandler handler)
    {
        this(nThreads, factory, new UnlimitedLinkedBlockingQueue<Runnable>(), handler);
    }

    /**
     * Construct a fixed thread pool executor. This will immediately start the
     * specified number of daemon threads using the supplied factory.
     *
     * @param nThreads The number of threads.
     * @param factory The thread factory.
     */
    public FixedThreadPoolExecutor(int nThreads, ThreadFactory factory)
    {
        this(nThreads, factory, new UnlimitedLinkedBlockingQueue<Runnable>());
    }

    /**
     * Construct a fixed thread pool executor. This will immediately start the
     * specified number of daemon threads using the supplied factory.
     *
     * @param nThreads The number of threads.
     * @param factory The thread factory.
     * @param queue The queue for the tasks which will be executed by this
     *            executor.
     */
    public FixedThreadPoolExecutor(int nThreads, ThreadFactory factory, BlockingQueue<Runnable> queue)
    {
        this(nThreads, factory, queue, DEFAULT_HANDLER);
    }

    /**
     * Construct a fixed thread pool executor. This will immediately start the
     * specified number of daemon threads using the supplied factory.
     *
     * @param nThreads The number of threads.
     * @param factory The thread factory.
     * @param queue The queue for the tasks which will be executed by this
     *            executor.
     * @param handler The handler to be used if a task cannot be handled due to
     *            queue bounds or the executor shutting down. The
     *            {@link ThreadPoolExecutor} passed to the handler will be
     *            {@code null}.
     */
    public FixedThreadPoolExecutor(int nThreads, ThreadFactory factory, BlockingQueue<Runnable> queue,
            RejectedExecutionHandler handler)
    {
        myQueue = queue;
        myWorkers = new ArrayList<>(nThreads);
        for (int i = 0; i < nThreads; i++)
        {
            Worker worker = new Worker();
            myWorkers.add(worker);
            Thread thread = factory.newThread(worker);
            worker.setThread(thread);
            thread.setDaemon(true);
            thread.start();
        }
        myHandler = handler;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
    {
        long limit = System.nanoTime() + unit.toNanos(timeout);
        boolean stillRunning = true;
        while (stillRunning && System.nanoTime() < limit)
        {
            synchronized (myWorkers)
            {
                if (myWorkers.isEmpty())
                {
                    stillRunning = false;
                }
                else
                {
                    long waitMillis = (limit - System.nanoTime()) / Constants.NANO_PER_MILLI;
                    if (waitMillis > 0)
                    {
                        myWorkers.wait(waitMillis);
                    }
                }
            }
        }

        return !stillRunning;
    }

    @Override
    @SuppressFBWarnings(value = "NP_NONNULL_PARAM_VIOLATION", justification = "No ThreadPoolExecutor available to pass.")
    public void execute(Runnable command)
    {
        myAddLock.lock();
        try
        {
            if (myShuttingDown || !myQueue.add(command))
            {
                myHandler.rejectedExecution(command, null);
            }
        }
        finally
        {
            myAddLock.unlock();
        }
    }

    /**
     * Get access to the underlying queue.
     *
     * @return The queue.
     */
    public BlockingQueue<Runnable> getQueue()
    {
        return myQueue;
    }

    @Override
    public boolean isShutdown()
    {
        return myShuttingDown;
    }

    @Override
    public boolean isTerminated()
    {
        synchronized (myWorkers)
        {
            return myWorkers.isEmpty();
        }
    }

    @Override
    public void shutdown()
    {
        myShuttingDown = true;
        synchronized (myWorkers)
        {
            for (Worker worker : myWorkers)
            {
                worker.interruptIfIdle();
            }
        }
    }

    @Override
    public List<Runnable> shutdownNow()
    {
        myStopExecuting = true;

        myAddLock.lock();
        try
        {
            myShuttingDown = true;
        }
        finally
        {
            myAddLock.unlock();
        }

        List<Runnable> unrun = new ArrayList<>();
        myQueue.drainTo(unrun);

        synchronized (myWorkers)
        {
            for (Worker worker : myWorkers)
            {
                worker.interrupt();
            }
        }

        return unrun;
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable)
    {
        if (callable instanceof Comparable<?>)
        {
            return new ComparableFutureTask<>(callable)
            {
                /**
                 * Extend cancel to remove the task from the queue to prevent
                 * the queue from getting too long.
                 * <p>
                 * {@inheritDoc}
                 */
                @Override
                public boolean cancel(boolean mayInterruptIfRunning)
                {
                    myQueue.remove(this);
                    return super.cancel(mayInterruptIfRunning);
                }
            };
        }
        else if (callable != null)
        {
            return new FutureTaskExtension<>(callable);
        }
        else
        {
            return null;
        }
    }

    @Override
    protected <T extends Object> java.util.concurrent.RunnableFuture<T> newTaskFor(Runnable runnable, T value)
    {
        if (runnable instanceof Comparable<?>)
        {
            return new ComparableFutureTask<>(runnable, value)
            {
                /**
                 * Extend cancel to remove the task from the queue to prevent
                 * the queue from getting too long.
                 * <p>
                 * {@inheritDoc}
                 */
                @Override
                public boolean cancel(boolean mayInterruptIfRunning)
                {
                    myQueue.remove(this);
                    return super.cancel(mayInterruptIfRunning);
                }
            };
        }
        else if (runnable != null)
        {
            return new FutureTask<>(runnable, value)
            {
                /**
                 * Extend cancel to remove the task from the queue to prevent
                 * the queue from getting too long.
                 * <p>
                 * {@inheritDoc}
                 */
                @Override
                public boolean cancel(boolean mayInterruptIfRunning)
                {
                    myQueue.remove(this);
                    return super.cancel(mayInterruptIfRunning);
                }
            };
        }
        else
        {
            return null;
        }
    }

    /**
     * A handler for rejected tasks that throws a
     * {@code RejectedExecutionException}.
     */
    private static class AbortPolicy implements RejectedExecutionHandler
    {
        /**
         * Creates an {@code AbortPolicy}.
         */
        public AbortPolicy()
        {
        }

        /**
         * Always throws RejectedExecutionException.
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         * @throws RejectedExecutionException always.
         */
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e)
        {
            throw new RejectedExecutionException("Task " + r.toString() + " rejected");
        }
    }

    /**
     * A {@link FutureTask} that extends cancel to remove the task from the
     * queue to prevent the queue from getting too long.
     *
     * @param <T> The return value from the {@link Callable}.
     */
    private final class FutureTaskExtension<T> extends FutureTask<T>
    {
        /**
         * Constructor.
         *
         * @param callable The callable.
         */
        public FutureTaskExtension(Callable<T> callable)
        {
            super(callable);
        }

        /**
         * Constructor that takes a result.
         *
         * @param runnable The runnable.
         * @param result The result to return.
         */
        public FutureTaskExtension(Runnable runnable, T result)
        {
            super(runnable, result);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning)
        {
            myQueue.remove(this);
            return super.cancel(mayInterruptIfRunning);
        }
    }

    /** The worker class to be run by each thread. */
    private class Worker implements Runnable
    {
        /** A lock to prevent interrupting while a task is running. */
        private final Lock myRunLock = new ReentrantLock();

        /** The thread this worker is running in. */
        private Thread myThread;

        /**
         * Interrupt this worker's thread.
         */
        public void interrupt()
        {
            myThread.interrupt();
        }

        /**
         * Interrupt this worker if it's idle.
         */
        public void interruptIfIdle()
        {
            if (myRunLock.tryLock())
            {
                try
                {
                    myThread.interrupt();
                }
                finally
                {
                    myRunLock.unlock();
                }
            }
        }

        @Override
        public void run()
        {
            while (!myShuttingDown || !myQueue.isEmpty())
            {
                try
                {
                    Runnable r = myQueue.take();
                    myRunLock.lock();
                    try
                    {
                        if (Thread.interrupted() && myStopExecuting)
                        {
                            myThread.interrupt();
                        }

                        r.run();
                    }
                    finally
                    {
                        myRunLock.unlock();
                    }
                }
                catch (InterruptedException e)
                {
                }
            }

            synchronized (myWorkers)
            {
                myWorkers.remove(this);
                myWorkers.notifyAll();
            }
        }

        /**
         * Set the thread this worker is running in.
         *
         * @param thread The thread this worker will run in.
         */
        public void setThread(Thread thread)
        {
            myThread = thread;
        }
    }
}
