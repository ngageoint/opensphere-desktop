package io.opensphere.csvcommon.format.color;

import java.awt.Color;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.csvcommon.format.CellFormatter;

/**
 * A cell formatter used to convert String values to Color objects.
 */
public class ColorFormatter implements CellFormatter
{
    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.csvcommon.format.CellFormatter#formatCell(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public Object formatCell(String cellValue, String format) throws ParseException
    {
        Color returnValue = null;

        String parseValue = cellValue;
        // if user shortcutted the value with one character per
        // field, double each character (e.g.: FFF is white, but
        // should be FFFFFF for parsing purposes)
        if (parseValue.length() >= 3 && parseValue.length() <= 4)
        {
            parseValue = "";
            for (int i = 0; i < cellValue.length(); i++)
            {
                parseValue += cellValue.charAt(i) + cellValue.charAt(i);
            }
        }

        if (parseValue.matches("[0-9a-fA-F]{6,8}"))
        {
            parseValue = "#" + cellValue;
        }

        if (parseValue.startsWith("#") || StringUtils.startsWithIgnoreCase(cellValue, "0x"))
        {
            if (cellValue.length() == 8)
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
            else if (cellValue.length() == 6)
            {
                int i = Integer.decode(parseValue).intValue();
                returnValue = new Color((i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF);
            }
        }

        return returnValue;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.csvcommon.format.CellFormatter#fromObjectValue(java.lang.Object,
     *      java.lang.String)
     */
    @Override
    public String fromObjectValue(Object value, String format)
    {
        if (value instanceof Color)
        {
            Color color = (Color)value;
            String hexColour = Integer.toHexString(color.getAlpha() & 0xFF) + Integer.toHexString(color.getRGB() & 0xffffff);
            if (hexColour.length() < 8)
            {
                hexColour = "00000000".substring(0, 6 - hexColour.length()) + hexColour;
            }
            return "#" + hexColour;
        }

        return value.toString().toUpperCase();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.csvcommon.format.CellFormatter#getKnownPossibleFormats()
     */
    @Override
    public Collection<String> getKnownPossibleFormats()
    {
        return List.of("color");
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.csvcommon.format.CellFormatter#getSystemFormat()
     */
    @Override
    public String getSystemFormat()
    {
        return "color";
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.csvcommon.format.CellFormatter#getFormat(java.util.List)
     */
    @Override
    public String getFormat(List<String> values)
    {
        return "color";
    }
}
