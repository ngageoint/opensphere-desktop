package io.opensphere.wfs.gml311;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.common.time.DateUtils;
import io.opensphere.core.common.util.SimpleDateFormatHelper;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * The Class MetaDataHandler.
 */
public class TimeHandler
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(TimeHandler.class);

    /** GML tag for the start of a time span. */
    private static final String BEGIN_TIME_TAG = "beginPosition";

    /** GML tag for the end of a time span. */
    private static final String END_TIME_TAG = "endPosition";

    /** GML tag for a feature's instantaneous time. */
    private static final String TIME_POSITION_TAG = "timePosition";

    /** List of supported GML tags that encapsulate the actual time values. */
    private static final List<String> SURROUNDING_TAGS;

    static
    {
        List<String> tempList = New.list();
        tempList.add("TimeInstant");
        tempList.add("TimePeriod");
        SURROUNDING_TAGS = Collections.unmodifiableList(tempList);
    }

    /** End date derived from column data. */
    private Date myEndDate;

    /** Start date derived from column data. */
    private Date myStartDate;

    /**
     * Test whether this handler supports the specified GML Time tag.
     *
     * @param tag the GML time tag in question
     * @return true, if the tag is a supported GML time tag
     */
    public boolean handlesTag(String tag)
    {
        return SURROUNDING_TAGS.contains(tag);
    }

    /**
     * Handle a GML time key.
     *
     * @param key the Time key
     * @param value the key's value
     */
    public void handleTimeData(String key, String value)
    {
        if (key.equals(BEGIN_TIME_TAG) || key.equals(TIME_POSITION_TAG))
        {
            myStartDate = convertDateTime(value);
        }
        else if (key.equals(END_TIME_TAG))
        {
            myEndDate = convertDateTime(value);
        }
    }

    /**
     * Reset the current state.
     */
    public void reset()
    {
        myStartDate = null;
        myEndDate = null;
    }

    /**
     * Resolve times.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return the time span
     */
    public TimeSpan resolveTimes(Date startDate, Date endDate)
    {
        Date start = myStartDate != null ? myStartDate : startDate;
        Date end = myEndDate != null ? myEndDate : endDate;
        if (start != null)
        {
            if (end != null && end.after(start))
            {
                return TimeSpan.get(start, end);
            }
            return TimeSpan.get(start, start);
        }
        return TimeSpan.TIMELESS;
    }

    /**
     * Resolve times.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return the time span
     */
    public TimeSpan resolveTimes(String startDate, String endDate)
    {
        Date start = StringUtils.isEmpty(startDate) ? null : convertDateTime(startDate);
        Date end = StringUtils.isEmpty(endDate) ? null : convertDateTime(endDate);
        return resolveTimes(start, end);
    }

    /**
     * Resolve time with end day.
     *
     * @param mdEndDay the md end day
     * @return the time span
     */
    public TimeSpan resolveTimeWithEndDay(String mdEndDay)
    {
        try
        {
            SimpleDateFormat dayFormatter = SimpleDateFormatHelper.getSimpleDateFormat("yyyy-MM-dd 'z'HHmmss'.00'");
            SimpleDateFormat day = SimpleDateFormatHelper.getSimpleDateFormat("yyyy-MM-dd");
            String time = day.format(myStartDate);
            time = StringUtilities.concat(time, " ", mdEndDay.replace(" ", ""));
            Date end = DateTimeUtilities.parse(dayFormatter, time);

            // if it spanned a day boundary, add a day.
            if (end.before(myStartDate))
            {
                end.setTime(end.getTime() + Constants.MILLIS_PER_DAY);
            }
            return resolveTimes(myStartDate, end);
        }
        catch (ParseException e)
        {
            LOGGER.warn("Parsing optional downtimes " + e.getMessage());
        }
        return TimeSpan.TIMELESS;
    }

    /**
     * Convert date time.
     *
     * @param dateTime the date time
     * @return the date
     */
    private Date convertDateTime(String dateTime)
    {
        String time = dateTime.replace("T", " ").replace("Z", "");
        return DateUtils.parseISO8601date(DateTimeUtilities.fixMillis(time));
    }
}
