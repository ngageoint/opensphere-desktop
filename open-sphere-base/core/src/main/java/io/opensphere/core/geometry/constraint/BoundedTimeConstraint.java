package io.opensphere.core.geometry.constraint;

import javax.annotation.concurrent.Immutable;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.util.Constants;

/**
 * A {@link TimeConstraint} that has additional restrictions based on the
 * duration of the overlapping time span.
 * <p>
 * Examples:<br>
 * <ul>
 * <li>Given the active time span is 01/01/2000 00:00:00 - 01/01/2000 01:00:00.
 * (3600 seconds.)<br>
 * A geometry with a bounded time constraint of:
 * <ul>
 * <li>01/01/2000 00:00:00 - 01/01/2000 01:00:00 minDur(0) maxDur(3600)
 * <b>is</b> visible</li>
 * <li>01/01/2000 00:00:00 - 01/02/2000 00:00:00 minDur(3600) maxDur(86400) is
 * <b>not</b> visible</li>
 * </ul>
 * </li>
 * <li>Given the active time span is 01/01/2000 00:00:00 - 01/01/2000 01:00:01.
 * (3601 seconds.)<br>
 * A geometry with a bounded time constraint of:
 * <ul>
 * <li>01/01/2000 00:00:00 - 01/01/2000 01:00:00 minDur(0) maxDur(3600) is
 * <b>not</b> visible</li>
 * <li>01/01/2000 00:00:00 - 01/02/2000 00:00:00 minDur(3600) maxDur(86400)
 * <b>is</b> visible</li>
 * </ul>
 * </li>
 * <li>Given the active time span is 01/01/2000 00:00:00 - 01/02/2000 00:00:00.
 * (86400 seconds.)<br>
 * A geometry with a bounded time constraint of:
 * <ul>
 * <li>01/01/2000 00:00:00 - 01/01/2000 01:00:00 minDur(0) maxDur(3600) is
 * <b>not</b> visible</li>
 * <li>01/01/2000 00:00:00 - 01/02/2000 00:00:00 minDur(3600) maxDur(86400)
 * <b>is</b> visible</li>
 * </ul>
 * </li>
 * <li>Given the active time span is 01/01/2000 00:00:00 - 01/02/2000 00:00:01.
 * (86401 seconds.)<br>
 * A geometry with a bounded time constraint of:
 * <ul>
 * <li>01/01/2000 00:00:00 - 01/01/2000 01:00:00 minDur(0) maxDur(3600) is
 * <b>not</b> visible</li>
 * <li>01/01/2000 00:00:00 - 01/02/2000 00:00:00 minDur(3600) maxDur(86400) is
 * <b>not</b> visible</li>
 * </ul>
 * </li>
 * </ul>
 */
@Immutable
public class BoundedTimeConstraint extends TimeConstraint
{
    /**
     * The inclusive upper bound marking the longest that the overlapping time
     * span can be for it to be eligible to satisfy this constraint.
     */
    private final int myMaximumDurationSeconds;

    /**
     * The exclusive lower bound marking the longest that the overlapping time
     * span can be and still be too short to be eligible to satisfy this
     * constraint.
     */
    private final int myMinimumDurationSeconds;

    /**
     * Construct a bounded time constraint.
     *
     * @param key The key associated with the constraint.
     * @param timeSpan The time span.
     * @param minDuration The exclusive lower boundary on time span durations,
     *            or {@code null} to indicate no bound.
     * @param maxDuration The inclusive upper boundary on time span durations,
     *            or {@code null} to indicate no bound.
     * @return A time constraint.
     * @throws IllegalArgumentException If minDurationSeconds &gt;=
     *             maxDurationSeconds.
     */
    public static synchronized BoundedTimeConstraint getTimeConstraint(Object key, TimeSpan timeSpan, Duration minDuration,
            Duration maxDuration)
    {
        long minTime = timeSpan.isUnboundedStart() ? Long.MIN_VALUE : timeSpan.getStart();
        long maxTime = timeSpan.isUnboundedEnd() ? Long.MAX_VALUE : timeSpan.getEnd();
        int minDurationSeconds = minDuration == null ? 0 : new Seconds(minDuration).intValue();
        int maxDurationSeconds = maxDuration == null ? Integer.MAX_VALUE : new Seconds(maxDuration).intValue();
        return (BoundedTimeConstraint)getPool()
                .get(new BoundedTimeConstraint(key, minTime, maxTime, minDurationSeconds, maxDurationSeconds));
    }

    /**
     * Construct a bounded time constraint, with the time measured in seconds.
     *
     * @param timeSpan The time span.
     * @param minDuration The exclusive lower boundary on time span durations,
     *            or {@code null} to indicate no bound.
     * @param maxDuration The inclusive upper boundary on time span durations,
     *            or {@code null} to indicate no bound.
     * @return A time constraint.
     * @throws IllegalArgumentException If minDurationSeconds &gt;=
     *             maxDurationSeconds.
     */
    public static synchronized BoundedTimeConstraint getTimeConstraint(TimeSpan timeSpan, Duration minDuration,
            Duration maxDuration)
    {
        return getTimeConstraint(null, timeSpan, minDuration, maxDuration);
    }

    /**
     * Construct a time constraint.
     *
     * @param key The key associated with this constraint.
     * @param minTime The early time boundary in milliseconds since Java epoch.
     * @param maxTime The late time boundary in milliseconds since Java epoch.
     * @param minDurationSeconds The exclusive lower boundary on time span
     *            durations.
     * @param maxDurationSeconds The inclusive upper boundary on time span
     *            durations.
     * @throws IllegalArgumentException If minDurationSeconds &gt;=
     *             maxDurationSeconds.
     */
    protected BoundedTimeConstraint(Object key, long minTime, long maxTime, int minDurationSeconds, int maxDurationSeconds)
    {
        super(key, minTime, maxTime);
        if (minDurationSeconds >= maxDurationSeconds)
        {
            throw new IllegalArgumentException("minDurationSeconds >= maxDurationSeconds");
        }
        myMinimumDurationSeconds = minDurationSeconds;
        myMaximumDurationSeconds = maxDurationSeconds;
    }

    @Override
    public boolean check(TimeSpan span)
    {
        int seconds = (int)Math.ceil((double)span.getDurationMs() / Constants.MILLI_PER_UNIT);
        if (seconds <= myMinimumDurationSeconds || seconds > myMaximumDurationSeconds)
        {
            return false;
        }
        else
        {
            return super.check(span);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass())
        {
            return false;
        }
        BoundedTimeConstraint other = (BoundedTimeConstraint)obj;
        return myMaximumDurationSeconds == other.myMaximumDurationSeconds
                && myMinimumDurationSeconds == other.myMinimumDurationSeconds;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + myMaximumDurationSeconds;
        result = prime * result + myMinimumDurationSeconds;
        return result;
    }
}
