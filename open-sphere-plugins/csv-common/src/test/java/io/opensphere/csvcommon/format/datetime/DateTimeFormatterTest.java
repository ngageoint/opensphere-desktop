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
import io.opensphere.csvcommon.format.datetime.DateTimeFormatter;

/**
 * Test for the DateFormatter class.
 */
public class DateTimeFormatterTest
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

        DateTimeFormatter formatter = new DateTimeFormatter(registry);

        assertNull(formatter.formatCell("05-19-2014 12:06", null));
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

        String cellValue = "20140429 124810";
        String format = "yyyyMMdd HHmmss";

        DateTimeFormatter formatter = new DateTimeFormatter(registry);
        Date date = formatter.formatCell(cellValue, format);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        assertEquals(2014, calendar.get(Calendar.YEAR));
        assertEquals(3, calendar.get(Calendar.MONTH));
        assertEquals(29, calendar.get(Calendar.DAY_OF_MONTH));

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

        String cellValue = "04-29-2014 04:29:00";
        String format = "MM-dd-yyyy HH:mm:ss";

        DateTimeFormatter formatter = new DateTimeFormatter(registry);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, 3, 29, 4, 29, 0);

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

        Set<String> formats = New.set("yyyy-M-d HH:mm:ss", "yyyy-MM-dd HH:mm:ss");

        SimpleDateFormat simpleFormatter = new SimpleDateFormat(formats.iterator().next());

        List<String> testData = New.list();

        long time = System.currentTimeMillis();
        for (int i = 0; i < 100; i++)
        {
            testData.add(simpleFormatter.format(new Date(time)));
            time += 1000;
        }

        support.replayAll();

        DateTimeFormatter formatter = new DateTimeFormatter(registry);
        String actual = formatter.getFormat(testData);

        assertTrue(formats.contains(actual));

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

        DateTimeFormatter formatter = new DateTimeFormatter(registry);
        Collection<String> formats = formatter.getKnownPossibleFormats();

        Set<String> formatSet = New.set(formats);

        assertEquals(formatSet.size(), formats.size());

        Set<String> actuals = New.set();
        for (DateFormat aFormat : config.getFormats())
        {
            if (aFormat.getType() == Type.TIMESTAMP)
            {
                assertTrue(formatSet.contains(aFormat.getSdf()));
                actuals.add(aFormat.getSdf());
            }
        }

        assertEquals(formatSet.size(), actuals.size());

        support.verifyAll();
    }

    /**
     * Tests saving a new format.
     */
    @Test
    public void testSaveFormat()
    {
        EasyMockSupport support = new EasyMockSupport();

        String newFormat = "yyyyMMdd HHmmss";
        String dateFormat = "yyyyMMdd";
        String timeFormat = "HHmmss";

        PreferencesRegistry prefsRegistry = TestDateTimeUtils.createSaveRegistry(support, newFormat, Type.TIMESTAMP);
        support.replayAll();

        DateTimeFormatter formatter = new DateTimeFormatter(prefsRegistry);
        formatter.saveNewFormat(newFormat);
        formatter.saveNewFormat(newFormat);
        formatter.saveNewFormat(dateFormat);
        formatter.saveNewFormat(timeFormat);

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

        String format = "yyyy:M:d::HH:mm:ss.SSS";
        String timeString = "2014:05:22::13:10:10.123092838202";
        String expectedTime = "2014:5:22::13:10:10.123";

        DateTimeFormatter formatter = new DateTimeFormatter(registry);
        Date date = formatter.formatCell(timeString, format);

        assertEquals(expectedTime, formatter.fromObjectValue(date, format));

        format = "yyyy:M:d::HH:mm:ss.SSS'Z'";
        timeString = "2014:05:22::13:10:10.123092838202Z";
        expectedTime = "2014:5:22::13:10:10.123Z";

        formatter = new DateTimeFormatter(registry);
        date = formatter.formatCell(timeString, format);

        assertEquals(expectedTime, formatter.fromObjectValue(date, format));

        support.verifyAll();
    }
}
