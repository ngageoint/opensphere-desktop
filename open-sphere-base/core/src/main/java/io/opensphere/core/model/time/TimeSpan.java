package io.opensphere.core.model.time;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.concurrent.Immutable;

import io.opensphere.core.model.RangeRelationType;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.SizeProvider;
import io.opensphere.core.util.Utilities;

/**
 * <p>
 * Representation of a span of time. This is an abstract class to allow for
 * optimized storage implementations. There are various {@code get} methods that
 * should be used to get {@linkplain TimeSpan} instances. {@linkplain TimeSpan}s
 * are immutable, and multiple calls to {@code get} methods may return duplicate
 * instances.
 * </p>
 * To create a {@linkplain TimeSpan} with a start and end time, you may use:
 * <ul>
 * <li>{@link #get(Date, Date)}</li>
 * <li>{@link #get(long, long)}</li>
 * </ul>
 * To create a {@linkplain TimeSpan} with a start or end time and a duration,
 * you may use:
 * <ul>
 * <li>{@link #get(Date, Duration)}</li>
 * <li>{@link #get(long, Duration)}</li>
 * <li>{@link #get(Duration, Date)}</li>
 * <li>{@link #get(Duration, long)}</li>
 * </ul>
 * To create an instantaneous {@linkplain TimeSpan}:
 * <ul>
 * <li>{@link #get()} for the current time</li>
 * <li>{@link #get(Date)}</li>
 * <li>{@link #get(long)}</li>
 * </ul>
 * To create a {@linkplain TimeSpan} that is unbounded on one end:
 * <ul>
 * <li>{@link #newUnboundedEndTimeSpan(Date)}</li>
 * <li>{@link #newUnboundedEndTimeSpan(long)}</li>
 * <li>{@link #newUnboundedStartTimeSpan(Date)}</li>
 * <li>{@link #newUnboundedStartTimeSpan(long)}</li>
 * </ul>
 * <p>
 * There are two special cases:
 * </p>
 * <ul>
 * <li>{@link #ZERO} - the instantaneous time span that represents no time</li>
 * <li>{@link #TIMELESS} - the unbounded time span that represents all time</li>
 * </ul>
 * <p>
 * Note that the end value of a span is exclusive. The end time is treated as an
 * instant, so the time span 00:00:00-01:00:00 does not overlap the time span
 * 01:00:00-02:00:00.
 * </p>
 */
@Immutable
@SuppressWarnings("PMD.GodClass")
public abstract class TimeSpan implements Comparable<TimeSpan>, Serializable, TimeSpanProvider, SizeProvider
{
    /**
     * The unique identifier used in serialization operations.
     */
    private static final long serialVersionUID = -2489726809756583112L;

    /**
     * Constant instance for a time span that comprises all time.
     */
    public static final TimeSpan TIMELESS = new TimelessTimeSpan();

    /**
     * Constant instance for an empty time span.
     */
    public static final TimeSpan ZERO = new ZeroTimeSpan();

    /**
     * A reference time near the current time to reduce the memory necessary to
     * represent times close to now.
     */
    protected static final long REFERENCE_TIME = System.currentTimeMillis() / Constants.MILLI_PER_UNIT * Constants.MILLI_PER_UNIT;

    /**
     * Parse the input string as an ISO8601 interval and return the equivalent
     * {@link TimeSpan}.
     *
     * @param input The input string.
     * @return The time span.
     * @throws ParseException If the string cannot be parsed.
     */
    public static TimeSpan fromISO8601String(String input) throws ParseException
    {
        Utilities.checkNull(input, "input");
        String[] split = input.split("/");
        if (split.length != 2)
        {
            throw new ParseException("Failed to find '/' in input string [" + input + "].", 0);
        }
        Date start = DateTimeUtilities.parseISO8601Date(split[0]);
        Date end = DateTimeUtilities.parseISO8601Date(split[1]);
        return TimeSpan.get(start, end);
    }

