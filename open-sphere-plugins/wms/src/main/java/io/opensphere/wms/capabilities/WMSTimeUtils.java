package io.opensphere.wms.capabilities;

import java.util.Date;

import io.opensphere.core.common.time.DateUtils;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.DateTimeUtilities;

/**
 * Collection of utilities for dealing with WMS times.
 */
public final class WMSTimeUtils
{
    /**
     * Convenience method for converting an ISO8601 time extent into a.
     *
     * @param extent the ISO8601 time extent
     * @return the equivalent {@link TimeSpan}
     * @throws IllegalArgumentException the illegal argument exception
     */
    public static TimeSpan parseISOTimeExtent(String extent) throws IllegalArgumentException
    {
        String[] timeSplit = extent.split("/");
        if (timeSplit.length > 1)
        {
            Date start = DateUtils.parseISO8601date(DateTimeUtilities.fixMillis(timeSplit[0]));
            Date end = DateUtils.parseISO8601date(DateTimeUtilities.fixMillis(timeSplit[1]));
            if (start != null && end != null)
            {
                return TimeSpan.get(start, end);
            }
        }
        return TimeSpan.TIMELESS;
    }

    /**
     * Forbid public instantiation of utility class.
     */
    private WMSTimeUtils()
    {
    }
}
