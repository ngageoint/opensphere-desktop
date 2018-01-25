package io.opensphere.core.model.time;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import io.opensphere.core.util.DateTimeFormats;

/** Tests for {@link TimeSpanFormatter}. */
public class TimeSpanFormatterTest
{
    /** Set up. */
    @Before
    public void setUp()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Tests {@link TimeSpanFormatter#toSmartString(TimeSpan)}.
     *
     * @exception ParseException if one of the times cannot be parsed.
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    @Test
    public void testToSmartStringTimeless() throws ParseException
    {
        // Timeless
        assertEquals("TIMELESS", TimeSpanFormatter.toSmartString(TimeSpan.TIMELESS));
    }

    /**
     * Tests {@link TimeSpanFormatter#toSmartString(TimeSpan)}.
     *
     * @exception ParseException if one of the times cannot be parsed.
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    @Test
    public void testToSmartStringZero() throws ParseException
    {
        // Zero
        assertEquals("ZERO", TimeSpanFormatter.toSmartString(TimeSpan.ZERO));
    }

    /**
     * Tests {@link TimeSpanFormatter#toSmartString(TimeSpan)}.
     *
     * @exception ParseException if one of the times cannot be parsed.
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    @Test
    public void testToSmartStringInstant() throws ParseException
    {
        // Instant
        assertEquals("2015-04-09 01:02:03", TimeSpanFormatter.toSmartString(span("2015-04-09 01:02:03")));
    }

    /**
     * Tests {@link TimeSpanFormatter#toSmartString(TimeSpan)}.
     *
     * @exception ParseException if one of the times cannot be parsed.
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    @Test
    public void testToSmartStringSpan() throws ParseException
    {
        // Span
        assertEquals("2015-04-09 01:02:03 - 2015-04-09 02:02:03",
                TimeSpanFormatter.toSmartString(span("2015-04-09 01:02:03", "2015-04-09 02:02:03")));
    }

    /**
     * Tests {@link TimeSpanFormatter#toSmartString(TimeSpan)}.
     *
     * @exception ParseException if one of the times cannot be parsed.
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    @Test
    public void testToSmartStringDayBoundary() throws ParseException
    {
        // Instant at day boundary
        assertEquals("2015-04-09 00:00:00", TimeSpanFormatter.toSmartString(span("2015-04-09")));
    }

    /**
     * Tests {@link TimeSpanFormatter#toSmartString(TimeSpan)}.
     *
     * @exception ParseException if one of the times cannot be parsed.
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    @Test
    public void testToSmartStringOneDay() throws ParseException
    {
        // 1 day
        assertEquals("2015-04-09", TimeSpanFormatter.toSmartString(span("2015-04-09", "2015-04-10")));
    }

    /**
     * Tests {@link TimeSpanFormatter#toSmartString(TimeSpan)}.
     *
     * @exception ParseException if one of the times cannot be parsed.
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    @Test
    public void testToSmartStringTwoDays() throws ParseException
    {
        // 2 days
        assertEquals("2015-04-09 - 2015-04-10", TimeSpanFormatter.toSmartString(span("2015-04-09", "2015-04-11")));
    }

    /**
     * Tests {@link TimeSpanFormatter#toSmartString(TimeSpan)}.
     *
     * @exception ParseException if one of the times cannot be parsed.
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    @Test
    public void testToSmartStringInstantMonthBoundary() throws ParseException
    {
        // Instant at month boundary
        assertEquals("2015-04-01 00:00:00", TimeSpanFormatter.toSmartString(span("2015-04-01")));
    }

    /**
     * Tests {@link TimeSpanFormatter#toSmartString(TimeSpan)}.
     *
     * @exception ParseException if one of the times cannot be parsed.
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    @Test
    public void testToSmartStringOneMonth() throws ParseException
    {
        // 1 month
        assertEquals("Apr 2015", TimeSpanFormatter.toSmartString(span("2015-04-01", "2015-05-01")));
    }

    /**
     * Tests {@link TimeSpanFormatter#toSmartString(TimeSpan)}.
     *
     * @exception ParseException if one of the times cannot be parsed.
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    @Test
    public void testToSmartStringTwoMonths() throws ParseException
    {
        // 2 months
        assertEquals("Apr 2015 - May 2015", TimeSpanFormatter.toSmartString(span("2015-04-01", "2015-06-01")));
    }

    /**
     * Creates a TimeSpan.
     *
     * @param start the start time string
     * @param end the end time string
     * @return the time span
     * @exception ParseException if one of the times cannot be parsed.
     */
    public static TimeSpan span(String start, String end) throws ParseException
    {
        return TimeSpan.get(parse(start), parse(end));
    }

    /**
     * Creates a TimeSpan.
     *
     * @param instant the instant time string
     * @return the time span
     * @exception ParseException if the time cannot be parsed.
     */
    public static TimeSpan span(String instant) throws ParseException
    {
        return TimeSpan.get(parse(instant));
    }

    /**
     * Creates a SimpleDateFormat for the given time.
     *
     * @param time the time string
     * @return the SimpleDateFormat
     * @exception ParseException if the time cannot be parsed.
     */
    private static Date parse(String time) throws ParseException
    {
        String pattern = time.length() == 10 ? DateTimeFormats.DATE_FORMAT : DateTimeFormats.DATE_TIME_FORMAT;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.parse(time);
    }
}
