package io.opensphere.analysis.table.functions;

import java.util.Set;

import javax.swing.JTable;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;

/**
 * Function class that defines string representations, value formatting, and how
 * to execute. These functions are used on the status bar of analyze tools.
 */
public abstract class StatusBarFunction
{
    /** The format string. */
    private volatile String myFormatString;

    /** The function label/name. */
    protected final String myLabel;

    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /**
     * The set of datatypes supported by the function. If the set is empty, any
     * datatype is supported.
     */
    private Set<Class<?>> mySupportedDataTypes;

    /**
     * Constructs a Spreadsheet Function.
     *
     * @param toolbox The toolbox through which application state is accessed.
     * @param formatString the function result format string
     * @param label the function name
     */
    public StatusBarFunction(Toolbox toolbox, String formatString, String label)
    {
        myToolbox = toolbox;
        myFormatString = formatString;
        myLabel = label;
        mySupportedDataTypes = New.set();
    }

    /**
     * Gets the value of the toolbox ({@link #myToolbox}) field.
     *
     * @return the value stored in the {@link #myToolbox} field.
     */
    protected Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Format String getter.
     *
     * @return myFormatString
     */
    public String getFormatString()
    {
        return myFormatString;
    }

    /**
     * Sets the value of the formatString ({@link #myFormatString}) field.
     *
     * @param formatString the value to store in the {@link #myFormatString}
     *            field.
     */
    protected void setFormatString(String formatString)
    {
        myFormatString = formatString;
    }

    /**
     * Label getter.
     *
     * @return myLabel
     */
    public String getLabel()
    {
        return myLabel;
    }

    /**
     * Gets the value of the supportedDataTypes ({@link #mySupportedDataTypes})
     * field.
     *
     * @return the value stored in the {@link #mySupportedDataTypes} field.
     */
    protected Set<Class<?>> getSupportedDataTypes()
    {
        return mySupportedDataTypes;
    }

    /**
     * Tests to determine if the supplied datatype is supported by the function.
     *
     * @param type the type to test.
     * @return true if the datatype is supported by the function, false
     *         otherwise.
     */
    public boolean isSupported(Class<?> type)
    {
        return mySupportedDataTypes.isEmpty() || mySupportedDataTypes.contains(type);
    }

    /**
     * Executes the function.
     *
     * @param table the table to execute the function on
     * @return the resulting numeric function execution
     */
    public abstract Number execute(JTable table);

    /**
     * Gets the text used when the function is not applicable.
     *
     * @return the text used when the function is not applicable.
     */
    public String getNotApplicableText()
    {
        return "N/A";
    }
}
