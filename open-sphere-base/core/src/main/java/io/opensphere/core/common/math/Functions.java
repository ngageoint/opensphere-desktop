package io.opensphere.core.common.math;

import java.util.Arrays;

import cern.jet.stat.Gamma;

public class Functions
{

    public static double gammaInverse(double probability, double alpha, double beta)
    {
        if (beta <= 0)
        {
            throw new IllegalArgumentException("beta, is not a valid value: " + beta);

        }
        return Functions.gammaInverse(probability, alpha) * beta;
    }

    private static double gammaInverse(double probability, double alpha)
    {
        double beta = 1.0;
        double x = 0;

        if (alpha <= 0)
        {
            throw new IllegalArgumentException("alpha, is not a valid value: " + alpha);

        }

        if (probability < 0 || probability > 1)
        {
            throw new IllegalArgumentException("Probability is not a valid value: " + probability);
        }
        if (probability == 0)
        {
            x = 0;
        }
        else if (probability == 1)
        {
            x = Double.POSITIVE_INFINITY;
        }
        else
        {
            int LIMIT = 100;
            int count = 0;
            double kProbability = probability;
            // compute a starting guess
            double mn = alpha * beta;
            double v = mn * beta;
            double temp = Math.log(v + Math.pow(mn, 2));
            double mu = 2 * Math.log(mn) - 0.5 * temp;
            double sigma = -2 * Math.log(mn) + temp;
            double normal = normalInverse(probability, mu, sigma);
            double xK = Math.exp(normal);

            double h = 1;
            double EPS = Math.pow(2, -52);
            while (Math.abs(h) > Math.sqrt(EPS) * Math.abs(xK) && Math.abs(h) > Math.sqrt(EPS) && count < LIMIT)
            {
                count++;
                double cdf = gammaCdf(xK, alpha, beta);
                double pdf = gammaPdf(xK, alpha, beta);
                h = (cdf - kProbability) / pdf;
                double xNew = xK - h;
                if (xNew < 0)
                {
                    xNew = xK / 10.0;
                    h = xK - xNew;
                }
                xK = xNew;
            }
            x = xK;
        }
        return x;
    }

    public static double normalInverse(double probability, double mu, double sigma)
    {
        double x = 0.0;
        // inverse of the gamma cdf
        if (probability < 0 || probability > 1)
        {
            throw new IllegalArgumentException("probability was out of range " + probability);
        }
        if (sigma <= 0)
        {
            throw new IllegalArgumentException("sigma was out of range " + sigma);
        }
        if (probability == 0)
        {
            return Double.NEGATIVE_INFINITY;
        }
        else if (probability == 1)
        {
            return Double.POSITIVE_INFINITY;
        }
        else
        {

            x = Math.sqrt(2) * sigma * errorFunctionInverse(2 * probability - 1) + mu;
        }
        return x;
    }

    public static double errorFunctionInverse(double p)
    {
        if (p < -1 || p > 1)
        {
            throw new IllegalArgumentException("probability was out of range " + p);
        }

        double x = 0.0;
        if (p == 1.)
            return (Double.POSITIVE_INFINITY);
        else if (p == 0.)
            return (0);
        else if (p == -1)
            return Double.NEGATIVE_INFINITY;
        // see http://homepages.physik.uni-muenchen.de/~Winitzki/erf-approx.pdf
        // for the approximation equation and justification
        // largest relative error is 1.3 * 10^-4
        double c = 0.147;
        double a = -2 / (Math.PI * c);
        double b = 0 - Math.log(1 - Math.pow(p, 2)) / 2.0;
        double d = Math.pow((0 - a - b), 2);
        double e = d - (1 / c * Math.log(1 - Math.pow(p, 2)));
        double f = Math.sqrt(e) + b + a;
        x = Math.pow(f, 0.5);
        if (p < 0)
        {
            x = -x;
        }
        return x;
    }

    public static double normalInverse_T(float prob, double mu, double sigma)
    {
        // inverse of the gamma cdf
        if (prob < 0 || prob > 1)
        {
            throw new IllegalArgumentException("Probability is not a valid value: " + prob);
        }
        if (sigma <= 0)
        {
            throw new IllegalArgumentException("Sigma is not a valid value: " + prob + " - must be > 0");
        }
        if (prob == 0)
            return Double.NEGATIVE_INFINITY;
        else if (prob == 1)
            return Double.POSITIVE_INFINITY;
        else
            return Math.sqrt(2) * sigma * errorFunctionInverse(2 * prob - 1) + mu;
    }

    public static double gammaCdf(double x, double a, double b)
    {
        double p = 0.0;
        if (a <= 0)
        {
            throw new IllegalArgumentException("a, is not a valid value: " + a);
        }
        if (b <= 0)
        {
            throw new IllegalArgumentException("b, is not a valid value: " + b);
        }
        if (x == 0)
            return p;
        else
            p = Math.min(1.0, Gamma.incompleteGamma(a, x / b));

        return p;
    }

    public static double gammaPdf(double x, double a, double b)
    {
        double y = 0.0;
        if (a <= 0)
        {
            throw new IllegalArgumentException("a, is not a valid value: " + a);
        }
        if (b <= 0)
        {
            throw new IllegalArgumentException("b, is not a valid value: " + b);
        }

        if (x == 0 && a == 1)
        {
            y = 0.0;
        }
        else
        {
            y = (a - 1) * Math.log(x) - x / b - Gamma.logGamma(a) - a * Math.log(b);
            y = Math.exp(y);
        }
        return y;
    }

    public static double dot(double[] a, double[] b)
    {
        double sum = 0.0;
        if (a.length != b.length)
        {
            throw new IllegalArgumentException("vectors a and b must be same length to perform dot product");
        }
        for (int i = 0; i < a.length; i++)
        {
            sum += a[i] * b[i];
        }
        return sum;
    }

    public static double dot(double[] a, int[] b)
    {
        double[] bDoubles = new double[b.length];
        for (int i = 0; i < b.length; i++)
        {
            bDoubles[i] = b[i];
        }
        return dot(a, bDoubles);
    }

    public static double max(double[] a)
    {
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < a.length; i++)
        {
            max = (a[i] > max) ? a[i] : max;
        }
        return max;
    }

    public static double min(double[] a)
    {
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < a.length; i++)
        {
            min = (a[i] < min) ? a[i] : min;
        }
        return min;
    }

    public static int maxIndex(double[] a)
    {
        int index = -1;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < a.length; i++)
        {
            if (a[i] > max)
            {
                max = a[i];
                index = i;
            }
        }
        return index;
    }

    // this is not the most efficient way to do this
    // but it does not matter too much because n should be
    // substantially less than a.length
    public static int[] maxIndices(double[] a, int n)
    {
        if (n > a.length || n < 0)
        {
            throw new IllegalArgumentException("Can't return " + n + " indices from array of size " + a.length);
        }
        double[] copy = Arrays.copyOf(a, a.length);

        int[] indices = new int[n];
        int index = -1;
        for (int m = 0; m < n; m++)
        {
            double max = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < copy.length; i++)
            {
                if (copy[i] > max)
                {
                    max = copy[i];
                    index = i;
                }
            }
            copy[index] = Double.NEGATIVE_INFINITY;
            indices[m] = index;
        }
        return indices;
    }

    public static int minIndex(double[] a)
    {
        double min = Double.POSITIVE_INFINITY;
        int index = -1;
        for (int i = 0; i < a.length; i++)
        {
            if (a[i] < min)
            {
                min = a[i];
                index = i;
            }
        }
        return index;
    }

}
