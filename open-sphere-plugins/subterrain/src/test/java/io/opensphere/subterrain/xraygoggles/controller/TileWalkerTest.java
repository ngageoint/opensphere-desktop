package io.opensphere.subterrain.xraygoggles.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.MapManager;
import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.AbstractTileGeometry.AbstractDivider;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.TileGeometry.Builder;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.collections.New;
import io.opensphere.subterrain.xraygoggles.model.XrayGogglesModel;

/**
 * Unit test for {@link TileWalker}.
 */
public class TileWalkerTest extends AbstractDivider<GeographicPosition>
{
    /**
     * Default constructor.
     */
    public TileWalkerTest()
    {
        super("test");
    }

    @Override
    public Collection<AbstractTileGeometry<?>> divide(AbstractTileGeometry<?> parent)
    {
        List<AbstractTileGeometry<?>> dividedTiles = New.list();
        List<GeographicBoundingBox> newBoxes = calculateNewBoxes((GeographicBoundingBox)parent.getBounds());

        for (GeographicBoundingBox newBounds : newBoxes)
        {
            AbstractTileGeometry<?> subTile = parent.createSubTile(newBounds, null, this);
            dividedTiles.add(subTile);
        }

        return dividedTiles;
    }

    /**
     * Tests collecting tiles.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<TileGeometry> expected = New.list();
        List<Geometry> geometries = buildGeometries(expected);
        GeometryRegistry registry = createGeometryRegistry(support, geometries);
        MapManager mapManager = createMapManager(support);

        support.replayAll();

        XrayGogglesModel model = new XrayGogglesModel();
        model.setScreenPosition(new ScreenPosition(0, 8), new ScreenPosition(2, 8), new ScreenPosition(0, 10),
                new ScreenPosition(2, 10));
        model.setGeoPosition(new GeographicPosition(LatLonAlt.createFromDegrees(2, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(2, 2)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 2)),
                new GeographicPosition(LatLonAlt.createFromDegrees(1, 1)));
        TileWalker walker = new TileWalker(mapManager, registry, model);

        List<TileGeometry> actual = walker.collectTiles();

        assertEquals(expected, actual);

        support.verifyAll();
    }

    /**
     * Tests collecting tiles.
     */
    @Test
    public void test1()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<TileGeometry> expected = New.list();
        List<Geometry> geometries = buildGeometries(expected);
        GeometryRegistry registry = createGeometryRegistry(support, geometries);
        MapManager mapManager = createMapManager(support);

        support.replayAll();

        XrayGogglesModel model = new XrayGogglesModel();
        model.setScreenPosition(new ScreenPosition(-0.1, 8.1), new ScreenPosition(2.1, 8), new ScreenPosition(-0.1, 10.1),
                new ScreenPosition(2.1, 10.1));
        model.setGeoPosition(new GeographicPosition(LatLonAlt.createFromDegrees(2.1, -0.1)),
                new GeographicPosition(LatLonAlt.createFromDegrees(2.1, 2.1)),
                new GeographicPosition(LatLonAlt.createFromDegrees(-0.1, -0.1)),
                new GeographicPosition(LatLonAlt.createFromDegrees(-0.1, 2.1)),
                new GeographicPosition(LatLonAlt.createFromDegrees(1, 1)));
        TileWalker walker = new TileWalker(mapManager, registry, model);

        List<TileGeometry> actual = walker.collectTiles();

        assertTrue(actual.containsAll(expected));

