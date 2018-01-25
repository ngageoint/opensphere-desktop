package io.opensphere.xyztile.transformer;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.ImageManager.RequestObserver;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.util.collections.New;
import io.opensphere.xyztile.model.Projection;
import io.opensphere.xyztile.model.XYZServerInfo;
import io.opensphere.xyztile.model.XYZTileLayerInfo;

/**
 * Unit test for the {@link XYZ4326Divider} class.
 */
public class XYZ4326DividerTest
{
    /**
     * Tests dividing tiles.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        TileRenderProperties props = createProps(support);
        GeographicBoundingBox bounds = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-10, -10),
                LatLonAlt.createFromDegrees(10, 10));
        XYZTileLayerInfo layer = new XYZTileLayerInfo("osm", "Open Street Map", Projection.EPSG_4326, 2, false, 0,
                new XYZServerInfo("OSM", "http://osm.geointservices.io/osm_tiles_pc"));

        DataRegistry registry = createRegistry(support);
        RequestObserver observer = support.createMock(RequestObserver.class);

        support.replayAll();

        TileGeometry.Builder<GeographicPosition> builder = new TileGeometry.Builder<GeographicPosition>();
        builder.setBounds(bounds);
        ZYXImageKey parentImageKey = new ZYXImageKey(8, 0, 0, bounds);
        builder.setImageManager(new ImageManager(parentImageKey, new XYZImageProvider(registry, layer)));
        TileGeometry tileToDivide = new TileGeometry(builder, props, null);

        XYZ4326Divider divider = new XYZ4326Divider(layer, observer);
        Collection<AbstractTileGeometry<?>> subTiles = divider.divide(tileToDivide);

        assertEquals(4, subTiles.size());
        Map<String, GeographicBoundingBox> actualBounds = New.map();
        for (AbstractTileGeometry<?> subTile : subTiles)
        {
            assertEquals(divider, subTile.getSplitJoinRequestProvider());
            String imageKey = subTile.getImageManager().getImageKey().toString();
            GeographicBoundingBox actualTileBounds = (GeographicBoundingBox)subTile.getBounds();

            actualBounds.put(imageKey, actualTileBounds);
        }

        GeographicBoundingBox expectedBounds1 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(0d, -10d),
                LatLonAlt.createFromDegrees(10d, 0d));
        GeographicBoundingBox expectedBounds2 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(0d, 0d),
                LatLonAlt.createFromDegrees(10d, 10d));
        GeographicBoundingBox expectedBounds3 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-10d, 0d),
                LatLonAlt.createFromDegrees(0, 10d));
        GeographicBoundingBox expectedBounds4 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-10d, -10d),
                LatLonAlt.createFromDegrees(0d, 0d));

        ZYXImageKey expectedKey1 = new ZYXImageKey(9, 0, 0, expectedBounds1);
        ZYXImageKey expectedKey2 = new ZYXImageKey(9, 0, 1, expectedBounds2);
        ZYXImageKey expectedKey3 = new ZYXImageKey(9, 1, 1, expectedBounds3);
        ZYXImageKey expectedKey4 = new ZYXImageKey(9, 1, 0, expectedBounds4);

        List<ZYXImageKey> expecteds = New.list(expectedKey1, expectedKey2, expectedKey3, expectedKey4);

        for (ZYXImageKey expected : expecteds)
        {
            GeographicBoundingBox actual = actualBounds.get(expected.toString());
            assertEquals(expected.getBounds(), actual);
        }

        support.verifyAll();
    }

    /**
     * Tests dividing tms tiles.
     */
    @Test
    public void testTMS()
    {
        EasyMockSupport support = new EasyMockSupport();

        TileRenderProperties props = createProps(support);
        GeographicBoundingBox bounds = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-10, -10),
                LatLonAlt.createFromDegrees(10, 10));
        XYZTileLayerInfo layer = new XYZTileLayerInfo("osm", "Open Street Map", Projection.EPSG_4326, 2, true, 0,
                new XYZServerInfo("OSM", "http://osm.geointservices.io/osm_tiles_pc"));

        DataRegistry registry = createRegistry(support);
        RequestObserver observer = support.createMock(RequestObserver.class);

        support.replayAll();

        TileGeometry.Builder<GeographicPosition> builder = new TileGeometry.Builder<GeographicPosition>();
        builder.setBounds(bounds);
        ZYXImageKey parentImageKey = new ZYXImageKey(8, 0, 0, bounds);
        builder.setImageManager(new ImageManager(parentImageKey, new XYZImageProvider(registry, layer)));
        TileGeometry tileToDivide = new TileGeometry(builder, props, null);

        XYZ4326Divider divider = new XYZ4326Divider(layer, observer);
        Collection<AbstractTileGeometry<?>> subTiles = divider.divide(tileToDivide);

        assertEquals(4, subTiles.size());
        Map<String, GeographicBoundingBox> actualBounds = New.map();
        for (AbstractTileGeometry<?> subTile : subTiles)
        {
            assertEquals(divider, subTile.getSplitJoinRequestProvider());
            String imageKey = subTile.getImageManager().getImageKey().toString();
            GeographicBoundingBox actualTileBounds = (GeographicBoundingBox)subTile.getBounds();

            actualBounds.put(imageKey, actualTileBounds);
        }

        GeographicBoundingBox expectedBounds1 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(0d, -10d),
                LatLonAlt.createFromDegrees(10d, 0d));
        GeographicBoundingBox expectedBounds2 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(0d, 0d),
                LatLonAlt.createFromDegrees(10d, 10d));
        GeographicBoundingBox expectedBounds3 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-10d, 0d),
                LatLonAlt.createFromDegrees(0, 10d));
        GeographicBoundingBox expectedBounds4 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-10d, -10d),
                LatLonAlt.createFromDegrees(0d, 0d));

        ZYXImageKey expectedKey1 = new ZYXImageKey(9, 1, 0, expectedBounds1);
        ZYXImageKey expectedKey2 = new ZYXImageKey(9, 1, 1, expectedBounds2);
        ZYXImageKey expectedKey3 = new ZYXImageKey(9, 0, 1, expectedBounds3);
        ZYXImageKey expectedKey4 = new ZYXImageKey(9, 0, 0, expectedBounds4);

        List<ZYXImageKey> expecteds = New.list(expectedKey1, expectedKey2, expectedKey3, expectedKey4);

        for (ZYXImageKey expected : expecteds)
        {
            GeographicBoundingBox actual = actualBounds.get(expected.toString());
            assertEquals(expected.getBounds(), actual);
        }

        support.verifyAll();
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
     * Creates an easy mocked data registry.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link DataRegistry}.
     */
    private DataRegistry createRegistry(EasyMockSupport support)
    {
        DataRegistry registry = support.createMock(DataRegistry.class);

        return registry;
    }
}
