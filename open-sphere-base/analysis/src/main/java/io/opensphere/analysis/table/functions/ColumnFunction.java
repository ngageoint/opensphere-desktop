package io.opensphere.analysis.table.functions;

import java.util.Objects;
import java.util.function.Function;

/**
 * Representation of a multi-column function. Constructors are used to generate
 * templates, which are then instantiated for use via {@link #build(int...)}.
 */
public class ColumnFunction
{
    /** Array of column indices. Operations are executed in order. */
    private final int[] myColumns;

    /** The function name. */
    private final String myName;

    /** The function to apply. */
    private final Function<Object[], Object> myFunction;

    private String myValueAsString;

    /**
     * Constructs a ColumnFunction against any number of columns
     *
     * @param name the name of the function
     * @param columnCount the number of columns
     * @param function the function to use
     */
    protected ColumnFunction(String name, int columnCount, Function<Object[], Object> function)
    {
        myName = name;
        myColumns = new int[columnCount];
        myFunction = function;
    }

    /**
     * Sets the values that the function will utilize.
     *
     * @param index the index of the array
     * @param column the index of the column
     */
    public void setColumn(int index, int column)
    {
        myColumns[index] = column;
    }

    /**
     * Retrieves the value of {@link #myColumns}.
     *
     * @return the array of column indices
     */
    public int[] getColumns()
    {
        return myColumns;
    }

    /**
     * Returns the result of the function.
     *
     * @param values the values to calculate against
     * @return the function result after applying to each value
     */
    public String getValue(Object... values)
    {
        myValueAsString = Objects.toString(myFunction.apply(values));
        return myValueAsString;
    }

    @Override
    public String toString()
    {
        String returnVal = myName;
        if (myValueAsString != null)
        {
            returnVal = myValueAsString;
        }

        return returnVal;
    }

    /**
     * Builds and applies the function with whatever values it requires.
     *
     * @param columns the columns this function applies to
     * @return a readable ColumnFunction
     */
    public ColumnFunction build(int... columns)
    {
        ColumnFunction result = new ColumnFunction(myName, columns.length, myFunction);
        for (int i = 0; i < columns.length; i++)
        {
            result.setColumn(i, columns[i]);
        }

        return result;
    }
}
