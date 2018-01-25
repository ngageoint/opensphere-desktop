package io.opensphere.server.control;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.server.services.AbstractServerDataTypeInfo;
import io.opensphere.server.services.ServerDataTypeSync;

/**
 * The Class DefaultServerDataGroupInfo.
 */
public class DefaultServerDataGroupInfo extends DefaultDataGroupInfo
{
    /** Object used to keep this group's members in sync. */
    private final ServerDataTypeSync myTypeSync = new ServerDataTypeSync();

    /**
     * CTOR for group info with id for the group. Note: Display name will be set
     * to id initially.
     *
     * @param rootNode - true if this is a root level node ( root nodes cannot
     *            have parents set )
     * @param aToolbox the toolbox
     * @param id - the id for the group
     */
    public DefaultServerDataGroupInfo(boolean rootNode, Toolbox aToolbox, String id)
    {
        super(rootNode, aToolbox, "OGC Server", id);
    }

    /**
     * Instantiates a new default data group info.
     *
     * @param rootNode - true if this is a root level node ( root nodes cannot
     *            have parents set )
     * @param aToolbox the toolbox
     * @param id the id for the group.
     * @param displayName the display name
     */
    public DefaultServerDataGroupInfo(boolean rootNode, Toolbox aToolbox, String id, String displayName)
    {
        super(rootNode, aToolbox, "OGC Server", id, displayName);
    }

    @Override
    public void addMember(DataTypeInfo dti, Object source)
    {
        if (dti instanceof AbstractServerDataTypeInfo)
        {
            AbstractServerDataTypeInfo serverInfo = (AbstractServerDataTypeInfo)dti;
            serverInfo.setTypeSync(myTypeSync);
        }
        else
        {
            throw new IllegalArgumentException("Unsupported DataTypeInfo class type: " + dti.getClass().getSimpleName());
        }
        super.addMember(dti, source);
    }

    /**
     * Gets the type sync.
     *
     * @return the type sync
     */
    public ServerDataTypeSync getTypeSync()
    {
        return myTypeSync;
    }
}
