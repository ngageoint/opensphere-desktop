package io.opensphere.core.math;

import java.io.Serializable;
import java.nio.DoubleBuffer;

import io.opensphere.core.util.lang.ExpectedCloneableException;

/**
 * Abstract base class for matrices.
 */
public abstract class AbstractMatrix implements Serializable, Cloneable
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    @Override
    public AbstractMatrix clone()
    {
        try
        {
            return (AbstractMatrix)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }

    /**
     * Calculates the determinate of this matrix.
     *
     * @return the determinate
     */
    public abstract double determinant();

    /**
     * fills a DoubleBuffer object with the matrix data.
     *
     * @param fb the buffer to fill, starting at current position.
     */
    public void fillDoubleBuffer(DoubleBuffer fb)
    {
        fillDoubleBuffer(fb, false);
    }

    /**
     * Fills a DoubleBuffer object with the matrix data.
     *
     * @param fb the buffer to fill, starting at current position.
     * @param columnMajor if true, this buffer should be filled with column
     *            major data, otherwise it will be filled row major.
     */
    public abstract void fillDoubleBuffer(DoubleBuffer fb, boolean columnMajor);

    /**
     * Sets this matrix4d to the values specified by an angle and an axis of
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
     * Sets this matrix to the values specified by an angle and a normalized
     * axis of rotation.
     *
     * @param angle the angle to rotate (in radians).
     * @param axis the axis of rotation (already normalized).
     */
    public abstract void fromAngleNormalAxis(double angle, Vector3d axis);

    /**
     * Reads value for this matrix from a DoubleBuffer.
     *
     * @param fb the buffer to read from, must be correct size
     */
    public void readDoubleBuffer(DoubleBuffer fb)
    {
        readDoubleBuffer(fb, false);
    }

    /**
     * Reads value for this matrix from a DoubleBuffer.
     *
     * @param fb the buffer to read from, must be correct size
     * @param columnMajor if true, this buffer should be filled with column
     *            major data, otherwise it will be filled row major.
     */
    public abstract void readDoubleBuffer(DoubleBuffer fb, boolean columnMajor);

    /**
     * sets the values of this matrix from an array of values assuming that the
     * data is rowMajor order.
     *
     * @param matrix the matrix to set the value to.
     */
    public final void set(double[] matrix)
    {
        set(matrix, true);
    }

    /**
     * sets the values of this matrix from an array of values.
     *
     * @param matrix the matrix to set the value to.
     * @param rowMajor whether the incoming data is in row or column major
     *            order.
     */
    public abstract void set(double[] matrix, boolean rowMajor);
}
