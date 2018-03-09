package io.opensphere.analysis.table.functions;

import io.opensphere.core.util.lang.NumberUtilities;

/** Class representation of a delta function between multiple columns. */
public class Difference extends ColumnFunction
{
    /**
     * Constructs a ColumnDelta.
     *
     * @param columnA the initial column
     * @param columnB the applied column, subtract from initial
     * @param valueA the initial value
     * @param valueB the applied value, subtract from initial
     */
    public Difference(String columnA, String columnB, Object valueA, Object valueB)
    {
        super(columnA, columnB, valueA, valueB, (left, right) ->
        {
            double dLeft = NumberUtilities.parseDouble(left, 0.0);
            double dRight = NumberUtilities.parseDouble(right, 0.0);

            return Double.valueOf(dLeft - dRight);
        });
    }
}
