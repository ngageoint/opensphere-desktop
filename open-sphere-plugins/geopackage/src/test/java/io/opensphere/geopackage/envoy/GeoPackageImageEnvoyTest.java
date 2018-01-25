package io.opensphere.geopackage.envoy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.InputStreamAccessor;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.matcher.ZYXKeyPropertyMatcher;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.image.Image;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.importer.MockGeoPackageConnection;
import io.opensphere.geopackage.importer.MockTileDao;
import io.opensphere.geopackage.model.GeoPackagePropertyDescriptors;
import io.opensphere.geopackage.model.GeoPackageTile;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDataType;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;

/**
 * Unit test for {@link GeoPackageImageEnvoy}.
 */
public class GeoPackageImageEnvoyTest
{
    /**
     * The test package file.
     */
    private static final String ourPackageFile = "c:\\somefile.gpkg";

    /**
     * Our test bounding box.
     */
    private static final GeographicBoundingBox ourTileBoundingBox = new GeographicBoundingBox(LatLonAlt.createFromDegrees(10, 10),
            LatLonAlt.createFromDegrees(10.5, 10.5));

    /**
     * The test column value for the tile.
     */
    private static final int ourX = 0;

    /**
     * The test row value for the tile.
     */
    private static final int ourY = 1;

    /**
     * The test layer name.
     */
    private static final String ourTileLayerName = "testTile";

    /**
     * Our test zoom level.
     */
    private static final int ourZoomLevel = 1;

    /**
     * Tests closing the envoy.
     */
    @Test
    public void testClose()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = support.createMock(Toolbox.class);
        GeoPackage geopackage = support.createMock(GeoPackage.class);
        geopackage.close();

        support.replayAll();

        GeoPackageImageEnvoy envoy = new GeoPackageImageEnvoy(toolbox, geopackage);
        envoy.close();

