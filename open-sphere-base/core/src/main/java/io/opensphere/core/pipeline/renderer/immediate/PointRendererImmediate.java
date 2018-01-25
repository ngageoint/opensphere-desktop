package io.opensphere.core.pipeline.renderer.immediate;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.processor.PointProcessor.ModelCoordinates;
import io.opensphere.core.pipeline.renderer.AbstractPointRenderer;
import io.opensphere.core.pipeline.util.GL2Utilities;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * Immediate mode GL point renderer.
 */
public class PointRendererImmediate extends AbstractPointRenderer<PointGeometry>
        implements GeometryRendererImmediate<PointGeometry>
{
    /** Bits used for {@link GL2#glPushAttrib(int)}. */
    private static final int ATTRIB_BITS = GL2.GL_ENABLE_BIT | GL.GL_COLOR_BUFFER_BIT | GL2.GL_CURRENT_BIT | GL2.GL_POINT_BIT;

    /** Bits used for {@link GL2#glPushClientAttrib(int)}. */
    private static final int CLIENT_ATTRIB_BITS = -1;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PointRendererImmediate.class);

    /**
     * Construct the renderer.
     *
     * @param cache the cache holding the model coordinates for the geometries
     */
    protected PointRendererImmediate(CacheProvider cache)
    {
        super(cache);
    }

    @Override
    public void cleanupShaders(RenderContext rc, Collection<? extends PointGeometry> input)
    {
    }

    @Override
    public void doRender(RenderContext rc, Collection<? extends PointGeometry> input, Collection<? super PointGeometry> rejected,
            PickManager pickManager, MapContext<?> mapContext, ModelDataRetriever<PointGeometry> dataRetriever)
    {
        PointRenderData renderData = (PointRenderData)getRenderData();
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

            Set<Geometry> pickedGeometries = pickManager.getPickedGeometries();
            for (PointGeometry geom : input)
            {
                ModelCoordinates coords = fetchModelData(rc, dataRetriever, renderData, geom);
                if (coords == null)
                {
                    rejected.add(geom);
                }
                else
                {
                    float size;
                    if (pickedGeometries.contains(geom))
                    {
                        size = geom.getRenderProperties().getHighlightSize();
                    }
                    else
                    {
                        size = geom.getRenderProperties().getSize();
                    }
                    rc.glPointSize(size);
                    rc.setPointSmoothing(geom.getRenderProperties().getRoundnessRenderProperty().isRound());
                    rc.glDepthMask(geom.getRenderProperties().isObscurant());
                    rc.glBegin(GL.GL_POINTS);
                    GL2Utilities.glColor(rc, pickManager, geom);
                    rc.getGL().getGL2().glVertex3f(coords.getX(), coords.getY(), coords.getZ());
                }
            }
        }
        finally
        {
            try
            {
                rc.glEnd();
            }
            finally
            {
                rc.popAttributes();
            }
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
        return PointGeometry.class;
    }

    @Override
    public void initializeShaders(RenderContext rc, Collection<? extends PointGeometry> input)
    {
    }

    @Override
    public void preRender(Collection<? extends PointGeometry> input, Collection<? extends PointGeometry> drawable,
            Collection<? extends PointGeometry> pickable, PickManager pickManager,
            ModelDataRetriever<PointGeometry> dataRetriever, Projection projection)
    {
        super.preRender(input, drawable, pickable, pickManager, dataRetriever, projection);
        Map<PointGeometry, ModelCoordinates> renderMap = New.weakMap(input.size());

        for (PointGeometry geom : input)
        {
            ModelCoordinates modelData = (ModelCoordinates)dataRetriever.getModelData(geom, projection, null, TimeBudget.ZERO);
            if (modelData != null)
            {
                renderMap.put(geom, modelData);
            }
        }

        setRenderData(new PointRenderData(renderMap, projection));
    }

    @Override
    protected Logger getLogger()
    {
        return LOGGER;
    }

    /**
     * Get the model data either from the render data or from the data retriever
     * if necessary.
     *
     * @param rc The render context.
     * @param dataRetriever The model data retriever.
     * @param renderData The render data gathered during pre-render.
     * @param geom The geometry whose model data is being fetched.
     * @return the model data.
     */
    private ModelCoordinates fetchModelData(RenderContext rc, ModelDataRetriever<PointGeometry> dataRetriever,
            PointRenderData renderData, PointGeometry geom)
    {
        ModelCoordinates coords = renderData.getData().get(geom);
        if (coords == null)
        {
            coords = (ModelCoordinates)dataRetriever.getModelData(geom, renderData.getProjection(), null, rc.getTimeBudget());
            if (coords != null)
            {
                renderData.getData().put(geom, coords);
            }
        }
        return coords;
    }

    /** A factory for creating this renderer. */
    public static class Factory extends GeometryRendererImmediate.Factory<PointGeometry>
    {
        @Override
        public GeometryRendererImmediate<PointGeometry> createRenderer()
        {
            return new PointRendererImmediate(getCache());
        }

        @Override
        public Class<? extends Geometry> getType()
        {
            return PointGeometry.class;
        }

        @Override
        public boolean isViable(RenderContext rc, Collection<String> warnings)
        {
            return true;
        }
    }

    /** Data used for rendering. */
    private static class PointRenderData extends RenderData
    {
        /** The map of geometries to pairs of data. */
        private final Map<PointGeometry, ModelCoordinates> myData;

        /**
         * Constructor.
         *
         * @param data The map of geometries to pairs of render data.
         * @param projection The projection used to generate this data.
         */
        public PointRenderData(Map<PointGeometry, ModelCoordinates> data, Projection projection)
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
        public Map<PointGeometry, ModelCoordinates> getData()
        {
            return myData;
        }
    }
}
