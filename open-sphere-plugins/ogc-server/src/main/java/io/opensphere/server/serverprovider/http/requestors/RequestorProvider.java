package io.opensphere.server.serverprovider.http.requestors;

import java.net.URL;

import com.bitsys.common.http.client.HttpClient;
import com.bitsys.common.http.proxy.ProxyHostConfig;

import io.opensphere.core.util.lang.Pair;

/**
 * Provides the different requestors available to use to communicate with an
 * Http server.
 */
public interface RequestorProvider
{
    /**
     * Gets the connection to the server.
     *
     * @return The connection to the server.
     */
    HttpClient getClient();

    /**
     * Gets the delete requestor.
     *
     * @return The delete requestor.
     */
    DeleteRequestor getDeleteRequestor();

    /**
     * Gets the file post requestor.
     *
     * @return The file post requestor.
     */
    FilePostRequestor getFilePoster();

    /**
     * Gets the post requestor.
     *
     * @return The post requestor.
     */
    PostRequestor getPostRequestor();

    /**
     * Gets the get requestor.
     *
     * @return The get requestor.
     */
    GetRequestor getRequestor();

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
     * Gets a ProxyHostConfig based on the configured proxy settings for this
     * server.
     *
     * @param destination The url to the destination.
     * @return The ProxyHostConfig or null if a proxy server is not configured
     *         for this guy.
     */
    ProxyHostConfig resolveProxyConfig(URL destination);

    /**
     * Sets the buffer size to use when reading from the socket.
     *
     * @param bufferSize The size of the buffer in bytes, must be greater than
     *            0.
     */
    void setBufferSize(int bufferSize);

    /**
     * Sets the timeouts.
     *
     * @param readTimeout The read timeout in milliseconds.
     * @param connectTimeout The connect timeout in milliseconds.
     */
    void setTimeouts(int readTimeout, int connectTimeout);
}
