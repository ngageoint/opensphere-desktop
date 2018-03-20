package io.opensphere.analysis.table.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JTable;

import io.opensphere.core.util.lang.NumberUtilities;

/**
 * Representation of Table Selection Mean. Does not include non-numeric cells.
 */
public class Mean extends SpreadsheetFunction
{
    /** Constructs a Mean function. */
    public Mean()
    {
        super("%-10.3f", "Mean Value");
    }

    /**
     * Determines the mean value of the selected cells in a given table.
     *
     * @override
     * @return the mean value, or NaN if nothing is selected
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
                .map((d) -> NumberUtilities.parseDouble(d, Double.NaN)).reduce((a, b) -> Double.sum(a, b));

        return result.map((d) -> d / selectedValues.size()).orElse(Double.NaN);
    }
}
