package io.opensphere.core.common.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class contains date/time-related constants and routines.
 */
public final class DateUtils
{
    /**
     * The object used to capture logging output.
     */
    private static final Log LOGGER = LogFactory.getLog(DateUtils.class);

    /**
     * The GMT time zone.
     */
    public static final TimeZone GMT_TIME_ZONE = TimeZone.getTimeZone("GMT");

    /**
     * The default XML date pattern string.
     */
    public static final String XML_DATE_PATTERN_STRING = "[0-9]{4}-[0-9]{2}-[0-9]{2}";

    /**
     * The default XML time pattern string.
     */
    public static final String XML_TIME_PATTERN_STRING = "[0-9]{2}:[0-9]{2}:[0-9]{2}Z";

    /**
     * The default XML time with milliseconds pattern string.
     */
    public static final String XML_TIME_W_MILLIS_PATTERN_STRING = "[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}Z";

    /**
     * The default XML date/time pattern string.
     */
    public static final String XML_DATE_TIME_PATTERN_STRING = XML_DATE_PATTERN_STRING + "T" + XML_TIME_PATTERN_STRING;

    /**
     * The default XML date/time with milliseconds pattern string.
     */
    public static final String XML_DATE_TIME_W_MILLIS_PATTERN_STRING = XML_DATE_PATTERN_STRING + "T"
            + XML_TIME_W_MILLIS_PATTERN_STRING;

    /**
     * The default XML date format (ISO8601) string.
     */
    public static final String XML_DATE_FORMAT_STRING = "yyyy-MM-dd";

    /**
     * The default XML time format (ISO8601) string.
     */
    public static final String XML_TIME_FORMAT_STRING = "HH:mm:ss'Z'";

    /**
     * The default XML time format (ISO8601) string with milliseconds.
     */
    public static final String XML_TIME_FORMAT_STRING_W_MILLIS = "HH:mm:ss.SSS'Z'";

    /**
     * The default XML date/time format (ISO8601) string.
     */
    public static final String XML_FORMAT_STRING = XML_DATE_FORMAT_STRING + "'T'" + XML_TIME_FORMAT_STRING;

    /**
     * The default XML date/time format (ISO8601) string with milliseconds.
     */
    public static final String XML_FORMAT_STRING_W_MILLIS = XML_DATE_FORMAT_STRING + "'T'" + XML_TIME_FORMAT_STRING_W_MILLIS;

    /**
     * The XML formatter (ISO8601) for date.
     */
    public static final FastDateFormat XML_DATE_FORMAT = FastDateFormat.getInstance(XML_DATE_FORMAT_STRING, GMT_TIME_ZONE);

    /**
     * The XML formatter (ISO8601) for time.
     */
    public static final FastDateFormat XML_TIME_FORMAT = FastDateFormat.getInstance(XML_TIME_FORMAT_STRING, GMT_TIME_ZONE);

    /**
     * The XML formatter (ISO8601) for date/time.
     */
    public static final FastDateFormat XML_DATE_TIME_FORMAT = FastDateFormat.getInstance(XML_FORMAT_STRING, GMT_TIME_ZONE);

    /**
     * The XML formatter (ISO8601) for date/time with milliseconds.
     */
    public static final FastDateFormat XML_DATE_TIME_W_MILLIS_FORMAT = FastDateFormat.getInstance(XML_FORMAT_STRING_W_MILLIS,
            GMT_TIME_ZONE);

    /**
     * The XML format (ISO8601) for date.
     */
    private static final SimpleDateFormat XML_DATE_PARSER = new SimpleDateFormat(XML_DATE_FORMAT_STRING);

    /**
     * The XML format (ISO8601) for time.
     */
    private static final SimpleDateFormat XML_TIME_PARSER = new SimpleDateFormat(XML_TIME_FORMAT_STRING);

    /**
     * The XML format (ISO8601) for date/time.
     */
    private static final SimpleDateFormat XML_DATE_TIME_PARSER = new SimpleDateFormat(XML_FORMAT_STRING);

    /**
     * The XML format (ISO8601) for date/time with millis.
     */
    private static final SimpleDateFormat XML_DATE_TIME_W_MILLIS_PARSER = new SimpleDateFormat(XML_FORMAT_STRING_W_MILLIS);

