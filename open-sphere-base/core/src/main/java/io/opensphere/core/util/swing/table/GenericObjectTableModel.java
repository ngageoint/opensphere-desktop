package io.opensphere.core.util.swing.table;

import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * A flexible table model that uses pluggable providers for conversion between
 * row index and data object, and data object and row values.
 *
 * @param <T> the generic type of the data objects
 */
@NotThreadSafe
public class GenericObjectTableModel<T> extends AbstractColumnTableModel
{
    /** The serialVersionUID constant. */
    private static final long serialVersionUID = 1L;

    /** The row data provider. */
    private RowDataProvider<T> myRowDataProvider;

    /** The row values provider. */
    private RowValuesProvider<T> myRowValuesProvider;

    /**
     * Sets the rowDataProvider.
     *
     * @param rowDataProvider the rowDataProvider
     */
    public void setRowDataProvider(RowDataProvider<T> rowDataProvider)
    {
        myRowDataProvider = rowDataProvider;
    }

    /**
     * Sets the rowValuesProvider.
     *
     * @param rowValuesProvider the rowValuesProvider
     */
    public void setRowValuesProvider(RowValuesProvider<T> rowValuesProvider)
    {
        myRowValuesProvider = rowValuesProvider;
    }

    /**
     * Gets the rowDataProvider.
     *
     * @return the rowDataProvider
     */
    public RowDataProvider<T> getRowDataProvider()
    {
        return myRowDataProvider;
    }

    /**
     * Gets the rowValuesProvider.
     *
     * @return the rowValuesProvider
     */
    public RowValuesProvider<T> getRowValuesProvider()
    {
        return myRowValuesProvider;
    }

    @Override
    public int getRowCount()
    {
        return myRowDataProvider.getRowCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        if (myRowDataProvider.getRowCount() <= rowIndex)
            return null;
        T data = myRowDataProvider.getData(rowIndex);
        if (data == null)
            return null;
        List<?> values = myRowValuesProvider.getValues(rowIndex, data);
        if (values.size() <= columnIndex)
            return null;
        return values.get(columnIndex);
    }

    /**
     * Gets the data object at the given row index.
     *
     * @param rowIndex the row index
     * @return the data object
     */
    public T getDataAt(int rowIndex)
    {
        return myRowDataProvider.getData(rowIndex);
    }
}
