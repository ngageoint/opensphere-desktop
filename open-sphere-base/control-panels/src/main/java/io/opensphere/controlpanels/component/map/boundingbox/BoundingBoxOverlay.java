package io.opensphere.controlpanels.component.map.boundingbox;

import java.awt.Graphics;

import io.opensphere.controlpanels.component.map.model.MapModel;
import io.opensphere.controlpanels.component.map.overlay.Overlay;
import io.opensphere.core.math.LineSegment2d;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.util.Colors;

/**
 * The overlay that shows a bounding box on the map.
 */
public class BoundingBoxOverlay implements Overlay
{
    /**
     * The controller that converts the geographic box to line segments to draw.
     */
    private final BoundingBoxController myController;

    /**
     * Contains the bound box line segments to draw.
     */
    private final BoundingBoxModel myModel = new BoundingBoxModel();

    /**
     * Constructs a new BoundingboxOverlay.
     *
     * @param mapModel The map model containing the geographic bounding box to
     *            draw.
     */
    public BoundingBoxOverlay(MapModel mapModel)
    {
        myController = new BoundingBoxController(mapModel, myModel);
    }

    @Override
    public void close()
    {
        myController.close();
    }

    @Override
    public void draw(Graphics graphics)
    {
        graphics.setColor(Colors.QUERY_REGION);

        for (LineSegment2d segment : myModel.getBoundingBox())
        {
            Vector2d vertexA = segment.getVertexA();
            Vector2d vertexB = segment.getVertexB();

            graphics.drawLine((int)vertexA.getX(), (int)vertexA.getY(), (int)vertexB.getX(), (int)vertexB.getY());
        }
    }
}
