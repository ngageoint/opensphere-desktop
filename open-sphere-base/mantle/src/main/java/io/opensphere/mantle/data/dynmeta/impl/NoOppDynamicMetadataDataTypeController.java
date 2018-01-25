package io.opensphere.mantle.data.dynmeta.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.dynmeta.DynamicMetadataDataTypeController;

/**
 * The Class NoOppDynamicColumnDataTypeController.
 */
public class NoOppDynamicMetadataDataTypeController implements DynamicMetadataDataTypeController
{
    /** The Data type info. */
    private final DataTypeInfo myDataTypeInfo;

    /** The Unsupported oper message. */
    private final String myUnsupportedOperMessage;

    /**
     * Instantiates a new no opp dynamic column data type controller.
     *
     * @param dti the dti
     */
    public NoOppDynamicMetadataDataTypeController(DataTypeInfo dti)
    {
        myDataTypeInfo = dti;
        myUnsupportedOperMessage = "Data type " + dti.getDisplayName() + " does not support dynamic meta data columns.";
    }

    @Override
    public boolean addDynamicColumn(String columnName, Class<?> columnClass, Object source)
    {
        return false;
    }

    @Override
    public void appendValue(List<Long> cacheIds, int columnIndex, Object value, Object source)
    {
        throw new UnsupportedOperationException(myUnsupportedOperMessage);
    }

    @Override
    public void appendValue(List<Long> cacheIds, String columnName, Object value, Object source)
    {
        throw new UnsupportedOperationException(myUnsupportedOperMessage);
    }

    @Override
    public void appendValue(long elementCacheId, int columnIndex, Object value, Object source)
    {
        throw new UnsupportedOperationException(myUnsupportedOperMessage);
    }

    @Override
    public void appendValue(long elementCacheId, String columnName, Object value, Object source)
    {
        throw new UnsupportedOperationException(myUnsupportedOperMessage);
    }

    @Override
    public void clearValues(int columnIndex, Object source)
    {
        throw new UnsupportedOperationException(myUnsupportedOperMessage);
    }

    @Override
    public void clearValues(List<Long> cacheIds, Object source)
    {
        throw new UnsupportedOperationException(myUnsupportedOperMessage);
    }

    @Override
    public void clearValues(String dynamicColumnName, Object source)
    {
        throw new UnsupportedOperationException(myUnsupportedOperMessage);
    }

    @Override
    public boolean columnSupportsAppending(int columnIndex)
    {
        return false;
    }

    @Override
    public boolean columnSupportsAppending(String columnName)
    {
        return false;
    }

    @Override
    public DataTypeInfo getDataTypeInfo()
    {
        return myDataTypeInfo;
    }

    @Override
    public int getDynamicColumnCount()
    {
        return 0;
    }

    @Override
    public int getDynamicColumnIndex(String columnName)
    {
        return -1;
    }

    @Override
    public String getDynamicColumnName(int index)
    {
        return null;
    }

    @Override
    public Set<String> getDynamicColumnNames()
    {
        return Collections.<String>emptySet();
    }

    @Override
    public Set<String> getDynamicColumnNamesOfType(Class<?> type, boolean appendableOnly)
    {
        return Collections.<String>emptySet();
    }

    @Override
    public int getOriginalColumnCount()
    {
        return 0;
    }

    @Override
    public Object getValue(long elementCacheId, int columnIndex)
    {
        return null;
    }

    @Override
    public Object getValue(long elementCacheId, String columnName)
    {
        return null;
    }

    @Override
    public boolean isDynamicColumn(String columnName)
    {
        return false;
    }

    @Override
    public boolean isDynamicColumnIndex(int index)
    {
        return false;
    }

    @Override
    public boolean isValidNewDynamicColumnName(String columnName)
    {
        return false;
    }

    @Override
    public void setValue(long elementCacheId, int columnIndex, Object value, Object source)
    {
        throw new UnsupportedOperationException(myUnsupportedOperMessage);
    }

    @Override
    public void setValue(long elementCacheId, String columnName, Object value, Object source)
    {
        throw new UnsupportedOperationException(myUnsupportedOperMessage);
    }

    @Override
    public void setValues(List<Long> cacheIds, int columnIndex, Object value, Object source)
    {
        throw new UnsupportedOperationException(myUnsupportedOperMessage);
    }

    @Override
    public void setValues(List<Long> cacheIds, String columnName, Object value, Object source)
    {
        throw new UnsupportedOperationException(myUnsupportedOperMessage);
    }

    @Override
    public boolean supportsDynamicColumns()
    {
        return false;
    }
}
