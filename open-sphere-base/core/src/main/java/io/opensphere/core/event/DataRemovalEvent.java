package io.opensphere.core.event;

/**
 * The Class DataRemovalEvent.
 */
public class DataRemovalEvent extends AbstractSingleStateEvent
{
    /** The Object that originated this event. */
    private final Object mySource;

    /**
     * Instantiates a new data removal event.
     *
     * @param source the requesting source
     */
    public DataRemovalEvent(Object source)
    {
        mySource = source;
    }

    @Override
    public String getDescription()
    {
        return "Event that serves as a request to remove all data";
    }

    /**
     * Gets the originating source of this event.
     *
     * @return the source that initiated this event
     */
    public Object getSource()
    {
        return mySource;
    }
}
