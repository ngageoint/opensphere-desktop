package io.opensphere.arcgis2.envoy;

import java.util.TimeZone;

/** Constants used for handling arc features. */
public final class ArcConstants
{
    /** The Constant ATTRIBUTE_DATA_DATE_KEY. */
    public static final String ATTRIBUTE_DATA_DATE_KEY = "DATA_DATE";

    /** The Constant ATTRIBUTE_DATE_STRING_KEY. */
    public static final String ATTRIBUTE_DATE_STRING_KEY = "DATE_STRING";

    /** The Constant ATTRIBUTE_ID_KEY. */
    public static final String ATTRIBUTE_ID_KEY = "ID";

    /** The Constant ATTRIBUTE_OBJECTID_KEY. */
    public static final String ATTRIBUTE_OBJECTID_KEY = "OBJECTID";

    /** The Constant ATTRIBUTES_KEY. */
    public static final String ATTRIBUTES_KEY = "attributes";

    /** The Constant DATE_FORMAT_1. */
    public static final String DATE_FORMAT_1 = "dd-MMM-yy hh'.'mm'.'ss.ssssss aa";

    /** The Constant DATE_FORMAT_2. */
    public static final String DATE_FORMAT_2 = "yyyy-MM-dd 'z'HHmmss'.00'";

    /** The Constant DATE_FORMAT_3. */
    public static final String DATE_FORMAT_3 = "yyyy-MM-dd HH:mm:ss";

    /** The Constant FEATURES_KEY. */
    public static final String FEATURES_KEY = "features";

    /** The Constant GEOMETRY_KEY. */
    public static final String GEOMETRY_KEY = "geometry";

    /** The Constant GEOMETRY_LAT_KEY. */
    public static final String GEOMETRY_LAT_KEY = "y";

    /** The Constant GEOMETRY_LON_KEY. */
    public static final String GEOMETRY_LON_KEY = "x";

    /** The Constant GEOMETRY_TYPE_KEY. */
    public static final String GEOMETRY_TYPE_KEY = "geometryType";

    /** The Constant time zone for GMT. */
    public static final TimeZone TIME_ZONE_GMT00 = TimeZone.getTimeZone("GMT+00:00");

    /** Disallow instantiation. */
    private ArcConstants()
    {
    }

    /** Enum used to track the current parse State. */
    public enum ArcSaxState
    {
        /** Collecting information from the current attribute. */
        COLLECT_ATTRIBUTES,

        /** Collecting information from the current geometry. */
        COLLECT_GEOMETRY_TYPE,

        /** Collecting the value of the current point's "x" tag. */
        COLLECT_POINT_X_VALUE,

        /** Collecting the value of the current point's "y" tag. */
        COLLECT_POINT_Y_VALUE,

        /** Collecting the value of the current polygon point's "x" tag. */
        COLLECT_POLYGON_RING_X,

        /** Collecting the value of the current polygon point's "y" tag. */
        COLLECT_POLYGON_RING_Y,

        /** Collecting the value of the current track point's "x" tag. */
        COLLECT_TRACK_PATH_X,

        /** Collecting the value of the current track point's "y" tag. */
        COLLECT_TRACK_PATH_Y,

        /** Seeking for the next Attributes tag. */
        SEEK_ATTRIBUTES_TAG,

        /** Seeking for the next Features tag. */
        SEEK_FEATURES_TAG,

        /** Seeking for the next Geometry tag. */
        SEEK_GEOMETRY_TAG,

        /** Seeking for the next Geometry type tag. */
        SEEK_GEOMETRY_TYPE_TAG,

        /** Seeking for the tag representing a point's "x" value. */
        SEEK_POINT_X_TAG,

        /** Seeking for the tag representing a point's "y" value. */
        SEEK_POINT_Y_TAG,

        /** Seeking for the tag representing a polygon geometry. */
        SEEK_POLYGON_RING_TAG,

        /** Seeking for the tag representing a track geometry. */
        SEEK_TRACK_PATH_TAG,

        ;
    }
}
