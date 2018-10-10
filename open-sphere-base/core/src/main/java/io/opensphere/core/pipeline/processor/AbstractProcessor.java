package io.opensphere.core.pipeline.processor;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import io.opensphere.core.TimeManager;
import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.ConstrainableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.HierarchicalGeometry;
import io.opensphere.core.geometry.RenderableGeometry;
import io.opensphere.core.geometry.constraint.ConstraintsChangedEvent;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.RenderPropertyChangedEvent;
import io.opensphere.core.math.Ellipsoid;
import io.opensphere.core.math.Ray3d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.ModelBoundingBox;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.processor.ConstraintChecker.TimeConstraintStatus;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.pipeline.renderer.TimeFilteringRenderer;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.pipeline.util.RepaintListener;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.concurrent.ThreadedStateMachine;
import io.opensphere.core.util.concurrent.ThreadedStateMachine.StateChangeHandler;
import io.opensphere.core.util.concurrent.ThreadedStateMachine.StateController;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.lang.UnexpectedEnumException;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.MapContext;
import io.opensphere.core.viewer.impl.PositionConverter;

/**
 * Abstract implementation of {@code GeometryProcessor} that provides common
 * functionality between geometry types.
 *
 * @param <E> The type of geometry handled by this processor.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class AbstractProcessor<E extends Geometry> implements RenderableGeometryProcessor<E>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AbstractProcessor.class);

    /** Flag that indicates that updates should not be sent to the renderers. */
    private static boolean ourRendererUpdatesPaused;

    /** Flag that indicates whether splitting and joining is allowed. */
    private static boolean ourSplitJoinPaused;

    /** The cache for model data. */
    private final CacheProvider myCache;

    /** Flag indicating if time constraints need to be checked. */
    private volatile boolean myCheckingTimeConstraintsNeeded;

    /** Flag indicating if the processor is closed. */
    private volatile boolean myClosed;

    /** The constraint checker. */
    private final ConstraintChecker myConstraintChecker;

    /** Executor to handle setting myOnscreenDirty. */
    private final ProcrastinatingExecutor myDirtyExecutor;

    /** The non-GL executor. */
    private final ExecutorService myExecutorService;

    /**
     * The set of top level geometries this processor is currently managing. The
     * processor may be managing more geometries for geometry types which are
     * hierarchical.
     */
    private final Collection<E> myGeometries = Collections.synchronizedSet(New.<E>weakSet());

    /** The concrete geometry type handled by this processor. */
    private final Class<?> myGeometryType;

    /**
     * Indicates if the processor has a most recent constrained geometry.
     */
    private boolean myHasMostRecent;

    /** Helper to handle listening for various events. */
    private AbstractProcessorListenerHelper myListenerHelper;

    /** The viewer set, used to access the viewer and projection. */
    private final MapContext<?> myMapContext;

    /**
     * The facility provided to the renderer for retrieving model data for
     * geometries.
     */
    private final AbstractRenderer.ModelDataRetriever<E> myModelDataRetriever = this::getModelData;

    /** Used to filter out MostRecent constrained geometries. */
    private final MostRecentGeometryFilter myMostRecentFilter = new MostRecentGeometryFilter();

    /**
     * Executor for activities which are not required to be on the GL thread.
     */
    private final ScheduledExecutorService myNonGLScheduledExecutor;

    /** Executor for doing determination of on screen geometries. */
    private final Executor myOnscreenDeterminationExecutor;

    /** Flag indicating that geometries need to be rendered. */
    private boolean myOnscreenDirty;

    /**
     * The current on-screen drawable geometries. This is designed to be quickly
     * switched out for a new collection to avoid delaying the render loop.
     */
    private volatile Collection<E> myOnscreenDrawableGeometries = Collections.emptyList();

    /**
     * The current on-screen pickable geometries. This is designed to be quickly
     * switched out for a new collection to avoid delaying the render loop.
     */
    private volatile Collection<E> myOnscreenPickableGeometries = Collections.emptyList();

    /**
     * The first element of this map is a geometry which is replacing another
     * geometry. The second element is a list which contains the geometries it
     * replaces. For example, if A will be replaced by B and B will be replaced
     * with C, the map will contain the entry [B, {A}] and the entry [C, {B,
     * A}].
     */
    private final Map<E, Collection<E>> myPendingReplacement = New.map();

    /**
     * The facility for determining which geometries have been picked by the
     * mouse.
     */
    private final PickManager myPickManager;

    /** The pick manager geometry remover. */
    private final PickManagerGeometryRemover myPickManagerGeometryRemover;

    /** Helper that converts different position types. */
    private final PositionConverter myPositionConverter;

    /**
     * The position type of the geometries in this processor.
     */
    private final AtomicReference<Class<? extends Position>> myPositionTypeRef = new AtomicReference<>();

    /** Lock for projection changes. */
    private final ReentrantReadWriteLock myProjectionChangeLock = new ReentrantReadWriteLock();

    /**
     * Flag indicating that the projection has changed and pre-rendering is
     * required.
     */
    private volatile boolean myProjectionDirty;

    /** A snapshot of the current state of the projection. */
    private volatile Projection myProjectionSnapshot;

    /** The manager for synchronizing projection changes in the processors. */
    private final ProjectionSyncManager myProjectionSyncManager;

    /**
     * The set of geometries that are ready to be displayed. This is separate
     * from the READY state in the state machine so that geometries can still be
     * on-screen even when they are being reprocessed in the state machine.
     * <p>
     * This is guarded by its own monitor for all operations, but if geometries
     * are to be added to the set, {@link #myGeometries} must also be locked
     * before {@link #myReadyGeometries} is locked so that geometries cannot be
     * added while {@link #handleProjectionChanged(ProjectionChangedEvent)} is
     * working.
     */
    private final Collection<E> myReadyGeometries = New.set();

    /**
     * Observers to be notified when all of the geometries for this processor
     * are ready.
     */
    private final Collection<Runnable> myReadyObservers = New.collection(1);

    /** A reference to the renderer implementation. */
    private final GeometryRenderer<E> myRenderer;

    /** The listener for repaint requests. */
    private final RepaintListener myRepaintListener;

    /** The state change handler for the state machine. */
    private final StateChangeHandler<E> myStateChangeHandler = new StateChangeHandler<>()
    {
        @Override
        public void handleStateChanged(List<? extends E> objects, ThreadedStateMachine.State newState,
                StateController<E> controller)
        {
            if (State.class.isInstance(newState))
            {
                switch (State.class.cast(newState))
                {
                    case UNPROCESSED:
                        processUnprocessed(objects, controller);
                        break;
                    case DEFERRED:
                        processDeferred(objects, controller);
                        break;
                    case READY:
                        processReady(objects, controller);
                        break;
                    default:
                        throw new UnexpectedEnumException(State.class.cast(newState));
                }
            }
        }
    };

    /**
     * The state machine used to manage the threads used for processing
     * geometries.
     */
    private final ThreadedStateMachine<E> myStateMachine;

    /** Reference to the time manager. */
    private final TimeManager myTimeManager;

    /**
     * Get the splitJoinPaused.
     *
     * @return the splitJoinPaused
     */
    public static boolean isSplitJoinPaused()
    {
        return ourSplitJoinPaused;
    }

    /**
     * Set renderer updates paused.
     *
     * @param pause Flag indicating if updates should be paused.
     */
    public static void setRendererUpdatesPaused(boolean pause)
    {
        ourRendererUpdatesPaused = pause;
        if (pause)
        {
            LOGGER.info("Renderer updates paused.");
        }
        else
        {
            LOGGER.info("Renderer updates unpaused.");
        }
    }

    /**
     * Set the splitJoinPaused.
     *
     * @param pause the splitJoinPaused to set
     */
    public static void setSplitJoinPaused(boolean pause)
    {
        ourSplitJoinPaused = pause;
        if (pause)
        {
            LOGGER.info("Tile splitting and joining paused.");
        }
        else
        {
            LOGGER.info("Tile splitting and joining unpaused.");
        }
    }

    /** Toggle pausing the renderer updates. */
    public static void toggleRendererUpdatesPaused()
    {
        setRendererUpdatesPaused(!ourRendererUpdatesPaused);
    }

    /** Toggle whether splitting and joining is allowed. */
    public static void toggleSplitJoinPaused()
    {
        setSplitJoinPaused(!ourSplitJoinPaused);
    }

    /**
     * Construct the processor.
     *
     * @param geometryType The concrete type of geometry handled by this
     *            processor.
     * @param builder The builder for the processor.
     * @param renderer The renderer for the geometries handled by this
     *            processor.
     */
    public AbstractProcessor(Class<?> geometryType, ProcessorBuilder builder, GeometryRenderer<E> renderer)
    {
        this(geometryType, builder, renderer, 100);
    }

    /**
     * Construct the processor.
     *
     * @param geometryType The concrete type of geometry handled by this
     *            processor.
     * @param builder The builder for the processor.
     * @param renderer The renderer for the geometries handled by this
     *            processor.
     * @param executorObjectCountThreshold The number of geometries in a state
     *            transition that cause the high volume executor to be engaged.
     */
    public AbstractProcessor(Class<?> geometryType, ProcessorBuilder builder, GeometryRenderer<E> renderer,
            int executorObjectCountThreshold)
    {
        Utilities.checkNull(geometryType, "geometryType");

        CacheProvider cache = builder.getCache();
        Utilities.checkNull(cache, "cache");

        MapContext<?> mapContext = builder.getMapContext();
        Utilities.checkNull(mapContext, "mapContext");

        ExecutorService executorService = builder.getExecutorService();
        Utilities.checkNull(executorService, "executorService");

        ScheduledExecutorService scheduledExecutorService = builder.getScheduledExecutorService();
        Utilities.checkNull(scheduledExecutorService, "scheduledExecutorService");

        Executor loadSensitiveExecutor = builder.getLoadSensitiveExecutor();
        Utilities.checkNull(loadSensitiveExecutor, "loadSensitiveExecutor");

        PickManager pickManager = builder.getPickManager();
        Utilities.checkNull(pickManager, "pickManager");

        Utilities.checkNull(renderer, "renderer");

        RepaintListener repaintListener = builder.getRepaintListener();
        Utilities.checkNull(repaintListener, "repaintListener");

        TimeManager timeManager = builder.getTimeManager();
        Utilities.checkNull(timeManager, "timeManager");

        myProjectionSyncManager = builder.getProjectionSyncManager();

        myExecutorService = executorService;

        myNonGLScheduledExecutor = scheduledExecutorService;
        myOnscreenDeterminationExecutor = new ProcrastinatingExecutor(builder.getFixedPoolExecutorService());

        myGeometryType = geometryType;
        myCache = cache;
        myPickManager = pickManager;
        myRenderer = renderer;
        myRepaintListener = repaintListener;
        myTimeManager = timeManager;

        myStateMachine = createStateMachine(executorService, loadSensitiveExecutor, executorObjectCountThreshold);

        myMapContext = mapContext;

        myListenerHelper = new AbstractProcessorListenerHelperImpl(myTimeManager, builder.getScheduledExecutorService(),
                myRenderer);
        myDirtyExecutor = new ProcrastinatingExecutor(builder.getFixedPoolExecutorService());

        myPositionConverter = new PositionConverter(mapContext);

        myConstraintChecker = new ConstraintChecker(builder.getPositiveConstraints(), builder.getNegativeConstraints());

        myPickManagerGeometryRemover = new PickManagerGeometryRemover(builder.getPickManager(),
                builder.getScheduledExecutorService());
    }

    @Override
    public boolean allGeometriesReady()
    {
        int all = myGeometries.size();
        int ready;
        synchronized (myReadyGeometries)
        {
            ready = myReadyGeometries.size();
        }

        return all == ready;
    }

    @Override
    public void close()
    {
        synchronized (myGeometries)
        {
            receiveObjects(this, Collections.<Geometry>emptyList(), getStateMachine().getAllObjects());
            myClosed = true;
        }
        getRenderer().close();
        getStateMachine().stop();
        myListenerHelper.close();
    }

    /**
     * Get the collection of geometries currently handled by this processor.
     *
     * @return The geometries.
     */
    @Override
    public Collection<E> getGeometries()
    {
        Collection<E> result;
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
     * Get the model data for a geometry. Attempt to retrieve the data from the
     * cache first, and if it isn't in the cache, generate the data and store it
     * in the cache. If {@code override} is not {@code null}, use its values to
     * construct the result.
     *
     * @param geom The geometry.
     * @param projectionSnapshot If non-null this snapshot will be used when
     *            creating model data.
     * @param override Optional override that may be used to supply part of the
     *            model data.
     * @param timeBudget The time budget for getting the model data.
     * @return The model data.
     */
    public AbstractRenderer.ModelData getModelData(E geom, Projection projectionSnapshot, AbstractRenderer.ModelData override,
            TimeBudget timeBudget)
    {
        Lock readLock = getProjectionChangeLock().readLock();
        readLock.lock();
        try
        {
            AbstractRenderer.ModelData data;
            AbstractRenderer.ModelData cachedData = getCachedData(geom, null);
            if (cachedData == null)
            {
                data = processGeometry(geom, projectionSnapshot, override, timeBudget);
                if (data != null)
                {
                    cacheData(geom, data);
                    setOnscreenDirty();
                }
            }
            else
            {
                data = cachedData;
            }

            return data;
        }
        finally
        {
            readLock.unlock();
        }
    }

    /**
     * Accessor for the pickManager.
     *
     * @return the pickManager
     */
    public PickManager getPickManager()
    {
        return myPickManager;
    }

    @Override
    public Class<? extends Position> getPositionType()
    {
        return myPositionTypeRef.get();
    }

    /**
     * Get the projectionSnapshot.
     *
     * @return the projectionSnapshot
     */
    public Projection getProjectionSnapshot()
    {
        if (myProjectionSnapshot != null)
        {
            return myProjectionSnapshot;
        }

        if (myMapContext != null)
        {
            if (sensitiveToProjectionChanges())
            {
                return myMapContext.getProjection();
            }
            // Use the raw projection so that copies of snapshots are not
            // saved in the render data for renderers which are not
            // projection sensitive.
            return myMapContext.getRawProjection();
        }

        return null;
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public void handleProjectionChanged(ProjectionChangedEvent evt)
    {
        if (!sensitiveToProjectionChanges())
        {
            setProjectionSnapshot(null);
            return;
        }
        Lock lock = myProjectionChangeLock.writeLock();
        lock.lock();
        try
        {
            setProjectionSnapshot(evt.getProjectionSnapshot());

            // Trigger the geometries to be reprocessed.
            myRenderer.handleProjectionChanged(evt);

            // TODO do not reset all geometries, just the ones in the changed
            // region.
            // This also needs some refactoring so that we can better control
            // which geometries get cleared.
            if (evt.isFullClear())
            {
                // Synchronize on myGeometries so that new geometries
                // cannot be added to the ready collection until after it has
                // been cleared.
                synchronized (myGeometries)
                {
                    resetStateDueToProjectionChange();
                    synchronized (myReadyGeometries)
                    {
                        myReadyGeometries.clear();
                    }
                }
            }
            else
            {
                resetStateDueToProjectionChange();
            }

            // Set the projection to dirty. This needs to happen
            // so that when replaceOnScreen get called for the new
            // projection, it will be sure to pre-render.
            myProjectionDirty = true;

            // If this is a full projection change, clear the screen
            // until the new model coordinates can be calculated. If there are
            // no ready geometries which are not deferred, force on screen
            // determination so that the renderer will report that it is ready
            // to switch to the new projection.
            boolean determinationRequired = false;
            if (evt.isFullClear() || myReadyGeometries.isEmpty())
            {
                determinationRequired = true;
            }
            else
            {
                // If there is at least one reading geometry which is not
                // deferred, then do not force on screen determination.
                Collection<E> deferred = getStateMachine().getObjectsInState(State.DEFERRED, New.<E>setFactory());
                if (myReadyGeometries.size() <= deferred.size())
                {
                    for (E geom : myReadyGeometries)
                    {
                        if (!deferred.contains(geom))
                        {
                            break;
                        }
                    }
                    determinationRequired = true;
                }
            }

            if (determinationRequired)
            {
                determineOnscreen();
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public boolean handlesType(Class<? extends E> type)
    {
        return myGeometryType.isAssignableFrom(type) && myRenderer.getType().isAssignableFrom(type);
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

    @Override
    public boolean isViable(RenderContext rc, Collection<String> warnings)
    {
        return true;
    }

    @Override
    public boolean needsRender(AbstractGeometry.RenderMode mode)
    {
        return mode == AbstractGeometry.RenderMode.DRAW ? !getOnscreenDrawableGeometries().isEmpty()
                : !getOnscreenPickableGeometries().isEmpty();
    }

    @Override
    public void notifyWhenReady(Runnable task)
    {
        synchronized (myReadyObservers)
        {
            if (allGeometriesReady())
            {
                task.run();
            }
            else
            {
                myReadyObservers.add(task);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void receiveObjects(Object source, Collection<? extends Geometry> adds, Collection<? extends Geometry> removes)
    {
        if (isClosed())
        {
            return;
        }
        boolean changed;
        Collection<? extends Geometry> news;
        Set<E> toRemove = New.set();
        synchronized (myGeometries)
        {
            manageGeometryReplacement(adds, removes, toRemove);

            // Optimization for the common case that all geometries are being
            // removed.
            if (toRemove.size() > 1 && myGeometries.size() == toRemove.size() && myGeometries.containsAll(toRemove))
            {
                changed = true;
                myGeometries.clear();
                if (!adds.isEmpty())
                {
                    myGeometries.addAll((Collection<? extends E>)adds);
                }
                news = adds;
            }
            else
            {
                news = removeKnownGeometries(adds);
                changed = (!myGeometries.isEmpty() && myGeometries.removeAll(toRemove))
                        | (!news.isEmpty() && myGeometries.addAll((Collection<? extends E>)news));
            }
        }

        if (changed)
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("receiveObjects source [" + source + "] adds: " + adds.size() + " removes: " + removes.size());
            }

            for (Geometry geometry : news)
            {
                if (geometry instanceof ConstrainableGeometry)
                {
                    ConstrainableGeometry constrainable = (ConstrainableGeometry)geometry;
                    if (constrainable.getConstraints() != null)
                    {
                        TimeConstraint timeConstraint = constrainable.getConstraints().getTimeConstraint();
                        if (timeConstraint != null && timeConstraint.isMostRecent())
                        {
                            myHasMostRecent = true;
                            break;
                        }
                    }
                }
            }

            doReceiveObjects(source, (Collection<? extends E>)news, toRemove);
        }
    }

    @Override
    public void render(RenderContext rc)
    {
        Collection<? extends E> geometries;
        if (rc.getRenderMode() == AbstractGeometry.RenderMode.DRAW)
        {
            geometries = getOnscreenDrawableGeometries();
        }
        else if (rc.getRenderMode() == AbstractGeometry.RenderMode.PICK)
        {
            geometries = getOnscreenPickableGeometries();
        }
        else
        {
            throw new UnexpectedEnumException(rc.getRenderMode());
        }

        if (!geometries.isEmpty())
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(
                        "Rendering (" + rc.getRenderMode() + ") " + geometries.size() + " geometries of type " + myGeometryType);
            }
            Collection<E> reloads = New.collection(0);
            myRenderer.render(rc, geometries, reloads, getPickManager(), getMapContext(), getDataRetriever(), true);
            reloadGeometries(reloads);
        }

        myPickManagerGeometryRemover.flush();
    }

    @Override
    public boolean sensitiveToProjectionChanges()
    {
        return GeographicPosition.class.isAssignableFrom(getPositionType());
    }

    @Override
    public void switchToProjection(Projection projectionSnapshot)
    {
        if (sensitiveToProjectionChanges())
        {
            myRenderer.switchToProjection(projectionSnapshot);
            getRepaintListener().repaint();
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(32);
        sb.append(getClass().getSimpleName()).append(" [").append(getGeometrySet().size()).append(" geometries]");
        return sb.toString();
    }

    /**
     * Cache the model data.
     *
     * @param geo The geometry.
     * @param data The model data for the geometry.
     */
    protected abstract void cacheData(E geo, AbstractRenderer.ModelData data);

    /**
     * Clear any cached data for the given geometries.
     *
     * @param geoms The geometries.
     */
    protected abstract void clearCachedData(Collection<? extends Geometry> geoms);

    /**
     * Get all the objects in the {@link State#READY} state and determine which
     * ones are on-screen. Replace the on-screen geometry list with the
     * newly-determined one.
     */
    protected final void determineOnscreen()
    {
        determineOnscreen(false);
    }

    /**
     * Get all the objects in the {@link State#READY} state and determine which
     * ones are on-screen. Replace the on-screen geometry list with the
     * newly-determined one.
     *
     * @param forcePreRender When true, pre-render even if the on screen
     *            geometries have not changed.
     */
    protected final void determineOnscreen(final boolean forcePreRender)
    {
        if (getPositionType() != null)
        {
            myOnscreenDeterminationExecutor.execute(() -> doDetermineOnscreen(forcePreRender));
        }
    }

    /**
     * Do the actual work for determining whether geometries are on screen.
     *
     * @param forcePreRender When true, pre-render even if the on screen
     *            geometries have not changed.
     */
    protected void doDetermineOnscreen(boolean forcePreRender)
    {
        Collection<E> objects = getReadyGeometries();
        List<E> onscreen = filterOnscreen(objects);
        replaceOnscreen(onscreen, forcePreRender);
    }

    /**
     * Handle geometries being added or removed from the processor. This is a
     * hook method that may be overridden by derivative processors. This is
     * called after the geometries have been add/removed from
     * {@link #myGeometries} and is not called if there are no changes.
     *
     * @param source The source of the geometries.
     * @param adds The added geometries.
     * @param removes The removed geometries.
     */
    protected void doReceiveObjects(Object source, Collection<? extends E> adds, Collection<? extends Geometry> removes)
    {
        processRemoves(removes);

        // Update listeners before changing geometry state so that time
        // constraints are properly checked.
        updateListeners(adds, removes);

        if (adds.isEmpty())
        {
            // Without any adds, there's nothing to trigger the on-screen
            // geometries to be replaced.
            determineOnscreen();
        }
        else
        {
            setPositionType(adds);
            resetState(adds, State.UNPROCESSED);
        }

        // If the number of adds is equal to the number of removes, assume
        // this is a geometry exchange and it is safe to discard cached
        // information for the removed geometries.
        if (adds.size() == removes.size())
        {
            clearCachedData(removes);
        }
    }

    /**
     * Determine which of the input objects are on-screen.
     *
     * TODO: the on-screen determination should be moved to a scene graph to
     * avoid so many calculations every time the viewer is moved.
     *
     * @param objects The input objects.
     * @return The on-screen objects.
     */
    @SuppressWarnings("PMD.SimplifiedTernary")
    protected List<E> filterOnscreen(Collection<? extends E> objects)
    {
        Collection<? extends E> objectsAfterMostRecent = myHasMostRecent
                ? myMostRecentFilter.filterMostRecent(objects, getTimeManager(), true) : objects;

        List<E> onscreen = New.list(objectsAfterMostRecent.size());
        for (E geo : objectsAfterMostRecent)
        {
            if (isOnScreen(geo, true))
            {
                onscreen.add(geo);
            }
        }

        Collections.sort(onscreen, RENDER_ORDER_COMPARATOR);

        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Onscreen: " + onscreen);

            Collection<? extends E> offscreen = New.set(objects);
            offscreen.removeAll(onscreen);
            LOGGER.trace("Offscreen: " + offscreen);
        }

        return onscreen;
    }

    /**
     * Accessor for the cache facility.
     *
     * @return The cache facility.
     */
    protected CacheProvider getCache()
    {
        return myCache;
    }

    /**
     * Get the cached data, if any, for a geometry. An override may be provided
     * that may be used by the processor to compose the return model data, to
     * avoid multiple cache hits if the model data has multiple parts and some
     * of the parts are already known.
     *
     * @param geom The geometry.
     * @param override Optional override that may be used to supply part of the
     *            model data.
     * @return The cached data, or <code>null</code>.
     */
    protected abstract AbstractRenderer.ModelData getCachedData(E geom, AbstractRenderer.ModelData override);

    /**
     * Accessor for the constraintChecker.
     *
     * @return The constraintChecker.
     */
    protected ConstraintChecker getConstraintChecker()
    {
        return myConstraintChecker;
    }

    /**
     * Get the facility for retrieving model data for geometries.
     *
     * @return The data retriever.
     */
    protected AbstractRenderer.ModelDataRetriever<E> getDataRetriever()
    {
        return myModelDataRetriever;
    }

    /**
     * Get the executor service for doing background processing.
     *
     * @return The executor.
     */
    protected ExecutorService getExecutorService()
    {
        return myExecutorService;
    }

    /**
     * Get access to the actual geometry set, not a copy.
     *
     * @return The geometries for this processor.
     */
    protected Collection<E> getGeometrySet()
    {
        return myGeometries;
    }

    /**
     * Get the map context.
     *
     * @return The map context.
     */
    protected MapContext<?> getMapContext()
    {
        return myMapContext;
    }

    /**
     * Get the nonGLScheduledExecutor.
     *
     * @return the nonGLScheduledExecutor
     */
    protected ScheduledExecutorService getNonGLScheduledExecutor()
    {
        return myNonGLScheduledExecutor;
    }

    /**
     * Get the currently on-screen drawable geometries.
     *
     * @return The geometries.
     */
    protected Collection<? extends E> getOnscreenDrawableGeometries()
    {
        return myOnscreenDrawableGeometries;
    }

    /**
     * Get the currently on-screen pickable geometries.
     *
     * @return The geometries.
     */
    protected Collection<? extends E> getOnscreenPickableGeometries()
    {
        return myOnscreenPickableGeometries;
    }

    /**
     * Get the pick manager geometry remover.
     *
     * @return The pick manager geometry remover.
     */
    protected final PickManagerGeometryRemover getPickManagerGeometryRemover()
    {
        return myPickManagerGeometryRemover;
    }

    /**
     * Accessor for the positionConverter.
     *
     * @return The positionConverter.
     */
    protected PositionConverter getPositionConverter()
    {
        return myPositionConverter;
    }

    /**
     * Get access to the lock on projection change events.
     *
     * @return The projection change write lock.
     */
    protected ReentrantReadWriteLock getProjectionChangeLock()
    {
        return myProjectionChangeLock;
    }

    /**
     * Get the set of geometries that are ready to be displayed.
     *
     * @return The ready geometries.
     */
    protected Collection<E> getReadyGeometries()
    {
        synchronized (myReadyGeometries)
        {
            return New.set(myReadyGeometries);
        }
    }

    /**
     * Get the renderer.
     *
     * @return The renderer.
     */
    protected GeometryRenderer<E> getRenderer()
    {
        return myRenderer;
    }

    /**
     * Get the repaint listener.
     *
     * @return the repaintListener
     */
    protected RepaintListener getRepaintListener()
    {
        return myRepaintListener;
    }

    /**
     * Get the processing state machine.
     *
     * @return The processing state machine.
     */
    protected ThreadedStateMachine<E> getStateMachine()
    {
        return myStateMachine;
    }

    /**
     * Accessor for the timeManager.
     *
     * @return The timeManager.
     */
    protected TimeManager getTimeManager()
    {
        return myTimeManager;
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
     * Handle constraint changes to geometries which are already in the
     * processor.
     *
     * @param evt The event associated with the changed constraints.
     */
    protected void handleConstraintsChanged(ConstraintsChangedEvent evt)
    {
        determineOnscreen(true);
    }

    /**
     * Handle property changes to geometries which are already in the processor.
     *
     * @param evt The event associated with the changed properties.
     */
    protected void handlePropertyChanged(RenderPropertyChangedEvent evt)
    {
        determineOnscreen(true);
    }

    /**
     * Handle a change to the active time spans.
     */
    protected void handleTimeSpansChanged()
    {
        getConstraintChecker().handleTimeSpansChanged(getTimeManager());
        setCheckingTimeConstraintsNeeded(getConstraintChecker().getTimeConstraintStatus() == TimeConstraintStatus.UNKNOWN);
    }

    /**
     * Call-back for a view changed event. When the view changes, the geometries
     * must be reviewed to determine which ones are on-screen.
     *
     * @param view The viewer.
     * @param type The viewer update type.
     */
    protected void handleViewChanged(final Viewer view, final ViewChangeSupport.ViewChangeType type)
    {
        if (type == ViewChangeSupport.ViewChangeType.VIEW_CHANGE)
        {
            if (sensitiveToViewChanges())
            {
                determineOnscreen();
                getRenderer().handleViewChanged(view, type);
            }
        }
        else if (type == ViewChangeSupport.ViewChangeType.WINDOW_RESIZE && ScreenPosition.class.equals(getPositionType()))
        {
            clearCachedData(getStateMachine().getAllObjects());
            getStateMachine().resetState(State.UNPROCESSED);
        }
    }

    /**
     * Get if my renderer does time filtering.
     *
     * @return {@code true} if I have a time filtering renderer.
     */
    protected boolean hasTimeFilteringRenderer()
    {
        return getRenderer() instanceof TimeFilteringRenderer;
    }

    /**
     * Check to see if the bounding ellipsoid for the geometry is on-screen.
     *
     * @param geom The geometry.
     * @param useTime Indicates if time constraints should be used if
     *            applicable.
     * @param boundingBox The bounds of the geometry.
     * @return {@code true} when the bounding ellipsoid is on screen;
     */
    protected boolean isBoundingEllipsoidOnScreen(E geom, boolean useTime, BoundingBox<? extends Position> boundingBox)
    {
        // TODO This check should be trivial for screen bounding box and it
        // should be easy to get the bounding ellipse for the model bounds.
        if (boundingBox instanceof ScreenBoundingBox || boundingBox instanceof ModelBoundingBox)
        {
            return true;
        }

        if (boundingBox != null)
        {
            GeographicBoundingBox geoBounds = (GeographicBoundingBox)boundingBox;
            CacheProvider cache = getCache();
            Ellipsoid bounds = cache.getCacheAssociation(geom, Ellipsoid.class);
            if (bounds == null)
            {
                // If the bounding box covers more than 90 degrees from corner
                // to corner, assume that it is in view.
                double deltaLonSq = geoBounds.getDeltaLonD() * geoBounds.getDeltaLonD();
                double deltaLatSq = geoBounds.getDeltaLatD() * geoBounds.getDeltaLatD();
                if (Math.sqrt(deltaLonSq + deltaLatSq) > 90.)
                {
                    return true;
                }
                bounds = getProjectionSnapshot().getBoundingEllipsoid(geoBounds, Vector3d.ORIGIN, false);
                // Perform the check to get the size on the ellipsoid right.
                boolean inView = getViewer().isInView(bounds, ELLIPSOID_CULL_COSINE);
                cache.putCacheAssociation(geom, bounds, Ellipsoid.class, bounds.getSizeBytes(), 0L);
                return inView;
            }
            return getViewer().isInView(bounds, ELLIPSOID_CULL_COSINE);
        }
        return true;
    }

    /**
     * Get if checking time constraints is needed.
     *
     * @return {@code true} if checking time constraints is needed.
     */
    protected boolean isCheckingTimeConstraintsNeeded()
    {
        return myCheckingTimeConstraintsNeeded;
    }

    /**
     * Determine if a model point is obscured by the surface.
     *
     * @param model The model point.
     * @return <code>true</code> if the point is obscured.
     */
    protected boolean isObscured(Vector3d model)
    {
        Vector3d eye = getViewer().getPosition().getLocation();
        Vector3d terrainIntersect = getProjectionSnapshot().getTerrainIntersection(new Ray3d(eye, model.subtract(eye)),
                getViewer());

        if (terrainIntersect == null)
        {
            return false;
        }

        double relativeDistance = terrainIntersect.distance(eye) - model.distance(eye);
        return relativeDistance < -2.;
    }

    /**
     * Determine if a geometry is currently on-screen.
     *
     * @param geom The geometry.
     * @param useTime Indicates if time constraints should be used if
     *            applicable.
     * @return <code>true</code> if the geometry is on-screen.
     */
    protected boolean isOnScreen(E geom, boolean useTime)
    {
        return getConstraintChecker().checkConstraints(geom, getMapContext(), getConstraintTimeManager(useTime))
                && !(geom instanceof RenderableGeometry && ((RenderableGeometry)geom).getRenderProperties().isHidden());
    }

    /**
     * Get if the on-screen geometries need to be re-rendered.
     *
     * @return if the on-screen geometries are out of date.
     */
    protected synchronized boolean isOnscreenDirty()
    {
        return myOnscreenDirty;
    }

    /**
     * Notify and clear the ready observers if all geometries are ready.
     */
    protected final void notifyReadyObservers()
    {
        synchronized (myReadyObservers)
        {
            if (!myReadyObservers.isEmpty() && allGeometriesReady())
            {
                myReadyObservers.forEach(t -> t.run());
                myReadyObservers.clear();
            }
        }
    }

    /**
     * Callback from the state machine for geometries in the
     * {@link State#DEFERRED} state.
     *
     * @param unprocessed The objects.
     * @param controller The state controller.
     */
    protected void processDeferred(Collection<? extends E> unprocessed, StateController<E> controller)
    {
        // If geometries which were on screen are now deferred, on screen
        // determination must occur.
        determineOnscreen();
    }

    /**
     * Process geometries; determine the model coordinates and cache them.
     *
     * @param unprocessed The input geometries.
     * @param ready Output collection of geometries that were successfully
     *            processed.
     * @param controller A controller used to initiate further state changes for
     *            the geometries.
     */
    protected void processGeometries(Collection<? extends E> unprocessed, Collection<? super E> ready,
            StateController<E> controller)
    {
        for (E geo : unprocessed)
        {
            AbstractRenderer.ModelData modelData = getModelData(geo, null, null, TimeBudget.INDEFINITE);
            if (modelData == null)
            {
                LOGGER.warn("Processing failed for geometry [" + geo + "]");
            }
            else
            {
                ready.add(geo);
            }
        }
    }

    /**
     * Process a single geometry to produce a model. If {@code override} is not
     * {@code null}, use any data it has, rather than calculating it.
     *
     * @param geo The geometry.
     * @param projectionSnapshot If non-null this snapshot will be used when
     *            creating model data.
     * @param override Optional override that may be used to supply part of the
     *            model data.
     * @param timeBudget The time budget for getting the model data. This may be
     *            ignored, at the discretion of the implementation.
     * @return The model object.
     */
    protected abstract AbstractRenderer.ModelData processGeometry(E geo, Projection projectionSnapshot,
            AbstractRenderer.ModelData override, TimeBudget timeBudget);

    /**
     * Callback from the state machine for geometries in the {@link State#READY}
     * state. Add the objects to the ready collection and determine the new
     * on-screen geometries.
     *
     * @param ready The objects.
     * @param controller The state controller.
     */
    protected void processReady(Collection<? extends E> ready, StateController<E> controller)
    {
        // Hold the geometries constant while moving geometries into the ready
        // collection to avoid putting a geometry into the ready collection that
        // has been removed from the processor in the midst of this method.
        List<E> remainingReady = New.list(ready.size());
        final Collection<E> needsRemoval = New.set();
        synchronized (myGeometries)
        {
            // Ensure that the geometries coming out of processing have not been
            // removed.
            for (E geom : ready)
            {
                Geometry top;
                if (geom instanceof HierarchicalGeometry)
                {
                    top = ((HierarchicalGeometry<?>)geom).getTopAncestor();
                }
                else
                {
                    top = geom;
                }
                if (myGeometries.contains(top))
                {
                    Collection<E> pendingRemoval = myPendingReplacement.remove(geom);
                    if (pendingRemoval != null)
                    {
                        for (E pending : pendingRemoval)
                        {
                            Collection<E> p2 = myPendingReplacement.remove(pending);
                            if (p2 != null)
                            {
                                needsRemoval.addAll(p2);
                            }
                        }
                        needsRemoval.addAll(pendingRemoval);
                    }
                    remainingReady.add(geom);
                }
            }

            synchronized (myReadyGeometries)
            {
                myReadyGeometries.addAll(remainingReady);
            }
        }

        notifyReadyObservers();
        determineOnscreen();

        if (!needsRemoval.isEmpty())
        {
            // Take this off of the current thread to allow ready geometries to
            // be rendered more quickly.
            ThreadUtilities.runBackground(() -> receiveObjects(this, Collections.<E>emptyList(), needsRemoval));
        }
    }

    /**
     * Remove geometries from the processor.
     *
     * @param removes The set of removes.
     */
    protected void processRemoves(Collection<? extends Geometry> removes)
    {
        if (removes != null && !removes.isEmpty())
        {
            getStateMachine().removeFromState(removes);
            removeFromReady(removes);
            myPickManagerGeometryRemover.addAll(removes);
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
    protected void processUnprocessed(Collection<? extends E> unprocessed, StateController<E> controller)
    {
        Collection<E> ready = New.collection();
        processGeometries(unprocessed, ready, controller);
        if (ready.isEmpty())
        {
            determineOnscreen();
        }
        else
        {
            controller.changeState(ready, State.READY);
        }
    }

    /**
     * Move geometries to the {@link State#UNPROCESSED} state so they will be
     * reprocessed.
     *
     * @param reloads The geometries.
     */
    protected void reloadGeometries(Collection<? extends E> reloads)
    {
        if (!reloads.isEmpty())
        {
            removeFromReady(reloads);
            resetState(reloads, State.UNPROCESSED);
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
            CollectionUtilities.removeAll(myReadyGeometries, notReady);
        }
    }

    /**
     * Return the geometries from the input collection that are not in
     * {@link #myGeometries}.
     *
     * @param geoms The geometries.
     * @return The new geometries.
     */
    protected Collection<? extends Geometry> removeKnownGeometries(Collection<? extends Geometry> geoms)
    {
        synchronized (myGeometries)
        {
            Collection<? extends Geometry> news;
            if (myGeometries.isEmpty())
            {
                news = geoms;
            }
            else
            {
                news = New.set(geoms);
                news.removeAll(myGeometries);
            }
            return news;
        }
    }

    /**
     * Replace the on-screen geometries with a new collection. When a projection
     * change has occurred, it is required that pre rendering be done in order
     * for the projection to become ready.
     *
     * @param onscreen The new on-screen geometries.
     * @param forcePreRender When true, preRender even if the on screen
     *            geometries hasn't changed.
     */
    protected final synchronized void replaceOnscreen(List<E> onscreen, boolean forcePreRender)
    {
        if (ourRendererUpdatesPaused)
        {
            return;
        }

        // Determine which geometries are pickable and which are drawable.
        Collection<E> drawable = New.collection(onscreen.size());
        Collection<E> pickable = New.collection(onscreen.size());
        for (E geom : onscreen)
        {
            /* TODO When both of these ifs were incorrectly set to be true all
             * of the time, this caused the compass to not keep up with the
             * viewer moves. */
            if (!(geom.getRenderProperties() instanceof BaseRenderProperties)
                    || ((BaseRenderProperties)geom.getRenderProperties()).isDrawable())
            {
                drawable.add(geom);
            }
            if (!(geom.getRenderProperties() instanceof BaseRenderProperties)
                    || ((BaseRenderProperties)geom.getRenderProperties()).isPickable())
            {
                pickable.add(geom);
            }
        }
        CollectionUtilities.trimToSize(drawable);
        CollectionUtilities.trimToSize(pickable);

        // Lock the projection to keep it constant during preRender.
        Lock readLock = myProjectionChangeLock.readLock();
        readLock.lock();
        try
        {
            /* Determine if the set of on-screen geometries has changed. If not,
             * there's no need to call preRender, unless the projection has
             * changed. To handle that, the projection handler clears the
             * on-screen geometries, to force them to be different. */
            Projection projection = getProjectionSnapshot();
            if (isOnscreenDirty() || myProjectionDirty || forcePreRender || drawable.size() != myOnscreenDrawableGeometries.size()
                    || pickable.size() != myOnscreenPickableGeometries.size() || !drawable.equals(myOnscreenDrawableGeometries)
                    || !pickable.equals(myOnscreenPickableGeometries))
            {
                /* If the projection synchronization manager has a newer
                 * projection, then this one is already out of date, so don't
                 * pre-render. */
                if (!sensitiveToProjectionChanges() || myProjectionSyncManager.isProjectionCurrent(projection))
                {
                    myRenderer.preRender(onscreen, drawable, pickable, getPickManager(), getDataRetriever(), projection);
                    myProjectionDirty = false;
                    // Replace the geometries after preRender is called to be
                    // sure the necessary data are generated.
                    myOnscreenDrawableGeometries = drawable;
                    myOnscreenPickableGeometries = pickable;
                }

                myOnscreenDirty = false;
            }
        }
        catch (RuntimeException e)
        {
            LOGGER.error("Exception during preRender: " + e, e);
            throw e;
        }
        finally
        {
            readLock.unlock();
        }

        // Always repaint because the view may have changed.
        myRepaintListener.repaint();

        // At this point the geometries that have been added to the pick manager
        // geometry remover should not be in the ready collection, so it should
        // be safe to remove them after the next render loop.
        myPickManagerGeometryRemover.gather();
    }

    /**
     * Reset the state of some geometries in the state machine.
     *
     * @param objects The geometries to reset.
     * @param toState The state to reset the geometries to.
     */
    protected void resetState(Collection<? extends E> objects, ThreadedStateMachine.State toState)
    {
        getStateMachine().resetState(objects, toState);
    }

    /**
     * Reset geometry states due to a projection change. This is overridable to
     * allow subclasses to change the target state.
     */
    protected void resetStateDueToProjectionChange()
    {
        getStateMachine().resetState(State.UNPROCESSED);
    }

    /**
     * Get if this processor cares about view changes.
     *
     * @return {@code true} if this processor cares about view changes.
     */
    protected boolean sensitiveToViewChanges()
    {
        return !ScreenPosition.class.isAssignableFrom(getPositionType());
    }

    /**
     * Set if checking time constraints is needed.
     *
     * @param checkingTimeConstraintsNeeded Indicates if checking time
     *            constraints is needed.
     */
    protected void setCheckingTimeConstraintsNeeded(boolean checkingTimeConstraintsNeeded)
    {
        /* If the renderer does time filtering, leave the processor flag alone,
         * but enable/disable the renderer's time filtering based on the input
         * flag. */
        if (hasTimeFilteringRenderer())
        {
            if (checkingTimeConstraintsNeeded)
            {
                boolean forcePreRender = ((TimeFilteringRenderer)getRenderer()).setGroupInterval(
                        getConstraintChecker().getPositiveGroupTimeConstraint().getTimeSpan()) || hasMostRecentGeometries();
                ((TimeFilteringRenderer)getRenderer()).setTimeManager(getTimeManager());
                if (forcePreRender)
                {
                    /* Force pre-render because even though the on-screen
                     * geometries have not changed, the renderer needs to upload
                     * the new time information. */
                    determineOnscreen(true);
                }
            }
            else
            {
                ((TimeFilteringRenderer)getRenderer()).setTimeManager((TimeManager)null);
            }
        }
        else
        {
            myCheckingTimeConstraintsNeeded = checkingTimeConstraintsNeeded;
            if (getConstraintChecker().pollOnscreenDirty())
            {
                determineOnscreen(false);
            }
        }
    }

    /**
     * Set the on-screen dirty flag. This executes in the background to avoid a
     * dead-lock condition waiting for the processor lock.
     */
    protected void setOnscreenDirty()
    {
        myDirtyExecutor.execute(() -> setOnscreenDirtyImmediately());
    }

    /** Set the on-screen dirty flag. */
    protected void setOnscreenDirtyImmediately()
    {
        synchronized (this)
        {
            myOnscreenDirty = true;
        }
    }

    /**
     * Set the position type of the processor.
     *
     * @param adds The geometries being added.
     */
    protected void setPositionType(Collection<? extends Geometry> adds)
    {
        Class<? extends Position> positionType = adds.iterator().next().getPositionType();
        if (!myPositionTypeRef.compareAndSet(null, positionType))
        {
            /* The position type is not null, so check that the new geometries
             * have the same position type as the old ones, or that there are no
             * old geometries. */
            synchronized (myGeometries)
            {
                if (myPositionTypeRef.get() != positionType)
                {
                    if (myGeometries.size() != adds.size() || !myGeometries.containsAll(adds))
                    {
                        throw new IllegalStateException(
                                "Cannot have geometries with different position types in the same processor. Geometries are: "
                                        + myGeometries);
                    }
                    myPositionTypeRef.set(positionType);
                }
            }
        }
    }

    /**
     * Set the projectionSnapshot.
     *
     * @param projectionSnapshot the projectionSnapshot to set
     */
    protected void setProjectionSnapshot(Projection projectionSnapshot)
    {
        myProjectionSnapshot = projectionSnapshot;
    }

    /**
     * Update listeners for changes to the geometries.
     *
     * @param adds The added geometries.
     * @param removes The removed geometries.
     */
    protected void updateListeners(Collection<? extends Geometry> adds, Collection<? extends Geometry> removes)
    {
        myListenerHelper.updateListeners(adds, removes);

        if (myListenerHelper.isCheckingTimeConstraintsNeeded())
        {
            handleTimeSpansChanged();
        }
        else
        {
            getConstraintChecker().setTimeConstraintStatus(TimeConstraintStatus.ALL_SATISFIED);
        }
    }

    /**
     * Create the state machine.
     *
     * @param highVolumeExecutor The executor for the state machine for high
     *            volumes of objects.
     * @param lowVolumeExecutor The executor for the state machine for low
     *            volumes of objects.
     * @param executorObjectCountThreshold The number of objects that triggers
     *            the high-volume executor to be used by the state machine.
     * @return The state machine.
     */
    private ThreadedStateMachine<E> createStateMachine(Executor highVolumeExecutor, Executor lowVolumeExecutor,
            int executorObjectCountThreshold)
    {
        ThreadedStateMachine<E> tsm = new ThreadedStateMachine<>(EnumSet.of(State.DEFERRED));
        EnumSet<State> handledStates = EnumSet.allOf(State.class);
        tsm.registerStateChangeHandler(handledStates, myStateChangeHandler, highVolumeExecutor, lowVolumeExecutor,
                executorObjectCountThreshold);

        return tsm;
    }

    /**
     * Gets the time manager or null if time constraints should not be checked.
     *
     * @param useTime Indicates if time should be checked.
     * @return The time manager or null if time constraints should not be
     *         checked at this time.
     */
    private TimeManager getConstraintTimeManager(boolean useTime)
    {
        return useTime && isCheckingTimeConstraintsNeeded() ? getTimeManager() : null;
    }

    /**
     * Checks to see if there are most recent constrained geometries.
     *
     * @return True if there are false otherwise.
     */
    private boolean hasMostRecentGeometries()
    {
        return myHasMostRecent;
    }

    /**
     * Determine whether a geometry is being replace and manage the tracking of
     * and removal of replacements as necessary.
     *
     * @param adds The geometries which are being added to the processor.
     * @param removes The geometries which are being requested to be removed
     *            from the processor.
     * @param toRemove The geometries which are actually being removed. This
     *            collection may contain addition geometries which are no longer
     *            required because of replacement and may or may not include
     *            items in the removes collection depending on whether they
     *            replace another geometry.
     */
    @SuppressWarnings("unchecked")
    private void manageGeometryReplacement(Collection<? extends Geometry> adds, Collection<? extends Geometry> removes,
            Set<E> toRemove)
    {
        /* If there is exactly one add and one remove, then assume that the add
         * is a replacement for the remove. Mark the existing one for removal
         * and remove later when the replacement is ready. */
        if (adds.size() == 1 && removes.size() == 1)
        {
            E replace = (E)removes.iterator().next();
            E replaceWith = (E)adds.iterator().next();

            Collection<E> replaceAncestors = myPendingReplacement.get(replace);
            if (replaceAncestors != null && replaceAncestors.contains(replaceWith))
            {
                // This is means that this add eventually replaces itself.
                // In this case, clear the chain and just add as normal.
                toRemove.addAll(replaceAncestors);
                toRemove.addAll((Collection<? extends E>)removes);
            }
            else
            {
                Collection<E> replaceCol = New.collection(1);
                replaceCol.add(replace);
                if (replaceAncestors != null)
                {
                    replaceCol.addAll(replaceAncestors);
                }

                myPendingReplacement.put(replaceWith, replaceCol);
            }
        }
        else
        {
            toRemove.addAll((Collection<? extends E>)removes);
        }

        /* Remove any geometries which are being replaced by a geometry which is
         * being removed. */
        Set<E> additionalRemoves = New.set();
        for (E remove : toRemove)
        {
            Collection<E> replace = myPendingReplacement.remove(remove);
            if (replace != null)
            {
                additionalRemoves.addAll(replace);
            }
        }
        toRemove.addAll(additionalRemoves);

        for (E additional : additionalRemoves)
        {
            myPendingReplacement.remove(additional);
        }
    }

    /** The states used by this processor in the processing state machine. */
    protected enum State implements ThreadedStateMachine.State
    {
        /**
         * State for geometries which may not be ready, but do not currently
         * need processing.
         */
        DEFERRED(200),

        /** State for geometries that have been processed. */
        READY(300),

        /** State for geometries that have not been processed. */
        UNPROCESSED(100),

        ;

        /** The order of the state. */
        private final int myStateOrder;

        /**
         * Construct a state.
         *
         * @param order The order of the state.
         */
        State(int order)
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
     * The listener helper.
     */
    private final class AbstractProcessorListenerHelperImpl extends AbstractProcessorListenerHelper
    {
        /**
         * Constructor.
         *
         * @param timeManager The time manager.
         * @param scheduledExecutorService The scheduled executor service for
         *            the listeners.
         * @param renderer The renderer associated with the processor this
         *            helper helps.
         */
        public AbstractProcessorListenerHelperImpl(TimeManager timeManager, ScheduledExecutorService scheduledExecutorService,
                GeometryRenderer<?> renderer)
        {
            super(timeManager, scheduledExecutorService, renderer);
        }

        @Override
        protected Collection<? extends Geometry> getAllObjects()
        {
            return getStateMachine().getAllObjects();
        }

        @Override
        protected String getProcessorDescription()
        {
            return AbstractProcessor.this.toString();
        }

        @Override
        protected void handleConstraintsChanged(ConstraintsChangedEvent evt)
        {
            AbstractProcessor.this.handleConstraintsChanged(evt);
        }

        @Override
        protected void handleProjectionReady(Projection projection)
        {
            if (sensitiveToProjectionChanges())
            {
                myProjectionSyncManager.projectionReady(AbstractProcessor.this, projection);
            }
        }

        @Override
        protected void handlePropertyChanged(RenderPropertyChangedEvent evt)
        {
            AbstractProcessor.this.handlePropertyChanged(evt);
        }

        @Override
        protected void handleTimeSpansChanged()
        {
            AbstractProcessor.this.handleTimeSpansChanged();
        }

        @Override
        protected boolean isClosed()
        {
            return AbstractProcessor.this.isClosed();
        }
    }
}
