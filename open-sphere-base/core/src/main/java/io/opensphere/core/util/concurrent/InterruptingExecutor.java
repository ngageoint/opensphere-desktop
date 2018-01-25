package io.opensphere.core.util.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import io.opensphere.core.util.Constants;

/**
 * An {@link Executor} that will interrupt tasks that take too long. This
 * implements {@link ScheduledExecutorService}, but only operations supported by
 * the wrapped {@link Executor} are supported.
 */
public class InterruptingExecutor implements ScheduledExecutorService
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(InterruptingExecutor.class);

    /** The runnables that are current executing. */
    private final Collection<Interruptible<?>> myCurrentInterruptibles = new ArrayList<>();

    /** The wrapped executor. */
    private final Executor myExecutor;

    /** The future for the overtime guard. */
    private final ScheduledFuture<?> myFuture;

    /** The runnable used to check for tasks that are over the time limit. */
    private final OvertimeGuard myOvertimeGuard;

    /** Flag indicating if this executor has been stopped. */
    private volatile boolean myShutdown;

    /** The time limit for tasks. */
    private final long myTimeLimitNanoseconds;

    /**
     * Construct the interrupting executor.
     *
     * @param wrappedExecutor The executor that will actually run the tasks
     *            passed to this executor.
     * @param timeLimitMilliseconds The time limit for tasks in milliseconds.
     */
    public InterruptingExecutor(Executor wrappedExecutor, long timeLimitMilliseconds)
    {
        myExecutor = wrappedExecutor;
        myTimeLimitNanoseconds = timeLimitMilliseconds * Constants.NANO_PER_MILLI;

        myOvertimeGuard = new OvertimeGuard();
        myFuture = CommonTimer.scheduleAtFixedRate(myOvertimeGuard, timeLimitMilliseconds, timeLimitMilliseconds);
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
    {
        checkInterface(ExecutorService.class);
        return ((ExecutorService)myExecutor).awaitTermination(timeout, unit);
    }

    @Override
    public synchronized void execute(Runnable command)
    {
        if (isShutdown())
        {
            throw new RejectedExecutionException("Cannot submit runnable after executor has been stopped.");
        }
        myExecutor.execute(new Interruptible<Void>(command));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException
    {
        checkInterface(ExecutorService.class);
        return ((ExecutorService)myExecutor).invokeAll(decorateTasks(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException
    {
        checkInterface(ExecutorService.class);
        return ((ExecutorService)myExecutor).invokeAll(decorateTasks(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException
    {
        checkInterface(ExecutorService.class);
        return ((ExecutorService)myExecutor).invokeAny(decorateTasks(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException
    {
        checkInterface(ExecutorService.class);
        return ((ExecutorService)myExecutor).invokeAny(decorateTasks(tasks), timeout, unit);
    }

    @Override
    public boolean isShutdown()
    {
        return myShutdown;
    }

    @Override
    public boolean isTerminated()
    {
        checkInterface(ExecutorService.class);
        return ((ExecutorService)myExecutor).isTerminated();
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit)
    {
        checkInterface(ScheduledExecutorService.class);
        return ((ScheduledExecutorService)myExecutor).schedule((Callable<V>)new Interruptible<V>(callable), delay, unit);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit)
    {
        checkInterface(ScheduledExecutorService.class);
        return ((ScheduledExecutorService)myExecutor).schedule((Runnable)new Interruptible<Void>(command), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
    {
        checkInterface(ScheduledExecutorService.class);
        return ((ScheduledExecutorService)myExecutor).scheduleAtFixedRate(new Interruptible<Void>(command), initialDelay, period,
                unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
    {
        checkInterface(ScheduledExecutorService.class);
        return ((ScheduledExecutorService)myExecutor).scheduleWithFixedDelay(new Interruptible<Void>(command), initialDelay,
                delay, unit);
    }

    @Override
    public void shutdown()
    {
        myShutdown = true;
        myOvertimeGuard.setCancelAfterNextRun();
        if (myExecutor instanceof ExecutorService)
        {
            ((ExecutorService)myExecutor).shutdown();
        }
    }

    @Override
    public List<Runnable> shutdownNow()
    {
        myShutdown = true;
        myFuture.cancel(false);
        if (myExecutor instanceof ExecutorService)
        {
            return ((ExecutorService)myExecutor).shutdownNow();
        }
        else
        {
            checkForOvertimeTasks();
        }

        LOGGER.warn("shudownNow() not executed for executor type " + myExecutor.getClass().getName());
        return null;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task)
    {
        checkInterface(ExecutorService.class);
        return ((ExecutorService)myExecutor).submit((Callable<T>)new Interruptible<T>(task));
    }

    @Override
    public Future<?> submit(Runnable task)
    {
        checkInterface(ExecutorService.class);
        return ((ExecutorService)myExecutor).submit((Runnable)new Interruptible<Void>(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result)
    {
        checkInterface(ExecutorService.class);
        return ((ExecutorService)myExecutor).submit(new Interruptible<T>(task), result);
    }

    /**
     * Add a currently running runnable.
     *
     * @param runnable The runnable.
     */
    protected void addCurrentInterruptible(Interruptible<?> runnable)
    {
        synchronized (myCurrentInterruptibles)
        {
            myCurrentInterruptibles.add(runnable);
        }
    }

    /**
     * Check the currently running tasks and interrupt any that have been
     * running too long.
     */
    protected void checkForOvertimeTasks()
    {
        long limit = System.nanoTime() - myTimeLimitNanoseconds;
        synchronized (myCurrentInterruptibles)
        {
            if (!myCurrentInterruptibles.isEmpty())
            {
                for (Iterator<Interruptible<?>> iter = myCurrentInterruptibles.iterator(); iter.hasNext();)
                {
                    Interruptible<?> interruptible = iter.next();
                    if (interruptible.interruptIfOvertime(limit))
                    {
                        iter.remove();
                    }
                }
            }
        }
    }

    /**
     * Wrap some {@link Callable}s in {@link InterruptingExecutor.Interruptible}
     * s.
     *
     * @param <T> The return type for the {@linkplain Callable}.
     * @param tasks The {@linkplain Callable}s.
     * @return The {@linkplain InterruptingExecutor.Interruptible}s.
     */
    protected <T> Collection<Callable<T>> decorateTasks(Collection<? extends Callable<T>> tasks)
    {
        Collection<Callable<T>> interruptibles = new ArrayList<>(tasks.size());
        for (Callable<T> task : tasks)
        {
            interruptibles.add(new Interruptible<T>(task));
        }
        return interruptibles;
    }

    /**
     * Remove a currently running interruptible.
     *
     * @param interruptible The interruptible.
     */
    protected void removeCurrentInterruptible(Interruptible<?> interruptible)
    {
        synchronized (myCurrentInterruptibles)
        {
            myCurrentInterruptibles.remove(interruptible);
        }
    }

    /**
     * Verify that the wrapped executor implements an interface.
     *
     * @param type The interface type.
     */
    private void checkInterface(Class<?> type)
    {
        if (!type.isInstance(myExecutor))
        {
            throw new UnsupportedOperationException(
                    "Wrapped executor is not an " + type.getSimpleName() + ". This method is not supported.");
        }
    }

    /**
     * A wrapper for the callable/runnable that keeps track of its thread and
     * start time and can be instructed to interrupt its thread.
     *
     * @param <T> The return type for the callable.
     */
    private final class Interruptible<T> implements Callable<T>, Runnable
    {
        /** The wrapped callable (may be {@code null}). */
        private final Callable<T> myCallable;

        /** A lock used for synchronization. */
        private final Lock myLock = new ReentrantLock();

        /** The wrapped runnable (may be {@code null}). */
        private final Runnable myRunnable;

        /** The start time for this task in nanoseconds. */
        private long myStartTime;

        /** The thread on which this task is executing. */
        private Thread myThread;

        /**
         * Construct the interruptible with a callable.
         *
         * @param wrappedCallable The actual callable.
         */
        public Interruptible(Callable<T> wrappedCallable)
        {
            myCallable = wrappedCallable;
            myRunnable = null;
        }

        /**
         * Construct the interruptible with a runnable.
         *
         * @param wrappedRunnable The actual runnable.
         */
        public Interruptible(Runnable wrappedRunnable)
        {
            myCallable = null;
            myRunnable = wrappedRunnable;
        }

        @Override
        @SuppressWarnings("PMD.SignatureDeclareThrowsException")
        public T call() throws Exception
        {
            if (isShutdown())
            {
                return null;
            }
            try
            {
                myLock.lock();
                try
                {
                    myThread = Thread.currentThread();
                    myStartTime = System.nanoTime();
                }
                finally
                {
                    myLock.unlock();
                }
                addCurrentInterruptible(this);
                return myCallable.call();
            }
            finally
            {
                removeCurrentInterruptible(this);
                myLock.lock();
                try
                {
                    myThread = null;
                }
                finally
                {
                    myLock.unlock();
                }
            }
        }

        /**
         * Interrupt the runnable's thread if the task was started before the
         * provided {@code limit} or if the executor has been stopped. This will
         * only cause the thread to be interrupted once. Subsequent calls after
         * this returns <code>true</code> will have no effect.
         *
         * @param limit The threshold time in nanoseconds.
         * @return <code>true</code> if the task was interrupted.
         */
        public boolean interruptIfOvertime(long limit)
        {
            myLock.lock();
            try
            {
                if (myThread != null && (isShutdown() || myStartTime < limit))
                {
                    myThread.interrupt();
                    myThread = null;
                    return true;
                }
                else
                {
                    return false;
                }
            }
            finally
            {
                myLock.unlock();
            }
        }

        @Override
        public void run()
        {
            if (isShutdown())
            {
                return;
            }
            try
            {
                myLock.lock();
                try
                {
                    myStartTime = System.nanoTime();
                    myThread = Thread.currentThread();
                }
                finally
                {
                    myLock.unlock();
                }
                addCurrentInterruptible(this);
                myRunnable.run();
            }
            finally
            {
                removeCurrentInterruptible(this);
                myLock.lock();
                try
                {
                    myThread = null;
                }
                finally
                {
                    myLock.unlock();
                }
            }
        }
    }

    /**
     * A runnable that checks for overtime tasks on a periodic basis.
     */
    private final class OvertimeGuard implements Runnable
    {
        /**
         * Flag indicating if the future executions of this guard should be
         * cancelled after the next run.
         */
        private volatile boolean myCancelAfterNextRun;

        @Override
        public void run()
        {
            checkForOvertimeTasks();
            if (myCancelAfterNextRun)
            {
                myFuture.cancel(false);
            }
        }

        /**
         * Set the flag to cancel executions after the next run.
         */
        public void setCancelAfterNextRun()
        {
            myCancelAfterNextRun = true;
        }
    }
}
