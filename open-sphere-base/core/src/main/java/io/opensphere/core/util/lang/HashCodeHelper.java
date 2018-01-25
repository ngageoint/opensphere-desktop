package io.opensphere.core.util.lang;

/**
 * Helps to generate hash code values for common hash code implementations.
 */
public final class HashCodeHelper
{
    /**
     * Gets the hash code for a boolean value.
     *
     * NOTE: This is what eclipse does for booleans in an auto generated
     * hashCode function. We do it here to avoid the complexity of the ternary
     * function in our own hash code methods.
     *
     * @param val the boolean value for which to generate a hash code.
     * @return the hash code
     */
    public static int getHashCode(boolean val)
    {
        return val ? 1231 : 1237;
    }

    /**
     * Gets the hash code for a byte value.
     *
     * NOTE: This is what eclipse does for byte in an auto generated hashCode
     * function.
     *
     * @param val the byte value for which to generate a hash code.
     * @return the hash code
     */
    public static int getHashCode(byte val)
    {
        return val;
    }

    /**
     * Gets the hash code for a double value.
     *
     * NOTE: This is what eclipse does for double in an auto generated hashCode
     * function.
     *
     * @param val the double value for which to generate a hash code.
     * @return the hash code
     */
    public static int getHashCode(double val)
    {
        return getHashCode(Double.doubleToLongBits(val));
    }

    /**
     * Gets the hash code for a float value.
     *
     * NOTE: This is what eclipse does for float in an auto generated hashCode
     * function.
     *
     * @param val the float value for which to generate a hash code.
     * @return the hash code
     */
    public static int getHashCode(float val)
    {
        return Float.floatToIntBits(val);
    }

    /**
     * Gets the hash code for a int value.
     *
     * NOTE: This is what eclipse does for int in an auto generated hashCode
     * function.
     *
     * @param val the int value for which to generate a hash code.
     * @return the hash code
     */
    public static int getHashCode(int val)
    {
        return val;
    }

    /**
     * Gets the hash code for some objects, taking null into account.
     *
     * @param startHash The hash code to start with.
     * @param prime A prime number to multiply with each intermediate result.
     * @param objects The objects.
     *
     * @return the hash code or 0 if null;
     */
    public static int getHashCode(int startHash, int prime, Object... objects)
    {
        int result = startHash;
        for (Object obj : objects)
        {
            result = prime * result + getHashCode(obj);
        }
        return result;
    }

    /**
     * Gets the hash code for a long value.
     *
     * NOTE: This is what eclipse does for long in an auto generated hashCode
     * function.
     *
     * @param val the long value for which to generate a hash code.
     * @return the hash code
     */
    public static int getHashCode(long val)
    {
        return (int)(val ^ val >>> 32);
    }

    /**
     * Gets the hash code for an object, taking null into account.
     *
     * @param o the {@link Object} for which to generate a hash code.
     * @return the hash code or 0 if null;
     */
    public static int getHashCode(Object o)
    {
        return o == null ? 0 : o.hashCode();
    }

    /**
     * Gets the hash code for a short value.
     *
     * NOTE: This is what eclipse does for short in an auto generated hashCode
     * function.
     *
     * @param val the short value for which to generate a hash code.
     * @return the hash code
     */
    @SuppressWarnings("PMD.AvoidUsingShortType")
    public static int getHashCode(short val)
    {
        return val;
    }

    /**
     * Gets the hash code for some objects, assuming none of them are
     * {@code null}.
     *
     * @param startHash The hash code to start with.
     * @param prime A prime number to multiply with each intermediate result.
     * @param objects The objects.
     *
     * @return the hash code or 0 if null;
     */
    public static int getHashCodeNoNulls(int startHash, int prime, Object... objects)
    {
        int result = startHash;
        for (Object obj : objects)
        {
            result = prime * result + obj.hashCode();
        }
        return result;
    }

    /**
     * Instantiates a new hash code helper.
     */
    private HashCodeHelper()
    {
        // Don't allow instantiation.
    }
}
