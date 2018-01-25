package io.opensphere.core.model.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.DateTimeFormats;

/** Test for {@link TimeInstant}. */
public class TimeInstantTest
{
    /**
     * General test for {@link TimeInstant}.
     */
    @Test
    public void test()
    {
        long epochMillis = System.currentTimeMillis();
        TimeInstant instant = TimeInstant.get(epochMillis);

        Assert.assertEquals(epochMillis, instant.getEpochMillis());
        Assert.assertEquals(-1, TimeInstant.get(epochMillis).compareTo(TimeInstant.get(epochMillis + 1L)));
        Assert.assertEquals(0, TimeInstant.get(epochMillis).compareTo(TimeInstant.get(epochMillis)));
        Assert.assertEquals(1, TimeInstant.get(epochMillis + 1L).compareTo(TimeInstant.get(epochMillis)));

        Assert.assertTrue(TimeInstant.get(epochMillis).equals(TimeInstant.get(epochMillis)));
        Assert.assertFalse(TimeInstant.get(epochMillis + 1L).equals(TimeInstant.get(epochMillis)));

        Assert.assertTrue(TimeInstant.get(epochMillis).hashCode() == TimeInstant.get(epochMillis).hashCode());
        Assert.assertFalse(TimeInstant.get(epochMillis + 1L).hashCode() == TimeInstant.get(epochMillis).hashCode());
    }

    /**
     * Test for {@link TimeInstant#toISO8601String()}.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testToISO8601String() throws ParseException
    {
        SimpleDateFormat fmt = new SimpleDateFormat(DateTimeFormats.DATE_TIME_FORMAT);
        TimeInstant timeInstant = TimeInstant.get(fmt.parse("2010-03-13 12:35:17"));
        String actual = timeInstant.toISO8601String();
        String expected = "2010-03-13T12:35:17Z";
        Assert.assertEquals(expected, actual);

        fmt = new SimpleDateFormat(DateTimeFormats.DATE_TIME_MILLIS_FORMAT);
        timeInstant = TimeInstant.get(fmt.parse("2010-03-13 12:35:17.001"));
        actual = timeInstant.toISO8601String();
        expected = "2010-03-13T12:35:17.001Z";
        Assert.assertEquals(expected, actual);
    }
}
