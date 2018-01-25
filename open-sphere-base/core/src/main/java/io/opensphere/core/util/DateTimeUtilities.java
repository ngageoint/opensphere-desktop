package io.opensphere.core.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date/Time Utilities.
 */
public final class DateTimeUtilities
{
    /**
     * Valid Formats for the date portion of the ISO8601 date. Note that yyyyMM
     * is not allowed because of the confusion with yyMMdd.
     */
    private static final String[] ISO_DATE_FORMATS = { "yyyyMMdd", "yyyy", "yyyyDDD", "yyyy'W'wwd", "yy'W'wwd", "yy'W'ww",
        "yyyy'W'ww", };

    /**
     * Valid Formats for the time portion of the ISO8601 date.
     */
    private static final String[] ISO_TIME_FORMATS = { "HHmmss", "HHmmss.SSS", "HHmm", "HH", "", };

    /** The pattern to find a millisecond format of more than 1-2 digits. */
    private static final Pattern MILLI_FORMAT_PATTERN = Pattern.compile(".*?(\\.S{1,3}).*");

    /** The pattern to find milliseconds of more than 3 digits. */
    private static final Pattern TOO_MANY_MILLIS_PATTERN = Pattern.compile(".*?(\\.\\d{4,}).*");

    /**
     * Replaces milliseconds longer than 3 digits with the first 3 digits
     * (rounded). This is necessary because {@link SimpleDateFormat} does not
     * handle milliseconds longer than 3 digits correctly. For example,
     * {@code new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS").parse("2013-04-09 13:38:15.9999")}
     * will give you 2013-04-09 13:38:24.999.
     *
     * @param dateTimeString The string that could contain milliseconds
     * @return The fixed string
     */
    public static String fixMillis(String dateTimeString)
    {
        String fixedString = dateTimeString;

        Matcher matcher = TOO_MANY_MILLIS_PATTERN.matcher(dateTimeString);
        if (matcher.matches())
        {
            String match = matcher.group(1);
            int millis = Integer.parseInt(match.substring(1, 5));
            millis = Math.round((float)millis / 10);
            String replacement = new StringBuilder(4).append('.').append(millis).toString();
            fixedString = Pattern.compile(match, Pattern.LITERAL).matcher(dateTimeString).replaceFirst(replacement);
        }

        return fixedString;
    }

    /**
     * Produce an ISO8601 compatible date string representing the given date.
     * Specifically this will produce a string with the format
     * "yyyy-MM-dd'T'HH:mm:ss'Z'" if the date does not have millisecond
     * precision, or "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" if it does.
     *
     * @param date The date for which the date string is desired.
     * @return The date string, or {@code null} if the given date is null.
     */
    public static String generateISO8601DateString(Date date)
    {
        if (date != null)
        {
            SimpleDateFormat format = date.getTime() % 1000 == 0L ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    : new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            return format.format(date);
        }
        return null;
    }

    /**
     * Wraps {@link DateFormat#parse(String)} with fixing parsing of
     * milliseconds longer than 3 digits.
     *
     * @param dateFormat The date format to use
     * @param source A <code>String</code> whose beginning should be parsed.
     * @return A <code>Date</code> parsed from the string.
     * @exception ParseException if the beginning of the specified string cannot
     *                be parsed.
     */
    public static Date parse(DateFormat dateFormat, String source) throws ParseException
    {
        Date date = dateFormat.parse(fixMillis(source));
        if (dateFormat instanceof SimpleDateFormat)
        {
            date = reducePrecision((SimpleDateFormat)dateFormat, date);
        }
        return date;
    }

    /**
     * If a date is known to be ISO8601 compatible, but the actual format is not
     * known this will attempt valid formats until the date is correctly parsed.
     *
     * @param dateStr The string representation of the date.
     * @return The {@link Date}.
     * @throws ParseException If the string cannot be parsed.
     */
    public static Date parseISO8601Date(String dateStr) throws ParseException
    {
        // Strip '-' and ':' from the string.
        String reformattedDate = dateStr.replace("-", "").replace(":", "");

        // Replace any 'T' or 'Z' with a space
        reformattedDate = reformattedDate.replace("Z", " ").replace("T", " ").trim();

        for (String dateFmt : ISO_DATE_FORMATS)
        {
            for (String timeFmt : ISO_TIME_FORMATS)
            {
                // TODO it is faster to use a regular expression to determine a
                // matching format than to attempt to format using
                // SimpleDateFormat (see
                // com.bitsys.common.configuration.date.DateFormat).
                Date date = null;
                if (!timeFmt.isEmpty())
                {
                    date = formatISO8601Date(dateFmt + " " + timeFmt, reformattedDate);
                }
                else
                {
                    date = formatISO8601Date(dateFmt, reformattedDate);
                }
                if (date != null)
                {
                    return date;
                }
            }
        }
        throw new ParseException("The string [" + dateStr + "] could not be parsed as an ISO8601 date.", 0);
    }

    /**
     * Given a date string and a format string, return the date. ISO8601 does
     * not allow more than 3 digits for the milliseconds, so no adjustment is
     * required.
     *
     * @param fmtStr The format string to use to attempt to parse the date.
     * @param dateStr The string representing date to be parsed.
     * @return The {@link Date} if the format is compatible with the date
     *         string, or {@code null} if the date is incompatible with the
     *         format.
     */
    @SuppressWarnings("PMD.PrematureDeclaration")
    private static Date formatISO8601Date(String fmtStr, String dateStr)
    {
        SimpleDateFormat fmt = new SimpleDateFormat(fmtStr);
        fmt.setLenient(false);
        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        ParsePosition pos = new ParsePosition(0);
        Date date = fmt.parse(dateStr, pos);
        if (pos.getIndex() < dateStr.length())
        {
            return null;
        }
        return date;
    }

    /**
     * Reduces the millisecond precision of the given date based on the given
     * format.
     *
     * @param dateFormat The format
     * @param date The date
     * @return The new date with reduced millisecond precision
     */
    private static Date reducePrecision(SimpleDateFormat dateFormat, Date date)
    {
        Date reducedDate = date;
        Matcher matcher = MILLI_FORMAT_PATTERN.matcher(dateFormat.toPattern());
        if (matcher.matches())
        {
            int milliCount = matcher.group(1).length() - 1;
            if (milliCount < 3)
            {
                int roundFactor = milliCount == 1 ? 100 : 10;
                long roundedTime = Math.round((double)date.getTime() / roundFactor) * roundFactor;
                reducedDate = new Date(roundedTime);
            }
        }
        return reducedDate;
    }

    /**
     * Private constructor.
     */
    private DateTimeUtilities()
    {
    }
}