        support.verifyAll();
    }

    /**
     * Tests getting the thread pool name.
     */
    @Test
    public void testGetThreadPoolName()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = support.createMock(Toolbox.class);
        GeoPackage geopackage = support.createMock(GeoPackage.class);

        support.replayAll();

        GeoPackageImageEnvoy envoy = new GeoPackageImageEnvoy(toolbox, geopackage);
        assertEquals(GeoPackageImageEnvoy.class.getSimpleName(), envoy.getThreadPoolName());

        support.verifyAll();
    }

    /**
     * Tests provides data for.
     */
    @Test
    public void testProvidesDataFor()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = support.createMock(Toolbox.class);
        GeoPackage geopackage = support.createMock(GeoPackage.class);
        EasyMock.expect(geopackage.getPath()).andReturn(ourPackageFile).atLeastOnce();

        support.replayAll();

        GeoPackageImageEnvoy envoy = new GeoPackageImageEnvoy(toolbox, geopackage);

        DataModelCategory yes = new DataModelCategory(ourPackageFile, ourTileLayerName, Image.class.getName());
        DataModelCategory no = new DataModelCategory(ourPackageFile, null, Image.class.getName());
        DataModelCategory no1 = new DataModelCategory("something else", ourTileLayerName, Image.class.getName());
        DataModelCategory no2 = new DataModelCategory(ourPackageFile, ourTileLayerName, GeoPackageTile.class.getName());
        DataModelCategory no3 = new DataModelCategory(null, ourTileLayerName, Image.class.getName());
        DataModelCategory no4 = new DataModelCategory(ourPackageFile, ourTileLayerName, null);

        assertTrue(envoy.providesDataFor(yes));
        assertFalse(envoy.providesDataFor(no));
        assertFalse(envoy.providesDataFor(no1));
        assertFalse(envoy.providesDataFor(no2));
        assertFalse(envoy.providesDataFor(no3));
        assertFalse(envoy.providesDataFor(no4));

        support.verifyAll();
    }

    /**
     * Tests querying the geopackage.
     *
     * @throws QueryException Bad query.
     * @throws InterruptedException Bad Interrupted.
     * @throws CacheException Bad Cache.
     */
    @Test
    public void testQuery() throws InterruptedException, QueryException, CacheException
    {
        EasyMockSupport support = new EasyMockSupport();

        TileDao dao = createTileDao(support);
        GeoPackage geopackage = createGeoPackage(support, dao);
        Toolbox toolbox = support.createMock(Toolbox.class);
        CacheDepositReceiver receiver = createReceiver(support);

        support.replayAll();

        GeoPackageImageEnvoy envoy = new GeoPackageImageEnvoy(toolbox, geopackage);

        ZYXImageKey key = new ZYXImageKey(ourZoomLevel, ourY, ourX, ourTileBoundingBox);
        DataModelCategory category = new DataModelCategory(ourPackageFile, ourTileLayerName, Image.class.getName());
        List<PropertyMatcher<?>> matchers = New.list();
        ZYXKeyPropertyMatcher matcher = new ZYXKeyPropertyMatcher(GeoPackagePropertyDescriptors.KEY_PROPERTY_DESCRIPTOR, key);
        matchers.add(matcher);

        envoy.query(category, Collections.emptyList(), matchers, Collections.emptyList(), -1,
                New.list(GeoPackagePropertyDescriptors.IMAGE_PROPERTY_DESCRIPTOR), receiver);

        support.verifyAll();
    }

    /**
     * Asserts the deposit for the image data.
     *
     * @return The model id.
     * @throws IOException Bad IO.
     */
    @SuppressWarnings("unchecked")
    private long[] assertImageDeposit() throws IOException
    {
        DefaultCacheDeposit<InputStream> deposit = (DefaultCacheDeposit<InputStream>)EasyMock.getCurrentArguments()[0];

        DataModelCategory category = deposit.getCategory();
        assertEquals(new DataModelCategory(ourPackageFile, ourTileLayerName, Image.class.getName()), category);

        Collection<PropertyAccessor<InputStream, ?>> accessors = (Collection<PropertyAccessor<InputStream, ?>>)deposit
                .getAccessors();
        assertEquals(2, accessors.size());

        Iterator<PropertyAccessor<InputStream, ?>> iterator = accessors.iterator();

        PropertyAccessor<InputStream, String> keyAccessor = (PropertyAccessor<InputStream, String>)iterator.next();
        assertTrue(keyAccessor instanceof SerializableAccessor);
        assertEquals(GeoPackagePropertyDescriptors.KEY_PROPERTY_DESCRIPTOR, keyAccessor.getPropertyDescriptor());
        assertEquals(new ZYXImageKey(ourZoomLevel, ourY, ourX, ourTileBoundingBox).toString(), keyAccessor.access(null));

        PropertyAccessor<InputStream, InputStream> tileAccessor = (PropertyAccessor<InputStream, InputStream>)iterator.next();
        assertTrue(tileAccessor instanceof InputStreamAccessor);
        assertEquals(GeoPackagePropertyDescriptors.IMAGE_PROPERTY_DESCRIPTOR, tileAccessor.getPropertyDescriptor());

        assertEquals(CacheDeposit.SESSION_END, deposit.getExpirationDate());

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
     * Creates an easy mocked {@link CacheDepositReceiver}.
     *
     * @param support Used to create the mock.
     * @return The mocked receiver.
     * @throws CacheException Bad cache.
     */
    @SuppressWarnings("unchecked")
    private CacheDepositReceiver createReceiver(EasyMockSupport support) throws CacheException
    {
        CacheDepositReceiver receiver = support.createMock(CacheDepositReceiver.class);

        EasyMock.expect(receiver.receive(EasyMock.isA(DefaultCacheDeposit.class))).andAnswer(this::assertImageDeposit);

        return receiver;
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

        TileMatrix matrix = new TileMatrix();
        matrix.setZoomLevel(ourZoomLevel);
        matrix.setTileWidth(1);
        matrix.setTileHeight(1);
        matrix.setMatrixWidth(2);
        matrix.setMatrixHeight(2);
        matrix.setPixelXSize(100);
        matrix.setPixelYSize(100);

        MockTileDao dao = new MockTileDao(db, matrixSet, New.list(matrix));

        return dao;
    }
}
