package io.opensphere.csvcommon.format.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import io.opensphere.core.model.LatLonAlt.CoordFormat;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.util.LocationTestUtils;

/**
 * Tests the LatitudeFormatter class.
 *
 */
@SuppressWarnings("boxing")
public class LatitudeFormatterTest
{
    /**
     * Tests formatting the cell.
     *
     * @throws ParseException bad parse.
     */
    @Test
    public void testFormatCell() throws ParseException
    {
        LatitudeFormatter formatter = new LatitudeFormatter(null);

        String decimalLatitude = "39.93588583333333";

        String latitude = (String)formatter.formatCell(decimalLatitude, "Decimal");

        assertEquals(decimalLatitude, latitude);

        latitude = (String)formatter.formatCell("N395609.189", CoordFormat.DMS.toString());

        assertEquals("  39°56'09.189\"N", latitude);

        assertNull(formatter.formatCell(null, CoordFormat.DMS.toString()));
    }

    /**
     * Tests formatting from the object value, which does nothing.
     */
    @Test
    public void testFromObjectValue()
    {
        LatitudeFormatter formatter = new LatitudeFormatter(null);

        Double expectedLat = 39.9358858333d;

        String actualLat = formatter.fromObjectValue(expectedLat, formatter.getSystemFormat());

        assertEquals(expectedLat.toString(), actualLat);

        assertTrue(StringUtils.isEmpty(formatter.fromObjectValue(null, formatter.getSystemFormat())));
    }

    /**
     * Tests getting the format for a given set of latitude values.
     */
    @Test
    public void testGetFormat()
    {
        PreferencesRegistry registry = LocationTestUtils.getPrefsRegistry();

        LatitudeFormatter formatter = new LatitudeFormatter(registry);

        String format = formatter.getFormat(New.list("39.9358858333"));

        assertEquals(CoordFormat.DECIMAL.toString(), format);

        format = formatter.getFormat(New.list("39°56'9.189\"N"));

        assertEquals(CoordFormat.DMS.toString(), format);
    }

    /**
     * Tests the known possible formats.
     */
    @Test
    public void testGetKnownPossibleFormats()
    {
        LatitudeFormatter formatter = new LatitudeFormatter(null);
        Collection<String> knownFormats = formatter.getKnownPossibleFormats();

        assertTrue(knownFormats.contains(CoordFormat.DMS.toString()));
        assertTrue(knownFormats.contains(CoordFormat.DECIMAL.toString()));
    }

    /**
     * Tests getting the system format which is nothing.
     */
    @Test
    public void testGetSystemFormat()
    {
        LatitudeFormatter formatter = new LatitudeFormatter(null);

        assertNull(formatter.getSystemFormat());
    }
}
