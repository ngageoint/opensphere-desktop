package io.opensphere.core.util;

import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link MathUtil}.
 */
public class MathUtilTest
{
    /** Test for {@link MathUtil#addSafe(int, int)}. */
    @Test
    public void testAddSafeIntInt()
    {
        Assert.assertEquals(Integer.MIN_VALUE, MathUtil.addSafe(Integer.MIN_VALUE, Integer.MIN_VALUE));
        Assert.assertEquals(Integer.MIN_VALUE, MathUtil.addSafe(Integer.MIN_VALUE, -1));
        Assert.assertEquals(Integer.MIN_VALUE, MathUtil.addSafe(-1, Integer.MIN_VALUE));
        Assert.assertEquals(Integer.MIN_VALUE, MathUtil.addSafe(Integer.MIN_VALUE, 0));
        Assert.assertEquals(Integer.MIN_VALUE, MathUtil.addSafe(0, Integer.MIN_VALUE));
        Assert.assertEquals(Integer.MIN_VALUE + 1, MathUtil.addSafe(Integer.MIN_VALUE, 1));
        Assert.assertEquals(Integer.MIN_VALUE + 1, MathUtil.addSafe(1, Integer.MIN_VALUE));

        Assert.assertEquals(Integer.MAX_VALUE, MathUtil.addSafe(Integer.MAX_VALUE, Integer.MAX_VALUE));
        Assert.assertEquals(Integer.MAX_VALUE, MathUtil.addSafe(Integer.MAX_VALUE, 1));
        Assert.assertEquals(Integer.MAX_VALUE, MathUtil.addSafe(1, Integer.MAX_VALUE));
        Assert.assertEquals(Integer.MAX_VALUE, MathUtil.addSafe(Integer.MAX_VALUE, 0));
        Assert.assertEquals(Integer.MAX_VALUE, MathUtil.addSafe(0, Integer.MAX_VALUE));
        Assert.assertEquals(Integer.MAX_VALUE - 1, MathUtil.addSafe(Integer.MAX_VALUE, -1));
        Assert.assertEquals(Integer.MAX_VALUE - 1, MathUtil.addSafe(-1, Integer.MAX_VALUE));

        Assert.assertEquals(-1, MathUtil.addSafe(Integer.MIN_VALUE, Integer.MAX_VALUE));
        Assert.assertEquals(-1, MathUtil.addSafe(Integer.MAX_VALUE, Integer.MIN_VALUE));

        Assert.assertEquals(Integer.MIN_VALUE, MathUtil.addSafe(Integer.MIN_VALUE, -Integer.MAX_VALUE));
    }

    /** Test for {@link MathUtil#addSafe(long, long)}. */
    @Test
    public void testAddSafeLongLong()
    {
        Assert.assertEquals(Long.MIN_VALUE, MathUtil.addSafe(Long.MIN_VALUE, Long.MIN_VALUE));
        Assert.assertEquals(Long.MIN_VALUE, MathUtil.addSafe(Long.MIN_VALUE, -1));
        Assert.assertEquals(Long.MIN_VALUE, MathUtil.addSafe(-1, Long.MIN_VALUE));
        Assert.assertEquals(Long.MIN_VALUE, MathUtil.addSafe(Long.MIN_VALUE, 0));
        Assert.assertEquals(Long.MIN_VALUE, MathUtil.addSafe(0, Long.MIN_VALUE));
        Assert.assertEquals(Long.MIN_VALUE + 1, MathUtil.addSafe(Long.MIN_VALUE, 1));
        Assert.assertEquals(Long.MIN_VALUE + 1, MathUtil.addSafe(1, Long.MIN_VALUE));

        Assert.assertEquals(Long.MAX_VALUE, MathUtil.addSafe(Long.MAX_VALUE, Long.MAX_VALUE));
        Assert.assertEquals(Long.MAX_VALUE, MathUtil.addSafe(Long.MAX_VALUE, 1));
        Assert.assertEquals(Long.MAX_VALUE, MathUtil.addSafe(1, Long.MAX_VALUE));
        Assert.assertEquals(Long.MAX_VALUE, MathUtil.addSafe(Long.MAX_VALUE, 0));
        Assert.assertEquals(Long.MAX_VALUE, MathUtil.addSafe(0, Long.MAX_VALUE));
        Assert.assertEquals(Long.MAX_VALUE - 1, MathUtil.addSafe(Long.MAX_VALUE, -1));
        Assert.assertEquals(Long.MAX_VALUE - 1, MathUtil.addSafe(-1, Long.MAX_VALUE));

        Assert.assertEquals(-1, MathUtil.addSafe(Long.MIN_VALUE, Long.MAX_VALUE));
        Assert.assertEquals(-1, MathUtil.addSafe(Long.MAX_VALUE, Long.MIN_VALUE));
    }

