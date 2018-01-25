package io.opensphere.analysis.binning.criteria;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.opensphere.core.model.IntegerRange;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Hours;
import io.opensphere.core.units.duration.Months;
import io.opensphere.core.units.duration.Weeks;
import io.opensphere.core.units.duration.Years;
import io.opensphere.core.util.DateTimeFormats;
import io.opensphere.core.util.time.TimelineUtilities;

/** Enumeration of time bin types. */
public enum TimeBinType
{
    /** Unique value. */
    UNIQUE("Unique", -1, false, null)
    {
        @Override
        public String getLabel(Date date)
        {
            return new SimpleDateFormat(DateTimeFormats.DATE_TIME_FORMAT).format(date);
        }
    },

    /** Hour. */
    HOUR("Hour", -1, true, Hours.ONE)
    {
        @Override
        public String getLabel(Date date)
        {
            return new SimpleDateFormat("yyyy-MM-dd HH:00").format(date);
        }
    },

    /** Hour of day. */
    HOUR_OF_DAY("Hour of day", Calendar.HOUR_OF_DAY, false, Hours.ONE)
    {
        @Override
        public IntegerRange getPeriodicRange()
        {
            return new IntegerRange(0, 23);
        }

        @Override
        public String getLabel(Date date)
        {
            return new SimpleDateFormat("HH").format(date);
        }
    },

    /** Day. */
    DAY("Day", -1, true, Days.ONE)
    {
        @Override
        public String getLabel(Date date)
        {
            return new SimpleDateFormat("yyyy-MM-dd").format(date);
        }
    },

    /** Day of week. */
    DAY_OF_WEEK("Day of week", Calendar.DAY_OF_WEEK, false, Days.ONE)
    {
        @Override
        public IntegerRange getPeriodicRange()
        {
            return new IntegerRange(1, 7);
        }

        @Override
        public String getLabel(Date date)
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return DateFormatSymbols.getInstance().getWeekdays()[cal.get(Calendar.DAY_OF_WEEK)];
        }
    },

    /** Week. */
    WEEK("Week", -1, true, Weeks.ONE)
    {
        @Override
        public String getLabel(Date date)
        {
            return "Week of " + new SimpleDateFormat("yyyy-MM-dd").format(date);
        }
    },

    /** Month. */
    MONTH("Month", -1, true, Months.ONE)
    {
        @Override
        public String getLabel(Date date)
        {
            return new SimpleDateFormat("yyyy-MM").format(date);
        }
    },

    /** Year. */
    YEAR("Year", -1, true, Years.ONE)
    {
        @Override
        public String getLabel(Date date)
        {
            return new SimpleDateFormat("yyyy").format(date);
        }
    };

    /** The display text. */
    private final String myText;

    /** The calendar field for periods, -1 otherwise. */
    private final int myPeriodField;

    /** Whether this bin type is a range type. */
    private final boolean myIsRange;

    /** The duration. */
    private final Duration myDuration;

    /**
     * Constructor.
     *
     * @param text The display text
     * @param periodField The calendar field for periods, -1 otherwise
     * @param isRange Whether this bin type is a range type
     * @param duration The duration
     */
    private TimeBinType(String text, int periodField, boolean isRange, Duration duration)
    {
        myText = text;
        myPeriodField = periodField;
        myIsRange = isRange;
        myDuration = duration;
    }

    @Override
    public String toString()
    {
        return myText;
    }

    /**
     * Gets the isPeriod.
     *
     * @return the isPeriod
     */
    public boolean isPeriod()
    {
        return myPeriodField != -1;
    }

    /**
     * Gets the isRange.
     *
     * @return the isRange
     */
    public boolean isRange()
    {
        return myIsRange;
    }

    /**
     * Gets the periodField.
     *
     * @return the periodField
     */
    public int getPeriodField()
    {
        return myPeriodField;
    }

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    public Duration getDuration()
    {
        return myDuration;
    }

    /**
     * Converts the date to a value to be used for comparison.
     *
     * @param date the date
     * @return the value
     */
    public int getPeriodicValue(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(myPeriodField);
    }

    /**
     * Gets the range of available periodic values.
     *
     * @return the values, or null
     */
    public IntegerRange getPeriodicRange()
    {
        return null;
    }

    /**
     * Gets the minimum value for the date.
     *
     * @param date the date
     * @return the minimum value
     */
    public long getMin(Date date)
    {
        return TimelineUtilities.roundDown(date, myDuration).getTimeInMillis();
    }

    /**
     * Gets the minimum value for the date.
     *
     * @param date the date
     * @return the minimum value
     */
    public long getMax(Date date)
    {
        return TimeInstant.get(getMin(date)).plus(myDuration).getEpochMillis();
    }

    /**
     * Gets the label for the date.
     *
     * @param date the date
     * @return the label
     */
    public abstract String getLabel(Date date);
}
