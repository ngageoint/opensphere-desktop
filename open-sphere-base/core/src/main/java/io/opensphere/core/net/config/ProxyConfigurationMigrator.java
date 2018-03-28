package io.opensphere.core.net.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.core.appl.PreConfigurationUpdateModule;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;

/** Migrates the old proxy configs to the new format. */
public class ProxyConfigurationMigrator implements PreConfigurationUpdateModule
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ProxyConfigurationMigrator.class);

    /** The proxy preference keys. */
    private static final Set<String> PREFERENCE_KEYS = new HashSet<String>(
            Arrays.asList("ProxyConfigUrl", "ProxyExclusionPatterns", "ProxyHost", "ProxyPort", "SystemProxiesEnabled"));

    @Override
    public void updateConfigs(PreferencesRegistry prefsRegistry)
    {
        Preferences proxyPreferences = prefsRegistry.getPreferences("io.opensphere.core.net.NetworkConfigurationManagerImpl");
        Map<String, String> preferenceValues = new HashMap<String, String>();
        boolean isChanged = false;
        for (String key : PREFERENCE_KEYS)
        {
            String preferenceValue = proxyPreferences.getString(key, "");
            if (!preferenceValue.isEmpty())
            {
                System.out.println("NON EMPTY STRING: " + preferenceValue);
                isChanged = true;
                preferenceValues.put(key, preferenceValue);
            }
            proxyPreferences.remove(key, this);
        }

        if (isChanged)
        {
            LOGGER.info("Migrating old proxy configurations");
            ProxyConfigurations proxyConfigs = convertPreferences(preferenceValues);
            proxyPreferences.putJAXBObject("configurations", proxyConfigs, false, this);
            proxyPreferences.printPrefs();
            LOGGER.info("Finished migrating to new proxy configurations");
        }
    }

    /**
     * Converts the old preferences to the new proxy configurations.
     *
     * @param preferences the map of old preferences
     * @return the new proxy configurations
     */
    private ProxyConfigurations convertPreferences(Map<String, String> preferences)
    {
        ProxyConfigurations proxyConfigs = new ProxyConfigurations();

        List<String> exclusions = new ArrayList<String>();
        if (preferences.get("ProxyExclusionPatterns") != null)
        {
            // split exclusions on one or more spaces, comma and one or more spaces, or comma
            exclusions = Arrays.asList(preferences.get("ProxyExclusionPatterns").trim().split(" +|, +|,"));
        }

        if (Boolean.parseBoolean(preferences.get("SystemProxiesEnabled")))
        {
            // System proxy & exclusions
            proxyConfigs.setSelectedConfigurationType(ConfigurationType.SYSTEM);
            proxyConfigs.getSystemProxyConfiguration().getExclusionPatterns().addAll(exclusions);
        }
        else if (preferences.get("ProxyConfigUrl") != null)
        {
            // Url proxy and url
            proxyConfigs.setSelectedConfigurationType(ConfigurationType.URL);
            proxyConfigs.getUrlProxyConfiguration().setProxyUrl(preferences.get("ProxyConfigUrl"));
        }
        else if (preferences.get("ProxyPort") != null)
        {
            // Manual proxy, host, port, and exclusions
            proxyConfigs.setSelectedConfigurationType(ConfigurationType.MANUAL);
            // Must be set to empty host if the field was blank, not null
            proxyConfigs.getManualProxyConfiguration()
                    .setHost(preferences.get("ProxyHost") != null ? preferences.get("ProxyHost") : "");
            proxyConfigs.getManualProxyConfiguration().setPort(Integer.parseInt(preferences.get("ProxyPort")));
            proxyConfigs.getManualProxyConfiguration().getExclusionPatterns().addAll(exclusions);
        }
        else
        {
            // No proxy
            proxyConfigs.setSelectedConfigurationType(ConfigurationType.NONE);
        }
        return proxyConfigs;
    }
}
