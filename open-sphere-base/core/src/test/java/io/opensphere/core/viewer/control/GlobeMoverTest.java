package io.opensphere.core.viewer.control;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.MapManager;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;

/**
 * Tests the {@link GlobeMover} class.
 */
public class GlobeMoverTest
{
    /**
     * Tests move globe.
     */
    @Test
    public void testMoveGlobe()
    {
        EasyMockSupport support = new EasyMockSupport();

        MockAbstractviewerControlTranslator viewer = new MockAbstractviewerControlTranslator();
        GeographicPosition position = new GeographicPosition(LatLonAlt.createFromDegrees(10d, 10d));
        Vector2i mouseScreenPos = new Vector2i(50, 50);
        Vector2i previousPos = new Vector2i(25, 25);
        MapManager mapManager = createMapManager(support, position, previousPos);

        support.replayAll();

        GlobeMover mover = new GlobeMover();

        mover.moveGlobe(viewer, mapManager, position, mouseScreenPos);
        mover.moveGlobe(viewer, mapManager, null, new Vector2i(10, 10));

        assertEquals(previousPos, viewer.getMoveFrom());
        assertEquals(mouseScreenPos, viewer.getMoveTo());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link MapManager}.
     *
     * @param support Used to create the mock.
     * @param position The position to expect.
     * @param previousPos The position to return.
     * @return The mocked {@link MapManager}.
     */
    private MapManager createMapManager(EasyMockSupport support, GeographicPosition position, Vector2i previousPos)
    {
        MapManager mapManager = support.createMock(MapManager.class);
        EasyMock.expect(mapManager.convertToPoint(EasyMock.eq(position))).andReturn(previousPos);

        return mapManager;
    }
}
