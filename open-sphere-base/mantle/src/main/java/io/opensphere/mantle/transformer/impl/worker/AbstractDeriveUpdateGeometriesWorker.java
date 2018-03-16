package io.opensphere.mantle.transformer.impl.worker;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.AbstractGeometryGroup;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.factory.impl.DefaultRenderPropertyPool;
import io.opensphere.mantle.transformer.util.GeometrySetUtil;

/**
 * The Class DeriveUpdateGeometriesWorker.
 */
public abstract class AbstractDeriveUpdateGeometriesWorker extends AbstractDataElementTransformerWorker
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AbstractDeriveUpdateGeometriesWorker.class);

    /** The id set. */
    private final List<Long> myIdsOfInterest;

    /**
     * Instantiates a new derive geometries worker.
     *
     * @param provider the provider
     * @param idSet the id set
     */
    public AbstractDeriveUpdateGeometriesWorker(DataElementTransformerWorkerDataProvider provider, List<Long> idSet)
    {
        super(provider);
        myIdsOfInterest = idSet;
    }

    /**
     * Cleanup.
     */
    public abstract void cleanup();

    /**
     * Gets the altered render property.
     *
     * @param id the id
     * @param orig the orig
     * @return the altered render property
     */
    public abstract BaseRenderProperties getAlteredRenderProperty(Long id, BaseRenderProperties orig);

    /**
     * Gets the ids of interest.
     *
     * @return the ids of interest
     */
    public List<Long> getIdsOfInterest()
    {
        return myIdsOfInterest;
    }

    /**
     * Process geometry set.
     *
     * @param rpp the rpp
     * @param geomSet the geom set
     * @return the sets the
     */
    public Set<Geometry> processGeometrySet(RenderPropertyPool rpp, Collection<Geometry> geomSet)
    {
        Set<Geometry> newGeomSet = New.set(geomSet.size());
        for (Geometry geom : geomSet)
        {
            if (geom instanceof AbstractRenderableGeometry)
            {
                AbstractRenderableGeometry aGeom = (AbstractRenderableGeometry)geom;
                BaseRenderProperties prop = aGeom.getRenderProperties().clone();
                prop = getAlteredRenderProperty(Long.valueOf(geom.getDataModelId()), prop);
                prop = rpp.getPoolInstance(prop);

                AbstractRenderableGeometry derived = aGeom.derive(prop, aGeom.getConstraints());
                newGeomSet.add(derived);
            }
            else if (geom instanceof AbstractGeometryGroup)
            {
                newGeomSet.addAll(processGeometrySet(rpp, ((AbstractGeometryGroup)geom).getGeometryRegistry().getGeometries()));
            }
        }
        return newGeomSet;
    }

    /**
     * Retrieve necessary data.
     */
    public abstract void retrieveNecessaryData();

    @Override
    @SuppressWarnings("PMD.GuardLogStatement")
    public void run()
    {
        if (myIdsOfInterest != null && !myIdsOfInterest.isEmpty())
        {
            getProvider().getUpdateTaskActivity().registerUpdateInProgress(getProvider().getUpdateSource());
            long start = System.nanoTime();
            retrieveNecessaryData();
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(StringUtilities.formatTimingMessage("Retrieve Necessary Data: ", System.nanoTime() - start));
            }

            getProvider().getGeometrySetLock().lock();
            try
            {
                start = System.nanoTime();
                Set<Geometry> oldVisibleGeomSet = GeometrySetUtil.findGeometrySetWithIds(getProvider().getGeometrySet(),
                        getProvider().getGeometrySetLock(), myIdsOfInterest, null,
                        getProvider().getDataModelIdFromGeometryIdBitMask());

                Set<Geometry> oldHiddenGeomSet = GeometrySetUtil.findGeometrySetWithIds(getProvider().getHiddenGeometrySet(),
                        getProvider().getGeometrySetLock(), myIdsOfInterest, null,
                        getProvider().getDataModelIdFromGeometryIdBitMask());

                RenderPropertyPool rpp = DefaultRenderPropertyPool.createPool(getProvider().getDataType(), oldVisibleGeomSet,
                        oldHiddenGeomSet);

                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace(StringUtilities.formatTimingMessage("Pre-process: ", System.nanoTime() - start));
                }

                start = System.nanoTime();
                Set<Geometry> newVisGeomSet = processGeometrySet(rpp, oldVisibleGeomSet);
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace(StringUtilities.formatTimingMessage("Process Visible Set: ", System.nanoTime() - start));
                }

                start = System.nanoTime();
                Set<Geometry> newHiddenGeomSet = processGeometrySet(rpp, oldHiddenGeomSet);
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace(StringUtilities.formatTimingMessage("Process Hidden Set: ", System.nanoTime() - start));
                }

                getProvider().getGeometrySet().removeAll(oldVisibleGeomSet);
                getProvider().getGeometrySet().addAll(newVisGeomSet);
                getProvider().getHiddenGeometrySet().removeAll(oldHiddenGeomSet);
                getProvider().getHiddenGeometrySet().addAll(newHiddenGeomSet);

                // Send changes to the
                if (!newVisGeomSet.isEmpty() && getProvider().isPublishUpdatesToGeometryRegistry())
                {
                    start = System.nanoTime();
                    LOGGER.trace("Starting Publish to Geometry Registry");
                    getProvider().getToolbox().getGeometryRegistry().receiveObjects(getProvider().getUpdateSource(),
                            newVisGeomSet, oldVisibleGeomSet);
                    if (LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace(
                                StringUtilities.formatTimingMessage("Publish to Geometry Registry: ", System.nanoTime() - start));
                    }
                }

                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("RenderPropertyPoolSize: " + rpp.size());
                }
                rpp.clearPool();
            }
            finally
            {
                getProvider().getGeometrySetLock().unlock();
                getProvider().getUpdateTaskActivity().unregisterUpdateInProgress(getProvider().getUpdateSource());
            }

            start = System.nanoTime();
            cleanup();
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(StringUtilities.formatTimingMessage("Clean up: ", System.nanoTime() - start));
            }
        }
    }
}
