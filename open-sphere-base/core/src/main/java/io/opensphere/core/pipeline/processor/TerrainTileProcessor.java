package io.opensphere.core.pipeline.processor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.ImageGroup;
import io.opensphere.core.geometry.ImageProvidingGeometry;
import io.opensphere.core.geometry.TerrainTileGeometry;
import io.opensphere.core.image.Image;
import io.opensphere.core.math.Ellipsoid;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicConvexPolygon;
import io.opensphere.core.model.GeographicPolygon;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.Position;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.processor.AbstractProcessor.State;
import io.opensphere.core.pipeline.processor.TerrainTileProcessor.TerrainTileState;
import io.opensphere.core.pipeline.util.TileSplitJoinHelper;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.projection.impl.MutableGlobeProjection;
import io.opensphere.core.terrain.util.AbsoluteElevationProvider;
import io.opensphere.core.terrain.util.ElevationImageReader;
import io.opensphere.core.terrain.util.ElevationImageReaderException;
import io.opensphere.core.terrain.util.ElevationManager;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.BatchingBlockingQueue;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ThreadedStateMachine;
import io.opensphere.core.util.concurrent.ThreadedStateMachine.StateChangeHandler;
import io.opensphere.core.util.concurrent.ThreadedStateMachine.StateController;
import io.opensphere.core.util.lang.UnexpectedEnumException;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * Processor for {@link TerrainTileGeometry}s. This class handles ensuring that
 * terrain tiles have backing images to use to provide altitudes and
 * additionally acts as a provider of the correct
 * {@link AbsoluteElevationProvider} for a given location. It is important to
 * note that it is assumed that this processor will be the only provider for
 * elevation from this source. In this case that means that all tiles for a
 * single source will be managed by a single processor.
 */
@SuppressWarnings("PMD.GodClass")
public class TerrainTileProcessor implements GeometryProcessor<TerrainTileGeometry>
{
    /**
     * The value which represents a missing value rather than an actual
     * elevation.
     */
    private static final double ELEVATION_NO_VALUE = -Double.MAX_VALUE;

    /** How long the geometry queue waits before releasing a batch. */
    private static final long GEOMETRY_QUEUE_DELAY_MILLISECONDS = 300L;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(TerrainTileProcessor.class);

    /** The cache for model data and geometries. */
    private final CacheProvider myCache;

    /** Flag indicating if the processor is closed. */
    private volatile boolean myClosed;

    /**
     * Elevation reader used for providing general information about the
     * elevation provided by this provider.
     */
    private ElevationImageReader myElevationImageReader;

    /** The elevation manager which manages this processor. */
    private ElevationManager myElevationManager;

//    /** The regions for which I can provide elevation values. */
//    private List<GeographicPolygon> myCoverage;

