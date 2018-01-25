package io.opensphere.csvcommon.ui.columndefinition.ui;

import java.util.Observable;

import javax.swing.JComboBox;

import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.ui.BaseComboBoxBinder;

/**
 * Binds the available data types combo box to the model and keeps the combo box
 * in sync with the model.
 *
 */
public class AvailableDataTypesBinder extends BaseComboBoxBinder
{
    /**
     * The column definition model.
     */
    private final ColumnDefinitionModel myModel;

    /**
     * Constructs a new available data types binder.
     *
     * @param comboBox The available data types combo box.
     * @param model The column definition model.
     */
    public AvailableDataTypesBinder(JComboBox<String> comboBox, ColumnDefinitionModel model)
    {
        super(comboBox, model);
        myModel = model;
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (ColumnDefinitionModel.AVAILABLE_DATA_TYPES_PROPERTY.equals(arg))
        {
            String selectedItem = null;

            if (myModel.getSelectedDefinition() != null)
            {
                selectedItem = myModel.getSelectedDefinition().getDataType();
            }

            super.updateComboBox(myModel.getAvailableDataTypes(), selectedItem);
        }
    }
}
