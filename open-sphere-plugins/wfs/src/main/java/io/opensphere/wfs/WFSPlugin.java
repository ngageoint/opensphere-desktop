package io.opensphere.wfs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.hud.awt.HUDJInternalFrame;
import io.opensphere.core.options.OptionsProvider;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.server.services.ServerConfigEvent;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.state.StateConstants;
import io.opensphere.server.toolbox.ServerToolboxUtils;
import io.opensphere.server.toolbox.ServerValidatorRegistry;
import io.opensphere.server.util.ServerConstants;
import io.opensphere.wfs.envoy.AbstractWFSEnvoy;
import io.opensphere.wfs.envoy.DefaultWFSEnvoyFactory;
import io.opensphere.wfs.envoy.WFSDownloadMonitorDisplay;
import io.opensphere.wfs.envoy.WFSEnvoyHelper;
import io.opensphere.wfs.envoy.WFSToolbox;
import io.opensphere.wfs.envoy.WFSTools;
import io.opensphere.wfs.mantle.WFSQueryController;
import io.opensphere.wfs.state.WFSStateController;
import io.opensphere.wfs.transformer.WFSTransformer;

/**
 * The main class for the WFS plug-in.
 */
public class WFSPlugin extends PluginAdapter
{
    /**
     * The {@link Logger} instance used to capture output.
     */
    private static final Logger LOG = Logger.getLogger(WFSPlugin.class);

    /** Envoys that request WFS features from the server. */
    private final Collection<AbstractWFSEnvoy> myEnvoys = New.list();

    /** My download monitor GUI. */
    private WFSDownloadMonitorDisplay myMonitorGUI;

    /** The Options provider. */
    private WFSPluginOptionsProvider myOptionsProvider;

    /** The tool box used by plugins to interact with the rest of the system. */
    private Toolbox myToolbox;

    /** The transformer that generates point geometries from WFSPoint models. */
    private WFSTransformer myTransformer;

    /** My WFS server event listener. */
    private EventListener<? super ServerConfigEvent> myWFSServerEventListener;

    /** Holder class for various WFS tools. */
    private WFSTools myWFSTools;

    /** The WFS state manager. */
    private WFSStateController myWFSStateManager;

    /** The ContextMenuProvider. */
    private ContextMenuProvider<DataGroupContextKey> myContextMenuProvider;

    /** The toolbox used to access WFS Plugin state. */
    private WFSToolbox myWfsToolbox;

    /**
     * Queries for new data or removes data as load spans change and query regions change.
     */
    private WFSQueryController myQueryController;

