package io.opensphere.core.net.config;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;

/** Test {@link ProxyConfigurationMigrator}. */
public class ProxyConfigurationMigratorTest
{
    /**
     * Test {@link ProxyConfigurationMigrator#convertPreferences(Map)} for the
     * no proxy case.
     */
    @Test
    public void testConvertPreferencesNoProxy()
    {
        ProxyConfigurations expectedConfigs = new ProxyConfigurations();
        expectedConfigs.setSelectedConfigurationType(ConfigurationType.NONE);

        Map<String, String> preferenceValues = new HashMap<String, String>();
        preferenceValues.put("SystemProxiesEnabled", "false");
        ProxyConfigurations actualConfigs = new ProxyConfigurationMigrator().convertPreferences(preferenceValues);

        assertTrue(configsMatch(expectedConfigs, actualConfigs));
    }

    /**
     * Test {@link ProxyConfigurationMigrator#convertPreferences(Map)} for the
     * system proxy case.
     */
    @Test
    public void testConvertPreferencesSystemProxy()
    {
        ProxyConfigurations expectedConfigs = new ProxyConfigurations();
        expectedConfigs.setSelectedConfigurationType(ConfigurationType.SYSTEM);
        expectedConfigs.getSystemProxyConfiguration().getExclusionPatterns()
                .addAll(new HashSet<String>(Arrays.asList("example1", "example2", "example3", "example4")));

        Map<String, String> preferenceValues = new HashMap<String, String>();
        preferenceValues.put("SystemProxiesEnabled", "true");
        preferenceValues.put("ProxyExclusionPatterns", " example1, example2,example3   example4 ");
        ProxyConfigurations actualConfigs = new ProxyConfigurationMigrator().convertPreferences(preferenceValues);

        assertTrue(configsMatch(expectedConfigs, actualConfigs));
    }

    /**
     * Test {@link ProxyConfigurationMigrator#convertPreferences(Map)} for the
     * url proxy case.
     */
    @Test
    public void testConvertPreferencesUrlProxy()
    {
        ProxyConfigurations expectedConfigs = new ProxyConfigurations();
        expectedConfigs.setSelectedConfigurationType(ConfigurationType.URL);
        expectedConfigs.getUrlProxyConfiguration().setProxyUrl("example-url");

        Map<String, String> preferenceValues = new HashMap<String, String>();
        preferenceValues.put("SystemProxiesEnabled", "false");
        preferenceValues.put("ProxyConfigUrl", "example-url");
        ProxyConfigurations actualConfigs = new ProxyConfigurationMigrator().convertPreferences(preferenceValues);

        assertTrue(configsMatch(expectedConfigs, actualConfigs));
    }

    /**
     * Test {@link ProxyConfigurationMigrator#convertPreferences(Map)} for the
     * manual proxy case.
     */
    @Test
    public void testConvertPreferencesManualProxy()
    {
        ProxyConfigurations expectedConfigs = new ProxyConfigurations();
        expectedConfigs.setSelectedConfigurationType(ConfigurationType.MANUAL);
        expectedConfigs.getManualProxyConfiguration().setHost("example-host");
        expectedConfigs.getManualProxyConfiguration().setPort(80);
        expectedConfigs.getManualProxyConfiguration().getExclusionPatterns()
                .addAll(new HashSet<String>(Arrays.asList("example1", "example2", "example3")));

        Map<String, String> preferenceValues = new HashMap<String, String>();
        preferenceValues.put("SystemProxiesEnabled", "false");
        preferenceValues.put("ProxyHost", "example-host");
        preferenceValues.put("ProxyPort", "80");
        preferenceValues.put("ProxyExclusionPatterns", "example1 example2 example3");
        ProxyConfigurations actualConfigs = new ProxyConfigurationMigrator().convertPreferences(preferenceValues);

        assertTrue(configsMatch(expectedConfigs, actualConfigs));
    }

    /**
     * Test if the actual converted proxy configurations match the expected
     * proxy configurations.
     *
     * @param expected the expected proxy configurations
     * @param actual the actual proxy configurations
     * @return if the expected and new configurations match
     */
    private boolean configsMatch(ProxyConfigurations expected, ProxyConfigurations actual)
    {
        return expected.getSelectedConfigurationType() == actual.getSelectedConfigurationType()
                && systemProxiesMatch(expected.getSystemProxyConfiguration(), actual.getSystemProxyConfiguration())
                && urlProxiesMatch(expected.getUrlProxyConfiguration(), actual.getUrlProxyConfiguration())
                && manualProxiesMatch(expected.getManualProxyConfiguration(), actual.getManualProxyConfiguration());
    }

    /**
     * Test if the system proxy configurations match.
     *
     * @param expected the expected system proxy configuration
     * @param actual the actual system proxy configuration
     * @return if the system proxies match
     */
    private boolean systemProxiesMatch(SystemProxyConfiguration expected, SystemProxyConfiguration actual)
    {
        return expected.getExclusionPatterns().equals(actual.getExclusionPatterns());
    }

    /**
     * Test if the url proxy configurations match.
     *
     * @param expected the expected url proxy configuration
     * @param actual the actual url proxy configuration
     * @return if the url proxies match
     */
    private boolean urlProxiesMatch(UrlProxyConfiguration expected, UrlProxyConfiguration actual)
    {
        if (null == expected.getProxyUrl())
        {
            return null == actual.getProxyUrl();
        }
        else
        {
            return expected.getProxyUrl().equals(actual.getProxyUrl());
        }
    }

    /**
     * Test if the system proxy configurations match.
     *
     * @param expected the expected system proxy configuration
     * @param actual the actual system proxy configuration
     * @return if the system proxies match
     */
    private boolean manualProxiesMatch(ManualProxyConfiguration expected, ManualProxyConfiguration actual)
    {
        if (null == expected.getHost())
        {
            return null == actual.getHost() && expected.getExclusionPatterns().equals(actual.getExclusionPatterns())
                    && expected.getPort() == actual.getPort();
        }
        else
        {
            return expected.getHost().equals(actual.getHost()) && expected.getPort() == actual.getPort()
                    && expected.getExclusionPatterns().equals(actual.getExclusionPatterns());
        }
    }
}
