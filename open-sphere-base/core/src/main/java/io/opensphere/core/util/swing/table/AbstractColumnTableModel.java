package io.opensphere.core.util.swing.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.swing.table.AbstractTableModel;

/**
 * Abstract base class for table models that only deals with columns. This
 * allows sub classes to implement whatever data providing strategy they want
 * without worrying about columns.
 */
@ThreadSafe
public abstract class AbstractColumnTableModel extends AbstractTableModel
{
    /** The serialVersionUID constant. */
    private static final long serialVersionUID = 1L;

    /** The <code>List</code> of column identifiers. */
    @GuardedBy("myColumnIdentifiers")
    private final List<String> myColumnIdentifiers;

    /** The <code>List</code> of column classes. */
    @GuardedBy("myColumnIdentifiers")
    private final List<Class<?>> myColumnClasses;

    /**
     * Constructor.
     */
    public AbstractColumnTableModel()
    {
        super();
        myColumnIdentifiers = new ArrayList<>();
        myColumnClasses = new ArrayList<>();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        Class<?> columnClass;
        synchronized (myColumnIdentifiers)
        {
            if (columnIndex < myColumnClasses.size() && columnIndex >= 0)
            {
                columnClass = myColumnClasses.get(columnIndex);
            }
            else
            {
                columnClass = Object.class;
            }
        }
        return columnClass;
    }

    @Override
    public int getColumnCount()
    {
        synchronized (myColumnIdentifiers)
        {
            return myColumnIdentifiers.size();
        }
    }

    @Override
    public String getColumnName(int columnIndex)
    {
        synchronized (myColumnIdentifiers)
        {
            return myColumnIdentifiers.get(columnIndex);
        }
    }

    /**
     * Sets column classes.
     *
     * @param columnClasses The column classes
     */
    public void setColumnClasses(Class<?>... columnClasses)
    {
        setColumnClasses(Arrays.asList(columnClasses));
    }

    /**
     * Sets column classes.
     *
     * @param columnClasses The column classes
     */
    public void setColumnClasses(List<Class<?>> columnClasses)
    {
        synchronized (myColumnIdentifiers)
        {
            myColumnClasses.clear();
            myColumnClasses.addAll(columnClasses);
        }
    }

    /**
     * Sets the column identifiers. This is a mutator.
     *
     * @param columnIdentifiers The column identifiers
     */
    public void setColumnIdentifiers(String... columnIdentifiers)
    {
        setColumnIdentifiers(Arrays.asList(columnIdentifiers));
    }

    /**
     * Sets the column identifiers. This is a mutator.
     *
     * @param columnIdentifiers The column identifiers
     */
    public void setColumnIdentifiers(List<String> columnIdentifiers)
    {
        synchronized (myColumnIdentifiers)
        {
            myColumnIdentifiers.clear();
            myColumnIdentifiers.addAll(columnIdentifiers);
        }
        fireTableStructureChanged();
    }

    /**
     * Adds a column.
     *
     * @param columnIdentifier the column identifier
     * @param columnClass the column class
     */
    public void addColumn(String columnIdentifier, Class<?> columnClass)
    {
        synchronized (myColumnIdentifiers)
        {
            myColumnIdentifiers.add(columnIdentifier);
            myColumnClasses.add(columnClass);
        }
        fireTableStructureChanged();
    }

    /**
     * Removes a column.
     *
     * @param columnIdentifier the column identifier
     */
    public void removeColumn(String columnIdentifier)
    {
        int colIndex;
        synchronized (myColumnIdentifiers)
        {
            colIndex = findColumn(columnIdentifier);
            if (colIndex != -1)
            {
                myColumnIdentifiers.remove(colIndex);
                myColumnClasses.remove(colIndex);
            }
        }
        if (colIndex != -1)
        {
            fireTableStructureChanged();
        }
    }

    /**
     * Gets the columnIdentifiers.
     *
     * @return the columnIdentifiers
     */
    public List<String> getColumnIdentifiers()
    {
        synchronized (myColumnIdentifiers)
        {
            return new ArrayList<>(myColumnIdentifiers);
        }
    }

    /**
     * Gets the columnClasses.
     *
     * @return the columnClasses
     */
    public List<Class<?>> getColumnClasses()
    {
        synchronized (myColumnIdentifiers)
        {
            return new ArrayList<>(myColumnClasses);
        }
    }
}
