package io.opensphere.core.util.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.opensphere.core.util.Constants;
import io.opensphere.core.util.lang.NamedThreadFactory;

/**
 * An executor that runs a task after a delay. If another task is submitted
 * before the first task has run, the first task is cancelled and the new task
 * is scheduled. If the first task is already running when the second task is
 * added, the second task will be scheduled after the first task is complete.
 */
@SuppressWarnings("PMD.GodClass")
public class ProcrastinatingExecutor implements Executor
{
    /**
     * The earliest run time for the latest runnable, from
     * {@link System#nanoTime()}.
     */
    private long myEarliestRunTime;

    /** The wrapped executor. */
    private final ExecutorService myExecutor;

    /**
     * The latest future. This is initialized with a meaningless object simply
     * to avoid null checks elsewhere.
     */
    private Future<?> myLatestFuture = new FutureTask<Object>(new Callable<Object>()
    {
        @Override
        public Object call()
        {
            return null;
        }
    });

    /** The latest scheduled runner. */
    private Runnable myLatestRunner;

    /**
     * The latest run time for the latest runnable, from
     * {@link System#nanoTime()}. This isn't really the latest, since runnables
     * will never be allowed to overlap, but if the current time is past this
     * limit, the latest runnable will run immediately.
     */
    private long myLatestRunTime = Long.MAX_VALUE;

    /** A lock. */
    private final Lock myLock = new ReentrantLock();

    /**
     * The length of time that must pass after the start of an execution before
     * the latest runnable is executed immediately following the execution.
     */
    private final long myMaxDelayNanoseconds;

    /** The length of the waiting period before new runnables are executed. */
    private final long myMinDelayNanoseconds;

    /** The runnable wrapper. */
    private final ProcrastinatingRunnable myProcrastinatingRunnable = new ProcrastinatingRunnable();

    /** Flag indicating if a task is currently running. */
    private boolean myRunning;

    /**
     * Wrap a {@link ScheduledExecutorService} so that it can only be used by
     * {@link ProcrastinatingExecutor}s. This is to protect against accidentally
     * submitting tasks directly to the {@link ScheduledExecutorService} without
     * properly handling exceptions.
     *
     * @param service The wrapped service.
     * @return The wrapper.
     */
    public static ScheduledExecutorService protect(ScheduledExecutorService service)
    {
        return service instanceof ProtectedScheduledExecutorService ? service : new ProtectedScheduledExecutorService(service);
    }

    /**
     * Construct the executor with zero delay.
     *
     * @param executor A wrapped executor used for scheduling tasks.
     */
    public ProcrastinatingExecutor(ExecutorService executor)
    {
        myExecutor = executor instanceof ProtectedScheduledExecutorService
                ? ((ProtectedScheduledExecutorService)executor).getService() : executor;
        myMinDelayNanoseconds = 0L;
        myMaxDelayNanoseconds = -1L;
    }

    /**
     * Constructor the executor with a minimum delay.
     *
     * @param executor A wrapped executor used for scheduling tasks.
     * @param minDelayMilliseconds The minimum delay between when a task is
     *            submitted and when it is executed.
     * @throws IllegalArgumentException If the minDelayMilliseconds is &lt; 0.
     */
    public ProcrastinatingExecutor(ScheduledExecutorService executor, int minDelayMilliseconds) throws IllegalArgumentException
    {
        if (minDelayMilliseconds < 0)
        {
            throw new IllegalArgumentException("minDelayMilliseconds cannot be < 0");
        }

        myExecutor = executor instanceof ProtectedScheduledExecutorService
                ? ((ProtectedScheduledExecutorService)executor).getService() : executor;
        myMinDelayNanoseconds = (long)minDelayMilliseconds * Constants.NANO_PER_MILLI;
        myMaxDelayNanoseconds = -1L;
    }

