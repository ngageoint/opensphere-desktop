package io.opensphere.core.util.lang;

/** Helper class for handling bit arrays. */
public final class BitArrays
{
    /**
     * Checks to see if a flag is set in the bit array.
     *
     * @param mask The mask to check
     * @param array The bit array.
     * @return true if set, false if not
     */
    public static boolean isFlagSet(byte mask, byte array)
    {
        return (array & mask) != 0;
    }

    /**
     * Checks to see if a flag is set in the bit array.
     *
     * @param mask The mask to check
     * @param array The bit array.
     * @return true if set, false if not
     */
    public static boolean isFlagSet(int mask, int array)
    {
        return (array & mask) != 0;
    }

    /**
     * Checks to see if a flag is set in the bit array.
     *
     * @param mask The mask to check
     * @param array The bit array.
     * @return true if set, false if not
     */
    public static boolean isFlagSet(long mask, long array)
    {
        return (array & mask) != 0;
    }

    /**
     * Checks to see if a flag is set in the bit array.
     *
     * @param mask The mask to check
     * @param array The bit array.
     * @return true if set, false if not
     */
    @SuppressWarnings("PMD.AvoidUsingShortType")
    public static boolean isFlagSet(short mask, short array)
    {
        return (array & mask) != 0;
    }

    /**
     * Sets (or resets) a flag in the bit array.
     *
     * @param mask The mask to use
     * @param on True to set on, false to set off
     * @param array The input bit array.
     * @return The new bit array.
     */
    public static byte setFlag(byte mask, boolean on, byte array)
    {
        return on ? (byte)(array | mask) : (byte)(array & ~mask);
    }

    /**
     * Sets (or resets) a flag in the bit array.
     *
     * @param mask The mask to use
     * @param on True to set on, false to set off
     * @param array The input bit array.
     * @return The new bit array.
     */
    public static int setFlag(int mask, boolean on, int array)
    {
        return on ? array | mask : array & ~mask;
    }

    /**
     * Sets (or resets) a flag in the bit array.
     *
     * @param mask The mask to use
     * @param on True to set on, false to set off
     * @param array The input bit array.
     * @return The new bit array.
     */
    public static long setFlag(long mask, boolean on, long array)
    {
        return on ? array | mask : array & ~mask;
    }

    /**
     * Sets (or resets) a flag in the bit array.
     *
     * @param mask The mask to use
     * @param on True to set on, false to set off
     * @param array The input bit array.
     * @return The new bit array.
     */
    @SuppressWarnings("PMD.AvoidUsingShortType")
    public static short setFlag(short mask, boolean on, short array)
    {
        return on ? (short)(array | mask) : (short)(array & ~mask);
    }

    /** Disallow instantiation. */
    private BitArrays()
    {
    }
}
