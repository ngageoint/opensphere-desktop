package io.opensphere.core.units.length;

import io.opensphere.core.util.Utilities;

/**
 * A length with statute miles as its native unit.
 */
public final class StatuteMiles extends Length
{
    /** Feet per statute mile (exact). */
    public static final int FEET_PER_STATUTE_MILE = 5280;

    /** Long label. */
    public static final String MILES_LONG_LABEL = "statute mile";

    /** Short label. */
    public static final String MILES_SHORT_LABEL = "mi";

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Convert feet to statute miles.
     *
     * @param feet The feet.
     * @return The statute miles.
     */
    public static double fromFeet(double feet)
    {
        return feet / FEET_PER_STATUTE_MILE;
    }

    /**
     * Convert statute miles to feet.
     *
     * @param miles The statute miles.
     * @return The feet.
     */
    public static double toFeet(double miles)
    {
        return miles * FEET_PER_STATUTE_MILE;
    }

    /**
     * Constructor.
     *
     * @param mi The magnitude of the length.
     */
    public StatuteMiles(double mi)
    {
        super(mi);
    }

    /**
     * Construct this length from another length.
     *
     * @param dist The other length.
     */
    public StatuteMiles(Length dist)
    {
        super(Utilities.checkNull(dist, "dist").inFeet() / FEET_PER_STATUTE_MILE);
    }

    @Override
    public StatuteMiles clone()
    {
        return (StatuteMiles)super.clone();
    }

    @Override
    public String getLongLabel(boolean plural)
    {
        return plural ? MILES_LONG_LABEL + "s" : MILES_LONG_LABEL;
    }

    @Override
    public String getShortLabel(boolean plural)
    {
        return MILES_SHORT_LABEL;
    }

    @Override
    public double inFeet()
    {
        return getMagnitude() * FEET_PER_STATUTE_MILE;
    }
}
