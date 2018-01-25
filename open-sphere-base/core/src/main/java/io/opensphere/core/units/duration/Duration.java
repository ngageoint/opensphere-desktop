package io.opensphere.core.units.duration;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;

import io.opensphere.core.units.InconvertibleUnits;
import io.opensphere.core.units.InvalidUnitsException;
import io.opensphere.core.units.UnitsUtilities;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.JAXBable;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.ExpectedCloneableException;

/**
 * A unit-aware representation of a duration.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class Duration implements Cloneable, Serializable, Comparable<Duration>, JAXBable<JAXBDuration>
{
    /**
     * Scale used for division calculations. {@value} was chosen somewhat
     * arbitrarily to achieve good precision that is better than that of a
     * primitive {@code long}.
     *
     * @see BigDecimal
     */
    protected static final int DIVISION_SCALE = 32;

    /** Type of the values. */
    static final Class<BigDecimal> VALUE_TYPE = BigDecimal.class;

    /** The segments of a duration used in formatting. */
    private static final int[] FORMAT_SEGMENTS = new int[] { Constants.MILLIS_PER_DAY, Constants.MILLIS_PER_HOUR,
        Constants.MILLIS_PER_MINUTE, Constants.MILLI_PER_UNIT };

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(Duration.class);

    /** {@link Integer#MAX_VALUE} as a {@link BigDecimal}. */
    private static final BigDecimal MAX_INT = BigDecimal.valueOf(Integer.MAX_VALUE);

    /** {@link Long#MAX_VALUE} as a {@link BigDecimal}. */
    private static final BigDecimal MAX_LONG = BigDecimal.valueOf(Long.MAX_VALUE);

    /** {@link Long#MAX_VALUE} as a {@link BigInteger}. */
    private static final BigInteger MAX_LONG_INT = BigInteger.valueOf(Long.MAX_VALUE);

    /** {@link Integer#MIN_VALUE} as a {@link BigInteger}. */
    private static final BigDecimal MIN_INT = BigDecimal.valueOf(Integer.MIN_VALUE);

    /** {@link Long#MIN_VALUE} as a {@link BigDecimal}. */
    private static final BigDecimal MIN_LONG = BigDecimal.valueOf(Long.MIN_VALUE);

    /** {@link Long#MIN_VALUE} as a {@link BigInteger}. */
    private static final BigInteger MIN_LONG_INT = BigInteger.valueOf(Long.MIN_VALUE);

    /** The segments names used in formatting. */
    private static final String[] SEGMENT_LONG_NAMES = new String[] { "day", "hour", "min", "sec" };

    /** The segments names used in formatting. */
    private static final String[] SEGMENT_SHORT_NAMES = new String[] { "d", "h", "m", "s" };

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The scale for the magnitude. */
    private final int myScale;

    /** The unscaled magnitude. Units are defined by subclasses. */
    private final long myUnscaledMagnitude;

    /**
     * Create a new duration object.
     *
     * @param unit The ChronoUnit of the magnitude.
     * @param magnitude The magnitude of the new object, in its native units.
     *
     * @return The new duration object.
     */
    public static Duration create(ChronoUnit unit, BigDecimal magnitude)
    {
        Class<? extends Duration> type = fromChronoUnit(unit);
        return type == null ? null : create(type, magnitude);
    }

    /**
     * Create a new duration object.
     *
     * @param unit The ChronoUnit of the magnitude.
     * @param from The source duration object.
     * @return The new duration object.
     */
    public static Duration create(ChronoUnit unit, Duration from)
    {
        Class<? extends Duration> type = fromChronoUnit(unit);
        return type == null ? null : create(type, from);
    }

    /**
     * Create a new duration object.
     *
     * @param unit The ChronoUnit of the magnitude.
     * @param magnitude The magnitude of the new object, in its native units.
     *
     * @return The new duration object.
     */
    public static Duration create(ChronoUnit unit, long magnitude)
    {
        Class<? extends Duration> type = fromChronoUnit(unit);
        return type == null ? null : create(type, magnitude);
    }

    /**
     * Create a new duration object.
     *
     * @param unit The ChronoUnit of the magnitude.
     * @param magnitude The unscaled magnitude of the new object, in its native
     *            units.
     * @param scale The scale of the magnitude.
     * @return The new duration object.
     */
    public static Duration create(ChronoUnit unit, long magnitude, int scale)
    {
        Class<? extends Duration> type = fromChronoUnit(unit);
        return type == null ? null : create(type, magnitude, scale);
    }

    /**
     * Create a new duration object.
     *
     * @param <T> The type of the duration object.
     * @param type The type of the duration object.
     * @param magnitude The magnitude of the new object, in its native units.
     * @return The new duration object.
     * @throws InvalidUnitsException If the type is invalid.
     */
    public static <T extends Duration> T create(Class<T> type, BigDecimal magnitude)
    {
        return UnitsUtilities.create(type, VALUE_TYPE, magnitude);
    }

    /**
     * Return a duration in the specified units equivalent to another duration.
     * If the input duration is already in the requested units, it will be
     * returned as-is.
     *
     * @param <T> The type of the duration object.
     * @param type The type of the duration object.
     * @param from The source duration object.
     * @return The new duration object.
     * @throws InvalidUnitsException If the type is invalid.
     * @throws InconvertibleUnits If the duration types are not compatible.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Duration> T create(Class<T> type, Duration from)
    {
        if (type.isInstance(from))
        {
            return (T)from;
        }
        return UnitsUtilities.create(type, Duration.class, from);
    }

    /**
     * Create a new duration object.
     *
     * @param <T> The type of the duration object.
     * @param type The type of the duration object.
     * @param magnitude The magnitude of the new object, in its native units.
     * @return The new duration object.
     * @throws InvalidUnitsException If the type is invalid.
     */
    public static <T extends Duration> T create(Class<T> type, long magnitude)
    {
        return UnitsUtilities.create(type, Long.TYPE, Long.valueOf(magnitude));
    }

    /**
     * Create a new duration object.
     *
     * @param <T> The type of the duration object.
     * @param type The type of the duration object.
     * @param magnitude The unscaled magnitude of the new object, in its native
     *            units.
     * @param scale The scale of the magnitude.
     * @return The new duration object.
     * @throws InvalidUnitsException If the type is invalid.
     * @see BigDecimal
     */
    public static <T extends Duration> T create(Class<T> type, long magnitude, int scale)
    {
        Class<?>[] argTypes = new Class<?>[] { Long.TYPE, Integer.TYPE };
        Object[] args = new Object[] { Long.valueOf(magnitude), Integer.valueOf(scale) };
        return UnitsUtilities.create(type, argTypes, args);
    }

    /**
     * Convert from a {@link ChronoUnit} enum to a {@link Duration} subclass.
     *
     * @param unit The unit.
     * @return The duration class.
     */
    public static Class<? extends Duration> fromChronoUnit(ChronoUnit unit)
    {
        Class<? extends Duration> duration;
        switch (unit)
        {
            case YEARS:
                duration = Years.class;
                break;
            case MONTHS:
                duration = Months.class;
                break;
            case WEEKS:
                duration = Weeks.class;
                break;
            case DAYS:
                duration = Days.class;
                break;
            case HOURS:
                duration = Hours.class;
                break;
            case MINUTES:
                duration = Minutes.class;
                break;
            case SECONDS:
                duration = Seconds.class;
                break;
            case MILLIS:
                duration = Milliseconds.class;
                break;
            case MICROS:
                duration = Microseconds.class;
                break;
            case NANOS:
                duration = Nanoseconds.class;
                break;
            default:
                duration = null;
                break;
        }
        return duration;
    }

    /**
     * Parse an ISO8601 duration.
     *
     * @param input The ISO8601 duration string.
     * @return The duration.
     * @throws ParseException If the input cannot be parsed as a duration.
     * @throws UnsupportedOperationException If the duration cannot be
     *             represented using {@link Duration}
     */
    @SuppressWarnings("PMD.PreserveStackTrace")
    public static Duration fromISO8601String(String input) throws ParseException
    {
        if (input.length() < 3 || input.charAt(0) != 'P')
        {
            throw new ParseException("Input string is too short or does not start with 'P' [" + input + "]", 0);
        }
        Duration duration = null;
        // true when we get to the time section
        boolean foundT = false;
        for (int index = 1; index < input.length(); ++index)
        {
            int start = index;
            while (index < input.length() && (input.charAt(index) == '.' || Character.isDigit(input.charAt(index))))
            {
                ++index;
            }

            if (index == input.length())
            {
                throw new ParseException("Input string does not end with a duration specifier [" + input + "]", index);
            }
            else if (input.charAt(index) == 'T')
            {
                if (index != start || foundT)
                {
                    throw new ParseException(
                            "Missing duration specifier or extra 'T' found at index " + index + " for string [" + input + "]",
                            index);
                }
                foundT = true;
            }
            else
            {
                BigDecimal magnitude;
                try
                {
                    magnitude = new BigDecimal(input.substring(start, index));
                }
                catch (NumberFormatException e)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug(e, e);
                    }
                    throw new ParseException("Could not parse magnitude at index " + index + " for string [" + input + "]",
                            index);
                }

                Duration thisDur;
                switch (input.charAt(index))
                {
                    case 'Y':
                        thisDur = new Years(magnitude);
                        break;
                    case 'M':
                        thisDur = foundT ? new Minutes(magnitude) : new Months(magnitude);
                        break;
                    case 'W':
                        thisDur = new Weeks(magnitude);
                        break;
                    case 'D':
                        thisDur = new Days(magnitude);
                        break;
                    case 'H':
                        thisDur = new Hours(magnitude);
                        break;
                    case 'S':
                        thisDur = new Seconds(magnitude);
                        break;
                    default:
                        throw new ParseException("Unrecognized code at index " + index + " for string [" + input + "]", index);
                }
                try
                {
                    duration = duration == null ? thisDur : duration.add(thisDur);
                }
                catch (InconvertibleUnits e)
                {
                    throw new UnsupportedOperationException(
                            "Cannot represent a duration that adds " + thisDur + " to " + duration, e);
                }
            }
        }

        return duration;
    }

    /**
     * Get the long label for a duration type.
     *
     * @param type The type.
     * @param plural If the label should be the plural form.
     * @return The long label.
     * @throws InvalidUnitsException If the type is invalid.
     */
    public static String getLongLabel(Class<? extends Duration> type, boolean plural)
    {
        return create(type, 0L).getLongLabel(plural);
    }

    /**
     * Get a selection label for a type. This is a label that could be used to
     * select desired units (e.g., from a menu.)
     *
     * @param type The type.
     * @return The label.
     * @throws InvalidUnitsException If the type is invalid.
     */
    public static String getSelectionLabel(Class<? extends Duration> type)
    {
        return create(type, 0L).getSelectionLabel();
    }

    /**
     * Get the short label for a duration type.
     *
     * @param type The type.
     * @param plural If the label should be the plural form.
     * @return The short label.
     * @throws InvalidUnitsException If the length type is invalid.
     */
    public static String getShortLabel(Class<? extends Duration> type, boolean plural)
    {
        return create(type, 0L).getShortLabel(plural);
    }

    /**
     * Formats a duration in milliseconds into a pretty string.
     *
     * @param durationMs the duration in milliseconds
     * @return a pretty string
     */
    public static String millisToPrettyString(long durationMs)
    {
        // Get the individual values
        long[] values = new long[FORMAT_SEGMENTS.length];
        long workingDuration = durationMs;
        for (int i = 0; i < FORMAT_SEGMENTS.length; i++)
        {
            values[i] = workingDuration / FORMAT_SEGMENTS[i];
            workingDuration -= values[i] * FORMAT_SEGMENTS[i];
        }

        // Build the string
        StringBuilder text = new StringBuilder(32);
        boolean prevValues = false;
        boolean nextValues = true;
        for (int i = 0; i < values.length; i++)
        {
            if (prevValues && nextValues || values[i] > 0 || i == 3 && workingDuration > 0)
            {
                if (text.length() > 0)
                {
                    text.append(' ');
                }
                if (prevValues)
                {
                    text.append(pad(values[i], 2));
                }
                else
                {
                    text.append(values[i]);
                }
                if (i == 3 && workingDuration > 0)
                {
                    text.append('.').append(pad(workingDuration, 3));
                }

                if (nextValues && workingDuration == 0)
                {
                    nextValues = false;
                    for (int j = i + 1; j < values.length && !nextValues; j++)
                    {
                        nextValues = values[j] > 0;
                    }
                }
                if (prevValues || nextValues)
                {
                    text.append(SEGMENT_SHORT_NAMES[i]);
                }
                else
                {
                    text.append(' ').append(SEGMENT_LONG_NAMES[i]);
                    if (values[i] != 1)
                    {
                        text.append('s');
                    }
                }
                prevValues = true;
            }
        }
        return text.toString();
    }

    /**
     * Determines if the collection of durations contains the given duration
     * using compareTo() == 0.
     *
     * @param durations the durations
     * @param duration the duration
     * @return whether the collection contains the duration
     */
    public static boolean containsDuration(Collection<? extends Duration> durations, Duration duration)
    {
        boolean doesContain = false;
        for (Duration aDuration : durations)
        {
            try
            {
                if (aDuration.compareTo(duration) == 0)
                {
                    doesContain = true;
                    break;
                }
            }
            catch (InconvertibleUnits e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(e, e);
                }
            }
        }
        return doesContain;
    }

    /**
     * Check that two types of duration are the same.
     *
     * @param type1 The first type.
     * @param type2 The second type.
     * @throws InconvertibleUnits If the types are not the same.
     */
    protected static void checkExpectedUnits(Class<? extends Duration> type1, Class<? extends Duration> type2)
    {
        if (!type1.equals(type2))
        {
            throw new InconvertibleUnits(type1, type2);
        }
    }

    /**
     * Get if a magnitude can be represented as an {@code int}.
     *
     * @param mag The magnitude.
     * @return {@code true} if the magnitude can be represented as an
     *         {@code int}.
     */
    private static boolean isInIntRange(BigDecimal mag)
    {
        return mag.compareTo(MAX_INT) <= 0 && mag.compareTo(MIN_INT) >= 0;
    }

    /**
     * Utility to pad a value with leading 0s until it reaches the given width.
     *
     * @param value the value
     * @param width the desired width in characters
     * @return the padded string
     */
    private static String pad(long value, int width)
    {
        StringBuilder sb = new StringBuilder(width);
        String stringValue = String.valueOf(value);
        for (int i = 0, n = width - stringValue.length(); i < n; i++)
        {
            sb.append('0');
        }
        sb.append(stringValue);
        return sb.toString();
    }

    /**
     * Constructor that takes a {@link BigDecimal}.
     *
     * @param magnitude The magnitude of the duration. Precision beyond the
     *            width of a <tt>long</tt> will be lost.
     */
    public Duration(BigDecimal magnitude)
    {
        Utilities.checkNull(magnitude, "magnitude");
        BigDecimal usable;
        if (magnitude.unscaledValue().compareTo(MAX_LONG_INT) > 0)
        {
            usable = getScaled(magnitude, MAX_LONG_INT);
        }
        else if (magnitude.unscaledValue().compareTo(MIN_LONG_INT) < 0)
        {
            usable = getScaled(magnitude, MIN_LONG_INT);
        }
        else
        {
            usable = magnitude;
        }
        myUnscaledMagnitude = usable.unscaledValue().longValue();
        myScale = usable.scale();
    }

    /**
     * Constructor.
     *
     * @param unscaledMagnitude The unscaled magnitude of the duration.
     * @param scale The scale of the duration.
     *
     * @see BigDecimal
     */
    protected Duration(long unscaledMagnitude, int scale)
    {
        myUnscaledMagnitude = unscaledMagnitude;
        myScale = scale;
    }

    /**
     * Add this duration to another duration.
     *
     * @param other The other duration.
     * @return The result.
     * @throws InconvertibleUnits If the two durations do not share the same
     *             reference units.
     */
    public Duration add(Duration other)
    {
        checkExpectedUnits(getReferenceUnits(), other.getReferenceUnits());
        return Duration.create(getReferenceUnits(), inReferenceUnits().add(other.inReferenceUnits()));
    }

    /**
     * Add this duration to a calendar.
     *
     * @param cal The calendar.
     * @throws ArithmeticException If this duration's magnitude cannot be
     *             represented as an integer.
     */
    public abstract void addTo(Calendar cal);

    /**
     * Get the {@link Date} that represents the time that is this duration of
     * time from the Java epoch. If this duration is larger than what can be
     * represented by {@link Date}, the largest possible {@link Date} will be
     * returned.
     *
     * @return The Date.
     */
    public Date asDate()
    {
        Milliseconds millis = Milliseconds.get(this);
        if (millis.compareTo(new Milliseconds(Long.MAX_VALUE)) >= 0)
        {
            return new Date(Long.MAX_VALUE);
        }
        return new Date(millis.longValue());
    }

    @Override
    public Duration clone()
    {
        try
        {
            return (Duration)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }

    @Override
    public int compareTo(Duration o)
    {
        return inReferenceUnits(o.getReferenceUnits()).compareTo(o.inReferenceUnits());
    }

    /**
     * Divide this duration by a number.
     *
     * @param d The divisor.
     * @return The result.
     */
    public Duration divide(BigDecimal d)
    {
        return Duration.create(getClass(), getMagnitude().divide(d, DIVISION_SCALE, RoundingMode.HALF_EVEN).stripTrailingZeros());
    }

    /**
     * Divide this duration by a number.
     *
     * @param d The divisor.
     * @return The result.
     */
    public Duration divide(double d)
    {
        return divide(BigDecimal.valueOf(d));
    }

    /**
     * Divide this duration by another duration.
     *
     * @param d The other duration.
     * @return The result.
     * @throws InconvertibleUnits If the durations are inconvertible.
     */
    public BigDecimal divide(Duration d)
    {
        if (isZero())
        {
            return BigDecimal.ZERO;
        }
        return getMagnitude().divide(Duration.create(getClass(), d).getMagnitude(), DIVISION_SCALE, RoundingMode.HALF_EVEN)
                .stripTrailingZeros();
    }

    /**
     * Divide this duration by a number.
     *
     * @param d The divisor.
     * @return The result.
     */
    public Duration divide(long d)
    {
        return divide(BigDecimal.valueOf(d));
    }

    /**
     * Get the magnitude of this duration as a double.
     * <p>
     * Note that it is hazardous to compare durations using this method, since
     * the durations may not be in the same units. It is much safer to use
     * {@link #compareTo(Duration)}.
     *
     * @return The magnitude.
     */
    public double doubleValue()
    {
        return getMagnitude().doubleValue();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!getClass().equals(obj.getClass()))
        {
            return false;
        }
        Duration other = (Duration)obj;
        return getMagnitude().compareTo(other.getMagnitude()) == 0;
    }

    /**
     * Get if this duration equals another duration, without regard to what
     * units they are in. For example, using this method, 24 hours is equal to 1
     * day.
     *
     * @param other The other duration.
     * @return {@code true} if the durations are equal.
     */
    public boolean equalsIgnoreUnits(Duration other)
    {
        return this == other || other != null && isConvertibleTo(other) && compareTo(other) == 0;
    }

    /**
     * Gets the ChronoUnit from this duration (ignoring the magnitude).
     *
     * @return the ChronoUnit
     */
    public abstract ChronoUnit getChronoUnit();

    /**
     * Get a long version of the label for this unit of duration.
     *
     * @param plural If the label should be the plural form.
     * @return The long label.
     */
    public abstract String getLongLabel(boolean plural);

    /**
     * Get the magnitude of this duration as a BigDecimal.
     *
     * @return The magnitude.
     */
    public BigDecimal getMagnitude()
    {
        return BigDecimal.valueOf(myUnscaledMagnitude, myScale);
    }

    /**
     * Get the reference units for these units. Duration units that are all
     * convertible with each other should have the same reference units.
     *
     * @return The reference units.
     */
    public abstract Class<? extends Duration> getReferenceUnits();

    /**
     * Get a selection label for this duration. This is a label that could be
     * used to select desired units (e.g., from a menu.)
     *
     * @return The label.
     */
    public String getSelectionLabel()
    {
        return getLongLabel(true);
    }

    /**
     * Get a short version of the label for this unit of duration.
     *
     * @param plural If the label should be the plural form.
     * @return The long label.
     */
    public abstract String getShortLabel(boolean plural);

    @Override
    public JAXBDuration getWrapper()
    {
        return new JAXBDuration(this);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(inReferenceUnits().doubleValue());
        result = prime * result + (int)(temp ^ temp >>> 32);
        return result;
    }

    /**
     * Get the magnitude of this duration in its reference units.
     *
     * @return The magnitude.
     */
    public abstract BigDecimal inReferenceUnits();

    /**
     * Get the magnitude of this duration as an int.
     * <p>
     * <strong>WARNING:</strong> Note that it is hazardous to compare durations
     * using this method, since the durations may not be in the same units. It
     * is much safer to use {@link #compareTo(Duration)}.
     * </p>
     * <p>
     * <strong>WARNING:</strong> This is equivalent to casting to an
     * <tt>int</tt>. As such, fractions will be discarded and numbers too large
     * for a <tt>int</tt> will be wrong. See {@link BigDecimal#intValue()} for
     * an explanation.
     * </p>
     *
     * @return The magnitude.
     * @throws ArithmeticException If the magnitude cannot be represented as an
     *             <tt>int</tt>.
     * @see BigDecimal#longValue()
     */
    public int intValue()
    {
        BigDecimal mag = getMagnitude();
        if (!isInIntRange(mag))
        {
            throw new ArithmeticException("Magnitude [" + mag + "] is out of integer range.");
        }
        return mag.intValue();
    }

    /**
     * Get if this duration is between two values, inclusive.
     *
     * @param min The minimum.
     * @param max The maximum.
     * @return {@code true} if between
     */
    public boolean isBetween(Duration min, Duration max)
    {
        return compareTo(min) >= 0 && compareTo(max) <= 0;
    }

    /**
     * Get if this duration can be converted to the given duration type.
     *
     * @param type The duration type.
     * @return If the duration is convertible, {@code true}.
     */
    public boolean isConvertibleTo(Class<? extends Duration> type)
    {
        return isZero() || getReferenceUnits() == create(type, 0L).getReferenceUnits();
    }

    /**
     * Get if this duration can be converted to the units of another duration.
     *
     * @param other The other duration.
     * @return {@code true} if the units are mutually convertible.
     */
    public boolean isConvertibleTo(Duration other)
    {
        return isZero() || getReferenceUnits() == other.getReferenceUnits();
    }

    /**
     * Get if this duration is greater than another duration.
     *
     * @param o The other duration.
     * @return {@code true} if this duration is greater.
     */
    public boolean isGreaterThan(Duration o)
    {
        return compareTo(o) > 0;
    }

    /**
     * Get if the magnitude of this duration can be represented as an
     * {@code int}.
     *
     * @return {@code true} if the magnitude can be represented as an
     *         {@code int}.
     */
    public boolean isInIntRange()
    {
        return isInIntRange(getMagnitude());
    }

    /**
     * Get if this duration is lesser than another duration.
     *
     * @param o The other duration.
     * @return {@code true} if this duration is lesser.
     */
    public boolean isLessThan(Duration o)
    {
        return compareTo(o) < 0;
    }

    /**
     * Get if this duration has a magnitude equal to one.
     *
     * @return {@code true} if this duration is one.
     */
    public boolean isOne()
    {
        return getMagnitude().compareTo(BigDecimal.ONE) == 0;
    }

    /**
     * Get if this duration has zero magnitude.
     *
     * @return {@code true} if this duration is zero.
     */
    public boolean isZero()
    {
        return signum() == 0;
    }

    /**
     * Get the magnitude of this duration as a long.
     * <p>
     * <strong>WARNING:</strong> Note that it is hazardous to compare durations
     * using this method, since the durations may not be in the same units. It
     * is much safer to use {@link #compareTo(Duration)}.
     * </p>
     * <p>
     * <strong>WARNING:</strong> This is equivalent to casting to a
     * <tt>long</tt>. As such, fractions will be discarded and numbers too large
     * for a <tt>long</tt> will be wrong. See {@link BigDecimal#longValue()} for
     * an explanation.
     * </p>
     *
     * @return The magnitude.
     * @throws ArithmeticException If the magnitude cannot be represented as an
     *             <tt>long</tt>.
     *
     * @see BigDecimal#longValue()
     */
    public long longValue()
    {
        BigDecimal mag = getMagnitude();
        if (mag.compareTo(MAX_LONG) > 0 || mag.compareTo(MIN_LONG) < 0)
        {
            throw new ArithmeticException("Magnitude [" + mag + "] is out of long range.");
        }
        return mag.longValue();
    }

    /**
     * Multiply this duration by a number.
     *
     * @param d The multiplicand.
     * @return The product.
     */
    public Duration multiply(BigDecimal d)
    {
        return Duration.create(getClass(), getMagnitude().multiply(d));
    }

    /**
     * Multiply this duration by a number.
     *
     * @param d The multiplicand.
     * @return The product.
     */
    public Duration multiply(double d)
    {
        return multiply(BigDecimal.valueOf(d));
    }

    /**
     * Multiply this duration by a number.
     *
     * @param d The multiplicand.
     * @return The product.
     */
    public Duration multiply(long d)
    {
        return multiply(BigDecimal.valueOf(d));
    }

    /**
     * Get a duration that is the negative of this duration, in the same units.
     *
     * @return The negated duration.
     */
    public Duration negate()
    {
        return isZero() ? this : Duration.create(getClass(), getMagnitude().negate());
    }

    /**
     * Return one if this duration is greater than zero, zero if this duration
     * is zero, or negative one if this duration is less than zero.
     *
     * @return The signum function of this duration.
     */
    public int signum()
    {
        return Long.signum(myUnscaledMagnitude);
    }

    /**
     * Subtract another duration from this duration.
     *
     * @param other The other duration.
     * @return The result.
     */
    public Duration subtract(Duration other)
    {
        return Duration.create(getReferenceUnits(), inReferenceUnits().subtract(other.inReferenceUnits()));
    }

    /**
     * Get an ISO8601 representation for this duration.
     *
     * @return The ISO8601 string.
     */
    public String toISO8601String()
    {
        return new StringBuilder().append(getISO8601Prefix()).append(formatForISO8601(getMagnitude()))
                .append(getISO8601Designator()).toString();
    }

    /**
     * Return a string representation of this duration using its long label.
     *
     * @return The string.
     */
    public String toLongLabelString()
    {
        return new StringBuilder().append(getMagnitude()).append(' ').append(getLongLabel(getMagnitude().intValue() != 1))
                .toString();
    }

    /**
     * Get a pretty representation for this duration.
     *
     * @return a pretty string
     */
    public abstract String toPrettyString();

    /**
     * Return a string representation of this duration using its short label.
     *
     * @return The string.
     */
    public String toShortLabelString()
    {
        return new StringBuilder().append(getMagnitude()).append(' ').append(getShortLabel(getMagnitude().intValue() != 1))
                .toString();
    }

    @Override
    public String toString()
    {
        return toShortLabelString();
    }

    /**
     * Generate a string representation of the number and remove unnecessary
     * 0's.
     *
     * @param number The number.
     * @return The formatted string.
     */
    protected String formatForISO8601(BigDecimal number)
    {
        String result = String.format("%.9f", number);
        int end = result.length();
        while (end > 0)
        {
            if (result.charAt(end - 1) > '0')
            {
                break;
            }
            else if (result.charAt(end - 1) == '.')
            {
                --end;
                break;
            }
            --end;
        }
        return result.substring(0, end);
    }

    /**
     * Get the ISO8601 designator for this duration.
     *
     * @return The designator.
     */
    protected abstract char getISO8601Designator();

    /**
     * Get the ISO8601 prefix for this duration.
     *
     * @return The prefix.
     */
    protected abstract String getISO8601Prefix();

    /**
     * Get the scale of the magnitude.
     *
     * @return The scale.
     *
     * @see BigDecimal
     */
    protected int getScale()
    {
        return myScale;
    }

    /**
     * Get the unscaled magnitude of this duration.
     *
     * @return The unscaled magnitude.
     */
    protected long getUnscaledMagnitude()
    {
        return myUnscaledMagnitude;
    }

    /**
     * Get the magnitude of this duration in its reference units.
     *
     * @param expected The expected reference units.
     *
     * @return The magnitude.
     * @throws InconvertibleUnits If my reference units do not match the
     *             expected reference units.
     */
    protected abstract BigDecimal inReferenceUnits(Class<? extends Duration> expected);

    /**
     * Scale a decimal such that its unscaled value is less than limit in
     * absolute value.
     *
     * @param value The decimal.
     * @param limit The limit.
     * @return The adjusted decimal.
     */
    private BigDecimal getScaled(BigDecimal value, BigInteger limit)
    {
        int scaleDelta = (int)Math.ceil(Math
                .log10(new BigDecimal(value.unscaledValue()).divide(new BigDecimal(limit), RoundingMode.CEILING).doubleValue()));
        if (scaleDelta == 0)
        {
            scaleDelta = 1;
        }
        return value.setScale(value.scale() - scaleDelta, RoundingMode.HALF_EVEN);
    }
}
