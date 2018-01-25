package io.opensphere.mantle.data.accessor;

import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * A {@link SerializableAccessor} that accesses the {@link MapGeometrySupport}
 * that belongs to a {@link MapDataElement}.
 */
public class MDEMapGeometrySupportSerializableAccessor extends SerializableAccessor<MapDataElement, MapGeometrySupport>
{
    /**
     * Instantiates a new {@link MapDataElement} map geometry support
     * serializable accessor.
     */
    public MDEMapGeometrySupportSerializableAccessor()
    {
        super(MapGeometrySupport.PROPERTY_DESCRIPTOR);
    }

    @Override
    public MapGeometrySupport access(MapDataElement input)
    {
        return input.getMapGeometrySupport();
    }
}
