package io.opensphere.core.pipeline.processor;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Phaser;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.media.opengl.GL;

import org.apache.log4j.Logger;

import io.opensphere.core.AnimationChangeAdapter;
import io.opensphere.core.AnimationManager;
import io.opensphere.core.TimeManager;
import io.opensphere.core.TimeManager.ActiveTimeSpanChangeListener;
import io.opensphere.core.TimeManager.ActiveTimeSpans;
import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.animation.AnimationState;
import io.opensphere.core.geometry.ConstrainableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.StrictTimeConstraint;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanArrayList;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.pipeline.util.GLUtilities;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.pipeline.util.RepaintListener;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.LazyMap;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeType;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.MapContext;
import net.jcip.annotations.GuardedBy;

/**
 * Organize and group geometries to send to the various assorted processors.
 */
@SuppressWarnings("PMD.GodClass")
public class GeometryDistributor
{
    /**
     * A scale factor that is used to create a variable depth offset for each
     * polygon.
     */
    protected static final float POLYGON_OFFSET_FACTOR;

    /**
     * The units offset is multiplied by an implementation-specific value to
     * create a constant depth offset.
     */
    protected static final float POLYGON_OFFSET_UNITS;

    /**
     * The units offset is multiplied by an AMD card specific value to create a
     * constant depth offset.
     */
    protected static final float POLYGON_OFFSET_UNITS_ATI;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GeometryDistributor.class);

    /**
     * The scale of separation for the offset values as the z-order increases.
     */
    private static final float POLYGON_OFFSET_SCALE = 20f;

    /**
     * The offset units for polygon depth buffer.
     */
    private float myOffSetUnits;

    /**
     * Listener for changes to the current active time spans. This information
     * is used to manage which geometries should be processed at any given time.
     */
    private final ActiveTimeSpanChangeListener myActiveTimeSpanChangeListener = new ActiveTimeSpanChangeListener()
    {
        @Override
        public synchronized void activeTimeSpansChanged(ActiveTimeSpans newActive)
        {
            myUnprocessedGeometryLock.lock();
            try
            {
                final ActiveTimeSpans oldActive = myActiveTimeSpans;
                myActiveTimeSpans = newActive;
                myPendingActiveTimeSpans = newActive;

                boolean setTimedToUnprocessed;
                final Collection<TimeSpan> newSecondary = New.collection();
                myProcessorsLock.readLock().lock();
                try
                {
                    // If there is no animation plan, then the only timed
                    // processors should be the ones that correspond with the
                    // active time spans. So if the covered primary time spans
                    // contain time spans that are not currently active, the
                    // processors for those times need to be removed.
                    setTimedToUnprocessed = myAnimationPlan == null
                            && !newActive.getPrimary().containsAll(myCoveredPrimaryTimeSpans);

                    // If there are any new secondary time spans, redistribute
                    // so that processors are created for the new time spans.
                    if (!newActive.getSecondary().isEmpty())
                    {
                        for (final Map.Entry<? extends Object, ? extends Collection<? extends TimeSpan>> secondaryEntry : newActive
                                .getSecondary().entrySet())
                        {
                            if (!secondaryEntry.getValue().isEmpty())
                            {
                                final Collection<? extends TimeSpan> covered = oldActive.getSecondary()
                                        .get(secondaryEntry.getKey());
                                if (covered == null || covered.size() != secondaryEntry.getValue().size()
                                        || !covered.containsAll(secondaryEntry.getValue()))
                                {
                                    setTimedToUnprocessed = true;
                                    newSecondary.addAll(secondaryEntry.getValue());
                                }
                            }
                        }
                    }
                    // If any secondary time spans were removed, redistribute so
                    // that the old processors are removed.
                    if (!setTimedToUnprocessed && !myCoveredSecondaryTimeSpans.isEmpty())
                    {
                        for (final Object constraintKey : myCoveredSecondaryTimeSpans.keySet())
                        {
                            final Collection<? extends TimeSpan> secondary = newActive.getSecondary().get(constraintKey);
                            if (secondary == null || secondary.isEmpty())
                            {
                                setTimedToUnprocessed = true;
                                break;
                            }
                        }
                    }
                }
                finally
                {
                    myProcessorsLock.readLock().unlock();
                }

                if (setTimedToUnprocessed)
                {
                    setTimedToUnprocessed(new TimeSpanArrayList(newSecondary), newActive);
                }

                if (!myUnprocessedAdds.isEmpty()
                        && (setTimedToUnprocessed || !myCoveredPrimaryTimeSpans.covers(newActive.getPrimary())))
                {
                    processUnprocessed();
                }
            }
            finally
            {
                myUnprocessedGeometryLock.unlock();
            }

            myRepaintListener.repaint();
        }
    };

    /**
     * The current active time spans (indicating which processors should be
     * rendered). This is volatile to allow unsynchronized read access, but
     * {@link #myUnprocessedGeometryLock} should be locked when this is changed.
     */
    private volatile ActiveTimeSpans myActiveTimeSpans;

    /**
     * This is used to avoid a deadlock between the TimeManager monitor and
     * {@link #myUnprocessedGeometryLock}. This should always be equal to
     * {@link #myActiveTimeSpans} except when the time listener is not active
     * and geometries are being updated. In this case, this will be a reference
     * to the active time spans just before the geometry update begins.
     */
    private volatile ActiveTimeSpans myPendingActiveTimeSpans;

    /**
     * Listener for changes to the animation plan and current plan times. This
     * information is used to manage which geometries should be processed at any
     * given time.
     */
    private final AnimationChangeAdapter myAnimationListener = new AnimationChangeAdapter()
    {
        @Override
        public void animationPlanCancelled()
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("AnimationPlan cancelled");
            }

            // Do not set timed to unprocessed here, since another plan may be
            // about to be established that has the same coverage as the current
            // plan.
        }

        @Override
        public void animationPlanEstablished(AnimationPlan plan)
        {
            ThreadUtilities.runCpu(() -> handleAnimationPlanEstablished(plan));
        }

        /**
         * Handles when an animation plan is established.
         *
         * @param plan the plan
         */
        private void handleAnimationPlanEstablished(AnimationPlan plan)
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("AnimationPlan established: " + plan);
            }
            myUnprocessedGeometryLock.lock();
            try
            {
                if (myAnimationPlan != null && myAnimationPlan.getTimeCoverage().equals(plan.getTimeCoverage()))
                {
                    return;
                }

                myCurrentAnimationState = myAnimationManager.getAnimationState();
                myAnimationPlan = plan;
                final ActiveTimeSpans activeTimeSpans = myActiveTimeSpans;
                setTimedToUnprocessed(TimeSpanList.emptyList(), activeTimeSpans);
                if (!myUnprocessedAdds.isEmpty())
                {
                    processUnprocessed();
                }
            }
            finally
            {
                myUnprocessedGeometryLock.unlock();
            }
        }

        @Override
        public void animationStepChanged(AnimationState currentState)
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Animation step changed: " + currentState);
            }

            myCurrentAnimationState = currentState;
            handleStepChange();
        }

        @Override
        public boolean prepare(AnimationState changeInfo, Phaser phaser)
        {
            mySpecializedProcessorsLock.readLock().lock();
            try
            {
                final Phaser processorPhaser = new Phaser(phaser, myRenderableGeometryProcessors.size());
                final ActiveTimeSpans activeTimeSpans = myActiveTimeSpans;

                for (final Entry<ProcessorDistributionKey, RenderableGeometryProcessor<? extends Geometry>> entry : myRenderableGeometryProcessors
                        .entrySet())
                {
                    final ProcessorDistributionKey key = entry.getKey();
                    final RenderableGeometryProcessor<? extends Geometry> processor = entry.getValue();
                    if (!myIsDrawEnabled || key.getTimeSpan().isTimeless()
                            || !checkTime(activeTimeSpans, key.getTimeSpan(), key.getConstraintKey()))
                    {
                        processorPhaser.arrive();
                    }
                    else if (processor.getPositionType() == null)
                    {
                        processorPhaser.arrive();
                    }
                    else
                    {
                        processor.notifyWhenReady(() -> processorPhaser.arrive());
                    }
                }
            }
            finally
            {
                mySpecializedProcessorsLock.readLock().unlock();
            }
            return true;
        }
    };

    /** The system animation manager. */
    private final AnimationManager myAnimationManager;

    /** The current animation plan. */
    @GuardedBy("myUnprocessedGeometryLock")
    private AnimationPlan myAnimationPlan;

    /**
     * A list of the primary time spans that are covered by my processor keys.
     */
    private TimeSpanList myCoveredPrimaryTimeSpans;

    /** A map of constraint keys to covered secondary time spans. */
    private final Map<Object, Collection<? extends TimeSpan>> myCoveredSecondaryTimeSpans = Collections
            .synchronizedMap(New.<Object, Collection<? extends TimeSpan>>map());

    /** The current animation state. */
    private volatile AnimationState myCurrentAnimationState;

    /**
     * The currently active geometry processors. These need not be in order, so
     * use a hash map.
     */
    @GuardedBy("myProcessorsLock")
    private final Map<ProcessorDistributionKey, GeometryProcessor<? extends Geometry>> myGeometryProcessorsMap = New.map();

    /** A snapshot of the latest collection of geometry processors. */
    @GuardedBy("myProcessorsLock")
    private volatile Collection<? extends GeometryProcessor<? extends Geometry>> myGeometryProcessorsSnapshot = Collections
            .emptyList();

    /**
     * Geometries which have been sorted by key, but do not need to be processed
     * yet.
     */
    @GuardedBy("myProcessorsLock")
    private final Map<ProcessorDistributionKey, Set<Geometry>> myInactiveGeometries = New.map();

    /**
     * When false do not render any of the processors. When true, render as
     * normal.
     */
    private volatile boolean myIsDrawEnabled = true;

    /** The manager for handling occlusion of labels. */
    private final LabelOcclusionManager myLabelOcclusionManager = new LabelOcclusionManager();

    /**
     * Handler for registering/de-registering and handling of events on behalf
     * of the processors.
     */
    private final GeometryDistributorListenerHelper myListenerHelper;

    /** The builder for the geometry processors. */
    private final ProcessorBuilder myProcessorBuilder;

    /** Lock for updates the animation plan. */
    private final ReadWriteLock myProcessorsLock = new ReentrantReadWriteLock();

    /**
     * The currently active geometry processors which are sensitive to
     * projection changes.
     */
    @GuardedBy("mySpecializedProcessorsLock")
    private final Map<ProcessorDistributionKey, GeometryProcessor<? extends Geometry>> myProjectionSensitiveProcessors = New
            .map();

    /** The manager for synchronizing projection changes in the processors. */
    private final ProjectionSyncManager myProjectionSyncManager = new ProjectionSyncManager();

    /**
     * The currently active geometry processors which are processors that can
     * render. The key implements comparable so that the tree will be iterated
     * in the order of rendering.
     */
    @GuardedBy("mySpecializedProcessorsLock")
    private final Map<ProcessorDistributionKey, RenderableGeometryProcessor<? extends Geometry>> myRenderableGeometryProcessors = New
            .naturalOrderMap();

    /** The pipeline repaint listener. */
    private final RepaintListener myRepaintListener;

    /**
     * Lock for managing concurrent access to myRenderableGeometryProcessors and
     * myProjectionSensitiveProcessors. If this lock and myProcessorsLock must
     * be locked at the same time, myProcessorsLock must be locked first.
     */
    private final ReadWriteLock mySpecializedProcessorsLock = new ReentrantReadWriteLock();

    /** Indicates if any of my geometries care about time. */
    @GuardedBy("myUnprocessedGeometriesLock")
    private boolean myTimeAgnostic = true;

    /** The system time manager. */
    private final TimeManager myTimeManager;

    /**
     * Any time there are geometries which are time constrained, but there is no
     * animation plan for determining the processor keys, the geometries must be
     * stored until the animation plan is available or until the geometries are
     * removed from the distributor.
     */
    private List<Geometry> myUnprocessedAdds = New.list();

    /** Lock for changing the unprocessed geometries. */
    private final Lock myUnprocessedGeometryLock = new ReentrantLock();

    static
    {
        final String factorStr = System.getProperty("opensphere.pipeline.opengl.polygonOffsetFactor");
        float factor;
        try
        {
            factor = Float.parseFloat(factorStr);
        }
        catch (final NumberFormatException e)
        {
            LOGGER.warn("Could not read opensphere.pipeline.jogl.polygonOffsetFactor");
            final float iv = 1f;
            factor = iv;
        }
        POLYGON_OFFSET_FACTOR = factor;

        final String unitsStr = System.getProperty("opensphere.pipeline.opengl.polygonOffsetUnits");
        float units;
        try
        {
            units = Float.parseFloat(unitsStr);
        }
        catch (final NumberFormatException e)
        {
            LOGGER.warn("Could not read opensphere.pipeline.jogl.polygonOffsetUnits");
            final float iv = 10f;
            units = iv;
        }
        POLYGON_OFFSET_UNITS = units;

        final String unitsStrAti = System.getProperty("opensphere.pipeline.opengl.polygonOffsetUnitsAti");
        float unitsAti;
        try
        {
            unitsAti = Float.parseFloat(unitsStrAti);
        }
        catch (final NumberFormatException e)
        {
            LOGGER.warn("Could not read opensphere.pipeline.jogl.polygonOffsetUnitsAti");
            final float iv = 10f;
            unitsAti = iv;
        }
        POLYGON_OFFSET_UNITS_ATI = unitsAti;
    }

    /**
     * Constructor.
     *
     * @param processorBuilder The builder for the processors.
     */
    public GeometryDistributor(ProcessorBuilder processorBuilder)
    {
        myProcessorBuilder = Utilities.checkNull(processorBuilder, "processorBuilder");
        processorBuilder.setProjectionSyncManager(myProjectionSyncManager);
        processorBuilder.setLabelOcclusionManager(myLabelOcclusionManager);
        myTimeManager = processorBuilder.getTimeManager();
        myAnimationManager = processorBuilder.getAnimationManager();

        myListenerHelper = new GeometryDistributorListenerHelper(processorBuilder.getMapContext(),
                processorBuilder.getFixedPoolExecutorService());

        myRepaintListener = myProcessorBuilder.getRepaintListener();
    }

    /** Handle any necessary cleanup when we are done with this distributor. */
    public void close()
    {
        myListenerHelper.close();
        mySpecializedProcessorsLock.writeLock().lock();
        try
        {
            myRenderableGeometryProcessors.clear();
            myProjectionSensitiveProcessors.clear();
        }
        finally
        {
            mySpecializedProcessorsLock.writeLock().unlock();
        }
        myProcessorsLock.writeLock().lock();
        try
        {
            for (final GeometryProcessor<? extends Geometry> proc : myGeometryProcessorsMap.values())
            {
                myProcessorBuilder.getPickManager().removeGeometries(proc.getGeometries());
                proc.close();
            }
            myGeometryProcessorsMap.clear();
            myInactiveGeometries.clear();
            recalculateCoveredTimeSpans();
            myGeometryProcessorsSnapshot = Collections.emptyList();
        }
        finally
        {
            myProcessorsLock.writeLock().unlock();
        }

        myUnprocessedGeometryLock.lock();
        try
        {
            removeTimeListeners();
        }
        finally
        {
            myUnprocessedGeometryLock.unlock();
        }
    }

    /**
     * Get the processorsLock.
     *
     * @return the processorsLock
     */
    public ReadWriteLock getProcessorsLock()
    {
        return myProcessorsLock;
    }

    /**
     * Get the renderableGeometryProcessors.
     *
     * @return the renderableGeometryProcessors
     */
    public Collection<RenderableGeometryProcessor<? extends Geometry>> getRenderableGeometryProcessors()
    {
        List<RenderableGeometryProcessor<? extends Geometry>> processors;
        mySpecializedProcessorsLock.readLock().lock();
        try
        {
            processors = New.list(myRenderableGeometryProcessors.values());
        }
        finally
        {
            mySpecializedProcessorsLock.readLock().unlock();
        }
        return processors;
    }

    /**
     * Get the specializedProcessorsLock.
     *
     * @return the specializedProcessorsLock
     */
    public ReadWriteLock getSpecializedProcessorsLock()
    {
        return mySpecializedProcessorsLock;
    }

    /**
     * Ask each geometry processor to render its geometries.
     *
     * @param renderContext The rendering context.
     */
    public void renderGeometries(RenderContext renderContext)
    {
        try
        {
            // Tell the ProjectionSyncManager not to switch projections during
            // rendering and get the projection which is being used.
            final Projection projection = myProjectionSyncManager.lockProjection();

            Class<? extends Position> lastPositionType = null;
            mySpecializedProcessorsLock.readLock().lock();
            try
            {
                final ActiveTimeSpans activeTimeSpans = myActiveTimeSpans;

                if (myOffSetUnits == 0)
                {
                    myOffSetUnits = POLYGON_OFFSET_UNITS;
                    String graphicsCard = renderContext.getGL().glGetString(GL.GL_RENDERER);
                    if (graphicsCard != null && graphicsCard.contains("AMD "))
                    {
                        myOffSetUnits = POLYGON_OFFSET_UNITS_ATI;
                    }
                }

                float offsetUnits = myOffSetUnits;

                for (final Entry<ProcessorDistributionKey, RenderableGeometryProcessor<? extends Geometry>> entry : myRenderableGeometryProcessors
                        .entrySet())
                {
                    final ProcessorDistributionKey key = entry.getKey();
                    if (!myIsDrawEnabled || !checkTime(activeTimeSpans, key.getTimeSpan(), key.getConstraintKey()))
                    {
                        continue;
                    }
                    final RenderableGeometryProcessor<? extends Geometry> processor = entry.getValue();
                    if (!processor.needsRender(renderContext.getRenderMode()))
                    {
                        continue;
                    }
                    final Class<? extends Position> positionType = processor.getPositionType();
                    if (positionType == null)
                    {
                        continue;
                    }
                    // Since GeometryGroupProcessors will set their own matrices
                    // do not set them here, but make sure that we set ours
                    // again if we have more processors.
                    if (processor instanceof GeometryGroupProcessor)
                    {
                        lastPositionType = null;
                    }
                    else if (!positionType.equals(lastPositionType))
                    {
                        renderContext.setModelViewAndProjection(positionType, projection);
                        lastPositionType = positionType;
                    }

                    // It is not a problem to set the offset for types that the
                    // offset does not apply, they will simply be ignored.
                    if (GeographicPosition.class.isAssignableFrom(positionType))
                    {
                        renderContext.glPolygonOffset(POLYGON_OFFSET_FACTOR, offsetUnits);
                        offsetUnits -= POLYGON_OFFSET_SCALE;
                    }
                    processor.render(renderContext);
                    GLUtilities.checkGLErrors(renderContext.getGL(), LOGGER, "after ", processor);
                }
            }
            finally
            {
                mySpecializedProcessorsLock.readLock().unlock();
            }
        }
        catch (final RuntimeException e)
        {
            LOGGER.fatal("Exception during render: " + e, e);
            throw e;
        }
        catch (final Error e)
        {
            LOGGER.fatal("Error during render: " + e, e);
            throw e;
        }
        finally
        {
            GLUtilities.checkGLErrors(renderContext.getGL(), LOGGER, "after geometry distributor render.");

            // Allow projection switching now that rendering is complete
            myProjectionSyncManager.unlockProjection();
        }
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(1024).append(GeometryDistributor.class.getSimpleName()).append(" [");
        myUnprocessedGeometryLock.lock();
        myProcessorsLock.readLock().lock();
        try
        {
            sb.append("unprocessed [").append(myUnprocessedAdds.size()).append("] inactive [").append(myInactiveGeometries.size())
                    .append("] active [").append(StringUtilities.LINE_SEP);
            for (final Entry<ProcessorDistributionKey, GeometryProcessor<? extends Geometry>> entry : myGeometryProcessorsMap
                    .entrySet())
            {
                sb.append(entry).append(StringUtilities.LINE_SEP);
            }
            sb.replace(sb.length() - 2, sb.length(), "]]");
        }
        finally
        {
            myProcessorsLock.readLock().unlock();
            myUnprocessedGeometryLock.unlock();
        }
        return sb.toString();
    }

    /**
     * Method called when the geometries in the geometry registry change.
     * Organize the geometries and send them to or remove them from the
     * appropriate processors.
     *
     * @param adds Geometries being added.
     * @param removes Geometries being removed.
     */
    public void updateGeometries(Collection<? extends Geometry> adds, Collection<? extends Geometry> removes)
    {
        // Get the active time spans here to avoid a deadlock between the
        // TimeManager monitor and myUnprocessedGeometryLock.
        myPendingActiveTimeSpans = myTimeManager.getActiveTimeSpans();
        boolean timeAgnostic;

        // new adds and removes will be processed immediately, so make sure
        // that they are not in a list waiting to be processed.
        myUnprocessedGeometryLock.lock();
        try
        {
            if (!myUnprocessedAdds.isEmpty())
            {
                myUnprocessedAdds.removeAll(adds);
                myUnprocessedAdds.removeAll(removes);
            }

            // In order to ensure that the removes are not added to the
            // unprocessed adds until distribution is complete, keep the
            // unprocessed adds locked during distribution.
            distributeGeometries(adds, removes);

            if (!removes.isEmpty())
            {
                myProcessorBuilder.getPickManager().removeGeometries(removes);
            }

            if (!myTimeAgnostic && myCoveredPrimaryTimeSpans.isEmpty() && myCoveredSecondaryTimeSpans.isEmpty()
                    && myUnprocessedAdds.isEmpty())
            {
                boolean inactiveTimed = false;
                if (!myInactiveGeometries.isEmpty())
                {
                    for (final ProcessorDistributionKey key : myInactiveGeometries.keySet())
                    {
                        if (!key.getTimeSpan().isTimeless())
                        {
                            inactiveTimed = true;
                            break;
                        }
                    }
                }
                if (!inactiveTimed)
                {
                    removeTimeListeners();
                }
            }

            timeAgnostic = myTimeAgnostic;
        }
        finally
        {
            myUnprocessedGeometryLock.unlock();
        }

        /* Make sure there were no missed updates between setting
         * myPendingActiveTimSpans and adding the time listeners. */
        if (!timeAgnostic)
        {
            myActiveTimeSpanChangeListener.activeTimeSpansChanged(myTimeManager.getActiveTimeSpans());
        }
    }

    /**
     * Register my time and animation listeners.
     */
    private void addTimeListeners()
    {
        myProcessorBuilder.getTimeManager().addActiveTimeSpanChangeListener(myActiveTimeSpanChangeListener);
        myProcessorBuilder.getAnimationManager().addAnimationChangeListener(myAnimationListener);
    }

    /**
     * Check the given time span against the active time span to see if the
     * input time span should be active.
     *
     * @param active The active time spans.
     * @param span The time span to check against the animation plan.
     * @param constraintKey Optional constraint key associated with the time
     *            span.
     * @return true when the time span is active.
     */
    private boolean checkTime(ActiveTimeSpans active, TimeSpan span, Object constraintKey)
    {
        if (span.isTimeless())
        {
            return true;
        }
        else if (constraintKey == null)
        {
            final AnimationPlan animationPlan = myAnimationPlan;
            final AnimationState currentAnimationState = myCurrentAnimationState;
            if (animationPlan == null || currentAnimationState == null)
            {
                return active != null && active.getPrimary().intersects(span);
            }
            final AnimationState state = animationPlan.findState(span, currentAnimationState.getDirection());
            if (state == null)
            {
                return false;
            }
            boolean result = false;
            try
            {
                result = animationPlan.calculateDistance(currentAnimationState, state) == 0;
            }
            catch (final RuntimeException e)
            {
                // If this test fails just fail the in range test as
                // this always happens during a plan change where the
                // distributor has yet to receive the updated plan and
                // ends up in a race condition with the animation
                // manager adjusting to the new plan.
                result = false;
            }
            return result;
        }
        else
        {
            final Collection<? extends TimeSpan> secondary = active == null ? null : active.getSecondary().get(constraintKey);
            boolean overlaps = false;
            if (secondary != null)
            {
                for (final TimeSpan ts : secondary)
                {
                    if (ts.overlaps(span))
                    {
                        overlaps = true;
                        break;
                    }
                }
            }
            return overlaps;
        }
    }

    /**
     * Distribute geometries which are being added by processor key. If a
     * matching processor key for removed geometries is available, the removes
     * will be submitted to the processor at the same time.
     *
     * @param addEntry The key and geometry collection pair which is being
     *            added.
     * @param removeKeyToGeoms The map of geometries which are being removed.
     * @return true when the map of processors has been changed.
     */
    private boolean distributeAddByKey(Entry<ProcessorDistributionKey, Collection<Geometry>> addEntry,
            Map<ProcessorDistributionKey, Collection<Geometry>> removeKeyToGeoms)
    {
        boolean processorCreated = false;
        final ProcessorDistributionKey key = addEntry.getKey();
        final Collection<Geometry> geometries = addEntry.getValue();
        GeometryProcessor<? extends Geometry> processor = myGeometryProcessorsMap.get(key);
        if (!geometries.isEmpty() && processor == null)
        {
            Set<Geometry> inactive = myInactiveGeometries.get(key);
            if (inactive == null)
            {
                final boolean process = inProcessRange(key, myActiveTimeSpans);
                if (process)
                {
                    setProcessorConstraints(key);
                    processor = myProcessorBuilder.createProcessorForClass(key.getGeometryType());
                    processorCreated = processor != null;
                }
                else
                {
                    inactive = New.set(geometries);
                    myInactiveGeometries.put(key, inactive);
                }
            }
            else
            {
                inactive.addAll(geometries);
            }
        }

        if (processor != null)
        {
            final Collection<? extends Geometry> procAdds = geometries;
            final Collection<? extends Geometry> procRemoves = removeKeyToGeoms.remove(key);
            processor.receiveObjects(this, procAdds, procRemoves == null ? Collections.<Geometry>emptyList() : procRemoves);
            if (processorCreated)
            {
                myGeometryProcessorsMap.put(addEntry.getKey(), processor);
            }
            if (LOGGER.isDebugEnabled())
            {
                if (processorCreated)
                {
                    LOGGER.debug("Created processor for distribution key " + addEntry.getKey());
                }
                LOGGER.debug("Added " + geometries.size() + ", removed " + (procRemoves == null ? 0 : procRemoves.size())
                        + " for processor " + processor);
            }
        }

        return processorCreated;
    }

    /**
     * Method called to distribute the geometries to or remove geometries from
     * the appropriate processors or inactive lists.
     *
     * @param adds Geometries being added.
     * @param removes Geometries being removed.
     */
    private void distributeGeometries(Collection<? extends Geometry> adds, Collection<? extends Geometry> removes)
    {
        final LazyMap<ProcessorDistributionKey, Collection<Geometry>> addKeyToGeoms = LazyMap.create(
                New.<ProcessorDistributionKey, Collection<Geometry>>map(), ProcessorDistributionKey.class,
                New.<Geometry>listFactory());
        final LazyMap<ProcessorDistributionKey, Collection<Geometry>> removeKeyToGeoms = LazyMap.create(
                New.<ProcessorDistributionKey, Collection<Geometry>>map(), ProcessorDistributionKey.class,
                New.<Geometry>listFactory());

        sortAddsAndRemoves(adds, removes, addKeyToGeoms, removeKeyToGeoms);

        // Synchronize to ensure that the geometry assignment stays
        // consistent.
        boolean processorsChanged = false;
        myProcessorsLock.writeLock().lock();
        try
        {
            for (final Entry<ProcessorDistributionKey, Collection<Geometry>> addEntry : addKeyToGeoms.entrySet())
            {
                processorsChanged |= distributeAddByKey(addEntry, removeKeyToGeoms);
            }

            for (final Entry<ProcessorDistributionKey, Collection<Geometry>> removeEntry : removeKeyToGeoms.entrySet())
            {
                processorsChanged |= distributeRemoveByKey(removeEntry);
            }

            if (processorsChanged)
            {
                recalculateCoveredTimeSpans();
            }

            myGeometryProcessorsSnapshot = Collections.unmodifiableCollection(New.collection(myGeometryProcessorsMap.values()));
        }
        finally
        {
            myProcessorsLock.writeLock().unlock();
        }

        if (processorsChanged)
        {
            populateRenderableProcessors();
        }
    }

    /**
     * Distribute geometries which are being removed by processor key.
     *
     * @param removeEntry The key and geometry collection pair which is being
     *            removed.
     * @return true when the map of processors has been changed.
     */
    private boolean distributeRemoveByKey(Entry<ProcessorDistributionKey, Collection<Geometry>> removeEntry)
    {
        boolean processorsChanged = false;
        final GeometryProcessor<? extends Geometry> processor = myGeometryProcessorsMap.get(removeEntry.getKey());
        final Collection<? extends Geometry> procRemoves = removeEntry.getValue();
        if (processor == null)
        {
            // If we are removing geometries and there is no
            // processor, then the geometries should be in one of the
            // holding lists.
            final Set<Geometry> inactive = myInactiveGeometries.get(removeEntry.getKey());
            if (inactive != null)
            {
                inactive.removeAll(procRemoves);
                if (inactive.isEmpty())
                {
                    myInactiveGeometries.remove(removeEntry.getKey());
                }
            }
        }
        else
        {
            processor.receiveObjects(this, Collections.<Geometry>emptyList(), procRemoves);
            if (processor.getGeometryCount() == 0)
            {
                processorsChanged = true;
                myGeometryProcessorsMap.remove(removeEntry.getKey());
                processor.close();
            }
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Removed " + procRemoves.size() + " for processor " + processor);
                if (processorsChanged)
                {
                    LOGGER.debug("Removed processor for distribution key " + removeEntry.getKey());
                }
            }
        }

        return processorsChanged;
    }

    /**
     * When the projection has changed, notify the processors.
     *
     * @param evt The event.
     */
    private void handleProjectionChanged(ProjectionChangedEvent evt)
    {
        mySpecializedProcessorsLock.readLock().lock();
        try
        {
            myProjectionSyncManager.setProjectionSnapshot(evt.getProjectionSnapshot());
            for (final GeometryProcessor<? extends Geometry> proc : myProjectionSensitiveProcessors.values())
            {
                proc.handleProjectionChanged(evt);
            }
        }
        finally
        {
            mySpecializedProcessorsLock.readLock().unlock();
        }
    }

    /** Handle when the animation plan step has changed. */
    private void handleStepChange()
    {
        boolean processorsChanged = false;
        myProcessorsLock.writeLock().lock();
        try
        {
            final Iterator<Entry<ProcessorDistributionKey, GeometryProcessor<? extends Geometry>>> procIter = myGeometryProcessorsMap
                    .entrySet().iterator();
            final ActiveTimeSpans activeTimeSpans = myActiveTimeSpans;
            while (procIter.hasNext())
            {
                final Entry<ProcessorDistributionKey, GeometryProcessor<? extends Geometry>> entry = procIter.next();
                if (!inProcessRange(entry.getKey(), activeTimeSpans))
                {
                    processorsChanged = true;
                    procIter.remove();
                    myInactiveGeometries.put(entry.getKey(), New.set(entry.getValue().getGeometries()));
                    entry.getValue().close();
                }
            }

            final Iterator<Entry<ProcessorDistributionKey, Set<Geometry>>> inactiveIter = myInactiveGeometries.entrySet()
                    .iterator();
            while (inactiveIter.hasNext())
            {
                final Entry<ProcessorDistributionKey, Set<Geometry>> entry = inactiveIter.next();
                if (inProcessRange(entry.getKey(), activeTimeSpans) && !entry.getValue().isEmpty())
                {
                    processorsChanged = true;
                    inactiveIter.remove();
                    setProcessorConstraints(entry.getKey());
                    final GeometryProcessor<? extends Geometry> processor = myProcessorBuilder
                            .createProcessorForClass(entry.getKey().getGeometryType());
                    processor.receiveObjects(this, entry.getValue(), Collections.<Geometry>emptyList());
                    myGeometryProcessorsMap.put(entry.getKey(), processor);
                }
            }
        }
        finally
        {
            myProcessorsLock.writeLock().unlock();
        }

        if (processorsChanged)
        {
            populateRenderableProcessors();
        }
    }

    /**
     * When the view has changed, notify the processors.
     *
     * @param view The viewer.
     * @param type The viewer update type.
     */
    private void handleViewChanged(Viewer view, ViewChangeType type)
    {
        for (final GeometryProcessor<? extends Geometry> proc : myGeometryProcessorsSnapshot)
        {
            if (proc instanceof AbstractProcessor)
            {
                ((AbstractProcessor<? extends Geometry>)proc).handleViewChanged(view, type);
            }
        }
    }

    /**
     * Determine whether the processor key is in the valid range for having a
     * processor created. If it is not in range, it should be put into the
     * inactive map.
     *
     * @param key The processor key.
     * @param active The active time spans.
     * @return True when a processor should be available for the key.
     */
    private boolean inProcessRange(ProcessorDistributionKey key, ActiveTimeSpans active)
    {
        if (checkTime(active, key.getTimeSpan(), key.getConstraintKey()))
        {
            return true;
        }

        final AnimationPlan animationPlan = myAnimationPlan;
        final AnimationState currentAnimationState = myCurrentAnimationState;
        if (animationPlan == null || currentAnimationState == null)
        {
            return false;
        }

        final int loadAhead = myProcessorBuilder.getProcessorFactory().getLoadAhead(key.getGeometryType());

        final AnimationState state = animationPlan.findState(key.getTimeSpan(), currentAnimationState.getDirection());
        if (state == null)
        {
            return false;
        }
        animationPlan.getTimeSpanForState(state);
        boolean inRange = false;
        try
        {
            final int distance = animationPlan.calculateDistance(currentAnimationState, state);
            inRange = distance <= loadAhead;
        }
        catch (final RuntimeException e)
        {
            // If this test fails just fail the in range test as this always
            // happens during a plan
            // change where the distributor has yet to receive the updated
            // plan and ends up in
            // a race condition with the animation manager adjusting to the
            // new plan.
            inRange = false;
        }
        return inRange;
    }

    /**
     * Populate my renderable processors from my current processors based on
     * type.
     */
    private void populateRenderableProcessors()
    {
        myProcessorsLock.readLock().lock();
        try
        {
            mySpecializedProcessorsLock.writeLock().lock();
            try
            {
                myRenderableGeometryProcessors.clear();
                myProjectionSensitiveProcessors.clear();
                for (final Entry<ProcessorDistributionKey, GeometryProcessor<? extends Geometry>> processorEntry : myGeometryProcessorsMap
                        .entrySet())
                {
                    if (processorEntry.getValue() instanceof RenderableGeometryProcessor)
                    {
                        myRenderableGeometryProcessors.put(processorEntry.getKey(),
                                (RenderableGeometryProcessor<? extends Geometry>)processorEntry.getValue());
                    }

                    if (processorEntry.getValue().sensitiveToProjectionChanges())
                    {
                        myProjectionSensitiveProcessors.put(processorEntry.getKey(), processorEntry.getValue());
                    }
                }
                myProjectionSyncManager.setProcessors(myProjectionSensitiveProcessors.values());
            }
            finally
            {
                mySpecializedProcessorsLock.writeLock().unlock();
            }
        }
        finally
        {
            myProcessorsLock.readLock().unlock();
        }
    }

    /**
     * Put the unprocessed adds into processors and remove the unprocessed
     * removes from the processors.
     */
    private void processUnprocessed()
    {
        List<Geometry> unprocAdds;
        myUnprocessedGeometryLock.lock();
        try
        {
            // Processing the unprocessed geometries may result in them being
            // added back to one of the unprocessed maps, so create a clean set
            // of maps for them to be added to.
            if (myUnprocessedAdds.isEmpty())
            {
                unprocAdds = null;
            }
            else
            {
                unprocAdds = myUnprocessedAdds;
                myUnprocessedAdds = New.list();
            }
        }
        finally
        {
            myUnprocessedGeometryLock.unlock();
        }

        if (unprocAdds != null)
        {
            distributeGeometries(unprocAdds, Collections.<Geometry>emptyList());
        }
    }

    /**
     * Recalculate the time spans covered by my processor keys. This should be
     * called whenever {@link #myGeometryProcessorsMap} is changed. This assumes
     * that {@link #myProcessorsLock} is already locked.
     */
    private void recalculateCoveredTimeSpans()
    {
        final Collection<TimeSpan> primarySpans = New.collection();
        final Map<Object, List<TimeSpan>> secondarySpans = New.map();
        for (final ProcessorDistributionKey key : myGeometryProcessorsMap.keySet())
        {
            if (!key.getTimeSpan().isTimeless())
            {
                if (key.getConstraintKey() == null)
                {
                    primarySpans.add(key.getTimeSpan());
                }
                else
                {
                    CollectionUtilities.multiMapAdd(secondarySpans, key.getConstraintKey(), key.getTimeSpan(), false);
                }
            }
        }

        myCoveredPrimaryTimeSpans = new TimeSpanArrayList(primarySpans);

        synchronized (myCoveredSecondaryTimeSpans)
        {
            myCoveredSecondaryTimeSpans.clear();
            for (final Map.Entry<Object, List<TimeSpan>> entry : secondarySpans.entrySet())
            {
                myCoveredSecondaryTimeSpans.put(entry.getKey(), New.unmodifiableSet(entry.getValue()));
            }
        }
    }

    /**
     * De-register my time and animation listeners.
     */
    private void removeTimeListeners()
    {
        myProcessorBuilder.getTimeManager().removeActiveTimeSpanChangeListener(myActiveTimeSpanChangeListener);
        myProcessorBuilder.getAnimationManager().removeAnimationChangeListener(myAnimationListener);
        myTimeAgnostic = true;
    }

    /**
     * Set the positive and negative constraints in the processor builder based
     * on the given distribution key.
     *
     * @param key The distribution key.
     */
    private void setProcessorConstraints(ProcessorDistributionKey key)
    {
        myProcessorBuilder
                .setPositiveConstraints(new Constraints(new StrictTimeConstraint(key.getConstraintKey(), key.getTimeSpan())));
        myProcessorBuilder.setNegativeConstraints(
                new Constraints(TimeConstraint.getNegativeTimeConstraint(key.getConstraintKey(), key.getTimeSpan())));
    }

    /**
     * Collect up the time restrained geometries from their respective
     * processors and put them in my list of unprocessed adds. This should be
     * done when the processors are being removed or recreated because of
     * animation plan changes. A lock must be obtained on
     * {@link #myUnprocessedGeometryLock} prior to making this call.
     *
     * @param newSecondary List of new secondary time spans.
     * @param activeTimeSpans The active time spans.
     */
    private void setTimedToUnprocessed(TimeSpanList newSecondary, ActiveTimeSpans activeTimeSpans)
    {
        final Collection<Geometry> moveToUnprocessed = New.collection();
        myProcessorsLock.writeLock().lock();
        try
        {
            for (final Iterator<Entry<ProcessorDistributionKey, GeometryProcessor<? extends Geometry>>> iter = myGeometryProcessorsMap
                    .entrySet().iterator(); iter.hasNext();)
            {
                final Entry<ProcessorDistributionKey, GeometryProcessor<? extends Geometry>> entry = iter.next();
                final GeometryProcessor<? extends Geometry> proc = entry.getValue();
                if (!entry.getKey().getTimeSpan().isTimeless())
                {
                    // Always move geometries to unprocessed in case new
                    // processors are created that overlap the time spans of
                    // the existing processors.
                    moveToUnprocessed.addAll(proc.getGeometries());

                    // Determine if the processor can still be used.
                    boolean remove;
                    if (entry.getKey().getConstraintKey() == null)
                    {
                        // This processor has no constraint key, so it was
                        // created for a primary time span. If the animation
                        // plan no longer overlaps its time span, or there is
                        // now a secondary time span that overlaps it, this
                        // processor must be removed.
                        remove = myAnimationPlan == null
                                || !myAnimationPlan.getTimeCoverage().contains(entry.getKey().getTimeSpan())
                                || newSecondary.intersects(entry.getKey().getTimeSpan());
                    }
                    else
                    {
                        // The processor was created for a secondary time
                        // span; check to see if the time span still exists.
                        final Collection<? extends TimeSpan> secondary = activeTimeSpans == null ? null
                                : activeTimeSpans.getSecondary().get(entry.getKey().getConstraintKey());
                        remove = secondary == null || !secondary.contains(entry.getKey().getTimeSpan());
                    }

                    if (remove)
                    {
                        iter.remove();
                        proc.close();
                    }
                }
            }
            recalculateCoveredTimeSpans();
            myGeometryProcessorsSnapshot = Collections.unmodifiableCollection(New.collection(myGeometryProcessorsMap.values()));

            for (final Collection<Geometry> inactive : myInactiveGeometries.values())
            {
                myUnprocessedAdds.addAll(inactive);
            }
            myInactiveGeometries.clear();

            myUnprocessedAdds.addAll(moveToUnprocessed);
        }
        finally
        {
            myProcessorsLock.writeLock().unlock();
        }
        populateRenderableProcessors();
    }

    /**
     * Sort adds and removed by the processor key(s) they go with.
     *
     * @param adds Geometries to add.
     * @param removes Geometries to remove.
     * @param addKeyToGeoms Added geometries sorted by key.
     * @param removeKeyToGeoms Removed geometries sorted by key.
     */
    private void sortAddsAndRemoves(Collection<? extends Geometry> adds, Collection<? extends Geometry> removes,
            LazyMap<ProcessorDistributionKey, Collection<Geometry>> addKeyToGeoms,
            LazyMap<ProcessorDistributionKey, Collection<Geometry>> removeKeyToGeoms)
    {
        myUnprocessedGeometryLock.lock();
        try
        {
            sortByKeys(addKeyToGeoms, adds, myUnprocessedAdds);
            sortByKeys(removeKeyToGeoms, removes, null);
        }
        finally
        {
            myUnprocessedGeometryLock.unlock();
        }
    }

    /**
     * Sort a geometry by processor key.
     *
     * @param activeTimeSpans The active time spans.
     * @param keyToGeoms The map in which successfully sorted geometries are
     *            placed.
     * @param geom The geometry which is to be sorted.
     * @param hull A mutable key to be used in order to avoid allocating memory.
     * @return {@code true} if a key was found.
     */
    private boolean sortByKeys(ActiveTimeSpans activeTimeSpans,
            LazyMap<ProcessorDistributionKey, Collection<Geometry>> keyToGeoms, ConstrainableGeometry geom,
            ProcessorDistributionKey hull)
    {
        final TimeConstraint timeConstr = geom.getConstraints().getTimeConstraint();

        boolean foundKey = false;
        if (activeTimeSpans != null)
        {
            final Collection<? extends TimeSpan> wild = activeTimeSpans.getSecondary().get(TimeManager.WILDCARD_CONSTRAINT_KEY);
            if (wild != null)
            {
                foundKey = sortByKeys(keyToGeoms, geom, hull, TimeManager.WILDCARD_CONSTRAINT_KEY, wild);
            }
        }

        if (!foundKey && timeConstr.getKey() != null && activeTimeSpans != null)
        {
            final Collection<? extends TimeSpan> timeSpans = activeTimeSpans.getSecondary().get(timeConstr.getKey());
            if (timeSpans != null)
            {
                foundKey = sortByKeys(keyToGeoms, geom, hull, timeConstr.getKey(), timeSpans);
            }
        }

        if (!foundKey && (myAnimationPlan != null || activeTimeSpans != null))
        {
            @SuppressWarnings("null")
            final Collection<? extends TimeSpan> timeSpans = myAnimationPlan == null ? activeTimeSpans.getPrimary()
                    : myAnimationPlan.getTimeCoverage();
            foundKey = sortByKeys(keyToGeoms, geom, hull, null, timeSpans);
        }
        return foundKey;
    }

    /**
     * Sort the geometries by processor key or add them to the unprocessed
     * collection if no processor key is available.
     *
     * @param keyToGeoms The map in which successfully sorted geometries are
     *            placed.
     * @param sortGeoms The Geometries which are to be sorted.
     * @param unprocessed The unprocessed geometries to which geometries should
     *            be added when no processor key is available.
     */
    private void sortByKeys(LazyMap<ProcessorDistributionKey, Collection<Geometry>> keyToGeoms,
            Collection<? extends Geometry> sortGeoms, Collection<Geometry> unprocessed)
    {
        final ProcessorDistributionKey hull = new ProcessorDistributionKey();
        for (final Geometry geom : sortGeoms)
        {
            sortByKeys(keyToGeoms, geom, unprocessed, hull);
        }
    }

    /**
     * Sort a geometry by processor key or add it to the unprocessed collection
     * if no processor key is available.
     *
     * @param keyToGeoms The map in which successfully sorted geometries are
     *            placed.
     * @param geom The geometry which is to be sorted.
     * @param unprocessed The unprocessed geometries to which geometries should
     *            be added when no processor key is available.
     * @param hull A mutable key to be used in order to avoid allocating memory.
     */
    private void sortByKeys(LazyMap<ProcessorDistributionKey, Collection<Geometry>> keyToGeoms, Geometry geom,
            Collection<Geometry> unprocessed, ProcessorDistributionKey hull)
    {
        boolean timeless;
        boolean foundKey;
        if (geom instanceof ConstrainableGeometry)
        {
            final Constraints constraints = ((ConstrainableGeometry)geom).getConstraints();

            if (constraints == null || constraints.getTimeConstraint() == null)
            {
                timeless = true;
                foundKey = false;
            }
            else
            {
                timeless = false;
                if (myTimeAgnostic)
                {
                    addTimeListeners();
                    updateTime();
                }
                foundKey = sortByKeys(myActiveTimeSpans, keyToGeoms, (ConstrainableGeometry)geom, hull);
            }
        }
        else
        {
            timeless = true;
            foundKey = false;
        }

        if (timeless)
        {
            foundKey = sortByKeys(keyToGeoms, geom, hull, null, TimeSpanList.TIMELESS);
        }
        if (!foundKey && unprocessed != null)
        {
            unprocessed.add(geom);
        }
    }

    /**
     * Sort a geometry by processor key.
     *
     * @param keyToGeoms The map in which successfully sorted geometries are
     *            placed.
     * @param geom The geometry which is to be sorted.
     * @param hull A mutable key to be used in order to avoid allocating memory.
     * @param constraintKey The constraint key to use for sorting, which may be
     *            {@code null}.
     * @param timeSpans The time spans to attempt to sort into.
     * @return {@code true} if a key was found.
     */
    private boolean sortByKeys(LazyMap<ProcessorDistributionKey, Collection<Geometry>> keyToGeoms, Geometry geom,
            ProcessorDistributionKey hull, Object constraintKey, Collection<? extends TimeSpan> timeSpans)
    {
        final TimeConstraint timeConstr = geom instanceof ConstrainableGeometry
                ? ((ConstrainableGeometry)geom).getConstraints() == null ? null
                        : ((ConstrainableGeometry)geom).getConstraints().getTimeConstraint()
                : null;

        boolean foundKey = false;
        for (final TimeSpan time : timeSpans)
        {
            if (timeConstr == null || timeConstr.check(time))
            {
                foundKey = true;
                hull.set(geom, constraintKey, time);
                if (keyToGeoms.containsKey(hull))
                {
                    keyToGeoms.get(hull).add(geom);
                }
                else
                {
                    keyToGeoms.get(new ImmutableProcessorDistributionKey(geom, constraintKey, time)).add(geom);
                }
            }
        }
        return foundKey;
    }

    /**
     * Update my time fields to the latest values. This should only be called
     * from within a {@link #myUnprocessedGeometryLock} and only when there are
     * no timed processors.
     */
    private void updateTime()
    {
        if (!myTimeAgnostic)
        {
            throw new IllegalStateException("Not time agnostic.");
        }
        myTimeAgnostic = false;
        myActiveTimeSpans = myPendingActiveTimeSpans;
        myCurrentAnimationState = myAnimationManager.getAnimationState();
        myAnimationPlan = myAnimationManager.getCurrentPlan();
    }

    /** The listener helper. */
    private final class GeometryDistributorListenerHelper extends AbstractGeometryDistributorListenerHelper
    {
        /**
         * Constructor.
         *
         * @param mapContext The map context.
         * @param executorService The executor service for the listeners.
         */
        public GeometryDistributorListenerHelper(MapContext<?> mapContext, ExecutorService executorService)
        {
            super(mapContext, executorService);
        }

        @Override
        protected void handleDrawEnabled(boolean flag)
        {
            myIsDrawEnabled = flag;
        }

        @Override
        protected void handleProjectionChanged(ProjectionChangedEvent evt)
        {
            GeometryDistributor.this.handleProjectionChanged(evt);
        }

        @Override
        protected void handleViewChanged(Viewer view, ViewChangeType type)
        {
            GeometryDistributor.this.handleViewChanged(view, type);
        }
    }
}
