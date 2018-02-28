package io.opensphere.analysis.table.renderers;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 */
public class SpreadsheetListener implements ListSelectionListener
{
    /** The table. */
    private final JTable myTable;

    /** The sum of selected numeric cells. */
    private double mySum = 0.0;

    /**
     * Initializes the SpreadsheetListener.
     *
     * @param table
     */
    public SpreadsheetListener(JTable table)
    {
        myTable = table;
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        setSum(myTable);
    }

    /**
     * Sets the sum. Sums the values of the selected table cells.
     *
     * @param table the table
     */
    private void setSum(JTable table)
    {
        mySum = 0.0;

        int[] rows = table.getSelectedRows();
        int[] cols = table.getSelectedColumns();
        for (int r : rows)
        {
            for (int c : cols)
            {
                Object value = table.getValueAt(r, c);
                if (!(value instanceof Number))
                {
                    continue;
                }

                mySum += ((Number)value).doubleValue();
            }
        }
    }

    /**
     * @return the current sum
     */
    public double getSum()
    {
        return mySum;
    }
}
