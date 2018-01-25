package io.opensphere.mantle.mp.event.impl;

import io.opensphere.mantle.mp.MutableMapAnnotationPointGroup;
import io.opensphere.mantle.mp.event.AbstractRootMapAnnotationPointRegistryEvent;

/**
 * Signals that a new root data group has been added to the controller.
 */
public class RootMapAnnotationPointGroupAddedEvent extends AbstractRootMapAnnotationPointRegistryEvent
{
    /**
     * Instantiates a new RootMapAnnotationPointGroupAddedEvent.
     *
     * @param addedRoot the root group that has been added
     * @param source - the source of the event ( object that caused the event to
     *            be generated )
     */
    public RootMapAnnotationPointGroupAddedEvent(MutableMapAnnotationPointGroup addedRoot, Object source)
    {
        super(addedRoot, null, source);
    }

    @Override
    public String getDescription()
    {
        return "RootMapAnnotationPointGroupAddedEvent";
    }
}
