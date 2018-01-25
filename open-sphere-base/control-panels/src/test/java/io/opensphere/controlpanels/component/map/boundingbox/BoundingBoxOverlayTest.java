package io.opensphere.controlpanels.component.map.boundingbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import io.opensphere.controlpanels.component.map.GraphicsMock;
import io.opensphere.controlpanels.component.map.model.MapModel;
import io.opensphere.core.math.LineSegment2d;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.collections.New;

/**
 * Tests the BoundingBoxOverlay.
 *
 */
public class BoundingBoxOverlayTest
{
    /**
     * Tests drawing line segments.
     */
    @Test
    public void test()
    {
        MapModel mapModel = new MapModel();
        GeographicBoundingBox boundingBox = new GeographicBoundingBox(LatLonAlt.createFromDegrees(0d, 0d),
                LatLonAlt.createFromDegrees(45d, 90d));
        mapModel.setRegion(boundingBox);
        mapModel.setHeightWidth(400, 400);
        mapModel.setViewport(new ScreenBoundingBox(new ScreenPosition(0d, 0d), new ScreenPosition(400, 400)));

        GraphicsMock graphics = new GraphicsMock();

        BoundingBoxOverlay overlay = new BoundingBoxOverlay(mapModel);
        overlay.draw(graphics);

        Vector2d lowerLeft = new Vector2d(200, 200);
        Vector2d lowerRight = new Vector2d(300, 200);
        Vector2d upperLeft = new Vector2d(200, 100);
        Vector2d upperRight = new Vector2d(300, 100);

        List<LineSegment2d> expectedSegments = New.list(new LineSegment2d(lowerLeft, lowerRight),
                new LineSegment2d(lowerRight, upperRight), new LineSegment2d(upperRight, upperLeft),
                new LineSegment2d(upperLeft, lowerLeft));

        List<LineSegment2d> actualSegments = graphics.getDrawnLines();

        for (LineSegment2d expectedSegment : expectedSegments)
        {
            boolean isActual = false;
            for (LineSegment2d actualSegment : actualSegments)
            {
                if (actualSegment.getVertexA().equals(expectedSegment.getVertexA())
                        && actualSegment.getVertexB().equals(expectedSegment.getVertexB())
                        || actualSegment.getVertexA().equals(expectedSegment.getVertexB())
                                && actualSegment.getVertexB().equals(expectedSegment.getVertexA()))
                {
                    isActual = true;
                    break;
                }
            }

            assertTrue("Did not find segment " + expectedSegment, isActual);
        }
    }

    /**
     * Tests drawing line segments.
     */
    @Test
    public void testClose()
    {
        MapModel mapModel = new MapModel();

        BoundingBoxOverlay overlay = new BoundingBoxOverlay(mapModel);

        assertEquals(1, mapModel.countObservers());

        overlay.close();

        assertEquals(0, mapModel.countObservers());
    }
}
