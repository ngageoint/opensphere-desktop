package io.opensphere.core.util.lang;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Generic number utilities.
 */
public final class NumberUtilities
{
    /**
     * Attempts conversion of an object to a Double in order to determine
     * whether or not it is a Number.
     *
     * @param value the object to convert
     * @return whether the conversion was successful
     */
    public static boolean isNumber(Object value)
    {
        if (value == null)
        {
            return false;
        }
        else if (value instanceof Number)
        {
            return true;
        }

        try
        {
            Double.valueOf(value.toString());
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    /**
     * Attempts conversion of an object to a Double in order to determine
     * whether or not it is a Number.
     *
     * @param values the objects to test
     * @return whether the conversion was successful
     */
    public static boolean areAllNumbers(Object... values)
    {
        // assume true until proven otherwise:
        boolean returnValue = true;

        for (Object value : values)
        {
            returnValue &= isNumber(value);
        }
        return returnValue;
    }

    /**
     * Returns the primitive value of the given Integer, or the default value if
     * it's null.
     *
     * @param value the value
     * @param defaultValue the default value
     * @return the primitive value, or default value if the value is null
     */
    public static int intValue(Integer value, int defaultValue)
    {
        return value == null ? defaultValue : value.intValue();
    }

    /**
     * Returns the double value of the given string, or the default value if
     * it's not parseable.
     *
     * @param string the string
     * @param defaultValue the default value
     * @return the double value, or default value if the value is not parseable
     */
    public static double parseDouble(String string, double defaultValue)
    {
        double value = defaultValue;
        if (string != null && !string.isEmpty())
        {
            try
            {
                value = Double.parseDouble(string);
            }
            catch (NumberFormatException e)
            {
                value = defaultValue;
            }
        }
        return value;
    }

    /**
     * Returns the double value of the given object, or the default value if
     * it's not parseable.
     *
     * @param object the object
     * @param defaultValue the default value
     * @return the double value, or default value if the value is not parseable
     */
    public static double parseDouble(Object object, double defaultValue)
    {
        if (object == null)
        {
            return defaultValue;
        }
        else if (object instanceof Number)
        {
            return ((Number)object).doubleValue();
        }

        return parseDouble(object.toString(), defaultValue);
    }

    /**
     * Returns the integer value of the given string, or the default value if
     * it's not parseable.
     *
     * @param string the string
     * @param defaultValue the default value
     * @return the integer value, or default value if the value is not parseable
     */
    public static int parseInt(String string, int defaultValue)
    {
        int value = defaultValue;
        if (string != null && !string.isEmpty())
        {
            try
            {
                value = Integer.parseInt(string);
            }
            catch (NumberFormatException e)
            {
                value = defaultValue;
            }
        }
        return value;
    }

    /**
     * Converts a big integer to int.
     *
     * @param value the big integer
     * @return the int
     */
    public static int toInt(Number value)
    {
        return toInt(value, 0);
    }

    /**
     * Converts a big integer to int.
     *
     * @param value the big integer
     * @param defaultValue the default value
     * @return the int
     */
    public static int toInt(Number value, int defaultValue)
    {
        return value != null ? value.intValue() : defaultValue;
    }

    /**
     * Converts a big decimal to float.
     *
     * @param value the big decimal
     * @return the float
     */
    public static float toFloat(Number value)
    {
        return toFloat(value, 0f);
    }

    /**
     * Converts a big decimal to float.
     *
     * @param value the big decimal
     * @param defaultValue the default value
     * @return the float
     */
    public static float toFloat(Number value, float defaultValue)
    {
        return value != null ? value.floatValue() : defaultValue;
    }

    /**
     * Converts a big decimal to double.
     *
     * @param value the big decimal
     * @return the double
     */
    public static double toDouble(Number value)
    {
        return toDouble(value, 0.);
    }

    /**
     * Converts a big decimal to double.
     *
     * @param value the big decimal
     * @param defaultValue the default value
     * @return the double
     */
    public static double toDouble(Number value, double defaultValue)
    {
        return value != null ? value.doubleValue() : defaultValue;
    }

    /**
     * Converts the supplied value to a {@link BigDecimal}.
     *
     * @param number the value to convert to a {@link BigDecimal}.
     * @return the {@link BigDecimal} equivalent of the supplied value.
     */
    public static BigDecimal toBigDecimal(Number number)
    {
        BigDecimal returnValue = null;
        if (number instanceof BigDecimal)
        {
            returnValue = (BigDecimal)number;
        }
        if (number instanceof BigInteger)
        {
            returnValue = new BigDecimal((BigInteger)number);
        }
        if (number instanceof Byte || number instanceof Short || number instanceof Integer || number instanceof Long)
        {
            returnValue = new BigDecimal(number.longValue());
        }
        if (number instanceof Float || number instanceof Double)
        {
            returnValue = new BigDecimal(number.doubleValue());
        }
        else
        {
            try
            {
                returnValue = new BigDecimal(number.toString());
            }
            catch (final NumberFormatException e)
            {
                throw new RuntimeException("The given number (\"" + number + "\" of class " + number.getClass().getName()
                        + ") does not have a parsable string representation", e);
            }
        }

        return returnValue;
    }

    /**
     * Compares the supplied values, determining if the left side is greater
     * than or less than the right side. If they're equivalent, the method will
     * return zero.
     *
     * @param left the left hand side of the comparison.
     * @param right the right hand side of the comparison.
     * @return the result of the comparison. If the left side of the comparison
     *         is is numerically equal to the right side, a value of 0 is
     *         returned; a value less than 0 if left is numerically less than
     *         right; and a value greater than 0 if left is numerically greater
     *         than right.
     */
    public static int compare(Number left, Number right)
    {
        int returnValue;
        if (ArithmeticUtilities.isSpecial(left) || ArithmeticUtilities.isSpecial(right))
        {
            returnValue = Double.compare(left.doubleValue(), right.doubleValue());
        }
        else
        {
            returnValue = NumberUtilities.toBigDecimal(left).compareTo(NumberUtilities.toBigDecimal(right));
        }
        return returnValue;
    }

    /**
     * Gets the supplied value as an Number, or null if the value cannot be
     * converted.
     *
     * @param value the value to convert.
     * @return a Number equivalent to the supplied value, or null if it cannot
     *         be converted.
     */
    public static Number getNumericValue(Object value)
    {
        Number returnValue = null;
        if (value instanceof Number)
        {
            returnValue = (Number)value;
        }
        return returnValue;
    }

    /**
     * Gets the supplied values array as an array of Numbers, ordered in the
     * same order, or null if the value cannot be converted.
     *
     * @param values the values to convert.
     * @return a Number equivalent to the supplied value, or null if it cannot
     *         be converted.
     */
    public static Number[] getNumericValues(Object... values)
    {
        Number[] returnValue = new Number[values.length];
        for (int i = 0; i < values.length; i++)
        {
            returnValue[i] = getNumericValue(values[i]);
        }
        return returnValue;
    }

    /** Disallow instantiation. */
    private NumberUtilities()
    {
    }
}
