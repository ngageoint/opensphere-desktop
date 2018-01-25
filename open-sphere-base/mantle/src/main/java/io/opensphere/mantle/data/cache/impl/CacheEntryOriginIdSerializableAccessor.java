package io.opensphere.mantle.data.cache.impl;

import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.mantle.data.element.MapDataElement;

/**
 * The Class CacheEntryOriginIdSerializableAccessor.
 */
public class CacheEntryOriginIdSerializableAccessor extends SerializableAccessor<CacheEntry, Long>
{
    /** The Constant CACHE_ENTRY_ORIGIN_ID_PROPERTY_NAME. */
    public static final String CACHE_ENTRY_ORIGIN_ID_PROPERTY_NAME = "CacheEntryOrigin.ID";

    /** The data element origin id property descriptor. */
    public static final PropertyDescriptor<Long> CACHE_ENTRY_ORIGIN_ID_PROPERTY_DESCRIPTOR = new PropertyDescriptor<Long>(
            CACHE_ENTRY_ORIGIN_ID_PROPERTY_NAME, Long.class);

    /**
     * Instantiates a new {@link MapDataElement} map geometry support
     * serializable accessor.
     */
    public CacheEntryOriginIdSerializableAccessor()
    {
        super(CACHE_ENTRY_ORIGIN_ID_PROPERTY_DESCRIPTOR);
    }

    @Override
    public Long access(CacheEntry input)
    {
        return input.getLoadedElementData().getOriginId();
    }
}
