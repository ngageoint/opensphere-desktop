package io.opensphere.core.math;

import java.nio.FloatBuffer;

import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.lang.ExpectedCloneableException;

/**
 * A vector in 3 dimensions.
 */
public class Vector3f extends AbstractVector implements Cloneable
{
    /** A vector with zero for each component. */
    public static final Vector3f ORIGIN = new Vector3f(0f, 0f, 0f);

    /** How many bytes used internally by one of these objects. */
    public static final int SIZE_BYTES = MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.FLOAT_SIZE_BYTES * 3,
            Constants.MEMORY_BLOCK_SIZE_BYTES);

    /** A unit vector in the X direction. */
    public static final Vector3f UNIT_X = new Vector3f(1f, 0f, 0f);

    /** A unit vector in the Y direction. */
    public static final Vector3f UNIT_Y = new Vector3f(0f, 1f, 0f);

    /** A unit vector in the Z direction. */
    public static final Vector3f UNIT_Z = new Vector3f(0f, 0f, 1f);

    /** X vector component. */
    private final float myX;

    /** Y vector component. */
    private final float myY;

    /** Z vector component. */
    private final float myZ;

    /**
     * Construct a normalized Vector3f.
     *
     * @param x The x component.
     * @param y The y component.
     * @param z The z component.
     * @return The new vector.
     */
    public static Vector3f normalize(float x, float y, float z)
    {
        double length = Math.sqrt(x * x + y * y + z * z);
        if (MathUtil.isZero(length))
        {
            return new Vector3f(x, y, z);
        }
        else
        {
            return new Vector3f((float)(x / length), (float)(y / length), (float)(z / length));
        }
    }

    /**
     * Construct me.
     *
     * @param x x coordinate.
     * @param y y coordinate.
     * @param z z coordinate.
     */
    public Vector3f(float x, float y, float z)
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
    public Vector3f(Vector3d vector)
    {
        myX = (float)vector.getX();
        myY = (float)vector.getY();
        myZ = (float)vector.getZ();
    }

    /**
     * Construct me.
     *
     * @param vector vector to copy
     */
    public Vector3f(Vector3f vector)
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
    public Vector3f add(Vector3f vector)
    {
        return new Vector3f(myX + vector.myX, myY + vector.myY, myZ + vector.myZ);
    }

    /**
     * Converts a point from Cartesian coordinates (using positive Y as up) to
     * Spherical and stores the results in the store var. (Radius, Azimuth,
     * Polar)
     *
     * @return spherical coordinates
     */
    public Vector3f cartesianToSpherical()
    {
        double x = getLength();
        double y = Math.atan2(myZ, myX);
        if (myX < 0)
        {
            y += Math.PI;
        }
        double z = Math.asin(myY / x);
        return new Vector3f((float)x, (float)y, (float)z);
    }

    /**
     * Converts a point from Cartesian coordinates (using positive Z as up) to
     * Spherical and stores the results in the store var. (Radius, Azimuth,
     * Polar)
     *
     * @return spherical coordinates
     */
    public Vector3f cartesianZToSpherical()
    {
        double x = getLength();
        double z = Math.atan2(myZ, myX);
        if (myX < 0)
        {
            z += Math.PI;
        }
        double y = Math.asin(myY / x);
        return new Vector3f((float)x, (float)y, (float)z);
    }

    @Override
    public Vector3f clone()
    {
        try
        {
            return (Vector3f)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }

    /**
     * Cross me with the given vector.
     *
     * @param vector vector to cross me with
     * @return resultant vector.
     */
    public Vector3f cross(Vector3f vector)
    {
        return new Vector3f(myY * vector.myZ - myZ * vector.myY, myZ * vector.myX - myX * vector.myZ,
                myX * vector.myY - myY * vector.myX);
    }

    @Override
    public double distanceSquared(AbstractVector in)
    {
        if (in instanceof Vector3f)
        {
            Vector3f vec3 = (Vector3f)in;
            float arg1 = myX - vec3.myX;
            float arg2 = myY - vec3.myY;
            float arg3 = myZ - vec3.myZ;
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
    public Vector3f divide(float value)
    {
        return multiply(1f / value);
    }

    /**
     * Create a new vector which is the result of dividing me my the given
     * value.
     *
     * @param vector value
     * @return resultant vector.
     */
    public Vector3f divide(Vector3f vector)
    {
        return new Vector3f(myX / vector.myX, myY / vector.myY, myZ / vector.myZ);
    }

    /**
     * Get the dot product of this vector with the given vector.
     *
     * @param vector The vector to get the dot product with.
     * @return the dot product.
     */
    public double dot(Vector3f vector)
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
        Vector3f other = (Vector3f)obj;
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
    public boolean equals(Vector3f vec, float tolerance)
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
    public float get(int index)
    {
        if (index == 0)
        {
            return myX;
        }
        else if (index == 1)
        {
            return myY;
        }
        else
        {
            return myZ;
        }
    }

    /**
     * Get the angle difference between another vector of floats and me. If you
     * know you have unit vectors, this is very inefficient. Instead use
     * getAngleDifferenceUnit(vec).
     *
     * @param vec The other vector.
     * @return The angle difference in radians.
     */
    public double getAngleDifference(Vector3f vec)
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
    public double getAngleDifferenceUnit(Vector3f vec)
    {
        return Math.acos(MathUtil.clamp(dot(vec), -1., 1.));
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
    public Vector3f getNormalized()
    {
        double length = getLength();
        if (MathUtil.isZero(length))
        {
            return this;
        }
        else
        {
            return new Vector3f((float)(myX / length), (float)(myY / length), (float)(myZ / length));
        }
    }

    /**
     * Get the x.
     *
     * @return the x
     */
    public float getX()
    {
        return myX;
    }

    /**
     * Get the y.
     *
     * @return the y
     */
    public float getY()
    {
        return myY;
    }

    /**
     * Get the z.
     *
     * @return the z
     */
    public float getZ()
    {
        return myZ;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(myX);
        result = prime * result + Float.floatToIntBits(myY);
        result = prime * result + Float.floatToIntBits(myZ);
        return result;
    }

    /**
     * Interpolate betwixt me and the given vector.
     *
     * @param vec other vector
     * @param fraction percentage along to other vector
     * @return interpolated point
     */
    public Vector3f interpolate(Vector3f vec, float fraction)
    {
        return multiply(1f - fraction).add(vec.multiply(fraction));
    }

    /**
     * Create a vector which is this*value.
     *
     * @param value value
     * @return resultant vector.
     */
    public Vector3f multiply(float value)
    {
        return new Vector3f(myX * value, myY * value, myZ * value);
    }

    /**
     * Create a vector which is this*vector.
     *
     * @param vector The vector to use.
     * @return resultant vector.
     */
    public Vector3f multiply(Vector3f vector)
    {
        return new Vector3f(myX * vector.myX, myY * vector.myY, myZ * vector.myZ);
    }

    /**
     * Converts this point from Spherical coordinates to Cartesian using
     * positive Y as up.
     *
     * @return Cartesian coordinates.
     */
    public Vector3f sphericalToCartesian()
    {
        double y = myX * Math.sin(myZ);
        double a = myX * Math.cos(myZ);
        double x = a * Math.cos(myY);
        double z = a * Math.sin(myY);

        return new Vector3f((float)x, (float)y, (float)z);
    }

    /**
     * Converts this point from Spherical coordinates to Cartesian using
     * positive Z as up.
     *
     * @return Cartesian coordinates.
     */
    public Vector3f sphericalToCartesianZ()
    {
        double z = myX * Math.sin(myZ);
        double a = myX * Math.cos(myZ);
        double x = a * Math.cos(myY);
        double y = a * Math.sin(myY);

        return new Vector3f((float)x, (float)y, (float)z);
    }

    /**
     * Get a unit vector that is the given vector rotated 90 degrees in the
     * direction of this vector.
     *
     * @param vec The other vector.
     * @return The square vector.
     */
    public Vector3f square(Vector3f vec)
    {
        Vector3f cross = vec.cross(this);
        return cross.cross(vec).getNormalized();
    }

    /**
     * Create a vector which is the result of subtracting the given vector from
     * me.
     *
     * @param vector vector
     * @return resultant vector
     */
    public Vector3f subtract(Vector3f vector)
    {
        return new Vector3f(myX - vector.myX, myY - vector.myY, myZ - vector.myZ);
    }

    /**
     * Convert me to an array [x, y, z].
     *
     * @return resultant array.
     */
    public float[] toArray()
    {
        float[] dubs = new float[3];
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
    public void toFloatBuffer(FloatBuffer buffer)
    {
        buffer.put(myX);
        buffer.put(myY);
        buffer.put(myZ);
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
     * Check whether {@link Float#isNaN(float)} returns false for all of my
     * coordinates.
     *
     * @return true when {@link Float#isNaN(float)} returns false for all of my
     *         coordinates.
     */
    public boolean validate()
    {
        return !(Float.isNaN(myX) || Float.isNaN(myY) || Float.isNaN(myZ));
    }
}
