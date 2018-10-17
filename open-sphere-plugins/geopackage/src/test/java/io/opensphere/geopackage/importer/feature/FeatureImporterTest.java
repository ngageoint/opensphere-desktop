package io.opensphere.geopackage.importer.feature;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.vividsolutions.jts.geom.Point;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.geopackage.importer.MockFeatureDao;
import io.opensphere.geopackage.importer.MockGeoPackageConnection;
import io.opensphere.geopackage.model.GeoPackageColumns;
import io.opensphere.geopackage.model.GeoPackageFeatureLayer;
import io.opensphere.geopackage.model.ProgressModel;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDataType;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.features.user.MockFeatureRow;
import mil.nga.sf.GeometryType;
import mil.nga.sf.proj.ProjectionConstants;

/**
 * Unit test for the {@link FeatureImporter} class.
 */
@SuppressWarnings("boxing")
public class FeatureImporterTest
{
    /**
     * Tests importing features.
     */
    @Test
    public void testImportFeatures()
    {
        EasyMockSupport support = new EasyMockSupport();

        Map<String, GeoPackageFeatureLayer> layers = createLayers();
        List<String> layerNames = New.list(layers.keySet());
        GeoPackage geopackage = createPackage(support, layerNames, null);
        CancellableTaskActivity ta = new CancellableTaskActivity();
        ProgressModel model = new ProgressModel();

        support.replayAll();

        FeatureImporter importer = new FeatureImporter();
        importer.importFeatures(geopackage, layers, ta, model);

        assertEquals(6, model.getCompletedCount());

        int layerIndex = 0;
        for (String layerName : layerNames)
        {
            GeoPackageFeatureLayer layer = layers.get(layerName);

            List<Map<String, Serializable>> data = layer.getData();
            assertEquals(3, data.size());

            int index = 0;
            for (Map<String, Serializable> row : data)
            {
                assertEquals(4, row.size());

                int valueAdder = 100 + layerIndex;
                assertEquals("value" + index + valueAdder, row.get("column1"));
                assertEquals(10 + index + valueAdder, row.get("column2"));
                assertEquals(54.3 + index + valueAdder, row.get("column3"));

                Point actualLocation = (Point)row.get(GeoPackageColumns.GEOMETRY_COLUMN);
                assertEquals(actualLocation.getCoordinate().x, 5 + index + valueAdder, 0d);
                assertEquals(actualLocation.getCoordinate().y, 6 + index + valueAdder, 0d);
                index++;
            }
            layerIndex++;
        }

        support.verifyAll();
    }

    /**
     * Tests importing features and user cancelled import.
     */
    @Test
    public void testImportFeaturesCancelled()
    {
        EasyMockSupport support = new EasyMockSupport();

        Map<String, GeoPackageFeatureLayer> layers = createLayers();
        List<String> layerNames = New.list(layers.keySet());
        CancellableTaskActivity ta = new CancellableTaskActivity();
        GeoPackage geopackage = createPackage(support, layerNames, ta);
        ProgressModel model = new ProgressModel();

        support.replayAll();

        FeatureImporter importer = new FeatureImporter();
        importer.importFeatures(geopackage, layers, ta, model);

        assertEquals(0, model.getCompletedCount());

        support.verifyAll();
    }

    /**
     * Create data to test with.
     *
     * @param support Used to create mocks.
     * @param tableName The name of the dao.
     * @param uniqueValueAdder Some unique number above 100 to help test values
     *            imported.
     * @return The test data.
     */
    private MockFeatureDao createDao(EasyMockSupport support, String tableName, int uniqueValueAdder)
    {
        FeatureTable table = new FeatureTable(tableName, New.list(FeatureColumn.createPrimaryKeyColumn(0, "key"),
                FeatureColumn.createGeometryColumn(1, "geom", GeometryType.POINT, false, null)));

        List<MockFeatureRow> rows = New.list();
        for (int i = 0; i < 3; i++)
        {
            Map<String, Object> tableData = New.map();
            tableData.put("column1", "value" + i + uniqueValueAdder);
            tableData.put("column2", 10 + i + uniqueValueAdder);
            tableData.put("column3", 54.3 + i + uniqueValueAdder);

            mil.nga.sf.Point location = new mil.nga.sf.Point(5 + i + uniqueValueAdder, 6 + i + uniqueValueAdder);
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
        MockFeatureDao dao = new MockFeatureDao(db, geometry, rows, null);

        return dao;
    }

    /**
     * Creates the layers to import.
     *
     * @return The layers to import mapped to their names.
     */
    private Map<String, GeoPackageFeatureLayer> createLayers()
    {
        GeoPackageFeatureLayer layer1 = new GeoPackageFeatureLayer("package", "c:\\somepackage.gpkg", "name1", 3);
        GeoPackageFeatureLayer layer2 = new GeoPackageFeatureLayer("package", "c:\\somepackage.gpkg", "name2", 3);

        Map<String, GeoPackageFeatureLayer> layers = New.map();

        layers.put(layer1.getName(), layer1);
        layers.put(layer2.getName(), layer2);

        return layers;
    }

    /**
     * Mocks a {@link GeoPackage} with test feature data.
     *
     * @param support Used to create the mock.
     * @param layers The layers to mock data for.
     * @param ta Used to test user cancelling import.
     * @return The mocked {@link GeoPackage}.
     */
    private GeoPackage createPackage(EasyMockSupport support, List<String> layers, CancellableTaskActivity ta)
    {
        List<MockFeatureDao> daos = New.list();
        int valueAdder = 100;
        for (String layer : layers)
        {
            daos.add(createDao(support, layer, valueAdder));
            valueAdder++;
        }

        GeoPackage geoPackage = support.createMock(GeoPackage.class);

        int index = 0;
        for (MockFeatureDao dao : daos)
        {
            String tableName = layers.get(index);
            EasyMock.expect(geoPackage.getFeatureDao(EasyMock.cmpEq(tableName))).andAnswer(() -> getFeatureDaoAnswer(dao, ta));
            if (ta != null)
            {
                break;
            }
            index++;
        }

        return geoPackage;
    }

    /**
     * The answer to the getFeatureDao call.
     *
     * @param dao The dao to return.
     * @param ta If not null, sets it to cancelled to test user cancelling.
     * @return The passed in dao.
     */
    private MockFeatureDao getFeatureDaoAnswer(MockFeatureDao dao, CancellableTaskActivity ta)
    {
        if (ta != null)
        {
            ta.setCancelled(true);
        }

        return dao;
    }
}