    /**
     * Provider for elevation data which is available from the terrain tiles.
     */
    private final AbsoluteElevationProvider myElevationProvider = new AbsoluteElevationProvider()
    {
        @Override
        public void close()
        {
        }

        @Override
        public GeographicBoundingBox getBoundingBox()
        {
            return GeographicBoundingBox.WHOLE_GLOBE;
        }

        @Override
        public String getCRS()
        {
            return myElevationImageReader.getCRS();
        }

        @Override
        public double getElevationM(GeographicPosition position, boolean approximate)
        {
            if (myClosed)
            {
                return 0.;
            }

            Collection<? extends TerrainTileGeometry> deepestTiles = getDeepestTilesAtPosition(position);
            if (deepestTiles.isEmpty())
            {
                return 0;
            }

            double elevation = getElevationForTiles(deepestTiles, position, false);
            return elevation == ELEVATION_NO_VALUE ? 0 : elevation;
        }

        @Override
        public String getElevationOrderId()
        {
            return myElevationImageReader.getElevationOrderId();
        }

        @Override
        public double getMinVariance()
        {
            return 0.;
        }

        @Override
        public double getMissingDataValue()
        {
            return myElevationImageReader.getMissingDataValue();
        }

        @Override
        public List<GeographicPolygon> getRegions()
        {
            // TODO need to change provider event to allow changing the coverage
            // area.
            return Collections.singletonList(
                    (GeographicPolygon)new GeographicConvexPolygon(GeographicBoundingBox.WHOLE_GLOBE.getVertices()));
//            return myCoverage == null ? Collections.<GeographicPolygon>emptyList() : myCoverage;
        }

        @Override
        public double getResolutionHintM()
        {
            return -1.;
        }

        @Override
        public boolean overlaps(GeographicPolygon polygon)
        {
            return true;
        }

        @Override
        public boolean petrifiesTerrain()
        {
            return false;
        }

        @Override
        public boolean providesForPosition(GeographicPosition position)
        {
            return true;
        }

        /**
         * Attempt to read an elevation from the tile's image. If the image is
         * not immediately available, check to see if there are any cached
         * elevations. If there are any, pick the closest one and use that. If
         * there are no cached elevations, defer to the parent tile. If there is
         * no parent tile, just return {@link #ELEVATION_NO_VALUE}.
         *
         * @param tiles The tiles.
         * @param position The position of interest.
         * @param isBackup Flag indicating if this is a parent tile, meaning its
         *            image should not be requested.
         * @return The elevation.
         */
        private double getElevationForTiles(Collection<? extends TerrainTileGeometry> tiles, GeographicPosition position,
                boolean isBackup)
        {
            double elevation = ELEVATION_NO_VALUE;

            Set<TerrainTileGeometry> parents = New.set();
            Collection<TerrainTileGeometry> reloads = null;
            for (TerrainTileGeometry tile : tiles)
            {
                if (tile.getParent() != null && tile.getParent().getBounds().contains(position, 0.))
                {
                    parents.add((TerrainTileGeometry)tile.getParent());
                }

                ImageGroup imageGroup = myCache.getCacheAssociation(tile, ImageGroup.class);
                if (imageGroup == null)
                {
                    if (!isBackup)
                    {
                        // The image has been cleaned from the cache and we
                        // still need it.
                        tile.requestImageData(myPriorityComparator, TimeBudget.ZERO);
                    }
                }
                else
                {
                    Image sampleImage = imageGroup.getImageMap().values().iterator().next();
                    try
                    {
                        elevation = tile.getReader().readElevation(position, sampleImage, (GeographicBoundingBox)tile.getBounds(),
                                true);
                    }
                    catch (ElevationImageReaderException e)
                    {
                        if (LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug("Failed to read elevation for tile: " + tile + ": " + e, e);
                        }
                        myCache.clearCacheAssociation(tile, ImageGroup.class);
                        reloads = CollectionUtilities.lazyAdd(tile, reloads);
                    }
                }

                if (elevation != ELEVATION_NO_VALUE)
                {
                    return elevation;
                }
            }

            if (!parents.isEmpty())
            {
                elevation = getElevationForTiles(parents, position, true);
            }
            if (reloads != null)
            {
                resetState(reloads, State.UNPROCESSED);
            }

            return elevation;
        }
    };

    /** The set of top level geometries this processor is currently managing. */
    private final Set<TerrainTileGeometry> myGeometries = Collections.synchronizedSet(New.<TerrainTileGeometry>weakSet());

    /** Queue of geometries ready to be reprocessed. */
    private final BatchingBlockingQueue<TerrainTileGeometry> myGeometryQueue;

    /** The context used to access the viewer and projection. */
    private final MapContext<?> myMapContext;

    /** Comparator that determines geometry processing priority. */
    private final Comparator<? super TerrainTileGeometry> myPriorityComparator;

    /**
     * The set of geometries that are ready to be used as terrain providers.
     */
    private final Set<TerrainTileGeometry> myReadyGeometries = New.set();

    /** Helper class for handling tile splits and joins. */
    private final TileSplitJoinHelper mySplitJoinHelper;

    /** State change handler for terrain tile states. */
    private final StateChangeHandler<TerrainTileGeometry> myStateChangeHandler = new StateChangeHandler<TerrainTileGeometry>()
    {
        @Override
        public void handleStateChanged(List<? extends TerrainTileGeometry> objects, ThreadedStateMachine.State newState,
                StateController<TerrainTileGeometry> controller)
        {
            if (TerrainTileState.class.isInstance(newState))
            {
                switch (TerrainTileState.class.cast(newState))
                {
                    case AWAITING_IMAGE:
                        processAwaitingImage(objects, controller);
                        break;
                    default:
                        throw new UnexpectedEnumException(TerrainTileState.class.cast(newState));
                }
            }
            else
            {
                switch (State.class.cast(newState))
                {
                    case UNPROCESSED:
                        processUnprocessed(objects, controller);
                        break;
                    case READY:
                        processReady(objects, controller);
                        break;
                    default:
                        throw new UnexpectedEnumException(TerrainTileState.class.cast(newState));
                }
            }
        }
    };

