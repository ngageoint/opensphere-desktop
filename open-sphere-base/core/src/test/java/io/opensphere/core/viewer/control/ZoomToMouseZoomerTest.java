package io.opensphere.core.viewer.control;

import static org.junit.Assert.assertEquals;

import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.MapManager;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;

/**
 * Tests the {@link ZoomToMouseZoomer}.
 */
public class ZoomToMouseZoomerTest
{
    /**
     * The expected mouse position.
     */
    private static final Vector2i ourMousePos = new Vector2i(50, 50);

    /**
     * The expected geographic position.
     */
    private static final GeographicPosition ourGeoPos = new GeographicPosition(LatLonAlt.createFromDegrees(10, -10));

    /**
     * Tests zooming in.
     */
    @Test
    public void testZoomInView()
    {
        EasyMockSupport support = new EasyMockSupport();

        MockAbstractviewerControlTranslator viewer = new MockAbstractviewerControlTranslator();
        Vector2i previousPos = new Vector2i(25, 25);
        MapManager mapManager = createMapManager(support, previousPos);

        support.replayAll();

        ZoomToMouseZoomer zoomer = new ZoomToMouseZoomer();
        MouseEvent event = new MouseEvent(new JPanel(), 0, 0, 0, ourMousePos.getX(), ourMousePos.getY(), 1, false);
        zoomer.zoomInView(viewer, mapManager, event);

        assertEquals(event, viewer.getZoomInEvent());
        assertEquals(ourMousePos, viewer.getMoveTo());
        assertEquals(previousPos, viewer.getMoveFrom());

        support.verifyAll();
    }

    /**
     * Tests zooming out.
     */
    @Test
    public void testZoomOutView()
    {
        EasyMockSupport support = new EasyMockSupport();

        MockAbstractviewerControlTranslator viewer = new MockAbstractviewerControlTranslator();
        Vector2i previousPos = new Vector2i(75, 75);
        MapManager mapManager = createMapManager(support, previousPos);

        support.replayAll();

        ZoomToMouseZoomer zoomer = new ZoomToMouseZoomer();
        MouseEvent event = new MouseEvent(new JPanel(), 0, 0, 0, ourMousePos.getX(), ourMousePos.getY(), 1, false);
        zoomer.zoomOutView(viewer, mapManager, event);

        assertEquals(event, viewer.getZoomOutEvent());
        assertEquals(ourMousePos, viewer.getMoveTo());
        assertEquals(previousPos, viewer.getMoveFrom());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link MapManager}.
     *
     * @param support Used to create the mock.
     * @param previousPos The position to return.
     * @return The mocked {@link MapManager}.
     */
    private MapManager createMapManager(EasyMockSupport support, Vector2i previousPos)
    {
        MapManager mapManager = support.createMock(MapManager.class);

        EasyMock.expect(mapManager.convertToPosition(EasyMock.eq(ourMousePos), EasyMock.eq(ReferenceLevel.ELLIPSOID)))
                .andReturn(ourGeoPos);

        EasyMock.expect(mapManager.convertToPoint(EasyMock.eq(ourGeoPos))).andReturn(previousPos);

        return mapManager;
    }
}
