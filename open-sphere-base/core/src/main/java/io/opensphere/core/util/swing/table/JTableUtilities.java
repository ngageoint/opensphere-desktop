package io.opensphere.core.util.swing.table;

import java.util.List;

import javax.swing.JTable;

import io.opensphere.core.util.collections.New;

/**
 * Utilities for {@link JTable}s.
 */
public final class JTableUtilities
{
    /**
     * Get the selected rows of a table in model coordinates.
     *
     * @param table The table.
     * @return The model row indices.
     */
    public static int[] getSelectedModelRows(JTable table)
    {
        return convertRowIndicesToModel(table, table.getSelectedRows());
    }

    /**
     * Convert an array of view row indices to model row indices.
     *
     * @param table The table.
     * @param viewRows The view row indices.
     * @return The model row indices.
     */
    public static int[] convertRowIndicesToModel(JTable table, int[] viewRows)
    {
        int[] modelRows = new int[viewRows.length];
        for (int index = 0; index < viewRows.length; ++index)
        {
            modelRows[index] = table.convertRowIndexToModel(viewRows[index]);
        }
        return modelRows;
    }

    /**
     * Gets the displayed column names in the table.
     *
     * @param table The table.
     * @return the column names
     */
    public static List<String> getColumnNames(JTable table)
    {
        int columnCount = table.getColumnCount();
        List<String> columnNames = New.list(columnCount);
        for (int col = 0; col < columnCount; col++)
        {
            columnNames.add(table.getColumnName(col));
        }
        return columnNames;
    }

    /** Disallow instantiation. */
    private JTableUtilities()
    {
    }
}
