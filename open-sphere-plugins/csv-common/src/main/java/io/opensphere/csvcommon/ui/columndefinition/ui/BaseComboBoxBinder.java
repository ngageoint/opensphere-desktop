package io.opensphere.csvcommon.ui.columndefinition.ui;

import java.util.List;
import java.util.Observer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;

/**
 * Base class used by the the combo box binders.
 *
 */
public abstract class BaseComboBoxBinder implements Observer
{
    /**
     * The combo box.
     */
    private final JComboBox<String> myComboBox;

    /**
     * The column definition model.
     */
    private final ColumnDefinitionModel myModel;

    /**
     * Constructs a new base combo box binder.
     *
     * @param comboBox The combo box.
     * @param model The column definition model.
     */
    public BaseComboBoxBinder(JComboBox<String> comboBox, ColumnDefinitionModel model)
    {
        myComboBox = comboBox;
        myModel = model;
        myModel.addObserver(this);
    }

    /**
     * Removes itself as a listener to the model.
     */
    public void close()
    {
        myModel.deleteObserver(this);
    }

    /**
     * Updates the combo box with the new items.
     *
     * @param newItems The new items to add to the combo box.
     * @param selectedItem The selectedItem, or null if there isn't one.
     */
    protected void updateComboBox(List<String> newItems, String selectedItem)
    {
        DefaultComboBoxModel<String> newModel = new DefaultComboBoxModel<>();

        for (String item : newItems)
        {
            newModel.addElement(item);
        }

        ColumnDefinitionRow selectedColumn = myModel.getSelectedDefinition();

        if (selectedColumn != null)
        {
            newModel.setSelectedItem(selectedItem);
        }

        myComboBox.setModel(newModel);
    }
}
