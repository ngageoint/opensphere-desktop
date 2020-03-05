package io.opensphere.kml.common.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.opensphere.core.util.LongSupplier;

/**
 * JUnit test for KMLConversionUtils.
 */
public class KMLSpatialTemporalUtilsTest
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
     * @return A Date
     */
    private static Date getDate(int year, int month, int day, int hourOfDay, int minute, int second)
    {
        return getDate(year, month, day, hourOfDay, minute, second, 0);
    }

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
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.set(year, month - 1, day, hourOfDay, minute, second);
        calendar.set(Calendar.MILLISECOND, millis);
        return calendar.getTime();
    }

    /**
     * Set up.
     *
     * @throws Exception If an exception occurs
     */
    @Before
    public void setUp() throws Exception
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Test method for
     * {@link io.opensphere.kml.common.util.KMLSpatialTemporalUtils#getExpiresHeaderTime(java.lang.String)}
     * .
     */
    @Test
    public void testExpiresHeaderTime()
    {
        Date expected = getDate(2013, 5, 6, 20, 31, 32, 0);
        Date actual;

        actual = KMLSpatialTemporalUtils.getExpiresHeaderTime("Mon, 06 May 2013 20:31:32 GMT");
        Assert.assertEquals(expected, actual);

        actual = KMLSpatialTemporalUtils.getExpiresHeaderTime("Mon, 06 May 2013 20:31:32");
        Assert.assertEquals(expected, actual);

        actual = KMLSpatialTemporalUtils.getExpiresHeaderTime("Mon, 06 May 2013 14:31:32 MDT");
        Assert.assertEquals(expected, actual);
    }

    /**
     * Test method for
     * {@link io.opensphere.kml.common.util.KMLSpatialTemporalUtils#getExpireTime(java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testExpiresTime()
    {
        SimpleDateFormat kmlFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        kmlFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        SimpleDateFormat rfc1123Format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        rfc1123Format.setTimeZone(TimeZone.getTimeZone("GMT"));

        Date expected;
        Date actual;

        expected = new Date(System.currentTimeMillis() / 1000L * 1000L + 120000L);
        actual = KMLSpatialTemporalUtils.getExpireTime(kmlFormat.format(expected), null, null);
        Assert.assertEquals(expected, actual);

        expected = new Date(System.currentTimeMillis() + 120000L);
        actual = KMLSpatialTemporalUtils.getExpireTime("blah", "max-age=120", null);
        long delta = Math.abs(actual.getTime() - expected.getTime());
        Assert.assertTrue(delta < 100L);

        expected = new Date(System.currentTimeMillis() / 1000L * 1000L + 120000L);
        actual = KMLSpatialTemporalUtils.getExpireTime("blah", "crap", rfc1123Format.format(expected));
        Assert.assertEquals(expected, actual);
    }

    /**
     * Test method for
     * {@link io.opensphere.kml.common.util.KMLSpatialTemporalUtils#parseDateString(java.lang.String)}
     * .
     */
    @Test
    public void testFullLocalTime()
    {
        Date actual = KMLSpatialTemporalUtils.parseDateString("2011-01-05T07:00:00+03:00");
        Date expected = getDate(2011, 1, 5, 4, 0, 0);
        Assert.assertEquals(expected, actual);
    }

    /**
     * Test method for
     * {@link io.opensphere.kml.common.util.KMLSpatialTemporalUtils#parseDateString(java.lang.String)}
     * .
     */
    @Test
    public void testFullZulu()
    {
        Date actual = KMLSpatialTemporalUtils.parseDateString("2011-01-04T06:00:00Z");
        Date expected = getDate(2011, 1, 4, 6, 0, 0);
        Assert.assertEquals(expected, actual);
    }

    /**
     * Test method for
     * {@link io.opensphere.kml.common.util.KMLSpatialTemporalUtils#isExpireTimeValid(java.util.Date)}
     * .
     */
    @Test
    public void testIsExpireTimeValid()
    {
        long now = System.currentTimeMillis();
        Assert.assertFalse(KMLSpatialTemporalUtils.isExpireTimeValid(null));
        Assert.assertFalse(KMLSpatialTemporalUtils.isExpireTimeValid(new Date(0L)));
        Assert.assertFalse(KMLSpatialTemporalUtils.isExpireTimeValid(new Date(now - 1000L)));
        Assert.assertTrue(KMLSpatialTemporalUtils.isExpireTimeValid(new Date(now + 1000L)));
        Assert.assertTrue(KMLSpatialTemporalUtils.isExpireTimeValid(new Date(now + 364L * 86400L * 1000L)));
        Assert.assertFalse(KMLSpatialTemporalUtils.isExpireTimeValid(new Date(now + 366L * 86400L * 1000L)));
    }

    /**
     * Test method for
     * {@link io.opensphere.kml.common.util.KMLSpatialTemporalUtils#getMaxAgeHeaderTime(java.lang.String, LongSupplier)}
     * .
     */
    @Test
    public void testMaxAgeHeaderTime()
    {
        final long now = System.currentTimeMillis();
        Date expected = new Date(now + 120000L);
        LongSupplier nowSupplier = new LongSupplier()
        {
            @Override
            public long get()
            {
                return now;
            }
        };
        Date actual = KMLSpatialTemporalUtils.getMaxAgeHeaderTime("blah, max-age=120, foo", nowSupplier);
        Assert.assertEquals(actual, expected);

        Assert.assertNull(KMLSpatialTemporalUtils.getMaxAgeHeaderTime("blah, max-age=0, foo", nowSupplier));
        Assert.assertNull(KMLSpatialTemporalUtils.getMaxAgeHeaderTime("blah, max-age=, foo", nowSupplier));
        Assert.assertNull(KMLSpatialTemporalUtils.getMaxAgeHeaderTime("blah, foo", nowSupplier));
        Assert.assertNull(KMLSpatialTemporalUtils.getMaxAgeHeaderTime(null, nowSupplier));
    }

    /**
     * Test method for
     * {@link io.opensphere.kml.common.util.KMLSpatialTemporalUtils#parseDateString(java.lang.String)}
     * .
     */
    @Test
    public void testMillis()
    {
        Date actual = KMLSpatialTemporalUtils.parseDateString("2011-01-04T06:00:00.381406Z");
        Date expected = getDate(2011, 1, 4, 6, 0, 0, 381);
        Assert.assertEquals(expected, actual);
    }

    /**
     * Test method for
     * {@link io.opensphere.kml.common.util.KMLSpatialTemporalUtils#parseDateString(java.lang.String)}
     * .
     */
    @Test
    public void testMillisFullLocalTime()
    {
        Date actual = KMLSpatialTemporalUtils.parseDateString("2011-01-05T07:00:00.381406+03:00");
        Date expected = getDate(2011, 1, 5, 4, 0, 0, 381);
        Assert.assertEquals(expected, actual);
    }

    /**
     * Test method for
     * {@link io.opensphere.kml.common.util.KMLSpatialTemporalUtils#parseDateString(java.lang.String)}
     * .
     */
    @Test
    public void testYear()
    {
        Date actual = KMLSpatialTemporalUtils.parseDateString("2011");
        Date expected = getDate(2011, 1, 1, 0, 0, 0);
        Assert.assertEquals(expected, actual);
    }

    /**
     * Test method for
     * {@link io.opensphere.kml.common.util.KMLSpatialTemporalUtils#parseDateString(java.lang.String)}
     * .
     */
    @Test
    public void testYearMonth()
    {
        Date actual = KMLSpatialTemporalUtils.parseDateString("2011-02");
        Date expected = getDate(2011, 2, 1, 0, 0, 0);
        Assert.assertEquals(expected, actual);
    }

    /**
     * Test method for
     * {@link io.opensphere.kml.common.util.KMLSpatialTemporalUtils#parseDateString(java.lang.String)}
     * .
     */
    @Test
    public void testYearMonthDay()
    {
        Date actual = KMLSpatialTemporalUtils.parseDateString("2011-02-03");
        Date expected = getDate(2011, 2, 3, 0, 0, 0);
        Assert.assertEquals(expected, actual);
    }
}