    /**
     * The state machine used to manage the threads used for processing
     * geometries.
     */
    private final ThreadedStateMachine<TerrainTileGeometry> myStateMachine;

    /** Observer to be notified when a geometry has new data available. */
    private final ImageProvidingGeometry.Observer<TerrainTileGeometry> myTileGeometryObserver = new ImageProvidingGeometry.Observer<TerrainTileGeometry>()
    {
        @Override
        public void dataReady(TerrainTileGeometry geom)
        {
            if (!isClosed() && geom.getImageManager().getCachedImageData() != null)
            {
                // Check to make sure that this tile is not orphaned.
                if (geom.isOrphan())
                {
                    return;
                }

                if (geom.isRapidUpdate())
                {
                    processImageUpdates(Collections.singletonList(geom));
                }
                else
                {
                    myGeometryQueue.offer(geom);
                }
            }
        }
    };

    /** Listener for view change events. */
    private final ViewChangeSupport.ViewChangeListener myViewChangeListener = new ViewChangeSupport.ViewChangeListener()
    {
        @Override
        public String toString()
        {
            return new StringBuilder().append(ViewChangeSupport.ViewChangeListener.class.getSimpleName()).append(" [")
                    .append(TerrainTileProcessor.this.toString()).append(']').toString();
        }

        @Override
        public void viewChanged(Viewer view, ViewChangeSupport.ViewChangeType type)
        {
            handleViewChanged(view, type);
        }
    };

    /**
     * Construct a tile processor.
     *
     * @param builder The builder for the processor.
     */
    public TerrainTileProcessor(ProcessorBuilder builder)
    {
        CacheProvider cache = builder.getCache();
        Utilities.checkNull(cache, "cache");

        MapContext<?> mapContext = builder.getMapContext();
        Utilities.checkNull(mapContext, "mapContext");

        Executor threadPoolExecutor = builder.getExecutorService();
        Utilities.checkNull(threadPoolExecutor, "threadPoolExecutor");

        Executor loadSensitiveExecutor = builder.getLoadSensitiveExecutor();
        Utilities.checkNull(loadSensitiveExecutor, "loadSensitiveExecutor");

        Utilities.checkNull(builder.getPriorityComparator(), "builder.getPriorityComparator()");
        myPriorityComparator = builder.getPriorityComparator();

        myCache = cache;
        myMapContext = mapContext;

        myStateMachine = createStateMachine(threadPoolExecutor, loadSensitiveExecutor);

        mySplitJoinHelper = new TerrainTileSplitJoinHelper(builder.getScheduledExecutorService(), myCache);

        ViewChangeSupport viewChangeSupport = myMapContext.getViewChangeSupport();
        if (viewChangeSupport != null)
        {
            viewChangeSupport.addViewChangeListener(myViewChangeListener);
        }

        myGeometryQueue = new BatchingBlockingQueue<TerrainTileGeometry>(builder.getScheduledExecutorService(),
                GEOMETRY_QUEUE_DELAY_MILLISECONDS);

        myGeometryQueue.addObserver(new BatchingBlockingQueue.Observer()
        {
            @Override
            public void objectsAdded()
            {
                List<TerrainTileGeometry> objs;
                if (myGeometryQueue.size() == 1)
                {
                    TerrainTileGeometry obj = myGeometryQueue.poll();
                    objs = obj == null ? Collections.<TerrainTileGeometry>emptyList() : Collections.singletonList(obj);
                }
                else
                {
                    objs = New.list();
                    myGeometryQueue.drainTo(objs);
                }

                if (!isClosed() && !objs.isEmpty())
                {
                    processImageUpdates(objs);
                }
            }
        });
    }

    @Override
    public boolean allGeometriesReady()
    {
        return true;
    }

