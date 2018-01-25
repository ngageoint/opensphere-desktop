package io.opensphere.csvcommon.ui.columndefinition.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.swing.JComboBox;

import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;

/**
 * Tests the AvailableFormatsBinder class.
 *
 */
public class AvailableFormatsBinderTest
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

        List<String> formats = New.list("format1", "format2", "format3");

        AvailableFormatsBinder binder = new AvailableFormatsBinder(comboBox, model);
        model.setAvailableFormats(formats);

        assertEquals(formats.size(), comboBox.getItemCount());

        int index = 0;
        for (String format : formats)
        {
            assertEquals(format, comboBox.getItemAt(index).toString());
            index++;
        }

        model.setAvailableFormats(New.<String>list());
        assertEquals(0, comboBox.getItemCount());
    }

    /**
     * Tests the can add formats.
     */
    @Test
    public void testUpdateCanAddFormats()
    {
        ColumnDefinitionModel model = new ColumnDefinitionModel();
        JComboBox<String> comboBox = new JComboBox<>();

        @SuppressWarnings("unused")
        AvailableFormatsBinder binder = new AvailableFormatsBinder(comboBox, model);

        model.setCanAddFormats(true);

        assertTrue(comboBox.isEditable());

        model.setCanAddFormats(false);

        assertFalse(comboBox.isEditable());
    }
}