    /**
     * Returns a new XML format (ISO8601) instance for date, configured for the
     * GMT timezone.
     *
     * @return a new XML format instance for date.
     */
    public static SimpleDateFormat newGmtDateFormatInstance()
    {
        SimpleDateFormat format = (SimpleDateFormat)XML_DATE_PARSER.clone();
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format;
    }

    /**
     * Returns a new XML format (ISO8601) instance for date.
     *
     * @return a new XML format instance for date.
     */
    public static SimpleDateFormat newXmlDateFormatInstance()
    {
        SimpleDateFormat format = (SimpleDateFormat)XML_DATE_PARSER.clone();
        format.setTimeZone(TimeZone.getDefault());
        return format;
    }

    /**
     * Returns a new XML format (ISO8601) instance for time.
     *
     * @return a new XML format instance for time.
     */
    public static SimpleDateFormat newXmlTimeFormatInstance()
    {
        SimpleDateFormat format = (SimpleDateFormat)XML_TIME_PARSER.clone();
        format.setTimeZone(TimeZone.getDefault());
        return format;
    }

    /**
     * Returns a new XML format (ISO8601) instance for date/time.
     *
     * @return a new XML format instance for date/time.
     */
    public static SimpleDateFormat newXmlDateTimeFormatInstance()
    {
        SimpleDateFormat format = (SimpleDateFormat)XML_DATE_TIME_PARSER.clone();
        format.setTimeZone(TimeZone.getDefault());
        return format;
    }

    /**
     * Returns a new XML format (ISO8601) instance for date, configured for the
     * GMT timezone.
     *
     * @return a new XML format instance for date.
     */
    public static SimpleDateFormat newGmtDateTimeFormatInstance()
    {
        SimpleDateFormat format = (SimpleDateFormat)XML_DATE_TIME_PARSER.clone();
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format;
    }

    /**
     * Returns a new XML format (ISO8601) instance for date/time with
     * millisecond precision.
     *
     * @return a new XML format instance for date/time.
     */
    public static SimpleDateFormat newXmlDateTimeWithMillisFormatInstance()
    {
        SimpleDateFormat format = (SimpleDateFormat)XML_DATE_TIME_W_MILLIS_PARSER.clone();
        format.setTimeZone(TimeZone.getDefault());
        return format;
    }

    /**
     * Determines if there is overlap in the given date/time ranges.
     *
     * @param startTime1 The start time for range 1.
     * @param endTime1 The end time for range 1.
     * @param startTime2 The start time for range 2.
     * @param endTime2 The end time for range 2.
     * @return true if there is overlap in the two ranges.
     */
    public static boolean overlaps(Date startTime1, Date endTime1, Date startTime2, Date endTime2)
    {
        return startTime1.compareTo(endTime2) <= 0 && startTime2.compareTo(endTime1) <= 0;
    }

    /**
     * Returns true if the first two dates completely contain the third date.
     *
     * @param startDate the inclusive start date.
     * @param endDate the inclusive end date.
     * @param dates the dates to check for containment.
     * @return <code>true</code> if all <code>dates</code> fall between
     *         <code>startDate</code> and <code>endDate</code>.
     */
    public static boolean contains(Date startDate, Date endDate, Date... dates)
    {
        if (startDate == null)
        {
            throw new IllegalArgumentException("startDate cannot be null");
        }
        if (endDate == null)
        {
            throw new IllegalArgumentException("endDate cannot be null");
        }
        if (dates == null)
        {
            throw new IllegalArgumentException("date cannot be null");
        }
        if (dates.length == 0)
        {
            throw new IllegalArgumentException("At least one date must be provided");
        }
        if (startDate.after(endDate))
        {
            throw new IllegalArgumentException("startDate cannot be after endDate");
        }

        // Check all dates for containment.
        boolean contains = true;
        for (Date date : dates)
        {
            if (date == null)
            {
                throw new IllegalArgumentException("All dates must be non-null");
            }
            contains = startDate.compareTo(date) <= 0 && date.compareTo(endDate) <= 0;
            if (!contains)
            {
                break;
            }
        }
        return contains;
    }

    /**
     * Parses the supplied date / time string using the GMT Timezone.
     *
     * @param date the raw date / time string to parse.
     * @return a Date object generated from the supplied string.
     * @throws ParseException if the date cannot be parsed.
     */
    public static Date parseDateGMT(String date) throws ParseException
    {
        SimpleDateFormat sdf = newXmlDateTimeFormatInstance();
        sdf.setTimeZone(GMT_TIME_ZONE);
        Date ret = null;
        try
        {
            ret = sdf.parse(date);
        }
        catch (Throwable t)
        {
            sdf = newXmlDateFormatInstance();
            sdf.setTimeZone(GMT_TIME_ZONE);
            ret = sdf.parse(date);
        }
        return ret;
    }

