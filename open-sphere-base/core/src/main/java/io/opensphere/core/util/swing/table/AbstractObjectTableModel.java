package io.opensphere.core.util.swing.table;

import java.util.List;

import net.jcip.annotations.NotThreadSafe;
import javax.swing.SwingUtilities;

import io.opensphere.core.util.cache.SimpleCache;
import io.opensphere.core.util.collections.FixedSizeBufferMap;

/**
 * A table model that uses a list to store the data objects, and a cache to
 * store the row values.
 *
 * @param <T> the generic type of the data objects
 */
@NotThreadSafe
public abstract class AbstractObjectTableModel<T> extends GenericObjectTableModel<T>
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The row data provider. */
    private final transient ListRowDataProvider<T> myRowDataProvider;

    /** The row values provider. */
    private final transient RowValuesProvider<T> myRowValuesProvider;

    /** The row values provider cache. */
    private final transient SimpleCache<T, List<?>> myCache;

    /**
     * Constructor.
     */
    public AbstractObjectTableModel()
    {
        super();

        myRowDataProvider = new ListRowDataProvider<>(this);
        setRowDataProvider(myRowDataProvider);

        myCache = new SimpleCache<>(new FixedSizeBufferMap<T, List<?>>(100), dataObject -> getRowValues(dataObject));
        myRowValuesProvider = (rowIndex, dataObject) -> myCache.apply(dataObject);
        setRowValuesProvider(myRowValuesProvider);
    }

    /**
     * Adds to the underlying data objects. This is a mutator.
     *
     * @param dataObjects The data objects
     */
    public void addData(List<? extends T> dataObjects)
    {
        assert SwingUtilities.isEventDispatchThread();
        if (!dataObjects.isEmpty())
        {
            int firstRow = myRowDataProvider.getRowCount();
            myRowDataProvider.addData(dataObjects);
            fireTableRowsInserted(firstRow, myRowDataProvider.getRowCount() - 1);
        }
    }

    /**
     * Sets the underlying data objects. This is a mutator.
     *
     * @param dataObjects The data objects
     */
    public void setData(List<? extends T> dataObjects)
    {
        assert SwingUtilities.isEventDispatchThread();
        myRowDataProvider.clear();
        myCache.clear();
        myRowDataProvider.addData(dataObjects);
        fireTableDataChanged();
    }

    /**
     * Clear the table data. This is a mutator.
     */
    public void clear()
    {
        assert SwingUtilities.isEventDispatchThread();
        myRowDataProvider.clear();
        myCache.clear();
        fireTableDataChanged();
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public List<T> getData()
    {
        return myRowDataProvider.getData();
    }

    @Override
    public T getDataAt(int rowIndex)
    {
        return myRowDataProvider.getData(rowIndex);
    }

    /**
     * Returns the row index of the data object, or -1.
     *
     * @param dataObject The data object
     * @return The index
     */
    public int rowIndexOf(T dataObject)
    {
        return myRowDataProvider.rowIndexOf(dataObject);
    }

    /**
     * Gets the row values for the given data object.
     *
     * @param dataObject The data object
     * @return the row values
     */
    protected abstract List<?> getRowValues(T dataObject);
}
