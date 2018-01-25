package io.opensphere.core.model;

import io.opensphere.core.math.Vector3d;

/**
 * Interface for an object that models a rectangle in 2D or a box in 3D.
 *
 * @param <T> position type of the bounding box corners.
 */
public interface BoundingBox<T extends Position> extends Quadrilateral<T>
{
    /**
     * Returns true if this bounding box contains another bounding box.
     *
     * @param otherBox the box to check for containment in this bounding box
     * @return true if contains, false if not.
     */
    boolean contains(BoundingBox<T> otherBox);

    /**
     * Get the depth of this bounding box. Units are implementation-dependent.
     *
     * @return The height.
     */
    double getDepth();

    /**
     * Get the height of this bounding box. Units are implementation-dependent.
     *
     * @return The height.
     */
    double getHeight();

    /**
     * Get the lower-left corner of the box.
     *
     * @return The lower-left corner.
     */
    T getLowerLeft();

    /**
     * Get the lower-right corner of the box.
     *
     * @return The lower-right corner.
     */
    T getLowerRight();

    /**
     * Determine the offset of this box inside another box. (0,0,0) is the
     * lower-left-back corner of the outer box and (1,1,1) is the
     * upper-right-front corner.
     *
     * @param outerBox The outer box.
     * @return The coordinates relative to the outer box.
     */
    Vector3d getOffset(BoundingBox<T> outerBox);

    /**
     * Get the percentage this is from the lowest corner to the highest corner.
     *
     * @param position Position within the box.
     * @return a vector containing the x and y percentage offset into the box.
     */
    Vector3d getOffsetPercent(Position position);

    /**
     * Get the upper-left corner of the box.
     *
     * @return The upper-left corner.
     */
    T getUpperLeft();

    /**
     * Get the upper-right corner of the box.
     *
     * @return The upper-right corner.
     */
    T getUpperRight();

    /**
     * Get the width of this bounding box. Units are implementation-dependent.
     *
     * @return The width.
     */
    double getWidth();

    /**
     * Returns a {@link BoundingBox} that represents the envelope (bounding
     * rectangle) of the intersection of two bounding boxes. If there is no
     * intersection it will return null.
     *
     * If one of the two bounding boxes is null it will return the envelope of
     * the non-null box. If both boxes are null it will return null.
     *
     * @param otherBox the {@link BoundingBox} to intersect with with
     * @return the envelope (rectangle) of the intersection of the two bounding
     *         boxes, or null if no intersection.
     * @throws UnsupportedOperationException if not valid for this type.
     */
    BoundingBox<T> intersection(BoundingBox<T> otherBox);

    /**
     * Returns true if this bounding box intersects another bounding box.
     * <p>
     * Intersection is defined like
     * {@link com.vividsolutions.jts.geom.Geometry#intersects(com.vividsolutions.jts.geom.Geometry)}
     * , and as such, includes the case where two geometries touch but do not
     * overlap.
     *
     * @param otherBox to check for intersection.
     * @return true if it intersects, false if not.
     * @throws UnsupportedOperationException if not valid for this type.
     */
    boolean intersects(BoundingBox<T> otherBox);

    /**
     * Provide a simple string with minimal data.
     *
     * @return Simple string.
     */
    String toSimpleString();

    /**
     * Returns the envelope (bounding rectangle) for the union of two bounding
     * boxes. If both boxes are null, returns null. If one of the bounding boxes
     * are null returns the envelope of the non-null box.
     *
     * @param otherBox the {@link BoundingBox} to union with with
     * @return the {@link BoundingBox} the resultant envelope (bounding
     *         rectangle) of the union or null.
     * @throws UnsupportedOperationException if not valid for this type.
     */
    BoundingBox<T> union(BoundingBox<T> otherBox);
}
