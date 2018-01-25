package io.opensphere.mantle.controller.event;

import io.opensphere.mantle.data.AbstractDataGroupInfoChangeEvent;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * The Class AbstractDataGroupControllerEvent.
 */
public abstract class AbstractRootDataGroupControllerEvent extends AbstractDataGroupInfoChangeEvent
{
    /**
     * The originating event that caused this event to be generated, if any.
     */
    private final AbstractDataGroupInfoChangeEvent myOriginatingEvent;

    /**
     * Instantiates a new abstract data group controller event.
     *
     * @param rootGroup the root group that has changed
     * @param originEvent the event that caused this event to be generated ( or
     *            null if none)
     * @param source - the source of the event ( object that caused the event to
     *            be generated )
     */
    public AbstractRootDataGroupControllerEvent(DataGroupInfo rootGroup, AbstractDataGroupInfoChangeEvent originEvent,
            Object source)
    {
        super(rootGroup, source);
        myOriginatingEvent = originEvent;
    }

    /**
     * Gets the origin event that caused this event to be generated or null if
     * none.
     *
     * @return the origin event
     */
    public AbstractDataGroupInfoChangeEvent getOriginEvent()
    {
        return myOriginatingEvent;
    }
}
