package io.opensphere.core.pipeline.renderer.immediate;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.pipeline.util.DisposalHelper;
import io.opensphere.core.pipeline.util.RenderContext;

/**
 * Marker interface for immediate-mode renderers.
 *
 * @param <T> The specific type of geometry that the renderer knows how to
 *            render.
 */
public interface GeometryRendererImmediate<T extends Geometry> extends GeometryRenderer<T>
{
    /**
     * Clean up any shaders which were loaded in initializeShaders(). This
     * should be done outside of the display list when display lists are used.
     *
     * @param rc The current render context.
     * @param input The geometries which were rendered.
     */
    void cleanupShaders(RenderContext rc, Collection<? extends T> input);

    /**
     * Set up an shaders which are required for the renderer to function
     * properly. This should be done outside of the display list when display
     * lists are used.
     *
     * @param rc The current render context.
     * @param input The geometries which will be rendered.
     */
    void initializeShaders(RenderContext rc, Collection<? extends T> input);

    /**
     * Interface for factories that create renderers.
     *
     * @param <T> The type of geometry rendered by this factory's renderers.
     */
    abstract class Factory<T extends Geometry> implements GeometryRenderer.Factory<T>
    {
        /** The cache provider for the renderer. */
        private CacheProvider myCache;

        @Override
        public abstract GeometryRendererImmediate<T> createRenderer();

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
        public Collection<? extends DisposalHelper> getDisposalHelpers()
        {
            return Collections.emptySet();
        }

        @Override
        public void setCache(CacheProvider cache)
        {
            myCache = cache;
        }
    }
}
