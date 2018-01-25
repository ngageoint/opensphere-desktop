package io.opensphere.core.util.lang;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/** Boolean utilities. */
public final class BooleanUtilities
{
    /**
     * Converts a byte to Boolean.
     *
     * @param b the byte
     * @return the Boolean
     */
    @SuppressFBWarnings("NP_BOOLEAN_RETURN_NULL")
    public static Boolean fromByte(byte b)
    {
        return b == 0 ? null : b == 1 ? Boolean.FALSE : Boolean.TRUE;
    }

    /**
     * Converts a Boolean to byte.
     *
     * @param bool the Boolean
     * @return the byte
     */
    public static byte toByte(Boolean bool)
    {
        return bool == null ? (byte)0 : Boolean.FALSE.equals(bool) ? (byte)1 : (byte)2;
    }

    /**
     * Converts an Boolean to boolean.
     *
     * @param value the Boolean
     * @return the boolean
     */
    public static boolean toBoolean(Boolean value)
    {
        return toBoolean(value, false);
    }

    /**
     * Converts an Boolean to boolean.
     *
     * @param value the Boolean
     * @param defaultValue the default value
     * @return the boolean
     */
    public static boolean toBoolean(Boolean value, boolean defaultValue)
    {
        return value != null ? value.booleanValue() : defaultValue;
    }

    /** Private constructor. */
    private BooleanUtilities()
    {
    }
}
