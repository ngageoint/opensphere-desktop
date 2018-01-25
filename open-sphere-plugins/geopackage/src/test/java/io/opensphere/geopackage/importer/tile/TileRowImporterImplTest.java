package io.opensphere.geopackage.importer.tile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.InputStreamAccessor;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.image.Image;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.importer.MockGeoPackageConnection;
import io.opensphere.geopackage.importer.MockTileDao;
import io.opensphere.geopackage.model.GeoPackagePropertyDescriptors;
import io.opensphere.geopackage.model.GeoPackageTile;
import io.opensphere.geopackage.model.GeoPackageTileLayer;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDataType;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Unit test for the {@link TileRowImporterImpl} class.
 */
public class TileRowImporterImplTest
{
    /**
     * The test layer name.
     */
    private static final String ourLayerName = "testLayer";

    /**
     * The test package file.
     */
    private static final String ourPackageFile = "c:\\somefile.gpkg";

    /**
     * The test package name.
     */
    private static final String ourPackageName = "testPackage";

    /**
     * The test bounding box for the test tile.
     */
    private static final GeographicBoundingBox ourTileBoundingBox = new GeographicBoundingBox(
            new GeographicPosition(LatLonAlt.createFromDegrees(10d, 10d)),
            new GeographicPosition(LatLonAlt.createFromDegrees(11d, 11d)));

    /**
     * The test zoom level for the tile.
     */
    private static final long ourZoomLevel = 10;

    /**
     * Tests importing a tile and depositing it into the data registry.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        GeoPackageTileLayer layer = createLayer();
        DataRegistry registry = createDataRegistry(support, layer.getId());
        TileDao tileDao = createTileDao(support);
        BoundingBox boundingBox = createBoundBox();
        TileRow row = createTileRow(tileDao);

        support.replayAll();

        TileRowImporterImpl importer = new TileRowImporterImpl(registry);
        importer.importTile(layer, tileDao, boundingBox, row);

        support.verifyAll();
    }

    /**
     * Asserts the deposit for the image data.
     *
     * @param layerId The expected layer id.
     * @return The model id.
     * @throws IOException Bad IO.
     */
    @SuppressWarnings("unchecked")
    private long[] assertImageDeposit(String layerId) throws IOException
    {
        DefaultCacheDeposit<InputStream> deposit = (DefaultCacheDeposit<InputStream>)EasyMock.getCurrentArguments()[0];

        DataModelCategory category = deposit.getCategory();
        assertEquals(new DataModelCategory(ourPackageFile, ourLayerName, Image.class.getName()), category);

        Collection<PropertyAccessor<InputStream, ?>> accessors = (Collection<PropertyAccessor<InputStream, ?>>)deposit
                .getAccessors();
        assertEquals(2, accessors.size());

        Iterator<PropertyAccessor<InputStream, ?>> iterator = accessors.iterator();

        PropertyAccessor<InputStream, String> keyAccessor = (PropertyAccessor<InputStream, String>)iterator.next();
        assertTrue(keyAccessor instanceof SerializableAccessor);
        assertEquals(GeoPackagePropertyDescriptors.KEY_PROPERTY_DESCRIPTOR, keyAccessor.getPropertyDescriptor());
        assertEquals(ourZoomLevel + "|" + ourTileBoundingBox.toSimpleString(), keyAccessor.access(null));

        PropertyAccessor<InputStream, InputStream> tileAccessor = (PropertyAccessor<InputStream, InputStream>)iterator.next();
        assertTrue(tileAccessor instanceof InputStreamAccessor);
        assertEquals(GeoPackagePropertyDescriptors.IMAGE_PROPERTY_DESCRIPTOR, tileAccessor.getPropertyDescriptor());

        assertEquals(new Date(Long.MAX_VALUE), deposit.getExpirationDate());

        assertTrue(deposit.isNew());
        assertFalse(deposit.isCritical());

        InputStream image = deposit.getInput().iterator().next();

        assertEquals(4, image.available());
        byte[] imageData = new byte[4];
        image.read(imageData);

        assertEquals(1, imageData[0]);
        assertEquals(2, imageData[1]);
        assertEquals(3, imageData[2]);
        assertEquals(4, imageData[3]);

        return new long[] { 1 };
    }

