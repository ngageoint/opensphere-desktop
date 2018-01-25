package io.opensphere.geopackage.importer.feature;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.geopackage.model.ProgressModel;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionTransform;

/**
 * Imports a whole geopackage table into a list of columns mapped to their
 * values.
 */
public class TableImporter
{
    /**
     * Imports data for a single row.
     */
    private final RowImporter myRowImporter = new RowImporter();

    /**
     * Imports all features from the given feature dao.
     *
     * @param dao Contains the feature data to import.
     * @param ta The task activity to check if user has cancelled the import.
     * @param model The model used by the import classes
     * @return The imported data.
     */
    public List<Map<String, Serializable>> importFeatures(FeatureDao dao, CancellableTaskActivity ta, ProgressModel model)
    {
        List<Map<String, Serializable>> importedTable = New.list();

        FeatureResultSet resultSet = dao.queryForAll();
        ProjectionTransform toGeodetic = dao.getProjection().getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
        while (resultSet.moveToNext() && !ta.isCancelled())
        {
            FeatureRow row = resultSet.getRow();
            Map<String, Serializable> importedRow = myRowImporter.importRow(row, toGeodetic);
            importedTable.add(importedRow);
            model.setCompletedCount(model.getCompletedCount() + 1);
        }

        return importedTable;
    }
}
