package io.opensphere.mantle.mp.event.impl;

import io.opensphere.mantle.mp.AbstractMapAnnotationPointGroupChangeEvent;
import io.opensphere.mantle.mp.MutableMapAnnotationPointGroup;

/**
 * The Class MapAnnotationPointGroupChildrenClearedEvent.
 */
public class MapAnnotationPointGroupChildrenClearedEvent extends AbstractMapAnnotationPointGroupChangeEvent
{
    /**
     * Instantiates a new data group info children cleared event.
     *
     * @param group the group
     * @param source the source
     */
    public MapAnnotationPointGroupChildrenClearedEvent(MutableMapAnnotationPointGroup group, Object source)
    {
        super(group, source);
    }

    @Override
    public String getDescription()
    {
        return "MapAnnotationPointGroupChildrenClearedEvent";
    }
}
