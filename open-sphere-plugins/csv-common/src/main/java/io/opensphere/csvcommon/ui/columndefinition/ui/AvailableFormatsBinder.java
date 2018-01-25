package io.opensphere.csvcommon.ui.columndefinition.ui;

import java.util.Observable;

import javax.swing.JComboBox;

import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.ui.BaseComboBoxBinder;

/**
 * Binds the available formats combo box to the model and keeps the combo box in
 * sync with the model.
 *
 */
public class AvailableFormatsBinder extends BaseComboBoxBinder
{
    /**
     * The available data types combo box.
     */
    private final JComboBox<String> myComboBox;

    /**
     * The column definition model.
     */
    private final ColumnDefinitionModel myModel;

    /**
     * Constructs a new available formats binder.
     *
     * @param comboBox The available formats combo box.
     * @param model The column definition model.
     */
    public AvailableFormatsBinder(JComboBox<String> comboBox, ColumnDefinitionModel model)
    {
        super(comboBox, model);
        myComboBox = comboBox;
        myModel = model;
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (ColumnDefinitionModel.AVAILABLE_FORMATS_PROPERTY.equals(arg))
        {
            String selectedItem = null;

            if (myModel.getSelectedDefinition() != null)
            {
                selectedItem = myModel.getSelectedDefinition().getFormat();
            }

            super.updateComboBox(myModel.getAvailableFormats(), selectedItem);
        }
        else if (ColumnDefinitionModel.CAN_ADD_FORMATS_PROPERTY.equals(arg))
        {
            myComboBox.setEditable(myModel.canAddFormats());
        }
    }
}
