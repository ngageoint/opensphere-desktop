package io.opensphere.stkterrain.transformer;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.TerrainTileGeometry;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.util.collections.New;
import io.opensphere.stkterrain.model.TileSetMetadata;

/**
 * Unit test for the {@link STKTerrainTileDivider}.
 */
public class STKTerrainTileDividerTest
{
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
     * The expected {@link ZYXImageKey} at zoom level 1.
     */
    private static final List<ZYXImageKey> ourTwoExpectedKeys = New.list(
            new ZYXImageKey(2, 3, 0,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(45, -180), LatLonAlt.createFromDegrees(90, -135))),
            new ZYXImageKey(2, 3, 1,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(45, -135), LatLonAlt.createFromDegrees(90, -90))),
            new ZYXImageKey(2, 2, 0,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, -180), LatLonAlt.createFromDegrees(45, -135))),
            new ZYXImageKey(2, 2, 1,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, -135), LatLonAlt.createFromDegrees(45, -90))),
            new ZYXImageKey(2, 3, 2,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(45, -90), LatLonAlt.createFromDegrees(90, -45))),
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
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(45, 135), LatLonAlt.createFromDegrees(90, 180))),
            new ZYXImageKey(2, 2, 6,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, 90), LatLonAlt.createFromDegrees(45, 135))),
            new ZYXImageKey(2, 2, 7,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, 135), LatLonAlt.createFromDegrees(45, 180))),
            new ZYXImageKey(2, 1, 0,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-45, -180), LatLonAlt.createFromDegrees(0, -135))),
            new ZYXImageKey(2, 1, 1,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-45, -135), LatLonAlt.createFromDegrees(0, -90))),
            new ZYXImageKey(2, 0, 0,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, -180), LatLonAlt.createFromDegrees(-45, -135))),
            new ZYXImageKey(2, 0, 1,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, -135), LatLonAlt.createFromDegrees(-45, -90))),
            new ZYXImageKey(2, 1, 2,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-45, -90), LatLonAlt.createFromDegrees(0, -45))),
            new ZYXImageKey(2, 1, 3,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-45, -45), LatLonAlt.createFromDegrees(0, 0))),
            new ZYXImageKey(2, 0, 2,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, -90), LatLonAlt.createFromDegrees(-45, -45))),
            new ZYXImageKey(2, 0, 3,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, -45), LatLonAlt.createFromDegrees(-45, 0))),
            new ZYXImageKey(2, 1, 4,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-45, 0), LatLonAlt.createFromDegrees(0, 45))),
            new ZYXImageKey(2, 1, 5,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-45, 45), LatLonAlt.createFromDegrees(0, 90))),
            new ZYXImageKey(2, 0, 4,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, 0), LatLonAlt.createFromDegrees(-45, 45))),
            new ZYXImageKey(2, 0, 5,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, 45), LatLonAlt.createFromDegrees(-45, 90))),
            new ZYXImageKey(2, 1, 6,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-45, 90), LatLonAlt.createFromDegrees(0, 135))),
            new ZYXImageKey(2, 1, 7,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-45, 135), LatLonAlt.createFromDegrees(0, 180))),
            new ZYXImageKey(2, 0, 6,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, 90), LatLonAlt.createFromDegrees(-45, 135))),
            new ZYXImageKey(2, 0, 7,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, 135), LatLonAlt.createFromDegrees(-45, 180))));

    /**
     * The expected {@link ZYXImageKey} at zoom level 0.
     */
    private static final List<ZYXImageKey> ourZeroExpectedKeys = New.list(
            new ZYXImageKey(0, 0, 0,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, -180), LatLonAlt.createFromDegrees(90, 0))),
            new ZYXImageKey(0, 0, 1,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, 0), LatLonAlt.createFromDegrees(90, 180))));

    /**
     * Tests dividing the tiles to zoom level 2.
     */
    @Test
    public void testDivide()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry dataRegistry = support.createMock(DataRegistry.class);
        TileRenderProperties props = support.createMock(TileRenderProperties.class);

        support.replayAll();

        String typeKey = "I am type key";
        String serverUrl = "http://somehost/terrain";
        String tileSetName = "world";
        TileSetMetadata tileSetMetadata = new TileSetMetadata();
        tileSetMetadata.setMaxzoom(2);

        STKTerrainTileDivider divider = new STKTerrainTileDivider(typeKey, tileSetMetadata);
        STKGeometryBuilder builder = new STKGeometryBuilder(dataRegistry);
        List<TerrainTileGeometry> geometries = builder.buildInitialGeometries(typeKey, serverUrl, tileSetName, tileSetMetadata,
                divider, props);

        assertGeometries(geometries, ourZeroExpectedKeys, divider);

        List<TerrainTileGeometry> zoomLevelOneGeoms = divideTiles(geometries, divider);

        assertGeometries(zoomLevelOneGeoms, ourOneExpectedKeys, divider);

        List<TerrainTileGeometry> zoomLevelTwoGeoms = divideTiles(zoomLevelOneGeoms, divider);

        assertGeometries(zoomLevelTwoGeoms, ourTwoExpectedKeys, null);

        support.verifyAll();
    }

    /**
     * Asserts the geometries.
     *
     * @param geometries The geometries to assert.
     * @param expectedKeys The expected {@link ZYXImageKey}s.
     * @param expectedDivider The expected divider to be set on the geometry.
     */
    private void assertGeometries(List<TerrainTileGeometry> geometries, List<ZYXImageKey> expectedKeys,
            STKTerrainTileDivider expectedDivider)
    {
        assertEquals(expectedKeys.size(), geometries.size());

        Map<String, TerrainTileGeometry> mappedGeoms = New.map();
        for (TerrainTileGeometry geometry : geometries)
        {
            mappedGeoms.put(geometry.getImageManager().getImageKey().toString(), geometry);
        }

        assertEquals(expectedKeys.size(), mappedGeoms.size());

        for (ZYXImageKey expected : expectedKeys)
        {
            TerrainTileGeometry geometry = mappedGeoms.get(expected.toString());
            ZYXImageKey imageKey = (ZYXImageKey)geometry.getImageManager().getImageKey();
            assertEquals(expected.toString(), imageKey.toString());
            assertEquals("Failed for image key " + imageKey.toString(), expected.getBounds(), imageKey.getBounds());
            assertEquals(expected.getBounds(), geometry.getBounds());
            assertEquals(expected.getZ(), geometry.getGeneration());
            assertEquals(expectedDivider, geometry.getSplitJoinRequestProvider());
            assertEquals("I am type key", geometry.getLayerId());
        }
    }

    /**
     * Divides the geometries into sub tiles.
     *
     * @param geometries The geometries to divide.
     * @param divider The divider to use.
     * @return The divided tiles.
     */
    private List<TerrainTileGeometry> divideTiles(List<TerrainTileGeometry> geometries, STKTerrainTileDivider divider)
    {
        List<TerrainTileGeometry> dividedGeoms = New.list();
        for (TerrainTileGeometry geometry : geometries)
        {
            Collection<AbstractTileGeometry<?>> subTiles = divider.divide(geometry);
            for (AbstractTileGeometry<?> subTile : subTiles)
            {
                dividedGeoms.add((TerrainTileGeometry)subTile);
            }
        }

        return dividedGeoms;
    }
}