    @Override
    public void close()
    {
        myClosed = true;
        myStateMachine.stop();
        mySplitJoinHelper.stop();
        if (myMapContext.getViewChangeSupport() != null)
        {
            myMapContext.getViewChangeSupport().removeViewChangeListener(myViewChangeListener);
        }

        for (Projection proj : myMapContext.getProjections().keySet())
        {
            if (proj instanceof MutableGlobeProjection && myElevationImageReader != null)
            {
                ((MutableGlobeProjection)proj).getModel().getCelestialBody().getElevationManager()
                        .deregisterProvider(myElevationProvider);
            }
        }
    }

    @Override
    public Collection<TerrainTileGeometry> getGeometries()
    {
        Collection<TerrainTileGeometry> result;
        synchronized (myGeometries)
        {
            result = New.collection(myGeometries);
        }
        return result;
    }

    @Override
    public int getGeometryCount()
    {
        synchronized (myGeometries)
        {
            return myGeometries.size();
        }
    }

    /**
     * Get the projectionSnapshot.
     *
     * @return the projectionSnapshot
     */
    public Projection getProjectionSnapshot()
    {
        return myMapContext.getProjection();
    }

    @Override
    public void handleProjectionChanged(ProjectionChangedEvent evt)
    {
    }

    @Override
    public boolean handlesType(Class<? extends TerrainTileGeometry> type)
    {
        return TerrainTileGeometry.class.isAssignableFrom(type);
    }

    @Override
    public boolean hasGeometry(Geometry geo)
    {
        return myGeometries.contains(geo);
    }

    @Override
    public boolean isClosed()
    {
        return myClosed;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void receiveObjects(Object source, Collection<? extends Geometry> adds, Collection<? extends Geometry> removes)
    {
        boolean changed;
        Set<Geometry> removesPlusChildren;
        synchronized (myGeometries)
        {
            if (removes.isEmpty())
            {
                removesPlusChildren = Collections.emptySet();
            }
            else
            {
                removesPlusChildren = AbstractTileGeometry.getGeometriesPlusDescendants(removes);
            }

            // Optimization for the common case that all geometries are being
            // removed.
            if (removesPlusChildren.size() > 1 && myGeometries.size() == removesPlusChildren.size()
                    && myGeometries.containsAll(removesPlusChildren))
            {
                changed = true;
                myGeometries.clear();
                if (!adds.isEmpty())
                {
                    myGeometries.addAll((Collection<? extends TerrainTileGeometry>)adds);
                }
            }
            else
            {
                changed = myGeometries.removeAll(removesPlusChildren)
                        | myGeometries.addAll((Collection<? extends TerrainTileGeometry>)adds);
            }
            checkElevationReadersCompatible();
            setCoveredRegions();
        }

        if (changed)
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("receiveObjects source [" + source + "] adds: " + adds.size() + " removes: "
                        + removesPlusChildren.size());
            }

            if (!removesPlusChildren.isEmpty())
            {
                List<TerrainTileGeometry> toRemove = Arrays
                        .asList(removesPlusChildren.toArray(new TerrainTileGeometry[removesPlusChildren.size()]));
                processRemoves(toRemove);
            }
            if (!adds.isEmpty())
            {
                myStateMachine.resetState((Collection<? extends TerrainTileGeometry>)adds, State.UNPROCESSED);
            }
        }

