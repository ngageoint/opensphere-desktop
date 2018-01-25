package io.opensphere.server.serverprovider.http.requestors;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import com.bitsys.common.http.client.HttpClient;
import com.bitsys.common.http.entity.FormEntity;
import com.bitsys.common.http.entity.HttpEntity;
import com.bitsys.common.http.entity.StringEntity;
import com.bitsys.common.http.header.ContentType;
import com.bitsys.common.http.message.HttpRequest;
import com.bitsys.common.http.message.HttpRequestFactory;

import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.server.serverprovider.http.header.HeaderValues;

/**
 * Sends a post request to the server.
 *
 */
public class PostRequestorImpl extends BaseRequestor implements PostRequestor
{
    /**
     * Constructs a post requestor.
     *
     * @param client The HttpClient object to use to communicate with the
     *            server.
     * @param headerValues Contains the header values.
     */
    public PostRequestorImpl(HttpClient client, HeaderValues headerValues)
    {
        super(client, headerValues);
    }

    @Override
    public CancellableInputStream sendPost(URL url, InputStream postData, Map<String, String> extraHeaderValues,
            ResponseValues response, ContentType contentType) throws IOException, URISyntaxException
    {
        // Convert the input stream into a string so that apache will use a
        // repeatable entity which will allow redirects to work.
        String stringData = new StreamReader(postData).readStreamIntoString(StringUtilities.DEFAULT_CHARSET);
        HttpEntity entity = new StringEntity(stringData, contentType);
        return sendPost(url, response, entity, extraHeaderValues);
    }

    @Override
    public CancellableInputStream sendPost(URL url, InputStream postData, ResponseValues responseValues)
        throws IOException, URISyntaxException
    {
        return sendPost(url, postData, responseValues, ContentType.APPLICATION_XML);
    }

    @Override
    public CancellableInputStream sendPost(URL url, InputStream postData, ResponseValues responseValues, ContentType contentType)
        throws IOException, URISyntaxException
    {
        return sendPost(url, postData, Collections.emptyMap(), responseValues, contentType);
    }

    @Override
    public CancellableInputStream sendPost(URL url, Map<String, String> extraHeaderValues, Map<String, String> postData,
            ResponseValues responseValues) throws IOException, URISyntaxException
    {
        HttpEntity entity = new FormEntity(postData.entrySet());
        return sendPost(url, responseValues, entity, extraHeaderValues);
    }

    @Override
    public CancellableInputStream sendPost(URL url, Map<String, String> postData, ResponseValues responseValues)
        throws IOException, URISyntaxException
    {
        HttpEntity entity = new FormEntity(postData.entrySet());
        return sendPost(url, responseValues, entity, Collections.emptyMap());
    }

    /**
     * Sends a post request to the server.
     *
     * @param url The url to send the post request to.
     * @param responseValues The response code and message returned from the
     *            server.
     * @param entity The http entity
     * @param extraHeaderValues Contains header values to add to the post
     *            request.
     * @return The input stream containing the data returned by the post
     *         request.
     * @throws IOException Thrown if an error occurs while communicating with
     *             the server.
     * @throws URISyntaxException Thrown if the url could not convert to a URI.
     */
    private CancellableInputStream sendPost(URL url, ResponseValues responseValues, final HttpEntity entity,
            Map<String, String> extraHeaderValues) throws URISyntaxException, IOException
    {
        HttpRequest request = HttpRequestFactory.getInstance().post(url.toURI(), entity);

        for (Entry<String, String> headerValue : extraHeaderValues.entrySet())
        {
            request.getHeaders().put(headerValue.getKey(), headerValue.getValue());
        }

        CancellableInputStream responseStream = executeRequest(request, responseValues);
        return handleRedirect(responseStream, responseValues, new Function<String, HttpRequest>()
        {
            @Override
            public HttpRequest apply(String newUrlString)
            {
                return entity.isRepeatable() ? HttpRequestFactory.getInstance().post(URI.create(newUrlString), entity) : null;
            }
        });
    }
}
