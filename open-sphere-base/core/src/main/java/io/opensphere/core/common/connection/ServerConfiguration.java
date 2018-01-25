package io.opensphere.core.common.connection;

/**
 * This class contains the server configuration parameters.
 */
public class ServerConfiguration implements Cloneable
{
    /**
     * The server's protocol (e.g. http or https).
     */
    private String protocol;

    /**
     * The server's host name or IP address.
     */
    private String host;

    /**
     * The server's port.
     */
    private int port;

    /**
     * The proxy configuration.
     */
    private ProxyConfiguration proxyConfiguration;

    /**
     * The basic authentication configuration.
     */
    private BasicAuthenticationConfiguration basicAuthenticationConfiguration;

    /**
     * The certificate configuration.
     */
    private CertificateConfiguration certificateConfiguration;

    /**
     * The form authentication user name.
     */
    private String username;

    /**
     * The form authentication password.
     */
    private String password;

    /**
     * The socket connect timeout.
     */
    private int connectTimeout = -1;

    /**
     * The socket read timeout.
     */
    private int readTimeout = -1;

    /**
     * Returns the server's protocol (e.g. http or https).
     *
     * @return the server's protocol.
     */
    public String getProtocol()
    {
        return protocol;
    }

    /**
     * Sets the server's protocol.
     *
     * @param protocol the server's protocol.
     */
    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

    /**
     * Returns the server's host name or IP address.
     *
     * @return the server's host name or IP address.
     */
    public String getHost()
    {
        return host;
    }

    /**
     * Sets the server's host name or IP address.
     *
     * @param host the server's host name or IP address.
     */
    public void setHost(String host)
    {
        this.host = host;
    }

    /**
     * Returns the server's port.
     *
     * @return the server's port.
     */
    public int getPort()
    {
        return port;
    }

    /**
     * Sets the server's port.
     *
     * @param port the server's port.
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    /**
     * Returns the server's <code>ProxyConfiguration</code>.
     *
     * @return the server's <code>ProxyConfiguration</code>.
     */
    public ProxyConfiguration getProxyConfiguration()
    {
        return proxyConfiguration;
    }

    /**
     * Sets the server's <code>ProxyConfiguration</code>.
     *
     * @param proxyConfig the server's <code>ProxyConfiguration</code>.
     */
    public void setProxyConfiguration(ProxyConfiguration proxyConfig)
    {
        proxyConfiguration = proxyConfig;
    }

    /**
     * Returns the server's <code>BasicAuthenticationConfiguration</code>.
     *
     * @return the server's <code>BasicAuthenticationConfiguration</code>.
     */
    public BasicAuthenticationConfiguration getBasicAuthenticationConfiguration()
    {
        return basicAuthenticationConfiguration;
    }

    /**
     * Sets the server's <code>BasicAuthenticationConfiguration</code>.
     *
     * @param basicAuthenticationConfig the server's
     *            <code>BasicAuthenticationConfiguration</code>.
     */
    public void setBasicAuthenticationConfiguration(BasicAuthenticationConfiguration basicAuthenticationConfig)
    {
        basicAuthenticationConfiguration = basicAuthenticationConfig;
    }

    /**
     * Returns the server's <code>CertificateConfiguration</code>.
     *
     * @return the server's <code>CertificateConfiguration</code>.
     */
    public CertificateConfiguration getCertificateConfiguration()
    {
        return certificateConfiguration;
    }

    /**
     * Sets the server's <code>CertificateConfiguration</code>.
     *
     * @param certConfig the server's <code>CertificateConfiguration</code>.
     */
    public void setCertificateConfiguration(CertificateConfiguration certConfig)
    {
        certificateConfiguration = certConfig;
    }

    /**
     * Returns the form authentication user name.
     *
     * @return the form authentication user name.
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * Sets the form authentication user name.
     *
     * @param username the form authentication user name.
     */
    public void setUsername(String username)
    {
        this.username = username;
    }

    /**
     * Returns the form authentication password.
     *
     * @return the form authentication password.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Sets the form authentication password.
     *
     * @param password the form authentication password.
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * Returns the socket connect timeout.
     *
     * @return the socket connect timeout. A value of <code>0</code> means that
     *         the timeout is ignored.
     */
    public int getConnectTimeout()
    {
        return connectTimeout;
    }

    /**
     * Sets the socket connect timeout. A value of <code>0</code> means that the
     * timeout is ignored.
     *
     * @param connectTimeout the socket connect timeout.
     */
    public void setConnectTimeout(int connectTimeout)
    {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Returns the socket read timeout.
     *
     * @return the socket read timeout (e.g. SO_TIMEOUT). A value of
     *         <code>0</code> is treated as infinity.
     */
    public int getReadTimeout()
    {
        return readTimeout;
    }

    /**
     * Sets the socket read timeout (e.g. SO_TIMEOUT). A value of <code>0</code>
     * is treated as infinity.
     *
     * @param readTimeout the socket read timeout.
     */
    public void setReadTimeout(int readTimeout)
    {
        this.readTimeout = readTimeout;
    }

    @Override
    public ServerConfiguration clone() throws CloneNotSupportedException
    {
        ServerConfiguration clone = (ServerConfiguration)super.clone();
        clone.proxyConfiguration = proxyConfiguration.clone();
        clone.basicAuthenticationConfiguration = basicAuthenticationConfiguration.clone();
        clone.certificateConfiguration = certificateConfiguration.clone();
        return clone;
    }
}
