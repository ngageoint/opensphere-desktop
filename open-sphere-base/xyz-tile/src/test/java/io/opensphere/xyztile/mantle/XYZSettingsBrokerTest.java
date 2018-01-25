package io.opensphere.xyztile.mantle;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.xyztile.model.Projection;
import io.opensphere.xyztile.model.XYZServerInfo;
import io.opensphere.xyztile.model.XYZSettings;
import io.opensphere.xyztile.model.XYZTileLayerInfo;

/**
 * Unit test for {@link XYZSettingsBroker}.
 */
public class XYZSettingsBrokerTest
{
    /**
     * The layer id used for tests.
     */
    private static final String ourLayerId = "iamlayerid";

    /**
     * Tests reading the settings from preferences.
     */
    @Test
    public void testGetSettings()
    {
        EasyMockSupport support = new EasyMockSupport();

        XYZTileLayerInfo layer = new XYZTileLayerInfo(ourLayerId, "A Name", Projection.EPSG_4326, 2, true, 4,
                new XYZServerInfo("serverName", "http://somehost"));
        layer.setMaxLevels(18);
        layer.setMaxLevelsUser(14);

        Preferences prefs = createPrefsRead(support, layer);
        PreferencesRegistry prefsRegistry = createPrefsRegistry(support, prefs);

        support.replayAll();

        XYZSettingsBroker broker = new XYZSettingsBroker(prefsRegistry);
        XYZSettings settings = broker.getSettings(layer);

        assertEquals(ourLayerId, settings.getLayerId());
        assertEquals(18, settings.getMaxZoomLevelDefault());
        assertEquals(14, settings.getMaxZoomLevelCurrent());

        support.verifyAll();
    }

    /**
     * Tests saving the settings.
     */
    @Test
    public void testSaveSettings()
    {
        EasyMockSupport support = new EasyMockSupport();

        XYZSettings settings = new XYZSettings();
        settings.setLayerId(ourLayerId);
        settings.setMaxZoomLevelCurrent(12);
        settings.setMaxZoomLevelDefault(18);

        Preferences prefs = createPrefsSave(support, settings);
        PreferencesRegistry prefsRegistry = createPrefsRegistry(support, prefs);

        support.replayAll();

        XYZSettingsBroker broker = new XYZSettingsBroker(prefsRegistry);
        broker.saveSettings(settings);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link Preferences} expecting a read call.
     *
     * @param support Used to create the mock.
     * @param layer The layer to get settings for.
     * @return The {@link Preferences}.
     */
    private Preferences createPrefsRead(EasyMockSupport support, XYZTileLayerInfo layer)
    {
        Preferences prefs = support.createMock(Preferences.class);

        EasyMock.expect(
                prefs.getJAXBObject(EasyMock.eq(XYZSettings.class), EasyMock.cmpEq(ourLayerId), EasyMock.isA(XYZSettings.class)))
                .andAnswer(() -> getJAXBAnswer(layer));

        return prefs;
    }

    /**
     * Creates an easy mocked {@link PreferencesRegistry}.
     *
     * @param support Used to create the mock.
     * @param prefs Mocked {@link Preferences} to return.
     * @return The mock.
     */
    private PreferencesRegistry createPrefsRegistry(EasyMockSupport support, Preferences prefs)
    {
        PreferencesRegistry prefsRegistry = support.createMock(PreferencesRegistry.class);

        EasyMock.expect(prefsRegistry.getPreferences(EasyMock.eq(XYZSettings.class))).andReturn(prefs);

        return prefsRegistry;
    }

    /**
     * Creates an easy mocked {@link Preferences} expecting a save call.
     *
     * @param support Used to create the mock.
     * @param expected The expected settings to be saved.
     * @return The mocked {@link Preferences}.
     */
    private Preferences createPrefsSave(EasyMockSupport support, XYZSettings expected)
    {
        Preferences prefs = support.createMock(Preferences.class);

        EasyMock.expect(prefs.putJAXBObject(EasyMock.cmpEq(ourLayerId), EasyMock.eq(expected), EasyMock.eq(false),
                EasyMock.isA(XYZSettingsBroker.class))).andReturn(Boolean.TRUE);

        return prefs;
    }

    /**
     * Answer for the mocked getJAXBObject call.
     *
     * @param layer The layer we are getting settings for.
     * @return The passed in settings.
     */
    private XYZSettings getJAXBAnswer(XYZTileLayerInfo layer)
    {
        XYZSettings settings = (XYZSettings)EasyMock.getCurrentArguments()[2];

        assertEquals(layer.getName(), settings.getLayerId());
        assertEquals(layer.getMaxLevels(), settings.getMaxZoomLevelCurrent());
        assertEquals(layer.getMaxLevelsDefault(), settings.getMaxZoomLevelDefault());

        settings.setMaxZoomLevelDefault(0);

        return settings;
    }
}
