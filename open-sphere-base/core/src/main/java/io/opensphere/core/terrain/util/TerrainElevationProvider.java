package io.opensphere.core.terrain.util;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicPosition;

/**
 * Interface for providers of elevation data on the current terrain. Consumers
 * should expect to receive values that can be used to place geometries on the
 * map such that they are actually on the terrain.
 */
public interface TerrainElevationProvider
{
    /**
     * Get the distance from the model center to the location on the terrain
     * including the altitude in meters.
     *
     * @param position The position for which to provide the distance from
     *            center.
     * @return The distance from the model center to the location on the
     *         terrain.
     */
    double getDistanceFromModelCenterM(GeographicPosition position);

    /**
     * Provide the elevation on the current terrain for the given position in
     * meters.
     *
     * @param position The position for which to provide the elevation.
     * @return The elevation for the given position in meters.
     */
    double getElevationOnTerrainM(GeographicPosition position);

    /**
     * Determine whether the model coordinates are outside of the terrain.
     *
     * @param modelCoordinates The model position to evaluate.
     * @return true when the model coordinates are outside of the terrain.
     */
    boolean isOutsideModel(Vector3d modelCoordinates);
}
