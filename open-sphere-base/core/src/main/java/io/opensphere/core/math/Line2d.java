package io.opensphere.core.math;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.util.MathUtil;

/**
 * A line in 2 dimensional space.
 */
public class Line2d
{
    /** A vector normal to the line. */
    private final Vector2d myNormal;

    /** A point on the line. */
    private final Vector2d myPoint;

    /**
     * Construct me.
     *
     * @param point A point on the line.
     * @param normal A vector normal to the line.
     */
    public Line2d(Vector2d point, Vector2d normal)
    {
        myPoint = point;
        myNormal = normal.getNormalized();
    }

    /**
     * Give the distance from a point to this line.
     *
     * @param pos a position in 2 dimensional space.
     * @return the distance from the point to the line.
     */
    public double distance(Vector2d pos)
    {
        return pos.subtract(myPoint).dot(myNormal);
    }

    /**
     * Get the distance we must go along the ray to get to the intersection of
     * this line.
     *
     * @param ray ray
     * @return distance along ray to intersection.
     */
    public double getDistance(Ray2d ray)
    {
        // We use the plane normal in both the numerator and denominator, so it
        // is not a problem if it points in the wrong direction.
        double pnDotDir = myNormal.dot(ray.getDirection());
        if (MathUtil.isZero(pnDotDir))
        {
            // this ray is parallel to the line
            return Double.NaN;
        }

        return myNormal.dot(myPoint.subtract(ray.getPosition())) / pnDotDir;
    }

    /**
     * Get the point where the ray intersects this line.
     *
     * @param ray ray.
     * @param allowNegative When true positions which are in the opposite
     *            direction of the ray will be allowed.
     * @return intersection location.
     */
    public Vector2d getIntersection(Ray2d ray, boolean allowNegative)
    {
        double dist = getDistance(ray);
        // If the distance is negative, the intersection with the line occurs in
        // the opposite direction of the ray.
        if (Double.isNaN(dist) || dist < 0. && !allowNegative)
        {
            return null;
        }
        return ray.getPosition().add(ray.getDirection().multiply(dist));
    }

    /**
     * Given two points on a line, determine where the line intersects this
     * line.
     *
     * @param ptA first point
     * @param ptB second point
     * @return intersection location
     */
    public Vector2d getIntersection(Vector2d ptA, Vector2d ptB)
    {
        Vector2d rayDir = ptA.subtract(ptB);
        return getIntersection(new Ray2d(ptA, rayDir), true);
    }

    /**
     * Get the normal.
     *
     * @return the normal.
     */
    public Vector2d getNormal()
    {
        return myNormal;
    }

    /**
     * Yes, I get the point.
     *
     * @return the point.
     */
    public Vector2d getPoint()
    {
        return myPoint;
    }

    /**
     * Given two points which define a line segment, determine where the line
     * segment intersects this line.
     *
     * @param ptA first point
     * @param ptB second point
     * @return intersection of the line segment and this line. If the line
     *         segment is collinear, return both end points.
     */
    public List<? extends Vector2d> getSegmentIntersection(Vector2d ptA, Vector2d ptB)
    {
        List<Vector2d> intersections;

        Vector2d aDir = ptA.subtract(myPoint);
        Vector2d bDir = ptB.subtract(myPoint);

        double aDot = aDir.dot(myNormal);
        double bDot = bDir.dot(myNormal);

        // When the line passes through one or both vertices, add them.
        if (MathUtil.isZero(aDot))
        {
            if (ptA.equals(ptB))
            {
                throw new IllegalArgumentException("Line segment has zero length.");
            }
            else if (MathUtil.isZero(bDot))
            {
                intersections = Arrays.asList(ptA, ptB);
            }
            else
            {
                intersections = Collections.singletonList(ptA);
            }
        }

        else if (MathUtil.isZero(bDot))
        {
            intersections = Collections.singletonList(ptB);
        }

        // When the line does not pass through either vertex, it can intersect
        // the line segment only when the points are on opposite sides of the
        // plane.
        else if (!MathUtil.sameSign(aDot, bDot))
        {
            Vector2d intersection = getIntersection(ptA, ptB);
            intersections = intersection == null ? Collections.<Vector2d>emptyList() : Collections.singletonList(intersection);
        }
        else
        {
            intersections = Collections.emptyList();
        }

        return intersections;
    }

    /**
     * Check to see if the line segment given by the terrain vertices intersects
     * the plane.
     *
     * @param ptA First vertex.
     * @param ptB Second vertex.
     * @return true when the line segment intersects the plane.
     */
    public boolean hasIntersection(Vector2d ptA, Vector2d ptB)
    {
        Vector2d aDir = ptA.subtract(myPoint).getNormalized();
        double aDot = aDir.dot(myNormal);
        if (MathUtil.isZero(aDot))
        {
            return true;
        }

        Vector2d bDir = ptB.subtract(myPoint).getNormalized();
        double bDot = bDir.dot(myNormal);
        return MathUtil.isZero(bDot) || !MathUtil.sameSign(aDot, bDot);
    }

    /**
     * Determine if the position is in front of this line within a given
     * tolerance.
     *
     * @param position position
     * @param distance tolerance
     * @return true if the position is in front.
     */
    public boolean isInFront(Vector2d position, double distance)
    {
        return position.subtract(myPoint).dot(myNormal) > -distance;
    }

    /**
     * Check where both points are on the same side of me.
     *
     * @param ptA first point.
     * @param ptB second point.
     * @return true if both point are on the same side of me.
     */
    public boolean onSameSide(Vector2d ptA, Vector2d ptB)
    {
        boolean aInFront = isInFront(ptA, 0);
        boolean bInFront = isInFront(ptB, 0);
        boolean bothInFront = aInFront && bInFront;
        boolean bothInBack = !aInFront && !bInFront;
        return bothInFront || bothInBack;
    }

    /**
     * Project the given point onto this plane.
     *
     * @param pos A point to be projected onto this plane
     * @return The projection of the given point.
     */
    public Vector2d project(Vector2d pos)
    {
        double dist = distance(pos);
        return pos.subtract(myNormal.multiply(dist));
    }

    /**
     * Comparator that orders coordinates by distance from a line. Negative
     * numbers are preserved for this, so coordinates which are farther in the
     * negative direction will come first.
     */
    @SuppressFBWarnings({ "CO_COMPARETO_INCORRECT_FLOATING", "For efficiency's sake, ignore NaN and -0.0" })
    public static final class DistanceComparator implements Comparator<Vector2d>
    {
        /** The location from which to compare distance. */
        private final Line2d myLine;

        /**
         * Constructor.
         *
         * @param line The location from which to compare distance.
         */
        public DistanceComparator(Line2d line)
        {
            myLine = line;
        }

        @Override
        public int compare(Vector2d o1, Vector2d o2)
        {
            double val1 = myLine.distance(o1);
            double val2 = myLine.distance(o2);
            // Even if the difference is less than MathUtil.DBL_EPSILON, compare
            // for inequality.
            return val1 < val2 ? -1 : val1 > val2 ? 1 : 0;
        }
    }
}
