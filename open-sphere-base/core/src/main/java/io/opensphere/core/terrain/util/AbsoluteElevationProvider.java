package io.opensphere.core.terrain.util;

import java.util.Collection;

import io.opensphere.core.model.GeographicPolygon;
import io.opensphere.core.model.GeographicPosition;

/**
 * Interface for providers of terrain data. Consumers should expected to receive
 * a value which is the most accurate elevation available at the time of the
 * request. This value may not match the rendered terrain at the positions off
 * of the terrain mesh vertices.
 */
public interface AbsoluteElevationProvider extends ElevationMetadataProvider
{
    /** Perform any required cleanup. */
    void close();

    /**
     * Provide the elevation for the given position in meters.
     *
     * @param position The position for which to provide the elevation.
     * @param approximate When true, return the an approximate value when the
     *            actual value is missing.
     * @return The elevation for the given position in meters.
     */
    double getElevationM(GeographicPosition position, boolean approximate);

    /**
     * Get the minimum change in elevation which should be used to generate
     * terrain features.
     *
     * @return the minimum variance.
     */
    double getMinVariance();

    /**
     * Get the regions for which this provider can provided elevations.
     *
     * @return the regions for which this provider can provided elevations.
     */
    Collection<? extends GeographicPolygon> getRegions();

    /**
     * Get the hint for how densely the resolution should be sampled. A value
     * less than zero should be interpreted as providing no hint.
     *
     * @return the hint for how densely the resolution should be sampled.
     */
    double getResolutionHintM();

    /**
     * Tell whether this provider can provided elevation data for any portion of
     * the given polygon.
     *
     * @param polygon The polygon for which to test.
     * @return true when this provider can provided elevation data for any
     *         portion of the given polygon.
     */
    boolean overlaps(GeographicPolygon polygon);

    /**
     * Tell whether the consumer of terrain values should generate terrain only
     * once then lock it. For providers that provide a high density resolution
     * hint, this will provide increased performance.
     *
     * @return When true terrain should be petrified after generation.
     */
    boolean petrifiesTerrain();

    /**
     * Tell whether this provider can provided elevation data for the given
     * position.
     *
     * @param position The position for which to test.
     * @return true when this provider can provided elevation data for the given
     *         position.
     */
    boolean providesForPosition(GeographicPosition position);
}
