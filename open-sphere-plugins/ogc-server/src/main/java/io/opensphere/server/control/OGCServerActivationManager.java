package io.opensphere.server.control;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Timer;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.Event.State;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.server.services.AbstractServerDataTypeInfo;
import io.opensphere.server.services.OGCServiceStateEvent;
import io.opensphere.server.services.ServerConfigEvent;
import io.opensphere.server.services.ServerConfigEvent.ServerEventAction;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.toolbox.ServerToolbox;
import io.opensphere.server.toolbox.ServerToolboxUtils;
import io.opensphere.server.util.ServerConstants;

/**
 * Support class that handles the activation and de-activation of OGC plugins (WMS, WFS, WPS) and assembles the layers from each
 * into a consolidated layer list.
 */
@SuppressWarnings("PMD.GodClass")
class OGCServerActivationManager
{
    /** The Logger. */
    private static final Logger LOGGER = Logger.getLogger(OGCServerActivationManager.class);

    /** Callback that gets notified when a source activation completes. */
    private Callback myCallback;

    /** Event Handler for State events from OGC Service plugins. */
    private EventListener<OGCServiceStateEvent> myOGCServiceEventListener;

    /** The connection parameters associated with this server source. */
    private final ServerConnectionParams myPayload;

    /** Flag indicating whether response has been sent. */
    private boolean myResponseSent;

    /** Response timeout timer. */
    private Timer myResponseTimer;

    /** The ServerSource associated with this Builder. */
    private final OGCServerSource mySource;

    /** The time that activation was started (for debug purposes). */
    private long myStartedTime;

    /** The EventManager from which to receive Plugin Events. */
    private final Toolbox myToolbox;

    /** WFS Layers. */
    private Collection<? extends AbstractServerDataTypeInfo> myWfsLayers;

    /** WFS Success Flag. */
    private boolean myWfsSuccess = true;

    /** WFS Flag. */
    private boolean myWfsWaiting;

    /* WMS Variables */
    /** WMS Layers. */
    private DataGroupInfo myWmsLayers;

    /** WMS Success Flag. */
    private boolean myWmsSuccess = true;

    /** WMS Flag. */
    private boolean myWmsWaiting;

    /* WPS Variables */
    /** WPS Layers. */
    private DataGroupInfo myWpsLayers;

    /** WPS Success Flag. */
    private boolean myWpsSuccess = true;

    /** WPS Flag. */
    private boolean myWpsWaiting;

    /** The root data group for the server. */
    private volatile DataGroupInfo myRootGroup;

    /**
     * Constructor.
     *
     * @param src The <code>OGCServerSource</code> that this builder is responsible for.
     * @param payload The server connection information.
     * @param toolbox The <code>Toolbox</code> from Core.
     */
    public OGCServerActivationManager(OGCServerSource src, ServerConnectionParams payload, Toolbox toolbox)
    {
        myPayload = payload;
        mySource = src;
        myToolbox = toolbox;
    }

    /**
     * Activate the WMS, WFS, and WPS plugins.
     *
     * @param listener the object to notify when activation completes
     */
    public void activate(Callback listener)
    {
        myCallback = listener;
        myStartedTime = System.nanoTime();
        initializeServerFlags();
        createEventListeners();
        sendServiceActivationEvent(true);
        startTimer();
    }

    /**
     * Deactivate the WMS, WFS, and WPS plugins, stop timer, and unsubscribe from further events.
     */
    public void deactivate()
    {
        // Cleanup event managers
        myToolbox.getEventManager().unsubscribe(OGCServiceStateEvent.class, myOGCServiceEventListener);
        sendServiceActivationEvent(false);

        stopTimer();
        myWmsWaiting = false;
        myWfsWaiting = false;
        myWpsWaiting = false;

        DataGroupInfo rootGroup = myRootGroup;
        if (rootGroup != null)
        {
            MantleToolbox mantleToolbox = myToolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
            mantleToolbox.getDataGroupController().removeDataGroupInfo(rootGroup, this);
        }
    }

    /**
     * Create the event listeners for this class.
     */
    private void createEventListeners()
    {
        myOGCServiceEventListener = event ->
        {
            ServerToolbox serverToolbox = ServerToolboxUtils.getServerToolbox(myToolbox);
            serverToolbox.getPluginExecutor().execute(() -> handleOGCServiceStateEvent(event));
        };
        myToolbox.getEventManager().subscribe(OGCServiceStateEvent.class, myOGCServiceEventListener);
    }

