package io.opensphere.core.net;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.NetworkConfigurationManager;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.lang.StringUtilities;

/** Implementation that manages the network configuration. */
public class NetworkConfigurationManagerImpl implements NetworkConfigurationManager
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(NetworkConfigurationManagerImpl.class);

    /** The key for the proxy config url in the preferences. */
    private static final String PROXY_CONFIG_URL_PREF_KEY = "ProxyConfigUrl";

    /** The key for the proxy config url in the preferences. */
    private static final String PROXY_EXCLUSION_PATTERNS_PREF_KEY = "ProxyExclusionPatterns";

    /** The key for the proxy host in the preferences. */
    private static final String PROXY_HOST_PREF_KEY = "ProxyHost";

    /** The key for the proxy port in the preferences. */
    private static final String PROXY_PORT_PREF_KEY = "ProxyPort";

    /** The key for the preference for system proxies. */
    private static final String SYSTEM_PROXIES_ENABLED_KEY = "SystemProxiesEnabled";

    /** Change support. */
    private final ChangeSupport<NetworkConfigurationChangeListener> myChangeSupport =
            new WeakChangeSupport<>();

    /** The preferences. */
    private final Preferences myPrefs;

    /** The system preferences registry. */
    private final PreferencesRegistry myPrefsRegistry;

    /**
     * Construct the network configuration manager.
     *
     * @param prefsRegistry The system preferences registry.
     */
    public NetworkConfigurationManagerImpl(PreferencesRegistry prefsRegistry)
    {
        myPrefsRegistry = prefsRegistry;
        myPrefs = prefsRegistry.getPreferences(NetworkConfigurationManagerImpl.class);
        myPrefs.printPrefs();
    }

    @Override
    public void addChangeListener(NetworkConfigurationChangeListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public String getProxyConfigUrl()
    {
        return myPrefs.getString(PROXY_CONFIG_URL_PREF_KEY, "");
    }

    @Override
    public String getProxyExclusions()
    {
        return myPrefs.getString(PROXY_EXCLUSION_PATTERNS_PREF_KEY, "");
    }

    @Override
    public String getProxyHost()
    {
        return myPrefs.getString(PROXY_HOST_PREF_KEY, "");
    }

    @Override
    public int getProxyPort()
    {
        return myPrefs.getInt(PROXY_PORT_PREF_KEY, -1);
    }

    @Override
    public boolean isExcludedFromProxy(String host)
    {
        String proxyExclusions = getProxyExclusions();
        if (StringUtils.isNotEmpty(proxyExclusions))
        {
            for (String excl : proxyExclusions.split("[\\s;,]+"))
            {
                // Generate a regular expression from the exclusion string.
                // Treat everything except '*' literally.
                String[] splitOnStar = excl.split("\\*", -1);
                String pattern = "\\Q" + StringUtilities.join("\\E.*\\Q", splitOnStar) + "\\E";
                if (Pattern.matches(pattern, host))
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug(StringUtilities.concat("Host [", host, "] matches proxy exclusion pattern [", excl,
                                "]; not using proxy."));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isUseSystemProxies()
    {
        return myPrefs.getBoolean(SYSTEM_PROXIES_ENABLED_KEY, false);
    }

    @Override
    public void removeChangeListener(NetworkConfigurationChangeListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    @Override
    public void restoreDefaults()
    {
        myPrefsRegistry.resetPreferences(NetworkConfigurationManagerImpl.class, this);
    }

    @Override
    public void setProxyConfiguration(String host, int port, boolean useSystemProxies, String configUrl, String hostPatterns)
    {
        setProxyHost(host, port);
        myPrefs.putBoolean(SYSTEM_PROXIES_ENABLED_KEY, useSystemProxies, this);
        myPrefs.putString(PROXY_CONFIG_URL_PREF_KEY, configUrl, this);
        myPrefs.putString(PROXY_EXCLUSION_PATTERNS_PREF_KEY, hostPatterns, this);

        notifyChanged();
    }

    /**
     * Set the system proxy host.
     *
     * @param host the host, or {@code ""} to disable the proxy
     * @param port the port number
     */
    private void setProxyHost(String host, int port)
    {
        if (StringUtils.isEmpty(host))
        {
            myPrefs.removeInt(PROXY_PORT_PREF_KEY, this);
        }
        else
        {
            myPrefs.putInt(PROXY_PORT_PREF_KEY, port, this);
        }
        myPrefs.putString(PROXY_HOST_PREF_KEY, host, this);
    }

    /**
     * Notify when the configuration has changed.
     */
    private void notifyChanged()
    {
        myChangeSupport.notifyListeners(listener -> listener.networkConfigurationChanged());
    }
}
