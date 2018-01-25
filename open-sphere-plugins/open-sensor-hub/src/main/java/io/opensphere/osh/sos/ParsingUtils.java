package io.opensphere.osh.sos;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.util.DateTimeUtilities;

/** OpenSensorHub parsing utilities. */
public final class ParsingUtils
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ParsingUtils.class);

    /**
     * Parses a date string.
     *
     * @param value the date string
     * @param isStart whether the time is the start time
     * @return the date or null
     */
    public static Date parseDate(String value, boolean isStart)
    {
        Date date = null;
        if (StringUtils.isNotEmpty(value) && !"now".equals(value))
        {
            try
            {
                date = DateTimeUtilities.parseISO8601Date(value);
            }
            catch (ParseException e)
            {
                LOGGER.error(e);
            }
        }
        return date;
    }

    /** Disallow instantiation. */
    private ParsingUtils()
    {
    }
}
