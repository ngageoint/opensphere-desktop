package io.opensphere.geopackage.export.tile.walker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.TileGeometry.Builder;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;

/**
 * Unit test for {@link GeometryCalculator}.
 */
public class GeometryCalculatorTest
{
    /**
     * Tests the anyAtFullContainment method.
     */
    @Test
    public void testAnyAtFullContainment()
    {
        EasyMockSupport support = new EasyMockSupport();

        TileRenderProperties props = support.createNiceMock(TileRenderProperties.class);

        support.replayAll();

        GeographicBoundingBox boundingBox = new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, 0),
                LatLonAlt.createFromDegrees(10, 10));

        GeographicBoundingBox outside = new GeographicBoundingBox(LatLonAlt.createFromDegrees(5, -5),
                LatLonAlt.createFromDegrees(10, .0001));
        GeographicBoundingBox intersects = new GeographicBoundingBox(LatLonAlt.createFromDegrees(5, -5),
                LatLonAlt.createFromDegrees(10, 15));
        GeographicBoundingBox contained = new GeographicBoundingBox(LatLonAlt.createFromDegrees(2, 2),
                LatLonAlt.createFromDegrees(7, 7));

        TileGeometry.Builder<GeographicPosition> builder = new Builder<>();
        builder.setBounds(outside);
        TileGeometry outSideGeom = new TileGeometry(builder, props, null);

        builder = new Builder<>();
        builder.setBounds(intersects);
        TileGeometry intersectsGeom = new TileGeometry(builder, props, null);

        builder = new Builder<>();
        builder.setBounds(contained);
        TileGeometry containedGeom = new TileGeometry(builder, props, null);

        List<TileGeometry> trueGeoms = New.list(outSideGeom, intersectsGeom, containedGeom);
        List<TileGeometry> falseGeoms = New.list(outSideGeom, intersectsGeom);

        GeometryCalculator calc = new GeometryCalculator();

        assertTrue(calc.anyAtFullContainment(trueGeoms, boundingBox));
        assertFalse(calc.anyAtFullContainment(falseGeoms, boundingBox));

        support.verifyAll();
    }

    /**
     * Tests getting containing geometries.
     */
    @Test
    public void testGetContainingGeometries()
    {
        EasyMockSupport support = new EasyMockSupport();

        TileRenderProperties props = support.createNiceMock(TileRenderProperties.class);

        support.replayAll();

        GeographicBoundingBox boundingBox = new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, 0),
                LatLonAlt.createFromDegrees(10, 10));

        GeographicBoundingBox outside = new GeographicBoundingBox(LatLonAlt.createFromDegrees(5, -5),
                LatLonAlt.createFromDegrees(10, -.0001));
        GeographicBoundingBox intersects = new GeographicBoundingBox(LatLonAlt.createFromDegrees(5, -5),
                LatLonAlt.createFromDegrees(10, 15));
        GeographicBoundingBox contained = new GeographicBoundingBox(LatLonAlt.createFromDegrees(2, 2),
                LatLonAlt.createFromDegrees(7, 7));
        GeographicBoundingBox overlapping = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-10, -10),
                LatLonAlt.createFromDegrees(15, 15));

        TileGeometry.Builder<GeographicPosition> builder = new Builder<>();
        builder.setBounds(outside);
        TileGeometry outSideGeom = new TileGeometry(builder, props, null);

        builder = new Builder<>();
        builder.setBounds(intersects);
        TileGeometry intersectsGeom = new TileGeometry(builder, props, null);

        builder = new Builder<>();
        builder.setBounds(contained);
        TileGeometry containedGeom = new TileGeometry(builder, props, null);

        builder = new Builder<>();
        builder.setBounds(overlapping);
        TileGeometry overlappingGeom = new TileGeometry(builder, props, null);

        List<AbstractTileGeometry<?>> geoms = New.list(outSideGeom, intersectsGeom, containedGeom, overlappingGeom);

        GeometryCalculator calc = new GeometryCalculator();
        List<AbstractTileGeometry<?>> viewable = calc.getContainingGeometries(geoms, boundingBox);

        assertEquals(3, viewable.size());
        assertTrue(viewable.contains(intersectsGeom));
        assertTrue(viewable.contains(containedGeom));
        assertTrue(viewable.contains(overlappingGeom));

        support.verifyAll();
    }
}
