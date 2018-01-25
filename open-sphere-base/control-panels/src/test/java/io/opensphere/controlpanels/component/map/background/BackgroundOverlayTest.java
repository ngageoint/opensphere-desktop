package io.opensphere.controlpanels.component.map.background;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import io.opensphere.controlpanels.component.map.GraphicsMock;
import io.opensphere.controlpanels.component.map.model.MapModel;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;

/**
 * Tests the BackgroundOverlay class.
 *
 */
public class BackgroundOverlayTest
{
    /**
     * Tests the close method.
     */
    @Test
    public void testClose()
    {
        MapModel mapModel = new MapModel();
        mapModel.setHeightWidth(150, 300);
        mapModel.setViewport(new ScreenBoundingBox(new ScreenPosition(0d, 0d), new ScreenPosition(300, 150)));

        BackgroundOverlay overlay = new BackgroundOverlay(mapModel);

        assertEquals(1, mapModel.countObservers());

        overlay.close();

        assertEquals(0, mapModel.countObservers());
    }

    /**
     * Tests the draw method.
     */
    @Test
    public void testDraw()
    {
        MapModel mapModel = new MapModel();
        mapModel.setHeightWidth(150, 300);
        mapModel.setViewport(new ScreenBoundingBox(new ScreenPosition(0d, 0d), new ScreenPosition(300, 150)));

        BackgroundOverlay overlay = new BackgroundOverlay(mapModel);

        GraphicsMock graphics = new GraphicsMock();
        overlay.draw(graphics);

        List<DrawnImage> drawnImages = graphics.getDrawnImages();

        assertEquals(1, drawnImages.size());

        DrawnImage drawn = drawnImages.get(0);

        assertEquals(0, drawn.getX());
        assertEquals(0, drawn.getY());
        assertNotNull(drawn.getImage());
        assertEquals(150, drawn.getImage().getHeight(null));
        assertEquals(300, drawn.getImage().getWidth(null));
    }

    /**
     * Tests the draw method.
     */
    @Test
    public void testDrawZoomed()
    {
        MapModel mapModel = new MapModel();
        mapModel.setHeightWidth(150, 300);
        mapModel.setViewport(new ScreenBoundingBox(new ScreenPosition(150d, 75d), new ScreenPosition(300, 150)));

        BackgroundOverlay overlay = new BackgroundOverlay(mapModel);

        GraphicsMock graphics = new GraphicsMock();
        overlay.draw(graphics);

        List<DrawnImage> drawnImages = graphics.getDrawnImages();

        assertEquals(1, drawnImages.size());

        DrawnImage drawn = drawnImages.get(0);

        assertEquals(-300, drawn.getX());
        assertEquals(-150, drawn.getY());
        assertNotNull(drawn.getImage());
        assertEquals(300, drawn.getImage().getHeight(null));
        assertEquals(600, drawn.getImage().getWidth(null));
    }
}
