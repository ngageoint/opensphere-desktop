package io.opensphere.mantle.util.bitmanip;

import java.math.BigInteger;

/**
 * The Class BitMaskGenerator.
 */
public final class BitMaskGenerator
{
    /** The Constant END_BIT_MUST_GREATER_THEN_START_ON_BIT. */
    private static final String END_BIT_MUST_GREATER_THEN_START_ON_BIT = "End bit must greater then startOnBit";

    /**
     * Creates a bit mask setting a range of bits to on for an byte.
     *
     *
     * @param startOnBit the start on bit ( 0 to 7 )
     * @param endOnBit the end on bit (0 to 7)
     * @return the bit mask
     * @throws IllegalArgumentException if start/end bit out of range or end
     *             &lt; start
     */
    public static byte createByteRangedBitMask(int startOnBit, int endOnBit)
    {
        if (startOnBit < 0 || startOnBit > 7)
        {
            throw new IllegalArgumentException("Start bit must be in range 0 <= startOnBit <= 75");
        }
        if (endOnBit < 0 || endOnBit > 7)
        {
            throw new IllegalArgumentException("End bit must be in range 0 <= endOnBit <= 7");
        }
        if (endOnBit < startOnBit)
        {
            throw new IllegalArgumentException(END_BIT_MUST_GREATER_THEN_START_ON_BIT);
        }

        BigInteger bi = BigInteger.valueOf(0L);
        for (int i = startOnBit; i <= endOnBit; i++)
        {
            bi = bi.setBit(i);
        }
        return bi.byteValue();
    }

    /**
     * Creates a bit mask setting a range of bits to on for an integer.
     *
     *
     * @param startOnBit the start on bit ( 0 to 31 )
     * @param endOnBit the end on bit (0 to 31)
     * @return the bit mask
     * @throws IllegalArgumentException if start/end bit out of range or end
     *             &gt; start
     */
    public static int createIntRangedBitMask(int startOnBit, int endOnBit)
    {
        if (startOnBit < 0 || startOnBit > 31)
        {
            throw new IllegalArgumentException("Start bit must be in range 0 <= startOnBit <= 31");
        }
        if (endOnBit < 0 || endOnBit > 31)
        {
            throw new IllegalArgumentException("End bit must be in range 0 <= endOnBit <= 31");
        }
        if (endOnBit < startOnBit)
        {
            throw new IllegalArgumentException(END_BIT_MUST_GREATER_THEN_START_ON_BIT);
        }

        BigInteger bi = BigInteger.valueOf(0L);
        for (int i = startOnBit; i <= endOnBit; i++)
        {
            bi = bi.setBit(i);
        }
        return bi.intValue();
    }

    /**
     * Creates the long ranged bit mask setting a range of bits for on for a
     * long.
     *
     * @param startOnBit the start on bit ( 0 to 63 )
     * @param endOnBit the end on bit ( 0 to 63 )
     * @return the bit mask
     * @throws IllegalArgumentException if start/end bit out of range or end
     *             &gt; start
     */
    public static long createLongRangedBitMask(int startOnBit, int endOnBit)
    {
        if (startOnBit < 0 || startOnBit > 63)
        {
            throw new IllegalArgumentException("Start bit must be in range 0 <= startOnBit <= 63");
        }
        if (endOnBit < 0 || endOnBit > 63)
        {
            throw new IllegalArgumentException("End bit must be in range 0 <= endOnBit <= 63");
        }
        if (endOnBit < startOnBit)
        {
            throw new IllegalArgumentException(END_BIT_MUST_GREATER_THEN_START_ON_BIT);
        }

        BigInteger bi = BigInteger.valueOf(0L);
        for (int i = startOnBit; i <= endOnBit; i++)
        {
            bi = bi.setBit(i);
        }
        return bi.longValue();
    }

    /**
     * Creates a bit mask setting a range of bits to on for an short.
     *
     *
     * @param startOnBit the start on bit ( 0 to 15 )
     * @param endOnBit the end on bit (0 to 15)
     * @return the bit mask
     * @throws IllegalArgumentException if start/end bit out of range or end
     *             &gt; start
     */
    @SuppressWarnings("PMD.AvoidUsingShortType")
    public static short createShortRangedBitMask(int startOnBit, int endOnBit)
    {
        if (startOnBit < 0 || startOnBit > 15)
        {
            throw new IllegalArgumentException("Start bit must be in range 0 <= startOnBit <= 15");
        }
        if (endOnBit < 0 || endOnBit > 15)
        {
            throw new IllegalArgumentException("End bit must be in range 0 <= endOnBit <= 15");
        }
        if (endOnBit < startOnBit)
        {
            throw new IllegalArgumentException(END_BIT_MUST_GREATER_THEN_START_ON_BIT);
        }

        BigInteger bi = BigInteger.valueOf(0L);
        for (int i = startOnBit; i <= endOnBit; i++)
        {
            bi = bi.setBit(i);
        }
        return bi.shortValue();
    }

    /**
     * Instantiates a new bit mask generator.
     */
    private BitMaskGenerator()
    {
        // Don't allow instantiation.
    }
}
