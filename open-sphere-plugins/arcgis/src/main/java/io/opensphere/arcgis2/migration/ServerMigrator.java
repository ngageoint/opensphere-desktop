package io.opensphere.arcgis2.migration;

import java.util.Map;

import io.opensphere.arcgis.config.v1.ArcGISServerConfig;
import io.opensphere.arcgis.config.v1.ArcGISServerSource;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.impl.UrlDataSource;
import io.opensphere.mantle.datasources.impl.UrlSourceConfig;

/**
 * Migrates the old server config's to the new one.
 */
public class ServerMigrator
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
    public ServerMigrator(PreferencesRegistry prefsRegistry)
    {
        myPrefsRegistry = prefsRegistry;
    }

    /**
     * Migrates the order manager registry values to match that of the new id
     * formats.
     *
     * @return The map of old server url's to new ones.
     */
    public Map<ArcGISServerSource, UrlDataSource> migrate()
    {
        Preferences oldPrefs = myPrefsRegistry.getPreferences("io.opensphere.arcgis.ArcGISPlugin");
        ArcGISServerConfig config = oldPrefs.getJAXBObject(ArcGISServerConfig.class, "serverConfig", (ArcGISServerConfig)null);

        Preferences newPrefs = myPrefsRegistry.getPreferences("io.opensphere.arcgis2.ArcGIS2Plugin");
        UrlSourceConfig newConfig = newPrefs.getJAXBObject(UrlSourceConfig.class, "serverConfig", new UrlSourceConfig());

        Map<ArcGISServerSource, UrlDataSource> oldToNew = New.map();
        if (config != null)
        {
            Map<String, UrlDataSource> existingNewUrls = New.map();
            for (IDataSource source : newConfig.getSourceList())
            {
                UrlDataSource urlSource = (UrlDataSource)source;
                existingNewUrls.put(urlSource.getBaseUrl(), urlSource);
            }

            boolean hasChanged = false;
            for (ArcGISServerSource participant : config.getServerSources())
            {
                if (!existingNewUrls.containsKey(participant.getURL("wfs")))
                {
                    UrlDataSource newSource = new UrlDataSource(participant.getName(), participant.getURL("wfs"));
                    newConfig.addSource(newSource);
                    hasChanged = true;
                    existingNewUrls.put(newSource.getBaseUrl(), newSource);
                }

                oldToNew.put(participant, existingNewUrls.get(participant.getURL("wfs")));
            }

            if (hasChanged)
            {
                newPrefs.putJAXBObject("serverConfig", newConfig, false, this);
            }
        }

        // Right now its a map of the same URL to same URL, but it may possible
        // be different depending on how high side looks.
        return oldToNew;
    }
}
