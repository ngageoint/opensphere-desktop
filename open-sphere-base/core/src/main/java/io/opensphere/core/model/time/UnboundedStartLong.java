package io.opensphere.core.model.time;

import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;

/** Time span with an unbounded start and a long integer for the end. */
class UnboundedStartLong extends UnboundedStartTimeSpan
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The size of the object in bytes. */
    private static final int SIZE_BYTES = MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.LONG_SIZE_BYTES,
            Constants.MEMORY_BLOCK_SIZE_BYTES);

    /** The end time. */
    private final long myEnd;

    /**
     * Constructor.
     *
     * @param end The end time.
     */
    public UnboundedStartLong(long end)
    {
        myEnd = end;
    }

    @Override
    public long getEnd()
    {
        return myEnd;
    }

    @Override
    public long getSizeBytes()
    {
        return SIZE_BYTES;
    }

    @Override
    public int hashCode()
    {
        final int prime = 37;
        int result = 1;
        result = prime * result + (int)(myEnd ^ myEnd >>> 32);
        return result;
    }
}
