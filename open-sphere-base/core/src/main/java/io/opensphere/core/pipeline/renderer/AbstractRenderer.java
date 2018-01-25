package io.opensphere.core.pipeline.renderer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * Abstract implementation of the {@link GeometryRenderer} interface. This
 * provides access to a geometry cache and some no-op implementations of
 * interface methods that may not be used by sub-classes.
 * <p>
 * This also provides a facility for managing debug features for renderers.
 *
 * @param <T> the specific type of geometry that the renderer knows how to
 *            render
 */
@SuppressWarnings("PMD.GodClass")
public abstract class AbstractRenderer<T extends Geometry> implements GeometryRenderer<T>
{
    /** How many vertices doth a quad have. */
    public static final int QUAD_VERTEX_COUNT = 4;

    /** How many vertices doth a triangle have. */
    public static final int TRIANGLE_VERTEX_COUNT = 3;

    /** How many vertices does a single triangle in a triangle strip have. */
    public static final int TRIANGLE_STRIP_VERTEX_COUNT = 2;

    /** Collection of enabled debug features. */
    private static volatile Collection<String> ourDebugFeatures;

    /** The geometry cache. */
    private final CacheProvider myCache;

    /**
     * Render data for a projection snapshot which is not currently being
     * rendered, but is ready to be rendered.
     */
    private final List<RenderData> myPendingProjectionData = New.list(3);

    /**
     * Support for listeners for when this renderer is ready to render
     * projections.
     */
    private final ChangeSupport<ProjectionReadyListener> myProjectionReadySupport = new WeakChangeSupport<>();

    /** The data used for rendering. */
    private volatile RenderData myRenderData;

    /**
     * Lock for managing concurrent changes to the render data and pending
     * render data.
     */
    private final ReentrantReadWriteLock myRenderDataLock = new ReentrantReadWriteLock();

    /**
     * Toggle a debug feature for a particular renderer.
     *
     * @param debugFeature the feature name to be toggled
     */
    public static synchronized void toggleFeature(String debugFeature)
    {
        if (ourDebugFeatures == null)
        {
            turnDebugOn(debugFeature);
            return;
        }
        Collection<String> debugFeatures = New.collection(ourDebugFeatures);
        if (debugFeatures.remove(debugFeature))
        {
            if (debugFeatures.isEmpty())
            {
                ourDebugFeatures = null;
            }
            else
            {
                ourDebugFeatures = debugFeatures;
            }
        }
        else
        {
            debugFeatures.add(debugFeature);
            ourDebugFeatures = debugFeatures;
        }
    }

    /**
     * Turn off certain debug features for a renderer.
     *
     * @param offFeatures the features to be turned off
     */
    public static synchronized void turnDebugOff(String... offFeatures)
    {
        Collection<String> debugFeatures = New.collection(ourDebugFeatures);
        if (debugFeatures != null)
        {
            debugFeatures.removeAll(Arrays.asList(offFeatures));
            if (debugFeatures.isEmpty())
            {
                ourDebugFeatures = null;
            }
            else
            {
                ourDebugFeatures = debugFeatures;
            }
        }
    }

    /**
     * Turn on certain debug features for a renderer.
     *
     * @param debugFeatures the features to be turned on
     */
    public static synchronized void turnDebugOn(String... debugFeatures)
    {
        Collection<String> features = ourDebugFeatures;
        if (features == null)
        {
            features = Arrays.asList(debugFeatures);
        }
        else
        {
            features.addAll(Arrays.asList(debugFeatures));
        }
        ourDebugFeatures = features;
    }

    /**
     * Construct the renderer.
     *
     * @param cache The geometry cache.
     */
    protected AbstractRenderer(CacheProvider cache)
    {
        myCache = cache;
    }

    @Override
    public void addProjectionReadyListener(ProjectionReadyListener listener)
    {
        myProjectionReadySupport.addListener(listener);
    }

    @Override
    public void close()
    {
    }

    /**
     * Get the render data which matches the projection.
     *
     * @param projection The render data which matches the projection.
     * @return the render data or null of no match is available.
     */
    public RenderData getMatchingRenderData(Projection projection)
    {
        myRenderDataLock.readLock().lock();
        try
        {
            if (myRenderData != null && Utilities.sameInstance(myRenderData.getProjection(), projection))
            {
                return myRenderData;
            }

            for (RenderData data : myPendingProjectionData)
            {
                if (Utilities.sameInstance(projection, data.getProjection()))
                {
                    return data;
                }
            }
        }
        finally
        {
            myRenderDataLock.readLock().unlock();
        }
        return null;
    }

    /**
     * Get the pendingProjectionData.
     *
     * @return the pendingProjectionData
     */
    public synchronized List<RenderData> getPendingProjectionData()
    {
        return New.list(myPendingProjectionData);
    }

    /**
     * Get the renderData.
     *
     * @return the renderData
     */
    public synchronized RenderData getRenderData()
    {
        return myRenderData;
    }

