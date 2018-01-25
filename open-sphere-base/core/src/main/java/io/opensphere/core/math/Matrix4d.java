package io.opensphere.core.math;

import java.nio.DoubleBuffer;

import io.opensphere.core.util.MathUtil;

/**
 * Defines and maintains a 4x4 matrix in row major order. This matrix is
 * intended for use in a translation and rotational capacity. It provides
 * convenience methods for creating the matrix from a multitude of sources.
 *
 * Matrices are stored assuming column vectors on the right, with the
 * translation in the rightmost column. Element numbering is row,column, so m03
 * is the zeroth row, third column, which is the "x" translation part. This
 * means that the implicit storage order is column major. However, the get() and
 * set() functions on double arrays default to row major order!
 */
public class Matrix4d extends AbstractMatrix
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /** Exception message for a column index out of bounds. */
    private static final String COLUMN_INDEX_MSG = "Column index must be between 0 and 3.";

    /** Exception message for a row index out of bounds. */
    private static final String ROW_INDEX_MSG = "Row index must be between 0 and 3.";

    /** matrix position (0, 0). */
    private double my00;

    /** matrix position (0, 1). */
    private double my01;

    /** matrix position (0, 2). */
    private double my02;

    /** matrix position (0, 3). */
    private double my03;

    /** matrix position (1, 0). */
    private double my10;

    /** matrix position (1, 1). */
    private double my11;

    /** matrix position (1, 2). */
    private double my12;

    /** matrix position (1, 3). */
    private double my13;

    /** matrix position (2, 0). */
    private double my20;

    /** matrix position (2, 1). */
    private double my21;

    /** matrix position (2, 2). */
    private double my22;

    /** matrix position (2, 3). */
    private double my23;

    /** matrix position (3, 0). */
    private double my30;

    /** matrix position (3, 1). */
    private double my31;

    /** matrix position (3, 2). */
    private double my32;

    /** matrix position (3, 3). */
    private double my33;

    /**
     * Constructor instantiates a new <code>Matrix</code> that is set to the
     * identity matrix.
     */
    public Matrix4d()
    {
        setIdentity();
    }

    /**
     * Create a new Matrix4d, given data in column-major format.
     *
     * @param array An array of 16 doubles in column-major format (translation
     *            in elements 12, 13 and 14).
     */
    public Matrix4d(double[] array)
    {
        set(array, false);
    }

    /**
     * Create a new Matrix4d, given data in column-major format.
     *
     * @param array An array of 16 doubles in column-major format (translation
     *            in elements 12, 13 and 14).
     */
    public Matrix4d(float[] array)
    {
        set(array, false);
    }

    /**
     * Constructor instantiates a new <code>Matrix</code> that is set to the
     * provided matrix. This constructor copies a given Matrix. If the provided
     * matrix is null, the constructor sets the matrix to the identity.
     *
     * @param mat the matrix to copy.
     */
    public Matrix4d(Matrix4d mat)
    {
        set(mat);
    }

    /**
     * Sum the positions of the matrices into a new matrix.
     *
     * @param mat matrix to add to me.
     * @return the resultant new matrix.
     */
    public Matrix4d add(Matrix4d mat)
    {
        Matrix4d result = new Matrix4d();
        result.my00 = my00 + mat.my00;
        result.my01 = my01 + mat.my01;
        result.my02 = my02 + mat.my02;
        result.my03 = my03 + mat.my03;
        result.my10 = my10 + mat.my10;
        result.my11 = my11 + mat.my11;
        result.my12 = my12 + mat.my12;
        result.my13 = my13 + mat.my13;
        result.my20 = my20 + mat.my20;
        result.my21 = my21 + mat.my21;
        result.my22 = my22 + mat.my22;
        result.my23 = my23 + mat.my23;
        result.my30 = my30 + mat.my30;
        result.my31 = my31 + mat.my31;
        result.my32 = my32 + mat.my32;
        result.my33 = my33 + mat.my33;
        return result;
    }

    /**
     * Adds the values of a parameter matrix to this matrix.
     *
     * @param mat the matrix to add to this.
     */
    public void addLocal(Matrix4d mat)
    {
        my00 += mat.my00;
        my01 += mat.my01;
        my02 += mat.my02;
        my03 += mat.my03;
        my10 += mat.my10;
        my11 += mat.my11;
        my12 += mat.my12;
        my13 += mat.my13;
        my20 += mat.my20;
        my21 += mat.my21;
        my22 += mat.my22;
        my23 += mat.my23;
        my30 += mat.my30;
        my31 += mat.my31;
        my32 += mat.my32;
        my33 += mat.my33;
    }

    /**
     * Places the adjoint of this matrix in store (creates store if null).
     *
     * @return the adjoint matrix.
     */
    public Matrix4d adjoint()
    {
        Matrix4d mat = new Matrix4d();

        double fA0 = my00 * my11 - my01 * my10;
        double fA1 = my00 * my12 - my02 * my10;
        double fA2 = my00 * my13 - my03 * my10;
        double fA3 = my01 * my12 - my02 * my11;
        double fA4 = my01 * my13 - my03 * my11;
        double fA5 = my02 * my13 - my03 * my12;
        double fB0 = my20 * my31 - my21 * my30;
        double fB1 = my20 * my32 - my22 * my30;
        double fB2 = my20 * my33 - my23 * my30;
        double fB3 = my21 * my32 - my22 * my31;
        double fB4 = my21 * my33 - my23 * my31;
        double fB5 = my22 * my33 - my23 * my32;

        mat.my00 = +my11 * fB5 - my12 * fB4 + my13 * fB3;
        mat.my10 = -my10 * fB5 + my12 * fB2 - my13 * fB1;
        mat.my20 = +my10 * fB4 - my11 * fB2 + my13 * fB0;
        mat.my30 = -my10 * fB3 + my11 * fB1 - my12 * fB0;
        mat.my01 = -my01 * fB5 + my02 * fB4 - my03 * fB3;
        mat.my11 = +my00 * fB5 - my02 * fB2 + my03 * fB1;
        mat.my21 = -my00 * fB4 + my01 * fB2 - my03 * fB0;
        mat.my31 = +my00 * fB3 - my01 * fB1 + my02 * fB0;
        mat.my02 = +my31 * fA5 - my32 * fA4 + my33 * fA3;
        mat.my12 = -my30 * fA5 + my32 * fA2 - my33 * fA1;
        mat.my22 = +my30 * fA4 - my31 * fA2 + my33 * fA0;
        mat.my32 = -my30 * fA3 + my31 * fA1 - my32 * fA0;
        mat.my03 = -my21 * fA5 + my22 * fA4 - my23 * fA3;
        mat.my13 = +my20 * fA5 - my22 * fA2 + my23 * fA1;
        mat.my23 = -my20 * fA4 + my21 * fA2 - my23 * fA0;
        mat.my33 = +my20 * fA3 - my21 * fA1 + my22 * fA0;

        return mat;
    }

    /**
     * Sets this matrix to that of a rotation about three axes (x, y, z). Where
     * each axis has a specified rotation in degrees. These rotations are
     * expressed in a single <code>Vector3d</code> object.
     *
     * @param angles the angles to rotate.
     */
    public void angleRotation(Vector3d angles)
    {
        double angle;
        double sr;
        double sp;
        double sy;
        double cr;
        double cp;
        double cy;

        angle = angles.getZ() * MathUtil.DEG_TO_RAD;
        sy = Math.sin(angle);
        cy = Math.cos(angle);
        angle = angles.getY() * MathUtil.DEG_TO_RAD;
        sp = Math.sin(angle);
        cp = Math.cos(angle);
        angle = angles.getX() * MathUtil.DEG_TO_RAD;
        sr = Math.sin(angle);
        cr = Math.cos(angle);

        // matrix = (Z * Y) * X
        my00 = cp * cy;
        my10 = cp * sy;
        my20 = -sp;
        my01 = sr * sp * cy + cr * -sy;
        my11 = sr * sp * sy + cr * cy;
        my21 = sr * cp;
        my02 = cr * sp * cy + -sr * -sy;
        my12 = cr * sp * sy + -sr * cy;
        my22 = cr * cp;
        my03 = 0.0;
        my13 = 0.0;
        my23 = 0.0;
    }

    /**
     * Convert me to a 3 dimensional matrix.
     *
     * @return new 3d matrix.
     */
    public Matrix3d asMatrix3d()
    {
        return new Matrix3d(my00, my01, my02, my10, my11, my12, my20, my21, my22);
    }

    @Override
    public Matrix4d clone()
    {
        return (Matrix4d)super.clone();
    }

    /**
     * Generates the determinate of this matrix.
     *
     * @return the determinate
     */
    @Override
    public double determinant()
    {
        double fA0 = my00 * my11 - my01 * my10;
        double fA1 = my00 * my12 - my02 * my10;
        double fA2 = my00 * my13 - my03 * my10;
        double fA3 = my01 * my12 - my02 * my11;
        double fA4 = my01 * my13 - my03 * my11;
        double fA5 = my02 * my13 - my03 * my12;
        double fB0 = my20 * my31 - my21 * my30;
        double fB1 = my20 * my32 - my22 * my30;
        double fB2 = my20 * my33 - my23 * my30;
        double fB3 = my21 * my32 - my22 * my31;
        double fB4 = my21 * my33 - my23 * my31;
        double fB5 = my22 * my33 - my23 * my32;
        return fA0 * fB5 - fA1 * fB4 + fA2 * fB3 + fA3 * fB2 - fA4 * fB1 + fA5 * fB0;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Matrix4d))
        {
            return false;
        }

        if (this == o)
        {
            return true;
        }

        Matrix4d comp = (Matrix4d)o;
        return MathUtil.equals(toArray(), comp.toArray());
    }

    @Override
    public void fillDoubleBuffer(DoubleBuffer fb, boolean columnMajor)
    {
        if (columnMajor)
        {
            fb.put(my00).put(my10).put(my20).put(my30);
            fb.put(my01).put(my11).put(my21).put(my31);
            fb.put(my02).put(my12).put(my22).put(my32);
            fb.put(my03).put(my13).put(my23).put(my33);
        }
        else
        {
            fb.put(my00).put(my01).put(my02).put(my03);
            fb.put(my10).put(my11).put(my12).put(my13);
            fb.put(my20).put(my21).put(my22).put(my23);
            fb.put(my30).put(my31).put(my32).put(my33);
        }
    }

    @Override
    public void fromAngleNormalAxis(double angle, Vector3d axis)
    {
        Matrix3d mat3 = new Matrix3d();
        mat3.fromAngleNormalAxis(angle, axis);
        set(mat3);
    }

    /**
     * Retrieves a value from the matrix at the given position.
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
                    case 2:
                        return my02;
                    case 3:
                        return my03;
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
                    case 2:
                        return my12;
                    case 3:
                        return my13;
                    default:
                        throw new IllegalArgumentException(COLUMN_INDEX_MSG);
                }
            case 2:
                switch (j)
                {
                    case 0:
                        return my20;
                    case 1:
                        return my21;
                    case 2:
                        return my22;
                    case 3:
                        return my23;
                    default:
                        throw new IllegalArgumentException(COLUMN_INDEX_MSG);
                }
            case 3:
                switch (j)
                {
                    case 0:
                        return my30;
                    case 1:
                        return my31;
                    case 2:
                        return my32;
                    case 3:
                        return my33;
                    default:
                        throw new IllegalArgumentException(COLUMN_INDEX_MSG);
                }
            default:
                throw new IllegalArgumentException(ROW_INDEX_MSG);
        }
    }

    /**
     * Returns one of three columns specified by the parameter. This column is
     * returned as a double array of length 4.
     *
     * @param i the column to retrieve. Must be between 0 and 3.
     * @return the column specified by the index.
     */
    public double[] getColumn(int i)
    {
        double[] result = new double[4];
        switch (i)
        {
            case 0:
                result[0] = my00;
                result[1] = my10;
                result[2] = my20;
                result[3] = my30;
                break;
            case 1:
                result[0] = my01;
                result[1] = my11;
                result[2] = my21;
                result[3] = my31;
                break;
            case 2:
                result[0] = my02;
                result[1] = my12;
                result[2] = my22;
                result[3] = my32;
                break;
            case 3:
                result[0] = my03;
                result[1] = my13;
                result[2] = my23;
                result[3] = my33;
                break;
            default:
                throw new IllegalArgumentException(COLUMN_INDEX_MSG);
        }
        return result;
    }

    @Override
    public int hashCode()
    {
        long hash = 37;
        hash = 37 * hash + Double.doubleToLongBits(my00);
        hash = 37 * hash + Double.doubleToLongBits(my01);
        hash = 37 * hash + Double.doubleToLongBits(my02);
        hash = 37 * hash + Double.doubleToLongBits(my03);

        hash = 37 * hash + Double.doubleToLongBits(my10);
        hash = 37 * hash + Double.doubleToLongBits(my11);
        hash = 37 * hash + Double.doubleToLongBits(my12);
        hash = 37 * hash + Double.doubleToLongBits(my13);

        hash = 37 * hash + Double.doubleToLongBits(my20);
        hash = 37 * hash + Double.doubleToLongBits(my21);
        hash = 37 * hash + Double.doubleToLongBits(my22);
        hash = 37 * hash + Double.doubleToLongBits(my23);

        hash = 37 * hash + Double.doubleToLongBits(my30);
        hash = 37 * hash + Double.doubleToLongBits(my31);
        hash = 37 * hash + Double.doubleToLongBits(my32);
        hash = 37 * hash + Double.doubleToLongBits(my33);

        return (int)hash;
    }

    /**
     * Translates a given Vector3d by the translation part of this matrix.
     *
     * @param vec the Vector3d data to be translated.
     */
    public void inverseTranslateVect(double[] vec)
    {
        vec[0] = vec[0] - my03;
        vec[1] = vec[1] - my13;
        vec[2] = vec[2] - my23;
    }

    /**
     * Inverts this matrix and stores it in the given store.
     *
     * @return The store
     */
    public Matrix4d invert()
    {
        Matrix4d mat = new Matrix4d();

        double fA0 = my00 * my11 - my01 * my10;
        double fA1 = my00 * my12 - my02 * my10;
        double fA2 = my00 * my13 - my03 * my10;
        double fA3 = my01 * my12 - my02 * my11;
        double fA4 = my01 * my13 - my03 * my11;
        double fA5 = my02 * my13 - my03 * my12;
        double fB0 = my20 * my31 - my21 * my30;
        double fB1 = my20 * my32 - my22 * my30;
        double fB2 = my20 * my33 - my23 * my30;
        double fB3 = my21 * my32 - my22 * my31;
        double fB4 = my21 * my33 - my23 * my31;
        double fB5 = my22 * my33 - my23 * my32;
        double fDet = fA0 * fB5 - fA1 * fB4 + fA2 * fB3 + fA3 * fB2 - fA4 * fB1 + fA5 * fB0;

        if (MathUtil.isZero(fDet))
        {
            mat.zero();
            return mat;
        }

        mat.my00 = +my11 * fB5 - my12 * fB4 + my13 * fB3;
        mat.my10 = -my10 * fB5 + my12 * fB2 - my13 * fB1;
        mat.my20 = +my10 * fB4 - my11 * fB2 + my13 * fB0;
        mat.my30 = -my10 * fB3 + my11 * fB1 - my12 * fB0;
        mat.my01 = -my01 * fB5 + my02 * fB4 - my03 * fB3;
        mat.my11 = +my00 * fB5 - my02 * fB2 + my03 * fB1;
        mat.my21 = -my00 * fB4 + my01 * fB2 - my03 * fB0;
        mat.my31 = +my00 * fB3 - my01 * fB1 + my02 * fB0;
        mat.my02 = +my31 * fA5 - my32 * fA4 + my33 * fA3;
        mat.my12 = -my30 * fA5 + my32 * fA2 - my33 * fA1;
        mat.my22 = +my30 * fA4 - my31 * fA2 + my33 * fA0;
        mat.my32 = -my30 * fA3 + my31 * fA1 - my32 * fA0;
        mat.my03 = -my21 * fA5 + my22 * fA4 - my23 * fA3;
        mat.my13 = +my20 * fA5 - my22 * fA2 + my23 * fA1;
        mat.my23 = -my20 * fA4 + my21 * fA2 - my23 * fA0;
        mat.my33 = +my20 * fA3 - my21 * fA1 + my22 * fA0;

        double fInvDet = 1.0 / fDet;
        mat.multLocal(fInvDet);

        return mat;
    }

    /**
     * Inverts this matrix locally.
     */
    public void invertLocal()
    {
        set(invert());
    }

    /**
     * Check whether I am the identity matrix.
     *
     * @return true if this matrix is identity
     */
    public boolean isIdentity()
    {
        Matrix4d ident = new Matrix4d();
        return equals(ident);
    }

    /**
     * Create a new matrix which is this matrix multiplied by the given value.
     *
     * @param scalar value to multiply by.
     * @return newly created matrix.
     */
    public Matrix4d mult(double scalar)
    {
        Matrix4d out = new Matrix4d();
        out.set(this);
        out.multLocal(scalar);
        return out;
    }

    /**
     * multiplies this matrix with another matrix. The result matrix will then
     * be returned. This matrix will be on the left hand side, while the
     * parameter matrix will be on the right.
     *
     * @param in2 the matrix to multiply this matrix by.
     * @return the resultant matrix
     */
    public Matrix4d mult(Matrix4d in2)
    {
        Matrix4d retMat = new Matrix4d();

        retMat.my00 = my00 * in2.my00 + my01 * in2.my10 + my02 * in2.my20 + my03 * in2.my30;
        retMat.my01 = my00 * in2.my01 + my01 * in2.my11 + my02 * in2.my21 + my03 * in2.my31;
        retMat.my02 = my00 * in2.my02 + my01 * in2.my12 + my02 * in2.my22 + my03 * in2.my32;
        retMat.my03 = my00 * in2.my03 + my01 * in2.my13 + my02 * in2.my23 + my03 * in2.my33;

        retMat.my10 = my10 * in2.my00 + my11 * in2.my10 + my12 * in2.my20 + my13 * in2.my30;
        retMat.my11 = my10 * in2.my01 + my11 * in2.my11 + my12 * in2.my21 + my13 * in2.my31;
        retMat.my12 = my10 * in2.my02 + my11 * in2.my12 + my12 * in2.my22 + my13 * in2.my32;
        retMat.my13 = my10 * in2.my03 + my11 * in2.my13 + my12 * in2.my23 + my13 * in2.my33;

        retMat.my20 = my20 * in2.my00 + my21 * in2.my10 + my22 * in2.my20 + my23 * in2.my30;
        retMat.my21 = my20 * in2.my01 + my21 * in2.my11 + my22 * in2.my21 + my23 * in2.my31;
        retMat.my22 = my20 * in2.my02 + my21 * in2.my12 + my22 * in2.my22 + my23 * in2.my32;
        retMat.my23 = my20 * in2.my03 + my21 * in2.my13 + my22 * in2.my23 + my23 * in2.my33;

        retMat.my30 = my30 * in2.my00 + my31 * in2.my10 + my32 * in2.my20 + my33 * in2.my30;
        retMat.my31 = my30 * in2.my01 + my31 * in2.my11 + my32 * in2.my21 + my33 * in2.my31;
        retMat.my32 = my30 * in2.my02 + my31 * in2.my12 + my32 * in2.my22 + my33 * in2.my32;
        retMat.my33 = my30 * in2.my03 + my31 * in2.my13 + my32 * in2.my23 + my33 * in2.my33;

        return retMat;
    }

    /**
     * multiplies a quaternion about a matrix. The resulting vector is returned.
     *
     * @param vec vec to multiply against.
     * @return this * vec
     */
    public Quaternion mult(Quaternion vec)
    {
        Quaternion quat = new Quaternion();

        quat.setX(my00 * vec.getX() + my01 * vec.getY() + my02 * vec.getZ() + my03 * vec.getW());
        quat.setY(my10 * vec.getX() + my11 * vec.getY() + my12 * vec.getZ() + my13 * vec.getW());
        quat.setZ(my20 * vec.getX() + my21 * vec.getY() + my22 * vec.getZ() + my23 * vec.getW());
        quat.setW(my30 * vec.getX() + my31 * vec.getY() + my32 * vec.getZ() + my33 * vec.getW());

        return quat;
    }

    /**
     * Multiplies a vector about a rotation matrix and adds translation. The
     * resulting vector is returned.
     *
     * @param vec Vector to multiply against.
     * @return The rotated vector.
     */
    public Vector3d mult(Vector3d vec)
    {
        return new Vector3d(my00 * vec.getX() + my01 * vec.getY() + my02 * vec.getZ() + my03,
                my10 * vec.getX() + my11 * vec.getY() + my12 * vec.getZ() + my13,
                my20 * vec.getX() + my21 * vec.getY() + my22 * vec.getZ() + my23);
    }

    /**
     * Multiplies a vector about a rotation matrix. The resulting vector is
     * returned.
     *
     * @param vec Vector to multiply against.
     * @return The rotated vector.
     */
    public Vector3d multAcross(Vector3d vec)
    {
        return new Vector3d(my00 * vec.getX() + my10 * vec.getY() + my20 * vec.getZ() + my30,
                my01 * vec.getX() + my11 * vec.getY() + my21 * vec.getZ() + my31,
                my02 * vec.getX() + my12 * vec.getY() + my22 * vec.getZ() + my32);
    }

    /**
     * multiplies this matrix by a scalar.
     *
     * @param scalar the scalar to multiply this matrix by.
     */
    public void multLocal(double scalar)
    {
        my00 *= scalar;
        my01 *= scalar;
        my02 *= scalar;
        my03 *= scalar;
        my10 *= scalar;
        my11 *= scalar;
        my12 *= scalar;
        my13 *= scalar;
        my20 *= scalar;
        my21 *= scalar;
        my22 *= scalar;
        my23 *= scalar;
        my30 *= scalar;
        my31 *= scalar;
        my32 *= scalar;
        my33 *= scalar;
    }

    /**
     * multiplies this matrix with another matrix. The results are stored
     * internally and a handle to this matrix will then be returned. This matrix
     * will be on the left hand side, while the parameter matrix will be on the
     * right.
     *
     * @param in2 the matrix to multiply this matrix by.
     */
    public void multLocal(Matrix4d in2)
    {
        set(mult(in2));
    }

    @Override
    public void readDoubleBuffer(DoubleBuffer fb, boolean columnMajor)
    {
        if (columnMajor)
        {
            my00 = fb.get();
            my10 = fb.get();
            my20 = fb.get();
            my30 = fb.get();
            my01 = fb.get();
            my11 = fb.get();
            my21 = fb.get();
            my31 = fb.get();
            my02 = fb.get();
            my12 = fb.get();
            my22 = fb.get();
            my32 = fb.get();
            my03 = fb.get();
            my13 = fb.get();
            my23 = fb.get();
            my33 = fb.get();
        }
        else
        {
            my00 = fb.get();
            my01 = fb.get();
            my02 = fb.get();
            my03 = fb.get();
            my10 = fb.get();
            my11 = fb.get();
            my12 = fb.get();
            my13 = fb.get();
            my20 = fb.get();
            my21 = fb.get();
            my22 = fb.get();
            my23 = fb.get();
            my30 = fb.get();
            my31 = fb.get();
            my32 = fb.get();
            my33 = fb.get();
        }
    }

    /**
     * Apply a scale to this matrix.
     *
     * @param scale the scale to apply
     */
    public void scale(Vector3d scale)
    {
        my00 *= scale.getX();
        my10 *= scale.getX();
        my20 *= scale.getX();
        my30 *= scale.getX();
        my01 *= scale.getY();
        my11 *= scale.getY();
        my21 *= scale.getY();
        my31 *= scale.getY();
        my02 *= scale.getZ();
        my12 *= scale.getZ();
        my22 *= scale.getZ();
        my32 *= scale.getZ();
    }

    @Override
    public final void set(double[] matrix, boolean rowMajor)
    {
        if (rowMajor)
        {
            my00 = matrix[0];
            my01 = matrix[1];
            my02 = matrix[2];
            my03 = matrix[3];
            my10 = matrix[4];
            my11 = matrix[5];
            my12 = matrix[6];
            my13 = matrix[7];
            my20 = matrix[8];
            my21 = matrix[9];
            my22 = matrix[10];
            my23 = matrix[11];
            my30 = matrix[12];
            my31 = matrix[13];
            my32 = matrix[14];
            my33 = matrix[15];
        }
        else
        {
            my00 = matrix[0];
            my01 = matrix[4];
            my02 = matrix[8];
            my03 = matrix[12];
            my10 = matrix[1];
            my11 = matrix[5];
            my12 = matrix[9];
            my13 = matrix[13];
            my20 = matrix[2];
            my21 = matrix[6];
            my22 = matrix[10];
            my23 = matrix[14];
            my30 = matrix[3];
            my31 = matrix[7];
            my32 = matrix[11];
            my33 = matrix[15];
        }
    }

    /**
     * Sets the values of this matrix from an array of values.
     *
     * @param matrix the matrix to set the value to.
     */
    public final void set(double[][] matrix)
    {
        my00 = matrix[0][0];
        my01 = matrix[0][1];
        my02 = matrix[0][2];
        my03 = matrix[0][3];
        my10 = matrix[1][0];
        my11 = matrix[1][1];
        my12 = matrix[1][2];
        my13 = matrix[1][3];
        my20 = matrix[2][0];
        my21 = matrix[2][1];
        my22 = matrix[2][2];
        my23 = matrix[2][3];
        my30 = matrix[3][0];
        my31 = matrix[3][1];
        my32 = matrix[3][2];
        my33 = matrix[3][3];
    }

    /**
     * Sets the values of this matrix from an array of values.
     *
     * @param matrix the matrix to set the value to.
     * @param rowMajor whether the incoming data is in row or column major
     *            order.
     */
    public final void set(float[] matrix, boolean rowMajor)
    {
        if (rowMajor)
        {
            my00 = matrix[0];
            my01 = matrix[1];
            my02 = matrix[2];
            my03 = matrix[3];
            my10 = matrix[4];
            my11 = matrix[5];
            my12 = matrix[6];
            my13 = matrix[7];
            my20 = matrix[8];
            my21 = matrix[9];
            my22 = matrix[10];
            my23 = matrix[11];
            my30 = matrix[12];
            my31 = matrix[13];
            my32 = matrix[14];
            my33 = matrix[15];
        }
        else
        {
            my00 = matrix[0];
            my01 = matrix[4];
            my02 = matrix[8];
            my03 = matrix[12];
            my10 = matrix[1];
            my11 = matrix[5];
            my12 = matrix[9];
            my13 = matrix[13];
            my20 = matrix[2];
            my21 = matrix[6];
            my22 = matrix[10];
            my23 = matrix[14];
            my30 = matrix[3];
            my31 = matrix[7];
            my32 = matrix[11];
            my33 = matrix[15];
        }
    }

    /**
     * Places a given value into the matrix at the given position.
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
                    case 2:
                        my02 = value;
                        return;
                    case 3:
                        my03 = value;
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
                    case 2:
                        my12 = value;
                        return;
                    case 3:
                        my13 = value;
                        return;
                    default:
                        throw new IllegalArgumentException(COLUMN_INDEX_MSG);
                }
            case 2:
                switch (j)
                {
                    case 0:
                        my20 = value;
                        return;
                    case 1:
                        my21 = value;
                        return;
                    case 2:
                        my22 = value;
                        return;
                    case 3:
                        my23 = value;
                        return;
                    default:
                        throw new IllegalArgumentException(COLUMN_INDEX_MSG);
                }
            case 3:
                switch (j)
                {
                    case 0:
                        my30 = value;
                        return;
                    case 1:
                        my31 = value;
                        return;
                    case 2:
                        my32 = value;
                        return;
                    case 3:
                        my33 = value;
                        return;
                    default:
                        throw new IllegalArgumentException(COLUMN_INDEX_MSG);
                }
            default:
                throw new IllegalArgumentException(ROW_INDEX_MSG);
        }
    }

    /**
     * Transfers the contents of a given matrix to this matrix. If a null matrix
     * is supplied, this matrix is set to the identity matrix.
     *
     * @param matrix the matrix to copy.
     */
    public final void set(Matrix3d matrix)
    {
        zero();
        my33 = 1.0;

        my00 = matrix.get(0, 0);
        my01 = matrix.get(0, 1);
        my02 = matrix.get(0, 2);
        my10 = matrix.get(1, 0);
        my11 = matrix.get(1, 1);
        my12 = matrix.get(1, 2);
        my20 = matrix.get(2, 0);
        my21 = matrix.get(2, 1);
        my22 = matrix.get(2, 2);
    }

    /**
     * Transfers the contents of a given matrix to this matrix. If a null matrix
     * is supplied, this matrix is set to the identity matrix.
     *
     * @param matrix the matrix to copy.
     */
    public final void set(Matrix4d matrix)
    {
        my00 = matrix.my00;
        my01 = matrix.my01;
        my02 = matrix.my02;
        my03 = matrix.my03;
        my10 = matrix.my10;
        my11 = matrix.my11;
        my12 = matrix.my12;
        my13 = matrix.my13;
        my20 = matrix.my20;
        my21 = matrix.my21;
        my22 = matrix.my22;
        my23 = matrix.my23;
        my30 = matrix.my30;
        my31 = matrix.my31;
        my32 = matrix.my32;
        my33 = matrix.my33;
    }

    /**
     * Sets a particular column of this matrix to that represented by the
     * provided vector.
     *
     * @param i the column to set.
     * @param column the data to set.
     */
    public void setColumn(int i, double[] column)
    {
        switch (i)
        {
            case 0:
                my00 = column[0];
                my10 = column[1];
                my20 = column[2];
                my30 = column[3];
                break;
            case 1:
                my01 = column[0];
                my11 = column[1];
                my21 = column[2];
                my31 = column[3];
                break;
            case 2:
                my02 = column[0];
                my12 = column[1];
                my22 = column[2];
                my32 = column[3];
                break;
            case 3:
                my03 = column[0];
                my13 = column[1];
                my23 = column[2];
                my33 = column[3];
                break;
            default:
                throw new IllegalArgumentException(COLUMN_INDEX_MSG);
        }
    }

    /**
     * Sets this matrix to the identity matrix, namely all zeros with ones along
     * the diagonal.
     */
    public final void setIdentity()
    {
        my01 = my02 = my03 = my10 = my12 = my13 = my20 = my21 = my23 = my30 = my31 = my32 = 0.0;
        my00 = my11 = my22 = my33 = 1.0;
    }

    /**
     * Builds an inverted rotation from Euler angles that are in degrees.
     *
     * @param angles the Euler angles in degrees.
     */
    public void setInverseRotationDegrees(double[] angles)
    {
        double[] vec = new double[3];
        vec[0] = angles[0] * MathUtil.RAD_TO_DEG;
        vec[1] = angles[1] * MathUtil.RAD_TO_DEG;
        vec[2] = angles[2] * MathUtil.RAD_TO_DEG;
        setInverseRotationRadians(vec);
    }

    /**
     * Builds an inverted rotation from Euler angles that are in radians.
     *
     * @param angles the Euler angles in radians.
     */
    public void setInverseRotationRadians(double[] angles)
    {
        double cr = Math.cos(angles[0]);
        double sr = Math.sin(angles[0]);
        double cp = Math.cos(angles[1]);
        double sp = Math.sin(angles[1]);
        double cy = Math.cos(angles[2]);
        double sy = Math.sin(angles[2]);

        my00 = cp * cy;
        my10 = cp * sy;
        my20 = -sp;

        double srsp = sr * sp;
        double crsp = cr * sp;

        my01 = srsp * cy - cr * sy;
        my11 = srsp * sy + cr * cy;
        my21 = sr * cp;

        my02 = crsp * cy + sr * sy;
        my12 = crsp * sy - sr * cy;
        my22 = cr * cp;
    }

    /**
     * Set the matrix's inverse translation values.
     *
     * @param translation the new values for the inverse translation.
     */
    public void setInverseTranslation(double[] translation)
    {
        my03 = -translation[0];
        my13 = -translation[1];
        my23 = -translation[2];
    }

    /**
     * Builds a rotation from a <code>Quaternion</code>.
     *
     * @param quat the quaternion to build the rotation from.
     */
    public void setRotationQuaternion(Quaternion quat)
    {
        double norm = quat.length();
        // we explicitly test norm against one here, saving a division
        // at the cost of a test and branch. Is it worth it?
        double s = MathUtil.isZero(norm - 1.) ? 2. : norm > 0. ? 2. / norm : 0.;

        // compute xs/ys/zs first to save 6 multiplications, since xs/ys/zs
        // will be used 2-4 times each.
        double xs = quat.getX() * s;
        double ys = quat.getY() * s;
        double zs = quat.getZ() * s;
        double xx = quat.getX() * xs;
        double xy = quat.getX() * ys;
        double xz = quat.getX() * zs;
        double xw = quat.getW() * xs;
        double yy = quat.getY() * ys;
        double yz = quat.getY() * zs;
        double yw = quat.getW() * ys;
        double zz = quat.getZ() * zs;
        double zw = quat.getW() * zs;

        // using s=2/norm (instead of 1/norm) saves 9 multiplications by 2 here
        my00 = 1 - (yy + zz);
        my01 = xy - zw;
        my02 = xz + yw;
        my10 = xy + zw;
        my11 = 1 - (xx + zz);
        my12 = yz - xw;
        my20 = xz - yw;
        my21 = yz + xw;
        my22 = 1 - (xx + yy);
    }

    /**
     * Set the matrix's translation values.
     *
     * @param x value of the translation on the x axis
     * @param y value of the translation on the y axis
     * @param z value of the translation on the z axis
     */
    public void setTranslation(double x, double y, double z)
    {
        my03 = x;
        my13 = y;
        my23 = z;
    }

    /**
     * Set the matrix's translation values.
     *
     * @param translation the new values for the translation.
     */
    public void setTranslation(double[] translation)
    {
        my03 = translation[0];
        my13 = translation[1];
        my23 = translation[2];
    }

    /**
     * Set the matrix's translation values.
     *
     * @param translation the new values for the translation.
     */
    public void setTranslation(Vector3d translation)
    {
        my03 = translation.getX();
        my13 = translation.getY();
        my23 = translation.getZ();
    }

    /**
     * Retrieves the values of this object into a double array in column-major
     * order.
     *
     * @return matrix the matrix to set the values into.
     */
    public double[] toArray()
    {
        return toArray(false);
    }

    /**
     * Retrieves the values of this object into a double array.
     *
     * @param rowMajor whether the outgoing data is in row or column major
     *            order.
     * @return matrix the matrix to set the values into.
     */
    public double[] toArray(boolean rowMajor)
    {
        double[] matrix = new double[16];
        if (rowMajor)
        {
            matrix[0] = my00;
            matrix[1] = my01;
            matrix[2] = my02;
            matrix[3] = my03;
            matrix[4] = my10;
            matrix[5] = my11;
            matrix[6] = my12;
            matrix[7] = my13;
            matrix[8] = my20;
            matrix[9] = my21;
            matrix[10] = my22;
            matrix[11] = my23;
            matrix[12] = my30;
            matrix[13] = my31;
            matrix[14] = my32;
            matrix[15] = my33;
        }
        else
        {
            matrix[0] = my00;
            matrix[4] = my01;
            matrix[8] = my02;
            matrix[12] = my03;
            matrix[1] = my10;
            matrix[5] = my11;
            matrix[9] = my12;
            matrix[13] = my13;
            matrix[2] = my20;
            matrix[6] = my21;
            matrix[10] = my22;
            matrix[14] = my23;
            matrix[3] = my30;
            matrix[7] = my31;
            matrix[11] = my32;
            matrix[15] = my33;
        }
        return matrix;
    }

    /**
     * Retrieves the values of this object into a double array in column-major
     * order.
     *
     * @return matrix the matrix to set the values into.
     */
    public float[] toFloatArray()
    {
        return toFloatArray(false);
    }

    /**
     * Retrieves the values of this object into a double array.
     *
     * @param rowMajor whether the outgoing data is in row or column major
     *            order.
     * @return matrix the matrix to set the values into.
     */
    public float[] toFloatArray(boolean rowMajor)
    {
        float[] matrix = new float[16];
        if (rowMajor)
        {
            matrix[0] = (float)my00;
            matrix[1] = (float)my01;
            matrix[2] = (float)my02;
            matrix[3] = (float)my03;
            matrix[4] = (float)my10;
            matrix[5] = (float)my11;
            matrix[6] = (float)my12;
            matrix[7] = (float)my13;
            matrix[8] = (float)my20;
            matrix[9] = (float)my21;
            matrix[10] = (float)my22;
            matrix[11] = (float)my23;
            matrix[12] = (float)my30;
            matrix[13] = (float)my31;
            matrix[14] = (float)my32;
            matrix[15] = (float)my33;
        }
        else
        {
            matrix[0] = (float)my00;
            matrix[4] = (float)my01;
            matrix[8] = (float)my02;
            matrix[12] = (float)my03;
            matrix[1] = (float)my10;
            matrix[5] = (float)my11;
            matrix[9] = (float)my12;
            matrix[13] = (float)my13;
            matrix[2] = (float)my20;
            matrix[6] = (float)my21;
            matrix[10] = (float)my22;
            matrix[14] = (float)my23;
            matrix[3] = (float)my30;
            matrix[7] = (float)my31;
            matrix[11] = (float)my32;
            matrix[15] = (float)my33;
        }
        return matrix;
    }

    @Override
    public String toString()
    {
        StringBuffer result = new StringBuffer(1024);
        result.append("Matrix4d\n[\n ");
        result.append(my00);
        result.append("  ");
        result.append(my01);
        result.append("  ");
        result.append(my02);
        result.append("  ");
        result.append(my03);
        result.append(" \n ");
        result.append(my10);
        result.append("  ");
        result.append(my11);
        result.append("  ");
        result.append(my12);
        result.append("  ");
        result.append(my13);
        result.append(" \n ");
        result.append(my20);
        result.append("  ");
        result.append(my21);
        result.append("  ");
        result.append(my22);
        result.append("  ");
        result.append(my23);
        result.append(" \n ");
        result.append(my30);
        result.append("  ");
        result.append(my31);
        result.append("  ");
        result.append(my32);
        result.append("  ");
        result.append(my33);
        result.append(" \n]");
        return result.toString();
    }

    /**
     * Create a transposed version of this matrix.
     *
     * @return newly created transpose matrix.
     */
    public Matrix4d transpose()
    {
        double[] tmp = toArray(true);
        return new Matrix4d(tmp);
    }

    /**
     * locally transposes this Matrix.
     */
    public void transposeLocal()
    {
        double tmp = my01;
        my01 = my10;
        my10 = tmp;

        tmp = my02;
        my02 = my20;
        my20 = tmp;

        tmp = my03;
        my03 = my30;
        my30 = tmp;

        tmp = my12;
        my12 = my21;
        my21 = tmp;

        tmp = my13;
        my13 = my31;
        my31 = tmp;

        tmp = my23;
        my23 = my32;
        my32 = tmp;
    }

    /**
     * Sets all of the values in this matrix to zero.
     */
    public final void zero()
    {
        my00 = my01 = my02 = my03 = my10 = my11 = my12 = my13 = my20 = my21 = my22 = my23 = my30 = my31 = my32 = my33 = 0.0;
    }
}
