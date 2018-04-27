package io.opensphere.core.model.time;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

/**
 * The Enum DayOfWeek.
 */
@XmlType(name = "dayOfWeek")
@XmlEnum
public enum DayOfWeek
{
    /** The SUNDAY. */
    SUNDAY(Calendar.SUNDAY),

    /** The MONDAY. */
    MONDAY(Calendar.MONDAY),

    /** The TUESDAY. */
    TUESDAY(Calendar.TUESDAY),

    /** The WEDNESDAY. */
    WEDNESDAY(Calendar.WEDNESDAY),

    /** The THURSDAY. */
    THURSDAY(Calendar.THURSDAY),

    /** The FRIDAY. */
    FRIDAY(Calendar.FRIDAY),

    /** The SATURDAY. */
    SATURDAY(Calendar.SATURDAY);

    /** The calendar day of week. */
    private final int myCalendarDayOfWeek;

    /**
     * The set of valid names, in lower case.
     */
    private static final Set<String> DAY_NAMES = getDayNames();

    /**
     * Returns the day of week used by the {@link Calendar} class for this Day
     * of Week.
     *
     * @return the int
     */
    public int getCalendarDayOfWeek()
    {
        return myCalendarDayOfWeek;
    }

    /**
     * Creates a DayOfWeek from the types in the {@link Calendar} class.
     *
     * @param calendarDayOfWeek the calendar day of week
     * @return the DayOfWeek
     */
    public static DayOfWeek getDayOfWeek(int calendarDayOfWeek)
    {
        for (DayOfWeek day : values())
        {
            if (day.getCalendarDayOfWeek() == calendarDayOfWeek)
            {
                return day;
            }
        }
        throw new IllegalArgumentException("Value is not a Day of Week from the Calendar Class");
    }

    /**
     * Gets the camel case day of week.
     *
     * @param dayOfWeek the day of week
     * @return the camel case day of week
     */
    public static String getDisplayName(DayOfWeek dayOfWeek)
    {
        return DateFormatSymbols.getInstance().getWeekdays()[dayOfWeek.getCalendarDayOfWeek()];
    }

    /**
     * Tests to determine if the supplied value is the name of a day of the
     * week.
     *
     * @param pName the string to test.
     * @return true if the supplied value is the name of a day of the week
     *         (regardless of case), or false otherwise (including null).
     */
    public static boolean isDayOfWeek(String pName)
    {
        if (StringUtils.isBlank(pName))
        {
            return false;
        }
        return DAY_NAMES.contains(pName.toLowerCase());
    }

    /**
     * Gets the set of valid day names, in lower case.
     *
     * @return the set of valid day names in lower case.
     */
    private static Set<String> getDayNames()
    {
        Set<String> names = new HashSet<>();
        for (String dayName : DateFormatSymbols.getInstance().getWeekdays())
        {
            names.add(dayName.toLowerCase());
        }
        return names;
    }

    /**
     * Gets the enum instance corresponding to the supplied day name (regardless
     * of case).
     *
     * @param pDayName the name of the day for which to get the enum instance
     *            (will return null if it doesn't contain a valid name).
     * @return the enum instance of the supplied day name, or null if none
     *         corresponds.
     */
    public static DayOfWeek fromDayName(String pDayName)
    {
        if (StringUtils.isBlank(pDayName))
        {
            return null;
        }

        return DayOfWeek.valueOf(pDayName.toUpperCase());
    }

    /**
     * Instantiates a new day of week.
     *
     * @param dow the dow
     */
    DayOfWeek(int dow)
    {
        myCalendarDayOfWeek = dow;
    }
}
