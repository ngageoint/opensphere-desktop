package io.opensphere.server.serverprovider.http.requestors;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import com.bitsys.common.http.header.ContentType;

import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.util.io.CancellableInputStream;

/**
 * Sends a post request to the server.
 *
 */
public interface PostRequestor
{
    /**
     * Sends a post request to the server.
     *
     * @param url The url to send the post request to.
     * @param postData The data to send to the post.
     * @param extraHeaderValues Any extra header information to add to the post
     *            request.
     * @param response The response code and message returned from the server.
     * @param contentType The content type of the post data.
     * @return The input stream containing the data returned by the post
     *         request.
     * @throws IOException Thrown if an error occurs while communicating with
     *             the server.
     * @throws URISyntaxException Thrown if an error occurs converting the URL
     *             to a URI.
     */
    CancellableInputStream sendPost(URL url, InputStream postData, Map<String, String> extraHeaderValues, ResponseValues response,
            ContentType contentType) throws IOException, URISyntaxException;

    /**
     * Sends a post request to the server.
     *
     * @param url The url to send the post request to.
     * @param postData The data to send to the post.
     * @param responseValues The response code and message returned from the
     *            server.
     * @return The input stream containing the data returned by the post
     *         request.
     * @throws IOException Thrown if an error occurs while communicating with
     *             the server.
     * @throws URISyntaxException Thrown if the url could not convert to a URI.
     */
    CancellableInputStream sendPost(URL url, InputStream postData, ResponseValues responseValues)
        throws IOException, URISyntaxException;

    /**
     * Sends a post request to the server.
     *
     * @param url The url to send the post request to.
     * @param postData The data to send to the post.
     * @param responseValues The response code and message returned from the
     *            server.
     * @param contentType The content type of the post data.
     * @return The input stream containing the data returned by the post
     *         request.
     * @throws IOException Thrown if an error occurs while communicating with
     *             the server.
     * @throws URISyntaxException Thrown if the url could not convert to a URI.
     */
    CancellableInputStream sendPost(URL url, InputStream postData, ResponseValues responseValues, ContentType contentType)
        throws IOException, URISyntaxException;

    /**
     * Sends a post request to the server.
     *
     * @param url The url to send the post request to.
     * @param extraHeaderValues Any extra header information to add to the post
     *            request.
     * @param postData The data to send to the post.
     * @param responseValues The response code and message returned from the
     *            server.
     * @return The input stream containing the data returned by the post
     *         request.
     * @throws IOException Thrown if an error occurs while communicating with
     *             the server.
     * @throws URISyntaxException Thrown if the url could not convert to a URI.
     */
    CancellableInputStream sendPost(URL url, Map<String, String> extraHeaderValues, Map<String, String> postData,
            ResponseValues responseValues) throws IOException, URISyntaxException;

    /**
     * Sends a post request to the server.
     *
     * @param url The url to send the post request to.
     * @param postData The data to send to the post.
     * @param responseValues The response code and message returned from the
     *            server.
     * @return The input stream containing the data returned by the post
     *         request.
     * @throws IOException Thrown if an error occurs while communicating with
     *             the server.
     * @throws URISyntaxException Thrown if the url could not convert to a URI.
     */
    CancellableInputStream sendPost(URL url, Map<String, String> postData, ResponseValues responseValues)
        throws IOException, URISyntaxException;
}
