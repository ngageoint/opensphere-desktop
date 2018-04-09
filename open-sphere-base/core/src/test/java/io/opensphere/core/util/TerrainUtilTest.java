package io.opensphere.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.MutableGeographicProjection;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.impl.Earth3D;
import io.opensphere.core.terrain.TriangleGlobeModel;
import io.opensphere.core.terrain.util.ElevationManager;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * Unit test for {@link TerrainUtil} class.
 */
public class TerrainUtilTest extends MutableGeographicProjection
{
    /**
     * Constructor.
     */
    public TerrainUtilTest()
    {
        super(new TriangleGlobeModel(0, 50, new Earth3D()));
    }

    @Override
    public void generateSnapshot()
    {
    }

    @Override
    public String getName()
    {
        return "myname";
    }

    /**
     * Tests getting the elevation in meters.
     */
    @Test
    public void testGetElevationInMeters()
    {
        EasyMockSupport support = new EasyMockSupport();

        GeographicPosition pos = new GeographicPosition(LatLonAlt.createFromDegrees(10, 9));
        MapContext<DynamicViewer> mapContext = createMapContext(support, new MockedElevationManager(pos));

        support.replayAll();

        double elevation = TerrainUtil.getInstance().getElevationInMeters(mapContext, pos);

        assertEquals(1001.1, elevation, 0d);

        support.verifyAll();
    }

    /**
     * Tests getting the elevation in 2d mode.
     */
    @Test
    public void testGetElevationIn2d()
    {
        EasyMockSupport support = new EasyMockSupport();

        GeographicPosition pos = new GeographicPosition(LatLonAlt.createFromDegrees(10, 9));
        MapContext<DynamicViewer> mapContext = createMapContext(support, null);

        support.replayAll();

        double elevation = TerrainUtil.getInstance().getElevationInMeters(mapContext, pos);

        assertEquals(0d, elevation, 0d);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked map context.
     *
     * @param support Used to create the mock.
     * @param elevationManager The elevation manager to return.
     * @return The mocked {@link MapContext}.
     */
    private MapContext<DynamicViewer> createMapContext(EasyMockSupport support, ElevationManager elevationManager)
    {
        Projection projection = support.createMock(Projection.class);
        EasyMock.expect(projection.getElevationManager()).andReturn(elevationManager).atLeastOnce();

        @SuppressWarnings("unchecked")
        MapContext<DynamicViewer> mapContext = support.createMock(MapContext.class);
        EasyMock.expect(mapContext.getRawProjection()).andReturn(projection);

        return mapContext;
    }

    /**
     * Mocked {@link ElevationManager} class used for testing.
     */
    private static class MockedElevationManager extends ElevationManager
    {
        /**
         * The expected geo position.
         */
        private final GeographicPosition myExpectedPos;

        /**
         * Constructor.
         *
         * @param pos The expected position.
         */
        public MockedElevationManager(GeographicPosition pos)
        {
            myExpectedPos = pos;
        }

        @Override
        public double getElevationM(GeographicPosition position, boolean approximate)
        {
            assertTrue(approximate);
            assertEquals(myExpectedPos, position);

            return 1001.1;
        }
    }
}
