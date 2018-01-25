package io.opensphere.csvcommon.common;

/**
 * The WktTypes.
 */
public enum WktType
{
    /** The POINT. */
    POINT("point"),

    /** The POLYGON. */
    POLYGON("polygon"),

    /** The LINESTRING. */
    LINESTRING("linestring"),

    /** The LINEARRING. */
    LINEARRING("linearring"),

    /** The MULTIPOINT. */
    MULTIPOINT("multipoint"),

    /** The MULTILINESTRING. */
    MULTILINESTRING("multilinestring"),

    /** The MULTIPOLYGON. */
    MULTIPOLYGON("multipolygon"),

    /** The GEOMETRYCOLLECTION. */
    GEOMETRYCOLLECTION("geometrycollection");

    /** The display text. */
    private final String myDisplayText;

    /**
     * Instantiates a new wkt type.
     *
     * @param displayText the display text
     */
    WktType(String displayText)
    {
        myDisplayText = displayText;
    }

    @Override
    public String toString()
    {
        return myDisplayText;
    }
}
