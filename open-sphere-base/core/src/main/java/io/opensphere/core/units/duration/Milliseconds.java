package io.opensphere.core.units.duration;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

import io.opensphere.core.units.InconvertibleUnits;
import io.opensphere.core.units.UnitsUtilities;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.Utilities;

/**
 * A duration with milliseconds as its native unit.
 */
public final class Milliseconds extends AbstractSecondBasedDuration
{
    /** Long label. */
    public static final String MILLIS_LONG_LABEL = "millisecond";

    /** Short label. */
    public static final String MILLIS_SHORT_LABEL = "ms";

    /** A single millisecond. */
    public static final Milliseconds ONE;

    /** Zero milliseconds. */
    public static final Milliseconds ZERO;

    /** Milliseconds per second as a {@link BigDecimal}. */
    private static final BigDecimal MILLISECONDS_PER_SECOND = BigDecimal.valueOf(Constants.MILLI_PER_UNIT);

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    static
    {
        ONE = UnitsUtilities.create(Milliseconds.class, Long.TYPE, Long.valueOf(1L));
        ZERO = UnitsUtilities.create(Milliseconds.class, Long.TYPE, Long.valueOf(0L));
    }

    /**
     * Convenience method to get milliseconds from a duration.
     *
     * @param dur The duration.
     * @return The milliseconds.
     */
    public static Milliseconds get(Duration dur)
    {
        return Duration.create(Milliseconds.class, dur);
    }

    /**
     * Constructor.
     *
     * @param milliseconds The magnitude of the duration. Precision beyond the
     *            width of a <tt>long</tt> will be lost.
     */
    public Milliseconds(BigDecimal milliseconds)
    {
        super(milliseconds);
    }

    /**
     * Constructor.
     *
     * @param milliseconds The milliseconds.
     */
    public Milliseconds(double milliseconds)
    {
        super(BigDecimal.valueOf(milliseconds));
    }

    /**
     * Construct this duration from another duration.
     *
     * @param dur The other duration.
     * @throws InconvertibleUnits If the input duration cannot be converted.
     */
    public Milliseconds(Duration dur) throws InconvertibleUnits
    {
        this(Utilities.checkNull(dur, "dur").inReferenceUnits(REFERENCE_UNITS).multiply(MILLISECONDS_PER_SECOND));
    }

    /**
     * Constructor.
     *
     * @param millis The milliseconds.
     */
    public Milliseconds(long millis)
    {
        super(millis, 0);
    }

    /**
     * Constructor.
     *
     * @param unscaled The unscaled magnitude of the duration.
     * @param scale The scale of the duration.
     *
     * @see BigDecimal
     */
    public Milliseconds(long unscaled, int scale)
    {
        super(unscaled, scale);
    }

    @Override
    public void addTo(Calendar cal) throws ArithmeticException
    {
        cal.add(Calendar.MILLISECOND, getMagnitude().intValueExact());
    }

    @Override
    public Milliseconds clone()
    {
        return (Milliseconds)super.clone();
    }

    @Override
    public ChronoUnit getChronoUnit()
    {
        return ChronoUnit.MILLIS;
    }

    @Override
    public String getLongLabel(boolean plural)
    {
        return plural ? MILLIS_LONG_LABEL + "s" : MILLIS_LONG_LABEL;
    }

    @Override
    public String getShortLabel(boolean plural)
    {
        return MILLIS_SHORT_LABEL;
    }

    @Override
    public BigDecimal inReferenceUnits()
    {
        return getMagnitude().divide(MILLISECONDS_PER_SECOND);
    }

    @Override
    public String toISO8601String()
    {
        return new StringBuilder().append(getISO8601Prefix()).append(formatForISO8601(inReferenceUnits()))
                .append(getISO8601Designator()).toString();
    }

    @Override
    protected char getISO8601Designator()
    {
        return 'S';
    }

    @Override
    protected String getISO8601Prefix()
    {
        return "PT";
    }
}
