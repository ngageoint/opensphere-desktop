package io.opensphere.server.serverprovider.http.requestors;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import com.bitsys.common.http.client.HttpClient;
import com.bitsys.common.http.message.HttpRequest;
import com.bitsys.common.http.message.HttpRequestFactory;

import io.opensphere.core.event.EventManager;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.server.serverprovider.http.header.HeaderValues;

/**
 * Sends HEAD requests to the server.
 */
public class HeadRequestorImpl extends BaseRequestor implements HeadRequestor
{
    /**
     * Constructs a HEAD requestor.
     *
     * @param client The HttpClient object to use to communicate with the
     *            server.
     * @param headerValues Contains the header values.
     * @param eventManager The manager through which events are sent.
     */
    public HeadRequestorImpl(HttpClient client, HeaderValues headerValues, EventManager eventManager)
    {
        super(client, headerValues, eventManager);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.server.serverprovider.http.requestors.HeadRequestor#sendHead(java.net.URL,
     *      java.util.Map, io.opensphere.core.server.ResponseValues)
     */
    @Override
    public void sendHead(URL url, Map<String, String> extraHeaderValues, ResponseValues responseValues)
        throws IOException, URISyntaxException
    {
        HttpRequest request = HttpRequestFactory.getInstance().get(url.toURI());
        for (Entry<String, String> extraHeader : extraHeaderValues.entrySet())
        {
            request.getHeaders().put(extraHeader.getKey(), extraHeader.getValue());
        }
        super.executeRequest(request, responseValues);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.server.serverprovider.http.requestors.HeadRequestor#sendHead(java.net.URL,
     *      io.opensphere.core.server.ResponseValues)
     */
    @Override
    public void sendHead(URL url, ResponseValues responseValues) throws IOException, URISyntaxException
    {
        HttpRequest request = HttpRequestFactory.getInstance().get(url.toURI());
        super.executeRequest(request, responseValues);
    }
}
