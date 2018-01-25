package io.opensphere.mantle.data.element.event;

/**
 * An event that occurs when a data element is double clicked.
 */
public class DataElementDoubleClickedEvent extends AbstractDataElementChangeEvent
{
    /**
     * Controls whether or not the event should be processed by peers.
     */
    private volatile boolean myConsumed;

    /**
     * Instantiates a new data element selected change event.
     *
     * @param regId the registry id
     * @param dtKey the data type key
     * @param source the instigator of the change
     */
    public DataElementDoubleClickedEvent(long regId, String dtKey, Object source)
    {
        super(regId, dtKey, source);
    }

    /**
     * Consumes this event.
     */
    public void consume()
    {
        myConsumed = true;
    }

    /**
     * Returns whether this event has been consumed.
     *
     * @return whether this event has been consumed
     */
    public boolean isConsumed()
    {
        return myConsumed;
    }
}
