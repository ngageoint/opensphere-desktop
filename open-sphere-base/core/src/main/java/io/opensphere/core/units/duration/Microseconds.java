package io.opensphere.core.units.duration;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

import io.opensphere.core.units.InconvertibleUnits;
import io.opensphere.core.units.UnitsUtilities;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.Utilities;

/**
 * A duration with microseconds as its native unit.
 */
public final class Microseconds extends AbstractSecondBasedDuration
{
    /** Long label. */
    public static final String MICROS_LONG_LABEL = "microsecond";

    /** Short label. */
    public static final String MICROS_SHORT_LABEL = "μs";

    /** A single microsecond. */
    public static final Microseconds ONE;

    /** Microseconds per second as a {@link BigDecimal}. */
    private static final BigDecimal MICROSECONDS_PER_SECOND = BigDecimal.valueOf(Constants.MICRO_PER_UNIT);

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    static
    {
        ONE = UnitsUtilities.create(Microseconds.class, Long.TYPE, Long.valueOf(1L));
    }

    /**
     * Convenience method to get microseconds from a duration.
     *
     * @param dur The duration.
     * @return The microseconds.
     */
    public static Microseconds get(Duration dur)
    {
        return Duration.create(Microseconds.class, dur);
    }

    /**
     * Constructor.
     *
     * @param microseconds The magnitude of the duration. Precision beyond the
     *            width of a <tt>long</tt> will be lost.
     */
    public Microseconds(BigDecimal microseconds)
    {
        super(microseconds);
    }

    /**
     * Constructor.
     *
     * @param microseconds The microseconds.
     */
    public Microseconds(double microseconds)
    {
        super(BigDecimal.valueOf(microseconds));
    }

    /**
     * Construct this duration from another duration.
     *
     * @param dur The other duration.
     * @throws InconvertibleUnits If the input duration cannot be converted.
     */
    public Microseconds(Duration dur) throws InconvertibleUnits
    {
        this(Utilities.checkNull(dur, "dur").inReferenceUnits(REFERENCE_UNITS).multiply(MICROSECONDS_PER_SECOND));
    }

    /**
     * Constructor.
     *
     * @param micros The milliseconds.
     */
    public Microseconds(long micros)
    {
        super(micros, 0);
    }

    /**
     * Constructor.
     *
     * @param unscaled The unscaled magnitude of the duration.
     * @param scale The scale of the duration.
     *
     * @see BigDecimal
     */
    public Microseconds(long unscaled, int scale)
    {
        super(unscaled, scale);
    }

    @Override
    public void addTo(Calendar cal)
    {
        cal.add(Calendar.MILLISECOND, new Milliseconds(this).intValue());
    }

    @Override
    public Microseconds clone()
    {
        return (Microseconds)super.clone();
    }

    @Override
    public ChronoUnit getChronoUnit()
    {
        return ChronoUnit.MICROS;
    }

    @Override
    public String getLongLabel(boolean plural)
    {
        return plural ? MICROS_LONG_LABEL + "s" : MICROS_LONG_LABEL;
    }

    @Override
    public String getShortLabel(boolean plural)
    {
        return MICROS_SHORT_LABEL;
    }

    @Override
    public BigDecimal inReferenceUnits()
    {
        return getMagnitude().divide(MICROSECONDS_PER_SECOND);
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
