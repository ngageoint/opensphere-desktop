package io.opensphere.analysis.table.model;

import javax.swing.JTable;

/**
 * Spreadsheet-related utilities for table models.
 */
public class SpreadsheetUtil
{
    /**
     * Sums the values of the selected table cells.
     *
     * @param table the table
     * @return summation
     */
    public static double getSum(JTable table)
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
}
