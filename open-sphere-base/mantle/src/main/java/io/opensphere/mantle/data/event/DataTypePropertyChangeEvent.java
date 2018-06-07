package io.opensphere.mantle.data.event;

import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.DataTypeInfo;

/** Event for an arbitrary data type property change. */
public class DataTypePropertyChangeEvent extends AbstractDataTypeInfoChangeEvent
{
    /** The property that changed. */
    private final String myProperty;

    /**
     * Constructor.
     *
     * @param dti - the {@link DataTypeInfo}
     * @param property the name of the property that changed
     * @param value - the value that changed if appropriate.
     * @param source - the source of the event.
     */
    public DataTypePropertyChangeEvent(DataTypeInfo dti, String property, Object value, Object source)
    {
        super(dti, null, value, source);
        myProperty = property;
    }

    /**
     * Gets the property.
     *
     * @return the property
     */
    public String getProperty()
    {
        return myProperty;
    }
}
