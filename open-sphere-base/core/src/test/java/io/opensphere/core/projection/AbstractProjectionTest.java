package io.opensphere.core.projection;

import static org.easymock.EasyMock.createStrictMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.math.Ellipsoid;
import io.opensphere.core.math.Ray3d;
import io.opensphere.core.math.Sphere;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.Tessera;
import io.opensphere.core.model.TesseraList;
import io.opensphere.core.projection.AbstractGeographicProjection.GeographicProjectedTesseraVertex;
import io.opensphere.core.projection.ProjectionChangeSupport.ProjectionChangeListener;
import io.opensphere.core.terrain.util.ElevationChangedEvent;
import io.opensphere.core.terrain.util.ElevationManager;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.viewer.Viewer;

/**
 * Test class to validate the functionality of the {@link AbstractProjection}
 * class.
 */
public class AbstractProjectionTest
{
    /**
     * The object on which tests are performed.
     */
    private AbstractProjection myTestObject;

    /**
     * Creates the resources needed to execute the tests.
     *
     * @throws java.lang.Exception if the resources cannot be initialized.
     */
    @Before
    public void setUp() throws Exception
    {
        // easymock 2.5.2 won't mock abstract classes, so create a dummy impl:
        myTestObject = new AbstractProjection()
        {
            @Override
            public void resetProjection(boolean highAccuracy)
            {
                /* intentionally blank */
            }

            @Override
            public Vector3d getTerrainIntersection(Ray3d ray, Viewer view)
            {
                return null;
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
            public double getModelWidth()
            {
                return 0;
            }

            @Override
            public double getModelHeight()
            {
                return 0;
            }

            @Override
            public double getMinimumTerrainDistance(Viewer view)
            {
                return 0;
            }

            @Override
            public ElevationManager getElevationManager()
            {
                return null;
            }

            @Override
            public Sphere getBoundingSphere(BoundingBox<GeographicPosition> bbox, Vector3d modelCenter, boolean forceGenerate)
            {
                return null;
            }

            @Override
            public Ellipsoid getBoundingEllipsoid(BoundingBox<GeographicPosition> bbox, Vector3d modelCenter,
                    boolean forceGenerate)
            {
                return null;
            }

            @Override
            public TesseraList<? extends GeographicProjectedTesseraVertex> convertTriangleToModel(GeographicPosition vert1,
                    GeographicPosition vert2, GeographicPosition vert3, Vector3d modelCenter)
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
                return null;
            }

            @Override
            public TesseraList<? extends GeographicProjectedTesseraVertex> convertQuadToModel(GeographicPosition lowerLeft,
                    GeographicPosition lowerRight, GeographicPosition upperRight, GeographicPosition upperLeft,
                    Vector3d modelCenter)
            {
                return null;
            }

            @Override
            public Collection<Vector3d> convertPositionsToModel(Collection<? extends GeographicPosition> positions,
                    Vector3d modelCenter)
            {
                return null;
            }

            @Override
            public TesseraList<? extends GeographicProjectedTesseraVertex> convertPolygonToModelMesh(Polygon polygon,
                    Vector3d modelCenter)
            {
                return null;
            }

            @Override
            public List<Vector3d> convertLinesToModel(List<? extends GeographicPosition> positions, int limit, LineType type,
                    Vector3d modelCenter)
            {
                return null;
            }

            @Override
            public Pair<Tessera<GeographicPosition>, ProjectionCursor> convertLineToModel(ProjectionCursor start,
                    GeographicPosition end, LineType type, Vector3d modelCenter)
            {
                return null;
            }

            @Override
            public Pair<Tessera<GeographicPosition>, ProjectionCursor> convertLineToModel(GeographicPosition start,
                    GeographicPosition end, LineType type, Vector3d modelCenter)
            {
                return null;
            }
        };
    }

    /**
     * Creates the resources needed to execute the tests.
     */
    @BeforeClass
    public static void setUpBeforeClass()
    {
        AbstractProjection.setTerrainLocked(false);
    }

    /**
     * Test method for {@link AbstractProjection#isTerrainLocked()}.
     */
    @Test
    public void testIsTerrainLocked()
    {
        assertFalse(AbstractProjection.isTerrainLocked());
    }

    /**
     * Test method for {@link AbstractProjection#setTerrainLocked(boolean)}.
     */
    @Test
    public void testSetTerrainLocked()
    {
        AbstractProjection.setTerrainLocked(true);
        assertTrue(AbstractProjection.isTerrainLocked());
        AbstractProjection.setTerrainLocked(false);
        assertFalse(AbstractProjection.isTerrainLocked());
    }

    /**
     * Test method for {@link AbstractProjection#toggleTerrainLocked()}.
     */
    @Test
    public void testToggleTerrainLocked()
    {
        AbstractProjection.setTerrainLocked(false);
        assertFalse(AbstractProjection.isTerrainLocked());
        AbstractProjection.toggleTerrainLocked();
        assertTrue(AbstractProjection.isTerrainLocked());
        AbstractProjection.toggleTerrainLocked();
        assertFalse(AbstractProjection.isTerrainLocked());
    }

