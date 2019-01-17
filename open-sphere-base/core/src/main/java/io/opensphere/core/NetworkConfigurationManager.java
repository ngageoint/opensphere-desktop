package io.opensphere.core;

import io.opensphere.core.net.config.ConfigurationType;
import io.opensphere.core.net.config.ManualProxyConfiguration;
import io.opensphere.core.net.config.NoProxyConfiguration;
import io.opensphere.core.net.config.SystemProxyConfiguration;
import io.opensphere.core.net.config.UrlProxyConfiguration;
import javafx.beans.property.BooleanProperty;

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
     * Gets the type of proxy currently in use for network operations.
     *
     * @return the type of proxy currently in use for network operations.
     */
    ConfigurationType getSelectedProxyType();

    /**
     * Gets the configuration object used when no proxy is selected.
     *
     * @return the configuration object used when no proxy is selected.
     */
    NoProxyConfiguration getNoProxyConfiguration();

    /**
     * Gets the configuration object used when a system-specified proxy is
     * selected.
     *
     * @return the configuration object used when a system-specified proxy is
     *         selected.
     */
    SystemProxyConfiguration getSystemConfiguration();

    /**
     * Gets the configuration object used when an automatic proxy URL is
     * selected.
     *
     * @return the configuration object used when an automatic proxy URL is
     *         selected.
     */
    UrlProxyConfiguration getUrlConfiguration();

    /**
     * Gets the configuration object used when a manual proxy host, port and
     * exclusion list are selected.
     *
     * @return the configuration object used when a manual proxy host, port and
     *         exclusion list are selected.
     */
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
     * Sets the proxy type to use for network operations.
     *
     * @param configurationType the proxy type to use for network operations.
     */
    void setSelectedProxyType(ConfigurationType configurationType);

    /**
     * Persists all proxy configuration information to preferences.
     */
    void persistConfiguration();

    /**
     * Tests to determine if the network monitor is enabled. If
     * <code>true</code>, all network traffic will be captured through events
     * for monitoring. This will significantly degrade performance, and as such,
     * the network monitor is disabled by default.
     *
     * @return <code>true</code> if network monitoring is enabled,
     *         <code>false</code> otherwise.
     */
    boolean isNetworkMonitorEnabled();

    /**
     * Enables or disabled network monitoring. If enabled, all network traffic
     * will be captured through events for monitoring. This will significantly
     * degrade performance, and as such, the network monitor is disabled by
     * default.
     *
     * @param networkMonitorEnabled <code>true</code> if network monitoring
     *            should be enabled, <code>false</code> otherwise.
     */
    void setNetworkMonitorEnabled(boolean networkMonitorEnabled);

    /**
     * Gets the property through which the enabled state of the network monitor
     * is tracked.
     *
     * @return the property through which the enabled state of the network
     *         monitor is tracked.
     */
    BooleanProperty networkMonitorEnabledProperty();

    /** Listener for changes to the network configuration. */
    @FunctionalInterface
    interface NetworkConfigurationChangeListener
    {
        /** Method called when the network configuration changes. */
        void networkConfigurationChanged();
    }
}
