package io.opensphere.mantle.transformer.impl.worker;

import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * The Class PublishUnpublishGeometrySetWorker.
 */
public class PublishUnpublishGeometrySetWorker extends AbstractDataElementTransformerWorker
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PublishUnpublishGeometrySetWorker.class);

    /** The Publish. */
    private final boolean myPublish;

    /** The Set. */
    private final Set<Geometry> mySet;

    /**
     * Instantiates a new publish/unpublish worker.
     *
     * @param provider the provider
     * @param set the set of {@link Geometry} to be published/unpublished.
     * @param publish true to publish ( republish ), false to unpublish
     *            geometries.
     */
    public PublishUnpublishGeometrySetWorker(DataElementTransformerWorkerDataProvider provider, Set<Geometry> set,
            boolean publish)
    {
        super(provider);
        myPublish = publish;
        mySet = set;
    }

    @Override
    public void run()
    {
        int count = mySet.size();
        long start = System.nanoTime();
        getProvider().getUpdateTaskActivity().registerUpdateInProgress(getProvider().getUpdateSource());
        getProvider().getToolbox().getGeometryRegistry().receiveObjects(getProvider().getUpdateSource(),
                myPublish ? mySet : EMPTY_GEOM_SET, myPublish ? EMPTY_GEOM_SET : mySet);
        getProvider().getUpdateTaskActivity().unregisterUpdateInProgress(getProvider().getUpdateSource());
        long end = System.nanoTime();
        LOGGER.info(StringUtilities.formatTimingMessage(
                (myPublish ? "Added " : "Removed ") + count + " Geometries " + (myPublish ? "to" : "from") + " registry in ",
                end - start));
    }
}
