package io.opensphere.core.units.length;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Collection;

import io.opensphere.core.units.InvalidUnitsException;
import io.opensphere.core.units.UnitsUtilities;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.JAXBable;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ExpectedCloneableException;

/**
 * A unit-aware representation of a length.
 */
public abstract class Length implements Cloneable, Serializable, Comparable<Length>, JAXBable<JAXBLength>
{
    /** Meters per foot. */
    public static final double METERS_PER_FOOT;

    /** Micrometers per foot (exact). */
    public static final int MICROMETERS_PER_FOOT = 304800;

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Type of the values. */
    static final Class<Double> VALUE_TYPE = Double.TYPE;

    /** The magnitude. Units are defined by subclasses. */
    private final double myMagnitude;

    static
    {
        METERS_PER_FOOT = (double)MICROMETERS_PER_FOOT / Constants.MICRO_PER_UNIT;
    }

    /**
     * Create a new length object.
     *
     * @param <T> The type of the length object.
     * @param type The type of the length object.
     * @param magnitude The magnitude of the new object, in its native units.
     * @return The new length object.
     * @throws InvalidUnitsException If the type is invalid.
     */
    public static <T extends Length> T create(Class<T> type, double magnitude) throws InvalidUnitsException
    {
        return UnitsUtilities.create(type, VALUE_TYPE, Double.valueOf(magnitude));
    }

    /**
     * Create a new length object.
     *
     * @param <T> The type of the length object.
     * @param type The type of the length object.
     * @param magnitude The magnitude of the new object, in its native units.
     * @return The new length object.
     * @throws InvalidUnitsException If the type is invalid.
     */
    public static <T extends Length> T create(Class<T> type, Double magnitude) throws InvalidUnitsException
    {
        return UnitsUtilities.create(type, VALUE_TYPE, magnitude);
    }

    /**
     * Return a length in the specified units equivalent to another length. If
     * the input length is already in the requested units, it will be returned
     * as-is.
     *
     * @param <T> The type of the length object.
     * @param type The type of the length object.
     * @param from The source length object.
     * @return The new length object.
     * @throws InvalidUnitsException If the type is invalid.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Length> T create(Class<T> type, Length from) throws InvalidUnitsException
    {
        if (type.isInstance(from))
        {
            return (T)from;
        }
        else
        {
            return UnitsUtilities.create(type, Length.class, from);
        }
    }

    /**
     * Creates a Length from the persistence string.
     *
     * @param className the class name
     * @param magnitude the magnitude
     * @return the Length
     * @throws InvalidUnitsException If the type is invalid.
     * @throws ParseException If the string is invalid.
     */
    @SuppressWarnings("unchecked")
    public static Length parse(String className, String magnitude) throws InvalidUnitsException, ParseException
    {
        try
        {
            return create((Class<? extends Length>)Class.forName(className), Double.parseDouble(magnitude));
        }
        catch (NumberFormatException | ClassNotFoundException e)
        {
            throw new ParseException(className + " " + magnitude, 0);
        }
    }

    /**
     * Convert feet to meters.
     *
     * @param feet The feet.
     * @return The meters.
     */
    public static double feetToMeters(double feet)
    {
        return feet * METERS_PER_FOOT;
    }

    /**
     * Get the fixed-scale units appropriate for a particular value. If the
     * input {@code type} is not an auto-scale type, it is simply returned
     * regardless of the input {@code value}.
     *
     * @param type The input type.
     * @param value The value.
     * @return The preferred units.
     */
    public static Class<? extends Length> getFixedScaleUnits(Class<? extends Length> type, Length value)
    {
        return Length.create(type, value).getDisplayClass();
    }

    /**
     * Get the long label for a length type.
     *
     * @param type The length type.
     * @param plural If the label should be the plural form.
     * @return The long label, or {@code null} if the type cannot be
     *         instantiated.
     * @throws InvalidUnitsException If the length type is invalid.
     */
    public static String getLongLabel(Class<? extends Length> type, boolean plural) throws InvalidUnitsException
    {
        return create(type, 0.).getLongLabel(plural);
    }

    /**
     * Get a selection label for a type. This is a label that could be used to
     * select desired units (e.g., from a menu.)
     *
     * @param type The type.
     * @return The label.
     * @throws InvalidUnitsException If the type is invalid.
     */
    public static String getSelectionLabel(Class<? extends Length> type) throws InvalidUnitsException
    {
        return create(type, 0.).getSelectionLabel();
    }

    /**
     * Get the selection labels for a collection of types.
     *
     * @param types The types.
     * @return The selection labels.
     * @throws InvalidUnitsException If any of the types are invalid.
     */
    public static Collection<String> getSelectionLabels(Collection<Class<? extends Length>> types) throws InvalidUnitsException
    {
        Collection<String> results = New.collection(types.size());
        for (Class<? extends Length> type : types)
        {
            results.add(getSelectionLabel(type));
        }
        return results;
    }

    /**
     * Get the short label for a length type.
     *
     * @param type The length type.
     * @param plural If the label should be the plural form.
     * @return The short label, or {@code null} if the type cannot be
     *         instantiated.
     * @throws InvalidUnitsException If the length type is invalid.
     */
    public static String getShortLabel(Class<? extends Length> type, boolean plural) throws InvalidUnitsException
    {
        return create(type, 0.).getShortLabel(plural);
    }

    /**
     * Convert meters to feet.
     *
     * @param meters The meters.
     * @return The feet.
     */
    public static double metersToFeet(double meters)
    {
        return meters / METERS_PER_FOOT;
    }

    /**
     * Constructor.
     *
     * @param magnitude The magnitude of the length.
     */
    protected Length(double magnitude)
    {
        myMagnitude = magnitude;
    }

