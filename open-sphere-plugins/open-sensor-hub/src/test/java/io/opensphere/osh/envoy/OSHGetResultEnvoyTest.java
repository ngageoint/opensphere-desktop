package io.opensphere.osh.envoy;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.DateTimeFormats;
import io.opensphere.core.util.DateTimeUtilities;
import org.junit.Assert;

/** Tests {@link OSHGetResultEnvoy}. */
public class OSHGetResultEnvoyTest
{
    /** Tests {@link OSHGetResultEnvoy#getTimeString(TimeSpan)}. */
    @Test
    public void testGetTimeString()
    {
        SimpleDateFormat format = new SimpleDateFormat(DateTimeFormats.DATE_FORMAT);
        try
        {
            Assert.assertEquals("2013-04-09T00:00:00Z",
                    OSHGetResultEnvoy.getTimeString(TimeSpan.get(DateTimeUtilities.parse(format, "2013-04-09"))));
            Assert.assertEquals("now", OSHGetResultEnvoy.getTimeString(TimeSpan.ZERO));
            Assert.assertEquals("now", OSHGetResultEnvoy.getTimeString(TimeSpan.TIMELESS));
            Assert.assertEquals("2013-04-09T00:00:00Z/2013-04-10T00:00:00Z", OSHGetResultEnvoy.getTimeString(
                    TimeSpan.get(DateTimeUtilities.parse(format, "2013-04-09"), DateTimeUtilities.parse(format, "2013-04-10"))));
            Assert.assertEquals("2013-04-09T00:00:00Z/now", OSHGetResultEnvoy
                    .getTimeString(TimeSpan.newUnboundedEndTimeSpan(DateTimeUtilities.parse(format, "2013-04-09"))));
            Assert.assertEquals("now/2013-04-09T00:00:00Z", OSHGetResultEnvoy
                    .getTimeString(TimeSpan.newUnboundedStartTimeSpan(DateTimeUtilities.parse(format, "2013-04-09"))));
        }
        catch (ParseException e)
        {
            Assert.fail(e.getMessage());
        }
    }
}