    /**
     * Get a time span with the given start and end times. If {@code start} is
     * equal to {@code unboundedStart} or {@code end} is equal to
     * {@code unboundedEnd} , an unbounded time span will be returned.
     *
     * @param start The start time in milliseconds since Java epoch.
     * @param end The end time in milliseconds since Java epoch. (exclusive)
     * @param unboundedStart The start value that indicates an unbounded start.
     * @param unboundedEnd The end value that indicates an unbounded end.
     * @return The time span.
     */
    public static TimeSpan fromLongs(long start, long end, long unboundedStart, long unboundedEnd)
    {
        return start == unboundedStart ? end == unboundedEnd ? TimeSpan.TIMELESS : TimeSpan.newUnboundedStartTimeSpan(end)
                : end == unboundedEnd ? TimeSpan.newUnboundedEndTimeSpan(start) : TimeSpan.get(start, end);
    }

    /**
     * Returns a TimeSpan initialized with the current time for both start and
     * end.
     *
     * @return The time span.
     */
    public static TimeSpan get()
    {
        return get(new Date());
    }

    /**
     * Creates an instantaneous timespan where the start and end are the same.
     *
     * @param instantaneous the instantaneous time for the span.
     * @return the Time span
     */
    public static TimeSpan get(Date instantaneous)
    {
        TimeSpan returnValue;
        if (instantaneous == null)
        {
            returnValue = TIMELESS;
        }
        else
        {
            returnValue = TimeSpan.get(instantaneous.getTime(), instantaneous.getTime());
        }

        return returnValue;
    }

    /**
     * Converts a start and end {@link Date} to a {@link TimeSpan}.  Unlike the
     * get method with the same signature, if one boundary is null, the result
     * is a degenerate time span (a point).
     *
     * @param start - the start date
     * @param end - the end date
     * @return a {@link TimeSpan} or TimeSpan.TIMELESS.
     */
    public static TimeSpan spanOrPt (Date start, Date end)
    {
        if (start != null && end != null)
            return TimeSpan.get(start.getTime(), end.getTime());
        else if (start != null)
            return TimeSpan.get(start.getTime(), start.getTime());
        else if (end != null)
            return TimeSpan.get(end.getTime(), end.getTime());
        return TimeSpan.TIMELESS;
    }

    /**
     * Get a time span with the given start and end {@link Date}s. If either
     * {@link Date} is {@code null}, that end of the span will be unbounded.
     *
     * @param start The start date.
     * @param end The end date.
     * @return The span.
     */
    public static TimeSpan get(Date start, Date end)
    {
        if (start != null && end != null)
            return TimeSpan.get(start.getTime(), end.getTime());
        else if (start != null)
            return newUnboundedEndTimeSpan(start);
        else if (end != null)
            return newUnboundedStartTimeSpan(end);
        return TIMELESS;
    }

    /**
     * get a time span with the given start {@link Date} and {@link Duration}.
     *
     * @param start The start date.
     * @param dur The duration.
     * @return The span.
     */
    public static TimeSpan get(Date start, Duration dur)
    {
        return get(start.getTime(), dur);
    }

    /**
     * get a time span with the given start {@link Date} and {@link Duration}.
     *
     * @param dur The duration.
     * @param end The end date.
     * @return The span.
     */
    public static TimeSpan get(Duration dur, Date end)
    {
        return get(dur, end.getTime());
    }

    /**
     * Get a time span with the given {@link Duration} and end time.
     *
     * @param dur The duration of the time span.
     * @param end The start time in milliseconds since Java epoch.
     * @return The span.
     */
    public static TimeSpan get(Duration dur, long end)
    {
        TimeSpan returnValue;

        if (dur.isConvertibleTo(Milliseconds.ONE))
        {
            long ms = Duration.create(Milliseconds.class, dur).longValue();
            if (ms < 0L)
            {
                throw new IllegalArgumentException("Cannot create time span with negative duration.");
            }
            if (end < 0L && end - Long.MIN_VALUE < ms)
            {
                throw new IllegalArgumentException("Duration [" + dur + "] is too large for end [" + end + "]");
            }

            if (dur.isZero() || dur instanceof Seconds)
            {
                returnValue = get(end - ms, end);
            }
            else
            {
                returnValue = get(end - ms, end, dur);
            }
        }
        else
        {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(end);
            dur.negate().addTo(cal);
            returnValue = get(cal.getTimeInMillis(), end, dur);
        }

        return returnValue;
    }

