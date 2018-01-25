package io.opensphere.core.util.concurrent;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * Runnable wrapper that catches otherwise uncaught exceptions and supplies them
 * to an uncaught exception handler. This is useful for cases where the
 * exception may otherwise be lost, like when calling
 * {@link java.util.concurrent.ScheduledExecutorService#execute(Runnable)}.
 */
public final class CatchingRunnable implements Runnable
{
    /** Wrapped runnable. */
    private final Runnable myRunnable;

    /** The uncaught exception handler. */
    private final UncaughtExceptionHandler myUncaughtExceptionHandler;

    /**
     * Constructor.
     *
     * @param runnable The wrapped runnable.
     */
    public CatchingRunnable(Runnable runnable)
    {
        this(runnable, (UncaughtExceptionHandler)null);
    }

    /**
     * Constructor.
     *
     * @param runnable The wrapped runnable.
     * @param handler The uncaught exception handler.
     */
    public CatchingRunnable(Runnable runnable, UncaughtExceptionHandler handler)
    {
        myRunnable = runnable;
        myUncaughtExceptionHandler = handler;
    }

    /**
     * Get the uncaught exception handler.
     *
     * @return The uncaught exception handler.
     */
    private UncaughtExceptionHandler getUncaughtExceptionHandler()
    {
        return myUncaughtExceptionHandler == null ? Thread.currentThread().getUncaughtExceptionHandler()
                : myUncaughtExceptionHandler;
    }

    @Override
    public void run()
    {
        try
        {
            myRunnable.run();
        }
        catch (RuntimeException | Error e)
        {
            getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            throw e;
        }
    }
}
