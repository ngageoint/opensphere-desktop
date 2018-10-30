package io.opensphere.analysis.listtool.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import io.opensphere.analysis.prefs.MGRSPreferences;
import io.opensphere.analysis.table.model.MetaColumn;
import io.opensphere.analysis.util.MGRSUtilities;
import io.opensphere.core.Toolbox;
import io.opensphere.core.model.IntegerRange;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.util.cache.SimpleIntCache;
import io.opensphere.core.util.collections.FixedSizeBufferMap;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.table.AbstractRowDataProvider;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.util.DataElementLookupUtils;
import io.opensphere.mantle.util.MantleToolboxUtils;
import javafx.beans.value.ChangeListener;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.NotThreadSafe;

/**
 * Data element provider for the list tool table model.
 */
@NotThreadSafe
class DataElementProvider extends AbstractRowDataProvider<List<?>>
{
    /** The toolbox. */
    private Toolbox myToolbox;

    /** The mantle data element lookup utilities. */
    private DataElementLookupUtils myDataElementLookupUtils;

//    /** The direct access retriever. */
//    private DirectAccessRetriever myRetriever;

    /** The data type. */
    private final DataTypeInfo myDataType;

    /** The meta columns. */
    private final transient List<MetaColumn<?>> myMetaColumns;

    /** The time column index. */
    private final int myTimeColumnIndex;

    /** The time spans. */
    private TimeSpanList myTimeSpans;

    /** Whether to query all time. */
    private boolean myIsAllTime = true;

    /** The ids. */
    @GuardedBy("this")
    private final TLongArrayList myIds = new TLongArrayList();

    /** The maximum ID, for performance. */
    private long myMaxId;

    /** The cache. */
    @GuardedBy("this")
    private final SimpleIntCache<List<?>> myCache;

    /**
     * Constructor.
     *
     * @param model the table model
     * @param toolbox the toolbox
     * @param dataType the data type
     * @param metaColumns the meta columns
     * @param timeColumnIndex the time column index
     */
    public DataElementProvider(TableModel model, Toolbox toolbox, DataTypeInfo dataType, List<MetaColumn<?>> metaColumns,
            int timeColumnIndex)
    {
        super(model);
        myToolbox = toolbox;
        myDataElementLookupUtils = MantleToolboxUtils.getDataElementLookupUtils(toolbox);
//            myRetriever = mantleToolbox.getDataElementCache().getDirectAccessRetriever(dataType);
        myDataType = dataType;
        myMetaColumns = metaColumns;
        myTimeColumnIndex = timeColumnIndex;
        myTimeSpans = TimeSpanList.emptyList();
        myCache = new SimpleIntCache<>(new FixedSizeBufferMap<Integer, List<?>>(100), new IntFunction<List<?>>()
        {
            @Override
            public List<?> apply(int rowIndex)
            {
                DataElement dataElement = getDataElement(rowIndex);
                return dataElement == null ? Collections.emptyList() : getValues(rowIndex, dataElement);
            }
        });

        ChangeListener<Object> metaColumnListener = (observable, oldValue, newValue) -> clearCache();
        for (MetaColumn<?> column : myMetaColumns)
        {
            column.getObservable().addListener(metaColumnListener);
        }
    }

    @Override
    public synchronized List<?> getData(int rowIndex)
    {
        return myCache.apply(rowIndex);
    }

    @Override
    public synchronized int getRowCount()
    {
        return myIds.size();
    }

    /**
     * Sets the time spans.
     *
     * @param timeSpans the time spans
     * @param reload whether to reload the data
     */
    public void setTimeSpans(TimeSpanList timeSpans, boolean reload)
    {
        assert SwingUtilities.isEventDispatchThread();
        if (!Objects.equals(myTimeSpans, timeSpans))
        {
            myTimeSpans = timeSpans;
            if (reload)
            {
                reload();
            }
        }
    }

    /**
     * Sets whether the loaded data is for all time.
     *
     * @param isAllTime whether the loaded data is for all time
     */
    public void setAllTime(boolean isAllTime)
    {
        assert SwingUtilities.isEventDispatchThread();
        if (myIsAllTime != isAllTime)
        {
            myIsAllTime = isAllTime;
            reload();
        }
    }

    /**
     * Loads the ids based on the current time values.
     */
    public void reload()
    {
        assert SwingUtilities.isEventDispatchThread();

        final TimeSpanList lookupSpans = timeMatters() ? myTimeSpans : TimeSpanList.emptyList();

        List<Long> ids = myDataElementLookupUtils.getDataElementCacheIds(myDataType, lookupSpans);

        synchronized (this)
        {
            myIds.clear();
            myIds.addAll(ids);
            updateMaxId();
            myCache.clear();
        }

        fireTableDataChanged();
    }

