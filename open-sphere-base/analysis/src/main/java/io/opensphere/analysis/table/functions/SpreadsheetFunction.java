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
     * Initializes enumerable.
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
     * Format String Getter.
     *
     * @return the format string
     */
    public String getFormatString()
    {
        return myLabel + ": " + myFormatString;
    }

    /**
     * Executes the function.
     *
     * @param table the table to execute the function on
     * @return the resulting numeric function execution
     */
    public abstract Number execute(JTable table);

    /** Representation of Table Cell Summation. */
    public static class Sum extends SpreadsheetFunction
    {
        /** Constructs a Sum function. */
        public Sum()
        {
            super("%-10.3f", "Sum");
        }

        /**
         * Sums the selected cells in the given table.
         *
         * @override
         */
        @Override
        public Number execute(JTable table)
        {
            double sum = 0.0;

            int[] rows = table.getSelectedRows();
            int[] cols = table.getSelectedColumns();
            for (int r : rows)
            {
                for (int c : cols)
                {
                    Object value = table.getValueAt(r, c);

                    try
                    {
                        sum += Double.valueOf(value.toString()).doubleValue();
                    }
                    catch (NumberFormatException | NullPointerException e)
                    {
                        // Ignore non-numeric values.
                        continue;
                    }
                }
            }

            return Double.valueOf(sum);
        }
    }
}
