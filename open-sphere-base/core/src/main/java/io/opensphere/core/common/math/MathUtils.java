package io.opensphere.core.common.math;

import java.util.Arrays;
import java.util.List;

/** Common Math functions. */
@Deprecated
public class MathUtils
{

    /** Constructor. */
    private MathUtils()
    {
    };

    /**
     * Determines the mean value of a list of numbers.
     *
     * @param nums the numbers
     * @return the mean, or NaN if nums is null or empty
     */
    public static double mean(List<Double> nums)
    {
        if (nums == null || nums.size() == 0)
        {
            return Double.NaN;
        }
        double sum = 0.0;
        for (int i = 0; i < nums.size(); i++)
        {
            sum += nums.get(i).doubleValue();
        }
        return sum / nums.size();
    }

    /**
     * Determines the median value in a list of numbers.
     * <p>
     * The math isn't correct (the median value in an even list is the average
     * of the two center values) but this isn't used anywhere so who cares.
     *
     * @param nums the numbers
     * @return the median value, or NaN if nums is null or empty
     */
    public static double median(List<Double> nums)
    {
        if (nums == null || nums.size() == 0)
        {
            return Double.NaN;
        }
        Double[] numbers = new Double[nums.size()];
        nums.toArray(numbers);
        Arrays.sort(numbers);
        int medianIndex = nums.size() / 2;
        return numbers[medianIndex].doubleValue();
    }

    /**
     * Determines the maximum value in a list of numbers.
     *
     * @param nums the numbers
     * @return the maximum value, or NaN if nums is null or empty
     */
    public static double max(List<Double> nums)
    {
        if (nums == null || nums.size() == 0)
        {
            return Double.NaN;
        }
        double max = Double.MIN_VALUE;
        for (int i = 0; i < nums.size(); i++)
        {
            double cur = nums.get(i).doubleValue();
            if (cur > max)
            {
                max = cur;
            }
        }
        return max;
    }

    /**
     * Determines the minimum value in a list of numbers.
     *
     * @param nums the numbers
     * @return the minimum value, or NaN if nums is null or empty
     */
    public static double min(List<Double> nums)
    {
        if (nums == null || nums.size() == 0)
        {
            return Double.NaN;
        }
        double min = Double.MAX_VALUE;
        for (int i = 0; i < nums.size(); i++)
        {
            double cur = nums.get(i).doubleValue();
            if (cur < min)
            {
                min = cur;
            }
        }
        return min;
    }

    /**
     * Calculates the distance between two points.
     *
     * @param x1 Point 1 X-coordinate
     * @param y1 Point 1 Y-coordinate
     * @param x2 Point 2 X-coordinate
     * @param y2 Point 2 Y-coordinate
     * @return the straight-line distance
     */
    public static double distance(int x1, int y1, int x2, int y2)
    {
        int xDist = Math.abs(x1 - x2);
        int yDist = Math.abs(y1 - y2);
        return Math.sqrt(xDist * xDist + yDist * yDist);
    }
}
