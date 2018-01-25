package io.opensphere.core.model.time;

import java.text.ParseException;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

/** An XmlAdaptor that converts TimeSpans to ISO8601 strings. */
public class ISO8601TimeSpanAdapter extends XmlAdapter<String, TimeSpan>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ISO8601TimeSpanAdapter.class);

    @Override
    public String marshal(TimeSpan v)
    {
        return v.toISO8601String();
    }

    @Override
    public TimeSpan unmarshal(String v)
    {
        try
        {
            return TimeSpan.fromISO8601String(v);
        }
        catch (UnsupportedOperationException | ParseException e)
        {
            LOGGER.error("Failed to convert ISO8601 interval: " + e, e);
            return null;
        }
    }
}
