package io.opensphere.mantle.data.element.factory;

import java.util.Date;
import java.util.regex.Pattern;

import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.specialkey.EndTimeKey;

/**
 * This class wraps a DataTypeInfo and discovers, by pattern matching, which
 * fields are most likely to contain time span information. The patterns in
 * question are case-insensitive regular expressions, and whichever column is
 * first to match either the start time or end time pattern is captured for
 * later use in assigning a time interval to a record when it is created. <br>
 * <br>
 * This class is not used when streaming serialized Java Objects because it is
 * not needed in that case. The Objects received from the server already have a
 * time interval specified, so there is no need for guesswork.
 */
public class AvroTimeHelper
{
    /** Puts java regex into case-insensitive matching mode. */
    private static final String IGNORE_CASE = "(?i)";

    /** Regex for possible start time field names. */
    private static final String START_REGEX = "^(DATE_TIME|(UP|START|BEGIN).*DATETIME.*)$";

    /** Regex for possible end time field names. */
    private static final String END_REGEX = "^(DOWN|STOP|END).*DATETIME.*$";

    /** Compiled Pattern for start time field names (case-insensitive). */
    private static Pattern stPat = Pattern.compile(IGNORE_CASE + START_REGEX);

    /** Compiled Pattern for end time field names (case-insensitive). */
    private static Pattern endPat = Pattern.compile(IGNORE_CASE + END_REGEX);

    /** Captured start time field name, if any, or null. */
    private String startKey;

    /** Captured end time field name, if any, or null. */
    private String endKey;

    /** The decorated DataTypeInfo. */
    private final DataTypeInfo type;

    /**
     * Create. In the process, the metadata are traversed in search of fields
     * likely to contain boundaries of a record's the time span.
     *
     * @param dti a DataTypeInfo
     */
    public AvroTimeHelper(DataTypeInfo dti)
    {
        type = dti;
        // now search for starting and ending time column names
        for (String k : type.getMetaDataInfo().getKeyNames())
        {
            if (stPat.matcher(k).matches())
            {
                setStartKey(k);
            }
            else if (endPat.matcher(k).matches())
            {
                setEndKey(k);
            }
        }

        if (StringUtils.isEmpty(startKey))
        {
            startKey = dti.getMetaDataInfo().getTimeKey();
        }

        if (StringUtils.isEmpty(endKey))
        {
            endKey = dti.getMetaDataInfo().getKeyForSpecialType(EndTimeKey.DEFAULT);
        }
    }

    /**
     * Get the wrapped DataTypeInfo.
     *
     * @return DataTypeInfo
     */
    public DataTypeInfo getType()
    {
        return type;
    }

    /**
     * Get the time span corresponding to the provided record, if any. The
     * captured start and end fields are used to extract what are hoped to be
     * the desired time values, which are then combined to create a TimeSpan. If
     * only one time boundary is found, the returned span is a single instant,
     * and of none, then it is TIMELESS.
     *
     * @param rec a GenericRecord
     * @return <i>rec</i>'s TimeSpan
     */
    public TimeSpan span(GenericRecord rec)
    {
        Date start = parseDate(valNonNull(rec, startKey));
        Date end = parseDate(valNonNull(rec, endKey));
        return TimeSpan.spanOrPt(start, end);
    }

    /**
     * Null-tolerant GenericRecord field access.
     *
     * @param rec GenericRecord
     * @param key field name
     * @return field value or null
     */
    private static Object valNonNull(GenericRecord rec, String key)
    {
        if (rec == null || key == null)
        {
            return null;
        }
        return rec.get(key);
    }

    /**
     * Get a Date from an Object, if the latter is of appropriate type. In
     * present terms, only a "JODA" DateTime (which can convert itself to a
     * Date) or a Long (which is interpreted as milliseconds from Java epoch)
     * produce non-null results.
     *
     * @param s the Object
     * @return the Date, if applicable, or null
     */
    private static Date parseDate(Object s)
    {
        if (s instanceof DateTime)
        {
            return ((DateTime)s).toDate();
        }
        // This case may be patently unnecessary, but it causes no harm
        if (s instanceof Long)
        {
            return new Date(((Long)s).longValue());
        }
        return null;
    }

    /**
     * Capture a start time field. If one has already been captured, the new one
     * is ignored. Thus, the first in visitation order is used.
     *
     * @param k a field name
     */
    private void setStartKey(String k)
    {
        if (startKey == null)
        {
            startKey = k;
        }
    }

    /**
     * Capture an end time field. If one has already been captured, the new one
     * is ignored. Thus, the first in visitation order is used.
     *
     * @param k a field name
     */
    private void setEndKey(String k)
    {
        if (endKey == null)
        {
            endKey = k;
        }
    }
}
