package io.opensphere.analysis.table.functions;

import javax.swing.JTable;

/**
 * Function class that defines string representations, value formatting, and how
 * to execute.
 */
public abstract class SpreadsheetFunction
{
    /** The format string. */
    protected final String myFormatString;

    /** The function label/name. */
    protected final String myLabel;

    /**
     * Constructs a Spreadsheet Function.
     *
     * @param formatString the function result format string
     * @param label the function name
     */
    public SpreadsheetFunction(String formatString, String label)
    {
        myFormatString = formatString;
        myLabel = label;
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
     * Label getter.
     *
     * @return myLabel
     */
    public String getLabel()
    {
        return myLabel;
    }

    /**
     * Executes the function.
     *
     * @param table the table to execute the function on
     * @return the resulting numeric function execution
     */
    public abstract Number execute(JTable table);
}
