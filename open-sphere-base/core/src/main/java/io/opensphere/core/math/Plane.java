package io.opensphere.core.math;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.util.MathUtil;

/**
 * A plane in 3 dimensions.
 */
public class Plane
{
    /** A vector normal to the plane. */
    private volatile Vector3d myNormal;

    /** A point on the plane. */
    private volatile Vector3d myPoint;

    /**
     * Give the unitized projection of the vector onto the given plane.
     *
     * @param planeNormal normal
     * @param vect vector
     * @return the vector projection onto the plane.
     */
    public static Vector3d unitProjection(Vector3d planeNormal, Vector3d vect)
    {
        return planeNormal.cross(planeNormal.cross(vect).getNormalized());
    }

    /** Constructor. */
    public Plane()
    {
    }

    /**
     * Constructor.
     *
     * @param point a point on the plane.
     * @param normal a vector normal to the plane.
     */
    public Plane(Vector3d point, Vector3d normal)
    {
        myPoint = point;
        myNormal = normal.getNormalized();
    }

    /**
     * Construct a plane from three points which are on the plane.
     *
     * @param pt1 the first point on the plane
     * @param pt2 the second point on the plane
     * @param pt3 the third point on the plane
     */
    public Plane(Vector3d pt1, Vector3d pt2, Vector3d pt3)
    {
        myPoint = pt1;
        myNormal = pt2.subtract(pt1).cross(pt3.subtract(pt1)).getNormalized();
    }

    /**
     * Get the shortest distance from the position to this plane.
     *
     * @param position Position to find the distance to.
     * @return shortest distance from the position to this plane.
     */
    public double getDistance(Vector3d position)
    {
        Vector3d vec = position.subtract(myPoint);
        return Math.abs(myNormal.dot(vec));
    }

    /**
     * Find the ray which defines the line that lies along the intersection of
     * this plane and the given plane.
     *
     * @param plane plane with which to intersect.
     * @return resulting line or null if the planes do not intersect.
     */
    public Ray3d getIntersection(Plane plane)
    {
        if (MathUtil.isZero(1d - plane.getNormal().dot(myNormal)))
        {
            return null;
        }

        // The cross products of the plane normals gives the direction of the
        // ray.
        Vector3d rayDir = myNormal.cross(plane.getNormal());

        // find a line on this plane
        Vector3d cross = rayDir.cross(myNormal);
        Ray3d crossLine = new Ray3d(myPoint, cross);

        return new Ray3d(plane.getIntersection(crossLine), rayDir);
    }

    /**
     * Get the point where the ray intersects this plane.
     *
     * @param ray ray
     * @return intersection
     */
    public Vector3d getIntersection(Ray3d ray)
    {
        // We use the plane normal in both the numerator and denominator, so it
        // is not a problem if it points in the wrong direction.
        double pnDotDir = myNormal.dot(ray.getDirection());
        if (MathUtil.isZero(pnDotDir))
        {
            // this ray is parallel to the plane
            return null;
        }

        double dist = myNormal.dot(myPoint.subtract(ray.getPosition())) / pnDotDir;
        return ray.getPosition().add(ray.getDirection().multiply(dist));
    }

    /**
     * Given two points on a line, determine where the line intersects this
     * plane.
     *
     * @param ptA first point
     * @param ptB second point
     * @return intersection
     */
    public Vector3d getIntersection(Vector3d ptA, Vector3d ptB)
    {
        Vector3d rayDir = ptA.subtract(ptB);
        return getIntersection(new Ray3d(ptA, rayDir));
    }

    /**
     * Get the normal.
     *
     * @return the normal
     */
    public Vector3d getNormal()
    {
        return myNormal;
    }

    /**
     * Get the point.
     *
     * @return the point
     */
    public Vector3d getPoint()
    {
        return myPoint;
    }

    /**
     * Given two points which define a line segment, determine where the line
     * segment intersects this plane.
     *
     * @param ptA first point
     * @param ptB second point
     * @return intersection of the line and the plane. If the line segment is on
     *         the plane, return both end points.
     */
    public List<Vector3d> getSegmentIntersection(Vector3d ptA, Vector3d ptB)
    {
        List<Vector3d> intersections = new ArrayList<>();

        Vector3d aDir = ptA.subtract(myPoint);
        Vector3d bDir = ptB.subtract(myPoint);

        double aDot = aDir.dot(myNormal);
        double bDot = bDir.dot(myNormal);

        boolean found = false;
        // When the plane passes through one or both vertices, add them.
        if (MathUtil.isZero(aDot, MathUtil.DBL_LARGE_EPSILON))
        {
            intersections.add(ptA);
            found = true;
        }

        if (MathUtil.isZero(bDot, MathUtil.DBL_LARGE_EPSILON))
        {
            intersections.add(ptB);
            found = true;
        }

        // When the plane does not pass through either vertex, it can intersect
        // the line segment only when the points are on opposite sides of the
        // plane.
        if (!found && !MathUtil.sameSign(aDot, bDot))
        {
            intersections.add(getIntersection(ptA, ptB));
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
    public boolean hasIntersection(Vector3d ptA, Vector3d ptB)
    {
        Vector3d aDir = ptA.subtract(myPoint);
        Vector3d bDir = ptB.subtract(myPoint);

        double aDot = aDir.dot(myNormal);
        double bDot = bDir.dot(myNormal);

        return MathUtil.isZero(aDot, MathUtil.DBL_LARGE_EPSILON) || MathUtil.isZero(bDot, MathUtil.DBL_LARGE_EPSILON)
                || !MathUtil.sameSign(aDot, bDot);
    }

    /**
     * Check where the position is in front of me within the specified
     * tolerance.
     *
     * @param position position
     * @param distance tolerance
     * @return true if the position is in front of me within the specified
     *         tolerance.
     */
    public boolean isInFront(Vector3d position, double distance)
    {
        return position.subtract(myPoint).dot(myNormal) > -distance;
    }

    /**
     * Check whether the points are on the same side of this plane.
     *
     * @param ptA first point
     * @param ptB second point
     * @return true if the points are on the same side of this plane.
     */
    public boolean onSameSide(Vector3d ptA, Vector3d ptB)
    {
        boolean aInFront = isInFront(ptA, 0);
        boolean bInFront = isInFront(ptB, 0);
        return aInFront && bInFront || !aInFront && !bInFront;
    }

    /**
     * Set the normal.
     *
     * @param normal the normal to set
     */
    public void setNormal(Vector3d normal)
    {
        myNormal = normal.getNormalized();
    }

    /**
     * Set the point.
     *
     * @param point the point to set
     */
    public void setPoint(Vector3d point)
    {
        myPoint = point;
    }

    @Override
    public String toString()
    {
        return new StringBuilder(160).append(getClass().getSimpleName()).append(" [point").append(getPoint()).append(", normal")
                .append(getNormal()).append(']').toString();
    }

    /**
     * Give the unitized projection of the vector onto this plane.
     *
     * @param vect vector
     * @return projection
     */
    public Vector3d unitProjection(Vector3d vect)
    {
        return unitProjection(myNormal, vect);
    }
}