    @Override
    public boolean handleProjectionChanged(ProjectionChangedEvent evt)
    {
        return false;
    }

    @Override
    public boolean handleViewChanged(Viewer view, ViewChangeSupport.ViewChangeType type)
    {
        return false;
    }

    @Override
    public void preRender(Collection<? extends T> input, Collection<? extends T> drawable, Collection<? extends T> pickable,
            PickManager pickManager, ModelDataRetriever<T> dataRetriever, Projection projection)
    {
        if (isLoggingEnabled(Level.TRACE))
        {
            log(Level.TRACE, toString() + " is pre-rendering " + input.size() + " geometries.");
        }
    }

    @Override
    public void render(RenderContext renderContext, Collection<? extends T> input, Collection<? super T> rejected,
            PickManager pickManager, MapContext<?> mapContext, ModelDataRetriever<T> dataRetriever, boolean pushAttributes)
    {
        if (isLoggingEnabled(Level.TRACE))
        {
            log(Level.TRACE, new StringBuilder(128).append("Rendering (").append(renderContext.getRenderMode()).append(") ")
                    .append(input.size()).append(" geometries of type ").append(getType()).toString());
        }

        if (pushAttributes && getAttribBits() != -1)
        {
            renderContext.getGL().getGL2().glPushAttrib(getAttribBits());
        }
        if (pushAttributes && getClientAttribBits() != -1)
        {
            renderContext.getGL().getGL2().glPushClientAttrib(getClientAttribBits());
        }

        try
        {
            doRender(renderContext, input, rejected, pickManager, mapContext, dataRetriever);
        }
        finally
        {
            if (pushAttributes && getClientAttribBits() != -1)
            {
                renderContext.getGL().getGL2().glPopClientAttrib();
            }
            if (pushAttributes && getAttribBits() != -1)
            {
                renderContext.getGL().getGL2().glPopAttrib();
            }
        }
    }

    /**
     * Set the renderData. This adds the render data to the pending set if the
     * render data is not for the current projection.
     *
     * @param renderData the renderData to set.
     */
    public void setRenderData(RenderData renderData)
    {
        myRenderDataLock.writeLock().lock();
        try
        {
            if (myRenderData == null || renderData.getProjection() == null
                    || Utilities.sameInstance(myRenderData.getProjection(), renderData.getProjection()))
            {
                // NOTE: if myRenderData is null, but the projection renderData
                // was produced with doesn't match the current projection, this
                // will result in rendering the wrong projection until the other
                // renderers catch up.
                myRenderData = renderData;
            }
            else
            {
                myPendingProjectionData.add(renderData);
            }
        }
        finally
        {
            myRenderDataLock.writeLock().unlock();
        }

        notifyListeners(renderData.getProjection());
    }

    @Override
    public void switchToProjection(Projection projectionSnapshot)
    {
        if (!myPendingProjectionData.isEmpty())
        {
            myRenderDataLock.writeLock().lock();
            try
            {
                Iterator<RenderData> iter = myPendingProjectionData.iterator();
                while (iter.hasNext())
                {
                    RenderData data = iter.next();
                    if (data.getProjection().getActivationTimestamp() < projectionSnapshot.getActivationTimestamp())
                    {
                        iter.remove();
                    }
                    else if (Utilities.sameInstance(data.getProjection(), projectionSnapshot))
                    {
                        myRenderData = data;
                        iter.remove();
                        break;
                    }
                }
            }
            finally
            {
                myRenderDataLock.writeLock().unlock();
            }
        }
    }

    /**
     * Worker method that renders the geometries to OpenGL.
     *
     * @param rc The render context.
     * @param input The input geometries.
     * @param rejected The output collection of geometries that cannot be
     *            rendered due to lack of cached information.
     * @param pickManager The pick manager.
     * @param mapContext The map context.
     * @param dataRetriever The model data retriever.
     */
    protected abstract void doRender(RenderContext rc, Collection<? extends T> input, Collection<? super T> rejected,
            PickManager pickManager, MapContext<?> mapContext, ModelDataRetriever<T> dataRetriever);

    /**
     * Get the geometry cache.
     *
     * @return the cache
     */
    protected CacheProvider getCache()
    {
        return myCache;
    }

    /**
     * Get the logger for the concrete renderer. Each concrete renderer must
     * implement this so that the logger for the concrete class is used. This
     * allows more fine control of the log output.
     *
     * @return The logger.
     */
    protected abstract Logger getLogger();

    /**
     * Get the picked geometries that are also in the input collection.
     *
     * @param input The input collection.
     * @param pickManager The pick manager.
     * @return The picked geometries.
     */
    protected Collection<T> getPickedGeometries(Collection<? extends T> input, PickManager pickManager)
    {
        Set<Geometry> pickedGeometries = pickManager.getPickedGeometries();
        if (pickedGeometries.isEmpty())
        {
            return Collections.emptySet();
        }
        else
        {
            Collection<T> pickedGeomsToRender = null;
            for (Geometry geometry : pickedGeometries)
            {
                if (getType().isInstance(geometry) && input.contains(geometry))
                {
                    @SuppressWarnings("unchecked")
                    T cast = (T)geometry;
                    pickedGeomsToRender = CollectionUtilities.lazyAdd(cast, pickedGeomsToRender);
                }
            }
            return pickedGeomsToRender == null ? Collections.<T>emptySet() : pickedGeomsToRender;
        }
    }

