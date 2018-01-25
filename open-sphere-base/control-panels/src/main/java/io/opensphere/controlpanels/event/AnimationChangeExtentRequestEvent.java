package io.opensphere.controlpanels.event;

import io.opensphere.core.event.AbstractSingleStateEvent;
import io.opensphere.core.event.SourceableEvent;
import io.opensphere.core.model.time.TimeSpan;

/**
 * The Class AnimationChangeExtentRequestEvent.
 */
public class AnimationChangeExtentRequestEvent extends AbstractSingleStateEvent implements SourceableEvent
{
    /** The my extent. */
    private final TimeSpan myExtent;

    /** The source. */
    private final Object mySource;

    /**
     * Instantiates a new discovery ui request focus event.
     *
     * @param extent the extent
     * @param source the source
     */
    public AnimationChangeExtentRequestEvent(TimeSpan extent, Object source)
    {
        mySource = source;
        myExtent = extent;
    }

    @Override
    public String getDescription()
    {
        return "Request animation extent be changed";
    }

    /**
     * Gets the extent.
     *
     * @return the extent
     */
    public TimeSpan getExtent()
    {
        return myExtent;
    }

    @Override
    public Object getSource()
    {
        return mySource;
    }
}
