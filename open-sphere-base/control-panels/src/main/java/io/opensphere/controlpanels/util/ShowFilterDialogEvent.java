package io.opensphere.controlpanels.util;

import io.opensphere.core.event.AbstractSingleStateEvent;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Event indicating that the filter dialog should be shown.
 */
public class ShowFilterDialogEvent extends AbstractSingleStateEvent
{
    /**
     * The data type to display the filter builder for.
     */
    private final DataTypeInfo myDataType;

    /** The key for the data type that is to be filtered. */
    private final String myTypeKey;

    /**
     * Constructs an event with the data type.
     *
     * @param dataType The data type to show the filter builder for.
     */
    public ShowFilterDialogEvent(DataTypeInfo dataType)
    {
        myDataType = dataType;
        myTypeKey = null;
    }

    /**
     * Construct the event.
     *
     * @param typeKey The type key.
     */
    public ShowFilterDialogEvent(String typeKey)
    {
        myTypeKey = typeKey;
        myDataType = null;
    }

    /**
     * Gets the data type to show the filter builder for.
     *
     * @return The data type.
     */
    public DataTypeInfo getDataType()
    {
        return myDataType;
    }

    @Override
    public String getDescription()
    {
        return "Indicates that the filter dialog should be shown.";
    }

    /**
     * Get the key for the data type that is to be filtered.
     *
     * @return The key.
     */
    public String getTypeKey()
    {
        return myTypeKey;
    }
}
