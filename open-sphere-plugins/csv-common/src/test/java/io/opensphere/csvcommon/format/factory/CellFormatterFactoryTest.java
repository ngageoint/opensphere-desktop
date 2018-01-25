package io.opensphere.csvcommon.format.factory;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.csvcommon.format.CellFormatter;
import io.opensphere.csvcommon.format.datetime.DateFormatter;
import io.opensphere.csvcommon.format.datetime.DateTimeFormatter;
import io.opensphere.csvcommon.format.datetime.TestDateTimeUtils;
import io.opensphere.csvcommon.format.datetime.TimeFormatter;
import io.opensphere.csvcommon.format.factory.CellFormatterFactory;
import io.opensphere.csvcommon.format.position.LatitudeFormatter;
import io.opensphere.csvcommon.format.position.LongitudeFormatter;
import io.opensphere.csvcommon.format.position.PositionFormatter;
import io.opensphere.importer.config.ColumnType;

/**
 * Tests the CellFormatterFactory class.
 *
 */
public class CellFormatterFactoryTest
{
    /**
     * Tests creating date time formatters.
     */
    @Test
    public void testCreateFormatterDateTime()
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesRegistry registry = TestDateTimeUtils.createPreferencesRegistry(support);

        support.replayAll();

        CellFormatterFactory factory = new CellFormatterFactory();

        CellFormatter formatter = factory.getFormatter(ColumnType.DATE, registry);
        assertTrue(formatter instanceof DateFormatter);

        formatter = factory.getFormatter(ColumnType.TIME, registry);
        assertTrue(formatter instanceof TimeFormatter);

        formatter = factory.getFormatter(ColumnType.TIMESTAMP, registry);
        assertTrue(formatter instanceof DateTimeFormatter);

        formatter = factory.getFormatter(ColumnType.LAT, registry);
        assertTrue(formatter instanceof LatitudeFormatter);

        formatter = factory.getFormatter(ColumnType.LON, registry);
        assertTrue(formatter instanceof LongitudeFormatter);

        formatter = factory.getFormatter(ColumnType.POSITION, registry);
        assertTrue(formatter instanceof PositionFormatter);

        support.verifyAll();
    }

    /**
     * Tests unknown column result.
     */
    @Test
    public void testCreateFormatterUnknown()
    {
        CellFormatterFactory factory = new CellFormatterFactory();
        CellFormatter nullFormatter = factory.getFormatter(ColumnType.OTHER, null);

        assertNull(nullFormatter);
    }
}
