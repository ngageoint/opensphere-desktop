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
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.server.serverprovider.http.header.HeaderValues;

/**
 * Sends a get request to the server.
 *
 */
public class GetRequestorImpl extends BaseRequestor implements GetRequestor
{
    /**
     * Constructs a get requestor.
     *
     * @param client The HttpClient object to use to communicate with the
     *            server.
     * @param headerValues Contains the header values.
     * @param eventManager The manager through which events are sent.
     */
    public GetRequestorImpl(HttpClient client, HeaderValues headerValues, EventManager eventManager)
    {
        super(client, headerValues, eventManager);
    }

    @Override
    public CancellableInputStream sendGet(URL url, Map<String, String> extraHeaderValues, ResponseValues responseValues)
        throws IOException, URISyntaxException
    {
        HttpRequest request = HttpRequestFactory.getInstance().get(url.toURI());
        for (Entry<String, String> extraHeader : extraHeaderValues.entrySet())
        {
            request.getHeaders().put(extraHeader.getKey(), extraHeader.getValue());
        }
        return super.executeRequest(request, responseValues);
    }

    @Override
    public CancellableInputStream sendGet(URL url, ResponseValues responseValues) throws IOException, URISyntaxException
    {
        HttpRequest request = HttpRequestFactory.getInstance().get(url.toURI());
        return super.executeRequest(request, responseValues);
    }
}
