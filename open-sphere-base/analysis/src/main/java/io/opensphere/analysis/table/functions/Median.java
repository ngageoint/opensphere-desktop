package io.opensphere.analysis.table.functions;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

import io.opensphere.core.util.lang.NumberUtilities;

/**
 * Representation of Table Selection Median. Does not include non-numeric cells.
 */
public class Median extends SpreadsheetFunction
{
    /** Constructs a Median function. */
    public Median()
    {
        super("%-10.3f", "Median Value");
    }

    /**
     * Determines the median value of the selected cells in a given table.
     *
     * @override
     * @return the median value, or NaN if nothing is selected
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

        Double[] result = selectedValues.stream().filter(NumberUtilities::isNumber).map((d) -> ((Number)d).doubleValue()).sorted()
                .toArray(Double[]::new);

        int len = result.length;
        if (len == 0)
        {
            return Double.NaN;
        }
        else if (len % 2 == 0)
        {
            return (result[len / 2] + result[len / 2 - 1]) / 2;
        }
        else
        {
            return result[len / 2];
        }
    }
}