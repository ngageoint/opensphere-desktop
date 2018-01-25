package io.opensphere.mantle.transformer.impl.worker;

import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import io.opensphere.core.util.lang.StringUtilities;

/**
 * The Class ShutdownTransformerWorker.
 */
public class ShutdownTransformerWorker extends AbstractDataElementTransformerWorker
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ShutdownTransformerWorker.class);

    /**
     * Instantiates a new clear geometries worker.
     *
     * @param provider the provider
     */
    public ShutdownTransformerWorker(DataElementTransformerWorkerDataProvider provider)
    {
        super(provider);
    }

    @Override
    public void run()
    {
        ReentrantLock aLock = getProvider().getGeometrySetLock();
        aLock.lock();
        try
        {
            // Make stuff invisible first so that we make it go away from
            // the map right away
            // and don't have to wait for all the rest of this to execute
            // fully.
            AllVisibilityRenderPropertyUpdator makeInvisible = new AllVisibilityRenderPropertyUpdator(getProvider(), false);
            makeInvisible.run();

            // Always remove the geometry set from the registry if we don't
            // think it's published just to make sure
            // it is cleared out on shutdown.
            long start = System.nanoTime();
            getProvider().getUpdateTaskActivity().registerUpdateInProgress(getProvider().getUpdateSource());
            getProvider().getToolbox().getGeometryRegistry().receiveObjects(getProvider().getUpdateSource(), EMPTY_GEOM_SET,
                    getProvider().getGeometrySet());
            getProvider().getUpdateTaskActivity().unregisterUpdateInProgress(getProvider().getUpdateSource());
            long end = System.nanoTime();
            LOGGER.info(StringUtilities.formatTimingMessage(
                    "Shutdown Removed " + getProvider().getGeometrySet().size() + " Geometries from registry in ", end - start));

            getProvider().getIdSet().clear();
            getProvider().getGeometrySet().clear();
            getProvider().getHiddenGeometrySet().clear();
        }
        finally
        {
            aLock.unlock();
        }
    }
}
