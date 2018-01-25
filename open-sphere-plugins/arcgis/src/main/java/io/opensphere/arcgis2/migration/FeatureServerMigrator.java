package io.opensphere.arcgis2.migration;

import java.util.List;
import java.util.Map;

import io.opensphere.arcgis.config.v1.ArcGISServerSource;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ThreeTuple;
import io.opensphere.core.util.net.UrlUtilities;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.impl.UrlDataSource;
import io.opensphere.mantle.datasources.impl.UrlSourceConfig;
import io.opensphere.server.OGCServerPlugin;
import io.opensphere.server.config.v1.OGCServerConfig;
import io.opensphere.server.source.OGCServerSource;

/**
 * Migrates old ArcGIS feature servers to the new plugin.
 */
public class FeatureServerMigrator implements MicroMigrator
{
    /**
     * Used to get and save preferences.
     */
    private final PreferencesRegistry myPreferencesRegistry;

    /**
     * Constructor.
     *
     * @param prefsRegistry The preferences registry.
     */
    public FeatureServerMigrator(PreferencesRegistry prefsRegistry)
    {
        myPreferencesRegistry = prefsRegistry;
    }

    @Override
    public void migrate(Map<ArcGISServerSource, UrlDataSource> oldServerToNewServer)
    {
        Preferences ogcServerPrefs = myPreferencesRegistry.getPreferences(OGCServerPlugin.class);
        OGCServerConfig ogcServerConfig = ogcServerPrefs.getJAXBObject(OGCServerConfig.class, "serverConfig", null);

        Preferences newPrefs = myPreferencesRegistry.getPreferences("io.opensphere.arcgis2.ArcGIS2Plugin");
        UrlSourceConfig newConfig = newPrefs.getJAXBObject(UrlSourceConfig.class, "serverConfig", null);

        if (ogcServerConfig != null && newConfig != null)
        {
            Map<String, UrlDataSource> existingNewUrls = New.map();
            for (IDataSource source : newConfig.getSourceList())
            {
                UrlDataSource urlSource = (UrlDataSource)source;
                ThreeTuple<String, String, Integer> host = UrlUtilities.getProtocolHostPort(urlSource.getBaseUrl(), 80);
                existingNewUrls.put(host.getSecondObject(), urlSource);
            }

            boolean hasChanged = false;
            List<OGCServerSource> toMigrate = New.list();
            for (OGCServerSource source : ogcServerConfig.getServerSources())
            {
                if ("ArcGIS".equals(source.getServerType()))
                {
                    toMigrate.add(source);
                }
            }

            for (OGCServerSource old : toMigrate)
            {
                String url = old.getURL(OGCServerSource.WMS_SERVICE);
                String restServices = "/rest/services";
                int servicesIndex = url.indexOf(restServices);
                if (servicesIndex > 0)
                {
                    url = url.substring(0, servicesIndex + restServices.length());
                }

                ThreeTuple<String, String, Integer> host = UrlUtilities.getProtocolHostPort(url, 80);
                if (!existingNewUrls.containsKey(host.getSecondObject()))
                {
                    UrlDataSource newSource = new UrlDataSource(old.getName(), url);
                    newConfig.addSource(newSource);
                    hasChanged = true;
                    existingNewUrls.put(host.getSecondObject(), newSource);
                }

                ogcServerConfig.removeSource(old);
            }

            if (!toMigrate.isEmpty())
            {
                ogcServerPrefs.putJAXBObject("serverConfig", ogcServerConfig, false, this);
            }

            if (hasChanged)
            {
                newPrefs.putJAXBObject("serverConfig", newConfig, false, this);
            }
        }
    }
}
