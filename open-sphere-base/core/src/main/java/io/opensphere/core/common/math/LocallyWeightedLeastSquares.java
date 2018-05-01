package io.opensphere.core.common.math;

public class LocallyWeightedLeastSquares
{

    private static double[] xArray;

    private static double[] yArray;

    private static double[] yEst;

    private static double[] xSub;

    private static double[] ySub;

    private static int subQueryPointIndex;

    private static double[] weights;

    public static double[] getSmoothedArray(double[] xInput, double[] yInput, double bandwidthPct)
    {

        xArray = xInput;
        yArray = yInput;
        int totalDataCount = xArray.length;
        yEst = new double[totalDataCount];

        int windowSize = Double.valueOf(bandwidthPct * totalDataCount).intValue();

        if (windowSize > 1)
        {
            xSub = new double[windowSize];
            ySub = new double[windowSize];
            weights = new double[windowSize];

            // Construct matrix
            Matrix WX = new Matrix(windowSize, 2);
            Matrix WY = new Matrix(windowSize, 1);

            for (int q = 0; q < totalDataCount; q++)
            {

                weights = getEstWindowSubArrayAndWeights(q, windowSize);

                for (int row = 0; row < windowSize; row++)
                {
                    WX.set(row, 0, Math.sqrt(weights[row]));
                    WX.set(row, 1, Math.sqrt(weights[row]) * xSub[row]);
                    WY.set(row, 0, Math.sqrt(weights[row]) * ySub[row]);
                }

                // Apply first order estimation: local linear model
                Matrix WXT = WX.transpose();
                try
                {
                    Matrix B = (WXT.times(WX)).inverse().times((WXT).times(WY));

                    float B0 = (float)B.get(0, 0);
                    float B1 = (float)B.get(1, 0);
                    yEst[q] = B0 + B1 * xSub[subQueryPointIndex];
                }
                catch (RuntimeException re)
                {
                    // matrix was singular
                    // all speeds were close enough that determinant was 0
                    // so just use the original y value
                    yEst[q] = yArray[q];
                }
            }
        }
        else
        {
            // windowSize = 0 or 1
            for (int q = 0; q < totalDataCount; q++)
            {
                yEst[q] = 0;
            }
        }
        return yEst;
    }

    public double[] getXarray()
    {
        return xArray;
    }

    public double[] getYEst()
    {
        return yEst;
    }

    private static double[] getEstWindowSubArrayAndWeights(int queryPointIndex, int window)
    {

        int fullLength = xArray.length;
        int subStartIndex = 0;
        int windowSize = window;
        double maxDistance = 0;

        // middle region data
        if ((queryPointIndex >= windowSize) && (queryPointIndex < fullLength - windowSize))
        {
            for (int i = 0; i < windowSize; i++)
            {
                if ((xArray[queryPointIndex] - (xArray[queryPointIndex - windowSize + 1 + i])) <= (xArray[queryPointIndex + 1 + i]
                        - xArray[queryPointIndex]))
                {
                    subStartIndex = queryPointIndex - windowSize + 1 + i;
                    subQueryPointIndex = queryPointIndex - subStartIndex;
                    maxDistance = (((xArray[queryPointIndex] - xArray[subStartIndex]) > (xArray[subStartIndex + windowSize - 1])
                            - xArray[queryPointIndex]) ? (xArray[queryPointIndex] - xArray[subStartIndex])
                                    : (xArray[subStartIndex + windowSize - 1] - xArray[queryPointIndex]));
                    break;
                }
            }
        }
        else if (queryPointIndex < windowSize)
        {
            for (int i = 0; i < windowSize; i++)
            {
                if ((xArray[queryPointIndex] - xArray[i]) <= (xArray[windowSize + i] - xArray[queryPointIndex]))
                {
                    subStartIndex = i;
                    subQueryPointIndex = queryPointIndex - subStartIndex;
                    maxDistance = ((xArray[queryPointIndex] - xArray[subStartIndex]) > (xArray[subStartIndex + windowSize - 1]
                            - xArray[queryPointIndex])) ? (xArray[queryPointIndex] - xArray[subStartIndex])
                                    : (xArray[subStartIndex + windowSize - 1] - xArray[queryPointIndex]);
                    break;
                }
            }
        }
        else
        {
            // right margin data
            for (int i = 0; i < windowSize; i++)
            {
                if ((xArray[fullLength - 1 - i] - xArray[queryPointIndex]) <= (xArray[queryPointIndex]
                        - xArray[fullLength - windowSize - 1 - i]))
                {
                    subStartIndex = fullLength - windowSize - i;
                    subQueryPointIndex = queryPointIndex - subStartIndex;
                    maxDistance = ((xArray[queryPointIndex] - xArray[subStartIndex]) > (xArray[subStartIndex + windowSize - 1]
                            - xArray[queryPointIndex])) ? (xArray[queryPointIndex] - xArray[subStartIndex])
                                    : (xArray[subStartIndex + windowSize - 1] - xArray[queryPointIndex]);
                    break;
                }
            }
        }

        System.arraycopy(xArray, subStartIndex, xSub, 0, windowSize);
        System.arraycopy(yArray, subStartIndex, ySub, 0, windowSize);

        // Calculate distances and find tricube weights
        for (int j = 0; j < windowSize; j++)
        {
            double distance = Math.abs(xSub[subQueryPointIndex] - xSub[j]);
            weights[j] = (float)Math.pow(1.0 - Math.pow((distance / maxDistance), 3.0), 3.0);
        }
        return weights;
    }
}
