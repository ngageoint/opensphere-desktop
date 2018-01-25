package io.opensphere.csvcommon.ui.columndefinition.model;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import io.opensphere.core.util.collections.New;

/**
 * The table model for the Before After table.
 *
 */
public class BeforeAfterTableModel extends AbstractTableModel
{
    /**
     * The before column name's suffix string.
     */
    public static final String ourBeforeSuffix = " (Original)";

    /**
     * The after column name's suffix string.
     */
    public static final String ourAfterSuffix = " (Formatted)";

    /**
     * The serial version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The prefix for the column names, this will be the name of the selected
     * column from the column definition table.
     */
    private String myColumnNamePrefix;

    /**
     * The rows of data for the table.
     */
    private final List<BeforeAfterRow> myRows = New.list();

    /**
     * Adds rows to the table model.
     *
     * @param newRows The rows to add.
     */
    public void addRows(List<BeforeAfterRow> newRows)
    {
        myRows.addAll(newRows);

        if (!myRows.isEmpty())
        {
            fireTableRowsInserted(0, myRows.size() - 1);
        }
    }

    /**
     * Removes all current rows.
     */
    public void clear()
    {
        int lastIndex = myRows.size() - 1;

        myRows.clear();

        if (lastIndex >= 0)
        {
            fireTableRowsDeleted(0, lastIndex);
        }
    }

    @Override
    public int getColumnCount()
    {
        return 2;
    }

    @Override
    public String getColumnName(int column)
    {
        String suffix;

        if (column == 0)
        {
            suffix = ourBeforeSuffix;
        }
        else
        {
            suffix = ourAfterSuffix;
        }

        return myColumnNamePrefix + suffix;
    }

    /**
     * Gets the row at the specified row index.
     *
     * @param rowIndex The row index to get.
     * @return The row at the specified index.
     */
    public BeforeAfterRow getRow(int rowIndex)
    {
        return myRows.get(rowIndex);
    }

    @Override
    public int getRowCount()
    {
        return myRows.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        BeforeAfterRow row = myRows.get(rowIndex);

        String cellValue = null;

        if (columnIndex == 0)
        {
            cellValue = row.getBeforeValue();
        }
        else
        {
            cellValue = row.getAfterValue();
        }

        return cellValue;
    }

    /**
     * Sets the column name prefix.
     *
     * @param columnNamePrefix The column name prefix.
     */
    public void setColumnNamePrefix(String columnNamePrefix)
    {
        myColumnNamePrefix = columnNamePrefix;

        fireTableStructureChanged();
    }
}
