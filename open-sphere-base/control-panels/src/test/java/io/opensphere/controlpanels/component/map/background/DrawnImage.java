package io.opensphere.controlpanels.component.map.background;

import java.awt.Image;

/**
 * The image and its x y values that was drawn to the GraphicMock class.
 *
 */
public class DrawnImage
{
    /**
     * The image.
     */
    private final Image myImage;

    /**
     * The x value.
     */
    private final int myX;

    /**
     * The y value.
     */
    private final int myY;

    /**
     * Constructs the drawn image.
     *
     * @param image The image.
     * @param x The x value.
     * @param y the y value.
     */
    public DrawnImage(Image image, int x, int y)
    {
        myImage = image;
        myX = x;
        myY = y;
    }

    /**
     * Gets the image.
     *
     * @return The image.
     */
    public Image getImage()
    {
        return myImage;
    }

    /**
     * Gets the x value.
     *
     * @return The x value.
     */
    public int getX()
    {
        return myX;
    }

    /**
     * Gets the y value.
     *
     * @return The y value.
     */
    public int getY()
    {
        return myY;
    }
}
