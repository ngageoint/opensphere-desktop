package io.opensphere.core.model.time;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.model.RangeRelationType;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.util.Constants;

/**
 * An implementation of the {@link UnboundedTimeSpan} used to represent a time
 * span encompassing all time.
 */
class TimelessTimeSpan extends UnboundedTimeSpan
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    @Override
    public int compareEnd(long time)
    {
        return 1;
    }

    @Override
    public int compareStart(long time)
    {
        return -1;
    }

    @Override
    public int compareTo(TimeSpan o)
    {
        return equals(o) ? 0 : -1;
    }

    @Override
    public boolean contains(TimeSpan other)
    {
        return true;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj == this;
    }

    @Override
    public long getEnd()
    {
        throw new UnsupportedOperationException("Cannot get end of unbounded time span.");
    }

    @Override
    public long getEnd(long unboundedValue)
    {
        return unboundedValue;
    }

    @Override
    public Duration getGapBetween(TimeSpan other)
    {
        return Milliseconds.ZERO;
    }

    @Override
    public TimeSpan getIntersection(TimeSpan other)
    {
        return other;
    }

    @Override
    public RangeRelationType getRelation(TimeSpan other)
    {
        return other.isTimeless() ? RangeRelationType.EQUAL : RangeRelationType.SUPERSET;
    }

    @Override
    public long getSizeBytes()
    {
        return Constants.OBJECT_SIZE_BYTES;
    }

    @Override
    public long getStart()
    {
        throw new UnsupportedOperationException("Cannot get start of unbounded time span.");
    }

    @Override
    public long getStart(long unboundedValue)
    {
        return unboundedValue;
    }

    @Override
    public int hashCode()
    {
        return 1;
    }

    @Override
    public TimeSpan interpolate(TimeSpan other, double fraction)
    {
        if (equals(other))
        {
            return this;
        }
        else if (other.isUnboundedEnd())
        {
            throw new IllegalArgumentException(
                    "Cannot interpolate between a time span with an unbounded start and a timespan with a bounded start.");
        }
        else
        {
            throw new IllegalArgumentException(
                    "Cannot interpolate between a time span with an unbounded start and a timespan with a bounded end.");
        }
    }

    @Override
    public boolean isTimeless()
    {
        return true;
    }

    @Override
    public boolean isUnboundedEnd()
    {
        return true;
    }

    @Override
    public boolean isUnboundedStart()
    {
        return true;
    }

    @Override
    public boolean overlaps(long time)
    {
        return true;
    }

    @Override
    public boolean overlaps(TimeSpan other)
    {
        return true;
    }

    @Override
    public int precedesIntersectsOrTrails(TimeSpan b)
    {
        return 0;
    }

    @Override
    public TimeSpan simpleUnion(TimeSpan other)
    {
        return this;
    }

    @Override
    public List<TimeSpan> subtract(TimeSpan other)
    {
        if (other.isUnboundedEnd())
        {
            if (other.isUnboundedStart())
            {
                return Collections.emptyList();
            }
            return Collections.singletonList(newUnboundedStartTimeSpan(other.getStart()));
        }
        else if (other.isUnboundedStart())
        {
            return Collections.singletonList(newUnboundedEndTimeSpan(other.getEnd()));
        }
        else
        {
            return Arrays.asList(newUnboundedStartTimeSpan(other.getStart()), newUnboundedEndTimeSpan(other.getEnd()));
        }
    }

    @Override
    public boolean touches(TimeSpan other)
    {
        return false;
    }

    @Override
    public TimeSpan union(TimeSpan other)
    {
        return this;
    }
}
