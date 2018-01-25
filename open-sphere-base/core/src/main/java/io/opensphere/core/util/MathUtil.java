package io.opensphere.core.util;

import java.util.Random;

import gnu.trove.map.hash.TIntIntHashMap;

/**
 * Math utilities.
 */
@SuppressWarnings("PMD.GodClass")
public final class MathUtil
{
    /** A "close to zero" epsilon value. */
    public static final double DBL_EPSILON = 1e-12d;

    /**
     * A "close to zero" epsilon value for use in situations where a large
     * amount of precision loss is expected.
     */
    public static final double DBL_LARGE_EPSILON = 1e-7d;

    /** A value to multiply a degree value by, to convert it to radians. */
    public static final double DEG_TO_RAD = Math.PI / 180.0;

    /** A "close to zero" epsilon value. */
    public static final float FLOAT_EPSILON = 1e-7f;

    /** The value Math.PI/2. (90 degrees). */
    public static final double HALF_PI = 0.5 * Math.PI;

    /** The value 1/Math.PI. */
    public static final double INV_PI = 1.0 / Math.PI;

    /** The value 2Math.PI. (360 degrees). */
    public static final double TWO_PI = 2.0 * Math.PI;

    /** The value 1/(2Math.PI). */
    public static final double INV_TWO_PI = 1.0 / TWO_PI;

    /** one third. */
    public static final double ONE_THIRD = 1.0 / 3;

    /** The value Math.PI/4. (45 degrees). */
    public static final double QUARTER_PI = 0.25 * Math.PI;

    /** A value to multiply a radians value by, to convert it to degrees. */
    public static final double RAD_TO_DEG = 180.0 / Math.PI;

    /** A pre-created random object for random numbers. */
    public static final Random RAND = new Random();

    /** Square root of 2. */
    public static final double SQRT2 = Math.sqrt(2.);

    /**
     * Add two integers, guarding against overflow. Similar to
     * {@link Math#addExact(int, int)} but returns {@link Integer#MIN_VALUE} or
     * {@link Integer#MAX_VALUE} rather than throwing an exception.
     *
     * @param a The first number.
     * @param b The second number.
     * @return The result.
     */
    public static int addSafe(int a, int b)
    {
        int r = a + b;
        if (((a ^ r) & (b ^ r)) < 0)
        {
            return (a & b) < 0 ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        }
        return r;
    }

    /**
     * Add two long integers, guarding against overflow. Similar to
     * {@link Math#addExact(long, long)} but returns {@link Long#MIN_VALUE} or
     * {@link Long#MAX_VALUE} rather than throwing an exception.
     *
     * @param a The first number.
     * @param b The second number.
     * @return The result.
     */
    public static long addSafe(long a, long b)
    {
        long r = a + b;
        if (((a ^ r) & (b ^ r)) < 0)
        {
            return (a & b) < 0 ? Long.MIN_VALUE : Long.MAX_VALUE;
        }
        return r;
    }

    /**
     * Returns the average of two doubles.
     *
     * @param a The first number
     * @param b The second number
     * @return The average
     */
    public static double average(double a, double b)
    {
        return (a + b) * 0.5;
    }

    /**
     * Returns the average of multiple integers.
     *
     * @param values The values
     * @return The average
     */
    public static double average(int... values)
    {
        int sum = 0;
        for (int value : values)
        {
            sum += value;
        }
        return (double)sum / values.length;
    }

    /**
     * Returns whether the value is between min and max (inclusive).
     *
     * @param val the value
     * @param min the minimum
     * @param max the maximum
     * @return whether the value is between min and max (inclusive)
     */
    public static boolean between(double val, double min, double max)
    {
        return val >= min && val <= max;
    }

    /**
     * Returns whether the value is between min and max (inclusive).
     *
     * @param val the value
     * @param min the minimum
     * @param max the maximum
     * @return whether the value is between min and max (inclusive)
     */
    public static boolean between(int val, int min, int max)
    {
        return val >= min && val <= max;
    }

