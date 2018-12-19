package io.opensphere.server.serverprovider.http.requestors;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.function.Function;

import com.bitsys.common.http.client.HttpClient;
import com.bitsys.common.http.entity.MultipartEntity;
import com.bitsys.common.http.entity.content.FileBodyPart;
import com.bitsys.common.http.entity.content.StringBodyPart;
import com.bitsys.common.http.header.ContentType;
import com.bitsys.common.http.message.HttpRequest;
import com.bitsys.common.http.message.HttpRequestFactory;

import io.opensphere.core.event.EventManager;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.server.serverprovider.http.header.HeaderValues;

/**
 * Posts a file to the server.
 *
 */
public class FilePostRequestorImpl extends BaseRequestor implements FilePostRequestor
{
    /**
     * Constructs a new file post requestor.
     *
     * @param client The HttpClient object to use to communicate with the
     *            server.
     * @param headerValues Contains the header values.
     * @param eventManager The manager through which events are sent.
     */
    public FilePostRequestorImpl(HttpClient client, HeaderValues headerValues, EventManager eventManager)
    {
        super(client, headerValues, eventManager);
    }

    @Override
    public CancellableInputStream postFileToServer(URL postToURL, Map<String, String> metaDataParts, File fileToPost,
            ResponseValues response) throws IOException, URISyntaxException
    {
        final MultipartEntity entity = new MultipartEntity();
        for (Map.Entry<String, String> entry : metaDataParts.entrySet())
        {
            entity.addPart(entry.getKey(), new StringBodyPart(entry.getValue(), ContentType.TEXT_PLAIN));
        }
        entity.addPart(fileToPost.getName(), new FileBodyPart(fileToPost));

        HttpRequest request = HttpRequestFactory.getInstance().post(postToURL.toURI(), entity);
        CancellableInputStream responseStream = executeRequest(request, response);
        return handleRedirect(responseStream, response, new Function<String, HttpRequest>()
        {
            @Override
            public HttpRequest apply(String newUrlString)
            {
                return entity.isRepeatable() ? HttpRequestFactory.getInstance().post(URI.create(newUrlString), entity) : null;
            }
        });
    }

    @Override
    public CancellableInputStream postFileToServer(URL postToURL, File fileToPost,
            ResponseValues response) throws IOException, URISyntaxException
    {
        final MultipartEntity entity = new MultipartEntity();
        entity.addPart(fileToPost.getName(), new FileBodyPart(fileToPost));

        HttpRequest request = HttpRequestFactory.getInstance().post(postToURL.toURI(), entity);
        CancellableInputStream responseStream = executeRequest(request, response);
        return handleRedirect(responseStream, response, new Function<String, HttpRequest>()
        {
            @Override
            public HttpRequest apply(String newUrlString)
            {
                return entity.isRepeatable() ? HttpRequestFactory.getInstance().post(URI.create(newUrlString), entity) : null;
            }
        });
    }
}
