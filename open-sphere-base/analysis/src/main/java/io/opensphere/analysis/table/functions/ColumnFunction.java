package io.opensphere.analysis.table.functions;

import java.util.function.BiFunction;

/**
 * Representation of a multi-column function.
 */
public class ColumnFunction
{
    /** Array of column values. */
    private final Object[] myValues = new Object[2];

    /** The function name. */
    private final String myName;

    /** The function to apply. */
    private final BiFunction<Object, Object, Object> myFunction;

    /**
     * Constructs a ColumnFunction.
     *
     * @param name the name of the function
     * @param function the function to apply each value to
     */
    public ColumnFunction(String name, BiFunction<Object, Object, Object> function)
    {
        myName = name;
        myFunction = function;
    }

    /**
     * Sets the values that the function will utilize.
     *
     * @param value the value to set
     * @param index the index to set
     */
    public void setValue(Object value, int index)
    {
        if (index >= 0 && index < myValues.length)
        {
            myValues[index] = value;
        }
    }

    /**
     * Returns the result of the function.
     *
     * @return the function result after applying to each value
     */
    public Object getValue()
    {
        return myFunction.apply(myValues[0], myValues[1]);
    }

    @Override
    public String toString()
    {
        return myName;
    };

    /**
     * Builds and applies the function with whatever values it requires.
     *
     * @param value1
     * @param value2
     * @return a readable ColumnFunction
     */
    public ColumnFunction build(Object value1, Object value2)
    {
        ColumnFunction result = new ColumnFunction(myName, myFunction);
        result.setValue(value1, 0);
        result.setValue(value2, 1);

        return result;
    }
}
