package io.opensphere.server;

import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.ProxySelector;
import java.util.Collection;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.apache.log4j.Logger;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.adapter.AbstractWindowMenuItemPlugin;
import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.control.ui.ToolbarManager.ToolbarLocation;
import io.opensphere.core.event.ApplicationLifecycleEvent;
import io.opensphere.core.event.ApplicationLifecycleEvent.Stage;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.options.OptionsProvider;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.StreamingServer;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.event.ServerManagerDialogChangeEvent;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.server.control.OGCServerRefreshOptionsProvider;
import io.opensphere.server.control.OGCServerTimeoutsOptionsProvider;
import io.opensphere.server.customization.ArcGisCustomization;
import io.opensphere.server.customization.DefaultCustomization;
import io.opensphere.server.customization.GeoServerCustomization;
import io.opensphere.server.display.OGCServerValidationEnvoy;
import io.opensphere.server.event.OverallServerStatusEvent.OverallServerStatus;
import io.opensphere.server.manager.ServerManagerDialog;
import io.opensphere.server.serverprovider.HttpServerProvider;
import io.opensphere.server.serverprovider.ProxySelectorImpl;
import io.opensphere.server.serverprovider.http.factory.HttpServerFactory;
import io.opensphere.server.serverprovider.streaming.StreamingServerProvider;
import io.opensphere.server.services.ServerConfigEvent;
import io.opensphere.server.services.ServerConfigEvent.ServerEventAction;
import io.opensphere.server.state.DefaultWFSLayerConfiguration;
import io.opensphere.server.state.StateConstants;
import io.opensphere.server.toolbox.ServerSourceController;
import io.opensphere.server.toolbox.ServerSourceControllerManager;
import io.opensphere.server.toolbox.ServerToolboxUtils;
import io.opensphere.server.toolbox.WFSLayerConfigurationManager;
import io.opensphere.server.toolbox.impl.ServerToolboxImpl;

/**
 * Main control class for the WMS plugin.
 */
public class OGCServerPlugin extends AbstractWindowMenuItemPlugin
{
    /**
     * The {@link Logger} instance used to capture output.
     */
    private static final Logger LOG = Logger.getLogger(OGCServerPlugin.class);

    /** The Options provider. */
    private OptionsProvider myOptionsProvider;

    /** The server configuration GUI that allows users to add/manage servers. */
    private ServerManagerDialog myServerManagerDialog;

    /** Envoy for server validation. */
    @SuppressWarnings("PMD.SingularField")
    private OGCServerValidationEnvoy myValidationEnvoy;

    /** The Activation button. */
    private JButton myServerConfigActivationButton;

    /** The Current status color. */
    private Color myCurrentStatusColor;

    /**
     * ServerConfigEvent listener.
     */
    private transient EventListener<ServerConfigEvent> myServerConfigEventListener;

    /** The Server manager visibility change listener. */
    private transient EventListener<ServerManagerDialogChangeEvent> myServerManagerDialogChangeListener;

    /** Lifecycle event listener that triggers the Controller to initialize. */
    private EventListener<ApplicationLifecycleEvent> myLifecycleListener;

    /**
     * Default constructor that sets up super class.
     */
    public OGCServerPlugin()
    {
        super("Servers", false, false);
    }

    @Override
    public void close()
    {
        getToolbox().getEventManager().unsubscribe(ServerConfigEvent.class, myServerConfigEventListener);
    }

    @Override
    public Collection<? extends Envoy> getEnvoys()
    {
        return Collections.singleton(myValidationEnvoy);
    }

    @Override
    public void initialize(PluginLoaderData data, Toolbox toolbox)
    {
        super.initialize(data, toolbox);
        LOG.info("OGC Server Plugin initialized.");

        ProxySelector.setDefault(new ProxySelectorImpl(toolbox.getSystemToolbox().getNetworkConfigurationManager()));

        createServerToolbox(toolbox);
        toolbox.getServerProviderRegistry().registerProvider(HttpServer.class,
                new HttpServerProvider(toolbox, new HttpServerFactory()));
        toolbox.getServerProviderRegistry().registerProvider(StreamingServer.class, new StreamingServerProvider());

        myValidationEnvoy = new OGCServerValidationEnvoy(toolbox);

        myOptionsProvider = new OGCServerTimeoutsOptionsProvider(toolbox);
        myOptionsProvider
                .addSubTopic(new OGCServerRefreshOptionsProvider(toolbox));
        toolbox.getUIRegistry().getOptionsRegistry().addOptionsProvider(myOptionsProvider);

        toolbox.getUIRegistry().getIconLegendRegistry().addIconToLegend(IconUtil.getNormalIcon(IconType.STORAGE), "Servers",
                "Opens the Servers dialog which allows adding, editing, and deleting servers. "
                        + "This icon will be green, yellow, or red based on the status the the current set of servers.");

        myServerConfigEventListener = new EventListener<ServerConfigEvent>()
        {
            @Override
            public void notify(ServerConfigEvent event)
            {
                if (event.getEventAction() == ServerEventAction.REMOVE || event.getEventAction() == ServerEventAction.LOADCOMPLETE
                        || event.getEventAction() == ServerEventAction.DEACTIVATE)
                {
                    updateOverallStatus();
                }
            }
        };
        toolbox.getEventManager().subscribe(ServerConfigEvent.class, myServerConfigEventListener);

        myServerManagerDialogChangeListener = new EventListener<ServerManagerDialogChangeEvent>()
        {
            @Override
            public void notify(ServerManagerDialogChangeEvent event)
            {
                switch (event.getEventType())
                {
                    case VISIBILITY_CHANGE:
                        doServerManagerVisibilityChangedAction();
                        break;

                    case SERVER_STATUS_REQUEST:
                        getToolbox().getEventManager()
                                .publishEvent(new ServerManagerDialogChangeEvent(this, myCurrentStatusColor));
                        break;

                    default:
                        break;
                }
            }
        };
        toolbox.getEventManager().subscribe(ServerManagerDialogChangeEvent.class, myServerManagerDialogChangeListener);

        myLifecycleListener = this::handleApplicationLifecycleEvent;
        toolbox.getEventManager().subscribe(ApplicationLifecycleEvent.class, myLifecycleListener);
    }

