package io.opensphere.analysis.export.model;

/**
 * The different formats the latitude and longitude columns can be exported to.
 */
public enum LatLonFormat
{
    /** The latitude/longitude is in decimal degrees format. */
    DECIMAL("Decimal"),

    /** The latitude/longitude is in a standard degree/minute/second format. */
    DMS("DMS"),

    /** The latitude/longitude is in a custom degree/minute/second format. */
    DMS_CUSTOM("DMS (period separated)");

    /** The display text. */
    private final String myDisplayText;

    /**
     * Instantiates a new coordinate format.
     *
     * @param displayText the display text
     */
    LatLonFormat(String displayText)
    {
        myDisplayText = displayText;
    }

    @Override
    public String toString()
    {
        return myDisplayText;
    }
}
