package io.opensphere.controlpanels.component.map.boundingbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.Test;

import io.opensphere.controlpanels.component.map.model.MapModel;
import io.opensphere.core.math.LineSegment2d;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.collections.New;

/**
 * Tests the BoundingBoxController class.
 *
 */
public class BoundingBoxControllerTest
{
    /**
     * Tests creating segments from a bounding box.
     */
    @SuppressWarnings("unused")
    @Test
    public void test()
    {
        MapModel mapModel = new MapModel();
        mapModel.setHeightWidth(400, 400);
        mapModel.setViewport(new ScreenBoundingBox(new ScreenPosition(0d, 0d), new ScreenPosition(400, 400)));

        BoundingBoxModel model = new BoundingBoxModel();

        new BoundingBoxController(mapModel, model);

        mapModel.setRegion(new GeographicBoundingBox(LatLonAlt.createFromDegrees(-45d, -90d), LatLonAlt.createFromDegrees(0, 0)));

        Vector2d lowerLeft = new Vector2d(100, 300);
        Vector2d lowerRight = new Vector2d(200, 300);
        Vector2d upperLeft = new Vector2d(100, 200);
        Vector2d upperRight = new Vector2d(200, 200);

        List<LineSegment2d> expectedSegments = New.list(new LineSegment2d(lowerLeft, lowerRight),
                new LineSegment2d(lowerRight, upperRight), new LineSegment2d(upperRight, upperLeft),
                new LineSegment2d(upperLeft, lowerLeft));
        Collection<LineSegment2d> actualSegments = model.getBoundingBox();

        assertEquals(expectedSegments.size(), actualSegments.size());

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
     * Tests the close method.
     */
    @Test
    public void testClose()
    {
        MapModel mapModel = new MapModel();
        mapModel.setHeightWidth(400, 400);

        BoundingBoxModel model = new BoundingBoxModel();

        BoundingBoxController controller = new BoundingBoxController(mapModel, model);

        assertEquals(1, mapModel.countObservers());
        assertEquals(1, model.countObservers());

        controller.close();

        assertEquals(0, mapModel.countObservers());
        assertEquals(0, model.countObservers());
    }

    /**
     * Tests creating segments from a bounding box.
     */
    @SuppressWarnings("unused")
    @Test
    public void testZoomed()
    {
        MapModel mapModel = new MapModel();
        mapModel.setHeightWidth(400, 400);
        mapModel.setViewport(new ScreenBoundingBox(new ScreenPosition(200d, 200d), new ScreenPosition(400, 400)));

        BoundingBoxModel model = new BoundingBoxModel();

        new BoundingBoxController(mapModel, model);

        mapModel.setRegion(
                new GeographicBoundingBox(LatLonAlt.createFromDegrees(-45d, 90d), LatLonAlt.createFromDegrees(0, 180)));

        Vector2d lowerLeft = new Vector2d(200, 200);
        Vector2d lowerRight = new Vector2d(400, 200);
        Vector2d upperLeft = new Vector2d(200, 0);
        Vector2d upperRight = new Vector2d(400, 0);

        List<LineSegment2d> expectedSegments = New.list(new LineSegment2d(lowerLeft, lowerRight),
                new LineSegment2d(lowerRight, upperRight), new LineSegment2d(upperRight, upperLeft),
                new LineSegment2d(upperLeft, lowerLeft));
        Collection<LineSegment2d> actualSegments = model.getBoundingBox();

        assertEquals(expectedSegments.size(), actualSegments.size());

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
}
