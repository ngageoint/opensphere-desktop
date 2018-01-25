package io.opensphere.core.common.math;

public class Fit
{

    public static double[] polyFit(double[] Xs, double[] Ys, int degree)
    {
        int numPoints = Xs.length;
        int degreePoly = degree;

        Matrix mat = new Matrix(numPoints, degreePoly + 1);
        double[] ab = { 0.0, 0.0, 0.0 };
        Matrix bmat;

        for (int j = 0; j < degreePoly + 1; j++)
        {
            for (int i = 0; i < numPoints; i++)
            {
                mat.set(i, j, Math.pow(Xs[i], j));
            }
        }
        // Set B Matrix
        bmat = new Matrix(numPoints, 1);
        for (int i = 0; i < numPoints; i++)
        {
            bmat.set(i, 0, Ys[i]);
        }

        try
        {
            ab = mat.solve(bmat).getRowPackedCopy();
        }
        catch (RuntimeException e)
        {
            // matrix was singular, try a linear fit
            return linearFit(Xs, Ys);
        }
        // reverse the coefficients, because that's what colt needs
        double[] reversed = new double[ab.length];
        for (int i = 0; i < ab.length; i++)
        {
            reversed[ab.length - 1 - i] = ab[i];
        }
        return reversed;
    }

    public static double[] linearFit(double[] Xs, double[] Ys)
    {
        if (Xs.length < 2 || Ys.length < 2)
        {
            throw new IllegalArgumentException("array length must be >= 2");
        }
        double[] ret = new double[2];
        double xDiff = Xs[1] - Xs[0];
        if (xDiff == 0.0)
        {
            if (Ys[0] <= Ys[1])
            {
                ret[0] = Double.POSITIVE_INFINITY;
            }
            else
            {
                ret[0] = Double.NEGATIVE_INFINITY;
            }
            ret[1] = Ys[0];
        }
        else
        {
            double slope = (Ys[1] - Ys[0]) / xDiff;
            ret[0] = slope;
            double yIntercept = Ys[0] - slope * Xs[0];
            ret[1] = yIntercept;
        }
        return ret;
    }

}
