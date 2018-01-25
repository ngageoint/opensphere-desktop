package io.opensphere.core.image.processor;

import java.awt.image.BufferedImage;

/**
 * An interface to process an image.
 */
@FunctionalInterface
public interface ImageProcessor
{
    /**
     * Processes an image.
     *
     * @param image The image to be processed
     * @return The processed image
     */
    BufferedImage process(BufferedImage image);
}
