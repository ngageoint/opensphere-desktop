package io.opensphere.core.common.connection;

/**
 * The auto-proxy configuration. This class represents a single result from the
 * proxy auto-config (PAC) function <code>FindProxyForURL</code>.
 */
public class AutoProxyConfig
{
    /**
     * The auto-proxy configuration type.
     */
    public enum AutoProxyType
    {
    /**
     * Connections should be made directly, without any proxies.
     */
    DIRECT,

    /**
     * The specified proxy should be used.
     */
    PROXY,

    /**
     * The specified SOCKS server should be used.
     */
    SOCKS
    }

    /**
     * The proxy type.
     */
    private AutoProxyConfig.AutoProxyType proxyType;

    /**
     * The (optional) proxy server's host name.
     */
    private String host;

    /**
     * The (optional) proxy server's port.
     */
    private int port;

    /**
     * Creates a {@link AutoProxyType#DIRECT DIRECT} configuration.
     */
    public AutoProxyConfig()
    {
        proxyType = AutoProxyType.DIRECT;
        port = -1;
    }

    /**
     *
     * Constructor.
     *
     * @param proxyType the {@link AutoProxyType} for the the configuration.
     * @param host the proxy server's host name.
     * @param port the proxy server's port.
     */
    public AutoProxyConfig(AutoProxyConfig.AutoProxyType proxyType, String host, int port)
    {
        this.proxyType = proxyType;
        this.host = host;
        this.port = port;
    }

    /**
     * Returns the proxy type.
     *
     * @return the proxy type.
     */
    public AutoProxyConfig.AutoProxyType getProxyType()
    {
        return proxyType;
    }

    /**
     * Returns the (optional) proxy server's host name.
     *
     * @return the proxy server's host name or <code>null</code> if not set.
     */
    public String getHost()
    {
        return host;
    }

    /**
     * Returns the (optional) proxy server's port.
     *
     * @return the proxy server's port or <code>-1</code> if not set.
     */
    public int getPort()
    {
        return port;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("AutoProxyConfig [proxyType=");
        builder.append(proxyType);
        builder.append(", host=");
        builder.append(host);
        builder.append(", port=");
        builder.append(port);
        builder.append("]");
        return builder.toString();
    }
}
