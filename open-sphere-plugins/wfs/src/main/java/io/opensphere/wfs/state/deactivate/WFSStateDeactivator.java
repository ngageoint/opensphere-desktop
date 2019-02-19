package io.opensphere.wfs.state.deactivate;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.util.registry.GenericRegistry;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DataGroupActivator;
import io.opensphere.mantle.data.impl.DefaultDataGroupActivator;
import io.opensphere.wfs.envoy.AbstractWFSEnvoy;
import io.opensphere.wfs.envoy.WFSEnvoy;
import io.opensphere.wfs.state.model.WFSStateGroup;

/**
 * Handles cleaning up data groups, data types, and associated elements when a
 * WFS state is deactivated.
 */
public class WFSStateDeactivator
{
    /** The Envoy registry. */
    private final GenericRegistry<Envoy> myEnvoyRegistry;

    /** The data group activator. */
    private final DataGroupActivator myDataGroupActivator;

    /**
     * Constructs a new state deactivator.
     *
     * @param toolbox The system toolbox.
     */
    public WFSStateDeactivator(Toolbox toolbox)
    {
        myEnvoyRegistry = toolbox.getEnvoyRegistry();
        myDataGroupActivator = new DefaultDataGroupActivator(toolbox.getEventManager());
    }

    /**
     * Deactivates the state. Get the set of envoys and deactivate the types
     * that are associated with a particular envoy. For example, use the server1
     * envoy to deactivate all server1 data types.
     *
     * @param stateGroup The state group to deactivate.
     * @throws InterruptedException If the thread is interrupted.
     */
    public void deactivateState(WFSStateGroup stateGroup) throws InterruptedException
    {
        List<DataGroupInfo> dgiList = stateGroup.getDataGroups();
        myDataGroupActivator.setGroupsActive(dgiList, false);

        Collection<WFSEnvoy> envoys = myEnvoyRegistry.getObjectsOfClass(WFSEnvoy.class);
        Iterator<WFSEnvoy> iter = envoys.iterator();
        while (iter.hasNext())
        {
            deactivateGroup(stateGroup.getDataGroups(), iter.next());
        }
    }

    /**
     * Deactivate group.
     *
     * @param list the list of groups to check for deactivation.
     * @param envoy the envoy
     */
    private void deactivateGroup(List<DataGroupInfo> list, AbstractWFSEnvoy envoy)
    {
        for (DataGroupInfo dgi : list)
        {
            for (DataTypeInfo dti : dgi.getMembers(false))
            {
                if (envoy.getGetCapabilitiesURL().equals(dti.getUrl()))
                {
                    if (envoy instanceof WFSEnvoy)
                    {
                        ((WFSEnvoy)envoy).deactivateState();
                    }
                }
            }
        }
    }
}
