package io.opensphere.controlpanels.component.map.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.opensphere.controlpanels.component.map.model.MapModel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.GeographicQuadrilateral;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;

/**
 * Tests the zoom controller.
 */
public class ZoomControllerTest
{
    /**
     * Tests the zoom controller.
     */
    @Test
    public void testCalculateViewPortLowerLeft()
    {
        MapModel model = new MapModel();
        model.setHeightWidth(400, 400);

        ZoomController controller = new ZoomController(model);

        assertEquals(0d, model.getViewport().getUpperLeft().getX(), 0d);
        assertEquals(0d, model.getViewport().getUpperLeft().getY(), 0d);
        assertEquals(400d, model.getViewport().getLowerRight().getX(), 0d);
        assertEquals(400d, model.getViewport().getLowerRight().getY(), 0d);

        GeographicQuadrilateral region = new GeographicQuadrilateral(
                New.list(new GeographicPosition(LatLonAlt.createFromDegrees(-45d, -90d)),
                        new GeographicPosition(LatLonAlt.createFromDegrees(-45d, -80d)),
                        new GeographicPosition(LatLonAlt.createFromDegrees(-40d, -80d)),
                        new GeographicPosition(LatLonAlt.createFromDegrees(-40d, -90d))));

        controller.calculateViewPort(region);

        assertTrue(model.getViewport().getUpperLeft().getX() >= 0d && model.getViewport().getUpperLeft().getX() < 100d);
        assertTrue(model.getViewport().getUpperLeft().getY() >= 200d && model.getViewport().getUpperLeft().getY() < 300d);

        assertTrue(model.getViewport().getLowerRight().getX() <= 200d && model.getViewport().getLowerRight().getX() > 100d);
        assertTrue(model.getViewport().getLowerRight().getY() <= 400d && model.getViewport().getLowerRight().getY() > 300d);
    }

    /**
     * Tests the zoom controller.
     */
    @Test
    public void testCalculateViewPortLowerRight()
    {
        MapModel model = new MapModel();
        model.setHeightWidth(400, 400);

        ZoomController controller = new ZoomController(model);

        assertEquals(0d, model.getViewport().getUpperLeft().getX(), 0d);
        assertEquals(0d, model.getViewport().getUpperLeft().getY(), 0d);
        assertEquals(400d, model.getViewport().getLowerRight().getX(), 0d);
        assertEquals(400d, model.getViewport().getLowerRight().getY(), 0d);

        GeographicQuadrilateral region = new GeographicQuadrilateral(
                New.list(new GeographicPosition(LatLonAlt.createFromDegrees(-45d, 90d)),
                        new GeographicPosition(LatLonAlt.createFromDegrees(-45d, 80d)),
                        new GeographicPosition(LatLonAlt.createFromDegrees(-40d, 80d)),
                        new GeographicPosition(LatLonAlt.createFromDegrees(-40d, 90d))));

        controller.calculateViewPort(region);

        assertTrue(model.getViewport().getUpperLeft().getX() >= 200d && model.getViewport().getUpperLeft().getX() < 300d);
        assertTrue(model.getViewport().getUpperLeft().getY() >= 200d && model.getViewport().getUpperLeft().getY() < 300d);

        assertTrue(model.getViewport().getLowerRight().getX() <= 400d && model.getViewport().getLowerRight().getX() > 300d);
        assertTrue(model.getViewport().getLowerRight().getY() <= 400d && model.getViewport().getLowerRight().getY() > 300d);
    }