        support.verifyAll();
    }

    /**
     * Tests collecting tiles.
     */
    @Test
    public void test2()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<TileGeometry> expected = New.list();
        List<Geometry> geometries = buildGeometries(expected);
        GeometryRegistry registry = createGeometryRegistry(support, geometries);
        MapManager mapManager = createMapManager(support);

        support.replayAll();

        XrayGogglesModel model = new XrayGogglesModel();
        model.setScreenPosition(new ScreenPosition(.5, 8.5), new ScreenPosition(2, 8.5), new ScreenPosition(1.2, 10),
                new ScreenPosition(1.7, 10));
        model.setGeoPosition(new GeographicPosition(LatLonAlt.createFromDegrees(1.5, .5)),
                new GeographicPosition(LatLonAlt.createFromDegrees(1.5, 2)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 1.2)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 1.7)),
                new GeographicPosition(LatLonAlt.createFromDegrees(1, 1.4)));
        TileWalker walker = new TileWalker(mapManager, registry, model);

        List<TileGeometry> actual = walker.collectTiles();

        assertEquals(expected, actual);

        support.verifyAll();
    }

    /**
     * Tests collecting tiles.
     */
    @Test
    public void testNoVertexContainment()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<TileGeometry> expected = New.list();
        List<Geometry> geometries = buildGeometries(expected);
        expected = New.list(expected.get(2));
        GeometryRegistry registry = createGeometryRegistry(support, geometries);
        MapManager mapManager = createMapManager(support);

        support.replayAll();

        XrayGogglesModel model = new XrayGogglesModel();
        model.setScreenPosition(new ScreenPosition(.25, 9.25), new ScreenPosition(.75, 9.25), new ScreenPosition(.25, 9.75),
                new ScreenPosition(.75, 9.75));
        model.setGeoPosition(new GeographicPosition(LatLonAlt.createFromDegrees(.75, .25)),
                new GeographicPosition(LatLonAlt.createFromDegrees(.75, .75)),
                new GeographicPosition(LatLonAlt.createFromDegrees(.25, .25)),
                new GeographicPosition(LatLonAlt.createFromDegrees(.25, .75)),
                new GeographicPosition(LatLonAlt.createFromDegrees(.5, .5)));
        TileWalker walker = new TileWalker(mapManager, registry, model);

        List<TileGeometry> actual = walker.collectTiles();

        assertEquals(expected, actual);

        support.verifyAll();
    }

    /**
     * Builds the test geometries.
     *
     * @param expected The list to add the expected geometries to.
     * @return The test geometries.
     */
    private List<Geometry> buildGeometries(List<TileGeometry> expected)
    {
        List<Geometry> geoms = New.list();

        DefaultTileRenderProperties props = new DefaultTileRenderProperties(0, true, false);
        Builder<GeographicPosition> builder = new Builder<>();
        builder.setBounds(new GeographicBoundingBox(new GeographicPosition(LatLonAlt.createFromDegrees(-4, -4)),
                new GeographicPosition(LatLonAlt.createFromDegrees(4, 4))));
        builder.setDivider(this);
        builder.setImageManager(new ImageManager("key", null));

        TileGeometry top = new TileGeometry(builder, props, null);
        List<TileGeometry> children = New.list(top.getChildren(true));
        List<TileGeometry> grandChildren = New.list(children.get(1).getChildren(true));
        List<TileGeometry> greatGrandChildren = New.list(grandChildren.get(2).getChildren(true));
        expected.addAll(greatGrandChildren);

        geoms.add(top);

        return geoms;
    }

    /**
     * Calculates the new bounding boxes of the divided tile.
     *
     * @param bbox The bounding box of the tile we are dividing.
     * @return The new sub tile bounding boxes.
     */
    private List<GeographicBoundingBox> calculateNewBoxes(GeographicBoundingBox bbox)
    {
        GeographicPosition upperLeftPos = bbox.getUpperLeft();
        GeographicPosition upperRightPos = bbox.getUpperRight();
        GeographicPosition lowerLeftPos = bbox.getLowerLeft();
        GeographicPosition lowerRightPos = bbox.getLowerRight();
        GeographicPosition centerPos = bbox.getCenter();

        GeographicBoundingBox upperLeft = new GeographicBoundingBox(
                LatLonAlt.createFromDegrees(centerPos.getLat().getMagnitude(), upperLeftPos.getLon().getMagnitude()),
                LatLonAlt.createFromDegrees(upperLeftPos.getLat().getMagnitude(), centerPos.getLon().getMagnitude()));
        GeographicBoundingBox upperRight = new GeographicBoundingBox(centerPos, upperRightPos);
        GeographicBoundingBox lowerLeft = new GeographicBoundingBox(lowerLeftPos, centerPos);
        GeographicBoundingBox lowerRight = new GeographicBoundingBox(
                LatLonAlt.createFromDegrees(lowerLeftPos.getLat().getMagnitude(), centerPos.getLon().getMagnitude()),
                LatLonAlt.createFromDegrees(centerPos.getLat().getMagnitude(), lowerRightPos.getLon().getMagnitude()));

        return New.list(upperLeft, upperRight, lowerLeft, lowerRight);
    }

    /**
     * Creates an easy mocked {@link GeometryRegistry}.
     *
     * @param support Used to create the mock.
     * @param geometries The geometries to return from the registry.
     * @return The mocked registry.
     */
    private GeometryRegistry createGeometryRegistry(EasyMockSupport support, List<Geometry> geometries)
    {
        GeometryRegistry geometryRegistry = support.createMock(GeometryRegistry.class);

        EasyMock.expect(geometryRegistry.getGeometries()).andReturn(geometries);

        return geometryRegistry;
    }

    /**
     * Creates a mocked {@link MapManager}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link MapManager}.
     */
    private MapManager createMapManager(EasyMockSupport support)
    {
        MapManager mapManager = support.createMock(MapManager.class);

        EasyMock.expect(mapManager.convertToPoint(EasyMock.isA(GeographicPosition.class))).andAnswer(() ->
        {
            GeographicPosition geo = (GeographicPosition)EasyMock.getCurrentArguments()[0];
            return new Vector2i((int)geo.getLatLonAlt().getLonD(), (int)(10 - geo.getLatLonAlt().getLatD()));
        }).atLeastOnce();

        return mapManager;
    }
}
