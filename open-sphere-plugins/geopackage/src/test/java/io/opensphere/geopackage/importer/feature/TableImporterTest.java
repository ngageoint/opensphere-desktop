package io.opensphere.geopackage.importer.feature;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.vividsolutions.jts.geom.Point;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.geopackage.importer.MockFeatureDao;
import io.opensphere.geopackage.importer.MockGeoPackageConnection;
import io.opensphere.geopackage.model.GeoPackageColumns;
import io.opensphere.geopackage.model.ProgressModel;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDataType;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.features.user.MockFeatureRow;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.wkb.geom.GeometryType;

/**
 * Unit test for the {@link TableImporter} class.
 */
public class TableImporterTest
{
    /**
     * Tests importing a feature table from a geopackage file.
     */
    @Test
    public void testImportFeatures()
    {
        EasyMockSupport support = new EasyMockSupport();

        CancellableTaskActivity ta = new CancellableTaskActivity();
        MockFeatureDao dao = createTestData(support, null);
        ProgressModel model = new ProgressModel();

        support.replayAll();

        TableImporter importer = new TableImporter();

        List<Map<String, Serializable>> importedRows = importer.importFeatures(dao, ta, model);

        assertEquals(3, model.getCompletedCount());
        assertEquals(3, importedRows.size());

        int index = 0;
        for (Map<String, Serializable> row : importedRows)
        {
            assertEquals(4, row.size());

            assertEquals("value" + index, row.get("column1"));
            assertEquals(10 + index, row.get("column2"));
            assertEquals(54.3 + index, row.get("column3"));

            Point actualLocation = (Point)row.get(GeoPackageColumns.GEOMETRY_COLUMN);
            assertEquals(actualLocation.getCoordinate().x, 5 + index, 0d);
            assertEquals(actualLocation.getCoordinate().y, 6 + index, 0d);
            index++;
        }

        support.verifyAll();
    }

    /**
     * Tests cancelling an import.
     */
    @Test
    public void testImportFeaturesCancel()
    {
        EasyMockSupport support = new EasyMockSupport();

        CancellableTaskActivity ta = new CancellableTaskActivity();
        MockFeatureDao dao = createTestData(support, ta);
        ProgressModel model = new ProgressModel();

        support.replayAll();

        TableImporter importer = new TableImporter();

        List<Map<String, Serializable>> importedRows = importer.importFeatures(dao, ta, model);

        assertEquals(1, model.getCompletedCount());
        assertEquals(1, importedRows.size());

        int index = 0;
        for (Map<String, Serializable> row : importedRows)
        {
            assertEquals(4, row.size());

            assertEquals("value" + index, row.get("column1"));
            assertEquals(10 + index, row.get("column2"));
            assertEquals(54.3 + index, row.get("column3"));

            Point actualLocation = (Point)row.get(GeoPackageColumns.GEOMETRY_COLUMN);
            assertEquals(actualLocation.getCoordinate().x, 5 + index, 0d);
            assertEquals(actualLocation.getCoordinate().y, 6 + index, 0d);
            index++;
        }

        support.verifyAll();
    }

    /**
     * Create data to test with.
     *
     * @param support Used to create mocks.
     * @param ta Used to test cancelling or null if cancelling should not be
     *            tested.
     * @return The test data.
     */
    private MockFeatureDao createTestData(EasyMockSupport support, CancellableTaskActivity ta)
    {
        FeatureTable table = new FeatureTable("test", New.list(FeatureColumn.createPrimaryKeyColumn(0, "key"),
                FeatureColumn.createGeometryColumn(1, "geom", GeometryType.POINT, false, null)));

        List<MockFeatureRow> rows = New.list();
        for (int i = 0; i < 3; i++)
        {
            Map<String, Object> tableData = New.map();
            tableData.put("column1", "value" + i);
            tableData.put("column2", 10 + i);
            tableData.put("column3", 54.3 + i);

            mil.nga.wkb.geom.Point location = new mil.nga.wkb.geom.Point(5 + i, 6 + i);
            MockFeatureRow row = new MockFeatureRow(table, location, tableData);
            rows.add(row);
        }

        MockGeoPackageConnection db = new MockGeoPackageConnection(support.createMock(Connection.class));

        GeometryColumns geometry = new GeometryColumns();
        Contents contents = new Contents();
        contents.setDataType(ContentsDataType.FEATURES);

        geometry.setContents(contents);
        geometry.setSrs(new SpatialReferenceSystem());
        geometry.getSrs().setOrganization(ProjectionConstants.AUTHORITY_EPSG);
        geometry.getSrs().setOrganizationCoordsysId(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
        MockFeatureDao dao = new MockFeatureDao(db, geometry, rows, ta);

        return dao;
    }
}
