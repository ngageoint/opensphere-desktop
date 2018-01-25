package io.opensphere.core.model.time;

import java.util.Collection;

import io.opensphere.core.model.Accumulator;

/**
 * An accumulator that determines the overall extent of the time spans added to
 * it.
 */
public class ExtentAccumulator implements Accumulator<TimeSpan>
{
    /** The end of the extent. */
    private long myEnd = Long.MIN_VALUE;

    /** The start of the extent. */
    private long myStart = Long.MAX_VALUE;

    /** If the end of the extent is unbounded. */
    private boolean myUnboundedEnd;

    /** The the start of the extent is unbounded. */
    private boolean myUnboundedStart;

    /**
     * Add a time span to the extent.
     *
     * @param span The time span.
     */
    @Override
    public void add(TimeSpan span)
    {
        if (!myUnboundedStart)
        {
            if (span.isUnboundedStart())
            {
                myUnboundedStart = true;
            }
            else if (span.compareStart(myStart) < 0)
            {
                myStart = span.getStart();
            }
        }
        if (!myUnboundedEnd)
        {
            if (span.isUnboundedEnd())
            {
                myUnboundedEnd = true;
            }
            else if (span.compareEnd(myEnd) > 0)
            {
                myEnd = span.getEnd();
            }
        }
    }

    /**
     * Add time spans to the extent.
     *
     * @param spans The time spans.
     */
    @Override
    public void addAll(Collection<? extends TimeSpan> spans)
    {
        for (TimeSpan span : spans)
        {
            add(span);
        }
    }

    /**
     * Get the overall extents of the time spans that have been added to the
     * accumulator. If no spans have been added, {@link TimeSpan#ZERO} is
     * returned.
     *
     * @return The extent.
     */
    @Override
    public TimeSpan getExtent()
    {
        if (myUnboundedStart)
        {
            if (myUnboundedEnd)
            {
                return TimeSpan.TIMELESS;
            }
            else
            {
                return TimeSpan.newUnboundedStartTimeSpan(myEnd);
            }
        }
        else if (myUnboundedEnd)
        {
            return TimeSpan.newUnboundedEndTimeSpan(myStart);
        }
        else if (myStart > myEnd)
        {
            return TimeSpan.ZERO;
        }
        else
        {
            return TimeSpan.get(myStart, myEnd);
        }
    }
}
