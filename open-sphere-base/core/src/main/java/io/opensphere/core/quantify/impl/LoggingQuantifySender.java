package io.opensphere.core.quantify.impl;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.opensphere.core.quantify.QuantifySender;
import io.opensphere.core.quantify.model.Metric;

/** A default sender to transmit JSON data to a remote endpoint. */
public class LoggingQuantifySender implements QuantifySender
{
    /** Logger reference. */
    private static final Logger LOG = Logger.getLogger(LoggingQuantifySender.class);

    /** The marhshaller used to convert the metrics to JSON. */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.quantify.QuantifySender#send(java.util.Collection)
     */
    @Override
    public void send(Collection<Metric> metrics)
    {
        try
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug(OBJECT_MAPPER.writeValueAsString(metrics));
            }
        }
        catch (JsonProcessingException e)
        {
            LOG.error("Unable to marshal data to JSON", e);
        }
    }
}
