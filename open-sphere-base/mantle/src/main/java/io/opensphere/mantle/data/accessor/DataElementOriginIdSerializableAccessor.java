package io.opensphere.mantle.data.accessor;

import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;

/**
 * The Class DataElementTypeKeySerializableAccessor.
 */
public class DataElementOriginIdSerializableAccessor extends SerializableAccessor<DataElement, Long>
{
    /**
     * Instantiates a new {@link MapDataElement} map geometry support
     * serializable accessor.
     */
    public DataElementOriginIdSerializableAccessor()
    {
        super(DataElement.DATA_ELEMENT_ORIGIN_ID_PROPERTY_DESCRIPTOR);
    }

    @Override
    public Long access(DataElement input)
    {
        return input.getId();
    }
}
