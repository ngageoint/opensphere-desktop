package io.opensphere.mantle.data.geom;

import java.util.function.Function;

import io.opensphere.core.image.processor.ImageProcessor;
import io.opensphere.core.units.length.Kilometers;

/**
 * A Map Point Geometry Support.
 */
public interface MapIconGeometrySupport extends MapLocationGeometrySupport
{
    /** The Constant DEFAULT_ICON_SIZE. */
    float DEFAULT_ICON_SIZE = 10.0f;

    /**
     * Gets the icon size.
     *
     * @return the icon size
     */
    float getIconSize();

    /**
     * Gets the icon highlight size.
     *
     * @return the icon highlight size
     */
    float getIconHighlightSize();

    /**
     * Gets the icon url.
     *
     * @return the icon url
     */
    String getIconURL();

    /**
     * Gets the ImageProcessor.
     *
     * @return the ImageProcessor
     */
    ImageProcessor getImageProcessor();

    /**
     * Gets the optional scale function. The first argument is the viewer
     * altitude, the return value is the scale.
     *
     * @return the scale function, or null
     */
    Function<Kilometers, Float> getScaleFunction();
}
