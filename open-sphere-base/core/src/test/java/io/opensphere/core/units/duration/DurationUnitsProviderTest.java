package io.opensphere.core.units.duration;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.easymock.EasyMock;
import org.junit.Test;

import io.opensphere.core.units.InconvertibleUnits;
import io.opensphere.core.units.InvalidUnitsException;
import io.opensphere.core.units.UnitsProvider;
import io.opensphere.core.util.DateTimeFormats;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.time.TimelineUtilities;
import org.junit.Assert;

/**
 * Test for {@link DurationUnitsProvider}.
 */
public class DurationUnitsProviderTest
{
    /**
     * Test for {@link DurationUnitsProvider#addUnits(Class)} and
     * {@link DurationUnitsProvider#removeUnits(Class)}.
     *
     * @throws InvalidUnitsException If there is a test failure.
     */
    @Test
    public void testAddAndRemoveUnits() throws InvalidUnitsException
    {
        DurationUnitsProvider units = new DurationUnitsProvider();
        @SuppressWarnings("unchecked")
        UnitsProvider.UnitsChangeListener<Duration> listener = EasyMock.createStrictMock(UnitsProvider.UnitsChangeListener.class);
        EasyMock.replay(listener);
        units.addListener(listener);
        // Listener should not be called.
        units.addUnits(Seconds.class);

        EasyMock.reset(listener);
        Collection<Class<? extends Duration>> expected = New.collection();
        expected.add(Milliseconds.class);
        expected.add(Seconds.class);
        expected.add(Minutes.class);
        expected.add(Hours.class);
        expected.add(Days.class);
        expected.add(Weeks.class);
        expected.add(Months.class);
        listener.availableUnitsChanged(Duration.class, expected);
        EasyMock.replay(listener);
        units.removeUnits(Years.class);
        EasyMock.verify(listener);

        EasyMock.reset(listener);
        expected.add(Years.class);
        listener.availableUnitsChanged(Duration.class, expected);
        EasyMock.replay(listener);
        units.addUnits(Years.class);
        EasyMock.verify(listener);
    }

    /**
     * Test for {@link TimelineUtilities#addDuration(Calendar, Duration)} .
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testAddDuration() throws ParseException
    {
        DurationUnitsProvider provider = new DurationUnitsProvider();

        Calendar cal = getCalendar("2014-08-20 12:34:56.789");

        provider.add(cal, new Milliseconds(5));
        Assert.assertEquals(getCalendar("2014-08-20 12:34:56.794"), cal);

        provider.add(cal, new Seconds(5));
        Assert.assertEquals(getCalendar("2014-08-20 12:35:01.794"), cal);

        provider.add(cal, new Minutes(5));
        Assert.assertEquals(getCalendar("2014-08-20 12:40:01.794"), cal);

        provider.add(cal, new Hours(5));
        Assert.assertEquals(getCalendar("2014-08-20 17:40:01.794"), cal);

        provider.add(cal, new Days(5));
        Assert.assertEquals(getCalendar("2014-08-25 17:40:01.794"), cal);

        provider.add(cal, new Milliseconds(new Weeks(4)));
        Assert.assertEquals(getCalendar("2014-09-22 17:40:01.794"), cal);

        provider.add(cal, new Months(5));
        Assert.assertEquals(getCalendar("2015-02-22 17:40:01.794"), cal);

        provider.add(cal, new Years(5));
        Assert.assertEquals(getCalendar("2020-02-22 17:40:01.794"), cal);
    }

    /**
     * Test for {@link DurationUnitsProvider#addUnits(Class)} with an invalid
     * duration type.
     *
     * @throws InvalidUnitsException If the test succeeds.
     */
    @Test(expected = InvalidUnitsException.class)
    public void testAddInvalidUnits1() throws InvalidUnitsException
    {
        new DurationUnitsProvider().addUnits(InvalidUnits1.class);
    }

    /**
     * Test for {@link DurationUnitsProvider#addUnits(Class)} with an invalid
     * duration type.
     *
     * @throws InvalidUnitsException If the test succeeds.
     */
    @Test(expected = InvalidUnitsException.class)
    public void testAddInvalidUnits2() throws InvalidUnitsException
    {
        new DurationUnitsProvider().addUnits(InvalidUnits2.class);
    }

    /**
     * Test for {@link DurationUnitsProvider#convert(Class, Duration)}.
     */
    @Test
    public void testConvert()
    {
        Seconds oneHourInSeconds = new Seconds(3600L);
        Assert.assertSame(oneHourInSeconds, new DurationUnitsProvider().convert(Seconds.class, oneHourInSeconds));
        Assert.assertEquals(oneHourInSeconds, new DurationUnitsProvider().convert(Seconds.class, new Hours(1L)));
    }

