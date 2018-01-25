package io.opensphere.core.util.swing.table;

import java.util.Arrays;

import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.junit.Assert;
import org.junit.Test;

/** Test for {@link JTableUtilities}. */
public class JTableUtilitiesTest
{
    /**
     * Test for
     * {@link JTableUtilities#convertRowIndicesToModel(javax.swing.JTable, int[])}
     * .
     */
    @Test
    public void testConvertRowIndicesToModel()
    {
        Object[][] data = new Object[3][];
        data[0] = new Object[] { "charlie" };
        data[1] = new Object[] { "bravo" };
        data[2] = new Object[] { "alpha" };
        TableModel tableModel = new DefaultTableModel(data, new Object[] { "name" });
        JTable table = new JTable(tableModel);
        table.setRowSorter(new TableRowSorter<TableModel>(tableModel));

        int[] indices = new int[] { 0, 1, 2 };
        Assert.assertArrayEquals(new int[] { 0, 1, 2 }, JTableUtilities.convertRowIndicesToModel(table, indices));

        table.getRowSorter().setSortKeys(Arrays.asList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        Assert.assertArrayEquals(new int[] { 2, 1, 0 }, JTableUtilities.convertRowIndicesToModel(table, indices));
    }
}
