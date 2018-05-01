package io.opensphere.analysis.table.functions;

/** Average of values. */
public class Average extends ColumnFunction
{
    /** Constructs a Average function. */
    public Average()
    {
        super("Average", 0, Average::performAverage);
    }

    /**
     * Maps all objects to Double, then returns their average.
     * <p>
     * Non-parseable objects are mapped to the (additive) identity value
     *
     * @param objects the objects to operate on
     * @return the average
     */
    static Double performAverage(Object... objects)
    {
        return Double.valueOf(Sum.performSum(objects).doubleValue() / objects.length);
    }
}
