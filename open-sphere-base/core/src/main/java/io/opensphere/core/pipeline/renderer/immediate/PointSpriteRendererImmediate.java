package io.opensphere.core.pipeline.renderer.immediate;

import java.util.Collection;
import java.util.Map;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES1;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.geometry.PointSpriteGeometry;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.processor.PointSpriteProcessor.SpriteModelCoordinates;
import io.opensphere.core.pipeline.processor.TextureModelData;
import io.opensphere.core.pipeline.renderer.AbstractPointRenderer;
import io.opensphere.core.pipeline.util.GL2Utilities;
import io.opensphere.core.pipeline.util.GLUtilities;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.pipeline.util.TextureGroup;
import io.opensphere.core.pipeline.util.TextureHandle;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * Immediate mode GL point renderer.
 *
 * @param <T> The geometry type.
 */
@SuppressWarnings("PMD.GodClass")
public class PointSpriteRendererImmediate<T extends PointSpriteGeometry> extends AbstractPointRenderer<T>
        implements GeometryRendererImmediate<T>
{
    /** Bits used for {@link GL2#glPushAttrib(int)}. */
    private static final int ATTRIB_BITS = GL2.GL_ENABLE_BIT | GL.GL_COLOR_BUFFER_BIT | GL2.GL_CURRENT_BIT | GL2.GL_POINT_BIT
            | GL2.GL_TEXTURE_BIT;

    /** Bits used for {@link GL2#glPushClientAttrib(int)}. */
    private static final int CLIENT_ATTRIB_BITS = -1;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PointSpriteRendererImmediate.class);

    /**
     * Construct the renderer.
     *
     * @param cache the cache holding the model coordinates for the geometries
     */
    protected PointSpriteRendererImmediate(CacheProvider cache)
    {
        super(cache);
    }

    @Override
    public void cleanupShaders(RenderContext rc, Collection<? extends T> input)
    {
    }

    @Override
    public void doRender(RenderContext rc, Collection<? extends T> input, Collection<? super T> rejected, PickManager pickManager,
            MapContext<?> mapContext, ModelDataRetriever<T> dataRetriever)
    {
        PointSpriteRenderData renderData = (PointSpriteRenderData)getRenderData();
        if (renderData == null)
        {
            return;
        }

        RenderState rs = new RenderState();
        try
        {
            prepareRenderContext(rc);

            for (T geom : input)
            {
                if (!doRender(rc, geom, pickManager, dataRetriever, rs, renderData))
                {
                    rejected.add(geom);
                }
            }
        }
        finally
        {
            try
            {
                glEndIfBegun(rc, rs);
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
    public void initializeShaders(RenderContext rc, Collection<? extends T> input)
    {
    }

    @Override
    public void preRender(Collection<? extends T> input, Collection<? extends T> drawable, Collection<? extends T> pickable,
            PickManager pickManager, ModelDataRetriever<T> dataRetriever, Projection projection)
    {
        super.preRender(input, drawable, pickable, pickManager, dataRetriever, projection);
        Map<PointSpriteGeometry, TextureModelData> renderMap = New.weakMap(input.size());

        for (T geom : input)
        {
            TextureModelData modelData = (TextureModelData)dataRetriever.getModelData(geom, projection, null, TimeBudget.ZERO);
            if (modelData != null)
            {
                renderMap.put(geom, modelData);
            }
        }

        setRenderData(new PointSpriteRenderData(renderMap, projection));
    }

    @Override
    protected Logger getLogger()
    {
        return LOGGER;
    }

    /**
     * Set the point size in the render context.
     *
     * @param rc The render context.
     * @param size The point size.
     * @param lastPointSize The last point size.
     * @return The point size.
     */
    protected float glPointSize(RenderContext rc, float size, float lastPointSize)
    {
        if (lastPointSize != size)
        {
            rc.getGL().getGL2().glPointSize(size);
        }
        return size;
    }

    /**
     * Prepare the render context for rendering.
     *
     * @param rc The render context.
     */
    protected void prepareRenderContext(RenderContext rc)
    {
        rc.getGL().glEnable(GL.GL_BLEND);
        rc.getGL().glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        if (rc.isMultiTextureAvailable())
        {
            rc.getGL().glActiveTexture(GL.GL_TEXTURE0);
        }
        rc.getGL().glEnable(GL2ES1.GL_POINT_SPRITE);
        rc.getGL().getGL2().glTexEnvi(GL2ES1.GL_POINT_SPRITE, GL2ES1.GL_COORD_REPLACE, GL.GL_TRUE);
        rc.getGL().glEnable(GL.GL_TEXTURE_2D);
        if (rc.getRenderMode() == AbstractGeometry.RenderMode.PICK && rc.is13Available())
        {
            rc.getGL().getGL2().glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_COMBINE_RGB, GL.GL_REPLACE);
            rc.getGL().getGL2().glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2.GL_SOURCE0_RGB, GL2ES1.GL_PRIMARY_COLOR);
            if (rc.isMultiTextureAvailable())
            {
                rc.getGL().getGL2().glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2.GL_SOURCE0_ALPHA, GL.GL_TEXTURE0);
            }
        }
    }

    /**
     * Render a geometry.
     *
     * @param rc The render context.
     * @param geom The geometry.
     * @param pickManager The pick manager.
     * @param dataRetriever The data retriever.
     * @param rs The render state.
     * @param renderData The render data.
     * @return {@code true} if the geometry was rendered.
     */
    private boolean doRender(RenderContext rc, T geom, PickManager pickManager, ModelDataRetriever<T> dataRetriever,
            RenderState rs, PointSpriteRenderData renderData)
    {
        TextureModelData modelData = fetchModelData(rc, dataRetriever, renderData, geom);
        if (modelData == null)
        {
            return false;
        }
        SpriteModelCoordinates coords = (SpriteModelCoordinates)modelData.getModelData();
        TextureGroup textureGroup = modelData.getTextureGroup();

        TextureHandle textureHandle;
        if (Utilities.sameInstance(textureGroup, rs.getLastTextureGroup()))
        {
            textureHandle = rs.getLastTextureHandle();
        }
        else
        {
            TextureGroup texture = modelData.getTextureGroup();
            if (texture != null && texture.getTextureMap().get(AbstractGeometry.RenderMode.PICK) != null)
            {
                rc.getGL().getGL2().glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE, GL.GL_BLEND);
                rc.getGL().getGL2().glColor4d(0., 0., 0., 1.);
                Object textureHandleKey = texture.getTextureMap().get(AbstractGeometry.RenderMode.PICK);
                textureHandle = getCache().getCacheAssociation(textureHandleKey, TextureHandle.class);
            }
            else
            {
                if (rc.is13Available())
                {
                    rc.getGL().getGL2().glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE, GL2ES1.GL_COMBINE);
                }

                Object textureHandleKey = texture == null ? null : texture.getTextureMap().get(AbstractGeometry.RenderMode.DRAW);
                if (textureHandleKey == null)
                {
                    TextureHandle whiteTextureHandle = GLUtilities.bindSolidWhiteTexture(rc, getCache(),
                            rs.getWhiteTextureHandle());
                    rs.setWhiteTextureHandle(whiteTextureHandle);
                    textureHandle = whiteTextureHandle;

                    // Set the handle and group since the texture is
                    // already bound by the call above.
                    rs.setLastTextureHandle(textureHandle);
                    rs.setLastTextureGroup(textureGroup);
                }
                else
                {
                    textureHandle = getCache().getCacheAssociation(textureHandleKey, TextureHandle.class);
                }
            }
        }

        if (textureHandle == null)
        {
            return false;
        }
        else
        {
            float size = pickManager.getPickedGeometries().contains(geom) ? coords.getHighlightSize() : coords.getSize();
            if (rs.getLastPointSize() != size || !Utilities.sameInstance(rs.getLastTextureHandle(), textureHandle))
            {
                glEndIfBegun(rc, rs);
                rs.setLastPointSize(glPointSize(rc, size, rs.getLastPointSize()));
                if (!Utilities.sameInstance(rs.getLastTextureHandle(), textureHandle))
                {
                    rc.getGL().glBindTexture(GL.GL_TEXTURE_2D, textureHandle.getTextureId());
                    rs.setLastTextureHandle(textureHandle);
                    rs.setLastTextureGroup(textureGroup);
                }
                rc.getGL().getGL2().glBegin(GL.GL_POINTS);
                rs.setBegun();
            }
            GL2Utilities.glColor(rc, pickManager, geom);
            rc.glDepthMask(geom.getRenderProperties().isObscurant());
            rc.getGL().getGL2().glVertex3f(coords.getX(), coords.getY(), coords.getZ());
            return true;
        }
    }

    /**
     * Get the model data either from the render data or from the data retriever
     * if necessary.
     *
     * @param rc The render context.
     * @param dataRetriever The model data retriever.
     * @param renderData The render data gathered during pre-render.
     * @param geom The geometry whose model data is being fetched.
     * @return The model data.
     */
    private TextureModelData fetchModelData(RenderContext rc, ModelDataRetriever<T> dataRetriever,
            PointSpriteRenderData renderData, T geom)
    {
        TextureModelData coords = renderData.getData().get(geom);
        if (coords == null || coords.getTextureGroup() == null || coords.getModelData() == null)
        {
            coords = (TextureModelData)dataRetriever.getModelData(geom, renderData.getProjection(), coords, rc.getTimeBudget());
            if (coords != null)
            {
                renderData.getData().put(geom, coords);
            }
        }
        return coords;
    }

    /**
     * Call glEnd if the render state is begun.
     *
     * @param rc the render context
     * @param rs the render state
     */
    private void glEndIfBegun(RenderContext rc, RenderState rs)
    {
        if (rs.isBegun())
        {
            rc.getGL().getGL2().glEnd();
        }
    }

    /** A factory for creating this renderer. */
    public static class Factory extends GeometryRendererImmediate.Factory<PointSpriteGeometry>
    {
        @Override
        public GeometryRendererImmediate<PointSpriteGeometry> createRenderer()
        {
            return new PointSpriteRendererImmediate<PointSpriteGeometry>(getCache());
        }

        @Override
        public Class<? extends Geometry> getType()
        {
            return PointSpriteGeometry.class;
        }

        @Override
        public boolean isViable(RenderContext rc, Collection<String> warnings)
        {
            return rc.isPointSpriteAvailable(getClass().getEnclosingClass().getSimpleName() + " is not viable", warnings);
        }
    }

    /** Data used for rendering. */
    private static class PointSpriteRenderData extends RenderData
    {
        /** The map of geometries to pairs of data. */
        private final Map<PointSpriteGeometry, TextureModelData> myData;

        /**
         * Constructor.
         *
         * @param data The map of geometries to pairs of render data.
         * @param projection The projection used to generate this data.
         */
        public PointSpriteRenderData(Map<PointSpriteGeometry, TextureModelData> data, Projection projection)
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
        public Map<PointSpriteGeometry, TextureModelData> getData()
        {
            return myData;
        }
    }

    /**
     * Class used to keep track of the current render state. TODO isn't most of
     * this handled by RenderContext?
     */
    private static class RenderState
    {
        /** The flag indicating if {@link GL2#glBegin(int)} has been called. */
        private boolean myBegun;

        /** The last point size. */
        private float myLastPointSize;

        /** The last texture group. */
        private TextureGroup myLastTextureGroup;

        /** The last texture handle. */
        private TextureHandle myLastTextureHandle;

        /** The handle for the white texture, if one was created. */
        private TextureHandle myWhiteTextureHandle;

        /**
         * Accessor for the lastPointSize.
         *
         * @return The lastPointSize.
         */
        public float getLastPointSize()
        {
            return myLastPointSize;
        }

        /**
         * Accessor for the lastTextureGroup.
         *
         * @return The lastTextureGroup.
         */
        public TextureGroup getLastTextureGroup()
        {
            return myLastTextureGroup;
        }

        /**
         * Accessor for the lastTextureHandle.
         *
         * @return The lastTextureHandle.
         */
        public TextureHandle getLastTextureHandle()
        {
            return myLastTextureHandle;
        }

        /**
         * Accessor for the whiteTextureHandle.
         *
         * @return The whiteTextureHandle.
         */
        public TextureHandle getWhiteTextureHandle()
        {
            return myWhiteTextureHandle;
        }

        /**
         * Get the flag indicating if {@link GL2#glBegin(int)} has been called.
         *
         * @return The flag.
         */
        public boolean isBegun()
        {
            return myBegun;
        }

        /**
         * Set the flag indicating that {@link GL2#glBegin(int)} has been
         * called.
         */
        public void setBegun()
        {
            myBegun = true;
        }

        /**
         * Mutator for the lastPointSize.
         *
         * @param lastPointSize The lastPointSize to set.
         */
        public void setLastPointSize(float lastPointSize)
        {
            myLastPointSize = lastPointSize;
        }

        /**
         * Mutator for the lastTextureGroup.
         *
         * @param lastTextureGroup The lastTextureGroup to set.
         */
        public void setLastTextureGroup(TextureGroup lastTextureGroup)
        {
            myLastTextureGroup = lastTextureGroup;
        }

        /**
         * Mutator for the lastTextureHandle.
         *
         * @param lastTextureHandle The lastTextureHandle to set.
         */
        public void setLastTextureHandle(TextureHandle lastTextureHandle)
        {
            myLastTextureHandle = lastTextureHandle;
        }

        /**
         * Mutator for the whiteTextureHandle.
         *
         * @param whiteTextureHandle The whiteTextureHandle to set.
         */
        public void setWhiteTextureHandle(TextureHandle whiteTextureHandle)
        {
            myWhiteTextureHandle = whiteTextureHandle;
        }
    }
}
