package io.opensphere.core.model;

/**
 * Enumeration representing geographic position format.
 */
public enum GeographicPositionFormat
{
    /** Degrees, minutes, and seconds. */
    DMSDEG("DMS Degrees"),

    /** Decimal degrees. */
    DECDEG("Decimal Degrees"),

    /** Degrees and decimal minutes. */
    DEG_DMIN("Deg. + Decimal Min."),

    /** Military Grid Reference System. */
    MGRS("MGRS");

    /** The title. */
    private final String myTitle;

    /**
     * Returns the enum for the given title.
     *
     * @param title the title
     * @return the corresponding enum, or null
     */
    public static GeographicPositionFormat fromTitle(String title)
    {
        for (GeographicPositionFormat format : GeographicPositionFormat.values())
        {
            if (format.myTitle.equals(title))
            {
                return format;
            }
        }
        return null;
    }

    /**
     * Instantiates a new lat lon format.
     *
     * @param title the title
     */
    GeographicPositionFormat(String title)
    {
        myTitle = title;
    }

    @Override
    public String toString()
    {
        return myTitle;
    }
}
