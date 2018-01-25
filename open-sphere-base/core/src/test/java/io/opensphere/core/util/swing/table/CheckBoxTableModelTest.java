package io.opensphere.core.util.swing.table;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

/** Test for {@link CheckBoxTableModel}. */
@SuppressWarnings({ "PMD.EmptyCatchBlock", "PMD.AvoidDuplicateLiterals" })
public class CheckBoxTableModelTest
{
    /** The values for the table. */
    private static final List<? extends String> VALUES = Arrays.asList("one", "two", "three", "four");

    /** The model to test. */
    private final CheckBoxTableModel myModel = new CheckBoxTableModel("", "Title", Boolean.TRUE, VALUES);

    /**
     * Test for
     * {@link CheckBoxTableModel#CheckBoxTableModel(String, String, Boolean, java.util.Collection)}
     * with a bad argument.
     */
    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("unused")
    public void testCheckBoxTableModel1()
    {
        new CheckBoxTableModel(null, "Title", Boolean.TRUE, VALUES);
    }

    /**
     * Test for
     * {@link CheckBoxTableModel#CheckBoxTableModel(String, String, Boolean, java.util.Collection)}
     * with a bad argument.
     */
    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("unused")
    public void testCheckBoxTableModel2()
    {
        new CheckBoxTableModel("", null, Boolean.TRUE, VALUES);
    }

    /**
     * Test for
     * {@link CheckBoxTableModel#CheckBoxTableModel(String, String, Boolean, java.util.Collection)}
     * with a bad argument.
     */
    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("unused")
    public void testCheckBoxTableModel3()
    {
        new CheckBoxTableModel("", "Title", null, VALUES);
    }

    /**
     * Test for
     * {@link CheckBoxTableModel#CheckBoxTableModel(String, String, Boolean, java.util.Collection)}
     * with a bad argument.
     */
    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("unused")
    public void testCheckBoxTableModel4()
    {
        new CheckBoxTableModel("", "Title", Boolean.TRUE, null);
    }

    /**
     * Test for {@link CheckBoxTableModel#getCheckedValues()}.
     */
    @Test
    public void testGetCheckedValues()
    {
        Assert.assertEquals(VALUES, myModel.getCheckedValues());

        myModel.setValueAt(Boolean.FALSE, 0, 0);
        Assert.assertEquals(VALUES.subList(1, VALUES.size()), myModel.getCheckedValues());

        myModel.setValueAt(Boolean.TRUE, 0, 0);
        myModel.setValueAt(Boolean.FALSE, 1, 0);
        Assert.assertEquals(Arrays.asList("one", "three", "four"), myModel.getCheckedValues());

        myModel.setValueAt(Boolean.FALSE, 0, 0);
        myModel.setValueAt(Boolean.FALSE, 2, 0);
        myModel.setValueAt(Boolean.FALSE, 3, 0);
        Assert.assertTrue(myModel.getCheckedValues().isEmpty());
    }

