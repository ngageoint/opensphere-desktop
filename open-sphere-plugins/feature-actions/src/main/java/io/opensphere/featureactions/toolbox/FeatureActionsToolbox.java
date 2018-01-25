package io.opensphere.featureactions.toolbox;

import io.opensphere.core.PluginToolbox;
import io.opensphere.featureactions.registry.FeatureActionsRegistry;

/** The Feature Actions toolbox. */
public final class FeatureActionsToolbox implements PluginToolbox
{
    /** The registry for feature actions. */
    private final FeatureActionsRegistry myRegistry;

    /**
     * Constructor.
     *
     * @param registry The registry for feature actions
     */
    public FeatureActionsToolbox(FeatureActionsRegistry registry)
    {
        myRegistry = registry;
    }

    @Override
    public String getDescription()
    {
        return "The Feature Actions toolbox";
    }

    /**
     * Gets the registry.
     *
     * @return the registry
     */
    public FeatureActionsRegistry getRegistry()
    {
        return myRegistry;
    }
}
