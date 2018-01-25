package io.opensphere.controlpanels.component.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import io.opensphere.controlpanels.component.map.background.DrawnImage;
import io.opensphere.core.math.LineSegment2d;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;

/**
 * Tests the map component.
 */
public class MapComponentTest
{
    /**
     * Tests the map component.
     */
    @Test
    public void test()
    {
        MapComponent map = new MapComponent();

        GeographicBoundingBox region = new GeographicBoundingBox(LatLonAlt.createFromDegrees(45d, -180d),
                LatLonAlt.createFromDegrees(90d, -90d));

        map.setRegion(region);

        GraphicsMock graphics = new GraphicsMock();

        map.paint(graphics);

        Vector2d lowerLeft = new Vector2d(0, 57d);
        Vector2d lowerRight = new Vector2d(112d, 57d);
        Vector2d upperLeft = new Vector2d(0, 0);
        Vector2d upperRight = new Vector2d(112d, 0);

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

        List<DrawnImage> drawnImages = graphics.getDrawnImages();

        assertEquals(1, drawnImages.size());

        DrawnImage drawn = drawnImages.get(0);

        assertEquals(0, drawn.getX());
        assertEquals(0, drawn.getY());
        assertNotNull(drawn.getImage());
        assertEquals(225, drawn.getImage().getHeight(null));
        assertEquals(450, drawn.getImage().getWidth(null));
    }
}
