package io.opensphere.core.math;

import java.nio.DoubleBuffer;

import io.opensphere.core.util.Constants;
import io.opensphere.core.util.JAXBable;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.lang.ExpectedCloneableException;

/**
 * A vector in 3 dimensions.
 */
public class Vector3d extends AbstractVector implements Cloneable, JAXBable<MutableVector3d>, Comparable<Vector3d>
{
    /** A vector with zero for each component. */
    public static final Vector3d ORIGIN = new Vector3d(0., 0., 0.);

    /** How many bytes used internally by one of these objects. */
    public static final int SIZE_BYTES = MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.DOUBLE_SIZE_BYTES * 3,
            Constants.MEMORY_BLOCK_SIZE_BYTES);

    /** A unit vector in the X direction. */
    public static final Vector3d UNIT_X = new Vector3d(1., 0., 0.);

    /** A unit vector in the Y direction. */
    public static final Vector3d UNIT_Y = new Vector3d(0., 1., 0.);

    /** A unit vector in the Z direction. */
    public static final Vector3d UNIT_Z = new Vector3d(0., 0., 1.);

    /** X vector component. */
    private final double myX;

    /** Y vector component. */
    private final double myY;

    /** Z vector component. */
    private final double myZ;

    /**
     * Create a vector that is the input vector clamped within the given bounds.
     *
     * @param vec The input vector.
     * @param minX The minimum X bound.
     * @param maxX The maximum X bound.
     * @param minY The minimum Y bound.
     * @param maxY The maximum Y bound.
     * @param minZ The minimum Z bound.
     * @param maxZ The maximum Z bound.
     *
     * @return The clamped vector.
     */
    public static Vector3d clamp(Vector3d vec, double minX, double maxX, double minY, double maxY, double minZ, double maxZ)
    {
        double x = Math.min(Math.max(minX, vec.getX()), maxX);
        double y = Math.min(Math.max(minY, vec.getY()), maxY);
        double z = Math.min(Math.max(minZ, vec.getZ()), maxZ);
        return new Vector3d(x, y, z);
    }

    /**
     * Construct a normalized Vector3d.
     *
     * @param x The x component.
     * @param y The y component.
     * @param z The z component.
     * @return The new vector.
     */
    public static Vector3d normalize(double x, double y, double z)
    {
        double length = Math.sqrt(x * x + y * y + z * z);
        if (MathUtil.isZero(length))
        {
            return new Vector3d(x, y, z);
        }
        return new Vector3d(x / length, y / length, z / length);
    }

    /**
     * Construct me.
     *
     * @param x x coordinate.
     * @param y y coordinate.
     * @param z z coordinate.
     */
    public Vector3d(double x, double y, double z)
    {
        myX = x;
        myY = y;
        myZ = z;
    }

    /**
     * Construct me.
     *
     * @param vector vector to copy
     */
    public Vector3d(Vector3d vector)
    {
        myX = vector.getX();
        myY = vector.getY();
        myZ = vector.getZ();
    }

    /**
     * Construct me.
     *
     * @param vector vector to copy
     */
    public Vector3d(Vector3f vector)
    {
        myX = vector.getX();
        myY = vector.getY();
        myZ = vector.getZ();
    }

    /**
     * Add the given vector to me.
     *
     * @param vector vector
     * @return newly created sum vector.
     */
    public Vector3d add(Vector3d vector)
    {
        return new Vector3d(myX + vector.myX, myY + vector.myY, myZ + vector.myZ);
    }

    /**
     * Converts a point from Cartesian coordinates (using positive Y as up) to
     * Spherical and stores the results in the store var. (Radius, Azimuth,
     * Polar)
     *
     * @return spherical coordinates
     */
    public Vector3d cartesianToSpherical()
    {
        double x = getLength();
        double y = Math.atan2(myZ, myX);
        if (myX < 0)
        {
            y += Math.PI;
        }
        double z = Math.asin(myY / x);
        return new Vector3d(x, y, z);
    }

    /**
     * Converts a point from Cartesian coordinates (using positive Z as up) to
     * Spherical and stores the results in the store var. (Radius, Azimuth,
     * Polar)
     *
     * @return spherical coordinates
     */
    public Vector3d cartesianZToSpherical()
    {
        double x = getLength();
        double z = Math.atan2(myZ, myX);
        if (myX < 0)
        {
            z += Math.PI;
        }
        double y = Math.asin(myY / x);
        return new Vector3d(x, y, z);
    }

    @Override
    public Vector3d clone()
    {
        try
        {
            return (Vector3d)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }

    @Override
    public int compareTo(Vector3d other)
    {
        int result = MathUtil.isZero(myX - other.myX) ? 0 : myX < other.myX ? -1 : 1;

        if (result == 0)
        {
            result = MathUtil.isZero(myY - other.myY) ? 0 : myY < other.myY ? -1 : 1;
        }

        if (result == 0)
        {
            result = MathUtil.isZero(myZ - other.myZ) ? 0 : myZ < other.myZ ? -1 : 1;
        }

        return result;
    }

    /**
     * Cross me with the given vector.
     *
     * @param vector vector to cross me with
     * @return resultant vector.
     */
    public Vector3d cross(Vector3d vector)
    {
        return new Vector3d(myY * vector.myZ - myZ * vector.myY, myZ * vector.myX - myX * vector.myZ,
                myX * vector.myY - myY * vector.myX);
    }

    @Override
    public double distanceSquared(AbstractVector in)
    {
        if (in instanceof Vector3d)
        {
            Vector3d vec3 = (Vector3d)in;
            double arg1 = myX - vec3.myX;
            double arg2 = myY - vec3.myY;
            double arg3 = myZ - vec3.myZ;
            return arg1 * arg1 + arg2 * arg2 + arg3 * arg3;
        }
        return 0;
    }

    /**
     * Create a new vector which is the result of dividing me my the given
     * value.
     *
     * @param value value
     * @return resultant vector.
     */
    public Vector3d divide(double value)
    {
        return multiply(1.0 / value);
    }

    /**
     * Create a new vector which is the result of dividing me my the given
     * value.
     *
     * @param vector value
     * @return resultant vector.
     */
    public Vector3d divide(Vector3d vector)
    {
        return new Vector3d(myX / vector.myX, myY / vector.myY, myZ / vector.myZ);
    }

    /**
     * Get the dot product of this vector with the given vector.
     *
     * @param vector The vector to get the dot product with.
     * @return the dot product.
     */
    public double dot(Vector3d vector)
    {
        return myX * vector.myX + myY * vector.myY + myZ * vector.myZ;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        Vector3d other = (Vector3d)obj;
        return MathUtil.isZero(myX - other.myX) && MathUtil.isZero(myY - other.myY) && MathUtil.isZero(myZ - other.myZ);
    }

    /**
     * Determine whether the vectors are equal to within a given tolerance.
     *
     * @param vec The vector for comparison.
     * @param tolerance The acceptable tolerance.
     * @return true when each component of the vector close to the other vector
     *         with in the given tolerance.
     */
    public boolean equals(Vector3d vec, double tolerance)
    {
        return MathUtil.isZero(myX - vec.myX, tolerance) && MathUtil.isZero(myY - vec.myY, tolerance)
                && MathUtil.isZero(myZ - vec.myZ, tolerance);
    }

    /**
     * Get the component of the vector as if it were an array in the order X, Y,
     * Z.
     *
     * @param index index
     * @return value
     */
    public double get(int index)
    {
        if (index == 0)
        {
            return myX;
        }
        else if (index == 1)
        {
            return myY;
        }
        return myZ;
    }

    /**
     * Get the angle difference between another vector and me. If you know you
     * have unit vectors, this is very inefficient. Instead use
     * getAngleDifferenceUnit(vec).
     *
     * @param vec The other vector.
     * @return The angle difference in radians.
     */
    public double getAngleDifference(Vector3d vec)
    {
        return Math.acos(MathUtil.clamp(dot(vec) / getLength() / vec.getLength(), -1., 1.));
    }

    /**
     * Get the angle difference between another vector and me when both vectors
     * are normalized.
     *
     * @param vec The other vector.
     * @return The angle difference in radians.
     */
    public double getAngleDifferenceUnit(Vector3d vec)
    {
        return Math.acos(MathUtil.clamp(dot(vec), -1., 1.));
    }

    /**
     * Get the angle between another vector and me when both vectors are
     * normalized. This method is sensitive to the direction required to rotate
     * from this vector to the given vector. If the cross product of this vector
     * with the given vector is in the same direction as the normal vector, then
     * rotation is positive.
     *
     * @param vec The other vector.
     * @param normal The vector normal to the plan defined by this vector and
     *            the other vector. The direction of the normal determines the
     *            positive side of the plane.
     * @return The angle difference in radians.
     */
    public double getAngleUnit(Vector3d vec, Vector3d normal)
    {
        double sign = Math.signum(cross(vec).dot(normal));
        sign = sign == 0. ? 1. : sign;
        return sign * Math.acos(MathUtil.clamp(dot(vec), -1., 1.));
    }

    @Override
    public double getLengthSquared()
    {
        return myX * myX + myY * myY + myZ * myZ;
    }

    /**
     * Get a normalized version of this vector.
     *
     * @return normalized vector
     */
    public Vector3d getNormalized()
    {
        double length = getLength();
        if (MathUtil.isZero(length))
        {
            return this;
        }
        return new Vector3d(myX / length, myY / length, myZ / length);
    }

    @Override
    public MutableVector3d getWrapper()
    {
        return new MutableVector3d(this);
    }

    /**
     * Get the x.
     *
     * @return the x
     */
    public double getX()
    {
        return myX;
    }

    /**
     * Get the y.
     *
     * @return the y
     */
    public double getY()
    {
        return myY;
    }

    /**
     * Get the z.
     *
     * @return the z
     */
    public double getZ()
    {
        return myZ;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        long result = 1;
        result = prime * result + Double.doubleToLongBits(myX);
        result = prime * result + Double.doubleToLongBits(myY);
        result = prime * result + Double.doubleToLongBits(myZ);
        return (int)result;
    }

    /**
     * Interpolate betwixt me and the given vector.
     *
     * @param vec other vector
     * @param fraction percentage along to other vector
     * @return interpolated point
     */
    public Vector3d interpolate(Vector3d vec, double fraction)
    {
        return multiply(1. - fraction).add(vec.multiply(fraction));
    }

    /**
     * Create a vector which is this*value.
     *
     * @param value value
     * @return resultant vector.
     */
    public Vector3d multiply(double value)
    {
        return new Vector3d(myX * value, myY * value, myZ * value);
    }

    /**
     * Create a vector which is this*vector.
     *
     * @param vector The vector to use.
     * @return resultant vector.
     */
    public Vector3d multiply(Vector3d vector)
    {
        return new Vector3d(myX * vector.myX, myY * vector.myY, myZ * vector.myZ);
    }

    /**
     * Rotate this vector around another vector.
     *
     * @param axis The normalized axis of rotation.
     * @param angleR The amount to rotate, in radians.
     * @return The rotated vector.
     */
    public Vector3d rotate(Vector3d axis, double angleR)
    {
        double fCos = Math.cos(angleR);
        double fSin = Math.sin(angleR);
        double fOneMinusCos = 1.0 - fCos;
        double fX2 = axis.getX() * axis.getX();
        double fY2 = axis.getY() * axis.getY();
        double fZ2 = axis.getZ() * axis.getZ();
        double fXYM = axis.getX() * axis.getY() * fOneMinusCos;
        double fXZM = axis.getX() * axis.getZ() * fOneMinusCos;
        double fYZM = axis.getY() * axis.getZ() * fOneMinusCos;
        double fXSin = axis.getX() * fSin;
        double fYSin = axis.getY() * fSin;
        double fZSin = axis.getZ() * fSin;

        return new Vector3d((fX2 * fOneMinusCos + fCos) * getX() + (fXYM - fZSin) * getY() + (fXZM + fYSin) * getZ(),
                (fXYM + fZSin) * getX() + (fY2 * fOneMinusCos + fCos) * getY() + (fYZM - fXSin) * getZ(),
                (fXZM - fYSin) * getX() + (fYZM + fXSin) * getY() + (fZ2 * fOneMinusCos + fCos) * getZ());
    }

    /**
     * Converts this point from Spherical coordinates to Cartesian using
     * positive Y as up.
     *
     * @return Cartesian coordinates.
     */
    public Vector3d sphericalToCartesian()
    {
        double y = myX * Math.sin(myZ);
        double a = myX * Math.cos(myZ);
        double x = a * Math.cos(myY);
        double z = a * Math.sin(myY);

        return new Vector3d(x, y, z);
    }

    /**
     * Converts this point from Spherical coordinates to Cartesian using
     * positive Z as up.
     *
     * @return Cartesian coordinates.
     */
    public Vector3d sphericalToCartesianZ()
    {
        double z = myX * Math.sin(myZ);
        double a = myX * Math.cos(myZ);
        double x = a * Math.cos(myY);
        double y = a * Math.sin(myY);

        return new Vector3d(x, y, z);
    }

    /**
     * Get a vector that is the given vector rotated 90 degrees in the direction
     * of this vector.
     *
     * @param vec The other vector.
     * @return The square vector.
     */
    public Vector3d square(Vector3d vec)
    {
        return vec.cross(this).cross(vec);
    }

    /**
     * Create a vector which is the result of subtracting the given vector from
     * me.
     *
     * @param vector vector
     * @return resultant vector
     */
    public Vector3d subtract(Vector3d vector)
    {
        return new Vector3d(myX - vector.myX, myY - vector.myY, myZ - vector.myZ);
    }

    /**
     * Convert me to an array [x, y, z].
     *
     * @return resultant array.
     */
    public double[] toArray()
    {
        double[] dubs = new double[3];
        dubs[0] = myX;
        dubs[1] = myY;
        dubs[2] = myZ;
        return dubs;
    }

    /**
     * Put my values into the buffer.
     *
     * @param buffer buffer to insert into.
     */
    public void toDoubleBuffer(DoubleBuffer buffer)
    {
        buffer.put(myX);
        buffer.put(myY);
        buffer.put(myZ);
    }

    /**
     * Convert me to an array [x, y, z].
     *
     * @return resultant array.
     */
    public float[] toFloatArray()
    {
        float[] arr = new float[3];
        arr[0] = (float)myX;
        arr[1] = (float)myY;
        arr[2] = (float)myZ;
        return arr;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append('[');
        sb.append(myX).append(' ');
        sb.append(myY).append(' ');
        sb.append(myZ);
        sb.append(']');
        return sb.toString();
    }

    /**
     * Check whether {@link Double#isNaN(double)} returns false for all of my
     * coordinates.
     *
     * @return true when {@link Double#isNaN(double)} returns false for all of
     *         my coordinates.
     */
    public boolean validate()
    {
        return !(Double.isNaN(myX) || Double.isNaN(myY) || Double.isNaN(myZ));
    }
}
