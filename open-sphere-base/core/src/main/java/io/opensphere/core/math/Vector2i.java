package io.opensphere.core.math;

import java.awt.Point;

/**
 * Integer vector in 2 dimensions.
 */
public class Vector2i extends AbstractVector
{
    /** A vector with zero for each component. */
    public static final Vector2i ORIGIN = new Vector2i(0, 0);

    /** The x component of the vector. */
    private final int myX;

    /** The y component of the vector. */
    private final int myY;

    /**
     * Creates a Vector2i with the given initial x and y values.
     *
     * @param x The x value of this Vector2i.
     * @param y The y value of this Vector2i.
     */
    public Vector2i(int x, int y)
    {
        myX = x;
        myY = y;
    }

    /**
     * Construct a copy of the given point.
     *
     * @param point the point to copy.
     */
    public Vector2i(Point point)
    {
        myX = point.x;
        myY = point.y;
    }

    /**
     * Creates a new Vector2i that contains the passed vector's information.
     *
     * @param vec The vector to copy
     */
    public Vector2i(Vector2i vec)
    {
        myX = vec.myX;
        myY = vec.myY;
    }

    /**
     * adds a provided vector to this vector creating a resultant vector which
     * is returned. If the provided vector is null, null is returned.
     *
     * @param vec the vector to add to this.
     * @return the resultant vector.
     */
    public Vector2i add(Vector2i vec)
    {
        return new Vector2i(myX + vec.myX, myY + vec.myY);
    }

    /**
     * returns (in radians) the angle required to rotate a ray represented by
     * this vector to lie colinear to a ray described by the given vector. It is
     * assumed that both this vector and the given vector are unit vectors (iow,
     * normalized).
     *
     * @param otherVector the "destination" unit vector
     * @return the angle in radians.
     */
    public double angleBetween(Vector2i otherVector)
    {
        return Math.atan2(otherVector.myY, otherVector.myX) - Math.atan2(myY, myX);
    }

    /**
     * Get this vector as a {@link Point}.
     *
     * @return The point.
     */
    public Point asPoint()
    {
        return new Point(myX, myY);
    }

    /**
     * calculates the cross product of this vector with a parameter vector v.
     *
     * @param v the vector to take the cross product of with this.
     * @return the cross product vector.
     */
    public Vector3d cross(Vector2i v)
    {
        return new Vector3d(0, 0, determinant(v));
    }

    /**
     * Get the determinant.
     *
     * @param vec other vector
     * @return determinant
     */
    public double determinant(Vector2i vec)
    {
        return myX * vec.myY - myY * vec.myX;
    }

    @Override
    public double distanceSquared(AbstractVector v)
    {
        if (v instanceof Vector2i)
        {
            Vector2i vec2 = (Vector2i)v;
            double dx = myX - vec2.getX();
            double dy = myY - vec2.getY();
            return dx * dx + dy * dy;
        }

        return 0;
    }

    /**
     * divides the values of this vector by a scalar and returns the result. The
     * values of this vector remain untouched.
     *
     * @param scalar the value to divide this vectors attributes by.
     * @return the result <code>Vector</code>.
     */
    public Vector2i divide(double scalar)
    {
        return new Vector2i((int)(myX / scalar), (int)(myY / scalar));
    }

