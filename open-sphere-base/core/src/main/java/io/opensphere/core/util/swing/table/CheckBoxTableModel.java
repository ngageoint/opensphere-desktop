package io.opensphere.core.util.swing.table;

import java.util.Collection;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * A simple table model with two columns: booleans in the first column and
 * Strings in the second column.
 */
public final class CheckBoxTableModel extends AbstractTableModel
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The booleans in the model. */
    private final List<Boolean> myBooleans;

    /** The column headers. */
    private final List<? extends String> myHeaders;

    /** The values in the model. */
    private final List<? extends String> myValues;

    /**
     * Constructor.
     *
     * @param checkBoxHeader The header for the CheckBox column.
     * @param valueHeader The header for the value column.
     * @param initialCheckBoxState The initial CheckBox state.
     * @param values The values to put in the second column.
     */
    public CheckBoxTableModel(String checkBoxHeader, String valueHeader, Boolean initialCheckBoxState,
            Collection<? extends String> values)
    {
        myHeaders = New.unmodifiableList(Utilities.checkNull(checkBoxHeader, "checkBoxHeader"),
                Utilities.checkNull(valueHeader, "valueHeader"));
        myValues = New.unmodifiableList(Utilities.checkNull(values, "values"));
        myBooleans = New.list(values.size());
        for (int index = 0; index < myValues.size(); ++index)
        {
            myBooleans.add(Utilities.checkNull(initialCheckBoxState, "initialCheckBoxState"));
        }
    }

    /**
     * Get a list of the checked values.
     *
     * @return The checked values.
     */
    public List<String> getCheckedValues()
    {
        List<String> results = New.list(myValues.size());
        for (int index = 0; index < myValues.size(); ++index)
        {
            if (myBooleans.get(index).booleanValue())
            {
                results.add(myValues.get(index));
            }
        }
        return results;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        if (columnIndex == 0)
        {
            return Boolean.class;
        }
        else if (columnIndex == 1)
        {
            return Object.class;
        }
        else
        {
            throw new IllegalArgumentException("Column index out of bounds: " + columnIndex);
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
        if (column < 0 || column > 1)
        {
            throw new IllegalArgumentException("Column index out of bounds: " + column);
        }
        return myHeaders.get(column);
    }

    @Override
    public int getRowCount()
    {
        return myValues.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        checkRowIndex(rowIndex);
        if (columnIndex == 0)
        {
            return myBooleans.get(rowIndex);
        }
        else if (columnIndex == 1)
        {
            return myValues.get(rowIndex);
        }
        else
        {
            throw new IllegalArgumentException("Column index out of bounds: " + columnIndex);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return columnIndex == 0;
    }

    /**
     * Set the checked values.
     *
     * @param values The checked values.
     */
    public void setCheckedValues(Collection<? extends String> values)
    {
        Utilities.checkNull(values, "values");
        for (int index = 0; index < myValues.size(); ++index)
        {
            myBooleans.set(index, Boolean.valueOf(values.contains(myValues.get(index))));
        }
        fireTableDataChanged();
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        if (columnIndex == 0)
        {
            checkRowIndex(rowIndex);
            myBooleans.set(rowIndex, (Boolean)aValue);
            fireTableCellUpdated(rowIndex, columnIndex);
        }
        else
        {
            throw new IllegalArgumentException("Only column 0 is editable.");
        }
    }

    /**
     * Check for a valid row index.
     *
     * @param rowIndex The row index.
     * @throws IllegalArgumentException If the row index is not valid.
     */
    private void checkRowIndex(int rowIndex) throws IllegalArgumentException
    {
        if (rowIndex < 0 || rowIndex > myBooleans.size() - 1)
        {
            throw new IllegalArgumentException("rowIndex must be >= 0 and <= " + (myBooleans.size() - 1));
        }
    }
}
