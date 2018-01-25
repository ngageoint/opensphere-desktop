package io.opensphere.mantle.util;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.DateTimeFormats;

/** Tests for {@link TimeSpanUtility}. */
public class TimeSpanUtilityTest
{
    /** Set up. */
    @Before
    public void setUp()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Tests {@link TimeSpanUtility#formatTimeSpan(SimpleDateFormat, TimeSpan)}.
     */
    @Test
    public void testFormatTimeSpan()
    {
        SimpleDateFormat format = new SimpleDateFormat(DateTimeFormats.DATE_TIME_FORMAT);

        Assert.assertEquals("", TimeSpanUtility.formatTimeSpan(format, null));
        Assert.assertEquals("TIMELESS", TimeSpanUtility.formatTimeSpan(format, TimeSpan.TIMELESS));
        Assert.assertEquals("ZERO", TimeSpanUtility.formatTimeSpan(format, TimeSpan.ZERO));
        Assert.assertEquals("1970-01-02 00:00:00",
                TimeSpanUtility.formatTimeSpan(format, TimeSpan.get(Constants.MILLIS_PER_DAY)));
        Assert.assertEquals("1970-01-02 00:00:00 to 1970-01-03 00:00:00",
                TimeSpanUtility.formatTimeSpan(format, TimeSpan.get(Constants.MILLIS_PER_DAY, Constants.MILLIS_PER_DAY * 2)));
        Assert.assertEquals("UNBOUNDED to 1970-01-02 00:00:00",
                TimeSpanUtility.formatTimeSpan(format, TimeSpan.newUnboundedStartTimeSpan(Constants.MILLIS_PER_DAY)));
        Assert.assertEquals("1970-01-02 00:00:00 to UNBOUNDED",
                TimeSpanUtility.formatTimeSpan(format, TimeSpan.newUnboundedEndTimeSpan(Constants.MILLIS_PER_DAY)));
    }
}
