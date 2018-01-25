package io.opensphere.csvcommon.ui.columndefinition.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.swing.JTable;

import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionTableModel;

/**
 * Tests the ColumnDefinitionTableBinder class.
 *
 */
public class ColumnDefinitionTableBinderTest
{
    /**
     * Tests when the model selection value is changed.
     */
    @SuppressWarnings("unused")
    @Test
    public void testUpdate()
    {
        ColumnDefinitionModel model = createModel();
        JTable table = createTestTable(model.getDefinitionTableModel());

        ColumnDefinitionTableBinder binder = new ColumnDefinitionTableBinder(table, model);

        model.setSelectedDefinition(model.getDefinitionTableModel().getRow(0));

        assertEquals(0, table.getSelectionModel().getMinSelectionIndex());

        model.setSelectedDefinition(model.getDefinitionTableModel().getRow(0));

        assertEquals(0, table.getSelectionModel().getMinSelectionIndex());
    }

    /**
     * Tests when the model selection is set to nothing.
     */
    @SuppressWarnings("unused")
    @Test
    public void testUpdateNoSelection()
    {
        ColumnDefinitionModel model = createModel();
        JTable table = createTestTable(model.getDefinitionTableModel());

        ColumnDefinitionTableBinder binder = new ColumnDefinitionTableBinder(table, model);

        model.setSelectedDefinition(model.getDefinitionTableModel().getRow(0));

        assertEquals(0, table.getSelectionModel().getMinSelectionIndex());

        model.setSelectedDefinition(null);

        assertTrue(table.getSelectionModel().getMinSelectionIndex() < 0);
    }

    /**
     * Tests when the table selection is changed.
     */
    @SuppressWarnings("unused")
    @Test
    public void testValueChanged()
    {
        ColumnDefinitionModel model = createModel();
        JTable table = createTestTable(model.getDefinitionTableModel());

        ColumnDefinitionTableBinder binder = new ColumnDefinitionTableBinder(table, model);

        table.getSelectionModel().setSelectionInterval(0, 0);

        assertEquals(0, model.getSelectedDefinition().getColumnId());

        table.getSelectionModel().setSelectionInterval(0, 0);

        assertEquals(0, model.getSelectedDefinition().getColumnId());
    }

    /**
     * Tests when the table selection is changed to nothing.
     */
    @SuppressWarnings("unused")
    @Test
    public void testValueChangedNoSelection()
    {
        ColumnDefinitionModel model = createModel();
        JTable table = createTestTable(model.getDefinitionTableModel());

        ColumnDefinitionTableBinder binder = new ColumnDefinitionTableBinder(table, model);

        table.getSelectionModel().setSelectionInterval(0, 0);

        assertEquals(0, model.getSelectedDefinition().getColumnId());

        table.getSelectionModel().clearSelection();

        assertNull(model.getSelectedDefinition());
    }

    /**
     * Create the test model to use.
     *
     * @return The test model.
     */
    private ColumnDefinitionModel createModel()
    {
        ColumnDefinitionRow row = new ColumnDefinitionRow();
        row.setColumnId(0);

        ColumnDefinitionModel mainModel = new ColumnDefinitionModel();
        mainModel.getDefinitionTableModel().addRows(New.list(row));

        return mainModel;
    }

    /**
     * Creates the test table to use.
     *
     * @param model The model to use for the table.
     * @return the test table.
     */
    private JTable createTestTable(ColumnDefinitionTableModel model)
    {
        JTable testTable = new JTable();

        testTable.setModel(model);

        return testTable;
    }
}
