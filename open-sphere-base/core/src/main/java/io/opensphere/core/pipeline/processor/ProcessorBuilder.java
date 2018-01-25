package io.opensphere.core.pipeline.processor;

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import io.opensphere.core.AnimationManager;
import io.opensphere.core.TimeManager;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RepaintListener;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * A builder class for geometry processors.
 */
public class ProcessorBuilder implements Cloneable
{
    /** The animation manager. */
    private AnimationManager myAnimationManager;

    /** The geometry cache. */
    private CacheProvider myCache;

    /** Data retriever executor. */
    private ExecutorService myDataRetriever;

    /** The normal executor. */
    private ExecutorService myExecutorService;

    /** The fixed pool executor. */
    private ExecutorService myFixedPoolExecutorService;

    /** The executor for GL tasks. */
    private Executor myGLExecutor;

    /** Manager for determining label occlusion. */
    private LabelOcclusionManager myLabelOcclusionManager;

    /** The load-sensitive executor. */
    private Executor myLoadSensitiveExecutor;

    /** The set of viewers. */
    private MapContext<?> myMapContext;

    /**
     * Constraints that may be used by the processor to do group constraint
     * checking. If these constraints are satisfied, it means none of the
     * geometries' constraints can be satisfied.
     */
    private Constraints myNegativeGroupConstraints;

    /** The pick manager. */
    private PickManager myPickManager;

    /**
     * Constraints that may be used by the processor to do group constraint
     * checking. If these constraints are satisfied, it means all of the
     * geometries' constraints are satisfied.
     */
    private Constraints myPositiveGroupConstraints;

    /** Comparator that determines geometry processing priority. */
    private Comparator<? super Geometry> myPriorityComparator;

    /** The processor factory. */
    private final GeometryProcessorFactory myProcessorFactory = new GeometryProcessorFactory();

    /** The manager for synchronizing projection changes in the processors. */
    private ProjectionSyncManager myProjectionSyncManager;

    /** The set of renderers for the current pipeline. */
    private GeometryRendererSet myRendererSet;

    /** The listener to be called to trigger repaints. */
    private RepaintListener myRepaintListener;

    /** The scheduled executor service. */
    private ScheduledExecutorService myScheduledExecutorService;

    /** The time manager. */
    private TimeManager myTimeManager;

    @Override
    public ProcessorBuilder clone()
    {
        try
        {
            return (ProcessorBuilder)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            return null;
        }
    }

    /**
     * Create a geometry processor.
     *
     * @param geometryType The geometry type.
     * @return The processor, or {@code null} if none are available.
     */
    public GeometryProcessor<? extends Geometry> createProcessorForClass(Class<? extends Geometry> geometryType)
    {
        return myProcessorFactory.createProcessorForClass(geometryType, this, (Collection<String>)null);
    }

    /**
     * Get the animation manager.
     *
     * @return The animation manager.
     */
    public AnimationManager getAnimationManager()
    {
        return myAnimationManager;
    }

    /**
     * Accessor for the cache.
     *
     * @return The cache.
     */
    public CacheProvider getCache()
    {
        return myCache;
    }

    /**
     * Accessor for the dataRetriever.
     *
     * @return The dataRetriever.
     */
    public ExecutorService getDataRetriever()
    {
        return myDataRetriever;
    }

    /**
     * Get the non-scheduled executor service. This service should be used when
     * scheduling is not necessary, since it may execute with less latency than
     * the scheduled executor.
     *
     * @return The executor.
     */
    public ExecutorService getExecutorService()
    {
        return myExecutorService;
    }

    /**
     * Accessor for the fixed pool executor service.
     *
     * @return The fixed pool executor service.
     */
    public ExecutorService getFixedPoolExecutorService()
    {
        return myFixedPoolExecutorService;
    }

    /**
     * Accessor for the gLExecutor.
     *
     * @return The gLExecutor.
     */
    public Executor getGLExecutor()
    {
        return myGLExecutor;
    }