    /**
     * get a time span with the given end {@link TimeInstant} and
     * {@link Duration}.
     *
     * @param dur The duration.
     * @param end The end time.
     * @return The span.
     */
    public static TimeSpan get(Duration dur, TimeInstant end)
    {
        return get(dur, end.getEpochMillis());
    }

    /**
     * Creates an instantaneous timespan where the start and end are the same.
     *
     * @param time The time in milliseconds since Java epoch.
     * @return The span.
     */
    public static TimeSpan get(long time)
    {
        return TimeSpan.get(time, time);
    }

    /**
     * Get a time span with the given start time and {@link Duration}.
     *
     * @param start The start time in milliseconds since Java epoch.
     * @param dur The duration of the time span.
     * @return The span.
     */
    public static TimeSpan get(long start, Duration dur)
    {
        TimeSpan returnValue;

        if (dur.isConvertibleTo(Milliseconds.class))
        {
            long ms = Duration.create(Milliseconds.class, dur).longValue();
            if (ms < 0L)
            {
                throw new IllegalArgumentException("Cannot create time span with negative duration.");
            }
            if (start >= 0L && Long.MAX_VALUE - start < ms)
            {
                throw new IllegalArgumentException("Duration [" + dur + "] is too large for start [" + start + "]");
            }
            if (dur.isZero() || dur instanceof Seconds)
            {
                returnValue = get(start, start + ms);
            }
            else
            {
                returnValue = get(start, start + ms, dur);
            }
        }
        else
        {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(start);
            dur.addTo(cal);
            returnValue = get(start, cal.getTimeInMillis(), dur);
        }

        return returnValue;
    }

    /**
     * Get a time span with the given start and end times.
     *
     * @param start The start time in milliseconds since Java epoch.
     * @param end The end time in milliseconds since Java epoch. (exclusive)
     * @return The span.
     */
    public static TimeSpan get(long start, long end)
    {
        TimeSpan returnValue;

        if (start == 0L && end == 0L)
        {
            returnValue = ZERO;
        }
        else if (start == end)
        {
            returnValue = new TimeSpanLong(start);
        }
        else if (TimeSpanIntIntSeconds.canConstruct(start, end))
        {
            returnValue = new TimeSpanIntIntSeconds(start, end);
        }
        else
        {
            returnValue = new TimeSpanLongLong(start, end);
        }

        return returnValue;
    }

    /**
     * Create an instantaneous timespan where the start and end are the same.
     *
     * @param instant the instantaneous time for the span.
     * @return The timespan.
     */
    public static TimeSpan get(TimeInstant instant)
    {
        return TimeSpan.get(instant, instant);
    }

    /**
     * get a time span with the given start {@link TimeInstant} and
     * {@link Duration}.
     *
     * @param start The start time.
     * @param dur The duration.
     * @return The span.
     */
    public static TimeSpan get(TimeInstant start, Duration dur)
    {
        return get(start.getEpochMillis(), dur);
    }

    /**
     * Get a time span with the given start and end times.
     *
     * @param start The start time in milliseconds since Java epoch.
     * @param end The end time in milliseconds since Java epoch. (exclusive)
     * @return The span.
     */
    public static TimeSpan get(TimeInstant start, TimeInstant end)
    {
        return get(start.getEpochMillis(), end.getEpochMillis());
    }

    /**
     * Static factory method to create a TimeSpan with an unbounded end time.
     *
     * @param start The start time as a {@link Date}.
     * @return The TimeSpan
     */
    public static TimeSpan newUnboundedEndTimeSpan(Date start)
    {
        return newUnboundedEndTimeSpan(start.getTime());
    }

    /**
     * Static factory method to create a TimeSpan with an unbounded end time.
     *
     * @param start The start time in milliseconds since Java epoch.
     * @return The TimeSpan
     */
    public static TimeSpan newUnboundedEndTimeSpan(long start)
    {
        return new UnboundedEndLong(start);
    }

    /**
     * Static factory method to create a TimeSpan with an unbounded start time.
     *
     * @param end The end time as a {@link Date}.
     * @return The TimeSpan.
     */
    public static TimeSpan newUnboundedStartTimeSpan(Date end)
    {
        return newUnboundedStartTimeSpan(end.getTime());
    }

