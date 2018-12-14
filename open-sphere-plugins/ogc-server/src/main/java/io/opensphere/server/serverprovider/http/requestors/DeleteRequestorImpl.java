package io.opensphere.server.serverprovider.http.requestors;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import com.bitsys.common.http.client.HttpClient;
import com.bitsys.common.http.message.HttpRequest;
import com.bitsys.common.http.message.HttpRequestFactory;

import io.opensphere.core.event.EventManager;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.server.serverprovider.http.header.HeaderValues;

/**
 * Sends a delete request to the server.
 *
 */
public class DeleteRequestorImpl extends BaseRequestor implements DeleteRequestor
{
    /**
     * Constructs a delete requestor.
     *
     * @param client The HttpClient object to use to communicate with the
     *            server.
     * @param headerValues Contains the header values.
     * @param eventManager The manager through which events are sent.
     */
    public DeleteRequestorImpl(HttpClient client, HeaderValues headerValues, EventManager eventManager)
    {
        super(client, headerValues, eventManager);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.server.serverprovider.http.requestors.DeleteRequestor#sendDelete(java.net.URL,
     *      java.util.Map, io.opensphere.core.server.ResponseValues)
     */
    @Override
    public CancellableInputStream sendDelete(URL url, Map<String, String> extraHeaderValues, ResponseValues responseValues)
        throws IOException, URISyntaxException
    {
        HttpRequest request = HttpRequestFactory.getInstance().delete(url.toURI());
        for (Entry<String, String> extraHeader : extraHeaderValues.entrySet())
        {
            request.getHeaders().put(extraHeader.getKey(), extraHeader.getValue());
        }
        CancellableInputStream responseStream = executeRequest(request, responseValues);
        return handleRedirect(responseStream, responseValues, new Function<String, HttpRequest>()
        {
            @Override
            public HttpRequest apply(String newUrlString)
            {
                return HttpRequestFactory.getInstance().delete(URI.create(newUrlString));
            }
        });
    }

    @Override
    public CancellableInputStream sendDelete(URL url, ResponseValues responseValues) throws IOException, URISyntaxException
    {
        HttpRequest request = HttpRequestFactory.getInstance().delete(url.toURI());
        CancellableInputStream responseStream = executeRequest(request, responseValues);
        return handleRedirect(responseStream, responseValues, new Function<String, HttpRequest>()
        {
            @Override
            public HttpRequest apply(String newUrlString)
            {
                return HttpRequestFactory.getInstance().delete(URI.create(newUrlString));
            }
        });
    }
}
