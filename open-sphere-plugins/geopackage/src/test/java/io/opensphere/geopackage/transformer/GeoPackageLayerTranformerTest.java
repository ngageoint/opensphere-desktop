package io.opensphere.geopackage.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.PropertyValueReceiver;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.order.impl.DefaultOrderParticipantKey;
import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.mantle.GeoPackageDataTypeInfo;
import io.opensphere.geopackage.model.GeoPackageTile;
import io.opensphere.geopackage.model.GeoPackageTileLayer;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.impl.DefaultMapTileVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultTileLevelController;

/**
 * Unit test for {@link GeoPackageLayerTransformer}.
 */
public class GeoPackageLayerTranformerTest
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
     * Tests activating and deactivating a layer.
     */
    @Test
    public void testLayerActivated()
    {
        EasyMockSupport support = new EasyMockSupport();

        String layerId = "theId";

        GeographicBoundingBox boundingBox1 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(10, 10),
                LatLonAlt.createFromDegrees(11, 11));
        GeoPackageTile tile1 = new GeoPackageTile(layerId, ourZoomLevel, boundingBox1, 0, 1);

        GeographicBoundingBox boundingBox2 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(11, 11),
                LatLonAlt.createFromDegrees(12, 12));
        GeoPackageTile tile2 = new GeoPackageTile(layerId, ourZoomLevel, boundingBox2, 1, 0);

        List<GeoPackageTile> tiles = New.list(tile1, tile2);
        DataRegistry registry = createRegistry(support, tiles);

        List<Geometry> publishedGeometries = New.list();
        GenericSubscriber<Geometry> subscriber = createSubscriber(support, publishedGeometries);

        GeoPackageDataTypeInfo layer = createLayer(layerId);

        UIRegistry uiRegistry = createUIRegistry(support);

        support.replayAll();

        GeoPackageLayerTransformer transformer = new GeoPackageLayerTransformer(registry, uiRegistry);
        transformer.addSubscriber(subscriber);
        transformer.open();
        transformer.layerActivated(layer);

        assertEquals(GeographicBoundingBox.merge(boundingBox1, boundingBox2), layer.getBoundingBox());
        assertEquals(2, publishedGeometries.size());

        TileGeometry geom1 = (TileGeometry)publishedGeometries.get(0);
        assertEquals(boundingBox1, geom1.getBounds());
        assertEquals(layer.getMapVisualizationInfo().getTileRenderProperties(), geom1.getRenderProperties());
        assertEquals(0, geom1.getGeneration());

        TileGeometry geom2 = (TileGeometry)publishedGeometries.get(1);
        assertEquals(boundingBox2, geom2.getBounds());
        assertEquals(layer.getMapVisualizationInfo().getTileRenderProperties(), geom2.getRenderProperties());
        assertEquals(0, geom2.getGeneration());

        DefaultTileLevelController levelController = (DefaultTileLevelController)layer.getMapVisualizationInfo()
                .getTileLevelController();

        assertEquals(7, levelController.getMaxGeneration());

        transformer.layerDeactivated(layer);

        assertTrue(publishedGeometries.isEmpty());

        support.verifyAll();
    }

    /**
     * Tests activating and deactivating a layer.
     */
    @Test
    public void testLayerActivatedBeforeOpen()
    {
        EasyMockSupport support = new EasyMockSupport();

        String layerId = "theId";

        GeographicBoundingBox boundingBox1 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(10, 10),
                LatLonAlt.createFromDegrees(11, 11));
        GeoPackageTile tile1 = new GeoPackageTile(layerId, ourZoomLevel, boundingBox1, 0, 1);

        GeographicBoundingBox boundingBox2 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(11, 11),
                LatLonAlt.createFromDegrees(12, 12));
        GeoPackageTile tile2 = new GeoPackageTile(layerId, ourZoomLevel, boundingBox2, 1, 0);

        List<GeoPackageTile> tiles = New.list(tile1, tile2);
        DataRegistry registry = createRegistry(support, tiles);

        List<Geometry> publishedGeometries = New.list();
        GenericSubscriber<Geometry> subscriber = createSubscriber(support, publishedGeometries);

        GeoPackageDataTypeInfo layer = createLayer(layerId);

        UIRegistry uiRegistry = createUIRegistry(support);

        support.replayAll();

        GeoPackageLayerTransformer transformer = new GeoPackageLayerTransformer(registry, uiRegistry);
        transformer.addSubscriber(subscriber);
        transformer.layerActivated(layer);
        transformer.open();

        assertEquals(GeographicBoundingBox.merge(boundingBox1, boundingBox2), layer.getBoundingBox());
        assertEquals(2, publishedGeometries.size());

        TileGeometry geom1 = (TileGeometry)publishedGeometries.get(0);
        assertEquals(boundingBox1, geom1.getBounds());
        assertEquals(layer.getMapVisualizationInfo().getTileRenderProperties(), geom1.getRenderProperties());
        assertEquals(0, geom1.getGeneration());

        TileGeometry geom2 = (TileGeometry)publishedGeometries.get(1);
        assertEquals(boundingBox2, geom2.getBounds());
        assertEquals(layer.getMapVisualizationInfo().getTileRenderProperties(), geom2.getRenderProperties());
        assertEquals(0, geom2.getGeneration());

        DefaultTileLevelController levelController = (DefaultTileLevelController)layer.getMapVisualizationInfo()
                .getTileLevelController();

        assertEquals(7, levelController.getMaxGeneration());

        transformer.layerDeactivated(layer);

        assertTrue(publishedGeometries.isEmpty());

        support.verifyAll();
    }

    /**
     * Creates a test layer.
     *
     * @param layerId The layer id.
     * @return The test layer.
     */
    private GeoPackageDataTypeInfo createLayer(String layerId)
    {
        GeoPackageTileLayer layer = new GeoPackageTileLayer("thePackage", ourPackageFile, ourLayerName, 2);
        layer.setMinZoomLevel(8);
        layer.setMaxZoomLevel(15);
        GeoPackageDataTypeInfo dataType = new GeoPackageDataTypeInfo(null, layer, layerId);
        DefaultOrderParticipantKey orderKey = new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY,
                DefaultOrderCategory.IMAGE_OVERLAY_CATEGORY, layerId);
        dataType.setOrderKey(orderKey);
        TileRenderProperties props = new DefaultTileRenderProperties(10, true, false);

        props.setOpacity(255);
        DefaultMapTileVisualizationInfo mapVisInfo = new DefaultMapTileVisualizationInfo(MapVisualizationType.IMAGE_TILE, props,
                true);
        mapVisInfo.setTileLevelController(new DefaultTileLevelController());
        dataType.setMapVisualizationInfo(mapVisInfo);

        return dataType;
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
     * Creates an easy mocked subscriber.
     *
     * @param support Used to create the mock.
     * @param publishedGeometries The list to add/remove published geometries to
     *            and from.
     * @return The mocked subscriber.
     */
    private GenericSubscriber<Geometry> createSubscriber(EasyMockSupport support, List<Geometry> publishedGeometries)
    {
        @SuppressWarnings("unchecked")
        GenericSubscriber<Geometry> subscriber = support.createMock(GenericSubscriber.class);

        subscriber.receiveObjects(EasyMock.isA(Object.class), EasyMock.notNull(), EasyMock.notNull());
        EasyMock.expectLastCall().andAnswer(() -> receiveAnswer(publishedGeometries)).times(2);

        return subscriber;
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

        List<PropertyValueReceiver<?>> receivers = (List<PropertyValueReceiver<?>>)query.getPropertyValueReceivers();
        assertEquals(1, receivers.size());

        PropertyValueReceiver<GeoPackageTile> receiver = (PropertyValueReceiver<GeoPackageTile>)receivers.get(0);

        receiver.receive(tilesToReturn);

        return tracker;
    }

    /**
     * The answer for the generic subscriber.
     *
     * @param publishedGeometries The list to add/remove published geometries to
     *            and from.
     * @return Null.
     */
    @SuppressWarnings("unchecked")
    private Void receiveAnswer(List<Geometry> publishedGeometries)
    {
        Collection<Geometry> adds = (Collection<Geometry>)EasyMock.getCurrentArguments()[1];
        Collection<Geometry> removes = (Collection<Geometry>)EasyMock.getCurrentArguments()[2];
        publishedGeometries.addAll(adds);
        publishedGeometries.removeAll(removes);
        return null;
    }
}
