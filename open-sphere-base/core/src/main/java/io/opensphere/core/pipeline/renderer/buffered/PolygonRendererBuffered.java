package io.opensphere.core.pipeline.renderer.buffered;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.apache.log4j.Logger;

import io.opensphere.core.TimeManager;
import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.StippleModelConfig;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.processor.PolygonMeshData;
import io.opensphere.core.pipeline.processor.PolygonProcessor;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.pipeline.renderer.TimeFilteringRenderer;
import io.opensphere.core.pipeline.renderer.util.PolygonRenderUtil;
import io.opensphere.core.pipeline.util.GL2LightHandler;
import io.opensphere.core.pipeline.util.GL2Utilities;
import io.opensphere.core.pipeline.util.GLUtilities;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * Polygon renderer that uses server-side buffering.
 */
public class PolygonRendererBuffered extends AbstractRenderer<PolygonGeometry> implements TimeFilteringRenderer
{
    /** Bits used for {@link GL2#glPushAttrib(int)}. */
    private static final int ATTRIB_BITS = GL2.GL_CURRENT_BIT | GL2.GL_POLYGON_BIT | GL.GL_COLOR_BUFFER_BIT | GL2.GL_ENABLE_BIT
            | GL2.GL_LIGHTING_BIT | GL.GL_DEPTH_BUFFER_BIT;

