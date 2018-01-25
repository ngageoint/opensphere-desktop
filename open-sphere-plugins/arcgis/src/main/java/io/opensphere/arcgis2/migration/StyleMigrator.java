package io.opensphere.arcgis2.migration;

import java.util.Map;
import java.util.Map.Entry;

import io.opensphere.arcgis.config.v1.ArcGISServerSource;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.mantle.datasources.impl.UrlDataSource;

/**
 * Migrates the styles of the old layers to match the id's of the new layers.
 */
public class StyleMigrator implements MicroMigrator
{
    /**
     * Used to get and save preferences.
     */
    private final PreferencesRegistry myPrefsRegistry;

    /**
     * Constructor.
     *
     * @param prefsRegistry Used to get and save preferences.
     */
    public StyleMigrator(PreferencesRegistry prefsRegistry)
    {
        myPrefsRegistry = prefsRegistry;
    }

    /**
     * Migrates the order manager registry values to match that of the new id
     * formats.
     *
     * @param oldServerToNewServer The map of old server url's, to the new
     *            server url's.
     */
    @Override
    public void migrate(Map<ArcGISServerSource, UrlDataSource> oldServerToNewServer)
    {
        Preferences prefs = myPrefsRegistry.getPreferences("io.opensphere.mantle.data.DataTypeInfoPreferenceAssistant");

        for (String key : prefs.keys())
        {
            for (Entry<ArcGISServerSource, UrlDataSource> entry : oldServerToNewServer.entrySet())
            {
                if (key.startsWith(entry.getKey().getURL(null)))
                {
                    String newKey = key.replace(entry.getKey().getURL(null), entry.getValue().getBaseUrl());
                    StringBuffer buffer = new StringBuffer(entry.getValue().getBaseUrl());
                    buffer.append(newKey);
                    newKey = buffer.toString();

                    if (key.endsWith("VISIBILITY"))
                    {
                        boolean value = prefs.getBoolean(key, true);
                        prefs.putBoolean(newKey, value, this);
                    }
                    else
                    {
                        int value = prefs.getInt(key, 0);
                        prefs.putInt(newKey, value, this);
                    }
                }
            }
        }
    }
}
