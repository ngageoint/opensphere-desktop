package io.opensphere.controlpanels.timeline;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Hours;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.units.duration.Minutes;
import io.opensphere.core.units.duration.Months;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.units.duration.Weeks;
import io.opensphere.core.units.duration.Years;

/**
 * Tick mark information.
 */
class TickInfo
{
    /**
     * The Calendar fields that match the Durations for which we do formatting.
     */
    private static final int[] CALENDAR_FIELDS = new int[] { Calendar.MILLISECOND, Calendar.SECOND, Calendar.MINUTE,
        Calendar.HOUR_OF_DAY, Calendar.DAY_OF_WEEK, Calendar.WEEK_OF_YEAR, Calendar.DAY_OF_MONTH };

    /** The Durations for which we do formatting. */
    private static final List<Class<? extends Duration>> DURATIONS = Arrays.<Class<? extends Duration>>asList(Milliseconds.class,
            Seconds.class, Minutes.class, Hours.class, Days.class, Weeks.class, Days.class);

    /** Approximate milliseconds per month. */
    private static final long MILLIS_PER_MONTH = 2592000000L;

    /** Approximate milliseconds per year. */
    private static final long MILLIS_PER_YEAR = 31536000000L;

    /** The days format. */
    private static final SimpleDateFormat ourDaysFormat = new SimpleDateFormat("MMM dd");

    /** The hours format. */
    private static final SimpleDateFormat ourHoursFormat = new SimpleDateFormat("HH:mm");

    /** The milliseconds format. */
    private static final SimpleDateFormat ourMillisecondsFormat = new SimpleDateFormat(".SSS");

    /** The minutes format. */
    private static final SimpleDateFormat ourMinutesFormat = new SimpleDateFormat("HH:mm");

    /** The months format. */
    private static final SimpleDateFormat ourMonthsFormat = new SimpleDateFormat("MMM");

    /** The seconds format. */
    private static final SimpleDateFormat ourSecondsFormat = new SimpleDateFormat(":ss");

    /** The weeks format. */
    private static final SimpleDateFormat ourWeeksFormat = new SimpleDateFormat("MMM dd");

    /** The years format. */
    private static final SimpleDateFormat ourYearsFormat = new SimpleDateFormat("yyyy");

    /** The interval duration. */
    private final Duration myIntervalDuration;

    /**
     * Constructor.
     *
     * @param intervalDuration The interval duration
     */
    public TickInfo(Duration intervalDuration)
    {
        myIntervalDuration = intervalDuration;
    }

    /**
     * Formats the given time.
     *
     * @param millis the time in millis
     * @param unit The format to use
     * @return the formatted time
     */
    public String format(long millis, Duration unit)
    {
        SimpleDateFormat format;
        if (unit instanceof Milliseconds)
        {
            format = ourMillisecondsFormat;
        }
        else if (unit instanceof Seconds)
        {
            format = ourSecondsFormat;
        }
        else if (unit instanceof Minutes)
        {
            format = ourMinutesFormat;
        }
        else if (unit instanceof Hours)
        {
            format = ourHoursFormat;
        }
        else if (unit instanceof Days)
        {
            format = ourDaysFormat;
        }
        else if (unit instanceof Weeks)
        {
            format = ourWeeksFormat;
        }
        else if (unit instanceof Months)
        {
            format = ourMonthsFormat;
        }
        else
        {
            format = ourYearsFormat;
        }

        return format.format(new Date(millis));
    }

    /**
     * Gets the interval duration.
     *
     * @return the interval duration
     */
    public Duration getIntervalDuration()
    {
        return myIntervalDuration;
    }

    /**
     * Find the largest calendar interval that intersects a time. For example,
     * if the time is 1-Feb 00:00, 1 Month will be returned, but if the time is
     * 1-Feb 01:00, 1 Hour will be returned, or if the time is 1-Feb 01:01, 1
     * minute will be returned.
     *
     * @param millis the time in millis
     * @return the duration of the interval
     */
    public Duration getLargestIntervalDuration(long millis)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);

        for (int index = 0; index < CALENDAR_FIELDS.length; ++index)
        {
            int val = cal.get(CALENDAR_FIELDS[index]);
            int minimum = cal.getMinimum(CALENDAR_FIELDS[index]);
            if (val != minimum)
            {
                Duration dur = Duration.create(DURATIONS.get(index), val - minimum);

                // Check for the first of the month/year
                if (dur instanceof Days || dur instanceof Weeks)
                {
                    val = cal.get(Calendar.DAY_OF_MONTH);
                    minimum = cal.getMinimum(Calendar.DAY_OF_MONTH);
                    if (val == minimum)
                    {
                        val = cal.get(Calendar.DAY_OF_YEAR);
                        minimum = cal.getMinimum(Calendar.DAY_OF_YEAR);
                        if (val == minimum)
                        {
                            dur = new Years(cal.get(Calendar.YEAR));
                        }
                        else
                        {
                            dur = new Months(cal.get(Calendar.MONTH) - cal.getMinimum(Calendar.MONTH));
                        }
                    }
                }

                return dur;
            }
        }

        return new Years(cal.get(Calendar.YEAR));
    }

    /**
     * Gets the approximate milliseconds per interval.
     *
     * @return the approximate milliseconds per interval
     */
    public long getMillisPerInterval()
    {
        long millis;
        if (myIntervalDuration instanceof Years)
        {
            millis = myIntervalDuration.longValue() * MILLIS_PER_YEAR;
        }
        else if (myIntervalDuration instanceof Months)
        {
            millis = myIntervalDuration.longValue() * MILLIS_PER_MONTH;
        }
        else
        {
            millis = Milliseconds.get(myIntervalDuration).longValue();
        }
        return millis;
    }
}
