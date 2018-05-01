package io.opensphere.analysis.table.functions;

import io.opensphere.core.util.lang.NumberUtilities;

/** Sum of values. */
public class Sum extends ColumnFunction
{
    /** Constructs a Sum function. */
    public Sum()
    {
        super("Sum", 0, Sum::performSum);
    }

    /**
     * Maps all objects to Double, then returns their sum.
     *
     * @param objects the objects to operate on
     * @return the sum
     */
    static Object performSum(Object... objects)
    {
        double sum = 0.;
        for (Object o : objects)
        {
            sum += NumberUtilities.parseDouble(o, 0.);
        }

        return Double.valueOf(sum);
    }
}
