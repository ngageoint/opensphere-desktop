package io.opensphere.geopackage.export.tile.walker;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.TerrainTileGeometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.TileGeometry.Builder;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.image.ImageProvider;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;

/**
 * Unit test for {@link TopLevelGeometryProvider}.
 */
public class TopLevelGeometryProviderTest
{
    /**
     * Tests getting the appropriate top level geometries.
     */
    @Test
    public void testGetTopLevelGeometries()
    {
        EasyMockSupport support = new EasyMockSupport();

        TileRenderProperties props = support.createNiceMock(TileRenderProperties.class);
        ZOrderRenderProperties zProps = support.createNiceMock(ZOrderRenderProperties.class);
        @SuppressWarnings("unchecked")
        ImageProvider<String> imageProvider = support.createNiceMock(ImageProvider.class);
        ImageManager imageManager = new ImageManager("key", imageProvider);

        Geometry geometry = support.createMock(Geometry.class);

        Collection<Geometry> geometries = New.list();

        GeometryRegistry geomRegistry = createGeomRegistry(support, geometries);

        support.replayAll();

        GeographicBoundingBox outside = new GeographicBoundingBox(LatLonAlt.createFromDegrees(5, -5),
                LatLonAlt.createFromDegrees(10, .0001));
        GeographicBoundingBox intersects = new GeographicBoundingBox(LatLonAlt.createFromDegrees(5, -5),
                LatLonAlt.createFromDegrees(10, 15));
        GeographicBoundingBox contained = new GeographicBoundingBox(LatLonAlt.createFromDegrees(2, 2),
                LatLonAlt.createFromDegrees(7, 7));

        TileGeometry.Builder<GeographicPosition> builder = new Builder<>();
        builder.setBounds(outside);
        TileGeometry outSideGeom = new TileGeometry(builder, props, null, "notmine");

        builder = new Builder<>();
        builder.setBounds(intersects);
        TileGeometry intersectsGeom = new TileGeometry(builder, props, null, "mine");

        TerrainTileGeometry.Builder<GeographicPosition> terrainBuilder = new TerrainTileGeometry.Builder<>();
        terrainBuilder.setBounds(contained);
        terrainBuilder.setImageManager(imageManager);
        TerrainTileGeometry containedGeom = new TerrainTileGeometry(terrainBuilder, zProps, "mine");

        geometries.add(geometry);
        geometries.add(outSideGeom);
        geometries.add(intersectsGeom);
        geometries.add(containedGeom);

        TopLevelGeometryProvider provider = new TopLevelGeometryProvider(geomRegistry);
        List<AbstractTileGeometry<?>> tops = provider.getTopLevelGeometries("mine");

        assertEquals(2, tops.size());

        assertEquals(intersectsGeom.getBounds(), tops.get(0).getBounds());
        org.junit.Assert.assertNotSame(intersectsGeom, tops.get(0));
        assertEquals(containedGeom.getBounds(), tops.get(1).getBounds());
        org.junit.Assert.assertNotSame(containedGeom, tops.get(1));

        support.verifyAll();
    }

    /**
     * Creates the easy mocked {@link GeometryRegistry}.
     *
     * @param support Used to create the mock.
     * @param geometriesToReturn The geometries to return.
     * @return The mocked {@link GeometryRegistry}.
     */
    private GeometryRegistry createGeomRegistry(EasyMockSupport support, Collection<Geometry> geometriesToReturn)
    {
        GeometryRegistry geoRegistry = support.createMock(GeometryRegistry.class);

        EasyMock.expect(geoRegistry.getGeometries()).andReturn(geometriesToReturn);

        return geoRegistry;
    }
}