    @Override
    public Length clone()
    {
        try
        {
            return (Length)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }

    @Override
    public int compareTo(Length o)
    {
        double delta = inMeters() - o.inMeters();
        return delta < -MathUtil.DBL_EPSILON ? -1 : delta > MathUtil.DBL_EPSILON ? 1 : 0;
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
        Length other = (Length)obj;
        return Math.abs(inMeters() - other.inMeters()) <= MathUtil.DBL_EPSILON;
    }

    /**
     * Get the class that will be used to display this length. This may be
     * different from this class if this is an auto-scaling length.
     *
     * @return The display class.
     */
    public Class<? extends Length> getDisplayClass()
    {
        return getClass();
    }

    /**
     * Get the displayed magnitude for this length.
     *
     * @return The magnitude.
     */
    public double getDisplayMagnitude()
    {
        return getMagnitude();
    }

    /**
     * Get the displayed magnitude for this length.
     *
     * @return The magnitude.
     */
    public Double getDisplayMagnitudeObj()
    {
        return getMagnitudeObj();
    }

    /**
     * Get the magnitude of this length as a String.
     *
     * @return The magnitude.
     */
    public String getDisplayMagnitudeString()
    {
        return Double.toString(getDisplayMagnitude());
    }

    /**
     * Get a long version of the label for this unit of length.
     *
     * @param plural If the label should be the plural form.
     * @return The long label.
     */
    public abstract String getLongLabel(boolean plural);

    /**
     * Get the magnitude of this length.
     *
     * @return The magnitude.
     */
    public double getMagnitude()
    {
        return myMagnitude;
    }

    /**
     * Get the magnitude of this length as an Object.
     *
     * @return The magnitude.
     */
    public Double getMagnitudeObj()
    {
        return Double.valueOf(myMagnitude);
    }

    /**
     * Get the magnitude of this length as a String.
     *
     * @return The magnitude.
     */
    public String getMagnitudeString()
    {
        return Double.toString(getMagnitude());
    }

    /**
     * Get a selection label for this length. This is a label that could be used
     * to select desired units (e.g., from a menu.)
     *
     * @return The label.
     */
    public String getSelectionLabel()
    {
        return getLongLabel(true);
    }

    /**
     * Get a short version of the label for this unit of length.
     *
     * @param plural If the label should be the plural form.
     * @return The long label.
     */
    public abstract String getShortLabel(boolean plural);

    @Override
    public JAXBLength getWrapper()
    {
        return new JAXBLength(this);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(inMeters());
        result = prime * result + (int)(temp ^ temp >>> 32);
        return result;
    }

    /**
     * Get this length in feet. Subclasses must override either this method or
     * {@link #inMeters()}.
     *
     * @return The length in meters.
     */
    public double inFeet()
    {
        return inMeters() / METERS_PER_FOOT;
    }

    /**
     * Get this length in meters. Subclasses must override either this method or
     * {@link #inFeet()}.
     *
     * @return The length in meters.
     */
    public double inMeters()
    {
        return inFeet() * METERS_PER_FOOT;
    }

    /**
     * Get if this length auto-scales its display.
     *
     * @return {@code true} if this length auto-scales.
     */
    public boolean isAutoscale()
    {
        return false;
    }

    /**
     * Multiply this length by a factor.
     *
     * @param factor The factor.
     * @return The new length.
     */
    public Length multiplyBy(double factor)
    {
        return create(getClass(), getMagnitude() * factor);
    }

    /**
     * Returns a negated copy of this length.
     *
     * @return the negated length
     */
    public Length negate()
    {
        return create(getClass(), -getMagnitude());
    }

    /**
     * Return a string representation of this length using its long label.
     *
     * @return The string.
     */
    public String toLongLabelString()
    {
        return new StringBuilder().append(getDisplayMagnitude()).append(' ').append(getLongLabel(getDisplayMagnitude() != 1.))
                .toString();
    }

    /**
     * Return a string representation of this length using its long label.
     *
     * @param width The width for the label.
     * @param precision The precision for the label, if the length were in
     *            meters. If the length is not in meters, this value will be
     *            adjusted accordingly. This will only affect the fractional
     *            portion of the number, and negative numbers are allowed.
     * @return The string.
     */
    public String toLongLabelString(int width, int precision)
    {
        return new StringBuilder().append(format(width, precision)).append(' ').append(getLongLabel(getDisplayMagnitude() != 1.))
                .toString();
    }

    /**
     * Return a string representation of this length using its short label.
     *
     * @return The string.
     */
    public String toShortLabelString()
    {
        return new StringBuilder().append(getDisplayMagnitude()).append(' ').append(getShortLabel(getDisplayMagnitude() != 1.))
                .toString();
    }

    /**
     * Return a string representation of this length using its short label.
     *
     * @param width The width for the label.
     * @param precision The precision for the label, if the length were in
     *            meters. If the length is not in meters, this value will be
     *            adjusted accordingly. This will only affect the fractional
     *            portion of the number, and negative numbers are allowed.
     * @return The string.
     */
    public String toShortLabelString(int width, int precision)
    {
        return new StringBuilder().append(format(width, precision)).append(' ').append(getShortLabel(getDisplayMagnitude() != 1.))
                .toString();
    }

    @Override
    public String toString()
    {
        return toShortLabelString();
    }

    /**
     * Format the magnitude as a string.
     *
     * @param width The preferred width of the string.
     * @param precision The precision.
     * @return The formatted string.
     */
    private String format(int width, int precision)
    {
        int adjPrec = Math.max(0, precision - (int)Math.log10(getDisplayMagnitude() / inMeters()));
        return String.format(new StringBuilder(16).append('%').append(width).append('.').append(adjPrec).append('f').toString(),
                Double.valueOf(getDisplayMagnitude()));
    }
}
