package io.opensphere.core.model.time;

import java.math.BigDecimal;

import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;

/**
 * Implementation using two longs.
 */
class TimeSpanLongLong extends BoundedTimeSpan
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The size of the object in bytes. */
    private static final int SIZE_BYTES = MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.LONG_SIZE_BYTES * 2,
            Constants.MEMORY_BLOCK_SIZE_BYTES);

    /** End time (exclusive). */
    private final long myEnd;

    /** Start time. */
    private final long myStart;

    /**
     * Constructor.
     *
     * @param start The start time in milliseconds since Java epoch.
     * @param end The end time in milliseconds since Java epoch. (exclusive)
     */
    public TimeSpanLongLong(long start, long end)
    {
        if (end <= start)
        {
            throw new IllegalArgumentException("Illegal time span: end [" + end + "] <= start [" + start + "]");
        }
        myStart = start;
        myEnd = end;
    }

    @Override
    public Duration getDuration()
    {
        if (myStart >= 0L)
        {
            return Duration.create(Milliseconds.class, myEnd - myStart);
        }
        return Duration.create(Milliseconds.class, new BigDecimal(myEnd).subtract(new BigDecimal(myStart)));
    }

    @Override
    public long getDurationMs() throws ArithmeticException
    {
        if (myStart >= 0L || myStart + Long.MAX_VALUE > myEnd)
        {
            return myEnd - myStart;
        }
        throw new ArithmeticException();
    }

    @Override
    public long getEnd()
    {
        return myEnd;
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
        return myStart;
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
        result = prime * result + (int)(myEnd ^ myEnd >>> 32);
        result = prime * result + (int)(myStart ^ myStart >>> 32);
        return result;
    }

    @Override
    public boolean isInstantaneous()
    {
        return false;
    }
}