    /**
     * Determine if any debug feature is on.
     *
     * @return {@code true} if any debug feature is on.
     */
    protected boolean isAnyDebugFeatureOn()
    {
        return ourDebugFeatures != null;
    }

    /**
     * Determine if a debug feature is on.
     *
     * @param feature The name of the feature.
     * @return <code>true</code> if the feature is on.
     */
    protected boolean isDebugFeatureOn(String feature)
    {
        return ourDebugFeatures != null && ourDebugFeatures.contains(feature);
    }

    /**
     * Determine if the given level is active in the logger for the concrete
     * renderer.
     *
     * @param level The logging level to check.
     * @return {@code true} if the level is enabled.
     */
    protected boolean isLoggingEnabled(Level level)
    {
        return getLogger().isEnabledFor(level);
    }

    /**
     * Send a message to the logging subsystem at the given level.
     *
     * @param level The logging level.
     * @param msg The message.
     */
    protected void log(Level level, String msg)
    {
        getLogger().log(level, msg);
    }

    /**
     * Notify the listeners that a projection is ready to render.
     *
     * @param projection The projection which is ready to render.
     */
    private void notifyListeners(final Projection projection)
    {
        myProjectionReadySupport.notifyListeners(new Callback<ProjectionReadyListener>()
        {
            @Override
            public void notify(ProjectionReadyListener listener)
            {
                listener.projectionReady(projection);
            }
        });
    }

    /**
     * Interface for factories that create renderers.
     *
     * @param <T> The type of geometry rendered by this factory's renderers.
     */
    public abstract static class Factory<T extends Geometry> implements GeometryRenderer.Factory<T>
    {
        /** The cache provider for the renderer. */
        private CacheProvider myCache;

        @Override
        public abstract GeometryRenderer<T> createRenderer();

        /**
         * Get the cache provider to be used by the renderer.
         *
         * @return The cache provider.
         */
        public CacheProvider getCache()
        {
            return myCache;
        }

        @Override
        public Set<? extends String> getCapabilities()
        {
            return Collections.emptySet();
        }

        @Override
        public void setCache(CacheProvider cache)
        {
            myCache = cache;
        }
    }

    /**
     * Interface for model data that describes how to draw geometries.
     */
    public interface ModelData
    {
    }

    /**
     * Interface for a facility that can get model data for a geometry.
     *
     * @param <T> The type of geometry.
     */
    @FunctionalInterface
    public interface ModelDataRetriever<T extends Geometry>
    {
        /**
         * Get the model data for the geometry.
         *
         * @param geom The geometry.
         * @param projectionSnapshot If non-null this snapshot will be used when
         *            creating model data.
         * @param override If non-null, this partial model data may be used to
         *            generate the returned model data.
         * @param timeBudget The time budget for retrieving the model data. This
         *            may be ignored, depending on the implementation.
         * @return The model data.
         */
        ModelData getModelData(T geom, Projection projectionSnapshot, AbstractRenderer.ModelData override, TimeBudget timeBudget);
    }

    /**
     * Interface for listeners interested in when this renderer is ready to
     * render projections.
     */
    @FunctionalInterface
    public interface ProjectionReadyListener
    {
        /**
         * Callback for when a projection is ready to render.
         *
         * @param projection The projection which is ready to render.
         */
        void projectionReady(Projection projection);
    }

    /** Data used for rendering. */
    protected static class RenderData
    {
        /** The projection associated with this data. */
        private final Projection myProjection;

        /**
         * Constructor.
         *
         * @param projection The projection used to generate this data.
         */
        public RenderData(Projection projection)
        {
            myProjection = projection;
        }

        /**
         * Get the projection used to generate this data.
         *
         * @return The projection.
         */
        public Projection getProjection()
        {
            return myProjection;
        }
    }

    /** Grouped sets of buffers for rendering during a single rendering pass. */
    protected abstract static class TimeRenderData extends RenderData
    {
        /** The time span associated with this render data. */
        private final TimeSpan myGroupTimeSpan;

        /**
         * Constructor.
         *
         * @param projection The projection used to generate this data.
         * @param groupTimeSpan The time span associated with this render data.
         */
        public TimeRenderData(Projection projection, TimeSpan groupTimeSpan)
        {
            super(projection);
            myGroupTimeSpan = groupTimeSpan;
        }

        /**
         * Get the time span associated with this render data, if any.
         *
         * @return The groupTimeSpan.
         */
        public TimeSpan getGroupTimeSpan()
        {
            return myGroupTimeSpan;
        }
    }
}
