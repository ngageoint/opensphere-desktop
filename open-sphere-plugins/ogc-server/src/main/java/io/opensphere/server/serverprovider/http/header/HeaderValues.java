package io.opensphere.server.serverprovider.http.header;

/**
 * Gets certain header value to supply to the server.
 *
 */
public interface HeaderValues
{
    /**
     * Gets the Accept-Encoding header value.
     *
     * @return The Accept-Encoding header value.
     */
    String getEncoding();

    /**
     * Gets the zip encoding.
     *
     * @return The zip encoding.
     */
    String getZippedEncoding();

    /**
     * Gets the User-Agent header value.
     *
     * @return The User-Agent header value.
     */
    String getUserAgent();

    /**
     * Gets the Accept header value.
     *
     * @return The Accept header value.
     */
    String getAccept();
}
