package io.opensphere.wms.toolbox.impl;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.wms.capabilities.WMSServerCapabilities;
import io.opensphere.wms.sld.config.v1.SldConfiguration;
import io.opensphere.wms.toolbox.WMSServerCapabilitiesListener;

/**
 * Unit test for {@link WMSToolboxImpl}.
 */
public class WMSToolboxImplTest
{
    /**
     * Tests notifies the {@link WMSServerCapabilitiesListener}.
     */
    @Test
    public void testNotify()
    {
        EasyMockSupport support = new EasyMockSupport();

        ServerConnectionParams params = support.createMock(ServerConnectionParams.class);
        WMSServerCapabilities capabilities = support.createMock(WMSServerCapabilities.class);
        WMSServerCapabilitiesListener listener = createListener(support, params, capabilities);
        Toolbox toolbox = createToolbox(support);

        support.replayAll();

        WMSToolboxImpl wmsToolbox = new WMSToolboxImpl(toolbox);

        wmsToolbox.addServerCapabiltiesListener(listener);
        wmsToolbox.notifyServerCapabilitiesListener(params, capabilities);
        wmsToolbox.removeServerCapabilitiesListener(listener);
        wmsToolbox.notifyServerCapabilitiesListener(params, null);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link WMSServerCapabilitiesListener}.
     * @param support Used to create the mock.
     * @param params The expected params.
     * @param capabilities The expected {@link WMSServerCapabilities}.
     * @return The mocked listener.
     */
    private WMSServerCapabilitiesListener createListener(EasyMockSupport support, ServerConnectionParams params, WMSServerCapabilities capabilities)
    {
        WMSServerCapabilitiesListener listener = support.createMock(WMSServerCapabilitiesListener.class);

        listener.received(params, capabilities);

        return listener;
    }

    /**
     * Creates an easy mocked toolbox.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link Toolbox}.
     */
    private Toolbox createToolbox(EasyMockSupport support)
    {
        Preferences prefs = support.createMock(Preferences.class);
        EasyMock.expect(prefs.getJAXBObject(EasyMock.eq(SldConfiguration.class), EasyMock.cmpEq("SldConfiguration"),
                EasyMock.isA(SldConfiguration.class))).andReturn(new SldConfiguration());

        PreferencesRegistry prefsRegistry = support.createMock(PreferencesRegistry.class);
        EasyMock.expect(prefsRegistry.getPreferences(SldConfiguration.class)).andReturn(prefs);

        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getPreferencesRegistry()).andReturn(prefsRegistry);

        return toolbox;
    }
}
