package io.opensphere.analysis.export.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import io.opensphere.analysis.export.model.ExportOptionsModel;
import io.opensphere.analysis.export.model.LatLonFormat;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LongitudeKey;

/**
 * Unit test for {@link LatLonFormatter}.
 */
public class LatLonFormatterTest
{
    /**
     * Tests formatting latitude for Decimal option.
     */
    @Test
    public void testFormatDecimalLat()
    {
        ExportOptionsModel model = new ExportOptionsModel();
        LatLonFormatter formatter = new LatLonFormatter(model);

        Object formatted = formatter.format(10.1, new LatitudeKey());

        assertEquals(10.1, (Double)formatted, 0d);
    }

    /**
     * Tests formatting longitude for Decimal option.
     */
    @Test
    public void testFormatDecimalLon()
    {
        ExportOptionsModel model = new ExportOptionsModel();
        LatLonFormatter formatter = new LatLonFormatter(model);

        Object formatted = formatter.format(100.1, new LongitudeKey());

        assertEquals(100.1, (Double)formatted, 0d);
    }

    /**
     * Tests formatting null lat for Decimal option.
     */
    @Test
    public void testFormatDecimalNull()
    {
        ExportOptionsModel model = new ExportOptionsModel();
        LatLonFormatter formatter = new LatLonFormatter(model);

        Object formatted = formatter.format(null, new LatitudeKey());

        assertNull(formatted);
    }

    /**
     * Tests formatting latitude for DMS option.
     */
    @Test
    public void testFormatDMSLat()
    {
        ExportOptionsModel model = new ExportOptionsModel();
        model.setSelectedLatLonFormat(LatLonFormat.DMS);
        LatLonFormatter formatter = new LatLonFormatter(model);

        Object formatted = formatter.format(39.8826573285, new LatitudeKey());

        assertEquals("39°52'57.566\"N", formatted.toString());
    }

    /**
     * Tests formatting longitude for DMS option.
     */
    @Test
    public void testFormatDMSLon()
    {
        ExportOptionsModel model = new ExportOptionsModel();
        model.setSelectedLatLonFormat(LatLonFormat.DMS);
        LatLonFormatter formatter = new LatLonFormatter(model);

        Object formatted = formatter.format(-104.780539575, new LongitudeKey());

        assertEquals("104°46'49.942\"W", formatted.toString());
    }

    /**
     * Tests formatting null longitude for DMS option.
     */
    @Test
    public void testFormatDMSNull()
    {
        ExportOptionsModel model = new ExportOptionsModel();
        model.setSelectedLatLonFormat(LatLonFormat.DMS);
        LatLonFormatter formatter = new LatLonFormatter(model);

        Object formatted = formatter.format(null, new LongitudeKey());

        assertNull(formatted);
    }

    /**
     * Tests formatting latitude for special DMS option.
     */
    @Test
    public void testFormatSpecialLat()
    {
        ExportOptionsModel model = new ExportOptionsModel();
        model.setSelectedLatLonFormat(LatLonFormat.DMS_CUSTOM);
        LatLonFormatter formatter = new LatLonFormatter(model);

        Object formatted = formatter.format(39.8826573285, new LatitudeKey());

        assertEquals("39.52.58 N", formatted.toString());
    }

    /**
     * Tests formatting longitude for special DMS option.
     */
    @Test
    public void testFormatSpecialLon()
    {
        ExportOptionsModel model = new ExportOptionsModel();
        model.setSelectedLatLonFormat(LatLonFormat.DMS_CUSTOM);
        LatLonFormatter formatter = new LatLonFormatter(model);

        Object formatted = formatter.format(-104.780539575, new LongitudeKey());

        assertEquals("104.46.50 W", formatted.toString());
    }

    /**
     * Tests formatting null for special DMS option.
     */
    @Test
    public void testFormatSpecialNull()
    {
        ExportOptionsModel model = new ExportOptionsModel();
        model.setSelectedLatLonFormat(LatLonFormat.DMS_CUSTOM);
        LatLonFormatter formatter = new LatLonFormatter(model);

        Object formatted = formatter.format(null, new LongitudeKey());

        assertNull(formatted);
    }
}
