package io.opensphere.geopackage.importer.tile;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.util.List;
import java.util.Observer;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.geopackage.importer.MockGeoPackageConnection;
import io.opensphere.geopackage.importer.MockTileDao;
import io.opensphere.geopackage.model.GeoPackageLayer;
import io.opensphere.geopackage.model.GeoPackageTileLayer;
import io.opensphere.geopackage.model.LayerType;
import io.opensphere.geopackage.model.ProgressModel;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDataType;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.sf.proj.ProjectionConstants;

/**
 * Unit test for {@link TileImporter} class.
 */
public class TileImporterTest
{
    /**
     * The test layer name.
     */
    private static final String ourTileLayerName = "testTile";

    /**
     * The previous completed count.
     */
    private int myPreviousCount;

    /**
     * Tests importing tiles.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();
        myPreviousCount = 0;
        GeoPackageTileLayer tileLayer = new GeoPackageTileLayer("package", "c:\\somefile.gpkg", ourTileLayerName, 4);
        GeoPackageLayer featureLayer = new GeoPackageLayer(tileLayer.getPackageName(), tileLayer.getPackageFile(), "testFeature",
                LayerType.FEATURE, 10);
        MockTileDao dao = createDao(support, 4);
        BoundingBox boundingBox = new BoundingBox(10, 10, 11, 11);

        TileRowImporter rowImporter = createImporter(support, tileLayer, dao, boundingBox,
                New.list(dao.getRows().get(0), dao.getRows().get(1)));

        GeoPackage geopackage = createGeoPackage(support, dao);

        CancellableTaskActivity ta = new CancellableTaskActivity();
        ProgressModel model = new ProgressModel();

        Observer observer = createObserver(support, 2, model, null);
        model.addObserver(observer);

        support.replayAll();

        TileImporter importer = new TileImporter(rowImporter);
        importer.importTiles(geopackage, New.list(featureLayer, tileLayer), ta, model);

        assertEquals(2, model.getCompletedCount());

        support.verifyAll();
    }

    /**
     * Tests importing tiles and the user cancels.
     */
    @Test
    public void testCancel()
    {
        EasyMockSupport support = new EasyMockSupport();
        myPreviousCount = 0;
        GeoPackageTileLayer tileLayer = new GeoPackageTileLayer("package", "c:\\somefile.gpkg", ourTileLayerName, 4);
        GeoPackageLayer featureLayer = new GeoPackageLayer(tileLayer.getPackageName(), tileLayer.getPackageFile(), "testFeature",
                LayerType.FEATURE, 10);
        MockTileDao dao = createDao(support, 4);
        BoundingBox boundingBox = new BoundingBox(10, 10, 11, 11);

        TileRowImporter rowImporter = createImporter(support, tileLayer, dao, boundingBox, New.list(dao.getRows().get(0)));

        GeoPackage geopackage = createGeoPackage(support, dao);

        CancellableTaskActivity ta = new CancellableTaskActivity();
        ProgressModel model = new ProgressModel();

        Observer observer = createObserver(support, 1, model, ta);
        model.addObserver(observer);

        support.replayAll();

        TileImporter importer = new TileImporter(rowImporter);
        importer.importTiles(geopackage, New.list(featureLayer, tileLayer), ta, model);

        assertEquals(model.getCompletedCount(), 1);

        support.verifyAll();
    }

    /**
     * Asserts the bounding box.
     *
     * @param expected The expected bounding box.
     * @return Null.
     */
    private Void assertBoundingBox(BoundingBox expected)
    {
        BoundingBox actual = (BoundingBox)EasyMock.getCurrentArguments()[2];

        assertEquals(expected.getMaxLatitude(), actual.getMaxLatitude(), 0.01d);
        assertEquals(expected.getMinLatitude(), actual.getMinLatitude(), 0.01d);
        assertEquals(expected.getMaxLongitude(), actual.getMaxLongitude(), 0.01d);
        assertEquals(expected.getMaxLongitude(), actual.getMaxLongitude(), 0.01d);

        return null;
    }