    /** Bits used for {@link GL2#glPushClientAttrib(int)}. */
    private static final int CLIENT_ATTRIB_BITS = GL2.GL_CLIENT_VERTEX_ARRAY_BIT;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PolygonRendererBuffered.class);

    /** The helper for faded rendering. */
    private final FadedRenderingHelper myFadedRenderingHelper = new FadedRenderingHelper();

    /**
     * The time span that represents the earliest and latest times that might be
     * in the geometry constraints.
     */
    private volatile TimeSpan myGroupTimeSpan;

    /**
     * Construct the renderer.
     *
     * @param cache The geometry cache.
     */
    public PolygonRendererBuffered(CacheProvider cache)
    {
        super(cache);
    }

    @Override
    public void doRender(RenderContext rc, Collection<? extends PolygonGeometry> input,
            Collection<? super PolygonGeometry> rejected, PickManager pickManager, MapContext<?> mapContext,
            ModelDataRetriever<PolygonGeometry> dataRetriever)
    {
        PolygonRenderData renderData = (PolygonRenderData)getRenderData();
        if (renderData == null)
        {
            return;
        }

        try
        {
            PolygonRenderUtil.setupGL(rc.getGL(), rc.getRenderMode(),
                    ScreenPosition.class.isAssignableFrom(input.iterator().next().getPositionType()),
                    isDebugFeatureOn("TessellationLines"));

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
        return PolygonGeometry.class;
    }

    @Override
    public void preRender(Collection<? extends PolygonGeometry> input, Collection<? extends PolygonGeometry> drawable,
            Collection<? extends PolygonGeometry> pickable, PickManager pickManager,
            ModelDataRetriever<PolygonGeometry> dataRetriever, Projection projectionSnapshot)
    {
        super.preRender(input, drawable, pickable, pickManager, dataRetriever, projectionSnapshot);

        // make sure this doesn't change
        TimeSpan groupTimeSpan = myGroupTimeSpan;

        Map<PolygonGeometry, Pair<PolygonMeshDataBuffered, PolylineDataBuffered>> renderDataMap = New.weakMap(input.size());
        for (PolygonGeometry geom : input)
        {
            Pair<PolygonMeshDataBuffered, PolylineDataBuffered> bufferedData = getBufferedData(geom, dataRetriever,
                    projectionSnapshot, TimeBudget.ZERO, groupTimeSpan);

            if (bufferedData != null)
            {
                renderDataMap.put(geom, bufferedData);
            }
        }

        setRenderData(new PolygonRenderData(renderDataMap, projectionSnapshot, groupTimeSpan));
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
     * Render each of the render data buffers.
     *
     * @param rc The render context.
     * @param input The input geometries.
     * @param rejected Output collection of geometries that cannot be rendered.
     * @param pickManager The pick manager.
     * @param dataRetriever The data retriever.
     * @param renderData The render data.
     */
    private void doRender(RenderContext rc, Collection<? extends PolygonGeometry> input,
            Collection<? super PolygonGeometry> rejected, PickManager pickManager,
            ModelDataRetriever<PolygonGeometry> dataRetriever, PolygonRenderData renderData)
    {
        myFadedRenderingHelper.initIntervalFilter(rc, false, null);

        boolean lastSmoothing = false;
        float lastWidth = 0f;
        StippleModelConfig lastStipple = null;
        LightingModelConfigGL lastLight = null;
        for (final PolygonGeometry geom : input)
        {
            Pair<PolygonMeshDataBuffered, PolylineDataBuffered> bufferedData = getBufferedData(rc, dataRetriever, renderData,
                    geom);

            if (bufferedData == null)
            {
                rejected.add(geom);
            }
            else
            {
                PolylineRenderProperties renderProps = geom.getRenderProperties();
                if (rc.getRenderMode() == AbstractGeometry.RenderMode.DRAW)
                {
                    lastLight = GL2LightHandler.setLight(rc, lastLight, geom.getRenderProperties().getLighting());
                }
                rc.glDepthMask(geom.getRenderProperties().isObscurant());

                PolygonMeshDataBuffered mesh = bufferedData.getFirstObject();
                if (mesh != null)
                {
                    if (geom.getRenderProperties().getFillColorRenderProperties() != null)
                    {
                        GL2Utilities.glColor(rc, pickManager, geom,
                                ((PolygonRenderProperties)renderProps).getFillColorRenderProperties());
                    }
                    mesh.draw(rc);

                    PolygonMeshData modelData = mesh.get(0).getPolygonMeshData();
                    // If the buffered data is not already in the cache, add
                    // it to enable the GPU memory to be cleaned up.
                    getCache().putCacheAssociation(modelData, mesh, PolygonMeshDataBuffered.class, 0L, mesh.getSizeGPU());
                }

                PolylineDataBuffered line = bufferedData.getSecondObject();
                if (bufferedData.getSecondObject() != null)
                {
                    if (rc.getRenderMode() == AbstractGeometry.RenderMode.DRAW)
                    {
                        lastSmoothing = GLUtilities.enableSmoothingIfNecessary(rc.getGL(), geom, lastSmoothing);
                    }

                    lastWidth = GLUtilities.glLineWidth(rc.getGL(),
                            rc.getRenderMode() == RenderMode.PICK ? renderProps.getWidth() + 1f : renderProps.getWidth(),
                            lastWidth);

                    lastStipple = GL2Utilities.glLineStipple(rc.getGL().getGL2(), rc.getRenderMode(), renderProps.getStipple(),
                            lastStipple);

                    GL2Utilities.glColor(rc, pickManager, geom, renderProps);
                    line.draw(rc, GL.GL_LINE_STRIP);
                }
            }
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
     * @param groupTimeSpan If non-null this will be used to upload time
     *            constraint information.
     * @return The buffer data.
     */
    private Pair<PolygonMeshDataBuffered, PolylineDataBuffered> getBufferedData(PolygonGeometry geom,
            ModelDataRetriever<PolygonGeometry> dataRetriever, Projection projectionSnapshot, TimeBudget timeBudget,
            TimeSpan groupTimeSpan)
    {
        PolygonProcessor.PolygonModelData modelData = (PolygonProcessor.PolygonModelData)dataRetriever.getModelData(geom,
                projectionSnapshot, null, TimeBudget.ZERO);
        PolygonMeshDataBuffered polygonBuffer = null;
        PolylineDataBuffered polylineBuffer = null;
        if (modelData != null)
        {
            polygonBuffer = getCache().getCacheAssociation(modelData, PolygonMeshDataBuffered.class);
            if (polygonBuffer == null && modelData.getMeshData() != null)
            {
                polygonBuffer = new PolygonMeshDataBuffered(modelData.getMeshData());
            }

            polylineBuffer = getCache().getCacheAssociation(modelData, PolylineDataBuffered.class);
            if (polylineBuffer == null && modelData.getLineData() != null)
            {
                polylineBuffer = new PolylineDataBuffered(modelData.getLineData(), groupTimeSpan);
            }
            return new Pair<PolygonMeshDataBuffered, PolylineDataBuffered>(polygonBuffer, polylineBuffer);
        }
        return null;
    }

    /**
     * Get the buffered data from the renderData or request it if necessary.
     *
     * @param rc The render context.
     * @param dataRetriever The model data retriever.
     * @param renderData The set of all data available to render for the
     *            processor.
     * @param geom The geometry which is going to be rendered.
     * @return The model data for the geometry.
     */
    private Pair<PolygonMeshDataBuffered, PolylineDataBuffered> getBufferedData(RenderContext rc,
            ModelDataRetriever<PolygonGeometry> dataRetriever, PolygonRenderData renderData, PolygonGeometry geom)
    {
        Pair<PolygonMeshDataBuffered, PolylineDataBuffered> bufferedData = renderData.getData().get(geom);
        if (bufferedData == null)
        {
            bufferedData = getBufferedData(geom, dataRetriever, renderData.getProjection(), rc.getTimeBudget(), myGroupTimeSpan);
            if (bufferedData != null)
            {
                renderData.getData().put(geom, bufferedData);
            }
        }
        return bufferedData;
    }

    /**
     * A factory for creating this renderer.
     */
    public static class Factory extends AbstractRenderer.Factory<PolygonGeometry>
    {
        @Override
        public GeometryRenderer<PolygonGeometry> createRenderer()
        {
            return new PolygonRendererBuffered(getCache());
        }

        @Override
        public Collection<? extends BufferDisposalHelper<?>> getDisposalHelpers()
        {
            return Collections.singleton(BufferDisposalHelper.create(PolygonMeshDataBuffered.class, getCache()));
        }

        @Override
        public Class<? extends Geometry> getType()
        {
            return PolygonGeometry.class;
        }

        @Override
        public boolean isViable(RenderContext rc, Collection<String> warnings)
        {
            return rc.is11Available(getClass().getEnclosingClass().getSimpleName() + " is not viable", warnings);
        }
    }

    /** Data used for rendering. */
    private static class PolygonRenderData extends TimeRenderData
    {
        /** The map of geometries to data. */
        private final Map<PolygonGeometry, Pair<PolygonMeshDataBuffered, PolylineDataBuffered>> myData;

        /**
         * Constructor.
         *
         * @param data The map of geometries to data.
         * @param projection The projection snapshot used to generate this data.
         * @param groupTimeSpan The time span associated with this render data.
         */
        public PolygonRenderData(Map<PolygonGeometry, Pair<PolygonMeshDataBuffered, PolylineDataBuffered>> data,
                Projection projection, TimeSpan groupTimeSpan)
        {
            super(projection, groupTimeSpan);
            myData = data;
        }

        /**
         * Get the data.
         *
         * @return the data
         */
        public Map<PolygonGeometry, Pair<PolygonMeshDataBuffered, PolylineDataBuffered>> getData()
        {
            return myData;
        }
    }
}
