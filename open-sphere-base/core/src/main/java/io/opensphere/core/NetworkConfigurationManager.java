package io.opensphere.core;

import io.opensphere.core.net.config.ConfigurationType;
import io.opensphere.core.net.config.ManualProxyConfiguration;
import io.opensphere.core.net.config.NoProxyConfiguration;
import io.opensphere.core.net.config.SystemProxyConfiguration;
import io.opensphere.core.net.config.UrlProxyConfiguration;

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

    ConfigurationType getSelectedProxyType();

    NoProxyConfiguration getNoProxyConfiguration();

    SystemProxyConfiguration getSystemConfiguration();

    UrlProxyConfiguration getUrlConfiguration();

    ManualProxyConfiguration getManualConfiguration();

    /**
     * Determine if a host should be excluded from the proxy configuration.
     *
     * @param host The host name.
     * @return {@code true} if the host should be excluded.
     */
    boolean isExcludedFromProxy(String host);

    /**
     * Remove a listener for changes to the network configuration.
     *
     * @param listener The listener.
     */
    void removeChangeListener(NetworkConfigurationChangeListener listener);

    /** Restore the default settings. */
    void restoreDefaults();

    /**
     * @param configurationType
     */
    void setSelectedProxyType(ConfigurationType configurationType);

    void persistConfiguration();

    /** Listener for changes to the network configuration. */
    @FunctionalInterface
    interface NetworkConfigurationChangeListener
    {
        /** Method called when the network configuration changes. */
        void networkConfigurationChanged();
    }
}
