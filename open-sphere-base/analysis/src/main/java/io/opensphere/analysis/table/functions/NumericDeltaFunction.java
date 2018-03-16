package io.opensphere.analysis.table.functions;

import io.opensphere.core.util.lang.NumberUtilities;

/** Class representation of a standard numeric delta. */
public class NumericDeltaFunction extends ColumnFunction
{
    /**
     * Constructs a NumericDeltaFunction.
     */
    public NumericDeltaFunction()
    {
        super("Numeric Delta", (left, right) ->
        {
            double dLeft = NumberUtilities.parseDouble(left, 0.0);
            double dRight = NumberUtilities.parseDouble(right, 0.0);

            return Double.valueOf(dLeft - dRight);
        });
    }
}