    /**
     * Static factory method to create a TimeSpan with an unbounded start time.
     *
     * @param end The end time in milliseconds since Java epoch. (exclusive)
     * @return The TimeSpan
     */
    public static TimeSpan newUnboundedStartTimeSpan(long end)
    {
        return new UnboundedStartLong(end);
    }

    /**
     * Get a time span with the given start and end times, and a duration.
     *
     * @param start The start time in milliseconds since Java epoch.
     * @param end The end time in milliseconds since Java epoch. (exclusive)
     * @param duration The duration of the time span.
     * @return The span.
     */
    private static TimeSpan get(long start, long end, Duration duration)
    {
        TimeSpan returnValue;

        if (TimeSpanIntIntSeconds.canConstruct(start, end))
        {
            returnValue = new TimeSpanIntIntSecondsDuration(start, end, duration);
        }
        else
        {
            returnValue = new TimeSpanLongLongDuration(start, end, duration);
        }

        return returnValue;
    }

    /**
     * Clamp a time instant to be between the start and end of this time span,
     * inclusive.
     *
     * @param t The input time.
     * @return The result time.
     */
    public TimeInstant clamp(TimeInstant t)
    {
        TimeInstant start = getStartInstant();
        if (start.compareTo(t) > 0)
        {
            return start;
        }
        TimeInstant end = getEndInstant();
        return end.compareTo(t) < 0 ? end : t;
    }

    /**
     * Clamp a time span to be between the start and end of this time span,
     * inclusive.
     *
     * @param t The input time.
     * @return The result time.
     */
    public TimeSpan clamp(TimeSpan t)
    {
        if (t.compareStart(getStart()) < 0)
        {
            TimeInstant start = clamp(t.getStartInstant());
            TimeInstant end = clamp(start.plus(t.getDuration()));
            return TimeSpan.get(start, end);
        }
        else if (t.compareEnd(getEnd()) > 0)
        {
            TimeInstant end = clamp(t.getEndInstant());
            TimeInstant start = clamp(end.minus(t.getDuration()));
            return TimeSpan.get(start, end);
        }
        else
        {
            return t;
        }
    }

    /**
     * Compare a time with my end.
     *
     * @param time The time.
     * @return -1 if my end time is before the input time, 0 if my end time is
     *         equal to the input time, 1 if my end time is after the input
     *         time.
     */
    public abstract int compareEnd(long time);

    /**
     * Compare a time with my start.
     *
     * @param time The time.
     * @return -1 if my start time is before the input time, 0 if my start time
     *         is equal to the input time, 1 if my start time is after the input
     *         time.
     */
    public abstract int compareStart(long time);

    /**
     * Determines if this time span contains (inclusive) another time span.
     *
     * @param other the other time span to test
     * @return true, if either time span is Timeless or the other time span is
     *         entirely contained within this time span.
     */
    public abstract boolean contains(TimeSpan other);

    @Override
    public abstract boolean equals(Object obj);

    /**
     * Returns true if the specified TimeSpan can combine with this time span
     * and form a contiguous range.
     *
     * @param other the other to test against
     * @return true, if it forms a contiguous time range.
     */
    public boolean formsContiguousRange(TimeSpan other)
    {
        return overlaps(other) || touches(other);
    }

    /**
     * Get the duration of the TimeSpan.
     *
     * @return The duration.
     * @throws UnsupportedOperationException If this time span is unbounded.
     */
    public abstract Duration getDuration();

    /**
     * Gets the duration in milliseconds of the TimeSpan.
     *
     * @return The duration in ms.
     * @throws ArithmeticException If the duration is too large to put in a
     *             {@code long}.
     * @throws UnsupportedOperationException If this time span is unbounded.
     */
    public abstract long getDurationMs();

    /**
     * Get the end time of this time span.
     *
     * @return The end in milliseconds since Java epoch.
     * @throws UnsupportedOperationException If this time span has an unbounded
     *             end.
     */
    public abstract long getEnd();

