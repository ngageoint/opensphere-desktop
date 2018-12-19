package io.opensphere.csvcommon.detect.util;

import java.awt.Color;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility methods used to simplify color interaction.
 */
public final class ColorUtilities
{
    /** Private constructor hidden from use. */
    private ColorUtilities()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }

    /**
     * Converts the supplied hex-encoded String to a {@link Color}.
     *
     * @param hexValue the source value to convert.
     * @return a Color object or null.
     */
    public static Color toColor(String hexValue)
    {
        Color returnValue = null;

        String parseValue = hexValue;
        // if user shortcutted the value with one character per
        // field, double each character (e.g.: FFF is white, but
        // should be FFFFFF for parsing purposes)
        if (parseValue.length() >= 3 && parseValue.length() <= 4)
        {
            parseValue = "";
            for (int i = 0; i < hexValue.length(); i++)
            {
                parseValue += hexValue.charAt(i) + hexValue.charAt(i);
            }
        }

        if (parseValue.matches("[0-9a-fA-F]{6,8}"))
        {
            parseValue = "#" + hexValue;
        }

        if (parseValue.startsWith("#") || StringUtils.startsWithIgnoreCase(hexValue, "0x"))
        {
            if (hexValue.length() == 8)
            {
                // need to do it this way, because Java.awt.Color won't parse
                // Opaque with a leading alpha channel, as it attempts to parse
                // as an integer, which blows past Integer.MAX_VALUE for leading
                // values of 0xFF.
                long longValue = Long.decode(parseValue).longValue();
                int alpha = (int)((longValue >> 24) & 0xFF);
                int red = (int)((longValue >> 16) & 0xFF);
                int green = (int)((longValue >> 8) & 0xFF);
                int blue = (int)(longValue & 0xFF);

                returnValue = new Color(red, green, blue, alpha);
            }
            else if (hexValue.length() == 6)
            {
                int i = Integer.decode(parseValue).intValue();
                returnValue = new Color((i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF);
            }
        }

        return returnValue;
    }
}
