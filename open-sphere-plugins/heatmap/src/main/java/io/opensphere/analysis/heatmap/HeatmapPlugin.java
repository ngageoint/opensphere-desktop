package io.opensphere.analysis.heatmap;

import java.util.Collection;
import java.util.Collections;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.api.adapter.AbstractServicePlugin;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry;
import io.opensphere.mantle.data.tile.InterpolatedTileVisualizationSupport;
import io.opensphere.mantle.util.MantleToolboxUtils;

/** Heat map plugin. */
public class HeatmapPlugin extends AbstractServicePlugin
{
    /** The toolbox. */
    private Toolbox myToolbox;

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        myToolbox = toolbox;

        VisualizationStyleRegistry registry = MantleToolboxUtils.getMantleToolbox(toolbox).getVisualizationStyleRegistry();
        registry.installStyle(HeatmapVisualizationStyle.class, this);
        registry.setDefaultStyle(InterpolatedTileVisualizationSupport.class, HeatmapVisualizationStyle.class, this);

        super.initialize(plugindata, toolbox);
    }

    @Override
    public Collection<? extends Transformer> getTransformers()
    {
        return Collections.singleton(new HeatmapTransformer(myToolbox));
    }

    @Override
    protected Collection<Service> getServices(PluginLoaderData plugindata, Toolbox toolbox)
    {
        HeatmapMantleController mantleController = new HeatmapMantleController(toolbox);
        HeatmapController heatmapController = HeatmapController.initInstance(toolbox, mantleController);
        return New.list(mantleController, HeatmapLayerMenuProvider.createService(toolbox, heatmapController),
                HeatmapGeometryMenuProvider.createService(toolbox, heatmapController));
    }
}
