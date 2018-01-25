package io.opensphere.csvcommon.ui.columndefinition.model;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;
import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.ui.columndefinition.model.BeforeAfterRow;
import io.opensphere.csvcommon.ui.columndefinition.model.BeforeAfterTableModel;

/**
 * Tests the BeforeAfterTableModel class.
 *
 */
public class BeforeAfterTableModelTest
{
    /**
     * Tests adding rows to the model.
     */
    @Test
    public void testAddRows()
    {
        EasyMockSupport support = new EasyMockSupport();

        final List<BeforeAfterRow> rows = createTestRows();

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

        BeforeAfterTableModel model = new BeforeAfterTableModel();
        model.addTableModelListener(listener);

        model.addRows(New.<BeforeAfterRow>list());

        model.addRows(rows);

        assertEquals(rows.size(), model.getRowCount());

        for (int i = 0; i < rows.size(); i++)
        {
            assertEquals(rows.get(i).getBeforeValue(), model.getValueAt(i, 0));
            assertEquals(rows.get(i).getAfterValue(), model.getValueAt(i, 1));
        }

        support.verifyAll();
    }

    /**
     * Tests clearing rows.
     */
    @Test
    public void testClearRows()
    {
        EasyMockSupport support = new EasyMockSupport();

        final List<BeforeAfterRow> rows = createTestRows();

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

        BeforeAfterTableModel model = new BeforeAfterTableModel();

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
     * Tests the column names.
     */
    @Test
    public void testColumnNames()
    {
        EasyMockSupport support = new EasyMockSupport();

        TableModelListener listener = support.createMock(TableModelListener.class);
        listener.tableChanged(EasyMock.isA(TableModelEvent.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Void>()
        {
            @Override
            public Void answer()
            {
                TableModelEvent event = (TableModelEvent)EasyMock.getCurrentArguments()[0];

                assertEquals(TableModelEvent.UPDATE, event.getType());

                return null;
            }
        });

        support.replayAll();

        BeforeAfterTableModel model = new BeforeAfterTableModel();

        model.addTableModelListener(listener);

        String columnNamePrefix = "columnName";

        model.setColumnNamePrefix(columnNamePrefix);

        assertEquals(columnNamePrefix + BeforeAfterTableModel.ourBeforeSuffix, model.getColumnName(0));
        assertEquals(columnNamePrefix + BeforeAfterTableModel.ourAfterSuffix, model.getColumnName(1));

        support.verifyAll();
    }

    /**
     * Creates the test rows.
     *
     * @return The test rows.
     */
    private List<BeforeAfterRow> createTestRows()
    {
        List<BeforeAfterRow> testRows = New.list();

        for (int i = 0; i < 100; i++)
        {
            BeforeAfterRow testRow = new BeforeAfterRow();
            testRow.setBeforeValue("before" + i);
            testRow.setAfterValue("after" + i);

            testRows.add(testRow);
        }

        return testRows;
    }
}
