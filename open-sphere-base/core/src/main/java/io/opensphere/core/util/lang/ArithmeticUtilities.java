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
     * @param leftNumber the left number portion of the sum.
     * @param rightNumber the right number portion of the sum.
     * @return the calculated sum.
     */
    public static Number sum(Number leftNumber, Number rightNumber)
    {
        Number returnValue = null;
        Collection<Number> numbers = Arrays.asList(leftNumber, rightNumber);
        if (leftNumber instanceof Double || leftNumber instanceof Float || rightNumber instanceof Double
                || rightNumber instanceof Float)
        {
            returnValue = numbers.stream().collect(Collectors.summingDouble(x -> x.doubleValue()));
        }
        else if (leftNumber instanceof Long || rightNumber instanceof Long)
        {
            returnValue = numbers.stream().collect(Collectors.summingLong(x -> x.longValue()));
        }
        else if (leftNumber instanceof Integer || leftNumber instanceof Short || rightNumber instanceof Integer
                || rightNumber instanceof Short)
        {
            returnValue = numbers.stream().collect(Collectors.summingInt(x -> x.intValue()));
        }
        else
        {
            BigDecimal sum = BigDecimal.ZERO;
            for (Number number : numbers)
            {
                sum = sum.add(new BigDecimal(number.toString()));
            }
            returnValue = sum;
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
            returnValue = new Double(dividend.doubleValue() / divisor.doubleValue());
        }
        else if (dividend instanceof Long || divisor instanceof Long)
        {
            returnValue = new Long(dividend.longValue() / divisor.longValue());
        }
        else if (dividend instanceof Integer || divisor instanceof Integer || dividend instanceof Short
                || divisor instanceof Short)
        {
            returnValue = new Integer(dividend.intValue() / divisor.intValue());
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
            returnValue = new Double(left.doubleValue() * right.doubleValue() + 0.0);
        }
        else if (left instanceof Long || right instanceof Long)
        {
            returnValue = new Long(left.longValue() * right.longValue());
        }
        else if (left instanceof Integer || right instanceof Integer || left instanceof Short || right instanceof Short)
        {
            returnValue = new Integer(left.intValue() * right.intValue());
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
            returnValue = new Double(left.doubleValue() - right.doubleValue());
        }
        else if (left instanceof Long || right instanceof Long)
        {
            returnValue = new Long(left.longValue() - right.longValue());
        }
        else if (left instanceof Integer || right instanceof Integer || left instanceof Short || right instanceof Short)
        {
            returnValue = new Integer(left.intValue() - right.intValue());
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