    /**
     * Test for
     * {@link DurationUnitsProvider#fromUnitsAndMagnitude(Class, Number)}.
     */
    @Test
    public void testFromTypeAndMagnitude()
    {
        Assert.assertEquals(new Seconds(3600L),
                new DurationUnitsProvider().fromUnitsAndMagnitude(Seconds.class, Integer.valueOf(3600)));
    }

    /**
     * Test for {@link DurationUnitsProvider#getAvailableUnits(boolean)}.
     */
    @Test
    public void testGetDurationTypes()
    {
        Collection<Class<? extends Duration>> expected = New.collection();
        expected.add(Milliseconds.class);
        expected.add(Seconds.class);
        expected.add(Minutes.class);
        expected.add(Hours.class);
        expected.add(Days.class);
        expected.add(Weeks.class);
        expected.add(Months.class);
        expected.add(Years.class);
        Assert.assertEquals(expected, new DurationUnitsProvider().getAvailableUnits(true));
    }

    /**
     * Test for
     * {@link DurationUnitsProvider#getLargestIntegerUnitType(Duration)}.
     */
    @Test
    public void testGetLargestIntegerUnitType()
    {
        DurationUnitsProvider units = new DurationUnitsProvider();
        Assert.assertEquals(new Milliseconds(0), units.getLargestIntegerUnitType(new Seconds(0)));
        Assert.assertEquals(new Seconds(1), units.getLargestIntegerUnitType(new Seconds(1)));
        Assert.assertEquals(new Seconds(59), units.getLargestIntegerUnitType(new Seconds(59)));
        Assert.assertEquals(new Minutes(1), units.getLargestIntegerUnitType(new Seconds(60)));
        Assert.assertEquals(new Minutes(59), units.getLargestIntegerUnitType(new Seconds(59 * 60)));
        Assert.assertEquals(new Hours(1), units.getLargestIntegerUnitType(new Seconds(60 * 60)));
        Assert.assertEquals(new Hours(23), units.getLargestIntegerUnitType(new Seconds(23 * 60 * 60)));
        Assert.assertEquals(new Days(1), units.getLargestIntegerUnitType(new Seconds(24 * 60 * 60)));
        Assert.assertEquals(new Days(6), units.getLargestIntegerUnitType(new Seconds(6 * 24 * 60 * 60)));
        Assert.assertEquals(new Weeks(1), units.getLargestIntegerUnitType(new Seconds(7 * 24 * 60 * 60)));
        Assert.assertEquals(new Seconds(7 * 24 * 60 * 60 - 1),
                units.getLargestIntegerUnitType(new Seconds(7 * 24 * 60 * 60 - 1)));
        Assert.assertEquals(new Minutes(7 * 24 * 60 - 1), units.getLargestIntegerUnitType(new Seconds(7 * 24 * 60 * 60 - 60)));
        Assert.assertEquals(new Hours(7 * 24 - 1), units.getLargestIntegerUnitType(new Seconds(7 * 24 * 60 * 60 - 60 * 60)));
        Assert.assertEquals(new Days(13), units.getLargestIntegerUnitType(new Seconds(2 * 7 * 24 * 60 * 60 - 60 * 60 * 24)));
    }

    /**
     * Test for {@link DurationUnitsProvider#getUnitsWithLongLabel(String)}.
     */
    @Test
    public void testGetUnitsWithLongLabel()
    {
        Assert.assertEquals(Milliseconds.class,
                new DurationUnitsProvider().getUnitsWithLongLabel(Milliseconds.MILLIS_LONG_LABEL));
        Assert.assertEquals(Seconds.class, new DurationUnitsProvider().getUnitsWithLongLabel(Seconds.SECONDS_LONG_LABEL));
        Assert.assertEquals(Minutes.class, new DurationUnitsProvider().getUnitsWithLongLabel(Minutes.MINUTES_LONG_LABEL));
        Assert.assertEquals(Hours.class, new DurationUnitsProvider().getUnitsWithLongLabel(Hours.HOURS_LONG_LABEL));
        Assert.assertEquals(Days.class, new DurationUnitsProvider().getUnitsWithLongLabel(Days.DAYS_LONG_LABEL));
        Assert.assertEquals(Weeks.class, new DurationUnitsProvider().getUnitsWithLongLabel(Weeks.WEEKS_LONG_LABEL));
        Assert.assertEquals(Months.class, new DurationUnitsProvider().getUnitsWithLongLabel(Months.MONTHS_LONG_LABEL));
        Assert.assertEquals(Years.class, new DurationUnitsProvider().getUnitsWithLongLabel(Years.YEARS_LONG_LABEL));
    }

