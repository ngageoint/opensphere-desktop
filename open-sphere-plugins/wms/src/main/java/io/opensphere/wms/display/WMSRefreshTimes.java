package io.opensphere.wms.display;

import io.opensphere.core.util.Constants;

/**
 * The valid refresh times for wms tile layers.
 */
public enum WMSRefreshTimes
{
    /**
     * No refresh.
     */
    NONE(0, ""),

    /**
     * Every fifteen seconds.
     */
    FIFTEEN_SECONDS(15000, "15 seconds"),

    /**
     * Every thirty seconds.
     */
    THIRTY_SECONDS(30000, "30 seconds"),

    /**
     * Every one minute.
     */
    ONE_MINUTE(Constants.MILLIS_PER_MINUTE, "1 minute"),

    /**
     * Every five minutes.
     */
    FIVE_MINUTES(Constants.MILLIS_PER_MINUTE * 5, "5 minutes"),

    /**
     * Every ten minutes.
     */
    TEN_MINUTES(Constants.MILLIS_PER_MINUTE * 10, "10 minutes"),

    /**
     * Every fifteen minutes.
     */
    FIFTEEN_MINUTES(Constants.MILLIS_PER_MINUTE * 15, "15 minutes"),

    /**
     * Every thirty minutes.
     */
    THIRTY_MINUTES(Constants.MILLIS_PER_MINUTE * 30, "30 minutes"),

    /**
     * Every one hour.
     */
    ONE_HOUR(Constants.MILLIS_PER_HOUR, "1 hour"),

    /**
     * Every two hours.
     */
    TWO_HOURS(Constants.MILLIS_PER_HOUR * 2, "2 hours"),

    /**
     * Every three hours.
     */
    THREE_HOURS(Constants.MILLIS_PER_HOUR * 3, "3 hours"),

    /**
     * Every six hours.
     */
    SIX_HOURS(Constants.MILLIS_PER_HOUR * 6, "6 hours"),

    /**
     * Every 12 hours.
     */
    TWELVE_HOURS(Constants.MILLIS_PER_HOUR * 12, "12 hours"),

    /**
     * Every 1 day.
     */
    ONE_DAY(Constants.MILLIS_PER_DAY, "1 day");

    /**
     * The display string of the enum.
     */
    private String myDisplayName;

    /**
     * The number of milliseconds it represents.
     */
    private long myMilliseconds;

    /**
     * Constructs a new refresh time enum.
     *
     * @param milliseconds The number of milliseconds for the refresh time
     *            value.
     * @param displayName The string to display to the user.
     */
    WMSRefreshTimes(int milliseconds, String displayName)
    {
        myMilliseconds = milliseconds;
        myDisplayName = displayName;
    }

    /**
     * Gets the milliseconds for the refresh time.
     *
     * @return The refresh time in milliseconds.
     */
    public long getMilliseconds()
    {
        return myMilliseconds;
    }

    @Override
    public String toString()
    {
        return myDisplayName;
    }
}