    /**
     * Asserts the completed count in the model.
     *
     * @param model The model to assert.
     * @param ta The {@link CancellableTaskActivity} to cancel, or null if this
     *            is not a cancel test.
     * @return Null.
     */
    private Void assertCompletedCount(ProgressModel model, CancellableTaskActivity ta)
    {
        assertEquals(myPreviousCount, model.getCompletedCount() - 1);
        myPreviousCount = model.getCompletedCount();
        if (ta != null)
        {
            ta.setCancelled(true);
        }
        return null;
    }

    /**
     * Creates a mocked {@link TileDao}.
     *
     * @param support Used to create mocks.
     * @param numberOfRows The number of tiles.
     * @return The mocked tile dao.
     */
    private MockTileDao createDao(EasyMockSupport support, int numberOfRows)
    {
        MockGeoPackageConnection db = new MockGeoPackageConnection(support.createMock(Connection.class));
        TileMatrixSet matrixSet = new TileMatrixSet();
        Contents contents = new Contents();
        contents.setDataType(ContentsDataType.TILES);

        matrixSet.setContents(contents);
        matrixSet.setSrs(new SpatialReferenceSystem());
        matrixSet.getSrs().setOrganization(ProjectionConstants.AUTHORITY_EPSG);
        matrixSet.getSrs().setOrganizationCoordsysId(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
        matrixSet.getSrs().setOrganizationCoordsysId(ProjectionConstants.EPSG_WEB_MERCATOR);
        matrixSet.setBoundingBox(new BoundingBox(10, 10, 11, 11));

        TileMatrix matrix = new TileMatrix();
        matrix.setZoomLevel(8);

        MockTileDao dao = new MockTileDao(db, matrixSet, New.list(matrix), numberOfRows);

        return dao;
    }

    /**
     * Creates a mocked {@link GeoPackage}.
     *
     * @param support used to create the mock.
     * @param dao The dao the mock should return.
     * @return The mocked {@link GeoPackage}.
     */
    private GeoPackage createGeoPackage(EasyMockSupport support, TileDao dao)
    {
        GeoPackage geoPackage = support.createMock(GeoPackage.class);

        EasyMock.expect(geoPackage.getTileDao(EasyMock.cmpEq(ourTileLayerName))).andReturn(dao);

        return geoPackage;
    }

    /**
     * Creates an easy mocked {@link TileRowImporter}.
     *
     * @param support Used to create the mock.
     * @param layer The tile layer.
     * @param tileDao The tile dao.
     * @param webMercatorBox The bounding box of the layer.
     * @param rows The rows to expect to be imported.
     * @return The mocked {@link TileRowImporter}.
     */
    private TileRowImporter createImporter(EasyMockSupport support, GeoPackageTileLayer layer, TileDao tileDao,
            BoundingBox webMercatorBox, List<TileRow> rows)
    {
        TileRowImporter importer = support.createMock(TileRowImporter.class);

        for (TileRow row : rows)
        {
            importer.importTile(EasyMock.eq(layer), EasyMock.eq(tileDao), EasyMock.isA(BoundingBox.class), EasyMock.eq(row));
            EasyMock.expectLastCall().andAnswer(() -> assertBoundingBox(webMercatorBox));
        }

        return importer;
    }

    /**
     * Creates a mocked {@link Observer}.
     *
     * @param support Used to create the mock.
     * @param expectedTimes The number of time to expect to be called.
     * @param model The import model to assert.
     * @param ta The {@link CancellableTaskActivity} to cancel, or null if this
     *            is not a cancel test.
     * @return The mocked Observer.
     */
    private Observer createObserver(EasyMockSupport support, int expectedTimes, ProgressModel model, CancellableTaskActivity ta)
    {
        Observer observer = support.createMock(Observer.class);

        observer.update(EasyMock.isA(ProgressModel.class), EasyMock.cmpEq(ProgressModel.COMPLETED_COUNT_PROP));
        EasyMock.expectLastCall().andAnswer(() -> assertCompletedCount(model, ta)).times(expectedTimes);

        return observer;
    }
}
