package io.opensphere.core.appl;

import io.opensphere.core.preferences.PreferencesRegistry;

/**
 * Interface to an object that needs to modify config files before any system
 * components have loaded them into memory.
 */
public interface PreConfigurationUpdateModule
{
    /**
     * Updates the necessary configuration files.
     *
     * @param prefsRegistry The system {@link PreferencesRegistry}.
     */
    public void updateConfigs(PreferencesRegistry prefsRegistry);
}
