package io.opensphere.core.math;

import java.nio.DoubleBuffer;
import java.util.List;

import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;

/**
 * Defines a 3x3 matrix. Matrix data is maintained internally and is accessible
 * via the get and set methods. Convenience methods are used for matrix
 * operations as well as generating a matrix from a given set of values.
 */
public class Matrix3d extends AbstractMatrix
{
    /** How many bytes used internally by one of these objects. */
    public static final int SIZE_BYTES = MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.DOUBLE_SIZE_BYTES * 9,
            Constants.MEMORY_BLOCK_SIZE_BYTES);

    /** Exception message for a column index out of bounds. */
    private static final String COLUMN_INDEX_MSG = "Column index must be between 0 and 2.";

    /** Exception message for a row index out of bounds. */
    private static final String ROW_INDEX_MSG = "Row index must be between 0 and 2.";

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /** matrix position (0, 0). */
    private double my00;

    /** matrix position (0, 1). */
    private double my01;

    /** matrix position (0, 2). */
    private double my02;

    /** matrix position (1, 0). */
    private double my10;

    /** matrix position (1, 1). */
    private double my11;

    /** matrix position (1, 2). */
    private double my12;

    /** matrix position (2, 0). */
    private double my20;

    /** matrix position (2, 1). */
    private double my21;

    /** matrix position (2, 2). */
    private double my22;

    /**
     * Get the transformation matrix which maps one quadrilateral onto another
     * quadrilateral. The matrix returned does not do the complete
     * transformation by simple multiplication with the source vector. Instead
     * the {@link Matrix3d#applyPerspectiveTransform(Vector2d)} method should be
     * used. It should also be noted that for non-convex quadrilaterals a matrix
     * will still be returned, but use of the transformation will give undefined
     * results.
     *
     * @param source The quadrilateral which the matrix will map from.
     * @param destination The quadrilateral which the matrix will map to.
     * @return The matrix which contains the perspective transform.
     */
    public static Matrix3d getQuadToQuadTransform(List<Vector2d> source, List<Vector2d> destination)
    {
        Matrix3d trans1 = getQuadToSquareTransform(source.get(0), source.get(1), source.get(2), source.get(3));
        Matrix3d trans2 = getSquareToQuadTransform(destination.get(0), destination.get(1), destination.get(2),
                destination.get(3));

        return trans2.mult(trans1);
    }

    /**
     * Get the transformation matrix which maps the given quadrilateral onto the
     * unit square. The matrix returned does not do the complete transformation
     * by simple multiplication with the source vector. Instead the
     * {@link Matrix3d#applyPerspectiveTransform(Vector2d)} method should be
     * used. It should also be noted that for non-convex quadrilaterals a matrix
     * will still be returned, but use of the transformation will give undefined
     * results.
     *
     * @param ll The lower left corner of the quadrilateral.
     * @param lr The lower right corner of the quadrilateral.
     * @param ur The upper right corner of the quadrilateral.
     * @param ul The upper left corner of the quadrilateral.
     * @return The matrix which contains the perspective transform.
     */
    public static Matrix3d getQuadToSquareTransform(Vector2d ll, Vector2d lr, Vector2d ur, Vector2d ul)
    {
        Matrix3d squareToQuad = getSquareToQuadTransform(ll, lr, ur, ul);
        return squareToQuad.adjoint();
    }

    /**
     * Get the transformation matrix which maps the unit square onto the given
     * quadrilateral. The matrix returned does not do the complete
     * transformation by simple multiplication with the source vector. Instead
     * the {@link Matrix3d#applyPerspectiveTransform(Vector2d)} method should be
     * used. It should also be noted that for non-convex quadrilaterals a matrix
     * will still be returned, but use of the transformation will give undefined
     * results.
     *
     * @param ll The lower left corner of the quadrilateral.
     * @param lr The lower right corner of the quadrilateral.
     * @param ur The upper right corner of the quadrilateral.
     * @param ul The upper left corner of the quadrilateral.
     * @return The matrix which contains the perspective transform.
     */
    public static Matrix3d getSquareToQuadTransform(Vector2d ll, Vector2d lr, Vector2d ur, Vector2d ul)
    {
        double x0 = ll.getX();
        double y0 = ll.getY();
        double x1 = lr.getX();
        double y1 = lr.getY();
        double x2 = ur.getX();
        double y2 = ur.getY();
        double x3 = ul.getX();
        double y3 = ul.getY();

        Matrix3d mat = new Matrix3d();
        mat.my22 = 1.;
        mat.my02 = x0;
        mat.my12 = y0;

        double dx3 = x0 - x1 + x2 - x3;
        double dy3 = y0 - y1 + y2 - y3;

        if (MathUtil.isZero(dx3) || MathUtil.isZero(dy3))
        {
            mat.my00 = x1 - x0;
            mat.my01 = x2 - x1;
            mat.my10 = y1 - y0;
            mat.my11 = y2 - y1;
            mat.my20 = 0.;
            mat.my21 = 0.;
        }
        else
        {
            double dx1 = x1 - x2;
            double dy1 = y1 - y2;
            double dx2 = x3 - x2;
            double dy2 = y3 - y2;
            double invdet = 1. / (dx1 * dy2 - dx2 * dy1);

            mat.my20 = (dx3 * dy2 - dx2 * dy3) * invdet;
            mat.my21 = (dx1 * dy3 - dx3 * dy1) * invdet;
            mat.my00 = x1 - x0 + mat.my20 * x1;
            mat.my01 = x3 - x0 + mat.my21 * x3;
            mat.my10 = y1 - y0 + mat.my20 * y1;
            mat.my11 = y3 - y0 + mat.my21 * y3;
        }

        return mat;
    }

    /**
     * The initial values for the matrix is that of the identity matrix.
     */
    public Matrix3d()
    {
        loadIdentity();
    }

    /**
     * constructs a matrix with the given values.
     *
     * @param m00 0x0 in the matrix.
     * @param m01 0x1 in the matrix.
     * @param m02 0x2 in the matrix.
     * @param m10 1x0 in the matrix.
     * @param m11 1x1 in the matrix.
     * @param m12 1x2 in the matrix.
     * @param m20 2x0 in the matrix.
     * @param m21 2x1 in the matrix.
     * @param m22 2x2 in the matrix.
     */
    public Matrix3d(double m00, double m01, double m02, double m10, double m11, double m12, double m20, double m21, double m22)
    {
        my00 = m00;
        my01 = m01;
        my02 = m02;
        my10 = m10;
        my11 = m11;
        my12 = m12;
        my20 = m20;
        my21 = m21;
        my22 = m22;
    }

    /**
     * Copy constructor that creates a new <code>Matrix3d</code> object that is
     * the same as the provided matrix.
     *
     * @param mat the matrix to copy.
     */
    public Matrix3d(Matrix3d mat)
    {
        set(mat);
    }

    /**
     * Construct me.
     *
     * @param quat quaternion
     */
    public Matrix3d(Quaternion quat)
    {
        set(quat);
    }

    /**
     * Constructor. TODO this is actually the transpose of what you'd expect.
     *
     * @param xAxis x axis.
     * @param yAxis y axis.
     * @param zAxis z axis.
     */
    public Matrix3d(Vector3d xAxis, Vector3d yAxis, Vector3d zAxis)
    {
        my00 = xAxis.getX();
        my01 = xAxis.getY();
        my02 = xAxis.getZ();
        my10 = yAxis.getX();
        my11 = yAxis.getY();
        my12 = yAxis.getZ();
        my20 = zAxis.getX();
        my21 = zAxis.getY();
        my22 = zAxis.getZ();
    }

    /**
     * adds the values of a parameter matrix to this matrix.
     *
     * @param mat the matrix to add to this.
     */
    public void add(Matrix3d mat)
    {
        my00 += mat.my00;
        my01 += mat.my01;
        my02 += mat.my02;
        my10 += mat.my10;
        my11 += mat.my11;
        my12 += mat.my12;
        my20 += mat.my20;
        my21 += mat.my21;
        my22 += mat.my22;
    }

    /**
     * Places the adjoint of this matrix in a newly created matrix.
     *
     * @return the adjoint
     */
    public Matrix3d adjoint()
    {
        Matrix3d result = new Matrix3d();

        result.my00 = my11 * my22 - my12 * my21;
        result.my01 = my02 * my21 - my01 * my22;
        result.my02 = my01 * my12 - my02 * my11;
        result.my10 = my12 * my20 - my10 * my22;
        result.my11 = my00 * my22 - my02 * my20;
        result.my12 = my02 * my10 - my00 * my12;
        result.my20 = my10 * my21 - my11 * my20;
        result.my21 = my01 * my20 - my00 * my21;
        result.my22 = my00 * my11 - my01 * my10;

        return result;
    }

    /**
     * When this matrix is a perspective transform matrix, apply the
     * transformation to the source.
     *
     * @param source The vector which is to be transformed.
     * @return The transformed vector.
     */
    public Vector2d applyPerspectiveTransform(Vector2d source)
    {
        double x = source.getX();
        double y = source.getY();
        double w = my20 * x + my21 * y + my22;
        if (MathUtil.isZero(w))
        {
            return new Vector2d(x, y);
        }
        else
        {
            return new Vector2d((my00 * x + my01 * y + my02) / w, (my10 * x + my11 * y + my12) / w);
        }
    }

    @Override
    public Matrix3d clone()
    {
        return (Matrix3d)super.clone();
    }

    @Override
    public double determinant()
    {
        double fCo00 = my11 * my22 - my12 * my21;
        double fCo10 = my12 * my20 - my10 * my22;
        double fCo20 = my10 * my21 - my11 * my20;
        return my00 * fCo00 + my01 * fCo10 + my02 * fCo20;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Matrix3d))
        {
            return false;
        }

        if (this == o)
        {
            return true;
        }

        Matrix3d comp = (Matrix3d)o;
        return MathUtil.equals(toArray(), comp.toArray());
    }

    @Override
    public void fillDoubleBuffer(DoubleBuffer fb, boolean columnMajor)
    {
        if (columnMajor)
        {
            fb.put(my00).put(my10).put(my20);
            fb.put(my01).put(my11).put(my21);
            fb.put(my02).put(my12).put(my22);
        }
        else
        {
            fb.put(my00).put(my01).put(my02);
            fb.put(my10).put(my11).put(my12);
            fb.put(my20).put(my21).put(my22);
        }
    }

    @Override
    public void fromAngleNormalAxis(double angle, Vector3d axis)
    {
        double fCos = Math.cos(angle);
        double fSin = Math.sin(angle);
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

        my00 = fX2 * fOneMinusCos + fCos;
        my01 = fXYM - fZSin;
        my02 = fXZM + fYSin;
        my10 = fXYM + fZSin;
        my11 = fY2 * fOneMinusCos + fCos;
        my12 = fYZM - fXSin;
        my20 = fXZM - fYSin;
        my21 = fYZM + fXSin;
        my22 = fZ2 * fOneMinusCos + fCos;
    }

    /**
     * Recreate Matrix using the provided axis.
     *
     * @param uAxis u axis
     * @param vAxis v axis
     * @param wAxis w axis
     */
    public void fromAxes(Vector3d uAxis, Vector3d vAxis, Vector3d wAxis)
    {
        my00 = uAxis.getX();
        my10 = uAxis.getY();
        my20 = uAxis.getZ();

        my01 = vAxis.getX();
        my11 = vAxis.getY();
        my21 = vAxis.getZ();

        my02 = wAxis.getX();
        my12 = wAxis.getY();
        my22 = wAxis.getZ();
    }

    /**
     * A function for creating a rotation matrix that rotates a vector called
     * "start" into another vector called "end".
     *
     * See "Tomas Möller, John Hughes \"Efficiently Building a Matrix to Rotate
     * \ One Vector to Another\" Journal of Graphics Tools, 4(4):1-4, 1999"
     *
     * @param start normalized non-zero starting vector
     * @param end normalized non-zero ending vector
     */
    public void fromStartEndVectors(Vector3d start, Vector3d end)
    {
        Vector3d cross = start.cross(end);
        double dot = start.dot(end);

        // if "from" and "to" vectors are nearly parallel
        if (MathUtil.isZero(Math.abs(dot) - 1.0))
        {
            fromStartEndParallel(start, end);
        }
        else
        {
            // the most common case, unless "start"="end", or "start"=-"end"
            double h = 1.0 / (1.0 + dot);
            double hvx = h * cross.getX();
            double hvz = h * cross.getZ();
            double hvxy = hvx * cross.getY();
            double hvxz = hvx * cross.getZ();
            double hvyz = hvz * cross.getY();
            set(0, 0, dot + hvx * cross.getX());
            set(0, 1, hvxy - cross.getZ());
            set(0, 2, hvxz + cross.getY());

            set(1, 0, hvxy + cross.getZ());
            set(1, 1, dot + h * cross.getY() * cross.getY());
            set(1, 2, hvyz - cross.getX());

            set(2, 0, hvxz - cross.getY());
            set(2, 1, hvyz + cross.getX());
            set(2, 2, dot + hvz * cross.getZ());
        }
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
                    case 2:
                        return my02;
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
                    default:
                        throw new IllegalArgumentException(COLUMN_INDEX_MSG);
                }
            default:
                throw new IllegalArgumentException(ROW_INDEX_MSG);
        }
    }

    /**
     * returns one of three columns specified by the parameter. This column is
     * returned as a <code>Vector3d</code> object.
     *
     * @param index the column to retrieve. Must be between 0 and 2.
     * @return the column specified by the index.
     */
    public Vector3d getColumn(int index)
    {
        switch (index)
        {
            case 0:
                return new Vector3d(my00, my10, my20);
            case 1:
                return new Vector3d(my01, my11, my21);
            case 2:
                return new Vector3d(my02, my12, my22);
            default:
                throw new IllegalArgumentException(COLUMN_INDEX_MSG);
        }
    }

    /**
     * generates a quaternion from this matrix. This matrix is assumed to be a
     * rotational matrix.
     *
     * @return the quaternion.
     */
    public Quaternion getQuaternion()
    {
        double quatW = 0.0;
        double quatX = 0.0;
        double quatY = 0.0;
        double quatZ = 0.0;
        // Use the Graphics Gems code, from
        // ftp://ftp.cis.upenn.edu/pub/graphics/shoemake/quatut.ps.getZ()
        // *NOT* the "Matrix and Quaternions FAQ", which has errors!

        // the trace is the sum of the diagonal elements; see
        // http://mathworld.wolfram.com/MatrixTrace.html
        double t = my00 + my11 + my22;

        // we protect the division by s by ensuring that s>=1
        if (t >= 0)
        {
            // |w| >= .5
            // |s|>=1 ...
            double s = Math.sqrt(t + 1);
            quatW = 0.5 * s;
            // so this division isn't bad
            s = 0.5 / s;
            quatX = (my21 - my12) * s;
            quatY = (my02 - my20) * s;
            quatZ = (my10 - my01) * s;
        }
        else if (my00 > my11 && my00 > my22)
        {
            // |s|>=1
            double s = Math.sqrt(1.0 + my00 - my11 - my22);
            // |x| >= .5
            quatX = s * 0.5;
            s = 0.5 / s;
            quatY = (my10 + my01) * s;
            quatZ = (my02 + my20) * s;
            quatW = (my21 - my12) * s;
        }
        else if (my11 > my22)
        {
            // |s|>=1
            double s = Math.sqrt(1.0 + my11 - my00 - my22);
            // |y| >= .5
            quatY = s * 0.5;
            s = 0.5 / s;
            quatX = (my10 + my01) * s;
            quatZ = (my21 + my12) * s;
            quatW = (my02 - my20) * s;
        }
        else
        {
            // |s|>=1
            double s = Math.sqrt(1.0 + my22 - my00 - my11);
            // |z| >= .5
            quatZ = s * 0.5;
            s = 0.5 / s;
            quatX = (my02 + my20) * s;
            quatY = (my21 + my12) * s;
            quatW = (my10 - my01) * s;
        }

        return new Quaternion(quatX, quatY, quatZ, quatW);
    }

    /**
     * returns one of three rows as specified by the parameter. This row is
     * returned as a <code>Vector3d</code> object.
     *
     * @param index the row to retrieve. Must be between 0 and 2.
     * @return the row specified by the index.
     */
    public Vector3d getRow(int index)
    {
        switch (index)
        {
            case 0:
                return new Vector3d(my00, my01, my02);
            case 1:
                return new Vector3d(my10, my11, my12);
            case 2:
                return new Vector3d(my20, my21, my22);
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
        hash = 37 * hash + Double.doubleToLongBits(my02);

        hash = 37 * hash + Double.doubleToLongBits(my10);
        hash = 37 * hash + Double.doubleToLongBits(my11);
        hash = 37 * hash + Double.doubleToLongBits(my12);

        hash = 37 * hash + Double.doubleToLongBits(my20);
        hash = 37 * hash + Double.doubleToLongBits(my21);
        hash = 37 * hash + Double.doubleToLongBits(my22);

        return (int)hash;
    }

    /**
     * Generate an inverse matrix.
     *
     * @return inverse matrix
     */
    public Matrix3d invert()
    {
        Matrix3d result = new Matrix3d();

        double det = determinant();
        if (MathUtil.isZero(det))
        {
            result.zero();
            return result;
        }

        result.my00 = my11 * my22 - my12 * my21;
        result.my01 = my02 * my21 - my01 * my22;
        result.my02 = my01 * my12 - my02 * my11;
        result.my10 = my12 * my20 - my10 * my22;
        result.my11 = my00 * my22 - my02 * my20;
        result.my12 = my02 * my10 - my00 * my12;
        result.my20 = my10 * my21 - my11 * my20;
        result.my21 = my01 * my20 - my00 * my21;
        result.my22 = my00 * my11 - my01 * my10;

        result.multLocal(1. / det);

        return result;
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
        Matrix3d ident = new Matrix3d();
        return equals(ident);
    }

    /**
     * sets this matrix to the identity matrix. Where all values are zero except
     * those along the diagonal which are one.
     */
    public final void loadIdentity()
    {
        my01 = my02 = my10 = my12 = my20 = my21 = 0;
        my00 = my11 = my22 = 1;
    }

    /**
     * multiplies this matrix by a given matrix. The result matrix is returned
     * as a new object.
     *
     * @param mat the matrix to multiply this matrix by.
     * @return a matrix3d object containing the result of this operation
     */
    public Matrix3d mult(Matrix3d mat)
    {
        Matrix3d result = new Matrix3d();

        double temp00 = my00 * mat.my00 + my01 * mat.my10 + my02 * mat.my20;
        double temp01 = my00 * mat.my01 + my01 * mat.my11 + my02 * mat.my21;
        double temp02 = my00 * mat.my02 + my01 * mat.my12 + my02 * mat.my22;
        double temp10 = my10 * mat.my00 + my11 * mat.my10 + my12 * mat.my20;
        double temp11 = my10 * mat.my01 + my11 * mat.my11 + my12 * mat.my21;
        double temp12 = my10 * mat.my02 + my11 * mat.my12 + my12 * mat.my22;
        double temp20 = my20 * mat.my00 + my21 * mat.my10 + my22 * mat.my20;
        double temp21 = my20 * mat.my01 + my21 * mat.my11 + my22 * mat.my21;
        double temp22 = my20 * mat.my02 + my21 * mat.my12 + my22 * mat.my22;

        result.my00 = temp00;
        result.my01 = temp01;
        result.my02 = temp02;
        result.my10 = temp10;
        result.my11 = temp11;
        result.my12 = temp12;
        result.my20 = temp20;
        result.my21 = temp21;
        result.my22 = temp22;

        return result;
    }

    /**
     * Multiplies this 3x3 matrix by the 1x3 Vector vec and stores the result in
     * product.
     *
     * @param vec The Vector3d to multiply.
     * @return The given product vector.
     */
    public Vector3d mult(Vector3d vec)
    {
        return new Vector3d(my00 * vec.getX() + my01 * vec.getY() + my02 * vec.getZ(),
                my10 * vec.getX() + my11 * vec.getY() + my12 * vec.getZ(),
                my20 * vec.getX() + my21 * vec.getY() + my22 * vec.getZ());
    }

    /**
     * multiplies this matrix internally by a given double scale factor.
     *
     * @param scale the value to scale by.
     * @return this Matrix3d
     */
    public Matrix3d multLocal(double scale)
    {
        my00 *= scale;
        my01 *= scale;
        my02 *= scale;
        my10 *= scale;
        my11 *= scale;
        my12 *= scale;
        my20 *= scale;
        my21 *= scale;
        my22 *= scale;
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
    public Matrix3d multLocal(Matrix3d mat)
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
            my20 = fb.get();
            my01 = fb.get();
            my11 = fb.get();
            my21 = fb.get();
            my02 = fb.get();
            my12 = fb.get();
            my22 = fb.get();
        }
        else
        {
            my00 = fb.get();
            my01 = fb.get();
            my02 = fb.get();
            my10 = fb.get();
            my11 = fb.get();
            my12 = fb.get();
            my20 = fb.get();
            my21 = fb.get();
            my22 = fb.get();
        }
    }

    /**
     * scales the operation performed by this matrix on a per-component basis.
     *
     * @param scale The scale applied to each of the X, Y and Z output values.
     */
    public void scale(Vector3d scale)
    {
        my00 *= scale.getX();
        my10 *= scale.getX();
        my20 *= scale.getX();
        my01 *= scale.getY();
        my11 *= scale.getY();
        my21 *= scale.getY();
        my02 *= scale.getZ();
        my12 *= scale.getZ();
        my22 *= scale.getZ();
    }

    @Override
    public final void set(double[] matrix, boolean rowMajor)
    {
        if (rowMajor)
        {
            my00 = matrix[0];
            my01 = matrix[1];
            my02 = matrix[2];
            my10 = matrix[3];
            my11 = matrix[4];
            my12 = matrix[5];
            my20 = matrix[6];
            my21 = matrix[7];
            my22 = matrix[8];
        }
        else
        {
            my00 = matrix[0];
            my01 = matrix[3];
            my02 = matrix[6];
            my10 = matrix[1];
            my11 = matrix[4];
            my12 = matrix[7];
            my20 = matrix[2];
            my21 = matrix[5];
            my22 = matrix[8];
        }
    }

    /**
     * sets the values of the matrix to those supplied by the 3x3 two dimenion
     * array.
     *
     * @param matrix the new values of the matrix.
     */
    public final void set(double[][] matrix)
    {
        my00 = matrix[0][0];
        my01 = matrix[0][1];
        my02 = matrix[0][2];
        my10 = matrix[1][0];
        my11 = matrix[1][1];
        my12 = matrix[1][2];
        my20 = matrix[2][0];
        my21 = matrix[2][1];
        my22 = matrix[2][2];
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
                    case 2:
                        my02 = value;
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
    public final void set(Matrix3d matrix)
    {
        my00 = matrix.my00;
        my01 = matrix.my01;
        my02 = matrix.my02;
        my10 = matrix.my10;
        my11 = matrix.my11;
        my12 = matrix.my12;
        my20 = matrix.my20;
        my21 = matrix.my21;
        my22 = matrix.my22;
    }

    /**
     * builds a rotation from a <code>Quaternion</code>.
     *
     * @param quat the quaternion to build the rotation from.
     */
    public final void set(Quaternion quat)
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
     * sets a particular column of this matrix to that represented by the
     * provided vector.
     *
     * @param index the column to set.
     * @param column the data to set.
     */
    public void setColumn(int index, Vector3d column)
    {
        switch (index)
        {
            case 0:
                my00 = column.getX();
                my10 = column.getY();
                my20 = column.getZ();
                break;
            case 1:
                my01 = column.getX();
                my11 = column.getY();
                my21 = column.getZ();
                break;
            case 2:
                my02 = column.getX();
                my12 = column.getY();
                my22 = column.getZ();
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
    public void setRow(int index, Vector3d row)
    {
        switch (index)
        {
            case 0:
                my00 = row.getX();
                my01 = row.getY();
                my02 = row.getZ();
                break;
            case 1:
                my10 = row.getX();
                my11 = row.getY();
                my12 = row.getZ();
                break;
            case 2:
                my20 = row.getX();
                my21 = row.getY();
                my22 = row.getZ();
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
        double[] data = new double[9];
        if (rowMajor)
        {
            data[0] = my00;
            data[1] = my01;
            data[2] = my02;
            data[3] = my10;
            data[4] = my11;
            data[5] = my12;
            data[6] = my20;
            data[7] = my21;
            data[8] = my22;
        }
        else
        {
            data[0] = my00;
            data[1] = my10;
            data[2] = my20;
            data[3] = my01;
            data[4] = my11;
            data[5] = my21;
            data[6] = my02;
            data[7] = my12;
            data[8] = my22;
        }
        return data;
    }

    @Override
    public String toString()
    {
        StringBuffer result = new StringBuffer("Matrix3d\n[\n ");
        result.append(my00);
        String spaces = "  ";
        result.append(spaces);
        result.append(my01);
        result.append(spaces);
        result.append(my02);
        result.append(" \n ");
        result.append(my10);
        result.append(spaces);
        result.append(my11);
        result.append(spaces);
        result.append(my12);
        result.append(" \n ");
        result.append(my20);
        result.append(spaces);
        result.append(my21);
        result.append(spaces);
        result.append(my22);
        result.append(" \n]");
        return result.toString();
    }

    /**
     * create a transposed version of this matrix.
     *
     * @return The new Matrix3d object.
     */
    public Matrix3d transpose()
    {
        return new Matrix3d(my00, my10, my20, my01, my11, my21, my02, my12, my22);
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
        my00 = my01 = my02 = my10 = my11 = my12 = my20 = my21 = my22 = 0.0;
    }

    /**
     * A function for creating a rotation matrix that rotates a vector called
     * "start" into another vector called "end" when start and end are almost
     * parallel.
     *
     * See "Tomas Möller, John Hughes \"Efficiently Building a Matrix to Rotate
     * \ One Vector to Another\" Journal of Graphics Tools, 4(4):1-4, 1999"
     *
     * @param start normalized non-zero starting vector
     * @param end normalized non-zero ending vector
     */
    private void fromStartEndParallel(Vector3d start, Vector3d end)
    {
        Vector3d x;

        x = new Vector3d(start.getX() > 0.0 ? start.getX() : -start.getX(), start.getY() > 0.0 ? start.getY() : -start.getY(),
                start.getZ() > 0.0 ? start.getZ() : -start.getZ());

        if (x.getX() < x.getY())
        {
            if (x.getX() < x.getZ())
            {
                x = Vector3d.UNIT_X;
            }
            else
            {
                x = Vector3d.UNIT_Z;
            }
        }
        else
        {
            if (x.getY() < x.getZ())
            {
                x = Vector3d.UNIT_Y;
            }
            else
            {
                x = Vector3d.UNIT_Z;
            }
        }

        Vector3d u = new Vector3d(x.getX() - start.getX(), x.getY() - start.getY(), x.getZ() - start.getZ());
        Vector3d cross = new Vector3d(x.getX() - end.getX(), x.getY() - end.getY(), x.getZ() - end.getZ());

        double c1 = 2.0 / u.dot(u);
        double c2 = 2.0 / cross.dot(cross);
        double c3 = c1 * c2 * u.dot(cross);

        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                double val = -c1 * u.get(i) * u.get(j) - c2 * cross.get(i) * cross.get(j) + c3 * cross.get(i) * u.get(j);
                set(i, j, val);
            }
            double val = get(i, i);
            set(i, i, val + 1.0);
        }
    }
}
