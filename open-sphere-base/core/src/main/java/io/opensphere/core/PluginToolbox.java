package io.opensphere.core;

/**
 * The interface for a toolbox provided by a plugin. Intentionally empty as
 * users of PluginToolbox will need to cast this interface into the concrete
 * class.
 */
@FunctionalInterface
public interface PluginToolbox
{
    /**
     * Gets the description for a plugin toolbox.
     *
     * @return the description
     */
    String getDescription();
}
