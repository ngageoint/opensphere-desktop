package io.opensphere.core.image.processor;

import java.util.Map;

/**
 * Chained image processor.
 */
public interface ChainedImageProcessor extends ImageProcessor
{
    /**
     * Adds the next image processor as the next in the chain.
     *
     * @param processor The next image processor in the chain
     */
    void addProcessor(ChainedImageProcessor processor);

    /**
     * Gets the map of processing properties.
     *
     * @return The map of processing properties
     */
    Map<String, Object> getProperties();
}
