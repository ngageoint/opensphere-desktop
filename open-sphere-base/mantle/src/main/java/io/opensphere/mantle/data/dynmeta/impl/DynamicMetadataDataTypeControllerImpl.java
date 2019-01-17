package io.opensphere.mantle.data.dynmeta.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import gnu.trove.map.hash.TIntObjectHashMap;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.ByteString;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.dynmeta.DynamicMetadataController;
import io.opensphere.mantle.data.dynmeta.DynamicMetadataDataTypeController;

/**
 * The Class DynamicColumnDataTypeCoordinator.
 */
@SuppressWarnings("PMD.GodClass")
public class DynamicMetadataDataTypeControllerImpl implements DynamicMetadataDataTypeController
{
    /** The DTI. */
    private final DataTypeInfo myDTI;

    /** The Dyn column name to value map. */
    private final TIntObjectHashMap<DynamicMetadataController<?>> myDynColumnNameToValueMap;

    /** The Original column count. */
    private final int myOriginalColumnCount;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Creates the controller.
     *
     * @param tb the tb
     * @param columnIndex the column index
     * @param dti the dti
     * @return the dynamic column controller
     */
    private static DynamicMetadataController<?> createController(Toolbox tb, int columnIndex, DataTypeInfo dti)
    {
        DynamicMetadataController<?> controller = null;
        String key = dti.getMetaDataInfo().getKeyNames().get(columnIndex);
        Class<?> type = dti.getMetaDataInfo().getKeyClassTypeMap().get(key);
        if (type == null)
        {
            type = Object.class;
        }

        if (isSameClass(String.class, type))
        {
            controller = new DynamicMetadataStringController(tb, columnIndex, key, dti);
        }
        else if (isSameClass(Integer.class, type))
        {
            controller = new DynamicMetadataIntegerController(tb, columnIndex, key, dti);
        }
        else if (isSameClass(Long.class, type))
        {
            controller = new DynamicMetadataLongController(tb, columnIndex, key, dti);
        }
        else if (isSameClass(Double.class, type))
        {
            controller = new DynamicMetadataDoubleController(tb, columnIndex, key, dti);
        }
        else if (isSameClass(Float.class, type))
        {
            controller = new DynamicMetadataFloatController(tb, columnIndex, key, dti);
        }
        else if (isSameClass(Date.class, type))
        {
            controller = new DynamicMetadataDateController(tb, columnIndex, key, dti);
        }
        else if (isSameClass(ByteString.class, type))
        {
            controller = new DynamicMetadataByteStringController(tb, columnIndex, key, dti);
        }
        else
        {
            controller = new DynamicMetadataObjectController(tb, columnIndex, key, dti);
        }

        return controller;
    }

    /**
     * Reindexes the dynamic columns supported by this class. Used when removing
     * columns.
     *
     * @param columnIndex the column index
     */
    private void reindex(int columnIndex)
    {
        TIntObjectHashMap<DynamicMetadataController<?>> tempStore = new TIntObjectHashMap<>();
        for (int key : myDynColumnNameToValueMap.keys())
        {
            if (key > columnIndex)
            {
                tempStore.put(key - 1, myDynColumnNameToValueMap.remove(key));
            }
        }

        for (int key : tempStore.keys())
        {
            myDynColumnNameToValueMap.put(key, tempStore.remove(key));
        }
    }

    /**
     * Checks if is same class.
     *
     * @param c1 the c1
     * @param c2 the c2
     * @return true, if is same class
     */
    private static boolean isSameClass(Class<?> c1, Class<?> c2)
    {
        return c1.getName().equals(c2.getName());
    }

    /**
     * Instantiates a new dynamic column data type coordinator.
     *
     * @param tb the tb
     * @param dti the dti
     */
    public DynamicMetadataDataTypeControllerImpl(Toolbox tb, DataTypeInfo dti)
    {
        myToolbox = tb;
        myDTI = dti;
        myOriginalColumnCount = myDTI.getMetaDataInfo().getOriginalKeyNames().size();
        myDynColumnNameToValueMap = new TIntObjectHashMap<>();
    }

    @Override
    public boolean addDynamicColumn(String columnName, Class<?> columnClass, Object source)
    {
        boolean added = false;

        if (!myDTI.getMetaDataInfo().hasKey(columnName))
        {
            added = myDTI.getMetaDataInfo().addKey(columnName, columnClass, source);
            int columnIndex = myDTI.getMetaDataInfo().getKeyNames().indexOf(columnName);
            DynamicMetadataController<?> controller = createController(myToolbox, columnIndex, myDTI);
            myDynColumnNameToValueMap.put(columnIndex + 1, controller);
        }

        return added;
    }

    @Override
    public boolean removeDynamicColumn(String columnName, Class<?> columnClass, Object source)
    {
        if (myDTI.getMetaDataInfo().hasKey(columnName))
        {
            int columnIndex = myDTI.getMetaDataInfo().getKeyNames().indexOf(columnName);
            boolean removed = myDTI.getMetaDataInfo().removeKey(columnName, columnClass, source);

            DynamicMetadataController<?> controller = myDynColumnNameToValueMap.remove(columnIndex);
            controller.clear(source);
            controller.setColumnIndex(-1);

            reindex(columnIndex);

            return removed;
        }
        return false;
    }

