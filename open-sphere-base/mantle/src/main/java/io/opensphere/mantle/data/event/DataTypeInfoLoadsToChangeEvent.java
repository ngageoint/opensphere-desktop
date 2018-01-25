package io.opensphere.mantle.data.event;

import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;

/**
 * Events from changes to LoadsTo for a DataTypeInfo.
 */
public class DataTypeInfoLoadsToChangeEvent extends AbstractDataTypeInfoChangeEvent
{
    /** The old loads to. */
    private final LoadsTo myOldLoadsTo;

    /**
     * Event CTOR.
     *
     * @param dti - the {@link DataTypeInfo}
     * @param newLoadsTo the new {@link LoadsTo}
     * @param oldLoadsTo the old {@link LoadsTo}
     * @param source - the source of the event.
     */
    public DataTypeInfoLoadsToChangeEvent(DataTypeInfo dti, LoadsTo newLoadsTo, LoadsTo oldLoadsTo, Object source)
    {
        super(dti, Type.LOADS_TO_CHANGED, newLoadsTo, source);
        myOldLoadsTo = oldLoadsTo;
    }

    /**
     * Gets the color for the change.
     *
     * @return the new color.
     */
    public LoadsTo getLoadsTo()
    {
        return (LoadsTo)getValue();
    }

    /**
     * Gets the color for the change.
     *
     * @return the new color.
     */
    public LoadsTo getOldLoadsTo()
    {
        return myOldLoadsTo;
    }
}
