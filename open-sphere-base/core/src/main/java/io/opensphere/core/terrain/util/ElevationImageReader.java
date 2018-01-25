package io.opensphere.core.terrain.util;

import io.opensphere.core.image.Image;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;

/** Interface for readers of terrain information out of an image. */
public interface ElevationImageReader extends ElevationMetadataProvider
{
    /**
     * Gets the image format this reader knows how to read.
     *
     * @return The image format.
     */
    String getImageFormat();

    /**
     * Initializes the reader if it was constructed by a ServiceLoader.
     *
     * @param bounds The bounds over which this reader provides data.
     * @param missingDataValue The value given when no data is available.
     * @param crs The CRS which matches the values returned by this reader.
     * @param orderId key used to determine the participant in the order manager
     *            with which this reader is associated.
     */
    void init(GeographicBoundingBox bounds, double missingDataValue, String crs, String orderId);

    /**
     * Provide the elevation for the given position in meters.
     *
     * @param position The position for which to provide the elevation.
     * @param image The image which contains the elevation data.
     * @param bounds the bounding box which the image covers.
     * @param approximate When true, if the value is either out of range or the
     *            sampled value is the "missing data" value, estimate based on
     *            the data available.
     * @return The elevation for the given position in meters.
     * @throws ElevationImageReaderException If there is an error reading from
     *             the image.
     */
    double readElevation(GeographicPosition position, Image image, GeographicBoundingBox bounds, boolean approximate)
        throws ElevationImageReaderException;
}
