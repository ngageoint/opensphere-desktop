package io.opensphere.core.model.time;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Months;
import io.opensphere.core.util.CalendarUtilities;
import io.opensphere.core.util.DateTimeFormats;
import io.opensphere.core.util.DateTimeUtilities;

/**
 * TimeSpan formatting methods.
 */
public final class TimeSpanFormatter
{
    /** Month format. */
    private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("MMM yyyy");

    /** Standard date format. */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DateTimeFormats.DATE_FORMAT);

    /** Standard date/time format. */
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat(DateTimeFormats.DATE_TIME_FORMAT);

    /** Standard date/time with milliseconds format. */
    private static final SimpleDateFormat DATE_TIME_MILLIS_FORMAT = new SimpleDateFormat(DateTimeFormats.DATE_TIME_MILLIS_FORMAT);

    /**
     * Returns a String representing the TimeSpan that is suitable for display.
     *
     * @param timeSpan the time span
     * @return The display string
     */
    public static String toDisplayString(TimeSpan timeSpan)
    {
        return buildString(timeSpan, DATE_TIME_FORMAT, " - ", new StringBuilder(41)).toString();
    }

    /**
     * Convert the time span to an ISO8601 interval representation.
     *
     * @param timeSpan the time span
     * @return The ISO8601 string.
     */
    public static String toISO8601String(TimeSpan timeSpan)
    {
        return new StringBuilder(49).append(DateTimeUtilities.generateISO8601DateString(timeSpan.getStartDate())).append('/')
                .append(DateTimeUtilities.generateISO8601DateString(timeSpan.getEndDate())).toString();
    }

    /**
     * Convert the time span to a Object.toString() format.
     *
     * @param timeSpan the time span
     * @return The string.
     */
    public static String toString(TimeSpan timeSpan)
    {
        StringBuilder builder = new StringBuilder(57);
        builder.append(TimeSpan.class.getSimpleName()).append('[');
        buildString(timeSpan, DATE_TIME_MILLIS_FORMAT, ",", builder);
        builder.append(']');
        return builder.toString();
    }

    /**
     * Convert the time span to an smartly formatted representation.
     *
     * @param timeSpan the time span
     * @return The formatted string.
     */
    public static String toSmartString(TimeSpan timeSpan)
    {
        String displayString;
        if (timeSpan.isTimeless() || timeSpan.isZero() || timeSpan.isInstantaneous())
        {
            displayString = toDisplayString(timeSpan);
        }
        else
        {
            Calendar startCal = Calendar.getInstance();
            startCal.setTimeInMillis(timeSpan.getStart());

            Calendar endCal = Calendar.getInstance();
            endCal.setTimeInMillis(timeSpan.getEnd());

            if (CalendarUtilities.isClearedFrom(startCal, CalendarUtilities.DAY_INDEX)
                    && CalendarUtilities.isClearedFrom(endCal, CalendarUtilities.DAY_INDEX))
            {
                if (timeSpan.getDuration().equals(Months.ONE))
                {
                    displayString = formatStart(timeSpan, MONTH_FORMAT);
                }
                else
                {
                    displayString = buildString(minusEnd(timeSpan, Months.ONE), MONTH_FORMAT, " - ", new StringBuilder(19))
                            .toString();
                }
            }
            else if (CalendarUtilities.isClearedFrom(startCal, CalendarUtilities.HOUR_INDEX)
                    && CalendarUtilities.isClearedFrom(endCal, CalendarUtilities.HOUR_INDEX))
            {
                if (timeSpan.getDuration().equals(Days.ONE))
                {
                    displayString = formatStart(timeSpan, DATE_FORMAT);
                }
                else
                {
                    displayString = buildString(minusEnd(timeSpan, Days.ONE), DATE_FORMAT, " - ", new StringBuilder(23))
                            .toString();
                }
            }
            else
            {
                displayString = toDisplayString(timeSpan);
            }
        }
        return displayString;
    }

    /**
     * Adds the time span into the string builder with the given parameters.
     *
     * @param timeSpan the time span
     * @param format the format
     * @param joiner the joining string
     * @param builder the string builder to use
     * @return the builder
     */
    public static StringBuilder buildString(TimeSpan timeSpan, SimpleDateFormat format, String joiner, StringBuilder builder)
    {
        if (timeSpan.isTimeless())
        {
            builder.append("TIMELESS");
        }
        else if (timeSpan.isZero())
        {
            builder.append("ZERO");
        }
        else if (timeSpan.isInstantaneous())
        {
            synchronized (format)
            {
                builder.append(format.format(timeSpan.getStartDate()));
            }
        }
        else
        {
            builder.append(formatStart(timeSpan, format)).append(joiner).append(formatEnd(timeSpan, format));
        }
        return builder;
    }

    /**
     * Returns a copy of the time span with the specified duration subtracted
     * from the end.
     *
     * @param timeSpan the time span.
     * @param dur The duration to subtract.
     * @return The new time span.
     */
    private static TimeSpan minusEnd(TimeSpan timeSpan, Duration dur)
    {
        return TimeSpan.get(timeSpan.getStartInstant(), timeSpan.getEndInstant().minus(dur));
    }

    /**
     * Formats the start date of the time span.
     *
     * @param timeSpan the time span
     * @param format the format
     * @return the formatted string
     */
    private static String formatStart(TimeSpan timeSpan, SimpleDateFormat format)
    {
        synchronized (format)
        {
            return timeSpan.isUnboundedStart() ? "UNBOUNDED" : format.format(timeSpan.getStartDate());
        }
    }

    /**
     * Formats the end date of the time span.
     *
     * @param timeSpan the time span
     * @param format the format
     * @return the formatted string
     */
    private static String formatEnd(TimeSpan timeSpan, SimpleDateFormat format)
    {
        synchronized (format)
        {
            return timeSpan.isUnboundedEnd() ? "UNBOUNDED" : format.format(timeSpan.getEndDate());
        }
    }

    /** Private constructor. */
    private TimeSpanFormatter()
    {
    }
}
