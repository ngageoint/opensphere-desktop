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
import io.opensphere.csvcommon.format.position.PositionFormatter;
import io.opensphere.csvcommon.util.LocationTestUtils;

/**
 * Tests the PositionFormatter class.
 *
 */
public class PositionFormatterTest
{
    /**
     * Tests formatting the cell.
     *
     * @throws ParseException bad parse.
     */
    @Test
    public void testFormatCell() throws ParseException
    {
        PositionFormatter formatter = new PositionFormatter(null);

        String position = "N314046 E0302407";
        String expectedPosition = "31.679444444444446 30.401944444444442";

        String actualPosition = (String)formatter.formatCell(position, "Decimal");

        assertEquals(expectedPosition, actualPosition);

        assertNull(formatter.formatCell(null, null));

        actualPosition = (String)formatter.formatCell(position, CoordFormat.DMS.toString());

        assertEquals("  31°40'46.000\"N   30°24'07.000\"E", actualPosition);
    }

    /**
     * Test formatting the object value.
     */
    @Test
    public void testFromObjectValue()
    {
        PositionFormatter formatter = new PositionFormatter(null);

        String expectedPosition = "31.6794444444 30.4019444444";

        String actualPosition = formatter.fromObjectValue(expectedPosition, formatter.getSystemFormat());

        assertEquals(expectedPosition, actualPosition);

        assertTrue(StringUtils.isEmpty(formatter.fromObjectValue(null, formatter.getSystemFormat())));
    }

    /**
     * Tests getting the format for a given set of longitude values.
     */
    @Test
    public void testGetFormat()
    {
        PreferencesRegistry registry = LocationTestUtils.getPrefsRegistry();

        PositionFormatter formatter = new PositionFormatter(registry);

        String format = formatter.getFormat(New.list("39.9358858333 -105.3927469444"));

        assertEquals("Decimal", format);

        format = formatter.getFormat(New.list("39°56'9.189\"N 105°23'33.889\"W"));

        assertEquals("DMS", format);
    }

    /**
     * Tests get known possible formats.
     */
    @Test
    public void testGetKnownPossibleFormats()
    {
        PositionFormatter formatter = new PositionFormatter(null);
        Collection<String> knownFormats = formatter.getKnownPossibleFormats();

        assertTrue(knownFormats.contains(CoordFormat.DMS.toString()));
        assertTrue(knownFormats.contains(CoordFormat.DECIMAL.toString()));
    }

    /**
     * Tests get system format.
     */
    @Test
    public void testGetSystemFormat()
    {
        PositionFormatter formatter = new PositionFormatter(null);

        assertNull(formatter.getSystemFormat());
    }
}
