package io.opensphere.mantle.data.impl;

import io.opensphere.core.Toolbox;

/** Extension to {@link DefaultDataGroupInfo} that allows user deletion. */
public class DefaultDeletableDataGroupInfo extends DefaultDataGroupInfo
{
    /**
     * CTOR for group info with id for the group. Note: Display name will be set
     * to id initially.
     *
     * @param rootNode - true if this is a root level node ( root nodes cannot
     *            have parents set )
     * @param aToolbox the toolbox
     * @param providerType the provider type
     * @param id - the id for the group
     */
    public DefaultDeletableDataGroupInfo(boolean rootNode, Toolbox aToolbox, String providerType, String id)
    {
        super(rootNode, aToolbox, providerType, id);
    }

    /**
     * Instantiates a new default data group info.
     *
     * @param rootNode - true if this is a root level node ( root nodes cannot
     *            have parents set )
     * @param aToolbox the toolbox
     * @param providerType the provider type
     * @param id the id for the group.
     * @param displayName the display name
     */
    public DefaultDeletableDataGroupInfo(boolean rootNode, Toolbox aToolbox, String providerType, String id, String displayName)
    {
        super(rootNode, aToolbox, providerType, id, displayName);
    }

    @Override
    public boolean userDeleteControl()
    {
        return true;
    }
}
