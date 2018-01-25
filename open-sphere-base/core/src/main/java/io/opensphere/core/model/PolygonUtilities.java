package io.opensphere.core.model;

import java.util.List;

import io.opensphere.core.math.LineSegment2d;
import io.opensphere.core.math.Ray2d;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.util.collections.New;

/**
 * Helper methods for the polygon utility classes.
 */
//TODO This class should have a unit test.
public final class PolygonUtilities
{
    /**
     * Determine whether the inner polygon is completely contained within the
     * outer polygon.
     *
     * @param outerPolygon The polygon which may contain the inner polygon.
     * @param innerPolygon The polygon which may be contained within the outer
     *            polygon.
     * @return true when the test polygon is completely contained within this
     *         polygon.
     */
    public static boolean contains(List<? extends Position> outerPolygon, List<? extends Position> innerPolygon)
    {
        // If any of the vertices of the test polygon are not contained then the
        // polygon is not contained.
        for (Position vertex : innerPolygon)
        {
            if (!containsRayCast(outerPolygon, vertex))
            {
                return false;
            }
        }

        // If any line segments intersect, then the test polygon is not
        // contained.
        return !PolygonUtilities.segmentsIntersect(getEdges(outerPolygon), getEdges(innerPolygon));
    }

    /**
     * Determine whether the position falls within the polygon. This method is
     * optimized for the case when we know that the polygon is convex.
     *
     * @param polygon The polygon which may contain the position.
     * @param position The position to test for containment.
     * @param tolerance The tolerance for containment.
     * @return true when the position is within the polygon.
     */
    public static boolean containsConvex(List<? extends Position> polygon, Position position, double tolerance)
    {
        Vector3d loc = position.asFlatVector3d();
        Vector3d posA = polygon.get(polygon.size() - 1).asFlatVector3d();
        for (int i = 0; i < polygon.size(); ++i)
        {
            Vector3d posB = polygon.get(i).asFlatVector3d();

            if (!PolygonUtilities.isCounterClockwise(posA, posB, loc, tolerance))
            {
                return false;
            }

            posA = posB;
        }

        return true;
    }

