package io.opensphere.server.serverprovider.http.requestors;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.util.io.CancellableInputStream;

/**
 * Sends a get request to the server.
 *
 */
public interface GetRequestor
{
    /**
     * Sends a get request to the server.
     *
     * @param url The url to the server which should include any parameters.
     * @param extraHeaderValues Header values to add to the request.
     * @param responseValues The response code and message returned from the get
     *            request.
     *
     * @return The input stream containing the data returned by the get request.
     * @throws IOException Thrown if an error happens when communicating with
     *             the server.
     * @throws URISyntaxException If the url could not be translated to a URI.
     */
    CancellableInputStream sendGet(URL url, Map<String, String> extraHeaderValues, ResponseValues responseValues)
        throws IOException, URISyntaxException;

    /**
     * Sends a get request to the server.
     *
     * @param url The url to the server which should include any parameters.
     * @param responseValues The response code and message returned from the get
     *            request.
     * @return The input stream containing the data returned by the get request.
     * @throws IOException Thrown if an error happens when communicating with
     *             the server.
     * @throws URISyntaxException If the url could not be translated to a URI.
     */
    CancellableInputStream sendGet(URL url, ResponseValues responseValues) throws IOException, URISyntaxException;
}
