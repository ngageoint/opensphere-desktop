package io.opensphere.analysis.listtool.view;

import java.awt.Color;
import java.awt.Point;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import javax.swing.DefaultRowSorter;
import javax.swing.SwingWorker;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.sort.TableSortController;

import io.opensphere.analysis.listtool.model.ListToolTableModel;
import io.opensphere.core.model.DoubleRange;
import io.opensphere.core.util.Utilities;

/**
 * The Class CustomTableSortControllerExtension.
 *
 * Helps to more efficiently retrieve the data that will be sorted by making a
 * single query to the cache and also using map to prevent repetitive data
 * retrieves on non-primary sort columns.
 */
public class CustomTableSortControllerExtension extends TableSortController<TableModel>
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(CustomTableSortControllerExtension.class);

    /** The null proxy. */
    private static Object ourNullProxy = new Object();

    /** The table model. */
    private final ListToolTableModel myTableModel;

    /** The my sort wrapper. */
    private final CustomTableRowSorterModelWrapper mySortWrapper;

    /** Whether the sorter is busy. */
    private final BooleanProperty myIsBusy = new SimpleBooleanProperty(false);

    /**
     * Instantiates a new custom table sort controller extension.
     *
     * @param model the model
     */
    public CustomTableSortControllerExtension(ListToolTableModel model)
    {
        super(model);
        myTableModel = model;
        mySortWrapper = new CustomTableRowSorterModelWrapper();
        setModelWrapper(mySortWrapper);
    }

    /**
     * Initialize. Call this after setting this as the row sorter because
     * JXTable will set this stuff itself.
     */
    public void init()
    {
        for (int column = 0; column < myTableModel.getColumnCount(); ++column)
        {
            setComparator(column, new ListToolComparator());
        }
    }

    @Override
    public void sort()
    {
        mySortWrapper.clearCache();
        try
        {
            super.sort();
        }
        catch (IndexOutOfBoundsException e)
        {
            // Ground this exception.
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Received IndexOutOfBoundsException on super.sort()");
            }
            LOGGER.error(e);
        }
    }

    @Override
    public void toggleSortOrder(final int column)
    {
        myIsBusy.set(true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
        {
            @Override
            protected Void doInBackground()
            {
                List<?> columnData = myTableModel.getColumnValues(column);
                mySortWrapper.setColumnData(column, columnData);
                return null;
            }

            @Override
            protected void done()
            {
                try
                {
                    get();
                }
                catch (InterruptedException | ExecutionException e)
                {
                    LOGGER.error(e, e);
                }

                try
                {
                    CustomTableSortControllerExtension.super.toggleSortOrder(column);
                    mySortWrapper.clearCache();
                }
                finally
                {
                    myIsBusy.set(false);
                }
            }
        };
        worker.execute();
    }

    /**
     * Gets the property for when the sorter is busy.
     *
     * @return the property for when the sorter is busy
     */
    public BooleanProperty isBusyProperty()
    {
        return myIsBusy;
    }

    /**
     * The Class CustomTableRowSorterModelWrapper.
     */
    private class CustomTableRowSorterModelWrapper extends DefaultRowSorter.ModelWrapper<TableModel, Integer>
    {
        /** The my column data. */
        private List<?> myColumnData;

        /** The primary column index. */
        private int myPrimaryColumnIndex;

        /** The point to object map. */
        private final Map<Point, Object> myPointToObjectMap = new HashMap<>();

        /**
         * Clear cache.
         */
        public void clearCache()
        {
            myPointToObjectMap.clear();
            myPrimaryColumnIndex = -1;
            myColumnData = null;
        }

        @Override
        public int getColumnCount()
        {
            return myTableModel != null ? myTableModel.getColumnCount() : 0;
        }

        @Override
        public Integer getIdentifier(int row)
        {
            return Integer.valueOf(row);
        }

        @Override
        public TableModel getModel()
        {
            return myTableModel;
        }

        @Override
        public int getRowCount()
        {
            return myTableModel != null ? myTableModel.getRowCount() : 0;
        }

        @Override
        public Object getValueAt(int row, int column)
        {
            Object value = null;
            if (column == myPrimaryColumnIndex && myColumnData != null && row < myColumnData.size())
            {
                value = myColumnData.get(row);
            }
            else
            {
                Point p = new Point(row, column);
                value = myPointToObjectMap.get(p);
                if (value == null)
                {
                    value = myTableModel.getValueAt(row, column);
                    myPointToObjectMap.put(p, value == null ? ourNullProxy : value);
                }
            }
            return Utilities.sameInstance(value, ourNullProxy) ? null : value;
        }

        /**
         * Sets the column data.
         *
         * @param index the index
         * @param data the data
         */
        public void setColumnData(int index, List<?> data)
        {
            myPrimaryColumnIndex = index;
            myColumnData = data;
        }
    }

    /**
     * The list tool comparator.
     */
    private static class ListToolComparator implements Comparator<Object>, Serializable
    {
        /** serialVersionUID. */
        private static final long serialVersionUID = 1L;

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public int compare(Object o1, Object o2)
        {
            if (o1 instanceof Comparable && o2 instanceof Comparable)
            {
                return ((Comparable)o1).compareTo(o2);
            }
            if (o1 instanceof DoubleRange && o2 instanceof DoubleRange)
            {
                return ((DoubleRange)o1).compareMaxThenMin((DoubleRange)o2);
            }
            if (o1 instanceof Color && o2 instanceof Color)
            {
                Color c1 = (Color)o1;
                Color c2 = (Color)o2;
                float h1 = Color.RGBtoHSB(c1.getRed(), c1.getGreen(), c1.getBlue(), null)[0];
                float h2 = Color.RGBtoHSB(c2.getRed(), c2.getGreen(), c2.getBlue(), null)[0];
                return Float.compare(h1, h2);
            }
            return 0;
        }
    }
}
