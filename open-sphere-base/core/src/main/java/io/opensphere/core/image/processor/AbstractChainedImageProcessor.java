package io.opensphere.core.image.processor;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Chained image processor.
 */
public abstract class AbstractChainedImageProcessor implements ChainedImageProcessor
{
    /**
     * The visible dimension property name. The visible dimension is the
     * original image dimension before adding transparent pixels around it.
     */
    protected static final String VISIBLE_DIMENSION = "visible.dimension";

    /** The next processor. */
    private ChainedImageProcessor myNext;

    /** The property map. */
    private final Map<String, Object> myProperties = new HashMap<>();

    @Override
    public void addProcessor(ChainedImageProcessor processor)
    {
        if (myNext != null)
        {
            myNext.addProcessor(processor);
        }
        else
        {
            myNext = processor;
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        AbstractChainedImageProcessor other = (AbstractChainedImageProcessor)obj;
        return Objects.equals(myNext, other.myNext);
    }

    @Override
    public Map<String, Object> getProperties()
    {
        return myProperties;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myNext == null ? 0 : myNext.hashCode());
        return result;
    }

    @Override
    public BufferedImage process(BufferedImage image)
    {
        BufferedImage processedImage = processInternal(image);
        if (myNext != null)
        {
            myNext.getProperties().putAll(myProperties);
            processedImage = myNext.process(processedImage);
        }
        return processedImage;
    }

    /**
     * Processes an image.
     *
     * @param image The image to be processed
     * @return The processed image
     */
    protected abstract BufferedImage processInternal(BufferedImage image);
}
