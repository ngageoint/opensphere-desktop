package io.opensphere.core.projection;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import io.opensphere.core.math.Ellipsoid;
import io.opensphere.core.math.Ray3d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.TesseraList;
import io.opensphere.core.model.TesseraList.TesseraBlock;
import io.opensphere.core.projection.AbstractGeographicProjection.GeographicProjectedTesseraVertex;
import io.opensphere.core.terrain.util.ElevationManager;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntList;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.Viewer.ViewerPosition;

/**
 * Test class to validate the functionality of the
 * {@link AbstractGeographicProjection} class.
 */
public class AbstractGeographicProjectionTest
{
    /**
     * Failure message.
     */
    private static final String NOT_YET_IMPLEMENTED = "Not yet implemented";

    /**
     * The object on which tests are performed.
     */
    private AbstractGeographicProjection myTestObject;

    /**
     * Creates the resources needed to execute the tests.
     *
     * @throws java.lang.Exception if the resources cannot be initialized.
     */
    @Before
    public void setUp() throws Exception
    {
        // easymock 2.5.2 won't mock abstract classes, so create a dummy impl:
        myTestObject = new AbstractGeographicProjection()
        {
            @Override
            public Vector3d getTerrainIntersection(Ray3d ray, Viewer view)
            {
                return Vector3d.ORIGIN;
            }

            @Override
            public Vector3d getSurfaceIntersection(Vector3d pointA, Vector3d pointB)
            {
                return null;
            }

            @Override
            public Vector3d getNormalAtPosition(GeographicPosition inPos)
            {
                return null;
            }

            @Override
            public String getName()
            {
                return null;
            }

            @Override
            public ElevationManager getElevationManager()
            {
                return null;
            }

            @Override
            public GeographicPosition convertToPosition(Vector3d inPos, ReferenceLevel altReference)
            {
                return null;
            }

            @Override
            public Vector3d convertToModel(GeographicPosition inPos, Vector3d modelCenter)
            {
                return Vector3d.ORIGIN;
            }
        };
    }

    /**
     * Test method for
     * {@link AbstractGeographicProjection#convertLinesToModel(List, int, LineType, Vector3d)}
     * .
     */
    @Test
    @Ignore
    public void testConvertLinesToModel()
    {
        // TODO
        fail(NOT_YET_IMPLEMENTED);
    }

    /**
     * Test method for
     * {@link AbstractGeographicProjection#convertLineToModel(GeographicPosition, GeographicPosition, LineType, Vector3d)}
     * .
     */
    @Test
    @Ignore
    public void testConvertLineToModelGeographicPositionGeographicPositionLineTypeVector3d()
    {
        // TODO
        fail(NOT_YET_IMPLEMENTED);
    }

    /**
     * Test method for
     * {@link AbstractGeographicProjection#convertLineToModel(Projection.ProjectionCursor, GeographicPosition, LineType, Vector3d)}
     * .
     */
    @Test
    @Ignore
    public void testConvertLineToModelProjectionCursorGeographicPositionLineTypeVector3d()
    {
        // TODO
        fail(NOT_YET_IMPLEMENTED);
    }

    /**
     * Test method for
     * {@link AbstractGeographicProjection#convertPolygonToModelMesh(com.vividsolutions.jts.geom.Polygon, Vector3d)}
     * .
     */
    @Test
    @Ignore
    public void testConvertPolygonToModelMesh()
    {
        // TODO
        fail(NOT_YET_IMPLEMENTED);
    }

