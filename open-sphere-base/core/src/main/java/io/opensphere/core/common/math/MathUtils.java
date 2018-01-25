package io.opensphere.core.common.math;

import java.util.Arrays;
import java.util.List;

public class MathUtils
{

    private MathUtils()
    {
    };

    public static double mean(List<Double> nums)
    {
        if (nums == null || nums.size() == 0)
        {
            return Double.NaN;
        }
        double sum = 0.0;
        for (int i = 0; i < nums.size(); i++)
        {
            sum += nums.get(i);
        }
        return sum / (double)nums.size();
    }

    public static double median(List<Double> nums)
    {
        if (nums == null || nums.size() == 0)
        {
            return Double.NaN;
        }
        Double[] numbers = new Double[nums.size()];
        nums.toArray(numbers);
        Arrays.sort(numbers);
        int medianIndex = (int)nums.size() / 2;
        return nums.get(medianIndex);
    }

    public static double max(List<Double> nums)
    {
        if (nums == null || nums.size() == 0)
        {
            return Double.NaN;
        }
        double max = Double.MIN_VALUE;
        for (int i = 0; i < nums.size(); i++)
        {
            if (nums.get(i) > max)
            {
                max = nums.get(i);
            }
        }
        return max;
    }

    public static double min(List<Double> nums)
    {
        if (nums == null || nums.size() == 0)
        {
            return Double.NaN;
        }
        double min = Double.MAX_VALUE;
        for (int i = 0; i < nums.size(); i++)
        {
            if (nums.get(i) < min)
            {
                min = nums.get(i);
            }
        }
        return min;
    }

    public static double distance(int x1, int y1, int x2, int y2)
    {
        int xDist = Math.abs(x1 - x2);
        int yDist = Math.abs(y1 - y2);
        return Math.sqrt(xDist * xDist + yDist * yDist);
    }
}
