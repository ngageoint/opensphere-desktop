package io.opensphere.mantle.data.element.event;

import io.opensphere.core.event.AbstractSingleStateEvent;
import io.opensphere.core.event.SourceableEvent;

/**
 * The Class AbstractDataElementChangeEvent.
 */
public abstract class AbstractDataElementChangeEvent extends AbstractSingleStateEvent implements SourceableEvent
{
    /** The data type key. */
    private final String myDataTypeKey;

    /** The registry id for the DataElement. */
    private final long myRegistryId;

    /** The instigator of the change. */
    private final Object mySource;

    /**
     * Instantiates a new abstract data element change event.
     *
     * @param regId the registry id for the data element
     * @param dtKey the dt key the data type key
     * @param source the source the instigator of the event
     */
    public AbstractDataElementChangeEvent(long regId, String dtKey, Object source)
    {
        super();
        myRegistryId = regId;
        mySource = source;
        myDataTypeKey = dtKey;
    }

    /**
     * Gets the data type key.
     *
     * @return the data type key
     */
    public String getDataTypeKey()
    {
        return myDataTypeKey;
    }

    @Override
    public String getDescription()
    {
        return "Change to DataElement";
    }

    /**
     * Gets the registry id.
     *
     * @return the registry id
     */
    public long getRegistryId()
    {
        return myRegistryId;
    }

    @Override
    public Object getSource()
    {
        return mySource;
    }
}
