package io.opensphere.core;

import java.util.List;

import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.viewbookmark.ViewBookmarkRegistry;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * Interface for accessing the map controls, viewer, and projection.
 */
public interface MapManager extends MapContext<DynamicViewer>
{
    /**
     * Project a geographic position using the current viewer and projection to
     * get the corresponding screen location.
     *
     * @param position The position.
     * @return The screen point based on the origin being in the upper left
     *         corner.
     */
    Vector2i convertToPoint(GeographicPosition position);

    /**
     * Unproject a screen point using the current viewer and projection to get
     * the position being drawn at that screen location.
     *
     * @param point The screen point based on the origin being in the upper left
     *            corner.
     * @param altReference The desired altitude reference for the returned
     *            position.
     * @return The position, or <code>null</code> if there is no intersection.
     */
    GeographicPosition convertToPosition(Vector2i point, ReferenceLevel altReference);

    /**
     * Gets the view book mark registry. Allows a named list of viewer positions
     * to be saved and loaded across sessions.
     *
     * @return the view book mark registry
     */
    ViewBookmarkRegistry getViewBookmarkRegistry();

    /**
     * Calculates the visible geographic boundaries of the map.
     *
     * @return A list of the visible geographic boundary positions
     */
    List<GeographicPosition> getVisibleBoundaries();

    /**
     * Gets the current visible bounding box.
     *
     * @return The visible bounding box.
     */
    GeographicBoundingBox getVisibleBoundingBox();
}
