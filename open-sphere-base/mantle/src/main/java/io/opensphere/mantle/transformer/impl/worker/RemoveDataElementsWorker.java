package io.opensphere.mantle.transformer.impl.worker;

import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.mantle.transformer.util.GeometrySetUtil;

/**
 * The Class RemoveDataElementsWorker.
 */
public class RemoveDataElementsWorker extends AbstractDataElementTransformerWorker
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(RemoveDataElementsWorker.class);

    /** The ids. */
    private final List<Long> myCandidateIdsToRemove;

    /**
     * Instantiates a new RemoveDataElementsWorker.
     *
     * @param provider the provider
     * @param idsToRemove the ids to remove
     */
    public RemoveDataElementsWorker(DataElementTransformerWorkerDataProvider provider, List<Long> idsToRemove)
    {
        super(provider);
        myCandidateIdsToRemove = idsToRemove;
    }

    @Override
    public void run()
    {
        ReentrantLock aLock = getProvider().getGeometrySetLock();
        aLock.lock();
        try
        {
            List<Long> idsToRemove = CollectionUtilities.intersectionAsList(getProvider().getIdSet(), myCandidateIdsToRemove);
            if (idsToRemove == null || idsToRemove.isEmpty())
            {
                return;
            }

            Set<Geometry> geomsToRemove = GeometrySetUtil.findGeometrySetWithIds(getProvider().getGeometrySet(),
                    getProvider().getGeometrySetLock(), idsToRemove, getProvider().getDataModelIdFromGeometryIdBitMask());
            Set<Geometry> hiddenGeomsToRemove = GeometrySetUtil.findGeometrySetWithIds(getProvider().getHiddenGeometrySet(),
                    getProvider().getGeometrySetLock(), idsToRemove, getProvider().getDataModelIdFromGeometryIdBitMask());
            getProvider().getIdSet().removeAll(idsToRemove);

            if (!geomsToRemove.isEmpty())
            {
                getProvider().getGeometrySet().removeAll(geomsToRemove);
                getProvider().getUpdateTaskActivity().registerUpdateInProgress(getProvider().getUpdateSource());
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Removing " + geomsToRemove.size() + " geometries from geometry registry.");
                }
                getProvider().getToolbox().getGeometryRegistry().receiveObjects(getProvider().getUpdateSource(),
                        EMPTY_GEOM_SET, geomsToRemove);
                getProvider().getUpdateTaskActivity().unregisterUpdateInProgress(getProvider().getUpdateSource());
            }

            if (!hiddenGeomsToRemove.isEmpty())
            {
                getProvider().getHiddenGeometrySet().removeAll(hiddenGeomsToRemove);
            }
        }
        finally
        {
            aLock.unlock();
        }
    }
}