    /** Test for {@link MathUtil#subtractSafe(int, int)}. */
    @Test
    public void testSubtractSafeIntInt()
    {
        Assert.assertEquals(Integer.MIN_VALUE, MathUtil.subtractSafe(Integer.MIN_VALUE, Integer.MAX_VALUE));
        Assert.assertEquals(Integer.MIN_VALUE, MathUtil.subtractSafe(Integer.MIN_VALUE, 1));
        Assert.assertEquals(Integer.MAX_VALUE, MathUtil.subtractSafe(1, Integer.MIN_VALUE));
        Assert.assertEquals(Integer.MIN_VALUE, MathUtil.subtractSafe(Integer.MIN_VALUE, 0));
        Assert.assertEquals(Integer.MAX_VALUE, MathUtil.subtractSafe(0, Integer.MIN_VALUE));
        Assert.assertEquals(Integer.MIN_VALUE + 1, MathUtil.subtractSafe(Integer.MIN_VALUE, -1));
        Assert.assertEquals(Integer.MAX_VALUE, MathUtil.subtractSafe(-1, Integer.MIN_VALUE));

        Assert.assertEquals(Integer.MAX_VALUE, MathUtil.subtractSafe(Integer.MAX_VALUE, Integer.MIN_VALUE));
        Assert.assertEquals(Integer.MAX_VALUE, MathUtil.subtractSafe(Integer.MAX_VALUE, -1));
        Assert.assertEquals(Integer.MIN_VALUE, MathUtil.subtractSafe(-1, Integer.MAX_VALUE));
        Assert.assertEquals(Integer.MAX_VALUE, MathUtil.subtractSafe(Integer.MAX_VALUE, 0));
        Assert.assertEquals(Integer.MIN_VALUE + 1, MathUtil.subtractSafe(0, Integer.MAX_VALUE));
        Assert.assertEquals(Integer.MAX_VALUE - 1, MathUtil.subtractSafe(Integer.MAX_VALUE, 1));
        Assert.assertEquals(Integer.MIN_VALUE + 2, MathUtil.subtractSafe(1, Integer.MAX_VALUE));

        Assert.assertEquals(0, MathUtil.subtractSafe(Integer.MIN_VALUE, Integer.MIN_VALUE));
        Assert.assertEquals(0, MathUtil.subtractSafe(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    /** Test for {@link MathUtil#subtractSafe(long, long)}. */
    @Test
    public void testSubtractSafeLongLong()
    {
        Assert.assertEquals(Long.MIN_VALUE, MathUtil.subtractSafe(Long.MIN_VALUE, Long.MAX_VALUE));
        Assert.assertEquals(Long.MIN_VALUE, MathUtil.subtractSafe(Long.MIN_VALUE, 1));
        Assert.assertEquals(Long.MAX_VALUE, MathUtil.subtractSafe(1, Long.MIN_VALUE));
        Assert.assertEquals(Long.MIN_VALUE, MathUtil.subtractSafe(Long.MIN_VALUE, 0));
        Assert.assertEquals(Long.MAX_VALUE, MathUtil.subtractSafe(0, Long.MIN_VALUE));
        Assert.assertEquals(Long.MIN_VALUE + 1, MathUtil.subtractSafe(Long.MIN_VALUE, -1));
        Assert.assertEquals(Long.MAX_VALUE, MathUtil.subtractSafe(-1, Long.MIN_VALUE));

        Assert.assertEquals(Long.MAX_VALUE, MathUtil.subtractSafe(Long.MAX_VALUE, Long.MIN_VALUE));
        Assert.assertEquals(Long.MAX_VALUE, MathUtil.subtractSafe(Long.MAX_VALUE, -1));
        Assert.assertEquals(Long.MIN_VALUE, MathUtil.subtractSafe(-1, Long.MAX_VALUE));
        Assert.assertEquals(Long.MAX_VALUE, MathUtil.subtractSafe(Long.MAX_VALUE, 0));
        Assert.assertEquals(Long.MIN_VALUE + 1, MathUtil.subtractSafe(0, Long.MAX_VALUE));
        Assert.assertEquals(Long.MAX_VALUE - 1, MathUtil.subtractSafe(Long.MAX_VALUE, 1));
        Assert.assertEquals(Long.MIN_VALUE + 2, MathUtil.subtractSafe(1, Long.MAX_VALUE));

        Assert.assertEquals(0, MathUtil.subtractSafe(Long.MIN_VALUE, Long.MIN_VALUE));
        Assert.assertEquals(0, MathUtil.subtractSafe(Long.MAX_VALUE, Long.MAX_VALUE));
    }

    /**
     * Test for {@link MathUtil#average(int...)}.
     */
    @Test
    public void testAverage()
    {
        Assert.assertEquals(5., MathUtil.average(2, 4, 4, 4, 5, 5, 7, 9), 0.);
        Assert.assertEquals(2., MathUtil.average(2), 0.);
        Assert.assertTrue(Double.isNaN(MathUtil.average()));
    }

    /**
     * Test for {@link MathUtil#bitShift(long, int)}.
     */
    @Test
    public void testBitShift()
    {
        Assert.assertEquals(4, MathUtil.bitShift(1, 2));
        Assert.assertEquals(2, MathUtil.bitShift(1, 1));
        Assert.assertEquals(1, MathUtil.bitShift(1, 0));
        Assert.assertEquals(0, MathUtil.bitShift(1, -1));
        Assert.assertEquals(0, MathUtil.bitShift(1, -2));

        Assert.assertEquals(8, MathUtil.bitShift(2, 2));
        Assert.assertEquals(4, MathUtil.bitShift(2, 1));
        Assert.assertEquals(2, MathUtil.bitShift(2, 0));
        Assert.assertEquals(1, MathUtil.bitShift(2, -1));
        Assert.assertEquals(0, MathUtil.bitShift(2, -2));
    }

    /**
     * Test for {@link MathUtil#byteShift(long, int)}.
     */
    @Test
    public void testByteShift()
    {
        Assert.assertEquals(65536, MathUtil.byteShift(1, 2));
        Assert.assertEquals(256, MathUtil.byteShift(1, 1));
        Assert.assertEquals(1, MathUtil.byteShift(1, 0));
        Assert.assertEquals(0, MathUtil.byteShift(1, -1));
        Assert.assertEquals(0, MathUtil.byteShift(1, -2));

        Assert.assertEquals(131072, MathUtil.byteShift(2, 2));
        Assert.assertEquals(512, MathUtil.byteShift(2, 1));
        Assert.assertEquals(2, MathUtil.byteShift(2, 0));
        Assert.assertEquals(0, MathUtil.byteShift(2, -1));
        Assert.assertEquals(0, MathUtil.byteShift(2, -2));

        Assert.assertEquals(0xaabbccddee0000L, MathUtil.byteShift(0xaabbccddeeL, 2));
        Assert.assertEquals(0xaabbccddee00L, MathUtil.byteShift(0xaabbccddeeL, 1));
        Assert.assertEquals(0xaabbccddeeL, MathUtil.byteShift(0xaabbccddeeL, 0));
        Assert.assertEquals(0xaabbccddL, MathUtil.byteShift(0xaabbccddeeL, -1));
        Assert.assertEquals(0xaabbccL, MathUtil.byteShift(0xaabbccddeeL, -2));
    }

    /**
     * Test for {@link MathUtil#contains(int[], int)}.
     */
    @Test
    public void testContains()
    {
        Assert.assertTrue(MathUtil.contains(new int[] { 1, 2, 3 }, 2));
        Assert.assertTrue(MathUtil.contains(new int[] { 2 }, 2));
        Assert.assertFalse(MathUtil.contains(new int[] { 1, 2, 3 }, 4));
        Assert.assertFalse(MathUtil.contains(new int[] {}, 4));
    }

    /**
     * Test for {@link MathUtil#gcd(int, int)}.
     */
    @Test
    public void testGcd()
    {
        Assert.assertEquals(0, MathUtil.gcd(0, 0));
        Assert.assertEquals(1, MathUtil.gcd(1, 0));
        Assert.assertEquals(1, MathUtil.gcd(0, 1));
        Assert.assertEquals(1, MathUtil.gcd(1, 2));
        Assert.assertEquals(1, MathUtil.gcd(2, 1));
        Assert.assertEquals(1, MathUtil.gcd(2, 3));
        Assert.assertEquals(1, MathUtil.gcd(3, 2));
        Assert.assertEquals(2, MathUtil.gcd(2, 4));
        Assert.assertEquals(2, MathUtil.gcd(4, 2));
        Assert.assertEquals(1, MathUtil.gcd(2, 5));
        Assert.assertEquals(1, MathUtil.gcd(5, 2));
        Assert.assertEquals(2, MathUtil.gcd(2, 6));
        Assert.assertEquals(2, MathUtil.gcd(6, 2));
        Assert.assertEquals(2, MathUtil.gcd(6, 10));
        Assert.assertEquals(2, MathUtil.gcd(10, 6));
        Assert.assertEquals(3, MathUtil.gcd(6, 15));
        Assert.assertEquals(3, MathUtil.gcd(15, 6));
        Assert.assertEquals(3, MathUtil.gcd(6, -15));
        Assert.assertEquals(3, MathUtil.gcd(-15, 6));
        Assert.assertEquals(3, MathUtil.gcd(-6, -15));
        Assert.assertEquals(3, MathUtil.gcd(-15, -6));
        Assert.assertEquals(5, MathUtil.gcd((int)Math.pow(2, 28) * 5, (int)Math.pow(3, 18) * 5));

        Assert.assertEquals(14, MathUtil.gcd(14, 28));
        Assert.assertEquals(7, MathUtil.gcd(14, 28, 21));
        Assert.assertEquals(7, MathUtil.gcd(21, 28, 14));
        Assert.assertEquals(7, MathUtil.gcd(14, 28, 21));
        Assert.assertEquals(7, MathUtil.gcd(new int[] { 14, 28, 21 }));
        Assert.assertEquals(7, MathUtil.gcd(new int[] { 21, 28, 14 }));
    }

    /** Test for {@link MathUtil#getModulatedFloat(long, long, long)}. */
    @Test
    public void testGetModulatedFloat()
    {
//        Assert.assertEquals(-Float.MAX_VALUE, MathUtil.getModulatedFloat(Long.MIN_VALUE, Long.MIN_VALUE, Long.MAX_VALUE), 0f);
//        Assert.assertEquals(0f, MathUtil.getModulatedFloat(0L, Long.MIN_VALUE, Long.MAX_VALUE), 0f);
//        Assert.assertEquals(Float.MAX_VALUE, MathUtil.getModulatedFloat(Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE), 0f);

        Assert.assertEquals(0, MathUtil.getModulatedFloat(0, 0, 1000), 0f);
        Assert.assertEquals(Float.MAX_VALUE / 4, MathUtil.getModulatedFloat(500, 0, 1000), 0f);
        Assert.assertEquals(Float.MAX_VALUE / 2, MathUtil.getModulatedFloat(1000, 0, 1000), 0f);

        long min = 1370476800000L;
        long max = min + Constants.MILLIS_PER_DAY;
        long step = 1000L;
        for (long test = min; test < max; test += step)
        {
            Assert.assertTrue(MathUtil.getModulatedFloat(test + step, min, max) != MathUtil.getModulatedFloat(test, min, max));
        }
    }

    /** Test for {@link MathUtil#map(int, double, double)}. */
    @Test
    public void testMapInt()
    {
        Assert.assertEquals(-1., MathUtil.map(-2147483647, -1., 1000.), 0.);
        Assert.assertEquals(1000., MathUtil.map(2147483647, -1., 1000.), 0.);
        Assert.assertEquals(499.5, MathUtil.map(0, -1., 1000.), 0.);

        Assert.assertEquals(1000., MathUtil.map(-2147483647, 1000, 2000.), 0.);
        Assert.assertEquals(1500., MathUtil.map(0, 1000, 2000.), 0.);
        Assert.assertEquals(2000., MathUtil.map(2147483647, 1000, 2000.), 0.);

        Assert.assertEquals(-1000., MathUtil.map(-2147483647, -1000, -2000.), 0.);
        Assert.assertEquals(-1500., MathUtil.map(0, -1000, -2000.), 0.);
        Assert.assertEquals(-2000., MathUtil.map(2147483647, -1000, -2000.), 0.);
    }

    /** Test for {@link MathUtil#map(long, double, double)}. */
    @Test
    public void testMapLong()
    {
        Assert.assertEquals(-1., MathUtil.map(-9223372036854775807L, -1., 1000.), 0.);
        Assert.assertEquals(1000., MathUtil.map(9223372036854775807L, -1., 1000.), 0.);
        Assert.assertEquals(499.5, MathUtil.map(0L, -1., 1000.), 0.);

        Assert.assertEquals(1000., MathUtil.map(-9223372036854775807L, 1000, 2000.), 0.);
        Assert.assertEquals(1500., MathUtil.map(0L, 1000, 2000.), 0.);
        Assert.assertEquals(2000., MathUtil.map(9223372036854775807L, 1000, 2000.), 0.);

        Assert.assertEquals(-1000., MathUtil.map(-9223372036854775807L, -1000, -2000.), 0.);
        Assert.assertEquals(-1500., MathUtil.map(0L, -1000, -2000.), 0.);
        Assert.assertEquals(-2000., MathUtil.map(9223372036854775807L, -1000, -2000.), 0.);
    }

    /** Test for {@link MathUtil#map(short, double, double)}. */
    @Test
    @SuppressWarnings("PMD.AvoidUsingShortType")
    public void testMapShort()
    {
        Assert.assertEquals(-1., MathUtil.map((short)-32767, -1., 1000.), 0.);
        Assert.assertEquals(1000., MathUtil.map((short)32767, -1., 1000.), 0.);
        Assert.assertEquals(499.5, MathUtil.map((short)0, -1., 1000.), 0.);

        Assert.assertEquals(1000., MathUtil.map((short)-32767, 1000, 2000.), 0.);
        Assert.assertEquals(1500., MathUtil.map((short)0, 1000, 2000.), 0.);
        Assert.assertEquals(2000., MathUtil.map((short)32767, 1000, 2000.), 0.);

        Assert.assertEquals(-1000., MathUtil.map((short)-32767, -1000, -2000.), 0.);
        Assert.assertEquals(-1500., MathUtil.map((short)0, -1000, -2000.), 0.);
        Assert.assertEquals(-2000., MathUtil.map((short)32767, -1000, -2000.), 0.);
    }

    /**
     * Test for {@link MathUtil#mode(int[], int...)}.
     */
    @Test
    public void testMode()
    {
        ValueWithCount<Integer> mode;

        mode = MathUtil.mode(new int[] { 1, 1, 2, 3 });
        Assert.assertEquals(1, mode.getValue().intValue());
        Assert.assertEquals(2, mode.getCount());

        mode = MathUtil.mode(new int[] { 1, 1, 1, 2, 2, 3 }, 1);
        Assert.assertEquals(2, mode.getValue().intValue());
        Assert.assertEquals(2, mode.getCount());

        mode = MathUtil.mode(new int[] { 1 });
        Assert.assertEquals(1, mode.getValue().intValue());
        Assert.assertEquals(1, mode.getCount());

        mode = MathUtil.mode(new int[] {});
        Assert.assertEquals(0, mode.getValue().intValue());
        Assert.assertEquals(0, mode.getCount());
    }

    /** Test for {@link MathUtil#normalize(double, double, double)}. */
    @Test
    public void testNormalize()
    {
        Assert.assertEquals(17., MathUtil.normalize(-43., -10., 20.), 0.);
        Assert.assertEquals(17., MathUtil.normalize(-13., -10., 20.), 0.);
        Assert.assertEquals(3., MathUtil.normalize(3., -10., 20.), 0.);
        Assert.assertEquals(3., MathUtil.normalize(33., -10., 20.), 0.);
        Assert.assertEquals(3., MathUtil.normalize(63., -10., 20.), 0.);

        Assert.assertEquals(38., MathUtil.normalize(8., 10., 40.), 0.);
        Assert.assertEquals(12., MathUtil.normalize(12., 10., 40.), 0.);
        Assert.assertEquals(12., MathUtil.normalize(42., 10., 40.), 0.);

        Assert.assertEquals(320., MathUtil.normalize(-400., 0., 360.), 0.);
        Assert.assertEquals(320., MathUtil.normalize(-40., 0., 360.), 0.);
        Assert.assertEquals(0., MathUtil.normalize(0., 0., 360.), 0.);
        Assert.assertEquals(40., MathUtil.normalize(40., 0., 360.), 0.);
        Assert.assertEquals(0., MathUtil.normalize(360., 0., 360.), 0.);
        Assert.assertEquals(40., MathUtil.normalize(400., 0., 360.), 0.);
        Assert.assertEquals(40., MathUtil.normalize(760., 0., 360.), 0.);
    }

    /**
     * Test for {@link MathUtil#roundDownTo(int, int)}.
     */
    @Test
    public void testRoundDownToIntInt()
    {
        for (int i = -8; i < 0; ++i)
        {
            Assert.assertEquals(-8, MathUtil.roundDownTo(i, 8));
        }
        for (int i = 0; i < 8; ++i)
        {
            Assert.assertEquals(0, MathUtil.roundDownTo(i, 8));
        }
        for (int i = 8; i < 16; ++i)
        {
            Assert.assertEquals(8, MathUtil.roundDownTo(i, 8));
        }
        Assert.assertEquals(16, MathUtil.roundDownTo(16, 8));
    }

    /**
     * Test for {@link MathUtil#roundDownTo(long, long)}.
     */
    @Test
    public void testRoundDownToLongLong()
    {
        for (long i = 0L; i < 8L; ++i)
        {
            Assert.assertEquals(0L, MathUtil.roundDownTo(i, 8L));
        }
        for (long i = 8L; i < 16L; ++i)
        {
            Assert.assertEquals(8L, MathUtil.roundDownTo(i, 8L));
        }
        Assert.assertEquals(16L, MathUtil.roundDownTo(16L, 8L));
    }

    /**
     * Test for {@link MathUtil#roundUpTo(int, int)}.
     */
    @Test
    public void testRoundUpToIntInt()
    {
        Assert.assertEquals(-8, MathUtil.roundUpTo(-8, 8));
        for (int i = -7; i <= 0; ++i)
        {
            Assert.assertEquals(0, MathUtil.roundUpTo(i, 8));
        }
        for (int i = 1; i <= 8; ++i)
        {
            Assert.assertEquals(8, MathUtil.roundUpTo(i, 8));
        }
        for (int i = 9; i <= 16; ++i)
        {
            Assert.assertEquals(16, MathUtil.roundUpTo(i, 8));
        }
    }

    /**
     * Test for {@link MathUtil#roundUpTo(long, long)}.
     */
    @Test
    public void testRoundUpToLongLong()
    {
        Assert.assertEquals(-8L, MathUtil.roundUpTo(-8L, 8L));
        for (long i = -7L; i <= 0L; ++i)
        {
            Assert.assertEquals(0L, MathUtil.roundUpTo(i, 8L));
        }
        for (long i = 1L; i <= 8L; ++i)
        {
            Assert.assertEquals(8L, MathUtil.roundUpTo(i, 8L));
        }
        for (long i = 9L; i <= 16L; ++i)
        {
            Assert.assertEquals(16L, MathUtil.roundUpTo(i, 8L));
        }
    }

    /**
     * Test for {@link MathUtil#standardDeviation(int...)}.
     */
    @Test
    public void testStandardDeviation()
    {
        Assert.assertEquals(2., MathUtil.standardDeviation(2, 4, 4, 4, 5, 5, 7, 9), 0.);
        Assert.assertEquals(0., MathUtil.standardDeviation(2), 0.);
        Assert.assertTrue(Double.isNaN(MathUtil.standardDeviation()));
    }

    /**
     * Test to verify that {@link MathUtil#toDegrees(double)} is faster than
     * {@link Math#toDegrees(double)} (at the cost of some accuracy).
     */
    @Test
    public void testToDegrees()
    {
        if (StringUtils.isEmpty(System.getenv("SLOW_MACHINE")))
        {
            int iterations = 100;
            int blockSize = 10000;
            double[] input = new double[blockSize];
            double[] output = new double[blockSize];
            long mathTime = 0L;
            long mathUtilTime = 0L;
            Random rand = new Random();
            for (int i = 0; i < iterations; ++i)
            {
                for (int j = 0; j < blockSize; ++j)
                {
                    input[j] = rand.nextDouble();
                }
                mathUtilTime -= System.nanoTime();
                for (int j = 0; j < blockSize; ++j)
                {
                    output[j] = MathUtil.toDegrees(input[j]);
                }
                mathUtilTime += System.nanoTime();
                mathTime -= System.nanoTime();
                for (int j = 0; j < blockSize; ++j)
                {
                    output[j] = Math.toDegrees(input[j]);
                }
                mathTime += System.nanoTime();
            }

            Assert.assertTrue("Time using " + MathUtil.class.getSimpleName() + " ("
                    + (double)mathUtilTime / iterations / blockSize + " ns) should have been less than time using "
                    + Math.class.getSimpleName() + " (" + (double)mathTime / iterations / blockSize + " ns)",
                    mathUtilTime < mathTime);
        }
    }

    /**
     * Test to verify that {@link MathUtil#toRadians(double)} is faster than
     * {@link Math#toRadians(double)} (at the cost of some accuracy).
     */
    @Test
    public void testToRadians()
    {
        if (StringUtils.isEmpty(System.getenv("SLOW_MACHINE")))
        {
            int iterations = 100;
            int blockSize = 10000;
            double[] input = new double[blockSize];
            double[] output = new double[blockSize];
            long mathTime = 0L;
            long mathUtilTime = 0L;
            Random rand = new Random();
            for (int i = 0; i < iterations; ++i)
            {
                for (int j = 0; j < blockSize; ++j)
                {
                    input[j] = rand.nextDouble();
                }
                mathUtilTime -= System.nanoTime();
                for (int j = 0; j < blockSize; ++j)
                {
                    output[j] = MathUtil.toRadians(input[j]);
                }
                mathUtilTime += System.nanoTime();
                mathTime -= System.nanoTime();
                for (int j = 0; j < blockSize; ++j)
                {
                    output[j] = Math.toRadians(input[j]);
                }
                mathTime += System.nanoTime();
            }

            Assert.assertTrue("Time using " + MathUtil.class.getSimpleName() + " ("
                    + (double)mathUtilTime / iterations / blockSize + " ns) should have been less than time using "
                    + Math.class.getSimpleName() + " (" + (double)mathTime / iterations / blockSize + " ns)",
                    mathUtilTime < mathTime);
        }
    }
}
