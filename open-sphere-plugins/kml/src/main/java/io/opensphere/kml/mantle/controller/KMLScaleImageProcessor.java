package io.opensphere.kml.mantle.controller;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import io.opensphere.core.image.processor.AbstractChainedImageProcessor;
import io.opensphere.core.image.processor.ScaleImageProcessor;
import io.opensphere.core.util.Utilities;

/**
 * KML scale image processor.
 */
public class KMLScaleImageProcessor extends AbstractChainedImageProcessor
{
    /** The scale. */
    private final double myScale;

    /**
     * Constructor.
     *
     * @param scale The scale
     */
    public KMLScaleImageProcessor(double scale)
    {
        myScale = scale;
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
        KMLScaleImageProcessor other = (KMLScaleImageProcessor)obj;
        return Utilities.equalsOrBothNaN(myScale, other.myScale);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(myScale);
        result = prime * result + (int)(temp ^ temp >>> 32);
        return result;
    }

    @Override
    public BufferedImage processInternal(BufferedImage image)
    {
        // Determine the dimensions of the image
        int width = image.getWidth();
        int height = image.getHeight();
        Object prop = getProperties().get(VISIBLE_DIMENSION);
        if (prop instanceof Dimension)
        {
            width = (int)((Dimension)prop).getWidth();
            height = (int)((Dimension)prop).getHeight();
        }

        // This is the default image size in Google Earth
        int defaultImageSize = 32;

        // Scale relative to the default size
        int max = Math.max(width, height);
        double scale = myScale * defaultImageSize / max;

        ScaleImageProcessor processor = new ScaleImageProcessor(scale, scale);
        BufferedImage processedImage = processor.process(image);
        getProperties().putAll(processor.getProperties());
        return processedImage;
    }
}
