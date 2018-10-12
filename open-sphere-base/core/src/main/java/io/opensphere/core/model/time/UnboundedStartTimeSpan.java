package io.opensphere.core.model.time;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.model.RangeRelationType;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.util.Utilities;

/** Time span with an unbounded start and a long integer for the end. */
@SuppressWarnings("PMD.GodClass")
abstract class UnboundedStartTimeSpan extends UnboundedTimeSpan
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    @Override
    public int compareEnd(long time)
    {
        return Long.compare(getEnd(), time);
    }

    @Override
    public int compareStart(long time)
    {
        return -1;
    }

    @Override
    public int compareTo(TimeSpan o)
    {
        if (o.isUnboundedStart())
        {
            return -o.compareEnd(getEnd());
        }
        return -1;
    }

    @Override
    public boolean contains(TimeSpan other)
    {
        return other.compareEnd(getEnd()) <= 0;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj == this || obj instanceof UnboundedStartTimeSpan && ((UnboundedStartTimeSpan)obj).getEnd() == getEnd();
    }

    @Override
    public long getEnd(long unboundedValue)
    {
        return getEnd();
    }

    @Override
    public Duration getGapBetween(TimeSpan other)
    {
        if (other.isUnboundedStart())
        {
            return Milliseconds.ZERO;
        }
        long millis = other.getStart() - getEnd();
        return millis > 0L ? Duration.create(Milliseconds.class, millis) : Milliseconds.ZERO;
    }

    @Override
    public TimeSpan getIntersection(TimeSpan other)
    {
        if (other.isUnboundedStart())
        {
            return other.compareEnd(getEnd()) < 0 ? other : this;
        }
        else if (other.compareStart(getEnd()) < 0)
        {
            return other.compareEnd(getEnd()) <= 0 ? other : get(other.getStart(), getEnd());
        }
        else
        {
            return null;
        }
    }

    @Override
    public RangeRelationType getRelation(TimeSpan other)
    {
        RangeRelationType result;
        if (other.isUnboundedStart())
        {
            if (other.compareEnd(getEnd()) == 0)
            {
                result = RangeRelationType.EQUAL;
            }
            else if (other.compareEnd(getEnd()) > 0)
            {
                result = RangeRelationType.SUBSET;
            }
            else
            {
                result = RangeRelationType.OVERLAPS_BACK_EDGE;
            }
        }
        else
        {
            if (other.compareStart(getEnd()) > 0)
            {
                result = RangeRelationType.BEFORE;
            }
            else if (other.compareStart(getEnd()) == 0)
            {
                result = RangeRelationType.BORDERS_BEFORE;
            }
            else if (other.compareEnd(getEnd()) >= 0)
            {
                result = RangeRelationType.OVERLAPS_FRONT_EDGE;
            }
            else
            {
                result = RangeRelationType.SUPERSET;
            }
        }

        return result;
    }

    @Override
    public long getStart() throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Cannot get start of unbounded time span.");
    }

    @Override
    public long getStart(long unboundedValue)
    {
        return unboundedValue;
    }

    @Override
    public TimeSpan interpolate(TimeSpan other, double fraction) throws IllegalArgumentException
    {
        if (other.isUnboundedEnd())
        {
            throw new IllegalArgumentException(
                    "Cannot interpolate between a time span with an unbounded end and a timespan with a bounded end.");
        }
        if (!other.isUnboundedStart())
        {
            throw new IllegalArgumentException(
                    "Cannot interpolate between a time span with an unbounded start and a timespan with a bounded start.");
        }
        return newUnboundedStartTimeSpan(Math.round(fraction * other.getEnd() + (1. - fraction) * getEnd()));
    }

    @Override
    public boolean isUnboundedEnd()
    {
        return false;
    }

    @Override
    public boolean isUnboundedStart()
    {
        return true;
    }

    @Override
    public boolean overlaps(long time)
    {
        return compareEnd(time) > 0;
    }

    @Override
    public boolean overlaps(TimeSpan other)
    {
        return other.compareStart(getEnd()) < 0;
    }

    @Override
    public int precedesIntersectsOrTrails(TimeSpan b)
    {
        if (b.compareStart(getEnd()) >= 0)
        {
            return 1;
        }
        return 0;
    }

    @Override
    public TimeSpan simpleUnion(TimeSpan other)
    {
        return other.isUnboundedEnd() ? TimeSpan.TIMELESS : newUnboundedStartTimeSpan(Math.max(getEnd(), other.getEnd()));
    }

    @Override
    public List<TimeSpan> subtract(TimeSpan other)
    {
        List<TimeSpan> result;

        if (other.compareStart(getEnd()) >= 0)
        {
            result = Collections.<TimeSpan>singletonList(this);
        }
        else if (other.compareEnd(getEnd()) < 0)
        {
            TimeSpan span = get(other.getEnd(), getEnd());
            if (other.isUnboundedStart())
            {
                result = Collections.singletonList(span);
            }
            else
            {
                result = Arrays.asList(newUnboundedStartTimeSpan(other.getStart()), span);
            }
        }
        else if (other.isUnboundedStart())
        {
            result = Collections.emptyList();
        }
        else
        {
            result = Collections.singletonList(newUnboundedStartTimeSpan(other.getStart()));
        }

        return result;
    }

    @Override
    public boolean touches(TimeSpan other)
    {
        return other.compareStart(getEnd()) == 0;
    }

    @Override
    public TimeSpan union(TimeSpan other) throws IllegalArgumentException
    {
        Utilities.checkNull(other, "other");
        if (other.isTimeless())
        {
            return other;
        }
        if (equals(other))
        {
            return this;
        }
        if (other.compareStart(getEnd()) > 0)
        {
            throw new IllegalArgumentException("Time spans do not overlap or touch: [" + this + "] and [" + other + "]");
        }
        return newUnboundedStartTimeSpan(Math.max(getEnd(), other.getEnd()));
    }
}
