package io.opensphere.controlpanels.component.image;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

/**
 * Mocks up an Image object.
 */
public class MockImage extends Image
{
    /**
     * The image height.
     */
    private final int myHeight;

    /**
     * The image width.
     */
    private final int myWidth;

    /**
     * Constructs a new mock image.
     *
     * @param width The width.
     * @param height The height.
     */
    public MockImage(int width, int height)
    {
        myWidth = width;
        myHeight = height;
    }

    @Override
    public Graphics getGraphics()
    {
        return null;
    }

    @Override
    public int getHeight(ImageObserver observer)
    {
        return myHeight;
    }

    @Override
    public Object getProperty(String name, ImageObserver observer)
    {
        return null;
    }

    @Override
    public ImageProducer getSource()
    {
        return null;
    }

    @Override
    public int getWidth(ImageObserver observer)
    {
        return myWidth;
    }
}
