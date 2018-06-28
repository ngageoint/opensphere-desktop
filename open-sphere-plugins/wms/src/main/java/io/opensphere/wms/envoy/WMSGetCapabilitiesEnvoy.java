package io.opensphere.wms.envoy;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.jcip.annotations.GuardedBy;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractEnvoy;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.UnserializableAccessor;
import io.opensphere.core.common.services.ServiceException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.Query;
import io.opensphere.core.data.util.SimpleIdQuery;
import io.opensphere.core.event.Event.State;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.StreamingImage;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Months;
import io.opensphere.core.units.duration.Weeks;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.lang.PhasedTaskCanceller;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.AbstractActivationListener;
import io.opensphere.mantle.data.ActivationListener;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultTimeExtents;
import io.opensphere.server.services.OGCServiceStateEvent;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.services.ServerRefreshEvent;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.wms.capabilities.WMSCapsLayer;
import io.opensphere.wms.capabilities.WMSServerCapabilities;
import io.opensphere.wms.config.v1.WMSLayerConfigChangeListener;
import io.opensphere.wms.config.v1.WMSLayerConfigurationSet;
import io.opensphere.wms.config.v1.WMSServerConfig;
import io.opensphere.wms.event.DefaultWmsConnectionParams;
import io.opensphere.wms.layer.WMSDataTypeInfo;
import io.opensphere.wms.layer.WMSLayer;
import io.opensphere.wms.toolbox.WMSToolbox;
import io.opensphere.wms.util.WMSEnvoyHelper;
import io.opensphere.wms.util.WMSQueryTracker;

/**
 * Envoy that retrieves WMS layer metadata from an OGC server.
 */
