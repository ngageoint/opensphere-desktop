package io.opensphere.core.model.time;

import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;

/**
 * Implementation using one long.
 */
final class TimeSpanLong extends InstantaneousTimeSpan
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The size of the object in bytes. */
    private static final int SIZE_BYTES = MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.LONG_SIZE_BYTES,
            Constants.MEMORY_BLOCK_SIZE_BYTES);

    /** The time in milliseconds since Java epoch. */
    private final long myTime;

    /**
     * Constructor.
     *
     * @param time The time in milliseconds since Java epoch.
     */
    public TimeSpanLong(long time)
    {
        myTime = time;
    }

    /**
     * Compare first using the start time, and then by the end time.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is
     *         less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(TimeSpan o)
    {
        int compareStart = o.compareStart(myTime);
        if (compareStart == 0)
        {
            return -o.compareEnd(myTime);
        }
        return -compareStart;
    }

    @Override
    public boolean contains(TimeSpan other)
    {
        return false;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj == this || obj instanceof InstantaneousTimeSpan && ((InstantaneousTimeSpan)obj).getStart() == getStart();
    }

    @Override
    public long getEnd(long unboundedValue)
    {
        return myTime;
    }

    @Override
    public long getSizeBytes()
    {
        return SIZE_BYTES;
    }

    @Override
    public long getStart()
    {
        return myTime;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int)(myTime ^ myTime >>> 32);
        return result;
    }
}
