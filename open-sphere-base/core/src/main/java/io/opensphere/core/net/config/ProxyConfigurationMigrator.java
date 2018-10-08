package io.opensphere.core.net.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.core.appl.PreConfigurationUpdateModule;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;

/** Migrates the old proxy configs to the new format. */
public class ProxyConfigurationMigrator implements PreConfigurationUpdateModule
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ProxyConfigurationMigrator.class);

    @Override
    public void updateConfigs(PreferencesRegistry prefsRegistry)
    {
        Preferences proxyPreferences = prefsRegistry.getPreferences("io.opensphere.core.net.NetworkConfigurationManagerImpl");
        Set<String> preferenceKeys = New.set("ProxyConfigUrl", "ProxyExclusionPatterns", "ProxyHost", "ProxyPort",
                "SystemProxiesEnabled");
        Map<String, String> preferenceValues = new HashMap<>();

        for (String key : preferenceKeys)
        {
            String preferenceValue = proxyPreferences.getString(key, "");
            if (!preferenceValue.isEmpty())
            {
                preferenceValues.put(key, preferenceValue);
            }
            proxyPreferences.remove(key, this);
        }

        if (!preferenceValues.isEmpty())
        {
            LOGGER.info("Migrating old proxy configurations");
            ProxyConfigurations proxyConfigs = convertPreferences(preferenceValues);
            proxyPreferences.putJAXBObject("configurations", proxyConfigs, false, this);
            LOGGER.info("Finished migrating to new proxy configurations");
            proxyPreferences.printPrefs();
        }
    }

    /**
     * Converts the old preferences to the new proxy configurations.
     *
     * @param preferences the map of old preferences
     * @return the new proxy configurations
     */
    protected ProxyConfigurations convertPreferences(Map<String, String> preferences)
    {
        ProxyConfigurations proxyConfigs = new ProxyConfigurations();

        List<String> exclusions = new ArrayList<>();
        if (preferences.get("ProxyExclusionPatterns") != null)
        {
            // split exclusions on one or more spaces, comma and one or more spaces, or comma
            exclusions = Arrays.asList(preferences.get("ProxyExclusionPatterns").trim().split(" +|, +|,"));
        }

        if (Boolean.parseBoolean(preferences.get("SystemProxiesEnabled")))
        {
            // System proxy with exclusions
            proxyConfigs.setSelectedConfigurationType(ConfigurationType.SYSTEM);
            proxyConfigs.getSystemProxyConfiguration().getExclusionPatterns().addAll(exclusions);
        }
        else if (preferences.get("ProxyConfigUrl") != null)
        {
            // Url proxy with url
            proxyConfigs.setSelectedConfigurationType(ConfigurationType.URL);
            proxyConfigs.getUrlProxyConfiguration().setProxyUrl(preferences.get("ProxyConfigUrl"));
        }
        else if (preferences.get("ProxyPort") != null)
        {
            // Manual proxy with host, port, and exclusions
            proxyConfigs.setSelectedConfigurationType(ConfigurationType.MANUAL);
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
