package io.opensphere.mantle.data.cache.impl;

import java.util.Collections;
import java.util.List;

import io.opensphere.core.cache.accessor.PropertyArrayAccessor;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;

/**
 * The Class CacheEntryPropertyListAccessor.
 */
public class CacheEntryPropertyListAccessor extends PropertyArrayAccessor<CacheEntry>
{
    /** The key names. */
    private final List<String> myKeyNames;

    /**
     * Instantiates a new map data element property list accessor.
     *
     * @param dti the DataTypeInfo
     */
    public CacheEntryPropertyListAccessor(DataTypeInfo dti)
    {
        this(dti.getMetaDataInfo());
    }

    /**
     * Instantiates a new map data element property list accessor.
     *
     * @param metaDataInfo the metaDataInfo
     */
    public CacheEntryPropertyListAccessor(MetaDataInfo metaDataInfo)
    {
        super(metaDataInfo == null || metaDataInfo.getPropertyArrayDescriptor() == null
                ? MetaDataInfo.EMPTY_PROPERTY_ARRAY_DESCRIPTOR : metaDataInfo.getPropertyArrayDescriptor());
        myKeyNames = metaDataInfo == null ? Collections.<String>emptyList() : metaDataInfo.getKeyNames();
    }

    @Override
    public Object[] access(CacheEntry input)
    {
        Object[] propValues = new Object[myKeyNames.size()];
        if (input.getLoadedElementData() != null && input.getLoadedElementData().getMetaData() != null)
        {
            List<Object> valueList = input.getLoadedElementData().getMetaData();
            for (int index = 0; index < myKeyNames.size(); ++index)
            {
                propValues[index] = valueList.get(index);
            }
        }
        return propValues;
    }
}
