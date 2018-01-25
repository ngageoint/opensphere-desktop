package io.opensphere.core.util.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Test;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanFormatterTest;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Hours;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.units.duration.Minutes;
import io.opensphere.core.units.duration.Months;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.units.duration.Weeks;
import io.opensphere.core.units.duration.Years;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.DateTimeFormats;
import io.opensphere.core.util.collections.New;
import org.junit.Assert;

/**
 * Tests for {@link TimelineUtilities}.
 */
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.GodClass" })
public class TimelineUtilitiesTest
{
    static
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Test for {@link TimelineUtilities#createDayDateEntries(TimeSpan, Map)}.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testCreateDayDateEntries() throws ParseException
    {
        Date start = getDate("2010-02-03 00:00:00.000");
        Date end = getDate("2010-02-04 00:00:00.000");

        Map<Duration, List<DateDurationKey>> map = New.map();
        TimeSpan span = TimeSpan.get(start, end);
        List<DateDurationKey> actual = TimelineUtilities.createDayDateEntries(span, map);

        Assert.assertEquals(1, actual.size());
        Assert.assertEquals(start, actual.get(0).getStartDate());
        Assert.assertEquals(end, actual.get(0).getEndDate());
        Assert.assertEquals(Days.ONE, actual.get(0).getDuration());

        start = getDate("2010-02-02 23:59:59.999");
        end = getDate("2010-02-04 00:00:00.001");

        span = TimeSpan.get(start, end);
        actual = TimelineUtilities.createDayDateEntries(span, map);

        Assert.assertEquals(3, actual.size());
        Assert.assertEquals(getDate("2010-02-02 00:00:00.000"), actual.get(0).getStartDate());
        Assert.assertEquals(getDate("2010-02-03 00:00:00.000"), actual.get(0).getEndDate());
        Assert.assertEquals(Days.ONE, actual.get(0).getDuration());
        Assert.assertEquals(getDate("2010-02-03 00:00:00.000"), actual.get(1).getStartDate());
        Assert.assertEquals(getDate("2010-02-04 00:00:00.000"), actual.get(1).getEndDate());
        Assert.assertEquals(Days.ONE, actual.get(1).getDuration());
        Assert.assertEquals(getDate("2010-02-04 00:00:00.000"), actual.get(2).getStartDate());
        Assert.assertEquals(getDate("2010-02-05 00:00:00.000"), actual.get(2).getEndDate());
        Assert.assertEquals(Days.ONE, actual.get(2).getDuration());
    }

    /**
     * Test for
     * {@link TimelineUtilities#createMonthDateEntries(TimeSpan, Duration)}.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testCreateMonthDateEntries() throws ParseException
    {
        Date start = getDate("2010-02-03 00:00:00.000");
        Date end = getDate("2010-03-01 00:00:00.000");

        TimeSpan span = TimeSpan.get(start, end);
        Map<Duration, List<DateDurationKey>> map = TimelineUtilities.createMonthDateEntries(span, Months.ONE);
        List<DateDurationKey> actual = map.get(Months.ONE);

        Assert.assertEquals(1, actual.size());
        Assert.assertEquals(getDate("2010-02-01 00:00:00.000"), actual.get(0).getStartDate());
        Assert.assertEquals(getDate("2010-03-01 00:00:00.000"), actual.get(0).getEndDate());
        Assert.assertEquals(Months.ONE, actual.get(0).getDuration());

        actual = map.get(Weeks.ONE);

        Assert.assertEquals(5, actual.size());
        Assert.assertEquals(getDate("2010-01-31 00:00:00.000"), actual.get(0).getStartDate());
        Assert.assertEquals(getDate("2010-02-07 00:00:00.000"), actual.get(0).getEndDate());
        Assert.assertEquals(Weeks.ONE, actual.get(0).getDuration());
        Assert.assertEquals(getDate("2010-02-07 00:00:00.000"), actual.get(1).getStartDate());
        Assert.assertEquals(getDate("2010-02-14 00:00:00.000"), actual.get(1).getEndDate());
        Assert.assertEquals(Weeks.ONE, actual.get(1).getDuration());
        Assert.assertEquals(getDate("2010-02-14 00:00:00.000"), actual.get(2).getStartDate());
        Assert.assertEquals(getDate("2010-02-21 00:00:00.000"), actual.get(2).getEndDate());
        Assert.assertEquals(Weeks.ONE, actual.get(2).getDuration());
        Assert.assertEquals(getDate("2010-02-21 00:00:00.000"), actual.get(3).getStartDate());
        Assert.assertEquals(getDate("2010-02-28 00:00:00.000"), actual.get(3).getEndDate());
        Assert.assertEquals(Weeks.ONE, actual.get(3).getDuration());
        Assert.assertEquals(getDate("2010-02-28 00:00:00.000"), actual.get(4).getStartDate());
        Assert.assertEquals(getDate("2010-03-07 00:00:00.000"), actual.get(4).getEndDate());
        Assert.assertEquals(Weeks.ONE, actual.get(4).getDuration());

        actual = map.get(Days.ONE);

        Assert.assertEquals(35, actual.size());
        long jan31 = getDate("2010-01-31 00:00:00.000").getTime();
        for (int index = 0; index < 35; ++index)
        {
            Assert.assertEquals(jan31 + index * (long)Constants.MILLIS_PER_DAY, actual.get(index).getStartDate().getTime());
            Assert.assertEquals(jan31 + (index + 1) * (long)Constants.MILLIS_PER_DAY, actual.get(index).getEndDate().getTime());
            Assert.assertEquals(Days.ONE, actual.get(index).getDuration());
        }

        start = getDate("2010-01-31 23:59:59.999");
        end = getDate("2010-03-01 00:00:00.001");

        span = TimeSpan.get(start, end);
        map = TimelineUtilities.createMonthDateEntries(span, Months.ONE);
        actual = map.get(Months.ONE);

        Assert.assertEquals(3, actual.size());
        Assert.assertEquals(getDate("2010-01-01 00:00:00.000"), actual.get(0).getStartDate());
        Assert.assertEquals(getDate("2010-02-01 00:00:00.000"), actual.get(0).getEndDate());
        Assert.assertEquals(Months.ONE, actual.get(0).getDuration());
        Assert.assertEquals(getDate("2010-02-01 00:00:00.000"), actual.get(1).getStartDate());
        Assert.assertEquals(getDate("2010-03-01 00:00:00.000"), actual.get(1).getEndDate());
        Assert.assertEquals(Months.ONE, actual.get(1).getDuration());
        Assert.assertEquals(getDate("2010-03-01 00:00:00.000"), actual.get(2).getStartDate());
        Assert.assertEquals(getDate("2010-04-01 00:00:00.000"), actual.get(2).getEndDate());
        Assert.assertEquals(Months.ONE, actual.get(2).getDuration());
    }

    /**
     * Test for
     * {@link TimelineUtilities#createWeekDateEntries(TimeSpan, Map, Duration)}.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testCreateWeekDateEntries() throws ParseException
    {
        Date start = getDate("2010-02-10 00:00:00.000");
        Date end = getDate("2010-03-01 00:00:00.000");

        TimeSpan span = TimeSpan.get(start, end);
        Map<Duration, List<DateDurationKey>> map = New.map();
        TimelineUtilities.createWeekDateEntries(span, map, Weeks.ONE);
        List<DateDurationKey> actual = map.get(Months.ONE);
        Assert.assertNull(actual);

        actual = map.get(Weeks.ONE);

        Assert.assertEquals(4, actual.size());
        Assert.assertEquals(getDate("2010-02-07 00:00:00.000"), actual.get(0).getStartDate());
        Assert.assertEquals(getDate("2010-02-14 00:00:00.000"), actual.get(0).getEndDate());
        Assert.assertEquals(Weeks.ONE, actual.get(1).getDuration());
        Assert.assertEquals(getDate("2010-02-14 00:00:00.000"), actual.get(1).getStartDate());
        Assert.assertEquals(getDate("2010-02-21 00:00:00.000"), actual.get(1).getEndDate());
        Assert.assertEquals(Weeks.ONE, actual.get(2).getDuration());
        Assert.assertEquals(getDate("2010-02-21 00:00:00.000"), actual.get(2).getStartDate());
        Assert.assertEquals(getDate("2010-02-28 00:00:00.000"), actual.get(2).getEndDate());
        Assert.assertEquals(Weeks.ONE, actual.get(3).getDuration());
        Assert.assertEquals(getDate("2010-02-28 00:00:00.000"), actual.get(3).getStartDate());
        Assert.assertEquals(getDate("2010-03-07 00:00:00.000"), actual.get(3).getEndDate());
        Assert.assertEquals(Weeks.ONE, actual.get(3).getDuration());

        actual = map.get(Days.ONE);

        Assert.assertEquals(28, actual.size());
        long feb7 = getDate("2010-02-07 00:00:00.000").getTime();
        for (int index = 0; index < 28; ++index)
        {
            Assert.assertEquals(feb7 + index * (long)Constants.MILLIS_PER_DAY, actual.get(index).getStartDate().getTime());
            Assert.assertEquals(feb7 + (index + 1) * (long)Constants.MILLIS_PER_DAY, actual.get(index).getEndDate().getTime());
            Assert.assertEquals(Days.ONE, actual.get(index).getDuration());
        }
    }

    /**
     * Test for {@link TimelineUtilities#get0000()}.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testGet0000() throws ParseException
    {
        Date actual = TimelineUtilities.get0000().getTime();

        Date expected = getDate(new SimpleDateFormat(DateTimeFormats.DATE_FORMAT).format(new Date()) + " 00:00:00.000");
        Assert.assertEquals(expected, actual);
    }

    /**
     * Test for {@link TimelineUtilities#get0000(Date)}.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testGet0000Date() throws ParseException
    {
        Date input = getDate("2010-02-03 04:05:43.123");
        Date actual = TimelineUtilities.get0000(input).getTime();

        Date expected = getDate("2010-02-03 00:00:00.000");
        Assert.assertEquals(expected, actual);
    }

    /**
     * Test for {@link TimelineUtilities#getEndCalendar(TimeSpan)}.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testGetEndCalendar() throws ParseException
    {
        Date start = getDate("2010-02-03 04:05:43.123");
        Date end;
        Date actual;

        end = getDate("2010-02-04 00:00:00.001");
        actual = TimelineUtilities.getEndCalendar(TimeSpan.get(start, end)).getTime();
        Assert.assertEquals(getDate("2010-02-05 00:00:00.000"), actual);

        end = getDate("2010-02-04 00:00:00.000");
        actual = TimelineUtilities.getEndCalendar(TimeSpan.get(start, end)).getTime();
        Assert.assertEquals(getDate("2010-02-04 00:00:00.000"), actual);
    }

    /**
     * Test for
     * {@link TimelineUtilities#getIntervalsForSpan(Date, TimeSpan, int)}.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testGetIntervalsForSpan() throws ParseException
    {
        GregorianCalendar start = new GregorianCalendar(2013, 9, 1);
        GregorianCalendar end = new GregorianCalendar(2014, 0, 1);
        TimeSpan span = TimeSpan.get(start.getTimeInMillis(), end.getTimeInMillis());

        List<TimeSpan> months = TimelineUtilities.getIntervalsForSpan(start.getTime(), span, Calendar.MONTH);
        Assert.assertEquals(3, months.size());
        Assert.assertEquals(months.get(0).getStart(), new GregorianCalendar(2013, 9, 1).getTimeInMillis());
        Assert.assertEquals(months.get(0).getEnd(), new GregorianCalendar(2013, 10, 1).getTimeInMillis());
        Assert.assertEquals(months.get(1).getStart(), new GregorianCalendar(2013, 10, 1).getTimeInMillis());
        Assert.assertEquals(months.get(1).getEnd(), new GregorianCalendar(2013, 11, 1).getTimeInMillis());
        Assert.assertEquals(months.get(2).getStart(), new GregorianCalendar(2013, 11, 1).getTimeInMillis());
        Assert.assertEquals(months.get(2).getEnd(), new GregorianCalendar(2014, 0, 1).getTimeInMillis());

        Date weekStart = TimelineUtilities.getThisWeek(start.getTime()).getStartDate();
        List<TimeSpan> weeks = TimelineUtilities.getIntervalsForSpan(weekStart, span, Calendar.WEEK_OF_YEAR);
        Assert.assertEquals(14, weeks.size());
        // just check the first two and last two intervals.
        Assert.assertEquals(weeks.get(0).getStart(), new GregorianCalendar(2013, 8, 29).getTimeInMillis());
        Assert.assertEquals(weeks.get(0).getEnd(), new GregorianCalendar(2013, 9, 6).getTimeInMillis());
        Assert.assertEquals(weeks.get(1).getStart(), new GregorianCalendar(2013, 9, 6).getTimeInMillis());
        Assert.assertEquals(weeks.get(1).getEnd(), new GregorianCalendar(2013, 9, 13).getTimeInMillis());
        Assert.assertEquals(weeks.get(12).getStart(), new GregorianCalendar(2013, 11, 22).getTimeInMillis());
        Assert.assertEquals(weeks.get(12).getEnd(), new GregorianCalendar(2013, 11, 29).getTimeInMillis());
        Assert.assertEquals(weeks.get(13).getStart(), new GregorianCalendar(2013, 11, 29).getTimeInMillis());
        Assert.assertEquals(weeks.get(13).getEnd(), new GregorianCalendar(2014, 0, 5).getTimeInMillis());

        List<TimeSpan> days = TimelineUtilities.getIntervalsForSpan(start.getTime(), span, Calendar.DAY_OF_YEAR);
        Assert.assertEquals(92, days.size());
        // just check the first two and last two intervals.
        Assert.assertEquals(days.get(0).getStart(), new GregorianCalendar(2013, 9, 1).getTimeInMillis());
        Assert.assertEquals(days.get(0).getEnd(), new GregorianCalendar(2013, 9, 2).getTimeInMillis());
        Assert.assertEquals(days.get(1).getStart(), new GregorianCalendar(2013, 9, 2).getTimeInMillis());
        Assert.assertEquals(days.get(1).getEnd(), new GregorianCalendar(2013, 9, 3).getTimeInMillis());
        Assert.assertEquals(days.get(90).getStart(), new GregorianCalendar(2013, 11, 30).getTimeInMillis());
        Assert.assertEquals(days.get(90).getEnd(), new GregorianCalendar(2013, 11, 31).getTimeInMillis());
        Assert.assertEquals(days.get(91).getStart(), new GregorianCalendar(2013, 11, 31).getTimeInMillis());
        Assert.assertEquals(days.get(91).getEnd(), new GregorianCalendar(2014, 0, 1).getTimeInMillis());
    }

    /**
     * Test for {@link TimelineUtilities#getNext0000(Date)}.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testGetNext0000Date() throws ParseException
    {
        Assert.assertEquals(getDate("2010-02-04 00:00:00.000"),
                TimelineUtilities.getNext0000(getDate("2010-02-03 04:05:43.123")).getTime());
        Assert.assertEquals(getDate("2010-02-04 00:00:00.000"),
                TimelineUtilities.getNext0000(getDate("2010-02-03 00:00:00.000")).getTime());
        Assert.assertEquals(getDate("2010-02-04 00:00:00.000"),
                TimelineUtilities.getNext0000(getDate("2010-02-03 23:59:59.999")).getTime());
    }

    /**
     * Test for {@link TimelineUtilities#getPartialWeek()}.
     */
    @Test
    public void testGetPartialWeek()
    {
        TimeSpan thisWeek = TimelineUtilities.getPartialWeek();
        Assert.assertTrue(TimelineUtilities.isAt0000(thisWeek.getStartDate()));
        Assert.assertTrue(thisWeek.getStart() < System.currentTimeMillis());
        Assert.assertTrue(System.currentTimeMillis() - thisWeek.getStart() < Constants.MILLIS_PER_WEEK);
        Assert.assertTrue(TimelineUtilities.isAt0000(thisWeek.getEndDate()));
        Assert.assertTrue(thisWeek.getEnd() > System.currentTimeMillis());
        Assert.assertTrue(System.currentTimeMillis() - thisWeek.getEnd() < Constants.MILLIS_PER_DAY);

        Calendar cal = Calendar.getInstance();
        cal.setTime(thisWeek.getStartDate());
        Assert.assertEquals(Calendar.SUNDAY, cal.get(Calendar.DAY_OF_WEEK));
    }

    /**
     * Test for {@link TimelineUtilities#getPrecedingMonths(Date, int, boolean)}
     * .
     */
    @Test
    public void testGetPrecedingMonths()
    {
        final Calendar cal = Calendar.getInstance();

        for (int off = 1; off < 5; ++off)
        {
            final int offset = off;
            TestRunnable r = new TestRunnable()
            {
                @Override
                public void run(int year, int month, int day)
                {
                    cal.set(year, month, day);
                    Date reference = cal.getTime();

                    TimeSpan actual;

                    // Test excluding the reference month.
                    actual = TimelineUtilities.getPrecedingMonths(reference, offset, false);
                    cal.setTime(actual.getStartDate());
                    int expectedStartMonth = month - offset;
                    if (expectedStartMonth < 0)
                    {
                        expectedStartMonth += Constants.MONTHS_PER_YEAR;
                    }
                    Assert.assertEquals(expectedStartMonth, cal.get(Calendar.MONTH));
                    Assert.assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
                    Assert.assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
                    Assert.assertEquals(0, cal.get(Calendar.MINUTE));
                    Assert.assertEquals(0, cal.get(Calendar.SECOND));
                    Assert.assertEquals(0, cal.get(Calendar.MILLISECOND));
                    cal.setTime(actual.getEndDate());
                    Assert.assertEquals(month, cal.get(Calendar.MONTH));
                    Assert.assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
                    Assert.assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
                    Assert.assertEquals(0, cal.get(Calendar.MINUTE));
                    Assert.assertEquals(0, cal.get(Calendar.SECOND));
                    Assert.assertEquals(0, cal.get(Calendar.MILLISECOND));

                    Assert.assertTrue(actual.getEnd() <= reference.getTime());

                    // Test including the reference month.
                    actual = TimelineUtilities.getPrecedingMonths(reference, offset, true);
                    cal.setTime(actual.getStartDate());
                    ++expectedStartMonth;
                    if (expectedStartMonth >= Constants.MONTHS_PER_YEAR)
                    {
                        expectedStartMonth -= Constants.MONTHS_PER_YEAR;
                    }
                    Assert.assertEquals(expectedStartMonth, cal.get(Calendar.MONTH));
                    Assert.assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
                    Assert.assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
                    Assert.assertEquals(0, cal.get(Calendar.MINUTE));
                    Assert.assertEquals(0, cal.get(Calendar.SECOND));
                    Assert.assertEquals(0, cal.get(Calendar.MILLISECOND));
                    cal.setTime(actual.getEndDate());
                    Assert.assertEquals(TimelineUtilities.getNext0000(reference), cal);
                }
            };
            iterateDays(r);
        }
    }

    /**
     * Test for {@link TimelineUtilities#getPrecedingWeeks(int, boolean)}.
     */
    @Test
    public void testGetPrecedingWeeks()
    {
        final Calendar cal = Calendar.getInstance();
        iterateDays(new TestRunnable()
        {
            @Override
            public void run(int year, int month, int day)
            {
                cal.set(year, month, day);
                Date reference = cal.getTime();
                TimeSpan actual1false = TimelineUtilities.getPrecedingWeeks(reference, 1, false);
                cal.setTime(actual1false.getStartDate());
                Assert.assertEquals(Calendar.SUNDAY, cal.get(Calendar.DAY_OF_WEEK));
                Assert.assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
                Assert.assertEquals(0, cal.get(Calendar.MINUTE));
                Assert.assertEquals(0, cal.get(Calendar.SECOND));
                Assert.assertEquals(0, cal.get(Calendar.MILLISECOND));
                cal.setTime(actual1false.getEndDate());
                Assert.assertEquals(Calendar.SUNDAY, cal.get(Calendar.DAY_OF_WEEK));
                Assert.assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
                Assert.assertEquals(0, cal.get(Calendar.MINUTE));
                Assert.assertEquals(0, cal.get(Calendar.SECOND));
                Assert.assertEquals(0, cal.get(Calendar.MILLISECOND));

                Assert.assertTrue(Weeks.ONE.compareTo(actual1false.getDuration()) == 0);
                Assert.assertTrue(reference.getTime() >= actual1false.getEnd());

                TimeSpan actual1true = TimelineUtilities.getPrecedingWeeks(reference, 1, true);
                Assert.assertEquals(actual1false.getEnd(), actual1true.getStart());
                Assert.assertEquals(TimelineUtilities.getNext0000(reference).getTimeInMillis(), actual1true.getEnd());

                TimeSpan actual2true = TimelineUtilities.getPrecedingWeeks(reference, 2, true);
                Assert.assertEquals(actual1false.getStart(), actual2true.getStart());
                Assert.assertEquals(TimelineUtilities.getNext0000(reference).getTimeInMillis(), actual2true.getEnd());
            }
        });
    }

    /**
     * Test for {@link TimelineUtilities#getStartCalendar(TimeSpan)}.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testGetStartCalendar() throws ParseException
    {
        Date start;
        Date end = getDate("2010-02-04 00:00:00.001");
        Date actual;

        start = getDate("2010-02-03 00:00:00.000");
        actual = TimelineUtilities.getStartCalendar(TimeSpan.get(start, end)).getTime();
        Assert.assertEquals(getDate("2010-02-03 00:00:00.000"), actual);

        start = getDate("2010-02-03 23:59:59.999");
        actual = TimelineUtilities.getStartCalendar(TimeSpan.get(start, end)).getTime();
        Assert.assertEquals(getDate("2010-02-03 00:00:00.000"), actual);
    }

    /**
     * Test for
     * {@link TimelineUtilities#getTableOfLegalDates(TimeSpan, Duration)} .
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testGetTableOfLegalDates() throws ParseException
    {
        Date start = getDate("2010-02-03 00:00:00.000");
        Date end = getDate("2010-03-02 00:00:00.000");

        TimeSpan span = TimeSpan.get(start, end);
        Map<Duration, List<DateDurationKey>> map = TimelineUtilities.getTableOfLegalDates(span, Months.ONE);
        List<DateDurationKey> actual = map.get(Months.ONE);

        Assert.assertEquals(2, actual.size());
        Assert.assertEquals(getDate("2010-02-01 00:00:00.000"), actual.get(0).getStartDate());
        Assert.assertEquals(getDate("2010-03-01 00:00:00.000"), actual.get(0).getEndDate());
        Assert.assertEquals(Months.ONE, actual.get(0).getDuration());
        Assert.assertEquals(getDate("2010-03-01 00:00:00.000"), actual.get(1).getStartDate());
        Assert.assertEquals(getDate("2010-04-01 00:00:00.000"), actual.get(1).getEndDate());
        Assert.assertEquals(Months.ONE, actual.get(1).getDuration());

        actual = map.get(Weeks.ONE);

        // TODO: this fails
//        Assert.assertEquals(9, actual.size());
//        Assert.assertEquals(getDate("2010-01-31 00:00:00.000"), actual.get(0).getStartDate());
//        Assert.assertEquals(getDate("2010-02-07 00:00:00.000"), actual.get(0).getEndDate());
//        Assert.assertEquals(Weeks.ONE, actual.get(0).getDuration());
//        Assert.assertEquals(getDate("2010-02-07 00:00:00.000"), actual.get(1).getStartDate());
//        Assert.assertEquals(getDate("2010-02-14 00:00:00.000"), actual.get(1).getEndDate());
//        Assert.assertEquals(Weeks.ONE, actual.get(1).getDuration());
//        Assert.assertEquals(getDate("2010-02-14 00:00:00.000"), actual.get(2).getStartDate());
//        Assert.assertEquals(getDate("2010-02-21 00:00:00.000"), actual.get(2).getEndDate());
//        Assert.assertEquals(Weeks.ONE, actual.get(2).getDuration());
//        Assert.assertEquals(getDate("2010-02-21 00:00:00.000"), actual.get(3).getStartDate());
//        Assert.assertEquals(getDate("2010-02-28 00:00:00.000"), actual.get(3).getEndDate());
//        Assert.assertEquals(Weeks.ONE, actual.get(3).getDuration());
//        Assert.assertEquals(getDate("2010-02-28 00:00:00.000"), actual.get(4).getStartDate());
//        Assert.assertEquals(getDate("2010-03-07 00:00:00.000"), actual.get(4).getEndDate());
//        Assert.assertEquals(Weeks.ONE, actual.get(4).getDuration());
//        Assert.assertEquals(getDate("2010-03-07 00:00:00.000"), actual.get(5).getStartDate());
//        Assert.assertEquals(getDate("2010-03-14 00:00:00.000"), actual.get(5).getEndDate());
//        Assert.assertEquals(Weeks.ONE, actual.get(5).getDuration());
//        Assert.assertEquals(getDate("2010-03-14 00:00:00.000"), actual.get(6).getStartDate());
//        Assert.assertEquals(getDate("2010-03-21 00:00:00.000"), actual.get(6).getEndDate());
//        Assert.assertEquals(Weeks.ONE, actual.get(6).getDuration());
//        Assert.assertEquals(getDate("2010-03-21 00:00:00.000"), actual.get(7).getStartDate());
//        Assert.assertEquals(getDate("2010-03-28 00:00:00.000"), actual.get(7).getEndDate());
//        Assert.assertEquals(Weeks.ONE, actual.get(7).getDuration());
//        Assert.assertEquals(getDate("2010-03-28 00:00:00.000"), actual.get(8).getStartDate());
//        Assert.assertEquals(getDate("2010-04-04 00:00:00.000"), actual.get(8).getEndDate());
//        Assert.assertEquals(Weeks.ONE, actual.get(8).getDuration());

        actual = map.get(Days.ONE);

        Assert.assertEquals(63, actual.size());
        long jan31 = getDate("2010-01-31 00:00:00.000").getTime();
        for (int index = 0; index < 63; ++index)
        {
            Assert.assertEquals(jan31 + index * (long)Constants.MILLIS_PER_DAY, actual.get(index).getStartDate().getTime());
            Assert.assertEquals(jan31 + (index + 1) * (long)Constants.MILLIS_PER_DAY, actual.get(index).getEndDate().getTime());
            Assert.assertEquals(Days.ONE, actual.get(index).getDuration());
        }
    }

    /**
     * Test for {@link TimelineUtilities#getThisDay(Date)}.
     */
    @Test
    public void testGetThisDay()
    {
        GregorianCalendar day = new GregorianCalendar(1972, 1, 11);
        GregorianCalendar nextDay = new GregorianCalendar(1972, 1, 12);

        // Check a time in the middle of the day.
        GregorianCalendar testTime = new GregorianCalendar(1972, 1, 11, 10, 53, 35);
        TimeSpan span = TimelineUtilities.getThisDay(testTime.getTime());
        Assert.assertEquals(day.getTimeInMillis(), span.getStart());
        Assert.assertEquals(nextDay.getTimeInMillis(), span.getEnd());

        // Check to make sure the we get the same value back for the beginning
        // of the day.
        span = TimelineUtilities.getThisDay(day.getTime());
        Assert.assertEquals(day.getTimeInMillis(), span.getStart());
        Assert.assertEquals(nextDay.getTimeInMillis(), span.getEnd());
    }

    /**
     * Test for {@link TimelineUtilities#getThisMonth(Date)}.
     */
    @Test
    public void testGetThisMonth()
    {
        GregorianCalendar month = new GregorianCalendar(1972, 1, 1);
        GregorianCalendar nextMonth = new GregorianCalendar(1972, 2, 1);

        // Check a time in the middle of the month.
        GregorianCalendar testTime = new GregorianCalendar(1972, 1, 11, 10, 53, 35);
        TimeSpan span = TimelineUtilities.getThisMonth(testTime.getTime());
        Assert.assertEquals(month.getTimeInMillis(), span.getStart());
        Assert.assertEquals(nextMonth.getTimeInMillis(), span.getEnd());

        // Check to make sure the we get the same value back for the beginning
        // of the month.
        span = TimelineUtilities.getThisMonth(month.getTime());
        Assert.assertEquals(month.getTimeInMillis(), span.getStart());
        Assert.assertEquals(nextMonth.getTimeInMillis(), span.getEnd());
    }

    /**
     * Test for {@link TimelineUtilities#getThisWeek(Date)}.
     */
    @Test
    public void testGetThisWeek()
    {
        GregorianCalendar sunday = new GregorianCalendar(2014, 3, 13);
        GregorianCalendar nextSunday = new GregorianCalendar(2014, 3, 20);

        // This is Wednesday
        GregorianCalendar testTime = new GregorianCalendar(2014, 3, 16, 10, 53, 35);
        TimeSpan span = TimelineUtilities.getThisWeek(testTime.getTime());
        Assert.assertEquals(sunday.getTimeInMillis(), span.getStart());
        Assert.assertEquals(nextSunday.getTimeInMillis(), span.getEnd());

        // Check to make sure the we get the same value back for the beginning
        // of the week.
        span = TimelineUtilities.getThisWeek(sunday.getTime());
        Assert.assertEquals(sunday.getTimeInMillis(), span.getStart());
        Assert.assertEquals(nextSunday.getTimeInMillis(), span.getEnd());
    }

    /**
     * Test for {@link TimelineUtilities#getToday()}.
     */
    @Test
    public void testGetToday()
    {
        TimeSpan today = TimelineUtilities.getToday();
        Assert.assertTrue(Days.ONE.compareTo(today.getDuration()) == 0);
        Assert.assertTrue(TimelineUtilities.isAt0000(today.getStartDate()));
        Assert.assertTrue(today.getStart() < System.currentTimeMillis());
        Assert.assertTrue(TimelineUtilities.isAt0000(today.getEndDate()));
        Assert.assertTrue(today.getEnd() > System.currentTimeMillis());
    }

    /**
     * Test for {@link TimelineUtilities#getYesterday()}.
     */
    @Test
    public void testGetYesterday()
    {
        TimeSpan yesterday = TimelineUtilities.getYesterday();
        Assert.assertTrue(Days.ONE.compareTo(yesterday.getDuration()) == 0);
        Assert.assertTrue(TimelineUtilities.isAt0000(yesterday.getStartDate()));
        Assert.assertTrue(yesterday.getStart() < System.currentTimeMillis());
        Assert.assertTrue(TimelineUtilities.isAt0000(yesterday.getEndDate()));
        Assert.assertTrue(yesterday.getEnd() < System.currentTimeMillis());
    }

    /**
     * Test for {@link TimelineUtilities#isAt0000(Date)}.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testIsAt0000() throws ParseException
    {
        Assert.assertTrue(TimelineUtilities.isAt0000(getDate("2010-02-04 00:00:00.000")));
        Assert.assertFalse(TimelineUtilities.isAt0000(getDate("2010-02-04 00:00:00.001")));
        Assert.assertFalse(TimelineUtilities.isAt0000(getDate("2010-02-04 23:59:59.999")));
    }

    /**
     * Test for {@link TimelineUtilities#isDay(TimeSpan)}.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testIsDay() throws ParseException
    {
        Assert.assertTrue(TimelineUtilities.isDay(TimeSpanFormatterTest.span("2015-04-09 00:00:00", "2015-04-10 00:00:00")));
        Assert.assertFalse(TimelineUtilities.isDay(TimeSpanFormatterTest.span("2015-04-09 01:00:00", "2015-04-10 01:00:00")));
        Assert.assertFalse(TimelineUtilities.isDay(TimeSpanFormatterTest.span("2015-04-09 00:00:00", "2015-04-11 00:00:00")));
    }

    /**
     * Test for {@link TimelineUtilities#isWeek(TimeSpan)}.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testIsWeek() throws ParseException
    {
        Assert.assertTrue(TimelineUtilities.isWeek(TimeSpanFormatterTest.span("2015-04-12 00:00:00", "2015-04-19 00:00:00")));
        Assert.assertFalse(TimelineUtilities.isWeek(TimeSpanFormatterTest.span("2015-04-13 00:00:00", "2015-04-20 00:00:00")));
        Assert.assertFalse(TimelineUtilities.isWeek(TimeSpanFormatterTest.span("2015-04-12 00:00:00", "2015-04-26 00:00:00")));
    }

    /**
     * Test for {@link TimelineUtilities#isMonth(TimeSpan)}.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testIsMonth() throws ParseException
    {
        Assert.assertTrue(TimelineUtilities.isMonth(TimeSpanFormatterTest.span("2015-04-01 00:00:00", "2015-05-01 00:00:00")));
        Assert.assertFalse(TimelineUtilities.isMonth(TimeSpanFormatterTest.span("2015-04-02 00:00:00", "2015-05-02 00:00:00")));
        Assert.assertFalse(TimelineUtilities.isMonth(TimeSpanFormatterTest.span("2015-04-01 00:00:00", "2015-06-01 00:00:00")));
    }

    /**
     * Test for {@link TimelineUtilities#roundDown(Date, Duration)}.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testRoundDown() throws ParseException
    {
        Date input = getDate("2014-08-20 12:34:56.789");
        Calendar expected;
        Calendar actual;

        expected = getCalendar("2014-01-01 00:00:00.000");
        actual = TimelineUtilities.roundDown(input, Years.ONE);
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2010-01-01 00:00:00.000");
        actual = TimelineUtilities.roundDown(input, new Years(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-01 00:00:00.000");
        actual = TimelineUtilities.roundDown(input, Months.ONE);
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-06-01 00:00:00.000");
        actual = TimelineUtilities.roundDown(input, new Months(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-17 00:00:00.000");
        actual = TimelineUtilities.roundDown(input, Weeks.ONE);
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 00:00:00.000");
        actual = TimelineUtilities.roundDown(input, Days.ONE);
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-16 00:00:00.000");
        actual = TimelineUtilities.roundDown(input, new Days(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 12:00:00.000");
        actual = TimelineUtilities.roundDown(input, Hours.ONE);
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 10:00:00.000");
        actual = TimelineUtilities.roundDown(input, new Hours(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 12:34:00.000");
        actual = TimelineUtilities.roundDown(input, Minutes.ONE);
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 12:30:00.000");
        actual = TimelineUtilities.roundDown(input, new Minutes(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 12:34:56.000");
        actual = TimelineUtilities.roundDown(input, Seconds.ONE);
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 12:34:55.000");
        actual = TimelineUtilities.roundDown(input, new Seconds(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 12:34:56.785");
        actual = TimelineUtilities.roundDown(input, new Milliseconds(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);
    }

    /**
     * Test for {@link TimelineUtilities#roundDown(Date, Duration)} that tests
     * rounding to a 5 day interval.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testRoundDown5day() throws ParseException
    {
        Calendar expected;
        Calendar actual;

        expected = getCalendar("2014-01-01 00:00:00.000");
        actual = TimelineUtilities.roundDown(getDate("2014-01-01 12:34:56.789"), new Days(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);
        actual = TimelineUtilities.roundDown(getDate("2014-01-02 12:34:56.789"), new Days(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);
        actual = TimelineUtilities.roundDown(getDate("2014-01-03 12:34:56.789"), new Days(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);
        actual = TimelineUtilities.roundDown(getDate("2014-01-04 12:34:56.789"), new Days(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);
        actual = TimelineUtilities.roundDown(getDate("2014-01-05 12:34:56.789"), new Days(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-01-06 00:00:00.000");
        actual = TimelineUtilities.roundDown(getDate("2014-01-06 12:34:56.789"), new Days(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);
    }

    /**
     * Test for {@link TimelineUtilities#roundUp(Date, Duration)}.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testRoundUp() throws ParseException
    {
        Date input = getDate("2014-08-20 12:34:56.789");
        Calendar expected;
        Calendar actual;

        expected = getCalendar("2015-01-01 00:00:00.000");
        actual = TimelineUtilities.roundUp(input, Years.ONE);
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2015-01-01 00:00:00.000");
        actual = TimelineUtilities.roundUp(input, new Years(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-09-01 00:00:00.000");
        actual = TimelineUtilities.roundUp(input, Months.ONE);
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-11-01 00:00:00.000");
        actual = TimelineUtilities.roundUp(input, new Months(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-24 00:00:00.000");
        actual = TimelineUtilities.roundUp(input, Weeks.ONE);
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-21 00:00:00.000");
        actual = TimelineUtilities.roundUp(input, Days.ONE);
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-21 00:00:00.000");
        actual = TimelineUtilities.roundUp(input, new Days(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 13:00:00.000");
        actual = TimelineUtilities.roundUp(input, Hours.ONE);
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 15:00:00.000");
        actual = TimelineUtilities.roundUp(input, new Hours(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 12:35:00.000");
        actual = TimelineUtilities.roundUp(input, Minutes.ONE);
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 12:35:00.000");
        actual = TimelineUtilities.roundUp(input, new Minutes(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 12:34:57.000");
        actual = TimelineUtilities.roundUp(input, Seconds.ONE);
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 12:35:00.000");
        actual = TimelineUtilities.roundUp(input, new Seconds(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 12:34:56.790");
        actual = TimelineUtilities.roundUp(input, new Milliseconds(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        // Already rounded values

        expected = getCalendar("2014-08-20 05:05:05.005");
        actual = TimelineUtilities.roundUp(getDate("2014-08-20 05:05:05.005"), Milliseconds.ONE);
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 05:05:05.005");
        actual = TimelineUtilities.roundUp(getDate("2014-08-20 05:05:05.005"), new Milliseconds(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 05:05:05.000");
        actual = TimelineUtilities.roundUp(getDate("2014-08-20 05:05:05.000"), Seconds.ONE);
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 05:05:05.000");
        actual = TimelineUtilities.roundUp(getDate("2014-08-20 05:05:05.000"), new Seconds(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 05:05:00.000");
        actual = TimelineUtilities.roundUp(getDate("2014-08-20 05:05:00.000"), Minutes.ONE);
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 05:05:00.000");
        actual = TimelineUtilities.roundUp(getDate("2014-08-20 05:05:00.000"), new Minutes(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 05:00:00.000");
        actual = TimelineUtilities.roundUp(getDate("2014-08-20 05:00:00.000"), Hours.ONE);
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 05:00:00.000");
        actual = TimelineUtilities.roundUp(getDate("2014-08-20 05:00:00.000"), new Hours(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-20 00:00:00.000");
        actual = TimelineUtilities.roundUp(getDate("2014-08-20 00:00:00.000"), Days.ONE);
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-21 00:00:00.000");
        actual = TimelineUtilities.roundUp(getDate("2014-08-21 00:00:00.000"), new Days(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-08-24 00:00:00.000");
        actual = TimelineUtilities.roundUp(getDate("2014-08-24 00:00:00.000"), Weeks.ONE);
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-11-01 00:00:00.000");
        actual = TimelineUtilities.roundUp(getDate("2014-11-01 00:00:00.000"), Months.ONE);
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2014-11-01 00:00:00.000");
        actual = TimelineUtilities.roundUp(getDate("2014-11-01 00:00:00.000"), new Months(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2015-01-01 00:00:00.000");
        actual = TimelineUtilities.roundUp(getDate("2015-01-01 00:00:00.000"), Years.ONE);
        Assert.assertEquals(getMessage(expected, actual), expected, actual);

        expected = getCalendar("2015-01-01 00:00:00.000");
        actual = TimelineUtilities.roundUp(getDate("2015-01-01 00:00:00.000"), new Years(5));
        Assert.assertEquals(getMessage(expected, actual), expected, actual);
    }

    /**
     * Test for {@link TimelineUtilities#scale(TimeSpan, double)}.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testScale() throws ParseException
    {
        TimeSpan span = TimeSpan.get(900, 1100);
        Assert.assertEquals(TimeSpan.get(1000), TimelineUtilities.scale(span, 0));
        Assert.assertEquals(TimeSpan.get(975, 1025), TimelineUtilities.scale(span, 0.25));
        Assert.assertEquals(TimeSpan.get(900, 1100), TimelineUtilities.scale(span, 1));
        Assert.assertEquals(TimeSpan.get(700, 1300), TimelineUtilities.scale(span, 3));
    }

    /**
     * Helper method that creates a calendar.
     *
     * @param str The time string.
     * @return The calendar.
     * @throws ParseException If the string cannot be parsed.
     */
    private Calendar getCalendar(String str) throws ParseException
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getDate(str));
        return cal;
    }

    /**
     * Helper method that creates a date.
     *
     * @param str The time string.
     * @return The date.
     * @throws ParseException If the string cannot be parsed.
     */
    private Date getDate(String str) throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat(DateTimeFormats.DATE_TIME_MILLIS_FORMAT);
        Date date = format.parse(str);
        return date;
    }

    /**
     * Creates a message for when two calendars are not equal.
     *
     * @param expected the expected calendar
     * @param actual the actual calendar
     * @return the string
     */
    private String getMessage(Calendar expected, Calendar actual)
    {
        return "\nExpected: " + toString(expected) + "\n     Actual: " + toString(actual) + "\n";
    }

    /**
     * Iterate over a number of calendar days and call the test runnable for
     * each one.
     *
     * @param r The test runnable.
     */
    private void iterateDays(TestRunnable r)
    {
        Calendar cal = Calendar.getInstance();
        for (int year = 2012; year < 2014; ++year)
        {
            for (int month = 0; month < 12; ++month)
            {
                cal.set(year, month, 1);
                int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                for (int day = 1; day <= maxDay; ++day)
                {
                    r.run(year, month, day);
                }
            }
        }
    }

    /**
     * Creates a readable string from a calendar.
     *
     * @param cal the calendar
     * @return the string
     */
    private String toString(Calendar cal)
    {
        return new SimpleDateFormat(DateTimeFormats.DATE_TIME_MILLIS_FORMAT).format(cal.getTime());
    }

    /**
     * Interface for functionality to be tested against a particular reference
     * date.
     */
    @FunctionalInterface
    private interface TestRunnable
    {
        /**
         * Run the functionality.
         *
         * @param year The year.
         * @param month The month (zero-based).
         * @param day The day.
         */
        void run(int year, int month, int day);
    }
}
