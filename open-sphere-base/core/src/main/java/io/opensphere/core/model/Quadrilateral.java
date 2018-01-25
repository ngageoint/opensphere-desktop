package io.opensphere.core.model;

import java.util.List;

/**
 * Interface for an object that models a quadrilateral.
 *
 * @param <T> position type of the bounding box corners.
 */
public interface Quadrilateral<T extends Position>
{
    /**
     * Determine whether a point is contained by this quadrilateral, within a
     * certain tolerance. If the tolerance is positive, {@code true} will be
     * returned for points slightly outside the quadrilateral. If the tolerance
     * is negative, {@code false} will be returned for points slightly inside
     * the quadrilateral.
     *
     * @param position The position.
     * @param tolerance The tolerance.
     * @return If this quadrilateral contains the point.
     */
    boolean contains(Position position, double tolerance);

    /**
     * Get the center of this quadrilateral, as defined by the average of each
     * of its coordinates.
     *
     * @return The center of the quadrilateral.
     */
    Position getCenter();

    /**
     * Get the type of position.
     *
     * @return The position type.
     */
    Class<T> getPositionType();

    /**
     * Get the vertices.
     *
     * @return The vertices.
     */
    List<? extends T> getVertices();

    /**
     * Determine whether the given quadrilateral overlaps this quadrilateral (or
     * is within the tolerance).
     *
     * @param otherQuad The quadrilateral to check for overlap.
     * @param tolerance A position in the other quadrilateral will be considered
     *            to be overlapping when it is within the tolerance.
     * @return true when the quadrilaterals overlap.
     */
    boolean overlaps(Quadrilateral<? extends T> otherQuad, double tolerance);
}
