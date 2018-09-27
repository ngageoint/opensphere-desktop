package io.opensphere.wms.capabilities;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.common.time.DateUtils;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Hours;
import io.opensphere.core.units.duration.Minutes;
import io.opensphere.core.units.duration.Months;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.units.duration.Years;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.lang.NumberUtilities;

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
        Date currentTime = new Date();
        String[] timeSplit = extent.split("/");
        if (timeSplit.length > 1)
        {
            Date start;
            if (StringUtils.startsWithAny(timeSplit[0], "P", "C"))
            {
                start = parseRelativeTime(currentTime, timeSplit[0]);
            }
            else
            {
                start = DateUtils.parseISO8601date(DateTimeUtilities.fixMillis(timeSplit[0]));
            }

            Date end;
            if (StringUtils.startsWithAny(timeSplit[1], "P", "C"))
            {
                end = parseRelativeTime(currentTime, timeSplit[1]);
            }
            else
            {
                end = DateUtils.parseISO8601date(DateTimeUtilities.fixMillis(timeSplit[1]));
            }
            if (start != null && end != null)
            {
                return TimeSpan.get(start, end);
            }
        }
        return TimeSpan.TIMELESS;
    }

    /**
     * Parses the supplied relative time string, calculating an offset Date from
     * the supplied current time.
     *
     * One end of the interval must be a time value, but the other may be a
     * duration value as defined by the ISO 8601 standard. The special keyword
     * PRESENT may be used to specify a time relative to the present server
     * time.
     *
     * The periodicity is also specified in ISO-8601 format: a capital P
     * followed by one or more interval lengths, each consisting of a number and
     * a letter identifying a time unit:
     *
     * <pre>
     * Unit    Abbreviation
     * Years       Y
     * Months      M
     * Days        D
     * Hours       H
     * Minutes     M
     * Seconds     S
     * </pre>
     *
     * The Year/Month/Day group of values must be separated from the
     * Hours/Minutes/Seconds group by a T character. The T itself may be omitted
     * if hours, minutes, and seconds are all omitted. Additionally, fields
     * which contain a 0 may be omitted entirely.
     *
     * @param currentTime the current time from which to calculate offsets.
     * @param dateTimeString the date time string to parse.
     * @return A date calculated from the supplied relative time string.
     */
    private static Date parseRelativeTime(Date currentTime, String dateTimeString)
    {
        TimeInstant timeInstant = TimeInstant.get(currentTime);

        if (!StringUtils.equals("PRESENT", dateTimeString) && !StringUtils.equals("CURRENT", dateTimeString))
        {
            // parse out the relative time:
            Pattern relativePattern = Pattern.compile(
                    "P{1}(?:(?<years>\\d+)[Y]{1})?(?:(?<months>\\d+)[M]{1})?(?:(?<days>\\d+)[D]{1})?(T{1}(?:(?<hours>\\d+)[H]{1})?(?:(?<minutes>\\d+)[M]{1})?(?:(?<seconds>\\d+)[S]{1})?)?");
            Matcher matcher = relativePattern.matcher(dateTimeString);
            if (matcher.matches())
            {
                timeInstant = timeInstant.minus(new Years(NumberUtilities.parseInt(matcher.group("years"), 0)));
                timeInstant = timeInstant.minus(new Months(NumberUtilities.parseInt(matcher.group("months"), 0)));
                timeInstant = timeInstant.minus(new Days(NumberUtilities.parseInt(matcher.group("days"), 0)));
                timeInstant = timeInstant.minus(new Hours(NumberUtilities.parseInt(matcher.group("hours"), 0)));
                timeInstant = timeInstant.minus(new Minutes(NumberUtilities.parseInt(matcher.group("minutes"), 0)));
                timeInstant = timeInstant.minus(new Seconds(NumberUtilities.parseInt(matcher.group("seconds"), 0)));
            }
        }
        return timeInstant.toDate();
    }

    /**
     * Forbid public instantiation of utility class.
     */
    private WMSTimeUtils()
    {
    }
}
