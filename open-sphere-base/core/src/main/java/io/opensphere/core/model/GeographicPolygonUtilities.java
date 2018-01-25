package io.opensphere.core.model;

import java.util.List;

/**
 * Utilities to do basic polygon operations for polygons which use geographic
 * positions.
 */
//TODO This class should have a unit test.
public final class GeographicPolygonUtilities
{
    /**
     * Get the circle which minimally bounds the bounding box.
     *
     * @param polygon The polygon for which a bounding circle is desired.
     * @return the circle which minimally bounds the bounding box.
     */
    public static GeographicBoundingCircle getBoundingCircle(List<? extends GeographicPosition> polygon)
    {
        GeographicBoundingBox bbox = GeographicBoundingBox.getMinimumBoundingBox(polygon);
        GeographicPosition center = bbox.getCenter();
        double radius = center.getLatLonAlt().asVec2d().distance(bbox.getUpperRight().getLatLonAlt().asVec2d());
        return new GeographicBoundingCircle(center, radius);
    }

    /**
     * Determine whether the geographic polygons overlap each other.
     *
     * @param polygon1 The first polygon to test for overlap.
     * @param bounds1 the bounding box for the first polygon.
     * @param polygon2 The second polygon to test for overlap.
     * @param bounds2 the bounding box for the second polygon.
     * @param tolerance The tolerance for overlap.
     * @param convex true when the both polygons are known to be convex. This is
     *            for optimization purposes. Giving false will always return
     *            correct results while giving true is only correct for convex
     *            polygons, but performs faster.
     * @return true when the polygons overlap each other.
     */
    public static boolean overlaps(List<? extends GeographicPosition> polygon1, GeographicBoundingCircle bounds1,
            List<? extends GeographicPosition> polygon2, GeographicBoundingCircle bounds2, double tolerance, boolean convex)
    {
        GeographicBoundingCircle testBounds1 = bounds1 == null ? getBoundingCircle(polygon1) : bounds1;
        GeographicBoundingCircle testBounds2 = bounds2 == null ? getBoundingCircle(polygon2) : bounds2;

        if (!testBounds1.overlaps(testBounds2))
        {
            return false;
        }

        return PolygonUtilities.overlaps(polygon1, polygon2, tolerance, convex);
    }

    /** Disallow instantiation. */
    private GeographicPolygonUtilities()
    {
    }
}
