package io.opensphere.analysis.table.model;

import javax.swing.JTable;

/**
 * Spreadsheet-related utilities for table models.
 */
public final class SpreadsheetUtilities
{
    /**
     * Constructor.
     */
    private SpreadsheetUtilities()
    {
    }

    /**
     * Sums the values of the selected table cells.
     *
     * @param table the table
     * @return summation
     */
    public static double getSelectedSum(JTable table)
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

        return sum;
    }

    /**
     * Function enumerable that defines string representations, value
     * formatting, and how to execute.
     */
    public static enum Function
    {
        /** The sum. */
        SUM("%-10.3f", "Sum")
        {
            @Override
            public String execute(JTable table)
            {
                return String.format(getLabel() + ": " + getFormatString(), Double.valueOf(getSelectedSum(table)));
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
        private Function(String formatString, String label)
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
}
