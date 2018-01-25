package io.opensphere.core.units.length;

import io.opensphere.core.util.Constants;
import io.opensphere.core.util.Utilities;

/**
 * A length with kilometers as its native unit.
 */
public final class Kilometers extends Length
{
    /** Long label. */
    public static final String KILOMETERS_LONG_LABEL = "kilometer";

    /** Short label. */
    public static final String KILOMETERS_SHORT_LABEL = "km";

    /** One kilometer. */
    public static final Kilometers ONE = Length.create(Kilometers.class, 1.);

    /** Zero kilometers. */
    public static final Kilometers ZERO = Length.create(Kilometers.class, 0.);

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Convert meters to kilometers.
     *
     * @param meters The meters.
     * @return The kilometers.
     */
    public static double fromMeters(double meters)
    {
        return meters / Constants.UNIT_PER_KILO;
    }

    /**
     * Convert kilometers to meters.
     *
     * @param kilometers The kilometers.
     * @return The meters.
     */
    public static double toMeters(double kilometers)
    {
        return kilometers * Constants.UNIT_PER_KILO;
    }

    /**
     * Constructor.
     *
     * @param kilometers The magnitude of the length.
     */
    public Kilometers(double kilometers)
    {
        super(kilometers);
    }

    /**
     * Construct this length from another length.
     *
     * @param dist The other length.
     */
    public Kilometers(Length dist)
    {
        super(fromMeters(Utilities.checkNull(dist, "dist").inMeters()));
    }

    @Override
    public Kilometers clone()
    {
        return (Kilometers)super.clone();
    }

    @Override
    public String getLongLabel(boolean plural)
    {
        return plural ? KILOMETERS_LONG_LABEL + "s" : KILOMETERS_LONG_LABEL;
    }

    @Override
    public String getShortLabel(boolean plural)
    {
        return KILOMETERS_SHORT_LABEL;
    }

    @Override
    public double inMeters()
    {
        return toMeters(getMagnitude());
    }
}
