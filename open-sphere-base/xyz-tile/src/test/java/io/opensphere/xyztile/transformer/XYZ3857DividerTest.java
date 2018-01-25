package io.opensphere.xyztile.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
 * Unit test for the {@link XYZ3857Divider}.
 */
public class XYZ3857DividerTest
{
    /**
     * The test layer.
     */
    private static final XYZTileLayerInfo ourLayer = new XYZTileLayerInfo("mapbox.dark", "Dark", Projection.EPSG_3857, 1, false,
            0, new XYZServerInfo("mapbox", "http://mapbox.geointapps.org"));

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

        DataRegistry registry = createRegistry(support);
        RequestObserver observer = support.createMock(RequestObserver.class);

        support.replayAll();

        TileGeometry.Builder<GeographicPosition> builder = new TileGeometry.Builder<GeographicPosition>();
        builder.setBounds(bounds);
        ZYXImageKey parentImageKey = new ZYXImageKey(8, 0, 0, bounds);
        builder.setImageManager(new ImageManager(parentImageKey, new XYZImageProvider(registry, ourLayer)));
        TileGeometry tileToDivide = new TileGeometry(builder, props, null);

        XYZ3857Divider divider = new XYZ3857Divider(ourLayer, observer);
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

        GeographicBoundingBox expectedBounds1 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(84.99010018023479, -180),
                LatLonAlt.createFromDegrees(85.05112877980659, -179.296875));
        GeographicBoundingBox expectedBounds2 = new GeographicBoundingBox(
                LatLonAlt.createFromDegrees(84.99010018023479, -179.296875),
                LatLonAlt.createFromDegrees(85.05112877980659, -178.59375));
        GeographicBoundingBox expectedBounds3 = new GeographicBoundingBox(
                LatLonAlt.createFromDegrees(84.92832092949963, -179.296875),
                LatLonAlt.createFromDegrees(84.99010018023479, -178.59375));
        GeographicBoundingBox expectedBounds4 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(84.92832092949963, -180.0),
                LatLonAlt.createFromDegrees(84.99010018023479, -179.296875));

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
     * Tests dividing tiles.
     */
    @Test
    public void testMaxZoom()
    {
        EasyMockSupport support = new EasyMockSupport();

        TileRenderProperties props = createProps(support);
        GeographicBoundingBox bounds = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-10, -10),
                LatLonAlt.createFromDegrees(10, 10));

        DataRegistry registry = createRegistry(support);

        RequestObserver observer = support.createMock(RequestObserver.class);

        support.replayAll();

        TileGeometry.Builder<GeographicPosition> builder = new TileGeometry.Builder<GeographicPosition>();
        builder.setBounds(bounds);
        ZYXImageKey parentImageKey = new ZYXImageKey(17, 0, 0, bounds);
        builder.setImageManager(new ImageManager(parentImageKey, new XYZImageProvider(registry, ourLayer)));
        TileGeometry tileToDivide = new TileGeometry(builder, props, null);

        XYZ3857Divider divider = new XYZ3857Divider(ourLayer, observer);
        Collection<AbstractTileGeometry<?>> subTiles = divider.divide(tileToDivide);

        assertEquals(4, subTiles.size());
        Map<String, GeographicBoundingBox> actualBounds = New.map();
        for (AbstractTileGeometry<?> subTile : subTiles)
        {
            assertNull(subTile.getSplitJoinRequestProvider());
            String imageKey = subTile.getImageManager().getImageKey().toString();
            GeographicBoundingBox actualTileBounds = (GeographicBoundingBox)subTile.getBounds();

            actualBounds.put(imageKey, actualTileBounds);
        }

        GeographicBoundingBox expectedBounds1 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(85.05101030905541, -180.0),
                LatLonAlt.createFromDegrees(85.05112877980659, -179.99862670898438));
        GeographicBoundingBox expectedBounds2 = new GeographicBoundingBox(
                LatLonAlt.createFromDegrees(85.05101030905541, -179.99862670898438),
                LatLonAlt.createFromDegrees(85.05112877980659, -179.99725341796875));
        GeographicBoundingBox expectedBounds3 = new GeographicBoundingBox(
                LatLonAlt.createFromDegrees(85.05089183547521, -179.99862670898438),
                LatLonAlt.createFromDegrees(85.05101030905541, -179.99725341796875));
        GeographicBoundingBox expectedBounds4 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(85.05089183547521, -180.0),
                LatLonAlt.createFromDegrees(85.05101030905541, -179.99862670898438));

        ZYXImageKey expectedKey1 = new ZYXImageKey(18, 0, 0, expectedBounds1);
        ZYXImageKey expectedKey2 = new ZYXImageKey(18, 0, 1, expectedBounds2);
        ZYXImageKey expectedKey3 = new ZYXImageKey(18, 1, 1, expectedBounds3);
        ZYXImageKey expectedKey4 = new ZYXImageKey(18, 1, 0, expectedBounds4);

        List<ZYXImageKey> expecteds = New.list(expectedKey1, expectedKey2, expectedKey3, expectedKey4);

        for (ZYXImageKey expected : expecteds)
        {
            GeographicBoundingBox actual = actualBounds.get(expected.toString());
            assertEquals(expected.getBounds(), actual);
        }

        support.verifyAll();
    }

    /**
     * Tests dividing TMS tiles.
     */
    @Test
    public void testTMS()
    {
        EasyMockSupport support = new EasyMockSupport();

        TileRenderProperties props = createProps(support);
        GeographicBoundingBox bounds = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-10, -10),
                LatLonAlt.createFromDegrees(10, 10));

        DataRegistry registry = createRegistry(support);
        RequestObserver observer = support.createMock(RequestObserver.class);

        support.replayAll();

        XYZTileLayerInfo layer = new XYZTileLayerInfo("mapbox.dark", "Dark", Projection.EPSG_3857, 1, true, 0,
                new XYZServerInfo("mapbox", "http://mapbox.geointapps.org"));
        TileGeometry.Builder<GeographicPosition> builder = new TileGeometry.Builder<GeographicPosition>();
        builder.setBounds(bounds);
        ZYXImageKey parentImageKey = new ZYXImageKey(8, 0, 0, bounds);
        builder.setImageManager(new ImageManager(parentImageKey, new XYZImageProvider(registry, layer)));
        TileGeometry tileToDivide = new TileGeometry(builder, props, null);

        XYZ3857Divider divider = new XYZ3857Divider(layer, observer);
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

        GeographicBoundingBox expectedBounds1 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(84.99010018023479, -180),
                LatLonAlt.createFromDegrees(85.05112877980659, -179.296875));
        GeographicBoundingBox expectedBounds2 = new GeographicBoundingBox(
                LatLonAlt.createFromDegrees(84.99010018023479, -179.296875),
                LatLonAlt.createFromDegrees(85.05112877980659, -178.59375));
        GeographicBoundingBox expectedBounds3 = new GeographicBoundingBox(
                LatLonAlt.createFromDegrees(84.92832092949963, -179.296875),
                LatLonAlt.createFromDegrees(84.99010018023479, -178.59375));
        GeographicBoundingBox expectedBounds4 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(84.92832092949963, -180.0),
                LatLonAlt.createFromDegrees(84.99010018023479, -179.296875));

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
