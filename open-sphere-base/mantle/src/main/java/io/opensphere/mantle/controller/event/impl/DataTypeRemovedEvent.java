package io.opensphere.mantle.controller.event.impl;

import io.opensphere.core.util.Utilities;
import io.opensphere.mantle.controller.event.AbstractDataTypeControllerEvent;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class DataTypeRemovedEvent.
 */
public class DataTypeRemovedEvent extends AbstractDataTypeControllerEvent
{
    /** The data type info. */
    private final DataTypeInfo myRemovedDataTypeInfo;

    /**
     * Instantiates a new DataTypeRemovedEvent.
     *
     * @param dti the {@link DataTypeInfo} that was removed.
     * @param source the source of the remove.
     */
    public DataTypeRemovedEvent(DataTypeInfo dti, Object source)
    {
        super(source);
        Utilities.checkNull(dti, "dti");
        myRemovedDataTypeInfo = dti;
    }

    /**
     * Gets the data type.
     *
     * @return the data type
     */
    public DataTypeInfo getDataType()
    {
        return myRemovedDataTypeInfo;
    }

    @Override
    public String getDescription()
    {
        return "DataTypeInfo removed event from " + (getSource() == null ? "UNKNOWN" : getSource().getClass().getSimpleName());
    }
}
