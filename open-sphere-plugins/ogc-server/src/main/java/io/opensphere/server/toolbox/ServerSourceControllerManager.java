package io.opensphere.server.toolbox;

import java.util.Collection;

/**
 * Manages the ServerSourceControllers.
 */
public interface ServerSourceControllerManager
{
    /**
     * Adds a load listener.
     *
     * @param listener the load listener
     */
    void addLoadListener(LoadListener listener);

    /**
     * Gets a collection of all {@link ServerSourceController}s.
     *
     * @return The collection of {@link ServerSourceController}s
     */
    Collection<ServerSourceController> getControllers();

    /**
     * Gets a {@link ServerSourceController} for the given type.
     *
     * @param typeName - the type to retrieve
     * @return the {@link ServerSourceController} or null if no controller is
     *         defined for the specified type
     */
    ServerSourceController getServerSourceController(String typeName);

    /**
     * Removes a load listener.
     *
     * @param listener the load listener
     */
    void removeLoadListener(LoadListener listener);

    /**
     * Maps a {@link ServerSourceController} to a specific preferences topic,
     * overriding the default.
     *
     * @param controllerClass The class of the {@link ServerSourceController}
     * @param preferencesTopic the string used to retrieve the top-level server
     *            preferences
     */
    void setPreferencesTopic(Class<? extends ServerSourceController> controllerClass, Class<?> preferencesTopic);

    /**
     * Load listener interface.
     */
    @FunctionalInterface
    public interface LoadListener
    {
        /**
         * Called when the load is complete.
         */
        void loadComplete();
    }
}
