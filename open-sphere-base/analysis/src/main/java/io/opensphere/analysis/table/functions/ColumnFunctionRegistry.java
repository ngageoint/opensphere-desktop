package io.opensphere.analysis.table.functions;

import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;

/**
 * A registry use to register and instantiate column functions.
 */
public class ColumnFunctionRegistry
{
    /** Function values. */
    private final List<ColumnFunction> myFunctionValues;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox through which application state is accessed.
     */
    public ColumnFunctionRegistry(Toolbox toolbox)
    {
        myFunctionValues = New.list();

        myFunctionValues.add(new StringJoin());
        myFunctionValues.add(new Sum());
        myFunctionValues.add(new Difference());
        myFunctionValues.add(new Product());
        myFunctionValues.add(new Quotient());
        myFunctionValues.add(new Average());
    }

    /**
     * Adds the supplied function to the registry.
     *
     * @param function the function to add to the registry.
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
