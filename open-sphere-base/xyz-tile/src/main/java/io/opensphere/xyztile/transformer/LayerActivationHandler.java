package io.opensphere.xyztile.transformer;

import java.util.Set;

import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.mantle.controller.event.impl.ActiveDataGroupsChangedEvent;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.event.DataGroupInfoChildRemovedEvent;
import io.opensphere.mantle.data.event.DataGroupInfoMemberAddedEvent;
import io.opensphere.mantle.data.event.DataGroupInfoMemberRemovedEvent;
import io.opensphere.mantle.data.event.DataTypeInfoZOrderChangeEvent;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;
import io.opensphere.xyztile.model.XYZDataTypeInfo;
import io.opensphere.xyztile.util.XYZTileUtils;

/**
 * Listens for when xyz layers have been activated and notifies a
 * {@link LayerActivationListener} of the active layer.
 */
public class LayerActivationHandler
{
    /**
     * The active listener.
     */
    private final EventListener<ActiveDataGroupsChangedEvent> myActiveListener = this::handleActivationChanged;

    /**
     * The event manager.
     */
    private final EventManager myEventManager;

    /**
     * The visibility listener.
     */
    private final EventListener<DataGroupInfoMemberAddedEvent> myMemberAddedListener = this::handleMemberAdded;

    /**
     * The {@link DataTypeInfo} removed listener.
     */
    private final EventListener<DataGroupInfoMemberRemovedEvent> myMemberRemovedListener = this::handleMemberRemoved;

    /**
     * The removed listener.
     */
    private final EventListener<DataGroupInfoChildRemovedEvent> myRemovedListener = this::handleRemoved;

    /**
     * The listener wanting notification when a xyz tile layer is activated.
     */
    private final LayerActivationListener myTileListener;

    /**
     * The visibility listener.
     */
    private final EventListener<DataTypeVisibilityChangeEvent> myVisibilityListener = this::handleVisibilityChanged;

    /**
     * The zorder listener.
     */
    private final EventListener<DataTypeInfoZOrderChangeEvent> myZorderListener = this::handleZOrderChanged;

    /**
     * Constructs a new layer provider.
     *
     * @param eventManager The event manager.
     * @param tileLayerListener The listener wanting notification when a xyz
     *            tile layer is activated.
     */
    public LayerActivationHandler(EventManager eventManager, LayerActivationListener tileLayerListener)
    {
        myTileListener = tileLayerListener;
        myEventManager = eventManager;
        myEventManager.subscribe(DataTypeVisibilityChangeEvent.class, myVisibilityListener);
        myEventManager.subscribe(DataGroupInfoChildRemovedEvent.class, myRemovedListener);
        myEventManager.subscribe(ActiveDataGroupsChangedEvent.class, myActiveListener);
        myEventManager.subscribe(DataTypeInfoZOrderChangeEvent.class, myZorderListener);
        myEventManager.subscribe(DataGroupInfoMemberAddedEvent.class, myMemberAddedListener);
        myEventManager.subscribe(DataGroupInfoMemberRemovedEvent.class, myMemberRemovedListener);
    }

    /**
     * Unsubscribes from the event manager.
     */
    public void close()
    {
        myEventManager.unsubscribe(DataTypeVisibilityChangeEvent.class, myVisibilityListener);
        myEventManager.unsubscribe(DataGroupInfoChildRemovedEvent.class, myRemovedListener);
        myEventManager.unsubscribe(ActiveDataGroupsChangedEvent.class, myActiveListener);
        myEventManager.unsubscribe(DataTypeInfoZOrderChangeEvent.class, myZorderListener);
        myEventManager.unsubscribe(DataGroupInfoMemberAddedEvent.class, myMemberAddedListener);
        myEventManager.unsubscribe(DataGroupInfoMemberRemovedEvent.class, myMemberRemovedListener);
    }

