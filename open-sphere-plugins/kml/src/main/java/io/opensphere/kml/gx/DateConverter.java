package io.opensphere.kml.gx;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.util.lang.StringUtilities;

/**
 * The data converter used for gx:tracks.
 *
 */
public class DateConverter extends XmlAdapter<String, Date>
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(DateConverter.class);

    /**
     * The formatter used for the date.
     */
    private final SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS'Z'");

    @Override
    public String marshal(Date v)
    {
        String marshalledDate = StringUtilities.EMPTY;

        if (v != null)
        {
            marshalledDate = myFormat.format(v);
        }

        return marshalledDate;
    }

    @Override
    public Date unmarshal(String v)
    {
        Date date = null;
        try
        {
            if (StringUtils.isNotEmpty(v))
            {
                date = myFormat.parse(v);
            }
        }
        catch (ParseException e)
        {
            LOGGER.error(e.getMessage(), e);
        }

        return date;
    }
}
