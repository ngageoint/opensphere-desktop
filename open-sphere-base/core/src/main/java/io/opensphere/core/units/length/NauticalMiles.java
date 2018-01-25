package io.opensphere.core.units.length;

import io.opensphere.core.util.Constants;
import io.opensphere.core.util.Utilities;

/**
 * A length with nautical miles as its native unit.
 */
public final class NauticalMiles extends Length
{
    /** Meters in a nautical mile (exact). */
    public static final int METERS_PER_NAUTICAL_MILE = 1852;

    /** Long label. */
    public static final String NM_LONG_LABEL = "nautical mile";

    /** Short label. */
    public static final String NM_SHORT_LABEL = "nm";

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Convert kilometers to nautical miles.
     *
     * @param km The kilometers.
     * @return The nautical miles.
     */
    public static double fromKilometers(double km)
    {
        return km / METERS_PER_NAUTICAL_MILE * Constants.UNIT_PER_KILO;
    }

    /**
     * Convert meters to nautical miles.
     *
     * @param meters The meters.
     * @return The nautical miles.
     */
    public static double fromMeters(double meters)
    {
        return meters / METERS_PER_NAUTICAL_MILE;
    }

    /**
     * Convert nautical miles to kilometers.
     *
     * @param nm The nautical miles.
     * @return The kilometers.
     */
    public static double toKilometers(double nm)
    {
        return nm / Constants.UNIT_PER_KILO * METERS_PER_NAUTICAL_MILE;
    }

    /**
     * Convert nautical miles to meters.
     *
     * @param nm The nautical miles.
     * @return The meters.
     */
    public static double toMeters(double nm)
    {
        return nm * METERS_PER_NAUTICAL_MILE;
    }

    /**
     * Constructor.
     *
     * @param nm The magnitude of the length.
     */
    public NauticalMiles(double nm)
    {
        super(nm);
    }

    /**
     * Construct this length from another length.
     *
     * @param dist The other length.
     */
    public NauticalMiles(Length dist)
    {
        super(Utilities.checkNull(dist, "dist").inMeters() / METERS_PER_NAUTICAL_MILE);
    }

    @Override
    public NauticalMiles clone()
    {
        return (NauticalMiles)super.clone();
    }

    @Override
    public String getLongLabel(boolean plural)
    {
        return plural ? NM_LONG_LABEL + "s" : NM_LONG_LABEL;
    }

    @Override
    public String getShortLabel(boolean plural)
    {
        return NM_SHORT_LABEL;
    }

    @Override
    public double inMeters()
    {
        return getMagnitude() * METERS_PER_NAUTICAL_MILE;
    }
}
