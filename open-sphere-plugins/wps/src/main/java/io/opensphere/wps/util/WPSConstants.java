package io.opensphere.wps.util;

import io.opensphere.wps.request.WpsProcessConfiguration;

/**
 * The Class WPSConstants that defines common constants used in the WPS Plugin.
 */
public final class WPSConstants
{
    /** The Constant BBOX. */
    public static final String BBOX = "BBOX";

    /** The begin time position identifier. */
    public static final String BEGIN_TIME_POSITION = "beginPosition";

    /**
     * The name of the color property in a {@link WpsProcessConfiguration}.
     */
    public static final String COLOR_PROP = "Color";

    /** The collection root identifier. */
    public static final String DATA_COLLECTION_ROOT = "wfs:FeatureCollection";

    /** The point color identifier. */
    public static final String DATA_POINT_COLOR = "styleVariation";

    /** The down time identifier. */
    public static final String DATA_POINT_DOWN_TIME = "DOWN_TIME";

    /** The integer time down identifier. */
    public static final String DATA_POINT_INT_TIME_DOWN = "INT_TIME_DOWN";

    /** The time down identifier. */
    public static final String DATA_POINT_TIME_DOWN = "TIMEDOWN";

    /** Down time identifier. */
    public static final String DOWN_TIME_FIELD = "DOWN_DATE_TIME";

    /** The end position identifier. */
    public static final String END_TIME_POSITION = "endPosition";

    /** The begin time position identifier. */
    public static final String GML_BEGIN_TIME_POSITION = "gml:beginPosition";

    /** The end position identifier. */
    public static final String GML_END_TIME_POSITION = "gml:endPosition";

    /** GML feature member tag. */
    public static final String GML_FEATURE_MEMBER = "gml:featureMember";

    /**
     * The string to add to any saved analytics layer keys.
     */
    public static final String SAVED_ANALYTICS_KEY = "!!Saved Analytics";

    /**
     * The string to add to any available analytics layer keys.
     */
    public static final String AVAILABLE_ANALYTICS_KEY = "!!Available Analytics";

    /** The "KEY" identifier. */
    public static final String KEY = "KEY";

    /** Latitude identifier. */
    public static final String LAT = "LAT";

    /** Longitude identifier. */
    public static final String LON = "LON";

    /** Time identifier. */
    public static final String TIME_FIELD = "TIME";

    /** The time position identifier. */
    public static final String TIME_POSITION = "gml:timePosition";

    /** Up time identifier. */
    public static final String UP_TIME_FIELD = "UP_DATE_TIME";

    /** The raw data output type for requests. */
    public static final String RAW_DATA_OUTPUT = "OutputImage";

    /**
     * The name of the value in the visitor in which the instance name is
     * stored.
     */
    public static final String PROCESS_INSTANCE_NAME = "PROCESS_INSTANCE_NAME";

    /** Forbid public instantiation of utility class. */
    private WPSConstants()
    {
    }
}
