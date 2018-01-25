package io.opensphere.core.preferences;

import java.text.SimpleDateFormat;

/**
 * A utility method to get list tool preferences.
 *
 */
public final class ListToolPreferences
{
    /** The Constant LIST_TOOL_TIME_PRECISION_DIGITS. */
    public static final String LIST_TOOL_TIME_PRECISION_DIGITS = "ListToolTimePrecision";

    /** The Constant SNAP_TO_HIGHLIGHTED_PREFERENCE. */
    public static final String SNAP_TO_HIGHLIGHTED_PREFERENCE = "SNAP_TO_HIGHLIGHTED_PREFERENCE";

    /** The Constant SWITCH_FOCUS_ON_CURRENT_TYPE_CHANGE_PREFERENCE. */
    public static final String SWITCH_FOCUS_ON_CURRENT_TYPE_CHANGE_PREFERENCE = "SWITCH_FOCUS_ON_CURRENT_TYPE_CHANGE_PREFERENCE";

    /**
     * Gets the simple date format for precision.
     *
     * @param precision the precision
     * @return the simple date format for precision
     */
    public static SimpleDateFormat getSimpleDateFormatForPrecision(int precision)
    {
        return new SimpleDateFormat("yyyy-MM-dd " + getTimeFormat(precision));
    }

    /**
     * Gets the simple time format for precision.
     *
     * @param precision the precision
     * @return the simple date format for precision
     */
    public static SimpleDateFormat getSimpleTimeFormatForPrecision(int precision)
    {
        return new SimpleDateFormat(getTimeFormat(precision));
    }

    /**
     * Gets the simple time format for precision.
     *
     * @param precision the precision
     * @return the simple date format for precision
     */
    private static String getTimeFormat(int precision)
    {
        String timeFormat;
        switch (precision)
        {
            case 0:
                timeFormat = "HH:mm:ss";
                break;
            case 1:
                timeFormat = "HH:mm:ss.S";
                break;
            case 2:
                timeFormat = "HH:mm:ss.SS";
                break;
            case 3:
                timeFormat = "HH:mm:ss.SSS";
                break;
            default:
                timeFormat = "HH:mm:ss";
                break;
        }
        return timeFormat;
    }

    /**
     * Not constructible.
     */
    private ListToolPreferences()
    {
        // Don't allow construction.
    }
}
