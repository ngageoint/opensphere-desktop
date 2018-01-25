package io.opensphere.analysis;

import io.opensphere.analysis.toolbox.AnalysisToolbox;
import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractServicePlugin;
import io.opensphere.core.util.Service;

/**
 * Main class for the analysis plug-in.
 *
 * Some additional work that could be done:
 * <ul>
 * <li>Size the baseball dialog to better fit the table size</li>
 * </ul>
 */
public class AnalysisPlugin extends AbstractServicePlugin
{
    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        super.initialize(plugindata, toolbox);
        addService(toolbox.getPluginToolboxRegistry().getRegistrationService(new AnalysisToolbox()));
        startServices();
        for (Service toolService : new ToolInitializer(toolbox).createToolServices())
        {
            addService(toolService);
        }
        startServices();
    }
}
