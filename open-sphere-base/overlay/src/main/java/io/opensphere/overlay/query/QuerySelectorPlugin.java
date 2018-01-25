package io.opensphere.overlay.query;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.control.ui.ToolbarManager.ToolbarLocation;

/** The Query Selector plugin. */
public class QuerySelectorPlugin extends PluginAdapter
{
    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        toolbox.getUIRegistry().getToolbarComponentRegistry().registerToolbarComponent(ToolbarLocation.NORTH, "QuerySelector",
                new QuerySelector(toolbox), 401, SeparatorLocation.NONE);
    }
}
