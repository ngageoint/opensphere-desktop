package io.opensphere.core.model;

import java.util.List;

/**
 * Utilities to do basic polygon operations for polygons which use screen
 * positions.
 */
//TODO This class should have a unit test.
public final class ScreenPolygonUtilities
{
    /**
     * Determine whether the screen polygons overlap each other.
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
    public static boolean overlaps(List<? extends ScreenPosition> polygon1, ScreenBoundingBox bounds1,
            List<? extends ScreenPosition> polygon2, ScreenBoundingBox bounds2, double tolerance, boolean convex)
    {
        ScreenBoundingBox testBounds1 = bounds1 == null ? ScreenBoundingBox.getMinimumBoundingBox(polygon1) : bounds1;
        ScreenBoundingBox testBounds2 = bounds2 == null ? ScreenBoundingBox.getMinimumBoundingBox(polygon2) : bounds2;

        if (!testBounds1.overlaps(testBounds2, tolerance))
        {
            return false;
        }

        return PolygonUtilities.overlaps(polygon1, polygon2, tolerance, convex);
    }

    /** Disallow instantiation. */
    private ScreenPolygonUtilities()
    {
    }
}