    /**
     * Constructor the executor with minimum and maximum delays.
     *
     * @param executor A wrapped executor used for scheduling tasks.
     * @param minDelayMilliseconds The minimum delay between when a task is
     *            submitted and when it is executed.
     * @param maxDelayMilliseconds The (best effort) maximum delay between when
     *            one task is submitted and when the latest task is executed.
     *            This will not cause multiple tasks to be executed
     *            concurrently, regardless of how long they take.
     * @throws IllegalArgumentException If the minDelayMilliseconds is &lt; 0.
     */
    public ProcrastinatingExecutor(ScheduledExecutorService executor, int minDelayMilliseconds, int maxDelayMilliseconds)
        throws IllegalArgumentException
    {
        if (minDelayMilliseconds < 0)
        {
            throw new IllegalArgumentException("minDelayMilliseconds cannot be < 0");
        }
        if (maxDelayMilliseconds < minDelayMilliseconds)
        {
            throw new IllegalArgumentException("maxDelayMilliseconds cannot be < minDelayMilliseconds");
        }
        myExecutor = executor instanceof ProtectedScheduledExecutorService
                ? ((ProtectedScheduledExecutorService)executor).getService() : executor;
        myMinDelayNanoseconds = (long)minDelayMilliseconds * Constants.NANO_PER_MILLI;
        myMaxDelayNanoseconds = (long)maxDelayMilliseconds * Constants.NANO_PER_MILLI;
    }

    /**
     * Instantiates a new procrastinating executor with a single thread named
     * scheduled thread pool with minimum delay.
     *
     * @param executorThreadName the executor thread name for the named
     *            scheduled thread pool.
     * @param minDelayMilliseconds The minimum delay between when a task is
     *            submitted and when it is executed.
     * @throws IllegalArgumentException If the minDelayMilliseconds is &lt; 0.
     */
    public ProcrastinatingExecutor(String executorThreadName, int minDelayMilliseconds) throws IllegalArgumentException
    {
        this(Executors.newScheduledThreadPool(1, new NamedThreadFactory(executorThreadName)), minDelayMilliseconds);
    }

    /**
     * Instantiates a new procrastinating executor with a single thread named
     * scheduled thread pool with minimum and maximum delays.
     *
     * @param executorThreadName the executor thread name for the named
     *            scheduled thread pool.
     * @param minDelayMilliseconds The minimum delay between when a task is
     *            submitted and when it is executed.
     * @param maxDelayMilliseconds The (best effort) maximum delay between when
     *            one task is submitted and when the latest task is executed.
     *            This will not cause multiple tasks to be executed
     *            concurrently, regardless of how long they take.
     * @throws IllegalArgumentException If the minDelayMilliseconds is &lt; 0.
     */
    public ProcrastinatingExecutor(String executorThreadName, int minDelayMilliseconds, int maxDelayMilliseconds)
        throws IllegalArgumentException
    {
        this(Executors.newScheduledThreadPool(1, new NamedThreadFactory(executorThreadName)), minDelayMilliseconds,
                maxDelayMilliseconds);
    }

