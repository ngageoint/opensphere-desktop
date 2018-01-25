package io.opensphere.geopackage.importer.feature;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;

import io.opensphere.core.util.collections.New;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.projection.ProjectionTransform;

/**
 * Imports a {@link FeatureRow} and returns a Map of columns to their values.
 */
public class RowImporter
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(RowImporter.class);

    /**
     * Imports the geometry.
     */
    private final GeometryImporter myGeometryImporter = new GeometryImporter();

    /**
     * Imports all the row data and returns a map of columns to values.
     *
     * @param row The row to import.
     * @param toGeodetic A {@link ProjectionTransform} that will convert the
     *            coordinates of the geometries from their current projection to
     *            a geodetic projection.
     * @return The imported data.
     */
    public Map<String, Serializable> importRow(FeatureRow row, ProjectionTransform toGeodetic)
    {
        String[] columnNames = row.getColumnNames();

        Map<String, Serializable> importedRow = New.map();

        for (String columnName : columnNames)
        {
            if (!columnName.equals(row.getGeometryColumn().getName()))
            {
                Object value = row.getValue(columnName);
                if (value instanceof Serializable)
                {
                    importedRow.put(columnName, (Serializable)value);
                }
                else if (value != null)
                {
                    LOGGER.warn("Ignoring value for column " + columnName + " in table " + row.getTable().getTableName()
                            + ", the value is not serializable");
                }
            }
        }

        myGeometryImporter.importGeometry(importedRow, row, toGeodetic);

        return importedRow;
    }
}
