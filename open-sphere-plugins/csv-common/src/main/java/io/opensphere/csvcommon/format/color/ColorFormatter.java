package io.opensphere.csvcommon.format.color;

import java.awt.Color;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;

import io.opensphere.csvcommon.detect.util.ColorUtilities;
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
        return ColorUtilities.toColor(cellValue);
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
