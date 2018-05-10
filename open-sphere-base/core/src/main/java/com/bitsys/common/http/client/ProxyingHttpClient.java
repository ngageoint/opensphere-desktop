package com.bitsys.common.http.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitsys.common.http.auth.ClearableCredentialsProvider;
import com.bitsys.common.http.auth.CredentialsProviderUtils;
import com.bitsys.common.http.proxy.ProxyHostConfig;
import com.bitsys.common.http.proxy.ProxyHostConfig.ProxyType;
import com.bitsys.common.http.proxy.ProxyResolver;

/**
 * This class enables automatic HTTP proxying for HTTP clients.
 */
@SuppressWarnings("deprecation")
public class ProxyingHttpClient implements HttpClient, Closeable
{
    /**
     * The <code>Logger</code> instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyingHttpClient.class);

    /**
     * The back-end HTTP client.
     */
    private final DefaultHttpClient httpClient;

    /**
     * The HTTP client options that contain the proxying configuration.
     */
    private final HttpClientOptions options;

    /**
     * Constructs a <code>ProxyingHttpClient</code> that automatically handles
     * HTTP proxying on the fly.
     *
     * @param httpClient the base client
     * @param options the client options
     */
    public ProxyingHttpClient(final DefaultHttpClient httpClient, final HttpClientOptions options)
    {
        this.httpClient = httpClient;
        this.options = options;
    }

    /**
     * Returns the options.
     *
     * @return the options.
     */
    public HttpClientOptions getOptions()
    {
        return options;
    }

    @Override
    public HttpParams getParams()
    {
        return httpClient.getParams();
    }

    @Override
    public ClientConnectionManager getConnectionManager()
    {
        return httpClient.getConnectionManager();
    }

    @Override
    public HttpResponse execute(final HttpUriRequest request) throws IOException
    {
        configureProxy(request.getParams(), request.getURI());
        return httpClient.execute(request);
    }

    @Override
    public HttpResponse execute(final HttpUriRequest request, final HttpContext context) throws IOException
    {
        configureProxy(request.getParams(), request.getURI());
        return httpClient.execute(request, context);
    }

    @Override
    public HttpResponse execute(final HttpHost target, final HttpRequest request) throws IOException
    {
        configureProxy(request.getParams(), target.toURI());
        return httpClient.execute(target, request);
    }

    @Override
    public HttpResponse execute(final HttpHost target, final HttpRequest request, final HttpContext context) throws IOException
    {
        configureProxy(request.getParams(), target.toURI());
        return httpClient.execute(target, request, context);
    }

    @Override
    public <T> T execute(final HttpUriRequest request, final ResponseHandler<? extends T> responseHandler) throws IOException
    {
        configureProxy(request.getParams(), request.getURI());
        return httpClient.execute(request, responseHandler);
    }

    @Override
    public <T> T execute(final HttpUriRequest request, final ResponseHandler<? extends T> responseHandler,
            final HttpContext context)
        throws IOException
    {
        configureProxy(request.getParams(), request.getURI());
        return httpClient.execute(request, responseHandler, context);
    }

    @Override
    public <T> T execute(final HttpHost target, final HttpRequest request, final ResponseHandler<? extends T> responseHandler)
        throws IOException
    {
        configureProxy(request.getParams(), target.toURI());
        return httpClient.execute(target, request, responseHandler);
    }

    @Override
    public <T> T execute(final HttpHost target, final HttpRequest request, final ResponseHandler<? extends T> responseHandler,
            final HttpContext context)
        throws IOException
    {
        configureProxy(request.getParams(), target.toURI());
        return httpClient.execute(target, request, responseHandler, context);
    }

    /**
     * Attempts to configure the proxy to the given URI in the HTTP parameters.
     *
     * @param params the HTTP parameters in which the proxy configuration will
     *            be stored.
     * @param uriString the requested URI string.
     * @throws IOException if an error occurs while determining the proxy
     *             server.
     */
    protected void configureProxy(final HttpParams params, final String uriString) throws IOException
    {
        try
        {
            configureProxy(params, new URI(uriString));
        }
        catch (final URISyntaxException e)
        {
            LOGGER.warn("Failed to convert '" + uriString + "' to a URI. The proxy will not be configured.", e);
        }
    }

    /**
     * Attempts to configure the proxy to the given URI in the HTTP parameters.
     *
     * @param params the HTTP parameters in which the proxy configuration will
     *            be stored.
     * @param uri the requested URI.
     * @throws IOException if an error occurs while determining the proxy
     *             server.
     */
    protected void configureProxy(final HttpParams params, final URI uri) throws IOException
    {
        try
        {
            configureProxy(params, uri.toURL());
        }
        catch (final MalformedURLException e)
        {
            LOGGER.warn("The URI '" + uri + "' could not be converted to a URL. The proxy will not be configured.", e);
        }
    }

    /**
     * Attempts to configure the proxy to the given scheme, host name and port
     * in the HTTP parameters.
     *
     * @param params the HTTP parameters in which the proxy configuration will
     *            be stored.
     * @param url the destination URL.
     * @throws IOException if an error occurs while determining the proxy
     *             server.
     */
    protected void configureProxy(final HttpParams params, final URL url) throws IOException
    {
        final ProxyConfig proxyConfig = options.getProxyConfig();
        HttpRoutePlanner routePlanner = null;

        // If the options contain a proxy configuration and the parameters do
        // not, attempt to configure the proxy.
        if (proxyConfig != null && proxyConfig.getProxyResolver() != null
                && params.getParameter(ConnRoutePNames.DEFAULT_PROXY) == null)
        {
            final ProxyResolver resolver = proxyConfig.getProxyResolver();
            final List<ProxyHostConfig> configs = resolver.getProxyServer(url);
            if (!configs.isEmpty())
            {
                // TODO: Add support for multiple proxy configurations.
                final ProxyHostConfig config = configs.get(0);

                // If the proxy type is "PROXY", configure the proxy server.
                if (config.getProxyType() == ProxyType.PROXY)
                {
                    final HttpHost proxy = new HttpHost(config.getHost(), config.getPort());
                    routePlanner = new ProxyRoutePlanner(proxy);
                    params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                    LOGGER.debug("Configuring the default proxy: " + proxy);

                    final AuthScope authScope = new AuthScope(proxy);
                    final CredentialsProvider credentialsProvider = httpClient.getCredentialsProvider();

                    // If proxy credentials are set, set the credentials.
                    if (proxyConfig.getCredentials() != null)
                    {
                        final Credentials credentials = CredentialsProviderUtils.toCredentials(proxyConfig.getCredentials());
                        credentialsProvider.setCredentials(authScope, credentials);
                    }

                    // Otherwise, clear the credentials.
                    else if (credentialsProvider instanceof ClearableCredentialsProvider)
                    {
                        final ClearableCredentialsProvider provider = (ClearableCredentialsProvider)credentialsProvider;
                        provider.clearCredentials(authScope);
                    }
                }
                else if (config.getProxyType() != ProxyType.DIRECT)
                {
                    throw new UnsupportedOperationException("'" + config.getProxyType() + "' proxying is not supported!");
                }
            }
        }

        httpClient.setRoutePlanner(routePlanner);
    }

    @Override
    public void close() throws IOException
    {
        httpClient.close();
    }
}
