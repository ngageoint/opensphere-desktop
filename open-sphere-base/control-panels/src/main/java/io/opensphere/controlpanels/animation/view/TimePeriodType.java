package io.opensphere.controlpanels.animation.view;

import java.time.temporal.ChronoUnit;

import io.opensphere.core.util.swing.input.view.DateTextFieldFormat;

/**
 * Time period type.
 */
public enum TimePeriodType
{
    /** Custom. */
    CUSTOM("Custom", null, DateTextFieldFormat.DATE_TIME, true),

    /** Days. */
    DAYS("Day", ChronoUnit.DAYS, DateTextFieldFormat.DATE, false),

    /** Weeks. */
    WEEKS("Week", ChronoUnit.WEEKS, DateTextFieldFormat.DATE, true),

    /** Months. */
    MONTHS("Month", ChronoUnit.MONTHS, DateTextFieldFormat.MONTH, false);

    /** The name. */
    private String myName;

    /** The chrono unit. */
    private ChronoUnit myChronoUnit;

    /** The date text field format. */
    private final DateTextFieldFormat myFormat;

    /** Whether the end time UI should be visible. */
    private final boolean myEndVisible;

    /**
     * Gets the TimePeriodType for the given ChronoUnit.
     *
     * @param chronoUnit the chrono unit
     * @return the TimePeriodType, or null
     */
    public static TimePeriodType fromChronoUnit(ChronoUnit chronoUnit)
    {
        TimePeriodType timePeriodType;
        switch (chronoUnit)
        {
            case DAYS:
                timePeriodType = DAYS;
                break;
            case WEEKS:
                timePeriodType = WEEKS;
                break;
            case MONTHS:
                timePeriodType = MONTHS;
                break;
            default:
                timePeriodType = CUSTOM;
                break;
        }
        return timePeriodType;
    }

    /**
     * Constructor.
     *
     * @param name the name
     * @param chronoUnit the chrono unit
     * @param format the date text field format
     * @param endVisible whether the end time UI should be visible
     */
    TimePeriodType(String name, ChronoUnit chronoUnit, DateTextFieldFormat format, boolean endVisible)
    {
        myName = name;
        myChronoUnit = chronoUnit;
        myFormat = format;
        myEndVisible = endVisible;
    }

    /**
     * Gets the chrono unit.
     *
     * @return the chrono unit
     */
    public ChronoUnit getChronoUnit()
    {
        return myChronoUnit;
    }

    /**
     * Gets the format.
     *
     * @return the format
     */
    public DateTextFieldFormat getFormat()
    {
        return myFormat;
    }

    /**
     * Gets the endVisible.
     *
     * @return the endVisible
     */
    public boolean isEndVisible()
    {
        return myEndVisible;
    }

    @Override
    public String toString()
    {
        return myName;
    }
}
