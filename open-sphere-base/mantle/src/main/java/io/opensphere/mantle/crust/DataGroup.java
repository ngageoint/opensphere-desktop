package io.opensphere.mantle.crust;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;

/**
 * Trivial subclass of DDGI with more friendly constructor.
 */
public class DataGroup extends DefaultDataGroupInfo
{
    /**
     * Creates a new data group.
     *
     * @param parentGroup the group to which the new instance belongs.
     * @param groupId the group ID of the new instance.
     * @param toolbox the toolbox through which application state is accessed.
     */
    public DataGroup(DataGroupInfo parentGroup, String groupId, Toolbox toolbox)
    {
        super(false, toolbox, parentGroup.getId(), groupId, groupId);
    }

    // override to return true and we show a "trash" icon to delete the
    // layer from within the layer tree
    // NOTE: this button really causes the thing to be deleted, but does
    // not cause a callback (i.e., to clear the elements from the cache);
    // for the time being, do not use it.
    /* @Override public boolean userDeleteControl () { return true; } */
}
