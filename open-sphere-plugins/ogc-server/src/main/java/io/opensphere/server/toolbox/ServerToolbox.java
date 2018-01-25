package io.opensphere.server.toolbox;

import java.util.concurrent.ExecutorService;

import io.opensphere.core.PluginToolbox;

/**
 * The Interface ServerToolbox used to access common OGC Server managers and
 * utilities.
 */
public interface ServerToolbox extends PluginToolbox
{
    /**
     * Gets the permalink controller.
     *
     * @return The permalink controller.
     */
    PermalinkController getPermalinkController();

    /**
     * Getter for the plugin executor.
     *
     * @return the plugin executor
     */
    ExecutorService getPluginExecutor();

    /**
     * Gets a manager from the server plugin that provides information about
     * available server layers.
     *
     * @return the {@link ServerListManager}
     */
    ServerListManager getServerLayerListManager();

    /**
     * Gets the controller that commands server refreshes.
     *
     * @return the server refresh controller
     */
    ServerRefreshController getServerRefreshController();

    /**
     * Gets a manager from the server plugin that provides information about
     * available server source controllers.
     *
     * @return the {@link ServerSourceControllerManager}
     */
    ServerSourceControllerManager getServerSourceControllerManager();

    /**
     * Gets a state controller that will restore server connections from a state
     * node.
     *
     * @return The server state controller.
     */
    ServerStateController getServerStateController();

    /**
     * Gets a manager from the server plugin that allows OGC service plugins to
     * be validated by the server plugin. Service plugins will register with
     * this manager and the server plugin will look them up by Service and call
     * their appropriate Validation method.
     *
     * @return the {@link ServerValidatorRegistry}
     */
    ServerValidatorRegistry getServerValidatorRegistry();

    /**
     * Gets the label generator to create human readable labels for various
     * server types.
     *
     * @return the label generator to create human readable labels for various
     *         server types.
     */
    ServerLabelGenerator getServerLabelGenerator();

    /**
     * Gets the state configuration manager instance used to manage layer
     * configurations within the OpenSphere Saved State XML model.
     *
     * @return the state configuration manager instance used to manage layer
     *         configurations within the OpenSphere Saved State XML model.
     */
    WFSLayerConfigurationManager getLayerConfigurationManager();

    /**
     * Sets the value of the ValidatorRegistry field.
     *
     * @param validatorRegistry the value to store in the ValidatorRegistry
     *            field.
     */
    void setServerValidatorRegistry(ServerValidatorRegistry validatorRegistry);

    /**
     * Sets the value of the ServerStateController field.
     *
     * @param stateController the value to store in the ServerStateController
     *            field.
     */
    void setServerStateController(ServerStateController stateController);

    /**
     * Sets the value of the ServerSourceControllerManager field.
     *
     * @param serverSourceControllerManager the value to store in the
     *            ServerSourceControllerManager field.
     */
    void setServerSourceControllerManager(ServerSourceControllerManager serverSourceControllerManager);

    /**
     * Sets the value of the ServerRefreshController field.
     *
     * @param refreshController the value to store in the
     *            ServerRefreshController field.
     */
    void setServerRefreshController(ServerRefreshController refreshController);

    /**
     * Sets the value of the ServerLayerListManager field.
     *
     * @param layerListManager the value to store in the ServerLayerListManager
     *            field.
     */
    void setServerLayerListManager(ServerListManager layerListManager);

    /**
     * Sets the value of the PluginExecutor field.
     *
     * @param pluginExecutor the value to store in the PluginExecutor field.
     */
    void setPluginExecutor(ExecutorService pluginExecutor);

    /**
     * Sets the value of the PermalinkController field.
     *
     * @param permalinkController the value to store in the PermalinkController
     *            field.
     */
    void setPermalinkController(PermalinkController permalinkController);

    /**
     * Sets the label generator to create human readable labels for various
     * server types.
     *
     * @param serverLabelGenerator the label generator to create human readable
     *            labels for various server types.
     */
    void setServerLabelGenerator(ServerLabelGenerator serverLabelGenerator);

    /**
     * Sets the state configuration manager instance used to manage layer
     * configurations within the OpenSphere Saved State XML model.
     *
     * @param configurationManager the state configuration manager instance used
     *            to manage layer configurations within the OpenSphere Saved
     *            State XML model.
     */
    void setStateConfigurationManager(WFSLayerConfigurationManager configurationManager);
}
