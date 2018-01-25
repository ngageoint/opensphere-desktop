package io.opensphere.mantle.controller.event.impl;

import io.opensphere.core.util.Utilities;
import io.opensphere.mantle.controller.event.AbstractDataTypeControllerEvent;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class CurrentDataTypeChangedEvent.
 */
public class CurrentDataTypeChangedEvent extends AbstractDataTypeControllerEvent
{
    /** The data type info. */
    private final DataTypeInfo myNewType;

    /**
     * Instantiates a new CurrentDataTypeChangedEvent.
     *
     * @param dti the new current {@link DataTypeInfo}.
     * @param source the source of the chagne.
     */
    public CurrentDataTypeChangedEvent(DataTypeInfo dti, Object source)
    {
        super(source);
        Utilities.checkNull(dti, "dti");
        myNewType = dti;
    }

    @Override
    public String getDescription()
    {
        return "Current Data Type Changed to " + (myNewType == null ? "NONE" : myNewType.getDisplayName()) + " by"
                + (getSource() == null ? "UNKNOWN" : getSource().getClass().getSimpleName());
    }

    /**
     * Gets the data type.
     *
     * @return the data type
     */
    public DataTypeInfo getNewType()
    {
        return myNewType;
    }
}
