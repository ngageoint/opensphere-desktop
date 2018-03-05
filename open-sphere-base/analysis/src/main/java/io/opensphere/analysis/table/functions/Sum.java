package io.opensphere.analysis.table.functions;

import javax.swing.JTable;

import io.opensphere.core.util.lang.NumberUtilities;

/** Representation of Table Cell Summation. */
public class Sum extends SpreadsheetFunction
{
    /** Constructs a Sum function. */
    public Sum()
    {
        super("%-10.3f", "Sum");
    }

    /**
     * Sums the selected cells in a given table.
     *
     * @override
     */
    @SuppressWarnings("boxing")
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
                sum += NumberUtilities.parseDouble(table.getValueAt(r, c), 0.0);
            }
        }

        return sum;
    }
}