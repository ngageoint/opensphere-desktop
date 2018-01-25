package io.opensphere.controlpanels.component.image;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests the {@link ScaledImageController} class.
 */
public class ScaledImageControllerTest
{
    /**
     * Tests when the image height is bigger than its width, and the windows
     * width is bigger than its height and the image is bigger than the window.
     */
    @SuppressWarnings("unused")
    @Test
    public void testBigTallImageWideWindow()
    {
        MockImage image = new MockImage(300, 600);
        ScaledImageModel model = new ScaledImageModel();

        new ScaledImageController(model);
        model.setImage(image);

        model.setWidthAndHeight(300, 150);

        assertEquals(112, model.getX());
        assertEquals(0, model.getY());
        assertEquals(150, model.getImageHeight());
        assertEquals(75, model.getImageWidth());
    }

    /**
     * Tests when the image width is bigger than its height, and the windows
     * height is bigger than its width and the image is bigger than the window.
     */
    @SuppressWarnings("unused")
    @Test
    public void testBigWideImageTallWindow()
    {
        MockImage image = new MockImage(600, 300);
        ScaledImageModel model = new ScaledImageModel();

        new ScaledImageController(model);
        model.setImage(image);

        model.setWidthAndHeight(150, 300);

        assertEquals(0, model.getX());
        assertEquals(112, model.getY());
        assertEquals(75, model.getImageHeight());
        assertEquals(150, model.getImageWidth());
    }

    /**
     * Tests closing the controller.
     */
    @Test
    public void testClose()
    {
        ScaledImageModel model = new ScaledImageModel();

        ScaledImageController controller = new ScaledImageController(model);

        assertEquals(1, model.countObservers());
        controller.close();
        assertEquals(0, model.countObservers());
    }

    /**
     * Tests when there isn't an image set.
     */
    @SuppressWarnings("unused")
    @Test
    public void testNoImage()
    {
        ScaledImageModel model = new ScaledImageModel();

        new ScaledImageController(model);

        model.setWidthAndHeight(300, 150);

        assertEquals(0, model.getX());
        assertEquals(0, model.getY());
        assertEquals(0, model.getImageHeight());
        assertEquals(0, model.getImageWidth());
    }

    /**
     * Tests when the window size hasn't been determined.
     */
    @SuppressWarnings("unused")
    @Test
    public void testNoWindow()
    {
        MockImage image = new MockImage(300, 600);
        ScaledImageModel model = new ScaledImageModel();

        new ScaledImageController(model);
        model.setImage(image);

        assertEquals(0, model.getX());
        assertEquals(0, model.getY());
        assertEquals(600, model.getImageHeight());
        assertEquals(300, model.getImageWidth());
    }

    /**
     * Tests when the image and window have the same aspect but the window is
     * bigger.
     */
    @SuppressWarnings("unused")
    @Test
    public void testSameAspectBiggerWindow()
    {
        MockImage image = new MockImage(300, 150);
        ScaledImageModel model = new ScaledImageModel();

        new ScaledImageController(model);
        model.setImage(image);

        model.setWidthAndHeight(400, 200);

        assertEquals(0, model.getX());
        assertEquals(0, model.getY());
        assertEquals(200, model.getImageHeight());
        assertEquals(400, model.getImageWidth());
    }

    /**
     * Tests when the image and window are the same size.
     */
    @SuppressWarnings("unused")
    @Test
    public void testSameAspectSameSize()
    {
        MockImage image = new MockImage(400, 400);
        ScaledImageModel model = new ScaledImageModel();

        new ScaledImageController(model);
        model.setImage(image);

        model.setWidthAndHeight(400, 400);

        assertEquals(0, model.getX());
        assertEquals(0, model.getY());
        assertEquals(400, model.getImageHeight());
        assertEquals(400, model.getImageWidth());
    }

    /**
     * Tests when the image and window have the same aspect but the window is
     * smaller than the image.
     */
    @SuppressWarnings("unused")
    @Test
    public void testSameAspectSmallerWindow()
    {
        MockImage image = new MockImage(400, 200);
        ScaledImageModel model = new ScaledImageModel();

        new ScaledImageController(model);
        model.setImage(image);

        model.setWidthAndHeight(300, 150);

        assertEquals(0, model.getX());
        assertEquals(0, model.getY());
        assertEquals(150, model.getImageHeight());
        assertEquals(300, model.getImageWidth());
    }

    /**
     * Tests when the image height is bigger than its width, and the windows
     * width is bigger than its height and the image is smaller than the window.
     */
    @SuppressWarnings("unused")
    @Test
    public void testSmallTallImageWideWindow()
    {
        MockImage image = new MockImage(150, 300);
        ScaledImageModel model = new ScaledImageModel();

        new ScaledImageController(model);
        model.setImage(image);

        model.setWidthAndHeight(300, 150);

        assertEquals(112, model.getX());
        assertEquals(0, model.getY());
        assertEquals(150, model.getImageHeight());
        assertEquals(75, model.getImageWidth());
    }

    /**
     * Tests when the image width is bigger than its height, and the windows
     * height is bigger than its width and the image is smaller than the window.
     */
    @SuppressWarnings("unused")
    @Test
    public void testSmallWideImageTallWindow()
    {
        MockImage image = new MockImage(300, 150);
        ScaledImageModel model = new ScaledImageModel();

        new ScaledImageController(model);
        model.setImage(image);

        model.setWidthAndHeight(150, 300);

        assertEquals(0, model.getX());
        assertEquals(112, model.getY());
        assertEquals(150, model.getImageWidth());
        assertEquals(75, model.getImageHeight());
    }
}
