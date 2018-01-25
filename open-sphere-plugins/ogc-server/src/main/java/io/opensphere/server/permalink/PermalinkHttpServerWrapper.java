package io.opensphere.server.permalink;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import io.opensphere.core.server.ContentType;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.server.toolbox.FilePayload;
import io.opensphere.server.toolbox.PermalinkController;

/**
 * Wrapper of a {@link HttpServer} that provides support for permalinks.
 */
public class PermalinkHttpServerWrapper implements PermalinkHttpServer
{
    /** The permalinker. */
    private final PermalinkController myPermalinker;

    /** The wrapped server. */
    private final HttpServer myServer;

    /**
     * Constructor.
     *
     * @param server The wrapped server.
     * @param permalinkController The permalink controller.
     */
    public PermalinkHttpServerWrapper(HttpServer server, PermalinkController permalinkController)
    {
        myServer = server;
        myPermalinker = permalinkController;
    }

    @Override
    public InputStream downloadFile(String fileUrl) throws IOException, URISyntaxException
    {
        return myPermalinker.downloadFile(fileUrl, this);
    }

    @Override
    public String getHost()
    {
        return myServer.getHost();
    }

    @Override
    public String getProtocol()
    {
        return myServer.getProtocol();
    }

    @Override
    public CancellableInputStream postFile(URL postToURL, Map<String, String> metaDataParts, File fileToPost,
            ResponseValues response)
        throws IOException, URISyntaxException
    {
        return myServer.postFile(postToURL, metaDataParts, fileToPost, response);
    }

    @Override
    public CancellableInputStream postFile(URL postToURL, File fileToPost, ResponseValues response)
        throws IOException, URISyntaxException
    {
        return myServer.postFile(postToURL, fileToPost, response);
    }

    @Override
    public Pair<String, Integer> resolveProxy(URL destination)
    {
        return myServer.resolveProxy(destination);
    }

    @Override
    public Proxy resolveProxyConfig(URL destination)
    {
        return myServer.resolveProxyConfig(destination);
    }

    @Override
    public CancellableInputStream sendDelete(URL url, ResponseValues response) throws IOException, URISyntaxException
    {
        return myServer.sendDelete(url, response);
    }

    @Override
    public CancellableInputStream sendGet(URL url, Map<String, String> extraHeaderValues, ResponseValues response)
        throws IOException, URISyntaxException
    {
        return myServer.sendGet(url, extraHeaderValues, response);
    }

    @Override
    public CancellableInputStream sendGet(URL url, ResponseValues response) throws IOException, URISyntaxException
    {
        return myServer.sendGet(url, response);
    }

    @Override
    public CancellableInputStream sendPost(URL url, InputStream postData, Map<String, String> extraHeaderValues,
            ResponseValues response, ContentType contentType)
        throws IOException, URISyntaxException
    {
        return myServer.sendPost(url, postData, extraHeaderValues, response, contentType);
    }

    @Override
    public CancellableInputStream sendPost(URL url, InputStream postData, ResponseValues response)
        throws IOException, URISyntaxException
    {
        return myServer.sendPost(url, postData, response);
    }

    @Override
    public CancellableInputStream sendPost(URL url, InputStream postData, ResponseValues response, ContentType contentType)
        throws IOException, URISyntaxException
    {
        return myServer.sendPost(url, postData, response, contentType);
    }

    @Override
    public CancellableInputStream sendPost(URL url, Map<String, String> extraHeaderValues, Map<String, String> postData,
            ResponseValues response)
        throws IOException, URISyntaxException
    {
        return myServer.sendPost(url, extraHeaderValues, postData, response);
    }

    @Override
    public CancellableInputStream sendPost(URL url, Map<String, String> postData, ResponseValues response)
        throws IOException, URISyntaxException
    {
        return myServer.sendPost(url, postData, response);
    }

    @Override
    public void setBufferSize(int bufferSize)
    {
        myServer.setBufferSize(bufferSize);
    }

    @Override
    public void setTimeouts(int readTimeout, int connectTimeout)
    {
        myServer.setTimeouts(readTimeout, connectTimeout);
    }

    @Override
    public String uploadFile(FilePayload payload) throws IOException, URISyntaxException
    {
        return myPermalinker.uploadFile(payload, this);
    }
}
