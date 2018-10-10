package io.opensphere.core.model.time;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.model.RangeRelationType;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.util.Utilities;

/**
 * Base class for bounded time spans.
 */
@SuppressWarnings("PMD.GodClass")
abstract class BoundedTimeSpan extends TimeSpan
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
        return Long.compare(getStart(), time);
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
        int compareStart = o.compareStart(getStart());
        if (compareStart == 0)
        {
            int compareEnd = o.compareEnd(getEnd());
            if (compareEnd > 0)
            {
                return -1;
            }
            else if (compareEnd < 0)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
        return -compareStart;
    }

    @Override
    public boolean contains(TimeSpan other)
    {
        return other.compareStart(getStart()) >= 0 && other.compareStart(getEnd()) < 0 && other.compareEnd(getEnd()) <= 0;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj == this || obj instanceof BoundedTimeSpan && ((BoundedTimeSpan)obj).getStart() == getStart()
                && ((BoundedTimeSpan)obj).getEnd() == getEnd();
    }

    @Override
    public Duration getGapBetween(TimeSpan other)
    {
        if (other.isUnboundedStart())
        {
            if (other.isUnboundedEnd())
            {
                return Milliseconds.ZERO;
            }
            long millis = getStart() - other.getEnd();
            return millis > 0L ? Duration.create(Milliseconds.class, millis) : Milliseconds.ZERO;
        }
        else if (other.isUnboundedEnd())
        {
            long millis = other.getStart() - getEnd();
            return millis > 0L ? Duration.create(Milliseconds.class, millis) : Milliseconds.ZERO;
        }
        else
        {
            long millis = other.getStart() - getEnd();
            if (millis < 0L)
            {
                millis = getStart() - other.getEnd();
            }
            return millis > 0L ? Duration.create(Milliseconds.class, millis) : Milliseconds.ZERO;
        }
    }

    @Override
    public TimeSpan getIntersection(TimeSpan other)
    {
        if (other.isTimeless())
        {
            return this;
        }

        long start = other.compareStart(getStart()) < 0 ? getStart() : other.getStart();
        long end = other.compareEnd(getEnd()) > 0 ? getEnd() : other.getEnd();

        if (start < end)
        {
            if (start == getStart() && end == getEnd())
            {
                return this;
            }
            else if (other.compareStart(start) == 0 && other.compareEnd(end) == 0)
            {
                return other;
            }
            else
            {
                return TimeSpan.get(start, end);
            }
        }
        return null;
    }

    @Override
    public RangeRelationType getRelation(TimeSpan other)
    {
        // This is designed to minimize the number of branches across all
        // possible return values.
        RangeRelationType result;
        if (other.compareStart(getStart()) > 0)
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
        else if (other.compareEnd(getStart()) > 0)
        {
            if (other.compareEnd(getEnd()) == 0)
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
            else if (other.compareEnd(getEnd()) > 0)
            {
                result = RangeRelationType.SUBSET;
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
    public TimeSpan interpolate(TimeSpan other, double fraction) throws IllegalArgumentException
    {
        if (!other.isBounded())
        {
            throw new IllegalArgumentException("Cannot interpolate between a bounded time span and an unbounded time span.");
        }
        else if (fraction == 0.)
        {
            return this;
        }
        else if (fraction == 1.)
        {
            return other;
        }

        long start = Math.round(fraction * other.getStart() + (1. - fraction) * getStart());
        long end = Math.round(fraction * other.getEnd() + (1. - fraction) * getEnd());
        return TimeSpan.get(start, end);
    }

    @Override
    public boolean isBounded()
    {
        return true;
    }

    @Override
    public boolean isUnboundedEnd()
    {
        return false;
    }

    @Override
    public boolean isUnboundedStart()
    {
        return false;
    }

    @Override
    public boolean isZero()
    {
        return getStart() == 0L && getEnd() == 0L;
    }

    @Override
    public boolean overlaps(long time)
    {
        return getStart() <= time && time < getEnd();
    }

    @Override
    public boolean overlaps(TimeSpan other)
    {
        return other.compareEnd(getStart()) > 0 && other.compareStart(getStart()) <= 0
                || other.compareStart(getEnd()) < 0 && other.compareStart(getStart()) >= 0;
    }

    @Override
    public int precedesIntersectsOrTrails(TimeSpan b)
    {
        if (b.compareEnd(getStart()) < 0)
        {
            return -1;
        }
        else if (b.compareStart(getEnd()) >= 0)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    @Override
    public TimeSpan simpleUnion(TimeSpan other)
    {
        Utilities.checkNull(other, "other");
        if (other.isTimeless())
        {
            return other;
        }
        else if (equals(other))
        {
            return this;
        }
        else if (other.isUnboundedStart())
        {
            return newUnboundedStartTimeSpan(Math.max(getEnd(), other.getEnd()));
        }
        else if (other.isUnboundedEnd())
        {
            return newUnboundedEndTimeSpan(Math.min(getStart(), other.getStart()));
        }
        else
        {
            return get(Math.min(getStart(), other.getStart()), Math.max(getEnd(), other.getEnd()));
        }
    }

    @Override
    public List<TimeSpan> subtract(TimeSpan other)
    {
        List<TimeSpan> result;

        if (other.isTimeless())
        {
            result = Collections.emptyList();
        }
        else if (other.compareEnd(getStart()) <= 0 || other.compareStart(getEnd()) >= 0)
        {
            result = Collections.<TimeSpan>singletonList(this);
        }
        else if (other.compareStart(getStart()) > 0)
        {
            TimeSpan span1 = get(getStart(), other.getStart());

            if (other.compareEnd(getEnd()) < 0)
            {
                result = Arrays.asList(span1, get(other.getEnd(), getEnd()));
            }
            else
            {
                result = Collections.singletonList(span1);
            }
        }
        else
        {
            if (other.compareEnd(getEnd()) < 0)
            {
                result = Collections.singletonList(get(other.getEnd(), getEnd()));
            }
            else
            {
                result = Collections.emptyList();
            }
        }

        return result;
    }

    @Override
    public boolean touches(TimeSpan other)
    {
        return other.compareStart(getEnd()) == 0 || other.compareEnd(getStart()) == 0;
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
        if (other.compareEnd(getStart()) < 0 || other.compareStart(getEnd()) > 0)
        {
            throw new IllegalArgumentException("Time spans do not overlap or touch: [" + this + "] and [" + other + "]");
        }
        if (other.isUnboundedStart())
        {
            return newUnboundedStartTimeSpan(Math.max(getEnd(), other.getEnd()));
        }
        else if (other.isUnboundedEnd())
        {
            return newUnboundedEndTimeSpan(Math.min(getStart(), other.getStart()));
        }
        else
        {
            return get(Math.min(getStart(), other.getStart()), Math.max(getEnd(), other.getEnd()));
        }
    }
}