    /**
     * Determine whether the inner polygon is completely contained within the
     * outer polygon.
     *
     * @param outerPolygon The polygon which may contain the inner polygon.
     * @param innerPolygon The polygon which may be contained within the outer
     *            polygon.
     * @param tolerance The testing tolerance.
     * @return true when the test polygon is completely contained within this
     *         polygon.
     */
    public static boolean containsConvex(List<? extends Position> outerPolygon, List<? extends Position> innerPolygon,
            double tolerance)
    {
        // If any of the vertices of the test polygon are not contained then the
        // polygon is not contained.
        for (Position vertex : innerPolygon)
        {
            if (!containsConvex(outerPolygon, vertex, tolerance))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Determine whether the position falls within the polygon using ray
     * casting. Ray-casting tests the number of edges we cross from the point in
     * any one direction. If the number of crossings is even or 0, then we are
     * outside of the polygon.
     *
     * @param polygon The polygon which may contain the position.
     * @param position The position to test for containment.
     * @return true when the position is within the polygon.
     */
    public static boolean containsRayCast(List<? extends Position> polygon, Position position)
    {
        Vector2d loc = position.asVector2d();
        Ray2d ray = new Ray2d(loc, new Vector2d(1., 0.));

        Vector2d previous = polygon.get(polygon.size() - 1).asVector2d();
        int intersections = 0;
        for (int i = 0; i < polygon.size(); ++i)
        {
            Vector2d next = polygon.get(i).asVector2d();
            if (ray.hasIntersection(previous, next))
            {
                ++intersections;
            }
        }

        return intersections % 2 != 0;
    }

    /**
     * Determine whether the first polygon contains a vertex of the second
     * polygon.
     *
     * @param polygon1 The polygon which may contain a vertex from the second
     *            polygon.
     * @param polygon2 The polygon which may have a vertex contained within the
     *            first polygon.
     * @param tolerance The tolerance for proximity.
     * @param convex true when the both polygons are known to be convex. This is
     *            for optimization purposes. Giving false will always return
     *            correct results while giving true is only correct for convex
     *            polygons, but performs faster.
     * @return true when the first polygon contains one or more vertices of the
     *         second polygon.
     */
    public static boolean containsVertex(List<? extends Position> polygon1, List<? extends Position> polygon2, double tolerance,
            boolean convex)
    {
        for (Position vertex : polygon1)
        {
            if (convex && containsConvex(polygon2, vertex, tolerance))
            {
                return true;
            }
            else if (containsRayCast(polygon2, vertex))
            {
                return true;
            }
        }

        return false;
    }

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
        double radius = center.asVector2d().distance(bbox.getUpperRight().getLatLonAlt().asVec2d());
        return new GeographicBoundingCircle(center, radius);
    }

    /**
     * Provide a counter-clockwise listing of the line segments which make up
     * the edges of the polygon.
     *
     * @param polygon The polygon for which the edges are desired.
     * @return A counter-clockwise listing of the line segments which make up
     *         the edges of the polygon.
     */
    public static List<LineSegment2d> getEdges(List<? extends Position> polygon)
    {
        List<LineSegment2d> sides = New.list();
        Vector2d start = polygon.get(polygon.size() - 1).asVector2d();
        for (int i = 0; i < polygon.size(); ++i)
        {
            Vector2d end = polygon.get(i).asVector2d();
            sides.add(new LineSegment2d(start, end));
            start = end;
        }
        return sides;
    }

    /**
     * Determine whether this polygon is convex. This method assumes that the
     * polygon has counter-clockwise winding. When the winding is clockwise,
     * false will be returned even if the polygon is convex. This method does
     * not work for polygons which cross the antimeridian.
     *
     * @param polygon The polygon to test.
     * @return true when the polygon is convex.
     */
    public static boolean isConvex(List<? extends Position> polygon)
    {
        boolean hasClockwiseVertex = false;
        boolean hasCounterClockwiseVertex = false;
        Vector3d posA = polygon.get(polygon.size() - 2).asFlatVector3d();
        Vector3d posB = polygon.get(polygon.size() - 1).asFlatVector3d();
        for (int i = 0; i < polygon.size(); ++i)
        {
            Vector3d posC = polygon.get(i).asFlatVector3d();
            if (PolygonUtilities.isCounterClockwise(posA, posB, posC, 0.))
            {
                hasCounterClockwiseVertex = true;
            }
            else
            {
                hasClockwiseVertex = true;
            }

            // As long as they are all clockwise or all counter-clockwise it is
            // convex.
            if (hasClockwiseVertex && hasCounterClockwiseVertex)
            {
                return false;
            }

            posA = posB;
            posB = posC;
        }

        return true;
    }

    /**
     * Determine whether the geographic polygons overlap each other.
     *
     * @param polygon1 The first polygon to test for overlap.
     * @param polygon2 The second polygon to test for overlap.
     * @param tolerance The tolerance for overlap.
     * @param convex true when the both polygons are known to be convex. This is
     *            for optimization purposes. Giving false will always return
     *            correct results while giving true is only correct for convex
     *            polygons, but performs faster.
     * @return true when the polygons overlap each other.
     */
    public static boolean overlaps(List<? extends Position> polygon1, List<? extends Position> polygon2, double tolerance,
            boolean convex)
    {
        // If any of the vertices from one polygon are contained in the other,
        // then they overlap.
        if (containsVertex(polygon1, polygon2, tolerance, convex) || containsVertex(polygon2, polygon1, tolerance, convex))
        {
            return true;
        }

        // If no points are contained, the polygons may still overlap if the an
        // edge from one crosses an edge from the other.
        return PolygonUtilities.segmentsIntersect(getEdges(polygon1), getEdges(polygon2));
    }

    /**
     * This method assumes that the z coordinate of each of the given vectors is
     * zero and tests to see if the cross product of the vector AB and the
     * vector BC gives a positive z coordinate value.
     *
     * @param posA The first position.
     * @param posB The second position.
     * @param posC The third position.
     * @param tolerance The error tolerance for success.
     * @return true when the positions represent a counter-clockwise corner.
     */
    public static boolean isCounterClockwise(Vector3d posA, Vector3d posB, Vector3d posC, double tolerance)
    {
        Vector3d ab = posB.subtract(posA);
        Vector3d bc = posC.subtract(posB);
        Vector3d cross = ab.cross(bc);
        return cross.getZ() >= -tolerance;
    }

    /**
     * Determine whether any segment from the first collection intersects any
     * segment from the second collection.
     *
     * @param segments1 The first collection of segments.
     * @param segments2 The second collection of segments.
     * @return true when there is one or more intersections.
     */
    public static boolean segmentsIntersect(List<LineSegment2d> segments1, List<LineSegment2d> segments2)
    {
        for (LineSegment2d edge : segments1)
        {
            for (LineSegment2d otherEdge : segments2)
            {
                if (edge.intersects(otherEdge))
                {
                    return false;
                }
            }
        }
        return true;
    }

    /** Disallow instantiation. */
    private PolygonUtilities()
    {
    }
}
