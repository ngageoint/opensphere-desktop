package io.opensphere.core.terrain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.impl.Earth3D;

/**
 * Test for {@link TerrainTriangle}.
 */
public class TerrainTriangleTest
{
    /** The globe which will have the terrain triangles for testing. */
    private static final ImmutableTriangleGlobeModel GLOBE = new ImmutableTriangleGlobeModel(
            new TriangleGlobeModel(13, 25, new Earth3D()));

    /**
     * Test position containment for a triangle.
     */
    @Test
    public void testTriangleContainment()
    {
        GeographicPosition inPos = new GeographicPosition(LatLonAlt.createFromDegrees(12., 10.));
        TerrainTriangle tri = GLOBE.getContainingTriangle(inPos, null);

        // make sure that the point is actually in the triangle.
        assertTrue(tri.contains(inPos));

        // a position which is outside of the triangle
        GeographicPosition outPos1 = new GeographicPosition(LatLonAlt.createFromDegrees(17., -1.));
        assertFalse(tri.contains(outPos1));

        // a position which is outside of the triangle on a different side
        GeographicPosition outPos2 = new GeographicPosition(LatLonAlt.createFromDegrees(10., 7.));
        assertFalse(tri.contains(outPos2));

        // a position which is outside of the triangle on the third side
        GeographicPosition outPos3 = new GeographicPosition(LatLonAlt.createFromDegrees(15., 10.));
        assertFalse(tri.contains(outPos3));
    }
}
