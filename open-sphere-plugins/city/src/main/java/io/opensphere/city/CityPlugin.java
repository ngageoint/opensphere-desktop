package io.opensphere.city;

import java.util.Collection;

import io.opensphere.city.envoy.CityEnvoy;
import io.opensphere.city.transformer.CityTransformer;
import io.opensphere.city.transformer.ColladaToGeometries;
import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;

/** The CyberCity 3D plugin. */
public class CityPlugin extends PluginAdapter
{
    /** The toolbox. */
    private Toolbox myToolbox;

    /**
     * The transformer.
     */
    private CityTransformer myTransformer;

    /**
     * An envoy that intercepts xyz image requests and changes them to collada
     * queries.
     */
    private ColladaToGeometries myColladaPublisher;

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        myToolbox = toolbox;
//        ServerToolboxUtils.getServerToolbox(toolbox).getServerSourceControllerManager()
//                .setPreferencesTopic(CityServerSourceController.class, CityPlugin.class);
        super.initialize(plugindata, toolbox);
        myTransformer = new CityTransformer(toolbox.getDataRegistry());
        myColladaPublisher = new ColladaToGeometries(toolbox, myTransformer);
        MantleToolbox mantleTb = toolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        mantleTb.getQueryRegionManager().addQueryRegionListener(myColladaPublisher);
    }

    @Override
    public Collection<? extends Envoy> getEnvoys()
    {
        return New.list(new CityEnvoy(myToolbox), myColladaPublisher);
    }

    @Override
    public Collection<? extends Transformer> getTransformers()
    {
        return New.list(myTransformer);
    }
}
