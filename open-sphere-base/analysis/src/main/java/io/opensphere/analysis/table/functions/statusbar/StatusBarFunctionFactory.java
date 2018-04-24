package io.opensphere.analysis.table.functions.statusbar;

import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;

/**
 * Spreadsheet-related utilities for table models.
 */
public final class StatusBarFunctionFactory
{
    /** Function values. */
    private final List<StatusBarFunction> myFunctionValues;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox through which application state is accessed.
     */
    public StatusBarFunctionFactory(Toolbox toolbox)
    {
        myFunctionValues = New.list();

        myFunctionValues.add(new Sum(toolbox));
        myFunctionValues.add(new Min(toolbox));
        myFunctionValues.add(new Max(toolbox));
        myFunctionValues.add(new Median(toolbox));
        myFunctionValues.add(new Mean(toolbox));
    }

    /**
     * Adds the supplied function to the factory.
     *
     * @param function the function to add to the factory.
     */
    public void addFunction(StatusBarFunction function)
    {
        myFunctionValues.add(function);
    }

    /**
     * Retrieves all standard Function values.
     *
     * @return array of defined values
     */
    public List<StatusBarFunction> getFunctions()
    {
        return myFunctionValues;
    }
}