    /**
     * Gets the tile layer listener.
     *
     * @return The new tile layer listener.
     */
    public LayerActivationListener getTileLayerListener()
    {
        return myTileListener;
    }

    /**
     * Handles activation changes and notifies the activation listener.
     *
     * @param event The event.
     */
    private void handleActivationChanged(ActiveDataGroupsChangedEvent event)
    {
        ThreadUtilities.runCpu(() ->
        {
            handleActivationChanged(true, event.getActivatedGroups());
            handleActivationChanged(false, event.getDeactivatedGroups());
        });
    }

    /**
     * Handles activation changes and notifies the activation listener.
     *
     * @param active True if the groups are active, false if the groups have
     *            been deactivated.
     * @param groups The groups whose activation state has changed.
     */
    private void handleActivationChanged(boolean active, Set<DataGroupInfo> groups)
    {
        for (DataGroupInfo group : groups)
        {
            handleCommit(active, group);
        }
    }

    /**
     * Notifies the tile listener of the changes.
     *
     * @param active True if the layer is active, false otherwise.
     * @param group The layer.
     * @return True if the group is an XYZ group, false if its some other group.
     */
    private boolean handleCommit(boolean active, DataGroupInfo group)
    {
        for (DataTypeInfo info : group.getMembers(false))
        {
            handleDataTypeEvent(active, info);
        }
        boolean isXYZGroup = XYZTileUtils.XYZ_PROVIDER.equals(group.getProviderType());
        return isXYZGroup;
    }

    /**
     * Handles a data type event.
     *
     * @param active True if the data type is active, false otherwise.
     * @param dataType The data type to handle.
     */
    private void handleDataTypeEvent(boolean active, DataTypeInfo dataType)
    {
        if (dataType instanceof XYZDataTypeInfo)
        {
            if (active && dataType.isVisible())
            {
                myTileListener.layerActivated((XYZDataTypeInfo)dataType);
            }
            else
            {
                myTileListener.layerDeactivated((XYZDataTypeInfo)dataType);
            }
        }
    }

    /**
     * Handles visibility changes and notifies the activation listener.
     *
     * @param event The event.
     */
    private void handleMemberAdded(DataGroupInfoMemberAddedEvent event)
    {
        ThreadUtilities.runCpu(() ->
        {
            handleDataTypeEvent(event.getGroup().activationProperty().isActive(), event.getAdded());
        });
    }

    /**
     * Handles when a member is removed, and if that member is an xyz layer, it
     * will remove notify the {@link LayerActivationListener} of this change.
     *
     * @param event The event.
     */
    private void handleMemberRemoved(DataGroupInfoMemberRemovedEvent event)
    {
        handleDataTypeEvent(false, event.getRemoved());
    }

    /**
     * Handles removed changes and notifies the activation listener.
     *
     * @param event The event.
     */
    private void handleRemoved(DataGroupInfoChildRemovedEvent event)
    {
        handleCommit(false, event.getRemoved());
    }

    /**
     * Handles visibility changes and notifies the activation listener.
     *
     * @param event The event.
     */
    private void handleVisibilityChanged(DataTypeVisibilityChangeEvent event)
    {
        handleDataTypeEvent(event.isVisible(), event.getDataTypeInfo());
    }

    /**
     * Handles visibility changes and notifies the activation listener.
     *
     * @param event The event.
     */
    private void handleZOrderChanged(DataTypeInfoZOrderChangeEvent event)
    {
        if (event.getDataTypeInfo() != null && event.getDataTypeInfo().getParent() != null
                && event.getDataTypeInfo() instanceof XYZDataTypeInfo
                && event.getDataTypeInfo().getParent().activationProperty().isActive())
        {
            ThreadUtilities.runCpu(() ->
            {
                handleDataTypeEvent(false, event.getDataTypeInfo());
                handleDataTypeEvent(true, event.getDataTypeInfo());
            });
        }
    }
}
