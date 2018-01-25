package io.opensphere.core.units.duration;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.junit.Test;

import io.opensphere.core.units.InconvertibleUnits;
import org.junit.Assert;

/**
 * Test for various {@link Duration}s.
 */
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.GodClass" })
public class DurationTest
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DurationTest.class);

    /**
     * Test for {@link Duration#addTo(java.util.Calendar)}.
     */
    @Test
    public void testAddTo()
    {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2010, 3, 1);

        Calendar expected = (Calendar)cal.clone();

        new Nanoseconds(999999).addTo(cal);
        Assert.assertEquals(expected.getTimeInMillis(), cal.getTimeInMillis());

        expected.add(Calendar.MILLISECOND, 1);
        new Nanoseconds(1000000).addTo(cal);
        Assert.assertEquals(expected.getTimeInMillis(), cal.getTimeInMillis());

        new Microseconds(999).addTo(cal);
        Assert.assertEquals(expected.getTimeInMillis(), cal.getTimeInMillis());

        expected.add(Calendar.MILLISECOND, 1);
        new Microseconds(1000).addTo(cal);
        Assert.assertEquals(expected.getTimeInMillis(), cal.getTimeInMillis());

        expected.add(Calendar.MILLISECOND, 1);
        Milliseconds.ONE.addTo(cal);
        Assert.assertEquals(expected.getTimeInMillis(), cal.getTimeInMillis());

        expected.add(Calendar.SECOND, 1);
        Seconds.ONE.addTo(cal);
        Assert.assertEquals(expected.getTimeInMillis(), cal.getTimeInMillis());

        expected.add(Calendar.MINUTE, 1);
        Minutes.ONE.addTo(cal);
        Assert.assertEquals(expected.getTimeInMillis(), cal.getTimeInMillis());

        expected.add(Calendar.HOUR, 1);
        Hours.ONE.addTo(cal);
        Assert.assertEquals(expected.getTimeInMillis(), cal.getTimeInMillis());

        expected.add(Calendar.DAY_OF_YEAR, 1);
        Days.ONE.addTo(cal);
        Assert.assertEquals(expected.getTimeInMillis(), cal.getTimeInMillis());

        expected.add(Calendar.WEEK_OF_YEAR, 1);
        Weeks.ONE.addTo(cal);
        Assert.assertEquals(expected.getTimeInMillis(), cal.getTimeInMillis());

        expected.add(Calendar.MONTH, 1);
        Months.ONE.addTo(cal);
        Assert.assertEquals(expected.getTimeInMillis(), cal.getTimeInMillis());

        expected.add(Calendar.YEAR, 1);
        Years.ONE.addTo(cal);
        Assert.assertEquals(expected.getTimeInMillis(), cal.getTimeInMillis());
    }

    /**
     * Test for {@link Duration#asDate()}.
     */
    @Test
    public void testAsDate()
    {
        Duration dur;
        dur = new Nanoseconds(67);
        Assert.assertEquals(new Date(0), dur.asDate());
        dur = new Microseconds(67);
        Assert.assertEquals(new Date(0), dur.asDate());
        dur = new Milliseconds(67);
        Assert.assertEquals(new Date(67), dur.asDate());
        dur = new Seconds(67);
        Assert.assertEquals(new Date(67000), dur.asDate());
        dur = new Minutes(67);
        Assert.assertEquals(new Date(67000 * 60), dur.asDate());
        dur = new Hours(67);
        Assert.assertEquals(new Date(67000 * 3600), dur.asDate());
        dur = new Days(67);
        Assert.assertEquals(new Date(67000L * 3600 * 24), dur.asDate());
        dur = new Weeks(67);
        Assert.assertEquals(new Date(67000L * 3600 * 24 * 7), dur.asDate());
        dur = new Milliseconds(Long.MAX_VALUE - 1L);
        Assert.assertEquals(new Date(Long.MAX_VALUE - 1L), dur.asDate());
        dur = new Milliseconds(Long.MAX_VALUE);
        Assert.assertEquals(new Date(Long.MAX_VALUE), dur.asDate());
        dur = new Milliseconds(Long.MAX_VALUE).add(new Milliseconds(1L));
        Assert.assertEquals(new Date(Long.MAX_VALUE), dur.asDate());
    }

    /**
     * Test {@link Duration#clone()}.
     */
    @Test
    public void testClone()
    {
        Duration dur;
        dur = new Nanoseconds(67);
        Assert.assertEquals(dur.getMagnitude(), dur.clone().getMagnitude());
        dur = new Microseconds(67);
        Assert.assertEquals(dur.getMagnitude(), dur.clone().getMagnitude());
        dur = new Milliseconds(67);
        Assert.assertEquals(dur.getMagnitude(), dur.clone().getMagnitude());
        dur = new Seconds(67);
        Assert.assertEquals(dur.getMagnitude(), dur.clone().getMagnitude());
        dur = new Minutes(67);
        Assert.assertEquals(dur.getMagnitude(), dur.clone().getMagnitude());
        dur = new Hours(67);
        Assert.assertEquals(dur.getMagnitude(), dur.clone().getMagnitude());
        dur = new Days(67);
        Assert.assertEquals(dur.getMagnitude(), dur.clone().getMagnitude());
        dur = new Weeks(67);
        Assert.assertEquals(dur.getMagnitude(), dur.clone().getMagnitude());
        dur = new Months(67);
        Assert.assertEquals(dur.getMagnitude(), dur.clone().getMagnitude());
        dur = new Years(67);
        Assert.assertEquals(dur.getMagnitude(), dur.clone().getMagnitude());
    }

    /** Test for {@link Duration#compareTo(Duration)}. */
    @Test
    public void testCompareTo()
    {
        Assert.assertEquals(0, new Seconds(99 * 3600 + 59 * 60 + 59).compareTo(new Hours(100).subtract(Seconds.ONE)));
        Assert.assertEquals(0, new Seconds(86400).compareTo(Days.ONE));
        Assert.assertEquals(1, new Seconds(86401).compareTo(Days.ONE));
        Assert.assertEquals(-1, new Seconds(86399).compareTo(Days.ONE));
        Assert.assertFalse(new Seconds(86400).isGreaterThan(Days.ONE));
        Assert.assertTrue(new Seconds(86401).isGreaterThan(Days.ONE));
        Assert.assertFalse(new Seconds(86399).isGreaterThan(Days.ONE));
        Duration[] dur = new Duration[16];
        dur[0] = new Nanoseconds(604800000000000L);
        dur[1] = new Microseconds(604800000000L);
        dur[2] = new Milliseconds(604800000);
        dur[3] = new Seconds(604800);
        dur[4] = new Minutes(10080);
        dur[5] = new Hours(168);
        dur[6] = new Days(7);
        dur[7] = new Weeks(1);
        for (int i = 0; i < dur.length / 2; ++i)
        {
            dur[dur.length / 2 + i] = dur[i].clone();
        }
        for (int i = 0; i < dur.length; ++i)
        {
            for (int j = 0; j < dur.length; ++j)
            {
                Assert.assertEquals(shouldBeEqual(dur, i, j), 0, dur[i].compareTo(dur[j]));
            }
        }
        Assert.assertEquals(1, dur[0].compareTo(new Nanoseconds(0)));
        Assert.assertEquals(1, dur[1].compareTo(new Microseconds(0)));
        Assert.assertEquals(1, dur[2].compareTo(new Milliseconds(0)));
        Assert.assertEquals(1, dur[3].compareTo(new Seconds(0)));
        Assert.assertEquals(1, dur[4].compareTo(new Minutes(0)));
        Assert.assertEquals(1, dur[5].compareTo(new Hours(0)));
        Assert.assertEquals(1, dur[6].compareTo(new Days(0)));
        Assert.assertEquals(1, dur[7].compareTo(new Weeks(0)));

        dur = new Duration[4];
        dur[0] = new Months(24);
        dur[1] = new Years(2);
        for (int i = 0; i < dur.length / 2; ++i)
        {
            dur[dur.length / 2 + i] = dur[i].clone();
        }
        for (int i = 0; i < dur.length; ++i)
        {
            for (int j = 0; j < dur.length; ++j)
            {
                Assert.assertEquals(shouldBeEqual(dur, i, j), 0, dur[i].compareTo(dur[j]));
            }
        }
        Assert.assertEquals(1, dur[0].compareTo(new Months(0)));
        Assert.assertEquals(1, dur[1].compareTo(new Years(0)));

        Assert.assertEquals(1, Months.ONE.compareTo(new Days(28).subtract(Nanoseconds.ONE)));
        Assert.assertEquals(-1, Months.ONE.compareTo(new Days(31).add(Nanoseconds.ONE)));
        Assert.assertEquals(1, Years.ONE.compareTo(new Days(365).subtract(Nanoseconds.ONE)));
        Assert.assertEquals(-1, Years.ONE.compareTo(new Days(366).add(Nanoseconds.ONE)));

        Assert.assertEquals(1, new Months(.5).compareTo(new Days(14).subtract(Nanoseconds.ONE)));
        Assert.assertEquals(-1, new Months(.5).compareTo(new Days(15.5).add(Nanoseconds.ONE)));
        Assert.assertEquals(1, new Years(.5).compareTo(new Days(182.5).subtract(Nanoseconds.ONE)));
        Assert.assertEquals(-1, new Years(.5).compareTo(new Days(183).add(Nanoseconds.ONE)));

        Assert.assertEquals(-1, new Months(-.5).compareTo(new Days(-14).add(Nanoseconds.ONE)));
        Assert.assertEquals(1, new Months(-.5).compareTo(new Days(-15.5).subtract(Nanoseconds.ONE)));
        Assert.assertEquals(-1, new Years(-.5).compareTo(new Days(-182.5).add(Nanoseconds.ONE)));
        Assert.assertEquals(1, new Years(-.5).compareTo(new Days(-183).subtract(Nanoseconds.ONE)));

        Assert.assertEquals(-1, new Days(-28).compareTo(Months.ONE));
        Assert.assertEquals(1, Months.ONE.compareTo(new Days(-28)));
    }

    /** Test for {@link Duration#compareTo(Duration)} with ambiguous results. */
    @Test(expected = InconvertibleUnits.class)
    public void testCompareToFail1()
    {
        Months.ONE.compareTo(new Days(28));
    }

    /** Test for {@link Duration#compareTo(Duration)} with ambiguous results. */
    @Test(expected = InconvertibleUnits.class)
    public void testCompareToFail2()
    {
        Months.ONE.compareTo(new Days(31));
    }

    /** Test for {@link Duration#compareTo(Duration)} with ambiguous results. */
    @Test(expected = InconvertibleUnits.class)
    public void testCompareToFail3()
    {
        Years.ONE.compareTo(new Days(365));
    }

    /** Test for {@link Duration#compareTo(Duration)} with ambiguous results. */
    @Test(expected = InconvertibleUnits.class)
    public void testCompareToFail4()
    {
        Years.ONE.compareTo(new Days(366));
    }

    /** Test {@link Duration#create(ChronoUnit, BigDecimal)}. */
    @Test
    public void testCreateChronoUnitBigDecimal()
    {
        Assert.assertEquals(new Nanoseconds(70L), Duration.create(ChronoUnit.NANOS, BigDecimal.valueOf(7L, -1)));
        Assert.assertEquals(new Microseconds(70L), Duration.create(ChronoUnit.MICROS, BigDecimal.valueOf(7L, -1)));
        Assert.assertEquals(new Milliseconds(70L), Duration.create(ChronoUnit.MILLIS, BigDecimal.valueOf(7L, -1)));
        Assert.assertEquals(new Seconds(70L), Duration.create(ChronoUnit.SECONDS, BigDecimal.valueOf(7L, -1)));
        Assert.assertEquals(new Minutes(70L), Duration.create(ChronoUnit.MINUTES, BigDecimal.valueOf(7L, -1)));
        Assert.assertEquals(new Hours(70L), Duration.create(ChronoUnit.HOURS, BigDecimal.valueOf(7L, -1)));
        Assert.assertEquals(new Days(70L), Duration.create(ChronoUnit.DAYS, BigDecimal.valueOf(7L, -1)));
        Assert.assertEquals(new Weeks(70L), Duration.create(ChronoUnit.WEEKS, BigDecimal.valueOf(7L, -1)));
        Assert.assertEquals(new Months(70L), Duration.create(ChronoUnit.MONTHS, BigDecimal.valueOf(7L, -1)));
        Assert.assertEquals(new Years(70L), Duration.create(ChronoUnit.YEARS, BigDecimal.valueOf(7L, -1)));
    }

    /** Test {@link Duration#create(ChronoUnit, Duration)}. */
    @Test
    public void testCreateChronoUnitDuration()
    {
        Assert.assertEquals(new Nanoseconds(70L), Duration.create(ChronoUnit.NANOS, new Seconds(7L, 8)));
        Assert.assertEquals(new Microseconds(70L), Duration.create(ChronoUnit.MICROS, new Nanoseconds(7L, -4)));
        Assert.assertEquals(new Milliseconds(70L), Duration.create(ChronoUnit.MILLIS, new Nanoseconds(7L, -7)));
        Assert.assertEquals(new Seconds(70L), Duration.create(ChronoUnit.SECONDS, new Nanoseconds(7L, -10)));
        Assert.assertEquals(new Minutes(70L), Duration.create(ChronoUnit.MINUTES, new Nanoseconds(42L, -11)));
        Assert.assertEquals(new Hours(70L), Duration.create(ChronoUnit.HOURS, new Nanoseconds(252L, -12)));
        Assert.assertEquals(new Days(70L), Duration.create(ChronoUnit.DAYS, new Nanoseconds(6048, -12)));
        Assert.assertEquals(new Weeks(70L), Duration.create(ChronoUnit.WEEKS, new Nanoseconds(42336L, -12)));
        Assert.assertEquals(new Months(70L), Duration.create(ChronoUnit.MONTHS, new Months(7L, -1)));
        Assert.assertEquals(new Years(70L), Duration.create(ChronoUnit.YEARS, new Months(84L, -1)));
    }

    /** Test {@link Duration#create(ChronoUnit, long)}. */
    @Test
    public void testCreateChronoUnitLong()
    {
        Assert.assertEquals(new Nanoseconds(7L), Duration.create(ChronoUnit.NANOS, 7L));
        Assert.assertEquals(new Microseconds(7L), Duration.create(ChronoUnit.MICROS, 7L));
        Assert.assertEquals(new Milliseconds(7L), Duration.create(ChronoUnit.MILLIS, 7L));
        Assert.assertEquals(new Seconds(7L), Duration.create(ChronoUnit.SECONDS, 7L));
        Assert.assertEquals(new Minutes(7L), Duration.create(ChronoUnit.MINUTES, 7L));
        Assert.assertEquals(new Hours(7L), Duration.create(ChronoUnit.HOURS, 7L));
        Assert.assertEquals(new Days(7L), Duration.create(ChronoUnit.DAYS, 7L));
        Assert.assertEquals(new Weeks(7L), Duration.create(ChronoUnit.WEEKS, 7L));
        Assert.assertEquals(new Months(7L), Duration.create(ChronoUnit.MONTHS, 7L));
        Assert.assertEquals(new Years(7L), Duration.create(ChronoUnit.YEARS, 7L));
    }

    /** Test {@link Duration#create(ChronoUnit, long, int)}. */
    @Test
    public void testCreateChronoUnitLongInt()
    {
        Assert.assertEquals(new Nanoseconds(70L), Duration.create(ChronoUnit.NANOS, 7L, -1));
        Assert.assertEquals(new Microseconds(70L), Duration.create(ChronoUnit.MICROS, 7L, -1));
        Assert.assertEquals(new Milliseconds(70L), Duration.create(ChronoUnit.MILLIS, 7L, -1));
        Assert.assertEquals(new Seconds(70L), Duration.create(ChronoUnit.SECONDS, 7L, -1));
        Assert.assertEquals(new Minutes(70L), Duration.create(ChronoUnit.MINUTES, 7L, -1));
        Assert.assertEquals(new Hours(70L), Duration.create(ChronoUnit.HOURS, 7L, -1));
        Assert.assertEquals(new Days(70L), Duration.create(ChronoUnit.DAYS, 7L, -1));
        Assert.assertEquals(new Weeks(70L), Duration.create(ChronoUnit.WEEKS, 7L, -1));
        Assert.assertEquals(new Months(70L), Duration.create(ChronoUnit.MONTHS, 7L, -1));
        Assert.assertEquals(new Years(70L), Duration.create(ChronoUnit.YEARS, 7L, -1));
    }

    /** Test {@link Duration#create(Class, BigDecimal)}. */
    @Test
    public void testCreateClassBigDecimal()
    {
        Assert.assertEquals(new Nanoseconds(70L), Duration.create(Nanoseconds.class, BigDecimal.valueOf(7L, -1)));
        Assert.assertEquals(new Microseconds(70L), Duration.create(Microseconds.class, BigDecimal.valueOf(7L, -1)));
        Assert.assertEquals(new Milliseconds(70L), Duration.create(Milliseconds.class, BigDecimal.valueOf(7L, -1)));
        Assert.assertEquals(new Seconds(70L), Duration.create(Seconds.class, BigDecimal.valueOf(7L, -1)));
        Assert.assertEquals(new Minutes(70L), Duration.create(Minutes.class, BigDecimal.valueOf(7L, -1)));
        Assert.assertEquals(new Hours(70L), Duration.create(Hours.class, BigDecimal.valueOf(7L, -1)));
        Assert.assertEquals(new Days(70L), Duration.create(Days.class, BigDecimal.valueOf(7L, -1)));
        Assert.assertEquals(new Weeks(70L), Duration.create(Weeks.class, BigDecimal.valueOf(7L, -1)));
        Assert.assertEquals(new Months(70L), Duration.create(Months.class, BigDecimal.valueOf(7L, -1)));
        Assert.assertEquals(new Years(70L), Duration.create(Years.class, BigDecimal.valueOf(7L, -1)));
    }

    /** Test {@link Duration#create(Class, Duration)}. */
    @Test
    public void testCreateClassDuration()
    {
        Assert.assertEquals(new Nanoseconds(70L), Duration.create(Nanoseconds.class, new Seconds(7L, 8)));
        Assert.assertEquals(new Microseconds(70L), Duration.create(Microseconds.class, new Nanoseconds(7L, -4)));
        Assert.assertEquals(new Milliseconds(70L), Duration.create(Milliseconds.class, new Nanoseconds(7L, -7)));
        Assert.assertEquals(new Seconds(70L), Duration.create(Seconds.class, new Nanoseconds(7L, -10)));
        Assert.assertEquals(new Minutes(70L), Duration.create(Minutes.class, new Nanoseconds(42L, -11)));
        Assert.assertEquals(new Hours(70L), Duration.create(Hours.class, new Nanoseconds(252L, -12)));
        Assert.assertEquals(new Days(70L), Duration.create(Days.class, new Nanoseconds(6048, -12)));
        Assert.assertEquals(new Weeks(70L), Duration.create(Weeks.class, new Nanoseconds(42336L, -12)));
        Assert.assertEquals(new Months(70L), Duration.create(Months.class, new Months(7L, -1)));
        Assert.assertEquals(new Years(70L), Duration.create(Years.class, new Months(84L, -1)));
    }

    /** Test {@link Duration#create(Class, long)}. */
    @Test
    public void testCreateClassLong()
    {
        Assert.assertEquals(new Nanoseconds(7L), Duration.create(Nanoseconds.class, 7L));
        Assert.assertEquals(new Microseconds(7L), Duration.create(Microseconds.class, 7L));
        Assert.assertEquals(new Milliseconds(7L), Duration.create(Milliseconds.class, 7L));
        Assert.assertEquals(new Seconds(7L), Duration.create(Seconds.class, 7L));
        Assert.assertEquals(new Minutes(7L), Duration.create(Minutes.class, 7L));
        Assert.assertEquals(new Hours(7L), Duration.create(Hours.class, 7L));
        Assert.assertEquals(new Days(7L), Duration.create(Days.class, 7L));
        Assert.assertEquals(new Weeks(7L), Duration.create(Weeks.class, 7L));
        Assert.assertEquals(new Months(7L), Duration.create(Months.class, 7L));
        Assert.assertEquals(new Years(7L), Duration.create(Years.class, 7L));
    }

    /** Test {@link Duration#create(Class, long, int)}. */
    @Test
    public void testCreateClassLongInt()
    {
        Assert.assertEquals(new Nanoseconds(70L), Duration.create(Nanoseconds.class, 7L, -1));
        Assert.assertEquals(new Microseconds(70L), Duration.create(Microseconds.class, 7L, -1));
        Assert.assertEquals(new Milliseconds(70L), Duration.create(Milliseconds.class, 7L, -1));
        Assert.assertEquals(new Seconds(70L), Duration.create(Seconds.class, 7L, -1));
        Assert.assertEquals(new Minutes(70L), Duration.create(Minutes.class, 7L, -1));
        Assert.assertEquals(new Hours(70L), Duration.create(Hours.class, 7L, -1));
        Assert.assertEquals(new Days(70L), Duration.create(Days.class, 7L, -1));
        Assert.assertEquals(new Weeks(70L), Duration.create(Weeks.class, 7L, -1));
        Assert.assertEquals(new Months(70L), Duration.create(Months.class, 7L, -1));
        Assert.assertEquals(new Years(70L), Duration.create(Years.class, 7L, -1));
    }

    /**
     * Test {@link Duration#divide(Duration)}.
     */
    @Test
    public void testDivideDuration()
    {
        Duration[] dur = new Duration[8];
        dur[0] = new Nanoseconds(604800000000000L);
        dur[1] = new Microseconds(604800000000L);
        dur[2] = new Milliseconds(604800000);
        dur[3] = new Seconds(604800);
        dur[4] = new Minutes(10080);
        dur[5] = new Hours(168);
        dur[6] = new Days(7);
        dur[7] = new Weeks(1);
        for (int i = 0; i < dur.length; ++i)
        {
            Assert.assertEquals(0, BigDecimal.TEN.compareTo(dur[i].divide(new Seconds(60480))));
        }
    }

    /**
     * Test dividing by different number types.
     */
    @Test
    public void testDivideNumber()
    {
        Duration[] dur = new Duration[8];
        dur[0] = new Nanoseconds(604800000000000L);
        dur[1] = new Microseconds(604800000000L);
        dur[2] = new Milliseconds(604800000);
        dur[3] = new Seconds(604800);
        dur[4] = new Minutes(10080);
        dur[5] = new Hours(168);
        dur[6] = new Days(7);
        dur[7] = new Weeks(1);
        for (int i = 5; i < dur.length; ++i)
        {
            Assert.assertEquals(Duration.create(dur[i].getClass(), new Seconds(60480)), dur[i].divide(BigDecimal.TEN));
            Assert.assertEquals(Duration.create(dur[i].getClass(), new Seconds(60480)), dur[i].divide(10L));
            Assert.assertEquals(Duration.create(dur[i].getClass(), new Seconds(60480)), dur[i].divide(10.));
        }
    }

    /**
     * Test {@link #equals(Object)}.
     */
    @Test
    public void testEquals()
    {
        Duration[] dur = new Duration[16];
        dur[0] = new Nanoseconds(604800000000000L);
        dur[1] = new Microseconds(604800000000L);
        dur[2] = new Milliseconds(604800000);
        dur[3] = new Seconds(604800);
        dur[4] = new Minutes(10080);
        dur[5] = new Hours(168);
        dur[6] = new Days(7);
        dur[7] = new Weeks(1);
        for (int i = 0; i < dur.length / 2; ++i)
        {
            dur[dur.length / 2 + i] = dur[i].clone();
        }
        for (int i = 0; i < dur.length; ++i)
        {
            for (int j = 0; j < dur.length; ++j)
            {
                if ((j - i) * 2 % dur.length == 0)
                {
                    Assert.assertTrue(shouldBeEqual(dur, i, j), dur[i].equals(dur[j]));
                }
                else
                {
                    Assert.assertFalse(shouldBeUnequal(dur, i, j), dur[i].equals(dur[j]));
                }
            }
        }
        Assert.assertFalse(dur[0].equals(new Nanoseconds(0)));
        Assert.assertFalse(dur[1].equals(new Microseconds(0)));
        Assert.assertFalse(dur[2].equals(new Milliseconds(0)));
        Assert.assertFalse(dur[3].equals(new Seconds(0)));
        Assert.assertFalse(dur[4].equals(new Minutes(0)));
        Assert.assertFalse(dur[5].equals(new Hours(0)));
        Assert.assertFalse(dur[6].equals(new Days(0)));
        Assert.assertFalse(dur[7].equals(new Weeks(0)));

        dur = new Duration[4];
        dur[0] = new Months(24);
        dur[1] = new Years(2);
        for (int i = 0; i < dur.length / 2; ++i)
        {
            dur[dur.length / 2 + i] = dur[i].clone();
        }
        for (int i = 0; i < dur.length; ++i)
        {
            for (int j = 0; j < dur.length; ++j)
            {
                if ((j - i) * 2 % dur.length == 0)
                {
                    Assert.assertTrue(shouldBeEqual(dur, i, j), dur[i].equals(dur[j]));
                }
                else
                {
                    Assert.assertFalse(shouldBeUnequal(dur, i, j), dur[i].equals(dur[j]));
                }
            }
        }
        Assert.assertFalse(dur[0].equals(new Months(0)));
        Assert.assertFalse(dur[1].equals(new Years(0)));
    }

    /**
     * Test {@link Duration#equalsIgnoreUnits(Duration)}.
     */
    @Test
    public void testEqualsIgnoreUnits()
    {
        Duration[] dur = new Duration[16];
        dur[0] = new Nanoseconds(604800000000000L);
        dur[1] = new Microseconds(604800000000L);
        dur[2] = new Milliseconds(604800000);
        dur[3] = new Seconds(604800);
        dur[4] = new Minutes(10080);
        dur[5] = new Hours(168);
        dur[6] = new Days(7);
        dur[7] = new Weeks(1);
        for (int i = 0; i < dur.length / 2; ++i)
        {
            dur[dur.length / 2 + i] = dur[i].clone();
        }
        for (int i = 0; i < dur.length; ++i)
        {
            for (int j = 0; j < dur.length; ++j)
            {
                Assert.assertTrue(shouldBeEqual(dur, i, j), dur[i].equalsIgnoreUnits(dur[j]));
            }
        }
        Assert.assertFalse(dur[0].equalsIgnoreUnits(new Nanoseconds(0)));
        Assert.assertFalse(dur[1].equalsIgnoreUnits(new Microseconds(0)));
        Assert.assertFalse(dur[2].equalsIgnoreUnits(new Milliseconds(0)));
        Assert.assertFalse(dur[3].equalsIgnoreUnits(new Seconds(0)));
        Assert.assertFalse(dur[4].equalsIgnoreUnits(new Minutes(0)));
        Assert.assertFalse(dur[5].equalsIgnoreUnits(new Hours(0)));
        Assert.assertFalse(dur[6].equalsIgnoreUnits(new Days(0)));
        Assert.assertFalse(dur[7].equalsIgnoreUnits(new Weeks(0)));

        dur = new Duration[4];
        dur[0] = new Months(24);
        dur[1] = new Years(2);
        for (int i = 0; i < dur.length / 2; ++i)
        {
            dur[dur.length / 2 + i] = dur[i].clone();
        }
        for (int i = 0; i < dur.length; ++i)
        {
            for (int j = 0; j < dur.length; ++j)
            {
                Assert.assertTrue(shouldBeEqual(dur, i, j), dur[i].equalsIgnoreUnits(dur[j]));
            }
        }
        Assert.assertFalse(dur[0].equalsIgnoreUnits(new Months(0)));
        Assert.assertFalse(dur[1].equalsIgnoreUnits(new Years(0)));
    }

    /** Test {@link Duration#fromChronoUnit(ChronoUnit)}. */
    @Test
    public void testFromChronoUnit()
    {
        Assert.assertEquals(Nanoseconds.class, Duration.fromChronoUnit(ChronoUnit.NANOS));
        Assert.assertEquals(Microseconds.class, Duration.fromChronoUnit(ChronoUnit.MICROS));
        Assert.assertEquals(Milliseconds.class, Duration.fromChronoUnit(ChronoUnit.MILLIS));
        Assert.assertEquals(Seconds.class, Duration.fromChronoUnit(ChronoUnit.SECONDS));
        Assert.assertEquals(Minutes.class, Duration.fromChronoUnit(ChronoUnit.MINUTES));
        Assert.assertEquals(Hours.class, Duration.fromChronoUnit(ChronoUnit.HOURS));
        Assert.assertEquals(Days.class, Duration.fromChronoUnit(ChronoUnit.DAYS));
        Assert.assertEquals(Weeks.class, Duration.fromChronoUnit(ChronoUnit.WEEKS));
        Assert.assertEquals(Months.class, Duration.fromChronoUnit(ChronoUnit.MONTHS));
        Assert.assertEquals(Years.class, Duration.fromChronoUnit(ChronoUnit.YEARS));
    }

    /**
     * Test conversions from ISO8601 strings.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testFromISO8601String() throws ParseException
    {
        Assert.assertEquals(Years.ONE, Duration.fromISO8601String("P1Y"));
        Assert.assertEquals(Months.ONE, Duration.fromISO8601String("P1M"));
        Assert.assertEquals(Weeks.ONE, Duration.fromISO8601String("P1W"));
        Assert.assertEquals(Days.ONE, Duration.fromISO8601String("P1D"));
        Assert.assertEquals(Hours.ONE, Duration.fromISO8601String("PT1H"));
        Assert.assertEquals(Minutes.ONE, Duration.fromISO8601String("PT1M"));
        Assert.assertEquals(Seconds.ONE, Duration.fromISO8601String("PT1S"));

        Assert.assertEquals(new Years(10), Duration.fromISO8601String("P10Y"));
        Assert.assertEquals(new Months(10), Duration.fromISO8601String("P10M"));
        Assert.assertEquals(new Weeks(10), Duration.fromISO8601String("P10W"));
        Assert.assertEquals(new Days(10), Duration.fromISO8601String("P10D"));
        Assert.assertEquals(new Hours(10), Duration.fromISO8601String("PT10H"));
        Assert.assertEquals(new Minutes(10), Duration.fromISO8601String("PT10M"));
        Assert.assertEquals(new Seconds(10), Duration.fromISO8601String("PT10S"));

        Assert.assertEquals(new Years(new BigDecimal("2.5")), Duration.fromISO8601String("P2.5Y"));
        Assert.assertEquals(new Months(new BigDecimal("2.5")), Duration.fromISO8601String("P2.5M"));
        Assert.assertEquals(new Weeks(new BigDecimal("2.5")), Duration.fromISO8601String("P2.5W"));
        Assert.assertEquals(new Days(new BigDecimal("2.5")), Duration.fromISO8601String("P2.5D"));
        Assert.assertEquals(new Hours(new BigDecimal("2.5")), Duration.fromISO8601String("PT2.5H"));
        Assert.assertEquals(new Minutes(new BigDecimal("2.5")), Duration.fromISO8601String("PT2.5M"));
        Assert.assertEquals(new Seconds(new BigDecimal("2.5")), Duration.fromISO8601String("PT2.5S"));

        Assert.assertTrue(new Years(new BigDecimal("2.5")).compareTo(Duration.fromISO8601String("P2Y6M")) == 0);
        Assert.assertTrue(new Days(8).compareTo(Duration.fromISO8601String("P1W1D")) == 0);
        Assert.assertTrue(new Hours(8 * 24 + 2).compareTo(Duration.fromISO8601String("P1W1DT2H")) == 0);
        Assert.assertTrue(new Minutes((8 * 24 + 2) * 60 + 4).compareTo(Duration.fromISO8601String("P1W1DT2H4M")) == 0);
        Assert.assertTrue(
                new Seconds(((8 * 24 + 2) * 60 + 4) * 60 + 13).compareTo(Duration.fromISO8601String("P1W1DT2H4M13S")) == 0);
    }

    /**
     * Test conversions from ISO8601 strings.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testFromISO8601StringBad() throws ParseException
    {
        try
        {
            Duration.fromISO8601String("P");
            Assert.fail();
        }
        catch (ParseException e)
        {
            LOGGER.info(e);
        }
        try
        {
            Duration.fromISO8601String("1Y1M");
            Assert.fail();
        }
        catch (ParseException e)
        {
            LOGGER.info(e);
        }
        try
        {
            Duration.fromISO8601String("P1Y1");
            Assert.fail();
        }
        catch (ParseException e)
        {
            LOGGER.info(e);
        }
        try
        {
            Duration.fromISO8601String("P111");
            Assert.fail();
        }
        catch (ParseException e)
        {
            LOGGER.info(e);
        }
        try
        {
            Duration.fromISO8601String("P111A");
            Assert.fail();
        }
        catch (ParseException e)
        {
            LOGGER.info(e);
        }
        try
        {
            Duration.fromISO8601String("P111YD");
            Assert.fail();
        }
        catch (ParseException e)
        {
            LOGGER.info(e);
        }
        try
        {
            Duration.fromISO8601String("P1Y1D");
            Assert.fail();
        }
        catch (UnsupportedOperationException e)
        {
            LOGGER.info(e);
        }
        try
        {
            Duration.fromISO8601String("P1Y1T");
            Assert.fail();
        }
        catch (ParseException e)
        {
            LOGGER.info(e);
        }
        try
        {
            Duration.fromISO8601String("P1WT12HT");
            Assert.fail();
        }
        catch (ParseException e)
        {
            LOGGER.info(e);
        }
    }

    /** Test {@link Duration#getChronoUnit()}. */
    @Test
    public void testGetChronoUnit()
    {
        Assert.assertEquals(ChronoUnit.NANOS, new Nanoseconds(0L).getChronoUnit());
        Assert.assertEquals(ChronoUnit.MICROS, new Microseconds(0L).getChronoUnit());
        Assert.assertEquals(ChronoUnit.MILLIS, new Milliseconds(0L).getChronoUnit());
        Assert.assertEquals(ChronoUnit.SECONDS, new Seconds(0L).getChronoUnit());
        Assert.assertEquals(ChronoUnit.MINUTES, new Minutes(0L).getChronoUnit());
        Assert.assertEquals(ChronoUnit.HOURS, new Hours(0L).getChronoUnit());
        Assert.assertEquals(ChronoUnit.DAYS, new Days(0L).getChronoUnit());
        Assert.assertEquals(ChronoUnit.WEEKS, new Weeks(0L).getChronoUnit());
        Assert.assertEquals(ChronoUnit.MONTHS, new Months(0L).getChronoUnit());
        Assert.assertEquals(ChronoUnit.YEARS, new Years(0L).getChronoUnit());
    }

    /**
     * Test {@link Duration#getMagnitude()} and {@link Duration#intValue()} and
     * {@link Duration#longValue()}.
     */
    @Test
    public void testGetMagnitude()
    {
        Assert.assertEquals(BigDecimal.valueOf(Long.MAX_VALUE, -1),
                new Seconds(BigDecimal.valueOf(Long.MAX_VALUE, -1)).getMagnitude());
        Assert.assertEquals(Long.MAX_VALUE, new Seconds(BigDecimal.valueOf(Long.MAX_VALUE, 0)).longValue());
        Assert.assertEquals(Integer.MAX_VALUE, new Seconds(BigDecimal.valueOf(Integer.MAX_VALUE, 0)).intValue());
        Assert.assertEquals(Integer.MAX_VALUE, new Seconds(BigDecimal.valueOf(Integer.MAX_VALUE, 0)).longValue());
    }

    /**
     * Test {@link #hashCode()}.
     */
    @Test
    public void testHashCode()
    {
        Assert.assertEquals(new Nanoseconds(60).hashCode(), new Nanoseconds(60).hashCode());
        Assert.assertFalse(new Nanoseconds(60).hashCode() == new Nanoseconds(59).hashCode());
        Assert.assertFalse(new Nanoseconds(60).hashCode() == new Nanoseconds(1).hashCode());
        Assert.assertFalse(new Nanoseconds(1).hashCode() == new Minutes(1).hashCode());
        Assert.assertEquals(new Microseconds(60).hashCode(), new Microseconds(60).hashCode());
        Assert.assertFalse(new Microseconds(60).hashCode() == new Microseconds(59).hashCode());
        Assert.assertFalse(new Microseconds(60).hashCode() == new Microseconds(1).hashCode());
        Assert.assertEquals(new Milliseconds(60).hashCode(), new Milliseconds(60).hashCode());
        Assert.assertFalse(new Milliseconds(60).hashCode() == new Milliseconds(59).hashCode());
        Assert.assertFalse(new Milliseconds(60).hashCode() == new Milliseconds(1).hashCode());
        Assert.assertEquals(new Seconds(60).hashCode(), new Seconds(60).hashCode());
        Assert.assertFalse(new Seconds(60).hashCode() == new Seconds(59).hashCode());
        Assert.assertFalse(new Seconds(60).hashCode() == new Seconds(1).hashCode());
        Assert.assertEquals(new Minutes(60).hashCode(), new Minutes(60).hashCode());
        Assert.assertFalse(new Minutes(60).hashCode() == new Minutes(59).hashCode());
        Assert.assertFalse(new Minutes(60).hashCode() == new Minutes(1).hashCode());
        Assert.assertEquals(new Hours(60).hashCode(), new Hours(60).hashCode());
        Assert.assertFalse(new Hours(60).hashCode() == new Hours(59).hashCode());
        Assert.assertFalse(new Hours(60).hashCode() == new Hours(1).hashCode());
        Assert.assertEquals(new Days(60).hashCode(), new Days(60).hashCode());
        Assert.assertFalse(new Days(60).hashCode() == new Days(59).hashCode());
        Assert.assertFalse(new Days(60).hashCode() == new Days(1).hashCode());
        Assert.assertEquals(new Weeks(60).hashCode(), new Weeks(60).hashCode());
        Assert.assertFalse(new Weeks(60).hashCode() == new Weeks(59).hashCode());
        Assert.assertFalse(new Weeks(60).hashCode() == new Weeks(1).hashCode());
        Assert.assertEquals(new Months(60).hashCode(), new Months(60).hashCode());
        Assert.assertFalse(new Months(60).hashCode() == new Months(59).hashCode());
        Assert.assertFalse(new Months(60).hashCode() == new Months(1).hashCode());
        Assert.assertEquals(new Years(60).hashCode(), new Years(60).hashCode());
        Assert.assertFalse(new Years(60).hashCode() == new Years(59).hashCode());
        Assert.assertFalse(new Years(60).hashCode() == new Years(1).hashCode());
    }

    /**
     * Test {@link Duration#intValue()} with overflow.
     */
    @Test(expected = ArithmeticException.class)
    public void testIntValueOverflow()
    {
        new Seconds(BigDecimal.valueOf(Long.MAX_VALUE, 0)).intValue();
    }

    /** Test for {@link Duration#isConvertibleTo(Class)}. */
    @Test
    public void testIsConvertibleTo()
    {
        Duration[] durs1 = new Duration[] { new Nanoseconds(1L), new Microseconds(1L), new Milliseconds(1L), new Seconds(1L),
            new Minutes(1L), new Hours(1L), new Days(1L), new Weeks(1L) };
        Duration[] durs2 = new Duration[] { new Months(1L), new Years(1L) };
        for (Duration dur1 : durs1)
        {
            for (Duration dur2 : durs1)
            {
                Assert.assertTrue(dur1.isConvertibleTo(dur2.getClass()));
            }
        }
        for (Duration dur1 : durs2)
        {
            for (Duration dur2 : durs2)
            {
                Assert.assertTrue(dur1.isConvertibleTo(dur2.getClass()));
            }
        }
        for (Duration dur1 : durs1)
        {
            for (Duration dur2 : durs2)
            {
                Assert.assertFalse(dur1.isConvertibleTo(dur2.getClass()));
                Assert.assertFalse(dur2.isConvertibleTo(dur1.getClass()));
            }
        }
    }

    /** Test for {@link Duration#isConvertibleTo(Class)} with zero values. */
    @Test
    public void testIsConvertibleToZeroes()
    {
        Duration[] durs1 = new Duration[] { new Nanoseconds(0L), new Microseconds(0L), new Milliseconds(0L), new Seconds(0L),
            new Minutes(0L), new Hours(0L), new Days(0L), new Weeks(0L), new Months(0L), new Years(0L) };
        for (Duration dur1 : durs1)
        {
            for (Duration dur2 : durs1)
            {
                Assert.assertTrue(dur1.isConvertibleTo(dur2.getClass()));
            }
        }
    }

    /**
     * Test the label and toString methods.
     */
    @Test
    public void testLabels()
    {
        Duration[] arr = new Duration[10];
        arr[0] = new Nanoseconds(604800000000000L);
        arr[1] = new Microseconds(604800000000L);
        arr[2] = new Milliseconds(604800000);
        arr[3] = new Seconds(604800);
        arr[4] = new Minutes(10080);
        arr[5] = new Hours(168);
        arr[6] = new Days(7);
        arr[7] = new Weeks(1);
        arr[8] = new Months(24);
        arr[9] = new Years(2);

        for (Duration dur : arr)
        {
            Assert.assertTrue(dur.getLongLabel(true).length() > 0);
            Assert.assertTrue(dur.getLongLabel(false).length() > 0);
            Assert.assertTrue(dur.getShortLabel(true).length() > 0);
            Assert.assertTrue(dur.getShortLabel(false).length() > 0);
            Assert.assertTrue(dur.toString().length() > 0);
        }
    }

    /**
     * Test {@link Duration#longValue()} with overflow.
     */
    @Test(expected = ArithmeticException.class)
    public void testLongValueOverflow()
    {
        new Seconds(BigDecimal.valueOf(Long.MAX_VALUE, 0).add(BigDecimal.ONE)).longValue();
    }

    /**
     * Test multiplying by different number types.
     */
    @Test
    public void testMultiply()
    {
        Assert.assertEquals(new Nanoseconds(3600), new Nanoseconds(60).multiply(60));
        Assert.assertEquals(new Microseconds(3600), new Microseconds(60).multiply(60));
        Assert.assertEquals(new Milliseconds(3600), new Milliseconds(60).multiply(60));
        Assert.assertEquals(new Seconds(3600), new Seconds(60).multiply(60));
        Assert.assertEquals(new Minutes(3600), new Minutes(60).multiply(60));
        Assert.assertEquals(new Hours(3600), new Hours(60).multiply(60));
        Assert.assertEquals(new Days(3600), new Days(60).multiply(60));
        Assert.assertEquals(new Weeks(3600), new Weeks(60).multiply(60));
        Assert.assertEquals(new Months(3600), new Months(60).multiply(60));
        Assert.assertEquals(new Years(3600), new Years(60).multiply(60));

        Assert.assertEquals(new Nanoseconds(3630), new Nanoseconds(60).multiply(60.5));
        Assert.assertEquals(new Microseconds(3630), new Microseconds(60).multiply(60.5));
        Assert.assertEquals(new Milliseconds(3630), new Milliseconds(60).multiply(60.5));
        Assert.assertEquals(new Seconds(3630), new Seconds(60).multiply(60.5));
        Assert.assertEquals(new Minutes(3630), new Minutes(60).multiply(60.5));
        Assert.assertEquals(new Hours(3630), new Hours(60).multiply(60.5));
        Assert.assertEquals(new Days(3630), new Days(60).multiply(60.5));
        Assert.assertEquals(new Weeks(3630), new Weeks(60).multiply(60.5));
        Assert.assertEquals(new Months(3630), new Months(60).multiply(60.5));
        Assert.assertEquals(new Years(3630), new Years(60).multiply(60.5));

        String sixtyAndAHalf = "60.5";
        Assert.assertEquals(new Nanoseconds(3630), new Nanoseconds(60).multiply(new BigDecimal(sixtyAndAHalf)));
        Assert.assertEquals(new Microseconds(3630), new Microseconds(60).multiply(new BigDecimal(sixtyAndAHalf)));
        Assert.assertEquals(new Milliseconds(3630), new Milliseconds(60).multiply(new BigDecimal(sixtyAndAHalf)));
        Assert.assertEquals(new Seconds(3630), new Seconds(60).multiply(new BigDecimal(sixtyAndAHalf)));
        Assert.assertEquals(new Minutes(3630), new Minutes(60).multiply(new BigDecimal(sixtyAndAHalf)));
        Assert.assertEquals(new Hours(3630), new Hours(60).multiply(new BigDecimal(sixtyAndAHalf)));
        Assert.assertEquals(new Days(3630), new Days(60).multiply(new BigDecimal(sixtyAndAHalf)));
        Assert.assertEquals(new Weeks(3630), new Weeks(60).multiply(new BigDecimal(sixtyAndAHalf)));
        Assert.assertEquals(new Months(3630), new Months(60).multiply(new BigDecimal(sixtyAndAHalf)));
        Assert.assertEquals(new Years(3630), new Years(60).multiply(new BigDecimal(sixtyAndAHalf)));
    }

    /**
     * Test constructing durations with large numbers.
     */
    @Test
    public void testOverflowMax()
    {
        // Create a duration with a magnitude of the maximum long value.
        Assert.assertEquals(9223372036854775807L, new Seconds(BigDecimal.valueOf(Long.MAX_VALUE)).longValue());

        BigDecimal expected;

        // Create a duration with a magnitude of the maximum long value plus 1.
        expected = new BigDecimal("9223372036854775810");
        BigDecimal maxLongPlusOne = new Seconds(BigDecimal.valueOf(Long.MAX_VALUE).add(BigDecimal.ONE)).getMagnitude();
        Assert.assertEquals(0, expected.compareTo(maxLongPlusOne));

        // Create a duration with a magnitude of the maximum long value times
        // 10.
        expected = new BigDecimal("92233720368547758070");
        BigDecimal maxLongTimesTen = new Seconds(BigDecimal.valueOf(Long.MAX_VALUE).multiply(BigDecimal.valueOf(10.)))
                .getMagnitude();
        Assert.assertEquals(0, expected.compareTo(maxLongTimesTen));
    }

    /**
     * Test constructing durations with large negative numbers.
     */
    @Test
    public void testOverflowMin()
    {
        // Create a duration with a magnitude of the maximum long value.
        Assert.assertEquals(-9223372036854775808L, new Seconds(BigDecimal.valueOf(Long.MIN_VALUE)).longValue());

        BigDecimal expected;

        // Create a duration with a magnitude of the minimum long value minus 1.
        expected = new BigDecimal("-9223372036854775810");
        BigDecimal minLongMinusOne = new Seconds(BigDecimal.valueOf(Long.MIN_VALUE).subtract(BigDecimal.ONE)).getMagnitude();
        Assert.assertEquals(0, expected.compareTo(minLongMinusOne));

        // Create a duration with a magnitude of the maximum long value times
        // 10.
        expected = new BigDecimal("-92233720368547758080");
        BigDecimal minLongTimesTen = new Seconds(BigDecimal.valueOf(Long.MIN_VALUE).multiply(BigDecimal.TEN)).getMagnitude();
        Assert.assertEquals(0, expected.compareTo(minLongTimesTen));
    }

    /** Test conversions to ISO8601 strings. */
    @Test
    public void testToISO8601String()
    {
        Assert.assertEquals("P1Y", Years.ONE.toISO8601String());
        Assert.assertEquals("P1M", Months.ONE.toISO8601String());
        Assert.assertEquals("P1W", Weeks.ONE.toISO8601String());
        Assert.assertEquals("P1D", Days.ONE.toISO8601String());
        Assert.assertEquals("PT1H", Hours.ONE.toISO8601String());
        Assert.assertEquals("PT1M", Minutes.ONE.toISO8601String());
        Assert.assertEquals("PT1S", Seconds.ONE.toISO8601String());
        Assert.assertEquals("PT0.001S", Milliseconds.ONE.toISO8601String());
        Assert.assertEquals("PT0.000001S", Microseconds.ONE.toISO8601String());
        Assert.assertEquals("PT0.000000001S", Nanoseconds.ONE.toISO8601String());

        Assert.assertEquals("P10Y", new Years(10).toISO8601String());
        Assert.assertEquals("P10M", new Months(10).toISO8601String());
        Assert.assertEquals("P10W", new Weeks(10).toISO8601String());
        Assert.assertEquals("P10D", new Days(10).toISO8601String());
        Assert.assertEquals("PT10H", new Hours(10).toISO8601String());
        Assert.assertEquals("PT10M", new Minutes(10).toISO8601String());
        Assert.assertEquals("PT10S", new Seconds(10).toISO8601String());
        Assert.assertEquals("PT0.01S", new Milliseconds(10).toISO8601String());
        Assert.assertEquals("PT0.00001S", new Microseconds(10).toISO8601String());
        Assert.assertEquals("PT0.00000001S", new Nanoseconds(10).toISO8601String());

        Assert.assertEquals("P2.5Y", new Years(new BigDecimal("2.5")).toISO8601String());
        Assert.assertEquals("P2.5M", new Months(new BigDecimal("2.5")).toISO8601String());
        Assert.assertEquals("P2.5W", new Weeks(new BigDecimal("2.5")).toISO8601String());
        Assert.assertEquals("P2.5D", new Days(new BigDecimal("2.5")).toISO8601String());
        Assert.assertEquals("PT2.5H", new Hours(new BigDecimal("2.5")).toISO8601String());
        Assert.assertEquals("PT2.5M", new Minutes(new BigDecimal("2.5")).toISO8601String());
        Assert.assertEquals("PT2.5S", new Seconds(new BigDecimal("2.5")).toISO8601String());
        Assert.assertEquals("PT0.0025S", new Milliseconds(new BigDecimal("2.5")).toISO8601String());
        Assert.assertEquals("PT0.0000025S", new Microseconds(new BigDecimal("2.5")).toISO8601String());
    }

    /**
     * Test {@link Duration#toLongLabelString()}.
     */
    @Test
    public void testToLongLabelString()
    {
        Assert.assertEquals("2 nanoseconds", new Nanoseconds(2).toLongLabelString());
        Assert.assertEquals("1 nanosecond", new Nanoseconds(1).toLongLabelString());
        Assert.assertEquals("0.1 nanoseconds", new Nanoseconds(1, 1).toLongLabelString());

        Assert.assertEquals("2 microseconds", new Microseconds(2).toLongLabelString());
        Assert.assertEquals("1 microsecond", new Microseconds(1).toLongLabelString());
        Assert.assertEquals("0.1 microseconds", new Microseconds(1, 1).toLongLabelString());

        Assert.assertEquals("2 milliseconds", new Milliseconds(2).toLongLabelString());
        Assert.assertEquals("1 millisecond", new Milliseconds(1).toLongLabelString());
        Assert.assertEquals("0.1 milliseconds", new Milliseconds(1, 1).toLongLabelString());

        Assert.assertEquals("2 seconds", new Seconds(2).toLongLabelString());
        Assert.assertEquals("1 second", new Seconds(1).toLongLabelString());
        Assert.assertEquals("0.1 seconds", new Seconds(1, 1).toLongLabelString());

        Assert.assertEquals("2 minutes", new Minutes(2).toLongLabelString());
        Assert.assertEquals("1 minute", new Minutes(1).toLongLabelString());
        Assert.assertEquals("0.1 minutes", new Minutes(1, 1).toLongLabelString());

        Assert.assertEquals("2 hours", new Hours(2).toLongLabelString());
        Assert.assertEquals("1 hour", new Hours(1).toLongLabelString());
        Assert.assertEquals("0.1 hours", new Hours(1, 1).toLongLabelString());

        Assert.assertEquals("2 days", new Days(2).toLongLabelString());
        Assert.assertEquals("1 day", new Days(1).toLongLabelString());
        Assert.assertEquals("0.1 days", new Days(1, 1).toLongLabelString());

        Assert.assertEquals("2 weeks", new Weeks(2).toLongLabelString());
        Assert.assertEquals("1 week", new Weeks(1).toLongLabelString());
        Assert.assertEquals("0.1 weeks", new Weeks(1, 1).toLongLabelString());

        Assert.assertEquals("2 months", new Months(2).toLongLabelString());
        Assert.assertEquals("1 month", new Months(1).toLongLabelString());
        Assert.assertEquals("0.1 months", new Months(1, 1).toLongLabelString());

        Assert.assertEquals("2 years", new Years(2).toLongLabelString());
        Assert.assertEquals("1 year", new Years(1).toLongLabelString());
        Assert.assertEquals("0.1 years", new Years(1, 1).toLongLabelString());
    }

    /** Test {@link Duration#toPrettyString()}. */
    @Test
    public void testToPrettyString()
    {
        Assert.assertEquals("0.001s", new Milliseconds(1).toPrettyString());
        Assert.assertEquals("1 sec", new Milliseconds(1000).toPrettyString());
        Assert.assertEquals("1.001s", new Milliseconds(1001).toPrettyString());
        Assert.assertEquals("1 min", new Milliseconds(60000).toPrettyString());
        Assert.assertEquals("1m 00.001s", new Milliseconds(60001).toPrettyString());
        Assert.assertEquals("1 hour", new Milliseconds(3600000).toPrettyString());
        Assert.assertEquals("1h 00m 00.001s", new Milliseconds(3600001).toPrettyString());
        Assert.assertEquals("1 day", new Milliseconds(86400000).toPrettyString());
        Assert.assertEquals("1d 00h 00m 00.001s", new Milliseconds(86400001).toPrettyString());
        Assert.assertEquals("0.002s", new Milliseconds(2).toPrettyString());
        Assert.assertEquals("2 secs", new Milliseconds(2000).toPrettyString());
        Assert.assertEquals("2 mins", new Milliseconds(120000).toPrettyString());
        Assert.assertEquals("2 hours", new Milliseconds(7200000).toPrettyString());
        Assert.assertEquals("2 days", new Milliseconds(172800000).toPrettyString());
        Assert.assertEquals("1h 00m 01s", new Milliseconds(3601000).toPrettyString());
        Assert.assertEquals("1d 00h 00m 00.001s", new Milliseconds(86400001).toPrettyString());
        Assert.assertEquals("1d 01h 01m 01.001s", new Milliseconds(90061001).toPrettyString());
    }

    /**
     * Test {@link Duration#toShortLabelString()}.
     */
    @Test
    public void testToShortLabelString()
    {
        Assert.assertEquals("2 ns", new Nanoseconds(2).toShortLabelString());
        Assert.assertEquals("1 ns", new Nanoseconds(1).toShortLabelString());
        Assert.assertEquals("0.1 ns", new Nanoseconds(1, 1).toShortLabelString());

        Assert.assertEquals("2 μs", new Microseconds(2).toShortLabelString());
        Assert.assertEquals("1 μs", new Microseconds(1).toShortLabelString());
        Assert.assertEquals("0.1 μs", new Microseconds(1, 1).toShortLabelString());

        Assert.assertEquals("2 ms", new Milliseconds(2).toShortLabelString());
        Assert.assertEquals("1 ms", new Milliseconds(1).toShortLabelString());
        Assert.assertEquals("0.1 ms", new Milliseconds(1, 1).toShortLabelString());

        Assert.assertEquals("2 s", new Seconds(2).toShortLabelString());
        Assert.assertEquals("1 s", new Seconds(1).toShortLabelString());
        Assert.assertEquals("0.1 s", new Seconds(1, 1).toShortLabelString());

        Assert.assertEquals("2 mins", new Minutes(2).toShortLabelString());
        Assert.assertEquals("1 min", new Minutes(1).toShortLabelString());
        Assert.assertEquals("0.1 mins", new Minutes(1, 1).toShortLabelString());

        Assert.assertEquals("2 hrs", new Hours(2).toShortLabelString());
        Assert.assertEquals("1 hr", new Hours(1).toShortLabelString());
        Assert.assertEquals("0.1 hrs", new Hours(1, 1).toShortLabelString());

        Assert.assertEquals("2 days", new Days(2).toShortLabelString());
        Assert.assertEquals("1 day", new Days(1).toShortLabelString());
        Assert.assertEquals("0.1 days", new Days(1, 1).toShortLabelString());

        Assert.assertEquals("2 wks", new Weeks(2).toShortLabelString());
        Assert.assertEquals("1 wk", new Weeks(1).toShortLabelString());
        Assert.assertEquals("0.1 wks", new Weeks(1, 1).toShortLabelString());

        Assert.assertEquals("2 mons", new Months(2).toShortLabelString());
        Assert.assertEquals("1 mon", new Months(1).toShortLabelString());
        Assert.assertEquals("0.1 mons", new Months(1, 1).toShortLabelString());

        Assert.assertEquals("2 yrs", new Years(2).toShortLabelString());
        Assert.assertEquals("1 yr", new Years(1).toShortLabelString());
        Assert.assertEquals("0.1 yrs", new Years(1, 1).toShortLabelString());
    }

    /**
     * Test unit conversion.
     */
    @Test
    public void testUnits()
    {
        Duration[] dur = new Duration[8];
        dur[0] = new Nanoseconds(604800000000000L);
        dur[1] = new Microseconds(604800000000L);
        dur[2] = new Milliseconds(604800000);
        dur[3] = new Seconds(604800);
        dur[4] = new Minutes(10080);
        dur[5] = new Hours(168);
        dur[6] = new Days(7);
        dur[7] = new Weeks(1);

        for (int i = 0; i < dur.length; ++i)
        {
            Assert.assertEquals(604800000000000L, new Nanoseconds(dur[i]).longValue());
            Assert.assertEquals(604800000000L, new Microseconds(dur[i]).longValue());
            Assert.assertEquals(604800000, new Milliseconds(dur[i]).intValue());
            Assert.assertEquals(604800, new Seconds(dur[i]).intValue());
            Assert.assertEquals(10080, new Minutes(dur[i]).intValue());
            Assert.assertEquals(168, new Hours(dur[i]).intValue());
            Assert.assertEquals(7, new Days(dur[i]).intValue());
            Assert.assertEquals(1, new Weeks(dur[i]).intValue());
        }

        dur = new Duration[2];
        dur[0] = new Months(24);
        dur[1] = new Years(2);

        for (int i = 0; i < dur.length; ++i)
        {
            Assert.assertEquals(24, new Months(dur[i]).intValue());
            Assert.assertEquals(2, new Years(dur[i]).intValue());
        }
    }

    /**
     * Message that two durations should be equal.
     *
     * @param dur The array of durations.
     * @param i The first index.
     * @param j The seconds index.
     * @return The message.
     */
    private String shouldBeEqual(Duration[] dur, int i, int j)
    {
        return dur[i] + " and " + dur[j] + " should have been equal.";
    }

    /**
     * Message that two durations should not be equal.
     *
     * @param dur The array of durations.
     * @param i The first index.
     * @param j The seconds index.
     * @return The message.
     */
    private String shouldBeUnequal(Duration[] dur, int i, int j)
    {
        return dur[i] + " and " + dur[j] + " should have been unequal.";
    }
}
