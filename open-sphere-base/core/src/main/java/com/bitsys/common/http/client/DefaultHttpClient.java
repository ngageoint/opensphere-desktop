package com.bitsys.common.http.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.PKIXParameters;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyStoreBuilderParameters;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;

import com.bitsys.common.http.auth.ApacheCredentialsProviderProxy;
import com.bitsys.common.http.auth.CachingCredentialsProvider;
import com.bitsys.common.http.entity.HttpEntity;
import com.bitsys.common.http.entity.HttpEntityConversionUtils;
import com.bitsys.common.http.message.Abortable;
import com.bitsys.common.http.message.BasicHttpResponse;
import com.bitsys.common.http.message.HttpRequest;
import com.bitsys.common.http.message.HttpResponse;
import com.bitsys.common.http.ssl.CachingCertificateVerifier;
import com.bitsys.common.http.ssl.CachingHostNameVerifier;
import com.bitsys.common.http.ssl.ClientCertificateSelector;
import com.bitsys.common.http.ssl.EnhancedSSLSocketFactory;
import com.bitsys.common.http.ssl.EnhancedSSLSocketFactory.SSLSocketCustomizer;
import com.bitsys.common.http.ssl.InteractiveX509KeyManager;
import com.bitsys.common.http.ssl.InteractiveX509TrustManager;
import com.bitsys.common.http.ssl.X509HostNameVerifierCourtRoom;

/**
 * This class is a thin wrapper around Apache's
 * {@link org.apache.http.client.HttpClient HttpClient} to simplify the
 * configuration.
 */
@SuppressWarnings("deprecation")
public class DefaultHttpClient implements CloseableHttpClient
{
    /**
     * This class proxies an {@link Abortable} to an abortable Apache request.
     */
    protected final class ApacheAbortableProxy implements Abortable
    {
        /** The abortable Apache request. */
        private final HttpUriRequest myApacheRequest;

        /** Indicates if abort has been called. */
        private final AtomicBoolean myAborted = new AtomicBoolean();

        /**
         * Constructs a new instance from the given abortable Apache request.
         *
         * @param apacheRequest the Apache request.
         */
        public ApacheAbortableProxy(final HttpUriRequest apacheRequest)
        {
            if (apacheRequest == null)
            {
                throw new IllegalArgumentException("The HTTP URI request is null");
            }
            this.myApacheRequest = apacheRequest;
        }

        @Override
        public boolean isAborted()
        {
            return myAborted.get();
        }

        @Override
        public void abort()
        {
            myApacheRequest.abort();
            myAborted.set(true);
        }
    }

    /** The options for this HTTP client. */
    private final HttpClientOptions myOptions = new HttpClientOptions();

    /** The Apache HTTP client instance. */
    private org.apache.http.client.HttpClient myApacheHttpClient;

    /** The caching credentials provider. */
    private CachingCredentialsProvider myCachingCredentialsProvider;

    /** The caching host name verifier. */
    private CachingHostNameVerifier myCachingHostNameVerifier;

    /** The caching certificate verifier. */
    private CachingCertificateVerifier myCachingCertificateVerifier;

    @Override
    public HttpClientOptions getOptions()
    {
        return myOptions;
    }

    @Override
    public HttpResponse execute(final HttpRequest httpRequest) throws IOException
    {
        // Transform to Apache's request.
        final HttpUriRequest apacheRequest = toApacheRequest(httpRequest);
        setHttpHeaders(httpRequest, apacheRequest);

        // Setup the Abortable.
        synchronized (httpRequest)
        {
            final boolean aborted = httpRequest.isAborted();
            httpRequest.setAbortable(new ApacheAbortableProxy(apacheRequest));
            if (aborted)
            {
                httpRequest.abort();
            }
        }

        // Execute the request.
        final org.apache.http.HttpResponse apacheResponse = getHttpClient().execute(apacheRequest);

        // Update the HTTP request headers based on what Apache set.
        setHttpHeaders(httpRequest, apacheRequest);

        // Transform from Apache's response.
        return fromApacheResponse(apacheResponse);
    }

