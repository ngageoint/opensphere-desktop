package io.opensphere.core.common.util;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * This class assists in caching SimpleDateFormat classes by Thread to reduce
 * constructor time ( since the {@link SimpleDateFormat} is expensive to
 * construct due to some of the security checking that goes on deep in the
 * constructor) and to provide a Thread safe way to retrieve the cached
 * SimpleDateFormat since they are not thread safe for date parsing using the
 * same object.
 *
 * Best used for high volume conversion threads.
 *
 * This is implemented with a ThreadLocal under the hood which manages cleaning
 * out the cache for threads that end.
 */
public class SimpleDateFormatHelper
{
    private final static TimeZone DEFAULT_TIME_ZONE_GMT00 = TimeZone.getTimeZone("GMT+00:00");

    private static ThreadLocal<Map<String, SimpleDateFormat>> threadLocalPatternToSDF = new ThreadLocal<Map<String, SimpleDateFormat>>()
    {
        @Override
        protected Map<String, SimpleDateFormat> initialValue()
        {
            return new HashMap<>();
        }
    };

    /**
     * A method that allows the current thread to clear out its cache. Thread
     * safe call.
     */
    public static void clearCacheForCurrentThread()
    {
        threadLocalPatternToSDF.get().clear();
    }

    /**
     * Gets the cached SimpleDateFormat for the provided format string for the
     * current thread in the DEFAULT time zone of GMT00.
     *
     * If the SimpleDateFormat does not exist it will be created and returned.
     *
     * This is a thread safe call.
     *
     * @param sdf - the simple date format string.
     * @return
     */
    public static SimpleDateFormat getSimpleDateFormat(String sdf)
    {
        return getSimpleDateFormat(sdf, DEFAULT_TIME_ZONE_GMT00);
    }

    /**
     * Gets the cached SimpleDateFormat for the provided format string for the
     * current thread in the provided time zone.
     *
     * If the SimpleDateFormat does not exist it will be created and returned.
     *
     * This is a thread safe call.
     *
     * @param sdf - the simple date format string.
     * @param tz -t he  time zone
     * @return the {@link SimpleDateFormat}
     */
    public static SimpleDateFormat getSimpleDateFormat(String sdf, TimeZone tz)
    {
        Map<String, SimpleDateFormat> formatMap = threadLocalPatternToSDF.get();
        SimpleDateFormat formatter = formatMap.get(sdf);

        // If we don't have a SimpleDateFormat yet
        // create it and cache it in our map.
        if (formatter == null)
        {
            formatter = new SimpleDateFormat(sdf);
            formatMap.put(sdf, formatter);
        }
        formatter.setTimeZone(tz);
        return formatter;
    }
}