    /**
     * Run a task after some delay. If this method gets called again before the
     * delay is complete, the first task will be cancelled and the delay will
     * start over.
     * <p>
     * This can be used for times when some follow-up processing needs to be
     * done after some repeated activity is complete.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void execute(Runnable runner)
    {
        Lock lock = getLock();
        lock.lock();
        try
        {
            myLatestRunner = runner;
            schedule();
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Get if there is a pending runnable that has not yet started to run.
     *
     * @return {@code true} if there is a pending runnable.
     */
    public boolean hasPending()
    {
        Lock lock = getLock();
        lock.lock();
        try
        {
            return myLatestRunner != null;
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * If my wrapped executor supports shutdown, request an orderly shutdown.
     */
    public void shutdown()
    {
        if (myExecutor instanceof ScheduledExecutorService)
        {
            ((ScheduledExecutorService)myExecutor).shutdown();
        }
    }

    /**
     * If my wrapped executor supports shutdown, request immediate shutdown.
     *
     * @return list of tasks that never commenced execution
     */
    public List<Runnable> shutdownNow()
    {
        if (myExecutor instanceof ScheduledExecutorService)
        {
            return ((ScheduledExecutorService)myExecutor).shutdownNow();
        }
        return null;
    }

    /**
     * Get if a time is before the (current) earliest run time.
     *
     * @param t0 A time as given by {@link System#nanoTime()}.
     * @return If the given time is before the earliest run time.
     */
    protected boolean beforeEarliestRunTime(long t0)
    {
        return t0 < myEarliestRunTime;
    }

    /**
     * Accessor for the lock.
     *
     * @return The lock.
     */
    protected Lock getLock()
    {
        return myLock;
    }

    /**
     * Get if there is currently a task running.
     *
     * @return If a task is currently running.
     */
    protected boolean isRunning()
    {
        return myRunning;
    }

    /**
     * Schedule the procrastinating runnable, cancelling any obsolete futures.
     */
    protected void schedule()
    {
        myLatestFuture.cancel(false);
        if (myMinDelayNanoseconds > 0L)
        {
            long now = System.nanoTime();
            if (myMaxDelayNanoseconds > 0L)
            {
                if (myLatestRunTime == Long.MAX_VALUE)
                {
                    myLatestRunTime = now + myMaxDelayNanoseconds;
                    myEarliestRunTime = now + myMinDelayNanoseconds;
                }
                else
                {
                    myEarliestRunTime = Math.min(myLatestRunTime, now + myMinDelayNanoseconds);
                }
            }
            else
            {
                myEarliestRunTime = now + myMinDelayNanoseconds;
            }
            myLatestFuture = ((ScheduledExecutorService)myExecutor).schedule(myProcrastinatingRunnable, myEarliestRunTime - now,
                    TimeUnit.NANOSECONDS);
        }
        else
        {
            myLatestFuture = myExecutor.submit(myProcrastinatingRunnable);
        }
    }

    /**
     * Set the flag indicating if a task is currently running.
     *
     * @param running The flag.
     */
    protected void setRunning(boolean running)
    {
        myRunning = running;
    }

    /**
     * Wrapper for a {@link ScheduledExecutorService} that only allows a
     * {@link ProcrastinatingExecutor} to submit tasks. This is to protect
     * against accidentally submitting tasks directly to the
     * {@link ScheduledExecutorService} without properly handling exceptions.
     */
    protected static class ProtectedScheduledExecutorService implements ScheduledExecutorService
    {
        /** The wrapped scheduled executor service. */
        private final ScheduledExecutorService myService;

        /**
         * Constructor.
         *
         * @param service The wrapped service.
         */
        protected ProtectedScheduledExecutorService(ScheduledExecutorService service)
        {
            myService = service;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
        {
            return getService().awaitTermination(timeout, unit);
        }

        @Override
        public void execute(Runnable command)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isShutdown()
        {
            return getService().isShutdown();
        }

        @Override
        public boolean isTerminated()
        {
            return getService().isTerminated();
        }

        @Override
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void shutdown()
        {
            getService().shutdown();
        }

        @Override
        public List<Runnable> shutdownNow()
        {
            return getService().shutdownNow();
        }

        @Override
        public <T> Future<T> submit(Callable<T> task)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Future<?> submit(Runnable task)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result)
        {
            throw new UnsupportedOperationException();
        }

        /**
         * Accessor for the service.
         *
         * @return The service.
         */
        protected ScheduledExecutorService getService()
        {
            return myService;
        }
    }

    /**
     * An runnable that can schedule another runnable after it completes.
     */
    private class ProcrastinatingRunnable implements Runnable
    {
        @Override
        public void run()
        {
            long t0 = System.nanoTime();

            Runnable runner;

            Lock lock = getLock();
            lock.lock();
            try
            {
                // If another task is still running, or my runner isn't the
                // latest one, skip it. The latest runner will always get
                // scheduled once the current one completes.
                if (isRunning() || beforeEarliestRunTime(t0) || myLatestRunner == null)
                {
                    return;
                }

                setRunning(true);

                runner = myLatestRunner;
                myLatestRunner = null;

                if (myMaxDelayNanoseconds > 0L)
                {
                    myLatestRunTime = t0 + myMaxDelayNanoseconds;
                }
            }
            finally
            {
                lock.unlock();
            }

            try
            {
                runner.run();
            }
            catch (RuntimeException t)
            {
                // Catch runtime exceptions here to avoid them being swallowed
                // by FutureTask.
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), t);
            }
            catch (Error t)
            {
                // Catch errors here to avoid them being swallowed by
                // FutureTask.
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), t);
            }
            finally
            {
                lock.lock();
                try
                {
                    setRunning(false);

                    if (myLatestRunner != null)
                    {
                        // Re-schedule the latest runner so that the full delay
                        // is after this runner completes.
                        schedule();
                    }
                }
                finally
                {
                    lock.unlock();
                }
            }
        }
    }
}
