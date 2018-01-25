package io.opensphere.xyztile;

import java.util.Collection;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.util.collections.New;
import io.opensphere.xyztile.mantle.XYZMantleController;
import io.opensphere.xyztile.transformer.XYZLayerTransformer;

/** The XYZTile plugin. */
public class XYZTilePlugin extends PluginAdapter
{
    /**
     * The mantle controller.
     */
    private XYZMantleController myMantleController;

    /**
     * The xyz transformer.
     */
    private XYZLayerTransformer myTransformer;

    @Override
    public void close()
    {
        myTransformer.close();
        myMantleController.close();
    }

    @Override
    public Collection<? extends Envoy> getEnvoys()
    {
        return null;
    }

    @Override
    public Collection<? extends Transformer> getTransformers()
    {
        return New.list(myTransformer);
    }

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        myMantleController = new XYZMantleController(toolbox);
        myTransformer = new XYZLayerTransformer(toolbox.getDataRegistry(), toolbox.getUIRegistry(), toolbox.getEventManager());
    }
}
