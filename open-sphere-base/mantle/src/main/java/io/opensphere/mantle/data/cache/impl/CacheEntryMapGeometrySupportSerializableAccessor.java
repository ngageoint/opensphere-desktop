package io.opensphere.mantle.data.cache.impl;

import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * A {@link SerializableAccessor} that accesses the {@link MapGeometrySupport}
 * that belongs to a {@link CacheEntry}.
 */
public class CacheEntryMapGeometrySupportSerializableAccessor extends SerializableAccessor<CacheEntry, MapGeometrySupport>
{
    /**
     * Instantiates a new {@link CacheEntry} map geometry support serializable
     * accessor.
     */
    public CacheEntryMapGeometrySupportSerializableAccessor()
    {
        super(MapGeometrySupport.PROPERTY_DESCRIPTOR);
    }

    @Override
    public MapGeometrySupport access(CacheEntry input)
    {
        return input.getLoadedElementData() == null ? null : input.getLoadedElementData().getMapGeometrySupport();
    }
}
