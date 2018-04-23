package io.opensphere.mantle.transformer.impl.worker;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.renderproperties.RenderProperties;

/**
 * The Class AllRenderPropertyUpdator.
 */
public abstract class AbstractRenderPropertyUpdator implements Runnable
{
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

    @Override
    public void run()
    {
        // Get a set of unique render property objects
        Map<RenderProperties, Object> renderPropertyMap = new IdentityHashMap<>();
        for (Geometry geometry : myProvider.getGeometrySet())
        {
            renderPropertyMap.put(geometry.getRenderProperties(), null);
        }
        for (Geometry geometry : myProvider.getHiddenGeometrySet())
        {
            renderPropertyMap.put(geometry.getRenderProperties(), null);
        }
        Set<RenderProperties> renderProperties = renderPropertyMap.keySet();

        for (RenderProperties rp : renderProperties)
        {
            adjust(rp);
        }
    }
}
