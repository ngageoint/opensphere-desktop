package io.opensphere.myplaces;

import java.util.Collection;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.myplaces.controllers.MyPlacesController;

/**
 * The my places plugin is responsible for managing the my places layer within
 * the layers panel.
 */
public class MyPlacesPlugin extends PluginAdapter
{
    /**
     * The data accessor used to load and save changes to the My Places layers.
     */
    private volatile MyPlacesController myController;

    /**
     * The toolbox through which application interaction occurs.
     */
    private Toolbox myToolbox;

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        myToolbox = toolbox;
        myController = new MyPlacesController(myToolbox);
        myController.open();
    }

    @Override
    public void close()
    {
        myController.close();
    }

    @Override
    public Collection<? extends Transformer> getTransformers()
    {
        return myController.getTransformers();
    }
}
