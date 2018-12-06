package io.opensphere.analysis.listtool.model;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import io.opensphere.analysis.table.functions.ColumnFunction;
import io.opensphere.analysis.table.model.MetaColumn;
import io.opensphere.analysis.table.model.MetaColumnsTableModel;
import io.opensphere.core.Toolbox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.table.AbstractColumnTableModel;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.dynmeta.DynamicMetadataDataTypeController;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;
import net.jcip.annotations.NotThreadSafe;

/**
 * The list tool table model.
 */
@NotThreadSafe
public class ListToolTableModel extends AbstractColumnTableModel implements MetaColumnsTableModel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The data type. */
    private final transient DataTypeInfo myDataType;

    /** The meta columns. */
    private final transient List<MetaColumn<?>> myMetaColumns;

    /** The row data provider. */
    private transient DataElementProvider myRowDataProvider;

    /** The data type controller. */
    private transient DynamicMetadataDataTypeController myDataTypeController;

    /** The time column index. */
    private int myTimeColumnIndex = -1;

    /** The highlighted id. */
    private Long myHighlightedId;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param dataType the data type
     */
    public ListToolTableModel(Toolbox toolbox, DataTypeInfo dataType)
    {
        super();

        assert SwingUtilities.isEventDispatchThread();
        myDataType = dataType;
        myMetaColumns = Collections.unmodifiableList(createMetaColumns());
        setColumnInfo();

        setRowDataProvider(new DataElementProvider(this, toolbox, dataType, myMetaColumns, myTimeColumnIndex));
    }

    @Override
    public int getRowCount()
    {
        return myRowDataProvider.getRowCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        Object value = null;
        if (columnIndex == 0)
        {
            // This might be a little dirty, but it allows us to do the cache
            // re-mapping optimization in DataElementProvider while still
            // returning a valid row index here.
            return Integer.valueOf(rowIndex);
        }
        List<?> values = myRowDataProvider.getData(rowIndex);
        if (columnIndex < values.size())
        {
            value = values.get(columnIndex);
        }
        return value;
    }

    /**
     * Sets the time spans.
     *
     * @param timeSpans the time spans
     * @param reload whether to reload the data
     */
    public void setTimeSpans(TimeSpanList timeSpans, boolean reload)
    {
        myRowDataProvider.setTimeSpans(timeSpans, reload);
    }

    /**
     * Sets whether the loaded data is for all time.
     *
     * @param isAllTime whether the loaded data is for all time
     */
    public void setAllTime(boolean isAllTime)
    {
        myRowDataProvider.setAllTime(isAllTime);
    }

    /**
     * Loads the ids based on the current time values.
     */
    public void reload()
    {
        myRowDataProvider.reload();
    }

    /**
     * Adds some ids.
     *
     * @param ids the ids
     * @param removeDuplicates whether to remove duplicates
     */
    public void addIds(Collection<Long> ids, boolean removeDuplicates)
    {
        myRowDataProvider.addIds(ids, removeDuplicates);
    }

    /**
     * Removes some ids.
     *
     * @param ids the ids
     */
    public void removeIds(Collection<Long> ids)
    {
        myRowDataProvider.removeIds(ids);
    }

    /**
     * Removes all IDs/records.
     */
    public void removeAll()
    {
        myRowDataProvider.removeAll();
    }

    /**
     * Clears the cache.
     */
    public void clearCache()
    {
        myRowDataProvider.clearCache();
    }

    /**
     * Gets the time spans.
     *
     * @return the time spans
     */
    public TimeSpanList getTimeSpans()
    {
        return myRowDataProvider.getTimeSpans();
    }

    /**
     * Gets whether the loaded data is for all time.
     *
     * @return whether the loaded data is for all time
     */
    public boolean isAllTime()
    {
        return myRowDataProvider.isAllTime();
    }

    @Override
    public DataElement getDataAt(int rowIndex)
    {
        return myRowDataProvider.getDataElement(rowIndex);
    }

    /**
     * Gets the data element IDs.
     *
     * @return the data element IDs
     */
    public List<Long> getDataElementIds()
    {
        return myRowDataProvider.getDataElementIds();
    }

    /**
     * Gets the data element id at the given row index.
     *
     * @param rowIndex the row index
     * @return the data element id
     */
    public Long getDataElementId(int rowIndex)
    {
        return myRowDataProvider.getDataElementId(rowIndex);
    }

    /**
     * Index of data element id.
     *
     * @param id the id
     * @return the index
     */
    public int indexOfDataElementId(long id)
    {
        return myRowDataProvider.indexOfDataElementId(id);
    }

    /**
     * Gets the columnIdentifiers.
     *
     * @param includeMetaColumns whether to include meta columns
     * @return the columnIdentifiers
     */
    public List<String> getColumnIdentifiers(boolean includeMetaColumns)
    {
        return includeMetaColumns ? super.getColumnIdentifiers() : myDataType.getMetaDataInfo().getKeyNames();
    }

    /**
     * Retrieves all the values for a specified column, if it needs to it will
     * make the query to the cache.
     *
     * @param columnIndex the column index
     * @return the column data
     */
    public List<?> getColumnValues(int columnIndex)
    {
        return columnIndex == myTimeColumnIndex ? myRowDataProvider.getTimeColumnValues()
                : myRowDataProvider.getColumnValues(columnIndex);
    }

    /**
     * Gets the model index for the given special key.
     *
     * @param specialType the special key
     * @return the index, or -1
     */
    public final int getIndexForKey(SpecialKey specialType)
    {
        String key = myDataType.getMetaDataInfo().getKeyForSpecialType(specialType);
        int index = key != null ? getColumnIdentifiers().indexOf(key) : -1;
        return index;
    }

    @Override
    public List<MetaColumn<?>> getMetaColumns()
    {
        assert SwingUtilities.isEventDispatchThread();
        return myMetaColumns;
    }

    /**
     * Returns true if the column is a meta-column.
     *
     * @param columnIndex the column index
     * @return true, if is meta column
     */
    public boolean isMetaColumn(int columnIndex)
    {
        // We specifically allow MGRS because it is not really a meta-column,
        // it is more of a derived column.
        return columnIndex < myMetaColumns.size() && !MetaColumn.MGRS_DERIVED.equals(getColumnName(columnIndex));
    }

    /**
     * If a column is user-created all cells inside are editable.
     *
     * @return true if we can retrieve a dynamic column from the index of the
     *         given cell
     * @override
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        String colName = getColumnName(columnIndex);
        // We don't want to manually edit ColumnFunction cells
        return myDataTypeController == null ? false : myDataTypeController.isDynamicColumn(colName)
                && !myDataTypeController.getDynamicColumnNamesOfType(ColumnFunction.class, false).contains(colName);
    }

    /**
     * Sets a cell value within a user-created column.
     *
     * @override
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        String colName = getColumnName(columnIndex);
        long dataElementId = getDataElementId(rowIndex).longValue();
        myDataTypeController.setValue(dataElementId, colName, aValue, this);
    }

    /**
     * Sets the highlighted id.
     *
     * @param id the highlighted id
     */
    public void setHighlightedId(Long id)
    {
        if (!Objects.equals(myHighlightedId, id))
        {
            myHighlightedId = id;
            myRowDataProvider.clearCache();
        }
    }

    /**
     * Sets the rowDataProvider (for test purposes only).
     *
     * @param rowDataProvider the rowDataProvider
     */
    final void setRowDataProvider(DataElementProvider rowDataProvider)
    {
        myRowDataProvider = rowDataProvider;

        // Pass data provider events up to table model listeners
        myRowDataProvider.addTableModelListener(new TableModelListener()
        {
            @Override
            public void tableChanged(TableModelEvent e)
            {
                fireTableChanged(e);
            }
        });
    }

    /**
     * Sets myDataTypeController. Used to determine if a column is user-created.
     *
     * @param controller the dynamic meta-data controller
     */
    public void setDynamicColumnProvider(DynamicMetadataDataTypeController controller)
    {
        myDataTypeController = controller;
    }

    /**
     * Creates the meta columns.
     *
     * @return the meta columns
     */
    private List<MetaColumn<?>> createMetaColumns()
    {
        List<MetaColumn<?>> metaColumns = New.list(6);
        metaColumns.add(new MetaColumn<>(MetaColumn.INDEX, Integer.class, true)
        {
            @Override
            public Integer getValue(int rowIndex, DataElement dataElement)
            {
                return Integer.valueOf(rowIndex);
            }
        });
        metaColumns.add(new MetaColumn<>(MetaColumn.COLOR, Color.class, true)
        {
            @Override
            public Color getValue(int rowIndex, DataElement dataElement)
            {
                return dataElement.getVisualizationState() == null ? Color.WHITE : dataElement.getVisualizationState().getColor();
            }
        });
        metaColumns.add(new MetaColumn<>(MetaColumn.VISIBLE, Boolean.class, false)
        {
            @Override
            public Boolean getValue(int rowIndex, DataElement dataElement)
            {
                return dataElement.getVisualizationState() == null ? Boolean.FALSE
                        : Boolean.valueOf(dataElement.getVisualizationState().isVisible());
            }
        });
        metaColumns.add(new MetaColumn<>(MetaColumn.HILIGHT, Boolean.class, false)
        {
            @Override
            public Boolean getValue(int rowIndex, DataElement dataElement)
            {
                return myHighlightedId == null ? Boolean.FALSE
                        : Boolean.valueOf(myHighlightedId.equals(myRowDataProvider.getDataElementId(rowIndex)));
            }
        });
        metaColumns.add(new MetaColumn<>(MetaColumn.SELECTED, Boolean.class, false)
        {
            @Override
            public Boolean getValue(int rowIndex, DataElement dataElement)
            {
                return dataElement.getVisualizationState() == null ? Boolean.FALSE
                        : Boolean.valueOf(dataElement.getVisualizationState().isSelected());
            }
        });
        metaColumns.add(new MetaColumn<>(MetaColumn.LOB_VISIBLE, Boolean.class, false)
        {
            @Override
            public Boolean getValue(int rowIndex, DataElement dataElement)
            {
                return dataElement.getVisualizationState() == null ? Boolean.FALSE
                        : Boolean.valueOf(dataElement.getVisualizationState().isLobVisible());
            }
        });
        return metaColumns;
    }

    /**
     * Sets the column info.
     */
    private void setColumnInfo()
    {
        List<String> keyNames = myDataType.getMetaDataInfo().getKeyNames();
        int size = myMetaColumns.size() + keyNames.size();
        List<String> columnIdentifiers = New.list(size);
        List<Class<?>> columnClasses = New.list(size);
        for (MetaColumn<?> metaColumn : myMetaColumns)
        {
            columnIdentifiers.add(metaColumn.getColumnIdentifier());
            columnClasses.add(metaColumn.getColumnClass());
        }
        for (String key : keyNames)
        {
            columnIdentifiers.add(key);
            columnClasses.add(myDataType.getMetaDataInfo().getKeyClassType(key));
        }
        setColumnIdentifiers(columnIdentifiers);
        setColumnClasses(columnClasses);

        // Do this after setting the column classes because getIndexForKey needs
        // them set to work
        myTimeColumnIndex = getIndexForKey(TimeKey.DEFAULT);
        if (myTimeColumnIndex != -1)
        {
            List<Class<?>> columnClasses2 = getColumnClasses();
            columnClasses2.set(myTimeColumnIndex, TimeSpan.class);
            setColumnClasses(columnClasses2);
        }
    }
}
