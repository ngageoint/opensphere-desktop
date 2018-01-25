package io.opensphere.core.util.jts;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.SimpleTesseraBlockBuilder;
import io.opensphere.core.projection.AbstractGeographicProjection.GeographicTesseraVertex;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.jts.PolygonTriangulationUtil.VertexGenerator;

/**
 * Unit test for {@link PolygonTriangulationUtil}.
 */
public class PolygonTriangulationUtilTest
{
    /**
     * Tests the triangulation.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        // POLYGON ((-86.5808500689736 34.69113349914551, -86.58084869384766
        // 34.6911340937625, -86.58084869384766 34.69113349914551,
        // -86.5808500689736 34.69113349914551))
        // [[-86.5808500689736,34.69113349914551],
        // [-86.58084869384766,34.6911340937625],
        // [-86.58084869384766,34.69113349914551]]

        SimpleTesseraBlockBuilder<GeographicTesseraVertex> triBuilder = new SimpleTesseraBlockBuilder<GeographicTesseraVertex>(3,
                Vector3d.ORIGIN);
        GeographicPosition pos1 = new GeographicPosition(LatLonAlt.createFromDegrees(34.691133, -86.58085));
        GeographicPosition pos2 = new GeographicPosition(LatLonAlt.createFromDegrees(34.691134, -86.58084));
        GeographicPosition pos3 = new GeographicPosition(LatLonAlt.createFromDegrees(34.691133, -86.58084));
        GeographicPosition pos4 = new GeographicPosition(LatLonAlt.createFromDegrees(34.691133, -86.58085));

        Polygon polygon = JTSUtilities.createJTSPolygon(New.list(pos1, pos2, pos3, pos4), New.list());

        VertexGenerator<GeographicTesseraVertex> generator = createGenerator(support);

        support.replayAll();

        PolygonTriangulationUtil.triangulatePolygon(triBuilder, polygon, generator);

        assertFalse(triBuilder.getBlockVertices().isEmpty());

        support.verifyAll();
    }

    /**
     * Tests the triangulation.
     */
    @Test
    public void testSmallPoly()
    {
        EasyMockSupport support = new EasyMockSupport();

        // POLYGON ((-86.5808500689736 34.69113349914551, -86.58084869384766
        // 34.6911340937625, -86.58084869384766 34.69113349914551,
        // -86.5808500689736 34.69113349914551))
        // [[-86.5808500689736,34.69113349914551],
        // [-86.58084869384766,34.6911340937625],
        // [-86.58084869384766,34.69113349914551]]

        SimpleTesseraBlockBuilder<GeographicTesseraVertex> triBuilder = new SimpleTesseraBlockBuilder<GeographicTesseraVertex>(3,
                Vector3d.ORIGIN);
        GeographicPosition pos1 = new GeographicPosition(LatLonAlt.createFromDegrees(34.69113349914551, -86.5808500689736));
        GeographicPosition pos2 = new GeographicPosition(LatLonAlt.createFromDegrees(34.6911340937625, -86.58084869384766));
        GeographicPosition pos3 = new GeographicPosition(LatLonAlt.createFromDegrees(34.69113349914551, -86.58084869384766));
        GeographicPosition pos4 = new GeographicPosition(LatLonAlt.createFromDegrees(34.69113349914551, -86.5808500689736));

        Polygon polygon = JTSUtilities.createJTSPolygon(New.list(pos1, pos2, pos3, pos4), New.list());

        VertexGenerator<GeographicTesseraVertex> generator = createGenerator(support);

        support.replayAll();

        PolygonTriangulationUtil.triangulatePolygon(triBuilder, polygon, generator);

        assertTrue(triBuilder.getBlockVertices().isEmpty());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link VertexGenerator}.
     *
     * @param support Used to create the mock.
     * @return The {@link VertexGenerator}.
     */
    private VertexGenerator<GeographicTesseraVertex> createGenerator(EasyMockSupport support)
    {
        @SuppressWarnings("unchecked")
        VertexGenerator<GeographicTesseraVertex> generator = support.createNiceMock(VertexGenerator.class);

        return generator;
    }
}