        if (!adds.isEmpty())
        {
            mySplitJoinHelper.doSplitsAndJoins((Collection<? extends AbstractTileGeometry<?>>)adds);
        }
    }

    @Override
    public boolean sensitiveToProjectionChanges()
    {
        return false;
    }

    @Override
    public void switchToProjection(Projection projectionSnapshot)
    {
    }

    /**
     * Determine the state of the input geometries and place them in the
     * appropriate return collection.
     *
     * @param input The input geometries.
     * @param awaitingImage Return collection of geometries that are still
     *            waiting for image data.
     * @param ready Return collection of geometries that have loaded images.
     */
    protected void determineStates(Collection<? extends TerrainTileGeometry> input,
            Collection<? super TerrainTileGeometry> awaitingImage, Collection<? super TerrainTileGeometry> ready)
    {
        // If the geometries have geographic positions, sort them by their
        // distance from the center of the view.
        Collection<? extends TerrainTileGeometry> geometries;
        TerrainTileGeometry first = input.iterator().next();
        if (GeographicPosition.class.isAssignableFrom(first.getPositionType()))
        {
            List<? extends TerrainTileGeometry> sortedGeometries = CollectionUtilities.getList(input);
            Vector3d modelCenter = getViewer().getModelIntersection();
            if (modelCenter == null)
            {
                modelCenter = getViewer().getClosestModelPosition();
            }
            Collections.sort(sortedGeometries, new RadialGeometryComparator(modelCenter, getProjectionSnapshot()));
            geometries = sortedGeometries;
        }
        else
        {
            geometries = input;
        }

        for (TerrainTileGeometry geom : geometries)
        {
            Enum<? extends ThreadedStateMachine.State> state = determineTileState(geom);

            if (state instanceof TerrainTileState)
            {
                switch ((TerrainTileState)state)
                {
                    case AWAITING_IMAGE:
                        awaitingImage.add(geom);
                        break;
                    default:
                        throw new UnexpectedEnumException(state);
                }
            }
            else if (state == State.READY)
            {
                ready.add(geom);
            }
            else
            {
                throw new UnexpectedEnumException(state);
            }
        }
    }

    /**
     * Determine the target state for a geometry based on what cached data is
     * available.
     *
     * @param geom The geometry.
     * @return The target state.
     */
    protected Enum<? extends ThreadedStateMachine.State> determineTileState(TerrainTileGeometry geom)
    {
        Enum<? extends ThreadedStateMachine.State> state;

        synchronized (myGeometries)
        {
            if (hasGeometry(geom.getTopAncestor()))
            {
                geom.addObserver(myTileGeometryObserver);
            }
        }
        if (!geom.hasChildren())
        {
            geom.requestImageData(myPriorityComparator, TimeBudget.ZERO);
        }
        ImageGroup imageGroup = geom.getImageManager().pollCachedImageData();
        if (imageGroup == null)
        {
            // No image data; wait for image.
            state = TerrainTileState.AWAITING_IMAGE;
        }
        else
        {
            state = State.READY;
            if (!geom.hasChildren())
            {
                myCache.putCacheAssociation(geom, imageGroup, ImageGroup.class, imageGroup.getSizeBytes(), 0L);
            }
        }

        return state;
    }

    /**
     * Get the set of geometries that are ready to be displayed.
     *
     * @return The ready geometries.
     */
    protected Set<TerrainTileGeometry> getReadyGeometries()
    {
        synchronized (myReadyGeometries)
        {
            return New.set(myReadyGeometries);
        }
    }

    /**
     * Get the viewer.
     *
     * @return The viewer.
     */
    protected Viewer getViewer()
    {
        return myMapContext.getStandardViewer();
    }

    /**
     * Call-back for a view changed event. When the view changes, the geometries
     * must be reviewed to determine which ones should be split or joined.
     *
     * @param view The viewer.
     * @param type The viewer update type.
     */
    protected void handleViewChanged(final Viewer view, final ViewChangeSupport.ViewChangeType type)
    {
        if (!AbstractProcessor.isSplitJoinPaused())
        {
            mySplitJoinHelper.scheduleSplitJoin();
        }
    }

    /**
     * Determine if a geometry is currently on-screen. This is used to determine
     * tile splitting.
     *
     * @param geom The geometry.
     * @return <code>true</code> if the geometry is on-screen.
     */
    protected boolean isInView(AbstractTileGeometry<?> geom)
    {
        Object bbox = geom.getBounds();
        if (bbox instanceof GeographicBoundingBox)
        {
            Ellipsoid bounds = myCache.getCacheAssociation(bbox, Ellipsoid.class);
            if (bounds == null)
            {
                bounds = getProjectionSnapshot().getBoundingEllipsoid((GeographicBoundingBox)bbox, Vector3d.ORIGIN, false);

                // Perform the check to get the size on the ellipsoid right.
                boolean inView = getViewer().isInView(bounds, ELLIPSOID_CULL_COSINE);
                myCache.putCacheAssociation(bbox, bounds, Ellipsoid.class, bounds.getSizeBytes(), 0L);
                return inView;
            }
            return getViewer().isInView(bounds, ELLIPSOID_CULL_COSINE);
        }

        return true;
    }

    /**
     * Callback from the tile state machine for geometries in the
     * {@link TerrainTileState#AWAITING_IMAGE} state. Check to see if the
     * geometries have image data.
     *
     * @param objects The objects.
     * @param controller A controller used to initiate further state changes for
     *            the geometries.
     */
    protected void processAwaitingImage(Collection<? extends TerrainTileGeometry> objects,
            StateController<TerrainTileGeometry> controller)
    {
        Collection<TerrainTileGeometry> ready = New.collection();

        for (TerrainTileGeometry geom : objects)
        {
            ImageGroup imageGroup = geom.getImageManager().pollCachedImageData();
            if (imageGroup != null)
            {
                ready.add(geom);
                myCache.putCacheAssociation(geom, imageGroup, ImageGroup.class, imageGroup.getSizeBytes(), 0L);
            }
        }

        controller.changeState(ready, State.READY);
    }

    /**
     * Callback from the state machine for geometries in the {@link State#READY}
     * state. Add the objects to the ready collection and instruct the
     * projection to reset terrain over the tile bounds.
     *
     * @param ready The objects.
     * @param controller The state controller.
     */
    protected void processReady(Collection<? extends TerrainTileGeometry> ready, StateController<TerrainTileGeometry> controller)
    {
        // Hold the geometries constant while moving geometries into the ready
        // collection to avoid putting a geometry into the ready collection that
        // has been removed from the processor in the midst of this method.
        List<TerrainTileGeometry> remainingReady = New.list(ready.size());
        synchronized (myGeometries)
        {
            // Ensure that the geometries coming out of processing have not been
            // removed.
            for (TerrainTileGeometry geom : ready)
            {
                TerrainTileGeometry top = (TerrainTileGeometry)geom.getTopAncestor();
                if (myGeometries.contains(top))
                {
                    remainingReady.add(geom);
                }
            }

            synchronized (myReadyGeometries)
            {
                myReadyGeometries.addAll(remainingReady);
            }
        }

        final Collection<GeographicPolygon> changedRegions = New.collection();
        for (final TerrainTileGeometry readyGeom : ready)
        {
            // If the geometry has children then do not update. The consumer
            // should be using values from the child tiles.
            if (!readyGeom.hasChildren())
            {
                changedRegions.add(((GeographicBoundingBox)readyGeom.getBounds()).asGeographicPolygon());
            }
        }

        myElevationManager.notifyElevationsModified(myElevationProvider, changedRegions);
    }

    /**
     * Remove geometries from the processor.
     *
     * @param removes The set of removes.
     */
    @SuppressWarnings("unchecked")
    protected void processRemoves(Collection<? extends AbstractTileGeometry<?>> removes)
    {
        if (removes != null && !removes.isEmpty())
        {
            myStateMachine.removeFromState(removes);
            removeFromReady(removes);

            final Collection<GeographicPolygon> changedRegions = New.collection();
            for (AbstractTileGeometry<?> geom : removes)
            {
                ImageProvidingGeometry<TerrainTileGeometry> cast = (ImageProvidingGeometry<TerrainTileGeometry>)geom;
                cast.removeObserver(myTileGeometryObserver);
                // If the geometry's parent is also being removed, the changed
                // regions will already cover the geometry's bounds.
                if (geom.getParent() == null || !removes.contains(geom.getParent()))
                {
                    changedRegions.add(((GeographicBoundingBox)geom.getBounds()).asGeographicPolygon());
                }
            }

            myElevationManager.notifyElevationsModified(myElevationProvider, changedRegions);
        }
    }

    /**
     * Callback from the state machine for geometries in the
     * {@link State#UNPROCESSED} state. Process the geometries and move the ones
     * that are ready into the {@link State#READY} state.
     *
     * @param unprocessed The objects.
     * @param controller The state controller.
     */
    protected void processUnprocessed(Collection<? extends TerrainTileGeometry> unprocessed,
            StateController<TerrainTileGeometry> controller)
    {
        if (unprocessed.isEmpty())
        {
            return;
        }

        Collection<TerrainTileGeometry> awaitingImage = New.collection();

        Collection<TerrainTileGeometry> ready = New.collection();
        determineStates(unprocessed, awaitingImage, ready);

        if (!awaitingImage.isEmpty())
        {
            controller.changeState(awaitingImage, TerrainTileState.AWAITING_IMAGE);
        }

        if (!ready.isEmpty())
        {
            controller.changeState(ready, State.READY);
        }
    }

    /**
     * Remove some geometries from the ready collection.
     *
     * @param notReady The geometries that are no longer ready.
     */
    protected void removeFromReady(Collection<? extends Geometry> notReady)
    {
        synchronized (myReadyGeometries)
        {
            myReadyGeometries.removeAll(notReady);
        }
    }

    /**
     * Determine whether the given elevation readers are compatible for the
     * purposes of this processor.
     */
    private void checkElevationReadersCompatible()
    {
        for (TerrainTileGeometry geom : myGeometries)
        {
            if (myElevationImageReader == null)
            {
                myElevationImageReader = geom.getReader();

                for (Projection proj : myMapContext.getProjections().keySet())
                {
                    if (proj instanceof MutableGlobeProjection)
                    {
                        myElevationManager = ((MutableGlobeProjection)proj).getModel().getCelestialBody().getElevationManager();
                        myElevationManager.registerProvider(myElevationProvider);
                    }
                }
            }
            else
            {
                if (!(myElevationImageReader.getCRS().equals(geom.getReader().getCRS())
                        && myElevationImageReader.getMissingDataValue() == geom.getReader().getMissingDataValue()))
                {
                    LOGGER.error("Processing terrain tiles which are not compatible.");
                }
            }
        }
    }

    /**
     * Create the state machine.
     *
     * @param highVolumeExecutor The executor for the state machine for high
     *            volumes of objects.
     * @param lowVolumeExecutor The executor for the state machine for low
     *            volumes of objects.
     * @return The state machine.
     */
    private ThreadedStateMachine<TerrainTileGeometry> createStateMachine(Executor highVolumeExecutor, Executor lowVolumeExecutor)
    {
        ThreadedStateMachine<TerrainTileGeometry> tsm = new ThreadedStateMachine<>();

        EnumSet<TerrainTileState> handledStates = EnumSet.allOf(TerrainTileState.class);
        tsm.registerStateChangeHandler(handledStates, myStateChangeHandler, highVolumeExecutor, lowVolumeExecutor, 0);
        EnumSet<State> handledBaseStates = EnumSet.allOf(State.class);
        tsm.registerStateChangeHandler(handledBaseStates, myStateChangeHandler, highVolumeExecutor, lowVolumeExecutor, 0);

        return tsm;
    }

    /**
     * Find the deepest child which contains the position.
     *
     * @param ready The terrain tiles which were in the ready state at the time
     *            of the request.
     * @param geom Tile to search.
     * @param position Position which must be within the returned geometry.
     * @param result Output collection containing the deepest children.
     * @return {@code true} if anything was added to the result collection.
     */
    private boolean getDeepestContainers(Collection<TerrainTileGeometry> ready, TerrainTileGeometry geom, Position position,
            Collection<TerrainTileGeometry> result)
    {
        boolean found = false;
        if (geom.getBounds().contains(position, 0d))
        {
            for (AbstractTileGeometry<?> child : geom.getChildren(false))
            {
                found |= getDeepestContainers(ready, (TerrainTileGeometry)child, position, result);
            }
            if (!found || ready.contains(geom))
            {
                result.add(geom);
                found = true;
            }
        }
        return found;
    }

    /**
     * Get the deepest tiles at the given position.
     *
     * @param position The position to be contained in the deepest tile.
     * @return the deepest tiles at the given position.
     */
    private Collection<? extends TerrainTileGeometry> getDeepestTilesAtPosition(Position position)
    {
        Collection<TerrainTileGeometry> ready = getReadyGeometries();
        Collection<TerrainTileGeometry> results = New.collection();
        for (TerrainTileGeometry geom : getGeometries())
        {
            getDeepestContainers(ready, geom, position, results);
        }

        return results;
    }

    /**
     * Process geometries for image updates. This resets the geometries to
     * unprocessed so that the image will be acquired through the normal
     * mechanism.
     *
     * @param geoms The geometries.
     */
    private void processImageUpdates(List<TerrainTileGeometry> geoms)
    {
        // Make sure the geometries aren't removed from the processor
        // while this is happening.
        synchronized (myGeometries)
        {
            // Remove any of the geometries in the geometry queue that
            // have been removed from the processor.
            for (Iterator<TerrainTileGeometry> iter = geoms.iterator(); iter.hasNext();)
            {
                TerrainTileGeometry geom = iter.next();
                if (!myGeometries.contains(geom.getTopAncestor()))
                {
                    if (geoms.size() == 1)
                    {
                        return;
                    }
                    iter.remove();
                }
            }

            if (!geoms.isEmpty())
            {
                resetState(geoms, State.UNPROCESSED);
            }
        }
    }

    /**
     * Before resetting the state in the state machine, check to make sure that
     * we are not resetting state for a tile which has been orphaned.
     *
     * @param objects The objects to reset, which may be {@code null}.
     * @param toState The destination state.
     */
    private void resetState(Collection<? extends TerrainTileGeometry> objects, ThreadedStateMachine.State toState)
    {
        if (CollectionUtilities.hasContent(objects))
        {
            synchronized (myGeometries)
            {
                Collection<TerrainTileGeometry> toReset = New.collection(objects.size());
                Collection<TerrainTileGeometry> orphans = null;
                for (TerrainTileGeometry geom : objects)
                {
                    if (!geom.isOrphan() && myGeometries.contains(geom.getTopAncestor()))
                    {
                        toReset.add(geom);
                    }
                    else
                    {
                        orphans = CollectionUtilities.lazyAdd(geom, orphans);
                    }
                }

                myStateMachine.resetState(toReset, toState);
                processRemoves(orphans);
            }
        }
    }

    /** Set my coverage regions based on the bounding boxes of my geometries. */
