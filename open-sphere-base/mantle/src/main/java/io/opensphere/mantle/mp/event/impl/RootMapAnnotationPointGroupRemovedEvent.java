package io.opensphere.mantle.mp.event.impl;

import io.opensphere.mantle.mp.AbstractMapAnnotationPointGroupChangeEvent;
import io.opensphere.mantle.mp.MutableMapAnnotationPointGroup;
import io.opensphere.mantle.mp.event.AbstractRootMapAnnotationPointRegistryEvent;

/**
 * Signals that a root data group has been removed from the controller.
 */
public class RootMapAnnotationPointGroupRemovedEvent extends AbstractRootMapAnnotationPointRegistryEvent
{
    /**
     * Instantiates a new RootMapAnnotationPointGroupRemovedEvent.
     *
     * @param removedRoot the root group that has been removed
     * @param originEvent the event that caused this event to be generated ( or
     *            null if none)
     * @param source - the source of the event ( object that caused the event to
     *            be generated )
     */
    public RootMapAnnotationPointGroupRemovedEvent(MutableMapAnnotationPointGroup removedRoot,
            AbstractMapAnnotationPointGroupChangeEvent originEvent, Object source)
    {
        super(removedRoot, originEvent, source);
    }

    @Override
    public String getDescription()
    {
        return "RootMapAnnotationPointGroupRemovedEvent";
    }
}
