package io.opensphere.csvcommon.ui.columndefinition.ui;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.swing.JComboBox;

import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;

/**
 * Tests the AvailableDataTypesBinder class.
 *
 */
public class AvailableDataTypesBinderTest
{
    /**
     * Tests the binder.
     */
    @SuppressWarnings("unused")
    @Test
    public void testUpdate()
    {
        ColumnDefinitionModel model = new ColumnDefinitionModel();
        JComboBox<String> comboBox = new JComboBox<>();

        List<String> availableDataTypes = New.list("dataType1", "dataType2", "dataType3");

        AvailableDataTypesBinder binder = new AvailableDataTypesBinder(comboBox, model);
        model.setAvailableDataTypes(availableDataTypes);

        assertEquals(availableDataTypes.size(), comboBox.getItemCount());

        int index = 0;
        for (String format : availableDataTypes)
        {
            assertEquals(format, comboBox.getItemAt(index).toString());
            index++;
        }

        model.setAvailableDataTypes(New.<String>list());
        assertEquals(0, comboBox.getItemCount());
    }
}