    /**
     * Returns whether the value is between min and max (inclusive).
     *
     * @param val the value
     * @param min the minimum
     * @param max the maximum
     * @return whether the value is between min and max (inclusive)
     */
    public static boolean between(long val, long min, long max)
    {
        return val >= min && val <= max;
    }

    /**
     * Shift an integer by some number of bits, allowing negative shift values.
     *
     * @param inputVal The input value.
     * @param shift The number of bits to shift, positive being a left shift and
     *            negative being a right shift.
     * @return The shifted value.
     */
    public static long bitShift(long inputVal, int shift)
    {
        return shift > 0 ? inputVal << shift : shift < 0 ? inputVal >> shift * -1 : inputVal;
    }

    /**
     * Shift an integer by some number of bytes, allowing negative shift values.
     *
     * @param inputVal The input value.
     * @param shift The number of bytes to shift, positive being a left shift
     *            and negative being a right shift.
     * @return The shifted value.
     */
    public static long byteShift(long inputVal, int shift)
    {
        return shift > 0 ? inputVal << shift * Constants.BITS_PER_BYTE
                : shift < 0 ? inputVal >> shift * -Constants.BITS_PER_BYTE : inputVal;
    }

    /**
     * Take a double input and clamp it between min and max.
     *
     * @param input input
     * @param min min
     * @param max max
     * @return clamped input
     */
    public static double clamp(double input, double min, double max)
    {
        return input < min ? min : input > max ? max : input;
    }

    /**
     * Take a float input and clamp it between min and max.
     *
     * @param input input
     * @param min min
     * @param max max
     * @return clamped input
     */
    public static float clamp(float input, float min, float max)
    {
        return input < min ? min : input > max ? max : input;
    }

    /**
     * Take an int input and clamp it between min and max.
     *
     * @param input input
     * @param min min
     * @param max max
     * @return clamped input
     */
    public static int clamp(int input, int min, int max)
    {
        return input < min ? min : input > max ? max : input;
    }

    /**
     * Take a long integer input and clamp it between min and max.
     *
     * @param input input
     * @param min min
     * @param max max
     * @return clamped input
     */
    public static long clamp(long input, long min, long max)
    {
        if (input < min)
        {
            return min;
        }
        else if (input > max)
        {
            return max;
        }
        else
        {
            return input;
        }
    }

