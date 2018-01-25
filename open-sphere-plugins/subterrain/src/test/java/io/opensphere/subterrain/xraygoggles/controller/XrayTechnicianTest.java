package io.opensphere.subterrain.xraygoggles.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.collections.New;
import io.opensphere.subterrain.xraygoggles.model.XrayGogglesModel;

/**
 * Unit test for {@link XrayTechnician}.
 */
public class XrayTechnicianTest extends AbstractDivider<GeographicPosition>
{
    /**
     * The geometry registry subscriber.
     */
    private GenericSubscriber<Geometry> mySubscriber;

    /**
     * Default constructor.
     */
    public XrayTechnicianTest()
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
     * Tests making some tiles transparent.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<TileGeometry> expected = New.list();
        List<Geometry> geometries = buildGeometries(expected, true);
        GeometryRegistry registry = createGeometryRegistry(support, geometries);
        MapManager mapManager = createMapManager(support);

        support.replayAll();

        XrayGogglesModel model = new XrayGogglesModel();

        XrayTechnician technician = new XrayTechnician(mapManager, registry, model);

        model.setScreenPosition(new ScreenPosition(0, 8), new ScreenPosition(2, 8), new ScreenPosition(0, 10),
                new ScreenPosition(2, 10));
        model.setGeoPosition(new GeographicPosition(LatLonAlt.createFromDegrees(2, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(2, 2)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 2)),
                new GeographicPosition(LatLonAlt.createFromDegrees(1, 1)));

        for (TileGeometry geom : expected)
        {
            assertEquals(25, geom.getRenderProperties().getOpacity(), 0f);
            assertFalse(geom.getRenderProperties().isObscurant());
        }

        model.setScreenPosition(new ScreenPosition(10, 18), new ScreenPosition(12, 18), new ScreenPosition(10, 20),
                new ScreenPosition(12, 20));
        model.setGeoPosition(new GeographicPosition(LatLonAlt.createFromDegrees(12, 10)),
                new GeographicPosition(LatLonAlt.createFromDegrees(12, 12)),
                new GeographicPosition(LatLonAlt.createFromDegrees(10, 10)),
                new GeographicPosition(LatLonAlt.createFromDegrees(10, 12)),
                new GeographicPosition(LatLonAlt.createFromDegrees(11, 11)));

        for (TileGeometry geom : expected)
        {
            assertEquals(255, geom.getRenderProperties().getOpacity(), 0f);
            assertTrue(geom.getRenderProperties().isObscurant());
        }

        model.setScreenPosition(new ScreenPosition(0, 8), new ScreenPosition(2, 8), new ScreenPosition(0, 10),
                new ScreenPosition(2, 10));
        model.setGeoPosition(new GeographicPosition(LatLonAlt.createFromDegrees(2, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(2, 2)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 2)),
                new GeographicPosition(LatLonAlt.createFromDegrees(1, 1)));

        for (TileGeometry geom : expected)
        {
            assertEquals(25, geom.getRenderProperties().getOpacity(), 0f);
            assertFalse(geom.getRenderProperties().isObscurant());
        }

        technician.close();

        for (TileGeometry geom : expected)
        {
            assertEquals(255, geom.getRenderProperties().getOpacity(), 0f);
            assertTrue(geom.getRenderProperties().isObscurant());
        }

        model.setScreenPosition(new ScreenPosition(0, 8), new ScreenPosition(2, 8), new ScreenPosition(0, 10),
                new ScreenPosition(2, 10));
        model.setGeoPosition(new GeographicPosition(LatLonAlt.createFromDegrees(2, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(2, 2)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 2)),
                new GeographicPosition(LatLonAlt.createFromDegrees(1, 1)));

        for (TileGeometry geom : expected)
        {
            assertEquals(255, geom.getRenderProperties().getOpacity(), 0f);
            assertTrue(geom.getRenderProperties().isObscurant());
        }

