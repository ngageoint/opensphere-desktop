package io.opensphere.core.units.length;

import io.opensphere.core.util.Utilities;

/**
 * A length with inches as its native unit.
 */
public final class Inches extends Length
{
    /** Inches per foot. */
    public static final int INCHES_PER_FOOT = 12;

    /** Long label. */
    public static final String INCHES_LONG_LABEL = "inch";

    /** Short label. */
    public static final String INCHES_SHORT_LABEL = "in";

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param in The magnitude of the length.
     */
    public Inches(double in)
    {
        super(in);
    }

    /**
     * Construct this length from another length.
     *
     * @param dist The other length.
     */
    public Inches(Length dist)
    {
        super(Utilities.checkNull(dist, "dist").inFeet() * INCHES_PER_FOOT);
    }

    @Override
    public Inches clone()
    {
        return (Inches)super.clone();
    }

    @Override
    public String getLongLabel(boolean plural)
    {
        return plural ? INCHES_LONG_LABEL + "es" : INCHES_LONG_LABEL;
    }

    @Override
    public String getShortLabel(boolean plural)
    {
        return INCHES_SHORT_LABEL;
    }

    @Override
    public double inFeet()
    {
        return getMagnitude() / INCHES_PER_FOOT;
    }
}
