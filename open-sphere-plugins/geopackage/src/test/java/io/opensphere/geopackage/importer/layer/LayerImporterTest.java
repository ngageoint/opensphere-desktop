package io.opensphere.geopackage.importer.layer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.geopackage.importer.MockExtensionsDao;
import io.opensphere.geopackage.importer.MockFeatureDao;
import io.opensphere.geopackage.importer.MockGeoPackageConnection;
import io.opensphere.geopackage.importer.MockTileDao;
import io.opensphere.geopackage.model.GeoPackageFeatureLayer;
import io.opensphere.geopackage.model.GeoPackageLayer;
import io.opensphere.geopackage.model.GeoPackagePropertyDescriptors;
import io.opensphere.geopackage.model.GeoPackageTileLayer;
import io.opensphere.geopackage.model.LayerType;
import io.opensphere.geopackage.progress.ProgressReporter;
import io.opensphere.geopackage.util.Constants;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDataType;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.extension.Extensions;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.features.user.MockFeatureRow;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.GeometryType;

/**
 * Tests importing layers from a geopackage file.
 */
public class LayerImporterTest
{
    /**
     * The test feature tables.
     */
    private static final List<String> ourFeatureLayers = New.list("feature1", "feature2");

    /**
     * Test file path to a fake geopackage file.
     */
    private static final String ourFilePath = "c:\test.gpkg";

    /**
     * Test geopackage name.
     */
    private static final String ourPackageName = "Test Package";

    /**
     * The test tile tables.
     */
    private static final List<String> ourTileLayers = New.list("tile1", "tile2");

    /**
     * Tests importing layers for a geopackage file that contains both features
     * and tiles.
     *
     * @throws SQLException Bad SQL.
     */
    @Test
    public void testImportLayers() throws SQLException
    {
        EasyMockSupport support = new EasyMockSupport();

        List<TileMatrix> matrices = New.list();

        TileMatrix tileMatrix1 = new TileMatrix();
        tileMatrix1.setZoomLevel(8);
        tileMatrix1.setMatrixWidth(20);
        tileMatrix1.setMatrixHeight(20);

        TileMatrix tileMatrix2 = new TileMatrix();
        tileMatrix2.setZoomLevel(9);
        tileMatrix2.setMatrixWidth(40);
        tileMatrix2.setMatrixHeight(40);

        matrices.add(tileMatrix1);
        matrices.add(tileMatrix2);

        BoundingBox boundingBox = new BoundingBox(10, 11, 10, 11);

        GeoPackage geoPackage = createPackage(support, matrices, boundingBox);
        List<DefaultCacheDeposit<GeoPackageLayer>> deposits = New.list();
        DataRegistry registry = createRegistry(support, deposits);

        support.replayAll();

        LayerImporter importer = new LayerImporter(registry);
        List<GeoPackageLayer> importedLayers = New.list();
        CancellableTaskActivity ta = new CancellableTaskActivity();
        ProgressReporter reporter = importer.importLayers(geoPackage, importedLayers, ta);

        assertEquals(ourFeatureLayers.size(), reporter.getModel().getCompletedCount());

        assertEquals(1, deposits.size());

        DefaultCacheDeposit<GeoPackageLayer> deposit = deposits.get(0);

        assertEquals(1, deposit.getAccessors().size());

        @SuppressWarnings("unchecked")
        PropertyAccessor<GeoPackageLayer, GeoPackageLayer> accessor = (PropertyAccessor<GeoPackageLayer, GeoPackageLayer>)deposit
                .getAccessors().iterator().next();

        assertTrue(accessor instanceof SerializableAccessor);
        assertEquals(GeoPackagePropertyDescriptors.GEOPACKAGE_LAYER_PROPERTY_DESCRIPTOR, accessor.getPropertyDescriptor());

        assertEquals(new DataModelCategory(ourFilePath, ourPackageName, GeoPackageLayer.class.getName()), deposit.getCategory());
        assertEquals(Long.MAX_VALUE, deposit.getExpirationDate().getTime());

        assertTrue(deposit.isNew());

        assertEquals(4, deposit.getInput().size());

        @SuppressWarnings("unchecked")
        Iterator<GeoPackageLayer> layers = (Iterator<GeoPackageLayer>)deposit.getInput().iterator();
        for (String feature : ourFeatureLayers)
        {
            GeoPackageFeatureLayer layer = (GeoPackageFeatureLayer)layers.next();
            assertEquals(feature, layer.getName());
            assertEquals(LayerType.FEATURE, layer.getLayerType());
            assertEquals(ourPackageName, layer.getPackageName());
            assertEquals(ourFilePath, layer.getPackageFile());
            assertEquals(10000, layer.getRecordCount());
            assertEquals(feature, layer.getData().get(0).get("column1"));
        }

        for (String tile : ourTileLayers)
        {
            GeoPackageTileLayer layer = (GeoPackageTileLayer)layers.next();
            assertEquals(tile, layer.getName());
            assertEquals(LayerType.TILE, layer.getLayerType());
            assertEquals(ourPackageName, layer.getPackageName());
            assertEquals(ourFilePath, layer.getPackageFile());
            assertEquals(100, layer.getRecordCount());
            assertEquals(8, layer.getMinZoomLevel());
            assertEquals(9, layer.getMaxZoomLevel());
            assertEquals(
                    new io.opensphere.geopackage.model.TileMatrix(tileMatrix1.getMatrixHeight(), tileMatrix1.getMatrixWidth()),
                    layer.getZoomLevelToMatrix().get(Long.valueOf(8)));
            assertEquals(
                    new io.opensphere.geopackage.model.TileMatrix(tileMatrix2.getMatrixHeight(), tileMatrix2.getMatrixWidth()),
                    layer.getZoomLevelToMatrix().get(Long.valueOf(9)));
            assertEquals(new GeographicBoundingBox(LatLonAlt.createFromDegrees(10, 10), LatLonAlt.createFromDegrees(11, 11)),
                    layer.getBoundingBox());

            if ("tile1".equals(tile))
            {
                assertEquals(1, layer.getExtensions().size());
                assertEquals("mesh", layer.getExtensions().get(Constants.TERRAIN_EXTENSION));
            }
            else
            {
                assertTrue(layer.getExtensions().isEmpty());
            }
        }

        support.verifyAll();
    }

