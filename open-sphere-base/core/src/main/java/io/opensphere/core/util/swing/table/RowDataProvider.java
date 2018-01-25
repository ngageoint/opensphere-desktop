package io.opensphere.core.util.swing.table;

import javax.swing.event.TableModelListener;

/**
 * A provider of row data objects.
 *
 * @param <T> the type of the data object
 */
public interface RowDataProvider<T>
{
    /**
     * Gets the data object for the given row.
     *
     * @param rowIndex the row index
     * @return the data
     */
    T getData(int rowIndex);

    /**
     * Returns the number of rows in the model.
     *
     * @return the number of rows in the model
     */
    int getRowCount();

    /**
     * Adds a listener to the list that is notified each time a change to the
     * data model occurs.
     *
     * @param l the TableModelListener
     */
    void addTableModelListener(TableModelListener l);

    /**
     * Removes a listener from the list that is notified each time a change to
     * the data model occurs.
     *
     * @param l the TableModelListener
     */
    void removeTableModelListener(TableModelListener l);
}
