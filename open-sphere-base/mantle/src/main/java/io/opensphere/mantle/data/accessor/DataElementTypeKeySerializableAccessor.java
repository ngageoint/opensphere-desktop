package io.opensphere.mantle.data.accessor;

import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;

/**
 * The Class DataElementTypeKeySerializableAccessor.
 */
public class DataElementTypeKeySerializableAccessor extends SerializableAccessor<DataElement, String>
{
    /**
     * Instantiates a new {@link MapDataElement} map geometry support
     * serializable accessor.
     */
    public DataElementTypeKeySerializableAccessor()
    {
        super(DataElement.DATA_TYPE_KEY_PROPERTY_DESCRIPTOR);
    }

    @Override
    public String access(DataElement input)
    {
        return input.getDataTypeInfo() == null ? "NULL" : input.getDataTypeInfo().getTypeKey();
    }
}
