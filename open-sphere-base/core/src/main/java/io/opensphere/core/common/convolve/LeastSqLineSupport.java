package io.opensphere.core.common.convolve;

/**
 * Basic linear algebra and other tooling in support of the LeastSqLineLocator
 * class (q.v.).  Also included is a fairly extensive collection of graphical
 * testing tools.
 */
public class LeastSqLineSupport
{
    /**
     * Modify the matrix <i>u</i> by adding matrix <i>v</i>.
     *
     * @param u matrix to be modified
     * @param v matrix to be added
     */
    public static void add(double[][] u, double[][] v)
    {
        int m = Math.min(u.length, v.length);
        for (int i = 0; i < m; i++)
            add(u[i], v[i]);
    }

    /**
     * Modify vector <i>u</i> by adding vector <i>v</i> to it.
     *
     * @param u matrix to be modified
     * @param v matrix to be added
     */
    public static void add(double[] u, double[] v)
    {
        int n = Math.min(u.length, v.length);
        for (int j = 0; j < n; j++)
            u[j] += v[j];
    }

    /**
     * Add a scalar multiple of <i>v</i> to <i>u</i>.
     *
     * @param u the vector to be modified
     * @param a the scalar
     * @param v the vector to be added
     */
    public static void addMult(double[] u, double a, double[] v)
    {
        int n = Math.min(u.length, v.length);
        for (int j = 0; j < n; j++)
            u[j] += a * v[j];
    }

    /**
     * Multiply two matrices and return a new matrix as the product.
     *
     * @param u the multiplicand (left)
     * @param v the multiplyer (right)
     * @return the product of <i>u</i> and <i>v</i>
     */
    public static double[][] multMatrix(double[][] u, double[][] v)
    {
        int p = u.length;
        int q = v.length;
        int r = v[0].length;
        double[][] w = new double[p][r];
        for (int i = 0; i < p; i++)
            for (int j = 0; j < r; j++)
                for (int k = 0; k < q; k++)
                    w[i][j] += u[i][k] * v[k][j];
        return w;
    }

    /**
     * Multiply the transpose of <i>u</i> by <i>v</i> without modifying
     * <i>u</i> or explicitly calculating its transpose.
     *
     * @param u the transpose of the multiplicand (left)
     * @param v the multiplyer (right)
     * @return the requested matrix product
     */
    public static double[][] multStarMatrix(double[][] u, double[][] v)
    {
        int p = u[0].length;
        int q = v.length;
        int r = v[0].length;
        double[][] w = new double[p][r];
        for (int i = 0; i < p; i++)
            for (int j = 0; j < r; j++)
                for (int k = 0; k < q; k++)
                    w[i][j] += u[k][i] * v[k][j];
        return w;
    }

    /**
     * Multiply <i>u</i> by the transpose of <i>v</i> without modifying
     * <i>v</i> or explicitly calculating its transpose.
     *
     * @param u the multiplicand (left)
     * @param v the transpose of the multiplyer (right)
     * @return the requested matrix product
     */
    public static double[][] multMatrixStar(double[][] u, double[][] v)
    {
        int p = u.length;
        int q = v[0].length;
        int r = v.length;
        double[][] w = new double[p][r];
        for (int i = 0; i < p; i++)
            for (int j = 0; j < r; j++)
                for (int k = 0; k < q; k++)
                    w[i][j] += u[i][k] * v[j][k];
        return w;
    }

    /**
     * Apply transform matrix <i>u</i> to vector <i>v</i> and return the
     * result as a new vector.
     *
     * @param u the transform matrix
     * @param v the vector
     * @return the product of <i>u</i> and <i>v</i>
     */
    public static double[] multVec(double[][] u, double[] v)
    {
        int p = u.length;
        int q = u[0].length;
        double[] w = new double[p];
        for (int i = 0; i < p; i++)
            for (int j = 0; j < q; j++)
                w[i] += u[i][j] * v[j];
        return w;
    }

    /**
     * Apply the transpose of matrix <i>u</i> to vector <i>v</i> and return
     * the result as a new vector.  Matrix <i>u</i> is not modified, nor is
     * its transpose explicitly calculated.
     *
     * @param u the transpose of the transform matrix
     * @param v the vector
     * @return the requested product
     */
    public static double[] multStarVec(double[][] u, double[] v)
    {
        int p = u[0].length;
        int q = u.length;
        double[] w = new double[p];
        for (int i = 0; i < p; i++)
            for (int j = 0; j < q; j++)
                w[i] += u[j][i] * v[j];
        return w;
    }

    /**
     * Find the inner product of two vectors.
     *
     * @param u a vector
     * @param v another vector
     * @return the inner product of <i>u</i> and <i>v</i>
     */
    public static double multVec(double[] u, double[] v)
    {
        int n = Math.min(u.length, v.length);
        double sum = 0.0;
        for (int i = 0; i < n; i++)
            sum += u[i] * v[i];
        return sum;
    }

    /**
     * Multiply a vector by a scalar, modifying it in place.
     *
     * @param c the scalar
     * @param u the vector
     */
    public static void scalarMult(double c, double[] u)
    {
        for (int i = 0; i < u.length; i++)
            u[i] *= c;
    }