    @Override
    protected Window createWindow(Toolbox toolbox)
    {
        if (myServerManagerDialog == null)
        {
            myServerManagerDialog = new ServerManagerDialog(toolbox);
            updateOverallStatus();
        }
        return myServerManagerDialog;
    }

    /**
     * Handles lifecycle events so that the controller can be initialized after
     * the plugins are all initialized.
     *
     * @param event the event
     */
    private void handleApplicationLifecycleEvent(ApplicationLifecycleEvent event)
    {
        if (event.getStage() == Stage.PLUGINS_INITIALIZED)
        {
            EventQueueUtilities.invokeLater(() ->
            {
                myServerConfigActivationButton = getServerActivationButton();
                getToolbox().getUIRegistry().getToolbarComponentRegistry().registerToolbarComponent(ToolbarLocation.SOUTH,
                        "ServerManager", myServerConfigActivationButton, 200, SeparatorLocation.NONE);
            });
            setStatusIcon(OverallServerStatus.UNKNOWN);
        }
    }

    /**
     * Creates a server toolbox that gets used by the plugins that handle each
     * OGC Service.
     *
     * @param toolbox the toolbox
     */
    private void createServerToolbox(Toolbox toolbox)
    {
        ServerToolboxImpl serverToolbox = new ServerToolboxImpl(toolbox, getClass());

        WFSLayerConfigurationManager configurationManager = new WFSLayerConfigurationManager();
        configurationManager.addServerConfiguration(new DefaultWFSLayerConfiguration(StateConstants.WFS_LAYER_TYPE,
                DefaultCustomization.class));
        configurationManager.addServerConfiguration(new DefaultWFSLayerConfiguration("arcWFS", ArcGisCustomization.class, true));
        configurationManager
                .addServerConfiguration(new DefaultWFSLayerConfiguration("geoserverWFS", GeoServerCustomization.class, true));
        serverToolbox.setStateConfigurationManager(configurationManager);

        toolbox.getPluginToolboxRegistry().registerPluginToolbox(serverToolbox);
    }

    /**
     * Do server manager visibility changed action.
     */
    private void doServerManagerVisibilityChangedAction()
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                Window serverManagerDialog = getWindow();
                serverManagerDialog.setVisible(false);
                serverManagerDialog.setVisible(true);
            }
        });
    }

    /**
     * Creates a button for the toolbar that activates/deactivates servers.
     *
     * @return the Server Activation Button
     */
    private JButton getServerActivationButton()
    {
        LOG.info("Creating button.");
        JButton activationButton = new JButton();
        activationButton.setSize(26, 26);
        activationButton.setPreferredSize(activationButton.getSize());
        activationButton.setMinimumSize(activationButton.getSize());
        activationButton.setBorder(BorderFactory.createEmptyBorder());
        activationButton.setFocusPainted(false);
        activationButton.setContentAreaFilled(false);
        activationButton.setToolTipText("Manage Servers");
        activationButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                doServerManagerVisibilityChangedAction();
            }
        });
        return activationButton;
    }

    /**
     * Set the overall server status by looping through each server and
     * determining whether there were any problems initializing it.
     *
     * @param status the new status used to set the frame's icon
     */
    private void setStatusIcon(OverallServerStatus status)
    {
        switch (status)
        {
            case GOOD:
                myCurrentStatusColor = Color.GREEN;
                break;

            case DEGRADED:
                myCurrentStatusColor = Color.YELLOW;
                break;

            case FAILED:
                myCurrentStatusColor = Color.RED;
                break;

            case UNKNOWN:
            default:
                myCurrentStatusColor = IconUtil.DEFAULT_ICON_FOREGROUND;
                break;
        }

        getToolbox().getEventManager().publishEvent(new ServerManagerDialogChangeEvent(this, myCurrentStatusColor));

        EventQueueUtilities.invokeLater(() ->
        {
            if (myServerConfigActivationButton != null)
            {
                IconUtil.setIcons(myServerConfigActivationButton, IconType.STORAGE, myCurrentStatusColor);
                if (myServerManagerDialog != null)
                {
                    myServerManagerDialog.setIconImage(((ImageIcon)myServerConfigActivationButton.getIcon()).getImage());
                }
            }
        });
    }

    /**
     * Determines the overall status of all the servers updates appropriate
     * GUIs.
     */
    private void updateOverallStatus()
    {
        boolean anyFailed = false;
        boolean allFailed = true;
        ServerSourceControllerManager controllerManager = ServerToolboxUtils.getServerSourceControllerManager(getToolbox());
        for (ServerSourceController controller : controllerManager.getControllers())
        {
            for (IDataSource source : controller.getSourceList())
            {
                if (source.isActive() || source.loadError())
                {
                    anyFailed |= source.loadError();
                    allFailed &= source.loadError();
                }
            }
        }

        OverallServerStatus status;
        if (anyFailed)
        {
            if (allFailed)
            {
                status = OverallServerStatus.FAILED;
            }
            else
            {
                status = OverallServerStatus.DEGRADED;
            }
        }
        else
        {
            if (allFailed)
            {
                status = OverallServerStatus.UNKNOWN;
            }
            else
            {
                status = OverallServerStatus.GOOD;
            }
        }

        setStatusIcon(status);
    }
}