    @Override
    public void close()
    {
        myToolbox.getUIRegistry().getOptionsRegistry().removeOptionsProvider(myOptionsProvider);
        myToolbox.getEventManager().unsubscribe(ServerConfigEvent.class, myWFSServerEventListener);
        myQueryController.close();
        myWFSServerEventListener = null;

        myToolbox.getPluginToolboxRegistry().removePluginToolbox(myWfsToolbox);

        if (myContextMenuProvider != null)
        {
            ContextActionManager manager = myToolbox.getUIRegistry().getContextActionManager();
            manager.deregisterContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT, DataGroupContextKey.class,
                    myContextMenuProvider);
            myContextMenuProvider = null;
        }
    }

    @Override
    public synchronized Collection<? extends Envoy> getEnvoys()
    {
        return New.list(myEnvoys);
    }

    @Override
    public Collection<? extends Transformer> getTransformers()
    {
        return Collections.singleton(myTransformer);
    }

    @Override
    public void initialize(PluginLoaderData data, Toolbox toolbox)
    {
        LOG.debug("Initializing OpenSphere WFS Plugin.");
        myToolbox = toolbox;
        myTransformer = new WFSTransformer(toolbox);
        myQueryController = new WFSQueryController(toolbox);
        myWFSTools = new WFSTools(toolbox);
        myWfsToolbox = new WFSToolbox();
        myWfsToolbox.setEnvoyFactory(new DefaultWFSEnvoyFactory());
        myWfsToolbox.setEnvoyHelper(new WFSEnvoyHelper());
        myToolbox.getPluginToolboxRegistry().registerPluginToolbox(myWfsToolbox);

        myOptionsProvider = new WFSPluginOptionsProvider(toolbox.getPreferencesRegistry());

        OptionsProvider serverOptionsProvider = myToolbox.getUIRegistry().getOptionsRegistry()
                .getRootProviderByTopic(ServerConstants.OGC_SERVER_OPTIONS_PROVIDER_MAIN_TOPIC);
        if (serverOptionsProvider == null)
        {
            myToolbox.getUIRegistry().getOptionsRegistry().addOptionsProvider(myOptionsProvider);
        }
        else
        {
            serverOptionsProvider.addSubTopic(myOptionsProvider);
        }

        registerValidator(toolbox);
        createEventListeners(toolbox.getEventManager());

        EventQueueUtilities.runOnEDT(() -> initializeMenuBar(myToolbox));

        ContextActionManager manager = toolbox.getUIRegistry().getContextActionManager();
        if (manager != null)
        {
            myContextMenuProvider = new WFSContextMenuProvider(MantleToolboxUtils.getMantleToolbox(toolbox),
                    toolbox.getEventManager(), toolbox.getUIRegistry().getMainFrameProvider());
            manager.registerContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT, DataGroupContextKey.class,
                    myContextMenuProvider);
        }

        myWFSStateManager = new WFSStateController(myToolbox);
        myToolbox.getModuleStateManager().registerModuleStateController(StateConstants.MODULE_NAME, myWFSStateManager);
    }

    /**
     * Add a new server.
     *
     * @param id the unique id for the server that should be added
     * @param server Configuration (URLs, etc) for the server that should be
     *            added.
     */
    protected synchronized void addServer(String id, ServerConnectionParams server)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Adding server '" + id + "' with OpenSphere WFS Plugin.");
        }
        AbstractWFSEnvoy env = findEnvoy(id);
        if (env == null)
        {
            // String lowerCaseUrl = server.getWfsUrl().toLowerCase();
            // if (lowerCaseUrl.contains("arcgis") &&
            // lowerCaseUrl.contains("/rest/"))
            // {
            // env = new ArcRestEnvoy(myToolbox, server, myWFSTools);
            // }
            // else
            // {
            env = myWfsToolbox.getEnvoyFactory().createEnvoy(myToolbox,
                    myToolbox.getPreferencesRegistry().getPreferences(WFSPlugin.class), server, myWFSTools);
            // }
            myEnvoys.add(env);
            myToolbox.getEnvoyRegistry().addObjectsForSource(this, Collections.singleton(env));
        }
    }

    /**
     * Find Envoy in list based on the server URL.
     *
     * @param wfsUrl The server URL used to match an available envoy.
     * @return The local envoy whose URL matches the server URL or null
     */
    protected AbstractWFSEnvoy findEnvoy(String wfsUrl)
    {
        for (AbstractWFSEnvoy envoy : myEnvoys)
        {
            if (envoy.getGetCapabilitiesURL() != null && envoy.getGetCapabilitiesURL().equals(wfsUrl))
            {
                return envoy;
            }
        }

        return null;
    }

    /**
     * Remove a server.
     *
     * @param id the unique id for the server that should be removed.
     */
    protected synchronized void removeServer(String id)
    {
        AbstractWFSEnvoy removeEnvoy = findEnvoy(id);
        if (removeEnvoy != null)
        {
            myToolbox.getEnvoyRegistry().removeObjectsForSource(this, Collections.singleton(removeEnvoy));
            myEnvoys.remove(removeEnvoy);
        }
    }

    /**
     * Creates the menu item used to launch the Download Monitor GUI.
     *
     * @return JMenuItem The Download Monitor menu item
     */
    private JMenuItem createDownloadMonitorMenuItem()
    {
        final JMenuItem monitorMenuItem = new JMenuItem("Download Monitor");
        monitorMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                showDownloadMonitor();
            }
        });
        return monitorMenuItem;
    }

    /**
     * Create the event listeners for this class.
     *
     * @param eventManager The Event Manager to subscribe with to receive Events
     */
    private void createEventListeners(EventManager eventManager)
    {
        if (eventManager != null)
        {
            myWFSServerEventListener = new EventListener<ServerConfigEvent>()
            {
                @Override
                public void notify(ServerConfigEvent event)
                {
                    String serverId = event.getServer() == null ? null
                            : event.getServer().getServerId(OGCServerSource.WFS_SERVICE);
                    if (StringUtils.isNotEmpty(serverId))
                    {
                        switch (event.getEventAction())
                        {
                            case ACTIVATE:
                                ThreadUtilities.runCpu(() -> addServer(serverId, event.getServer()));
                                break;
                            case DEACTIVATE:
                                ThreadUtilities.runCpu(() -> removeServer(serverId));
                                break;
                            default:
                                break;
                        }
                    }
                }
            };
            eventManager.subscribe(ServerConfigEvent.class, myWFSServerEventListener);
        }
    }

    /**
     * Add menu bar items that are related to WFS features.
     *
     * @param toolbox The Toolbox to use.
     */
    private void initializeMenuBar(final Toolbox toolbox)
    {
        if (toolbox.getUIRegistry() != null)
        {
            JMenu debugMenu = toolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR,
                    MenuBarRegistry.DEBUG_MENU, "Features");
            if (debugMenu != null)
            {
                debugMenu.add(createDownloadMonitorMenuItem());
            }
        }
    }

    /**
     * Register with the Server plugin for WFS Service validation requests.
     *
     * @param toolbox the toolbox
     */
    private void registerValidator(Toolbox toolbox)
    {
        ServerValidatorRegistry validatorRegistry = ServerToolboxUtils.getServerValidatorRegistry(toolbox);
        if (validatorRegistry != null)
        {
            WFSEnvoyHelper envoyHelper = toolbox.getPluginToolboxRegistry().getPluginToolbox(WFSToolbox.class).getEnvoyHelper();
            validatorRegistry.register(new WFSValidator(toolbox.getServerProviderRegistry(), envoyHelper));
        }
    }

    /** Display the download monitor. */
    private void showDownloadMonitor()
    {
        if (myMonitorGUI == null)
        {
            myMonitorGUI = new WFSDownloadMonitorDisplay(myToolbox, myWFSTools.getDownloadMonitor());
            HUDJInternalFrame.Builder builder = new HUDJInternalFrame.Builder();
            builder.setInternalFrame(myMonitorGUI);
            HUDJInternalFrame lmFrame = new HUDJInternalFrame(builder);
            myToolbox.getUIRegistry().getComponentRegistry().addObjectsForSource(this, Collections.singleton(lmFrame));
        }
        myMonitorGUI.setVisible(true);
    }
}
