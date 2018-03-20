package io.opensphere.analysis.table.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JTable;

import io.opensphere.core.util.lang.NumberUtilities;

/**
 * Representation of Table Selection Minimum. Does not include non-numeric
 * cells.
 */
public class Min extends SpreadsheetFunction
{
    /** Constructs a Min function. */
    public Min()
    {
        super("%-10.3f", "Minimum Value");
    }

    /**
     * Determines the minimum value of the selected cells in a given table.
     *
     * @override
     * @return the minimum value, or 0.0 if nothing is selected
     */
    @SuppressWarnings("boxing")
    @Override
    public Number execute(JTable table)
    {
        int[] rows = table.getSelectedRows();
        int[] cols = table.getSelectedColumns();

        List<Object> selectedValues = new ArrayList<>();
        for (int r : rows)
        {
            for (int c : cols)
            {
                selectedValues.add(table.getValueAt(r, c));
            }
        }

        Optional<Double> result = selectedValues.stream().filter(NumberUtilities::isNumber)
                .map((d) -> NumberUtilities.parseDouble(d, 0.0)).reduce((a, b) -> Double.min(a, b));

        return result.orElse(0.0);
    }
}
