package io.opensphere.core.math;

import io.opensphere.core.util.MathUtil;

/**
 * Defines a single example of a more general class of hypercomplex numbers.
 * Quaternions extends a rotation in three dimensions to a rotation in four
 * dimensions. This avoids "gimbal lock" and allows for smooth continuous
 * rotation.
 *
 * <code>Quaternion</code> is defined by four doubleing point numbers: {x y z
 * w}.
 */
public final class Quaternion
{
    /**
     * Small number used to determine when two quaternions are close to
     * identical.
     */
    private static final double DOT_PRODUCT_EPS = 0.1;

    /** Small number used to determine when a number is close to .5. */
    private static final double HALF_MINUS_EPS = 0.499;

    /** W component. */
    private double myW;

    /** X component. */
    private double myX;

    /** Y component. */
    private double myY;

    /** Z component. */
    private double myZ;

    /**
     * Creates a <code>Quaternion</code> that represents the coordinate system
     * defined by three axes. These axes are assumed to be orthogonal and no
     * error checking is applied. Thus, the user must insure that the three axes
     * being provided indeed represents a proper right handed coordinate system.
     *
     * @param xAxis vector representing the x-axis of the coordinate system.
     * @param yAxis vector representing the y-axis of the coordinate system.
     * @param zAxis vector representing the z-axis of the coordinate system.
     * @return quaternion
     */
    public static Quaternion fromAxes(Vector3d xAxis, Vector3d yAxis, Vector3d zAxis)
    {
        Matrix3d matrix = new Matrix3d(xAxis, yAxis, zAxis);
        return matrix.getQuaternion();
    }

    /**
     * Creates a <code>Quaternion</code> that represents the coordinate system
     * defined by three axes. These axes are assumed to be orthogonal and no
     * error checking is applied. Thus, the user must insure that the three axes
     * being provided indeed represents a proper right handed coordinate system.
     *
     * @param axis the array containing the three vectors representing the
     *            coordinate system.
     * @return quaternion
     */
    public static Quaternion fromAxes(Vector3d[] axis)
    {
        if (axis.length != 3)
        {
            throw new IllegalArgumentException("Axis array must have three elements");
        }
        return fromAxes(axis[0], axis[1], axis[2]);
    }

    /**
     * Create a quaternion based on a direction and an up vector. It computes
     * the rotation to transform the z-axis to point into 'direction' and the
     * y-axis to 'up'.
     *
     * @param direction where to look at in terms of local coordinates
     * @param up a vector indicating the local up direction.
     * @return The quaternion.
     */
    public static Quaternion lookAt(Vector3d direction, Vector3d up)
    {
        Vector3d tmpXaxis = new Vector3d(up).cross(direction).getNormalized();
        Vector3d tmpZaxis = new Vector3d(direction).getNormalized();
        Vector3d tmpYaxis = new Vector3d(tmpZaxis).cross(tmpXaxis);

        return fromAxes(tmpXaxis, tmpYaxis, tmpZaxis);
    }

    /**
     * Constructor instantiates a new <code>Quaternion</code> object
     * initializing all values to zero, except w which is initialized to 1.
     */
    public Quaternion()
    {
        myX = 0;
        myY = 0;
        myZ = 0;
        myW = 1;
    }

    /**
     * Constructor instantiates a new <code>Quaternion</code> object from the
     * given list of parameters.
     *
     * @param x the x value of the quaternion.
     * @param y the y value of the quaternion.
     * @param z the z value of the quaternion.
     * @param w the w value of the quaternion.
     */
    public Quaternion(double x, double y, double z, double w)
    {
        myX = x;
        myY = y;
        myZ = z;
        myW = w;
    }

    /**
     * Constructor instantiates a new <code>Quaternion</code> object from a
     * collection of rotation angles.
     *
     * @param angles the angles of rotation (x, y, z) that will define the
     *            <code>Quaternion</code>.
     */
    public Quaternion(double[] angles)
    {
        fromAngles(angles);
    }

    /**
     * Constructor instantiates a new <code>Quaternion</code> object from an
     * existing quaternion, creating a copy.
     *
     * @param q the quaternion to copy.
     */
    public Quaternion(Quaternion q)
    {
        myX = q.myX;
        myY = q.myY;
        myZ = q.myZ;
        myW = q.myW;
    }

    /**
     * Constructor instantiates a new <code>Quaternion</code> object from an
     * interpolation between two other quaternions.
     *
     * @param q1 the first quaternion.
     * @param q2 the second quaternion.
     * @param interp the amount to interpolate between the two quaternions.
     */
    public Quaternion(Quaternion q1, Quaternion q2, double interp)
    {
        slerp(q1, q2, interp);
    }

