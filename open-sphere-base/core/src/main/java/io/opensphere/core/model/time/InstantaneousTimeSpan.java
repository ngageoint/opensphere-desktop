package io.opensphere.core.model.time;

import java.util.Collections;
import java.util.List;

import io.opensphere.core.model.RangeRelationType;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.util.Utilities;

/**
 * Base class for instantaneous time spans.
 */
abstract class InstantaneousTimeSpan extends BoundedTimeSpan
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    @Override
    public final Duration getDuration()
    {
        return Seconds.ZERO;
    }

    @Override
    public final long getDurationMs()
    {
        return 0L;
    }

    @Override
    public final long getEnd()
    {
        return getStart();
    }

    @Override
    public final TimeSpan getIntersection(TimeSpan other)
    {
        return other.overlaps(getStart()) ? this : null;
    }

    @Override
    public RangeRelationType getRelation(TimeSpan other)
    {
        RangeRelationType result;
        int compare = other.compareStart(getStart());
        if (compare > 0)
        {
            result = RangeRelationType.BEFORE;
        }
        else
        {
            compare = other.compareEnd(getStart());
            result = compare > 0 ? RangeRelationType.SUBSET
                    : compare < 0 ? RangeRelationType.AFTER : RangeRelationType.BORDERS_AFTER;
        }

        return result;
    }

    @Override
    public long getStart(long unboundedValue)
    {
        return getStart();
    }

    @Override
    public final boolean isBounded()
    {
        return true;
    }

    @Override
    public final boolean isInstantaneous()
    {
        return true;
    }

    @Override
    public boolean isZero()
    {
        return getStart() == 0;
    }

    @Override
    public boolean overlaps(TimeSpan other)
    {
        return equals(other) || other.compareEnd(getStart()) > 0 && other.compareStart(getStart()) <= 0;
    }

    @Override
    public List<TimeSpan> subtract(TimeSpan other)
    {
        return overlaps(other) ? Collections.<TimeSpan>emptyList() : Collections.<TimeSpan>singletonList(this);
    }

    @Override
    public final boolean touches(TimeSpan other)
    {
        return !other.isUnboundedStart() && getEnd() == other.getStart() || !other.isUnboundedEnd() && getEnd() == other.getEnd();
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
        if (!formsContiguousRange(other))
        {
            throw new IllegalArgumentException("Time spans do not overlap or touch: [" + this + "] and [" + other + "]");
        }
        return other;
    }
}
