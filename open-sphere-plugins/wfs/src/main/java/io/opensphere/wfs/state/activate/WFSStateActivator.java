package io.opensphere.wfs.state.activate;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bitsys.fade.mist.state.v4.LayerType;
import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.core.Toolbox;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupActivator;
import io.opensphere.server.state.utilities.ServerStateUtilities;
import io.opensphere.server.toolbox.LayerConfiguration;
import io.opensphere.server.toolbox.ServerStateController;
import io.opensphere.server.toolbox.ServerToolbox;
import io.opensphere.server.toolbox.ServerToolboxUtils;
import io.opensphere.server.toolbox.WFSLayerConfigurationManager;
import io.opensphere.wfs.layer.WFSDataType;
import io.opensphere.wfs.state.controllers.WFSNodeReader;
import io.opensphere.wfs.state.model.WFSLayerState;
import io.opensphere.wfs.state.model.WFSStateGroup;

/**
 * Handles WFS state activation, deactivation and determines if a state can be
 * activated.
 */
public class WFSStateActivator
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(WFSStateActivator.class);

    /** The Builder. */
    private final WFSDataTypeBuilder myBuilder;

    /** The Node reader. */
    private final WFSNodeReader myNodeReader;

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new wFS state activator.
     *
     * @param toolbox the toolbox
     * @param builder The builder to use.
     * @param nodeReader The node reader to use.
     */
    public WFSStateActivator(Toolbox toolbox, WFSDataTypeBuilder builder, WFSNodeReader nodeReader)
    {
        myBuilder = builder;
        myNodeReader = nodeReader;
        myToolbox = toolbox;
    }

    /**
     * Reads a state node and creates corresponding WFSLayerStates and then
     * builds WFS types for each state.
     *
     * @param id the id
     * @param node the node
     * @return the WFS state group
     * @throws InterruptedException If activation is interrupted.
     */
    public WFSStateGroup activateState(String id, Node node) throws InterruptedException
    {
        ServerStateController serverStateController = ServerToolboxUtils.getServerToolbox(myToolbox).getServerStateController();
        serverStateController.activateServers(node);

        List<WFSLayerState> states = getReader().readNode(node);
        List<DataGroupInfo> stateGroups = createAndActivateGroups(id, states);

        return new WFSStateGroup(id, stateGroups, states);
    }

    /**
     * Reads a state object and creates corresponding WFSLayerStates and then
     * builds WFS types for each state.
     *
     * @param id the id
     * @param state the state object
     * @return the WFS state group
     * @throws InterruptedException If activation is interrupted.
     */
    public WFSStateGroup activateState(String id, StateType state) throws InterruptedException
    {
        ServerToolbox serverToolbox = ServerToolboxUtils.getServerToolbox(myToolbox);
        ServerStateController serverStateController = serverToolbox.getServerStateController();
        serverStateController.activateServers(state);

        List<WFSLayerState> states = ServerStateUtilities.getWfsLayers(serverToolbox.getLayerConfigurationManager(), state)
                .stream().map(this::convertLayer).collect(Collectors.toList());
        List<DataGroupInfo> stateGroups = createAndActivateGroups(id, states);

        return new WFSStateGroup(id, stateGroups, states);
    }

    /**
     * Converts the state layer to a WFSLayerState.
     *
     * @param layerType the state layer
     * @return the WFSLayerState
     */
    protected WFSLayerState convertLayer(LayerType layerType)
    {
        return SaveStateV4ToV3Translator.toLayerState(layerType);
    }

    /**
     * Creates and activates data groups for the given states.
     *
     * @param id the state ID
     * @param states the states
     * @return the data groups
     * @throws InterruptedException if the thread was interrupted
     */
    protected List<DataGroupInfo> createAndActivateGroups(String id, Collection<? extends WFSLayerState> states)
        throws InterruptedException
    {
        List<DataGroupInfo> stateGroups = New.list();
        if (!states.isEmpty())
        {
            List<WFSDataType> types = getBuilder().createWFSTypes(id, states);
            List<DataGroupInfo> toActivate = New.list();

            for (WFSDataType type : types)
            {
                DataGroupInfo parent = type.getParent();

                if (getBuilder().canActivate(type))
                {
                    toActivate.add(parent);
                }

                stateGroups.add(parent);
            }

            /* Reactivate the group so that listeners notice that new members
             * have been added. */
            if (!toActivate.isEmpty()
                    && !new DefaultDataGroupActivator(myToolbox.getEventManager()).reactivateGroups(stateGroups))
            {
                throw new InterruptedException();
            }
        }
        return stateGroups;
    }

    /**
     * If at least one WFS layer exists in the node, it can be activated.
     *
     * @param node the node to check
     * @return true, if successful
     */
    public boolean canActivate(Node node)
    {
        boolean canActivate = false;
        try
        {
            WFSLayerConfigurationManager configurationManager = ServerToolboxUtils.getServerToolbox(myToolbox)
                    .getLayerConfigurationManager();

            for (LayerConfiguration configuration : configurationManager.getAllConfigurations())
            {
                NodeList children = StateXML.getChildNodes(node, configuration.getStateXPath());
                if (children != null && children.getLength() > 0)
                {
                    canActivate = true;
                }
            }
        }
        catch (XPathExpressionException e)
        {
            LOGGER.error(e.getMessage(), e);
        }
        return canActivate;
    }

    /**
     * If at least one WFS layer exists in the state, it can be activated.
     *
     * @param state the state to check
     * @return true, if successful
     */
    public boolean canActivate(StateType state)
    {
        ServerToolbox serverToolbox = ServerToolboxUtils.getServerToolbox(myToolbox);

        return !ServerStateUtilities.getWfsLayers(serverToolbox.getLayerConfigurationManager(), state).isEmpty();
    }

    /**
     * Gets the {@link WFSDataTypeBuilder}.
     *
     * @return The builder.
     */
    protected WFSDataTypeBuilder getBuilder()
    {
        return myBuilder;
    }

    /**
     * Gets the {@link WFSNodeReader}.
     *
     * @return The node reader.
     */
    protected WFSNodeReader getReader()
    {
        return myNodeReader;
    }

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    protected final Toolbox getToolbox()
    {
        return myToolbox;
    }
}
