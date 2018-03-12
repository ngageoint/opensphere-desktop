package io.opensphere.server.serverprovider.http.requestors;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import io.opensphere.core.server.ResponseValues;

/**
 * A requestor definition to send HEAD requests to the server. Note that per
 * <a href="https://tools.ietf.org/html/rfc7231#section-4.3.2">HTTP 1.1</a>, a
 * HEAD request does not send a response body, only the response headers.
 * Specifically:
 *
 * <pre>
 * The HEAD method is identical to GET except that the server MUST NOT
 * send a message body in the response (i.e., the response terminates at
 * the end of the header section).  The server SHOULD send the same
 * header fields in response to a HEAD request as it would have sent if
 * the request had been a GET, except that the payload header fields
 * (Section 3.3) MAY be omitted.  This method can be used for obtaining
 * metadata about the selected representation without transferring the
 * representation data and is often used for testing hypertext links for
 * validity, accessibility, and recent modification.
 *
 * A payload within a HEAD request message has no defined semantics;
 * sending a payload body on a HEAD request might cause some existing
 * implementations to reject the request.
 *
 * The response to a HEAD request is cacheable; a cache MAY use it to
 * satisfy subsequent HEAD requests unless otherwise indicated by the
 * Cache-Control header field (Section 5.2 of [RFC7234]).  A HEAD
 * response might also have an effect on previously cached responses to
 * GET; see Section 4.3.5 of [RFC7234].
 * </pre>
 */
public interface HeadRequestor
{
    /**
     * Sends a HEAD request to the server.
     *
     * @param url The url to the server which should include any parameters.
     * @param extraHeaderValues Header values to add to the request.
     * @param responseValues The response code and message returned from the get
     *            request.
     * @throws IOException Thrown if an error happens when communicating with
     *             the server.
     * @throws URISyntaxException If the url could not be translated to a URI.
     */
    void sendHead(URL url, Map<String, String> extraHeaderValues, ResponseValues responseValues)
        throws IOException, URISyntaxException;

    /**
     * Sends a HEAD request to the server.
     *
     * @param url The URL to the server which should include any parameters.
     * @param responseValues The response code and message returned from the get
     *            request.
     * @throws IOException Thrown if an error happens when communicating with
     *             the server.
     * @throws URISyntaxException If the URL could not be translated to a URI.
     */
    void sendHead(URL url, ResponseValues responseValues) throws IOException, URISyntaxException;
}
