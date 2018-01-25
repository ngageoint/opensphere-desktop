package io.opensphere.mantle.mp.impl;

import io.opensphere.mantle.mp.MapAnnotationPointGroupTreeNodeData;
import io.opensphere.mantle.mp.MutableMapAnnotationPointGroup;

/**
 * Default implementation of GroupInfoTreeNodeData.
 */
public class DefaultMapAnnotationPointGroupTreeNodeData implements MapAnnotationPointGroupTreeNodeData
{
    /** The my group. */
    private final MutableMapAnnotationPointGroup myGroup;

    /**
     * Instantiates a new default map annotation point group tree node data.
     *
     * @param group the group
     */
    public DefaultMapAnnotationPointGroupTreeNodeData(MutableMapAnnotationPointGroup group)
    {
        myGroup = group;
    }

    @Override
    public MutableMapAnnotationPointGroup getGroup()
    {
        return myGroup;
    }

    @Override
    public String getName()
    {
        return myGroup.getName();
    }

    @Override
    public String toString()
    {
        return myGroup.getName();
    }
}
