package io.opensphere.core.util.swing.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.table.TableModel;

/**
 * A provider of row data objects using a list of objects.
 *
 * @param <T> the type of the data objects
 */
public class ListRowDataProvider<T> extends AbstractRowDataProvider<T>
{
    /** The data. */
    @SuppressWarnings("PMD.LooseCoupling")
    private final ArrayList<T> myData = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param model the table model
     */
    public ListRowDataProvider(TableModel model)
    {
        super(model);
    }

    @Override
    public T getData(int rowIndex)
    {
        return myData.get(rowIndex);
    }

    @Override
    public int getRowCount()
    {
        return myData.size();
    }

    /**
     * Adds data.
     *
     * @param data the data to add
     */
    public void addData(Collection<? extends T> data)
    {
        myData.addAll(data);
//        fireTableRowsInserted(firstRow, lastRow);
    }

    /**
     * Clears the data.
     */
    public void clear()
    {
        int size = myData.size();
        myData.clear();
        if (size > 100)
        {
            myData.trimToSize();
        }
//        fireTableDataChanged();
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public List<T> getData()
    {
        return myData;
    }

    /**
     * Returns the row index of the data object, or -1.
     *
     * @param dataObject The data object
     * @return The index
     */
    public int rowIndexOf(T dataObject)
    {
        return myData.indexOf(dataObject);
    }
}