    /**
     * Test for {@link DurationUnitsProvider#getUnitsWithShortLabel(String)}.
     */
    @Test
    public void testGetUnitsWithShortLabel()
    {
        Assert.assertEquals(Milliseconds.class,
                new DurationUnitsProvider().getUnitsWithShortLabel(Milliseconds.MILLIS_SHORT_LABEL));
        Assert.assertEquals(Seconds.class, new DurationUnitsProvider().getUnitsWithShortLabel(Seconds.SECONDS_SHORT_LABEL));
        Assert.assertEquals(Minutes.class, new DurationUnitsProvider().getUnitsWithShortLabel(Minutes.MINUTES_SHORT_LABEL));
        Assert.assertEquals(Hours.class, new DurationUnitsProvider().getUnitsWithShortLabel(Hours.HOURS_SHORT_LABEL));
        Assert.assertEquals(Days.class, new DurationUnitsProvider().getUnitsWithShortLabel(Days.DAYS_SHORT_LABEL));
        Assert.assertEquals(Weeks.class, new DurationUnitsProvider().getUnitsWithShortLabel(Weeks.WEEKS_SHORT_LABEL));
        Assert.assertEquals(Months.class, new DurationUnitsProvider().getUnitsWithShortLabel(Months.MONTHS_SHORT_LABEL));
        Assert.assertEquals(Years.class, new DurationUnitsProvider().getUnitsWithShortLabel(Years.YEARS_SHORT_LABEL));
    }

    /**
     * Test for {@link DurationUnitsProvider#setPreferredUnits(Class)}.
     *
     * @throws InvalidUnitsException If there is a test failure.
     */
    @Test
    public void testSetPreferredUnits() throws InvalidUnitsException
    {
        DurationUnitsProvider units = new DurationUnitsProvider();
        units.setPreferredUnits(Seconds.class);
        @SuppressWarnings("unchecked")
        UnitsProvider.UnitsChangeListener<Duration> listener = EasyMock.createStrictMock(UnitsProvider.UnitsChangeListener.class);
        EasyMock.replay(listener);
        units.addListener(listener);
        // Listener should not be called.
        units.setPreferredUnits(Seconds.class);

        EasyMock.reset(listener);
        listener.preferredUnitsChanged(Weeks.class);
        EasyMock.replay(listener);
        units.setPreferredUnits(Weeks.class);
        EasyMock.verify(listener);
    }

    /**
     * Test for {@link DurationUnitsProvider#toShortLabelString(Duration)} and
     * {@link DurationUnitsProvider#fromShortLabelString(String)}.
     */
    @Test
    public void testToFromShortLabelString()
    {
        DurationUnitsProvider units = new DurationUnitsProvider();
        Duration input = new Seconds(BigDecimal.valueOf(827893745.34785384e45));
        String str = units.toShortLabelString(input);
        Duration output = units.fromShortLabelString(str);
        Assert.assertEquals(input, output);
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

    /** Duration adapter. */
    private abstract static class DurationAdapter extends Duration
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param magnitude The magnitude.
         */
        public DurationAdapter(BigDecimal magnitude)
        {
            super(magnitude);
        }

        /**
         * Constructor.
         *
         * @param magnitude The unscaled magnitude of the duration.
         * @param scale The scale of the duration.
         */
        public DurationAdapter(long magnitude, int scale)
        {
            super(magnitude, scale);
        }

        @Override
        public void addTo(Calendar cal)
        {
        }

        @Override
        public ChronoUnit getChronoUnit()
        {
            return null;
        }

        @Override
        public String getLongLabel(boolean plural)
        {
            return null;
        }

        @Override
        public Class<? extends Duration> getReferenceUnits()
        {
            return null;
        }

        @Override
        public String getShortLabel(boolean plural)
        {
            return null;
        }

        @Override
        public BigDecimal inReferenceUnits()
        {
            return null;
        }

        @Override
        public String toPrettyString()
        {
            return null;
        }

        @Override
        protected char getISO8601Designator()
        {
            return 0;
        }

        @Override
        protected String getISO8601Prefix()
        {
            return null;
        }

        @Override
        protected BigDecimal inReferenceUnits(Class<? extends Duration> expected) throws InconvertibleUnits
        {
            return null;
        }
    }

    /** An invalid duration class. */
    private static class InvalidUnits1 extends DurationAdapter
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param magnitude The magnitude.
         */
        public InvalidUnits1(BigDecimal magnitude)
        {
            super(magnitude);
        }
    }

    /** An invalid duration class. */
    private static class InvalidUnits2 extends DurationAdapter
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param magnitude The unscaled magnitude of the duration.
         * @param scale The scale of the duration.
         *
         * @see BigDecimal
         */
        protected InvalidUnits2(long magnitude, int scale)
        {
            super(magnitude, scale);
        }
    }
}
