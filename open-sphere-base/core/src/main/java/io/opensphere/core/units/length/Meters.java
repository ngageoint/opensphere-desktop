package io.opensphere.core.units.length;

import io.opensphere.core.util.Utilities;

/**
 * A length with meters as its native unit.
 */
public final class Meters extends Length
{
    /** Long label. */
    public static final String METERS_LONG_LABEL = "meter";

    /** Short label. */
    public static final String METERS_SHORT_LABEL = "m";

    /** One meter. */
    public static final Meters ONE = Length.create(Meters.class, 1.);

    /** Zero meters. */
    public static final Meters ZERO = Length.create(Meters.class, 0.);

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Convenience method to get meters from a length.
     *
     * @param length The length.
     * @return The meters.
     */
    public static Meters get(Length length)
    {
        return Length.create(Meters.class, length);
    }

    /**
     * Constructor.
     *
     * @param meters The magnitude of the length.
     */
    public Meters(double meters)
    {
        super(meters);
    }

    /**
     * Construct this length from another length.
     *
     * @param dist The other length.
     */
    public Meters(Length dist)
    {
        super(Utilities.checkNull(dist, "dist").inMeters());
    }

    @Override
    public Meters clone()
    {
        return (Meters)super.clone();
    }

    @Override
    public String getLongLabel(boolean plural)
    {
        return plural ? METERS_LONG_LABEL + "s" : METERS_LONG_LABEL;
    }

    @Override
    public String getShortLabel(boolean plural)
    {
        return METERS_SHORT_LABEL;
    }

    @Override
    public double inMeters()
    {
        return getMagnitude();
    }
}