    /**
     * The answer to addModels.
     *
     * @param deposits The list to add the deposit to.
     * @return The model ids.
     */
    @SuppressWarnings("unchecked")
    private long[] addModelsAnswer(List<DefaultCacheDeposit<GeoPackageLayer>> deposits)
    {
        DefaultCacheDeposit<GeoPackageLayer> deposit = (DefaultCacheDeposit<GeoPackageLayer>)EasyMock.getCurrentArguments()[0];
        deposits.add(deposit);
        return new long[] { 1, 2, 3, 4 };
    }

    /**
     * Creates an easy mocked geopackage.
     *
     * @param support Used to create the mock.
     * @param tileMatrices The TileMatrices to use for tile layers.
     * @param tileBoundingBox The bounding box to use for tile layers.
     * @return The mocked {@link GeoPackage}.
     * @throws SQLException Bad Sql.
     */
    private GeoPackage createPackage(EasyMockSupport support, List<TileMatrix> tileMatrices, BoundingBox tileBoundingBox)
        throws SQLException
    {
        GeoPackage geopackage = support.createMock(GeoPackage.class);

        EasyMock.expect(geopackage.getFeatureTables()).andReturn(ourFeatureLayers);
        EasyMock.expect(geopackage.getTileTables()).andReturn(ourTileLayers);
        EasyMock.expect(geopackage.getName()).andReturn(ourPackageName);
        EasyMock.expect(geopackage.getPath()).andReturn(ourFilePath);

        MockGeoPackageConnection db = new MockGeoPackageConnection(support.createMock(Connection.class));

        for (String featureLayer : ourFeatureLayers)
        {
            GeometryColumns geometry = new GeometryColumns();
            Contents contents = new Contents();
            contents.setDataType(ContentsDataType.FEATURES);

            geometry.setContents(contents);
            geometry.setSrs(new SpatialReferenceSystem());
            geometry.getSrs().setOrganization(ProjectionConstants.AUTHORITY_EPSG);
            geometry.getSrs().setOrganizationCoordsysId(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

            FeatureTable table = new FeatureTable(featureLayer, New.list(FeatureColumn.createPrimaryKeyColumn(0, "key"),
                    FeatureColumn.createGeometryColumn(1, "geom", GeometryType.POINT, false, null)));

            List<MockFeatureRow> rows = New.list();
            Map<String, Object> tableData = New.map();
            tableData.put("column1", featureLayer);

            mil.nga.sf.Point location = new mil.nga.sf.Point(5, 6);
            MockFeatureRow row = new MockFeatureRow(table, location, tableData);
            rows.add(row);

            MockFeatureDao dao = new MockFeatureDao(db, geometry, rows, null);
            EasyMock.expect(geopackage.getFeatureDao(EasyMock.cmpEq(featureLayer))).andReturn(dao).atLeastOnce();
        }

        for (String tileLayer : ourTileLayers)
        {
            TileMatrixSet matrix = new TileMatrixSet();
            Contents contents = new Contents();
            contents.setDataType(ContentsDataType.TILES);

            matrix.setContents(contents);
            matrix.setSrs(new SpatialReferenceSystem());
            matrix.getSrs().setOrganization(ProjectionConstants.AUTHORITY_EPSG);
            matrix.getSrs().setOrganizationCoordsysId(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

            matrix.setBoundingBox(tileBoundingBox);
            MockTileDao tileDao = new MockTileDao(db, matrix, tileMatrices);
            EasyMock.expect(geopackage.getTileDao(EasyMock.cmpEq(tileLayer))).andReturn(tileDao);
        }

        Extensions extension1 = new Extensions();
        extension1.setTableName("table");
        extension1.setExtensionName("something");
        extension1.setDefinition("another");

        Extensions extension2 = new Extensions();
        extension2.setTableName("tile1");
        extension2.setExtensionName(Constants.TERRAIN_EXTENSION);
        extension2.setDefinition("mesh");

        MockExtensionsDao extensionsDao = new MockExtensionsDao(New.list(extension1, extension2), null);
        EasyMock.expect(geopackage.getExtensionsDao()).andReturn(extensionsDao).atLeastOnce();

        return geopackage;
    }

    /**
     * Creates an easy mocked {@link DataRegistry}.
     *
     * @param support Used to create the mock.
     * @param deposits The list to add the deposit to.
     * @return The mocked {@link DataRegistry}.
     */
    @SuppressWarnings("unchecked")
    private DataRegistry createRegistry(EasyMockSupport support, List<DefaultCacheDeposit<GeoPackageLayer>> deposits)
    {
        DataRegistry registry = support.createMock(DataRegistry.class);

        EasyMock.expect(registry.addModels(EasyMock.isA(DefaultCacheDeposit.class))).andAnswer(() -> addModelsAnswer(deposits));

        return registry;
    }
}