    /**
     * Adds some ids.
     *
     * @param ids the ids
     * @param removeDuplicates whether to remove duplicates
     */
    public void addIds(Collection<Long> ids, boolean removeDuplicates)
    {
        assert SwingUtilities.isEventDispatchThread();

        if (!ids.isEmpty())
        {
            Collection<Long> filteredIds = filterIds(ids, removeDuplicates);
            if (!filteredIds.isEmpty())
            {
                int firstRow;
                int lastRow;
                synchronized (this)
                {
                    firstRow = myIds.size();
                    myIds.addAll(filteredIds);
                    updateMaxId();
                    lastRow = myIds.size() - 1;
                }

                fireTableRowsInserted(firstRow, lastRow);
            }
        }
    }

    /**
     * Removes some ids.
     *
     * @param ids the ids
     */
    public void removeIds(Collection<Long> ids)
    {
        assert SwingUtilities.isEventDispatchThread();
        if (!ids.isEmpty())
        {
            TIntArrayList removedIndexes = new TIntArrayList();
            List<IntegerRange> removedRanges = New.list();

            synchronized (this)
            {
                for (Long id : ids)
                {
                    int index = myIds.indexOf(id.longValue());
                    if (index != -1)
                    {
                        removedIndexes.add(index);
                    }
                }
            }

            removedIndexes.sort();

            int firstRow = -1;
            int lastRow = -1;
            for (TIntIterator iter = removedIndexes.iterator(); iter.hasNext();)
            {
                int index = iter.next();

                // Found a gap, add the previous section
                if (index != lastRow + 1 && lastRow != -1)
                {
                    removedRanges.add(new IntegerRange(firstRow, lastRow));
                    firstRow = lastRow = -1;
                }

                if (firstRow == -1)
                {
                    firstRow = index;
                }
                lastRow = index;
            }
            // Add the previous section
            if (lastRow != -1)
            {
                removedRanges.add(new IntegerRange(firstRow, lastRow));
            }

            for (int i = removedRanges.size() - 1; i >= 0; --i)
            {
                IntegerRange removedRange = removedRanges.get(i);
                int min = removedRange.getMin().intValue();
                int max = removedRange.getMax().intValue();
                synchronized (this)
                {
                    int size = myIds.size();
                    int rangeSize = max - min + 1;
                    myIds.remove(min, rangeSize);
                    updateMaxId();
                    for (int row = min; row < size; ++row)
                    {
                        myCache.remap(row, row + rangeSize);
                    }
                }

                fireTableRowsDeleted(min, max);
            }
        }
    }

    /**
     * Removes all IDs/records.
     */
    public void removeAll()
    {
        int max;
        synchronized (this)
        {
            max = myIds.size() - 1;
            myIds.clear();
            myMaxId = 0;
            myCache.clear();
        }
        fireTableRowsDeleted(0, max);
    }

    /**
     * Clears the cache.
     */
    public final synchronized void clearCache()
    {
        myCache.clear();
    }

    /**
     * Gets the time spans.
     *
     * @return the time spans
     */
    public TimeSpanList getTimeSpans()
    {
        assert SwingUtilities.isEventDispatchThread();
        return myTimeSpans;
    }

    /**
     * Gets whether the loaded data is for all time.
     *
     * @return whether the loaded data is for all time
     */
    public boolean isAllTime()
    {
        assert SwingUtilities.isEventDispatchThread();
        return myIsAllTime;
    }

    /**
     * Gets the data element IDs.
     *
     * @return the data element IDs
     */
    public synchronized List<Long> getDataElementIds()
    {
        return getLongList(myIds);
    }

    /**
     * Gets the data element id at the given row index.
     *
     * @param rowIndex the row index
     * @return the data element id
     */
    public synchronized Long getDataElementId(int rowIndex)
    {
        Long id = null;
        if (!myIds.isEmpty() && rowIndex >= 0 && rowIndex < myIds.size())
        {
            id = Long.valueOf(myIds.get(rowIndex));
        }
        return id;
    }

    /**
     * Index of data element id.
     *
     * @param id the id
     * @return the index
     */
    public synchronized int indexOfDataElementId(long id)
    {
        return myIds.indexOf(id);
    }

    /**
     * Retrieves all the values for a specified column, if it needs to it will
     * make the query to the cache.
     *
     * @param columnIndex the column index
     * @return the column data
     */
    public List<Object> getColumnValues(int columnIndex)
    {
        List<Object> result;
        if (columnIndex >= myMetaColumns.size())
        {
            String columnName = getModel().getColumnName(columnIndex);
            result = getDataElementIds().stream().map(id -> lookupDataElement(id.longValue()).getMetaData().getValue(columnName))
                    .collect(Collectors.toList());
        }
        else
        {
            result = New.list(getRowCount());
            for (int i = 0, n = getRowCount(); i < n; ++i)
            {
                result.add(getModel().getValueAt(i, columnIndex));
            }
        }
        return result;
    }

