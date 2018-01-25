package io.opensphere.geopackage.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.cache.matcher.NumberPropertyMatcher;
import io.opensphere.core.cache.matcher.NumberPropertyMatcher.OperatorType;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.PropertyValueReceiver;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.AbstractTileGeometry.Divider;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.model.GeoPackagePropertyDescriptors;
import io.opensphere.geopackage.model.GeoPackageTile;
import io.opensphere.geopackage.model.GeoPackageTileLayer;

/**
 * Unit test for {@link GeoPackageGeometryBuilder}.
 */
public class GeoPackageGeometryBuilderTest
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
     * The test zoom level.
     */
    private static final long ourZoomLevel = 8;

    /**
     * Tests building geometries when tiles are present.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        String layerId = "theId";

        GeographicBoundingBox boundingBox1 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(10, 10),
                LatLonAlt.createFromDegrees(11, 11));
        GeoPackageTile tile1 = new GeoPackageTile(layerId, ourZoomLevel, boundingBox1, 0, 1);

        GeographicBoundingBox boundingBox2 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(11, 11),
                LatLonAlt.createFromDegrees(12, 12));
        GeoPackageTile tile2 = new GeoPackageTile(layerId, ourZoomLevel, boundingBox2, 1, 0);

        DataRegistry registry = createRegistry(support, New.list(tile1, tile2));
        UIRegistry uiRegistry = createUIRegistry(support);

        GeoPackageTileLayer layer = new GeoPackageTileLayer("thePackage", ourPackageFile, ourLayerName, 2);
        layer.setMaxZoomLevel(22);

        TileRenderProperties props = createProps(support);
        Divider<GeographicPosition> divider = createDivider(support);

        support.replayAll();

        GeoPackageGeometryBuilder builder = new GeoPackageGeometryBuilder(registry, uiRegistry);
        List<AbstractTileGeometry<?>> geometries = builder.buildGeometries(layer, ourZoomLevel, props, divider);

        assertEquals(2, geometries.size());
        assertGeometry(geometries.get(0), tile1, props, divider);
        assertGeometry(geometries.get(1), tile2, props, divider);

        support.verifyAll();
    }

    /**
     * Tests building geometries when there are no tiles.
     */
    @Test
    public void testNoTiles()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry registry = createRegistry(support, New.list());
        UIRegistry uiRegistry = createUIRegistry(support);

        GeoPackageTileLayer layer = new GeoPackageTileLayer("thePackage", ourPackageFile, ourLayerName, 2);

        TileRenderProperties props = createProps(support);
        Divider<GeographicPosition> divider = createDivider(support);

        support.replayAll();

        GeoPackageGeometryBuilder builder = new GeoPackageGeometryBuilder(registry, uiRegistry);
        List<AbstractTileGeometry<?>> geometries = builder.buildGeometries(layer, ourZoomLevel, props, divider);

        assertTrue(geometries.isEmpty());

        support.verifyAll();
    }

    /**
     * Tests building geometries when tiles are present.
     */
    @Test
    public void testOneZoomLevel()
    {
        EasyMockSupport support = new EasyMockSupport();

        String layerId = "theId";

        GeographicBoundingBox boundingBox1 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(10, 10),
                LatLonAlt.createFromDegrees(11, 11));
        GeoPackageTile tile1 = new GeoPackageTile(layerId, ourZoomLevel, boundingBox1, 0, 1);

        GeographicBoundingBox boundingBox2 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(11, 11),
                LatLonAlt.createFromDegrees(12, 12));
        GeoPackageTile tile2 = new GeoPackageTile(layerId, ourZoomLevel, boundingBox2, 1, 0);

        DataRegistry registry = createRegistry(support, New.list(tile1, tile2));
        UIRegistry uiRegistry = createUIRegistry(support);

        GeoPackageTileLayer layer = new GeoPackageTileLayer("thePackage", ourPackageFile, ourLayerName, 2);
        layer.setMinZoomLevel(ourZoomLevel);
        layer.setMaxZoomLevel(ourZoomLevel);

        TileRenderProperties props = createProps(support);
        Divider<GeographicPosition> divider = createDivider(support);

        support.replayAll();

        GeoPackageGeometryBuilder builder = new GeoPackageGeometryBuilder(registry, uiRegistry);
        List<AbstractTileGeometry<?>> geometries = builder.buildGeometries(layer, ourZoomLevel, props, divider);

        assertEquals(2, geometries.size());
        assertGeometry(geometries.get(0), tile1, props, null);
        assertGeometry(geometries.get(1), tile2, props, null);

        support.verifyAll();
    }

    /**
     * Asserts the built geometries.
     *
     * @param geometry The geometry to assert.
     * @param tile The tile the geometry represents.
     * @param props The expected render properties.
     * @param divider The expected divider.
     */
    private void assertGeometry(AbstractTileGeometry<?> geometry, GeoPackageTile tile, TileRenderProperties props,
            Divider<GeographicPosition> divider)
    {
        assertEquals(tile.getBoundingBox(), geometry.getBounds());
        assertTrue(geometry.getImageManager().getImageProvider() instanceof GeoPackageImageProvider);
        assertEquals(props, geometry.getRenderProperties());
        assertEquals(divider, geometry.getSplitJoinRequestProvider());
        assertEquals(384, geometry.getMinimumDisplaySize());
        assertEquals(1280, geometry.getMaximumDisplaySize());
    }

    /**
     * Creates an easy mocked {@link Divider}.
     *
     * @param support Used to create the mock.
     * @return The mocked divider.
     */
    private Divider<GeographicPosition> createDivider(EasyMockSupport support)
    {
        @SuppressWarnings("unchecked")
        Divider<GeographicPosition> divider = support.createMock(Divider.class);

        return divider;
    }

    /**
     * Creates an easy mocked {@link TileRenderProperties}.
     *
     * @param support Used to create the mock.
     * @return The mocked properties.
     */
    private TileRenderProperties createProps(EasyMockSupport support)
    {
        TileRenderProperties props = support.createMock(TileRenderProperties.class);

        EasyMock.expect(Boolean.valueOf(props.isDrawable())).andReturn(Boolean.TRUE).anyTimes();

        return props;
    }

    /**
     * Creates an easy mocked {@link DataRegistry}.
     *
     * @param support Used to crete the mock.
     * @param tilesToReturn The tiles to return in the mocked query call.
     * @return The mocked {@link DataRegistry}.
     */
    private DataRegistry createRegistry(EasyMockSupport support, List<GeoPackageTile> tilesToReturn)
    {
        DataRegistry registry = support.createMock(DataRegistry.class);
        QueryTracker tracker = support.createMock(QueryTracker.class);

        EasyMock.expect(registry.performQuery(EasyMock.isA(SimpleQuery.class)))
                .andAnswer(() -> queryAnswer(tracker, tilesToReturn));

        return registry;
    }

    /**
     * Creates an easy mocked {@link UIRegistry}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link UIRegistry}.
     */
    private UIRegistry createUIRegistry(EasyMockSupport support)
    {
        UIRegistry uiRegistry = support.createMock(UIRegistry.class);

        return uiRegistry;
    }

    /**
     * The answer to the mocked query call.
     *
     * @param tracker The mocked {@link QueryTracker} to return.
     * @param tilesToReturn The tiles to return to the query.
     * @return The passed in tracker.
     */
    @SuppressWarnings("unchecked")
    private QueryTracker queryAnswer(QueryTracker tracker, List<GeoPackageTile> tilesToReturn)
    {
        SimpleQuery<GeoPackageTile> query = (SimpleQuery<GeoPackageTile>)EasyMock.getCurrentArguments()[0];

        DataModelCategory actual = query.getDataModelCategory();
        DataModelCategory expected = new DataModelCategory(ourPackageFile, ourLayerName, GeoPackageTile.class.getName());

        assertEquals(expected, actual);

        List<PropertyMatcher<?>> matchers = (List<PropertyMatcher<?>>)query.getParameters();
        assertEquals(1, matchers.size());

        NumberPropertyMatcher<Long> zoomLevelMatcher = (NumberPropertyMatcher<Long>)matchers.get(0);
        assertEquals(ourZoomLevel, zoomLevelMatcher.getOperand().longValue());
        assertEquals(GeoPackagePropertyDescriptors.ZOOM_LEVEL_PROPERTY_DESCRIPTOR, zoomLevelMatcher.getPropertyDescriptor());
        assertEquals(OperatorType.EQ, zoomLevelMatcher.getOperator());

        List<PropertyValueReceiver<?>> receivers = (List<PropertyValueReceiver<?>>)query.getPropertyValueReceivers();
        assertEquals(1, receivers.size());

        PropertyValueReceiver<GeoPackageTile> receiver = (PropertyValueReceiver<GeoPackageTile>)receivers.get(0);

        assertEquals(GeoPackagePropertyDescriptors.GEOPACKAGE_TILE_PROPERTY_DESCRIPTOR, receiver.getPropertyDescriptor());

        receiver.receive(tilesToReturn);

        return tracker;
    }
}
