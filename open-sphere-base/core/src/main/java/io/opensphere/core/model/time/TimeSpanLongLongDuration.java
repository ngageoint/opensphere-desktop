package io.opensphere.core.model.time;

import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.Utilities;

/**
 * Extension to {@link TimeSpanLongLong} that keeps track of the duration used
 * to construct it.
 */
final class TimeSpanLongLongDuration extends TimeSpanLongLong
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The size of the object in bytes. */
    private static final int SIZE_BYTES = MathUtil.roundUpTo(
            Constants.OBJECT_SIZE_BYTES + Constants.LONG_SIZE_BYTES * 2 + Constants.REFERENCE_SIZE_BYTES,
            Constants.MEMORY_BLOCK_SIZE_BYTES);

    /** The duration used to construct the time span. */
    private final Duration myDuration;

    /**
     * Constructor.
     *
     * @param start The start time in milliseconds since Java epoch.
     * @param end The end time in milliseconds since Java epoch. (exclusive)
     * @param duration The duration of the time span.
     */
    public TimeSpanLongLongDuration(long start, long end, Duration duration)
    {
        super(start, end);
        myDuration = Utilities.checkNull(duration, "duration");
    }

    @Override
    public Duration getDuration()
    {
        return myDuration;
    }

    @Override
    public long getSizeBytes()
    {
        return SIZE_BYTES;
    }
}
