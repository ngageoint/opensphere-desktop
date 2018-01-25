package io.opensphere.core.util.time;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.DurationUnitsProvider;
import io.opensphere.core.units.duration.Hours;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.units.duration.Minutes;
import io.opensphere.core.units.duration.Months;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.units.duration.Weeks;
import io.opensphere.core.units.duration.Years;
import io.opensphere.core.util.CalendarUtilities;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.collections.New;

/**
 * A class which is named TimelineUtilities (or something similar).
 */
@SuppressWarnings("PMD.GodClass")
public final class TimelineUtilities
{
    /**
     * Adds a duration to a Calendar.
     *
     * @param calendar the calendar
     * @param duration the duration to add
     */
    public static void addDuration(Calendar calendar, Duration duration)
    {
        new DurationUnitsProvider().add(calendar, duration);
    }

    /**
     * Get a calendar set to the latest 00:00 time at or before now.
     *
     * @return The calendar.
     */
    public static Calendar get0000()
    {
        Calendar c = Calendar.getInstance();
        CalendarUtilities.clearFields(c, CalendarUtilities.HOUR_INDEX, CalendarUtilities.MILLISECOND_INDEX);
        return c;
    }

    /**
     * Get a calendar set to the latest 00:00 time at or before the given
     * {@link Date}.
     *
     * @param aDate The reference date.
     * @return The calendar.
     */
    public static Calendar get0000(Date aDate)
    {
        Calendar c = CalendarUtilities.toCalendar(aDate);
        CalendarUtilities.clearFields(c, CalendarUtilities.HOUR_INDEX, CalendarUtilities.MILLISECOND_INDEX);
        return c;
    }

    /**
     * Get the intervals which overlap the span. The intervals will always start
     * on the given start date, so weeks are not required to start on Sunday and
     * months are not required to start on the 1st.
     *
     * @param startDate The time which is the beginning of the first interval.
     * @param span The span for which the intervals are desired.
     * @param spanType The calendar constant which defines the type (for example
     *            Calendar.DAY_OF_YEAR, Calendar.WEEK_OF_YEAR, or
     *            Calendar.MONTH).
     * @return The intervals which overlap the span.
     */
    public static List<TimeSpan> getIntervalsForSpan(Date startDate, TimeSpan span, int spanType)
    {
        List<TimeSpan> intervals = New.list();
        Date currentStart = startDate;
        while (currentStart.getTime() < span.getEndDate().getTime())
        {
            GregorianCalendar endCal = new GregorianCalendar();
            endCal.setTime(currentStart);
            endCal.add(spanType, 1);
            intervals.add(TimeSpan.get(currentStart, endCal.getTime()));

            currentStart = endCal.getTime();
        }
        return intervals;
    }

    /**
     * Get a calendar set to the next 00:00 time after the given {@link Date}.
     *
     * @param aDate The reference date.
     * @return The calendar.
     */
    public static Calendar getNext0000(Date aDate)
    {
        Calendar c = CalendarUtilities.toCalendar(aDate);
        c.set(Calendar.HOUR_OF_DAY, 24);
        CalendarUtilities.clearFields(c, CalendarUtilities.MINUTE_INDEX, CalendarUtilities.MILLISECOND_INDEX);
        return c;
    }

    /**
     * Gets a time span that represents the part of the calendar week that
     * includes the given current time up to the current time.
     *
     * @return the this calendar week
     */
    public static TimeSpan getPartialWeek()
    {
        return getPrecedingWeeks(1, true);
    }

    /**
     * Gets a TimeSpan that represents the the last x calendar months,
     * optionally including the current portion of the reference month.
     *
     * @param reference the reference time
     * @param months the number of months to include.
     * @param includeThisMonth true to include this month as one of the months,
     *            if false starts with end of previous month.
     * @return the {@link TimeSpan}
     */
    public static TimeSpan getPrecedingMonths(Date reference, int months, boolean includeThisMonth)
    {
        Calendar gc = get0000(reference);
        gc.set(Calendar.DAY_OF_MONTH, 1);
        gc.add(Calendar.MONTH, includeThisMonth ? 1 - months : -months);
        long start = gc.getTimeInMillis();

        gc.add(Calendar.MONTH, months);
        Date tomorrowQuadZ = getNext0000(reference).getTime();
        if (gc.getTime().after(tomorrowQuadZ))
        {
            gc.setTime(tomorrowQuadZ);
        }
        long end = gc.getTimeInMillis();

        return TimeSpan.get(start, end);
    }

