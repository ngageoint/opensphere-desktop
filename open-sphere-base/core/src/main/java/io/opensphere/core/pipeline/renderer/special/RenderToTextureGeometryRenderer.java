package io.opensphere.core.pipeline.renderer.special;

import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.RenderToTextureGeometry;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.pipeline.renderer.buffered.BufferDisposalHelper;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * This renderer is pipeline independent. For
 * <code>RenderToTextureGeometry</code>s which are ready but do not have
 * textures for the associated tile geometry, the pick and render textures are
 * generated.
 */
public class RenderToTextureGeometryRenderer extends AbstractRenderer<RenderToTextureGeometry>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(RenderToTextureGeometryRenderer.class);

    /**
     * Construct me.
     *
     * @param cache cache for my geometries.
     */
    protected RenderToTextureGeometryRenderer(CacheProvider cache)
    {
        super(cache);
    }

    @Override
    public void doRender(RenderContext rc, Collection<? extends RenderToTextureGeometry> input,
            Collection<? super RenderToTextureGeometry> rejected, PickManager pickManager, MapContext<?> mapContext,
            AbstractRenderer.ModelDataRetriever<RenderToTextureGeometry> dataRetriever)
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
    public Class<RenderToTextureGeometry> getType()
    {
        return RenderToTextureGeometry.class;
    }

    @Override
    protected Logger getLogger()
    {
        return LOGGER;
    }

    /** A factory for creating this renderer. */
    public static class Factory extends AbstractRenderer.Factory<RenderToTextureGeometry>
    {
        @Override
        public GeometryRenderer<RenderToTextureGeometry> createRenderer()
        {
            return new RenderToTextureGeometryRenderer(getCache());
        }

        @Override
        public Collection<? extends BufferDisposalHelper<?>> getDisposalHelpers()
        {
            return Collections.emptySet();
        }

        @Override
        public Class<? extends RenderToTextureGeometry> getType()
        {
            return RenderToTextureGeometry.class;
        }

        @Override
        public boolean isViable(RenderContext rc, Collection<String> warnings)
        {
            return true;
        }
    }
}
