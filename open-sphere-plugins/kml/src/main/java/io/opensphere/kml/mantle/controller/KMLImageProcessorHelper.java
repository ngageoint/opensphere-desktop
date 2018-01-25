package io.opensphere.kml.mantle.controller;

import java.util.ArrayList;
import java.util.Collection;

import de.micromata.opengis.kml.v_2_2_0.IconStyle;
import de.micromata.opengis.kml.v_2_2_0.Vec2;
import io.opensphere.core.image.processor.ChainedImageProcessor;
import io.opensphere.core.image.processor.FillToSquareImageProcessor;
import io.opensphere.core.image.processor.ImageProcessor;
import io.opensphere.core.image.processor.RotateImageProcessor;

/**
 * Helps create image processors.
 */
public final class KMLImageProcessorHelper
{
    /**
     * Creates the chain of icon image processors.
     *
     * @param iconStyle The icon style
     * @param overScale The factor by which to overscale the image
     * @return The image processor chain
     */
    public static ImageProcessor getIconImageProcessor(IconStyle iconStyle, double overScale)
    {
        // Get properties for the icon
        Vec2 hotSpot = null;
        double scale = 1.;
        double rotation = 0.;
        if (iconStyle != null)
        {
            hotSpot = iconStyle.getHotSpot();
            scale = iconStyle.getScale();
            rotation = iconStyle.getHeading();
        }

        if (overScale > 1.)
        {
            scale *= overScale;
        }

        Collection<ChainedImageProcessor> processors = new ArrayList<>(4);

        // Run the hot spot processor if necessary
        if (hotSpot != null)
        {
            processors.add(new HotSpotImageProcessor(hotSpot));
        }

        // Always scale
        processors.add(new KMLScaleImageProcessor(scale));

        // Rotate if necessary
        if (rotation != 0)
        {
            processors.add(new RotateImageProcessor(rotation));
        }

        // Always fill to square because point sprites are squares
        processors.add(new FillToSquareImageProcessor());

        // Create the processor chain
        return createChain(processors);
    }

    /**
     * Creates a chain of ImageProcessors from a Collection of
     * ChainedImageProcessors.
     *
     * @param processors The Collection of ChainedImageProcessors
     * @return The first ImageProcessor of the chain
     */
    private static ImageProcessor createChain(Collection<ChainedImageProcessor> processors)
    {
        ChainedImageProcessor processorChain = null;
        for (ChainedImageProcessor processor : processors)
        {
            if (processorChain == null)
            {
                processorChain = processor;
            }
            else
            {
                processorChain.addProcessor(processor);
            }
        }
        return processorChain;
    }

    /**
     * Private constructor.
     */
    private KMLImageProcessorHelper()
    {
    }
}