    /**
     * Find an arbitrary power of a diagonalizable 2x2 matrix.  Suitability
     * for the operation is assumed, so the caller is responsible for any
     * checking that may be necessary.
     *
     * @param u the matrix
     * @param p the power
     * @return <i>u</i> to the power of <i>p</i>
     */
    public static double[][] power(double[][] u, double p)
    {
        double[] lambda = eigenVal2x2(u);
        double[][] basis = eigenVec2x2(u, lambda);

        double[][] diag = new double[][] {{Math.pow(lambda[0], p), 0.0},
            {0.0, Math.pow(lambda[1], p)}};
        return multStarMatrix(basis, multMatrix(diag, basis));
    }

    /**
     * Calculate a coefficient describing how readily inverted <i>m</i> is.
     * Closer to zero is worse.
     * @param m a 2x2 matrix
     * @return the condition factor
     */
    public static double conditionFactor(double[][] m)
    {
        double[] lambda = eigenVal2x2(m);
        if (lambda.length == 0)
            return 0.0;
        if (lambda.length == 1)
            return 1.0;
        return lambda[1] / lambda[0];
    }

    /**
     * Calculate the inverse of a 2x2 matrix, if possible.  Suitability for
     * the operation is assumed, so the caller is responsible for any checking
     * that may be necessary.
     *
     * @param u the matrix
     * @return the inverse of <i>u</i>
     */
    public static double[][] inverse2x2(double[][] u)
    {
        double d = u[0][0] * u[1][1] - u[0][1] * u[1][0];
        return new double[][] {{u[1][1] / d, -u[0][1] / d},
            {-u[1][0] / d, u[0][0] / d}};
    }

    /**
     * Find the real eigenvalues of a 2x2 matrix by finding the characteristic
     * polynomial (a quadratic) and solving for its real roots.
     *
     * @param m the matrix
     * @return the eigenvalues of <i>m</i>
     */
    public static double[] eigenVal2x2(double[][] m)
    {
        return solveQuadratic(new double[] {
            m[0][0] * m[1][1] - m[0][1] * m[1][0], -m[0][0] - m[1][1], 1.0});
    }

    /**
     * Find the eigenvectors of the matrix given its eigenvalues.  The matrix
     * is assumed to be diagonalizable.
     *
     * @param m the matrix
     * @param lambda the eigenvalues of <i>m</i>
     * @return the eigenvectors of <i>m</i>
     */
    public static double[][] eigenVec2x2(double[][] m, double[] lambda)
    {
        // no real eigenvalues => punt
        if (lambda.length == 0)
            return null;
        // both eigenvalues are the same => all vectors are eigenvectors
        if (lambda.length == 1)
            return new double[][] {{1.0, 0.0}, {0.0, 1.0}};

        // find eigenvectors for the two distinct eigenvalues in descending order
        return new double[][] {eigenVec(m, lambda[0]), eigenVec(m, lambda[1])};
    }

    /**
     * Find an eigenvector of the matrix <i>m</i> for the given eigenvalue.
     *
     * @param m the matrix
     * @param lambda an eigenvalue of <i>m</i>
     * @return the eigenvector of <i>m</i> for eigenvalue <i>lambda</i>
     */
    private static double[] eigenVec(double[][] m, double lambda)
    {
        double c0 = m[0][0] - lambda;
        double c1 = m[0][1];
        if (c0 != 0.0 || c1 != 0.0)
            return orthUnit(new double[] {c0, c1});
        return orthUnit(new double[] {m[1][0], m[1][1] - lambda});
    }

    /**
     * Find a unit vector orthogonal to <i>u</i> in the plane.
     *
     * @param u a vector in the plane
     * @return an orthogonal unit vector
     */
    private static double[] orthUnit(double[] u)
    {
        double[] v = new double[] {-u[1], u[0]};
        normalize(v);
        return v;
    }

    /**
     * Calculate the magnitude of the given vector.
     * @param v the vector
     * @return the magnitude
     */
    public static double magnitude(double[] v)
    {
        return Math.sqrt(multVec(v, v));
    }

    /**
     * Convert a nonzero vector into a parallel unit vector.
     *
     * @param v the vector to be normalized
     */
    public static void normalize(double[] v)
    {
        scalarMult(1.0 / magnitude(v), v);
    }

    /**
     * Find the real roots of a quadratic polynomial.  In the degenerate case
     * where the quadratic coefficient is zero, a linear solution is found.
     *
     * @param p the coefficients in order of increasing degree
     * @return the real roots of <i>p</i>
     */
    public static double[] solveQuadratic(double[] p)
    {
        if (p[2] == 0.0)
            return solveLinear(p);
        double discr = sq(p[1]) - 4.0 * p[2] * p[0];
        if (discr < 0.0)
            return new double[0];
        double denom = 2.0 * p[2];
        if (discr == 0.0)
            return new double[] {-p[1] / denom};
        double rootDiscr = Math.sqrt(discr);
        return new double[] {(-p[1] + rootDiscr) / denom, (-p[1] - rootDiscr) / denom};
    }

    /**
     * Find the real root (if any) of a linear polynomial.
     *
     * @param p the coefficients in order of increasing degree
     * @return the real root, if it exists or an empty array
     */
    public static double[] solveLinear(double[] p)
    {
        if (p[1] == 0)
            return new double[0];
        return new double[] {-p[0] / p[1]};
    }

    /**
     * Square function.
     * @param t a value
     * @return <i>t</i> squared
     */
    public static double sq(double t)
    {
        return t * t;
    }
}
