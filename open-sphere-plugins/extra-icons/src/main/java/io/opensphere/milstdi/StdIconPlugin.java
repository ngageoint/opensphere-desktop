package io.opensphere.milstdi;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.milstdi.mantle.controller.StandardIcons;

/**
 * This plugin installs the MilStd2525c icon set for use by the application.
 */
public class StdIconPlugin extends PluginAdapter
{
    @Override
    public void initialize(PluginLoaderData plugindata, final Toolbox toolbox)
    {
        StandardIcons.getIconMap(toolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class));
    }
}
