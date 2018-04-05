package io.opensphere.core.pipeline.renderer.immediate;

import java.util.Collection;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.renderproperties.StippleModelConfig;
import io.opensphere.core.math.Vector3f;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.processor.PolylineModelData;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.util.GL2Utilities;
import io.opensphere.core.pipeline.util.GLUtilities;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * Immediate mode GL polyline renderer.
 */
public class PolylineRendererImmediate extends AbstractRenderer<PolylineGeometry>
        implements GeometryRendererImmediate<PolylineGeometry>
{
    /** Bits used for {@link GL2#glPushAttrib(int)}. */
    private static final int ATTRIB_BITS = GL2.GL_ENABLE_BIT | GL.GL_COLOR_BUFFER_BIT | GL2.GL_CURRENT_BIT | GL2.GL_LINE_BIT;

    /** Bits used for {@link GL2#glPushClientAttrib(int)}. */
    private static final int CLIENT_ATTRIB_BITS = -1;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PolylineRendererImmediate.class);

    /**
     * Construct the renderer.
     *
     * @param cache the cache holding the model coordinates for the geometries
     */
    protected PolylineRendererImmediate(CacheProvider cache)
    {
        super(cache);
    }

    @Override
    public void cleanupShaders(RenderContext rc, Collection<? extends PolylineGeometry> input)
    {
    }

    @Override
    public void doRender(RenderContext rc, Collection<? extends PolylineGeometry> input,
            Collection<? super PolylineGeometry> rejected, PickManager pickManager, MapContext<?> mapContext,
            ModelDataRetriever<PolylineGeometry> dataRetriever)
    {
        PolylineRenderData renderData = (PolylineRenderData)getRenderData();
        if (renderData == null)
        {
            return;
        }

        try
        {
            if (rc.getRenderMode() == AbstractGeometry.RenderMode.DRAW)
            {
                rc.getGL().glEnable(GL.GL_BLEND);
                rc.getGL().glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            }

            boolean lastSmoothing = false;
            float lastWidth = 0f;
            StippleModelConfig lastStipple = null;
            for (PolylineGeometry geo : input)
            {
                if (rc.getRenderMode() == AbstractGeometry.RenderMode.DRAW)
                {
                    lastSmoothing = GLUtilities.enableSmoothingIfNecessary(rc.getGL(), geo, lastSmoothing);
                }

                PolylineModelData positions = getPositions(rc, dataRetriever, renderData, geo);
                if (positions == null)
                {
                    rejected.add(geo);
                }
                else
                {
                    lastWidth = GLUtilities.glLineWidth(rc.getGL(), rc.getRenderMode() == RenderMode.PICK
                            ? geo.getRenderProperties().getWidth() + 1f : geo.getRenderProperties().getWidth(), lastWidth);

                    lastStipple = GL2Utilities.glLineStipple(rc.getGL().getGL2(), rc.getRenderMode(),
                            geo.getRenderProperties().getStipple(), lastStipple);

                    GL2Utilities.glColor(rc, pickManager, geo);
                    rc.glDepthMask(geo.getRenderProperties().isObscurant());

                    PolylineModelData fPositions = positions;
                    GL2Utilities.renderWithTransform(rc, geo.getRenderProperties().getTransform(),
                        () -> render(rc, geo, fPositions));
                }
            }
        }
        finally
        {
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
    public void initializeShaders(RenderContext rc, Collection<? extends PolylineGeometry> input)
    {
    }

    @Override
    public void preRender(Collection<? extends PolylineGeometry> input, Collection<? extends PolylineGeometry> drawable,
            Collection<? extends PolylineGeometry> pickable, PickManager pickManager,
            ModelDataRetriever<PolylineGeometry> dataRetriever, Projection projection)
    {
        super.preRender(input, drawable, pickable, pickManager, dataRetriever, projection);
        Map<PolylineGeometry, PolylineModelData> renderMap = New.weakMap(input.size());

        for (PolylineGeometry geom : input)
        {
            PolylineModelData modelData = (PolylineModelData)dataRetriever.getModelData(geom, projection, null, TimeBudget.ZERO);
            if (modelData != null)
            {
                renderMap.put(geom, modelData);
            }
        }

        setRenderData(new PolylineRenderData(renderMap, projection));
    }

    @Override
    protected Logger getLogger()
    {
        return LOGGER;
    }

    /**
     * Get the positions from the render data or request it from the data
     * retriever if it's missing.
     *
     * @param rc The rc.
     * @param dataRetriever The data retriever.
     * @param renderData The render data.
     * @param geo The geo.
     * @return The positions.
     */
    private PolylineModelData getPositions(RenderContext rc, ModelDataRetriever<PolylineGeometry> dataRetriever,
            PolylineRenderData renderData, PolylineGeometry geo)
    {
        PolylineModelData positions = renderData.getData().get(geo);
        if (positions == null)
        {
            positions = (PolylineModelData)dataRetriever.getModelData(geo, renderData.getProjection(), null, rc.getTimeBudget());
            if (positions != null)
            {
                renderData.getData().put(geo, positions);
            }
        }
        return positions;
    }

    /**
     * Render a single line.
     *
     * @param renderContext The render context.
     * @param geom The line to render.
     * @param positions The model positions which make up the line.
     */
    private void render(RenderContext renderContext, PolylineGeometry geom, PolylineModelData positions)
    {
        int drawMode;
        if (geom instanceof PolygonGeometry
                && ((PolygonGeometry)geom).getRenderProperties().getFillColorRenderProperties() != null) // TODO
        {
            switch (positions.getVectorCount())
            {
                case TRIANGLE_VERTEX_COUNT:
                    drawMode = GL.GL_TRIANGLES;
                    break;
                case QUAD_VERTEX_COUNT:
                    drawMode = GL2GL3.GL_QUADS;
                    break;
                default:
                    drawMode = GL2.GL_POLYGON;
                    break;
            }
        }
        else
        {
            drawMode = GL.GL_LINE_STRIP;
        }

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
    public static class Factory extends GeometryRendererImmediate.Factory<PolylineGeometry>
    {
        @Override
        public GeometryRendererImmediate<PolylineGeometry> createRenderer()
        {
            return new PolylineRendererImmediate(getCache());
        }

        @Override
        public Class<? extends Geometry> getType()
        {
            return PolylineGeometry.class;
        }

        @Override
        public boolean isViable(RenderContext rc, Collection<String> warnings)
        {
            return true;
        }
    }

    /** Data used for rendering. */
    private static class PolylineRenderData extends RenderData
    {
        /** The map of geometries to pairs of data. */
        private final Map<PolylineGeometry, PolylineModelData> myData;

        /**
         * Constructor.
         *
         * @param data The map of geometries to pairs of render data.
         * @param projection The projection snapshot used to generate this data.
         */
        public PolylineRenderData(Map<PolylineGeometry, PolylineModelData> data, Projection projection)
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
        public Map<PolylineGeometry, PolylineModelData> getData()
        {
            return myData;
        }
    }
}
