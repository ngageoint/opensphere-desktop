package io.opensphere.core.math;

import java.nio.DoubleBuffer;

import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;

/**
 * Defines a 2x2 matrix.
 */
public class Matrix2d extends AbstractMatrix
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /** How many bytes used internally by one of these objects. */
    public static final int SIZE_BYTES = MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.DOUBLE_SIZE_BYTES * 4,
            Constants.MEMORY_BLOCK_SIZE_BYTES);

    /** Exception message for a column index out of bounds. */
    private static final String COLUMN_INDEX_MSG = "Column index must be between 0 and 1.";

    /** Exception message for a row index out of bounds. */
    private static final String ROW_INDEX_MSG = "Row index must be between 0 and 1.";

    /** matrix position (0, 0). */
    private double my00;

    /** matrix position (0, 1). */
    private double my01;

    /** matrix position (1, 0). */
    private double my10;

    /** matrix position (1, 1). */
    private double my11;

    /**
     * The initial values for the matrix is that of the identity matrix.
     */
    public Matrix2d()
    {
        loadIdentity();
    }

    /**
     * constructs a matrix with the given values.
     *
     * @param m00 0x0 in the matrix.
     * @param m01 0x1 in the matrix.
     * @param m10 1x0 in the matrix.
     * @param m11 1x1 in the matrix.
     */
    public Matrix2d(double m00, double m01, double m10, double m11)
    {
        my00 = m00;
        my01 = m01;
        my10 = m10;
        my11 = m11;
    }

    /**
     * Copy constructor that creates a new <code>Matrix3d</code> object that is
     * the same as the provided matrix.
     *
     * @param mat the matrix to copy.
     */
    public Matrix2d(Matrix2d mat)
    {
        set(mat);
    }

    /**
     * Constructor. TODO this is actually the transpose of what you'd expect.
     *
     * @param xAxis x axis.
     * @param yAxis y axis.
     */
    public Matrix2d(Vector2d xAxis, Vector2d yAxis)
    {
        my00 = xAxis.getX();
        my01 = xAxis.getY();
        my10 = yAxis.getX();
        my11 = yAxis.getY();
    }

    /**
     * adds the values of a parameter matrix to this matrix.
     *
     * @param mat the matrix to add to this.
     */
    public void add(Matrix2d mat)
    {
        my00 += mat.my00;
        my01 += mat.my01;
        my10 += mat.my10;
        my11 += mat.my11;
    }

    /**
     * Places the adjoint of this matrix in a newly created matrix.
     *
     * @return the adjoint
     */
    public Matrix2d adjoint()
    {
        Matrix2d result = new Matrix2d();

        result.my00 = my11;
        result.my01 = -my10;
        result.my10 = -my01;
        result.my11 = my00;

        return result;
    }

    @Override
    public Matrix2d clone()
    {
        return (Matrix2d)super.clone();
    }

    @Override
    public double determinant()
    {
        return my00 * my11 - my01 * my10;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Matrix2d))
        {
            return false;
        }

        if (this == o)
        {
            return true;
        }

        Matrix2d comp = (Matrix2d)o;
        return MathUtil.equals(toArray(), comp.toArray());
    }

    @Override
    public void fillDoubleBuffer(DoubleBuffer fb, boolean columnMajor)
    {
        if (columnMajor)
        {
            fb.put(my00).put(my10);
            fb.put(my01).put(my11);
        }
        else
        {
            fb.put(my00).put(my01);
            fb.put(my10).put(my11);
        }
    }

    @Override
    public void fromAngleNormalAxis(double angle, Vector3d axis)
    {
        throw new UnsupportedOperationException("A 2D matrix cannot be defined by an angle and an axis.");
    }

    /**
     * Recreate Matrix using the provided axis.
     *
     * @param uAxis u axis
     * @param vAxis v axis
     */
    public void fromAxes(Vector2d uAxis, Vector2d vAxis)
    {
        my00 = uAxis.getX();
        my10 = uAxis.getY();

        my01 = vAxis.getX();
        my11 = vAxis.getY();
    }

    /**
     * retrieves a value from the matrix at the given position.
     *
     * @param i the row index.
     * @param j the column index.
     * @return the value at (i, j).
     */
    @SuppressWarnings("PMD.MissingBreakInSwitch")
    public double get(int i, int j)
    {
        switch (i)
        {
            case 0:
                switch (j)
                {
                    case 0:
                        return my00;
                    case 1:
                        return my01;
                    default:
                        throw new IllegalArgumentException(COLUMN_INDEX_MSG);
                }
            case 1:
                switch (j)
                {
                    case 0:
                        return my10;
                    case 1:
                        return my11;
                    default:
                        throw new IllegalArgumentException(COLUMN_INDEX_MSG);
                }
            default:
                throw new IllegalArgumentException(ROW_INDEX_MSG);
        }
    }

    /**
     * returns one of three columns specified by the parameter. This column is
     * returned as a <code>Vector2d</code> object.
     *
     * @param index the column to retrieve. Must be between 0 and 2.
     * @return the column specified by the index.
     */
    public Vector2d getColumn(int index)
    {
        switch (index)
        {
            case 0:
                return new Vector2d(my00, my10);
            case 1:
                return new Vector2d(my01, my11);
            default:
                throw new IllegalArgumentException(COLUMN_INDEX_MSG);
        }
    }

    /**
     * returns one of three rows as specified by the parameter. This row is
     * returned as a <code>Vector2d</code> object.
     *
     * @param index the row to retrieve. Must be between 0 and 2.
     * @return the row specified by the index.
     */
    public Vector2d getRow(int index)
    {
        switch (index)
        {
            case 0:
                return new Vector2d(my00, my01);
            case 1:
                return new Vector2d(my10, my11);
            default:
                throw new IllegalArgumentException(ROW_INDEX_MSG);
        }
    }

    @Override
    public int hashCode()
    {
        long hash = 37;
        hash = 37 * hash + Double.doubleToLongBits(my00);
        hash = 37 * hash + Double.doubleToLongBits(my01);

        hash = 37 * hash + Double.doubleToLongBits(my10);
        hash = 37 * hash + Double.doubleToLongBits(my11);

        return (int)hash;
    }

    /**
     * Generate an inverse matrix.
     *
     * @return inverse matrix
     */
    public Matrix2d invert()
    {
        double det = determinant();
        if (MathUtil.isZero(det))
        {
            return new Matrix2d(0., 0., 0., 0.);
        }

        return adjoint().multLocal(1. / det);
    }

    /**
     * Inverts this matrix locally.
     */
    public void invertLocal()
    {
        set(invert());
    }

    /**
     * Check whether this matrix is identity.
     *
     * @return true if this matrix is identity
     */
    public boolean isIdentity()
    {
        Matrix2d ident = new Matrix2d();
        return equals(ident);
    }

    /**
     * sets this matrix to the identity matrix. Where all values are zero except
     * those along the diagonal which are one.
     */
    public final void loadIdentity()
    {
        my01 = my10 = 0;
        my00 = my11 = 1;
    }

    /**
     * Multiplies this matrix by a given matrix.
     *
     * @param mat the matrix to multiply this matrix by.
     * @return a matrix2d containing the result of this operation.
     */
    public Matrix2d mult(Matrix2d mat)
    {
        Matrix2d result = new Matrix2d();

        double temp00 = my00 * mat.my00 + my01 * mat.my10;
        double temp01 = my00 * mat.my01 + my01 * mat.my11;
        double temp10 = my10 * mat.my00 + my11 * mat.my10;
        double temp11 = my10 * mat.my01 + my11 * mat.my11;

        result.my00 = temp00;
        result.my01 = temp01;
        result.my10 = temp10;
        result.my11 = temp11;

        return result;
    }

    /**
     * Multiplies this 2x2 matrix by the 1x2 Vector.
     *
     * @param vec The Vector2d to multiply.
     * @return The given product vector.
     */
    public Vector2d mult(Vector2d vec)
    {
        return new Vector2d(my00 * vec.getX() + my01 * vec.getY(), my10 * vec.getX() + my11 * vec.getY());
    }

    /**
     * Multiplies this matrix internally by a given double scale factor.
     *
     * @param scale the value to scale by.
     * @return this Matrix2d
     */
    public Matrix2d multLocal(double scale)
    {
        my00 *= scale;
        my01 *= scale;
        my10 *= scale;
        my11 *= scale;
        return this;
    }

    /**
     * multiplies this matrix by a given matrix. The result matrix is saved in
     * the current matrix. If the given matrix is null, nothing happens. The
     * current matrix is returned. This is equivalent to this*=mat
     *
     * @param mat the matrix to multiply this matrix by.
     * @return This matrix, after the multiplication
     */
    public Matrix2d multLocal(Matrix2d mat)
    {
        set(mult(mat));
        return this;
    }

    @Override
    public void readDoubleBuffer(DoubleBuffer fb, boolean columnMajor)
    {
        if (columnMajor)
        {
            my00 = fb.get();
            my10 = fb.get();
            my01 = fb.get();
            my11 = fb.get();
        }
        else
        {
            my00 = fb.get();
            my01 = fb.get();
            my10 = fb.get();
            my11 = fb.get();
        }
    }

    /**
     * scales the operation performed by this matrix on a per-component basis.
     *
     * @param scale The scale applied to each of the X, Y and Z output values.
     */
    public void scale(Vector2d scale)
    {
        my00 *= scale.getX();
        my10 *= scale.getX();
        my01 *= scale.getY();
        my11 *= scale.getY();
    }

    @Override
    public final void set(double[] matrix, boolean rowMajor)
    {
        if (rowMajor)
        {
            my00 = matrix[0];
            my01 = matrix[1];
            my10 = matrix[2];
            my11 = matrix[3];
        }
        else
        {
            my00 = matrix[0];
            my01 = matrix[2];
            my10 = matrix[1];
            my11 = matrix[3];
        }
    }

    /**
     * sets the values of the matrix to those supplied by the 2x2 two dimenion
     * array.
     *
     * @param matrix the new values of the matrix.
     */
    public final void set(double[][] matrix)
    {
        my00 = matrix[0][0];
        my01 = matrix[0][1];
        my10 = matrix[1][0];
        my11 = matrix[1][1];
    }

    /**
     * places a given value into the matrix at the given position.
     *
     * @param i the row index.
     * @param j the column index.
     * @param value the value for (i, j).
     */
    @SuppressWarnings("PMD.MissingBreakInSwitch")
    public final void set(int i, int j, double value)
    {
        switch (i)
        {
            case 0:
                switch (j)
                {
                    case 0:
                        my00 = value;
                        return;
                    case 1:
                        my01 = value;
                        return;
                    default:
                        throw new IllegalArgumentException(COLUMN_INDEX_MSG);
                }
            case 1:
                switch (j)
                {
                    case 0:
                        my10 = value;
                        return;
                    case 1:
                        my11 = value;
                        return;
                    default:
                        throw new IllegalArgumentException(COLUMN_INDEX_MSG);
                }
            default:
                throw new IllegalArgumentException(ROW_INDEX_MSG);
        }
    }

    /**
     * transfers the contents of a given matrix to this matrix. If a null matrix
     * is supplied, this matrix is set to the identity matrix.
     *
     * @param matrix the matrix to copy.
     */
    public final void set(Matrix2d matrix)
    {
        my00 = matrix.my00;
        my01 = matrix.my01;
        my10 = matrix.my10;
        my11 = matrix.my11;
    }

    /**
     * sets a particular column of this matrix to that represented by the
     * provided vector.
     *
     * @param index the column to set.
     * @param column the data to set.
     */
    public void setColumn(int index, Vector2d column)
    {
        switch (index)
        {
            case 0:
                my00 = column.getX();
                my10 = column.getY();
                break;
            case 1:
                my01 = column.getX();
                my11 = column.getY();
                break;
            default:
                throw new IllegalArgumentException(COLUMN_INDEX_MSG);
        }
    }

    /**
     * sets a particular row of this matrix to that represented by the provided
     * vector.
     *
     * @param index the row to set.
     * @param row the data to set.
     */
    public void setRow(int index, Vector2d row)
    {
        switch (index)
        {
            case 0:
                my00 = row.getX();
                my01 = row.getY();
                break;
            case 1:
                my10 = row.getX();
                my11 = row.getY();
                break;
            default:
                throw new IllegalArgumentException(ROW_INDEX_MSG);
        }
    }

    /**
     * Retrieves the values of this object into a double array in row-major
     * order.
     *
     * @return matrix the matrix to set the values into.
     */
    public double[] toArray()
    {
        return toArray(true);
    }

    /**
     * returns the matrix in row-major or column-major order.
     *
     * @param rowMajor True for row major storage in the array.
     * @return array containing the values for this matrix.
     */
    public double[] toArray(boolean rowMajor)
    {
        double[] data = new double[4];
        if (rowMajor)
        {
            data[0] = my00;
            data[1] = my01;
            data[2] = my10;
            data[3] = my11;
        }
        else
        {
            data[0] = my00;
            data[1] = my10;
            data[2] = my01;
            data[3] = my11;
        }
        return data;
    }

    @Override
    public String toString()
    {
        StringBuffer result = new StringBuffer("Matrix2d\n[\n ");
        result.append(my00);
        String spaces = "  ";
        result.append(spaces);
        result.append(my01);
        result.append(" \n ");
        result.append(my10);
        result.append(spaces);
        result.append(my11);
        result.append(" \n]");
        return result.toString();
    }

    /**
     * create a transposed version of this matrix.
     *
     * @return The new Matrix3d object.
     */
    public Matrix2d transpose()
    {
        return new Matrix2d(my00, my10, my01, my11);
    }

    /**
     * Transposes this matrix in place.
     */
    public void transposeLocal()
    {
        double[] tmp = toArray(false);
        set(tmp, true);
    }

    /**
     * Sets all of the values in this matrix to zero.
     */
    public void zero()
    {
        my00 = my01 = my10 = my11 = 0.0;
    }
}
