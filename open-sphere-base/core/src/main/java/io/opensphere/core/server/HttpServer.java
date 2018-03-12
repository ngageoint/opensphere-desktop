package io.opensphere.core.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.Pair;

/**
 * Sends Http requests to a specified server.
 *
 */
public interface HttpServer
{
    /**
     * Gets the host name of the server.
     *
     * @return The server host name.
     */
    String getHost();

    /**
     * Gets the protocol used to communicate with the server.
     *
     * @return The server's protocol.
     */
    String getProtocol();

    /**
     * Sends a file to the specified url.
     *
     * @param postToURL the post to url
     * @param metaDataParts map of name to data for parts to precede the main
     *            file part
     * @param fileToPost the file to post
     * @param response the response
     * @return the input stream
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws URISyntaxException Thrown if an error occurs converting the URL
     *             to a URI.
     */
    CancellableInputStream postFile(URL postToURL, Map<String, String> metaDataParts, File fileToPost, ResponseValues response)
        throws IOException, URISyntaxException;

    /**
     * Sends a file to the specified url.
     *
     * @param postToURL the post to url
     * @param fileToPost the file to post
     * @param response the response
     * @return the input stream
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws URISyntaxException Thrown if an error occurs converting the URL
     *             to a URI.
     */
    CancellableInputStream postFile(URL postToURL, File fileToPost, ResponseValues response)
        throws IOException, URISyntaxException;

    /**
     * Gets a host and port to connect to based on the configured proxy settings
     * for this server.
     *
     * @param destination The url to the destination.
     * @return The proxy host and port or null if a proxy server is not
     *         configured for this guy.
     */
    Pair<String, Integer> resolveProxy(URL destination);

    /**
     * Gets a Proxy based on the configured proxy settings for this server.
     *
     * @param destination The url to the destination.
     * @return The ProxyHostConfig or null if a proxy server is not configured
     *         for this guy.
     */
    Proxy resolveProxyConfig(URL destination);

    /**
     * Sends a delete request to the server.
     *
     * @param url The url to the server which should include any parameters.
     * @param response The response code and message returned from the delete
     *            request.
     * @return The input stream containing the data returned by the delete
     *         request.
     * @throws IOException Thrown if an error happens when communicating with
     *             the server.
     * @throws URISyntaxException Thrown if an error occurs converting the URL
     *             to a URI.
     */
    CancellableInputStream sendDelete(URL url, ResponseValues response) throws IOException, URISyntaxException;

    /**
     * Sends a get request to the server.
     *
     * @param url The url to the server which should include any parameters.
     * @param extraHeaderValues Header values to add to the request header.
     * @param response The response code and message returned from the get
     *            request.
     * @return The input stream containing the data returned by the get request.
     * @throws IOException Thrown if an error happens when communicating with
     *             the server.
     * @throws URISyntaxException Thrown if an error occurs converting the URL
     *             to a URI.
     */
    CancellableInputStream sendGet(URL url, Map<String, String> extraHeaderValues, ResponseValues response)
        throws IOException, URISyntaxException;

    /**
     * Sends a get request to the server.
     *
     * @param url The url to the server which should include any parameters.
     * @param response The response code and message returned from the get
     *            request.
     * @return The input stream containing the data returned by the get request.
     * @throws IOException Thrown if an error happens when communicating with
     *             the server.
     * @throws URISyntaxException Thrown if an error occurs converting the URL
     *             to a URI.
     */
    CancellableInputStream sendGet(URL url, ResponseValues response) throws IOException, URISyntaxException;

    /**
     * Sends a HEAD request to the server.
     *
     * @param url The url to the server which should include any parameters.
     * @param extraHeaderValues Header values to add to the request header.
     * @param response The response code and message returned from the get
     *            request.
     * @throws IOException Thrown if an error happens when communicating with
     *             the server.
     * @throws URISyntaxException Thrown if an error occurs converting the URL
     *             to a URI.
     */
    void sendHead(URL url, Map<String, String> extraHeaderValues, ResponseValues response) throws IOException, URISyntaxException;

    /**
     * Sends a HEAD request to the server.
     *
     * @param url The url to the server which should include any parameters.
     * @param response The response code and message returned from the get
     *            request.
     * @throws IOException Thrown if an error happens when communicating with
     *             the server.
     * @throws URISyntaxException Thrown if an error occurs converting the URL
     *             to a URI.
     */
    void sendHead(URL url, ResponseValues response) throws IOException, URISyntaxException;

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
            ContentType contentType)
        throws IOException, URISyntaxException;

    /**
     * Sends a post request to the server.
     *
     * @param url The url to send the post request to.
     * @param postData The data to send to the post.
     * @param response The response code and message returned from the server.
     * @return The input stream containing the data returned by the post
     *         request.
     * @throws IOException Thrown if an error occurs while communicating with
     *             the server.
     * @throws URISyntaxException Thrown if an error occurs converting the URL
     *             to a URI.
     */
    CancellableInputStream sendPost(URL url, InputStream postData, ResponseValues response)
        throws IOException, URISyntaxException;

    /**
     * Sends a post request to the server.
     *
     * @param url The url to send the post request to.
     * @param postData The data to send to the post.
     * @param response The response code and message returned from the server.
     * @param contentType The content type of the post data.
     * @return The input stream containing the data returned by the post
     *         request.
     * @throws IOException Thrown if an error occurs while communicating with
     *             the server.
     * @throws URISyntaxException Thrown if an error occurs converting the URL
     *             to a URI.
     */
    CancellableInputStream sendPost(URL url, InputStream postData, ResponseValues response, ContentType contentType)
        throws IOException, URISyntaxException;

    /**
     * Sends a post request to the server.
     *
     * @param url The url to send the post request to.
     * @param extraHeaderValues Any extra header information to add to the post
     *            request.
     * @param postData The data to send to the post.
     * @param response The response code and message returned from the server.
     * @return The input stream containing the data returned by the post
     *         request.
     * @throws IOException Thrown if an error occurs while communicating with
     *             the server.
     * @throws URISyntaxException Thrown if an error occurs converting the URL
     *             to a URI.
     */
    CancellableInputStream sendPost(URL url, Map<String, String> extraHeaderValues, Map<String, String> postData,
            ResponseValues response)
        throws IOException, URISyntaxException;

    /**
     * Sends a post request to the server.
     *
     * @param url The url to send the post request to.
     * @param postData The data to send to the post.
     * @param response The response code and message returned from the server.
     * @return The input stream containing the data returned by the post
     *         request.
     * @throws IOException Thrown if an error occurs while communicating with
     *             the server.
     * @throws URISyntaxException Thrown if an error occurs converting the URL
     *             to a URI.
     */
    CancellableInputStream sendPost(URL url, Map<String, String> postData, ResponseValues response)
        throws IOException, URISyntaxException;

    /**
     * Sets the size of the buffer used when reading http messages.
     *
     * @param bufferSize The size of the buffer to use in bytes, must be greater
     *            than 0.
     */
    void setBufferSize(int bufferSize);

    /**
     * Sets the timeouts.
     *
     * @param readTimeout The read timeout.
     * @param connectTimeout The connect timeout.
     */
    void setTimeouts(int readTimeout, int connectTimeout);
}
