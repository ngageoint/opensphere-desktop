package io.opensphere.csvcommon.format.datetime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.common.configuration.date.DateFormatsConfig;
import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.DateTimeFormats;
import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.format.datetime.DateFormatter;

/**
 * Test for the DateFormatter class.
 */
public class DateFormatterTest
{
    /**
     * Tests when an empty format is passed.
     *
     * @throws ParseException Bad parse.
     */
    @Test
    public void testEmptyFormat() throws ParseException
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesRegistry registry = TestDateTimeUtils.createPreferencesRegistry(support);

        support.replayAll();

        DateFormatter formatter = new DateFormatter(registry);

        assertNull(formatter.formatCell("05192014", null));
        assertNull(formatter.fromObjectValue(null, null));

        support.verifyAll();
    }

    /**
     * Tests the format cell method.
     *
     * @throws ParseException Bad parse.
     */
    @Test
    public void testFormatCell() throws ParseException
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesRegistry registry = TestDateTimeUtils.createPreferencesRegistry(support);

        support.replayAll();

        String cellValue = "20140429";
        String format = "yyyyMMdd";

        DateFormatter formatter = new DateFormatter(registry);
        Date date = formatter.formatCell(cellValue, format);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        assertEquals(2014, calendar.get(Calendar.YEAR));
        assertEquals(3, calendar.get(Calendar.MONTH));
        assertEquals(29, calendar.get(Calendar.DAY_OF_MONTH));

        support.verifyAll();
    }

    /**
     * Test formatting a date value to a string.
     */
    @Test
    public void testFormatObjectValue()
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesRegistry registry = TestDateTimeUtils.createPreferencesRegistry(support);

        support.replayAll();

        String cellValue = "04-29-2014";
        String format = "MM-dd-yyyy";

        DateFormatter formatter = new DateFormatter(registry);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, 3, 29);

        String actualValue = formatter.fromObjectValue(calendar.getTime(), format);

        assertEquals(cellValue, actualValue);

        support.verifyAll();
    }

    /**
     * Tests getting the format.
     */
    @Test
    public void testGetFormat()
    {
        DateFormatsConfig config = TestDateTimeUtils.getConfiguration();

        EasyMockSupport support = new EasyMockSupport();

        PreferencesRegistry registry = TestDateTimeUtils.createPreferencesRegistry(support, config);

        String format = "yyyyMMdd";

        SimpleDateFormat simpleFormatter = new SimpleDateFormat(format);

        List<String> testData = New.list();

        long time = System.currentTimeMillis();
        for (int i = 0; i < 100; i++)
        {
            testData.add(simpleFormatter.format(new Date(time)));
            time += 1000;
        }

        support.replayAll();

        DateFormatter formatter = new DateFormatter(registry);
        String actual = formatter.getFormat(testData);

        assertEquals(format, actual);

        support.verifyAll();
    }

    /**
     * Tests get the known possible formats.
     */
    @Test
    public void testGetKnownPossibleFormats()
    {
        EasyMockSupport support = new EasyMockSupport();

        DateFormatsConfig config = TestDateTimeUtils.getConfiguration();
        PreferencesRegistry registry = TestDateTimeUtils.createPreferencesRegistry(support, config);

        support.replayAll();

        DateFormatter formatter = new DateFormatter(registry);
        Collection<String> formats = formatter.getKnownPossibleFormats();

        Set<String> formatSet = New.set(formats);

        assertEquals(formatSet.size(), formats.size());

        int dateCount = 0;
        for (DateFormat aFormat : config.getFormats())
        {
            if (aFormat.getType() == Type.DATE)
            {
                dateCount++;
                assertTrue(formatSet.contains(aFormat.getSdf()));
            }
        }

        assertEquals(formatSet.size(), dateCount);

        support.verifyAll();
    }

    /**
     * Tests getting the system format.
     */
    @Test
    public void testGetSystemFormat()
    {
        for (int i = 0; i < 4; i++)
        {
            EasyMockSupport support = new EasyMockSupport();

            PreferencesRegistry registry = TestDateTimeUtils.createPreferencesRegistry(support, i);

            support.replayAll();

            DateFormatter formatter = new DateFormatter(registry);

            assertEquals(DateTimeFormats.DATE_FORMAT, formatter.getSystemFormat());

            support.verifyAll();
        }
    }

    /**
     * Tests saving a new format.
     */
    @Test
    public void testSaveFormat()
    {
        EasyMockSupport support = new EasyMockSupport();

        String newFormat = "yyyyMMdd";
        String timeStampFormat = "yyyyHH";
        String timeFormat = "HH";

        PreferencesRegistry prefsRegistry = TestDateTimeUtils.createSaveRegistry(support, newFormat, Type.DATE);

        support.replayAll();

        DateFormatter formatter = new DateFormatter(prefsRegistry);
        formatter.saveNewFormat(newFormat);
        formatter.saveNewFormat(newFormat);
        formatter.saveNewFormat(timeStampFormat);
        formatter.saveNewFormat(timeFormat);

        support.verifyAll();
    }
}
