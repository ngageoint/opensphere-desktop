package io.opensphere.core.util.concurrent;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;

import io.opensphere.core.util.Utilities;

/**
 * A {@link RejectedExecutionHandler} that behaves like {@link AbortPolicy}
 * unless it is suppressed, in which case it behaves like {@link DiscardPolicy}.
 */
public final class SuppressableRejectedExecutionHandler implements RejectedExecutionHandler
{
    /** The discard policy. */
    private static final AbortPolicy ABORT_POLICY = new AbortPolicy();

    /** The discard policy. */
    private static final DiscardPolicy DISCARD_POLICY = new DiscardPolicy();

    /** Singleton reference. */
    private static final SuppressableRejectedExecutionHandler ourInstance = new SuppressableRejectedExecutionHandler();

    /** The current policy. */
    private volatile RejectedExecutionHandler myPolicy = ABORT_POLICY;

    /**
     * Get a reference to the SuppressableRejectedExecutionHandler.
     *
     * @return the singleton instance
     */
    public static SuppressableRejectedExecutionHandler getInstance()
    {
        return ourInstance;
    }

    /**
     * Private constructor to enforce singleton pattern.
     */
    private SuppressableRejectedExecutionHandler()
    {
    }

    /**
     * Get if exceptions are suppressed.
     *
     * @return {@code true} if exceptions are suppressed.
     */
    public boolean isSuppressed()
    {
        return Utilities.sameInstance(myPolicy, DISCARD_POLICY);
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
    {
        myPolicy.rejectedExecution(r, executor);
    }

    /**
     * Set suppressing the exceptions.
     *
     * @param suppressed The flag indicating if the exceptions should be
     *            suppressed.
     */
    public void setSuppressed(boolean suppressed)
    {
        myPolicy = suppressed ? DISCARD_POLICY : ABORT_POLICY;
    }
}
