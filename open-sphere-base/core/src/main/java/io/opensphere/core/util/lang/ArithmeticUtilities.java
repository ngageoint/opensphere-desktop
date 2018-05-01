package io.opensphere.core.util.lang;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A collection of utilities to perform basic arithmetic operatoins on generic
 * java.lang.Number instances.
 */
public final class ArithmeticUtilities
{
    /** Private constructor to prevent instantiation. */
    private ArithmeticUtilities()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not supported.");
    }

    /**
     * Calculates the sum of the supplied numbers, and returns the result.
     *
     * @param numbers the numbers for which to calculate the sum.
     * @return the calculated sum.
     */
    public static Number sum(Number... numbers)
    {
        Number returnValue = null;
        Collection<Number> numberCollection = Arrays.asList(numbers);
        int doubleCount = 0;
        int longCount = 0;
        int integerCount = 0;
        int bigDecimalCount = 0;
        for (Number number : numberCollection)
        {
            if (number instanceof Double || number instanceof Float)
            {
                doubleCount++;
            }
            else if (number instanceof Long)
            {
                longCount++;
            }
            else if (number instanceof Integer || number instanceof Short)
            {
                integerCount++;
            }
            else
            {
                bigDecimalCount++;
            }
        }

        if (bigDecimalCount > 0)
        {
            BigDecimal sum = BigDecimal.ZERO;
            numberCollection.stream().map(n -> new BigDecimal(n.toString())).forEach(bd -> sum.add(bd));
            returnValue = sum;
        }
        else if (doubleCount > 0)
        {
            returnValue = numberCollection.stream().collect(Collectors.summingDouble(x -> x.doubleValue()));
        }
        else if (longCount > 0)
        {
            returnValue = numberCollection.stream().collect(Collectors.summingLong(x -> x.longValue()));
        }
        else if (integerCount > 0)
        {
            returnValue = numberCollection.stream().collect(Collectors.summingInt(x -> x.intValue()));
        }

        return returnValue;
    }

    /**
     * Calculates the product of the supplied numbers, and returns the result.
     * In the event of a divisor equal to zero, a null value is returned.
     *
     * @param dividend the number to be divided by the divisor.
     * @param divisor the number by which the dividend is divided.
     * @return the calculated quotient.
     */
    public static Number quotient(Number dividend, Number divisor)
    {
        if (divisor.doubleValue() == 0)
        {
            return null;
        }
        Number returnValue = null;
        if (dividend instanceof Double || dividend instanceof Float || divisor instanceof Double || divisor instanceof Float)
        {
            returnValue = Double.valueOf(dividend.doubleValue() / divisor.doubleValue());
        }
        else if (dividend instanceof Long || divisor instanceof Long)
        {
            returnValue = Long.valueOf(dividend.longValue() / divisor.longValue());
        }
        else if (dividend instanceof Integer || divisor instanceof Integer || dividend instanceof Short
                || divisor instanceof Short)
        {
            returnValue = Integer.valueOf(dividend.intValue() / divisor.intValue());
        }
        else
        {
            BigDecimal leftNumber = new BigDecimal(dividend.toString());
            returnValue = leftNumber.divide(new BigDecimal(divisor.toString()));
        }

        return returnValue;
    }

    /**
     * Calculates the product of the supplied numbers, and returns the result.
     *
     * @param left the left number portion of the product.
     * @param right the right number portion of the product.
     * @return the calculated product.
     */
    public static Number product(Number left, Number right)
    {
        Number returnValue = null;
        if (left instanceof Double || left instanceof Float || right instanceof Double || right instanceof Float)
        {
            returnValue = Double.valueOf(left.doubleValue() * right.doubleValue() + 0.0);
        }
        else if (left instanceof Long || right instanceof Long)
        {
            returnValue = Long.valueOf(left.longValue() * right.longValue());
        }
        else if (left instanceof Integer || right instanceof Integer || left instanceof Short || right instanceof Short)
        {
            returnValue = Integer.valueOf(left.intValue() * right.intValue());
        }
        else
        {
            BigDecimal leftNumber = new BigDecimal(left.toString());
            returnValue = leftNumber.multiply(new BigDecimal(right.toString()));
        }

        return returnValue;
    }

    /**
     * Calculates the difference of the supplied numbers, and returns the
     * result.
     *
     * @param left the left number portion of the difference.
     * @param right the right number portion of the difference.
     * @return the calculated difference.
     */
    public static Number difference(Number left, Number right)
    {
        Number returnValue = null;
        if (left instanceof Double || left instanceof Float || right instanceof Double || right instanceof Float)
        {
            returnValue = Double.valueOf(left.doubleValue() - right.doubleValue());
        }
        else if (left instanceof Long || right instanceof Long)
        {
            returnValue = Long.valueOf(left.longValue() - right.longValue());
        }
        else if (left instanceof Integer || right instanceof Integer || left instanceof Short || right instanceof Short)
        {
            returnValue = Integer.valueOf(left.intValue() - right.intValue());
        }
        else
        {
            BigDecimal leftNumber = new BigDecimal(left.toString());
            returnValue = leftNumber.subtract(new BigDecimal(right.toString()));
        }

        return returnValue;
    }

    /**
     * Tests to determine if the supplied number is a special case, such as
     * {@link Double#NaN} or infinity.
     *
     * @param value the value to test.
     * @return true if the supplied value is a special case, false otherwise.
     */
    public static boolean isSpecial(Number value)
    {
        boolean returnValue = false;
        if (value instanceof Double)
        {
            returnValue = Double.isNaN(value.doubleValue()) || Double.isInfinite(value.doubleValue());
        }
        else if (value instanceof Float)
        {
            returnValue = Float.isNaN(value.floatValue()) || Float.isInfinite(value.floatValue());
        }
        return returnValue;
    }
}
