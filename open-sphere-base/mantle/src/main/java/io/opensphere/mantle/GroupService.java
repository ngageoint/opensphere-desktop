package io.opensphere.mantle;

import io.opensphere.core.util.CompositeService;
import io.opensphere.mantle.data.DataGroupInfo;

/** Service that creates/destroys a data group. */
public class GroupService extends CompositeService
{
    /** The parent group. */
    private final DataGroupInfo myParentGroup;

    /** The group managed by this service. */
    private final DataGroupInfo myGroup;

    /**
     * Constructor.
     *
     * @param parent The parent data group
     * @param group The data group to manage
     */
    public GroupService(DataGroupInfo parent, DataGroupInfo group)
    {
        super();
        myParentGroup = parent;
        myGroup = group;
    }

    @Override
    public void open()
    {
        super.open();
        myParentGroup.addChild(myGroup, this);
//        myGroup.activationProperty().setActive(true);
    }

    @Override
    public void close()
    {
//        myGroup.activationProperty().setActive(false);
        myParentGroup.removeChild(myGroup, this);
        super.close();
    }

    /**
     * Gets the group.
     *
     * @return the group
     */
    public DataGroupInfo getGroup()
    {
        return myGroup;
    }
}