    @Override
    public void appendValue(List<Long> cacheIds, int columnIndex, Object value, Object source)
    {
        Utilities.checkNull(cacheIds, "cacheIds");
        validateColumnIndexIsDynamic(columnIndex);
        validateColumnSupportsAppending(columnIndex);

        if (cacheIds.isEmpty())
        {
            return;
        }

        DynamicMetadataController<?> cController = myDynColumnNameToValueMap.get(columnIndex);
        if (cController == null)
        {
            cController = createController(myToolbox, columnIndex, myDTI);
            myDynColumnNameToValueMap.put(columnIndex, cController);
        }
        cController.appendValue(cacheIds, value, source);
    }

    @Override
    public void appendValue(List<Long> cacheIds, String columnName, Object value, Object source)
    {
        int index = vaildateColumnNameIsDynamic(columnName);
        validateColumnSupportsAppending(columnName);
        appendValue(cacheIds, index, value, source);
    }

    @Override
    public void appendValue(long elementCacheId, int columnIndex, Object value, Object source)
    {
        validateColumnSupportsAppending(columnIndex);
        DynamicMetadataController<?> cController = myDynColumnNameToValueMap.get(columnIndex);
        if (cController == null)
        {
            cController = createController(myToolbox, columnIndex, myDTI);
            myDynColumnNameToValueMap.put(columnIndex, cController);
        }
        cController.appendValue(elementCacheId, value, source);
    }

    @Override
    public void appendValue(long elementCacheId, String columnName, Object value, Object source)
    {
        int index = vaildateColumnNameIsDynamic(columnName);
        validateColumnSupportsAppending(columnName);
        if (index != -1)
        {
            appendValue(elementCacheId, index, value, source);
        }
    }

    @Override
    public void clearValues(int columnIndex, Object source)
    {
        validateColumnIndexIsDynamic(columnIndex);
        DynamicMetadataController<?> mapToAlter = myDynColumnNameToValueMap.get(columnIndex);
        if (mapToAlter != null)
        {
            mapToAlter.clear(source);
        }
    }

    @Override
    public void clearValues(List<Long> cacheIds, Object source)
    {
        Utilities.checkNull(cacheIds, "cacheIds");
        if (!cacheIds.isEmpty())
        {
            Set<String> dynColNames = getDynamicColumnNames();
            for (String colName : dynColNames)
            {
                int colIndex = getDynamicColumnIndex(colName);
                DynamicMetadataController<?> cController = myDynColumnNameToValueMap.get(colIndex);
                if (cController != null)
                {
                    cController.clearValues(cacheIds, source);
                }
            }
        }
    }

    @Override
    public void clearValues(String dynamicColumnName, Object source)
    {
        int index = vaildateColumnNameIsDynamic(dynamicColumnName);
        if (index != -1)
        {
            clearValues(index, source);
        }
    }

    @Override
    public boolean columnSupportsAppending(int columnIndex)
    {
        if (isDynamicColumnIndex(columnIndex))
        {
            DynamicMetadataController<?> columnController = myDynColumnNameToValueMap.get(columnIndex);
            if (columnController != null)
            {
                return columnController.supportsAppend();
            }
        }
        return false;
    }

    @Override
    public boolean columnSupportsAppending(String columnName)
    {
        if (isDynamicColumn(columnName))
        {
            int index = getDynamicColumnIndex(columnName);
            return columnSupportsAppending(index);
        }
        return false;
    }

    @Override
    public DataTypeInfo getDataTypeInfo()
    {
        return myDTI;
    }

    @Override
    public int getDynamicColumnCount()
    {
        return myDynColumnNameToValueMap == null ? 0 : myDynColumnNameToValueMap.size();
    }

    @Override
    public int getDynamicColumnIndex(String columnName)
    {
        int index = myDTI.getMetaDataInfo().getKeyIndex(columnName);
        if (index != -1 && !isDynamicColumnIndex(index))
        {
            index = -1;
        }
        return index;
    }

    @Override
    public String getDynamicColumnName(int index)
    {
        if (isDynamicColumnIndex(index))
        {
            return myDTI.getMetaDataInfo().getKeyNames().get(index);
        }
        throw new IllegalArgumentException("The specified column index is not a dynamic column.");
    }

    @Override
    public Set<String> getDynamicColumnNames()
    {
        Set<String> dynColumnNameSet = new HashSet<>();
        if (myDTI.getMetaDataInfo().getKeyCount() > myOriginalColumnCount)
        {
            List<String> keyNames = myDTI.getMetaDataInfo().getKeyNames();
            dynColumnNameSet.addAll(keyNames.subList(myOriginalColumnCount, keyNames.size()));
        }
        return dynColumnNameSet;
    }

