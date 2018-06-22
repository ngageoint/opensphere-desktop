package io.opensphere.mantle.infinity;

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

    /** The default bin width */
    public static final double DEFAULT_BIN_WIDTH = 10.0;

    /** The default bin width fractional digits*/
    public static final int DEFAULT_BIN_WIDTH_FRAC_DIGITS = 1;

    /** The default bin offset */
    public static final double DEFAULT_BIN_OFFSET = 0.0;

    /** The default user number format */
    public static final String DEFAULT_USER_NUM_FORMAT = "%.1f";

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

    /** Disallow instantiation. */
    private InfinityUtilities()
    {
    }
}
