package io.opensphere.wms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.PluginProperty;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.net.OpenSphereContentHandlerFactory;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.lidar.elevation.LidarImageCache;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;
import io.opensphere.server.services.ServerConfigEvent;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.toolbox.ServerToolboxUtils;
import io.opensphere.server.toolbox.ServerValidatorRegistry;
import io.opensphere.wms.config.v1.WMSLayerConfigChangeListener.WMSLayerConfigChangeEvent;
import io.opensphere.wms.config.v1.WMSServerConfig;
import io.opensphere.wms.envoy.WMSGetCapabilitiesEnvoy;
import io.opensphere.wms.layer.WMSDataTypeInfo;
import io.opensphere.wms.state.DataLayerStateController;
import io.opensphere.wms.state.MapLayerStateController;
import io.opensphere.wms.toolbox.impl.WMSToolboxImpl;
import io.opensphere.wms.util.ExceptionContentHandler;
import io.opensphere.wms.util.WMSQueryTracker;

/**
 * Main control class for the WMS plugin.
 */
@SuppressWarnings("PMD.GodClass")
public class WMSPlugin extends PluginAdapter
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(WMSPlugin.class);

    /** The envoys that retrieve the WMS data. */
    private final Collection<Envoy> myEnvoys = new CopyOnWriteArrayList<>();

    /** The WMS Plugin Executor. */
    private Executor myPluginExecutor;

    /** The Query metrics tracker. */
    private WMSQueryTracker myQueryMetricsTracker;

    /** My WMS Server Event listener. */
    private EventListener<ServerConfigEvent> myServerConfigEventListener;

    /** The tool box. */
    private Toolbox myToolbox;

    /** The transformer that converts the WMS models into geometries. */
    private WMSTransformer myTransformer;

    /**
     * Saves WMS data layers to the system state.
     */
    private DataLayerStateController myDataLayerStateController;

    /**
     * Saves WMS map layers to the system state.
     */
    private MapLayerStateController myMapLayerStateController;

    /** The ContextMenuProvider. */
    private final ContextMenuProvider<DataGroupContextKey> myContextMenuProvider = new ContextMenuProvider<DataGroupContextKey>()
    {
        @Override
        public List<JMenuItem> getMenuItems(final String contextId, final DataGroupContextKey key)
        {
            if (key.getDataType() instanceof WMSDataTypeInfo)
            {
                final WMSDataTypeInfo wmsDataType = (WMSDataTypeInfo)key.getDataType();
                JMenuItem mapOptionsMI = new JMenuItem("Edit Map Options");
                mapOptionsMI.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent evt)
                    {
                        wmsDataType.showConfig(myToolbox.getUIRegistry().getMainFrameProvider().get());
                    }
                });
                JMenuItem reloadMI = new JMenuItem("Reload Tiles");
                reloadMI.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent evt)
                    {
                        WMSLayerConfigChangeEvent event = new WMSLayerConfigChangeEvent(wmsDataType.getWmsConfig());
                        wmsDataType.getWmsConfig().getServerConfig().notifyLayerConfigChanged(event);
                    }
                });
                return Arrays.asList(reloadMI, mapOptionsMI);
            }
            return null;
        }

        @Override
        public int getPriority()
        {
            return 0;
        }
    };

    @Override
    public void close()
    {
        LidarImageCache.getInstance().close();
        if (myServerConfigEventListener != null)
        {
            myToolbox.getEventManager().unsubscribe(ServerConfigEvent.class, myServerConfigEventListener);
        }

        ContextActionManager manager = myToolbox.getUIRegistry().getContextActionManager();
        manager.deregisterContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT, DataGroupContextKey.class,
                myContextMenuProvider);
        myToolbox.getModuleStateManager().unregisterModuleStateController(DataLayerStateController.MODULE_NAME,
                myDataLayerStateController);
        myToolbox.getModuleStateManager().unregisterModuleStateController(MapLayerStateController.MODULE_NAME,
                myMapLayerStateController);
    }

    @Override
    public Collection<? extends Envoy> getEnvoys()
    {
        return Collections.unmodifiableCollection(myEnvoys);
    }

    @Override
    public Collection<? extends Transformer> getTransformers()
    {
        return Collections.singleton(myTransformer);
    }

    @Override
    public void initialize(PluginLoaderData data, Toolbox toolbox)
    {
        myPluginExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("WMSPlugin"));
        myToolbox = toolbox;
        myToolbox.getPluginToolboxRegistry().registerPluginToolbox(new WMSToolboxImpl(toolbox));
        createQueryMetricsTracker(toolbox);
        createTransformer(toolbox);

        OpenSphereContentHandlerFactory.getInstance().registerContentHandler(ExceptionContentHandler.MIME_TYPE,
                ExceptionContentHandler.class);

        File blueMarbleFolder = null;
        boolean getSingleBlueMarbleFromDisk = false;

        List<PluginProperty> pluginProperties = data.getPluginProperty();
        for (PluginProperty pluginProperty : pluginProperties)
        {
            String key = pluginProperty.getKey();
            String value = StringUtilities.expandProperties(pluginProperty.getValue(), System.getProperties());
            if ("loadBlueMarbleFromDisk".equalsIgnoreCase(key))
            {
                blueMarbleFolder = new File(value);
            }
            else if ("loadSingleBlueMarbleFromDisk".equalsIgnoreCase(key))
            {
                getSingleBlueMarbleFromDisk = Boolean.parseBoolean(value);
            }
            else
            {
                LOGGER.warn("Unexpected plugin property for plugin [" + data.getId() + "]: " + key);
            }
        }

        if (blueMarbleFolder != null)
        {
            addFileEnvoy(toolbox, blueMarbleFolder);
        }
        if (getSingleBlueMarbleFromDisk)
        {
            addSingleFileEnvoy(toolbox);
        }

        registerValidator(toolbox);
        createEventListeners();

        createMenuItems(toolbox);

        createStateModule(data);
    }

    /**
     * Add the file WMS envoy.
     *
     * @param toolbox The toolbox to pass to the envoy.
     * @param blueMarbleFolder The folder for the BMNG files.
     */
    protected void addFileEnvoy(Toolbox toolbox, File blueMarbleFolder)
    {
        String extension = ".dds";
        myEnvoys.add(new FileWMSEnvoy(toolbox, blueMarbleFolder.getAbsolutePath(), extension));
    }

    /**
     * Add a new server.
     *
     * @param server The server to add.
     */
    protected void addNewServer(ServerConnectionParams server)
    {
        String serverId = server.getServerId(OGCServerSource.WMS_SERVICE);
        Preferences prefs = myToolbox.getPreferencesRegistry().getPreferences(WMSPlugin.class);
        WMSServerConfig serverConfig = prefs.getJAXBObject(WMSServerConfig.class, serverId, null);
        if (serverConfig == null)
        {
            serverConfig = new WMSServerConfig();
            serverConfig.setServerId(serverId);
        }
        WMSGetCapabilitiesEnvoy env = new WMSGetCapabilitiesEnvoy(myToolbox,
                myToolbox.getPreferencesRegistry().getPreferences(WMSPlugin.class), serverConfig, myQueryMetricsTracker, server);
        myEnvoys.add(env);
        myToolbox.getEnvoyRegistry().addObjectsForSource(this, Collections.singleton(env));
    }

    /**
     * Add the single file WMS envoy.
     *
     * @param toolbox The toolbox to pass to the envoy.
     */
    protected void addSingleFileEnvoy(Toolbox toolbox)
    {
        myEnvoys.add(new SingleFileWMSEnvoy(toolbox));
    }

    /**
     * Creates the query metrics tracker.
     *
     * @param toolbox the toolbox
     */
    protected void createQueryMetricsTracker(Toolbox toolbox)
    {
        myQueryMetricsTracker = new WMSQueryTracker(toolbox, "WMS");
    }

    /**
     * Create the WMS transformer.
     *
     * @param toolbox the toolbox
     */
    protected void createTransformer(Toolbox toolbox)
    {
        myTransformer = new WMSTransformer(toolbox, myQueryMetricsTracker);
    }

    /**
     * Find Envoy in list based on a serverConfig.
     *
     * @param id the id that uniquely identifies a server, usually the URL.
     * @return The local envoy whose URL matches the server config or null
     */
    protected WMSGetCapabilitiesEnvoy findEnvoy(String id)
    {
        WMSGetCapabilitiesEnvoy returnEnvoy = null;
        for (Envoy envoy : myEnvoys)
        {
            if (envoy instanceof WMSGetCapabilitiesEnvoy)
            {
                WMSGetCapabilitiesEnvoy wmsEnvoy = (WMSGetCapabilitiesEnvoy)envoy;
                if (wmsEnvoy.getServerConfig() != null && wmsEnvoy.getServerConfig().getServerId().equals(id))
                {
                    returnEnvoy = wmsEnvoy;
                    break;
                }
            }
        }

        return returnEnvoy;
    }

    /**
     * Create the event listeners for this class.
     */
    private void createEventListeners()
    {
        myServerConfigEventListener = new EventListener<ServerConfigEvent>()
        {
            @Override
            public void notify(final ServerConfigEvent event)
            {
                myPluginExecutor.execute(() -> handleServerConfigEvent(event));
            }
        };
        myToolbox.getEventManager().subscribe(ServerConfigEvent.class, myServerConfigEventListener);
    }

    /**
     * Create the menu items for WMS.
     *
     * @param toolbox The toolbox.
     */
    private void createMenuItems(final Toolbox toolbox)
    {
        // Create menu option to clear tile cache
        final JMenuItem clearCacheMenuItem = new JMenuItem("Clear WMS Tile Cache");
        clearCacheMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                for (Envoy envoy : myEnvoys)
                {
                    if (envoy instanceof WMSGetCapabilitiesEnvoy)
                    {
                        LOGGER.info("Clearing tile cache for " + envoy.toString());
                        ((WMSGetCapabilitiesEnvoy)envoy).clearTileCache();
                    }
                }
            }
        });

        EventQueueUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                // Get the edit menu
                MenuBarRegistry mbr = toolbox.getUIRegistry().getMenuBarRegistry();
                JMenu editMenu = mbr.getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.EDIT_MENU);
                if (editMenu == null)
                {
                    throw new IllegalStateException("Edit menu cannot be found.");
                }
                editMenu.add(clearCacheMenuItem);
            }
        });

        ContextActionManager manager = toolbox.getUIRegistry().getContextActionManager();
        manager.registerContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT, DataGroupContextKey.class,
                myContextMenuProvider);
    }

    /**
     * Creates the state controller and registers it with the system.
     *
     * @param data the plugin loader data.
     */
    private void createStateModule(PluginLoaderData data)
    {
        myDataLayerStateController = new DataLayerStateController(this, myToolbox);
        myToolbox.getModuleStateManager().registerModuleStateController(DataLayerStateController.MODULE_NAME,
                myDataLayerStateController);

        myMapLayerStateController = new MapLayerStateController(this, myToolbox);
        myToolbox.getModuleStateManager().registerModuleStateController(MapLayerStateController.MODULE_NAME,
                myMapLayerStateController);
    }

    /**
     * Handles a ServerConfigEvent.
     *
     * @param event The ServerConfigEvent
     */
    private void handleServerConfigEvent(final ServerConfigEvent event)
    {
        String serverId = event.getServer() == null ? null : event.getServer().getServerId(OGCServerSource.WMS_SERVICE);
        if (StringUtils.isNotEmpty(serverId))
        {
            switch (event.getEventAction())
            {
                case ACTIVATE:
                    WMSGetCapabilitiesEnvoy thisEnvoy = findEnvoy(serverId);
                    if (thisEnvoy == null)
                    {
                        addNewServer(event.getServer());
                    }
                    break;
                case DEACTIVATE:
                    WMSGetCapabilitiesEnvoy removeEnvoy = findEnvoy(serverId);
                    if (removeEnvoy != null)
                    {
                        myToolbox.getEnvoyRegistry().removeObjectsForSource(this, Collections.singleton(removeEnvoy));
                        myEnvoys.remove(removeEnvoy);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Register a validator with the Server plugin to allow WMS validation.
     *
     * @param toolbox the toolbox
     */
    private void registerValidator(Toolbox toolbox)
    {
        ServerValidatorRegistry validatorRegistry = ServerToolboxUtils.getServerValidatorRegistry(toolbox);
        if (validatorRegistry != null)
        {
            validatorRegistry.register(new WMSValidator(toolbox));
        }
    }
}
