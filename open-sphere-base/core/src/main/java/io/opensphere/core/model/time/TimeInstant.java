package io.opensphere.core.model.time;

import java.io.Serializable;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

import net.jcip.annotations.Immutable;

import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.DateTimeFormats;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.SizeProvider;
import io.opensphere.core.util.Utilities;

/**
 * An instant in time.
 */
@Immutable
public abstract class TimeInstant implements Comparable<TimeInstant>, Serializable, SizeProvider
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Get a time instant that represents now.
     *
     * @return The time instant.
     */
    public static TimeInstant get()
    {
        return get(System.currentTimeMillis());
    }

    /**
     * Creates a time instant.
     *
     * @param time A calendar.
     * @return The time instant.
     */
    public static TimeInstant get(Calendar time)
    {
        return get(Utilities.checkNull(time, "time").getTimeInMillis());
    }

    /**
     * Creates a time instant.
     *
     * @param time The time.
     * @return The time instant.
     */
    public static TimeInstant get(Date time)
    {
        return get(Utilities.checkNull(time, "time").getTime());
    }

    /**
     * Creates a time instant.
     *
     * @param epochMillis The milliseconds since Java epoch.
     * @return The time instant.
     */
    public static TimeInstant get(long epochMillis)
    {
        return new TimeInstantLongMilliseconds(epochMillis);
    }

    /**
     * Find the maximum of one or two TimeInstant values.  If both arguments
     * are null, then the method returns null.
     * @param t1 one TimeInstant
     * @param t2 another TimeInstant
     * @return the largest of the non-null arguments, or null
     */
    public static TimeInstant max(TimeInstant t1, TimeInstant t2)
    {
        if (t1 == null)
            return t2;
        if (t2 == null)
            return t1;
        if (t2.isAfter(t1))
            return t2;
        return t1;
    }

    /**
     * Find the minimum of one or two TimeInstant values.  If both arguments
     * are null, then the method returns null.
     * @param t1 one TimeInstant
     * @param t2 another TimeInstant
     * @return the smallest of the non-null arguments, or null
     */
    public static TimeInstant min(TimeInstant t1, TimeInstant t2)
    {
        if (t1 == null)
            return t2;
        if (t2 == null)
            return t1;
        if (t2.isBefore(t1))
            return t2;
        return t1;
    }

    @Override
    public int compareTo(TimeInstant o)
    {
        return Long.compare(getEpochMillis(), o.getEpochMillis());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (!(obj instanceof TimeInstant))
        {
            return false;
        }
        TimeInstant other = (TimeInstant)obj;
        return other.getEpochMillis() == getEpochMillis();
    }

    /**
     * Get the milliseconds since Java epoch.
     *
     * @return The milliseconds.
     */
    public abstract long getEpochMillis();

    @Override
    public int hashCode()
    {
        return Objects.hashCode(Long.valueOf(getEpochMillis()));
    }

    /**
     * Get if this time is after another time.
     *
     * @param other The other time.
     * @return If this time is after the other time.
     */
    public boolean isAfter(TimeInstant other)
    {
        return compareTo(other) > 0;
    }

    /**
     * Get if this time is before another time.
     *
     * @param other The other time.
     * @return If this time is before the other time.
     */
    public boolean isBefore(TimeInstant other)
    {
        return compareTo(other) < 0;
    }

    /**
     * Do any time spans overlap this time instant?
     *
     * @param timeSpans The time spans.
     * @return {@code true} if any of the time spans overlap this instant.
     */
    public boolean isOverlapped(Collection<? extends TimeSpan> timeSpans)
    {
        for (TimeSpan timeSpan : timeSpans)
        {
            if (timeSpan.overlaps(this))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a copy of this time instant with the specified duration
     * subtracted.
     *
     * @param dur The duration to subtract.
     * @return The new time instant.
     */
    public TimeInstant minus(Duration dur)
    {
        if (dur.isZero())
        {
            return this;
        }
        else
        {
            return plus(dur.negate());
        }
    }

    /**
     * Returns the duration between this time instant and another time instant.
     *
     * @param other The other value.
     * @return The duration.
     */
    public Duration minus(TimeInstant other)
    {
        return new Milliseconds(getEpochMillis() - other.getEpochMillis());
    }

    /**
     * Returns a copy of this time instant with the specified duration added.
     *
     * @param dur The duration to add.
     * @return The new time instant.
     */
    public TimeInstant plus(Duration dur)
    {
        if (dur.isZero())
        {
            return this;
        }
        else if (dur.getReferenceUnits() == Milliseconds.ONE.getReferenceUnits())
        {
            return TimeInstant.get(
                    getEpochMillis() + Milliseconds.get(dur).getMagnitude().setScale(0, RoundingMode.HALF_UP).longValueExact());
        }
        else
        {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(getEpochMillis());
            dur.addTo(cal);
            return TimeInstant.get(cal.getTimeInMillis());
        }
    }

    /**
     * Get this time instant as a Java Date, truncating any extra precision.
     *
     * @return The date.
     */
    public Date toDate()
    {
        return new Date(getEpochMillis());
    }

    /**
     * Generate a display string for this time instant, truncating useless
     * precision.
     *
     * @return The display string.
     */
    public String toDisplayString()
    {
        String fmt;
        if (getEpochMillis() % Constants.MILLI_PER_UNIT != 0)
        {
            fmt = DateTimeFormats.DATE_TIME_MILLIS_FORMAT;
        }
        else if (getEpochMillis() % Constants.MILLIS_PER_MINUTE != 0)
        {
            fmt = DateTimeFormats.DATE_TIME_FORMAT;
        }
        else
        {
            fmt = "yyyy-MM-dd HH:mm";
        }

        return toDisplayString(fmt);
    }

    /**
     * Generate a display string for this time instant using the provided
     * format.
     *
     * @param fmt The {@link SimpleDateFormat} format.
     * @return The formatted time.
     */
    public String toDisplayString(String fmt)
    {
        return new SimpleDateFormat(fmt).format(toDate());
    }

    /**
     * Convert this time instant to an ISO8601 representation.
     *
     * @return The ISO8601 string.
     */
    public String toISO8601String()
    {
        return DateTimeUtilities.generateISO8601DateString(toDate());
    }

    @Override
    public String toString()
    {
        SimpleDateFormat fmt = new SimpleDateFormat(DateTimeFormats.DATE_TIME_MILLIS_FORMAT);
        return new StringBuilder().append(TimeInstant.class.getSimpleName()).append('[').append(fmt.format(toDate())).append(']')
                .toString();
    }

    /**
     * Time instant implementation using a long.
     */
    protected static class TimeInstantLongMilliseconds extends TimeInstant
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The size of the object in bytes. */
        private static final int SIZE_BYTES = MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.LONG_SIZE_BYTES,
                Constants.MEMORY_BLOCK_SIZE_BYTES);

        /** The milliseconds since Java epoch. */
        private final long myTime;

        /**
         * Constructor.
         *
         * @param time The milliseconds since Java epoch.
         */
        public TimeInstantLongMilliseconds(long time)
        {
            myTime = time;
        }

        @Override
        public long getEpochMillis()
        {
            return myTime;
        }

        @Override
        public long getSizeBytes()
        {
            return SIZE_BYTES;
        }
    }
}
