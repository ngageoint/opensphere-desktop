package com.bitsys.common.http.proxy;

/**
 * The proxy host configuration. This class represents a single result from the
 * proxy auto-config (PAC) function <code>FindProxyForURL</code>.
 */
public class ProxyHostConfig
{
    /**
     * The proxy type: {@link ProxyType#DIRECT DIRECT}, {@link ProxyType#PROXY
     * PROXY} or {@link ProxyType#SOCKS SOCKS}.
     */
    public enum ProxyType
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
    private final ProxyHostConfig.ProxyType proxyType;

    /**
     * The (optional) proxy server's host name.
     */
    private String host;

    /**
     * The (optional) proxy server's port.
     */
    private final int port;

    /**
     * Creates a {@link ProxyType#DIRECT DIRECT} configuration.
     */
    public ProxyHostConfig()
    {
        proxyType = ProxyType.DIRECT;
        port = -1;
    }

    /**
     *
     * Constructor.
     *
     * @param proxyType the {@link ProxyType} for the the configuration.
     * @param host the proxy server's host name.
     * @param port the proxy server's port.
     */
    public ProxyHostConfig(final ProxyHostConfig.ProxyType proxyType, final String host, final int port)
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
    public ProxyHostConfig.ProxyType getProxyType()
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
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (host == null ? 0 : host.hashCode());
        result = prime * result + port;
        result = prime * result + (proxyType == null ? 0 : proxyType.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final ProxyHostConfig other = (ProxyHostConfig)obj;
        if (host == null)
        {
            if (other.host != null)
            {
                return false;
            }
        }
        else if (!host.equals(other.host))
        {
            return false;
        }
        if (port != other.port)
        {
            return false;
        }
        if (proxyType != other.proxyType)
        {
            return false;
        }
        return true;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("ProxyHostConfig [proxyType=");
        builder.append(proxyType);
        builder.append(", host=");
        builder.append(host);
        builder.append(", port=");
        builder.append(port);
        builder.append("]");
        return builder.toString();
    }
}