    /**
     * Adds the values of this quaternion to those of the parameter quaternion.
     * The result is returned as a new quaternion.
     *
     * @param q the quaternion to add to this.
     * @return the new quaternion.
     */
    public Quaternion add(Quaternion q)
    {
        return new Quaternion(myX + q.myX, myY + q.myY, myZ + q.myZ, myW + q.myW);
    }

    /**
     * Adds the values of this quaternion to those of the parameter quaternion.
     * The result is stored in this Quaternion.
     *
     * @param q the quaternion to add to this.
     */
    public void addLocal(Quaternion q)
    {
        myX += q.myX;
        myY += q.myY;
        myZ += q.myZ;
        myW += q.myW;
    }

    /**
     * multiplies this quaternion by a parameter matrix internally.
     *
     * @param matrix the matrix to apply to this quaternion.
     */
    public void apply(Matrix3d matrix)
    {
        double oldX = myX;
        double oldY = myY;
        double oldZ = myZ;
        double oldW = myW;
        Quaternion quat = matrix.getQuaternion();
        double tempX = quat.getX();
        double tempY = quat.getY();
        double tempZ = quat.getZ();
        double tempW = quat.getW();

        myX = oldX * tempW + oldY * tempZ - oldZ * tempY + oldW * tempX;
        myY = -oldX * tempZ + oldY * tempW + oldZ * tempX + oldW * tempY;
        myZ = oldX * tempY - oldY * tempX + oldZ * tempW + oldW * tempZ;
        myW = -oldX * tempX - oldY * tempY - oldZ * tempZ + oldW * tempW;
    }

