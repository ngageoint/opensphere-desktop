package io.opensphere.core;

import io.opensphere.core.util.ChangeSupport;

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

    void addChangeListener(ProxySettingsChangeListener listener);

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
     * Set if system proxies should be used.
     *
     * @param use {@code true} if system proxies should be used.
     */
    void setUseSystemProxies(boolean use);

    /**
     * Remove a listener for changes to the network configuration.
     *
     * @param listener The listener.
     */
    void removeChangeListener(NetworkConfigurationChangeListener listener);

    void removeChangeListener(ProxySettingsChangeListener listener);

    /** Restore the default settings. */
    void restoreDefaults();

    /**
     * Set the system proxy host.
     *
     * @param host The host, or {@code ""} to disable the proxy.
     * @param port The port number.
     */
    void setProxy(String host, int port);

    /**
     * Set the system proxy config URL.
     *
     * @param url The URL.
     */
    void setProxyConfigUrl(String url);

    /**
     * Set the hosts to be excluded from proxy usage.
     *
     * @param hostPatterns A string comprising the host patterns.
     */
    void setProxyExclusions(String hostPatterns);
    
    void notifyProxySettingsChanged();

    /** Listener for changes to the network configuration. */
    @FunctionalInterface
    interface NetworkConfigurationChangeListener
    {
        /** Method called when the network configuration changes. */
        void networkConfigurationChanged();
    }

    @FunctionalInterface
    interface ProxySettingsChangeListener
    {
        void proxySettingsChanged();
    }
}
