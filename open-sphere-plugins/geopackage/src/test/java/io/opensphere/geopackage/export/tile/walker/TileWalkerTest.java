package io.opensphere.geopackage.export.tile.walker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.AbstractTileGeometry.AbstractDivider;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.TerrainTileGeometry;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.image.ImageProvider;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.geopackage.export.model.ExportModel;
import io.opensphere.geopackage.model.ProgressModel;
import io.opensphere.geopackage.progress.ProgressReporter;

/**
 * Unit test for {@link TileWalker}.
 */
public class TileWalkerTest extends AbstractDivider<GeographicPosition>
{
    /**
     * The test layer id.
     */
    private static final String ourLayerId = "I am layer";

    /**
     * The expected {@link ZYXImageKey} at zoom level 1.
     */
    private static final List<ZYXImageKey> ourOneExpectedKeys = New.list(
            new ZYXImageKey(1, 0, 0,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, -180), LatLonAlt.createFromDegrees(0, -90))),
            new ZYXImageKey(1, 0, 1,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, -90), LatLonAlt.createFromDegrees(0, 0))),
            new ZYXImageKey(1, 1, 0,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, -180), LatLonAlt.createFromDegrees(90, -90))),
            new ZYXImageKey(1, 1, 1,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, -90), LatLonAlt.createFromDegrees(90, 0))),
            new ZYXImageKey(1, 0, 2,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, 0), LatLonAlt.createFromDegrees(0, 90))),
            new ZYXImageKey(1, 0, 3,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, 90), LatLonAlt.createFromDegrees(0, 180))),
            new ZYXImageKey(1, 1, 2,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, 0), LatLonAlt.createFromDegrees(90, 90))),
            new ZYXImageKey(1, 1, 3,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, 90), LatLonAlt.createFromDegrees(90, 180))));

    /**
     * The expected {@link ZYXImageKey} at zoom level 2.
     */
    private static final List<ZYXImageKey> ourTwoExpectedKeys = New
            .list(new ZYXImageKey(2, 3, 0,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(45, -180), LatLonAlt.createFromDegrees(90, -135))),
                    new ZYXImageKey(2, 3, 1,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(45, -135),
                                    LatLonAlt.createFromDegrees(90, -90))),
                    new ZYXImageKey(2, 2, 0,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, -180),
                                    LatLonAlt.createFromDegrees(45, -135))),
                    new ZYXImageKey(2, 2, 1,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, -135),
                                    LatLonAlt.createFromDegrees(45, -90))),
                    new ZYXImageKey(2, 3, 2,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(45, -90),
                                    LatLonAlt.createFromDegrees(90, -45))),
                    new ZYXImageKey(2, 3, 3,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(45, -45), LatLonAlt.createFromDegrees(90, 0))),
                    new ZYXImageKey(2, 2, 2,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, -90), LatLonAlt.createFromDegrees(45, -45))),
                    new ZYXImageKey(2, 2, 3,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, -45), LatLonAlt.createFromDegrees(45, 0))),
                    new ZYXImageKey(2, 3, 4,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(45, 0), LatLonAlt.createFromDegrees(90, 45))),
                    new ZYXImageKey(2, 3, 5,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(45, 45), LatLonAlt.createFromDegrees(90, 90))),
                    new ZYXImageKey(2, 2, 4,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, 0), LatLonAlt.createFromDegrees(45, 45))),
                    new ZYXImageKey(2, 2, 5,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, 45), LatLonAlt.createFromDegrees(45, 90))),
                    new ZYXImageKey(2, 3, 6,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(45, 90), LatLonAlt.createFromDegrees(90, 135))),
                    new ZYXImageKey(2, 3, 7,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(45, 135),
                                    LatLonAlt.createFromDegrees(90, 180))),
                    new ZYXImageKey(2, 2, 6,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, 90), LatLonAlt.createFromDegrees(45, 135))),
                    new ZYXImageKey(2, 2, 7,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, 135), LatLonAlt.createFromDegrees(45, 180))),
                    new ZYXImageKey(2, 1, 0,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(-45, -180),
                                    LatLonAlt.createFromDegrees(0, -135))),
                    new ZYXImageKey(2, 1, 1,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(-45, -135),
                                    LatLonAlt.createFromDegrees(0, -90))),
                    new ZYXImageKey(2, 0, 0,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, -180),
                                    LatLonAlt.createFromDegrees(-45, -135))),
                    new ZYXImageKey(2, 0, 1,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, -135),
                                    LatLonAlt.createFromDegrees(-45, -90))),
                    new ZYXImageKey(2, 1, 2,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(-45, -90),
                                    LatLonAlt.createFromDegrees(0, -45))),
                    new ZYXImageKey(2, 1, 3,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(-45, -45), LatLonAlt.createFromDegrees(0, 0))),
                    new ZYXImageKey(2, 0, 2,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, -90),
                                    LatLonAlt.createFromDegrees(-45, -45))),
                    new ZYXImageKey(2, 0, 3,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, -45),
                                    LatLonAlt.createFromDegrees(-45, 0))),
                    new ZYXImageKey(2, 1, 4,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(-45, 0), LatLonAlt.createFromDegrees(0, 45))),
                    new ZYXImageKey(2, 1, 5,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(-45, 45), LatLonAlt.createFromDegrees(0, 90))),
                    new ZYXImageKey(2, 0, 4,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, 0), LatLonAlt.createFromDegrees(-45, 45))),
                    new ZYXImageKey(2, 0, 5,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, 45),
                                    LatLonAlt.createFromDegrees(-45, 90))),
                    new ZYXImageKey(2, 1, 6,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(-45, 90), LatLonAlt.createFromDegrees(0, 135))),
                    new ZYXImageKey(2, 1, 7,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(-45, 135),
                                    LatLonAlt.createFromDegrees(0, 180))),
                    new ZYXImageKey(2, 0, 6,
                            new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, 90),
                                    LatLonAlt.createFromDegrees(-45, 135))),
                    new ZYXImageKey(2, 0, 7, new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, 135),
                            LatLonAlt.createFromDegrees(-45, 180))));

    /**
     * The expected {@link ZYXImageKey} at zoom level 0.
     */
    private static final List<ZYXImageKey> ourZeroExpectedKeys = New.list(
            new ZYXImageKey(0, 0, 0,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, -180), LatLonAlt.createFromDegrees(90, 0))),
            new ZYXImageKey(0, 0, 1,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, 0), LatLonAlt.createFromDegrees(90, 180))));

    /**
     * Default constructor.
     */
    public TileWalkerTest()
    {
        super(ourLayerId);
    }

    @Override
    public Collection<AbstractTileGeometry<?>> divide(AbstractTileGeometry<?> parent)
    {
        int generation = parent.getGeneration() + 1;

        List<AbstractTileGeometry<?>> dividedTiles = New.list();

        List<GeographicBoundingBox> newBoxes = calculateNewBoxes((GeographicBoundingBox)parent.getBounds());

        for (GeographicBoundingBox newBounds : newBoxes)
        {
            TileWalkerTest divider = this;
            if (generation + 1 > 2)
            {
                divider = null;
            }

            AbstractTileGeometry<?> subTile = parent.createSubTile(newBounds, "newKey", divider);
            dividedTiles.add(subTile);
        }

        return dividedTiles;
    }

    /**
     * Tests getting geometries when the bounding box contains a whole tile.
     */
    @Test
    public void testGetGeometries()
    {
        EasyMockSupport support = new EasyMockSupport();

        GeographicBoundingBox boundingBox = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-20, 20),
                LatLonAlt.createFromDegrees(60, 100));
        GeometryRegistry geomRegistry = createGeomRegistry(support);
        ExportModel model = createExportModel();
        List<TileInfo> tiles = New.list();
        Consumer<TileInfo> consumer = createConsumer(support, tiles);

        support.replayAll();

        TileWalker walker = new TileWalker(geomRegistry, model);
        walker.getGeometries(ourLayerId, boundingBox, consumer);

        Map<Integer, List<AbstractTileGeometry<?>>> geometries = New.map();

        for (TileInfo tile : tiles)
        {
            Integer zoomLevel = Integer.valueOf(tile.getZoomLevel());
            if (!geometries.containsKey(zoomLevel))
            {
                geometries.put(zoomLevel, New.list());
            }

            geometries.get(zoomLevel).add(tile.getGeometry());
        }

        assertEquals(1, geometries.size());
        List<AbstractTileGeometry<?>> levelTwoGeoms = geometries.get(Integer.valueOf(2));

        assertNotNull(levelTwoGeoms);
        assertEquals(9, levelTwoGeoms.size());

        Map<GeographicBoundingBox, AbstractTileGeometry<?>> mappedGeoms = New.map();
        for (AbstractTileGeometry<?> geom : levelTwoGeoms)
        {
            mappedGeoms.put((GeographicBoundingBox)geom.getBounds(), geom);
        }

        int[] expectedLevelTwos = new int[] { 8, 9, 10, 11, 12, 14, 24, 25, 28 };
        for (int expectedLevelTwo : expectedLevelTwos)
        {
            assertTrue(mappedGeoms.containsKey(ourTwoExpectedKeys.get(expectedLevelTwo).getBounds()));
        }

        support.verifyAll();
    }

    /**
     * Tests getting geometries when the bounding box never contains a whole
     * tile.
     */
    @Test
    public void testGetGeometriesNoContainment()
    {
        EasyMockSupport support = new EasyMockSupport();

        GeographicBoundingBox boundingBox = new GeographicBoundingBox(LatLonAlt.createFromDegrees(20, -60),
                LatLonAlt.createFromDegrees(60, -20));
        GeometryRegistry geomRegistry = createGeomRegistry(support);

        ExportModel model = createExportModel();
        List<TileInfo> tiles = New.list();
        Consumer<TileInfo> consumer = createConsumer(support, tiles);

        support.replayAll();

        TileWalker walker = new TileWalker(geomRegistry, model);
        walker.getGeometries(ourLayerId, boundingBox, consumer);

        Map<Integer, List<AbstractTileGeometry<?>>> geometries = New.map();

        for (TileInfo tile : tiles)
        {
            Integer zoomLevel = Integer.valueOf(tile.getZoomLevel());
            if (!geometries.containsKey(zoomLevel))
            {
                geometries.put(zoomLevel, New.list());
            }

            geometries.get(zoomLevel).add(tile.getGeometry());
        }

        assertEquals(1, geometries.size());
        List<AbstractTileGeometry<?>> levelTwoGeoms = geometries.get(Integer.valueOf(2));

        assertNotNull(levelTwoGeoms);
        assertEquals(4, levelTwoGeoms.size());

        Map<GeographicBoundingBox, AbstractTileGeometry<?>> mappedGeoms = New.map();
        for (AbstractTileGeometry<?> geom : levelTwoGeoms)
        {
            mappedGeoms.put((GeographicBoundingBox)geom.getBounds(), geom);
        }

        int[] expectedLevelTwos = new int[] { 4, 5, 6, 7 };
        for (int expectedLevelTwo : expectedLevelTwos)
        {
            assertTrue(mappedGeoms.containsKey(ourTwoExpectedKeys.get(expectedLevelTwo).getBounds()));
        }

        support.verifyAll();
    }

    /**
     * Tests getting geometries when the bounding box never contains a whole
     * tile.
     */
    @Test
    public void testGetGeometriesTwoLevels()
    {
        EasyMockSupport support = new EasyMockSupport();

        GeographicBoundingBox boundingBox = new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, 0),
                LatLonAlt.createFromDegrees(90, 90));
        GeometryRegistry geomRegistry = createGeomRegistry(support);

        ExportModel model = createExportModel();
        List<TileInfo> tiles = New.list();
        Consumer<TileInfo> consumer = createConsumer(support, tiles);

        support.replayAll();

        TileWalker walker = new TileWalker(geomRegistry, model);
        walker.getGeometries(ourLayerId, boundingBox, consumer);

        Map<Integer, List<AbstractTileGeometry<?>>> geometries = New.map();

        for (TileInfo tile : tiles)
        {
            Integer zoomLevel = Integer.valueOf(tile.getZoomLevel());
            if (!geometries.containsKey(zoomLevel))
            {
                geometries.put(zoomLevel, New.list());
            }

            geometries.get(zoomLevel).add(tile.getGeometry());
        }

        assertEquals(2, geometries.size());

        List<AbstractTileGeometry<?>> levelOneGeoms = geometries.get(Integer.valueOf(1));
        assertNotNull(levelOneGeoms);

        assertEquals(6, levelOneGeoms.size());
        Map<GeographicBoundingBox, AbstractTileGeometry<?>> mappedGeoms = New.map();
        for (AbstractTileGeometry<?> geom : levelOneGeoms)
        {
            mappedGeoms.put((GeographicBoundingBox)geom.getBounds(), geom);
        }

        int[] expectedLevelOnes = new int[] { 1, 3, 4, 5, 6, 7 };
        for (int expectedLevelOne : expectedLevelOnes)
        {
            assertTrue(mappedGeoms.containsKey(ourOneExpectedKeys.get(expectedLevelOne).getBounds()));
        }

        List<AbstractTileGeometry<?>> levelTwoGeoms = geometries.get(Integer.valueOf(2));

        assertNotNull(levelTwoGeoms);
        assertEquals(24, levelTwoGeoms.size());

        mappedGeoms = New.map();
        for (AbstractTileGeometry<?> geom : levelTwoGeoms)
        {
            mappedGeoms.put((GeographicBoundingBox)geom.getBounds(), geom);
        }

        int[] expectedLevelTwos = new int[] { 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
            30, 31 };
        for (int expectedLevelTwo : expectedLevelTwos)
        {
            assertTrue(mappedGeoms.containsKey(ourTwoExpectedKeys.get(expectedLevelTwo).getBounds()));
        }

        support.verifyAll();
    }

    /**
     * Tests getting whole world of geometries.
     */
    @Test
    public void testGetGeometriesWholeWorld()
    {
        EasyMockSupport support = new EasyMockSupport();

        GeographicBoundingBox boundingBox = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, -180),
                LatLonAlt.createFromDegrees(90, 180));
        GeometryRegistry geomRegistry = createGeomRegistry(support);

        ExportModel model = createExportModel();
        List<TileInfo> tiles = New.list();
        Consumer<TileInfo> consumer = createConsumer(support, tiles);

        support.replayAll();

        TileWalker walker = new TileWalker(geomRegistry, model);
        walker.getGeometries(ourLayerId, boundingBox, consumer);

        Map<Integer, List<AbstractTileGeometry<?>>> geometries = New.map();

        for (TileInfo tile : tiles)
        {
            Integer zoomLevel = Integer.valueOf(tile.getZoomLevel());
            if (!geometries.containsKey(zoomLevel))
            {
                geometries.put(zoomLevel, New.list());
            }

            geometries.get(zoomLevel).add(tile.getGeometry());
        }

        assertEquals(3, geometries.size());

        List<List<ZYXImageKey>> expected = New.list(ourZeroExpectedKeys, ourOneExpectedKeys, ourTwoExpectedKeys);
        for (int i = 0; i < expected.size(); i++)
        {
            List<ZYXImageKey> expectedAtLevel = expected.get(i);
            List<AbstractTileGeometry<?>> actualAtLevel = geometries.get(Integer.valueOf(i));

            assertEquals(expectedAtLevel.size(), actualAtLevel.size());

            Map<GeographicBoundingBox, AbstractTileGeometry<?>> mappedGeoms = New.map();
            for (AbstractTileGeometry<?> geom : actualAtLevel)
            {
                mappedGeoms.put((GeographicBoundingBox)geom.getBounds(), geom);
            }

            for (ZYXImageKey expectedKey : expectedAtLevel)
            {
                assertTrue(mappedGeoms.containsKey(expectedKey.getBounds()));
            }
        }

        support.verifyAll();
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

        return New.list(lowerLeft, lowerRight, upperLeft, upperRight);
    }

    /**
     * Creates an easy mocked tile consumer.
     *
     * @param support Used to create the mock.
     * @param geometries The list to add the consumed geometries to.
     * @return The mocked consumer.
     */
    private Consumer<TileInfo> createConsumer(EasyMockSupport support, List<TileInfo> geometries)
    {
        @SuppressWarnings("unchecked")
        Consumer<TileInfo> consumer = support.createMock(Consumer.class);

        consumer.accept(EasyMock.isA(TileInfo.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            TileInfo info = (TileInfo)EasyMock.getCurrentArguments()[0];
            geometries.add(info);
            return null;
        }).anyTimes();

        return consumer;
    }

    /**
     * Creates a test export model.
     *
     * @return The export model.
     */
    private ExportModel createExportModel()
    {
        ExportModel model = new ExportModel(new File("./testfile.gpkg"));
        model.setProgressReporter(
                new ProgressReporter(new ProgressModel(), New.list(), CancellableTaskActivity.createActive("test")));
        model.setMaxZoomLevel(22);

        return model;
    }

    /**
     * Creates an easy mocked {@link GeometryRegistry}.
     *
     * @param support Used to create the mock.
     * @return The {@link GeometryRegistry}.
     */
    private GeometryRegistry createGeomRegistry(EasyMockSupport support)
    {
        ZOrderRenderProperties zProps = new MockZOrderRenderProperties();
        @SuppressWarnings("unchecked")
        ImageProvider<String> imageProvider = support.createNiceMock(ImageProvider.class);
        ImageManager imageManager = new ImageManager("key", imageProvider);

        TerrainTileGeometry.Builder<GeographicPosition> terrainBuilder = new TerrainTileGeometry.Builder<>();
        terrainBuilder.setBounds(ourZeroExpectedKeys.get(0).getBounds());
        terrainBuilder.setImageManager(imageManager);
        terrainBuilder.setDivider(this);
        TerrainTileGeometry eastern = new TerrainTileGeometry(terrainBuilder, zProps, ourLayerId);

        terrainBuilder = new TerrainTileGeometry.Builder<>();
        terrainBuilder.setBounds(ourZeroExpectedKeys.get(1).getBounds());
        terrainBuilder.setImageManager(imageManager);
        terrainBuilder.setDivider(this);
        TerrainTileGeometry western = new TerrainTileGeometry(terrainBuilder, zProps, ourLayerId);

        GeometryRegistry geomRegistry = support.createMock(GeometryRegistry.class);
        EasyMock.expect(geomRegistry.getGeometries()).andReturn(New.list(eastern, western));

        return geomRegistry;
    }
}
