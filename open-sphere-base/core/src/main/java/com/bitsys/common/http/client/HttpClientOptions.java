package com.bitsys.common.http.client;

import com.bitsys.common.http.auth.CredentialsProvider;
import com.bitsys.common.http.auth.DefaultCredentialsProvider;
import com.google.common.annotations.Beta;

/**
 * This class defines the configuration options for a {@link HttpClient}.
 */
public class HttpClientOptions
{
    /**
     * Defines the built-in HTTP redirect handling modes.
     */
    @Beta
    public enum RedirectMode
    {
        /**
         * This strategy mode the restrictions on automatic redirection of
         * entity enclosing methods such as <code>POST</code> and
         * <code>PUT</code> imposed by the HTTP specification.
         * <code>302 Moved Temporarily</code>, <code>301 Moved
         * Permanently</code> and <code>307 Temporary Redirect</code> status
         * codes will result in an automatic redirect of <code>HEAD</code> and
         * <code>GET</code> methods only. <code>POST</code> and <code>PUT</code>
         * methods will not be automatically redirected as requiring user
         * confirmation.
         */
        DEFAULT,

        /**
         * In addition to the automatic redirects handled by the
         * {@link #DEFAULT} mode, this mode automatically redirects all
         * <code>HEAD</code>, <code>GET</code> and <code>POST</code> requests.
         */
        LAX;
    }

    /** The proxy configuration for this client. */
    private ProxyConfig proxyConfig;

    /** The SSL configuration for this client. */
    private SslConfig sslConfig;

    /** The credentials provider. */
    private CredentialsProvider credentialsProvider;

    /** The connection timeout (in seconds). */
    private int connectTimeout;

    /** The read timeout (in seconds). */
    private int readTimeout;

    /**
     * The size of the internal socket buffer used to buffer data while
     * receiving / transmitting HTTP messages.
     */
    private int socketBufferSize = 8192;

    /** Indicates if the <code>TCP_NODELAY</code> option is enabled. */
    private boolean tcpNoDelay = true;

    /** The default value for the <code>User-Agent</code> HTTP header. */
    private String defaultUserAgent;

    /** The default maximum number of connections per route. */
    private int maxConnectionsPerRoute = 4;

    /** The maximum number of total connections. */
    private int maxConnections = 32;

    /** The redirect handling mode. */
    private RedirectMode redirectMode = RedirectMode.DEFAULT;

    /** Indicates if content is automatically decompressed. */
    private boolean contentDecompressed;

    /** Indicates if circular redirects are allowed. */
    private boolean allowCircularRedirects;

    /**
     * Sets the proxy configuration for this client. If not set or the argument
     * is <code>null</code>, a minimal configuration will be used.
     *
     * @param proxyConfig the proxy configuration for this client.
     * @since 1.0.0
     */
    public void setProxyConfig(final ProxyConfig proxyConfig)
    {
        this.proxyConfig = proxyConfig;
    }

    /**
     * Returns the proxy configuration for this client.
     *
     * @return the proxy configuration for this client.
     * @since 1.0.0
     */
    public ProxyConfig getProxyConfig()
    {
        if (proxyConfig == null)
        {
            proxyConfig = new ProxyConfig();
        }
        return proxyConfig;
    }

    /**
     * Sets the SSL configuration for this client. If not set or the argument is
     * <code>null</code>, a minimal configuration will be used.
     *
     * @param sslConfig the SSL configuration or <code>null</code> for the
     *            default.
     * @since 1.0.0
     */
    public void setSslConfig(final SslConfig sslConfig)
    {
        this.sslConfig = sslConfig;
    }

    /**
     * Returns the SSL configuration for this client.
     *
     * @return the SSL configuration for this client
     * @since 1.0.0
     */
    public SslConfig getSslConfig()
    {
        if (sslConfig == null)
        {
            sslConfig = new SslConfig();
        }
        return sslConfig;
    }

    /**
     * Sets the credentials provider. If not set or the argument is
     * <code>null</code>, a {@link DefaultCredentialsProvider default provider}
     * will be used.
     *
     * @param credentialsProvider the credentials provider.
     * @since 1.0.0
     */
    public void setCredentialsProvider(final CredentialsProvider credentialsProvider)
    {
        this.credentialsProvider = credentialsProvider;
    }

    /**
     * Returns the credentials provider.
     *
     * @return the credentials provider.
     * @since 1.0.0
     */
    public CredentialsProvider getCredentialsProvider()
    {
        if (credentialsProvider == null)
        {
            credentialsProvider = new DefaultCredentialsProvider();
        }
        return credentialsProvider;
    }

    /**
     * Sets the connection timeout (in seconds).
     *
     * @param timeout the connection timeout (in seconds).
     * @since 1.0.0
     */
    public void setConnectTimeout(final int timeout)
    {
        connectTimeout = timeout;
    }

    /**
     * Returns the connection timeout (in seconds).
     *
     * @return the connection timeout (in seconds).
     * @since 1.0.0
     */
    public int getConnectTimeout()
    {
        return connectTimeout;
    }

    /**
     * Sets the read timeout (in seconds).
     *
     * @param timeout the read timeout (in seconds).
     * @since 1.0.0
     */
    public void setReadTimeout(final int timeout)
    {
        readTimeout = timeout;
    }

    /**
     * Returns the read timeout (in seconds).
     *
     * @return the read timeout (in seconds).
     * @since 1.0.0
     */
    public int getReadTimeout()
    {
        return readTimeout;
    }

