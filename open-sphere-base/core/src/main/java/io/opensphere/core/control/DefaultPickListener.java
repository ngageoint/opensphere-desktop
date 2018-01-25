package io.opensphere.core.control;

/**
 * A pick listener that simply keeps a reference to the most recent pick event.
 */
public class DefaultPickListener implements PickListener
{
    /** Reference to the latest pick event. */
    private volatile PickEvent myLatestEvent;

    /**
     * Get the latest pick event.
     *
     * @return The latest pick event.
     */
    public PickEvent getLatestEvent()
    {
        return myLatestEvent;
    }

    @Override
    public void handlePickEvent(PickEvent evt)
    {
        myLatestEvent = evt;
    }
}
