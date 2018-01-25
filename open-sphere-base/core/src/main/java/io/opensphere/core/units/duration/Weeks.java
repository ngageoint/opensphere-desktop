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
 * A duration with weeks as its native unit. Leap seconds cannot be accounted.
 */
public final class Weeks extends AbstractSecondBasedDuration
{
    /** A single week. */
    public static final Weeks ONE;

    /** Two weeks. */
    public static final Weeks TWO;

    /** The long label. */
    public static final String WEEKS_LONG_LABEL = "week";

    /** The short label. */
    public static final String WEEKS_SHORT_LABEL = "wk";

    /** Seconds per week as a {@link BigDecimal}. */
    private static final BigDecimal SECONDS_PER_WEEK = BigDecimal.valueOf(Constants.SECONDS_PER_WEEK);

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    static
    {
        ONE = UnitsUtilities.create(Weeks.class, Long.TYPE, Long.valueOf(1L));
        TWO = UnitsUtilities.create(Weeks.class, Long.TYPE, Long.valueOf(2L));
    }

    /**
     * Convenience method to get weeks from a duration.
     *
     * @param dur The duration.
     * @return The weeks.
     */
    public static Weeks get(Duration dur)
    {
        return Duration.create(Weeks.class, dur);
    }

    /**
     * Constructor.
     *
     * @param weeks The magnitude of the duration. Precision beyond the width of
     *            a <tt>long</tt> will be lost.
     */
    public Weeks(BigDecimal weeks)
    {
        super(weeks);
    }

    /**
     * Constructor.
     *
     * @param weeks The weeks.
     */
    public Weeks(double weeks)
    {
        super(BigDecimal.valueOf(weeks));
    }

    /**
     * Construct this duration from another duration.
     *
     * @param dur The other duration.
     * @throws InconvertibleUnits If the input duration cannot be converted.
     */
    public Weeks(Duration dur) throws InconvertibleUnits
    {
        this(Utilities.checkNull(dur, "dur").inReferenceUnits(REFERENCE_UNITS)
                .divide(SECONDS_PER_WEEK, DIVISION_SCALE, RoundingMode.HALF_EVEN).stripTrailingZeros());
    }

    /**
     * Constructor.
     *
     * @param weeks The weeks.
     */
    public Weeks(long weeks)
    {
        super(weeks, 0);
    }

    /**
     * Constructor.
     *
     * @param unscaled The unscaled magnitude of the duration.
     * @param scale The scale of the duration.
     *
     * @see BigDecimal
     */
    public Weeks(long unscaled, int scale)
    {
        super(unscaled, scale);
    }

    @Override
    public void addTo(Calendar cal) throws ArithmeticException
    {
        cal.add(Calendar.WEEK_OF_YEAR, getMagnitude().intValueExact());
    }

    @Override
    public Weeks clone()
    {
        return (Weeks)super.clone();
    }

    @Override
    public ChronoUnit getChronoUnit()
    {
        return ChronoUnit.WEEKS;
    }

    @Override
    public String getLongLabel(boolean plural)
    {
        return plural ? WEEKS_LONG_LABEL + "s" : WEEKS_LONG_LABEL;
    }

    @Override
    public String getShortLabel(boolean plural)
    {
        return plural ? WEEKS_SHORT_LABEL + "s" : WEEKS_SHORT_LABEL;
    }

    @Override
    public BigDecimal inReferenceUnits()
    {
        return getMagnitude().multiply(SECONDS_PER_WEEK);
    }

    @Override
    protected char getISO8601Designator()
    {
        return 'W';
    }

    @Override
    protected String getISO8601Prefix()
    {
        return "P";
    }
}
