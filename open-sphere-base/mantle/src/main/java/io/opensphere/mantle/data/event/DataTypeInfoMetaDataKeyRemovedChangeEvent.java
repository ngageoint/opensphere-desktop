package io.opensphere.mantle.data.event;

import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Events from changes to DataTypeInfo.
 */
public class DataTypeInfoMetaDataKeyRemovedChangeEvent extends AbstractDataTypeInfoChangeEvent
{
    /**
     * Event CTOR.
     *
     * @param dti - the {@link DataTypeInfo}
     * @param key - the key that was removed
     * @param source - the source of the event.
     */
    public DataTypeInfoMetaDataKeyRemovedChangeEvent(DataTypeInfo dti, String key, Object source)
    {
        super(dti, Type.METADATA_KEY_REMOVED, key, source);
    }

    /**
     * Gets the key that was removed.
     *
     * @return the removed key.
     */
    public String getKey()
    {
        return getValue().toString();
    }
}
