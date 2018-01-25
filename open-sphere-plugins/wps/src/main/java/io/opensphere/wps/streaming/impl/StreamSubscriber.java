package io.opensphere.wps.streaming.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.wps.streaming.StreamingConstants;
import io.opensphere.wps.streaming.Subscriber;
import io.opensphere.wps.streaming.SubscriptionContext;
import io.opensphere.wps.streaming.beans.Configuration;
import net.opengis.ogc._110.FilterType;
import net.opengis.ows._110.CodeType;
import net.opengis.wps._100.ComplexDataType;
import net.opengis.wps._100.DataInputsType;
import net.opengis.wps._100.DataType;
import net.opengis.wps._100.Execute;
import net.opengis.wps._100.InputType;
import net.opengis.wps._100.LiteralDataType;
import net.opengis.wps._100.OutputDefinitionType;
import net.opengis.wps._100.ResponseFormType;

/**
 * Subscribes to the NRT stream and returns the stream's filter id used to get
 * new data.
 */
public class StreamSubscriber implements Subscriber
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(StreamSubscriber.class);

    /**
     * Handles the serializing of the filter to be passed to server in the
     * subscription.
     */
    private final FilterHandler myFilterHandler = new FilterHandler();

    /**
     * Used to get the server to make the WPS request.
     */
    private final ServerProviderRegistry myProviderRegistry;

    /**
     * Constructs a new subscriber.
     *
     * @param providerRegistry Used to get the server to make the WPS request.
     */
    public StreamSubscriber(ServerProviderRegistry providerRegistry)
    {
        myProviderRegistry = providerRegistry;
    }

    /**
     * Gets the {@link ServerProviderRegistry}.
     *
     * @return The {@link ServerProviderRegistry}.
     */
    public ServerProviderRegistry getServerProviderRegistry()
    {
        return myProviderRegistry;
    }

    @Override
    public SubscriptionContext subscribeToStream(URL wpsUrl, String stream, DataFilter filter, Geometry g)
    {
        LOGGER.info("Performing request " + StreamingConstants.SUBSCRIBE_PROCESS + " - " + wpsUrl + " for layer " + stream
                + " using data filter [" + filter + "] and spatial filter [" + g + "]");
        InputStream response = getResponseViaPost(wpsUrl, buildRequest(stream, filter, normalize(g)));
        if (response == null)
        {
            return null;
        }

        try
        {
            Configuration cfg = XMLUtilities.readXMLObject(response, Configuration.class);
            // if the URL is null, then this was most likely an error message
            if (cfg == null || cfg.getUrl() == null)
            {
                return null;
            }
            String filterId = cfg.getFilterId();
            String idParam = cfg.getUrl().getRequestParameters().get(0);

            StringBuilder buf = new StringBuilder();
            buf.append(cfg.getUrl().getBase());
            buf.append('?');
            buf.append(idParam);
            buf.append('=');
            buf.append(filterId);
            buf.append('&');
            buf.append(cfg.getUrl().getRequestParameters().get(1));
            buf.append('=');
            int pollInterval = Integer.parseInt(System.getProperty("NRTPollInterval", "5000"));
            buf.append(pollInterval);

            URL url = new URL(buf.toString());
            LOGGER.info("Stream URL for layer " + stream + " is " + url);

            SubscriptionContext context = new SubscriptionContext();
            context.setFilterIdParameterName(idParam);
            context.setStreamId(UUID.fromString(filterId));
            context.setStreamUrl(url);
            context.setPollInterval(pollInterval);
            return context;
        }
        catch (JAXBException | MalformedURLException e)
        {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

    private static Geometry normalize(Geometry g)
    {
        if (!(g instanceof MultiPolygon))
        {
            return g;
        }
        int n = g.getNumGeometries();
        Geometry ret = null;
        for (int i = 0; i < n; i++)
        {
            ret = unionOf(ret, g.getGeometryN(i));
        }
        return ret;
    }

    private static Geometry unionOf(Geometry g0, Geometry g1)
    {
        if (g0 == null)
        {
            return g1;
        }
        if (g1 == null)
        {
            return g0;
        }
        return g0.union(g1);
    }

    /**
     * Builds the layer name inputs for the subscription request.
     *
     * @param dataInputs The object to add the layer name inputs to.
     * @param stream The name of the layer.
     */
    private void buildLayerNameData(DataInputsType dataInputs, String stream)
    {
        InputType layerName = new InputType();
        CodeType codeType = new CodeType();
        codeType.setValue(StreamingConstants.LAYER_NAME);
        layerName.setIdentifier(codeType);
        DataType dataType = new DataType();
        LiteralDataType layerDataType = new LiteralDataType();
        layerDataType.setValue(stream);
        dataType.setLiteralData(layerDataType);
        layerName.setData(dataType);
        dataInputs.getInput().add(layerName);
    }

    /**
     * Builds the output data format inputs for the subscription request.
     *
     * @param dataInputs The object to add the output format to.
     */
    private void buildOutputData(DataInputsType dataInputs)
    {
        InputType outputFormat = new InputType();
        CodeType codeType = new CodeType();
        codeType.setValue(StreamingConstants.OUTPUT_FORMAT);
        outputFormat.setIdentifier(codeType);

        DataType dataType = new DataType();
        ComplexDataType complex = new ComplexDataType();
        complex.setMimeType(StreamingConstants.OUTPUT_MIME_TYPE);
        dataType.setComplexData(complex);
        outputFormat.setData(dataType);

        dataInputs.getInput().add(outputFormat);
    }

    /**
     * Builds the subscription request to send to the WPS process.
     *
     * @param stream The layer name to stream.
     * @param filter The filter to use during streaming, or null.
     * @param spatialFilter The spatial filter to use during streaming, or null.
     * @return The request.
     */
    private Execute buildRequest(String stream, DataFilter filter, Geometry spatialFilter)
    {
        Execute request = new Execute();
        CodeType codeType = new CodeType();
        codeType.setValue(StreamingConstants.SUBSCRIBE_PROCESS);
        request.setIdentifier(codeType);

        ResponseFormType responseForm = new ResponseFormType();
        OutputDefinitionType outputDefinition = new OutputDefinitionType();
        codeType = new CodeType();
        codeType.setValue(StreamingConstants.SUBSCRIPTION_OUTPUT);
        outputDefinition.setIdentifier(codeType);
        responseForm.setRawDataOutput(outputDefinition);
        request.setResponseForm(responseForm);

        DataInputsType dataInputs = new DataInputsType();

        buildLayerNameData(dataInputs, stream);
        buildOutputData(dataInputs);
        myFilterHandler.serializeFilter(dataInputs, filter, spatialFilter, stream);

        request.setDataInputs(dataInputs);

        return request;
    }

    /**
     * Sends the request to the server via post.
     *
     * @param url The url to send the request to.
     * @param request The request to send.
     * @return The response from the server, or null if there was an issue
     *         subscribing with the server.
     */
    private InputStream getResponseViaPost(URL url, Execute request)
    {
        InputStream returnStream = null;

        try
        {
            HttpServer server = myProviderRegistry.getProvider(HttpServer.class).getServer(url);

            ResponseValues response = new ResponseValues();

            InputStream postData = XMLUtilities.writeXMLObjectToInputStreamSync(request, Execute.class, FilterType.class);

            InputStream responseStream = server.sendPost(url, postData, response);

            if (response.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                returnStream = responseStream;
            }
            else
            {
                LOGGER.error("Error subscribing to " + request.getIdentifier().getValue() + " server returned "
                        + response.getResponseCode() + " " + response.getResponseMessage());
            }
        }
        catch (JAXBException | IOException | URISyntaxException e)
        {
            LOGGER.error(e.getMessage(), e);
        }

        return returnStream;
    }
}
