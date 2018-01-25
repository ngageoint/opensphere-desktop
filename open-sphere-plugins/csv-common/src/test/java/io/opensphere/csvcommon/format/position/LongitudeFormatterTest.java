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
import io.opensphere.csvcommon.format.position.LongitudeFormatter;
import io.opensphere.csvcommon.util.LocationTestUtils;

/**
 * Tests the LongitudeFormatter class.
 *
 */
public class LongitudeFormatterTest
{
    /**
     * Tests formatting the cells.
     *
     * @throws ParseException Bad parse.
     */
    @Test
    public void testFormatCell() throws ParseException
    {
        LongitudeFormatter formatter = new LongitudeFormatter(null);

        String decimalLongitude = "-105.39274694444445";

        String longitude = (String)formatter.formatCell(decimalLongitude, "Decimal");

        assertEquals(decimalLongitude, longitude);

        longitude = (String)formatter.formatCell("W1052333.889", CoordFormat.DMS.toString());

        assertEquals(" 105°23'33.889\"W", longitude);

        assertNull(formatter.formatCell(null, CoordFormat.DMS.toString()));
    }

    /**
     * Tests getting the string value from the formatted value.
     */
    @Test
    public void testFromObjectValue()
    {
        LongitudeFormatter formatter = new LongitudeFormatter(null);

        Double expectedLon = -105.3927469444d;

        String actualLon = formatter.fromObjectValue(expectedLon, formatter.getSystemFormat());

        assertEquals(expectedLon.toString(), actualLon);

        assertTrue(StringUtils.isEmpty(formatter.fromObjectValue(null, formatter.getSystemFormat())));
    }

    /**
     * Tests getting the format for a given set of longitude values.
     */
    @Test
    public void testGetFormat()
    {
        PreferencesRegistry registry = LocationTestUtils.getPrefsRegistry();

        LongitudeFormatter formatter = new LongitudeFormatter(registry);

        String format = formatter.getFormat(New.list("-105.3927469444"));

        assertEquals(CoordFormat.DECIMAL.toString(), format);

        format = formatter.getFormat(New.list("105°23'33.889\"W"));

        assertEquals(CoordFormat.DMS.toString(), format);
    }

    /**
     * Tests getting the known possible formats.
     */
    @Test
    public void testGetKnownPossibleFormats()
    {
        LongitudeFormatter formatter = new LongitudeFormatter(null);
        Collection<String> knownFormats = formatter.getKnownPossibleFormats();

        assertTrue(knownFormats.contains(CoordFormat.DMS.toString()));
        assertTrue(knownFormats.contains(CoordFormat.DECIMAL.toString()));
    }

    /**
     * Tests getting the system format.
     */
    @Test
    public void testGetSystemFormat()
    {
        LongitudeFormatter formatter = new LongitudeFormatter(null);

        assertNull(formatter.getSystemFormat());
    }
}
