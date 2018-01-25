package io.opensphere.core.pipeline.renderer.special;

import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.GeometryGroupGeometry;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.pipeline.renderer.buffered.BufferDisposalHelper;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * This renderer is pipeline independent. For <code>GeometryGroup</code>s which
 * are ready have the distributor for the geometry have its processors render.
 */
public class GeometryGroupRenderer extends AbstractRenderer<GeometryGroupGeometry>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GeometryGroupRenderer.class);

    /**
     * Construct me.
     *
     * @param cache cache for my geometries.
     */
    protected GeometryGroupRenderer(CacheProvider cache)
    {
        super(cache);
    }

    @Override
    public void doRender(RenderContext rc, Collection<? extends GeometryGroupGeometry> input,
            Collection<? super GeometryGroupGeometry> rejected, PickManager pickManager, MapContext<?> mapContext,
            AbstractRenderer.ModelDataRetriever<GeometryGroupGeometry> dataRetriever)
    {
        // Do nothing. The sub-geometries are rendered by the processor and the
        // associated tile is rendered by the tile renderer.
    }

    @Override
    public int getAttribBits()
    {
        return -1;
    }

    @Override
    public int getClientAttribBits()
    {
        return -1;
    }

    @Override
    public Class<?> getType()
    {
        return GeometryGroupGeometry.class;
    }

    @Override
    protected Logger getLogger()
    {
        return LOGGER;
    }

    /** A factory for creating this renderer. */
    public static class Factory extends AbstractRenderer.Factory<GeometryGroupGeometry>
    {
        @Override
        public GeometryRenderer<GeometryGroupGeometry> createRenderer()
        {
            return new GeometryGroupRenderer(getCache());
        }

        @Override
        public Collection<? extends BufferDisposalHelper<?>> getDisposalHelpers()
        {
            return Collections.emptySet();
        }

        @Override
        public Class<? extends Geometry> getType()
        {
            return GeometryGroupGeometry.class;
        }

        @Override
        public boolean isViable(RenderContext rc, Collection<String> warnings)
        {
            return true;
        }
    }
}
