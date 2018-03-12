package io.opensphere.core;

/** Facility that manages the network configuration. */
public interface NetworkConfigurationManager
{
    /**
     * The "purpose" that should be used when creating a username/password
     * provider for the network proxy.
     */
    String PROXY_PURPOSE = "proxy";

    /**
     * Add a listener for changes to the network configuration. Only a weak
     * reference is held.
     *
     * @param listener The listener.
     */
    void addChangeListener(NetworkConfigurationChangeListener listener);

    /**
     * Get the system proxy config URL if one has been set.
     *
     * @return The URL or {@code null}.
     */
    String getProxyConfigUrl();

    /**
     * Get the hosts to be excluded from proxy usage.
     *
     * @return The string comprising the host patterns.
     */
    String getProxyExclusions();

    /**
     * Get the system proxy host if one has been set.
     *
     * @return The host or {@code ""}.
     */
    String getProxyHost();

    /**
     * Get the system proxy port if one has been set.
     *
     * @return The port or -1.
     */
    int getProxyPort();

    /**
     * Determine if a host should be excluded from the proxy configuration.
     *
     * @param host The host name.
     * @return {@code true} if the host should be excluded.
     */
    boolean isExcludedFromProxy(String host);

    /**
     * Get if system proxies should be used.
     *
     * @return {@code true} if system proxies should be used.
     */
    boolean isUseSystemProxies();

    /**
     * Remove a listener for changes to the network configuration.
     *
     * @param listener The listener.
     */
    void removeChangeListener(NetworkConfigurationChangeListener listener);

    /** Restore the default settings. */
    void restoreDefaults();

    /**
     * Set the proxy configuration.
     *
     * @param host the host, or {@code ""} to disable the proxy
     * @param port the port number
     * @param useSystemProxies {@code true} if system proxies should be used
     * @param configUrl the config URL
     * @param hostPatterns the string comprising the host patterns
     */
    void setProxyConfiguration(String host, int port, boolean useSystemProxies, String configUrl, String hostPatterns);

    /** Listener for changes to the network configuration. */
    @FunctionalInterface
    interface NetworkConfigurationChangeListener
    {
        /** Method called when the network configuration changes. */
        void networkConfigurationChanged();
    }
}