    /** Test for {@link CheckBoxTableModel#getColumnClass(int)}. */
    @Test
    public void testGetColumnClassInt()
    {
        Assert.assertEquals(Boolean.class, myModel.getColumnClass(0));
        Assert.assertEquals(Object.class, myModel.getColumnClass(1));
        // Test bad column indices.
        try
        {
            myModel.getColumnClass(-1);
            Assert.fail(IllegalArgumentException.class + " should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }
        try
        {
            myModel.getColumnClass(2);
            Assert.fail(IllegalArgumentException.class + " should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }
    }

    /** Test for {@link CheckBoxTableModel#getColumnCount()}. */
    @Test
    public void testGetColumnCount()
    {
        Assert.assertEquals(2, myModel.getColumnCount());
    }

    /** Test for {@link CheckBoxTableModel#getColumnCount()}. */
    @Test
    public void testGetColumnNameInt()
    {
        Assert.assertEquals("", myModel.getColumnName(0));
        Assert.assertEquals("Title", myModel.getColumnName(1));

        // Test bad column indices.
        try
        {
            myModel.getColumnName(-1);
            Assert.fail(IllegalArgumentException.class + " should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }
        try
        {
            myModel.getColumnName(2);
            Assert.fail(IllegalArgumentException.class + " should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }
    }

    /** Test for {@link CheckBoxTableModel#getRowCount()}. */
    @Test
    public void testGetRowCount()
    {
        Assert.assertEquals(VALUES.size(), myModel.getRowCount());
    }

    /** Test for {@link CheckBoxTableModel#getValueAt(int, int)}. */
    @Test
    public void testGetValueAt()
    {
        for (int row = 0; row < myModel.getRowCount(); ++row)
        {
            Assert.assertEquals(Boolean.TRUE, myModel.getValueAt(row, 0));
            Assert.assertEquals(VALUES.get(row), myModel.getValueAt(row, 1));
        }

        // Test bad row indices.
        try
        {
            myModel.getValueAt(-1, 0);
            Assert.fail(IllegalArgumentException.class + " should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }

        try
        {
            myModel.getValueAt(VALUES.size(), 0);
            Assert.fail(IllegalArgumentException.class + " should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }
    }

    /** Test for {@link CheckBoxTableModel#isCellEditable(int, int)}. */
    @Test
    public void testIsCellEditable()
    {
        for (int col = 0; col < myModel.getColumnCount(); ++col)
        {
            for (int row = 0; row < myModel.getRowCount(); ++row)
            {
                Assert.assertFalse(col == 0 ^ myModel.isCellEditable(row, col));
            }
        }
    }

    /**
     * Test for
     * {@link CheckBoxTableModel#setCheckedValues(java.util.Collection)}.
     */
    @Test
    public void testSetCheckedValues()
    {
        TableModelListener listener = EasyMock.createMock(TableModelListener.class);
        listener.tableChanged(EasyMock.isA(TableModelEvent.class));
        EasyMock.replay(listener);

        myModel.addTableModelListener(listener);

        Assert.assertEquals(VALUES, myModel.getCheckedValues());

        myModel.setCheckedValues(VALUES.subList(1, VALUES.size()));
        Assert.assertEquals(VALUES.subList(1, VALUES.size()), myModel.getCheckedValues());

        EasyMock.verify(listener);
        EasyMock.reset(listener);
        listener.tableChanged(EasyMock.isA(TableModelEvent.class));
        EasyMock.replay(listener);

        myModel.setCheckedValues(Arrays.asList("one", "three", "four"));
        Assert.assertEquals(Arrays.asList("one", "three", "four"), myModel.getCheckedValues());

        EasyMock.verify(listener);
        EasyMock.reset(listener);
        listener.tableChanged(EasyMock.isA(TableModelEvent.class));
        EasyMock.replay(listener);

        myModel.setCheckedValues(Collections.<String>emptyList());
        Assert.assertTrue(myModel.getCheckedValues().isEmpty());

        EasyMock.verify(listener);
    }

    /** Test for {@link CheckBoxTableModel#setValueAt(Object, int, int)}. */
    @Test
    public void testSetValueAtObjectIntInt()
    {
        for (int row = 0; row < myModel.getRowCount(); ++row)
        {
            myModel.setValueAt(Boolean.FALSE, row, 0);
            Assert.assertEquals(Boolean.FALSE, myModel.getValueAt(row, 0));
            try
            {
                myModel.setValueAt("", row, 1);
                Assert.fail(IllegalArgumentException.class + " should have been thrown.");
            }
            catch (IllegalArgumentException e)
            {
                // success
            }
        }

        // Test bad row indices.
        try
        {
            myModel.setValueAt(Boolean.FALSE, -1, 0);
            Assert.fail(IllegalArgumentException.class + " should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }

        try
        {
            myModel.setValueAt(Boolean.FALSE, VALUES.size(), 0);
            Assert.fail(IllegalArgumentException.class + " should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }
    }
}
