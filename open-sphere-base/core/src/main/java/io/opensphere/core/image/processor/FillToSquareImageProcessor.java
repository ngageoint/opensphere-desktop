package io.opensphere.core.image.processor;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

/**
 * Image processor that fills an image with transparent pixels so that it's
 * square.
 */
public class FillToSquareImageProcessor extends AbstractChainedImageProcessor
{
    @Override
    public BufferedImage processInternal(BufferedImage image)
    {
        // Set the visible dimension to the original image dimension
        if (!getProperties().containsKey(VISIBLE_DIMENSION))
        {
            getProperties().put(VISIBLE_DIMENSION, new Dimension(image.getWidth(), image.getHeight()));
        }

        if (image.getWidth() != image.getHeight())
        {
            int max = Math.max(image.getWidth(), image.getHeight());
            int x = (max - image.getWidth()) / 2;
            int y = (max - image.getHeight()) / 2;

            BufferedImage squareImage = new BufferedImage(max, max, BufferedImage.TYPE_4BYTE_ABGR);
            squareImage.getGraphics().drawImage(image, x, y, null);
            return squareImage;
        }
        else
        {
            return image;
        }
    }
}