    /**
     * Converts the current date to a <code>XMLGregorianCalendar</code> date.
     *
     * @param date the date to convert.
     * @return XMLGregorianCalendar value of provided date
     */
    public static XMLGregorianCalendar getDateAsXMLGregorianCalendar(Date date)
    {
        GregorianCalendar gregCal = new GregorianCalendar();
        gregCal.setTime(date);
        try
        {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregCal);
        }
        catch (DatatypeConfigurationException e)
        {
            throw new RuntimeException("Problem creating xml gregorian date.", e);
        }
    }

    /**
     * Extracts just the number of milliseconds since midnight from the given
     * date.
     *
     * @param dateTime the <code>Date</code> instance from which the time will
     *            be extracted.
     * @return the number of milliseconds since midnight.
     */
    public static long extractTime(Date dateTime)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateTime);
        return extractTime(cal);
    }

    /**
     * Extracts just the number of milliseconds since midnight from the given
     * calendar.
     *
     * @param calendar the <code>Calendar</code> instance from which the time
     *            will be extracted.
     * @return the number of milliseconds since midnight.
     */
    public static long extractTime(Calendar calendar)
    {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int millisecond = calendar.get(Calendar.MILLISECOND);
        return ((hour * 60 + minute) * 60 + second) * 1000 + millisecond;
    }

    /**
     * If a date is known to be ISO8601 compatible, but the actual format is not
     * know this will attempt valid formats until the date is correctly parsed.
     *
     * @param dateStr the source text from which to create a date string.
     * @return an ISO-8601 compliant date string generated from the supplied
     *         parameter.
     */
    public static Date parseISO8601date(String dateStr)
    {
        // First strip '-' and ':' from the string.
        dateStr = dateStr.replace("-", "").replace(":", "");
        // Replace any 'T' or 'Z' with a space
        dateStr = dateStr.replace("Z", " ").replace("T", " ");

        // Note that yyyyMM is not allowed because of the confusion with yyMMdd
        String[] isoDateFmts = { "yyyyMMdd", "yyyy", "yyyyDDD", "yyyy'W'wwd", "yyyy'W'ww", "yy'W'wwd", "yy'W'ww" };

        String[] isoTimeFmts = { "HHmmss", "HHmmss.SSS", "HHmm", "HH", "" };

        for (String dateFmt : isoDateFmts)
        {
            for (String timeFmt : isoTimeFmts)
            {
                Date date = null;
                if (timeFmt != "")
                {
                    date = formatDate(dateFmt + " " + timeFmt, dateStr);
                }
                else
                {
                    date = formatDate(dateFmt, dateStr);
                }
                if (date != null)
                {
                    return date;
                }
            }
        }
        return null;
    }

    /**
     * Given a date string and a format string, return the date. If the format
     * does not match the date string, null is returned.
     *
     * @param fmtStr the format of the supplied date string to use in parsing.
     * @param dateStr the source string to parse and reformat.
     * @return a Date object parsed from the supplied string.
     */
    public static Date formatDate(String fmtStr, String dateStr)
    {
        try
        {
            SimpleDateFormat fmt = new SimpleDateFormat(fmtStr);
            Date date = fmt.parse(dateStr);
            return date;
        }
        catch (ParseException e)
        {
            // If it fails to parse, this may be expected, just return null.
        }

        return null;
    }

    /**
     * Attempts to convert an Object into a formatted date using the attached
     * formatter.
     *
     * @param formatter The formatter to use when converting from Date to
     *            String.
     * @param value The value attempting to be formatted (or verified).
     * @return A String containing the formatted value, or null if the value
     *         cannot be formatted appropriately.
     */
    public static String parseObject(final FastDateFormat formatter, final Object value)
    {
        if (value instanceof String)
        {
            return (String)value;
        }
        else if (value instanceof Date && formatter != null)
        {
            return formatter.format((Date)value);
        }
        LOGGER.warn("Invalid value: " + value + ". Cannot parse with " + formatter);
        return null;
    }

    /**
     * Private constructor, to prevent instantiation.
     */
    private DateUtils()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }
}
