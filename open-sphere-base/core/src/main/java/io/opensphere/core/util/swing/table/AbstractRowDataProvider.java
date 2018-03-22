package io.opensphere.core.util.swing.table;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import io.opensphere.core.util.AbstractChangeSupport;
import io.opensphere.core.util.StrongChangeSupport;

/**
 * Abstract RowDataProvider that provides event/listener support.
 *
 * @param <T> the type of the data object
 */
public abstract class AbstractRowDataProvider<T> implements RowDataProvider<T>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AbstractRowDataProvider.class);

    /** The change support. */
    private final transient AbstractChangeSupport<TableModelListener> myChangeSupport = new StrongChangeSupport<>();

    /** The table model. */
    private final TableModel myModel;

    /**
     * Constructor.
     *
     * @param model the table model
     */
    public AbstractRowDataProvider(TableModel model)
    {
        myModel = model;
    }

    /**
     * Adds a listener to the list that is notified each time a change to the
     * data model occurs.
     *
     * @param l the TableModelListener
     */
    @Override
    public void addTableModelListener(TableModelListener l)
    {
        myChangeSupport.addListener(l);
    }

    /**
     * Removes a listener from the list that is notified each time a change to
     * the data model occurs.
     *
     * @param l the TableModelListener
     */
    @Override
    public void removeTableModelListener(TableModelListener l)
    {
        myChangeSupport.removeListener(l);
    }

    /**
     * Notifies all listeners that all cell values in the table's rows may have
     * changed. The number of rows may also have changed and the
     * <code>JTable</code> should redraw the table from scratch. The structure
     * of the table (as in the order of the columns) is assumed to be the same.
     *
     * @see TableModelEvent
     * @see EventListenerList
     * @see javax.swing.JTable#tableChanged(TableModelEvent)
     */
    public void fireTableDataChanged()
    {
        fireTableChanged(new TableModelEvent(myModel));
    }

    /**
     * Notifies all listeners that the table's structure has changed. The number
     * of columns in the table, and the names and types of the new columns may
     * be different from the previous state. If the <code>JTable</code> receives
     * this event and its <code>autoCreateColumnsFromModel</code> flag is set it
     * discards any table columns that it had and reallocates default columns in
     * the order they appear in the model. This is the same as calling
     * <code>setModel(TableModel)</code> on the <code>JTable</code>.
     *
     * @see TableModelEvent
     * @see EventListenerList
     */
    public void fireTableStructureChanged()
    {
        fireTableChanged(new TableModelEvent(myModel, TableModelEvent.HEADER_ROW));
    }

    /**
     * Notifies all listeners that rows in the range
     * <code>[firstRow, lastRow]</code>, inclusive, have been inserted.
     *
     * @param firstRow the first row
     * @param lastRow the last row
     *
     * @see TableModelEvent
     * @see EventListenerList
     *
     */
    public void fireTableRowsInserted(int firstRow, int lastRow)
    {
        fireTableChanged(new TableModelEvent(myModel, firstRow, lastRow, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
    }

    /**
     * Notifies all listeners that rows in the range
     * <code>[firstRow, lastRow]</code>, inclusive, have been updated.
     *
     * @param firstRow the first row
     * @param lastRow the last row
     *
     * @see TableModelEvent
     * @see EventListenerList
     */
    public void fireTableRowsUpdated(int firstRow, int lastRow)
    {
        fireTableChanged(new TableModelEvent(myModel, firstRow, lastRow, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
    }

    /**
     * Notifies all listeners that rows in the range
     * <code>[firstRow, lastRow]</code>, inclusive, have been deleted.
     *
     * @param firstRow the first row
     * @param lastRow the last row
     *
     * @see TableModelEvent
     * @see EventListenerList
     */
    public void fireTableRowsDeleted(int firstRow, int lastRow)
    {
        fireTableChanged(new TableModelEvent(myModel, firstRow, lastRow, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
    }

    /**
     * Notifies all listeners that the value of the cell at
     * <code>[row, column]</code> has been updated.
     *
     * @param row row of cell which has been updated
     * @param column column of cell which has been updated
     * @see TableModelEvent
     * @see EventListenerList
     */
    public void fireTableCellUpdated(int row, int column)
    {
        fireTableChanged(new TableModelEvent(myModel, row, row, column));
    }

    /**
     * Forwards the given notification event to all
     * <code>TableModelListeners</code> that registered themselves as listeners
     * for this table model.
     *
     * @param event the event to be forwarded
     *
     * @see #addTableModelListener
     * @see TableModelEvent
     * @see EventListenerList
     */
    public void fireTableChanged(final TableModelEvent event)
    {
        myChangeSupport.notifyListeners(listener ->
        {
            try
            {
                listener.tableChanged(event);
            }
            catch (RuntimeException e)
            {
                LOGGER.warn(e);
            }
        });
    }

    /**
     * Gets the model.
     *
     * @return the model
     */
    protected TableModel getModel()
    {
        return myModel;
    }
}
