package io.opensphere.core.util.fx;

import java.util.function.Consumer;

import io.opensphere.core.util.Service;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Watches a {@link TableView} and resizes columns as necessary to accommodate
 * new values. This is a {@link Service} and so {@link #open()} must be called
 * for it to start listening to the table.
 * <p>
 * The listener on the table's items is a weak reference but the listener on the
 * table's skin property is strong, so the auto-sizer should be cleaned up when
 * the table is no longer used, even if the table's items are still referenced.
 * {@link #close()} may be used to explicitly clean it up.
 *
 * @param <T> The type of the items in the table.
 */
public class TableViewAutosizer<T> implements Service
{
    /** Weak reference to the listener that's added to the table. */
    private final ListChangeListener<T> myWeakListChangeListener;

    /** The table being sized. */
    private final TableView<T> myTable;

    /** Listener used to update the column widths when alerts are added. */
    private final ListChangeListener<T> myListChangeListener;

    /**
     * Listener for changes to the table skin, used to initially size the
     * columns.
     */
    private final ChangeListener<? super Skin<?>> mySkinListener = (v, o, n) -> getTable().getColumns()
            .forEach(c -> updatePrefWidth(c, 0, getTable().getItems().size()));

    /**
     * Get the table.
     *
     * @return The table.
     */
    public TableView<T> getTable()
    {
        return myTable;
    }

    /**
     * Constructor.
     *
     * @param table The table.
     */
    public TableViewAutosizer(TableView<T> table)
    {
        myTable = table;
        myListChangeListener = this::handleListChange;
        myWeakListChangeListener = new WeakListChangeListener<>(myListChangeListener);
    }

    @Override
    public void open()
    {
        myTable.getItems().addListener(myWeakListChangeListener);
        myTable.skinProperty().addListener(mySkinListener);
    }

    @Override
    public void close()
    {
        myTable.getItems().removeListener(myWeakListChangeListener);
        myTable.skinProperty().removeListener(mySkinListener);
    }

    /**
     * Update the preferred width of a column.
     *
     * @param col The column.
     * @param from The start index (inclusive).
     * @param to The end index (exclusive).
     */
    protected void updatePrefWidth(TableColumn<T, ?> col, int from, int to)
    {
        int width = (int)FXUtilities.getMaxTableCellWidth(col, from, to);
        col.setPrefWidth(Math.max(col.getPrefWidth(), width));
    }

    /**
     * Handle a change to the table's items.
     *
     * @param change The change.
     */
    protected void handleListChange(Change<? extends T> change)
    {
        while (change.next())
        {
            if (change.wasAdded())
            {
                myTable.getColumns().forEach(
                        (Consumer<? super TableColumn<T, ?>>)col -> updatePrefWidth(col, change.getFrom(), change.getTo()));
            }
        }
    }
}
