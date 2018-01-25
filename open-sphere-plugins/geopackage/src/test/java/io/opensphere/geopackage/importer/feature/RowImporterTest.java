package io.opensphere.geopackage.importer.feature;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Map;

import org.junit.Test;

import com.vividsolutions.jts.geom.Point;

import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.model.GeoPackageColumns;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.features.user.MockFeatureRow;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.wkb.geom.GeometryType;

/**
 * Unit test for {@link RowImporter}.
 */
public class RowImporterTest
{
    /**
     * Tests importing a row.
     */
    @Test
    public void testImportRow()
    {
        Projection geodetic = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
        ProjectionTransform toGeodetic = geodetic.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        Map<String, Object> tableData = New.map();
        tableData.put("column1", "value1");
        tableData.put("column2", 10);
        tableData.put("column3", 54.3);

        FeatureTable table = new FeatureTable("test", New.list(FeatureColumn.createPrimaryKeyColumn(0, "key"),
                FeatureColumn.createGeometryColumn(1, "geom", GeometryType.POINT, false, null)));
        mil.nga.wkb.geom.Point location = new mil.nga.wkb.geom.Point(5, 6);
        MockFeatureRow row = new MockFeatureRow(table, location, tableData);
        RowImporter importer = new RowImporter();
        Map<String, Serializable> importedRow = importer.importRow(row, toGeodetic);

        assertEquals(4, importedRow.size());

        assertEquals("value1", importedRow.get("column1"));
        assertEquals(10, importedRow.get("column2"));
        assertEquals(54.3, importedRow.get("column3"));

        Point actualLocation = (Point)importedRow.get(GeoPackageColumns.GEOMETRY_COLUMN);
        assertEquals(actualLocation.getCoordinate().x, location.getX(), 0d);
        assertEquals(actualLocation.getCoordinate().y, location.getY(), 0d);
    }
}