@SuppressWarnings("PMD.GodClass")
public class WMSGetCapabilitiesEnvoy extends AbstractEnvoy implements WMSEnvoy
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(WMSGetCapabilitiesEnvoy.class);

    /** Single-thread executor used for layer activation. */
    private ProcrastinatingExecutor myActivationExecutor;

    /** Listener for WMS data group activation. */
    private final ActivationListener myActiveLayerChangeListener = new AbstractActivationListener()
    {
        /** Time used to keep track of which layers have been loaded. */
        @GuardedBy("this")
        private long myReloadTime;

        @Override
        public void handleCommit(boolean active, DataGroupInfo dgi, PhasedTaskCanceller canceller)
        {
            boolean changed = false;
            if (dgi.hasMembers(false))
            {
                for (DataTypeInfo info : dgi.getMembers(false))
                {
                    if (info instanceof WMSDataTypeInfo
                            && (myLayerTree.hasMember(info, true) || myActiveLayerIds.contains(info.getTypeKey())))
                    {
                        changed |= active ? myActiveLayerIds.add(info.getTypeKey()) : myActiveLayerIds.remove(info.getTypeKey());
                    }
                }
            }

            if (changed)
            {
                long time = System.nanoTime();
                myActivationExecutor.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        synchronized (myActiveLayerChangeListener)
                        {
                            myReloadTime = time;
                            myActiveLayerChangeListener.notifyAll();
                        }
                        reloadLayers();
                    }
                });
                /* Wait until the procrastinating executor runs. */
                synchronized (myActiveLayerChangeListener)
                {
                    while (time > myReloadTime)
                    {
                        try
                        {
                            myActiveLayerChangeListener.wait();
                        }
                        catch (InterruptedException e)
                        {
                            LOGGER.error(e, e);
                        }
                    }
                }
            }
        }
    };

    /** List of active layer names. */
    private final Set<String> myActiveLayerIds = Collections.synchronizedSet(New.set());

    /** The active timed data groups. */
    private final Set<String> myActiveTimedDataGroups = Collections.synchronizedSet(New.<String>set());

    /**
     * Listener for configuration changes to the layers which are handled by
     * this envoy.
     */
    private final WMSLayerConfigChangeListener myLayerConfigChangeListener = new WMSLayerConfigChangeListener()
    {
        @Override
        public void configurationChanged(WMSLayerConfigChangeListener.WMSLayerConfigChangeEvent event)
        {
            handleLayerConfigChange(event);
        }
    };

    /** A map of layer names to layers. */
    private final Map<String, WMSLayer> myLayerMap = New.map();

    /** Concurrency lock for myLayerMap. */
    private final ReentrantReadWriteLock myLayerMapLock = new ReentrantReadWriteLock();

    /** Event listener for events that prompt layer refreshes. */
    private final EventListener<ServerRefreshEvent> myLayerRefreshEventListener;

    /** My Mantle-style layer tree. */
    private DataGroupInfo myLayerTree;

    /** The Metrics tracker. */
    private final WMSQueryTracker myMetricsTracker;

    /** The WMS preferences. */
    private final Preferences myPrefs;

    /** The server configuration. */
    private final WMSServerConfig myServerConfig;

    /** The server connection configuration. */
    private final transient ServerConnectionParams myServerConnConfig;

    /**
     * The WMS version used in requests made by this envoy (either "1.1.1" or
     * "1.3.0").
     */
    private String myWMSVersion;

    /**
     * Construct the envoy, explicitly specifying layers, bypassing
     * GetCapabilities.
     *
     * @param toolbox the toolbox used to retrieve dataRegistry, eventManager
     * @param prefs The WMS preferences.
     * @param serverConfig Server configuration.
     * @param metricsTracker the metrics tracker
     * @param servConf The server configuration.
     */
    public WMSGetCapabilitiesEnvoy(Toolbox toolbox, Preferences prefs, WMSServerConfig serverConfig,
            WMSQueryTracker metricsTracker, ServerConnectionParams servConf)
    {
        super(toolbox);
        myPrefs = prefs;
        myServerConfig = serverConfig;
        myMetricsTracker = metricsTracker;

        // Create a local copy of the connection parameters.
        myServerConnConfig = new DefaultWmsConnectionParams(servConf);

        myLayerRefreshEventListener = new EventListener<ServerRefreshEvent>()
        {
            @Override
            public void notify(ServerRefreshEvent event)
            {
                getExecutor().execute(() -> refreshLayers());
            }
        };
    }

    /**
     * Method to clear the cache for all our layers.
     *
     */
    public void clearTileCache()
    {
        getExecutor().execute(new Runnable()
        {
            @Override
            public void run()
            {
                Collection<WMSLayer> layers;
                myLayerMapLock.readLock().lock();
                try
                {
                    layers = New.collection(myLayerMap.values());
                }
                finally
                {
                    myLayerMapLock.readLock().unlock();
                }
                for (WMSLayer layer : layers)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Removing the cached layer : " + layer.getTypeInfo().getTypeKey());
                    }
                    clearLayerCache(layer.getTypeInfo());
                }
            }
        });
    }

    @Override
    public synchronized void close()
    {
        getToolbox().getEnvoyRegistry().removeObjectsForSource(this);
        if (getToolbox().getEventManager() != null && myLayerRefreshEventListener != null)
        {
            getToolbox().getEventManager().unsubscribe(ServerRefreshEvent.class, myLayerRefreshEventListener);
        }
        if (myServerConfig != null)
        {
            myServerConfig.removeLayerConfigListener(myLayerConfigChangeListener);
        }
        getToolbox().getPluginToolboxRegistry().getPluginToolbox(WMSToolbox.class)
                .notifyServerCapabilitiesListener(myServerConnConfig, null);

        // Clear any old layers for this server.
        getDataRegistry().removeModels(new DataModelCategory(toString(), WMSLayer.class.getName(), null), false);

        getToolbox().getTimeManager().releaseDataDurationRequest(this);

        super.close();
    }

    @Override
    public ActivationListener getActiveLayerChangeListener()
    {
        return myActiveLayerChangeListener;
    }

    @Override
    public WMSQueryTracker getQueryTracker()
    {
        return myMetricsTracker;
    }

    /**
     * Get my server configuration.
     *
     * @return The server configuration.
     */
    public WMSServerConfig getServerConfig()
    {
        return myServerConfig;
    }

    @Override
    public ServerConnectionParams getServerConnectionConfig()
    {
        return myServerConnConfig;
    }

    @Override
    public String getWMSVersion()
    {
        return myWMSVersion;
    }

    @Override
    public synchronized void open()
    {
        myActivationExecutor = new ProcrastinatingExecutor(getExecutor());
        String errorString = null;
        WMSServerCapabilities capabilities;
        try
        {
            capabilities = WMSEnvoyHelper.requestCapabilitiesFromServer(myServerConnConfig, getToolbox());

            // Clear old layers for this server, then submit the new layers.
            getDataRegistry().removeModels(new DataModelCategory(toString(), WMSLayer.class.getName(), null), false);

            setLayerTree(populateServerConfig(myServerConfig, capabilities));
            Collection<? extends WMSDataTypeInfo> dtis = getActiveDTIs();
            if (!dtis.isEmpty())
            {
                activateLayers(dtis);
            }

            if (myActiveTimedDataGroups.isEmpty())
            {
                getToolbox().getTimeManager().releaseDataDurationRequest(this);
            }
            else
            {
                getToolbox().getTimeManager().requestDataDurations(this, Arrays.asList(Days.ONE, Weeks.ONE, Months.ONE));
            }

            // Listen for configuration changes to any of the layers for
            // this server.
            myServerConfig.addLayerConfigListener(myLayerConfigChangeListener);
            myServerConfig.setServerState(WMSServerConfig.WMSServerState.ACTIVE);

            // Listen for layer set and refresh events
            if (getToolbox().getEventManager() != null)
            {
                getToolbox().getEventManager().subscribe(ServerRefreshEvent.class, myLayerRefreshEventListener);
            }

            getToolbox().getPluginToolboxRegistry().getPluginToolbox(WMSToolbox.class)
                    .notifyServerCapabilitiesListener(myServerConnConfig, capabilities);
            myWMSVersion = capabilities.getVersion();
        }
        catch (UnknownHostException ex)
        {
            errorString = "Unknown Host : " + ex.getMessage();
            capabilities = null;
        }
        catch (IOException ex)
        {
            errorString = ex.getLocalizedMessage() != null ? ex.getLocalizedMessage()
                    : "Connections to server failed for " + myServerConnConfig.getServerId(OGCServerSource.WMS_SERVICE);
            capabilities = null;
        }
        catch (GeneralSecurityException ex)
        {
            errorString = "Authentication failed for " + myServerConnConfig.getServerId(OGCServerSource.WMS_SERVICE);
            capabilities = null;
        }
        catch (InterruptedException e)
        {
            errorString = "Request was cancelled for " + myServerConnConfig.getServerId(OGCServerSource.WMS_SERVICE);
            capabilities = null;
        }
        catch (ServiceException e)
        {
            errorString = "Service exception encountered for server: "
                    + myServerConnConfig.getServerId(OGCServerSource.WMS_SERVICE);
            LOGGER.warn(errorString + ": " + e, e);
            capabilities = null;
        }
        catch (URISyntaxException e)
        {
            errorString = e.getMessage();
            capabilities = null;
        }
        fireStateEvent(myServerConfig, capabilities != null, errorString);
    }

    @Override
    public String toString()
    {
        String serverTitle = myServerConfig.getServerTitle();
        String id = myServerConfig.getServerId();
        return new StringBuilder().append(WMSGetCapabilitiesEnvoy.class.getSimpleName()).append(',').append(id).append(',')
                .append(serverTitle).toString();
    }

    /**
     * Fire state event.
     *
     * @param serverConf the server configuration
     * @param isValid the valid flag
     * @param error String detailing any errors that occurred.
     */
    protected void fireStateEvent(WMSServerConfig serverConf, boolean isValid, String error)
    {
        if (getToolbox().getEventManager() != null)
        {
            OGCServiceStateEvent stateEvent = new OGCServiceStateEvent(serverConf.getServerId(), serverConf.getServerTitle(),
                    OGCServerSource.WMS_SERVICE, myLayerTree, isValid);
            if (error != null)
            {
                stateEvent.setError(error);
                // Note: EventManager.setEventState() publishes the event
                getToolbox().getEventManager().setEventState(stateEvent, State.FAILED);
                LOGGER.warn(error);
            }
            else
            {
                if (serverConf.getServerState() == WMSServerConfig.WMSServerState.ACTIVE)
                {
                    // Note: EventManager.setEventState() publishes the event
                    getToolbox().getEventManager().setEventState(stateEvent, State.COMPLETED);
                }
                else
                {
                    getToolbox().getEventManager().publishEvent(stateEvent);
                }
            }
        }
    }

    /**
     * Send the WMS layers to the data registry.
     *
     * @param layers The WMS layers.
     */
    protected void sendLayersToDataRegistry(Collection<? extends WMSLayer> layers)
    {
        String source = toString();
        String family = WMSLayer.class.getName();
        Date expiration = CacheDeposit.SESSION_END;

        Collection<PropertyAccessor<WMSLayer, ?>> accessors = New.collection();
        accessors.add(UnserializableAccessor.getHomogeneousAccessor(WMSLayer.PROPERTY_DESCRIPTOR));
        accessors.add(WMSLayer.getKeyAccessor());
        for (WMSLayer layer : layers)
        {
            String category = layer.getConfiguration().getLayerName();
            CacheDeposit<WMSLayer> deposit = new DefaultCacheDeposit<WMSLayer>(new DataModelCategory(source, family, category),
                    accessors, Collections.singleton(layer), true, expiration, true);
            getDataRegistry().addModels(deposit);
        }
    }

    /**
     * Active layers based on the type info.
     *
     * @param dtis The type infos of the layers being activated.
     */
    private void activateLayers(Collection<? extends WMSDataTypeInfo> dtis)
    {
        Collection<WMSGetMapEnvoy> envoys = New.collection(dtis.size());
        Collection<WMSLayer> layers = New.collection(dtis.size());
        myLayerMapLock.writeLock().lock();
        try
        {
            for (WMSDataTypeInfo dti : dtis)
            {
                WMSLayer layer = createLayer(dti);
                layers.add(layer);
                myLayerMap.put(dti.getTypeKey(), layer);
                envoys.add(new WMSGetMapEnvoy(layer, getToolbox(), null, myServerConnConfig, myWMSVersion));
                if (!layer.isTimeless())
                {
                    myActiveTimedDataGroups.add(dti.getTypeKey());
                }
            }
        }
        finally
        {
            myLayerMapLock.writeLock().unlock();
        }
        getToolbox().getEnvoyRegistry().addObjectsForSource(this, envoys);
        sendLayersToDataRegistry(layers);
    }

    /**
     * Method to clear the cache for a specific layer. The layer configuration
     * will be replaced by the passed-in {@link WMSLayerConfigurationSet}.
     *
     * @param layerName The layer name.
     * @param wmsLCS The layer's new configuration set.
     */
    private void clearLayerCache(String layerName, WMSLayerConfigurationSet wmsLCS)
    {
        // Find the right WMSDataTypeInfo and replace the config set
        WMSDataTypeInfo typeInfo = null;
        if (myLayerTree != null && myLayerTree.hasMembers(true))
        {
            for (DataTypeInfo info : myLayerTree.getMembers(true))
            {
                if (info instanceof WMSDataTypeInfo)
                {
                    WMSDataTypeInfo wmsInfo = (WMSDataTypeInfo)info;
                    if (layerName.equals(wmsInfo.getWmsConfig().getLayerConfig().getLayerName()))
                    {
                        typeInfo = wmsInfo;
                        wmsInfo.setWmsConfig(wmsLCS);
                        break;
                    }
                }
            }
        }

        if (typeInfo != null)
        {
            clearLayerCache(typeInfo);
        }
    }

    /**
     * Method to clear the cache for a specific layer. This method assumes that
     * any configuration changes have already been made in the
     * {@link WMSDataTypeInfo}.
     *
     * @param typeInfo {@link WMSDataTypeInfo} whose layer should be cleared
     */
    private void clearLayerCache(WMSDataTypeInfo typeInfo)
    {
        Utilities.checkNull(typeInfo, "typeInfo");
        boolean layerActive = typeInfo.isEnabled();
        if (layerActive)
        {
            deactivateLayer(typeInfo.getTypeKey());
        }

        // Clear the images since the changed configuration may produce
        // different images.
        getDataRegistry().removeModels(new DataModelCategory(null, Image.class.getName(), typeInfo.getTypeKey()), false);
        getDataRegistry().removeModels(new DataModelCategory(null, StreamingImage.class.getName(), typeInfo.getTypeKey()), false);

        if (layerActive)
        {
            activateLayers(Collections.singleton(typeInfo));
        }
    }

    /**
     * Create the layer model.
     *
     * @param info The DataTypeInfo for the WMS layer to be created.
     * @return A layer model.
     */
    private WMSLayer createLayer(WMSDataTypeInfo info)
    {
        return WMSLayerCreatorImpl.getInstance().createLayer(info, 384, 1280);
    }

    /**
     * Remove a layer model.
     *
     * @param layerName the layer name
     */
    private void deactivateLayer(String layerName)
    {
        // remove the layer from my list
        WMSLayer layer;
        myLayerMapLock.writeLock().lock();
        try
        {
            layer = myLayerMap.remove(layerName);
        }
        finally
        {
            myLayerMapLock.writeLock().unlock();
        }
        if (layer != null)
        {
            if (!layer.isTimeless())
            {
                myActiveTimedDataGroups.remove(layerName);
            }
            // remove the layer from the data registry
            String source = toString();
            String key = layer.getTypeInfo().getTypeKey();
            DataModelCategory dmc = new DataModelCategory(source, WMSLayer.class.getName(), null);
            Query query = new SimpleIdQuery(dmc, key, WMSLayer.KEY_PROPERTY_DESCRIPTOR);
            long[] ids = getDataRegistry().performLocalQuery(query);
            getDataRegistry().removeModels(ids);

            Collection<WMSGetMapEnvoy> envoys = getToolbox().getEnvoyRegistry().getObjectsForSource(this, WMSGetMapEnvoy.class);
            for (WMSGetMapEnvoy envoy : envoys)
            {
                if (envoy.getWMSLayer().equals(layer))
                {
                    getToolbox().getEnvoyRegistry().removeObjects(Collections.singleton(envoy));
                    break;
                }
            }
        }
    }

    /**
     * Build the layers in the active working set.
     *
     * @return The created layers.
     */
    private Collection<? extends WMSDataTypeInfo> getActiveDTIs()
    {
        Collection<WMSDataTypeInfo> result = New.collection();

        if (myLayerTree != null && myLayerTree.hasMembers(true))
        {
            for (DataTypeInfo info : myLayerTree.getMembers(true))
            {
                if (info instanceof WMSDataTypeInfo)
                {
                    WMSDataTypeInfo wmsInfo = (WMSDataTypeInfo)info;
                    if (myActiveLayerIds.contains(wmsInfo.getTypeKey()))
                    {
                        wmsInfo.setEnabled(true);
                        result.add(wmsInfo);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Handle a change to the configuration of a layer.
     *
     * @param event Layer configuration change event.
     */
    private void handleLayerConfigChange(WMSLayerConfigChangeListener.WMSLayerConfigChangeEvent event)
    {
        clearLayerCache(event.getLayerConfig().getLayerConfig().getLayerName(), event.getLayerConfig());
    }

    /**
     * Build a WMSServerConfig from my WMS GetCapabilities document.
     *
     * @param serverConf The ServerConfig to put the layer info into.
     * @param capabilities The WMS capabilities.
     * @return The constructed DataGroupInfo tree.
     */
    private DataGroupInfo populateServerConfig(WMSServerConfig serverConf, WMSServerCapabilities capabilities)
    {
        serverConf.setUserDefinedSymbolization(capabilities.getUserDefinedSymbolization());
        serverConf.setServerTitle(capabilities.getTitle());
        if ("1.3.0".equals(capabilities.getVersion()))
        {
            return new WMS130LayerTreeBuilder(getToolbox(), myServerConnConfig, myPrefs).build130Nodes(serverConf, capabilities,
                    myActiveLayerChangeListener);
        }
        else if ("1.1.1".equals(capabilities.getVersion()))
        {
            return new WMS111LayerTreeBuilder(getToolbox(), myServerConnConfig, myPrefs).build111Nodes(serverConf, capabilities,
                    myActiveLayerChangeListener);
        }
        else
        {
            LOGGER.error("Unknown WMS Capabilities version : \"" + capabilities.getVersion() + "\".");
        }

        return null;
    }

    /**
     * Get a new capabilities document from the server and check to see if any
     * layers' times or geographic bounding boxes have changed. If so, update
     * the layer and flush its cache.
     */
    private void refreshLayers()
    {
        long startTime = System.nanoTime();
        LOGGER.info("Refreshing layers for server " + myServerConfig.getServerTitle());
        try
        {
            WMSServerCapabilities capabilities = WMSEnvoyHelper.requestCapabilitiesFromServer(myServerConnConfig, getToolbox());
            Map<String, WMSDataTypeInfo> allDtis = New.map();
            for (DataTypeInfo info : myLayerTree.getMembers(true))
            {
                if (info instanceof WMSDataTypeInfo)
                {
                    allDtis.put(info.getTypeName(), (WMSDataTypeInfo)info);
                }
            }
            for (WMSCapsLayer capsLayer : capabilities.getLayerList())
            {
                if (capsLayer.getTimeExtent() != TimeSpan.TIMELESS)
                {
                    WMSDataTypeInfo match = allDtis.get(capsLayer.getName());
                    if (match != null && capsLayer.getTimeExtent().compareEnd(match.getTimeExtents().getExtent().getEnd()) > 0)
                    {
                        match.setTimeExtents(new DefaultTimeExtents(capsLayer.getTimeExtent()), this);
                        clearLayerCache(match);
                    }
                }
            }
        }
        catch (IOException e)
        {
            LOGGER.warn("IOException caught while attempting to refresh [" + myServerConfig.getServerTitle()
                    + "]. Skipping refresh.");
        }
        catch (GeneralSecurityException e)
        {
            LOGGER.warn("GeneralSecurityException caught while attempting to refresh [" + myServerConfig.getServerTitle()
                    + "]. Skipping refresh.");
        }
        catch (InterruptedException e)
        {
            LOGGER.warn("Request was cancelled to refresh [" + myServerConfig.getServerTitle() + "]. Skipping refresh.");
        }
        catch (URISyntaxException e)
        {
            LOGGER.warn(e.getMessage(), e);
        }
        LOGGER.info(StringUtilities.formatTimingMessage("Total update time: ", System.nanoTime() - startTime));
    }

    /**
     * Load the layers that are not in the current layer list and remove those
     * that are not in the list of new layers.
     */
    private void reloadLayers()
    {
        Collection<? extends WMSDataTypeInfo> dtis;

        Set<String> currentLayerIds = null;
        myLayerMapLock.readLock().lock();
        try
        {
            currentLayerIds = New.set(myLayerMap.keySet());
        }
        finally
        {
            myLayerMapLock.readLock().unlock();
        }

        // De-activate just the layers that are not in the new layer list.
        for (Iterator<String> iter = currentLayerIds.iterator(); iter.hasNext();)
        {
            String layerId = iter.next();
            if (!myActiveLayerIds.contains(layerId))
            {
                deactivateLayer(layerId);
                iter.remove();
            }
        }

        // Activate just the layers that are not already loaded.
        dtis = getActiveDTIs();
        for (Iterator<? extends WMSDataTypeInfo> iter = dtis.iterator(); iter.hasNext();)
        {
            WMSDataTypeInfo dti = iter.next();
            String layerName = dti.getTypeKey();
            if (currentLayerIds.contains(layerName))
            {
                iter.remove();
            }
        }
        if (CollectionUtilities.hasContent(dtis))
        {
            activateLayers(dtis);
        }

        if (myActiveTimedDataGroups.isEmpty())
        {
            getToolbox().getTimeManager().releaseDataDurationRequest(this);
        }
        else
        {
            getToolbox().getTimeManager().requestDataDurations(this, Arrays.asList(Days.ONE, Weeks.ONE, Months.ONE));
        }
    }

    /**
     * Set the layer tree.
     *
     * @param layerTree The layer tree.
     */
    private void setLayerTree(DataGroupInfo layerTree)
    {
        myLayerTree = layerTree;
    }
}