    /**
     * Retrieves all the values for the time column, if it needs to it will make
     * the query to the cache.
     *
     * @return the column data
     */
    public List<TimeSpan> getTimeColumnValues()
    {
        return myDataElementLookupUtils.getTimespans(getDataElementIds());
    }

    /**
     * Gets the data element for the given row index.
     *
     * @param rowIndex the row index
     * @return the data element
     */
    public DataElement getDataElement(int rowIndex)
    {
        long id;
        synchronized (this)
        {
            id = myIds.get(rowIndex);
        }
        return lookupDataElement(id);
    }

    /**
     * Looks up the data element for the given id.
     *
     * @param id the id
     * @return the data element
     */
    public DataElement lookupDataElement(long id)
    {
        DataElement dataElement = myDataElementLookupUtils.getDataElement(id, myDataType, myDataType.getTypeKey());
        if (dataElement instanceof MapDataElement && ((MapDataElement)dataElement).getMapGeometrySupport() != null)
        {
            // If the element is a MapDataElement, generate a replacement
            // element with MGRS Derived data
            return MGRSUtilities.getMGRSDataElement((MapDataElement)dataElement,
                    MGRSPreferences.getToolMGRSPrecision(myToolbox.getPreferencesRegistry()), this);
        }
        // User label doesn't currently show up with direct access DataElements
//        if (myRetriever.getMapGeometrySupport(id) == null)
//        {
//            dataElement = new DirectAccessDataElement(id, myRetriever);
//        }
//        else
//        {
//            dataElement = new DirectAccessMapDataElement(id, myRetriever);
//        }
        return dataElement;
    }

    /**
     * Gets the cell values for the data element, including meta columns.
     *
     * @param rowIndex the row index
     * @param dataElement the data element
     * @return the values
     */
    private List<?> getValues(int rowIndex, DataElement dataElement)
    {
        // Add the meta columns values to the front of the data element values
        List<Object> elementValues = dataElement.getMetaData().getValues();
        if (elementValues == null)
        {
            elementValues = Collections.emptyList();
        }
        List<Object> values = New.list(myMetaColumns.size() + elementValues.size());
        synchronized (this)
        {
            for (MetaColumn<?> column : myMetaColumns)
            {
                values.add(column.getValue(rowIndex, dataElement));
            }
        }
        values.addAll(elementValues);

        // Set the time value to the time because for some reason it's just a
        // String in the meta data
        if (myTimeColumnIndex != -1 && myTimeColumnIndex < values.size())
        {
            values.set(myTimeColumnIndex, dataElement.getTimeSpan());
        }

        return values;
    }

    /**
     * Filters IDs based on the current state of the provider.
     *
     * @param ids the IDs to filter
     * @param removeDuplicates whether to remove duplicates
     * @return the filtered IDs
     */
    private Collection<Long> filterIds(Collection<Long> ids, boolean removeDuplicates)
    {
        Collection<Long> filteredIds = ids;

        // Remove duplicates
        if (removeDuplicates)
        {
            filteredIds = New.list(ids.size());
            synchronized (this)
            {
                for (Long id : ids)
                {
                    long idValue = id.longValue();
                    if (idValue > myMaxId || !myIds.contains(idValue))
                    {
                        filteredIds.add(id);
                    }
                }
            }
        }

        // Perform time filtering
        if (timeMatters())
        {
            filteredIds = myDataElementLookupUtils.filterIdsByTimeOfInterest(myTimeSpans, filteredIds);
        }

        return filteredIds;
    }

    /**
     * Determines whether time matters at all in the current state.
     *
     * @return whether time matters at all in the current state
     */
    private boolean timeMatters()
    {
        return myDataType.getBasicVisualizationInfo() != null
                && myDataType.getBasicVisualizationInfo().getLoadsTo().isTimelineEnabled() && !myIsAllTime;
    }

    /**
     * Updates the maximum ID.
     */
    private void updateMaxId()
    {
        myMaxId = myIds.isEmpty() ? 0 : myIds.max();
    }

    /**
     * Converts a trove long list to List of Long.
     *
     * @param tList the trove list
     * @return the List of Long
     */
    private static List<Long> getLongList(TLongArrayList tList)
    {
        List<Long> list = New.list(tList.size());
        for (TLongIterator iter = tList.iterator(); iter.hasNext();)
        {
            list.add(Long.valueOf(iter.next()));
        }
        return list;
    }
}
