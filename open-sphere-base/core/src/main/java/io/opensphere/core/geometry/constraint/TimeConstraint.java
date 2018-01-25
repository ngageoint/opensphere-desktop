package io.opensphere.core.geometry.constraint;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.SharedObjectPool;

/**
 * Constrains geometry visibility based on current time frame.
 */
@Immutable
public class TimeConstraint
{
    /** Cache for time constraints. */
    private static final SharedObjectPool<TimeConstraint> ourPool = new SharedObjectPool<>();

    /**
     * Indicates if this time constraint is a most recent time constraint so
     * that only the most recent geometry will will be displayed even though
     * there are other geometries within the active window.
     */
    private final boolean myIsMostRecent;

    /**
     * The key associated with this time constraint. Active time spans may be
     * associated with specific keys.
     */
    private final Object myKey;

    /** The latest time, in milliseconds since Java epoch. */
    private final long myMaximumTime;

    /** The earliest time, in milliseconds since Java epoch. */
    private final long myMinimumTime;

    /** Flag indicating if the check should be negated. */
    private final boolean myNegative;

    /**
     * Construct a negative time constraint.
     *
     * @param key The key associated with this constraint. The key must be an
     *            immutable object.
     * @param timeSpan The time span.
     * @return A time constraint.
     */
    public static synchronized TimeConstraint getNegativeTimeConstraint(Object key, TimeSpan timeSpan)
    {
        return ourPool.get(new TimeConstraint(key, timeSpan.isUnboundedStart() ? Long.MIN_VALUE : timeSpan.getStart(),
                timeSpan.isUnboundedEnd() ? Long.MAX_VALUE : timeSpan.getEnd(), true, false));
    }

    /**
     * Construct a negative time constraint.
     *
     * @param timeSpan The time span.
     * @return A time constraint.
     */
    public static synchronized TimeConstraint getNegativeTimeConstraint(TimeSpan timeSpan)
    {
        return ourPool.get(new TimeConstraint(null, timeSpan.isUnboundedStart() ? Long.MIN_VALUE : timeSpan.getStart(),
                timeSpan.isUnboundedEnd() ? Long.MAX_VALUE : timeSpan.getEnd(), true, false));
    }

    /**
     * Construct a most recent time constraint.
     *
     * @param key The key of this constraint so all similar constraints can be
     *            grouped to compare for most recent time.
     * @param time The time instant of the constraint.
     * @return A time constraint.
     */
    public static synchronized TimeConstraint getMostRecentTimeConstraint(Object key, long time)
    {
        return ourPool.get(new TimeConstraint(key, time, true));
    }

    /**
     * Construct a time constraint.
     *
     * @param key The key associated with this constraint. The key must be an
     *            immutable object.
     * @param timeSpan The time span.
     * @return A time constraint.
     */
    public static synchronized TimeConstraint getTimeConstraint(Object key, TimeSpan timeSpan)
    {
        return ourPool.get(new TimeConstraint(key, timeSpan.isUnboundedStart() ? Long.MIN_VALUE : timeSpan.getStart(),
                timeSpan.isUnboundedEnd() ? Long.MAX_VALUE : timeSpan.getEnd()));
    }

    /**
     * Construct a time constraint.
     *
     * @param timeSpan The time span.
     * @return A time constraint.
     */
    public static synchronized TimeConstraint getTimeConstraint(TimeSpan timeSpan)
    {
        return ourPool.get(new TimeConstraint(null, timeSpan.isUnboundedStart() ? Long.MIN_VALUE : timeSpan.getStart(),
                timeSpan.isUnboundedEnd() ? Long.MAX_VALUE : timeSpan.getEnd()));
    }

    /**
     * Accessor for the object pool.
     *
     * @return The pool.
     */
    protected static SharedObjectPool<TimeConstraint> getPool()
    {
        return ourPool;
    }

    /**
     * Construct a time constraint.
     *
     * @param key The key associated with this constraint.
     * @param time The time instant of this time constraint.
     * @param isMostRecent Indicates if this time constraint is a most recent
     *            time constraint so that only the most recent geometry will
     *            will be displayed even though there are other geometries
     *            within the active window.
     */
    protected TimeConstraint(Object key, long time, boolean isMostRecent)
    {
        this(key, time, time, false, isMostRecent);
    }

