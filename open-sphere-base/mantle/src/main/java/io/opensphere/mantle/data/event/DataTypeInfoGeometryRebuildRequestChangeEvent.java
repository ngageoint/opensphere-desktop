package io.opensphere.mantle.data.event;

import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Events from changes to DataTypeInfo.
 */
public class DataTypeInfoGeometryRebuildRequestChangeEvent extends AbstractDataTypeInfoChangeEvent
{
    /**
     * Event CTOR.
     *
     * @param dti - the {@link DataTypeInfo}
     * @param source - the source of the event.
     */
    public DataTypeInfoGeometryRebuildRequestChangeEvent(DataTypeInfo dti, Object source)
    {
        super(dti, Type.REBUILD_GEOMETRY_REQUEST, source);
    }

    /**
     * Checks if this is geometry rebuild request.
     *
     * @return true, if is geometry rebuild request
     */
    public boolean isGeometryRebuildRequest()
    {
        return true;
    }
}
