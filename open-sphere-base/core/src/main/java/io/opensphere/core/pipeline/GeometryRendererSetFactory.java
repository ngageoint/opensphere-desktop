package io.opensphere.core.pipeline;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.processor.GeometryRendererSet;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.pipeline.renderer.immediate.DisplayListRenderer;
import io.opensphere.core.pipeline.renderer.immediate.GeometryRendererImmediate;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;

/**
 * Factory for {@link GeometryRendererSet}s.
 */
public class GeometryRendererSetFactory
{
    /** Collection of warnings about renderer inviability. */
    private final Collection<String> myWarnings = New.collection();

    /**
     * Initialize the renderer factories. Test the available factories with the
     * given render context to determine if they are viable.
     *
     * @param rc The render context (used for testing the renderers).
     * @param cache The cache provider to be used by the renderers.
     * @param useDisplayLists If display lists should be used.
     * @param safemode If safe mode is enabled, i.e., the most basic renderers
     *            should be used. This overrides the useDisplayLists setting.
     * @return The renderer set.
     */
    public GeometryRendererSet createGeometryRendererSet(RenderContext rc, CacheProvider cache, boolean useDisplayLists,
            boolean safemode)
    {
        Map<Class<? extends Geometry>, List<GeometryRenderer.Factory<? extends Geometry>>> map = New.map();
        for (GeometryRenderer.Factory<?> factory : ServiceLoader.load(GeometryRenderer.Factory.class))
        {
            if (factory.isViable(rc, myWarnings))
            {
                factory.setCache(cache);

                if (safemode)
                {
                    map.put(factory.getType(), Collections.<GeometryRenderer.Factory<?>>singletonList(factory));
                }
                else
                {
                    if (useDisplayLists && factory instanceof GeometryRendererImmediate.Factory)
                    {
                        CollectionUtilities.multiMapAdd(map, factory.getType(),
                                DisplayListRenderer.createFactory((GeometryRendererImmediate.Factory<? extends Geometry>)factory),
                                false);
                    }

                    CollectionUtilities.multiMapAdd(map, factory.getType(), factory, false);
                }
            }
        }

        return new GeometryRendererSet(map);
    }

    /**
     * Accessor for the warnings.
     *
     * @return The warnings.
     */
    public Collection<? extends String> getWarnings()
    {
        return New.unmodifiableCollection(myWarnings);
    }
}
