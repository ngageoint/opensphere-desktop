package io.opensphere.mantle.mp.event;

import io.opensphere.mantle.mp.AbstractMapAnnotationPointGroupChangeEvent;
import io.opensphere.mantle.mp.MutableMapAnnotationPointGroup;

/**
 * The Class AbstractDataGroupControllerEvent.
 */
public abstract class AbstractRootMapAnnotationPointRegistryEvent extends AbstractMapAnnotationPointGroupChangeEvent
{
    /**
     * The originating event that caused this event to be generated, if any.
     */
    private final AbstractMapAnnotationPointGroupChangeEvent myOriginatingEvent;

    /**
     * Instantiates a new abstract root map annotation point registry event.
     *
     * @param rootGroup the root group
     * @param originEvent the origin event
     * @param source the source
     */
    public AbstractRootMapAnnotationPointRegistryEvent(MutableMapAnnotationPointGroup rootGroup,
            AbstractMapAnnotationPointGroupChangeEvent originEvent, Object source)
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
    public AbstractMapAnnotationPointGroupChangeEvent getOriginEvent()
    {
        return myOriginatingEvent;
    }
}
