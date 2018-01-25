package io.opensphere.mantle.data.event;

import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.TimeExtents;

/**
 * Events from changes to DataTypeInfo.
 */
public class DataTypeTimeExtentsChangeEvent extends AbstractDataTypeInfoChangeEvent
{
    /** The old extents. */
    private final TimeExtents myOldExtents;

    /**
     * Event CTOR.
     *
     * @param dti - the {@link DataTypeInfo}
     * @param newExtents the new {@link TimeExtents}
     * @param oldExtents the old {@link TimeExtents}
     * @param source - the source of the event.
     */
    public DataTypeTimeExtentsChangeEvent(DataTypeInfo dti, TimeExtents newExtents, TimeExtents oldExtents, Object source)
    {
        super(dti, Type.TIME_EXTENTS_CHANGED, newExtents, source);
        myOldExtents = oldExtents;
    }

    /**
     * Gets the new time extents.
     *
     * @return the new time extents.
     */
    public TimeExtents getExtents()
    {
        return getValue() instanceof TimeExtents ? (TimeExtents)getValue() : null;
    }

    /**
     * Gets the old extents.
     *
     * @return the old extents
     */
    public TimeExtents getOldExtents()
    {
        return myOldExtents;
    }
}