    /**
     * Test method for
     * {@link AbstractGeographicProjection#convertPositionsToModel(Collection, Vector3d)}
     * .
     */
    @Test
    public void testConvertPositionsToModel()
    {
        Collection<GeographicPosition> positions = new ArrayList<>();
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(0, 0)));

        Collection<Vector3d> results = myTestObject.convertPositionsToModel(positions, Vector3d.ORIGIN);
        assertEquals(1, results.size());

        // the AbstractGeographicProjection relies on subclasses to implement
        // the convertToModel method, and the test
        // provides a default implementation that returns the origin. Therefore,
        // it is sufficient to check for the
        // origin here
        assertEquals(Vector3d.ORIGIN, results.iterator().next());
    }

    /**
     * Test method for
     * {@link AbstractGeographicProjection#convertQuadToModel(GeographicPosition, GeographicPosition, GeographicPosition, GeographicPosition, Vector3d)}
     * .
     */
    @Test
    public void testConvertQuadToModel()
    {
        GeographicPosition pos1 = new GeographicPosition(LatLonAlt.createFromDegrees(0, 0));
        GeographicPosition pos2 = new GeographicPosition(LatLonAlt.createFromDegrees(1, 1));
        GeographicPosition pos3 = new GeographicPosition(LatLonAlt.createFromDegrees(2, 2));
        GeographicPosition pos4 = new GeographicPosition(LatLonAlt.createFromDegrees(3, 3));

        TesseraList<? extends GeographicProjectedTesseraVertex> results = myTestObject.convertQuadToModel(pos1, pos2, pos3, pos4,
                Vector3d.ORIGIN);

        assertEquals(1, results.getTesseraBlocks().size());
        assertEquals(4, results.getTesseraBlocks().get(0).getTesseraVertexCount());

        // the AbstractGeographicProjection relies on subclasses to implement
        // the convertToModel method, and the test
        // provides a default implementation that returns null. Therefore, it is
        // sufficient to skip value checks here.
    }

    /**
     * Test method for
     * {@link AbstractGeographicProjection#convertTriangleToModel(GeographicPosition, GeographicPosition, GeographicPosition, Vector3d)}
     * .
     */
    @Test
    public void testConvertTriangleToModel()
    {
        GeographicPosition pos1 = new GeographicPosition(LatLonAlt.createFromDegrees(0, 0));
        GeographicPosition pos2 = new GeographicPosition(LatLonAlt.createFromDegrees(1, 1));
        GeographicPosition pos3 = new GeographicPosition(LatLonAlt.createFromDegrees(2, 2));

        TesseraList<? extends GeographicProjectedTesseraVertex> results = myTestObject.convertTriangleToModel(pos1, pos2, pos3,
                Vector3d.ORIGIN);

        assertEquals(1, results.getTesseraBlocks().size());
        assertEquals(3, results.getTesseraBlocks().get(0).getTesseraVertexCount());

        // the AbstractGeographicProjection relies on subclasses to implement
        // the convertToModel method, and the test
        // provides a default implementation that returns null. Therefore, it is
        // sufficient to skip value checks here.
    }

    /**
     * Test method for
     * {@link AbstractGeographicProjection#getBoundingEllipsoid(BoundingBox, Vector3d, boolean)}
     * .
     */
    @Test
    @Ignore
    public void testGetBoundingEllipsoid()
    {
        // TODO
        fail(NOT_YET_IMPLEMENTED);
    }

    /**
     * Test method for
     * {@link AbstractGeographicProjection#getBoundingSphere(BoundingBox, Vector3d, boolean)}
     * .
     */
    @Test
    @Ignore
    public void testGetBoundingSphere()
    {
        // TODO
        fail(NOT_YET_IMPLEMENTED);
    }

    /**
     * Test method for
     * {@link AbstractGeographicProjection#getMinimumTerrainDistance(io.opensphere.core.viewer.Viewer)}
     * .
     */
    @Test
    public void testGetMinimumTerrainDistance()
    {
        Viewer mockViewer = createStrictMock(Viewer.class);
        ViewerPosition mockViewerPosition = createStrictMock(ViewerPosition.class);

        expect(mockViewer.getPosition()).andReturn(mockViewerPosition);
        expect(mockViewerPosition.getLocation()).andReturn(Vector3d.ORIGIN);

        replay(mockViewer, mockViewerPosition);

        assertEquals(0, myTestObject.getMinimumTerrainDistance(mockViewer), 0);

        verify(mockViewer, mockViewerPosition);
    }

    /**
     * Test method for {@link AbstractGeographicProjection#getModelHeight()}.
     */
    @Test
    public void testGetModelHeight()
    {
        assertEquals(0.0, myTestObject.getModelHeight(), 0);
    }

    /**
     * Test method for {@link AbstractGeographicProjection#getModelWidth()}.
     */
    @Test
    public void testGetModelWidth()
    {
        assertEquals(0.0, myTestObject.getModelWidth(), 0);
    }

    /**
     * Test method for
     * {@link AbstractGeographicProjection#resetProjection(boolean)}.
     */
    @Test
    public void testResetProjection()
    {
        myTestObject.resetProjection(false);
    }

    /**
     * Test method for
     * {@link AbstractGeographicProjection#cacheEllipsoid(BoundingBox, Ellipsoid)}
     * .
     */
    @Test
    public void testCacheEllipsoid()
    {
        BoundingBox<GeographicPosition> mockBox = createStrictMock(BoundingBox.class);
        Ellipsoid mockEllipsoid = createStrictMock(Ellipsoid.class);

        myTestObject.cacheEllipsoid(mockBox, mockEllipsoid);

        assertEquals(mockEllipsoid, myTestObject.getEllipsoidFromCache(mockBox));
    }

    /**
     * Test method for
     * {@link AbstractGeographicProjection#quadTessellate(GeographicPosition, GeographicPosition, GeographicPosition, GeographicPosition, int, int, Vector3d)}
     * .
     */
    @Test
    @Ignore
    public void testQuadTessellateGeographicPositionGeographicPositionGeographicPositionGeographicPositionIntIntVector3d()
    {
        // TODO
        fail(NOT_YET_IMPLEMENTED);
    }

    /**
     * Test method for
     * {@link AbstractGeographicProjection#quadTessellate(GeographicPosition, GeographicPosition, GeographicPosition, GeographicPosition, Vector3d)}
     * .
     */
    @Test
    public void testQuadTessellateGeographicPositionGeographicPositionGeographicPositionGeographicPositionVector3d()
    {
        GeographicPosition pos1 = new GeographicPosition(LatLonAlt.createFromDegrees(0, 0));
        GeographicPosition pos2 = new GeographicPosition(LatLonAlt.createFromDegrees(1, 1));
        GeographicPosition pos3 = new GeographicPosition(LatLonAlt.createFromDegrees(2, 2));
        GeographicPosition pos4 = new GeographicPosition(LatLonAlt.createFromDegrees(3, 3));

        TesseraList<GeographicProjectedTesseraVertex> results = myTestObject.quadTessellate(pos1, pos2, pos3, pos4,
                Vector3d.ORIGIN);

        assertEquals(1, results.getTesseraBlocks().size());

        TesseraBlock<? extends GeographicProjectedTesseraVertex> vertexBlock = results.getTesseraBlocks().get(0);
        assertEquals(4, vertexBlock.getTesseraVertexCount());

        assertEquals(4, vertexBlock.getIndices().size());

        assertEquals(0, vertexBlock.getIndices().get(0));
        assertEquals(1, vertexBlock.getIndices().get(1));
        assertEquals(2, vertexBlock.getIndices().get(2));
        assertEquals(3, vertexBlock.getIndices().get(3));

        assertFalse(vertexBlock.isPetrified());

        assertEquals(4, vertexBlock.getVertices().size());

        assertEquals(0.0, vertexBlock.getVertices().get(0).getCoordinates().getLat().getMagnitude(), 0);
        assertEquals(0.0, vertexBlock.getVertices().get(0).getCoordinates().getLon().getMagnitude(), 0);
        assertEquals(0.0, vertexBlock.getVertices().get(0).getCoordinates().getAlt().getKilometers(), 0);

        assertEquals(1.0, vertexBlock.getVertices().get(1).getCoordinates().getLat().getMagnitude(), 0);
        assertEquals(1.0, vertexBlock.getVertices().get(1).getCoordinates().getLon().getMagnitude(), 0);
        assertEquals(0.0, vertexBlock.getVertices().get(1).getCoordinates().getAlt().getKilometers(), 0);

        assertEquals(2.0, vertexBlock.getVertices().get(2).getCoordinates().getLat().getMagnitude(), 0);
        assertEquals(2.0, vertexBlock.getVertices().get(2).getCoordinates().getLon().getMagnitude(), 0);
        assertEquals(0.0, vertexBlock.getVertices().get(2).getCoordinates().getAlt().getKilometers(), 0);

        assertEquals(3.0, vertexBlock.getVertices().get(3).getCoordinates().getLat().getMagnitude(), 0);
        assertEquals(3.0, vertexBlock.getVertices().get(3).getCoordinates().getLon().getMagnitude(), 0);
        assertEquals(0.0, vertexBlock.getVertices().get(3).getCoordinates().getAlt().getKilometers(), 0);
    }

    /**
     * Test method for
     * {@link AbstractGeographicProjection#quadTessellate(List, int, int, int, int, int, int)}
     * .
     */
    @Test
    @Ignore
    public void testQuadTessellateListOfGeographicTesseraVertexIntIntIntIntIntInt()
    {
        // TODO
        fail(NOT_YET_IMPLEMENTED);
    }

    /**
     * Test method for
     * {@link AbstractGeographicProjection#quadTessellate(List, PetrifyableTIntList, Vector3d)}
     * .
     */
    @Test
    @Ignore
    public void testQuadTessellateListOfGeographicTesseraVertexPetrifyableTIntListVector3d()
    {
        // TODO
        fail(NOT_YET_IMPLEMENTED);
    }
}
