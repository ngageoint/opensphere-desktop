package io.opensphere.analysis.table.model;

import javax.swing.JTable;

/**
 * Function enumerable that defines string representations, value formatting,
 * and how to execute.
 */
public enum SpreadsheetFunction
{
    /** The sum. */
    SUM("%-10.3f", "Sum")
    {
        @Override
        public String execute(JTable table)
        {
            return String.format(getLabel() + ": " + getFormatString(),
                    Double.valueOf(SpreadsheetUtilities.getSelectedSum(table)));
        }
    };

    /** The format string. */
    private String myFormatString;

    /** The function label/name. */
    private String myLabel;

    /**
     * Initializes enumerable.
     *
     * @param formatString the function result format string
     * @param label the function name
     */
    private SpreadsheetFunction(String formatString, String label)
    {
        myFormatString = formatString;
        myLabel = label;
    }

    /**
     * Format String Getter.
     *
     * @return the format string
     */
    public String getFormatString()
    {
        return myFormatString;
    }

    /**
     * Label Getter.
     *
     * @return the label
     */
    public String getLabel()
    {
        return myLabel;
    }

    /**
     * Executes the function.
     *
     * @param table the table to execute the function on
     * @return the formatted function execution
     */
    public abstract String execute(JTable table);
}
