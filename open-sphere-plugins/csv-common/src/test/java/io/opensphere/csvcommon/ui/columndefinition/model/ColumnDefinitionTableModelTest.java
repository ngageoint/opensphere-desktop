package io.opensphere.csvcommon.ui.columndefinition.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;
import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionTableModel;

/**
 * Tests ColumnDefinitionTableModel class.
 *
 */
public class ColumnDefinitionTableModelTest
{
    /**
     * Tests adding rows.
     */
    @Test
    public void testAddRows()
    {
        EasyMockSupport support = new EasyMockSupport();

        final List<ColumnDefinitionRow> rows = createTestRows();

        TableModelListener listener = support.createMock(TableModelListener.class);
        listener.tableChanged(EasyMock.isA(TableModelEvent.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Void>()
        {
            @Override
            public Void answer()
            {
                TableModelEvent event = (TableModelEvent)EasyMock.getCurrentArguments()[0];

                assertEquals(0, event.getFirstRow());
                assertEquals(rows.size() - 1, event.getLastRow());
                assertEquals(TableModelEvent.INSERT, event.getType());

                return null;
            }
        });

        support.replayAll();

        ColumnDefinitionTableModel model = new ColumnDefinitionTableModel();
        model.addTableModelListener(listener);

        model.addRows(New.<ColumnDefinitionRow>list());

        model.addRows(rows);

        assertEquals(rows.size(), model.getRowCount());

        for (int i = 0; i < rows.size(); i++)
        {
            assertEquals(rows.get(i).isImport(), model.getValueAt(i, 0));
            assertEquals(rows.get(i).getColumnName(), model.getValueAt(i, 1));
            assertEquals(rows.get(i).getDataType(), model.getValueAt(i, 2));
            assertEquals(rows.get(i).getFormat(), model.getValueAt(i, 3));
        }

        support.verifyAll();
    }

    /**
     * Test clear.
     */
    @Test
    public void testClear()
    {
        EasyMockSupport support = new EasyMockSupport();

        final List<ColumnDefinitionRow> rows = createTestRows();

        TableModelListener listener = support.createMock(TableModelListener.class);
        listener.tableChanged(EasyMock.isA(TableModelEvent.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Void>()
        {
            @Override
            public Void answer()
            {
                TableModelEvent event = (TableModelEvent)EasyMock.getCurrentArguments()[0];

                assertEquals(0, event.getFirstRow());
                assertEquals(rows.size() - 1, event.getLastRow());
                assertEquals(TableModelEvent.DELETE, event.getType());

                return null;
            }
        });

        support.replayAll();

        ColumnDefinitionTableModel model = new ColumnDefinitionTableModel();

        model.addTableModelListener(listener);

        model.clear();

        model.removeTableModelListener(listener);

        model.addRows(rows);

        model.addTableModelListener(listener);

        model.clear();

        assertEquals(0, model.getRowCount());

        support.verifyAll();
    }

    /**
     * Tests getting column classes.
     */
    @Test
    public void testGetColumnClassInt()
    {
        ColumnDefinitionTableModel model = new ColumnDefinitionTableModel();

        assertEquals(Boolean.class, model.getColumnClass(0));
        assertEquals(String.class, model.getColumnClass(1));
        assertEquals(String.class, model.getColumnClass(2));
        assertEquals(String.class, model.getColumnClass(3));
    }

    /**
     * Test get column count.
     */
    @Test
    public void testGetColumnCount()
    {
        ColumnDefinitionTableModel model = new ColumnDefinitionTableModel();
        assertEquals(4, model.getColumnCount());
    }

    /**
     * Test get column name.
     */
    @Test
    public void testGetColumnNameInt()
    {
        ColumnDefinitionTableModel model = new ColumnDefinitionTableModel();

        assertEquals("Import", model.getColumnName(0));
        assertEquals("Column Name", model.getColumnName(1));
        assertEquals("Type", model.getColumnName(2));
        assertEquals("Format", model.getColumnName(3));
    }

    /**
     * Verifies that the table is editable.
     */
    @Test
    public void testIsCellEditable()
    {
        ColumnDefinitionTableModel model = new ColumnDefinitionTableModel();
        ColumnDefinitionRow row = new ColumnDefinitionRow();
        model.addRows(New.list(row));

        assertTrue(model.isCellEditable(0, 0));
        assertTrue(model.isCellEditable(0, 1));
        assertTrue(model.isCellEditable(0, 2));
        assertFalse(model.isCellEditable(0, 3));

        row.setDataType("Date");

        assertTrue(model.isCellEditable(0, 3));
    }

    /**
     * Tests setting the values within the table.
     */
    @Test
    public void testSetEmptyValue()
    {
        final List<ColumnDefinitionRow> rows = createTestRows();

        ColumnDefinitionTableModel model = new ColumnDefinitionTableModel();

        model.addRows(rows);

        assertEquals(rows.size(), model.getRowCount());

        for (int i = 0; i < rows.size(); i++)
        {
            model.setValueAt(null, i, 1);
        }

        for (int i = 0; i < rows.size(); i++)
        {
            ColumnDefinitionRow row = model.getRow(i);
            assertEquals("columnName" + i, row.getColumnName());
        }
    }

    /**
     * Tests the set format editable.
     */
    @Test
    public void testSetFormatEditable()
    {
        ColumnDefinitionTableModel model = new ColumnDefinitionTableModel();

        ColumnDefinitionRow row = new ColumnDefinitionRow();
        row.setDataType("Date");

        model.addRows(New.list(row));

        assertTrue(model.isCellEditable(0, 3));

        model.setFormatEditable(false);

        assertFalse(model.isCellEditable(0, 3));
    }

    /**
     * Tests setting the values within the table.
     */
    @Test
    public void testSetNameNonUnique()
    {
        final List<ColumnDefinitionRow> rows = createTestRows();

        ColumnDefinitionTableModel model = new ColumnDefinitionTableModel();

        model.addRows(rows);

        assertEquals(rows.size(), model.getRowCount());

        String originalValue = model.getValueAt(rows.size() - 1, 1).toString();

        model.setValueAt("columnName1", rows.size() - 1, 1);

        assertEquals(originalValue, model.getValueAt(rows.size() - 1, 1).toString());
    }

    /**
     * Tests setting the values within the table.
     */
    @Test
    public void testSetValue()
    {
        final List<ColumnDefinitionRow> rows = createTestRows();

        ColumnDefinitionTableModel model = new ColumnDefinitionTableModel();

        model.addRows(rows);

        assertEquals(rows.size(), model.getRowCount());

        String changedValue = "changed";

        for (int i = 0; i < rows.size(); i++)
        {
            model.setValueAt(i % 3 == 0, i, 0);
            model.setValueAt(changedValue + i + 1, i, 1);
            model.setValueAt(changedValue + i + 2, i, 2);
            model.setValueAt(changedValue + i + 3, i, 3);
        }

        for (int i = 0; i < rows.size(); i++)
        {
            ColumnDefinitionRow row = model.getRow(i);
            assertEquals(i % 3 == 0, row.isImport());
            assertEquals("changed" + i + 1, row.getColumnName());
            assertEquals("changed" + i + 2, row.getDataType());
            assertEquals("changed" + i + 3, row.getFormat());
        }
    }

    /**
     * Creates the test rows.
     *
     * @return The test rows.
     */
    private List<ColumnDefinitionRow> createTestRows()
    {
        List<ColumnDefinitionRow> testRows = New.list();

        for (int i = 0; i < 100; i++)
        {
            ColumnDefinitionRow testRow = new ColumnDefinitionRow();
            testRow.setColumnId(i);
            testRow.setColumnName("columnName" + i);
            testRow.setDataType("dataType" + i);
            testRow.setFormat("format" + i);
            testRow.setIsImport(i % 2 == 0);

            testRows.add(testRow);
        }

        return testRows;
    }
}
