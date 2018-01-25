package io.opensphere.core.preferences;

/** Provides a way to update file preferences before they are read in. */
public interface FilePreferencesPersistenceUpdater
{
    /**
     * Updates the necessary configuration files.
     *
     * @param baseDirectory The directory containing the file preferences.
     */
    void updateConfigs(String baseDirectory);
}