    /**
     * Get the end time of this time span, returning the specified value if the
     * end is not bounded.
     *
     * @param unboundedValue The value to return if the end is unbounded.
     * @return The end in milliseconds since Java epoch.
     */
    public abstract long getEnd(long unboundedValue);

    /**
     * Get the end time of this time span.
     *
     * @return The end as a {@code Date}.
     * @throws UnsupportedOperationException If this time span has an unbounded
     *             end.
     */
    public Date getEndDate()
    {
        return new Date(getEnd());
    }

    /**
     * Get the end time of this time span.
     *
     * @param unboundedValue The value to return if the end is unbounded.
     * @return The end as a {@code Date}.
     */
    public Date getEndDate(Date unboundedValue)
    {
        return isUnboundedEnd() ? unboundedValue : new Date(getEnd());
    }

    /**
     * Get the end time of this time span.
     *
     * @return The end time.
     * @throws UnsupportedOperationException If this time span has an unbounded
     *             end.
     */
    public TimeInstant getEndInstant()
    {
        return TimeInstant.get(getEnd());
    }

    /**
     * Get the duration gap between the closest edges of this time span and
     * another time span. If the time spans overlap, the gap is zero.
     *
     * @param other The other time span.
     * @return The gap between the time spans.
     */
    public abstract Duration getGapBetween(TimeSpan other);

    /**
     * Get the intersection of this time span with another time span.
     *
     * @param other The other time span.
     * @return The intersection or <code>null</code> if there was none.
     */
    public abstract TimeSpan getIntersection(TimeSpan other);

    /**
     * Get the midpoint of the time span.
     *
     * @return The midpoint in milliseconds since Java epoch.
     */
    public long getMidpoint()
    {
        return (getStart() >> 1) + (getEnd() >> 1) + (getStart() & getEnd() & 1L);
    }

    /**
     * Get the midpoint of the time span.
     *
     * @return The midpoint as a {@code Date}.
     */
    public Date getMidpointDate()
    {
        return new Date(getMidpoint());
    }

    /**
     * Get the midpoint of the time span.
     *
     * @return The midpoint.
     */
    public TimeInstant getMidpointInstant()
    {
        return TimeInstant.get(getMidpoint());
    }

    /**
     * Determine the relationship between this time span and another.
     *
     * @param other The other time span.
     * @return The relation of this time span to the other time span.
     */
    public abstract RangeRelationType getRelation(TimeSpan other);

    /**
     * Get the start time of this time span.
     *
     * @return The start in milliseconds since Java epoch.
     * @throws UnsupportedOperationException If this time span has an unbounded
     *             start.
     */
    public abstract long getStart();

    /**
     * Get the start time of this time span, returning the specified value if
     * the start is not bounded.
     *
     * @param unboundedValue The value to return if the start is unbounded.
     * @return The start in milliseconds since Java epoch.
     */
    public abstract long getStart(long unboundedValue);

    /**
     * Get the start date of this time span.
     *
     * @return The start as a {@code Date}.
     * @throws UnsupportedOperationException If this time span has an unbounded
     *             start.
     */
    public Date getStartDate()
    {
        return new Date(getStart());
    }

    /**
     * Get the start date of this time span.
     *
     * @param unboundedValue The value to return if the start is unbounded.
     * @return The start as a {@code Date}.
     */
    public Date getStartDate(Date unboundedValue)
    {
        return isUnboundedStart() ? unboundedValue : new Date(getStart());
    }

    /**
     * Get the start time of this time span.
     *
     * @return The start time.
     * @throws UnsupportedOperationException If this time span has an unbounded
     *             start.
     */
    public TimeInstant getStartInstant()
    {
        return TimeInstant.get(getStart());
    }

    @Override
    public TimeSpan getTimeSpan()
    {
        return this;
    }

    @Override
    public abstract int hashCode();

    /**
     * Interpolate between this time span and another. This simply interpolates
     * the start and end points.
     *
     * @param other The other time span.
     * @param fraction The fraction of the difference between the time span end
     *            points.
     * @return The interpolated time span.
     * @throws IllegalArgumentException If the input time span is bounded on an
     *             end that I am not or vice-versa.
     */
    public abstract TimeSpan interpolate(TimeSpan other, double fraction);

