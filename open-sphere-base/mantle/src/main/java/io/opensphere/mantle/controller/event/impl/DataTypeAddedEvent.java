package io.opensphere.mantle.controller.event.impl;

import io.opensphere.core.util.Utilities;
import io.opensphere.mantle.controller.event.AbstractDataTypeControllerEvent;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class DataTypeAddedEvent.
 */
public class DataTypeAddedEvent extends AbstractDataTypeControllerEvent
{
    /** The data type info. */
    private final DataTypeInfo myDataTypeInfo;

    /**
     * Instantiates a new DataTypeAddedEvent.
     *
     * @param dti the {@link DataTypeInfo} that was added.
     * @param source the source of the addition.
     */
    public DataTypeAddedEvent(DataTypeInfo dti, Object source)
    {
        super(source);
        Utilities.checkNull(dti, "dataTypeInfo");
        myDataTypeInfo = dti;
    }

    /**
     * Gets the data type.
     *
     * @return the data type
     */
    public DataTypeInfo getDataType()
    {
        return myDataTypeInfo;
    }

    @Override
    public String getDescription()
    {
        return "DataTypeInfo added event from " + (getSource() == null ? "UNKNOWN" : getSource().getClass().getSimpleName());
    }
}
