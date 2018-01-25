package io.opensphere.core.units.duration;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

import io.opensphere.core.units.InconvertibleUnits;
import io.opensphere.core.units.UnitsUtilities;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.Utilities;

/**
 * A duration with nanoseconds as its native unit.
 */
public final class Nanoseconds extends AbstractSecondBasedDuration
{
    /** The maximum long integer value of nanoseconds. */
    public static final Nanoseconds MAXLONG = new Nanoseconds(Long.MAX_VALUE);

    /** Long label. */
    public static final String NANOS_LONG_LABEL = "nanosecond";

    /** Short label. */
    public static final String NANOS_SHORT_LABEL = "ns";

    /** A single nanosecond. */
    public static final Nanoseconds ONE;

    /** Nanoseconds per second as a {@link BigDecimal}. */
    private static final BigDecimal NANOSECONDS_PER_SECOND = BigDecimal.valueOf(Constants.NANO_PER_UNIT);

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    static
    {
        ONE = UnitsUtilities.create(Nanoseconds.class, Long.TYPE, Long.valueOf(1L));
    }

    /**
     * Convenience method to get nanoseconds from a duration.
     *
     * @param dur The duration.
     * @return The nanoseconds.
     */
    public static Nanoseconds get(Duration dur)
    {
        return Duration.create(Nanoseconds.class, dur);
    }

    /**
     * Constructor.
     *
     * @param nanoseconds The magnitude of the duration. Precision beyond the
     *            width of a <tt>long</tt> will be lost.
     */
    public Nanoseconds(BigDecimal nanoseconds)
    {
        super(nanoseconds);
    }

    /**
     * Constructor.
     *
     * @param nanoseconds The nanoseconds.
     */
    public Nanoseconds(double nanoseconds)
    {
        super(BigDecimal.valueOf(nanoseconds));
    }

    /**
     * Construct this duration from another duration.
     *
     * @param dur The other duration.
     * @throws InconvertibleUnits If the input duration cannot be converted.
     */
    public Nanoseconds(Duration dur) throws InconvertibleUnits
    {
        this(Utilities.checkNull(dur, "dur").inReferenceUnits(REFERENCE_UNITS).multiply(NANOSECONDS_PER_SECOND));
    }

    /**
     * Constructor.
     *
     * @param nanos The milliseconds.
     */
    public Nanoseconds(long nanos)
    {
        super(nanos, 0);
    }

    /**
     * Constructor.
     *
     * @param unscaled The unscaled magnitude of the duration.
     * @param scale The scale of the duration.
     *
     * @see BigDecimal
     */
    public Nanoseconds(long unscaled, int scale)
    {
        super(unscaled, scale);
    }

    @Override
    public void addTo(Calendar cal)
    {
        cal.add(Calendar.MILLISECOND, new Milliseconds(this).intValue());
    }

    @Override
    public Nanoseconds clone()
    {
        return (Nanoseconds)super.clone();
    }

    @Override
    public ChronoUnit getChronoUnit()
    {
        return ChronoUnit.NANOS;
    }

    @Override
    public String getLongLabel(boolean plural)
    {
        return plural ? NANOS_LONG_LABEL + "s" : NANOS_LONG_LABEL;
    }

    @Override
    public String getShortLabel(boolean plural)
    {
        return NANOS_SHORT_LABEL;
    }

    @Override
    public BigDecimal inReferenceUnits()
    {
        return getMagnitude().divide(NANOSECONDS_PER_SECOND);
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