    /**
     * Checks if this time span is after another {@link TimeSpan}.
     *
     * @param o the time span to test against
     * @return true, if it is after
     */
    public boolean isAfter(TimeSpan o)
    {
        return precedesIntersectsOrTrails(o) < 0;
    }

    /**
     * Checks if this time span is before another {@link TimeSpan}.
     *
     * @param o the time span to test against
     * @return true, if is before
     */
    public boolean isBefore(TimeSpan o)
    {
        return precedesIntersectsOrTrails(o) > 0;
    }

    /**
     * Get if this timespan is bounded on both ends.
     *
     * @return If this is a definite time span.
     */
    public abstract boolean isBounded();

    /**
     * Checks if is instantaneous ( i.e. start == end )
     *
     * @return true, if is instantaneous
     */
    public abstract boolean isInstantaneous();

    /**
     * Get if this time span is the special TIMELESS time span.
     *
     * @return If this is the indefinite time span.
     */
    public boolean isTimeless()
    {
        return this == TIMELESS;
    }

    /**
     * Get if this time span has an unbounded end time.
     *
     * @return If this has an unbounded end time.
     */
    public abstract boolean isUnboundedEnd();

    /**
     * Get if this time span has an unbounded start time.
     *
     * @return If this has an unbounded start time.
     */
    public abstract boolean isUnboundedStart();

    /**
     * Checks if this is equal to the {@link TimeSpan}.ZERO constant.
     *
     * @return true, if is the zero constant.
     */
    public abstract boolean isZero();

    /**
     * Returns a copy of this time span with the specified duration subtracted
     * from the start and end.
     *
     * @param dur the duration to subtract
     * @return the new time span
     */
    public TimeSpan minus(Duration dur)
    {
        if (dur.isZero())
        {
            return this;
        }
        return plus(dur.negate());
    }

