package io.opensphere.core.util.lang;

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

    /** Disallow instantiation. */
    private NumberUtilities()
    {
    }
}
