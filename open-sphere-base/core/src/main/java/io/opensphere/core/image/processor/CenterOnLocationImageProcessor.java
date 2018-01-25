package io.opensphere.core.image.processor;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import io.opensphere.core.util.Utilities;

/**
 * Image processor that fills an image with transparent pixels so that it is
 * centered on a particular location within the image.
 */
public class CenterOnLocationImageProcessor extends AbstractChainedImageProcessor
{
    /** The x pixel location in the image from the left. */
    private final double myX;

    /** The y pixel location in the image from the bottom. */
    private final double myY;

    /**
     * Constructor.
     *
     * @param x The x pixel location in the image from the left
     * @param y The y pixel location in the image from the bottom
     */
    public CenterOnLocationImageProcessor(double x, double y)
    {
        myX = x;
        myY = y;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass())
        {
            return false;
        }
        CenterOnLocationImageProcessor other = (CenterOnLocationImageProcessor)obj;
        return Utilities.equalsOrBothNaN(myX, other.myX) && Utilities.equalsOrBothNaN(myY, other.myY);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(myX);
        result = prime * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(myY);
        result = prime * result + (int)(temp ^ temp >>> 32);
        return result;
    }

    @Override
    public BufferedImage processInternal(BufferedImage image)
    {
        // Set the visible dimension to the original image dimension
        if (!getProperties().containsKey(VISIBLE_DIMENSION))
        {
            getProperties().put(VISIBLE_DIMENSION, new Dimension(image.getWidth(), image.getHeight()));
        }

        float newWidth;
        float newHeight;
        float xPos;
        float yPos;

        float x2 = 2 * (float)myX;
        float y2 = 2 * (float)myY;
        if (x2 < image.getWidth())
        {
            newWidth = 2 * image.getWidth() - x2;
            xPos = newWidth - image.getWidth();
        }
        else
        {
            newWidth = x2;
            xPos = 0;
        }
        if (y2 < image.getHeight())
        {
            newHeight = 2 * image.getHeight() - y2;
            yPos = 0;
        }
        else
        {
            newHeight = y2;
            yPos = newHeight - image.getHeight();
        }

        BufferedImage squareImage = new BufferedImage(Math.round(newWidth), Math.round(newHeight), BufferedImage.TYPE_4BYTE_ABGR);
        squareImage.getGraphics().drawImage(image, Math.round(xPos), Math.round(yPos), null);
        return squareImage;
    }
}
