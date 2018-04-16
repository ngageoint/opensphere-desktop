package io.opensphere.analysis.table.functions;

import java.util.List;

import com.bitsys.mist.analysis.table.functions.column.NumericDeltaFunction;
import com.bitsys.mist.analysis.table.functions.column.TimeDeltaFunction;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;

/**
 * A factory use to instantiate column functions.
 */
public class ColumnFunctionFactory
{
    /** Function values. */
    private final List<ColumnFunction> myFunctionValues;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox through which application state is accessed.
     */
    public ColumnFunctionFactory(Toolbox toolbox)
    {
        myFunctionValues = New.list();

        myFunctionValues.add(new NumericDeltaFunction());
        myFunctionValues.add(new TimeDeltaFunction());
    }

    /**
     * Adds the supplied function to the factory.
     *
     * @param function the function to add to the factory.
     */
    public void addFunction(ColumnFunction function)
    {
        myFunctionValues.add(function);
    }

    /**
     * Retrieves all standard Function values.
     *
     * @return array of defined values
     */
    public List<ColumnFunction> getFunctions()
    {
        return myFunctionValues;
    }
}