    /**
     * Sets the size of the internal socket buffer. This buffer is used to
     * buffer data while receiving / transmitting HTTP messages.
     *
     * @param socketBufferSize the new size of the internal socket buffer.
     * @since 1.1.11
     */
    public void setSocketBufferSize(final int socketBufferSize)
    {
        this.socketBufferSize = socketBufferSize;
    }

    /**
     * Returns the size of the internal socket buffer. This buffer is used to
     * buffer data while receiving / transmitting HTTP messages.
     *
     * @return the internal socket buffer size.
     * @since 1.1.11
     */
    public int getSocketBufferSize()
    {
        return socketBufferSize;
    }

    /**
     * Sets the <code>TCP_NODELAY</code> option. This option determines whether
     * Nagle's algorithm is to be used. Nagle's algorithm tries to conserve
     * bandwidth by minimizing the number of segments that are sent. When
     * applications wish to decrease network latency and increase performance,
     * they can disable Nagle's algorithm; that is enable
     * <code>TCP_NODELAY</code>. Data will be sent earlier, at the cost of an
     * increase in bandwidth consumption.
     *
     * @param tcpNoDelay the new value of the <code>TCP_NODELAY</code> option.
     * @since 1.1.11
     */
    public void setTcpNoDelay(final boolean tcpNoDelay)
    {
        this.tcpNoDelay = tcpNoDelay;
    }

    /**
     * Indicates if the <code>TCP_NODELAY</code> option is enabled. The default
     * is to enable this option (no delay).
     *
     * @return the state of the <code>TCP_NODELAY</code> option.
     * @since 1.1.11
     */
    public boolean isTcpNoDelay()
    {
        return tcpNoDelay;
    }

    /**
     * Sets the default value for the <code>User-Agent</code> HTTP header.
     *
     * @param defaultUserAgent the default user agent.
     * @since 1.0.0
     */
    public void setDefaultUserAgent(final String defaultUserAgent)
    {
        this.defaultUserAgent = defaultUserAgent;
    }

    /**
     * Returns the default value for the <code>User-Agent</code> HTTP header.
     *
     * @return the default user agent.
     * @since 1.0.0
     */
    public String getDefaultUserAgent()
    {
        return defaultUserAgent;
    }

    /**
     * Sets the default maximum number of connections per route.
     *
     * @param maximum the maximum number of connections.
     * @since 1.1.1
     */
    public void setMaxConnectionsPerRoute(final int maximum)
    {
        maxConnectionsPerRoute = maximum;
    }

    /**
     * Returns the default maximum number of connections per route. The default
     * value is 4.
     *
     * @return the maximum number of connections.
     * @since 1.1.1
     */
    public int getMaxConnectionsPerRoute()
    {
        return maxConnectionsPerRoute;
    }

    /**
     * Sets the maximum total number of connections.
     *
     * @param maximum the maximum total number of connections.
     * @since 1.1.1
     */
    public void setMaxConnections(final int maximum)
    {
        maxConnections = maximum;
    }

    /**
     * Returns the maximum total number of connections. The default value is 32.
     *
     * @return the maximum total number of connections.
     * @since 1.1.1
     */
    public int getMaxConnections()
    {
        return maxConnections;
    }

    /**
     * Sets the HTTP redirect handling mode.
     * <p>
     * <b>NOTE:</b> This is a <u>beta</u> API method and may be removed in
     * future releases without warning.
     *
     * @param redirectMode the redirect handling mode.
     * @since 1.1.12
     */
    @Beta
    public void setRedirectMode(final RedirectMode redirectMode)
    {
        this.redirectMode = redirectMode;
    }

    /**
     * Returns the HTTP redirect handling mode.
     * <p>
     * <b>NOTE:</b> This is a <u>beta</u> API method and may be removed in
     * future releases without warning.
     *
     * @return the redirect handling mode.
     * @since 1.1.12
     */
    @Beta
    public RedirectMode getRedirectMode()
    {
        return redirectMode;
    }

    /**
     * Sets the flag indicating if content should be automatically decompressed.
     * If set to <code>true</code>, HTTP requests will include the
     * <code>Accept-Encoding</code> header to indicate support for
     * <code>gzip</code> and <code>deflate</code> compression schemes. An HTTP
     * response containing the <code>Content-Encoding</code> header indicating
     * the content is compressed, the client will attempt to automatically
     * decompress the content.
     * <p>
     * Any upstream clients need to be aware that this effectively obscures
     * visibility into the length of a server response body, since the
     * <code>Content-Length</code> header will correspond to the compressed
     * entity length received from the server, but the content length
     * experienced by reading the response body may be different (hopefully
     * higher!).
     *
     * @param flag <code>true</code> to automatically decompress content.
     * @since 1.1.4
     */
    public void setContentDecompressed(final boolean flag)
    {
        contentDecompressed = flag;
    }

    /**
     * Indicates if content is automatically decompressed.
     *
     * @return <code>true</code> if content will be automatically decompressed.
     * @since 1.1.4
     */
    public boolean isContentDecompressed()
    {
        return contentDecompressed;
    }

    /**
     * Indicates if circular redirects are allowed.
     *
     * @return true if circular redirects are allowed.
     */
    public boolean isAllowCircularRedirects()
    {
        return allowCircularRedirects;
    }

    /**
     * Gets parameter indicating if circular redirects are allowed.
     */
    public void setAllowCircularRedirects(boolean allowCircularRedirects)
    {
        this.allowCircularRedirects = allowCircularRedirects;
    }

}
