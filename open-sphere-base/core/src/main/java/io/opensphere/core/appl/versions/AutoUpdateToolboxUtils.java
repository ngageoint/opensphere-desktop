package io.opensphere.core.appl.versions;

import io.opensphere.core.Toolbox;

/**
 * A set of utilities to ease the interaction with the
 * {@link AutoUpdateToolbox}.
 */
public final class AutoUpdateToolboxUtils
{
    /** Private constructor to prevent instantiation. */
    private AutoUpdateToolboxUtils()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }

    /**
     * Gets the {@link AutoUpdateToolbox} instance assigned in the plugin
     * toolbox registry.
     *
     * @param toolbox the {@link Toolbox} through which application state is
     *            accessed.
     * @return the {@link AutoUpdateToolbox} instance extracted from the plugin
     *         toolbox registry.
     */
    public static AutoUpdateToolbox getAutoUpdateToolboxToolbox(Toolbox toolbox)
    {
        return toolbox.getPluginToolboxRegistry().getPluginToolbox(AutoUpdateToolbox.class);
    }
}
