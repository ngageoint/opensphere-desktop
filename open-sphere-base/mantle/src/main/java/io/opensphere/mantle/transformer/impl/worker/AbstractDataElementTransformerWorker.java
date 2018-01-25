package io.opensphere.mantle.transformer.impl.worker;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.factory.impl.DefaultRenderPropertyPool;

/**
 * The Class AbstractDataElementTransformerWorker.
 */
public abstract class AbstractDataElementTransformerWorker implements Runnable
{
    /** The Constant EMPTY_GEOM_SET. */
    public static final Set<Geometry> EMPTY_GEOM_SET = Collections.<Geometry>emptySet();

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AbstractDataElementTransformerWorker.class);

    /** The Transformer provider. */
    private final DataElementTransformerWorkerDataProvider myTransformerProvider;

    /**
     * Instantiates a new abstract data element transformer worker.
     *
     * @param provider the provider
     */
    public AbstractDataElementTransformerWorker(DataElementTransformerWorkerDataProvider provider)
    {
        Utilities.checkNull(provider, "provider");
        myTransformerProvider = provider;
    }

    /**
     * Gets the provider.
     *
     * @return the provider
     */
    public DataElementTransformerWorkerDataProvider getProvider()
    {
        return myTransformerProvider;
    }

    /**
     * Publish geometries.
     *
     * @param geometrySet the geometry set
     */
    protected void publishGeometries(Set<Geometry> geometrySet)
    {
        if (getProvider().isPublishUpdatesToGeometryRegistry())
        {
            long start = System.nanoTime();

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Sent " + geometrySet.size() + " new geometries to the Geometry Registry at "
                        + System.currentTimeMillis());
            }

            getProvider().getToolbox().getGeometryRegistry().receiveObjects(getProvider().getUpdateSource(), geometrySet,
                    EMPTY_GEOM_SET);

            if (LOGGER.isTraceEnabled())
            {
                long end = System.nanoTime();
                LOGGER.trace(StringUtilities.formatTimingMessage("Added " + geometrySet.size() + " Geometries to registry in ",
                        end - start));
            }
        }
    }

    /**
     * Unpublish geometries.
     *
     * @param geometrySet the geometry set
     */
    protected void unpublishGeometries(Set<Geometry> geometrySet)
    {
        getProvider().getToolbox().getGeometryRegistry().receiveObjects(getProvider().getUpdateSource(), EMPTY_GEOM_SET,
                geometrySet);
    }

    /**
     * Creates a render property pool with the current geometries.
     *
     * @return the pool
     */
    protected RenderPropertyPool createPool()
    {
        RenderPropertyPool rpp;
        ReentrantLock lock = getProvider().getGeometrySetLock();
        lock.lock();
        try
        {
            rpp = DefaultRenderPropertyPool.createPool(getProvider().getDataType(),
                    getProvider().getGeometrySet(), getProvider().getHiddenGeometrySet());
        }
        finally
        {
            lock.unlock();
        }
        return rpp;
    }
}
