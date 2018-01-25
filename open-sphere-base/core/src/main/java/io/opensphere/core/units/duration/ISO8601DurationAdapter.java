package io.opensphere.core.units.duration;

import java.text.ParseException;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

/** An XmlAdaptor that converts Durations to ISO8601 strings. */
public class ISO8601DurationAdapter extends XmlAdapter<String, Duration>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ISO8601DurationAdapter.class);

    @Override
    public String marshal(Duration v)
    {
        return v.toISO8601String();
    }

    @Override
    public Duration unmarshal(String v)
    {
        try
        {
            return Duration.fromISO8601String(v);
        }
        catch (UnsupportedOperationException | ParseException e)
        {
            LOGGER.error("Failed to convert ISO8601 duration: " + e, e);
            return null;
        }
    }
}
