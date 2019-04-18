package io.opensphere.wms.state.activate;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.core.Plugin;
import io.opensphere.core.Toolbox;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupActivator;
import io.opensphere.server.state.utilities.ServerStateUtilities;
import io.opensphere.server.toolbox.ServerToolbox;
import io.opensphere.server.toolbox.ServerToolboxUtils;
import io.opensphere.wms.state.activate.controllers.EnvoyCoupler;
import io.opensphere.wms.state.activate.controllers.LayerActivator;
import io.opensphere.wms.state.activate.controllers.NodeReader;
import io.opensphere.wms.state.activate.controllers.SaveStateV4ToV3Translator;
import io.opensphere.wms.state.model.StateGroup;
import io.opensphere.wms.state.model.WMSEnvoyAndState;
import io.opensphere.wms.state.model.WMSLayerState;

/**
 * Activates the layers saved within a saved state.
 */
public class StateActivator
{
    /**
     * Reads the node and exstracts the wms layer states from it.
     */
    private final NodeReader myNodeReader;

    /**
     * Couples existing layer envoys to saved layer states.
     */
    private final EnvoyCoupler myEnvoyCoupler;

    /**
     * Activates the new layers.
     */
    private final LayerActivator myActivator;

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a new state activator.
     *
     * @param wmsPlugin The WMS plugin used to get the get capabilities envoys.
     * @param toolbox The system toolbox.
     */
    public StateActivator(Plugin wmsPlugin, Toolbox toolbox)
    {
        myNodeReader = new NodeReader();
        DataGroupController dataGroupController = toolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class)
                .getDataGroupController();
        myEnvoyCoupler = new EnvoyCoupler(wmsPlugin, toolbox.getEnvoyRegistry(), dataGroupController);
        myActivator = new LayerActivator(new DefaultDataGroupActivator(toolbox.getEventManager()));
        myToolbox = toolbox;
    }

    /**
     * Activates the layers saved within the specified saved state node.
     *
     * @param stateId The id of the state to activate.
     * @param node The saved state node containing the saved layers.
     * @param activateDataLayer True if WMS data layers should be activated,
     *            false if WMS map layers should be activated.
     * @param tags Tags to append to the new data types.
     * @return A StateGroup containing the activated layers and the id of the
     *         state they were saved in.
     * @throws InterruptedException If the thread is interrupted.
     */
    public StateGroup activateState(String stateId, Node node, boolean activateDataLayer, Collection<? extends String> tags)
        throws InterruptedException
    {
        ServerToolbox serverToolbox = ServerToolboxUtils.getServerToolbox(myToolbox);
        serverToolbox.getServerStateController().activateServers(node);

        List<WMSLayerState> states = myNodeReader.readNode(node, activateDataLayer);
        return activateStates(stateId, activateDataLayer, tags, states);
    }

    /**
     * Activates the layers saved within the specified saved state node.
     *
     * @param stateId The id of the state to activate.
     * @param state The saved state object containing the saved layers.
     * @param activateDataLayer True if WMS data layers should be activated,
     *            false if WMS map layers should be activated.
     * @param tags Tags to append to the new data types.
     * @return A StateGroup containing the activated layers and the id of the
     *         state they were saved in.
     * @throws InterruptedException If the thread is interrupted.
     */
    public StateGroup activateState(String stateId, StateType state, boolean activateDataLayer, Collection<? extends String> tags)
        throws InterruptedException
    {
        ServerToolbox serverToolbox = ServerToolboxUtils.getServerToolbox(myToolbox);
        serverToolbox.getServerStateController().activateServers(state);

        List<WMSLayerState> states = ServerStateUtilities.getWmsLayers(state, activateDataLayer).stream()
                .map(SaveStateV4ToV3Translator::toLayerType).collect(Collectors.toList());
        return activateStates(stateId, activateDataLayer, tags, states);
    }

    /**
     * Activates the layers saved within the specified saved state node.
     *
     * @param stateId The id of the state to activate.
     * @param activateDataLayer True if WMS data layers should be activated,
     *            false if WMS map layers should be activated.
     * @param tags Tags to append to the new data types.
     * @param states The layer states to activate.
     * @return A StateGroup containing the activated layers and the id of the
     *         state they were saved in.
     * @throws InterruptedException If the thread is interrupted.
     */
    private StateGroup activateStates(String stateId, boolean activateDataLayer, Collection<? extends String> tags,
            Collection<? extends WMSLayerState> states)
        throws InterruptedException
    {
        List<WMSEnvoyAndState> envoyAndStates = myEnvoyCoupler.retrieveRelatedEnvoys(states);
        List<DataGroupInfo> groups = envoyAndStates.stream().map(e -> e.getTypeInfo().getParent()).collect(Collectors.toList());

        StateGroup stateGroup;
        if (groups.stream().allMatch(e -> e.activationProperty().isActiveOrActivating()))
        {
            stateGroup = new StateGroup(stateId, groups);
        }
        else
        {
            stateGroup = myActivator.activateLayers(stateId, groups, activateDataLayer);
        }

        return stateGroup;
    }

    /**
     * Checks to see if there are any wms layer states stored in the node.
     *
     * @param node The node to inspect.
     * @param activateDataLayer True if it needs to check for wms data layer
     *            states, false if it needs to checks for wms map layer states.
     * @return True if the node contains the necessary state information false
     *         otherwise.
     */
    public boolean canActivate(Node node, boolean activateDataLayer)
    {
        return myNodeReader.canActivateState(node, activateDataLayer);
    }

    /**
     * Checks to see if there are any wms layer states stored in the state
     * object.
     *
     * @param state The state object to inspect.
     * @param activateDataLayer True if it needs to check for wms data layer
     *            states, false if it needs to checks for wms map layer states.
     * @return True if the object contains the necessary state information false
     *         otherwise.
     */
    public boolean canActivate(StateType state, boolean activateDataLayer)
    {
        return !ServerStateUtilities.getWmsLayers(state, activateDataLayer).isEmpty();
    }
}
