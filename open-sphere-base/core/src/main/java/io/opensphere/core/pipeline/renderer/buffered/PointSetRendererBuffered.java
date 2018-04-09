package io.opensphere.core.pipeline.renderer.buffered;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.jogamp.opengl.GL;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PointSetGeometry;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.pipeline.util.GL2Utilities;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.collections.New;

/**
 * Renderer for point sets that uses server-side buffering.
 *
 * @param <T> The geometry type.
 */
public class PointSetRendererBuffered<T extends PointSetGeometry> extends AbstractPointRendererBuffered<T>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PointSetRendererBuffered.class);

    /**
     * Construct the renderer.
     *
     * @param cache The geometry cache.
     */
    public PointSetRendererBuffered(CacheProvider cache)
    {
        super(cache);
    }

    @Override
    public void close()
    {
    }

    @Override
    public Class<?> getType()
    {
        return PointSetGeometry.class;
    }

    @Override
    public void preRender(Collection<? extends T> input, Collection<? extends T> drawable, Collection<? extends T> pickable,
            PickManager pickManager, ModelDataRetriever<T> dataRetriever, Projection projection)
    {
        super.preRender(input, drawable, pickable, pickManager, dataRetriever, projection);

        Map<PointSetGeometry, PointSetDataBuffered> buffers = New.map();
        // make sure this doesn't change
        TimeSpan groupTimeSpan = getGroupInterval();
        for (T geom : input)
        {
            PointSetDataBuffered pointData = new PointSetDataBuffered(geom, false, pickManager, dataRetriever, projection,
                    groupTimeSpan);
            buffers.put(geom, pointData);
        }
        setRenderData(new PointSetRenderData(buffers, projection, groupTimeSpan));
    }

    @Override
    protected void doRenderPoints(RenderContext rc, Collection<? extends T> input, Collection<? super T> rejects,
            PickManager pickManager, ModelDataRetriever<T> dataRetriever, TimeRenderData renderData)
    {
        try
        {
            for (T geom : input)
            {
                PointSetDataBuffered pointData = ((PointSetRenderData)getRenderData()).getBuffers().get(geom);
                if (pointData != null)
                {
                    rc.setPointSmoothing(geom.getRenderProperties().getRoundnessRenderProperty().isRound());
                    rc.getGL().getGL2().glPointSize(geom.getRenderProperties().getSize());
                    GL2Utilities.glColor(rc, pickManager, geom);
                    rc.glDepthMask(geom.getRenderProperties().isObscurant());
                    pointData.draw(rc, GL.GL_POINTS);

                    // If the buffered data is not already in the cache, add it
                    // to enable the GPU memory to be cleaned up.
                    getCache().putCacheAssociation(geom, pointData, PointSetDataBuffered.class, 0L, pointData.getSizeGPU());
                }
            }
        }
        finally
        {
            rc.popAttributes();
        }

        if (!pickManager.getPickedGeometries().isEmpty())
        {
            renderHighlights(rc, input, pickManager, dataRetriever, renderData);
        }
    }

    @Override
    protected Logger getLogger()
    {
        return LOGGER;
    }

    /**
     * Render the highlighted geometries.
     *
     * @param rc The render context.
     * @param input The geometries being rendered.
     * @param pickManager The pick manager.
     * @param dataRetriever The data retriever.
     * @param renderData The render data.
     */
    protected void renderHighlights(RenderContext rc, Collection<? extends T> input, PickManager pickManager,
            ModelDataRetriever<T> dataRetriever, TimeRenderData renderData)
    {
        Collection<T> pickedGeomsToRender = getPickedGeometries(input, pickManager);
        if (!pickedGeomsToRender.isEmpty())
        {
            for (T geom : pickedGeomsToRender)
            {
                PointSetDataBuffered highlightData = new PointSetDataBuffered(geom, true, pickManager, dataRetriever,
                        renderData.getProjection(), renderData.getGroupTimeSpan());
                if (rc.getRenderMode() == RenderMode.DRAW)
                {
                    rc.getGL().getGL2().glPointSize(geom.getRenderProperties().getHighlightSize());
                    rc.getGL().glDisable(GL.GL_DEPTH_TEST);
                }
                else
                {
                    // When rendering the pick color, render in the
                    // regular size so that nearby points are not
                    // occluded.
                    rc.getGL().getGL2().glPointSize(geom.getRenderProperties().getSize());
                }
                highlightData.draw(rc, GL.GL_POINTS);
                highlightData.dispose(rc.getGL());
            }
        }
    }

    /** A factory for creating this renderer. */
    public static class Factory extends AbstractPointRendererBuffered.Factory<PointSetGeometry>
    {
        @Override
        public GeometryRenderer<PointSetGeometry> createRenderer()
        {
            return new PointSetRendererBuffered<>(getCache());
        }

        @Override
        public Collection<? extends BufferDisposalHelper<?>> getDisposalHelpers()
        {
            return Collections.singleton(BufferDisposalHelper.create(PointDataBuffered.class, getCache()));
        }

        @Override
        public Class<? extends Geometry> getType()
        {
            return PointSetGeometry.class;
        }
    }

    /** Grouped sets of buffers for rendering during a single rendering pass. */
    protected static class PointSetRenderData extends TimeRenderData
    {
        /** The buffers for the geometries. */
        private final Map<PointSetGeometry, PointSetDataBuffered> myBuffers;

        /**
         * Constructor.
         *
         * @param buffers The buffers associated with the geometries.
         * @param projection The projection used to generate this data.
         * @param groupTimeSpan The time span associated with this render data.
         */
        public PointSetRenderData(Map<PointSetGeometry, PointSetDataBuffered> buffers, Projection projection,
                TimeSpan groupTimeSpan)
        {
            super(projection, groupTimeSpan);
            myBuffers = buffers;
        }

        /**
         * Get the buffers.
         *
         * @return the buffers
         */
        public Map<PointSetGeometry, PointSetDataBuffered> getBuffers()
        {
            return myBuffers;
        }
    }
}
