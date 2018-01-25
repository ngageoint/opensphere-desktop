package io.opensphere.xyztile.mantle;

import io.opensphere.xyztile.model.XYZSettings;
import io.opensphere.xyztile.model.XYZTileLayerInfo;

/**
 * Interface to an object that knows how to save xyz settings.
 */
public interface SettingsBroker
{
    /**
     * Reads the {@link XYZSettings} from preferences for the specified layer.
     *
     * @param layer The layer to read the setting for.
     * @return The layer's settings.
     */
    XYZSettings getSettings(XYZTileLayerInfo layer);

    /**
     * Saves the settings to the preferences.
     *
     * @param settings The settings to save.
     */
    void saveSettings(XYZSettings settings);
}
