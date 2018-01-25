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
 * A duration with years as its native unit.
 */
public final class Years extends AbstractMonthBasedDuration
{
    /** Maximum number of seconds in a year. */
    public static final BigDecimal MAX_SECONDS_PER_YEAR = BigDecimal
            .valueOf(Constants.SECONDS_PER_DAY * Constants.MAX_DAYS_PER_YEAR);

    /** Minimum number of seconds in a year. */
    public static final BigDecimal MIN_SECONDS_PER_YEAR = BigDecimal
            .valueOf(Constants.SECONDS_PER_DAY * Constants.MIN_DAYS_PER_YEAR);

    /** A single year. */
    public static final Years ONE;

    /** The long label. */
    public static final String YEARS_LONG_LABEL = "year";

    /** The short label. */
    public static final String YEARS_SHORT_LABEL = "yr";

    /** Months per year as a {@link BigDecimal}. */
    private static final BigDecimal MONTHS_PER_YEAR = BigDecimal.valueOf(Constants.MONTHS_PER_YEAR);

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    static
    {
        ONE = UnitsUtilities.create(Years.class, Long.TYPE, Long.valueOf(1L));
    }

    /**
     * Convenience method to get years from a duration.
     *
     * @param dur The duration.
     * @return The years.
     */
    public static Years get(Duration dur)
    {
        return Duration.create(Years.class, dur);
    }

    /**
     * Constructor.
     *
     * @param years The magnitude of the duration. Precision beyond the width of
     *            a <tt>long</tt> will be lost.
     */
    public Years(BigDecimal years)
    {
        super(years);
    }

    /**
     * Constructor.
     *
     * @param years The years.
     */
    public Years(double years)
    {
        super(BigDecimal.valueOf(years));
    }

    /**
     * Construct this duration from another duration.
     *
     * @param dur The other duration.
     * @throws InconvertibleUnits If the input duration cannot be converted.
     */
    public Years(Duration dur) throws InconvertibleUnits
    {
        this(Utilities.checkNull(dur, "dur").inReferenceUnits(REFERENCE_UNITS)
                .divide(MONTHS_PER_YEAR, DIVISION_SCALE, RoundingMode.HALF_EVEN).stripTrailingZeros());
    }

    /**
     * Constructor.
     *
     * @param years The years.
     */
    public Years(long years)
    {
        super(years, 0);
    }

    /**
     * Constructor.
     *
     * @param unscaled The unscaled magnitude of the duration.
     * @param scale The scale of the duration.
     *
     * @see BigDecimal
     */
    public Years(long unscaled, int scale)
    {
        super(unscaled, scale);
    }

    @Override
    public void addTo(Calendar cal) throws ArithmeticException
    {
        cal.add(Calendar.YEAR, getMagnitude().intValueExact());
    }

    @Override
    public Years clone()
    {
        return (Years)super.clone();
    }

    @Override
    public ChronoUnit getChronoUnit()
    {
        return ChronoUnit.YEARS;
    }

    @Override
    public String getLongLabel(boolean plural)
    {
        return plural ? YEARS_LONG_LABEL + "s" : YEARS_LONG_LABEL;
    }

    @Override
    public String getShortLabel(boolean plural)
    {
        return plural ? YEARS_SHORT_LABEL + "s" : YEARS_SHORT_LABEL;
    }

    @Override
    public BigDecimal inReferenceUnits()
    {
        return getMagnitude().multiply(MONTHS_PER_YEAR);
    }

    @Override
    protected char getISO8601Designator()
    {
        return 'Y';
    }

    @Override
    protected String getISO8601Prefix()
    {
        return "P";
    }

    @Override
    protected BigDecimal getMaxSecondsPerUnit()
    {
        return MAX_SECONDS_PER_YEAR;
    }

    @Override
    protected BigDecimal getMinSecondsPerUnit()
    {
        return MIN_SECONDS_PER_YEAR;
    }
}
