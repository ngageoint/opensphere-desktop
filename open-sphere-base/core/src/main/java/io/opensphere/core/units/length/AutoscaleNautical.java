package io.opensphere.core.units.length;

import io.opensphere.core.util.Utilities;

/**
 * A length based on meters that scales automatically to nautical miles
 * depending on the order of the magnitude.
 */
public class AutoscaleNautical extends AutoscaleLength
{
    /** How large a length can be before it is converted to kilometers. */
    private static final double AUTOSCALE_LIMIT_METERS = 10000.;

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param meters The magnitude of the length.
     */
    public AutoscaleNautical(double meters)
    {
        super(meters, meters > AUTOSCALE_LIMIT_METERS ? Length.create(NauticalMiles.class, NauticalMiles.fromMeters(meters))
                : Length.create(Meters.class, meters));
    }

    /**
     * Construct this length from another length.
     *
     * @param dist The other length.
     */
    public AutoscaleNautical(Length dist)
    {
        this(Utilities.checkNull(dist, "dist").inMeters());
    }

    @Override
    public String getSelectionLabel()
    {
        return "autoscale nautical";
    }

    @Override
    public double inMeters()
    {
        return getMagnitude();
    }
}
