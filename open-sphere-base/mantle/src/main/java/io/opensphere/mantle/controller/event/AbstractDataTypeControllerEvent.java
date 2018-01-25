package io.opensphere.mantle.controller.event;

import io.opensphere.core.event.AbstractSingleStateEvent;
import io.opensphere.core.event.SourceableEvent;

/**
 * The Class AbstractDataTypeControllerEvent.
 */
public abstract class AbstractDataTypeControllerEvent extends AbstractSingleStateEvent implements SourceableEvent
{
    /** The source. */
    private final Object mySource;

    /**
     * Instantiates a new abstract data type controller event.
     *
     * @param source - the source of the event ( object that caused the event to
     *            be generated )
     */
    public AbstractDataTypeControllerEvent(Object source)
    {
        mySource = source;
    }

    @Override
    public Object getSource()
    {
        return mySource;
    }
}
