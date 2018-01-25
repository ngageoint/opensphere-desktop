package io.opensphere.core.units.duration;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.units.AbstractUnitsProvider;
import io.opensphere.core.units.InconvertibleUnits;
import io.opensphere.core.units.InvalidUnitsException;

/**
 * A units provider for duration units.
 */
public final class DurationUnitsProvider extends AbstractUnitsProvider<Duration>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DurationUnitsProvider.class);

    /**
     * Constructor. This initializes the units provider with the following
     * units:
     * <ul>
     * <li>milliseconds</li>
     * <li>seconds</li>
     * <li>minutes</li>
     * <li>hours</li>
     * <li>days</li>
     * <li>weeks</li>
     * <li>months</li>
     * <li>years</li>
     * </ul>
     * Additional units may be added with {@link #addUnits(Class)}; units may be
     * removed with {@link #removeUnits(Class)}.
     */
    public DurationUnitsProvider()
    {
        List<Class<? extends Duration>> units = getAvailableUnitsUnsync();
        units.add(Milliseconds.class);
        units.add(Seconds.class);
        units.add(Minutes.class);
        units.add(Hours.class);
        units.add(Days.class);
        units.add(Weeks.class);
        units.add(Months.class);
        units.add(Years.class);
        setPreferredUnits(units.get(0));
    }

    /**
     * Add a {@link Duration} to a {@link Calendar}, mutating the
     * {@link Calendar}.
     *
     * @param cal The calendar.
     * @param dur The duration.
     */
    @SuppressWarnings("PMD.PreserveStackTrace")
    public void add(Calendar cal, Duration dur)
    {
        try
        {
            dur.addTo(cal);
        }
        catch (ArithmeticException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(e, e);
            }
            Duration adjusted = getLargestIntegerUnitType(dur);
            if (adjusted == null)
            {
                throw new IllegalArgumentException(dur + " cannot be represented as an integer for calendar addition.");
            }
            adjusted.addTo(cal);
        }
    }

    @Override
    public <T extends Duration> T convert(Class<T> desiredType, Duration from) throws InvalidUnitsException
    {
        return Duration.create(desiredType, from);
    }

    @Override
    public <T extends Duration> T fromUnitsAndMagnitude(Class<T> type, Number magnitude) throws InvalidUnitsException
    {
        return Duration.create(type, new BigDecimal(magnitude.toString()));
    }

    @Override
    public Class<? extends Duration> getDisplayClass(Duration value)
    {
        return value.getClass();
    }

    /**
     * Get a value in the largest unit type that can represent it as a whole
     * number.
     *
     * @param input The input duration.
     * @return The duration converted to the largest whole unit type.
     */
    public Duration getLargestIntegerUnitType(Duration input)
    {
        Duration largest = null;
        for (Class<? extends Duration> type : getAvailableUnits(false))
        {
            try
            {
                Duration dur = Duration.create(type, input);
                BigDecimal durMag = dur.getMagnitude();
                if (durMag.stripTrailingZeros().scale() <= 0 && (largest == null || largest.getMagnitude().compareTo(durMag) > 0))
                {
                    largest = dur;
                }
            }
            catch (InvalidUnitsException e)
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Invalid units encountered: " + e, e);
                }
            }
            catch (InconvertibleUnits e)
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Inconvertible encountered: " + e, e);
                }
            }
        }

        return largest;
    }

    @Override
    public String getLongLabel(Class<? extends Duration> type, boolean plural) throws InvalidUnitsException
    {
        return Duration.getLongLabel(type, plural);
    }

    @Override
    public String getSelectionLabel(Class<? extends Duration> type) throws InvalidUnitsException
    {
        return Duration.getSelectionLabel(type);
    }

    @Override
    public String getShortLabel(Class<? extends Duration> type, boolean plural) throws InvalidUnitsException
    {
        return Duration.getShortLabel(type, plural);
    }

    @Override
    public Class<Duration> getSuperType()
    {
        return Duration.class;
    }

    @Override
    public String toShortLabelString(Duration obj)
    {
        return obj.toShortLabelString();
    }

    @Override
    protected Class<? extends Number> getValueType()
    {
        return Duration.VALUE_TYPE;
    }

    @Override
    protected void testUnits(Class<? extends Duration> type) throws InvalidUnitsException
    {
        Duration.create(type, 0L);
    }
}
