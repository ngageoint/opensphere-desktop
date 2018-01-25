package io.opensphere.controlpanels.component.map;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import io.opensphere.controlpanels.component.map.background.BackgroundOverlay;
import io.opensphere.controlpanels.component.map.boundingbox.BoundingBoxOverlay;
import io.opensphere.controlpanels.component.map.controller.ZoomController;
import io.opensphere.controlpanels.component.map.model.MapModel;
import io.opensphere.controlpanels.component.map.overlay.Overlay;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.Quadrilateral;
import io.opensphere.core.util.Colors;

/**
 * A small 2d map that can be used to show a bounding box and displayed within a
 * swing component.
 */
public class MapComponent extends Component
{
    /**
     * The serial id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * A message describing an error that occurred while getting the region.
     */
    private String myErrorMessage;

    /**
     * The model.
     */
    private final MapModel myModel = new MapModel();

    /**
     * The background overlay.
     */
    private final Overlay[] myOverlays;

    /**
     * The zoom controller.
     */
    private final ZoomController myZoomController;

    /**
     * Constructs a new map component.
     */
    public MapComponent()
    {
        Dimension size = new Dimension(450, 225);
        myModel.setHeightWidth(size.height, size.width);
        myZoomController = new ZoomController(myModel);
        myOverlays = new Overlay[] { new BackgroundOverlay(myModel), new BoundingBoxOverlay(myModel) };
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);

        if (myErrorMessage == null)
        {
            for (Overlay overlay : myOverlays)
            {
                overlay.draw(g);
            }
        }
        else
        {
            Graphics2D g2d = (Graphics2D)g.create();
            g2d.setPaint(Colors.QUERY_REGION);
            Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(myErrorMessage, g2d);
            g2d.drawString(myErrorMessage, (getWidth() - (int)bounds.getWidth()) / 2,
                    (getHeight() - (int)bounds.getHeight()) / 2);
            g2d.dispose();
        }
    }

    /**
     * Set an error message.
     *
     * @param errorMessage The error message.
     */
    public void setErrorMessage(String errorMessage)
    {
        myErrorMessage = errorMessage;
    }

    /**
     * Sets the region to display on the map.
     *
     * @param region The region to display on the map.
     */
    public void setRegion(Quadrilateral<GeographicPosition> region)
    {
        if (getHeight() != 0 && getWidth() != 0)
        {
            myModel.setHeightWidth(getHeight(), getWidth());
        }

        myZoomController.calculateViewPort(region);
        myModel.setRegion(region);
    }

    @Override
    protected void finalize() throws Throwable
    {
        for (Overlay overlay : myOverlays)
        {
            overlay.close();
        }
        super.finalize();
    }
}