    /**
     * Test method for {@link AbstractProjection#getActivationTimestamp()}.
     */
    @Test
    public void testGetActivationTimestamp()
    {
        long currentTime = System.currentTimeMillis();
        myTestObject.setActivationTimestamp();
        assertTrue(myTestObject.getActivationTimestamp() >= currentTime);
    }

    /**
     * Test method for {@link AbstractProjection#getCreationTimestamp()}.
     */
    @Test
    public void testGetCreationTimestamp()
    {
        long currentTime = System.currentTimeMillis();
        assertTrue(myTestObject.getCreationTimestamp() <= currentTime);
    }

    /**
     * Test method for
     * {@link AbstractProjection#getDistanceFromModelCenterM(GeographicPosition)}
     * .
     */
    @Test
    public void testGetDistanceFromModelCenterM()
    {
        // default implementation always returns zero:
        assertEquals(0.0, myTestObject.getDistanceFromModelCenterM(null), 0);
    }

    /**
     * Test method for
     * {@link AbstractProjection#getElevationOnTerrainM(GeographicPosition)} .
     */
    @Test
    public void testGetElevationOnTerrainM()
    {
        // default implementation always returns zero:
        assertEquals(0.0, myTestObject.getElevationOnTerrainM(null), 0);
    }

    /**
     * Test method for {@link AbstractProjection#getModelCenter()}.
     */
    @Test
    public void testGetModelCenter()
    {
        assertEquals(Vector3d.ORIGIN, myTestObject.getModelCenter());
    }

    /**
     * Test method for {@link AbstractProjection#getModelViewAdjustment()}.
     */
    @Test
    public void testGetModelViewAdjustment()
    {
        assertNull(myTestObject.getModelViewAdjustment());
    }

    /**
     * Test method for {@link AbstractProjection#getSnapshot()}.
     */
    @Test
    public void testGetSnapshot()
    {
        assertEquals(myTestObject, myTestObject.getSnapshot());
    }

    /**
     * Test method for
     * {@link AbstractProjection#handleElevationChange(ElevationChangedEvent)} .
     */
    @Test
    public void testHandleElevationChange()
    {
        // default implementation always returns null
        assertNull(myTestObject.handleElevationChange(null));
    }

    /**
     * Test method for {@link AbstractProjection#handleModelDensityChanged(int)}
     * .
     */
    @Test
    public void testHandleModelDensityChanged()
    {
        // default implementation always returns null
        assertNull(myTestObject.handleModelDensityChanged(0));
    }

    /**
     * Test method for {@link AbstractProjection#isModelCenterLocked()}.
     */
    @Test
    public void testIsModelCenterLocked()
    {
        assertFalse(myTestObject.isModelCenterLocked());
        myTestObject.setModelCenterLocked(true);
        assertTrue(myTestObject.isModelCenterLocked());
        myTestObject.setModelCenterLocked(false);
        // the center lock is sticky, verify that the above false call doesn't
        // change it
        assertTrue(myTestObject.isModelCenterLocked());
    }

    /**
     * Test method for
     * {@link AbstractProjection#isOutsideModel(io.opensphere.core.math.Vector3d)}
     * .
     */
    @Test
    public void testIsOutsideModel()
    {
        // default implementation always returns false
        assertFalse(myTestObject.isOutsideModel(null));
    }

    /**
     * Test method for
     * {@link AbstractProjection#setModelCenter(io.opensphere.core.math.Vector3d)}
     * .
     */
    @Test
    public void testSetModelCenter()
    {
        myTestObject.setModelCenter(Vector3d.UNIT_X);
        assertEquals(Vector3d.UNIT_X, myTestObject.getModelCenter());
        assertNotNull(myTestObject.getModelViewAdjustment());
        assertEquals(Vector3d.UNIT_X.getX(), myTestObject.getModelViewAdjustment().get(0, 3), 0);
        assertEquals(Vector3d.UNIT_X.getY(), myTestObject.getModelViewAdjustment().get(1, 3), 0);
        assertEquals(Vector3d.UNIT_X.getZ(), myTestObject.getModelViewAdjustment().get(2, 3), 0);
    }

    /**
     * Test method for
     * {@link AbstractProjection#setModelCenter(io.opensphere.core.math.Vector3d)}
     * .
     */
    @Test
    public void testSetModelCenterOrigin()
    {
        myTestObject.setModelCenter(Vector3d.ORIGIN);
        assertEquals(Vector3d.ORIGIN, myTestObject.getModelCenter());
        assertNull(myTestObject.getModelViewAdjustment());
    }

    /**
     * Test method for
     * {@link AbstractProjection#setProjectionChangeListener(ProjectionChangeSupport.ProjectionChangeListener)}
     * .
     */
    @Test
    public void testProjectionChangeListener()
    {
        ProjectionChangeListener mockListener = createStrictMock(ProjectionChangeListener.class);
        myTestObject.setProjectionChangeListener(mockListener);

        assertEquals(mockListener, myTestObject.getProjectionChangeListener());
    }

    /**
     * Test method for
     * {@link AbstractProjection#useElevationOrderManager(io.opensphere.core.order.OrderManager)}
     * .
     */
    @Test
    public void testUseElevationOrderManager()
    {
        myTestObject.useElevationOrderManager(null);
    }
}
