package io.opensphere.core.terrain.util;

import io.opensphere.core.model.GeographicBoundingBox;

/**
 * Interface for providers of terrain data. Consumers should expected to receive
 * a value which is the most accurate elevation available at the time of the
 * request. Some elevation providers may provide more or less accurate values at
 * different times (for example, less accurate values may be provided based on
 * the distance of the viewer to the relevant region).
 */
public interface ElevationMetadataProvider
{
    /**
     * Get the bounds over which this provider provides terrain.
     *
     * @return The terrain bounding box.
     */
    GeographicBoundingBox getBoundingBox();

    /**
     * Give the OGC Coordinate Reference System or Spatial Reference System on
     * which the altitude values are based.
     *
     * @return the SRS or CRS.
     */
    String getCRS();

    /**
     * Get the key used to determine the participant in the order manager with
     * which this provider is associated.
     *
     * @return the elevationOrderKey
     */
    String getElevationOrderId();

    /**
     * The value which denotes missing data.
     *
     * @return The value which denotes missing data.
     */
    double getMissingDataValue();
}
