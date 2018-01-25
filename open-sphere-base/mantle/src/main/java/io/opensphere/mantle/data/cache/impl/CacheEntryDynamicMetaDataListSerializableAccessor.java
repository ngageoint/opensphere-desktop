package io.opensphere.mantle.data.cache.impl;

import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.mantle.data.element.DynamicMetaDataList;

/**
 * A {@link SerializableAccessor} that accesses the {@link DynamicMetaDataList}
 * that belongs to a {@link CacheEntry}.
 */
public class CacheEntryDynamicMetaDataListSerializableAccessor extends SerializableAccessor<CacheEntry, DynamicMetaDataList>
{
    /** Property descriptor used in the data registry. */
    public static final PropertyDescriptor<DynamicMetaDataList> PROPERTY_DESCRIPTOR = new PropertyDescriptor<DynamicMetaDataList>(
            "dynamicMetaDataList", DynamicMetaDataList.class);

    /**
     * Instantiates a new {@link CacheEntry} map geometry support serializable
     * accessor.
     */
    public CacheEntryDynamicMetaDataListSerializableAccessor()
    {
        super(PROPERTY_DESCRIPTOR);
    }

    @Override
    public DynamicMetaDataList access(CacheEntry input)
    {
        return input.getLoadedElementData() == null ? null : (DynamicMetaDataList)input.getLoadedElementData().getMetaData();
    }
}
