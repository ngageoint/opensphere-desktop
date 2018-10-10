package io.opensphere.core.pipeline.renderer.immediate;

import java.util.Collection;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.StippleModelConfig;
import io.opensphere.core.math.Vector3f;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.processor.PolygonMeshData;
import io.opensphere.core.pipeline.processor.PolygonProcessor.PolygonModelData;
import io.opensphere.core.pipeline.processor.PolylineModelData;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.util.PolygonRenderUtil;
import io.opensphere.core.pipeline.util.GL2Utilities;
import io.opensphere.core.pipeline.util.GLUtilities;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.viewer.impl.MapContext;

/** The immediate mode renderer for polygons. */
public class PolygonRendererImmediate extends AbstractRenderer<PolygonGeometry>
implements GeometryRendererImmediate<PolygonGeometry>
{
    /** Bits used for {@link GL2#glPushAttrib(int)}. */
    private static final int ATTRIB_BITS = GL2.GL_CURRENT_BIT | GL2.GL_POLYGON_BIT | GL.GL_COLOR_BUFFER_BIT | GL2.GL_ENABLE_BIT
            | GL2.GL_LIGHTING_BIT | GL.GL_DEPTH_BUFFER_BIT;

    /** Bits used for {@link GL2#glPushClientAttrib(int)}. */
    private static final int CLIENT_ATTRIB_BITS = -1;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PolygonRendererImmediate.class);

    /**
     * Construct the renderer.
     *
     * @param cache the cache holding the model coordinates for the geometries
     */
    protected PolygonRendererImmediate(CacheProvider cache)
    {
        super(cache);
    }

    @Override
    public void cleanupShaders(RenderContext rc, Collection<? extends PolygonGeometry> input)
    {
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
    public void initializeShaders(RenderContext rc, Collection<? extends PolygonGeometry> input)
    {
    }

    @Override
    public void preRender(Collection<? extends PolygonGeometry> input, Collection<? extends PolygonGeometry> drawable,
            Collection<? extends PolygonGeometry> pickable, PickManager pickManager,
            ModelDataRetriever<PolygonGeometry> dataRetriever, Projection projection)
    {
        super.preRender(input, drawable, pickable, pickManager, dataRetriever, projection);
        Map<PolygonGeometry, PolygonModelData> renderMap = New.weakMap(input.size());

        for (PolygonGeometry geom : input)
        {
            PolygonModelData modelData = (PolygonModelData)dataRetriever.getModelData(geom, projection, null, TimeBudget.ZERO);
            if (modelData != null)
            {
                renderMap.put(geom, modelData);
            }
        }

        setRenderData(new PolygonRenderData(renderMap, projection));
    }

    @Override
    protected void doRender(RenderContext rc, Collection<? extends PolygonGeometry> input,
            Collection<? super PolygonGeometry> rejected, PickManager pickManager, MapContext<?> mapContext,
            io.opensphere.core.pipeline.renderer.AbstractRenderer.ModelDataRetriever<PolygonGeometry> dataRetriever)
    {
        PolygonRenderData renderData = (PolygonRenderData)getRenderData();
        if (renderData == null)
        {
            return;
        }

        try
        {
            PolygonRenderUtil.setupGL(rc.getGL(), rc.getRenderMode(),
                    ScreenPosition.class.isAssignableFrom(input.iterator().next().getPositionType()), false);

            boolean lastSmoothing = false;
            float lastWidth = 0f;
            StippleModelConfig lastStipple = null;
            for (final PolygonGeometry geom : input)
            {
                for (PolygonGeometry geo : input)
                {
                    PolygonModelData modelData = getModelData(rc, dataRetriever, renderData, geo);

                    if (modelData == null)
                    {
                        rejected.add(geo);
                    }
                    else
                    {
                        PolylineRenderProperties renderProps = geom.getRenderProperties();
                        rc.glDepthMask(geom.getRenderProperties().isObscurant());

                        Collection<PolylineModelData> lines = modelData.getLineData();
                        if (lines != null)
                        {
                            if (rc.getRenderMode() == AbstractGeometry.RenderMode.DRAW)
                            {
                                lastSmoothing = GLUtilities.enableSmoothingIfNecessary(rc.getGL(), geo, lastSmoothing);
                            }
                            lastWidth = GLUtilities.glLineWidth(rc.getGL(), rc.getRenderMode() == RenderMode.PICK
                                    ? geo.getRenderProperties().getWidth() + 1f : geo.getRenderProperties().getWidth(),
                                            lastWidth);

                            lastStipple = GL2Utilities.glLineStipple(rc.getGL().getGL2(), rc.getRenderMode(),
                                    geo.getRenderProperties().getStipple(), lastStipple);

                            GL2Utilities.glColor(rc, pickManager, geo);
                            rc.glDepthMask(geo.getRenderProperties().isObscurant());

                            GL2Utilities.renderWithTransform(rc, geo.getRenderProperties().getTransform(), () ->
                            {
                                for (PolylineModelData lineData : lines)
                                {
                                    renderBorder(rc, lineData);
                                }
                            });
                        }

                        PolygonMeshData fillData = modelData.getMeshData();
                        if (fillData != null)
                        {
                            rc.glDepthMask(geom.getRenderProperties().isObscurant());
                            if (geom.getRenderProperties().getFillColorRenderProperties() != null)
                            {
                                GL2Utilities.glColor(rc, pickManager, geom,
                                        ((PolygonRenderProperties)renderProps).getFillColorRenderProperties());
                            }
                            GL2Utilities.renderWithTransform(rc, geo.getRenderProperties().getTransform(),
                                    () -> PolygonRenderUtil.drawPolygonMesh(rc.getGL(), fillData));
                        }
                    }
                }
            }
        }
        finally
        {
            rc.popAttributes();
        }
    }

    @Override
    protected Logger getLogger()
    {
        return LOGGER;
    }

    /**
     * Get the model data from the renderData or request it from the data
     * retriever if necessary.
     *
     * @param rc The render context.
     * @param dataRetriever The model data retriever.
     * @param renderData The set of all data available to render for the
     *            processor.
     * @param geo The geometry which is going to be rendered.
     * @return The model data for the geometry.
     */
    private PolygonModelData getModelData(RenderContext rc, ModelDataRetriever<PolygonGeometry> dataRetriever,
            PolygonRenderData renderData, PolygonGeometry geo)
    {
        PolygonModelData modelData = renderData.getData().get(geo);
        if (modelData == null)
        {
            modelData = (PolygonModelData)dataRetriever.getModelData(geo, renderData.getProjection(), null, rc.getTimeBudget());
            if (modelData != null)
            {
                renderData.getData().put(geo, modelData);
            }
        }
        return modelData;
    }

    /**
     * Render a single line.
     *
     * @param renderContext The render context.
     * @param positions The model positions which make up the line.
     */
    private void renderBorder(RenderContext renderContext, PolylineModelData positions)
    {
        int drawMode = GL.GL_LINE_STRIP;
        renderContext.getGL().getGL2().glBegin(drawMode);
        try
        {
            for (Vector3f vec : positions.getModelPositions())
            {
                renderContext.getGL().getGL2().glVertex3f(vec.getX(), vec.getY(), vec.getZ());
            }
        }
        finally
        {
            renderContext.getGL().getGL2().glEnd();
        }
    }

    /** A factory for creating this renderer. */
    public static class Factory extends GeometryRendererImmediate.Factory<PolygonGeometry>
    {
        @Override
        public GeometryRendererImmediate<PolygonGeometry> createRenderer()
        {
            return new PolygonRendererImmediate(getCache());
        }

        @Override
        public Class<? extends Geometry> getType()
        {
            return PolygonGeometry.class;
        }

        @Override
        public boolean isViable(RenderContext rc, Collection<String> warnings)
        {
            return true;
        }
    }

    /** Data used for rendering. */
    private static class PolygonRenderData extends RenderData
    {
        /** The map of geometries to pairs of data. */
        private final Map<PolygonGeometry, PolygonModelData> myData;

        /**
         * Constructor.
         *
         * @param data The map of geometries to pairs of render data.
         * @param projection The projection snapshot used to generate this data.
         */
        public PolygonRenderData(Map<PolygonGeometry, PolygonModelData> data, Projection projection)
        {
            super(projection);
            myData = data;
        }

        /**
         * Access to the data map. This is not thread-safe and should only be
         * called from the GL thread.
         *
         * @return The data map.
         */
        public Map<PolygonGeometry, PolygonModelData> getData()
        {
            return myData;
        }
    }
}
