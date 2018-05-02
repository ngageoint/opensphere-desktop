package io.opensphere.analysis.table.functions;

import io.opensphere.core.util.lang.NumberUtilities;

/** Difference between values in order of appearance. */
public class Difference extends ColumnFunction
{
    /** Constructs a Difference function. */
    public Difference()
    {
        super("Difference", 0, Difference::performDiff);
    }

    /**
     * Maps all objects to Double, then returns their difference.
     * <p>
     * Non-parseable objects are mapped to the (additive) identity value
     *
     * @param objects the objects to operate on
     * @return the difference
     */
    static Double performDiff(Object... objects)
    {
        double[] diffArr = new double[objects.length];
        for (int i = 0; i < objects.length; i++)
        {
            diffArr[i] = NumberUtilities.parseDouble(objects[i], 0.);
        }

        // this is gonna throw if we don't have any objects but whatever
        double diff = diffArr[0];
        for (int i = 1; i < diffArr.length; i++)
        {
            diff -= diffArr[i];
        }

        return Double.valueOf(diff);
    }
}
