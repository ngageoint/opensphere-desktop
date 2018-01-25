package io.opensphere.server.serverprovider.http.requestors;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Function;

import com.bitsys.common.http.client.HttpClient;
import com.bitsys.common.http.message.HttpRequest;
import com.bitsys.common.http.message.HttpRequestFactory;

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
     */
    public DeleteRequestorImpl(HttpClient client, HeaderValues headerValues)
    {
        super(client, headerValues);
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
