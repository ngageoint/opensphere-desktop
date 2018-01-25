package io.opensphere.wfs.state;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.server.services.ServerConfigEvent;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.wfs.layer.WFSDataType;
import io.opensphere.wfs.state.model.WFSStateGroup;

/**
 * The state controller used to register with the system's save state component
 * that saves and restores WFS data layers.
 */
public class WFSStateController extends BaseWFSStateController
{
    /** The Server configs. */
    private final Map<String, ServerConnectionParams> myServerConfigs;

    /** Listener for when servers are activated or deactivated. */
    private final EventListener<? super ServerConfigEvent> myServerEventListener = new EventListener<ServerConfigEvent>()
    {
        @Override
        public void notify(ServerConfigEvent event)
        {
            String serverId = event.getServer() == null ? null : event.getServer().getServerId(OGCServerSource.WFS_SERVICE);
            if (StringUtils.isNotEmpty(serverId))
            {
                serverId = serverId.toLowerCase();
                switch (event.getEventAction())
                {
                    case ACTIVATE:
                        myServerConfigs.put(serverId, event.getServer());
                        break;
                    case DEACTIVATE:
                        myServerConfigs.remove(serverId);
                        break;
                    default:
                        break;
                }
            }
        }
    };

    /** The Active states. */
    private final Map<String, WFSStateGroup> myActiveStates = New.map();

    /** The Data group controller. */
    private final DataGroupController myDataGroupController;

    /** The data type controller. */
    private final DataTypeController myDataTypeController;

    /**
     * Instantiates a new wFS state controller.
     *
     * @param toolbox the toolbox
     */
    public WFSStateController(Toolbox toolbox)
    {
        super(toolbox);
        myServerConfigs = New.map();
        MantleToolbox mantleToolbox = MantleToolboxUtils.getMantleToolbox(toolbox);
        myDataGroupController = mantleToolbox.getDataGroupController();
        myDataTypeController = mantleToolbox.getDataTypeController();
        toolbox.getEventManager().subscribe(ServerConfigEvent.class, myServerEventListener);
    }

    @Override
    public void activateState(String id, String description, Collection<? extends String> tags, Node node)
        throws InterruptedException
    {
        WFSStateGroup group = getActivator().activateState(id, node);
        synchronized (myActiveStates)
        {
            myActiveStates.put(group.getStateId(), group);
        }
    }

    @Override
    public void activateState(String id, String description, Collection<? extends String> tags, StateType state)
        throws InterruptedException
    {
        WFSStateGroup group = getActivator().activateState(id, state);
        synchronized (myActiveStates)
        {
            myActiveStates.put(group.getStateId(), group);
        }
    }

    @Override
    public boolean canActivateState(Node node)
    {
        return getActivator().canActivate(node);
    }

    @Override
    public boolean canActivateState(StateType state)
    {
        return getActivator().canActivate(state);
    }

    @Override
    public boolean canSaveState()
    {
        return true;
    }

    @Override
    public void deactivateState(String id, Node node) throws InterruptedException
    {
        WFSStateGroup stateGroup = null;
        synchronized (myActiveStates)
        {
            stateGroup = myActiveStates.get(id);
        }

        if (stateGroup != null)
        {
            getDeactivator().deactivateState(stateGroup);
        }

        synchronized (myActiveStates)
        {
            myActiveStates.remove(id);
        }
    }

    @Override
    public void deactivateState(String id, StateType state) throws InterruptedException
    {
        deactivateState(id, (Node)null);
    }

    @Override
    public boolean isSaveStateByDefault()
    {
        return true;
    }

    @Override
    public void saveState(Node node)
    {
        getSaver().saveState(node, myServerConfigs, getActiveGroups());
    }

    @Override
    public void saveState(StateType state)
    {
        getSaver().saveState(state, myServerConfigs, getActiveGroups());
    }

    /**
     * Gets the active WFS data groups.
     *
     * @return the active WFS data groups
     */
    private Set<DataGroupInfo> getActiveGroups()
    {
        return myDataGroupController.findActiveDataGroupInfo(this::isActiveWfs, false);
    }

    /**
     * Determines if a data group is an active WFS layer.
     *
     * @param group the data group
     * @return whether it's an active WFS layer
     */
    private boolean isActiveWfs(DataGroupInfo group)
    {
        if (group.hasMembers(false) && group.activationProperty().isActiveOrActivating())
        {
            /* We need to check that it's in the data type controller because
             * WFS currently does not deactivate groups when the server is
             * deactivated. */
            return group.hasMember(
                t -> t instanceof WFSDataType && myDataTypeController.hasDataTypeInfoForTypeKey(t.getTypeKey()), false);
        }
        return false;
    }
}
