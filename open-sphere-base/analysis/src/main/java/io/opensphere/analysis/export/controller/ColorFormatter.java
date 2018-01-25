package io.opensphere.analysis.export.controller;

import java.awt.Color;

import io.opensphere.analysis.export.model.ColorFormat;
import io.opensphere.analysis.export.model.ExportOptionsModel;

/**
 * Takes a color value and formats according to the user's selection.
 */
public class ColorFormatter
{
    /**
     * Contains the user's selection.
     */
    private final ExportOptionsModel myExportModel;

    /**
     * Constructs a new color formatter.
     *
     * @param exportModel Contains the user's inputs.
     */
    public ColorFormatter(ExportOptionsModel exportModel)
    {
        myExportModel = exportModel;
    }

    /**
     * Formats the color value to the format specified by the user.
     *
     * @param value The color value or null.
     * @return The formatted value, or null if value was null.
     */
    public String format(Object value)
    {
        String formatted = null;

        if (value instanceof Color)
        {
            Color color = (Color)value;
            if (myExportModel.getSelectedColorFormat() == ColorFormat.HEXADECIMAL)
            {
                formatted = Integer.toHexString(color.getRGB());
            }
            else
            {
                StringBuilder sb = new StringBuilder("color[r=").append(color.getRed()).append(",g=").append(color.getGreen())
                        .append(",b=").append(color.getBlue()).append(",a=").append(color.getAlpha()).append(']');
                formatted = sb.toString();
            }
        }

        return formatted;
    }
}