    /**
     * Gets a TimeSpan that represents the the last x calendar months,
     * optionally including the current portion of the current month.
     *
     * @param months the number of months to include.
     * @param includeThisMonth true to include this month as one of the months,
     *            if false starts with end of previous month.
     * @return the {@link TimeSpan}
     */
    public static TimeSpan getPrecedingMonths(int months, boolean includeThisMonth)
    {
        return getPrecedingMonths(new Date(), months, includeThisMonth);
    }

    /**
     * Gets a TimeSpan that represents the the last x calendar weeks from the
     * reference date, optionally including the current portion of the reference
     * week.
     *
     * @param reference The reference week.
     * @param numWeeks the number of weeks to include.
     * @param includeThisWeek true to include this week as one of the weeks, if
     *            false starts with end of previous week.
     * @return the {@link TimeSpan}
     */
    public static TimeSpan getPrecedingWeeks(Date reference, int numWeeks, boolean includeThisWeek)
    {
        Calendar gc = get0000(reference);

        gc.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        gc.add(Calendar.WEEK_OF_YEAR, includeThisWeek ? 1 - numWeeks : -numWeeks);
        long start = gc.getTimeInMillis();

        gc.add(Calendar.WEEK_OF_YEAR, numWeeks);
        Date tomorrowQuadZ = getNext0000(reference).getTime();
        if (gc.getTime().after(tomorrowQuadZ))
        {
            gc.setTime(tomorrowQuadZ);
        }
        long end = gc.getTimeInMillis();

        return TimeSpan.get(start, end);
    }

    /**
     * Gets a TimeSpan that represents the the last x calendar weeks from the
     * current date, optionally including the current portion of the current
     * week.
     *
     * @param numWeeks the number of weeks to include.
     * @param includeThisWeek true to include this week as one of the weeks, if
     *            false starts with end of previous week.
     * @return the {@link TimeSpan}
     */
    public static TimeSpan getPrecedingWeeks(int numWeeks, boolean includeThisWeek)
    {
        return getPrecedingWeeks(new Date(), numWeeks, includeThisWeek);
    }

    /**
     * Gets the table of legal dates.
     *
     * @param span the time span
     * @param duration the duration
     * @return the table of legal dates
     */
    public static Map<Duration, List<DateDurationKey>> getTableOfLegalDates(TimeSpan span, Duration duration)
    {
        Map<Duration, List<DateDurationKey>> toReturn = new HashMap<>();

        if (duration instanceof Months)
        {
            toReturn.putAll(createMonthDateEntries(span, duration));
        }
        else if (duration instanceof Weeks)
        {
            createWeekDateEntries(span, toReturn, duration);
        }
        else if (duration instanceof Days)
        {
            createDayDateEntries(span, toReturn);
        }

        removeDayDuplicates(toReturn);
        return toReturn;
    }

    /**
     * Get the day which contains the given reference time.
     *
     * @param time The reference time.
     * @return The day which contains the given reference time.
     */
    public static TimeSpan getThisDay(Date time)
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(time);

