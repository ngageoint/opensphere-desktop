package io.opensphere.xyztile.mantle;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.xyztile.model.XYZSettings;
import io.opensphere.xyztile.model.XYZTileLayerInfo;

/**
 * Class that reads and saves {@link XYZSettings} to preferences.
 */
public class XYZSettingsBroker implements SettingsBroker
{
    /**
     * The XYZ settings preferences.
     */
    private final Preferences myPreferences;

    /**
     * Constructs a new {@link XYZSettings} reader and writer.
     *
     * @param prefsRegistry The system {@link PreferencesRegistry}.
     */
    public XYZSettingsBroker(PreferencesRegistry prefsRegistry)
    {
        myPreferences = prefsRegistry.getPreferences(XYZSettings.class);
    }

    @Override
    public XYZSettings getSettings(XYZTileLayerInfo layer)
    {
        XYZSettings defaultSettings = new XYZSettings();
        defaultSettings.setLayerId(layer.getName());
        defaultSettings.setMaxZoomLevelCurrent(layer.getMaxLevels());
        defaultSettings.setMaxZoomLevelDefault(layer.getMaxLevelsDefault());

        XYZSettings settings = myPreferences.getJAXBObject(XYZSettings.class, layer.getName(), defaultSettings);
        settings.setMaxZoomLevelDefault(layer.getMaxLevelsDefault());

        return settings;
    }

    @Override
    public void saveSettings(XYZSettings settings)
    {
        myPreferences.putJAXBObject(settings.getLayerId(), settings, false, this);
    }
}
