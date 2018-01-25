package io.opensphere.mantle.data.event;

import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Events from changes to DataTypeInfo.
 */
public class DataTypeInfoMetaDataKeyAddedChangeEvent extends AbstractDataTypeInfoChangeEvent
{
    /**
     * Event CTOR.
     *
     * @param dti - the {@link DataTypeInfo}
     * @param key - the key that has been added.
     * @param source - the source of the event.
     */
    public DataTypeInfoMetaDataKeyAddedChangeEvent(DataTypeInfo dti, String key, Object source)
    {
        super(dti, Type.METADATA_KEY_ADDED, key, source);
    }

    /**
     * Gets the key that has been added.
     *
     * @return the key
     */
    public String getKey()
    {
        return getValue().toString();
    }
}
