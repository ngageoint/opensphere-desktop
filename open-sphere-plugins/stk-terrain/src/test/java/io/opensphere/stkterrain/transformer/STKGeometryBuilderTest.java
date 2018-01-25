package io.opensphere.stkterrain.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.geometry.AbstractTileGeometry.Divider;
import io.opensphere.core.geometry.TerrainTileGeometry;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.stkterrain.model.TileSetMetadata;

/**
 * Unit test for the {@link STKGeometryBuilder} class.
 */
public class STKGeometryBuilderTest
{
    /**
     * Tests building the initial geometries.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry registry = createDataRegistry(support);
        Divider<GeographicPosition> divider = createDivider(support);
        TileRenderProperties props = createProps(support);

        support.replayAll();

        String typeKey = "I am key";
        String serverUrl = "http://somehost/terrain";
        String tileSetName = "world";
        TileSetMetadata tileSetMetadata = new TileSetMetadata();
        tileSetMetadata.setProjection("WGS84");

        STKGeometryBuilder builder = new STKGeometryBuilder(registry);
        List<TerrainTileGeometry> geometries = builder.buildInitialGeometries(typeKey, serverUrl, tileSetName, tileSetMetadata, divider, props);

        assertEquals(2, geometries.size());
        TerrainTileGeometry western = geometries.get(0);
        GeographicBoundingBox expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, -180), LatLonAlt.createFromDegrees(90, 0));

        assertEquals(expected, western.getBounds());
        assertEquals(0, western.getGeneration());
        assertTrue(western.getImageManager().getImageProvider() instanceof STKTerrainImageProvider);
        ZYXImageKey expectedKey = new ZYXImageKey(0, 0, 0, expected);
        assertEquals(expectedKey.toString(), western.getImageManager().getImageKey().toString());
        assertTrue(western.getReader() instanceof STKElevationImageReader);
        assertEquals(props, western.getRenderProperties());
        assertEquals(divider, western.getSplitJoinRequestProvider());
        assertEquals(250, western.getMaximumDisplaySize());
        assertEquals(50, western.getMinimumDisplaySize());
        assertEquals(typeKey, western.getLayerId());

        TerrainTileGeometry eastern = geometries.get(1);
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, 0), LatLonAlt.createFromDegrees(90, 180));

        assertEquals(expected, eastern.getBounds());
        assertEquals(0, eastern.getGeneration());
        assertTrue(eastern.getImageManager().getImageProvider() instanceof STKTerrainImageProvider);
        expectedKey = new ZYXImageKey(0, 0, 1, expected);
        assertEquals(expectedKey.toString(), eastern.getImageManager().getImageKey().toString());
        assertTrue(eastern.getReader() instanceof STKElevationImageReader);
        assertEquals(props, eastern.getRenderProperties());
        assertEquals(divider, eastern.getSplitJoinRequestProvider());
        assertEquals(250, eastern.getMaximumDisplaySize());
        assertEquals(50, eastern.getMinimumDisplaySize());
        assertEquals(typeKey, eastern.getLayerId());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked data registry.
     *
     * @param support Used to create the mock.
     * @return The mocked data registry.
     */
    private DataRegistry createDataRegistry(EasyMockSupport support)
    {
        DataRegistry registry = support.createMock(DataRegistry.class);

        return registry;
    }

    /**
     * Creates an easy mocked divider.
     *
     * @param support Used to create the mock.
     * @return The mocked divider.
     */
    @SuppressWarnings("unchecked")
    private Divider<GeographicPosition> createDivider(EasyMockSupport support)
    {
        Divider<GeographicPosition> divider = support.createMock(Divider.class);

        return divider;
    }

    /**
     * Creates the {@link TileRenderProperties}.
     *
     * @param support Used to create the mock.
     * @return The mocked props.
     */
    private TileRenderProperties createProps(EasyMockSupport support)
    {
        TileRenderProperties props = support.createMock(TileRenderProperties.class);

        return props;
    }
}
