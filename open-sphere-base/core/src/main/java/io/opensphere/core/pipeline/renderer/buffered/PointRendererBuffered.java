package io.opensphere.core.pipeline.renderer.buffered;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.jogamp.opengl.GL;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.geometry.renderproperties.PointSizeRenderProperty;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;

/**
 * Renderer for point geometries that uses server-side buffering.
 *
 * @param <T> The geometry type.
 */
public class PointRendererBuffered<T extends PointGeometry> extends AbstractPointRendererBuffered<T>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PointRendererBuffered.class);

    /**
     * In order to keep the buffered data in the cache so that it is correctly
     * accounted for for GPU memory usage we need a key for the buffer which is
     * unique. This set also prevents the keys from being garbage collected
     * which would leave the buffered data stranded in the cache.
     */
    private final Collection<Pair<PointRendererBuffered<T>, RenderKey>> myBufferKeys = New.set();

    /**
     * Construct the renderer.
     *
     * @param cache The geometry cache.
     */
    public PointRendererBuffered(CacheProvider cache)
    {
        super(cache);
    }

    @Override
    public Class<?> getType()
    {
        return PointGeometry.class;
    }

    @Override
    public void preRender(Collection<? extends T> input, Collection<? extends T> drawable, Collection<? extends T> pickable,
            PickManager pickManager, ModelDataRetriever<T> dataRetriever, Projection projection)
    {
        super.preRender(input, drawable, pickable, pickManager, dataRetriever, projection);

        Map<RenderKey, PointDataBuffered> buffers = New.map();
        Map<RenderKey, List<T>> sizeSorted = groupPoints(input);
        // make sure this doesn't change
        TimeSpan groupTimeSpan = getGroupInterval();

        for (Entry<RenderKey, List<T>> entry : sizeSorted.entrySet())
        {
            List<T> list = entry.getValue();
            PointDataBuffered pointData = new PointDataBuffered(list, false, pickManager, dataRetriever, projection,
                    groupTimeSpan);
            buffers.put(entry.getKey(), pointData);
        }

        setRenderData(new PointRenderData(buffers, projection, groupTimeSpan));
    }

    /**
     * Remove the buffered data from the cache so that the GPU memory can be
     * freed.
     */
    @Override
    protected void cleanBuffersFromCache()
    {
        synchronized (myBufferKeys)
        {
            if (!myBufferKeys.isEmpty())
            {
                CacheProvider cache = getCache();
                for (Pair<PointRendererBuffered<T>, RenderKey> key : myBufferKeys)
                {
                    cache.clearCacheAssociation(key, PointDataBuffered.class);
                }
                myBufferKeys.clear();
            }
        }
    }

    @Override
    protected void doRenderPoints(RenderContext rc, Collection<? extends T> input, Collection<? super T> rejects, PickManager pickManager,
            ModelDataRetriever<T> dataRetriever, TimeRenderData renderData)
    {
        getFadedRenderingHelper().initIntervalFilter(rc, false, null);

        try
        {
            for (Entry<RenderKey, PointDataBuffered> entry : ((PointRenderData)renderData).getBuffers().entrySet())
            {
                PointDataBuffered pointData = entry.getValue();

                rc.getGL().getGL2().glPointSize(entry.getKey().getSizeProperty().getSize());
                rc.setPointSmoothing(entry.getKey().getRoundnessProperty().isRound());
                rc.glDepthMask(entry.getKey().isObscurant());
                pointData.draw(rc, GL.GL_POINTS);

                Pair<PointRendererBuffered<T>, RenderKey> key = new Pair<PointRendererBuffered<T>, RenderKey>(this,
                        entry.getKey());
                if (getCache().getCacheAssociation(key, PointDataBuffered.class) == null)
                {
                    synchronized (myBufferKeys)
                    {
                        // If the key is not already in the cache, add it to
                        // enable the GPU memory to be cleaned up.
                        myBufferKeys.add(key);
                        getCache().putCacheAssociation(key, pointData, PointDataBuffered.class, 0L, pointData.getSizeGPU());
                    }
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
     * Render geometries that are picked in the highlight color.
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
            Map<RenderKey, List<T>> sizeSorted = groupPoints(pickedGeomsToRender);
            for (Entry<RenderKey, List<T>> entry : sizeSorted.entrySet())
            {
                List<T> pointData = entry.getValue();
                PointSizeRenderProperty sizeProps = entry.getKey().getSizeProperty();

                PointDataBuffered highlightData = new PointDataBuffered(pointData, true, pickManager, dataRetriever,
                        renderData.getProjection(), renderData.getGroupTimeSpan());
                if (rc.getRenderMode() == RenderMode.DRAW)
                {
                    rc.getGL().getGL2().glPointSize(sizeProps.getHighlightSize());
                    rc.getGL().glDisable(GL.GL_DEPTH_TEST);
                }
                else
                {
                    // When rendering the pick color, render in the
                    // regular size so that nearby points are not
                    // occluded.
                    rc.getGL().getGL2().glPointSize(sizeProps.getSize());
                }
                rc.setPointSmoothing(entry.getKey().getRoundnessProperty().isRound());
                highlightData.draw(rc, GL.GL_POINTS);
                highlightData.dispose(rc.getGL());
            }
        }
    }

    /** A factory for creating this renderer. */
    public static class Factory extends AbstractPointRendererBuffered.Factory<PointGeometry>
    {
        @Override
        public GeometryRenderer<PointGeometry> createRenderer()
        {
            return new PointRendererBuffered<PointGeometry>(getCache());
        }

        @Override
        public Collection<? extends BufferDisposalHelper<?>> getDisposalHelpers()
        {
            return Collections.singleton(BufferDisposalHelper.create(PointDataBuffered.class, getCache()));
        }

        @Override
        public Class<? extends Geometry> getType()
        {
            return PointGeometry.class;
        }
    }

    /** Grouped sets of buffers for rendering during a single rendering pass. */
    protected static class PointRenderData extends TimeRenderData
    {
        /** Buffers with their associated size properties. */
        private final Map<RenderKey, PointDataBuffered> myBuffers;

        /**
         * Constructor.
         *
         * @param buffers Buffers with their associated size properties.
         * @param projection The projection used to generate this data.
         * @param groupTimeSpan The time span associated with this render data.
         */
        public PointRenderData(Map<RenderKey, PointDataBuffered> buffers, Projection projection, TimeSpan groupTimeSpan)
        {
            super(projection, groupTimeSpan);
            myBuffers = buffers;
        }

        /**
         * Get the buffers.
         *
         * @return the buffers
         */
        public Map<RenderKey, PointDataBuffered> getBuffers()
        {
            return myBuffers;
        }
    }
}
