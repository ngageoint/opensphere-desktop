package io.opensphere.analysis.table.functions;

import javax.swing.JTable;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.lang.NumberUtilities;

/** Representation of Table Selection Maximum. */
public class Max extends StatusBarFunction
{
    /**
     * Constructs a Max function.
     *
     * @param toolbox The toolbox through which application state is accessed.
     */
    public Max(Toolbox toolbox)
    {
        super(toolbox, "%-10.3f", "Maximum Value");
    }

    /**
     * Determines the maximum value of the selected cells in a given table.
     *
     * @override
     * @return the maximum value, of 0.0 if nothing is selected
     */
    @SuppressWarnings("boxing")
    @Override
    public Number execute(JTable table)
    {
        double max = 0.0;

        int[] rows = table.getSelectedRows();
        int[] cols = table.getSelectedColumns();
        for (int r : rows)
        {
            for (int c : cols)
            {
                max = Math.max(max, NumberUtilities.parseDouble(table.getValueAt(r, c), 0.0));
            }
        }

        return max;
    }
}
