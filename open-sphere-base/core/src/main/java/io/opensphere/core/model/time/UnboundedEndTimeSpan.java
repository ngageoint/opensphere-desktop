package io.opensphere.core.model.time;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.model.RangeRelationType;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.util.Utilities;

/** Time span with an unbounded end and a long integer for the start. */
@SuppressWarnings("PMD.GodClass")
abstract class UnboundedEndTimeSpan extends UnboundedTimeSpan
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
        return Long.compare(getStart(), time);
    }

    @Override
    public int compareTo(TimeSpan o)
    {
        int compareStart = -o.compareStart(getStart());
        return compareStart == 0 ? o.isUnboundedEnd() ? 0 : 1 : compareStart;
    }

    @Override
    public boolean contains(TimeSpan other)
    {
        return other.compareStart(getStart()) >= 0;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj == this || obj instanceof UnboundedEndTimeSpan && ((UnboundedEndTimeSpan)obj).getStart() == getStart();
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
        if (other.isUnboundedEnd())
        {
            return Milliseconds.ZERO;
        }
        long millis = getStart() - other.getEnd();
        return millis > 0L ? Duration.create(Milliseconds.class, millis) : Milliseconds.ZERO;
    }

    @Override
    public TimeSpan getIntersection(TimeSpan other)
    {
        if (other.isUnboundedEnd())
        {
            return other.compareStart(getStart()) > 0 ? other : this;
        }
        else if (other.compareEnd(getStart()) > 0)
        {
            return other.compareStart(getStart()) >= 0 ? other : get(getStart(), other.getEnd());
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
        if (other.compareStart(getStart()) > 0)
        {
            if (other.isUnboundedEnd())
            {
                result = RangeRelationType.OVERLAPS_FRONT_EDGE;
            }
            else
            {
                result = RangeRelationType.SUPERSET;
            }
        }
        else if (other.compareEnd(getStart()) > 0)
        {
            if (other.isUnboundedEnd())
            {
                if (other.compareStart(getStart()) == 0)
                {
                    result = RangeRelationType.EQUAL;
                }
                else
                {
                    result = RangeRelationType.SUPERSET;
                }
            }
            else
            {
                result = RangeRelationType.OVERLAPS_BACK_EDGE;
            }
        }
        else if (other.compareEnd(getStart()) < 0)
        {
            result = RangeRelationType.AFTER;
        }
        else
        {
            result = RangeRelationType.BORDERS_AFTER;
        }

        return result;
    }

    @Override
    public long getStart(long unboundedValue)
    {
        return getStart();
    }

    @Override
    public TimeSpan interpolate(TimeSpan other, double fraction) throws IllegalArgumentException
    {
        if (!other.isUnboundedEnd())
        {
            throw new IllegalArgumentException(
                    "Cannot interpolate between a time span with an unbounded end and a timespan with a bounded end.");
        }
        if (other.isUnboundedStart())
        {
            throw new IllegalArgumentException(
                    "Cannot interpolate between a time span with an unbounded start and a timespan with a bounded start.");
        }
        return newUnboundedEndTimeSpan(Math.round(fraction * other.getStart() + (1. - fraction) * getStart()));
    }

    @Override
    public boolean isUnboundedEnd()
    {
        return true;
    }

    @Override
    public boolean isUnboundedStart()
    {
        return false;
    }

    @Override
    public boolean overlaps(long time)
    {
        return compareStart(time) <= 0;
    }

    @Override
    public boolean overlaps(TimeSpan other)
    {
        return other.compareEnd(getStart()) > 0;
    }

    @Override
    public int precedesIntersectsOrTrails(TimeSpan b)
    {
        if (b.compareEnd(getStart()) < 0)
        {
            return -1;
        }
        return 0;
    }

    @Override
    public TimeSpan simpleUnion(TimeSpan other)
    {
        return other.isUnboundedStart() ? TimeSpan.TIMELESS : newUnboundedEndTimeSpan(Math.min(getStart(), other.getStart()));
    }

    @Override
    public List<TimeSpan> subtract(TimeSpan other)
    {
        List<TimeSpan> result;

        if (other.compareEnd(getStart()) <= 0)
        {
            result = Collections.<TimeSpan>singletonList(this);
        }
        else if (other.compareStart(getStart()) > 0)
        {
            TimeSpan span = get(getStart(), other.getStart());
            if (other.isUnboundedEnd())
            {
                result = Collections.singletonList(span);
            }
            else
            {
                result = Arrays.asList(span, newUnboundedEndTimeSpan(other.getEnd()));
            }
        }
        else if (other.isUnboundedEnd())
        {
            result = Collections.emptyList();
        }
        else
        {
            result = Collections.singletonList(newUnboundedEndTimeSpan(other.getEnd()));
        }

        return result;
    }

    @Override
    public boolean touches(TimeSpan other)
    {
        return other.compareEnd(getStart()) == 0;
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
        if (other.compareEnd(getStart()) < 0)
        {
            throw new IllegalArgumentException("Time spans do not overlap or touch: [" + this + "] and [" + other + "]");
        }
        return newUnboundedEndTimeSpan(Math.min(getStart(), other.getStart()));
    }
}
