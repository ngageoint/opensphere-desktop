package io.opensphere.core.net;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.NetworkConfigurationManager;
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
        Capture<String> prefKey = EasyMock.newCapture();
        EasyMock.expect(prefs.removeInt(EasyMock.eq("ProxyPort"), EasyMock.isA(NetworkConfigurationManager.class))).andReturn(80);
        EasyMock.expect(
                prefs.putString(EasyMock.eq("ProxyHost"), EasyMock.eq(""), EasyMock.isA(NetworkConfigurationManager.class)))
                .andReturn(null);
        EasyMock.expect(prefs.putBoolean(EasyMock.eq("SystemProxiesEnabled"), EasyMock.eq(false),
                EasyMock.isA(NetworkConfigurationManager.class))).andReturn(null);
        EasyMock.expect(
                prefs.putString(EasyMock.eq("ProxyConfigUrl"), EasyMock.eq(""), EasyMock.isA(NetworkConfigurationManager.class)))
                .andReturn(null);
        EasyMock.expect(prefs.putString(EasyMock.eq("ProxyExclusionPatterns"), EasyMock.capture(prefKey), EasyMock.anyObject()))
                .andReturn(null);
        EasyMock.expect(prefs.getString("ProxyExclusionPatterns", "")).andAnswer(() -> prefKey.getValue());
        PreferencesRegistry prefsRegistry = support.createMock(PreferencesRegistry.class);
        EasyMock.expect(prefsRegistry.getPreferences(NetworkConfigurationManagerImpl.class)).andReturn(prefs);
        prefs.printPrefs();
        EasyMock.expectLastCall();

        support.replayAll();

        NetworkConfigurationManager networkConfigurationManager = new NetworkConfigurationManagerImpl(prefsRegistry);
        networkConfigurationManager.setProxyConfiguration("", -1, false, "", exclusions);

        boolean excluded = networkConfigurationManager.isExcludedFromProxy(host);

        support.verifyAll();

        return excluded;
    }
}
