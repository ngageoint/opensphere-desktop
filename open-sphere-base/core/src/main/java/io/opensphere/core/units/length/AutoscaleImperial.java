package io.opensphere.core.units.length;

import io.opensphere.core.util.Constants;
import io.opensphere.core.util.Utilities;

/**
 * A length based on feet that scales automatically to statute miles depending
 * on the order of the magnitude.
 */
public class AutoscaleImperial extends AutoscaleLength
{
    /** How large a length can be before it is converted to miles. */
    private static final double AUTOSCALE_LIMIT_FEET = 10000. * Constants.FEET_PER_METER;

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param feet The magnitude of the length.
     */
    public AutoscaleImperial(double feet)
    {
        super(feet, feet > AUTOSCALE_LIMIT_FEET ? Length.create(StatuteMiles.class, StatuteMiles.fromFeet(feet))
                : Length.create(Feet.class, feet));
    }

    /**
     * Construct this length from another length.
     *
     * @param dist The other length.
     */
    public AutoscaleImperial(Length dist)
    {
        this(Utilities.checkNull(dist, "dist").inFeet());
    }

    @Override
    public String getSelectionLabel()
    {
        return "autoscale imperial";
    }

    @Override
    public double inFeet()
    {
        return getMagnitude();
    }
}