    @Override
    public Set<String> getDynamicColumnNamesOfType(Class<?> type, boolean appendableOnly)
    {
        Utilities.checkNull(type, "type");
        Set<String> dynColNames = getDynamicColumnNames();
        if (!dynColNames.isEmpty())
        {
            Iterator<String> nameItr = dynColNames.iterator();
            while (nameItr.hasNext())
            {
                String name = nameItr.next();
                Class<?> colClass = myDTI.getMetaDataInfo().getKeyClassTypeMap().get(name);
                if (!type.getName().equals(colClass.getName()))
                {
                    nameItr.remove();
                }
                else
                {
                    if (appendableOnly && !columnSupportsAppending(name))
                    {
                        nameItr.remove();
                    }
                }
            }
        }
        return dynColNames;
    }

    @Override
    public int getOriginalColumnCount()
    {
        return myOriginalColumnCount;
    }

    @Override
    public Object getValue(long elementCacheId, int columnIndex)
    {
        validateColumnIndexIsDynamic(columnIndex);
        DynamicMetadataController<?> mapToRetrieveFrom = myDynColumnNameToValueMap.get(columnIndex);
        if (mapToRetrieveFrom != null)
        {
            return mapToRetrieveFrom.getValue(elementCacheId);
        }
        return null;
    }

    @Override
    public Object getValue(long elementCacheId, String columnName)
    {
        int index = vaildateColumnNameIsDynamic(columnName);
        if (index != -1)
        {
            return getValue(elementCacheId, index);
        }
        return null;
    }

    @Override
    public boolean isDynamicColumn(String columnName)
    {
        int index = myDTI.getMetaDataInfo().getKeyNames().indexOf(columnName);
        return index != -1 && isDynamicColumnIndex(index);
    }

    @Override
    public boolean isDynamicColumnIndex(int index)
    {
        return index >= myOriginalColumnCount;
    }

    @Override
    public boolean isValidNewDynamicColumnName(String columnName)
    {
        return !myDTI.getMetaDataInfo().hasKey(columnName);
    }

    @Override
    public void setValue(long elementCacheId, int columnIndex, Object value, Object source)
    {
        validateColumnIndexIsDynamic(columnIndex);
        DynamicMetadataController<?> mapToAlter = myDynColumnNameToValueMap.get(columnIndex);
        if (mapToAlter == null)
        {
            mapToAlter = createController(myToolbox, columnIndex, myDTI);
            myDynColumnNameToValueMap.put(columnIndex, mapToAlter);
        }
        mapToAlter.setValue(elementCacheId, value, source);
    }

    @Override
    public void setValue(long elementCacheId, String columnName, Object value, Object source)
    {
        int index = vaildateColumnNameIsDynamic(columnName);
        if (index != -1)
        {
            setValue(elementCacheId, index, value, source);
        }
    }

    @Override
    public void setValues(List<Long> cacheIds, int columnIndex, Object value, Object source)
    {
        Utilities.checkNull(cacheIds, "cacheIds");
        validateColumnIndexIsDynamic(columnIndex);

        if (cacheIds.isEmpty())
        {
            return;
        }

        DynamicMetadataController<?> mapToAlter = myDynColumnNameToValueMap.get(columnIndex);
        if (mapToAlter == null)
        {
            mapToAlter = createController(myToolbox, columnIndex, myDTI);
            myDynColumnNameToValueMap.put(columnIndex, mapToAlter);
        }
        mapToAlter.setValues(cacheIds, value, source);
    }

    @Override
    public void setValues(List<Long> cacheIds, String columnName, Object value, Object source)
    {
        int index = vaildateColumnNameIsDynamic(columnName);
        if (index != -1)
        {
            setValues(cacheIds, index, value, source);
        }
    }

    @Override
    public boolean supportsDynamicColumns()
    {
        return true;
    }

    /**
     * Validate column name is dynamic.
     *
     * @param columnName the column name
     * @return the index of the dynamic column
     */
    protected int vaildateColumnNameIsDynamic(String columnName)
    {
        int index = getDynamicColumnIndex(columnName);
        if (index == -1)
        {
            throw new IllegalArgumentException("The column " + columnName + " is not a dynamic column.");
        }
        return index;
    }

    /**
     * Validate column index is dynamic.
     *
     * @param columnIndex the column index
     */
    protected void validateColumnIndexIsDynamic(int columnIndex)
    {
        if (!isDynamicColumnIndex(columnIndex))
        {
            throw new IllegalArgumentException("The column index " + columnIndex + " is not a dynamic column.");
        }
    }

    /**
     * Validate column supports appending.
     *
     * @param columnIndex the column index
     */
    private void validateColumnSupportsAppending(int columnIndex)
    {
        if (!columnSupportsAppending(columnIndex))
        {
            throw new UnsupportedOperationException("The column index " + columnIndex + " does not support appending.");
        }
    }

    /**
     * Validate column supports appending.
     *
     * @param columnName the column name
     */
    private void validateColumnSupportsAppending(String columnName)
    {
        if (!columnSupportsAppending(columnName))
        {
            throw new UnsupportedOperationException("The column \"" + columnName + "\" does not support appending.");
        }
    }
}
