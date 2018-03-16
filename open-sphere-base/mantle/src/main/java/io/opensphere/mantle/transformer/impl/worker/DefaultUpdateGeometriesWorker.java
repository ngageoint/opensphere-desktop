package io.opensphere.mantle.transformer.impl.worker;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.factory.impl.MapGeometrySupportGeometryFactory;
import io.opensphere.mantle.transformer.util.GeometrySetUtil;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class DefaultUpdateGeometriesWorker.
 */
public class DefaultUpdateGeometriesWorker extends AbstractDataElementTransformerWorker
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DefaultUpdateGeometriesWorker.class);

    /** The Factory. */
    private final MapGeometrySupportGeometryFactory myFactory;

    /** The id set. */
    private final List<Long> myIdsOfInterest;

    /**
     * Instantiates a new update geometries.
     *
     * @param provider the provider
     * @param factory the factory
     * @param idSet the id set
     */
    public DefaultUpdateGeometriesWorker(DataElementTransformerWorkerDataProvider provider,
            MapGeometrySupportGeometryFactory factory, List<Long> idSet)
    {
        super(provider);
        myIdsOfInterest = idSet;
        myFactory = factory;
    }

    @Override
    public void run()
    {
        getProvider().getUpdateTaskActivity().registerUpdateInProgress(getProvider().getUpdateSource());
        try
        {
            runInternal();
        }
        finally
        {
            getProvider().getUpdateTaskActivity().unregisterUpdateInProgress(getProvider().getUpdateSource());
        }
    }

    /**
     * Adjust geometry sets.
     *
     * @param oldVisibleGeomSet the old visible geom set
     * @param oldHiddenGeomSet the old hidden geom set
     * @param newVisibleGeomSet the new visible geom set
     * @param newHiddenGeomSet the new hidden geom set
     */
    private void adjustGeometrySets(Set<Geometry> oldVisibleGeomSet, Set<Geometry> oldHiddenGeomSet,
            Set<Geometry> newVisibleGeomSet, Set<Geometry> newHiddenGeomSet)
    {
        long start = System.nanoTime();
        ReentrantLock lock = getProvider().getGeometrySetLock();
        lock.lock();
        try
        {
            getProvider().getGeometrySet().removeAll(oldVisibleGeomSet);
            getProvider().getGeometrySet().addAll(newVisibleGeomSet);

            getProvider().getHiddenGeometrySet().removeAll(oldHiddenGeomSet);
            getProvider().getHiddenGeometrySet().addAll(newHiddenGeomSet);

            adjustHiddenStateAndColorRenderProperties();
        }
        finally
        {
            lock.unlock();
        }
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace(
                    StringUtilities.formatTimingMessage("Post Geom Creation Set Maintance took ", System.nanoTime() - start));
        }
    }

    /**
     * Adjust hidden state and color render properties.
     */
    private void adjustHiddenStateAndColorRenderProperties()
    {
        // Adjust hidden state of render properties to match data type.
        if (!getProvider().getDataType().isVisible())
        {
            AllVisibilityRenderPropertyUpdator visPropUpdate = new AllVisibilityRenderPropertyUpdator(getProvider(),
                    getProvider().getDataType().isVisible());
            visPropUpdate.run();
        }

        // Adjust opacity of all color render properties to match data
        // type.
        if (getProvider().getDataType().getBasicVisualizationInfo() != null
                && getProvider().getDataType().getBasicVisualizationInfo().getDefaultTypeColor().getAlpha() != 255)
        {
            ColorAllRenderPropertyUpdator alphaUpdate = new ColorAllRenderPropertyUpdator(getProvider(),
                    getProvider().getDataType().getBasicVisualizationInfo().getDefaultTypeColor(), true);
            alphaUpdate.run();
        }
    }

    /**
     * Get the map geometry supports from the data element cache for my ids.
     *
     * @return The map geometry supports.
     */
    private List<MapGeometrySupport> getMapGeometrySupports()
    {
        long start = System.nanoTime();
        List<MapGeometrySupport> mgsList = MantleToolboxUtils.getDataElementLookupUtils(getProvider().getToolbox())
                .getMapGeometrySupport(myIdsOfInterest);
        if (LOGGER.isTraceEnabled())
        {
            long end = System.nanoTime();
            LOGGER.trace(StringUtilities.formatTimingMessage("MGS Lookup for " + myIdsOfInterest.size() + " elements took ",
                    end - start));
        }
        return mgsList;
    }

    /**
     * Get the visualization states from the data element cache for my ids.
     *
     * @return The visualization states.
     */
    private List<VisualizationState> getVisualizationStates()
    {
        long start = System.nanoTime();
        List<VisualizationState> vsList = MantleToolboxUtils.getDataElementLookupUtils(getProvider().getToolbox())
                .getVisualizationStates(myIdsOfInterest);
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace(StringUtilities.formatTimingMessage("VS Lookup for " + myIdsOfInterest.size() + " elements took ",
                    System.nanoTime() - start));
        }
        return vsList;
    }

    /**
     * Publish to registry.
     *
     * @param oldVisibleGeomSet the old visible geom set
     * @param newVisibleGeomSet the new visible geom set
     * @param newHiddenGeomSet the new hidden geom set
     */
    private void publishToRegistry(Set<Geometry> oldVisibleGeomSet, Set<Geometry> newVisibleGeomSet,
            Set<Geometry> newHiddenGeomSet)
    {
        long start;
        start = System.nanoTime();

        getProvider().getToolbox().getGeometryRegistry().receiveObjects(getProvider().getUpdateSource(), newVisibleGeomSet,
                oldVisibleGeomSet);

        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace(StringUtilities.formatTimingMessage(
                    "Added/Remove Geometries to registry[" + newVisibleGeomSet.size() + "/" + newHiddenGeomSet.size() + "] in ",
                    System.nanoTime() - start));
        }
    }

    /**
     * Run internal.
     */
    private void runInternal()
    {
        long zero = System.nanoTime();
        List<MapGeometrySupport> geometries = getMapGeometrySupports();

        List<VisualizationState> visualizationStates = getVisualizationStates();

        VisualizationState defaultVSState = new VisualizationState(true);

        long start = System.nanoTime();
        Set<Geometry> oldVisibleGeomSet = GeometrySetUtil.findGeometrySetWithIds(getProvider().getGeometrySet(),
                getProvider().getGeometrySetLock(), myIdsOfInterest, null,
                getProvider().getDataModelIdFromGeometryIdBitMask());

        Set<Geometry> oldHiddenGeomSet = GeometrySetUtil.findGeometrySetWithIds(getProvider().getHiddenGeometrySet(),
                getProvider().getGeometrySetLock(), myIdsOfInterest, null,
                getProvider().getDataModelIdFromGeometryIdBitMask());

        Set<Geometry> newVisibleGeomSet = New.set(oldVisibleGeomSet.size());
        Set<Geometry> newHiddenGeomSet = New.set(oldHiddenGeomSet.size());
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace(StringUtilities.formatTimingMessage("Pre Geom Creation Setup took ", System.nanoTime() - start));
        }

        Iterator<Long> idItr = myIdsOfInterest.iterator();
        Iterator<MapGeometrySupport> geometryIterator = geometries.iterator();
        Iterator<VisualizationState> visualizationIterator = visualizationStates.iterator();

        start = System.nanoTime();
        RenderPropertyPool pool = createPool();
        while (idItr.hasNext())
        {
            Long id = idItr.next();
            VisualizationState vs = visualizationIterator.next();
            vs = vs == null ? defaultVSState : vs;

            MapGeometrySupport mgs = geometryIterator.next();

            if (mgs != null)
            {
                myFactory.createGeometries(vs.isVisible() ? newVisibleGeomSet : newHiddenGeomSet, mgs, id.longValue(),
                        getProvider().getDataType(), vs, pool);
            }
        }

        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("RenderPropertyPoolSize: " + pool.size());
        }
        pool.clearPool();

        if (LOGGER.isTraceEnabled())
        {
            int totVis = newVisibleGeomSet.size();
            int totHidden = newHiddenGeomSet.size();
            int total = totVis + totHidden;
            LOGGER.trace(StringUtilities.formatTimingMessage(
                    "Created " + total + " (" + totVis + "/" + totHidden + ") Geometries in ", System.nanoTime() - start));
        }

        adjustGeometrySets(oldVisibleGeomSet, oldHiddenGeomSet, newVisibleGeomSet, newHiddenGeomSet);

        if (getProvider().isPublishUpdatesToGeometryRegistry())
        {
            publishToRegistry(oldVisibleGeomSet, newVisibleGeomSet, newHiddenGeomSet);
        }
        LOGGER.info(StringUtilities.formatTimingMessage("Overall Geometry Update Took: ", System.nanoTime() - zero));
    }
}
