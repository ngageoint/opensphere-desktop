package io.opensphere.core.pipeline.renderer.buffered;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.apache.log4j.Logger;

import io.opensphere.core.TimeManager;
import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.StippleModelConfig;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.processor.PolylineModelData;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.pipeline.renderer.TimeFilteringRenderer;
import io.opensphere.core.pipeline.util.GL2Utilities;
import io.opensphere.core.pipeline.util.GLUtilities;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * Renderer for polylines that uses server-side buffer objects.
 */
public class PolylineRendererBuffered extends AbstractRenderer<PolylineGeometry> implements TimeFilteringRenderer
{
    /** Bits used for {@link GL2#glPushAttrib(int)}. */
    private static final int ATTRIB_BITS = GL2.GL_ENABLE_BIT | GL.GL_COLOR_BUFFER_BIT | GL2.GL_CURRENT_BIT | GL2.GL_LINE_BIT;

    /** Bits used for {@link GL2#glPushClientAttrib(int)}. */
    private static final int CLIENT_ATTRIB_BITS = GL2.GL_CLIENT_VERTEX_ARRAY_BIT;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PolylineRendererBuffered.class);

    /** The helper for faded rendering. */
    private final FadedRenderingHelper myFadedRenderingHelper = new FadedRenderingHelper();

    /**
     * The time span that represents the earliest and latest times that might be
     * in the geometry constraints.
     */
    private volatile TimeSpan myGroupTimeSpan;

    /**
     * The previously used model data during last preRender call.
     */
    private final Set<PolylineModelData> myPreviouslyUsedKeys = Collections.synchronizedSet(New.<PolylineModelData>set());

    /**
     * The used model data during the current preRender call.
     */
    private final Set<PolylineModelData> myUsedKeys = Collections.synchronizedSet(New.<PolylineModelData>set());

    /**
     * Construct the renderer.
     *
     * @param cache The geometry cache.
     */
    public PolylineRendererBuffered(CacheProvider cache)
    {
        super(cache);
    }

    @Override
    public void close()
    {
        myUsedKeys.clear();
        cleanBuffersFromCache();
        super.close();
    }

    @Override
    public void doRender(final RenderContext rc, final Collection<? extends PolylineGeometry> input,
            final Collection<? super PolylineGeometry> rejected, final PickManager pickManager, MapContext<?> mapContext,
            final ModelDataRetriever<PolylineGeometry> dataRetriever)
    {
        final PolylineRenderData renderData = (PolylineRenderData)getRenderData();
        if (renderData == null)
        {
            return;
        }

        try
        {
            if (rc.getRenderMode() == RenderMode.DRAW)
            {
                rc.getGL().glEnable(GL.GL_BLEND);
                rc.getGL().glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            }
            else
            {
                rc.getGL().glDisable(GL.GL_LINE_SMOOTH);
            }

            myFadedRenderingHelper.renderEachTimeSpan(rc, renderData.getGroupTimeSpan(),
                () -> doRender(rc, input, rejected, pickManager, dataRetriever, renderData));
        }
        finally
        {
            rc.getShaderRendererUtilities().cleanupShaders(rc.getGL());
            rc.popAttributes();
        }
    }

    @Override
    public int getAttribBits()
    {
        return ATTRIB_BITS;
    }

    @Override
    public int getClientAttribBits()
    {
        return CLIENT_ATTRIB_BITS;
    }

    @Override
    public Class<?> getType()
    {
        return PolylineGeometry.class;
    }

    @Override
    public void preRender(Collection<? extends PolylineGeometry> input, Collection<? extends PolylineGeometry> drawable,
            Collection<? extends PolylineGeometry> pickable, PickManager pickManager,
            ModelDataRetriever<PolylineGeometry> dataRetriever, Projection projection)
    {
        super.preRender(input, drawable, pickable, pickManager, dataRetriever, projection);

        myUsedKeys.clear();

        Map<PolylineGeometry, PolylineDataBuffered> renderDataMap = New.weakMap(input.size());
        // make sure this doesn't change
        TimeSpan groupTimeSpan = myGroupTimeSpan;
        for (PolylineGeometry geom : input)
        {
            PolylineDataBuffered bufferedData = getBufferedData(geom, dataRetriever, projection, TimeBudget.ZERO, pickManager,
                    groupTimeSpan);

            if (bufferedData != null)
            {
                renderDataMap.put(geom, bufferedData);
            }
        }

        cleanBuffersFromCache();

        setRenderData(new PolylineRenderData(renderDataMap, projection, groupTimeSpan));
    }

    @Override
    public boolean setGroupInterval(TimeSpan span)
    {
        if (Objects.equals(span, myGroupTimeSpan))
        {
            return false;
        }
        myGroupTimeSpan = span;
        getCache().clearCacheAssociations(PolylineDataBuffered.class);
        return true;
    }

    @Override
    public void setTimeManager(TimeManager timeManager)
    {
        myFadedRenderingHelper.setTimeManager(timeManager);
    }

    @Override
    protected Logger getLogger()
    {
        return LOGGER;
    }

    /**
     * Removes the unused buffers from the cache.
     */
    private void cleanBuffersFromCache()
    {
        synchronized (myUsedKeys)
        {
            CacheProvider cache = getCache();
            for (PolylineModelData key : myPreviouslyUsedKeys)
            {
                if (!myUsedKeys.contains(key))
                {
                    cache.clearCacheAssociation(key, PolylineDataBuffered.class);
                }
            }

            myPreviouslyUsedKeys.clear();
            myPreviouslyUsedKeys.addAll(myUsedKeys);
        }
    }

    /**
     * Render each of the render data buffers.
     *
     * @param rc The render context.
     * @param input The input geometries.
     * @param rejected Output collection of geometries that cannot be rendered.
     * @param pickManager The pick manager.
     * @param dataRetriever The data retriever.
     * @param renderData The render data.
     */
    private void doRender(RenderContext rc, Collection<? extends PolylineGeometry> input,
            Collection<? super PolylineGeometry> rejected, PickManager pickManager,
            ModelDataRetriever<PolylineGeometry> dataRetriever, PolylineRenderData renderData)
    {
        myFadedRenderingHelper.initIntervalFilter(rc, false, null);

        boolean lastSmoothing = false;
        float lastWidth = 0f;
        StippleModelConfig lastStipple = null;
        try
        {
            for (PolylineGeometry geom : input)
            {
                if (rc.getRenderMode() == RenderMode.DRAW)
                {
                    // For line strips, enabling line smoothing will cause some
                    // pixels to be missed (dropped out) at the location of the
                    // vertices. This might be occurring when the transformed
                    // position lands on a whole pixel and GL tries to avoid
                    // double blending that position.
                    lastSmoothing = GLUtilities.enableSmoothingIfNecessary(rc.getGL(), geom, lastSmoothing);
                }

                PolylineDataBuffered line = getLine(pickManager, dataRetriever, renderData, geom);
                if (line == null)
                {
                    rejected.add(geom);
                }
                else
                {
                    PolylineRenderProperties renderProperties = geom.getRenderProperties();
                    lastWidth = GLUtilities.glLineWidth(rc.getGL(), rc.getRenderMode() == RenderMode.PICK
                            ? renderProperties.getWidth() + 1f : renderProperties.getWidth(), lastWidth);

                    lastStipple = GL2Utilities.glLineStipple(rc.getGL().getGL2(), rc.getRenderMode(),
                            renderProperties.getStipple(), lastStipple);

                    GL2Utilities.glColor(rc, pickManager, geom, renderProperties);
                    rc.glDepthMask(renderProperties.isObscurant());

                    GL2Utilities.renderWithTransform(rc, geom.getRenderProperties().getTransform(),
                        () -> line.draw(rc, GL.GL_LINE_STRIP));

                    if (geom instanceof PolygonGeometry
                            && ((PolygonRenderProperties)renderProperties).getFillColorRenderProperties() != null)
                    {
                        GL2Utilities.glColor(rc, pickManager, geom,
                                ((PolygonRenderProperties)renderProperties).getFillColorRenderProperties());
                        line.draw(rc, GL2.GL_POLYGON);
                    }
                }
            }
        }
        finally
        {
            rc.popAttributes();
        }
    }

    /**
     * Retrieve the buffer data from the cache or try to create it if it is
     * absent from the cache.
     *
     * @param geom The geometry.
     * @param dataRetriever A retriever for the model data.
     * @param projectionSnapshot The projection snapshot to use when generating
     *            any missing model data.
     * @param timeBudget The time budget.
     * @param pickManager The pick manager for getting pick colors.
     * @param groupTimeSpan If non-null this will be used to upload time
     *            constraint information.
     * @return The buffer data.
     */
    private PolylineDataBuffered getBufferedData(PolylineGeometry geom, ModelDataRetriever<PolylineGeometry> dataRetriever,
            Projection projectionSnapshot, TimeBudget timeBudget, PickManager pickManager, TimeSpan groupTimeSpan)
    {
        PolylineModelData modelData = (PolylineModelData)dataRetriever.getModelData(geom, projectionSnapshot, null, timeBudget);
        myUsedKeys.add(modelData);
        PolylineDataBuffered bufferedData = null;
        if (modelData != null)
        {
            bufferedData = getCache().getCacheAssociation(modelData, PolylineDataBuffered.class);
            if (bufferedData == null)
            {
                bufferedData = new PolylineDataBuffered(modelData, groupTimeSpan);

                // If the buffered data is not already in the cache, add it
                // to enable the GPU memory to be cleaned up.
                getCache().putCacheAssociation(modelData, bufferedData, PolylineDataBuffered.class, 0L,
                        bufferedData.getSizeGPU());
            }
        }
        return bufferedData;
    }

    /**
     * Get the line from the render data or request it from the data retriever.
     *
     * @param pickManager The pick manager.
     * @param dataRetriever The data retriever.
     * @param renderData The render data.
     * @param geom The geometry.
     * @return The line.
     */
    private PolylineDataBuffered getLine(PickManager pickManager, ModelDataRetriever<PolylineGeometry> dataRetriever,
            PolylineRenderData renderData, PolylineGeometry geom)
    {
        PolylineDataBuffered line = renderData.getData().get(geom);
        if (line == null)
        {
            line = getBufferedData(geom, dataRetriever, renderData.getProjection(), TimeBudget.ZERO, pickManager,
                    myGroupTimeSpan);
            if (line != null)
            {
                renderData.getData().put(geom, line);
            }
        }
        return line;
    }

    /**
     * A factory for creating this renderer.
     */
    public static class Factory extends AbstractRenderer.Factory<PolylineGeometry>
    {
        @Override
        public GeometryRenderer<PolylineGeometry> createRenderer()
        {
            return new PolylineRendererBuffered(getCache());
        }

        @Override
        public Collection<? extends BufferDisposalHelper<?>> getDisposalHelpers()
        {
            return Collections.singleton(BufferDisposalHelper.create(PolylineDataBuffered.class, getCache()));
        }

        @Override
        public Class<? extends Geometry> getType()
        {
            return PolylineGeometry.class;
        }

        @Override
        public boolean isViable(RenderContext rc, Collection<String> warnings)
        {
            return rc.is20Available(getClass().getEnclosingClass().getSimpleName() + " is not viable", warnings);
        }
    }

    /** Data used for rendering. */
    private static class PolylineRenderData extends TimeRenderData
    {
        /** The map of geometries to data. */
        private final Map<PolylineGeometry, PolylineDataBuffered> myData;

        /**
         * Constructor.
         *
         * @param data The map of geometries to data.
         * @param projection The projection snapshot used to generate this data.
         * @param groupTimeSpan The time span associated with this render data.
         */
        public PolylineRenderData(Map<PolylineGeometry, PolylineDataBuffered> data, Projection projection, TimeSpan groupTimeSpan)
        {
            super(projection, groupTimeSpan);
            myData = data;
        }

        /**
         * Get the data.
         *
         * @return the data
         */
        public Map<PolylineGeometry, PolylineDataBuffered> getData()
        {
            return myData;
        }
    }
}
