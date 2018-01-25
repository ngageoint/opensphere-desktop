package io.opensphere.core.util.concurrent;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A wrapper for a {@link ScheduledExecutorService} that intercepts
 * {@link Throwable}s and reports them to the thread's uncaught exception
 * handler.
 */
public class ReportingScheduledExecutorService extends AbstractExecutorService implements ScheduledExecutorService
{
    /** The wrapped executor. */
    private final ScheduledExecutorService myExecutor;

    /** The uncaught exception handler. */
    private final UncaughtExceptionHandler myUncaughtExceptionHandler;

    /**
     * Constructor.
     *
     * @param executor The wrapped executor.
     */
    public ReportingScheduledExecutorService(ScheduledExecutorService executor)
    {
        myExecutor = executor;
        myUncaughtExceptionHandler = null;
    }

    /**
     * Constructor.
     *
     * @param executor The wrapped executor.
     * @param uncaughtExceptionHandler The uncaught exception handler.
     */
    public ReportingScheduledExecutorService(ScheduledExecutorService executor, UncaughtExceptionHandler uncaughtExceptionHandler)
    {
        myExecutor = executor;
        myUncaughtExceptionHandler = uncaughtExceptionHandler;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
    {
        return myExecutor.awaitTermination(timeout, unit);
    }

    @Override
    public void execute(Runnable command)
    {
        myExecutor.execute(new CatchingRunnable(command, getUncaughtExceptionHandler()));
    }

    /**
     * Get the uncaught exception handler.
     *
     * @return The uncaught exception handler.
     */
    public UncaughtExceptionHandler getUncaughtExceptionHandler()
    {
        return myUncaughtExceptionHandler;
    }

    @Override
    public boolean isShutdown()
    {
        return myExecutor.isShutdown();
    }

    @Override
    public boolean isTerminated()
    {
        return myExecutor.isTerminated();
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit)
    {
        return myExecutor.schedule(callable, delay, unit);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit)
    {
        return myExecutor.schedule(command, delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
    {
        return myExecutor.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
    {
        return myExecutor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    @Override
    public void shutdown()
    {
        myExecutor.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow()
    {
        return myExecutor.shutdownNow();
    }
}
