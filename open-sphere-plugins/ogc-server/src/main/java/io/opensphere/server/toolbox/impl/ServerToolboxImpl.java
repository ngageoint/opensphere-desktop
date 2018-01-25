package io.opensphere.server.toolbox.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.server.permalink.PermalinkControllerImpl;
import io.opensphere.server.state.ServerStateControllerImpl;
import io.opensphere.server.toolbox.PermalinkController;
import io.opensphere.server.toolbox.ServerLabelGenerator;
import io.opensphere.server.toolbox.ServerListManager;
import io.opensphere.server.toolbox.ServerRefreshController;
import io.opensphere.server.toolbox.ServerSourceControllerManager;
import io.opensphere.server.toolbox.ServerStateController;
import io.opensphere.server.toolbox.ServerToolbox;
import io.opensphere.server.toolbox.ServerValidatorRegistry;
import io.opensphere.server.toolbox.WFSLayerConfigurationManager;

/** Default implementation of the {@link ServerToolbox}. */
public class ServerToolboxImpl implements ServerToolbox
{
    /** Server layer list manager. */
    private ServerListManager myLayerListManager;

    /** Provides the permalink URL for a specified server. */
    private PermalinkController myPermalinkController;

    /** The OGC Server Plugin Executor. */
    private ExecutorService myPluginExecutor;

    /** Server refresh controller. */
    private ServerRefreshController myRefreshController;

    /** Server source controller manager. */
    private ServerSourceControllerManager myServerSourceControllerManager;

    /** The controller that restores server connections from a state file. */
    private ServerStateController myStateController;

    /** Server validator registry. */
    private ServerValidatorRegistry myValidatorRegistry;

    /**
     * The label generator used to create human readable descriptions of various
     * server types.
     */
    private ServerLabelGenerator myServerLabelGenerator;

    /**
     * The state configuration manager instance used to manage layer
     * configurations within the OpenSphere Saved State XML model.
     */
    private WFSLayerConfigurationManager myConfigurationManager;

    /**
     * Instantiates a default implementation of the {@link ServerToolbox}.
     *
     * @param toolbox the Core toolbox
     * @param preferencesTopic the string used to retrieve the top-level server
     *            preferences
     */
    public ServerToolboxImpl(Toolbox toolbox, Class<?> preferencesTopic)
    {
        myPluginExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("OGCServerPlugin"));
        myLayerListManager = new ServerListManagerImpl(toolbox);
        myRefreshController = new ServerRefreshControllerImpl(preferencesTopic.getName(), toolbox);
        myValidatorRegistry = new ServerValidatorRegistryImpl();
        myServerSourceControllerManager = new ServerSourceControllerManagerImpl(toolbox, preferencesTopic, myPluginExecutor);
        myStateController = new ServerStateControllerImpl(toolbox, myServerSourceControllerManager);
        myPermalinkController = new PermalinkControllerImpl(myLayerListManager, myServerSourceControllerManager);
        myServerLabelGenerator = new DefaultServerLabelGenerator();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.PluginToolbox#getDescription()
     */
    @Override
    public String getDescription()
    {
        return "Toolbox used to access common OGC Server managers and utilities";
    }

    /**
     * {@inheritDoc}
     *
     * @see ServerToolbox#setPermalinkController(PermalinkController)
     */
    @Override
    public void setPermalinkController(PermalinkController permalinkController)
    {
        myPermalinkController = permalinkController;
    }

    /**
     * {@inheritDoc}
     *
     * @see ServerToolbox#getPermalinkController()
     */
    @Override
    public PermalinkController getPermalinkController()
    {
        return myPermalinkController;
    }

    /**
     * {@inheritDoc}
     *
     * @see ServerToolbox#setPluginExecutor(ExecutorService)
     */
    @Override
    public void setPluginExecutor(ExecutorService pluginExecutor)
    {
        myPluginExecutor = pluginExecutor;
    }

    /**
     * {@inheritDoc}
     *
     * @see ServerToolbox#getPluginExecutor()
     */
    @Override
    public ExecutorService getPluginExecutor()
    {
        return myPluginExecutor;
    }

    /**
     * {@inheritDoc}
     *
     * @see ServerToolbox#setServerLayerListManager(ServerListManager)
     */
    @Override
    public void setServerLayerListManager(ServerListManager layerListManager)
    {
        myLayerListManager = layerListManager;
    }

    /**
     * {@inheritDoc}
     *
     * @see ServerToolbox#getServerLayerListManager()
     */
    @Override
    public ServerListManager getServerLayerListManager()
    {
        return myLayerListManager;
    }

    /**
     * {@inheritDoc}
     *
     * @see ServerToolbox#setServerRefreshController(ServerRefreshController)
     */
    @Override
    public void setServerRefreshController(ServerRefreshController refreshController)
    {
        myRefreshController = refreshController;
    }

    /**
     * {@inheritDoc}
     *
     * @see ServerToolbox#getServerRefreshController()
     */
    @Override
    public ServerRefreshController getServerRefreshController()
    {
        return myRefreshController;
    }

    /**
     * {@inheritDoc}
     *
     * @see ServerToolbox#setServerSourceControllerManager(ServerSourceControllerManager)
     */
    @Override
    public void setServerSourceControllerManager(ServerSourceControllerManager serverSourceControllerManager)
    {
        myServerSourceControllerManager = serverSourceControllerManager;
    }

    /**
     * {@inheritDoc}
     *
     * @see ServerToolbox#getServerSourceControllerManager()
     */
    @Override
    public ServerSourceControllerManager getServerSourceControllerManager()
    {
        return myServerSourceControllerManager;
    }

    /**
     * {@inheritDoc}
     *
     * @see ServerToolbox#setServerStateController(ServerStateController)
     */
    @Override
    public void setServerStateController(ServerStateController stateController)
    {
        myStateController = stateController;
    }

    /**
     * {@inheritDoc}
     *
     * @see ServerToolbox#getServerStateController()
     */
    @Override
    public ServerStateController getServerStateController()
    {
        return myStateController;
    }

    /**
     * {@inheritDoc}
     *
     * @see ServerToolbox#setServerValidatorRegistry(ServerValidatorRegistry)
     */
    @Override
    public void setServerValidatorRegistry(ServerValidatorRegistry validatorRegistry)
    {
        myValidatorRegistry = validatorRegistry;
    }

    /**
     * {@inheritDoc}
     *
     * @see ServerToolbox#getServerValidatorRegistry()
     */
    @Override
    public ServerValidatorRegistry getServerValidatorRegistry()
    {
        return myValidatorRegistry;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.server.toolbox.ServerToolbox#getServerLabelGenerator()
     */
    @Override
    public ServerLabelGenerator getServerLabelGenerator()
    {
        return myServerLabelGenerator;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.server.toolbox.ServerToolbox#setServerLabelGenerator(io.opensphere.server.toolbox.ServerLabelGenerator)
     */
    @Override
    public void setServerLabelGenerator(ServerLabelGenerator serverLabelGenerator)
    {
        myServerLabelGenerator = serverLabelGenerator;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.server.toolbox.ServerToolbox#getLayerConfigurationManager()
     */
    @Override
    public WFSLayerConfigurationManager getLayerConfigurationManager()
    {
        return myConfigurationManager;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.server.toolbox.ServerToolbox#setStateConfigurationManager(io.opensphere.server.toolbox.WFSLayerConfigurationManager)
     */
    @Override
    public void setStateConfigurationManager(WFSLayerConfigurationManager configurationManager)
    {
        myConfigurationManager = configurationManager;
    }
}
