package io.opensphere.core.math;

/**
 * Line segment in two dimensions.
 */
public class LineSegment2d
{
    /** First vertex. */
    private final Vector2d myVertexA;

    /** Second vertex. */
    private final Vector2d myVertexB;

    /**
     * Determine whether the line segments [pt1A, pt1B] and [pt2A, pt2B]
     * intersect.
     *
     * @param pt1A First point of the first line.
     * @param pt1B Second point of the first line.
     * @param pt2A First point of the second line.
     * @param pt2B Second point of the second line.
     *
     * @return true when the segments intersect.
     */
    public static boolean segmentsIntersect(Vector2d pt1A, Vector2d pt1B, Vector2d pt2A, Vector2d pt2B)
    {
        Vector2d normalPerp = pt1B.subtract(pt1A).getNormalized();
        Vector2d normal = new Vector2d(normalPerp.getY(), -normalPerp.getX());
        Line2d line1 = new Line2d(pt1A, normal);

        normalPerp = pt2B.subtract(pt2A).getNormalized();
        normal = new Vector2d(normalPerp.getY(), -normalPerp.getX());
        Line2d line2 = new Line2d(pt2A, normal);

        return line1.hasIntersection(pt2A, pt2B) && line2.hasIntersection(pt1A, pt1B);
    }

    /**
     * Construct me.
     *
     * @param pos2 first position
     * @param pos1 second position
     */
    public LineSegment2d(Vector2d pos1, Vector2d pos2)
    {
        myVertexB = pos1;
        myVertexA = pos2;
    }

    /**
     * Get the center of the line segment.
     *
     * @return The center of the line segment.
     */
    public Vector2d getCenter()
    {
        return myVertexA.interpolate(myVertexB, 0.5);
    }

    /**
     * Get the length of this segment.
     *
     * @return The length of this line segment.
     */
    public double getLength()
    {
        return Math.sqrt(getLengthSqared());
    }

    /**
     * Get the square of the length of this segment.
     *
     * @return The square of the length of this line segment.
     */
    public double getLengthSqared()
    {
        return myVertexA.distanceSquared(myVertexB);
    }

    /**
     * Get the unit vector which is normal to the line segment.
     *
     * @return The unit vector which is normal to the line segment.
     */
    public Vector2d getNormal()
    {
        return myVertexA.subtract(myVertexB).getPerpendicular().getNormalized();
    }

    /**
     * Get vertexA.
     *
     * @return vertexA
     */
    public Vector2d getVertexA()
    {
        return myVertexA;
    }

    /**
     * Get vertexB.
     *
     * @return vertexB
     */
    public Vector2d getVertexB()
    {
        return myVertexB;
    }

    /**
     * Determine whether this line segment intersects another line segment.
     *
     * @param other The line segment to test for intersection.
     * @return true when the line segments intersect.
     */
    public boolean intersects(LineSegment2d other)
    {
        return segmentsIntersect(myVertexA, myVertexB, other.getVertexA(), other.getVertexB());
    }

    @Override
    public String toString()
    {
        return myVertexA + " :: " + myVertexB;
    }
}
