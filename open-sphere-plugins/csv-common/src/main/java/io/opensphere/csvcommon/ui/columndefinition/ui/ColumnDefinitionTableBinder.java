package io.opensphere.csvcommon.ui.columndefinition.ui;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;

/**
 * Binds the ColumnDefinitionTable to the model and keeps the model and table in
 * sync with the currently selected column definition row.
 */
public class ColumnDefinitionTableBinder implements Observer, ListSelectionListener
{
    /**
     * The model to listen for selection events and to keep selections in sync
     * with the table.
     */
    private final ColumnDefinitionModel myModel;

    /**
     * The column definition table to listen for selection events and to keep
     * selections in sync with the model.
     */
    private final JTable myTable;

    /**
     * Constructs a new binder that keeps selections in sync with the model and
     * the table.
     *
     * @param definitionTable The table to keep in sync with.
     * @param model The model to keep in synch with.
     */
    public ColumnDefinitionTableBinder(JTable definitionTable, ColumnDefinitionModel model)
    {
        myTable = definitionTable;
        myModel = model;
        myTable.getSelectionModel().addListSelectionListener(this);
        myModel.addObserver(this);
    }

    /**
     * Removes its self as a listener.
     */
    public void close()
    {
        myModel.deleteObserver(this);
        myTable.getSelectionModel().removeListSelectionListener(this);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (ColumnDefinitionModel.SELECTED_DEFINITION_PROPERTY.equals(arg))
        {
            int currentSelection = myTable.getSelectionModel().getAnchorSelectionIndex();
            ColumnDefinitionRow selectedRow = myModel.getSelectedDefinition();
            if (selectedRow != null && (currentSelection < 0 || selectedRow.getColumnId() != currentSelection))
            {
                myTable.getSelectionModel().setSelectionInterval(selectedRow.getColumnId(), selectedRow.getColumnId());
            }
            else if (selectedRow == null && currentSelection >= 0)
            {
                myTable.getSelectionModel().clearSelection();
                myTable.getSelectionModel().setAnchorSelectionIndex(-1);
            }
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        int selectedIndex = myTable.getSelectionModel().getMinSelectionIndex();
        ColumnDefinitionRow selectedRow = null;
        if (selectedIndex >= 0)
        {
            selectedRow = myModel.getDefinitionTableModel().getRow(selectedIndex);
        }

        if (myModel.getSelectedDefinition() != selectedRow)
        {
            myModel.setSelectedDefinition(selectedRow);
        }

        // start cell editing so that it can reread the table model, just incase
        // the isEditable changed after teh selected definition has been set in
        // the model.
        int column = myTable.getSelectedColumn();
        myTable.editCellAt(selectedIndex, column);
    }
}