    /**
     * Fire a load event.
     *
     * @param message the error message
     */
    private void fireLoadEvent(final String message)
    {
        if (!myResponseSent && myCallback != null)
        {
            DataGroupInfo info = null;
            if (isSuccess())
            {
                info = getDataGroupInfo();
            }
            myCallback.activationComplete(mySource, info, isSuccess(), message);
            myResponseSent = true;

            myRootGroup = info;
        }
    }

    /**
     * TEMPORARY method to Fix group keys.
     *
     * @param group the group
     * @param base the base
     */
    private void fixGroupKeys(DataGroupInfo group, String base)
    {
        StringBuilder key = new StringBuilder();
        if (base != null && !base.isEmpty())
        {
            key.append(base).append(ServerConstants.LAYERNAME_SEPARATOR);
        }
        key.append(group.getDisplayName());
        group.setId(key.toString(), this);
        if (group.hasChildren())
        {
            for (DataGroupInfo child : group.getChildren())
            {
                fixGroupKeys(child, key.toString());
            }
        }
    }

    /**
     * Get the consolidated layer list as a <code>DataGroupInfo</code>.
     *
     * @return <code>DataGroupInfo</code> hierarchy of the server's layers.
     */
    private DataGroupInfo getDataGroupInfo()
    {
        return getDataGroupInfo(myWmsLayers, myWfsLayers, myWpsLayers);
    }

    /**
     * Get a consolidated layer list as a <code>DataGroupInfo</code> based on the passed-in layer lists from WMS and WFS.
     *
     * @param wmsLayers The layer list from the WMS plugin
     * @param wfsLayers the wfs layers from the WFS plugin
     * @param pWpsLayers the WPS layers from the plugin.
     * @return <code>DataGroupInfo</code> hierarchy of the server's layers.
     */
    private DataGroupInfo getDataGroupInfo(DataGroupInfo wmsLayers, Collection<? extends AbstractServerDataTypeInfo> wfsLayers,
            DataGroupInfo pWpsLayers)
    {
        // Top-level (root) node is the server name
        DefaultDataGroupInfo serverGroup = new DefaultDataGroupInfo(true, myToolbox, "OGC Server", mySource.getName());
        if (wmsLayers != null)
        {
            serverGroup.addChild(wmsLayers, this);
        }

        // Add WFS to WMS tree
        if (CollectionUtilities.hasContent(wfsLayers))
        {
            List<AbstractServerDataTypeInfo> remainingWfs = New.linkedList(wfsLayers);
            for (DataGroupInfo group : getGroupsWithMembers(serverGroup))
            {
                // Get the WMS type
                DataTypeInfo wmsType = group.getMembers(false).iterator().next();

                // Remove the first WFS layer from the list that has a name in
                // the WMS names
                AbstractServerDataTypeInfo matchingType = CollectionUtilities.removeFirst(remainingWfs,
                    wfsType -> match(wfsType, wmsType));

                if (matchingType != null)
                {
                    group.addMember(matchingType, this);
                }
            }

            // Add the un-matched layers as WFS-only layers
            for (AbstractServerDataTypeInfo wfsInfo : remainingWfs)
            {
                String id = wfsInfo.getDisplayName();
                DefaultServerDataGroupInfo wfsGroup = new DefaultServerDataGroupInfo(false, myToolbox, id,
                        wfsInfo.getDisplayName());
                wfsGroup.setParent(serverGroup);
                wfsGroup.addMember(wfsInfo, this);
                serverGroup.addChild(wfsGroup, this);
            }
        }

        if (pWpsLayers != null)
        {
            if (pWpsLayers instanceof DefaultServerDataGroupInfo)
            {
                // don't add the server-level node, instead, add its children:
                Collection<DataGroupInfo> children = pWpsLayers.getChildren();
                for (DataGroupInfo dataGroup : children)
                {
                    serverGroup.addChild(dataGroup, this);
                }
            }
            else
            {
                serverGroup.addChild(pWpsLayers, this);
            }
        }

        // TODO: This is all kinds of wrong. Fix this so the tree is created
        // correctly the first time.
        fixGroupKeys(serverGroup, null);

        return serverGroup;
    }

    /**
     * Determines if two data types match.
     *
     * @param type1 the one type
     * @param type2 the other type
     * @return whether they match
     */
    private static boolean match(DataTypeInfo type1, DataTypeInfo type2)
    {
        return ServerToolboxUtils.formatNameForComparison(type1.getTypeName())
                .equals(ServerToolboxUtils.formatNameForComparison(type2.getTypeName()))
                || ServerToolboxUtils.formatNameForComparison(type1.getDisplayName())
                        .equals(ServerToolboxUtils.formatNameForComparison(type2.getDisplayName()));
    }

