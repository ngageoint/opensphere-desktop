package io.opensphere.core.server.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.server.StreamingServer;
import io.opensphere.core.util.XMLUtilities;

/**
 * Abstract {@link StreamingServer} that returns a specific data type to the
 * handler passed into {@link #start(String, Consumer)}.
 *
 * @param <T> The data type returned to the handler
 */
public abstract class AbstractDataStreamingServer<T> extends AbstractStreamingServer
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(AbstractDataStreamingServer.class);

    /**
     * Constructor.
     *
     * @param baseUrl The base URL
     * @param delay The delay between the termination of one query and the
     *            commencement of the next
     * @param provider The server provider
     */
    public AbstractDataStreamingServer(URL baseUrl, Duration delay, ServerProvider<HttpServer> provider)
    {
        super(baseUrl, delay, provider);
    }

    /**
     * Constructor.
     *
     * @param baseUrl The base URL
     * @param delay The delay between the termination of one query and the
     *            commencement of the next
     * @param provider The server provider
     * @param executor The executor
     */
    public AbstractDataStreamingServer(URL baseUrl, Duration delay, ServerProvider<HttpServer> provider,
            ScheduledExecutorService executor)
    {
        super(baseUrl, delay, provider, executor);
    }

    /**
     * Starts streaming data for the given stream.
     *
     * @param stream A stream, returned from getAvailableStream, to start.
     * @param dataHandler The handler to notify when new data has arrived.
     * @return The id of the started stream, used later to stop the stream.
     * @throws IOException If there was an error communicating with the server.
     */
    public UUID start(final String stream, final Consumer<T> dataHandler) throws IOException
    {
        return start(stream, (streamId, inputStream) -> handleData(stream, streamId, inputStream, dataHandler), null);
    }

    /**
     * Gets data from the input stream.
     *
     * @param stream the stream
     * @param streamId the stream ID
     * @param inputStream The stream of new data.
     * @return The data
     * @throws IOException if an exception is thrown getting the data
     */
    protected abstract T getData(String stream, UUID streamId, InputStream inputStream) throws IOException;

    /**
     * Convenience method that requests data from the URL and parses the XML
     * response into the given object type.
     *
     * @param <D> The type of object being read.
     * @param url The url
     * @param target The type of object being read.
     * @return the data object
     * @throws IOException If a problem occurs reading from the URL
     * @throws JAXBException If a problem occurs parsing the results
     */
    protected <D> D requestData(URL url, Class<D> target) throws IOException, JAXBException
    {
        ResponseValues response = new ResponseValues();
        InputStream inputStream = sendGet(getServerConnection(), url, response);
        return readAndCloseStream(inputStream, target);
    }

    /**
     * Called when a new data is available, implementations of this call must be
     * synchronous.
     *
     * @param stream the stream
     * @param streamId the stream ID
     * @param inputStream The stream of new data.
     * @param feedHandler The handler to notify when new data has arrived.
     */
    private void handleData(String stream, UUID streamId, InputStream inputStream, Consumer<T> feedHandler)
    {
        try
        {
            T data = getData(stream, streamId, inputStream);
            if (data != null)
            {
                feedHandler.accept(data);
            }
        }
        catch (IOException | RuntimeException | Error e)
        {
            // Decrement success, because it was incorrectly incremented if we got here
            getSuccessCount().decrementAndGet();
            error(streamId, e);
        }
    }

    /**
     * Read a JAXB object from the stream, and ensures the stream is closed when
     * done. Use a JAXB context that contains only the target class.
     *
     * @param inputStream The stream.
     * @param target The type of object being read.
     *
     * @param <T> The type of object being read.
     * @return The object.
     * @throws JAXBException If the object cannot be unmarshalled.
     */
    protected static <T> T readAndCloseStream(InputStream inputStream, Class<T> target) throws JAXBException
    {
        T object;
        try
        {
            object = XMLUtilities.readXMLObject(inputStream, target);
        }
        finally
        {
            try
            {
                inputStream.close();
            }
            catch (IOException e)
            {
                LOGGER.error(e);
            }
        }
        return object;
    }
}
