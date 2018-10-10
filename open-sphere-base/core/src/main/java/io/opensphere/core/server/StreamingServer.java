package io.opensphere.core.server;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.datafilter.DataFilter;

/**
 * Interface to a server that provides data streaming capabilities.
 */
public interface StreamingServer
{
    /**
     * Gets a list of available streams from the server. These streams can be
     * passed to the start method in order to start data streaming.
     *
     * @return The list of unique stream names available to the server.
     */
    List<String> getAvailableStreams();

    /**
     * Gets the URL to the server.
     *
     * @return The url to the server.
     */
    String getURL();

    /**
     * Starts streaming data for the given stream.
     *
     * @param stream A stream, returned from getAvailableStream, to start.
     * @param handler The handler to notify when new data has arrived.
     * @param executor Provides threads to run tasks on.
     *
     * @return The id of the started stream, used later to stop the stream.
     *
     * @throws IOException If there was an error communicating with the server.
     */
    UUID start(String stream, StreamHandler handler, ExecutorService executor) throws IOException;

    /**
     * Starts streaming data for the given stream applying the specified filter
     * to the stream.
     *
     * @param stream A stream, returned from getAvailableStream, to start.
     * @param handler The handler to notify when new data has arrived.
     * @param executor Provides threads to run tasks on.
     * @param filter The filter to apply on the incoming data.
     * @param spatialFilter The optional spatial filter to apply on the incoming
     *            data.
     *
     * @return The id of the started stream, used later to stop the stream.
     *
     * @throws IOException If there was an error communicating with the server.
     */
    UUID start(String stream, StreamHandler handler, ExecutorService executor, DataFilter filter, Geometry spatialFilter)
            throws IOException;

    /**
     * Stops the stream.
     *
     * @param streamId The id of the stream to stop.
     */
    void stop(UUID streamId);
}