    /**
     * calculates the dot product of this vector with a provided vector. If the
     * provided vector is null, 0 is returned.
     *
     * @param vec the vector to dot with this vector.
     * @return the resultant dot product of this vector and a given vector.
     */
    public double dot(Vector2i vec)
    {
        return myX * vec.myX + myY * vec.myY;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Vector2i))
        {
            return false;
        }

        if (this == o)
        {
            return true;
        }

        Vector2i comp = (Vector2i)o;
        return myX == comp.getX() && myY == comp.getY();
    }

    /**
     * returns (in radians) the angle represented by this Vector2i as expressed
     * by a conversion from rectangular coordinates ( <code>x</code>,&nbsp;
     * <code>y</code>) to polar coordinates (r,&nbsp;<i>theta</i>).
     *
     * @return the angle in radians. [-pi, pi)
     */
    public double getAngle()
    {
        return -Math.atan2(myY, myX);
    }

    @Override
    public double getLengthSquared()
    {
        return myX * myX + myY * myY;
    }

    /**
     * Get x.
     *
     * @return x
     */
    public int getX()
    {
        return myX;
    }

    /**
     * Get y.
     *
     * @return y
     */
    public int getY()
    {
        return myY;
    }

    @Override
    public int hashCode()
    {
        int hash = 37;
        hash += 37 * hash + Float.floatToIntBits(myX);
        hash += 37 * hash + Float.floatToIntBits(myY);
        return hash;
    }

    /**
     * Returns the interpolation by changeAmnt from this to the finalVec
     * this=(1-changeAmnt)*this + changeAmnt * finalVec.
     *
     * @param finalVec The final vector to interpolate towards
     * @param changeAmnt An amount between 0.0 - 1.0 representing a percentage
     *            change from this towards finalVec
     * @return The interpolated vector.
     */
    public Vector2i interpolate(Vector2i finalVec, double changeAmnt)
    {
        return new Vector2i((int)((1 - changeAmnt) * myX + changeAmnt * finalVec.myX),
                (int)((1 - changeAmnt) * myY + changeAmnt * finalVec.myY));
    }

    /**
     * Sets this vector to the interpolation by changeAmnt from beginVec to
     * finalVec this=(1-changeAmnt)*beginVec + changeAmnt * finalVec.
     *
     * @param beginVec The beginning vector (delta=0)
     * @param finalVec The final vector to interpolate towards (delta=1)
     * @param changeAmnt An amount between 0.0 - 1.0 representing a percentage
     *            change from beginVec towards finalVec
     * @return The interpolated vector.
     */
    public Vector2i interpolate(Vector2i beginVec, Vector2i finalVec, double changeAmnt)
    {
        return new Vector2i((int)((1 - changeAmnt) * beginVec.myX + changeAmnt * finalVec.myX),
                (int)((1 - changeAmnt) * beginVec.myY + changeAmnt * finalVec.myY));
    }

    /**
     * calculates the squared value of the magnitude of the vector.
     *
     * @return the magnitude squared of the vector.
     */
    public double lengthSquared()
    {
        return myX * myX + myY * myY;
    }

    /**
     * Multiplies this vector by a scalar. The resultant vector is returned.
     *
     * @param scalar the value to multiply this vector by.
     * @return the new vector.
     */
    public Vector2i multiply(double scalar)
    {
        return new Vector2i((int)(myX * scalar), (int)(myY * scalar));
    }

    /**
     * Rotate this point around the origin.
     *
     * @param angle angle
     * @param clockwise clockwise
     * @return The rotated point.
     */
    public Vector2i rotateAroundOrigin(double angle, boolean clockwise)
    {
        double ang = clockwise ? -angle : angle;
        double newX = Math.cos(ang) * myX - Math.sin(ang) * myY;
        double newY = Math.sin(ang) * myX + Math.cos(ang) * myY;
        return new Vector2i((int)newX, (int)newY);
    }

    /**
     * returns (in radians) the minimum angle between two vectors. It is assumed
     * that both this vector and the given vector are unit vectors (iow,
     * normalized).
     *
     * @param otherVector a unit vector to find the angle against
     * @return the angle in radians.
     */
    public double smallestAngleBetween(Vector2i otherVector)
    {
        double dotProduct = dot(otherVector);
        return Math.acos(dotProduct);
    }

    /**
     * subtracts the given x,y values from those of this vector creating a new
     * vector object.
     *
     * @param valX value to subtract from x
     * @param valY value to subtract from y
     * @return resultant vector
     */
    public Vector2i subtract(int valX, int valY)
    {
        return new Vector2i(myX - valX, myY - valY);
    }

    /**
     * subtracts the values of a given vector from those of this vector storing
     * the result in the given vector object. If the provided vector is null, an
     * exception is thrown.
     *
     * @param vec the vector to subtract from this vector.
     * @return the result vector.
     */
    public Vector2i subtract(Vector2i vec)
    {
        return subtract(vec.getX(), vec.getY());
    }

    /**
     * Saves this Vector2i into the given int[] object.
     *
     * @return The array, with X, Y int values in that order
     */
    public int[] toArray()
    {
        int[] vals = new int[2];
        vals[0] = myX;
        vals[1] = myY;
        return vals;
    }

    @Override
    public String toString()
    {
        return Vector2i.class.getName() + " [X=" + myX + ", Y=" + myY + "]";
    }
}
