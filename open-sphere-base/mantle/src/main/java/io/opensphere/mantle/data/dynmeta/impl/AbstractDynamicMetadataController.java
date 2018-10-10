package io.opensphere.mantle.data.dynmeta.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gnu.trove.map.hash.TLongObjectHashMap;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.dynmeta.DynamicMetadataController;
import io.opensphere.mantle.data.element.event.consolidated.ConsolidatedDataElementMetadataValueChangeEvent;

/**
 * The Class AbstractDynamicColumnController.
 *
 * @param <T> the generic type
 */
public abstract class AbstractDynamicMetadataController<T> implements DynamicMetadataController<T>
{
    /** The Column index. */
    private int myColumnIndex;

    /** The Column name. */
    private final String myColumnName;

    /** The Data type info. */
    private final DataTypeInfo myDataTypeInfo;

    /** The Dyn column name to value map. */
    private final TLongObjectHashMap<T> myIdToValueMap;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new abstract dynamic column controller.
     *
     * @param tb the tb
     * @param columnIndex the column index
     * @param columnName the column name
     * @param dti the dti
     */
    public AbstractDynamicMetadataController(Toolbox tb, int columnIndex, String columnName, DataTypeInfo dti)
    {
        myColumnIndex = columnIndex;
        myColumnName = columnName;
        myDataTypeInfo = dti;
        myIdToValueMap = new TLongObjectHashMap<>();
        myToolbox = tb;
    }

    @Override
    public void appendValue(List<Long> cacheIds, Object valueToAppend, Object source)
    {
        throw new UnsupportedOperationException(
                "Column " + myColumnName + " for data type " + myDataTypeInfo.getTypeKey() + " does not support appending.");
    }

    @Override
    public void appendValue(long elementId, Object valueToAppend, Object source)
    {
        throw new UnsupportedOperationException(
                "Column " + myColumnName + " for data type " + myDataTypeInfo.getTypeKey() + " does not support appending.");
    }

    @Override
    public void clear(Object source)
    {
        if (!myIdToValueMap.isEmpty())
        {
            long[] ids = myIdToValueMap.keys();
            myIdToValueMap.clear();
            fireChangeEvent(source, ids);
        }
    }

    @Override
    public void clearValues(List<Long> elementIds, Object source)
    {
        Set<Long> idsRemoved = new HashSet<>(elementIds);
        for (Long id : elementIds)
        {
            if (myIdToValueMap.contains(id.longValue()))
            {
                myIdToValueMap.remove(id.longValue());
                idsRemoved.add(id);
            }
        }
        if (!idsRemoved.isEmpty())
        {
            fireChangeEvent(new ArrayList<>(idsRemoved), source);
        }
    }

    @Override
    public void clearValues(Object source, long... elementIds)
    {
        clearValues(CollectionUtilities.listView(elementIds), source);
    }

    @Override
    public int getColumnIndex()
    {
        return myColumnIndex;
    }

    @Override
    public void setColumnIndex(int index)
    {
        myColumnIndex = index;
    }

    @Override
    public String getColumnName()
    {
        return myColumnName;
    }

    @Override
    public DataTypeInfo getDataTypeInfo()
    {
        return myDataTypeInfo;
    }

    @Override
    public T getValue(long elementId)
    {
        return myIdToValueMap.get(elementId);
    }

    @Override
    public final boolean isAcceptableValueType(Object value)
    {
        return value == null || getColumnClass().isAssignableFrom(value.getClass());
    }

    @Override
    public void setValue(long elementId, Object value, Object source)
    {
        T tempVal = validateValueIsAcceptable(value);
        boolean fireChanged = true;
        if (value == null)
        {
            if (myIdToValueMap.remove(elementId) == null)
            {
                fireChanged = false;
            }
        }
        else
        {
            myIdToValueMap.put(elementId, tempVal);
        }
        if (fireChanged)
        {
            fireChangeEvent(source, elementId);
        }
    }

    @Override
    public void setValues(List<Long> cacheIds, Object value, Object source)
    {
        Utilities.checkNull(cacheIds, "cacheIds");
        T tempVal = validateValueIsAcceptable(value);
        if (!cacheIds.isEmpty())
        {
            if (value == null)
            {
                clearValues(cacheIds, source);
            }
            else
            {
                for (Long id : cacheIds)
                {
                    myIdToValueMap.put(id.longValue(), tempVal);
                }
                fireChangeEvent(cacheIds, source);
            }
        }
    }

    @Override
    public boolean supportsAppend()
    {
        return false;
    }

    /**
     * Validate value is acceptable.
     *
     * @param value the value
     * @return the value as a type T
     */
    @SuppressWarnings("unchecked")
    public final T validateValueIsAcceptable(Object value)
    {
        if (!isAcceptableValueType(value))
        {
            throw new IllegalArgumentException("The class " + value.getClass()
                    + " is not a valid type for this dynamic column, only " + getColumnClass().getName() + " is acceptable.");
        }
        else
        {
            return (T)value;
        }
    }

    /**
     * Fire change event.
     *
     * @param idsChanged the ids changed
     * @param source the source
     */
    protected void fireChangeEvent(List<Long> idsChanged, Object source)
    {
        ConsolidatedDataElementMetadataValueChangeEvent evt = new ConsolidatedDataElementMetadataValueChangeEvent(idsChanged,
                Collections.singleton(myDataTypeInfo.getTypeKey()), source);
        myToolbox.getEventManager().publishEvent(evt);
    }

    /**
     * Fire change event.
     *
     * @param source the source
     * @param idsChanged the ids changed
     */
    protected void fireChangeEvent(Object source, long... idsChanged)
    {
        ConsolidatedDataElementMetadataValueChangeEvent evt = new ConsolidatedDataElementMetadataValueChangeEvent(idsChanged,
                Collections.singleton(myDataTypeInfo.getTypeKey()), source);
        myToolbox.getEventManager().publishEvent(evt);
    }

    /**
     * Gets the id to value map.
     *
     * @return the id to value map
     */
    protected TLongObjectHashMap<T> getIdToValueMap()
    {
        return myIdToValueMap;
    }
}
