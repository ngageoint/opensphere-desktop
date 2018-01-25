package io.opensphere.core.util;

/**
 * A {@link TimeBudget} that can be paused.
 */
public class PausingTimeBudget extends TimeBudget
{
    /** An indefinite time budget. */
    public static final PausingTimeBudget INDEFINITE = new PausingTimeBudget(Long.MAX_VALUE, 0L);

    /** The time of the pause, if the budget is currently paused. */
    private long myPauseTimeNanos;

    /** The total amount of pause time. */
    private long myPauseTotalNanos;

    /**
     * Start a time budget.
     *
     * @param milliseconds The magnitude of the budget, in milliseconds.
     * @return The new time budget.
     */
    public static PausingTimeBudget startMilliseconds(long milliseconds)
    {
        long now = System.nanoTime();
        if (millisecondsOverflow(now, milliseconds))
        {
            return INDEFINITE;
        }
        else
        {
            return new PausingTimeBudget(now + milliseconds * Constants.NANO_PER_MILLI, 0L);
        }
    }

    /**
     * Start a time budget in the paused state.
     *
     * @param milliseconds The magnitude of the budget, in milliseconds.
     * @return The new time budget.
     */
    public static PausingTimeBudget startMillisecondsPaused(long milliseconds)
    {
        long now = System.nanoTime();
        if (millisecondsOverflow(now, milliseconds))
        {
            return INDEFINITE;
        }
        else
        {
            return new PausingTimeBudget(now + milliseconds * Constants.NANO_PER_MILLI, now);
        }
    }

    /**
     * Start a time budget.
     *
     * @param nanoseconds The magnitude of the budget, in nanoseconds.
     * @return The new time budget.
     */
    public static PausingTimeBudget startNanoseconds(long nanoseconds)
    {
        long now = System.nanoTime();
        if (nanosecondsOverflow(now, nanoseconds))
        {
            return INDEFINITE;
        }
        else
        {
            return new PausingTimeBudget(now + nanoseconds, 0L);
        }
    }

    /**
     * Start a time budget in the paused state.
     *
     * @param nanoseconds The magnitude of the budget, in nanoseconds.
     * @return The new time budget.
     */
    public static PausingTimeBudget startNanosecondsPaused(long nanoseconds)
    {
        long now = System.nanoTime();
        if (nanosecondsOverflow(now, nanoseconds))
        {
            return INDEFINITE;
        }
        else
        {
            return new PausingTimeBudget(now + nanoseconds, now);
        }
    }

    /**
     * Construct a time budget.
     *
     * @param expirationTimeNanoseconds The expiration time in nanoseconds since
     *            epoch.
     * @param pauseTimeNanoseconds The pause time in nanoseconds since epoch.
     */
    protected PausingTimeBudget(long expirationTimeNanoseconds, long pauseTimeNanoseconds)
    {
        super(expirationTimeNanoseconds);
        myPauseTimeNanos = pauseTimeNanoseconds;
    }

    /**
     * Pause the time budget. Pausing an already-paused budget has no effect.
     *
     * @return If the pause occurred.
     */
    public synchronized boolean pause()
    {
        if (myPauseTimeNanos == 0L)
        {
            myPauseTimeNanos = System.nanoTime();
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public TimeBudget subBudgetMilliseconds(long milliseconds)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Un-pause the time budget. Un-pausing an un-paused budget has no effect.
     */
    public synchronized void unpause()
    {
        if (myPauseTimeNanos != 0L)
        {
            myPauseTotalNanos += System.nanoTime() - myPauseTimeNanos;
            myPauseTimeNanos = 0L;
        }
    }

    @Override
    protected synchronized long getExpirationTimeNanoseconds()
    {
        long expTime = super.getExpirationTimeNanoseconds() + myPauseTotalNanos;
        if (myPauseTimeNanos != 0L)
        {
            expTime += System.nanoTime() - myPauseTimeNanos;
        }
        return expTime;
    }
}
