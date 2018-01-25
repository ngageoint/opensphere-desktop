package io.opensphere.analysis.export.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.awt.Color;

import org.junit.Test;

import io.opensphere.analysis.export.model.ColorFormat;
import io.opensphere.analysis.export.model.ExportOptionsModel;

/**
 * Unit test for {@link ColorFormatter}.
 */
public class ColorFormatterTest
{
    /**
     * Tests formatting a color to hexadecimal.
     */
    @Test
    public void testFormat()
    {
        ExportOptionsModel model = new ExportOptionsModel();
        ColorFormatter formatter = new ColorFormatter(model);
        String format = formatter.format(Color.RED);

        assertEquals(Integer.toHexString(Color.RED.getRGB()), format);
    }

    /**
     * Tests formatting a null color.
     */
    @Test
    public void testFormatNull()
    {
        ExportOptionsModel model = new ExportOptionsModel();
        ColorFormatter formatter = new ColorFormatter(model);
        String format = formatter.format(null);

        assertNull(format);
    }

    /**
     * Tests formatting a color to rgb coded.
     */
    @Test
    public void testFormatRgb()
    {
        ExportOptionsModel model = new ExportOptionsModel();
        model.setSelectedColorFormat(ColorFormat.RGB_CODED);
        ColorFormatter formatter = new ColorFormatter(model);
        String format = formatter.format(Color.RED);

        assertEquals("color[r=255,g=0,b=0,a=255]", format);
    }
}
