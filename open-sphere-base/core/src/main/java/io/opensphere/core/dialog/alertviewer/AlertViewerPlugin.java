package io.opensphere.core.dialog.alertviewer;

import java.util.Collection;
import java.util.Collections;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractServicePlugin;
import io.opensphere.core.util.Service;

/** The alert viewer plugin. */
public class AlertViewerPlugin extends AbstractServicePlugin
{
    @Override
    protected Collection<Service> getServices(PluginLoaderData plugindata, Toolbox toolbox)
    {
        return Collections.singleton(new AlertViewerController(toolbox));
    }
}
