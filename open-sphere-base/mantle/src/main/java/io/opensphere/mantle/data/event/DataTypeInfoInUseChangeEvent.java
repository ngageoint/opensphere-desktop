package io.opensphere.mantle.data.event;

import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Events from changes to DataTypeInfo.
 */
public class DataTypeInfoInUseChangeEvent extends AbstractDataTypeInfoChangeEvent
{
    /**
     * Event CTOR.
     *
     * @param dti - the {@link DataTypeInfo}
     * @param inUse - true if in use, false if no longer in use
     * @param source - the source of the event.
     */
    public DataTypeInfoInUseChangeEvent(DataTypeInfo dti, boolean inUse, Object source)
    {
        super(dti, inUse ? Type.SOURCE_IN_USE : Type.SOURCE_NO_LONGER_IN_USE, null, source);
    }

    /**
     * Gets if in use.
     *
     * @return true if in use, false if no longer in use.
     */
    public boolean isInUse()
    {
        return getType() == Type.SOURCE_IN_USE;
    }
}
