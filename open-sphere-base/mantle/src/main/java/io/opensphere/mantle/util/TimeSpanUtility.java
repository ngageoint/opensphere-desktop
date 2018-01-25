package io.opensphere.mantle.util;

import java.text.SimpleDateFormat;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanFormatter;

/**
 * The Class TimeSpanUtility.
 */
public final class TimeSpanUtility
{
    /** Workaround timeless end time. */
    public static final long TIMELESS_END = Long.MAX_VALUE;

    /** Workaround timeless start time. */
    public static final long TIMELESS_START = Long.MIN_VALUE;

    /** Workaround encoded unbounded end time. */
    private static final long UNBOUNDED_END = 4102444800000L;

    /** Workaround encoded unbounded start constant. */
    private static final long UNBOUNDED_START = 1;

    /** Workaround zero end. */
    private static final long ZERO_END = 0L;

    /** Workaround zero start. */
    private static final long ZERO_START = 0L;

    /** The Constant BLANK. */
    private static final String BLANK = "";

    /**
     * Format time span to a string using the provided dateFormat for actual
     * times.
     *
     * @param dateFormat the {@link SimpleDateFormat} to use.
     * @param ts the {@link TimeSpan} to format.
     * @return the string result
     */
    public static String formatTimeSpan(SimpleDateFormat dateFormat, TimeSpan ts)
    {
        return ts == null ? BLANK : TimeSpanFormatter.buildString(ts, dateFormat, " to ", new StringBuilder(42)).toString();
    }

    /**
     * Format time span to a single time only. Used in CSV and shape file
     * exports.
     *
     * TIMELESS, ZERO are both blank. Spans only take the start time.
     * Instantaneous are not changed. Unbounded ends have only their start time.
     * Unbounded starts are blank.
     *
     * @param dateFormat the date format
     * @param ts the TimeSpan to format.
     * @return the string
     */
    public static String formatTimeSpanSingleTimeOnly(SimpleDateFormat dateFormat, TimeSpan ts)
    {
        String result = BLANK;
        if (ts != null)
        {
            if (ts.isTimeless())
            {
                result = BLANK;
            }
            else if (ts.isZero())
            {
                result = BLANK;
            }
            else
            {
                if (ts.isBounded())
                {
                    result = dateFormat.format(ts.getStartDate());
                }
                else
                {
                    StringBuilder sb = new StringBuilder();
                    if (ts.isUnboundedStart())
                    {
                        sb.append(BLANK);
                    }
                    else
                    {
                        sb.append(dateFormat.format(ts.getStartDate()));
                    }
                    result = sb.toString();
                }
            }
        }
        return result;
    }

    /**
     * Constructs a TimeSpan from a start and end time, taking into account the
     * special cases of TIMELESS, UNBOUNDED_START, and UNBOUNDED_END, and
     * returns the properly constructed TimeSpan for those and the standard
     * case.
     *
     * @param startTime the start time
     * @param endTime the end time
     * @return the time span
     */
    public static TimeSpan fromStartEnd(long startTime, long endTime)
    {
        return startTime == TIMELESS_START && endTime == TIMELESS_END ? TimeSpan.TIMELESS
                : startTime == UNBOUNDED_START ? TimeSpan.newUnboundedStartTimeSpan(endTime)
                        : endTime == UNBOUNDED_END ? TimeSpan
                                .newUnboundedEndTimeSpan(startTime)
                        : startTime == ZERO_START && endTime == ZERO_END ? TimeSpan.ZERO : TimeSpan.get(startTime, endTime);
    }

    /**
     * Gets the workaround end.
     *
     * @param ts the time span end encoded with special cases.
     * @return the workaround end
     */
    public static long getWorkaroundEnd(TimeSpan ts)
    {
        return ts == null ? TIMELESS_END
                : ts.isTimeless() ? TIMELESS_END : ts.isUnboundedEnd() ? UNBOUNDED_END : ts.isZero() ? ZERO_END : ts.getEnd();
    }

    /**
     * Gets the workaround start.
     *
     * @param ts the time span start encoded with special cases.
     * @return the workaround start
     */
    public static long getWorkaroundStart(TimeSpan ts)
    {
        return ts == null ? TIMELESS_START : ts.isTimeless() ? TIMELESS_START
                : ts.isUnboundedStart() ? UNBOUNDED_START : ts.isZero() ? ZERO_START : ts.getStart();
    }

    /**
     * Instantiates a new time span utility.
     */
    private TimeSpanUtility()
    {
        // Don't allow construction
    }
}