    /**
     * Tests the zoom controller.
     */
    @Test
    public void testCalculateViewPortNoZoom()
    {
        MapModel model = new MapModel();
        model.setHeightWidth(400, 400);

        ZoomController controller = new ZoomController(model);

        assertEquals(0d, model.getViewport().getUpperLeft().getX(), 0d);
        assertEquals(0d, model.getViewport().getUpperLeft().getY(), 0d);
        assertEquals(400d, model.getViewport().getLowerRight().getX(), 0d);
        assertEquals(400d, model.getViewport().getLowerRight().getY(), 0d);

        GeographicQuadrilateral region = new GeographicQuadrilateral(
                New.list(new GeographicPosition(LatLonAlt.createFromDegrees(45d, -90d)),
                        new GeographicPosition(LatLonAlt.createFromDegrees(45d, 90d)),
                        new GeographicPosition(LatLonAlt.createFromDegrees(-45d, 90d)),
                        new GeographicPosition(LatLonAlt.createFromDegrees(-45d, -90d))));

        controller.calculateViewPort(region);

        assertEquals(0d, model.getViewport().getUpperLeft().getX(), 0d);
        assertEquals(0d, model.getViewport().getUpperLeft().getY(), 0d);
        assertEquals(400d, model.getViewport().getLowerRight().getX(), 0d);
        assertEquals(400d, model.getViewport().getLowerRight().getY(), 0d);
    }

    /**
     * Tests the zoom controller.
     */
    @Test
    public void testCalculateViewPortUpperLeft()
    {
        MapModel model = new MapModel();
        model.setHeightWidth(400, 400);

        ZoomController controller = new ZoomController(model);

        assertEquals(0d, model.getViewport().getUpperLeft().getX(), 0d);
        assertEquals(0d, model.getViewport().getUpperLeft().getY(), 0d);
        assertEquals(400d, model.getViewport().getLowerRight().getX(), 0d);
        assertEquals(400d, model.getViewport().getLowerRight().getY(), 0d);

        GeographicQuadrilateral region = new GeographicQuadrilateral(
                New.list(new GeographicPosition(LatLonAlt.createFromDegrees(45d, -90d)),
                        new GeographicPosition(LatLonAlt.createFromDegrees(45d, -80d)),
                        new GeographicPosition(LatLonAlt.createFromDegrees(40d, -80d)),
                        new GeographicPosition(LatLonAlt.createFromDegrees(40d, -90d))));

        controller.calculateViewPort(region);

        assertTrue(model.getViewport().getUpperLeft().getX() >= 0d && model.getViewport().getUpperLeft().getX() < 100d);
        assertTrue(model.getViewport().getUpperLeft().getY() >= 0d && model.getViewport().getUpperLeft().getY() < 100d);

        assertTrue(model.getViewport().getLowerRight().getX() <= 200d && model.getViewport().getLowerRight().getX() > 100d);
        assertTrue(model.getViewport().getLowerRight().getY() <= 200d && model.getViewport().getLowerRight().getY() > 100d);
    }

    /**
     * Tests the zoom controller.
     */
    @Test
    public void testCalculateViewPortUpperRight()
    {
        MapModel model = new MapModel();
        model.setHeightWidth(400, 400);

        ZoomController controller = new ZoomController(model);

        assertEquals(0d, model.getViewport().getUpperLeft().getX(), 0d);
        assertEquals(0d, model.getViewport().getUpperLeft().getY(), 0d);
        assertEquals(400d, model.getViewport().getLowerRight().getX(), 0d);
        assertEquals(400d, model.getViewport().getLowerRight().getY(), 0d);

        GeographicQuadrilateral region = new GeographicQuadrilateral(
                New.list(new GeographicPosition(LatLonAlt.createFromDegrees(45d, 90d)),
                        new GeographicPosition(LatLonAlt.createFromDegrees(45d, 80d)),
                        new GeographicPosition(LatLonAlt.createFromDegrees(40d, 80d)),
                        new GeographicPosition(LatLonAlt.createFromDegrees(40d, 90d))));

        controller.calculateViewPort(region);

        assertTrue(model.getViewport().getUpperLeft().getX() >= 200d && model.getViewport().getUpperLeft().getX() < 300d);
        assertTrue(model.getViewport().getUpperLeft().getY() >= 0d && model.getViewport().getUpperLeft().getY() < 100d);

        assertTrue(model.getViewport().getLowerRight().getX() <= 400d && model.getViewport().getLowerRight().getX() > 300d);
        assertTrue(model.getViewport().getLowerRight().getY() <= 200d && model.getViewport().getLowerRight().getY() > 100d);
    }
}
