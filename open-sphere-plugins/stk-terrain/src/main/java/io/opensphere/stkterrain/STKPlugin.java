package io.opensphere.stkterrain;

import java.util.Collection;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.server.toolbox.ServerToolboxUtils;
import io.opensphere.stkterrain.debug.EnvoyDebuggins;
import io.opensphere.stkterrain.server.STKServerSourceController;
import io.opensphere.stkterrain.transformer.AttributionTransformer;
import io.opensphere.stkterrain.transformer.STKLayerTransformer;

/**
 * A plugin that is able to ingest terrain data from a Systems Tool Kit Terrain
 * Server and draw that terrain on the globe.
 */
public class STKPlugin extends PluginAdapter
{
    /**
     * The transformer responsible for displaying copyright information for
     * activated terrain layers.
     */
    private AttributionTransformer myAttributionTransformer;

    /**
     * The transformer for STK terrain layers.
     */
    private STKLayerTransformer myTransformer;

    @Override
    public Collection<? extends Transformer> getTransformers()
    {
        return New.list(myTransformer, myAttributionTransformer);
    }

    @SuppressWarnings("unused")
    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        myTransformer = new STKLayerTransformer(toolbox.getDataRegistry(),
                MantleToolboxUtils.getMantleToolbox(toolbox).getDataGroupController(), toolbox.getEventManager());
        myAttributionTransformer = new AttributionTransformer(toolbox.getDataRegistry(), toolbox.getMapManager());
        ServerToolboxUtils.getServerToolbox(toolbox).getServerSourceControllerManager()
                .setPreferencesTopic(STKServerSourceController.class, STKPlugin.class);

        // TODO
        new EnvoyDebuggins(toolbox);
    }
}
