package io.opensphere.wfs.placenames;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.CommonTimer;
import io.opensphere.core.util.lang.PhasedTaskCanceller;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeListener;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeType;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.Viewer2D;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.AbstractActivationListener;
import io.opensphere.mantle.data.ActivationState;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;
import io.opensphere.mantle.data.impl.DefaultBasicVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultMapFeatureVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultTimeExtents;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * Manager for the place name layers.
 */
public class PlaceNameLayerManager
{
    /** The source for place names. */
    public static final String SOURCE = "Place Names";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PlaceNameLayerManager.class);

    /** The active server groups. */
    private final Map<String, DataGroupInfo> myServerGroups;

    /** The Active server i ds. */
    private final Set<String> myActiveServerIDs;

    /** For synchronization locking. */
    private final Object myLock = new Object();

    /** The main place names group info. */
    private final DataGroupInfo myMasterGroup;

    /** Executor used to update the place names when the view changes. */
    private final transient Executor myProcrastinatingExecutor = CommonTimer.createProcrastinatingExecutor(20);

    /** The tool box used by plugins to interact with the rest of the system. */
    private final Toolbox myToolbox;

    /** Listener for view change events. */
    private final ViewChangeListener myViewListener = new ViewChangeListener()
    {
        @Override
        public void viewChanged(Viewer viewer, ViewChangeType type)
        {
            handleViewChanged();
        }
    };

    /** True when the warning message for 2D viewers has been logged. */
    private boolean myWarningLogged;

    /**
     * The Data type info listener. This listener will handle visibility changes
     * for all place name layers.
     */
    private final EventListener<DataTypeVisibilityChangeEvent> myDataTypeInfoListener;

    /** The Data group info activation listener. */
    private final transient AbstractActivationListener myActivationListener = new AbstractActivationListener()
    {
        @Override
        @SuppressWarnings("PMD.CollapsibleIfStatements")
        public void commit(io.opensphere.mantle.data.DataGroupActivationProperty property,
                io.opensphere.mantle.data.ActivationState state, PhasedTaskCanceller canceller)
        {
            if (state == ActivationState.ACTIVE)
            {
                if (!isIDActive(property.getDataGroup().getId()))
                {
                    setServerActive(property.getDataGroup().getId(), true);
                    handleViewChanged();
                }
            }
            else if (state == ActivationState.INACTIVE)
            {
                if (isIDActive(property.getDataGroup().getId()))
                {
                    setServerActive(property.getDataGroup().getId(), false);
                    clearLayer(property.getDataGroup());
                }
            }
        }
    };

    /**
     * Constructor.
     *
     * @param toolbox The tool box used by plugins to interact with the rest of
     *            the system.
     */
    public PlaceNameLayerManager(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myServerGroups = Collections.synchronizedMap(new HashMap<String, DataGroupInfo>());
        myActiveServerIDs = New.set();
        myMasterGroup = new DefaultDataGroupInfo(true, myToolbox, "Place Name", PlaceNameLayerManager.class.getName(),
                "Place Name Layers");
        if (myToolbox.getMapManager().getViewChangeSupport() != null)
        {
            myToolbox.getMapManager().getViewChangeSupport().addViewChangeListener(myViewListener);
        }

        myDataTypeInfoListener = createDataTypeInfoListener();
        toolbox.getEventManager().subscribe(DataTypeVisibilityChangeEvent.class, myDataTypeInfoListener);
    }

    /**
     * Adds a place name provider.
     *
     * @param serverName the server name
     */
    public void addServer(String serverName)
    {
        String sourceStr = "Place Names Layer";
        MantleToolbox mantleToolbox = myToolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        // Add master group if necessary.
        if (myServerGroups.isEmpty())
        {
            mantleToolbox.getDataGroupController().addRootDataGroupInfo(myMasterGroup, this);
        }

        DataGroupInfo serverGroup = null;
        synchronized (myLock)
        {
            if (myServerGroups.containsKey(serverName))
            {
                serverGroup = myServerGroups.get(serverName);
            }
            else
            {
                serverGroup = new DefaultDataGroupInfo(false, myToolbox, "Place Name", serverName + " (Place Names)", serverName);
                // Get the layers we are interested in from the configuration.
                PlaceNameConfig config = PlaceNameConfig.getConfig();
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("There are " + config.getPlaceNameLayers().size() + " place name layers to display for "
                            + serverName);
                }

                for (PlaceNameLayerConfig layerConf : config.getPlaceNameLayers())
                {
                    PlaceNameLayer layer = new PlaceNameLayer(layerConf, myToolbox, serverName);
                    DataTypeInfo dti = createDataTypeInfo(serverGroup.getDisplayName(), layer, layerConf.getDataSetName());
                    layer.setTypeKey(dti.getTypeKey());
                    serverGroup.addMember(dti, this);
                }
                setServerActive(serverGroup.getId(), false);
                serverGroup.activationProperty().addListener(myActivationListener);
                myServerGroups.put(serverName, serverGroup);
            }
        }
        myMasterGroup.addChild(serverGroup, sourceStr);
    }

    /** Handle any cleanup required before removal of this manager. */
    public void cleanup()
    {
        myToolbox.getMapManager().getViewChangeSupport().removeViewChangeListener(myViewListener);
        clearLayers();
    }

    /**
     * Removes the server and clears associated place name layers that are
     * active.
     *
     * @param serverName the server name
     */
    public void removeServer(String serverName)
    {
        synchronized (myLock)
        {
            DataGroupInfo serverGroup = myServerGroups.remove(serverName);

            if (serverGroup != null)
            {
                myMasterGroup.removeChild(serverGroup, this);
                if (isIDActive(serverGroup.getId()))
                {
                    setServerActive(serverGroup.getId(), false);
                    clearLayer(serverGroup);
                }
            }
        }
    }

    /**
     * Clear layer.
     *
     * @param serverGroup the server group
     */
    private void clearLayer(DataGroupInfo serverGroup)
    {
        for (DataTypeInfo dti : serverGroup.getMembers(false))
        {
            PlaceNamesDataTypeInfo placeNamesDti = (PlaceNamesDataTypeInfo)dti;
            placeNamesDti.getLayer().cleanup(true);
        }
    }

    /** Clear all geometries from all layers. */
    private void clearLayers()
    {
        for (Entry<String, DataGroupInfo> entry : myServerGroups.entrySet())
        {
            clearLayer(entry.getValue());
        }
    }

    /**
     * Create data type info for the given server and layer.
     *
     * @param serverName The server name.
     * @param layer the layer
     * @param layerName The layer name.
     * @return The newly created DataTypeInfo.
     */
    private DataTypeInfo createDataTypeInfo(String serverName, PlaceNameLayer layer, String layerName)
    {
        String key = serverName + ':' + layerName;
        // For dti's used as a place name dti, use the server name as the type
        // name. It is used for comparison
        // in the dataTypeVisibilityChanged listener.
        PlaceNamesDataTypeInfo dti = new PlaceNamesDataTypeInfo(myToolbox, layer, SOURCE, key, layerName, layerName, false);

        // Place names are base layers only.
        BasicVisualizationInfo basicInfo = new DefaultBasicVisualizationInfo(LoadsTo.BASE, Color.white, false);
        dti.setBasicVisualizationInfo(basicInfo);

        DefaultMapFeatureVisualizationInfo mapInfo = new DefaultMapFeatureVisualizationInfo(
                MapVisualizationType.PLACE_NAME_ELEMENTS, false);

        mapInfo.setDataTypeInfo(dti);
        dti.setMapVisualizationInfo(mapInfo);

        dti.setTimeExtents(new DefaultTimeExtents(TimeSpan.TIMELESS), this);

        boolean isPlaceNameLayerVisible = MantleToolboxUtils.getMantleToolbox(myToolbox).getDataTypeInfoPreferenceAssistant()
                .isVisiblePreference(dti.getTypeKey());

        layer.setActive(isPlaceNameLayerVisible);
        dti.setVisible(isPlaceNameLayerVisible, this);

        return dti;
    }

    /**
     * Creates the data type info change listener.
     *
     * @return The event listener.
     */
    private EventListener<DataTypeVisibilityChangeEvent> createDataTypeInfoListener()
    {
        return new EventListener<DataTypeVisibilityChangeEvent>()
        {
            @Override
            public void notify(DataTypeVisibilityChangeEvent event)
            {
                if (event.getDataTypeInfo() instanceof PlaceNamesDataTypeInfo)
                {
                    PlaceNamesDataTypeInfo placeNamesDti = (PlaceNamesDataTypeInfo)event.getDataTypeInfo();
                    final PlaceNameLayer pnl = placeNamesDti.getLayer();

                    pnl.setActive(((Boolean)event.getValue()).booleanValue());
                    Thread t = new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            pnl.cleanup(false);
                            if (pnl.isActive())
                            {
                                pnl.handleViewChanged();
                            }
                        }
                    });
                    t.start();
                }
            }
        };
    }

    /**
     * Handle a viewer changed event.
     */
    private void handleViewChanged()
    {
        myProcrastinatingExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (myLock)
                {
                    Viewer viewer = myToolbox.getMapManager().getStandardViewer();
                    if (viewer instanceof Viewer2D)
                    {
                        if (!myWarningLogged)
                        {
                            LOGGER.warn("Place names not supported for 2D projections.");
                            myWarningLogged = true;
                            clearLayers();
                        }
                        return;
                    }
                    myWarningLogged = false;
                    for (Entry<String, DataGroupInfo> entry : myServerGroups.entrySet())
                    {
                        if (isIDActive(entry.getValue().getId()))
                        {
                            for (DataTypeInfo dti : entry.getValue().getMembers(false))
                            {
                                PlaceNamesDataTypeInfo placeNamesDti = (PlaceNamesDataTypeInfo)dti;
                                placeNamesDti.getLayer().handleViewChanged();
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Checks if is iD active.
     *
     * @param id the id
     * @return true, if is iD active
     */
    private boolean isIDActive(String id)
    {
        boolean isActive = false;
        synchronized (myActiveServerIDs)
        {
            isActive = myActiveServerIDs.contains(id);
        }
        return isActive;
    }

    /**
     * Sets the server active.
     *
     * @param id the id
     * @param active the active
     */
    private void setServerActive(String id, boolean active)
    {
        synchronized (myActiveServerIDs)
        {
            if (active)
            {
                myActiveServerIDs.add(id);
            }
            else
            {
                myActiveServerIDs.remove(id);
            }
        }
    }

    /**
     * The Class PlaceNamesDataTypeInfo. Helper class for place name layer data
     * types that contains the server name for easy lookup.
     */
    private static final class PlaceNamesDataTypeInfo extends DefaultDataTypeInfo
    {
        /** The Layer. */
        private final PlaceNameLayer myLayer;

        /**
         * Instantiates a new place names data type info.
         *
         * @param tb the tb
         * @param layer the layer
         * @param sourcePrefix the source prefix
         * @param typeKey the type key
         * @param typeName the type name
         * @param displayName the display name
         * @param providerFiltersMetaData the provider filters meta data
         */
        public PlaceNamesDataTypeInfo(Toolbox tb, PlaceNameLayer layer, String sourcePrefix, String typeKey, String typeName,
                String displayName, boolean providerFiltersMetaData)
        {
            super(tb, sourcePrefix, typeKey, typeName, displayName, providerFiltersMetaData);
            myLayer = layer;
        }

        @Override
        @SuppressWarnings("PMD.OverrideMerelyCallsSuper")
        public boolean equals(Object obj)
        {
            return super.equals(obj);
        }

        /**
         * Gets the layer.
         *
         * @return the layer
         */
        public PlaceNameLayer getLayer()
        {
            return myLayer;
        }

        @Override
        @SuppressWarnings("PMD.OverrideMerelyCallsSuper")
        public int hashCode()
        {
            return super.hashCode();
        }
    }
}
