package io.opensphere.core.net;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import io.opensphere.core.NetworkConfigurationManager;
import io.opensphere.core.net.config.ConfigurationType;
import io.opensphere.core.net.config.ManualProxyConfiguration;
import io.opensphere.core.net.config.NoProxyConfiguration;
import io.opensphere.core.net.config.ProxyConfigurations;
import io.opensphere.core.net.config.SystemProxyConfiguration;
import io.opensphere.core.net.config.UrlProxyConfiguration;
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

    /** The lock object used for synchronization. */
    private static final Object PROXY_CONFIG_LOCK = new Object();

    /** The JAXB Configuration object stored to / read from preferences. */
    private ProxyConfigurations myConfigurations;

    /** Change support. */
    private final ChangeSupport<NetworkConfigurationChangeListener> myChangeSupport = new WeakChangeSupport<>();

    /** The preferences. */
    private final Preferences myPreferences;

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
        myPreferences = prefsRegistry.getPreferences(NetworkConfigurationManagerImpl.class);
        myPreferences.printPrefs();
    }

    @Override
    public void addChangeListener(NetworkConfigurationChangeListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Gets the configuration group object, loading it from preferences if
     * necessary.
     *
     * @return the configuration group object, reflecting the persisted state of
     *         the proxy.
     */
    private ProxyConfigurations getConfigurations()
    {
        synchronized (PROXY_CONFIG_LOCK)
        {
            if (myConfigurations == null)
            {
                myConfigurations = myPreferences.getJAXBObject(ProxyConfigurations.class, "configurations", null);
                // if it's still null, create a new one:
                if (myConfigurations == null)
                {
                    myConfigurations = new ProxyConfigurations();
                    myConfigurations.setSelectedConfigurationType(ConfigurationType.SYSTEM);
                    persistConfiguration();
                }
            }
        }
        return myConfigurations;
    }

    @Override
    public void persistConfiguration()
    {
        synchronized (PROXY_CONFIG_LOCK)
        {
            myPreferences.putJAXBObject("configurations", myConfigurations, false, this);
            notifyChanged();
        }
    }

    @Override
    public ConfigurationType getSelectedProxyType()
    {
        ConfigurationType configurationType = getConfigurations().getSelectedConfigurationType();
        if (configurationType == null)
        {
            configurationType = ConfigurationType.SYSTEM;
            getConfigurations().setSelectedConfigurationType(configurationType);
        }
        return configurationType;
    }

    @Override
    public void setSelectedProxyType(ConfigurationType configurationType)
    {
        getConfigurations().setSelectedConfigurationType(configurationType);
    }

    @Override
    public NoProxyConfiguration getNoProxyConfiguration()
    {
        return getConfigurations().getNoProxyConfiguration();
    }

    @Override
    public SystemProxyConfiguration getSystemConfiguration()
    {
        return getConfigurations().getSystemProxyConfiguration();
    }

    @Override
    public UrlProxyConfiguration getUrlConfiguration()
    {
        return getConfigurations().getUrlProxyConfiguration();
    }

    @Override
    public ManualProxyConfiguration getManualConfiguration()
    {
        return getConfigurations().getManualProxyConfiguration();
    }

    @Override
    public boolean isExcludedFromProxy(String host)
    {
        Set<String> exclusionPatterns;
        if (getSelectedProxyType() == ConfigurationType.SYSTEM)
        {
            exclusionPatterns = getSystemConfiguration().getExclusionPatterns();
        }
        else if (getSelectedProxyType() == ConfigurationType.MANUAL)
        {
            exclusionPatterns = getManualConfiguration().getExclusionPatterns();
        }
        else
        {
            exclusionPatterns = Collections.emptySet();
        }

        for (String exclusion : exclusionPatterns)
        {
            // Generate a regular expression from the exclusion string.
            // Treat everything except '*' literally.
            String[] splitOnStar = exclusion.split("\\*", -1);
            String pattern = "\\Q" + StringUtilities.join("\\E.*\\Q", splitOnStar) + "\\E";
            if (Pattern.matches(pattern, host))
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(StringUtilities.concat("Host [", host, "] matches proxy exclusion pattern [", exclusion,
                            "]; not using proxy."));
                }
                return true;
            }
        }
        return false;
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

    /**
     * Notify when the configuration has changed.
     */
    private void notifyChanged()
    {
        myChangeSupport.notifyListeners(listener -> listener.networkConfigurationChanged());
    }
}