    /**
     * Construct a time constraint.
     *
     * @param key The key associated with this constraint.
     * @param minTime The early time boundary in milliseconds since Java epoch.
     * @param maxTime The late time boundary in milliseconds since Java epoch.
     */
    protected TimeConstraint(Object key, long minTime, long maxTime)
    {
        this(key, minTime, maxTime, false, false);
    }

    /**
     * Construct a time constraint.
     *
     * @param key The key associated with this constraint.
     * @param minTime The early time boundary in milliseconds since Java epoch.
     * @param maxTime The late time boundary in milliseconds since Java epoch.
     * @param negate Flag indicating if the check should be negated.
     * @param isMostRecent Indicates if this time constraint is a most recent
     *            time constraint so that only the most recent geometry will
     *            will be displayed even though there are other geometries
     *            within the active window.
     */
    protected TimeConstraint(Object key, long minTime, long maxTime, boolean negate, boolean isMostRecent)
    {
        myKey = key;
        myMinimumTime = minTime;
        myMaximumTime = maxTime;
        myNegative = negate;
        myIsMostRecent = isMostRecent;
    }

    /**
     * Construct a time constraint.
     *
     * @param key The key associated with this constraint.
     * @param span The acceptable time span.
     * @param negate Flag indicating if the check should be negated.
     */
    protected TimeConstraint(Object key, TimeSpan span, boolean negate)
    {
        this(key, span.isUnboundedStart() ? Long.MIN_VALUE : span.getStart(),
                span.isUnboundedEnd() ? Long.MAX_VALUE : span.getEnd(), negate, false);
    }

    /**
     * Check this constraint against the input time span list.
     *
     * @param timeSpans The time span list to check against.
     * @return <code>true</code> if the geometry should be visible.
     */
    public boolean check(Collection<? extends TimeSpan> timeSpans)
    {
        if (isNegative())
        {
            for (TimeSpan span : timeSpans)
            {
                if (!check(span))
                {
                    return false;
                }
            }

            return true;
        }
        else
        {
            for (TimeSpan span : timeSpans)
            {
                if (check(span))
                {
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * Check this constraint against the input time span.
     *
     * @param span The time span to check against.
     * @return <code>true</code> if the geometry should be visible over this
     *         time span.
     */
    public boolean check(TimeSpan span)
    {
        return isNegative() ^ (span.isTimeless() || (span.isUnboundedStart() ? span.getEnd() > myMinimumTime
                : span.isUnboundedEnd() ? span.getStart() < myMaximumTime
                        : span.getStart() < myMaximumTime && span.getStart() >= myMinimumTime
                                || myMinimumTime < span.getEnd() && myMinimumTime >= span.getStart()));
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        TimeConstraint other = (TimeConstraint)obj;
        return myKey == other.myKey && myMaximumTime == other.myMaximumTime && myMinimumTime == other.myMinimumTime
                && myNegative == other.myNegative;
    }

    /**
     * Get the constraint key.
     *
     * @return The object, which may be null.
     */
    @Nullable
    public Object getKey()
    {
        return myKey;
    }

    /**
     * Get the time span of this constraint.
     *
     * @return The time span.
     */
    public TimeSpan getTimeSpan()
    {
        return TimeSpan.fromLongs(myMinimumTime, myMaximumTime, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myKey == null ? 0 : myKey.hashCode());
        result = prime * result + (int)(myMaximumTime ^ myMaximumTime >>> 32);
        result = prime * result + (int)(myMinimumTime ^ myMinimumTime >>> 32);
        result = prime * result + (myNegative ? 1231 : 1237);
        return result;
    }

    /**
     * Gets the flag that indicates if this time constraint is a most recent
     * time constraint so that only the most recent geometry will will be
     * displayed even though there are other geometries within the active
     * window.
     *
     * @return True if most recent constraint, false otherwise.
     */
    public boolean isMostRecent()
    {
        return myIsMostRecent;
    }

    /**
     * Get if the check is negated (the time spans must not overlap for the
     * check to be successful).
     *
     * @return {@code true} if the check is negative.
     */
    public boolean isNegative()
    {
        return myNegative;
    }

    @Override
    public String toString()
    {
        return new StringBuilder(128).append(getClass().getSimpleName()).append(" [").append(getTimeSpan())
                .append(isNegative() ? " (negative)" : "").append(']').toString();
    }
}
