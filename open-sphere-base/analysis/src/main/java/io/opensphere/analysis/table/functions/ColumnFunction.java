package io.opensphere.analysis.table.functions;

import java.util.function.BiFunction;

import io.opensphere.core.util.lang.NumberUtilities;

/** Representation of a multi-column function. */
public class ColumnFunction
{
    /** Array of column names. */
    private final String[] myColumns = new String[2];

    /** Array of column values. */
    private final double[] myValues = new double[2];

    /** The function to apply. */
    private final BiFunction<Object, Object, Object> myFunction;

    /**
     * Constructs a ColumnFunction.
     *
     * @param columnA the initial column
     * @param columnB the applied column
     * @param valueA the initial value
     * @param valueB the applied value
     * @param function the function to apply each value to
     */
    public ColumnFunction(String columnA, String columnB, Object valueA, Object valueB,
            BiFunction<Object, Object, Object> function)
    {
        myColumns[0] = columnA;
        myColumns[1] = columnB;

        myValues[0] = NumberUtilities.parseDouble(valueA, 0.0);
        myValues[1] = NumberUtilities.parseDouble(valueB, 0.0);

        myFunction = function;
    }

    /**
     * Constructs a ColumnFunction using a default function (addition).
     *
     * @param columnA the initial column
     * @param columnB the applied column
     * @param valueA the initial value
     * @param valueB the applied value
     */
    public ColumnFunction(String columnA, String columnB, Object valueA, Object valueB)
    {
        this(columnA, columnB, valueA, valueB, (left, right) ->
        {
            double dLeft = NumberUtilities.parseDouble(left, 0.0);
            double dRight = NumberUtilities.parseDouble(right, 0.0);

            return Double.valueOf(dLeft + dRight);
        });
    }

    /**
     * Retrieves myColumns.
     *
     * @return mycolumns
     */
    public String[] getColumns()
    {
        return myColumns;
    }

    @SuppressWarnings("boxing")
    @Override
    public String toString()
    {
        return myFunction.apply(myValues[0], myValues[1]).toString();
    };
}
