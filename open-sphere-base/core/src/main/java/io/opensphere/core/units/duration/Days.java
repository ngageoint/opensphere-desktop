package io.opensphere.core.units.duration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

import io.opensphere.core.units.InconvertibleUnits;
import io.opensphere.core.units.UnitsUtilities;
import io.opensphere.core.util.Constants;

/**
 * A duration with days as its native unit. Leap seconds cannot be accounted.
 */
public final class Days extends AbstractSecondBasedDuration
{
    /** Long label. */
    public static final String DAYS_LONG_LABEL = "day";

    /** Short label. */
    public static final String DAYS_SHORT_LABEL = "day";

    /** A single day. */
    public static final Days ONE;

    /** Seconds per day as a {@link BigDecimal}. */
    private static final BigDecimal SECONDS_PER_DAY = BigDecimal.valueOf(Constants.SECONDS_PER_DAY);

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    static
    {
        ONE = UnitsUtilities.create(Days.class, Long.TYPE, Long.valueOf(1L));
    }

    /**
     * Convenience method to get days from a duration.
     *
     * @param dur The duration.
     * @return The days.
     */
    public static Days get(Duration dur)
    {
        return Duration.create(Days.class, dur);
    }

    /**
     * Constructor.
     *
     * @param days The magnitude of the duration. Precision beyond the width of
     *            a <tt>long</tt> will be lost.
     */
    public Days(BigDecimal days)
    {
        super(days);
    }

    /**
     * Constructor.
     *
     * @param days The days.
     */
    public Days(double days)
    {
        super(BigDecimal.valueOf(days));
    }

    /**
     * Construct this duration from another duration.
     *
     * @param dur The other duration.
     * @throws InconvertibleUnits If the input duration cannot be converted.
     */
    public Days(Duration dur) throws InconvertibleUnits
    {
        this(dur.inReferenceUnits(REFERENCE_UNITS).divide(SECONDS_PER_DAY, DIVISION_SCALE, RoundingMode.HALF_EVEN)
                .stripTrailingZeros());
    }

    /**
     * Constructor.
     *
     * @param days The days.
     */
    public Days(long days)
    {
        super(days, 0);
    }

    /**
     * Constructor.
     *
     * @param unscaled The unscaled magnitude of the duration.
     * @param scale The scale of the duration.
     *
     * @see BigDecimal
     */
    public Days(long unscaled, int scale)
    {
        super(unscaled, scale);
    }

    @Override
    public void addTo(Calendar cal) throws ArithmeticException
    {
        cal.add(Calendar.DAY_OF_YEAR, getMagnitude().intValueExact());
    }

    @Override
    public Days clone()
    {
        return (Days)super.clone();
    }

    @Override
    public ChronoUnit getChronoUnit()
    {
        return ChronoUnit.DAYS;
    }

    @Override
    public String getLongLabel(boolean plural)
    {
        return plural ? DAYS_LONG_LABEL + "s" : DAYS_LONG_LABEL;
    }

    @Override
    public String getShortLabel(boolean plural)
    {
        return plural ? DAYS_SHORT_LABEL + "s" : DAYS_SHORT_LABEL;
    }

    @Override
    public BigDecimal inReferenceUnits()
    {
        return getMagnitude().multiply(SECONDS_PER_DAY);
    }

    @Override
    protected char getISO8601Designator()
    {
        return 'D';
    }

    @Override
    protected String getISO8601Prefix()
    {
        return "P";
    }
}
