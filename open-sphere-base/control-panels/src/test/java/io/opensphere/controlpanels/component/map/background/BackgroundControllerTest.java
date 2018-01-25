package io.opensphere.controlpanels.component.map.background;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.opensphere.controlpanels.component.map.model.MapModel;
import io.opensphere.core.geometry.SingletonImageProvider;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.image.Image;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;

/**
 * Tests the background controller class.
 *
 */
public class BackgroundControllerTest
{
    /**
     * Tests the constructor.
     */
    @SuppressWarnings("unused")
    @Test
    public void testBackgroundController()
    {
        MapModel mapModel = new MapModel();
        mapModel.setHeightWidth(150, 300);
        mapModel.setViewport(new ScreenBoundingBox(new ScreenPosition(0d, 0d), new ScreenPosition(300, 150)));

        BackgroundModel model = new BackgroundModel();

        new BackgroundController(mapModel, model);

        assertEquals(1, model.getGeometries().size());

        TileGeometry geometry = model.getGeometries().iterator().next();

        Vector3d upperLeft = ((ScreenBoundingBox)geometry.getBounds()).getUpperLeft().asVector3d();
        Vector3d lowerRight = ((ScreenBoundingBox)geometry.getBounds()).getLowerRight().asVector3d();

        assertEquals(0, (int)upperLeft.getX());
        assertEquals(0, (int)upperLeft.getY());
        assertEquals(150, (int)lowerRight.getY());
        assertEquals(300, (int)lowerRight.getX());

        assertEquals(1d, model.getGeometryScaleFactors().get(0), 0d);

        SingletonImageProvider provider = new SingletonImageProvider("/images/BMNG_world.topo.bathy.200405.3.2048x1024.jpg",
                Image.CompressionType.D3DFMT_DXT1);
        byte[] expected = provider.getImage(null).getByteBuffer().array();

        byte[] actual = geometry.getImageManager().getImageProvider().getImage(null).getByteBuffer().array();

        assertEquals(expected.length, actual.length);

        for (int i = 0; i < expected.length; i++)
        {
            assertEquals(expected[i], actual[i]);
        }
    }

    /**
     * Tests the controller when zoomed in.
     */
    @SuppressWarnings("unused")
    @Test
    public void testBackgroundControllerZoomedIn()
    {
        MapModel mapModel = new MapModel();
        mapModel.setHeightWidth(150, 300);
        mapModel.setViewport(new ScreenBoundingBox(new ScreenPosition(150d, 75d), new ScreenPosition(300, 150)));

        BackgroundModel model = new BackgroundModel();

        new BackgroundController(mapModel, model);

        assertEquals(1, model.getGeometries().size());

        TileGeometry geometry = model.getGeometries().iterator().next();

        Vector3d upperLeft = ((ScreenBoundingBox)geometry.getBounds()).getUpperLeft().asVector3d();
        Vector3d lowerRight = ((ScreenBoundingBox)geometry.getBounds()).getLowerRight().asVector3d();

        assertEquals(-150, (int)upperLeft.getX());
        assertEquals(-75, (int)upperLeft.getY());
        assertEquals(150, (int)lowerRight.getY());
        assertEquals(300, (int)lowerRight.getX());

        assertEquals(2d, model.getGeometryScaleFactors().get(0), 0d);

        SingletonImageProvider provider = new SingletonImageProvider("/images/BMNG_world.topo.bathy.200405.3.2048x1024.jpg",
                Image.CompressionType.D3DFMT_DXT1);
        byte[] expected = provider.getImage(null).getByteBuffer().array();

        byte[] actual = geometry.getImageManager().getImageProvider().getImage(null).getByteBuffer().array();

        assertEquals(expected.length, actual.length);

        for (int i = 0; i < expected.length; i++)
        {
            assertEquals(expected[i], actual[i]);
        }
    }

    /**
     * Tests the close.
     */
    @Test
    public void testClose()
    {
        MapModel mapModel = new MapModel();
        mapModel.setHeightWidth(150, 300);
        mapModel.setViewport(new ScreenBoundingBox(new ScreenPosition(0d, 0d), new ScreenPosition(300, 150)));

        BackgroundModel model = new BackgroundModel();

        BackgroundController controller = new BackgroundController(mapModel, model);

        assertEquals(1, mapModel.countObservers());

        controller.close();

        assertEquals(0, mapModel.countObservers());
    }

    /**
     * Tests the update.
     */
    @SuppressWarnings("unused")
    @Test
    public void testUpdate()
    {
        MapModel mapModel = new MapModel();
        mapModel.setHeightWidth(150, 300);
        mapModel.setViewport(new ScreenBoundingBox(new ScreenPosition(0d, 0d), new ScreenPosition(300, 150)));

        BackgroundModel model = new BackgroundModel();

        new BackgroundController(mapModel, model);

        mapModel.setViewport(new ScreenBoundingBox(new ScreenPosition(0d, 0d), new ScreenPosition(600, 300)));
        mapModel.setHeightWidth(300, 600);

        assertEquals(1, model.getGeometries().size());

        TileGeometry geometry = model.getGeometries().iterator().next();

        Vector3d upperLeft = ((ScreenBoundingBox)geometry.getBounds()).getUpperLeft().asVector3d();
        Vector3d lowerRight = ((ScreenBoundingBox)geometry.getBounds()).getLowerRight().asVector3d();

        assertEquals(0, (int)upperLeft.getX());
        assertEquals(0, (int)upperLeft.getY());
        assertEquals(300, (int)lowerRight.getY());
        assertEquals(600, (int)lowerRight.getX());

        assertEquals(1d, model.getGeometryScaleFactors().get(0), 0d);

        SingletonImageProvider provider = new SingletonImageProvider("/images/BMNG_world.topo.bathy.200405.3.2048x1024.jpg",
                Image.CompressionType.D3DFMT_DXT1);
        byte[] expected = provider.getImage(null).getByteBuffer().array();

        byte[] actual = geometry.getImageManager().getImageProvider().getImage(null).getByteBuffer().array();

        assertEquals(expected.length, actual.length);

        for (int i = 0; i < expected.length; i++)
        {
            assertEquals(expected[i], actual[i]);
        }
    }
}