    /**
     * calculates and returns the dot product of this quaternion with that of
     * the parameter quaternion.
     *
     * @param q the quaternion to calculate the dot product of.
     * @return the dot product of this and the parameter quaternion.
     */
    public double dot(Quaternion q)
    {
        return myW * q.myW + myX * q.myX + myY * q.myY + myZ * q.myZ;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Quaternion))
        {
            return false;
        }

        if (this == o)
        {
            return true;
        }

        Quaternion comp = (Quaternion)o;
        return Double.compare(myX, comp.myX) == 0 && Double.compare(myY, comp.myY) == 0 && Double.compare(myZ, comp.myZ) == 0
                && Double.compare(myW, comp.myW) == 0;
    }

    /**
     * sets this quaternion to the values specified by an angle and an axis of
     * rotation. This method creates an object, so use fromAngleNormalAxis if
     * your axis is already normalized.
     *
     * @param angle the angle to rotate (in radians).
     * @param axis the axis of rotation.
     */
    public void fromAngleAxis(double angle, Vector3d axis)
    {
        Vector3d normAxis = axis.getNormalized();
        fromAngleNormalAxis(angle, normAxis);
    }

    /**
     * sets this quaternion to the values specified by an angle and a normalized
     * axis of rotation.
     *
     * @param angle the angle to rotate (in radians).
     * @param axis the axis of rotation (already normalized).
     */
    public void fromAngleNormalAxis(double angle, Vector3d axis)
    {
        if (axis.getX() == 0 && axis.getY() == 0 && axis.getZ() == 0)
        {
            loadIdentity();
        }
        else
        {
            double halfAngle = 0.5 * angle;
            double sin = Math.sin(halfAngle);
            myW = Math.cos(halfAngle);
            myX = sin * axis.getX();
            myY = sin * axis.getY();
            myZ = sin * axis.getZ();
        }
    }

    /**
     * builds a Quaternion from the Euler rotation angles (y,r,p). Note that we
     * are applying in order: roll, pitch, yaw but we've ordered them in x, y,
     * and z for convenience. See: http://www.euclideanspace
     * .com/maths/geometry/rotations/conversions/eulerToQuaternion/index.htm
     *
     * @param yaw the Euler yaw of rotation (in radians). (aka Bank, often rot
     *            around x)
     * @param roll the Euler roll of rotation (in radians). (aka Heading, often
     *            rot around y)
     * @param pitch the Euler pitch of rotation (in radians). (aka Attitude,
     *            often rot around z)
     * @return quaternion built from the given angles.
     */
    public Quaternion fromAngles(double yaw, double roll, double pitch)
    {
        double angle;
        angle = pitch * 0.5;
        double sinPitch = Math.sin(angle);
        double cosPitch = Math.cos(angle);
        angle = roll * 0.5;
        double sinRoll = Math.sin(angle);
        double cosRoll = Math.cos(angle);
        angle = yaw * 0.5;
        double sinYaw = Math.sin(angle);
        double cosYaw = Math.cos(angle);
        // variables used to reduce multiplication calls.
        double cosRollXcosPitch = cosRoll * cosPitch;
        double sinRollXsinPitch = sinRoll * sinPitch;
        double cosRollXsinPitch = cosRoll * sinPitch;
        double sinRollXcosPitch = sinRoll * cosPitch;

        myW = cosRollXcosPitch * cosYaw - sinRollXsinPitch * sinYaw;
        myX = cosRollXcosPitch * sinYaw + sinRollXsinPitch * cosYaw;
        myY = sinRollXcosPitch * cosYaw + cosRollXsinPitch * sinYaw;
        myZ = cosRollXsinPitch * cosYaw - sinRollXcosPitch * sinYaw;

        normalize();
        return this;
    }

    /**
     * builds a quaternion from the Euler rotation angles (y,r,p).
     *
     * @param angles the Euler angles of rotation (in radians).
     */
    public void fromAngles(double[] angles)
    {
        if (angles.length != 3)
        {
            throw new IllegalArgumentException("Angles array must have three elements");
        }

        fromAngles(angles[0], angles[1], angles[2]);
    }

    /**
     * Returns one of three columns specified by the parameter. This column is
     * returned as a <code>Vector3d</code> object. The value is retrieved as if
     * this quaternion was first normalized.
     *
     * @param i the column to retrieve. Must be between 0 and 2.
     * @return the column specified by the index.
     */
    public Vector3d getRotationColumn(int i)
    {
        // TODO: This sqrt doesn't seem right.
        double norm = length();
        if (norm != 1.0)
        {
            norm = 1. / Math.sqrt(norm);
        }

        double xx = myX * myX * norm;
        double xy = myX * myY * norm;
        double xz = myX * myZ * norm;
        double xw = myX * myW * norm;
        double yy = myY * myY * norm;
        double yz = myY * myZ * norm;
        double yw = myY * myW * norm;
        double zz = myZ * myZ * norm;
        double zw = myZ * myW * norm;

        switch (i)
        {
            case 0:
                return new Vector3d(1 - 2 * (yy + zz), 2 * (xy + zw), 2 * (xz - yw));
            case 1:
                return new Vector3d(2 * (xy - zw), 1 - 2 * (xx + zz), 2 * (yz + xw));
            case 2:
                return new Vector3d(2 * (xz + yw), 2 * (yz - xw), 1 - 2 * (xx + yy));
            default:
                throw new IllegalArgumentException("Bad column value: " + i);
        }
    }

    /**
     * Get the w.
     *
     * @return the w
     */
    public double getW()
    {
        return myW;
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
        long hash = 37;
        hash = 37 * hash + Double.doubleToLongBits(myX);
        hash = 37 * hash + Double.doubleToLongBits(myY);
        hash = 37 * hash + Double.doubleToLongBits(myZ);
        hash = 37 * hash + Double.doubleToLongBits(myW);
        return (int)hash;
    }

    /**
     * returns the inverse of this quaternion as a new quaternion. If this
     * quaternion does not have an inverse (if its normal is 0 or less), then
     * null is returned.
     *
     * @return the inverse of this quaternion or null if the inverse does not
     *         exist.
     */
    public Quaternion inverse()
    {
        double norm = length();
        if (norm > 0.0)
        {
            double invNorm = 1.0 / norm;
            return new Quaternion(-myX * invNorm, -myY * invNorm, -myZ * invNorm, myW * invNorm);
        }
        // return an invalid result to flag the error
        return null;
    }

    /**
     * calculates the inverse of this quaternion and returns this quaternion
     * after it is calculated. If this quaternion does not have an inverse (if
     * it's norma is 0 or less), nothing happens
     *
     * @return the inverse of this quaternion
     */
    public Quaternion inverseLocal()
    {
        double norm = length();
        if (norm > 0.0)
        {
            double invNorm = 1.0 / norm;
            myX *= -invNorm;
            myY *= -invNorm;
            myZ *= -invNorm;
            myW *= invNorm;
        }
        return this;
    }

    /**
     * Check whether I am the identity.
     *
     * @return true if this Quaternion is {0,0,0,1}
     */
    public boolean isIdentity()
    {
        return MathUtil.isZero(myX) && MathUtil.isZero(myY) && MathUtil.isZero(myZ) && MathUtil.isZero(myW - 1.);
    }

    /**
     * returns the norm of this quaternion. This is the dot product of this
     * quaternion with itself.
     *
     * @return the length of the quaternion.
     */
    public double length()
    {
        return myW * myW + myX * myX + myY * myY + myZ * myZ;
    }

    /**
     * Sets this Quaternion to {0, 0, 0, 1}. Same as calling set(0,0,0,1).
     */
    public void loadIdentity()
    {
        myX = myY = myZ = 0;
        myW = 1;
    }

    /**
     * multiplies this quaternion by a parameter scalar. The result is returned
     * as a new quaternion.
     *
     * @param scalar the quaternion to multiply this quaternion by.
     * @return the new quaternion.
     */
    public Quaternion mult(double scalar)
    {
        return new Quaternion(scalar * myX, scalar * myY, scalar * myZ, scalar * myW);
    }

    /**
     * multiplies this quaternion by a parameter quaternion (q). 'this' is not
     * modified. It should be noted that quaternion multiplication is not
     * cummulative so q * p != p * q.
     *
     * @param quat the quaternion to multiply this quaternion by.
     * @return If specified res is null, then a new Quaternion; otherwise
     *         returns the populated 'res'.
     */
    public Quaternion mult(Quaternion quat)
    {
        Quaternion res = new Quaternion();
        double qw = quat.myW;
        double qx = quat.myX;
        double qy = quat.myY;
        double qz = quat.myZ;
        res.myX = myX * qw + myY * qz - myZ * qy + myW * qx;
        res.myY = -myX * qz + myY * qw + myZ * qx + myW * qy;
        res.myZ = myX * qy - myY * qx + myZ * qw + myW * qz;
        res.myW = -myX * qx - myY * qy - myZ * qz + myW * qw;
        return res;
    }

    /**
     * multiplies this quaternion by a parameter vector. The result is returned
     * as a new vector. 'this' is not modified.
     *
     * @param vect the vector to multiply this quaternion by.
     * @return the result vector.
     */
    public Vector3d mult(Vector3d vect)
    {
        if (vect.getX() == 0 && vect.getY() == 0 && vect.getZ() == 0)
        {
            return Vector3d.ORIGIN;
        }
        return new Vector3d(
                myW * myW * vect.getX() + 2 * myY * myW * vect.getZ() - 2 * myZ * myW * vect.getY() + myX * myX * vect.getX()
                + 2 * myY * myX * vect.getY() + 2 * myZ * myX * vect.getZ() - myZ * myZ * vect.getX()
                - myY * myY * vect.getX(),
                2 * myX * myY * vect.getX() + myY * myY * vect.getY() + 2 * myZ * myY * vect.getZ() + 2 * myW * myZ * vect.getX()
                - myZ * myZ * vect.getY() + myW * myW * vect.getY() - 2 * myX * myW * vect.getZ()
                - myX * myX * vect.getY(),
                2 * myX * myZ * vect.getX() + 2 * myY * myZ * vect.getY() + myZ * myZ * vect.getZ() - 2 * myW * myY * vect.getX()
                - myY * myY * vect.getZ() + 2 * myW * myX * vect.getY() - myX * myX * vect.getZ()
                + myW * myW * vect.getZ());
    }

    /**
     * multiplies this quaternion by a parameter scalar. The result is stored
     * locally.
     *
     * @param scalar the quaternion to multiply this quaternion by.
     */
    public void multLocal(double scalar)
    {
        myW *= scalar;
        myX *= scalar;
        myY *= scalar;
        myZ *= scalar;
    }

    /**
     * Multiplies this Quaternion by the supplied quaternion. The result is
     * stored in this Quaternion, which is also returned for chaining. Similar
     * to this *= q.
     *
     * @param qx quat x value
     * @param qy quat y value
     * @param qz quat z value
     * @param qw quat w value
     */
    public void multLocal(double qx, double qy, double qz, double qw)
    {
        double x1 = myX * qw + myY * qz - myZ * qy + myW * qx;
        double y1 = -myX * qz + myY * qw + myZ * qx + myW * qy;
        double z1 = myX * qy - myY * qx + myZ * qw + myW * qz;
        myW = -myX * qx - myY * qy - myZ * qz + myW * qw;
        myX = x1;
        myY = y1;
        myZ = z1;
    }

    /**
     * Multiplies this Quaternion by the supplied quaternion. The result is
     * stored in this Quaternion, which is also returned for chaining. Similar
     * to this *= q.
     *
     * @param q The Quaternion to multiply this one by.
     */
    public void multLocal(Quaternion q)
    {
        multLocal(q.getX(), q.getY(), q.getZ(), q.getW());
    }

    /**
     * Make me unit length.
     */
    public void normalize()
    {
        double n = 1d / Math.sqrt(length());
        myX *= n;
        myY *= n;
        myZ *= n;
        myW *= n;
    }

    /**
     * TODO: This seems to have singularity type issues with angle == 0,
     * possibly others such as PI.
     *
     * @return The quaternion that describes a rotation that would point you in
     *         the exact opposite direction of this Quaternion.
     */
    public Quaternion opposite()
    {
        throw new UnsupportedOperationException("This method needs to be revisited before it should be used.");
        //        Quaternion retQuat = new Quaternion();
        //
        //        Vector3d axis = new Vector3d();
        //        double angle = toAngleAxis(axis);
        //
        //        retQuat.fromAngleAxis(Math.PI + angle, axis);
        //        return retQuat;
    }

    /**
     * sets the data in a <code>Quaternion</code> object from the given list of
     * parameters.
     *
     * @param x the x value of the quaternion.
     * @param y the y value of the quaternion.
     * @param z the z value of the quaternion.
     * @param w the w value of the quaternion.
     */
    public void set(double x, double y, double z, double w)
    {
        myX = x;
        myY = y;
        myZ = z;
        myW = w;
    }

    /**
     * Sets the data in this <code>Quaternion</code> object to be equal to the
     * passed <code>Quaternion</code> object. The values are copied producing a
     * new object.
     *
     * @param q The Quaternion to copy values from.
     */
    public void set(Quaternion q)
    {
        myX = q.myX;
        myY = q.myY;
        myZ = q.myZ;
        myW = q.myW;
    }

    /**
     * Set the w.
     *
     * @param w the w to set
     */
    public void setW(double w)
    {
        myW = w;
    }

    /**
     * Set the x.
     *
     * @param x the x to set
     */
    public void setX(double x)
    {
        myX = x;
    }

    /**
     * Set the y.
     *
     * @param y the y to set
     */
    public void setY(double y)
    {
        myY = y;
    }

    /**
     * Set the z.
     *
     * @param z the z to set
     */
    public void setZ(double z)
    {
        myZ = z;
    }

    /**
     * Sets the values of this quaternion to the slerp from itself to q2 by
     * changeAmnt.
     *
     * @param q2 Final interpolation value
     * @param changeAmnt The amount diffrence
     */
    public void slerp(Quaternion q2, double changeAmnt)
    {
        if (MathUtil.isZero(myX - q2.myX) && MathUtil.isZero(myY - q2.myY) && MathUtil.isZero(myZ - q2.myZ)
                && MathUtil.isZero(myW - q2.myW))
        {
            return;
        }

        double result = myX * q2.myX + myY * q2.myY + myZ * q2.myZ + myW * q2.myW;

        if (result < 0.0)
        {
            // Negate the second quaternion and the result of the dot product
            q2.multLocal(-1.);
            result = -result;
        }

        // Set the first and second scale for the interpolation
        double scale0 = 1. - changeAmnt;
        double scale1 = changeAmnt;

        if (1 - result > DOT_PRODUCT_EPS)
        {
            // Get the angle between the 2 quaternions, and then store the sin()
            // of that angle
            double theta = Math.acos(result);
            double invSinTheta = 1. / Math.sin(theta);

            // Calculate the scale for q1 and q2, according to the angle and
            // it's sine value
            scale0 = Math.sin((1. - changeAmnt) * theta) * invSinTheta;
            scale1 = Math.sin(changeAmnt * theta) * invSinTheta;
        }

        // Calculate the x, y, z and w values for the quaternion by using a
        // special form of linear interpolation for quaternions.
        myX = scale0 * myX + scale1 * q2.myX;
        myY = scale0 * myY + scale1 * q2.myY;
        myZ = scale0 * myZ + scale1 * q2.myZ;
        myW = scale0 * myW + scale1 * q2.myW;
    }

    /**
     * sets this quaternion's value as an interpolation between two other
     * quaternions.
     *
     * @param q1 the first quaternion.
     * @param q2 the second quaternion.
     * @param t the amount to interpolate between the two quaternions.
     */
    public void slerp(Quaternion q1, Quaternion q2, double t)
    {
        // Create a local quaternion to store the interpolated quaternion
        if (MathUtil.isZero(myX - q2.myX) && MathUtil.isZero(myY - q2.myY) && MathUtil.isZero(myZ - q2.myZ)
                && MathUtil.isZero(myW - q2.myW))
        {
            set(q1);
            return;
        }

        double result = q1.myX * q2.myX + q1.myY * q2.myY + q1.myZ * q2.myZ + q1.myW * q2.myW;

        if (result < 0.0)
        {
            // Negate the second quaternion and the result of the dot product
            q2.multLocal(-1.);
            result = -result;
        }

        // Set the first and second scale for the interpolation
        double scale0 = 1. - t;
        double scale1 = t;

        // Check if the angle between the 2 quaternions was big enough to
        // warrant such calculations
        if (1 - result > DOT_PRODUCT_EPS)
        {
            // Get the angle between the 2 quaternions, and then store the sin()
            // of that angle
            double theta = Math.acos(result);
            double invSinTheta = 1. / Math.sin(theta);

            // Calculate the scale for q1 and q2, according to the angle and
            // it's sine value
            scale0 = Math.sin((1. - t) * theta) * invSinTheta;
            scale1 = Math.sin(t * theta) * invSinTheta;
        }

        // Calculate the x, y, z and w values for the quaternion by using a
        // special form of linear interpolation for quaternions.
        myX = scale0 * q1.myX + scale1 * q2.myX;
        myY = scale0 * q1.myY + scale1 * q2.myY;
        myZ = scale0 * q1.myZ + scale1 * q2.myZ;
        myW = scale0 * q1.myW + scale1 * q2.myW;
    }

    /**
     * Subtracts the values of the parameter quaternion from those of this
     * quaternion. The result is returned as a new quaternion.
     *
     * @param q the quaternion to subtract from this.
     * @return the new quaternion.
     */
    public Quaternion subtract(Quaternion q)
    {
        return new Quaternion(myX - q.myX, myY - q.myY, myZ - q.myZ, myW - q.myW);
    }

    /**
     * Subtracts the values of the parameter quaternion from those of this
     * quaternion. The result is stored in this Quaternion.
     *
     * @param q the quaternion to subtract from this.
     */
    public void subtractLocal(Quaternion q)
    {
        myX -= q.myX;
        myY -= q.myY;
        myZ -= q.myZ;
        myW -= q.myW;
    }

    /**
     * Returns this quaternion converted to Euler rotation angles
     * (yaw,roll,pitch).<br>
     * See http://www.euclideanspace.com/maths/geometry/rotations/conversions/
     * quaternionToEuler/index.htm
     *
     * @return the double[] in which the angles are stored.
     */
    public double[] toAngles()
    {
        double[] result = new double[3];

        double sqw = myW * myW;
        double sqx = myX * myX;
        double sqy = myY * myY;
        double sqz = myZ * myZ;
        // if normalized is one, otherwise is correction factor
        double unit = sqx + sqy + sqz + sqw;
        double test = myX * myY + myZ * myW;
        if (test > HALF_MINUS_EPS * unit)
        {
            // singularity at north pole
            result[1] = 2 * Math.atan2(myX, myW);
            result[2] = MathUtil.HALF_PI;
            result[0] = 0;
        }
        else if (test < -HALF_MINUS_EPS * unit)
        {
            // singularity at south pole
            result[1] = -2 * Math.atan2(myX, myW);
            result[2] = -MathUtil.HALF_PI;
            result[0] = 0;
        }
        else
        {
            // roll or heading
            result[1] = Math.atan2(2 * myY * myW - 2 * myX * myZ, sqx - sqy - sqz + sqw);
            // pitch or attitude
            result[2] = Math.asin(2 * test / unit);
            // yaw or bank
            result[0] = Math.atan2(2 * myX * myW - 2 * myY * myZ, -sqx + sqy - sqz + sqw);
        }
        return result;
    }

    /**
     * Takes in an array of three vectors. Each vector corresponds to an axis of
     * the coordinate system defined by the quaternion rotation.
     *
     * @param axis the array of vectors to be filled.
     */
    public void toAxes(Vector3d[] axis)
    {
        Matrix3d tempMat = new Matrix3d(this);
        axis[0] = tempMat.getColumn(0);
        axis[1] = tempMat.getColumn(1);
        axis[2] = tempMat.getColumn(2);
    }

    @Override
    public String toString()
    {
        return Quaternion.class.getName() + ": [x=" + myX + " y=" + myY + " z=" + myZ + " w=" + myW + "]";
    }
}
