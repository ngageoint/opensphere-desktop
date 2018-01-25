package io.opensphere.geopackage.importer.feature;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.geopackage.model.GeoPackageFeatureLayer;
import io.opensphere.geopackage.model.ProgressModel;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.features.user.FeatureDao;

/**
 * Imports all features contained with a Geopackage file and saves the data to
 * the registry.
 */
public class FeatureImporter
{
    /**
     * Imports all the data for a given feature table.
     */
    private final TableImporter myTableImporter = new TableImporter();

    /**
     * Imports the feature layer data contained in the specified geopackage.
     *
     * @param geopackage The geopackage containing the features to import.
     * @param featureLayers The feature layers to import data for.
     * @param ta Used to monitor if the user has cancelled the import.
     * @param model Used to report import progress to the user.
     */
    public void importFeatures(GeoPackage geopackage, Map<String, GeoPackageFeatureLayer> featureLayers,
            CancellableTaskActivity ta, ProgressModel model)
    {
        for (Entry<String, GeoPackageFeatureLayer> entry : featureLayers.entrySet())
        {
            String featureTable = entry.getKey();
            FeatureDao featureDao = geopackage.getFeatureDao(featureTable);
            List<Map<String, Serializable>> importedData = myTableImporter.importFeatures(featureDao, ta, model);
            entry.getValue().getData().addAll(importedData);

            if (ta.isCancelled())
            {
                break;
            }
        }
    }
}
