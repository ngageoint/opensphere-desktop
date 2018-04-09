package io.opensphere.core.common.convolve;

/**
 * Encapsulates the least-squares algorithm for approximating the intersection
 * of any number of lines in the plane. The location found is the one that
 * minimizes the sum of the squared distances from the lines. Also output are
 * the eigenvalues and eigenvectors of the covariance matrix, which can be used
 * to describe the probability distribution, assuming that probability density
 * is inversely proportional the exponential of the sum of the squared distances
 * from the lines.
 */
public class LeastSqLineLocator
{
    /** Threshold for rejection of linear system. */
    private static double MINIMUM_CONDITION = 0.002;

    /** Initial point of the parametric line form. */
    private double[][] p;

    /** Linear coefficient of the parametric line form. */
    private double[][] dp;

    /** Linear coefficients of input lines as affine subspaces. */
    private double[][] a;

    /** Constant coefficients of input lines as affine subspaces. */
    private double[] b;

    /** Coordinates of calculated least-squares location. */
    private double[] location;

    /** Eigenvalues of the covariance matrix. */
    private double[] lambda;

    /** Orthonormal eigenvector basis for the covariance matrix. */
    private double[][] basis;

    /** Report on problems that arise during the calculation. */
    private String errorMessage;

    /**
     * Retrieve the location as an output of the algorithm.
     *
     * @return the location
     */
    public double[] getLocation()
    {
        return location;
    }

    /**
     * Gets the eigenvalues of the covariance matrix. If there are two distinct
     * values, then the larger one is first.
     *
     * @return the eigenvalues
     */
    public double[] getEigenvals()
    {
        return lambda;
    }

    /**
     * Gets the unit-length eigenvectors of the covariance matrix. The
     * eigenvectors are in the same order as the corresponding eigenvalues.
     *
     * @return the eigenvectors
     */
    public double[][] getEigenVecs()
    {
        return basis;
    }

    /**
     * Find out what, if anything, went wrong during the calculation.
     *
     * @return the error message, if any, or null
     */
    public String getErrorMessage()
    {
        return errorMessage;
    }

    /**
     * Find the inverse of the standard deviation matrix (standard deviation is
     * the square-root of the covariance matrix).
     *
     * @return inverse STD
     */
    public double[][] getInverseStd()
    {
        return LeastSqLineSupport.power(getQuadraticMatrix(), -0.5);
    }

    /** Run the algorithm on supplied inputs and calculate the outputs. */
    public void localize()
    {
        double[][] aStarA = LeastSqLineSupport.multStarMatrix(a, a);
        double condFactor = LeastSqLineSupport.conditionFactor(aStarA);
        if (condFactor < MINIMUM_CONDITION)
        {
            errorMessage = "Can't localize because lines are nearly parallel.";
            return;
        }
        double[][] aStarAInv = LeastSqLineSupport.inverse2x2(aStarA);
        double[][] m = LeastSqLineSupport.multMatrixStar(aStarAInv, a);

        location = LeastSqLineSupport.multVec(m, b);
        LeastSqLineSupport.scalarMult(-1.0, location);

        double[][] u = getQuadraticMatrix();
        lambda = LeastSqLineSupport.eigenVal2x2(u);
        basis = LeastSqLineSupport.eigenVec2x2(u, lambda);
    }

    /**
     * Use the parametric form of the line at index <i>i</i> to test whether the
     * pointing angle is sufficiently close to the least-squares localization.
     * The method returns true if the angle (in degrees) between the ray and the
     * bearing to the calculated location is less than the specified threshold.
     *
     * @param i the index of the line to be tested
     * @param thresholdDeg the required angle in degrees
     * @return true if the ray points (more or less) toward the localization
     */
    public boolean checkPointingAngle(int i, double thresholdDeg)
    {
        // subtract the ray's initial point from the localization
        double[] bearingVector = new double[2];
        LeastSqLineSupport.add(bearingVector, location);
        LeastSqLineSupport.addMult(bearingVector, -1.0, p[i]);
        // the dot product of the ray's direction and the normalized vector
        // is the cosine of the relative bearing
        double cosTheta = LeastSqLineSupport.multVec(bearingVector, dp[i]) / LeastSqLineSupport.magnitude(bearingVector);
        // compare that to the cosine of the threshold angle
        return Math.cos(Math.toRadians(thresholdDeg)) < cosTheta;
    }

    /**
     * Set the number of lines; i.e., create arrays to hold the coefficients.
     *
     * @param n the number of lines
     */
    public void setNumLines(int n)
    {
        // make space for lines in parametric form (retain pointing direction)
        p = new double[n][];
        dp = new double[n][];

        // make space for lines in affine functional form
        a = new double[n][];
        b = new double[n];
    }

    /**
     * Add a new input line in parametric form p(t) = (x0, y0) + t (dx, dy). It
     * is converted to affine subspace form for the calculation.
     * 
     * @param x0 x-coordinate of a point on the line
     * @param y0 y-coordinate of a point on the line
     * @param dx x-coordinate of the direction of the line
     * @param dy y-coordinate of the direction of the line
     * @param index the numerical index of this line
     */
    public void addLine(double x0, double y0, double dx, double dy, int index)
    {
        double mag = Math.sqrt(LeastSqLineSupport.sq(dx) + LeastSqLineSupport.sq(dy));

        // keep the parametric form for later analysis
        p[index] = new double[] { x0, y0 };
        dp[index] = new double[] { dx / mag, dy / mag };

        // convert to affine functional form
        double ax = dy / mag;
        double ay = -dx / mag;
        a[index] = new double[] { ax, ay };
        b[index] = -(ax * x0 + ay * y0);
    }

    /**
     * Calculate the covariance matrix, which is the sum of the tensor products
     * of the linear coefficient vectors with themselves.
     *
     * @return the covariance matrix
     */
    private double[][] getQuadraticMatrix()
    {
        double[][] cov = new double[][] { { 0.0, 0.0 }, { 0.0, 0.0 } };
        for (int i = 0; i < a.length; i++)
        {
            double[][] row = new double[][] { a[i] };
            LeastSqLineSupport.add(cov, LeastSqLineSupport.multStarMatrix(row, row));
        }
        return cov;
    }
}