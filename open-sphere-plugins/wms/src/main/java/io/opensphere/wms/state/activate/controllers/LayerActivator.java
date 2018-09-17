package io.opensphere.wms.state.activate.controllers;

import java.util.List;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.impl.DataGroupActivator;
import io.opensphere.wms.state.model.StateGroup;

/**
 * Activates the list of layers built from a state.
 */
public class LayerActivator
{
    /** The activator. */
    private final DataGroupActivator myActivator;

    /**
     * Constructs a new layer activator.
     *
     * @param activator The activator.
     */
    public LayerActivator(DataGroupActivator activator)
    {
        myActivator = activator;
    }

    /**
     * Activates the list of layers.
     *
     * @param stateId The id of the state being activated.
     * @param dataGroups The data groups to activate.
     * @param isDataLayers true if activating wms data layers, false if just map
     *            layers.
     * @return The state group containing the activated layers and a state id.
     * @throws InterruptedException If the thread is interrupted.
     */
    public StateGroup activateLayers(String stateId, List<DataGroupInfo> dataGroups, boolean isDataLayers)
        throws InterruptedException
    {
        List<DataGroupInfo> toActivate = New.list();
        List<DataGroupInfo> toReactivate = New.list();
        for (DataGroupInfo dataGroup : dataGroups)
        {
            if (!isDataLayers || dataGroup.getMembers(false).size() >= 2)
            {
                if (dataGroup.activationProperty().isActiveOrActivating())
                {
                    toReactivate.add(dataGroup);
                }
                else
                {
                    toActivate.add(dataGroup);
                }
            }
        }

        myActivator.setGroupsActive(toActivate, true);
        myActivator.reactivateGroups(toReactivate);

        return new StateGroup(stateId, dataGroups);
    }
}