    /**
     * Get the labelOcclusionManager.
     *
     * @return the labelOcclusionManager
     */
    public LabelOcclusionManager getLabelOcclusionManager()
    {
        return myLabelOcclusionManager;
    }

    /**
     * Accessor for the viewer set.
     *
     * @return The viewer set.
     */
    public MapContext<?> getMapContext()
    {
        return myMapContext;
    }

    /**
     * Get the constraints that may be used by the processor to do group
     * constraint checking. If these constraints are satisfied, it means none of
     * the geometries' constraints can be satisfied.
     *
     * @return The constraints.
     */
    public Constraints getNegativeConstraints()
    {
        return myNegativeGroupConstraints;
    }

    /**
     * Accessor for the pickManager.
     *
     * @return The pickManager.
     */
    public PickManager getPickManager()
    {
        return myPickManager;
    }

    /**
     * Get the constraints that may be used by the processor to do group
     * constraint checking. If these constraints are satisfied, it means all of
     * the geometries' constraints are satisfied.
     *
     * @return The constraints.
     */
    public Constraints getPositiveConstraints()
    {
        return myPositiveGroupConstraints;
    }

    /**
     * Get the comparator used to prioritize geometry data requests.
     *
     * @return The priorityComparator.
     */
    public Comparator<? super Geometry> getPriorityComparator()
    {
        return myPriorityComparator;
    }

    /**
     * Access the geometry factory.
     *
     * @return The factory.
     */
    public GeometryProcessorFactory getProcessorFactory()
    {
        return myProcessorFactory;
    }

    /**
     * Get the projectionSyncManager.
     *
     * @return the projectionSyncManager
     */
    public ProjectionSyncManager getProjectionSyncManager()
    {
        return myProjectionSyncManager;
    }

    /**
     * Accessor for the rendererSet.
     *
     * @return The rendererSet.
     */
    public GeometryRendererSet getRendererSet()
    {
        return myRendererSet;
    }

    /**
     * Accessor for the repaintListener.
     *
     * @return The repaintListener.
     */
    public RepaintListener getRepaintListener()
    {
        return myRepaintListener;
    }

    /**
     * Get the scheduled executor service.
     *
     * @return The scheduled executor.
     */
    public ScheduledExecutorService getScheduledExecutorService()
    {
        return myScheduledExecutorService;
    }

    /**
     * Get the time manager.
     *
     * @return The time manager.
     */
    public TimeManager getTimeManager()
    {
        return myTimeManager;
    }

    /**
     * Set the animationManager.
     *
     * @param animationManager The animationManager to set.
     */
    public void setAnimationManager(AnimationManager animationManager)
    {
        myAnimationManager = animationManager;
    }

    /**
     * Mutator for the cache.
     *
     * @param cache The cache to set. return this;
     * @return The builder.
     */
    public ProcessorBuilder setCache(CacheProvider cache)
    {
        myCache = cache;
        return this;
    }

    /**
     * Mutator for the dataRetriever.
     *
     * @param dataRetriever The dataRetriever to set.
     * @return The builder.
     */
    public ProcessorBuilder setDataRetriever(ExecutorService dataRetriever)
    {
        myDataRetriever = dataRetriever;
        return this;
    }

    /**
     * Set the non-scheduled executor service.
     *
     * @param executorService The executor to set.
     * @return The builder.
     */
    public ProcessorBuilder setExecutorService(ExecutorService executorService)
    {
        myExecutorService = executorService;
        return this;
    }

    /**
     * Mutator for the fixed pool executor service.
     *
     * @param fixedPoolService The new fixed pool executor service.
     */
    public void setFixedPoolExecutorService(ExecutorService fixedPoolService)
    {
        myFixedPoolExecutorService = fixedPoolService;
    }

