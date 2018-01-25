package io.opensphere.shapefile;

import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.PluginProperty;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.adapter.PluginAdapter;

/**
 * Main control class for the shapefile plugin.
 */
public class ShapeFilePlugin extends PluginAdapter
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ShapeFilePlugin.class);

    /** The envoy. */
    private ShapeFileEnvoy myEnvoy;

    /** The state controller. */
    private ShapeFileStateController myStateController;

    @Override
    public void close()
    {
    }

    @Override
    public Collection<? extends Envoy> getEnvoys()
    {
        return Collections.singletonList(myEnvoy);
    }

    @Override
    public void initialize(PluginLoaderData data, Toolbox toolbox)
    {
        for (PluginProperty pluginProperty : data.getPluginProperty())
        {
            LOGGER.warn("Unexpected plugin property for plugin [" + data.getId() + "]: " + pluginProperty.getKey());
        }

        myEnvoy = new ShapeFileEnvoy(toolbox);
        myStateController = new ShapeFileStateController(myEnvoy);
        toolbox.getModuleStateManager().registerModuleStateController(ShapeFileStateConstants.MODULE_NAME, myStateController);
    }
}