        support.verifyAll();
    }

    /**
     * Tests making some tiles transparent that were added to the Geometry
     * registry.
     */
    @Test
    public void testAdded()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<TileGeometry> expected = New.list();
        List<Geometry> geometries = buildGeometries(expected, true);
        GeometryRegistry registry = createGeometryRegistry(support, geometries);
        MapManager mapManager = createMapManager(support);
        Geometry nonTile = support.createMock(Geometry.class);

        support.replayAll();

        XrayGogglesModel model = new XrayGogglesModel();

        model.setScreenPosition(new ScreenPosition(0, 8), new ScreenPosition(2, 8), new ScreenPosition(0, 10),
                new ScreenPosition(2, 10));
        model.setGeoPosition(new GeographicPosition(LatLonAlt.createFromDegrees(2, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(2, 2)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 2)),
                new GeographicPosition(LatLonAlt.createFromDegrees(1, 1)));

        XrayTechnician technician = new XrayTechnician(mapManager, registry, model);

        mySubscriber.receiveObjects(this, New.list(), New.list(expected));

        for (TileGeometry geom : expected)
        {
            assertEquals(255, geom.getRenderProperties().getOpacity(), 0f);
            assertTrue(geom.getRenderProperties().isObscurant());
        }

        mySubscriber.receiveObjects(this, New.list(nonTile), New.list());

        for (TileGeometry geom : expected)
        {
            assertEquals(255, geom.getRenderProperties().getOpacity(), 0f);
            assertTrue(geom.getRenderProperties().isObscurant());
        }

        mySubscriber.receiveObjects(this, geometries, New.list());

        for (TileGeometry geom : expected)
        {
            assertEquals(25, geom.getRenderProperties().getOpacity(), 0f);
            assertFalse(geom.getRenderProperties().isObscurant());
        }

        technician.close();

        assertNull(mySubscriber);

        support.verifyAll();
    }

    /**
     * Tests making some tiles transparent that were added to the Geometry
     * registry.
     */
    @Test
    public void testChildrenAdded()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<TileGeometry> expected = New.list();
        List<Geometry> geometries = buildGeometries(expected, false);
        GeometryRegistry registry = createGeometryRegistry(support, geometries);
        MapManager mapManager = createMapManager(support);
        Geometry nonTile = support.createMock(Geometry.class);

        support.replayAll();

        XrayGogglesModel model = new XrayGogglesModel();

        model.setScreenPosition(new ScreenPosition(0, 8), new ScreenPosition(2, 8), new ScreenPosition(0, 10),
                new ScreenPosition(2, 10));
        model.setGeoPosition(new GeographicPosition(LatLonAlt.createFromDegrees(2, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(2, 2)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 2)),
                new GeographicPosition(LatLonAlt.createFromDegrees(1, 1)));

        XrayTechnician technician = new XrayTechnician(mapManager, registry, model);

        mySubscriber.receiveObjects(this, New.list(), New.list(expected));

        for (TileGeometry geom : expected)
        {
            assertEquals(255, geom.getRenderProperties().getOpacity(), 0f);
            assertTrue(geom.getRenderProperties().isObscurant());
        }

        mySubscriber.receiveObjects(this, New.list(nonTile), New.list());

        for (TileGeometry geom : expected)
        {
            assertEquals(255, geom.getRenderProperties().getOpacity(), 0f);
            assertTrue(geom.getRenderProperties().isObscurant());
        }

        mySubscriber.receiveObjects(this, geometries, New.list());

        TileGeometry top = (TileGeometry)geometries.get(0);
        assertEquals(1, top.getChildrenListenerCount());

        List<TileGeometry> children = New.list(top.getChildren(true));
        assertEquals(0, top.getChildrenListenerCount());
        assertEquals(1, children.get(1).getChildrenListenerCount());

        List<TileGeometry> grandChildren = New.list(children.get(1).getChildren(true));
        assertEquals(0, children.get(1).getChildrenListenerCount());
        assertEquals(1, grandChildren.get(2).getChildrenListenerCount());

        List<TileGeometry> greatGrandChildren = New.list(grandChildren.get(2).getChildren(true));
        for (TileGeometry great : greatGrandChildren)
        {
            assertEquals(1, great.getChildrenListenerCount());
        }
        expected.addAll(greatGrandChildren);

        for (TileGeometry geom : expected)
        {
            assertEquals(25, geom.getRenderProperties().getOpacity(), 0f);
            assertFalse(geom.getRenderProperties().isObscurant());
        }

        technician.close();

        for (TileGeometry great : greatGrandChildren)
        {
            assertEquals(0, great.getChildrenListenerCount());
        }
        assertNull(mySubscriber);

        support.verifyAll();
    }

    /**
     * Builds the test geometries.
     *
     * @param expected The list to add the expected geometries to.
     * @param buildChildren True if children and great grandchildren should be
     *            constructed.
     * @return The test geometries.
     */
    private List<Geometry> buildGeometries(List<TileGeometry> expected, boolean buildChildren)
    {
        List<Geometry> geoms = New.list();

        DefaultTileRenderProperties props = new DefaultTileRenderProperties(0, true, false);
        Builder<GeographicPosition> builder = new Builder<>();
        builder.setBounds(new GeographicBoundingBox(new GeographicPosition(LatLonAlt.createFromDegrees(-4, -4)),
                new GeographicPosition(LatLonAlt.createFromDegrees(4, 4))));
        builder.setDivider(this);
        builder.setImageManager(new ImageManager("key", null));

        TileGeometry top = new TileGeometry(builder, props, null);

        if (buildChildren)
        {
            List<TileGeometry> children = New.list(top.getChildren(true));
            List<TileGeometry> grandChildren = New.list(children.get(1).getChildren(true));
            List<TileGeometry> greatGrandChildren = New.list(grandChildren.get(2).getChildren(true));
            expected.addAll(greatGrandChildren);
        }

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
    @SuppressWarnings("unchecked")
    private GeometryRegistry createGeometryRegistry(EasyMockSupport support, List<Geometry> geometries)
    {
        GeometryRegistry geometryRegistry = support.createMock(GeometryRegistry.class);

        EasyMock.expect(geometryRegistry.getGeometries()).andReturn(geometries).atLeastOnce();

        geometryRegistry.addSubscriber(EasyMock.isA(GenericSubscriber.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            mySubscriber = (GenericSubscriber<Geometry>)EasyMock.getCurrentArguments()[0];
            return null;
        });
        geometryRegistry.removeSubscriber(EasyMock.isA(GenericSubscriber.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            if (mySubscriber.equals(EasyMock.getCurrentArguments()[0]))
            {
                mySubscriber = null;
            }
            return null;
        });

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
