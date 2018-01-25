package io.opensphere.core;

import java.util.Set;

import io.opensphere.core.util.Service;

/**
 * A registry for specialized toolboxes provided by plug-ins for sharing with
 * other plug-ins.
 */
public interface PluginToolboxRegistry
{
    /**
     * Gets the set of available PluginToolbox classes in the registry.
     *
     * @return The available plugin toolbox types.
     */
    Set<Class<? extends PluginToolbox>> getAvailablePluginToolboxTypes();

    /**
     * Gets the plugin toolbox for a registered concrete PluginToolbox.
     *
     * @param <T> The toolbox concrete class.
     * @param toolboxClass The toolbox concrete class.
     * @return The plugin toolbox or <code>null</code> if one has not been
     *         registered for the provided class.
     */
    <T extends PluginToolbox> T getPluginToolbox(Class<T> toolboxClass);

    /**
     * Register a concrete instance of a {@link PluginToolbox} to be associated
     * with this Toolbox.
     *
     * @param toolbox The plugin toolbox to register.
     */
    void registerPluginToolbox(PluginToolbox toolbox);

    /**
     * Removes the plugin toolbox from the registry.
     *
     * @param toolbox The toolbox to remove.
     * @return {@code true} iff removed.
     */
    boolean removePluginToolbox(PluginToolbox toolbox);

    /**
     * Creates a service that can be used to register/remove the given toolbox.
     *
     * @param pluginToolbox the plugin toolbox
     * @return the service
     */
    default Service getRegistrationService(final PluginToolbox pluginToolbox)
    {
        return new Service()
        {
            @Override
            public void open()
            {
                registerPluginToolbox(pluginToolbox);
            }

            @Override
            public void close()
            {
                removePluginToolbox(pluginToolbox);
            }
        };
    }
}
