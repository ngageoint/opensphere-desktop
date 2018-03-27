package io.opensphere.core.net;

import java.util.Arrays;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.NetworkConfigurationManager;
import io.opensphere.core.net.config.ConfigurationType;
import io.opensphere.core.net.config.ProxyConfigurations;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;

/** Test {@link NetworkConfigurationManagerImpl}. */
public class NetworkConfigurationManagerImplTest
{
    /**
     * The host.
     */
    private static final String ourHost = "testhost";

    /**
     * Test {@link NetworkConfigurationManagerImpl#isExcludedFromProxy(String)}.
     */
    @Test
    public void testIsExcludedFromProxy()
    {
        Assert.assertFalse(isExcluded("", ourHost));
        Assert.assertFalse(isExcluded("*x", ourHost));
        Assert.assertFalse(isExcluded("x*", ourHost));
        Assert.assertFalse(isExcluded("x", ourHost));

        Assert.assertTrue(isExcluded("test*", ourHost));
        Assert.assertTrue(isExcluded("*host", ourHost));
        Assert.assertTrue(isExcluded("test*host", ourHost));
        Assert.assertTrue(isExcluded("*", ourHost));
    }

    /**
     * Test an exclusion pattern that should match the host name.
     *
     * @param exclusions The exclusion pattern.
     * @param host The host name.
     * @return If the host is excluded.
     */
    private boolean isExcluded(String exclusions, String host)
    {
        EasyMockSupport support = new EasyMockSupport();

        Preferences prefs = support.createMock(Preferences.class);
        ProxyConfigurations configurations = new ProxyConfigurations();

        EasyMock.expect(prefs.getJAXBObject(ProxyConfigurations.class, "configurations", null)).andReturn(configurations);

        PreferencesRegistry prefsRegistry = support.createMock(PreferencesRegistry.class);
        EasyMock.expect(prefsRegistry.getPreferences(NetworkConfigurationManagerImpl.class)).andReturn(prefs);
        prefs.printPrefs();
        EasyMock.expectLastCall();

        support.replayAll();

        NetworkConfigurationManager networkConfigurationManager = new NetworkConfigurationManagerImpl(prefsRegistry);
        networkConfigurationManager.setSelectedProxyType(ConfigurationType.SYSTEM);
        networkConfigurationManager.getSystemConfiguration().getExclusionPatterns()
                .addAll(Arrays.asList(exclusions.split(",\\s*|\\s+")));

        boolean excluded = networkConfigurationManager.isExcludedFromProxy(host);

        support.verifyAll();

        return excluded;
    }
}
