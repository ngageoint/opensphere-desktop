package io.opensphere.core.model.time;

import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;

/**
 * Implementation using two ints.
 */
class TimeSpanIntIntSeconds extends BoundedTimeSpan
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The size of the object in bytes. */
    private static final int SIZE_BYTES = MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.INT_SIZE_BYTES * 2,
            Constants.MEMORY_BLOCK_SIZE_BYTES);

    /** End time in seconds from {@link #REFERENCE_TIME} (exclusive). */
    private final int myEnd;

    /** Start time in seconds from {@link #REFERENCE_TIME}. */
    private final int myStart;

    /**
     * Determine if a {@link TimeSpanIntIntSeconds} can be constructed for a
     * particular start and end time in milliseconds since Java epoch.
     *
     * @param start The start time.
     * @param end The end time.
     * @return {@code true} if construction is possible.
     */
    public static boolean canConstruct(long start, long end)
    {
        return start % Constants.MILLI_PER_UNIT == 0L && end % Constants.MILLI_PER_UNIT == 0L
                && Math.abs((start - REFERENCE_TIME) / Constants.MILLI_PER_UNIT) <= Integer.MAX_VALUE
                && Math.abs((end - REFERENCE_TIME) / Constants.MILLI_PER_UNIT) <= Integer.MAX_VALUE;
    }

    /**
     * Constructor.
     *
     * @param start The start time in seconds since
     *            {@link io.opensphere.core.model.time.TimeSpan#REFERENCE_TIME}
     *            .
     * @param end The end time in seconds since
     *            {@link io.opensphere.core.model.time.TimeSpan#REFERENCE_TIME}
     *            . (exclusive)
     */
    public TimeSpanIntIntSeconds(int start, int end)
    {
        if (end <= start)
        {
            throw new IllegalArgumentException("Illegal time span: end [" + end + "] <= start [" + start + "]");
        }
        myStart = start;
        myEnd = end;
    }

    /**
     * Constructor. Fractions of a second will be ignored and values that are
     * out of range from {@link #REFERENCE_TIME} will result in undefined
     * results.
     *
     * @param start The start time in milliseconds since Java epoch.
     * @param end The end time in milliseconds since Java epoch.
     */
    public TimeSpanIntIntSeconds(long start, long end)
    {
        this((int)((start - REFERENCE_TIME) / Constants.MILLI_PER_UNIT),
                (int)((end - REFERENCE_TIME) / Constants.MILLI_PER_UNIT));
    }

    @Override
    public Duration getDuration()
    {
        return Duration.create(Seconds.class, (long)myEnd - myStart);
    }

    @Override
    public long getDurationMs()
    {
        return ((long)myEnd - myStart) * Constants.MILLI_PER_UNIT;
    }

    @Override
    public long getEnd()
    {
        return REFERENCE_TIME + (long)myEnd * Constants.MILLI_PER_UNIT;
    }

    @Override
    public long getEnd(long unboundedValue)
    {
        return getEnd();
    }

    @Override
    public long getSizeBytes()
    {
        return SIZE_BYTES;
    }

    @Override
    public long getStart()
    {
        return REFERENCE_TIME + (long)myStart * Constants.MILLI_PER_UNIT;
    }

    @Override
    public long getStart(long unboundedValue)
    {
        return getStart();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + myEnd;
        result = prime * result + myStart;
        return result;
    }

    @Override
    public boolean isInstantaneous()
    {
        return false;
    }
}
