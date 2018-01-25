package io.opensphere.core.units.duration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

import io.opensphere.core.units.InconvertibleUnits;
import io.opensphere.core.units.UnitsUtilities;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.Utilities;

/**
 * A duration with hours as its native unit.
 */
public final class Hours extends AbstractSecondBasedDuration
{
    /** The long label. */
    public static final String HOURS_LONG_LABEL = "hour";

    /** The short label. */
    public static final String HOURS_SHORT_LABEL = "hr";

    /** A single hour. */
    public static final Hours ONE;

    /** Seconds per hour as a {@link BigDecimal}. */
    private static final BigDecimal SECONDS_PER_HOUR = BigDecimal.valueOf(Constants.SECONDS_PER_HOUR);

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    static
    {
        ONE = UnitsUtilities.create(Hours.class, Long.TYPE, Long.valueOf(1L));
    }

    /**
     * Convenience method to get hours from a duration.
     *
     * @param dur The duration.
     * @return The hours.
     */
    public static Hours get(Duration dur)
    {
        return Duration.create(Hours.class, dur);
    }

    /**
     * Constructor.
     *
     * @param hours The magnitude of the duration. Precision beyond the width of
     *            a <tt>long</tt> will be lost.
     */
    public Hours(BigDecimal hours)
    {
        super(hours);
    }

    /**
     * Constructor.
     *
     * @param hours The hours.
     */
    public Hours(double hours)
    {
        super(BigDecimal.valueOf(hours));
    }

    /**
     * Construct this duration from another duration.
     *
     * @param dur The other duration.
     * @throws InconvertibleUnits If the input duration cannot be converted.
     */
    public Hours(Duration dur) throws InconvertibleUnits
    {
        this(Utilities.checkNull(dur, "dur").inReferenceUnits(REFERENCE_UNITS)
                .divide(SECONDS_PER_HOUR, DIVISION_SCALE, RoundingMode.HALF_EVEN).stripTrailingZeros());
    }

    /**
     * Constructor.
     *
     * @param hours The hours.
     */
    public Hours(long hours)
    {
        super(hours, 0);
    }

    /**
     * Constructor.
     *
     * @param unscaled The unscaled magnitude of the duration.
     * @param scale The scale of the duration.
     *
     * @see BigDecimal
     */
    public Hours(long unscaled, int scale)
    {
        super(unscaled, scale);
    }

    @Override
    public void addTo(Calendar cal) throws ArithmeticException
    {
        cal.add(Calendar.HOUR_OF_DAY, getMagnitude().intValueExact());
    }

    @Override
    public Hours clone()
    {
        return (Hours)super.clone();
    }

    @Override
    public ChronoUnit getChronoUnit()
    {
        return ChronoUnit.HOURS;
    }

    @Override
    public String getLongLabel(boolean plural)
    {
        return plural ? HOURS_LONG_LABEL + "s" : HOURS_LONG_LABEL;
    }

    @Override
    public String getShortLabel(boolean plural)
    {
        return plural ? HOURS_SHORT_LABEL + "s" : HOURS_SHORT_LABEL;
    }

    @Override
    public BigDecimal inReferenceUnits()
    {
        return getMagnitude().multiply(SECONDS_PER_HOUR);
    }

    @Override
    protected char getISO8601Designator()
    {
        return 'H';
    }

    @Override
    protected String getISO8601Prefix()
    {
        return "PT";
    }
}
