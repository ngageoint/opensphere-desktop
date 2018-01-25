package io.opensphere.mantle.transformer.impl.worker;

import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.renderproperties.RenderProperties;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.factory.impl.DefaultRenderPropertyPool;

/**
 * The Class AllRenderPropertyUpdator.
 */
public abstract class AbstractRenderPropertyUpdator implements Runnable
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AbstractRenderPropertyUpdator.class);

    /** The Provider. */
    private final DataElementTransformerWorkerDataProvider myProvider;

    /**
     * Instantiates a new rebuild all geometries worker.
     *
     * @param provider the provider
     */
    public AbstractRenderPropertyUpdator(DataElementTransformerWorkerDataProvider provider)
    {
        myProvider = provider;
    }

    /**
     * Adjust.
     *
     * @param rp the rp
     */
    public abstract void adjust(RenderProperties rp);

    /**
     * Gets the provider.
     *
     * @return the provider
     */
    public DataElementTransformerWorkerDataProvider getProvider()
    {
        return myProvider;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run()
    {
        RenderPropertyPool rpp = DefaultRenderPropertyPool.createPool(myProvider.getDataType(), myProvider.getGeometrySet(),
                myProvider.getHiddenGeometrySet());
        Set<RenderProperties> rpSet = rpp.values();
        for (RenderProperties rp : rpSet)
        {
            adjust(rp);
        }
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("AllRenderPropertyUpdator: RenderPropertyPoolSize: " + rpp.size());
        }
        rpp.clearPool();
    }
}
