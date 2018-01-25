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
import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.format.datetime.TimeFormatter;

/**
 * Test for the DateFormatter class.
 */
public class TimeFormatterTest
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

        TimeFormatter formatter = new TimeFormatter(registry);

        assertNull(formatter.formatCell("051920", null));
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

        String cellValue = "124810";
        String format = "HHmmss";

        TimeFormatter formatter = new TimeFormatter(registry);
        Date date = formatter.formatCell(cellValue, format);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        assertEquals(12, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(48, calendar.get(Calendar.MINUTE));
        assertEquals(10, calendar.get(Calendar.SECOND));

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

        String cellValue = "04:29:00";
        String format = "HH:mm:ss";

        TimeFormatter formatter = new TimeFormatter(registry);

        Calendar calendar = Calendar.getInstance();
        calendar.set(0, 0, 0, 4, 29, 0);

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

        String format = "HH:mm:ss";

        SimpleDateFormat simpleFormatter = new SimpleDateFormat(format);

        List<String> testData = New.list();

        long time = System.currentTimeMillis();
        for (int i = 0; i < 100; i++)
        {
            testData.add(simpleFormatter.format(new Date(time)));
            time += 1000;
        }

        support.replayAll();

        TimeFormatter formatter = new TimeFormatter(registry);
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

        TimeFormatter formatter = new TimeFormatter(registry);
        Collection<String> formats = formatter.getKnownPossibleFormats();

        Set<String> formatSet = New.set(formats);

        assertEquals(formatSet.size(), formats.size());

        int dateCount = 0;
        for (DateFormat aFormat : config.getFormats())
        {
            if (aFormat.getType() == Type.TIME)
            {
                dateCount++;
                assertTrue(formatSet.contains(aFormat.getSdf()));
            }
        }

        assertEquals(formatSet.size(), dateCount);
    }

    /**
     * Tests getting the system format.
     */
    @Test
    public void testGetSystemFormat()
    {
        StringBuilder formatStringBuilder = new StringBuilder("HH:mm:ss");

        for (int i = 0; i < 4; i++)
        {
            EasyMockSupport support = new EasyMockSupport();

            PreferencesRegistry registry = TestDateTimeUtils.createPreferencesRegistry(support, i);

            support.replayAll();

            TimeFormatter formatter = new TimeFormatter(registry);

            assertEquals(formatStringBuilder.toString(), formatter.getSystemFormat());

            support.verifyAll();

            if (i == 0)
            {
                formatStringBuilder.append('.');
            }

            formatStringBuilder.append('S');
        }
    }

    /**
     * Tests saving a new format.
     */
    @Test
    public void testSaveFormat()
    {
        EasyMockSupport support = new EasyMockSupport();

        String newFormat = "HHmmss";
        String dateFormat = "yyyyMMdd";
        String dateTimeFormat = "yyyyMMdd HHmmss";

        PreferencesRegistry prefsRegistry = TestDateTimeUtils.createSaveRegistry(support, newFormat, Type.TIME);

        support.replayAll();

        TimeFormatter formatter = new TimeFormatter(prefsRegistry);
        formatter.saveNewFormat(newFormat);
        formatter.saveNewFormat(newFormat);
        formatter.saveNewFormat(dateFormat);
        formatter.saveNewFormat(dateTimeFormat);

        support.verifyAll();
    }

    /**
     * Tests formatting time with lots of decimal seconds.
     *
     * @throws ParseException Bad parse.
     */
    @Test
    public void testLotsOfDecimalSeconds() throws ParseException
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesRegistry registry = TestDateTimeUtils.createPreferencesRegistry(support);

        support.replayAll();

        String format = "HH:mm:ss.SSS";
        String timeString = "13:10:10.123092838202";
        String expectedTime = "13:10:10.123";

        TimeFormatter formatter = new TimeFormatter(registry);
        Date date = formatter.formatCell(timeString, format);

        assertEquals(expectedTime, formatter.fromObjectValue(date, format));

        support.verifyAll();
    }
}