        // Align to the date to day boundaries.
        GregorianCalendar spanCal = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        long start = spanCal.getTimeInMillis();
        spanCal.add(Calendar.DAY_OF_MONTH, 1);
        long end = spanCal.getTimeInMillis();
        return TimeSpan.get(start, end);
    }

    /**
     * Get the month which contains the given reference time.
     *
     * @param time The reference time.
     * @return The month which contains the given reference time.
     */
    public static TimeSpan getThisMonth(Date time)
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(time);

        // Align to the date to day boundaries.
        GregorianCalendar spanCal = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1);

        long start = spanCal.getTimeInMillis();
        spanCal.add(Calendar.MONTH, 1);
        long end = spanCal.getTimeInMillis();
        return TimeSpan.get(start, end);
    }

    /**
     * Get the week which contains the given reference time.
     *
     * @param time The reference time.
     * @return The week which contains the given reference time.
     */
    public static TimeSpan getThisWeek(Date time)
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(time);

        // Align to the date to day boundaries.
        GregorianCalendar spanCal = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        // Get the milliseconds to cause the calendar to resolve before
        // adjusting to the first day of the week.
        spanCal.getTimeInMillis();
        spanCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        long start = spanCal.getTimeInMillis();
        spanCal.add(Calendar.WEEK_OF_YEAR, 1);
        long end = spanCal.getTimeInMillis();
        return TimeSpan.get(start, end);
    }

    /**
     * Gets a time span that covers all of today from Quad Z today to quad Z
     * tomorrow.
     *
     * @return the {@link TimeSpan} for today.
     */
    public static TimeSpan getToday()
    {
        return TimeSpan.get(get0000().getTimeInMillis(), Days.ONE);
    }

    /**
     * Gets the TimeSpan for yesterday.
     *
     * @return the yesterday
     */
    public static TimeSpan getYesterday()
    {
        return TimeSpan.get(Days.ONE, get0000().getTimeInMillis());
    }

    /**
     * Returns true if the date is at zero hour, minute, second, and millisecond
     * in its day.
     *
     * @param aDate the a date
     * @return true if at quad z.
     */
    public static boolean isAt0000(Date aDate)
    {
        return CalendarUtilities.isClearedFrom(CalendarUtilities.toCalendar(aDate), CalendarUtilities.HOUR_INDEX);
    }

    /**
     * Determines if the given time span represents 1 day, week, or month. See
     * {@link #isDay(TimeSpan)}, {@link #isWeek(TimeSpan)}, and
     * {@link #isMonth(TimeSpan)}.
     *
     * @param span the time span
     * @return whether the span is 1 day, week, or month
     */
    public static boolean isDayWeekMonth(TimeSpan span)
    {
        return isDay(span) || isWeek(span) || isMonth(span);
    }

    /**
     * Determines if the given time span represents 1 day from midnight to
     * midnight.
     *
     * @param span the time span
     * @return whether the span is 1 day
     */
    public static boolean isDay(TimeSpan span)
    {
        return isAt0000(span.getStartDate()) && span.getDuration().equalsIgnoreUnits(Days.ONE);
    }

    /**
     * Determines if the given time span represents 1 week from midnight Sunday
     * to midnight Sunday.
     *
     * @param span the time span
     * @return whether the span is 1 week
     */
    public static boolean isWeek(TimeSpan span)
    {
        Calendar startCal = CalendarUtilities.toCalendar(span.getStartDate());
        return startCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                && CalendarUtilities.isClearedFrom(startCal, CalendarUtilities.HOUR_INDEX)
                && span.getDuration().equalsIgnoreUnits(Weeks.ONE);
    }

    /**
     * Determines if the given time span represents 1 month from midnight on the
     * 1st to midnight on the last day.
     *
     * @param span the time span
     * @return whether the span is 1 month
     */
    public static boolean isMonth(TimeSpan span)
    {
        Calendar startCal = CalendarUtilities.toCalendar(span.getStartDate());
        boolean isMonth = CalendarUtilities.isClearedFrom(startCal, CalendarUtilities.DAY_INDEX);
        if (isMonth)
        {
            startCal.add(Calendar.MONTH, 1);
            Calendar endCal = CalendarUtilities.toCalendar(span.getEndDate());
            isMonth = startCal.equals(endCal);
        }
        return isMonth;
    }

    /**
     * Determines if the time is rounded to the given duration.
     *
     * @param time the time
     * @param duration the duration
     * @return whether it's rounded
     */
    public static boolean isRounded(Date time, Duration duration)
    {
        return roundDown(time, duration).getTimeInMillis() == time.getTime();
    }

    /**
     * Gets the first calendar before the given date that's on the given
     * interval duration boundary.
     *
     * @param date the date
     * @param interval the interval duration
     * @return the calendar
     */
    public static Calendar roundDown(Date date, Duration interval)
    {
        return round(CalendarUtilities.toCalendar(date), interval, false);
    }

    /**
     * Gets the first calendar after the given date that's on the given interval
     * duration boundary.
     *
     * @param date the date
     * @param interval the interval duration
     * @return the calendar
     */
    public static Calendar roundUp(Date date, Duration interval)
    {
        return round(CalendarUtilities.toCalendar(date), interval, true);
    }

    /**
     * Scales the time span by the given factor equally from the midpoint.
     *
     * @param span the time span
     * @param factor the scale factor
     * @return the new time span
     */
    public static TimeSpan scale(TimeSpan span, double factor)
    {
        // Use milliseconds duration to avoid potential month arithmetic.
        Duration delta = new Milliseconds(span.getDurationMs()).multiply(factor / 2.);
        TimeInstant midpoint = span.getMidpointInstant();
        return TimeSpan.get(midpoint.minus(delta), midpoint.plus(delta));
    }

    /**
     * Creates the day date entries.
     *
     * @param span the time span
     * @param toChange the to change
     * @return the list
     */
    static List<DateDurationKey> createDayDateEntries(TimeSpan span, Map<Duration, List<DateDurationKey>> toChange)
    {
        Calendar startTimeCal = getStartCalendar(span);
        Calendar endTimeCal = getEndCalendar(span);

        int totalDaysInDateDurationKey = countDays(startTimeCal, endTimeCal);

        GregorianCalendar nextStart = new GregorianCalendar();
        nextStart.setTime(startTimeCal.getTime());
        List<DateDurationKey> dayDates = new ArrayList<>();
        while (totalDaysInDateDurationKey > 0)
        {
            GregorianCalendar currentStart = new GregorianCalendar();
            currentStart.setTime(nextStart.getTime());
            nextStart.add(Calendar.DATE, 1);

            dayDates.add(new DateDurationKey(currentStart.getTime(), nextStart.getTime(), Days.ONE));

            totalDaysInDateDurationKey--;
        }
        List<DateDurationKey> days = toChange.get(Days.ONE);
        if (days == null)
        {
            toChange.put(Days.ONE, dayDates);
        }
        else
        {
            days.addAll(dayDates);
        }
        return dayDates;
    }

    /**
     * Creates the month date entries.
     *
     * @param span the time span
     * @param timeBarDuration the time bar duration
     * @return the map
     */
    static Map<Duration, List<DateDurationKey>> createMonthDateEntries(TimeSpan span, Duration timeBarDuration)
    {
        Map<Duration, List<DateDurationKey>> toReturn = new HashMap<>();
        List<DateDurationKey> currentWeekDateEntries = null;
        List<DateDurationKey> lastWeekDateEntries = null;

        Calendar startTimeCal = getStartCalendar(span);
        Calendar endTimeCal = getEndCalendar(span);

        // Set the first month's start day to the first of the month, regardless
        // of incoming day
        if (startTimeCal.get(Calendar.DAY_OF_MONTH) > 1)
        {
            startTimeCal.set(Calendar.DAY_OF_MONTH, 1);
        }

        // If the end month's day is not #1, go to the next month.
        if (endTimeCal.get(Calendar.DAY_OF_MONTH) > 1)
        {
            endTimeCal.set(Calendar.DAY_OF_MONTH, 1);
            endTimeCal.add(Calendar.MONTH, 1);
        }

        int totalDaysInDateDurationKey = countDays(startTimeCal, endTimeCal);

        GregorianCalendar nextStart = new GregorianCalendar();
        nextStart.setTime(startTimeCal.getTime());

        List<DateDurationKey> monthDates = new ArrayList<>();
        while (totalDaysInDateDurationKey > 0)
        {
            Set<DateDurationKey> weekDates = new HashSet<>();
            GregorianCalendar currentStart = new GregorianCalendar();
            currentStart.setTime(nextStart.getTime());

            // Get the number of days in the month
            int daysInCurrentMonth = currentStart.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (daysInCurrentMonth < totalDaysInDateDurationKey)
            {
                nextStart.add(Calendar.DATE, daysInCurrentMonth);
            }
            else
            {
                nextStart.add(Calendar.DATE, totalDaysInDateDurationKey);
            }

            /* Since all month intervals will start with the 1st day of the
             * month and end with the first day of the next month, the last and
             * first weeks are the same. This code will check each months group
             * of weeks sequentially and remove weeks that repeat EX: if weeks
             * are created for 1 Jan 2007 to 1 Feb 2007, and then 1 Feb 2007 and
             * 1 Mar 2007 =>the 1 Jan - 1 Feb weeks are: (31Dec06-7Jan07)
             * (7Jan07-14Jan07) (14Jan07-21Jan07) (21Jan07-28Jan07)
             * (28Jan07-4Feb07) =>the 1 Feb - 1 Mar weeks are: (28Jan07-4Feb07)
             * (4Feb07-11Feb07) (11Feb07-18Feb07) (18Feb07-25Feb07)
             * (25Feb07-4Mar07) In this example, the first week of the second
             * group is removed since it is the same as the last week of the
             * first group */
            if (lastWeekDateEntries == null)
            {
                lastWeekDateEntries = new ArrayList<>(createWeekDateEntries(
                        TimeSpan.get(currentStart.getTime(), nextStart.getTime()), toReturn, timeBarDuration));
                weekDates.addAll(lastWeekDateEntries);
            }
            else
            {
                currentWeekDateEntries = createWeekDateEntries(TimeSpan.get(currentStart.getTime(), nextStart.getTime()),
                        toReturn, timeBarDuration);
                for (DateDurationKey lastWeekKey : lastWeekDateEntries)
                {
                    if (currentWeekDateEntries.contains(lastWeekKey))
                    {
                        currentWeekDateEntries.remove(lastWeekKey);
                    }
                }
                weekDates.addAll(currentWeekDateEntries);
                lastWeekDateEntries = currentWeekDateEntries;
            }

            GregorianCalendar actualEndDay = (GregorianCalendar)nextStart.clone();

            monthDates.add(new DateDurationKey(currentStart.getTime(), actualEndDay.getTime(), Months.ONE, weekDates));

            totalDaysInDateDurationKey -= daysInCurrentMonth;
        }
        toReturn.put(Months.ONE, monthDates);
        return toReturn;
    }

    /**
     * Creates the week date entries.
     *
     * @param span the time span
     * @param toChange the to change
     * @param timeBarDuration the time bar duration
     * @return the list
     */
    static List<DateDurationKey> createWeekDateEntries(TimeSpan span, Map<Duration, List<DateDurationKey>> toChange,
            Duration timeBarDuration)
    {
        Calendar startTimeCal = getStartCalendar(span);
        Calendar endTimeCal = getEndCalendar(span);

        startTimeCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        int endTimeDay = endTimeCal.get(Calendar.DAY_OF_WEEK);
        if (endTimeDay > Calendar.SUNDAY)
        {
            endTimeCal.add(Calendar.DAY_OF_YEAR, 8 - endTimeDay);
        }

        int totalDaysInDateDurationKey = countDays(startTimeCal, endTimeCal);

        GregorianCalendar nextStart = new GregorianCalendar();
        nextStart.setTime(startTimeCal.getTime());
        List<DateDurationKey> weekDates = new ArrayList<>();
        while (totalDaysInDateDurationKey > 0)
        {
            Set<DateDurationKey> dayDates = new HashSet<>();
            GregorianCalendar currentStart = new GregorianCalendar();
            currentStart.setTime(nextStart.getTime());
            int daysToNextWeek = 7;
            if (daysToNextWeek < totalDaysInDateDurationKey)
            {
                nextStart.add(Calendar.DATE, daysToNextWeek);
            }
            else
            {
                nextStart.add(Calendar.DATE, totalDaysInDateDurationKey);
            }

            dayDates.addAll(createDayDateEntries(TimeSpan.get(currentStart.getTime(), nextStart.getTime()), toChange));

            GregorianCalendar actualEndDay = (GregorianCalendar)nextStart.clone();

            weekDates.add(new DateDurationKey(currentStart.getTime(), actualEndDay.getTime(), Weeks.ONE, dayDates));

            totalDaysInDateDurationKey -= daysToNextWeek;
        }
        List<DateDurationKey> weeks = toChange.get(Weeks.ONE);
        if (weeks == null)
        {
            toChange.put(Weeks.ONE, weekDates);
        }
        else
        {
            weeks.addAll(weekDates);
        }
        return weekDates;
    }

    /**
     * Get a calendar object set to the last day overlapped by the time span.
     *
     * @param span The time span.
     * @return The calendar.
     */
    static Calendar getEndCalendar(TimeSpan span)
    {
        assert !span.isInstantaneous();
        Date endDate = span.getEndDate();
        return isAt0000(endDate) ? get0000(endDate) : getNext0000(endDate);
    }

    /**
     * Get a calendar object set to the start time of the time span.
     *
     * @param span The time span.
     * @return The calendar.
     */
    static Calendar getStartCalendar(TimeSpan span)
    {
        assert !span.isInstantaneous();
        return get0000(span.getStartDate());
    }

    /**
     * Gets the span.
     *
     * @param pStart the start
     * @param pEnd the end
     * @return the span
     */
    private static int countDays(Calendar pStart, Calendar pEnd)
    {
        int days = 0;
        GregorianCalendar temp = (GregorianCalendar)pStart.clone();
        while (temp.before(pEnd))
        {
            temp.add(Calendar.DAY_OF_YEAR, 1);
            days++;
        }
        return days;
    }

    /**
     * Remove duplicate days.
     *
     * @param dateTable the date table from which to remove duplicates.
     */
    private static void removeDayDuplicates(Map<Duration, List<DateDurationKey>> dateTable)
    {
        List<DateDurationKey> days = dateTable.get(Days.ONE);
        if (days != null)
        {
            Set<DateDurationKey> newSet = new TreeSet<>();
            newSet.addAll(days);
            days.clear();
            days.addAll(newSet);
        }
    }

    /**
     * Gets the first calendar before/after the given date that's on the given
     * interval duration boundary.
     *
     * @param cal the date
     * @param interval the interval duration
     * @param isUp whether to round up (true for up, false for down)
     * @return the calendar
     */
    private static Calendar round(Calendar cal, Duration interval, boolean isUp)
    {
        if (interval instanceof Weeks)
        {
            if (!interval.isOne())
            {
                throw new IllegalArgumentException("Rounding cannot be done by multiple weeks");
            }
            boolean sundayMidnight = cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                    && CalendarUtilities.isClearedFrom(cal, CalendarUtilities.HOUR_INDEX);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            if (isUp && !sundayMidnight)
            {
                cal.add(Calendar.DAY_OF_YEAR, 7);
            }
            CalendarUtilities.clearFields(cal, CalendarUtilities.HOUR_INDEX, CalendarUtilities.MILLISECOND_INDEX);
        }
        else
        {
            int fieldIndex = 0;
            if (interval instanceof Years)
            {
                fieldIndex = CalendarUtilities.YEAR_INDEX;
            }
            else if (interval instanceof Months)
            {
                fieldIndex = CalendarUtilities.MONTH_INDEX;
            }
            else if (interval instanceof Days)
            {
                fieldIndex = CalendarUtilities.DAY_INDEX;
            }
            else if (interval instanceof Hours)
            {
                fieldIndex = CalendarUtilities.HOUR_INDEX;
            }
            else if (interval instanceof Minutes)
            {
                fieldIndex = CalendarUtilities.MINUTE_INDEX;
            }
            else if (interval instanceof Seconds)
            {
                fieldIndex = CalendarUtilities.SECOND_INDEX;
            }
            else if (interval instanceof Milliseconds)
            {
                fieldIndex = CalendarUtilities.MILLISECOND_INDEX;
            }

            int field = CalendarUtilities.getCalendarFields(fieldIndex);
            int zeroValue = CalendarUtilities.getClearedValue(fieldIndex);
            int value = cal.get(field);
            if (zeroValue > 0)
            {
                value -= zeroValue;
            }
            int rounded = MathUtil.roundDownTo(value, interval.intValue());
            boolean roundUp = isUp && (value != rounded || !CalendarUtilities.isClearedFrom(cal, fieldIndex + 1));
            if (zeroValue > 0)
            {
                rounded += zeroValue;
            }
            cal.set(field, roundUp ? rounded + interval.intValue() : rounded);
            CalendarUtilities.clearFields(cal, fieldIndex + 1, CalendarUtilities.MILLISECOND_INDEX);
        }

        return cal;
    }

    /** Disallow instantiation. */
    private TimelineUtilities()
    {
    }
}