    /**
     * Gets the groups with members. This will only look for groups that are leaf nodes and have members. Non-leaf (folder) nodes
     * should not have any associated DataTypeInfos.
     *
     * @param group the high-level group to retrieve children from
     * @return the groups with members
     */
    private Set<DataGroupInfo> getGroupsWithMembers(DataGroupInfo group)
    {
        Set<DataGroupInfo> returnSet = new HashSet<>();
        if (group.hasChildren())
        {
            for (DataGroupInfo child : group.getChildren())
            {
                returnSet.addAll(getGroupsWithMembers(child));
            }
        }
        else if (group.hasMembers(false))
        {
            returnSet.add(group);
        }
        return returnSet;
    }

    /**
     * Handles a OGCServiceStateEvent.
     *
     * @param event The OGCServiceStateEvent
     */
    @SuppressWarnings("PMD.CollapsibleIfStatements")
    private void handleOGCServiceStateEvent(final OGCServiceStateEvent event)
    {
        if (OGCServerSource.WMS_SERVICE.equals(event.getService()))
        {
            if (myWmsWaiting && event.getServerId() != null
                    && event.getServerId().equals(myPayload.getServerId(event.getService())) && event.getState() != State.STARTED)
            {
                processWmsState(event);
            }
        }
        else if (OGCServerSource.WFS_SERVICE.equals(event.getService()))
        {
            if (myWfsWaiting && event.getServerId() != null
                    && event.getServerId().equals(myPayload.getServerId(event.getService())) && event.getState() != State.STARTED)
            {
                processWfsState(event);
            }
        }
        else if (OGCServerSource.WPS_SERVICE.equals(event.getService()))
        {
            if (myWpsWaiting && event.getServerId() != null
                    && event.getServerId().equals(myPayload.getServerId(event.getService())) && event.getState() != State.STARTED)
            {
                processWpsState(event);
            }
        }
    }

    /**
     * Initialize the server flags based on the URLs in the Server Config.
     */
    private void initializeServerFlags()
    {
        if (mySource.getWMSServerURL() != null && !mySource.getWMSServerURL().isEmpty())
        {
            myWmsWaiting = true;
            myWmsSuccess = false;
        }
        if (mySource.getWFSServerURL() != null && !mySource.getWFSServerURL().isEmpty())
        {
            myWfsWaiting = true;
            myWfsSuccess = false;
        }
        if (mySource.getWPSServerURL() != null && !mySource.getWPSServerURL().isEmpty())
        {
            myWpsWaiting = true;
            myWpsSuccess = false;
        }
    }

    /**
     * Check for whether the response from the servers were received.
     *
     * @return True if responses were received, false otherwise.
     */
    private boolean isComplete()
    {
        return !myWmsWaiting && !myWfsWaiting && !myWpsWaiting;
    }

    /**
     * Check for whether the responses from the servers were success.
     *
     * @return Successful or not.
     */
    private boolean isSuccess()
    {
        return myWmsSuccess && myWfsSuccess && myWpsSuccess;
    }

    /**
     * Process WFS state event.
     *
     * @param event the WFS state event
     */
    private void processWfsState(OGCServiceStateEvent event)
    {
        LOGGER.info(StringUtilities.formatTimingMessage(mySource.getName() + " WFS Response took ",
                System.nanoTime() - myStartedTime));
        myWfsWaiting = false;
        if (event.getState() == State.COMPLETED)
        {
            myWfsSuccess = true;
            myWfsLayers = event.getLayerList();
            if (isComplete())
            {
                sendResponseAndCleanup(null);
            }
        }
        else
        {
            StringBuilder sb = new StringBuilder("Error initializing WFS server: ");
            sb.append(mySource.getName());
            if (StringUtils.isNotEmpty(event.getError()))
            {
                sb.append("\n\n").append(event.getError());
            }
            sendResponseAndCleanup(sb.toString());
        }
    }

    /**
     * Process WMS state event.
     *
     * @param event the WMS state event
     */
    private void processWmsState(OGCServiceStateEvent event)
    {
        LOGGER.info(StringUtilities.formatTimingMessage(mySource.getName() + " WMS Response took ",
                System.nanoTime() - myStartedTime));
        myWmsWaiting = false;
        if (event.getState() == State.COMPLETED)
        {
            myWmsSuccess = true;
            myWmsLayers = event.getLayerTree();
            if (isComplete())
            {
                sendResponseAndCleanup(null);
            }
        }
        else
        {
            StringBuilder sb = new StringBuilder("Error initializing WMS server: ");
            sb.append(mySource.getName());
            if (StringUtils.isNotEmpty(event.getError()))
            {
                sb.append("\n\n").append(event.getError());
            }
            sendResponseAndCleanup(sb.toString());
        }
    }