    /**
     * Determine if this time span overlaps any of the time spans in a list.
     *
     * @param list The list of time spans.
     * @return <code>true</code> if there is an overlap
     */
    public boolean overlaps(Collection<? extends TimeSpan> list)
    {
        for (TimeSpan timeSpan : list)
        {
            if (overlaps(timeSpan))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine if this time span overlaps an instant in time.
     *
     * @param time The time of interest.
     * @return If this time span overlaps the instant in time.
     */
    public boolean overlaps(Date time)
    {
        return overlaps(time.getTime());
    }

    /**
     * Determine if this time span overlaps an instant in time.
     *
     * @param time The time of interest.
     * @return If this time span overlaps the instant in time.
     */
    public abstract boolean overlaps(long time);

    /**
     * Determine if this time span overlaps an instant in time.
     *
     * @param time The time of interest.
     * @return If this time span overlaps the instant in time.
     */
    public boolean overlaps(TimeInstant time)
    {
        return overlaps(time.getEpochMillis());
    }

    /**
     * Determine if this time span overlaps another time span.
     *
     * @param other The time span of interest.
     * @return If this time span overlaps the other time span.
     */
    public abstract boolean overlaps(TimeSpan other);

    /**
     * Returns a copy of this time span with the specified duration added to the
     * start and end.
     *
     * @param dur the duration to add
     * @return the new time span
     */
    public TimeSpan plus(Duration dur)
    {
        if (dur.isZero())
        {
            return this;
        }
        else if (dur.isConvertibleTo(Milliseconds.class))
        {
            long durMillis = Duration.create(Milliseconds.class, dur).longValue();
            return TimeSpan.get(getStart() + durMillis, getEnd() + durMillis);
        }
        else
        {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(getStart());
            dur.addTo(cal);
            long start = cal.getTimeInMillis();
            cal.setTimeInMillis(getEnd());
            dur.addTo(cal);
            long end = cal.getTimeInMillis();
            return get(start, end, dur);
        }
    }

    /**
     * Tests to see if TimeSpan b precedes, intersects, or trails this TimeSpan.
     *
     * @param b - the range to see if it precedes, intersects, or trails
     *            (inclusive)
     * @return 0 if b intersects, -1 if b precedes, 1 if b trails.
     */
    public abstract int precedesIntersectsOrTrails(TimeSpan b);

    /**
     * Get the time span that is the union of this time span and another time
     * span. If the time spans do not overlap or touch, the result will include
     * the space between them.
     *
     * @param other The other time span.
     * @return The union of the two.
     */
    public abstract TimeSpan simpleUnion(TimeSpan other);

    /**
     * Sub-divides this time span into the specified number of divisions.
     * Minimum division size is 1 ms. Will throw
     * {@link IllegalArgumentException} if the range of the time span divided by
     * the number of divisions is less than 1 ms. If the time is not equally
     * divided into the number of divisions the final bin will be truncated to
     * match the end time for the overall span.
     *
     * @param divisions the number of divisions into which this {@link TimeSpan}
     *            should be divided. ( Must be greater than one. )
     * @return the list of {@link TimeSpan} that represent the divisions.
     * @throws IllegalStateException if this is an unbounded time span ( which
     *             cannot be sub-divided)
     * @throws IllegalArgumentException if the number of divisions is less than
     *             1, or the resultant subdivisions will be less than 1 ms in
     *             width.
     */
    public List<TimeSpan> subDivide(int divisions)
    {
        if (divisions <= 1)
        {
            throw new IllegalArgumentException("divisions must be greater than 1");
        }
        if (!isBounded())
        {
            throw new IllegalStateException("Indefinite time spans cannot be subdivided.");
        }

        double binSizeMs = (double)getDurationMs() / (double)divisions;
        if (binSizeMs < 1.0)
        {
            throw new IllegalArgumentException("Resultant number of subdivisions results in sub span sizes of less than 1 ms");
        }

        long binWidth = (int)Math.round(binSizeMs);
        TimeSpan[] bins = new TimeSpan[divisions];
        long curStart = getStart();
        long curEnd = curStart + binWidth;
        for (int count = 0; count < divisions; count++)
        {
            bins[count] = TimeSpan.get(curStart, curEnd);
            curStart = curEnd;
            if (count < divisions - 2)
            {
                curEnd = curEnd + binWidth;
            }
            else
            {
                curEnd = getEnd();
            }
        }
        return Arrays.asList(bins);
    }

    /**
     * Subtract other time spans from this one.
     *
     * @param others The other time spans.
     * @return The portions of this time span that do not intersect the other
     *         time spans, or the empty list.
     */
    public List<TimeSpan> subtract(Collection<? extends TimeSpan> others)
    {
        List<TimeSpan> results = Collections.singletonList(this);
        for (TimeSpan other : others)
        {
            List<TimeSpan> newResults = new ArrayList<>(results.size() + 1);
            for (TimeSpan result : results)
            {
                newResults.addAll(result.subtract(other));
            }
            results = newResults;
        }
        return results;
    }

    /**
     * Subtract another time span from this one.
     *
     * @param other The other time span.
     * @return The portions of this time span that do not intersect the other
     *         time span, or the empty list if this time span is eclipsed by the
     *         other one.
     */
    public abstract List<TimeSpan> subtract(TimeSpan other);

    /**
     * Returns a String representing this TimeSpan that is suitable for display.
     *
     * @return The display string
     */
    public String toDisplayString()
    {
        return TimeSpanFormatter.toDisplayString(this);
    }

    /**
     * Convert this time span to an ISO8601 interval representation.
     *
     * @return The ISO8601 string.
     */
    public String toISO8601String()
    {
        return TimeSpanFormatter.toISO8601String(this);
    }

    /**
     * Convert the time span to an smartly formatted representation.
     *
     * @return The formatted string.
     */
    public String toSmartString()
    {
        return TimeSpanFormatter.toSmartString(this);
    }

    @Override
    public String toString()
    {
        return TimeSpanFormatter.toString(this);
    }

    /**
     * Get if another {@link TimeSpan} touches this {@link TimeSpan} on one end.
     *
     * @param other The other time span.
     * @return {@code true} if the spans touch (but do not overlap).
     */
    public abstract boolean touches(TimeSpan other);

    /**
     * Get the time span that is the union of this time span and another time
     * span.
     *
     * @param other The other time span.
     * @return The union of the two.
     * @throws IllegalArgumentException If the two time spans do not overlap or
     *             touch.
     */
    public abstract TimeSpan union(TimeSpan other);
}
