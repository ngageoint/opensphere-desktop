package io.opensphere.controlpanels.component.map.boundingbox;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import io.opensphere.controlpanels.component.map.model.MapModel;
import io.opensphere.core.math.LineSegment2d;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.PolygonUtilities;
import io.opensphere.core.model.Quadrilateral;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenConvexQuadrilateral;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.model.GeographicUtilities;

/**
 * Controls the BoundBoxOverlay and supplies the bounding box data to the
 * overlay.
 */
public class BoundingBoxController implements Observer
{
    /**
     * The minimum size of the bounding box in pixels.
     */
    private static final int ourMinimumSize = 5;

    /**
     * The map model that contains the geographic bounding box.
     */
    private final MapModel myMapModel;

    /**
     * The bounding box model that will contain the screen bounding box.
     */
    private final BoundingBoxModel myModel;

    /**
     * Constructs a new bounding box controller.
     *
     * @param mapModel The map model.
     * @param model The model.
     */
    public BoundingBoxController(MapModel mapModel, BoundingBoxModel model)
    {
        myMapModel = mapModel;
        myModel = model;
        myMapModel.addObserver(this);
        myModel.addObserver(this);
        convertGeographicToScreen();
    }

    /**
     * Removes itself as a listener.
     */
    public void close()
    {
        myModel.deleteObserver(this);
        myMapModel.deleteObserver(this);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (o instanceof MapModel && (MapModel.SIZE_PROP.equals(arg) || MapModel.REGION_PROP.equals(arg)))
        {
            convertGeographicToScreen();
        }
    }

    /**
     * Converts the geographic coordinates of the bounding box to screen
     * coordinates and populates the BoundingBoxModel.
     */
    private void convertGeographicToScreen()
    {
        Quadrilateral<GeographicPosition> geoBox = myMapModel.getRegion();

        if (geoBox != null)
        {
            List<? extends GeographicPosition> geoPositions = geoBox.getVertices();

            ScreenBoundingBox viewport = myMapModel.getViewport();
            ScreenPosition portUpperLeft = viewport.getUpperLeft();

            double zoomLevel = myMapModel.getWidth() / viewport.getWidth();
            double realUpperLeftX = 0 - portUpperLeft.getX() * zoomLevel;
            double realUpperLeftY = 0 - portUpperLeft.getY() * zoomLevel;
            double realLowerRightX = realUpperLeftX + myMapModel.getWidth() * zoomLevel;
            double reallLowerRightY = realUpperLeftY + myMapModel.getHeight() * zoomLevel;

            ScreenBoundingBox screenMapBounds = new ScreenBoundingBox(new ScreenPosition(realUpperLeftX, realUpperLeftY),
                    new ScreenPosition(realLowerRightX, reallLowerRightY));
            List<ScreenPosition> positions = GeographicUtilities.toScreenPositions(geoPositions, screenMapBounds);

            Quadrilateral<ScreenPosition> screenBox = new ScreenConvexQuadrilateral(positions.get(0), positions.get(1),
                    positions.get(2), positions.get(3));

            if (screenBox.getVertices().get(0).asVector3d().distance(screenBox.getCenter().asVector3d()) < ourMinimumSize)
            {
                ScreenPosition pos1 = positions.get(0);
                ScreenPosition pos2 = new ScreenPosition(pos1.getX() + ourMinimumSize, pos1.getY());
                ScreenPosition pos3 = new ScreenPosition(pos1.getX() + ourMinimumSize, pos1.getY() + ourMinimumSize);
                ScreenPosition pos4 = new ScreenPosition(pos1.getX(), pos1.getY() + ourMinimumSize);

                screenBox = new ScreenConvexQuadrilateral(pos1, pos2, pos3, pos4);
            }

            List<LineSegment2d> segments = PolygonUtilities.getEdges(screenBox.getVertices());

            myModel.setBoundingBox(segments);
        }
    }
}
