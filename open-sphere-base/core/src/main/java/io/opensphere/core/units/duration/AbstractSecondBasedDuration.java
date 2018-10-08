package io.opensphere.core.units.duration;

import java.math.BigDecimal;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.units.InconvertibleUnits;

/**
 * Base class for durations the use seconds for their reference units.
 */
public abstract class AbstractSecondBasedDuration extends Duration
{
    /** The common units for durations convertible to this type. */
    protected static final Class<Seconds> REFERENCE_UNITS = Seconds.class;

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param magnitude The magnitude of the duration. Precision beyond the
     *            width of a <tt>long</tt> will be lost.
     */
    public AbstractSecondBasedDuration(BigDecimal magnitude)
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
    public AbstractSecondBasedDuration(long unscaled, int scale)
    {
        super(unscaled, scale);
    }

    @Override
    @SuppressFBWarnings("RV_NEGATING_RESULT_OF_COMPARETO")
    public int compareTo(Duration o) throws InconvertibleUnits
    {
        if (o.getReferenceUnits() == Months.class)
        {
            return -o.compareTo(this);
        }
        return super.compareTo(o);
    }

    @Override
    public final Class<? extends Duration> getReferenceUnits()
    {
        return REFERENCE_UNITS;
    }

    @Override
    public String toPrettyString()
    {
        return millisToPrettyString(Milliseconds.get(this).longValue());
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
