package io.opensphere.controlpanels.component.map.controller;

import java.util.List;

import io.opensphere.controlpanels.component.map.model.MapModel;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.Quadrilateral;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.model.GeographicUtilities;

/**
 * This controller controls the zoom variables that are used by the background
 * and bounding box overlay. When a new region is given to the map component
 * this controller will calculate the best screen bounds to use so that the
 * region can be clearly displayed and it is noticeable where it is.
 */
public class ZoomController
{
    /**
     * The maximum zoom level we will allow.
     */
    private static final double ourMaxZoom = 6;

    /**
     * The percentage of the region needs to be within the map display area.
     * Helps determine the zoom level.
     */
    private static final int ourZoomRatio = 10;

    /**
     * The map model.
     */
    private final MapModel myModel;

    /**
     * Constructs a new zoom controller.
     *
     * @param model The map model.
     */
    public ZoomController(MapModel model)
    {
        myModel = model;
        calculateViewPort(myModel.getRegion());
    }

    /**
     * Calculates the viewport based on the region set in the model.
     *
     * @param region The new region to calculate the zoom level for.
     */
    public final void calculateViewPort(Quadrilateral<GeographicPosition> region)
    {
        if (region == null)
        {
            myModel.setViewport(
                    new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(myModel.getWidth(), myModel.getHeight())));
        }
        else
        {
            // Convert the region to screen coordinates.
            List<Vector2d> screenCoords = toScreenCoordinates(region);

            // Find length the longest side
            double length = longestLength(screenCoords);

            // Multiply the longest side by the ratio that will be the new
            // width.
            double newWidth = length * ourZoomRatio;

            // Find the new width to original width ratio
            double newToOriginal = newWidth / myModel.getWidth();

            double newZoomLevel = 1 / newToOriginal;
            if (newZoomLevel > ourMaxZoom)
            {
                newToOriginal = 1d / ourMaxZoom;
                newWidth = myModel.getWidth() * newToOriginal;
            }

            if (newZoomLevel < 1d)
            {
                myModel.setViewport(new ScreenBoundingBox(new ScreenPosition(0, 0),
                        new ScreenPosition(myModel.getWidth(), myModel.getHeight())));
            }
            else
            {
                // Use that new/original ratio to find new height
                double newHeight = myModel.getHeight() * newToOriginal;

                /* now take the first screen vertex and put that in the middle
                 * of the view port. */
                Vector2d firstVertex = screenCoords.get(0);

                double halfNewWidth = newWidth / 2;
                double halfNewHeight = newHeight / 2;

                double upperLeftX = firstVertex.getX() - halfNewWidth;
                double upperLeftY = firstVertex.getY() - halfNewHeight;

                double lowerRightX = firstVertex.getX() + halfNewWidth;
                double lowerRightY = firstVertex.getY() + halfNewHeight;

                myModel.setViewport(new ScreenBoundingBox(new ScreenPosition(upperLeftX, upperLeftY),
                        new ScreenPosition(lowerRightX, lowerRightY)));
            }
        }
    }

    /**
     * Gets the longest line.
     *
     * @param vertices The vertices to get the longest line.
     * @return The longest side of the region.
     */
    private double longestLength(List<Vector2d> vertices)
    {
        double longestLength = 0;

        for (int i = 0; i < vertices.size(); i++)
        {
            int secondIndex = i + 1;
            if (secondIndex >= vertices.size())
            {
                secondIndex = 0;
            }

            Vector2d vertex1 = vertices.get(i);
            Vector2d vertex2 = vertices.get(secondIndex);

            double distance = vertex1.distance(vertex2);
            if (distance > longestLength)
            {
                longestLength = distance;
            }
        }

        return longestLength;
    }

    /**
     * Converts the region to a list of screen coordinates.
     *
     * @param region The region.
     * @return The region in screen coordinates.
     */
    private List<Vector2d> toScreenCoordinates(Quadrilateral<GeographicPosition> region)
    {
        List<ScreenPosition> screenPositions = GeographicUtilities.toScreenPositions(region.getVertices(),
                new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(myModel.getWidth(), myModel.getHeight())));

        List<Vector2d> screenVertices = New.list();

        for (ScreenPosition screenPosition : screenPositions)
        {
            screenVertices.add(screenPosition.asVector2d());
        }

        return screenVertices;
    }
}
