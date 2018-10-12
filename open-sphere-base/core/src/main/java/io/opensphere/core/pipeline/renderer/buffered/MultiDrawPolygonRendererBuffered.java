package io.opensphere.core.pipeline.renderer.buffered;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
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
import io.opensphere.core.geometry.renderproperties.StippleModelConfig;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.pipeline.cache.CacheProvider;
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
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * Polygon renderer that uses server-side buffering.
 */
public class MultiDrawPolygonRendererBuffered extends AbstractRenderer<PolygonGeometry> implements TimeFilteringRenderer
{
    /** Bits used for {@link GL2#glPushAttrib(int)}. */
    private static final int ATTRIB_BITS = GL2.GL_CURRENT_BIT | GL2.GL_POLYGON_BIT | GL.GL_COLOR_BUFFER_BIT | GL2.GL_ENABLE_BIT
            | GL2.GL_LIGHTING_BIT | GL.GL_DEPTH_BUFFER_BIT;

    /** Bits used for {@link GL2#glPushClientAttrib(int)}. */
    private static final int CLIENT_ATTRIB_BITS = GL2.GL_CLIENT_VERTEX_ARRAY_BIT;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MultiDrawPolygonRendererBuffered.class);

    /**
     * In order to keep the buffered data in the cache so that it is correctly
     * accounted for for GPU memory usage we need a key for the buffer which is
     * unique. This set also prevents the keys from being garbage collected
     * which would leave the buffered data stranded in the cache.
     */
    private final Collection<PolygonRenderKey> myBufferKeys = New.set();

    /** The helper for faded rendering. */
    private final FadedRenderingHelper myFadedRenderingHelper = new FadedRenderingHelper();

    /**
     * The time span that represents the earliest and latest times that might be
     * in the geometry constraints.
     */
    private volatile TimeSpan myGroupTimeSpan;

    /** The last render data used. */
    private TimeRenderData myLastRenderData;

    /**
     * Construct the renderer.
     *
     * @param cache The geometry cache.
     */
    public MultiDrawPolygonRendererBuffered(CacheProvider cache)
    {
        super(cache);
    }

    /**
     * Do render.
     *
     * @param rc The rc.
     * @param input The input.
     * @param rejected The rejected.
     * @param pickManager The pick manager.
     * @param mapContext The map context.
     * @param dataRetriever The data retriever.
     */
    @Override
    public void doRender(RenderContext rc, Collection<? extends PolygonGeometry> input,
            Collection<? super PolygonGeometry> rejected, PickManager pickManager, MapContext<?> mapContext,
            ModelDataRetriever<PolygonGeometry> dataRetriever)
    {
        MultiDrawPolygonRenderData renderData = (MultiDrawPolygonRenderData)getRenderData();
        if (renderData == null)
        {
            return;
        }

        if (myLastRenderData != null && !renderData.equals(myLastRenderData))
        {
            cleanBuffersFromCache();
        }
        myLastRenderData = renderData;

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

    /**
     * Get the attrib bits.
     *
     * @return The attrib bits.
     */
    @Override
    public int getAttribBits()
    {
        return ATTRIB_BITS;
    }

    /**
     * Get the client attrib bits.
     *
     * @return The client attrib bits.
     */
    @Override
    public int getClientAttribBits()
    {
        return CLIENT_ATTRIB_BITS;
    }

    /**
     * Get the type.
     *
     * @return The type.
     */
    @Override
    public Class<?> getType()
    {
        return PolygonGeometry.class;
    }

    /**
     * Pre render.
     *
     * @param input The input.
     * @param drawable The drawable.
     * @param pickable The pickable.
     * @param pickManager The pick manager.
     * @param dataRetriever The data retriever.
     * @param projectionSnapshot The projection snapshot.
     */
    @Override
    public void preRender(Collection<? extends PolygonGeometry> input, Collection<? extends PolygonGeometry> drawable,
            Collection<? extends PolygonGeometry> pickable, PickManager pickManager,
            ModelDataRetriever<PolygonGeometry> dataRetriever, Projection projectionSnapshot)
    {
        super.preRender(input, drawable, pickable, pickManager, dataRetriever, projectionSnapshot);

        // make sure this doesn't change
        TimeSpan groupTimeSpan = myGroupTimeSpan;

        Map<RenderMode, Map<PolygonRenderKey, Pair<MultiDrawPolygonMeshDataBuffered, MultiDrawPolylineDataBuffered>>> renderDataMap = New
                .map();

        MultiDrawBufferGenerator multiDrawBufferGenerator = new MultiDrawBufferGenerator();
        Map<PolygonRenderKey, Pair<MultiDrawPolygonMeshDataBuffered, MultiDrawPolylineDataBuffered>> drawMap = multiDrawBufferGenerator
                .getRenderKeyToBufferPairMap(drawable, false, pickManager, dataRetriever, projectionSnapshot, groupTimeSpan);
        renderDataMap.put(RenderMode.DRAW, drawMap);

        // Typically, the pickable will match the drawable, so optimize for that
        // case.
        if (drawable.size() == pickable.size() && drawable.equals(pickable))
        {
            renderDataMap.put(RenderMode.PICK, drawMap);
        }
        else
        {
            Map<PolygonRenderKey, Pair<MultiDrawPolygonMeshDataBuffered, MultiDrawPolylineDataBuffered>> pickMap = multiDrawBufferGenerator
                    .getRenderKeyToBufferPairMap(pickable, false, pickManager, dataRetriever, projectionSnapshot, groupTimeSpan);
            renderDataMap.put(RenderMode.PICK, pickMap);
        }

        setRenderData(new MultiDrawPolygonRenderData(renderDataMap, projectionSnapshot, groupTimeSpan));
    }

    /**
     * Sets the group interval.
     *
     * @param span The span.
     * @return true, if successful
     */
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

    /**
     * Set the time manager.
     *
     * @param timeManager The time manager.
     */
    @Override
    public void setTimeManager(TimeManager timeManager)
    {
        myFadedRenderingHelper.setTimeManager(timeManager);
    }

    /**
     * Remove the buffered data from the cache so that the GPU memory can be
     * freed.
     */
    protected void cleanBuffersFromCache()
    {
        synchronized (myBufferKeys)
        {
            if (!myBufferKeys.isEmpty())
            {
                CacheProvider cache = getCache();
                myBufferKeys.forEach(k -> cache.clearCacheAssociation(k, PointDataBuffered.class));
                myBufferKeys.clear();
            }
        }
    }

    /**
     * Get the logger.
     *
     * @return The logger.
     */
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
    protected void renderHighlights(RenderContext rc, Collection<? extends PolygonGeometry> input, PickManager pickManager,
            io.opensphere.core.pipeline.renderer.AbstractRenderer.ModelDataRetriever<PolygonGeometry> dataRetriever,
            MultiDrawPolygonRenderData renderData)
    {
        Collection<PolygonGeometry> pickedGeomsToRender = getPickedGeometries(input, pickManager);
        if (!pickedGeomsToRender.isEmpty())
        {
            Map<PolygonRenderKey, Pair<MultiDrawPolygonMeshDataBuffered, MultiDrawPolylineDataBuffered>> drawMap = new MultiDrawBufferGenerator()
                    .getRenderKeyToBufferPairMap(pickedGeomsToRender, true, pickManager, dataRetriever,
                            renderData.getProjection(), renderData.getGroupTimeSpan());

            doRender(rc, drawMap);

            for (Pair<MultiDrawPolygonMeshDataBuffered, MultiDrawPolylineDataBuffered> pair : drawMap.values())
            {
                if (pair.getFirstObject() != null)
                {
                    pair.getFirstObject().dispose(rc.getGL());
                }
                if (pair.getSecondObject() != null)
                {
                    pair.getSecondObject().dispose(rc.getGL());
                }
            }
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
    private void doRender(RenderContext rc, Collection<? extends PolygonGeometry> input,
            Collection<? super PolygonGeometry> rejected, PickManager pickManager,
            ModelDataRetriever<PolygonGeometry> dataRetriever, MultiDrawPolygonRenderData renderData)
    {
        myFadedRenderingHelper.initIntervalFilter(rc, false, null);

        Map<PolygonRenderKey, Pair<MultiDrawPolygonMeshDataBuffered, MultiDrawPolylineDataBuffered>> data = renderData.getData()
                .get(rc.getRenderMode());

        if (data == null)
        {
            rejected.addAll(input);
        }
        else
        {
            doRender(rc, data);
        }

        if (!pickManager.getPickedGeometries().isEmpty())
        {
            renderHighlights(rc, input, pickManager, dataRetriever, renderData);
        }
    }

    /**
     * Render the data in the map of render key to buffers.
     *
     * @param rc The render context.
     * @param data The map of render key to buffers.
     */
    private void doRender(RenderContext rc,
            Map<PolygonRenderKey, Pair<MultiDrawPolygonMeshDataBuffered, MultiDrawPolylineDataBuffered>> data)
    {
        boolean lastSmoothing = false;
        float lastWidth = 0f;
        StippleModelConfig lastStipple = null;
        LightingModelConfigGL lastLight = null;
        for (Entry<PolygonRenderKey, Pair<MultiDrawPolygonMeshDataBuffered, MultiDrawPolylineDataBuffered>> entry : data
                .entrySet())
        {
            PolygonRenderKey renderKey = entry.getKey();
            if (rc.getRenderMode() == AbstractGeometry.RenderMode.DRAW)
            {
                lastLight = GL2LightHandler.setLight(rc, lastLight, renderKey.getLighting());
            }
            rc.glDepthMask(renderKey.isObscurant());

            MultiDrawPolygonMeshDataBuffered mesh = entry.getValue().getFirstObject();
            if (mesh != null)
            {
                mesh.draw(rc);

                // Enable the GPU memory to be cleaned up.
                getCache().putCacheAssociation(renderKey, mesh, MultiDrawPolygonMeshDataBuffered.class, 0L, mesh.getSizeGPU());
            }

            MultiDrawPolylineDataBuffered lines = entry.getValue().getSecondObject();
            if (lines != null)
            {
                if (rc.getRenderMode() == AbstractGeometry.RenderMode.DRAW)
                {
                    lastSmoothing = GLUtilities.enableSmoothingIfNecessary(rc.getGL(), renderKey.isLineSmoothing(),
                            lastSmoothing);
                }

                lastWidth = GLUtilities.glLineWidth(rc.getGL(),
                        rc.getRenderMode() == RenderMode.PICK ? renderKey.getWidth() + 1f : renderKey.getWidth(), lastWidth);

                lastStipple = GL2Utilities.glLineStipple(rc.getGL().getGL2(), rc.getRenderMode(), renderKey.getStipple(),
                        lastStipple);

                lines.draw(rc, GL.GL_LINE_STRIP);

                // Enable the GPU memory to be cleaned up.
                getCache().putCacheAssociation(renderKey, lines, MultiDrawPolylineDataBuffered.class, 0L, lines.getSizeGPU());
            }
        }
    }

    /**
     * A factory for creating this renderer.
     */
    public static class Factory extends AbstractRenderer.Factory<PolygonGeometry>
    {
        /**
         * Creates the renderer.
         *
         * @return the geometry renderer
         */
        @Override
        public GeometryRenderer<PolygonGeometry> createRenderer()
        {
            return new MultiDrawPolygonRendererBuffered(getCache());
        }

        /**
         * Gets the disposal helpers.
         *
         * @return the disposal helpers
         */
        @Override
        public Collection<? extends BufferDisposalHelper<?>> getDisposalHelpers()
        {
            return Collections.singleton(BufferDisposalHelper.create(PolygonMeshDataBuffered.class, getCache()));
        }

        /**
         * Gets the type.
         *
         * @return the type
         */
        @Override
        public Class<? extends Geometry> getType()
        {
            return PolygonGeometry.class;
        }

        /**
         * Checks if is viable.
         *
         * @param rc The rc.
         * @param warnings The warnings.
         * @return true, if is viable
         */
        @Override
        public boolean isViable(RenderContext rc, Collection<String> warnings)
        {
            return rc.is14Available(getClass().getEnclosingClass().getSimpleName() + " is not viable", warnings);
        }
    }

    /** Data used for rendering. */
    private static class MultiDrawPolygonRenderData extends TimeRenderData
    {
        /** The map of render modes to render keys to buffers. */
        private final Map<RenderMode, Map<PolygonRenderKey, Pair<MultiDrawPolygonMeshDataBuffered, MultiDrawPolylineDataBuffered>>> myData;

        /**
         * Constructor.
         *
         * @param renderDataMap The map of render modes to data.
         * @param projection The projection snapshot used to generate this data.
         * @param groupTimeSpan The time span associated with this render data.
         */
        public MultiDrawPolygonRenderData(
                Map<RenderMode, Map<PolygonRenderKey, Pair<MultiDrawPolygonMeshDataBuffered, MultiDrawPolylineDataBuffered>>> renderDataMap,
                Projection projection, TimeSpan groupTimeSpan)
        {
            super(projection, groupTimeSpan);
            myData = renderDataMap;
        }

        /**
         * Get the data.
         *
         * @return the data
         */
        public Map<RenderMode, Map<PolygonRenderKey, Pair<MultiDrawPolygonMeshDataBuffered, MultiDrawPolylineDataBuffered>>> getData()
        {
            return myData;
        }
    }
}
