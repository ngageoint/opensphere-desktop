package io.opensphere.core.units.duration;

import java.math.BigDecimal;
import java.math.RoundingMode;

import io.opensphere.core.units.InconvertibleUnits;

/**
 * Base class for durations the use months for their reference units.
 */
public abstract class AbstractMonthBasedDuration extends Duration
{
    /** The common units for durations convertible to this type. */
    protected static final Class<Months> REFERENCE_UNITS = Months.class;

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param magnitude The magnitude of the duration. Precision beyond the
     *            width of a <tt>long</tt> will be lost.
     */
    public AbstractMonthBasedDuration(BigDecimal magnitude)
    {
        super(magnitude);
    }

    /**
     * Constructor.
     *
     * @param unscaled The unscaled magnitude of the duration.
     * @param scale The scale of the duration.
     *
     * @see BigDecimal
     */
    public AbstractMonthBasedDuration(long unscaled, int scale)
    {
        super(unscaled, scale);
    }

    @Override
    public int compareTo(Duration o) throws InconvertibleUnits
    {
        if (o.getReferenceUnits() == Seconds.class)
        {
            int signCompare = Integer.compare(signum(), o.signum());
            if (signCompare != 0)
            {
                return signCompare;
            }

            // If I am larger than the maximum that the other duration could
            // be...
            else if (Integer.signum(getMagnitude().compareTo(
                    o.inReferenceUnits().divide(getMinSecondsPerUnit(), DIVISION_SCALE, RoundingMode.UP))) == signum())
            {
                return signum();
            }

            // If I am smaller than the minimum that the other duration could
            // be...
            else if (Integer.signum(o.inReferenceUnits().divide(getMaxSecondsPerUnit(), DIVISION_SCALE, RoundingMode.DOWN)
                    .compareTo(getMagnitude())) == signum())
            {
                return -signum();
            }
            else
            {
                return super.compareTo(o);
            }
        }
        return super.compareTo(o);
    }

    /**
     * Get the maximum number of seconds in one of me.
     *
     * @return The seconds.
     */
    protected abstract BigDecimal getMaxSecondsPerUnit();

    /**
     * Get the minimum number of seconds in one of me.
     *
     * @return The seconds.
     */
    protected abstract BigDecimal getMinSecondsPerUnit();

    @Override
    public final Class<? extends Duration> getReferenceUnits()
    {
        return REFERENCE_UNITS;
    }

    @Override
    public String toPrettyString()
    {
        return toShortLabelString();
    }

    @Override
    protected final BigDecimal inReferenceUnits(Class<? extends Duration> expected) throws InconvertibleUnits
    {
        if (isZero())
        {
            return BigDecimal.ZERO;
        }
        checkExpectedUnits(expected, REFERENCE_UNITS);
        return inReferenceUnits();
    }
}
