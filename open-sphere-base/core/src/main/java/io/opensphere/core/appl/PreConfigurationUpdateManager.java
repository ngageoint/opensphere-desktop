package io.opensphere.core.appl;

import java.util.Iterator;
import java.util.ServiceLoader;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;

/**
 * Executes any {@link PreConfigurationUpdateModule}s before the plugins are
 * loaded.
 */
public class PreConfigurationUpdateManager
{
    /**
     * Checks for any {@link PreConfigurationUpdateModule}s and executes them if
     * any.
     *
     * @param prefsRegistry The system {@link PreferencesRegistry}.
     */
    public void checkForConfigChanges(PreferencesRegistry prefsRegistry)
    {
        Iterator<PreConfigurationUpdateModule> configUpdaters = ServiceLoader.load(PreConfigurationUpdateModule.class).iterator();
        Preferences prefs = prefsRegistry.getPreferences(getClass());
        while (configUpdaters.hasNext())
        {
            PreConfigurationUpdateModule configUpdater = configUpdaters.next();
            boolean hasBeenExecuted = prefs.getBoolean(configUpdater.getClass().getName(), false);
            if (!hasBeenExecuted)
            {
                configUpdater.updateConfigs(prefsRegistry);
                prefs.putBoolean(configUpdater.getClass().getName(), true, this);
            }
        }
    }
}