    /**
     * Determines if the array contains the given value.
     *
     * @param input the input array
     * @param value the value to search for
     * @return whether the value is contained in the array
     */
    public static boolean contains(int[] input, int value)
    {
        for (int in : input)
        {
            if (in == value)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the given arrays contain the same values.
     *
     * @param arr1 first array
     * @param arr2 second array
     * @return true if the given arrays contain the same values.
     */
    public static boolean equals(double[] arr1, double[] arr2)
    {
        if (arr1.length != arr2.length)
        {
            return false;
        }

        for (int i = 0; i < arr1.length; ++i)
        {
            if (!isZero(arr1[i] - arr2[i]))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Determine which of the given values is smallest, but greater than zero.
     *
     * @param values values to compare.
     * @return return -1, or the smallest positive value given
     */
    public static double findLeastPositive(double[] values)
    {
        double smallest = -1;
        for (int i = 0; i < values.length; ++i)
        {
            if (!Double.isNaN(values[i]) && values[i] > DBL_EPSILON)
            {
                if (smallest < 0)
                {
                    smallest = values[i];
                }
                else
                {
                    smallest = Math.min(smallest, values[i]);
                }
            }
        }

        return smallest;
    }

    /**
     * Get the greatest common denominator for two ints.
     *
     * @param i The first value.
     * @param j The second value.
     * @return The GCD.
     */
    public static int gcd(int i, int j)
    {
        int a = i;
        int b = j;
        while (b != 0)
        {
            a ^= b;
            b ^= a;
            a ^= b;
            b %= a;
        }
        return a >= 0 ? a : -a;
    }

    /**
     * Get the greatest common denominator for a number of ints.
     *
     * @param i The first value.
     * @param j The second value.
     * @param vals The rest of the values.
     * @return The GCD.
     */
    public static int gcd(int i, int j, int... vals)
    {
        int gcd = gcd(i, j);
        for (int index = 0; index < vals.length;)
        {
            gcd = gcd(gcd, vals[index++]);
        }
        return gcd;
    }

    /**
     * Get the greatest common denominator for a number of ints.
     *
     * @param vals The values.
     * @return The GCD.
     */
    public static int gcd(int[] vals)
    {
        if (vals.length == 0)
        {
            return 1;
        }
        else
        {
            int gcd = vals[0];
            for (int index = 1; index < vals.length;)
            {
                gcd = gcd(gcd, vals[index++]);
            }
            return gcd;
        }
    }

    /**
     * Return a float that represents the given integer within the given range.
     * If {@code value} is equal to {@code min}, 0 is returned. If {@code value}
     * is equal to {@code max}, half of {@link Float#MAX_VALUE} is returned.
     *
     * @param value The value.
     * @param min The minimum point of the interval.
     * @param max The maximum point of the interval.
     * @return A float value representing the position of {@code value} within
     *         the interval.
     */
    public static float getModulatedFloat(long value, long min, long max)
    {
        // Don't use max floats because when the active span is big compared to
        // the loop span the shader calculations would max out on the float
        // type. This caused incorrect alpha calculations and all features would
        // disappear.
        float modulatedValue = Float.MAX_VALUE / 2;
        if (value > min && value < max)
        {
            double range = max - min;
            double floatRange = modulatedValue - 0;

            double floatPerUnit = floatRange / range;
            long deltaFromMin = value - min;
            float floats = (float)(deltaFromMin * floatPerUnit);
            modulatedValue = floats;
        }
        else if (value <= min)
        {
            modulatedValue = 0;
        }

        return modulatedValue;
    }

    /**
     * Returns true if the number is a power of 2 (2,4,8,16...).
     *
     * A good implementation found on the Java boards. note: a number is a power
     * of two if and only if it is the smallest number with that number of
     * significant bits. Therefore, if you subtract 1, you know that the new
     * number will have fewer bits, so ANDing the original number with anything
     * less than it will give 0.
     *
     * @param number The number to test.
     * @return True if it is a power of two.
     */
    public static boolean isPowerOfTwo(int number)
    {
        return number > 0 && (number & number - 1) == 0;
    }

    /**
     * Check if the value is zero within the default tolerance.
     *
     * @param val value to check.
     * @return true if it is zero.
     */
    public static boolean isZero(double val)
    {
        return isZero(val, DBL_EPSILON);
    }

    /**
     * Check if the value is zero within a specified tolerance.
     *
     * @param val value to check.
     * @param tolerance allowable range.
     * @return true if it is zero.
     */
    public static boolean isZero(double val, double tolerance)
    {
        return val > -tolerance && val < tolerance;
    }

    /**
     * Linear interpolation from startValue to endValue by the given percent.
     * Basically: ((1 - percent) * startValue) + (percent * endValue)
     *
     * @param percent Percent value to use.
     * @param startValue Begining value. 0% of f
     * @param endValue ending value. 100% of f
     * @return The interpolated value between startValue and endValue.
     */
    public static double lerp(double percent, double startValue, double endValue)
    {
        if (startValue == endValue)
        {
            return startValue;
        }
        return (1. - percent) * startValue + percent * endValue;
    }

    /**
     * Returns the logarithm of value with given base, calculated as
     * log(value)/log(base), so that pow(base, return)==value (contributed by
     * vear).
     *
     * @param value The value to log.
     * @param base Base of logarithm.
     * @return The logarithm of value with given base
     */
    public static double log(double value, double base)
    {
        return Math.log(value) / Math.log(base);
    }

    /**
     * Map an integer in the range [-21483647,21483647] to the given range
     * [min,max].
     *
     * @param value The value to be mapped.
     * @param min The minimum result value.
     * @param max The maximum result value.
     * @return The mapped value.
     */
    public static double map(int value, double min, double max)
    {
        return (max - min) * .5 * (1 + value / (double)Integer.MAX_VALUE) + min;
    }

    /**
     * Map a long integer in the range
     * [-9223372036854775807,9223372036854775807] to the given range [min,max].
     *
     * @param value The value to be mapped.
     * @param min The minimum result value.
     * @param max The maximum result value.
     * @return The mapped value.
     */
    public static double map(long value, double min, double max)
    {
        return (max - min) * .5 * (1 + value / (double)Long.MAX_VALUE) + min;
    }

    /**
     * Map a short integer in the range [-32767,32767] to the given range
     * [min,max].
     *
     * @param value The value to be mapped.
     * @param min The minimum result value.
     * @param max The maximum result value.
     * @return The mapped value.
     */
    @SuppressWarnings("PMD.AvoidUsingShortType")
    public static double map(short value, double min, double max)
    {
        return (max - min) * .5 * (1 + value / (double)Short.MAX_VALUE) + min;
    }

    /**
     * Calculates the mode of the values and its count.
     *
     * @param values the values
     * @param valuesToIgnore the values to ignore (optional)
     * @return the mode and its count
     */
    public static ValueWithCount<Integer> mode(int[] values, int... valuesToIgnore)
    {
        int mode = 0;
        int count = 0;

        TIntIntHashMap countMap = new TIntIntHashMap();
        for (int value : values)
        {
            if (!countMap.containsKey(value))
            {
                countMap.put(value, 0);
            }
            countMap.increment(value);
        }

        for (int value : countMap.keys())
        {
            if (!contains(valuesToIgnore, value))
            {
                int thisCount = countMap.get(value);
                if (thisCount > count)
                {
                    mode = value;
                    count = thisCount;
                }
            }
        }

        return new ValueWithCount<Integer>(Integer.valueOf(mode), count);
    }

    /**
     * Find the nearest power of two.
     *
     * @param number number.
     * @return power of two nearest number.
     */
    public static int nearestPowerOfTwo(int number)
    {
        return (int)Math.pow(2., Math.ceil(Math.log(number) / Math.log(2.)));
    }

    /**
     * Normalizes a periodic value to be within range specified by min and max.
     *
     * @param value the value
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @return the normalized value
     */
    public static double normalize(double value, double min, double max)
    {
        double range = max - min;
        double normalized = (value - min) % range + min;
        if (normalized < min)
        {
            normalized += range;
        }
        return normalized;
    }

    /**
     * Returns a random int between min and max.
     *
     * @param min minimum.
     * @param max maximum.
     * @return A random int between <tt>min</tt> (inclusive) to <tt>max</tt>
     *         (inclusive).
     */
    public static int randomInt(int min, int max)
    {
        return (int)(RAND.nextFloat() * (max - min + 1)) + min;
    }

    /**
     * Fast Trig functions for x86. This forces the trig functiosn to stay
     * within the safe area on the x86 processor (-45 degrees to +45 degrees)
     * The results may be very slightly off from what the Math and StrictMath
     * trig functions give due to rounding in the angle reduction but it will be
     * very very close.
     *
     * note: code from wiki posting on java.net by jeffpk
     *
     * @param radians angle
     * @return sin
     */
    public static double reduceSinAngle(double radians)
    {
        double result = radians;
        // put us in -2Math.PI to +2Math.PI space
        result %= TWO_PI;
        if (Math.abs(result) > Math.PI)
        {
            // put us in -Math.PI to +Math.PI space
            result = result - TWO_PI;
        }
        if (Math.abs(result) > HALF_PI)
        {
            // put us in -Math.PI/2 to +Math.PI/2 space
            result = Math.PI - result;
        }

        return result;
    }

    /**
     * Round the given value to a specified number of places to the right of the
     * decimal point.
     *
     * @param value the original value
     * @param place The number of places to the right of the decimal point to
     *            which to round.
     * @return the rounded value.
     */
    public static double roundDecimalPlace(double value, int place)
    {
        double displacement = Math.pow(10, place);
        return Math.round(value * displacement) / displacement;
    }

    /**
     * Round the given value down to the next lower number which can be divided
     * by the modulus. For example roundDownTo(13, 8) should give 8.
     *
     * @param value the value to round.
     * @param modulus The number which must divide the result.
     * @return The next lower number which can be divided by the modulus.
     */
    public static int roundDownTo(int value, int modulus)
    {
        return value >= 0 ? value - value % modulus : value + (value % modulus == 0 ? 0 : -modulus - value % modulus);
    }

    /**
     * Round the given value down to the next lower number which can be divided
     * by the modulus. For example roundDownTo(13, 8) should give 8.
     *
     * @param value the value to round.
     * @param modulus The number which must divide the result.
     * @return The next lower number which can be divided by the modulus.
     */
    public static long roundDownTo(long value, long modulus)
    {
        return value >= 0 ? value - value % modulus : value + (value % modulus == 0 ? 0 : -modulus - value % modulus);
    }

    /**
     * Round the given value up to the next highest number which can be divided
     * by the modulus. For example roundUpTo(13, 8) should give 16.
     *
     * @param value the value to round.
     * @param modulus The number which must divide the result.
     * @return The next highest number which can be divided by the modulus.
     */
    public static int roundUpTo(int value, int modulus)
    {
        return value > 0 ? value % modulus == 0 ? value : value + modulus - value % modulus : value - value % modulus;
    }

    /**
     * Round the given value up to the next highest number which can be divided
     * by the modulus. For example roundUpTo(13, 8) should give 16.
     *
     * @param value the value to round.
     * @param modulus The number which must divide the result.
     * @return The next highest number which can be divided by the modulus.
     */
    public static long roundUpTo(long value, long modulus)
    {
        return value > 0 ? value % modulus == 0 ? value : value + modulus - value % modulus : value - value % modulus;
    }

    /**
     * Tell whether both values have the same sign.
     *
     * @param a first value.
     * @param b second value.
     * @return true when both values have the same sign.
     */
    public static boolean sameSign(double a, double b)
    {
        return a >= 0 ^ b < 0;
    }

    /**
     * Tell whether both values have the same sign.
     *
     * @param a first value.
     * @param b second value.
     * @return true when both values have the same sign.
     */
    public static boolean sameSign(int a, int b)
    {
        return a >= 0 ^ b < 0;
    }

    /**
     * Calculates the standard deviation of the given values.
     *
     * @param values the values
     * @return the standard deviation
     */
    public static double standardDeviation(int... values)
    {
        double average = average(values);
        double sumOfSquares = 0;
        double diff;
        for (int value : values)
        {
            diff = value - average;
            sumOfSquares += diff * diff;
        }
        return Math.sqrt(sumOfSquares / values.length);
    }

    /**
     * Subtract two integers, guarding against overflow. Similar to
     * {@link Math#subtractExact(int, int)} but returns
     * {@link Integer#MIN_VALUE} or {@link Integer#MAX_VALUE} rather than
     * throwing an exception.
     *
     * @param a The first number.
     * @param b The second number.
     * @return The result.
     */
    public static int subtractSafe(int a, int b)
    {
        int r = a - b;
        if (((a ^ b) & (a ^ r)) < 0)
        {
            return a < 0 ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        }
        return r;
    }

    /**
     * Subtract two long integers, guarding against overflow. Similar to
     * {@link Math#subtractExact(long, long)} but returns {@link Long#MIN_VALUE}
     * or {@link Long#MAX_VALUE} rather than throwing an exception.
     *
     * @param a The first number.
     * @param b The second number.
     * @return The result.
     */
    public static long subtractSafe(long a, long b)
    {
        long r = a - b;
        if (((a ^ b) & (a ^ r)) < 0)
        {
            return a < 0 ? Long.MIN_VALUE : Long.MAX_VALUE;
        }
        return r;
    }

    /**
     * Convert from radians to degrees.
     *
     * @param rad angle in radians.
     * @return angle in degrees.
     */
    public static double toDegrees(double rad)
    {
        return rad * RAD_TO_DEG;
    }

    /**
     * Convert degrees to radians.
     *
     * @param deg degrees
     * @return radians
     */
    public static double toRadians(double deg)
    {
        return deg * DEG_TO_RAD;
    }

    /** Disallow class instantiation. */
    private MathUtil()
    {
    }
}