    /**
     * Asserts the tile deposit.
     *
     * @param layerId The layer id.
     * @return The model id.
     */
    @SuppressWarnings("unchecked")
    private long[] assertTileDeposit(String layerId)
    {
        DefaultCacheDeposit<GeoPackageTile> deposit = (DefaultCacheDeposit<GeoPackageTile>)EasyMock.getCurrentArguments()[0];

        DataModelCategory category = deposit.getCategory();
        assertEquals(new DataModelCategory(ourPackageFile, ourLayerName, GeoPackageTile.class.getName()), category);

        Collection<PropertyAccessor<GeoPackageTile, ?>> accessors = (Collection<PropertyAccessor<GeoPackageTile, ?>>)deposit
                .getAccessors();
        assertEquals(3, accessors.size());

        Iterator<PropertyAccessor<GeoPackageTile, ?>> iterator = accessors.iterator();

        PropertyAccessor<GeoPackageTile, String> keyAccessor = (PropertyAccessor<GeoPackageTile, String>)iterator.next();
        assertTrue(keyAccessor instanceof SerializableAccessor);
        assertEquals(GeoPackagePropertyDescriptors.KEY_PROPERTY_DESCRIPTOR, keyAccessor.getPropertyDescriptor());
        assertEquals(ourZoomLevel + "|" + ourTileBoundingBox.toSimpleString(), keyAccessor.access(null));

        PropertyAccessor<GeoPackageTile, GeoPackageTile> tileAccessor = (PropertyAccessor<GeoPackageTile, GeoPackageTile>)iterator
                .next();
        assertTrue(tileAccessor instanceof SerializableAccessor);
        assertEquals(GeoPackagePropertyDescriptors.GEOPACKAGE_TILE_PROPERTY_DESCRIPTOR, tileAccessor.getPropertyDescriptor());

        PropertyAccessor<GeoPackageTile, Long> zoomLevelAccessor = (PropertyAccessor<GeoPackageTile, Long>)iterator.next();
        assertTrue(zoomLevelAccessor instanceof SerializableAccessor);
        assertEquals(GeoPackagePropertyDescriptors.ZOOM_LEVEL_PROPERTY_DESCRIPTOR, zoomLevelAccessor.getPropertyDescriptor());
        assertEquals(ourZoomLevel, zoomLevelAccessor.access(null).longValue());

        assertEquals(new Date(Long.MAX_VALUE), deposit.getExpirationDate());

        assertTrue(deposit.isNew());
        assertFalse(deposit.isCritical());

        GeoPackageTile tile = deposit.getInput().iterator().next();
        assertEquals(ourTileBoundingBox, tile.getBoundingBox());
        assertEquals(layerId, tile.getLayerId());
        assertEquals(ourZoomLevel, tile.getZoomLevel());

        return new long[] { 1 };
    }

    /**
     * Creates a bounding box for the entire package.
     *
     * @return The package bounding box.
     */
    private BoundingBox createBoundBox()
    {
        BoundingBox boundingBox = new BoundingBox(10, 11, 10, 11);

        Projection geodetic = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
        ProjectionTransform transform = geodetic.getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
        boundingBox = transform.transform(boundingBox);

        return boundingBox;
    }

    /**
     * Creates a mocked data registry.
     *
     * @param support Used to create the mock.
     * @param layerId The expected layer id.
     * @return The mocked data registry.
     */
    @SuppressWarnings("unchecked")
    private DataRegistry createDataRegistry(EasyMockSupport support, String layerId)
    {
        DataRegistry registry = support.createMock(DataRegistry.class);

        EasyMock.expect(registry.addModels(EasyMock.isA(DefaultCacheDeposit.class))).andAnswer(() -> assertTileDeposit(layerId));
        EasyMock.expect(registry.addModels(EasyMock.isA(DefaultCacheDeposit.class))).andAnswer(() -> assertImageDeposit(layerId));

        return registry;
    }

    /**
     * Creates a test {@link GeoPackageTileLayer}.
     *
     * @return The test layer.
     */
    private GeoPackageTileLayer createLayer()
    {
        GeoPackageTileLayer tileLayer = new GeoPackageTileLayer(ourPackageName, ourPackageFile, ourLayerName, 1);

        return tileLayer;
    }

    /**
     * Creates a mocked {@link TileDao} to use.
     *
     * @param support Used to create a mocked {@link Connection}.
     * @return The mocked tile dao.
     */
    private TileDao createTileDao(EasyMockSupport support)
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

        TileMatrix matrix = new TileMatrix();
        matrix.setZoomLevel(ourZoomLevel);
        matrix.setTileWidth(1);
        matrix.setTileHeight(1);
        matrix.setMatrixWidth(1);
        matrix.setMatrixHeight(1);
        matrix.setPixelXSize(100);
        matrix.setPixelYSize(100);

        MockTileDao dao = new MockTileDao(db, matrixSet, New.list(matrix));

        return dao;
    }

    /**
     * Creates a mock tile row to use.
     *
     * @param mockDao The mocked dao.
     * @return The tile row.
     */
    private TileRow createTileRow(TileDao mockDao)
    {
        TileRow row = mockDao.newRow();
        row.setTileRow(0);
        row.setTileColumn(0);
        row.setZoomLevel(ourZoomLevel);
        row.setTileData(new byte[] { 1, 2, 3, 4 });

        return row;
    }
}
