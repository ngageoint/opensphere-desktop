package io.opensphere.csvcommon.ui.columndefinition.model;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.collections.New;

/**
 * The table model for the Column Definition table.
 *
 */
public class ColumnDefinitionTableModel extends AbstractTableModel
{
    /**
     * The class types of our individual columns.
     */
    private static final Class<?>[] ourColumnClasses = new Class<?>[] { Boolean.class, String.class, String.class, String.class };

    /**
     * The column names for the table.
     */
    private static final String[] ourColumnNames = new String[] { "Import", "Column Name", "Type", "Format" };

    /**
     * The serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Indicates if the format column is editable.
     */
    private boolean myIsFormatEditable = true;

    /**
     * The rows of column definitions.
     */
    private final List<ColumnDefinitionRow> myRows = New.list();

    /**
     * Adds the rows to the model.
     *
     * @param rows The rows to add to the table model.
     */
    public void addRows(List<ColumnDefinitionRow> rows)
    {
        myRows.addAll(rows);

        if (!myRows.isEmpty())
        {
            fireTableRowsInserted(0, myRows.size() - 1);
        }
    }

    /**
     * Removes all rows from the model.
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
    public Class<?> getColumnClass(int columnIndex)
    {
        return ourColumnClasses[columnIndex];
    }

    @Override
    public int getColumnCount()
    {
        return ourColumnNames.length;
    }

    @Override
    public String getColumnName(int column)
    {
        return ourColumnNames[column];
    }

    /**
     * Gets the row at the given row index.
     *
     * @param rowIndex The row index to retrieve.
     * @return The row at the given index.
     */
    public ColumnDefinitionRow getRow(int rowIndex)
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
        ColumnDefinitionRow row = myRows.get(rowIndex);

        Object value;

        if (columnIndex == 0)
        {
            value = Boolean.valueOf(row.isImport());
        }
        else if (columnIndex == 1)
        {
            value = row.getColumnName();
        }
        else if (columnIndex == 2)
        {
            value = row.getDataType();
        }
        else
        {
            value = row.getFormat();
        }

        return value;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        boolean isEditable = true;

        if (columnIndex == 3)
        {
            isEditable = myIsFormatEditable;
            if (isEditable)
            {
                ColumnDefinitionRow row = myRows.get(rowIndex);
                isEditable = StringUtils.isNotEmpty(row.getDataType());
            }
        }

        return isEditable;
    }

    /**
     * Get if all the columns are set to import.
     *
     * @return {@code true} if all the columns are set to import.
     */
    public boolean isImportAllColumns()
    {
        for (ColumnDefinitionRow row : myRows)
        {
            if (!row.isImport())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets if the format column is editable or not.
     *
     * @param isEditable True if editable, false otherwise.
     */
    public void setFormatEditable(boolean isEditable)
    {
        myIsFormatEditable = isEditable;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        ColumnDefinitionRow row = myRows.get(rowIndex);

        if (columnIndex == 0)
        {
            row.setIsImport((Boolean)aValue);
        }
        else if (columnIndex == 1 && StringUtils.isNotEmpty((String)aValue))
        {
            String newName = (String)aValue;

            if (isNameUnique(newName))
            {
                row.setColumnName((String)aValue);
            }
        }
        else if (columnIndex == 2)
        {
            row.setDataType((String)aValue);
        }
        else
        {
            row.setFormat((String)aValue);
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    /**
     * Checks to see if the name is unique.
     *
     * @param newName The new name to check.
     * @return True if the name is unique for all columns, false otherwise.
     */
    private boolean isNameUnique(String newName)
    {
        boolean isUnique = true;
        for (ColumnDefinitionRow row : myRows)
        {
            if (row.getColumnName().equals(newName))
            {
                isUnique = false;
                break;
            }
        }

        return isUnique;
    }
}