    /**
     * Process WPS state event.
     *
     * @param event the WPS state event
     */
    private void processWpsState(OGCServiceStateEvent event)
    {
        LOGGER.info(StringUtilities.formatTimingMessage(mySource.getName() + " WPS Response took ",
                System.nanoTime() - myStartedTime));
        myWpsWaiting = false;
        if (event.getState() == State.COMPLETED)
        {
            myWpsSuccess = true;
            myWpsLayers = event.getLayerTree();
            if (isComplete())
            {
                sendResponseAndCleanup(null);
            }
        }
        else
        {
            StringBuilder sb = new StringBuilder("Error initializing WPS server: ");
            sb.append(mySource.getName());
            if (StringUtils.isNotEmpty(event.getError()))
            {
                sb.append("\n\n").append(event.getError());
            }
            sendResponseAndCleanup(sb.toString());
        }
    }

    /**
     * Send load complete event.
     */
    private void sendLoadCompleteEvent()
    {
        String serverName = mySource.getName();
        ServerConfigEvent wfsEvent = new ServerConfigEvent(serverName, myPayload, ServerEventAction.LOADCOMPLETE, isSuccess());
        myToolbox.getEventManager().publishEvent(wfsEvent);
    }

    /**
     * Send a response message and cleanup listeners so that this scenario is only processed once.
     *
     * @param error Description of the error that caused failure, if applicable
     */
    private void sendResponseAndCleanup(String error)
    {
        stopTimer();
        myWmsWaiting = false;
        myWfsWaiting = false;
        myWpsWaiting = false;

        // Cleanup event managers
        myToolbox.getEventManager().unsubscribe(OGCServiceStateEvent.class, myOGCServiceEventListener);

        fireLoadEvent(error);
        sendLoadCompleteEvent();
    }

    /**
     * Fire event to the OGC Service plugins to load/unload.
     *
     * @param activate If true, service should be activated; if false, it should be deactivated.
     */
    private void sendServiceActivationEvent(boolean activate)
    {
        String serverName = mySource.getName();
        ServerConfigEvent wfsEvent = new ServerConfigEvent(serverName, myPayload,
                activate ? ServerEventAction.ACTIVATE : ServerEventAction.DEACTIVATE);
        myToolbox.getEventManager().publishEvent(wfsEvent);
    }

    /**
     * Setup and Start the WMS response timer.
     */
    private void startTimer()
    {
        if (myResponseTimer == null)
        {
            myResponseTimer = new Timer(myPayload.getTimeBudget().getRemainingMilliseconds(), this::handleTimerEvent);
            myResponseTimer.setRepeats(false);
        }
        else
        {
            myResponseTimer.setInitialDelay(myPayload.getTimeBudget().getRemainingMilliseconds());
        }
        myResponseTimer.start();
        myPayload.getTimeBudget().unpause();
    }

    /**
     * Stop the response timer.
     */
    private void stopTimer()
    {
        if (myResponseTimer != null && myResponseTimer.isRunning())
        {
            myResponseTimer.stop();
        }
    }

    /**
     * Handles a timer event.
     *
     * @param e the event
     */
    public void handleTimerEvent(ActionEvent e)
    {
        if (myPayload.getTimeBudget().isExpired())
        {
            StringBuilder error = new StringBuilder(150);
            error.append("Timed out waiting for ");
            int numServicesWaiting = 0;
            if (myWmsWaiting)
            {
                numServicesWaiting++;
                error.append("WMS ");
            }
            if (myWfsWaiting)
            {
                numServicesWaiting++;
                error.append("WFS ");
            }
            if (myWpsWaiting)
            {
                numServicesWaiting++;
                error.append("WPS ");
            }
            error.append(numServicesWaiting > 1 ? "responses" : "response").append(" from Server: ").append(mySource.getName());
            error.append(".  Consider increasing timeout settings in Edit->Settings->Servers.");

            LOGGER.warn(error.toString());
            sendResponseAndCleanup(error.toString());
        }
        else
        {
            myResponseTimer.setInitialDelay(myPayload.getTimeBudget().getRemainingMilliseconds());
            myResponseTimer.start();
        }
    }

    /**
     * Callback for clients to know when a given source has completely finished its activation.
     */
    @FunctionalInterface
    public interface Callback
    {
        /**
         * Notify callback that a source activation has completed.
         *
         * @param source the server source
         * @param groupInfo the group info
         * @param success the success flag
         * @param errorMessage an error message
         */
        void activationComplete(OGCServerSource source, DataGroupInfo groupInfo, boolean success, String errorMessage);
    }
}
