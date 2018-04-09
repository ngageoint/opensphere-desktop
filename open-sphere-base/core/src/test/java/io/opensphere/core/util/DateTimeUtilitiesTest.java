package io.opensphere.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import org.junit.Assert;

/**
 * Test for {@link DateTimeUtilities}.
 */
public class DateTimeUtilitiesTest
{
    /**
     * Helper to create a Date.
     *
     * @param year The year
     * @param month The month (1-based)
     * @param day The day
     * @param hourOfDay The hourOfDay
     * @param minute The minute
     * @param second The second
     * @param millis The milliseconds
     * @return A Date
     */
    private static Date getDate(int year, int month, int day, int hourOfDay, int minute, int second, int millis)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, hourOfDay, minute, second);
        calendar.set(Calendar.MILLISECOND, millis);
        return calendar.getTime();
    }

    /**
     * Test for {@link DateTimeUtilities#fixMillis(String)}.
     *
     * @throws ParseException If parsing the test string fails.
     */
    @Test
    public void testFixMillis() throws ParseException
    {
        Assert.assertEquals("12:13:14.543", DateTimeUtilities.fixMillis("12:13:14.543"));
        Assert.assertEquals("12:13:14.543", DateTimeUtilities.fixMillis("12:13:14.5432"));
        Assert.assertEquals("12:13:14.988", DateTimeUtilities.fixMillis("12:13:14.9876"));
        Assert.assertEquals("12:13:14.988Z", DateTimeUtilities.fixMillis("12:13:14.9876Z"));
        Assert.assertEquals("a.123 b.568 c", DateTimeUtilities.fixMillis("a.123 b.56789 c"));
        Assert.assertEquals("12:13:2014", DateTimeUtilities.fixMillis("12:13:2014"));
    }

    /**
     * Test for {@link DateTimeUtilities#parse(java.text.DateFormat, String)}.
     *
     * @throws ParseException If a   parse exception occurs
     */
    @Test
    public void testParse() throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat(DateTimeFormats.DATE_TIME_MILLIS_FORMAT);
        Assert.assertEquals(getDate(2013, 4, 9, 13, 38, 15, 123), DateTimeUtilities.parse(format, "2013-04-09 13:38:15.123"));
        Assert.assertEquals(getDate(2013, 4, 9, 13, 38, 15, 123), DateTimeUtilities.parse(format, "2013-04-09 13:38:15.1234"));
        Assert.assertEquals(getDate(2013, 4, 9, 13, 38, 15, 124), DateTimeUtilities.parse(format, "2013-04-09 13:38:15.1235"));
        Assert.assertEquals(getDate(2013, 4, 9, 13, 38, 16, 0), DateTimeUtilities.parse(format, "2013-04-09 13:38:15.9999"));

        Assert.assertEquals(getDate(2013, 4, 9, 13, 38, 15, 120),
                DateTimeUtilities.parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS"), "2013-04-09 13:38:15.123"));
        Assert.assertEquals(getDate(2013, 4, 9, 13, 38, 15, 100),
                DateTimeUtilities.parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S"), "2013-04-09 13:38:15.123"));
        Assert.assertEquals(getDate(2013, 4, 9, 13, 38, 15, 200),
                DateTimeUtilities.parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S"), "2013-04-09 13:38:15.153"));
    }
}
