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
 * A duration with minutes as its native unit.
 */
public final class Minutes extends AbstractSecondBasedDuration
{
    /** Long label. */
    public static final String MINUTES_LONG_LABEL = "minute";

    /** Short label. */
    public static final String MINUTES_SHORT_LABEL = "min";

    /** A single minute. */
    public static final Minutes ONE;

    /** Seconds per minute as a {@link BigDecimal}. */
    private static final BigDecimal SECONDS_PER_MINUTE = BigDecimal.valueOf(Constants.SECONDS_PER_MINUTE);

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    static
    {
        ONE = UnitsUtilities.create(Minutes.class, Long.TYPE, Long.valueOf(1L));
    }

    /**
     * Convenience method to get minutes from a duration.
     *
     * @param dur The duration.
     * @return The minutes.
     */
    public static Minutes get(Duration dur)
    {
        return Duration.create(Minutes.class, dur);
    }

    /**
     * Constructor.
     *
     * @param minutes The magnitude of the duration. Precision beyond the width
     *            of a <tt>long</tt> will be lost.
     */
    public Minutes(BigDecimal minutes)
    {
        super(minutes);
    }

    /**
     * Constructor.
     *
     * @param minutes The minutes.
     */
    public Minutes(double minutes)
    {
        super(BigDecimal.valueOf(minutes));
    }

    /**
     * Construct this duration from another duration.
     *
     * @param dur The other duration.
     * @throws InconvertibleUnits If the input duration cannot be converted.
     */
    public Minutes(Duration dur) throws InconvertibleUnits
    {
        this(Utilities.checkNull(dur, "dur").inReferenceUnits(REFERENCE_UNITS)
                .divide(SECONDS_PER_MINUTE, DIVISION_SCALE, RoundingMode.HALF_EVEN).stripTrailingZeros());
    }

    /**
     * Constructor.
     *
     * @param minutes The minutes.
     */
    public Minutes(long minutes)
    {
        super(minutes, 0);
    }

    /**
     * Constructor.
     *
     * @param unscaled The unscaled magnitude of the duration.
     * @param scale The scale of the duration.
     *
     * @see BigDecimal
     */
    public Minutes(long unscaled, int scale)
    {
        super(unscaled, scale);
    }

    @Override
    public void addTo(Calendar cal) throws ArithmeticException
    {
        cal.add(Calendar.MINUTE, getMagnitude().intValueExact());
    }

    @Override
    public Minutes clone()
    {
        return (Minutes)super.clone();
    }

    @Override
    public ChronoUnit getChronoUnit()
    {
        return ChronoUnit.MINUTES;
    }

    @Override
    public String getLongLabel(boolean plural)
    {
        return plural ? MINUTES_LONG_LABEL + "s" : MINUTES_LONG_LABEL;
    }

    @Override
    public String getShortLabel(boolean plural)
    {
        return plural ? MINUTES_SHORT_LABEL + "s" : MINUTES_SHORT_LABEL;
    }

    @Override
    public BigDecimal inReferenceUnits()
    {
        return getMagnitude().multiply(SECONDS_PER_MINUTE);
    }

    @Override
    protected char getISO8601Designator()
    {
        return 'M';
    }

    @Override
    protected String getISO8601Prefix()
    {
        return "PT";
    }
}
