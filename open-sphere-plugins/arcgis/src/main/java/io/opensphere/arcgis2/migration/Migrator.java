package io.opensphere.arcgis2.migration;

import java.util.Map;

import org.apache.log4j.Logger;

import io.opensphere.arcgis.config.v1.ArcGISServerSource;
import io.opensphere.core.appl.PreConfigurationUpdateModule;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.mantle.datasources.impl.UrlDataSource;

/**
 * Migrates the old ArcGIS plugin configs to match the new configs for the this
 * new plugin.
 */
public class Migrator implements PreConfigurationUpdateModule
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(Migrator.class);

    @Override
    public void updateConfigs(PreferencesRegistry prefsRegistry)
    {
        LOGGER.info("Migrating old ArcGIS configurations.");
        ServerMigrator xyzServerMigrator = new ServerMigrator(prefsRegistry);
        Map<ArcGISServerSource, UrlDataSource> oldToNewServers = xyzServerMigrator.migrate();

        MicroMigrator[] migrators = new MicroMigrator[] { new FeatureServerMigrator(prefsRegistry),
            new ActiveLayersMigrator(prefsRegistry), new OrderManagerMigrator(prefsRegistry), new StyleMigrator(prefsRegistry) };

        for (MicroMigrator migrator : migrators)
        {
            migrator.migrate(oldToNewServers);
        }

        LOGGER.info("Done migrating old ArcGIS configurations.");
    }
}