    /**
     * Converts a {@link HttpRequest} to Apache's {@link HttpUriRequest}.
     *
     * @param httpRequest the request to convert.
     * @return the converted request.
     */
    protected HttpUriRequest toApacheRequest(final HttpRequest httpRequest)
    {
        final HttpRequestBase method = createMethod(httpRequest.getMethod(), httpRequest.getURI());

        // Convert the requested headers.
        for (final Entry<String, String> header : httpRequest.getHeaders().entries())
        {
            method.addHeader(header.getKey(), header.getValue());
        }

        // If the method is a request that can enclose an entity, add the
        // entity.
        if (method instanceof HttpEntityEnclosingRequest)
        {
            final HttpEntityEnclosingRequest enclosingRequest = (HttpEntityEnclosingRequest)method;
            final org.apache.http.HttpEntity entity = HttpEntityConversionUtils.toApacheEntity(httpRequest.getEntity());

            enclosingRequest.setEntity(entity);
        }

        setHttpHeaders(method, httpRequest);
        return method;
    }

    /**
     * Sets the HTTP header values in the Apache {@link HttpUriRequest}. Header
     * values are first pulled from the {@link #getOptions() options} and then
     * from the given {@link HttpRequest}.
     *
     * @param method the Apache method to which the header values will be set.
     * @param httpRequest the HTTP request.
     */
    protected void setHttpHeaders(final HttpUriRequest method, final HttpRequest httpRequest)
    {
        // If a default User-Agent is specified, add it to the header.
        if (StringUtils.isNotBlank(getOptions().getDefaultUserAgent()))
        {
            method.setHeader(HttpHeaders.USER_AGENT, getOptions().getDefaultUserAgent());
        }

        // First clear the current header of any existing, matching headers.
        for (final String key : httpRequest.getHeaders().keySet())
        {
            method.removeHeaders(key);
        }

        // Add all of the requested headers in the order in which they were
        // specified.
        for (final Entry<String, String> entry : httpRequest.getHeaders().entries())
        {
            method.addHeader(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Sets the HTTP header values in the {@link HttpRequest} from the Apache
     * {@link HttpUriRequest}. This method is intended to set the as-used HTTP
     * headers in the original request object for reporting back to the original
     * caller.
     *
     * @param httpRequest the HTTP request to which the header values will be
     *            set.
     * @param method the Apache method from which the values will be pulled.
     */
    protected void setHttpHeaders(final HttpRequest httpRequest, final HttpUriRequest method)
    {
        httpRequest.getHeaders().clear();
        for (final Header header : method.getAllHeaders())
        {
            httpRequest.getHeaders().put(header.getName(), header.getValue());
        }
    }

    /**
     * Creates the appropriate Apache method instance.
     *
     * @param methodName the type of method to create (e.g. <code>GET</code>,
     *            <code>POST</code>, <code>PUT</code>, <code>DELETE</code>,
     *            etc.)
     * @param uri the requested URI.
     * @return the Apache method instance.
     */
    private HttpRequestBase createMethod(final String methodName, final URI uri)
    {
        HttpRequestBase method;
        if (StringUtils.equalsIgnoreCase(HttpRequest.DELETE, methodName))
        {
            method = new HttpDelete(uri);
        }
        else if (StringUtils.equalsIgnoreCase(HttpRequest.GET, methodName))
        {
            method = new HttpGet(uri);
        }
        else if (StringUtils.equalsIgnoreCase(HttpRequest.HEAD, methodName))
        {
            method = new HttpHead(uri);
        }
        else if (StringUtils.equalsIgnoreCase(HttpRequest.PATCH, methodName))
        {
            method = new HttpPatch(uri);
        }
        else if (StringUtils.equalsIgnoreCase(HttpRequest.POST, methodName))
        {
            method = new HttpPost(uri);
        }
        else if (StringUtils.equalsIgnoreCase(HttpRequest.PUT, methodName))
        {
            method = new HttpPut(uri);
        }
        else if (StringUtils.equalsIgnoreCase(HttpRequest.OPTIONS, methodName))
        {
            method = new HttpOptions(uri);
        }
        else if (StringUtils.equalsIgnoreCase(HttpRequest.TRACE, methodName))
        {
            method = new HttpTrace(uri);
        }
        else
        {
            throw new HttpClientException("Unknown HTTP method '" + methodName + "'");
        }
        return method;
    }

    /**
     * Converts an Apache HTTP response to this library's HTTP response.
     *
     * @param apacheResponse the response to convert.
     * @return the converted response.
     * @throws IOException if the response cannot be generated from the supplied
     *             parameter.
     */
    protected HttpResponse fromApacheResponse(final org.apache.http.HttpResponse apacheResponse) throws IOException
    {
        final int statusCode = apacheResponse.getStatusLine().getStatusCode();
        final String statusMessage = apacheResponse.getStatusLine().getReasonPhrase();
        final HttpEntity entity = HttpEntityConversionUtils.fromApacheEntity(apacheResponse.getEntity());
        final HttpResponse response = new BasicHttpResponse(statusCode, statusMessage, entity);

        // Copy the headers to our response.
        for (final Header header : apacheResponse.getAllHeaders())
        {
            response.getHeaders().put(header.getName(), header.getValue());
        }
        return response;
    }

    /**
     * Returns the Apache {@link org.apache.http.client.HttpClient HttpClient}
     * for this client.
     *
     * @return the Apache <code>HttpClient</code> for this client.
     */
    protected org.apache.http.client.HttpClient getHttpClient()
    {
        if (myApacheHttpClient == null)
        {
            createHttpClient();
        }
        return myApacheHttpClient;
    }

    /**
     * Creates and initializes an Apache
     * {@link org.apache.http.client.HttpClient HttpClient}.
     */
    protected synchronized void createHttpClient()
    {
        if (myApacheHttpClient == null)
        {
            final HttpParams params = new SyncBasicHttpParams();
            configureParameters(params);

            final SchemeRegistry schemeRegistry = new SchemeRegistry();
            final Scheme http = new Scheme("http", 80, PlainSocketFactory.getSocketFactory());
            schemeRegistry.register(http);
            configureHttps(schemeRegistry);

            final PoolingClientConnectionManager conman = new PoolingClientConnectionManager(schemeRegistry);
            conman.setDefaultMaxPerRoute(getOptions().getMaxConnectionsPerRoute());
            conman.setMaxTotal(getOptions().getMaxConnections());
            final org.apache.http.impl.client.DefaultHttpClient defaultHttpClient = new org.apache.http.impl.client.DefaultHttpClient(
                    conman, params);

            configureCredentialsProvider(defaultHttpClient);
            configureRedirectStrategy(defaultHttpClient);

            // Add dynamic proxying support.
            myApacheHttpClient = new ProxyingHttpClient(defaultHttpClient, getOptions());

            // Add decompression support.
            if (getOptions().isContentDecompressed())
            {
                myApacheHttpClient = new DecompressingHttpClient(myApacheHttpClient);
            }
        }
    }

    // private void newCreateHttpClient()
    // {
    // // Configure the timeouts.
    // final RequestConfig requestConfig =
    // RequestConfig.custom().setConnectTimeout(options.getConnectTimeout() *
    // 1000)
    // .setSocketTimeout(options.getReadTimeout() * 1000).build();
    //
    // // Configure the connection manager.
    // // TODO: The schemeRegistry needs to be reproduced.
    // final PoolingHttpClientConnectionManager connManager = new
    // PoolingHttpClientConnectionManager();
    // connManager.setDefaultMaxPerRoute(getOptions().getMaxConnectionsPerRoute());
    // connManager.setMaxTotal(getOptions().getMaxConnections());
    //
    // // Configure the proxying route planner.
    // final HttpRoutePlanner routePlanner = new
    // DynamicProxyRoutePlanner(getOptions().getProxyConfig().getProxyResolver());
    // // TODO: The proxy credentials need to be provided.
    //
    // final HttpClientBuilder builder = HttpClientBuilder.create();
    // builder.setConnectionManager(connManager);
    // builder.setDefaultRequestConfig(requestConfig);
    // builder.setRoutePlanner(routePlanner);
    // final org.apache.http.impl.client.CloseableHttpClient closeableHttpClient
    // = builder.build();
    // throw new UnsupportedOperationException("Using the new HTTP Client API is
    // not yet supported");
    // }

    /**
     * Configures the credentials provider.
     *
     * @param defaultHttpClient the Apache HTTP client in which the credentials
     *            will be configured.
     */
    private void configureCredentialsProvider(final org.apache.http.impl.client.DefaultHttpClient defaultHttpClient)
    {
        // Create a couple of credentials providers. The first one provides the
        // credentials cache that can be cleared. The second one is the
        // client-facing provider.
        final ApacheCredentialsProviderProxy secondProvider = new ApacheCredentialsProviderProxy(
                myOptions.getCredentialsProvider());
        myCachingCredentialsProvider = new CachingCredentialsProvider(secondProvider);
        defaultHttpClient.setCredentialsProvider(myCachingCredentialsProvider);
    }

    /**
     * Configures the HTTP redirect handling strategy.
     *
     * @param defaultHttpClient the Apache HTTP client to be configured.
     */
    private void configureRedirectStrategy(final org.apache.http.impl.client.DefaultHttpClient defaultHttpClient)
    {
        RedirectStrategy strategy;
        switch (myOptions.getRedirectMode())
        {
            case LAX:
                strategy = new LaxRedirectStrategy();
                break;
            default:
                strategy = new DefaultRedirectStrategy();
        }
        defaultHttpClient.setRedirectStrategy(strategy);

        defaultHttpClient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS,
                Boolean.valueOf(getOptions().isAllowCircularRedirects()));
    }

    /**
     * Configures the parameters.
     *
     * @param params the location to store the configuration.
     */
    private void configureParameters(final HttpParams params)
    {
        params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, myOptions.getConnectTimeout() * 1000);
        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, myOptions.getReadTimeout() * 1000);
        params.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, myOptions.isTcpNoDelay());
        params.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, myOptions.getSocketBufferSize());
    }

    /**
     * Configures HTTPS.
     *
     * @param schemeRegistry the registry to store the configuration.
     * @throws HttpClientException if unable to configure HTTPS.
     */
    private void configureHttps(final SchemeRegistry schemeRegistry)
    {
        // Configure SSL.
        final SslConfig sslConfig = myOptions.getSslConfig();
        final String protocol = "TLS";
        try
        {
            final SSLContext sslContext = SSLContext.getInstance(protocol);
            final KeyManager[] km = createKeyManagers(sslConfig);
            final TrustManager[] tm = createTrustManagers(sslConfig);
            sslContext.init(km, tm, null);
            // TODO: Handle the host name verification better: ALL vs.
            // Browser vs. Strict. Rename the internal verifier to callback?
            myCachingHostNameVerifier = new CachingHostNameVerifier(sslConfig.getHostNameVerifier());
            final X509HostnameVerifier hostNameVerifier = new X509HostNameVerifierCourtRoom(
                    SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER, myCachingHostNameVerifier);
            final SSLSocketCustomizer customizer = socket ->
            {
                socket.setEnabledProtocols(sslConfig.getEnabledProtocols());
                socket.setEnabledCipherSuites(sslConfig.getEnabledCipherSuites());
            };
            final SSLSocketFactory socketFactory = new EnhancedSSLSocketFactory(sslContext, hostNameVerifier, customizer);
            final Scheme https = new Scheme("https", 443, socketFactory);
            schemeRegistry.register(https);
        }
        catch (final InvalidAlgorithmParameterException e)
        {
            throw new HttpClientException("Failed to initialize the Key Managers", e);
        }
        catch (final KeyManagementException e)
        {
            throw new HttpClientException("Failed to initialize the SSL context", e);
        }
        catch (final NoSuchAlgorithmException e)
        {
            throw new HttpClientException("The protocol '" + protocol + "' is not supported", e);
        }
        catch (final KeyStoreException e)
        {
            throw new HttpClientException("Failed to process a key store", e);
        }
    }

    /**
     * Creates the key managers.
     *
     * @param sslConfig provides the client certificate configuration.
     * @return the array of key managers.
     * @throws NoSuchAlgorithmException if unable to create the
     *             <code>KeyManagerFactory</code> instance.
     * @throws InvalidAlgorithmParameterException if unable to initialize the
     *             <code>KeyManagerFactory</code>.
     */
    protected KeyManager[] createKeyManagers(final SslConfig sslConfig)
        throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
    {
        KeyManager[] keyManagers = null;
        if (!sslConfig.getCustomKeyManagers().isEmpty())
        {
            final List<KeyManager> customs = sslConfig.getCustomKeyManagers();
            keyManagers = new KeyManager[customs.size()];

            customs.toArray(keyManagers);
        }
        else if (!sslConfig.getClientCertificates().isEmpty())
        {
            final KeyManagerFactory factory = KeyManagerFactory.getInstance("NewSunX509");
            final ManagerFactoryParameters factoryParameters = new KeyStoreBuilderParameters(sslConfig.getClientCertificates());
            factory.init(factoryParameters);
            final KeyManager[] defaultKeyManagers = factory.getKeyManagers();
            keyManagers = new KeyManager[defaultKeyManagers.length];
            final ClientCertificateSelector certificateSelector = sslConfig.getCertificateSelector();
            for (int ii = 0; ii < keyManagers.length; ii++)
            {
                keyManagers[ii] = new InteractiveX509KeyManager((X509ExtendedKeyManager)defaultKeyManagers[ii],
                        certificateSelector);
            }
        }
        return keyManagers;
    }

    /**
     * Creates the trust managers.
     *
     * @param sslConfig provides the trusted certificates provider and
     *            certificate verifier.
     * @return the array of trust managers.
     * @throws NoSuchAlgorithmException if unable to create the
     *             <code>TrustManagerFactory</code> instance.
     * @throws InvalidAlgorithmParameterException if unable to initialize the
     *             <code>TrustManagerFactory</code>.
     * @throws KeyStoreException should never happen.
     */
    protected TrustManager[] createTrustManagers(final SslConfig sslConfig)
        throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyStoreException
    {
        TrustManager[] trustManagers = null;

        if (!sslConfig.getCustomTrustManagers().isEmpty())
        {
            final List<TrustManager> customs = sslConfig.getCustomTrustManagers();
            trustManagers = new TrustManager[customs.size()];

            customs.toArray(trustManagers);
        }
        else
        {
            final TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

            if (sslConfig.getPkixBuilderParameters() != null)
            {
                final PKIXParameters parameters = sslConfig.getPkixBuilderParameters();
                final ManagerFactoryParameters factoryParameters = new CertPathTrustManagerParameters(parameters);
                factory.init(factoryParameters);
            }
            else
            {
                factory.init((KeyStore)null);
            }
            final TrustManager[] defaultTrustManagers = factory.getTrustManagers();
            trustManagers = new TrustManager[defaultTrustManagers.length];
            myCachingCertificateVerifier = new CachingCertificateVerifier(sslConfig.getCertificateVerifier());
            for (int ii = 0; ii < defaultTrustManagers.length; ii++)
            {
                trustManagers[ii] = new InteractiveX509TrustManager((X509TrustManager)defaultTrustManagers[ii],
                        myCachingCertificateVerifier);
            }
        }

        return trustManagers;
    }

    @Override
    public void clearCache(final ClearDataOptions options)
    {
        // TODO: Use more specific clearing options.
        myCachingCredentialsProvider.clear();

        if (myCachingHostNameVerifier != null)
        {
            myCachingHostNameVerifier.clearCache(options.getClearSince());
        }

        if (myCachingCertificateVerifier != null)
        {
            myCachingCertificateVerifier.clearCache(options.getClearSince());
        }

        // TODO: Implement cache clearing.
        throw new UnsupportedOperationException("The clearCache method has not yet been implemented");
    }

    /**
     * Closes the underlying Apache {@link HttpClient}.
     *
     * @see Closeable#close()
     * @since 1.1.1
     */
    @Override
    public void close() throws IOException
    {
        if (myApacheHttpClient instanceof DecompressingHttpClient)
        {
            final DecompressingHttpClient decompressingHttpClient = (DecompressingHttpClient)myApacheHttpClient;
            ((Closeable)decompressingHttpClient.getHttpClient()).close();
        }
    }
}