    /**
     * Mutator for the gLExecutor.
     *
     * @param gLExecutor The gLExecutor to set.
     * @return The builder.
     */
    public ProcessorBuilder setGLExecutor(Executor gLExecutor)
    {
        myGLExecutor = gLExecutor;
        return this;
    }

    /**
     * Set the labelOcclusionManager.
     *
     * @param labelOcclusionManager the labelOcclusionManager to set
     */
    public void setLabelOcclusionManager(LabelOcclusionManager labelOcclusionManager)
    {
        myLabelOcclusionManager = labelOcclusionManager;
    }

    /**
     * Mutator for the loadSensitiveExecutor.
     *
     * @param loadSensitiveExecutor The loadSensitiveExecutor to set.
     */
    public void setLoadSensitiveExecutor(Executor loadSensitiveExecutor)
    {
        myLoadSensitiveExecutor = loadSensitiveExecutor;
    }

    /**
     * Mutator for the mapContext.
     *
     * @param mapContext The mapManager to set.
     * @return The builder.
     */
    public ProcessorBuilder setMapContext(MapContext<?> mapContext)
    {
        myMapContext = mapContext;
        return this;
    }

    /**
     * Set the constraints that may be used by the processor to do group
     * constraint checking. If these constraints are satisfied, it means none of
     * the geometries' constraints can be satisfied.
     *
     * @param constraints The group constraints.
     */
    public void setNegativeConstraints(Constraints constraints)
    {
        myNegativeGroupConstraints = constraints;
    }

    /**
     * Mutator for the pickManager.
     *
     * @param pickManager The pickManager to set.
     * @return The builder.
     */
    public ProcessorBuilder setPickManager(PickManager pickManager)
    {
        myPickManager = pickManager;
        return this;
    }

    /**
     * Set the constraints that may be used by the processor to do group
     * constraint checking. If these constraints are satisfied, it means all of
     * the geometries' constraints are satisfied.
     *
     * @param constraints The group constraints.
     */
    public void setPositiveConstraints(Constraints constraints)
    {
        myPositiveGroupConstraints = constraints;
    }

    /**
     * Set the comparator used to prioritize geometry data requests.
     *
     * @param priorityComparator The priorityComparator to set.
     */
    public void setPriorityComparator(Comparator<? super Geometry> priorityComparator)
    {
        myPriorityComparator = priorityComparator;
    }

    /**
     * Set the projectionSyncManager.
     *
     * @param projectionSyncManager the projectionSyncManager to set
     */
    public void setProjectionSyncManager(ProjectionSyncManager projectionSyncManager)
    {
        myProjectionSyncManager = projectionSyncManager;
    }

    /**
     * Mutator for the rendererSet.
     *
     * @param rendererSet The rendererSet to set.
     * @return The builder.
     */
    public ProcessorBuilder setRendererSet(GeometryRendererSet rendererSet)
    {
        myRendererSet = rendererSet;
        return this;
    }

    /**
     * Mutator for the repaintListener.
     *
     * @param repaintListener The repaintListener to set.
     * @return The builder.
     */
    public ProcessorBuilder setRepaintListener(RepaintListener repaintListener)
    {
        myRepaintListener = repaintListener;
        return this;
    }

    /**
     * Set the scheduled executor service.
     *
     * @param executor The executor.
     * @return The builder.
     */
    public ProcessorBuilder setScheduledExecutorService(ScheduledExecutorService executor)
    {
        myScheduledExecutorService = executor;
        return this;
    }

    /**
     * Set the timeManager.
     *
     * @param timeManager The timeManager to set.
     */
    public void setTimeManager(TimeManager timeManager)
    {
        myTimeManager = timeManager;
    }

    /**
     * Accessor for the loadSensitiveExecutor.
     *
     * @return The loadSensitiveExecutor.
     */
    protected Executor getLoadSensitiveExecutor()
    {
        return myLoadSensitiveExecutor;
    }
}
