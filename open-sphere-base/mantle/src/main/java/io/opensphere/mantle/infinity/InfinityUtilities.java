package io.opensphere.mantle.infinity;

import java.util.HashMap;
import java.util.Map;

import io.opensphere.mantle.data.DataTypeInfo;

/** Infinity utilities. */
public final class InfinityUtilities
{
    /** URL tag. */
    public static final String URL = ".es-url";

    /** Index tag. */
    public static final String INDEX = ".es-index";

    /** Point geometry tag. */
    public static final String POINT = ".es-geopoint";

    /** Shape geometry tag. */
    public static final String SHAPE = ".es-geoshape";

    /** Start time tag. */
    public static final String START = ".es-starttime";

    /** End time tag. */
    public static final String END = ".es-endtime";

    /** Date time tag. */
    public static final String TIME = ".es-datetime";

    /** The "missing" data value. */
    public static final long MISSING_VALUE = 1000000000000000000L;

    /** The "missing" data value in scientific notation. */
    public static final String MISSING_VALUE_SCI_NOTATION = "1.0E18";

    /** The default bin width. */
    public static final double DEFAULT_BIN_WIDTH = 10.0;

    /** The default bin width fractional digits. */
    public static final int DEFAULT_BIN_WIDTH_FRAC_DIGITS = 1;

    /** The default bin offset. */
    public static final double DEFAULT_BIN_OFFSET = 0.0;

    /** The default min_doc_count. */
    public static final int DEFAULT_MIN_DOC_COUNT = 1;

    /** The default size. */
    public static final int DEFAULT_SIZE = 10000;

    /** The default user number format. */
    public static final String DEFAULT_USER_NUM_FORMAT = "%.1f";

    /** The default initial size of the infinity output stream. */
    public static final int DEFAULT_INITIAL_BYTE_STREAM_SIZE = 500;

    /** The interval for the date minute range. */
    public static final String BIN_MINUTE_INTERVAL = "minute";

    /** The interval for the date hour range. */
    public static final String BIN_HOUR_INTERVAL = "hour";

    /** The interval for the date hour range. */
    public static final String BIN_DAY_INTERVAL = "day";

    /** The interval for the date week range. */
    public static final String BIN_WEEK_INTERVAL = "week";

    /** The interval for the date month range. */
    public static final String BIN_MONTH_INTERVAL = "month";

    /** The interval for the date year range. */
    public static final String BIN_YEAR_INTERVAL = "year";

    /** The interval for the date year range. */
    public static final String BIN_UNIQUE_INTERVAL = "unique";

    /** The default interval for the date rage. */
    public static final String DEFAULT_DATE_BIN_INTERVAL = BIN_HOUR_INTERVAL;

    /** The default date format. **/
    public static final String DEFAULT_DATE_BIN_FORMAT = "yyyy-MM-dd HH";

    /** The default scripting language. */
    public static final String DEFAULT_SCRIPT_LANGUAGE = "painless";

    /** The mapping of intervals to date formats. */
    private static final Map<String, String> DATE_FORMAT_MAP = new HashMap<String, String>()
    {
        /** Default serial version ID  */
        private static final long serialVersionUID = 1L;

        {
            put(BIN_MINUTE_INTERVAL, "yyyy-MM-dd HH:mm");
            put(BIN_HOUR_INTERVAL, "yyyy-MM-dd HH");
            put(BIN_DAY_INTERVAL, "yyyy-MM-dd");
            put(BIN_WEEK_INTERVAL, "yyyy-MM-dd");
            put(BIN_MONTH_INTERVAL, "yyyy-MM");
            put(BIN_YEAR_INTERVAL, "yyyy");
        }
    };

    /**
     * Determines if the data type is infinity-enabled.
     *
     * @param dataType the data type
     * @return whether it's infinity-enabled
     */
    public static boolean isInfinityEnabled(DataTypeInfo dataType)
    {
        String completeKey = URL + "=";
        return dataType.getTags().stream().anyMatch(t -> t.startsWith(completeKey));
    }

    /**
     * Gets the URL for the data type.
     *
     * @param dataType the data type
     * @return the URL, or null
     */
    public static String getUrl(DataTypeInfo dataType)
    {
        String url = getTagValue(URL, dataType);
        String index = getTagValue(INDEX, dataType);
        if (index != null)
        {
            url += "?" + index;
        }
        return url;
    }

    /**
     * Gets the value for the tag key.
     *
     * @param tagKey the tag key
     * @param dataType the data type
     * @return the value, or null
     */
    public static String getTagValue(String tagKey, DataTypeInfo dataType)
    {
        String completeKey = tagKey + "=";
        return dataType.getTags().stream().filter(t -> t.startsWith(completeKey)).map(t -> t.replace(completeKey, "")).findAny()
                .orElse(null);
    }

    /**
     * Get the date format based on interval key.
     *
     * @param interval the date range interval
     * @return the date format
     */
    public static String getDateFormat(String interval)
    {
        return DATE_FORMAT_MAP.getOrDefault(interval, DEFAULT_DATE_BIN_FORMAT);
    }

    /** Disallow instantiation. */
    private InfinityUtilities()
    {
    }
}
