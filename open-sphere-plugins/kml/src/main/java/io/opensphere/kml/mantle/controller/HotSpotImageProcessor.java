package io.opensphere.kml.mantle.controller;

import java.awt.image.BufferedImage;
import java.util.Objects;

import de.micromata.opengis.kml.v_2_2_0.Vec2;
import io.opensphere.core.image.processor.AbstractChainedImageProcessor;
import io.opensphere.core.image.processor.CenterOnLocationImageProcessor;
import io.opensphere.kml.common.util.KMLSpatialTemporalUtils;

/**
 * Hot spot image processor.
 */
public class HotSpotImageProcessor extends AbstractChainedImageProcessor
{
    /** The hot spot. */
    private final Vec2 myHotSpot;

    /**
     * Constructor.
     *
     * @param hotSpot The hot spot
     */
    public HotSpotImageProcessor(Vec2 hotSpot)
    {
        myHotSpot = hotSpot;
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
        HotSpotImageProcessor other = (HotSpotImageProcessor)obj;
        return Objects.equals(myHotSpot, other.myHotSpot);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myHotSpot == null ? 0 : myHotSpot.hashCode());
        return result;
    }

    @Override
    public BufferedImage processInternal(BufferedImage image)
    {
        double normalizedX = KMLSpatialTemporalUtils.calculateX(myHotSpot, image.getWidth());
        double normalizedY = KMLSpatialTemporalUtils.calculateY(myHotSpot, image.getHeight());
        CenterOnLocationImageProcessor processor = new CenterOnLocationImageProcessor(normalizedX, normalizedY);
        BufferedImage processedImage = processor.process(image);
        getProperties().putAll(processor.getProperties());
        return processedImage;
    }
}
