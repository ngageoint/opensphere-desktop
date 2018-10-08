package io.opensphere.core.math;

import java.io.Serializable;
import java.util.Comparator;

import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;

/**
 * Vector in 2 dimensions.
 */
public class Vector2d extends AbstractVector implements Serializable
{
    /** Comparator that orders coordinates such that smaller ones come first. */
    public static final Comparator<Vector2d> LENGTH_COMPARATOR = new Comparator<>()
    {
        @Override
        public int compare(Vector2d o1, Vector2d o2)
        {
            // Since we are only interested in the magnitude, the square of the
            // distance may be used for efficiency.
            double val1 = o1.getLengthSquared();
            double val2 = o2.getLengthSquared();
            double delta = Math.abs(val1 - val2);
            return delta < MathUtil.DBL_EPSILON ? 0 : val1 < val2 ? 1 : -1;
        }
    };

    /** A vector with zero for each component. */
    public static final Vector2d ORIGIN = new Vector2d(0., 0.);

    /** How many bytes used internally by one of these objects. */
    public static final int SIZE_BYTES = MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.DOUBLE_SIZE_BYTES * 2,
            Constants.MEMORY_BLOCK_SIZE_BYTES);

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** the x value of the vector. */
    private final double myX;

    /** the y value of the vector. */
    private final double myY;

    /**
     * Given 3 points in a 2d plane, this function computes if the points going
     * from A-B-C are moving counter clock wise.
     *
     * @param p0 Point 0.
     * @param p1 Point 1.
     * @param p2 Point 2.
     * @return 1 If they are CCW, -1 if they are not CCW, 0 if p2 is between p0
     *         and p1.
     */
    public static int counterClockwise(Vector2d p0, Vector2d p1, Vector2d p2)
    {
        double dx1 = p1.getX() - p0.getX();
        double dy1 = p1.getY() - p0.getY();
        double dx2 = p2.getX() - p0.getX();
        double dy2 = p2.getY() - p0.getY();
        if (dx1 * dy2 > dy1 * dx2)
        {
            return 1;
        }
        if (dx1 * dy2 < dy1 * dx2)
        {
            return -1;
        }
        if (dx1 * dx2 < 0 || dy1 * dy2 < 0)
        {
            return -1;
        }
        if (dx1 * dx1 + dy1 * dy1 < dx2 * dx2 + dy2 * dy2)
        {
            return 1;
        }
        return 0;
    }

    /**
     * Creates a Vector2d with the given initial x and y values.
     *
     * @param x The x value of this Vector2d.
     * @param y The y value of this Vector2d.
     */
    public Vector2d(double x, double y)
    {
        myX = x;
        myY = y;
    }

    /**
     * Creates a new Vector2d that contains the passed vector's information.
     *
     * @param vec The vector to copy.
     */
    public Vector2d(Vector2d vec)
    {
        myX = vec.myX;
        myY = vec.myY;
    }

    /**
     * Creates a new Vector2d that contains the passed vector's information.
     *
     * @param vec The vector to copy.
     */
    public Vector2d(Vector2i vec)
    {
        myX = vec.getX();
        myY = vec.getY();
    }

    /**
     * Adds a provided vector to this vector creating a resultant vector which
     * is returned. If the provided vector is null, null is returned.
     *
     * @param vec the vector to add to this.
     * @return the resultant vector.
     */
    public Vector2d add(Vector2d vec)
    {
        return new Vector2d(myX + vec.myX, myY + vec.myY);
    }

    /**
     * Adds the provided components to this vector creating a resultant vector
     * which is returned. If the provided vector is null, null is returned.
     *
     * @param x The x component.
     * @param y The y component.
     * @return the resultant vector.
     */
    public Vector2d add(double x, double y)
    {
        return new Vector2d(myX + x, myY + y);
    }

    /**
     * Get the angle, in radians, required to rotate a ray represented by this
     * vector to lie colinear to a ray described by the given vector. It is
     * assumed that both this vector and the given vector are unit vectors (iow,
     * normalized).
     *
     * @param otherVector the "destination" unit vector
     * @return the angle in radians.
     */
    public double angleBetween(Vector2d otherVector)
    {
        // This is faster than Math.acos(dot(otherVector)) and can also produce
        // negative angles.
        return Math.atan2(otherVector.myY, otherVector.myX) - Math.atan2(myY, myX);
    }

    /**
     * Get the determinant.
     *
     * @param vec other vector
     * @return the determinant
     */
    public double determinant(Vector2d vec)
    {
        return myX * vec.myY - myY * vec.myX;
    }

    @Override
    public double distanceSquared(AbstractVector vec)
    {
        if (vec instanceof Vector2d)
        {
            Vector2d vec2 = (Vector2d)vec;
            double dx = myX - vec2.myX;
            double dy = myY - vec2.myY;
            return dx * dx + dy * dy;
        }
        return 0;
    }

    /**
     * Divides the values of this vector by a scalar and returns the result.
     *
     * @param scalar the value to divide this vectors attributes by.
     * @return the resultant vector.
     */
    public Vector2d divide(double scalar)
    {
        return new Vector2d(myX / scalar, myY / scalar);
    }

    /**
     * Calculate the dot product of this vector with a provided vector. If the
     * provided vector is null, 0 is returned.
     *
     * @param vec the vector to dot with this vector.
     * @return the resultant dot product of this vector and a given vector.
     */
    public double dot(Vector2d vec)
    {
        return myX * vec.myX + myY * vec.myY;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Vector2d))
        {
            return false;
        }

        if (this == o)
        {
            return true;
        }

        Vector2d comp = (Vector2d)o;
        return MathUtil.isZero(myX - comp.myX) && MathUtil.isZero(myY - comp.myY);
    }

    /**
     * Get the angle (in radians) represented by this Vector2d as expressed by a
     * conversion from rectangular coordinates ( <code>x</code>,&nbsp;
     * <code>y</code>) to polar coordinates (r,&nbsp;<i>theta</i>).
     *
     * @return the angle in radians. [-pi, pi)
     */
    public double getAngle()
    {
        return Math.atan2(myY, myX);
    }

    @Override
    public double getLengthSquared()
    {
        return myX * myX + myY * myY;
    }

    /**
     * Get a normalized version of this vector.
     *
     * @return normalized vector
     */
    public Vector2d getNormalized()
    {
        double length = getLength();
        return length == 1. ? this : new Vector2d(myX / length, myY / length);
    }

    /**
     * Get a vector which is perpendicular to this one.
     *
     * @return A vector which is perpendicular to this one.
     */
    public Vector2d getPerpendicular()
    {
        return new Vector2d(myY, -myX);
    }

    /**
     * Get x.
     *
     * @return x
     */
    public double getX()
    {
        return myX;
    }

    /**
     * Get y.
     *
     * @return y
     */
    public double getY()
    {
        return myY;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(myX);
        result = prime * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(myY);
        result = prime * result + (int)(temp ^ temp >>> 32);
        return result;
    }

    /**
     * Interpolate betwixt me and the given vector.
     *
     * @param vec other vector
     * @param fraction percentage along to other vector
     * @return interpolated point
     */
    public Vector2d interpolate(Vector2d vec, double fraction)
    {
        return multiply(1. - fraction).add(vec.multiply(fraction));
    }

    /**
     * Returns whether or not both components of the vector are finite.
     *
     * @return whether or not both components of the vector are finite
     */
    public boolean isFinite()
    {
        return Double.isFinite(myX) && Double.isFinite(myY);
    }

    /**
     * multiplies this vector by a scalar. The resultant vector is returned.
     *
     * @param scalar the value to multiply this vector by.
     * @return the new vector.
     */
    public Vector2d multiply(double scalar)
    {
        return new Vector2d(myX * scalar, myY * scalar);
    }

    /**
     * Rotate this point around the origin.
     *
     * @param angle The angle in radians (CCW).
     * @return The rotated point.
     */
    public Vector2d rotateAroundOrigin(double angle)
    {
        double newX = Math.cos(angle) * myX - Math.sin(angle) * myY;
        double newY = Math.sin(angle) * myX + Math.cos(angle) * myY;
        return new Vector2d(newX, newY);
    }

    /**
     * returns (in radians) the minimum angle between two vectors. It is assumed
     * that both this vector and the given vector are unit vectors (iow,
     * normalized).
     *
     * @param otherVector a unit vector to find the angle against
     * @return the angle in radians.
     */
    public double smallestAngleBetween(Vector2d otherVector)
    {
        double dotProduct = dot(otherVector);
        return Math.acos(dotProduct);
    }

    /**
     * subtracts the values of a given vector from those of this vector storing
     * the result in the given vector object. If the provided vector is null, an
     * exception is thrown.
     *
     * @param vec the vector to subtract from this vector.
     * @return the result vector.
     */
    public Vector2d subtract(Vector2d vec)
    {
        return new Vector2d(myX - vec.myX, myY - vec.myY);
    }

    /**
     * Saves this Vector2d into the given double[] object.
     *
     * @param doubles The double[] to take this Vector2d. If null, a new
     *            double[2] is created.
     * @return The array, with X, Y double values in that order
     */
    public double[] toArray(double[] doubles)
    {
        double[] result = doubles == null ? new double[2] : doubles;
        result[0] = myX;
        result[1] = myY;
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(32);
        sb.append('[');
        sb.append(myX).append(' ');
        sb.append(myY);
        sb.append(']');
        return sb.toString();
    }
}
