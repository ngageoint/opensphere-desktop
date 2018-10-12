package io.opensphere.core.units.angle;

import java.io.Serializable;

import io.opensphere.core.units.InvalidUnitsException;
import io.opensphere.core.units.UnitsUtilities;
import io.opensphere.core.util.JAXBable;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.lang.ExpectedCloneableException;

/**
 * A formatting-aware representation of an angle.
 */
public abstract class Angle implements Cloneable, Serializable, Comparable<Angle>, JAXBable<JAXBAngle>
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Type of the values. */
    static final Class<Double> VALUE_TYPE = Double.TYPE;

    /** The magnitude. Representation is defined by subclasses. */
    private final double myMagnitude;

    /**
     * Return an angle in the specified units equivalent to another angle. If
     * the input angle is already in the requested units, it will be returned
     * as-is.
     *
     * @param <T> The type of the angle object.
     * @param type The type of the angle object.
     * @param from The source angle object.
     * @return The new angle object.
     * @throws InvalidUnitsException If the type is invalid.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Angle> T create(Class<T> type, Angle from) throws InvalidUnitsException
    {
        if (type.isInstance(from))
        {
            return (T)from;
        }
        return UnitsUtilities.create(type, Angle.class, from);
    }

    /**
     * Create a new angle object.
     *
     * @param <T> The type of the angle object.
     * @param type The type of the angle object.
     * @param magnitude The magnitude of the new object.
     * @return The new angle object.
     * @throws InvalidUnitsException If the type is invalid.
     */
    public static <T extends Angle> T create(Class<T> type, double magnitude) throws InvalidUnitsException
    {
        return UnitsUtilities.create(type, VALUE_TYPE, Double.valueOf(magnitude));
    }

    /**
     * Get the long label for an angle type.
     *
     * @param type The angle type.
     * @return The long label, or {@code null} if the type cannot be
     *         instantiated.
     * @throws InvalidUnitsException If the angle type is invalid.
     */
    public static String getLongLabel(Class<? extends Angle> type) throws InvalidUnitsException
    {
        return create(type, 0.).getLongLabel();
    }

    /**
     * Get a selection label for a type. This is a label that could be used to
     * select desired units (e.g., from a menu.)
     *
     * @param type The type.
     * @return The label.
     * @throws InvalidUnitsException If the type is invalid.
     */
    public static String getSelectionLabel(Class<? extends Angle> type) throws InvalidUnitsException
    {
        return create(type, 0.).getSelectionLabel();
    }

    /**
     * Get the short label for an angle type.
     *
     * @param type The angle type.
     * @return The short label, or {@code null} if the type cannot be
     *         instantiated.
     * @throws InvalidUnitsException If the angle type is invalid.
     */
    public static String getShortLabel(Class<? extends Angle> type) throws InvalidUnitsException
    {
        return create(type, 0.).getShortLabel();
    }

    /**
     * Constructor.
     *
     * @param magnitude The magnitude of the angle in degrees.
     */
    protected Angle(double magnitude)
    {
        myMagnitude = magnitude % 360.;
    }

    @Override
    public Angle clone()
    {
        try
        {
            return (Angle)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }

    @Override
    public int compareTo(Angle o)
    {
        double delta = getMagnitude() - o.getMagnitude();
        return delta < -MathUtil.DBL_EPSILON ? -1 : delta > MathUtil.DBL_EPSILON ? 1 : 0;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass()))
        {
            return false;
        }
        Angle other = (Angle)obj;
        return Math.abs(getMagnitude() - other.getMagnitude()) <= MathUtil.DBL_EPSILON;
    }

    /**
     * Get a long version of the label for this angle.
     *
     * @return The long label.
     */
    public abstract String getLongLabel();

    /**
     * Get the magnitude of this angle.
     *
     * @return The magnitude.
     */
    public double getMagnitude()
    {
        return myMagnitude;
    }

    /**
     * Get the magnitude of this angle as an Object.
     *
     * @return The magnitude.
     */
    public Double getMagnitudeObj()
    {
        return Double.valueOf(myMagnitude);
    }

    /**
     * Get a selection label for this angle. This is a label that could be used
     * to select desired units (e.g., from a menu.)
     *
     * @return The label.
     */
    public String getSelectionLabel()
    {
        return getLongLabel();
    }

    /**
     * Get a short version of the label for this angle.
     *
     * @return The long label.
     */
    public abstract String getShortLabel();

    @Override
    public JAXBAngle getWrapper()
    {
        return new JAXBAngle(this);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(getMagnitude());
        result = prime * result + (int)(temp ^ temp >>> 32);
        return result;
    }

    /**
     * Return a string representation of this angle using its short label.
     *
     * @return The string.
     */
    public abstract String toShortLabelString();

    /**
     * Return a string representation of this angle using its short label.
     *
     * @param positive Character to use if the angle is positive.
     * @param negative Character to use if the angle is negative.
     *
     * @return The string.
     */
    public abstract String toShortLabelString(char positive, char negative);

    /**
     * Return a string representation of this angle using its short label.
     *
     * @param width The width of the output string.
     * @param precision The amount of precision of the output string.
     * @return The string.
     */
    public abstract String toShortLabelString(int width, int precision);

    /**
     * Return a string representation of this angle using its short label.
     *
     * @param width The width of the output string.
     * @param precision The amount of precision of the output string.
     * @param positive Character to use if the angle is positive.
     * @param negative Character to use if the angle is negative.
     *
     * @return The string.
     */
    public abstract String toShortLabelString(int width, int precision, char positive, char negative);

    @Override
    public String toString()
    {
        return toShortLabelString();
    }
}
