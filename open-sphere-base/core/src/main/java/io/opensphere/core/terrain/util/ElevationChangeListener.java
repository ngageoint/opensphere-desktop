package io.opensphere.core.terrain.util;

import java.util.Collection;

import io.opensphere.core.model.GeographicBoundingBox;

/** Interface for listeners of modified elevation data. */
@FunctionalInterface
public interface ElevationChangeListener
{
    /**
     * Handle changes to elevation within the given regions.
     *
     * @param event Event containing the details of the change.
     * @return The bounds for any affected portions of the listener.
     */
    Collection<GeographicBoundingBox> handleElevationChange(ElevationChangedEvent event);
}
