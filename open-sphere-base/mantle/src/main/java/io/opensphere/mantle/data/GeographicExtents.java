package io.opensphere.mantle.data;

import java.io.Serializable;
import java.util.List;

import io.opensphere.core.model.GeographicBoundingBox;

/**
 * Stores one or more {@link GeographicBoundingBox}'s.
 */
@FunctionalInterface
public interface GeographicExtents extends Serializable
{
    /**
     * Gets the bounding boxes.
     *
     * @return the bounding box list
     */
    List<GeographicBoundingBox> getBoundingBoxes();
}
