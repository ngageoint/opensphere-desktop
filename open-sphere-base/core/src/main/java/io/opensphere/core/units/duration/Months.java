package io.opensphere.core.units.duration;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

import io.opensphere.core.units.InconvertibleUnits;
import io.opensphere.core.units.UnitsUtilities;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.Utilities;

/**
 * A duration with months as its native unit.
 */
public final class Months extends AbstractMonthBasedDuration
{
    /** Maximum number of seconds in a month. */
    public static final BigDecimal MAX_SECONDS_PER_MONTH = BigDecimal
            .valueOf(Constants.SECONDS_PER_DAY * Constants.MAX_DAYS_PER_MONTH);

    /** Minimum number of seconds in a month. */
    public static final BigDecimal MIN_SECONDS_PER_MONTH = BigDecimal
            .valueOf(Constants.SECONDS_PER_DAY * Constants.MIN_DAYS_PER_MONTH);

    /** Long label. */
    public static final String MONTHS_LONG_LABEL = "month";

    /** Short label. */
    public static final String MONTHS_SHORT_LABEL = "mon";

    /** A single month. */
    public static final Months ONE;

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    static
    {
        ONE = UnitsUtilities.create(Months.class, Long.TYPE, Long.valueOf(1L));
    }

    /**
     * Convenience method to get months from a duration.
     *
     * @param dur The duration.
     * @return The months.
     */
    public static Months get(Duration dur)
    {
        return Duration.create(Months.class, dur);
    }

    /**
     * Constructor.
     *
     * @param months The magnitude of the duration. Precision beyond the width
     *            of a <tt>long</tt> will be lost.
     */
    public Months(BigDecimal months)
    {
        super(months);
    }

    /**
     * Constructor.
     *
     * @param months The months.
     */
    public Months(double months)
    {
        super(BigDecimal.valueOf(months));
    }

    /**
     * Construct this duration from another duration.
     *
     * @param dur The other duration.
     * @throws InconvertibleUnits If the input duration cannot be converted.
     */
    public Months(Duration dur) throws InconvertibleUnits
    {
        this(Utilities.checkNull(dur, "dur").inReferenceUnits(REFERENCE_UNITS));
    }

    /**
     * Constructor.
     *
     * @param months The months.
     */
    public Months(long months)
    {
        super(months, 0);
    }

    /**
     * Constructor.
     *
     * @param unscaled The unscaled magnitude of the duration.
     * @param scale The scale of the duration.
     *
     * @see BigDecimal
     */
    public Months(long unscaled, int scale)
    {
        super(unscaled, scale);
    }

    @Override
    public void addTo(Calendar cal) throws ArithmeticException
    {
        cal.add(Calendar.MONTH, getMagnitude().intValueExact());
    }

    @Override
    public Months clone()
    {
        return (Months)super.clone();
    }

    @Override
    public ChronoUnit getChronoUnit()
    {
        return ChronoUnit.MONTHS;
    }

    @Override
    public String getLongLabel(boolean plural)
    {
        return plural ? MONTHS_LONG_LABEL + "s" : MONTHS_LONG_LABEL;
    }

    @Override
    public String getShortLabel(boolean plural)
    {
        return plural ? MONTHS_SHORT_LABEL + "s" : MONTHS_SHORT_LABEL;
    }

    @Override
    protected char getISO8601Designator()
    {
        return 'M';
    }

    @Override
    protected String getISO8601Prefix()
    {
        return "P";
    }

    @Override
    protected BigDecimal getMaxSecondsPerUnit()
    {
        return MAX_SECONDS_PER_MONTH;
    }

    @Override
    protected BigDecimal getMinSecondsPerUnit()
    {
        return MIN_SECONDS_PER_MONTH;
    }

    @Override
    public BigDecimal inReferenceUnits()
    {
        return getMagnitude();
    }
}
