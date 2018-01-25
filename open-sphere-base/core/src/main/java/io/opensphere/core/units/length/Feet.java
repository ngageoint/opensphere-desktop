package io.opensphere.core.units.length;

import io.opensphere.core.util.Utilities;

/**
 * A length with feet as its native unit.
 */
public final class Feet extends Length
{
    /** Long label. */
    public static final String FEET_LONG_LABEL1 = "foot";

    /** Long label. */
    public static final String FEET_LONG_LABEL2 = "feet";

    /** Short label. */
    public static final String FEET_SHORT_LABEL = "ft";

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param feet The magnitude of the length.
     */
    public Feet(double feet)
    {
        super(feet);
    }

    /**
     * Construct this length from another length.
     *
     * @param dist The other length.
     */
    public Feet(Length dist)
    {
        super(Utilities.checkNull(dist, "dist").inFeet());
    }

    @Override
    public Feet clone()
    {
        return (Feet)super.clone();
    }

    @Override
    public String getLongLabel(boolean plural)
    {
        return plural ? FEET_LONG_LABEL2 : FEET_LONG_LABEL1;
    }

    @Override
    public String getShortLabel(boolean plural)
    {
        return FEET_SHORT_LABEL;
    }

    @Override
    public double inFeet()
    {
        return getMagnitude();
    }
}
