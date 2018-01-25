package io.opensphere.core.units.length;

import io.opensphere.core.util.Utilities;

/**
 * A length with yards as its native unit.
 */
public final class Yards extends Length
{
    /** Feet per yard. */
    public static final int FEET_PER_YARD = 3;

    /** Long label. */
    public static final String YARDS_LONG_LABEL = "yard";

    /** Short label. */
    public static final String YARDS_SHORT_LABEL = "yd";

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param yards The magnitude of the length.
     */
    public Yards(double yards)
    {
        super(yards);
    }

    /**
     * Construct this length from another length.
     *
     * @param dist The other length.
     */
    public Yards(Length dist)
    {
        super(Utilities.checkNull(dist, "dist").inFeet() / FEET_PER_YARD);
    }

    @Override
    public Yards clone()
    {
        return (Yards)super.clone();
    }

    @Override
    public String getLongLabel(boolean plural)
    {
        return plural ? YARDS_LONG_LABEL + "s" : YARDS_LONG_LABEL;
    }

    @Override
    public String getShortLabel(boolean plural)
    {
        return plural ? YARDS_SHORT_LABEL + "s" : YARDS_SHORT_LABEL;
    }

    @Override
    public double inFeet()
    {
        return getMagnitude() * FEET_PER_YARD;
    }
}
