package io.opensphere.server.serverprovider.http;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import com.bitsys.common.http.proxy.ProxyHostConfig;

import io.opensphere.core.server.ContentType;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.server.serverprovider.ProxySelectorImpl;
import io.opensphere.server.serverprovider.http.requestors.RequestorProvider;

/**
 * Sends http requests to a specified server.
 *
 */
public class HttpServerImpl implements HttpServer
{
    /**
     * The server host name.
     */
    private final String myHost;

    /**
     * The protocol used to connect to the server, e.g. http or https.
     */
    private final String myProtocol;

    /**
     * Provides the different requestors used to communicate with an http
     * server.
     */
    private final RequestorProvider myRequestorProvider;

    /**
     * Constructs an HttpServerImpl.
     *
     * @param host The server host name.
     * @param protocol The protocol used to connect to the server, e.g. http or
     *            https.
     * @param requestorProvider Provides the different requestors used to
     *            communicate with an http server.
     */
    public HttpServerImpl(String host, String protocol, RequestorProvider requestorProvider)
    {
        myHost = host;
        myProtocol = protocol;
        myRequestorProvider = requestorProvider;
    }

    @Override
    public String getHost()
    {
        return myHost;
    }

    @Override
    public String getProtocol()
    {
        return myProtocol;
    }

    /**
     * Gets the requestor provider.
     *
     * @return The requestor provider.
     */
    public RequestorProvider getRequestProvider()
    {
        return myRequestorProvider;
    }

    @Override
    public CancellableInputStream postFile(URL postToURL, Map<String, String> metaDataParts, File fileToPost,
            ResponseValues response)
        throws IOException, URISyntaxException
    {
        assert !EventQueue.isDispatchThread();

        return myRequestorProvider.getFilePoster().postFileToServer(postToURL, metaDataParts, fileToPost, response);
    }

    @Override
    public CancellableInputStream postFile(URL postToURL, File fileToPost, ResponseValues response)
        throws IOException, URISyntaxException
    {
        assert !EventQueue.isDispatchThread();

        return myRequestorProvider.getFilePoster().postFileToServer(postToURL, fileToPost, response);
    }

    @Override
    public Pair<String, Integer> resolveProxy(URL destination)
    {
        return myRequestorProvider.resolveProxy(destination);
    }

    @Override
    public Proxy resolveProxyConfig(URL destination)
    {
        Proxy proxy = null;
        ProxyHostConfig config = myRequestorProvider.resolveProxyConfig(destination);
        if (config != null)
        {
            proxy = ProxySelectorImpl.getProxy(config);
        }
        return proxy;
    }

    @Override
    public CancellableInputStream sendDelete(URL url, ResponseValues response) throws IOException, URISyntaxException
    {
        assert !EventQueue.isDispatchThread();

        return myRequestorProvider.getDeleteRequestor().sendDelete(url, response);
    }

    @Override
    public CancellableInputStream sendGet(URL url, Map<String, String> extraHeaderValues, ResponseValues response)
        throws IOException, URISyntaxException
    {
        assert !EventQueue.isDispatchThread();

        return myRequestorProvider.getRequestor().sendGet(url, extraHeaderValues, response);
    }

    @Override
    public CancellableInputStream sendGet(URL url, ResponseValues response) throws IOException, URISyntaxException
    {
        assert !EventQueue.isDispatchThread();

        return myRequestorProvider.getRequestor().sendGet(url, response);
    }

    @Override
    public CancellableInputStream sendPost(URL url, InputStream postData, Map<String, String> extraHeaderValues,
            ResponseValues response, ContentType contentType)
        throws IOException, URISyntaxException
    {
        assert !EventQueue.isDispatchThread();

        com.bitsys.common.http.header.ContentType commonContentType = com.bitsys.common.http.header.ContentType.APPLICATION_XML;

        if (contentType == ContentType.JSON)
        {
            commonContentType = com.bitsys.common.http.header.ContentType.APPLICATION_JSON;
        }

        return myRequestorProvider.getPostRequestor().sendPost(url, postData, extraHeaderValues, response, commonContentType);
    }

    @Override
    public CancellableInputStream sendPost(URL url, InputStream postData, ResponseValues response)
        throws IOException, URISyntaxException
    {
        assert !EventQueue.isDispatchThread();

        return myRequestorProvider.getPostRequestor().sendPost(url, postData, response);
    }

    @Override
    public CancellableInputStream sendPost(URL url, InputStream postData, ResponseValues response, ContentType contentType)
        throws IOException, URISyntaxException
    {
        assert !EventQueue.isDispatchThread();

        com.bitsys.common.http.header.ContentType commonContentType = com.bitsys.common.http.header.ContentType.APPLICATION_XML;

        if (contentType == ContentType.JSON)
        {
            commonContentType = com.bitsys.common.http.header.ContentType.APPLICATION_JSON;
        }

        return myRequestorProvider.getPostRequestor().sendPost(url, postData, response, commonContentType);
    }

    @Override
    public CancellableInputStream sendPost(URL url, Map<String, String> extraHeaderValues, Map<String, String> postData,
            ResponseValues response)
        throws IOException, URISyntaxException
    {
        return myRequestorProvider.getPostRequestor().sendPost(url, extraHeaderValues, postData, response);
    }

    @Override
    public CancellableInputStream sendPost(URL url, Map<String, String> postData, ResponseValues response)
        throws IOException, URISyntaxException
    {
        assert !EventQueue.isDispatchThread();

        return myRequestorProvider.getPostRequestor().sendPost(url, postData, response);
    }

    @Override
    public void setBufferSize(int bufferSize)
    {
        myRequestorProvider.setBufferSize(bufferSize);
    }

    @Override
    public void setTimeouts(int readTimeout, int connectTimeout)
    {
        myRequestorProvider.setTimeouts(readTimeout, connectTimeout);
    }
}
