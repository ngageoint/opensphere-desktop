package io.opensphere.mantle.data.event;

import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.SpecialKey;

/**
 * Events from changes to DataTypeInfo.
 */
public class DataTypeInfoMetaDataSpecialKeyChangeEvent extends AbstractDataTypeInfoChangeEvent
{
    /** The new key name. */
    private final String myNewKeyName;

    /** The old key name. */
    private final String myOldKeyName;

    /** The old special key. */
    private final SpecialKey myOldSpecialKey;

    /**
     * Event CTOR.
     *
     * @param dti - the {@link DataTypeInfo}
     * @param newSpecialKeyName the new special key name
     * @param newSpecialKey the new special key
     * @param oldSpecialKeyName the old special key name
     * @param oldSpecialKey the old special key
     * @param source - the source of the event.
     */
    public DataTypeInfoMetaDataSpecialKeyChangeEvent(DataTypeInfo dti, String newSpecialKeyName, SpecialKey newSpecialKey,
            String oldSpecialKeyName, SpecialKey oldSpecialKey, Object source)
    {
        super(dti, Type.METADATA_SPECIAL_KEY_CHANGED, newSpecialKey, source);
        myOldSpecialKey = oldSpecialKey;
        myOldKeyName = oldSpecialKeyName;
        myNewKeyName = newSpecialKeyName;
    }

    /**
     * Gets the special type that has changed.
     *
     * @return the special type.
     */
    public SpecialKey getNewSpecialKey()
    {
        return (SpecialKey)getValue();
    }

    /**
     * Gets the new special key name.
     *
     * @return the new special key name
     */
    public String getNewSpecialKeyName()
    {
        return myNewKeyName;
    }

    /**
     * Gets the old special key.
     *
     * @return the old special key
     */
    public SpecialKey getOldSpecialKey()
    {
        return myOldSpecialKey;
    }

    /**
     * Gets the old special key name.
     *
     * @return the old special key name
     */
    public String getOldSpecialKeyName()
    {
        return myOldKeyName;
    }
}
