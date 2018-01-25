package io.opensphere.core.model.time;

import io.opensphere.core.util.Constants;

/**
 * An implementation of the {@link UnboundedTimeSpan} used to represent a time
 * span encompassing no time.
 */
class ZeroTimeSpan extends InstantaneousTimeSpan
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    @Override
    public boolean contains(TimeSpan other)
    {
        return false;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof TimeSpan && ((TimeSpan)obj).isZero();
    }

    @Override
    public long getEnd(long unboundedValue)
    {
        return 0L;
    }

    @Override
    public long getSizeBytes()
    {
        return Constants.OBJECT_SIZE_BYTES;
    }

    @Override
    public long getStart()
    {
        return 0L;
    }

    @Override
    public long getStart(long unboundedValue)
    {
        return getStart();
    }

    @Override
    public int hashCode()
    {
        return 0;
    }

    @Override
    public boolean isZero()
    {
        return true;
    }

    @Override
    public String toString()
    {
        return "TimeSpan [ZERO]";
    }

    @Override
    public TimeSpan union(TimeSpan other)
    {
        return other;
    }
}
