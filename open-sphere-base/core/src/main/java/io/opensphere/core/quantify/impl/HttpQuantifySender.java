package io.opensphere.core.quantify.impl;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.opensphere.core.Toolbox;
import io.opensphere.core.quantify.QuantifySender;
import io.opensphere.core.quantify.model.Metric;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;

/** A quantify sender for sending HTTP data. */
public class HttpQuantifySender implements QuantifySender
{
    /** Logger reference. */
    private static final Logger LOG = Logger.getLogger(HttpQuantifySender.class);

    /** The marshaller used to convert the metrics to JSON. */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /** The URL to which to send metrics. */
    private final URL myUrl;

    /**
     * Creates a new sender with the supplied toolbox.
     *
     * @param toolbox the toolbox through which application state is accessed.
     * @param url the URL to which to send metrics
     */
    public HttpQuantifySender(Toolbox toolbox, URL url)
    {
        myToolbox = toolbox;
        myUrl = url;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.quantify.QuantifySender#send(java.util.Collection)
     */
    @Override
    public void send(Collection<Metric> metrics)
    {
        if (metrics != null && !metrics.isEmpty())
        {
            ServerProvider<HttpServer> provider = myToolbox.getServerProviderRegistry().getProvider(HttpServer.class);
            HttpServer serverConnection = provider.getServer(myUrl);

            try (PipedInputStream body = new PipedInputStream(); PipedOutputStream out = new PipedOutputStream(body))
            {
                OBJECT_MAPPER.writeValue(out, metrics);
                out.flush();
                ResponseValues response = new ResponseValues();
                serverConnection.sendPost(myUrl, body, response);
            }
            catch (JsonGenerationException | JsonMappingException e)
            {
                LOG.error("Unable to marshal data to JSON", e);
            }
            catch (IOException | URISyntaxException e)
            {
                LOG.error("Unable to send data to URL " + myUrl.toString(), e);
            }
        }
    }

}
