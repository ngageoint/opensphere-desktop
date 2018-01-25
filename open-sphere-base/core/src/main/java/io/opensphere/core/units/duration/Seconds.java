package io.opensphere.core.units.duration;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

import io.opensphere.core.units.InconvertibleUnits;
import io.opensphere.core.units.UnitsUtilities;
import io.opensphere.core.util.Utilities;

/**
 * A duration with seconds as its native unit.
 */
public final class Seconds extends AbstractSecondBasedDuration
{
    /** A single second. */
    public static final Seconds ONE;

    /** Long label. */
    public static final String SECONDS_LONG_LABEL = "second";

    /** Short label. */
    public static final String SECONDS_SHORT_LABEL = "s";

    /** A duration of zero seconds. */
    public static final Seconds ZERO;

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    static
    {
        ONE = UnitsUtilities.create(Seconds.class, Long.TYPE, Long.valueOf(1L));
        ZERO = UnitsUtilities.create(Seconds.class, Long.TYPE, Long.valueOf(0L));
    }

    /**
     * Convenience method to get seconds from a duration.
     *
     * @param dur The duration.
     * @return The seconds.
     */
    public static Seconds get(Duration dur)
    {
        return Duration.create(Seconds.class, dur);
    }

    /**
     * Constructor.
     *
     * @param seconds The magnitude of the duration. Precision beyond the width
     *            of a <tt>long</tt> will be lost.
     */
    public Seconds(BigDecimal seconds)
    {
        super(seconds);
    }

    /**
     * Constructor.
     *
     * @param seconds The seconds.
     */
    public Seconds(double seconds)
    {
        super(BigDecimal.valueOf(seconds));
    }

    /**
     * Construct this duration from another duration.
     *
     * @param dur The other duration.
     * @throws InconvertibleUnits If the input duration cannot be converted.
     */
    public Seconds(Duration dur) throws InconvertibleUnits
    {
        this(Utilities.checkNull(dur, "dur").inReferenceUnits(REFERENCE_UNITS));
    }

    /**
     * Constructor.
     *
     * @param seconds The seconds.
     */
    public Seconds(long seconds)
    {
        super(seconds, 0);
    }

    /**
     * Constructor.
     *
     * @param unscaled The unscaled magnitude of the duration.
     * @param scale The scale of the duration.
     *
     * @see BigDecimal
     */
    public Seconds(long unscaled, int scale)
    {
        super(unscaled, scale);
    }

    @Override
    public void addTo(Calendar cal) throws ArithmeticException
    {
        cal.add(Calendar.SECOND, getMagnitude().intValueExact());
    }

    @Override
    public Seconds clone()
    {
        return (Seconds)super.clone();
    }

    @Override
    public ChronoUnit getChronoUnit()
    {
        return ChronoUnit.SECONDS;
    }

    @Override
    public String getLongLabel(boolean plural)
    {
        return plural ? SECONDS_LONG_LABEL + "s" : SECONDS_LONG_LABEL;
    }

    @Override
    public String getShortLabel(boolean plural)
    {
        return SECONDS_SHORT_LABEL;
    }

    @Override
    public BigDecimal inReferenceUnits()
    {
        return getMagnitude();
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
