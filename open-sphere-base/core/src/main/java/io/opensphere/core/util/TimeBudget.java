package io.opensphere.core.util;

/**
 * Utility for managing an execution time budget. For example, a method may be
 * allowed to run for 100 milliseconds before it should be aborted.
 */
@net.jcip.annotations.ThreadSafe
public class TimeBudget
{
    /** An indefinite time budget. */
    public static final TimeBudget INDEFINITE = new TimeBudget(Long.MAX_VALUE);

    /** A time budget with no time. */
    public static final TimeBudget ZERO = new TimeBudget(0L);

    /**
     * The time that the budget expires, to be compared to
     * {@link System#nanoTime()}.
     */
    private final long myExpirationTimeNanoseconds;

    /**
     * Start a time budget.
     *
     * @param milliseconds The magnitude of the budget, in milliseconds.
     * @return The new time budget.
     */
    public static TimeBudget startMilliseconds(long milliseconds)
    {
        long now = System.nanoTime();
        if (millisecondsOverflow(now, milliseconds))
        {
            return INDEFINITE;
        }
        return new TimeBudget(now + milliseconds * Constants.NANO_PER_MILLI);
    }

    /**
     * Start a time budget.
     *
     * @param nanoseconds The magnitude of the budget, in nanoseconds.
     * @return The new time budget.
     */
    public static TimeBudget startNanoseconds(long nanoseconds)
    {
        long now = System.nanoTime();
        if (nanosecondsOverflow(now, nanoseconds))
        {
            return INDEFINITE;
        }
        return new TimeBudget(now + nanoseconds);
    }

    /**
     * Determine if a number of milliseconds is too large.
     *
     * @param now The current time, as returned from {@link System#nanoTime()}.
     * @param milliseconds The milliseconds being tested.
     * @return If the milliseconds are too large, {@code true}.
     */
    protected static boolean millisecondsOverflow(long now, long milliseconds)
    {
        return milliseconds > (Long.MAX_VALUE - now) / Constants.NANO_PER_MILLI;
    }

    /**
     * Determine if a number of nanoseconds is too large.
     *
     * @param now The current time, as returned from {@link System#nanoTime()}.
     * @param nanoseconds The nanoseconds being tested.
     * @return If the milliseconds are too large, {@code true}.
     */
    protected static boolean nanosecondsOverflow(long now, long nanoseconds)
    {
        return Utilities.sumOverflow(now, nanoseconds);
    }

    /**
     * Construct a time budget.
     *
     * @param expirationTimeNanoseconds The expiration time in nanoseconds since
     *            epoch.
     */
    protected TimeBudget(long expirationTimeNanoseconds)
    {
        myExpirationTimeNanoseconds = expirationTimeNanoseconds;
    }

    /**
     * Check the time budget. Throw an exception if the budget is expired.
     *
     * @throws TimeBudgetExpiredException If the time budget has expired.
     */
    public void check() throws TimeBudgetExpiredException
    {
        if (isExpired())
        {
            throw new TimeBudgetExpiredException();
        }
    }

    /**
     * Get the remaining budget in milliseconds.
     *
     * @return The remaining budget.
     */
    public int getRemainingMilliseconds()
    {
        return (int)(getRemainingNanoseconds() / Constants.NANO_PER_MILLI);
    }

    /**
     * Get the remaining budget in nanoseconds.
     *
     * @return The remaining budget.
     */
    public long getRemainingNanoseconds()
    {
        long result = getExpirationTimeNanoseconds() - System.nanoTime();
        return result > 0 ? result : 0;
    }

    /**
     * Get if the time budget is expired.
     *
     * @return If the budget is expired, {@code true}.
     */
    public boolean isExpired()
    {
        return System.nanoTime() >= getExpirationTimeNanoseconds();
    }

    /**
     * Start a new time budget that ends either at the end of this time budget
     * or after the provided number of milliseconds, whichever comes sooner.
     *
     * @param milliseconds The maximum magnitude of the budget.
     * @return The new time budget.
     */
    public TimeBudget subBudgetMilliseconds(long milliseconds)
    {
        long now = System.nanoTime();
        if (millisecondsOverflow(now, milliseconds) || now + milliseconds > getExpirationTimeNanoseconds())
        {
            return this;
        }
        return TimeBudget.startMilliseconds(milliseconds);
    }

    @Override
    public String toString()
    {
        return new StringBuilder(32).append(getClass().getSimpleName()).append("[remaining ").append(getRemainingNanoseconds())
                .append(" ns]").toString();
    }

    /**
     * Get the expiration time in nanoseconds, to be compared to
     * {@link System#nanoTime()}.
     *
     * @return The expiration time.
     */
    protected long getExpirationTimeNanoseconds()
    {
        return myExpirationTimeNanoseconds;
    }

    /**
     * Exception indicating that a time budget has expired.
     */
    public static class TimeBudgetExpiredException extends Exception
    {
        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         */
        public TimeBudgetExpiredException()
        {
            super("Time budget expired.");
        }
    }
}
