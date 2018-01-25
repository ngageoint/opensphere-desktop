package io.opensphere.mantle.mp.event.impl;

import io.opensphere.mantle.mp.AbstractMapAnnotationPointGroupChangeEvent;
import io.opensphere.mantle.mp.MutableMapAnnotationPointGroup;
import io.opensphere.mantle.mp.event.AbstractRootMapAnnotationPointRegistryEvent;

/**
 * Signals that a the tree structure of a root group node has changed.
 */
public class RootMapAnnotationPointGroupStructureChangeEvent extends AbstractRootMapAnnotationPointRegistryEvent
{
    /**
     * Indicates that the node structure has changed for the indicated root
     * group instigated by a child somewhere in this root's hierarchy.
     *
     * @param rootGroup the root group that has changed
     * @param originEvent the event that caused this event to be generated ( or
     *            null if none)
     * @param source - the source of the event ( object that caused the event to
     *            be generated )
     */
    public RootMapAnnotationPointGroupStructureChangeEvent(MutableMapAnnotationPointGroup rootGroup,
            AbstractMapAnnotationPointGroupChangeEvent originEvent, Object source)
    {
        super(rootGroup, originEvent, source);
    }

    @Override
    public String getDescription()
    {
        return "RootMapAnnotationPointGroupStructureChangeEvent";
    }
}
