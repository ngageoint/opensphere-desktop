package com.bitsys.common.http.client;

import java.io.IOException;

import com.bitsys.common.http.message.HttpRequest;
import com.bitsys.common.http.message.HttpResponse;

/**
 * This interface specifies the methods for an HTTP client.
 *
 * @see org.apache.http.client.HttpClient
 */
public interface HttpClient
{
    /**
     * Returns the options for this HTTP client. The options take effect upon
     * the first invocation of {@link #execute(HttpRequest)}. After that time,
     * options may no longer be changeable.
     *
     * @return the options for this HTTP client.
     */
    HttpClientOptions getOptions();

    /**
     * Executes the specified HTTP request.
     * <p>
     * The caller is responsible for cleaning up the response's entity: <br/>
     *
     * <pre>
     * HttpEntity entity = httpResponse.getEntity();
     * if (entity != null)
     * {
     *     InputStream inputStream = entity.getContent();
     *     try
     *     {
     *         // Process the entity.
     *     }
     *     finally
     *     {
     *         inputStream.close();
     *     }
     * }
     * </pre>
     *
     * @param httpRequest the HTTP request to execute.
     * @return the response from the request.
     * @throws IOException if an  error occurs while processing the request or
     *             response.
     */
    HttpResponse execute(final HttpRequest httpRequest) throws IOException;

    /**
     * Causes the client to clear its cache for the specified options.
     *
     * @param options the options for data to clear.
     */
    void clearCache(final ClearDataOptions options);
}
