package io.opensphere.arcgis2.migration;

import java.util.Map;

import io.opensphere.arcgis.config.v1.ArcGISServerSource;
import io.opensphere.mantle.datasources.impl.UrlDataSource;

/**
 * Migrates a specific feature from the old ArcGIS plugin to the new one.
 */
public interface MicroMigrator
{
    /**
     * Migrates the specific features.
     *
     * @param oldServerToNewServer Map of old server url to new server url.
     */
    void migrate(Map<ArcGISServerSource, UrlDataSource> oldServerToNewServer);
}
