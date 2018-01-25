package io.opensphere.myplaces.models;

import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The event indicating information about the my places objects has changed.
 *
 */
public class DataTypeInfoMyPlaceChangedEvent extends AbstractDataTypeInfoChangeEvent
{
    /**
     * Constructs a new event.
     *
     * @param dti The data type info that changed.
     * @param source The source object of the change.
     */
    public DataTypeInfoMyPlaceChangedEvent(DataTypeInfo dti, Object source)
    {
        super(dti, Type.REBUILD_GEOMETRY_REQUEST, source);
    }
}
