package io.opensphere.core.util.lang;

/**
 * Generic number utilities.
 */
public final class NumberUtilities
{
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
        double value;
        try
        {
            value = Double.parseDouble(string);
        }
        catch (NumberFormatException e)
        {
            value = defaultValue;
        }
        return value;
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
        int value;
        try
        {
            value = Integer.parseInt(string);
        }
        catch (NumberFormatException e)
        {
            value = defaultValue;
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
