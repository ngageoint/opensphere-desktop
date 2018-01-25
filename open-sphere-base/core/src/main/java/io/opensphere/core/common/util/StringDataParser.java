package io.opensphere.core.common.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for parsing strings into potentially non-string data.
 */
public final class StringDataParser
{
    /**
     * Parse a string into an instance of the specified type. Surely there must
     * be a better way!
     *
     * @param targetType the type of data to which the string will be converted.
     * @param stringToParse the string to convert.
     * @return in instance of the specified type, populated with the converted
     *         string data, if such a conversion is possible.
     * @throws ParseException if there is a problem parsing the string.
     */
    public static Object parseStringToType(Class<?> targetType, String stringToParse) throws ParseException
    {
        if (targetType == null)
        {
            throw new ParseException("Attempt to parse a string into a null target type.", 0);
        }

        if (stringToParse == null)
        {
            return null;
        }

        if (stringToParse.trim().isEmpty())
        {
            return null;
        }

        if (targetType == String.class)
        {
            return stringToParse;
        }

        if (targetType == int.class)
        {
            return Integer.parseInt(stringToParse);
        }

        if (targetType == long.class)
        {
            return Long.parseLong(stringToParse);
        }

        if (targetType == float.class)
        {
            return Float.parseFloat(stringToParse);
        }

        if (targetType == double.class)
        {
            return Double.parseDouble(stringToParse);
        }

        if (targetType == short.class)
        {
            return Short.parseShort(stringToParse);
        }

        if (targetType == boolean.class)
        {
            return Boolean.parseBoolean(stringToParse);
        }

        if (targetType == Byte.class)
        {
            return Byte.parseByte(stringToParse);
        }

        if (targetType == Character.class)
        {
            return stringToParse.charAt(0);
        }

        if (targetType == Date.class)
        {
            DateFormat df = new SimpleDateFormat();
            return df.parse(stringToParse);
        }

        if (targetType == BigDecimal.class)
        {
            return new BigDecimal(stringToParse);
        }

        if (targetType == BigInteger.class)
        {
            return new BigInteger(stringToParse);
        }

        throw new ParseException("Unable to convert string '" + stringToParse + "' to " + targetType.getName(), 0);
    }
}
