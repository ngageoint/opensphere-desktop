package io.opensphere.mantle.mp.event.impl;

import io.opensphere.mantle.mp.AbstractMapAnnotationPointGroupChangeEvent;
import io.opensphere.mantle.mp.MutableMapAnnotationPointGroup;

/**
 * The Class MapAnnotationPointGroupChildRemovedEvent.
 */
public class MapAnnotationPointGroupChildRemovedEvent extends AbstractMapAnnotationPointGroupChangeEvent
{
    /** The added member. */
    private final MutableMapAnnotationPointGroup myRemovedChild;

    /**
     * Instantiates a new data group info child added event.
     *
     * @param group the MapAnnotationPointGroup from which the child was
     *            removed.
     * @param removed the added child
     * @param source - the source of the event
     */
    public MapAnnotationPointGroupChildRemovedEvent(MutableMapAnnotationPointGroup group, MutableMapAnnotationPointGroup removed,
            Object source)
    {
        super(group, source);
        myRemovedChild = removed;
    }

    @Override
    public String getDescription()
    {
        return "MapAnnotationPointGroupChildRemovedEvent";
    }

    /**
     * Gets the removed child.
     *
     * @return the removed child
     */
    public MutableMapAnnotationPointGroup getRemoved()
    {
        return myRemovedChild;
    }
}
