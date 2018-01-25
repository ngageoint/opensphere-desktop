package io.opensphere.core.model.time;

import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;

/** Time span with an unbounded end and a long integer for the start. */
class UnboundedEndLong extends UnboundedEndTimeSpan
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The size of the object in bytes. */
    private static final int SIZE_BYTES = MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.LONG_SIZE_BYTES,
            Constants.MEMORY_BLOCK_SIZE_BYTES);

    /** The start time. */
    private final long myStart;

    /**
     * Constructor.
     *
     * @param start The start time.
     */
    public UnboundedEndLong(long start)
    {
        myStart = start;
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
    public int hashCode()
    {
        final int prime = 29;
        int result = 1;
        result = prime * result + (int)(myStart ^ myStart >>> 32);
        return result;
    }

    @Override
    public boolean isZero()
    {
        return false;
    }
}
