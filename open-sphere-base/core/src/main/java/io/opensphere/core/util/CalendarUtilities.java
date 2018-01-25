package io.opensphere.core.util;

import java.util.Calendar;
import java.util.Date;

/**
 * Calendar Utilities.
 */
public final class CalendarUtilities
{
    /** The year index into the CALENDAR_FIELDS array. */
    public static final int YEAR_INDEX = 0;

    /** The month index into the CALENDAR_FIELDS array. */
    public static final int MONTH_INDEX = 1;

    /** The day index into the CALENDAR_FIELDS array. */
    public static final int DAY_INDEX = 2;

    /** The hour index into the CALENDAR_FIELDS array. */
    public static final int HOUR_INDEX = 3;

    /** The minute index into the CALENDAR_FIELDS array. */
    public static final int MINUTE_INDEX = 4;

    /** The second index into the CALENDAR_FIELDS array. */
    public static final int SECOND_INDEX = 5;

    /** The millisecond index into the CALENDAR_FIELDS array. */
    public static final int MILLISECOND_INDEX = 6;

    /** The relevant calendar fields. */
    private static final int[] CALENDAR_FIELDS = { Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY,
        Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND };

    /** The cleared values for relevant calendar fields. */
    private static final int[] CALENDAR_CLEARED_VALUES = { 0, 0, 1, 0, 0, 0, 0 };

    /**
     * Determines if all the fields from startIndex to the milliseconds field
     * are cleared.
     *
     * @param cal the calendar
     * @param startIndex the starting field index
     * @return whether the fields are all cleared
     */
    public static boolean isClearedFrom(Calendar cal, int startIndex)
    {
        boolean allCleared = true;
        for (int i = startIndex; i <= MILLISECOND_INDEX; i++)
        {
            if (i < CALENDAR_FIELDS.length && cal.get(CALENDAR_FIELDS[i]) != CALENDAR_CLEARED_VALUES[i])
            {
                allCleared = false;
                break;
            }
        }
        return allCleared;
    }

    /**
     * Clears all the fields in the index range .
     *
     * @param cal the calendar
     * @param startIndex the starting field index
     * @param endIndex the ending field index
     */
    public static void clearFields(Calendar cal, int startIndex, int endIndex)
    {
        for (int i = startIndex; i <= endIndex && i < CALENDAR_FIELDS.length; i++)
        {
            cal.set(CALENDAR_FIELDS[i], CALENDAR_CLEARED_VALUES[i]);
        }
    }

    /**
     * Gets the calendar field at the given field index.
     *
     * @param fieldIndex the field index
     * @return the calendar field
     */
    public static int getCalendarFields(int fieldIndex)
    {
        return CALENDAR_FIELDS[fieldIndex];
    }

    /**
     * Gets the calendar cleared value at the given field index.
     *
     * @param fieldIndex the field index
     * @return the calendar cleared value
     */
    public static int getClearedValue(int fieldIndex)
    {
        return CALENDAR_CLEARED_VALUES[fieldIndex];
    }

    /**
     * Converts the given {@link Date} to a {@link Calendar}.
     *
     * @param date the date
     * @return the calendar
     */
    public static Calendar toCalendar(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    /**
     * Private constructor.
     */
    private CalendarUtilities()
    {
    }
}
