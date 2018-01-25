package io.opensphere.core.units.length;

import io.opensphere.core.util.Utilities;

/**
 * A length based on meters that scales automatically to kilometers depending on
 * the order of the magnitude.
 */
public class AutoscaleMetric extends AutoscaleLength
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
    public AutoscaleMetric(double meters)
    {
        super(meters, meters > AUTOSCALE_LIMIT_METERS ? Length.create(Kilometers.class, Kilometers.fromMeters(meters))
                : Length.create(Meters.class, meters));
    }

    /**
     * Construct this length from another length.
     *
     * @param dist The other length.
     */
    public AutoscaleMetric(Length dist)
    {
        this(Utilities.checkNull(dist, "dist").inMeters());
    }

    @Override
    public String getSelectionLabel()
    {
        return "autoscale metric";
    }

    @Override
    public double inMeters()
    {
        return getMagnitude();
    }
}