//    @SuppressWarnings("unchecked")
    private void setCoveredRegions()
    {
        // TODO changing the coverage region should cause an event to be sent
        // through the Elevation manager.
//        List<GeographicPolygon> regions = New.list(myGeometries.size());
//        for (TerrainTileGeometry geom : myGeometries)
//        {
//            regions.add(new GeographicConvexPolygon((List<? extends GeographicPosition>)geom.getBoundingBox().getVertices()));
//        }
//        myCoverage = regions;
    }

    /** The states used by this processor in the processing state machine. */
    protected enum TerrainTileState implements ThreadedStateMachine.State
    {
        /** State for geometries that are waiting for an image. */
        AWAITING_IMAGE(State.UNPROCESSED.getStateOrder() + 10),

        ;

        /** The order of the state. */
        private final int myStateOrder;

        /**
         * Construct a state.
         *
         * @param order The order of the state.
         */
        TerrainTileState(int order)
        {
            myStateOrder = order;
        }

        @Override
        public int getStateOrder()
        {
            return myStateOrder;
        }
    }

    /**
     * The split-join helper for terrain tiles.
     */
    private final class TerrainTileSplitJoinHelper extends TileSplitJoinHelper
    {
        /**
         * Constructor.
         *
         * @param executor An executor to use for splitting/joining.
         * @param cache The cache for data associated with the tiles.
         */
        public TerrainTileSplitJoinHelper(ScheduledExecutorService executor, CacheProvider cache)
        {
            super(executor);
        }

        @Override
        protected Collection<? extends AbstractTileGeometry<?>> getProcessorGeometries()
        {
            return getGeometries();
        }

        @Override
        protected Projection getProjection()
        {
            return getProjectionSnapshot();
        }

        @Override
        protected Set<? extends AbstractTileGeometry<?>> getReadyGeometries()
        {
            return TerrainTileProcessor.this.getReadyGeometries();
        }

        @Override
        protected Viewer getViewer()
        {
            return TerrainTileProcessor.this.getViewer();
        }

        @Override
        protected boolean isInView(AbstractTileGeometry<?> geom)
        {
            // Even for terrain tiles, we only want to split when the terrain is
            // visible.
            return TerrainTileProcessor.this.isInView(geom);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void receiveGeometries(Collection<? extends AbstractTileGeometry<?>> splitAdds,
                Collection<? extends AbstractTileGeometry<?>> joinRemoves)
        {
            // Do the removes first since resetState checks for orphaned tiles
            processRemoves(joinRemoves);
            if (!splitAdds.isEmpty())
            {
                // Do not to reset geometries which are already READY.
                splitAdds.removeAll(getReadyGeometries());
                resetState((Collection<? extends TerrainTileGeometry>)splitAdds, State.UNPROCESSED);
            }
        }
    }
}
