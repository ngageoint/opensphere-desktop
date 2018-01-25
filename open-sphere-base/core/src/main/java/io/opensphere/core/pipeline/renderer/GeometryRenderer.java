package io.opensphere.core.pipeline.renderer;

import java.util.Collection;
import java.util.Set;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.renderer.AbstractRenderer.ModelDataRetriever;
import io.opensphere.core.pipeline.renderer.AbstractRenderer.ProjectionReadyListener;
import io.opensphere.core.pipeline.util.DisposalHelper;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * This is the top-level interface to objects that know how to render geometries
 * using OpenGL.
 *
 * @param <T> The specific type of geometry that the renderer knows how to
 *            render.
 */
public interface GeometryRenderer<T extends Geometry>
{
    /**
     * Add a listener for when this renderer becomes ready to render a
     * projection.
     *
     * @param listener The listener interested in projection readiness.
     */
    void addProjectionReadyListener(ProjectionReadyListener listener);

    /** Handle any required cleanup. */
    void close();

    /**
     * Get the attributes for this renderer which should be pushed before
     * rendering and pop following rendering.
     *
     * @return the attribute bit mask.
     */
    int getAttribBits();

    /**
     * Get the client attributes for this renderer which should be pushed before
     * rendering and pop following rendering.
     *
     * @return the attribute bit mask.
     */
    int getClientAttribBits();

    /**
     * The specific type of geometry that this renderer handles.
     *
     * @return The geometry type.
     */
    Class<?> getType();

    /**
     * Method to be called when the projection had changed. In general, the
     * renderer will clear all cached information associated with its
     * geometries.
     *
     * @param evt An event describing what has changed.
     * @return <code>true</code> If the renderer changed due to the projection
     *         change.
     */
    boolean handleProjectionChanged(ProjectionChangedEvent evt);

    /**
     * Method to be called when the viewer had changed.
     *
     * @param view The viewer.
     * @param type The viewer update type.
     * @return <code>true</code> If the renderer changed due to the view change.
     */
    boolean handleViewChanged(Viewer view, ViewChangeSupport.ViewChangeType type);

    /**
     * This method will be called on a non-rendering thread prior to
     * {@link #render} being called for a set of geometries.
     *
     * @param input The input geometries.
     * @param drawable The subset of the input geometries that are drawable.
     * @param pickable The subset of the input geometries that are pickable.
     * @param pickManager The pick manager for getting pick colors.
     * @param dataRetriever The model data retriever.
     * @param projection The projection which matches the model data created for
     *            the input collection. Any new model data should match the
     *            projection as well.
     */
    void preRender(Collection<? extends T> input, Collection<? extends T> drawable, Collection<? extends T> pickable,
            PickManager pickManager, ModelDataRetriever<T> dataRetriever, Projection projection);

    /**
     * Render the geometries to OpenGL.
     *
     * @param renderContext The render context.
     * @param input The input geometries.
     * @param rejected The output collection of geometries that cannot be
     *            rendered due to lack of cached information.
     * @param pickManager The pick manager.
     * @param mapContext The map context.
     * @param dataRetriever The model data retriever.
     * @param pushAttributes When true push the GL attributes affected by this
     *            renderer before rendering and pop them when complete.
     */
    void render(RenderContext renderContext, Collection<? extends T> input, Collection<? super T> rejected,
            PickManager pickManager, MapContext<?> mapContext, ModelDataRetriever<T> dataRetriever, boolean pushAttributes);

    /**
     * Switch to using this projection snapshot.
     *
     * @param projectionSnapshot the projection snapshot to use.
     */
    void switchToProjection(Projection projectionSnapshot);

    /**
     * Interface for factories that create renderers.
     *
     * @param <T> The type of geometry rendered by this factory's renderers.
     */
    public interface Factory<T extends Geometry>
    {
        /**
         * Create a new renderer.
         *
         * @return The new renderer instance.
         */
        GeometryRenderer<T> createRenderer();

        /**
         * Get the rendering capabilities of this renderer.
         *
         * @return The capabilities.
         */
        Set<? extends String> getCapabilities();

        /**
         * Get the disposal helpers (if any) for this renderer.
         *
         * @return The disposal helpers.
         */
        Collection<? extends DisposalHelper> getDisposalHelpers();

        /**
         * The specific type of geometry that the renderers created by this
         * factory handle.
         *
         * @return The geometry type.
         */
        Class<? extends Geometry> getType();

        /**
         * Get if this renderer is viable for a given GL context. This tests
         * that all the GL extensions that the renderer requires are available
         * in the given context.
         *
         * @param rc The render context.
         * @param warnings Optional collection of warnings to be populated if
         *            the renderer is not viable.
         * @return {@code true} if the renderer is viable.
         */
        boolean isViable(RenderContext rc, Collection<String> warnings);

        /**
         * Set the cache provider to be used by the renderer.
         *
         * @param cache The cache provider.
         */
        void setCache(CacheProvider cache);
    }
}
