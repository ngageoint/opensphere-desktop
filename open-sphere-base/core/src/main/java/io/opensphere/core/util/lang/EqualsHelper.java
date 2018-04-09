package io.opensphere.core.util.lang;

import java.util.Objects;

/** Helper class for equals methods. */
public final class EqualsHelper
{
    /**
     * Determine if each pair of booleans is equal.
     *
     * @param values The pairs to compare.
     * @return <code>true</code> if all the pairs are equal.
     * @throws ArrayIndexOutOfBoundsException if an  odd number of objects is
     *             passed in.
     * @see #equals(Object, Object)
     */
    @SuppressWarnings("PMD.SuspiciousEqualsMethodName")
    public static boolean booleanEquals(boolean... values)
    {
        for (int i = 0; i < values.length;)
        {
            if (values[i++] != values[i++])
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Determine if two doubles are equal.
     *
     * @param val1 The first value
     * @param val2 The second value
     * @return <code>true</code> if the values are equal.
     */
    public static boolean doubleEquals(double val1, double val2)
    {
        return Double.doubleToLongBits(val1) == Double.doubleToLongBits(val2);
    }

    /**
     * Determine if each pair of objects is equal.
     *
     * @param objs The pairs to compare.
     * @return <code>true</code> if all the pairs are equal.
     * @throws ArrayIndexOutOfBoundsException if an  odd number of objects is
     *             passed in.
     * @see #equals(Object, Object)
     */
    @SuppressWarnings("PMD.SuspiciousEqualsMethodName")
    public static boolean equals(Object... objs)
    {
        for (int i = 0; i < objs.length;)
        {
            if (!Objects.equals(objs[i++], objs[i++]))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Determine if two objects are equal, handling <code>null</code>.
     *
     * @param obj1 The first object.
     * @param obj2 The second object.
     * @return <code>true</code> if both references point to the same object or
     *         both references are null or the objects are equal according to
     *         the first object's {@link #equals(Object)} method.
     */
    @SuppressWarnings("PMD.SuspiciousEqualsMethodName")
    public static boolean equals(Object obj1, Object obj2)
    {
        return Objects.equals(obj1, obj2);
    }

    /**
     * Determine if each pair of floats is equal.
     *
     * @param values The pairs to compare.
     * @return <code>true</code> if all the pairs are equal.
     * @throws ArrayIndexOutOfBoundsException if an  odd number of objects is
     *             passed in.
     * @see #equals(Object, Object)
     */
    @SuppressWarnings("PMD.SuspiciousEqualsMethodName")
    public static boolean floatEquals(float... values)
    {
        for (int i = 0; i < values.length;)
        {
            if (Float.floatToIntBits(values[i++]) != Float.floatToIntBits(values[i++]))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Determine if each pair of ints is equal.
     *
     * @param values The pairs to compare.
     * @return <code>true</code> if all the pairs are equal.
     * @throws ArrayIndexOutOfBoundsException if an  odd number of objects is
     *             passed in.
     * @see #equals(Object, Object)
     */
    @SuppressWarnings("PMD.SuspiciousEqualsMethodName")
    public static boolean intEquals(int... values)
    {
        for (int i = 0; i < values.length;)
        {
            if (values[i++] != values[i++])
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines of the given object matches any of the following objects.
     *
     * @param o the object
     * @param o1 an object for comparison
     * @param o2 an object for comparison
     * @return whether any are equal
     */
    public static boolean equalsAny(Object o, Object o1, Object o2)
    {
        return o1.equals(o) || o2.equals(o);
    }

    /** Disallow instantiation. */
    private EqualsHelper()
    {
    }
}
