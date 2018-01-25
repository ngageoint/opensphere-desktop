package io.opensphere.imagery;

import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.api.adapter.AbstractEnvoy;
import io.opensphere.core.api.adapter.PluginAdapter;

/**
 * The Class ImageryPlugin.
 */
public class ImageryPlugin extends PluginAdapter
{
    /** The envoy. */
    @SuppressWarnings("PMD.SingularField")
    private ImageryPluginEnvoy myEnvoy;

    /** The Imagery transformer. */
    private ImageryTransformer myImageryTransformer;

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
    public Collection<? extends Transformer> getTransformers()
    {
        return Collections.singleton(myImageryTransformer);
    }

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        createTransformer(toolbox);
        myEnvoy = new ImageryPluginEnvoy(toolbox);
    }

    /**
     * Create the WMS transformer.
     *
     * @param toolbox the toolbox
     */
    protected void createTransformer(Toolbox toolbox)
    {
        myImageryTransformer = new ImageryTransformer(toolbox);
    }

    /**
     * The Class ImageryPluginEnvoy.
     *
     * This envoy just delivers and initializes the controller.
     */
    private static class ImageryPluginEnvoy extends AbstractEnvoy
    {
        /** Logger. */
        private static final Logger LOGGER = Logger.getLogger(ImageryPluginEnvoy.class);

        /** The shape file controller. */
        private final ImageryFileSourceController myController;

        /**
         * Instantiates a new cSV envoy.
         *
         * @param toolBox the tool box
         */
        public ImageryPluginEnvoy(Toolbox toolBox)
        {
            super(toolBox);
            myController = new ImageryFileSourceController(toolBox);
        }

        @Override
        public void close()
        {
            super.close();
            myController.close();
        }

        @Override
        public void open()
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("ImageryPluginEnvoy");
            }
            myController.setExecutorService(getExecutor());
            myController.initialize();
        }

        @Override
        public void setFilter(Object filter)
        {
        }
    }
}
