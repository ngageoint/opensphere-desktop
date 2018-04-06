package io.opensphere.core.pipeline.renderer.buffered;

import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2GL3;

import org.apache.log4j.Logger;

import com.jogamp.opengl.util.texture.TextureCoords;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.RenderingCapabilities;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.BlendingConfigGL;
import io.opensphere.core.geometry.renderproperties.FragmentShaderProperties;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.Quadrilateral;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.processor.TextureModelData;
import io.opensphere.core.pipeline.processor.TileData;
import io.opensphere.core.pipeline.processor.TileData.TileMeshData;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.AbstractTileRenderer;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.pipeline.renderer.ShaderRendererUtilitiesGLSL;
import io.opensphere.core.pipeline.util.GL2LightHandler;
import io.opensphere.core.pipeline.util.GL2Utilities;
import io.opensphere.core.pipeline.util.GLBlendingHandler;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.pipeline.util.RenderToTexture;
import io.opensphere.core.pipeline.util.TextureGroup;
import io.opensphere.core.pipeline.util.TextureHandle;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.UnexpectedEnumException;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * Tile renderer that uses server-side buffering and fragment shaders. TODO Put
 * in an explanation of the caching strategy and how buffers get cleared.
 */
@SuppressWarnings("PMD.GodClass")
public class TileRendererBuffered extends AbstractTileRenderer
{
    /** Bits used for {@link GL2#glPushAttrib(int)}. */
    private static final int ATTRIB_BITS = GL2.GL_CURRENT_BIT | GL2.GL_POLYGON_BIT | GL2.GL_TEXTURE_BIT | GL.GL_COLOR_BUFFER_BIT
            | GL2.GL_ENABLE_BIT;

    /** Bits used for {@link GL2#glPushClientAttrib(int)}. */
    private static final int CLIENT_ATTRIB_BITS = GL2.GL_CLIENT_VERTEX_ARRAY_BIT;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(TileRendererBuffered.class);

    /** The buffer object for the texture coordinates. */
    private TextureCoordinateBufferObject myBackgroundTextureCoordinatesBufferObject;

    /**
     * Construct the renderer.
     *
     * @param cache The geometry cache.
     */
    public TileRendererBuffered(CacheProvider cache)
    {
        super(cache);
    }

    @Override
    public void doRender(RenderContext rc, Collection<? extends TileGeometry> input, Collection<? super TileGeometry> rejected,
            PickManager pickManager, MapContext<?> mapContext, ModelDataRetriever<TileGeometry> dataRetriever)
    {
        TileRenderData renderData = (TileRenderData)getRenderData();
        if (renderData == null || renderData.getData() == null)
        {
            return;
        }

        try
        {
            setupGL(rc);

            // Depth testing should be on for tiles with terrain, so that
            // the triangles appear correctly. For screen tiles depth
            // testing should be off so that they always render regardless
            // of their location in model coordinates.
            if (input.iterator().next().getBounds() instanceof ScreenBoundingBox)
            {
                rc.getGL().glDisable(GL.GL_DEPTH_TEST);
            }
            else
            {
                rc.getGL().glEnable(GL.GL_DEPTH_TEST);
            }

            LightingModelConfigGL lastLight = null;
            BlendingConfigGL lastBlend = null;

            if (rc.isMultiTextureAvailable())
            {
                rc.getGL().glActiveTexture(GL.GL_TEXTURE0);
            }
            for (final TileGeometry geom : input)
            {
                if (rc.getRenderMode() == AbstractGeometry.RenderMode.DRAW && RenderToTexture.getActiveInstance() == null)
                {
                    lastLight = GL2LightHandler.setLight(rc, lastLight, geom.getRenderProperties().getLighting());
                    lastBlend = GLBlendingHandler.setBlending(rc, lastBlend, geom.getRenderProperties().getBlending());
                }

                rc.glDepthMask(geom.getRenderProperties().isObscurant());

                Pair<TileDataBuffered, TextureGroup> tilePair = renderData.getData().get(geom);
                if (tilePair == null || tilePair.getFirstObject() == null
                        || tilePair.getSecondObject() == null && rc.getRenderMode() != AbstractGeometry.RenderMode.PICK)
                {
                    tilePair = getWrapperAndTexture(geom, dataRetriever, tilePair, renderData.getProjection(),
                            rc.getTimeBudget().subBudgetMilliseconds(DATA_RETRIEVAL_TIME_BUDGET_MILLIS));
                    renderData.getData().put(geom, tilePair);
                }

                TileDataBuffered tile = tilePair.getFirstObject();
                TextureGroup textureGroup = tilePair.getSecondObject();

                if (!doRender(rc, pickManager, geom, tile, textureGroup))
                {
                    // TODO This seems to happen often and should be
                    // investigated.
                    rejected.add(geom);
                }
            }
        }
        finally
        {
            cleanup(rc);
        }

        if (isAnyDebugFeatureOn())
        {
            drawDebugFeatures(rc, input, mapContext);
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
        return TileGeometry.class;
    }

    @Override
    public void preRender(Collection<? extends TileGeometry> input, Collection<? extends TileGeometry> drawable,
            Collection<? extends TileGeometry> pickable, PickManager pickManager, ModelDataRetriever<TileGeometry> dataRetriever,
            Projection projection)
    {
        if (input.isEmpty())
        {
            setRenderData(new TileRenderData(null, projection));
            return;
        }
        super.preRender(input, drawable, pickable, pickManager, dataRetriever, projection);

        TileRenderData matchingRenderData = (TileRenderData)getMatchingRenderData(projection);
        Map<TileGeometry, Pair<TileDataBuffered, TextureGroup>> map = New.weakMap(input.size());
        for (final TileGeometry geom : input)
        {
            Pair<TileDataBuffered, TextureGroup> wrapperTexturePair = null;
            if (matchingRenderData != null && matchingRenderData.getData() != null
                    && GeographicPosition.class.isAssignableFrom(geom.getPositionType()))
            {
                wrapperTexturePair = matchingRenderData.getData().get(geom);
            }

            if (wrapperTexturePair == null)
            {
                wrapperTexturePair = getWrapperAndTexture(geom, dataRetriever, null, projection, TimeBudget.ZERO);
            }

            if (wrapperTexturePair != null)
            {
                map.put(geom, wrapperTexturePair);
            }
        }
        setRenderData(new TileRenderData(map, projection));
    }

    /**
     * Free the on card memory used for the background texture's texture
     * coordinates.
     *
     * @param gl the OpenGL context.
     */
    protected void cleanupTextureBackground(GL gl)
    {
        if (myBackgroundTextureCoordinatesBufferObject != null)
        {
            myBackgroundTextureCoordinatesBufferObject.dispose(gl);
            myBackgroundTextureCoordinatesBufferObject = null;
        }
    }

    /**
     * Render a tile.
     *
     * @param rc The render context.
     * @param pickManager The pick manager.
     * @param geom The geometry to render.
     * @param tile The buffered tile data.
     * @param textureGroup The tile's texture group.
     * @return {@code true} if rendering was successful.
     */
    protected boolean doRender(RenderContext rc, PickManager pickManager, final TileGeometry geom, TileDataBuffered tile,
            TextureGroup textureGroup)
    {
        if (tile == null)
        {
            return false;
        }

        Object textureHandleKey = null;
        TextureHandle handle = null;
        TextureCoords imageTextureCoords = null;
        if (textureGroup != null)
        {
            textureHandleKey = getTextureHandleKeyForMode(rc.getRenderMode(), textureGroup);
            handle = textureHandleKey == null ? null : getCache().getCacheAssociation(textureHandleKey, TextureHandle.class);
            imageTextureCoords = textureGroup.getImageTexCoords();
        }

        if (rc.getRenderMode() == AbstractGeometry.RenderMode.DRAW)
        {
            if (handle == null)
            {
                return false;
            }
            prepareDraw(rc, geom, imageTextureCoords, handle, pickManager);
        }
        else if (rc.getRenderMode() == AbstractGeometry.RenderMode.PICK)
        {
            if (textureHandleKey != null && handle == null)
            {
                return false;
            }
            // If there's a pick texture, don't pass the pickManager
            // in since we want to use the color from the texture.
            boolean needPickManager = textureGroup == null
                    || !textureGroup.getTextureMap().containsKey(AbstractGeometry.RenderMode.PICK);
            preparePick(rc, geom, imageTextureCoords, handle, needPickManager ? pickManager : null);
        }
        else
        {
            throw new UnexpectedEnumException(rc.getRenderMode());
        }

        if (RenderToTexture.getActiveInstance() != null && rc.isMultiTextureAvailable())
        {
            setupTextureBackground(rc, geom);
        }

        // If this is the first time the TileDataBuffered is drawn, the TileData
        // it was created from will still be available. Once draw() is called,
        // it will be cleared.
        TileData dataSource = tile.getDataSource();
        setDebugTileColor(rc, tile);
        tile.draw(rc);
        // If the buffered data is not already in the cache, add it
        // to enable the GPU memory to be cleaned up.
        if (dataSource != null)
        {
            getCache().putCacheAssociation(dataSource, tile, TileDataBuffered.class, 0L, tile.getSizeGPU());
        }

        cleanupTextureBackground(rc.getGL());

        return true;
    }

    /**
     * Draw debug features.
     *
     * @param rc The render context.
     * @param input The input geometries.
     * @param mapContext The map context.
     */
    protected void drawDebugFeatures(RenderContext rc, Collection<? extends TileGeometry> input, MapContext<?> mapContext)
    {
        if (isTileLabelsOn())
        {
            drawTileLabels(rc, input, mapContext);
        }
        if (isTileEllipsoidsOn())
        {
            drawEllipsoids(rc, input, mapContext);
        }
        if (isTileEllipsoidAxesOn())
        {
            drawEllipsoidAxes(rc, input, mapContext);
        }
    }

    @Override
    protected Logger getLogger()
    {
        return LOGGER;
    }

    /**
     * Set up the geometry to be rendered in draw mode.
     *
     * @param rc The render context.
     * @param geom The geometry to render.
     * @param imgTexCoords The texture coordinates for this tile.
     * @param handle The texture handle for this tile.
     * @param pickManager The pick manager.
     */
    protected void prepareDraw(RenderContext rc, final TileGeometry geom, TextureCoords imgTexCoords, TextureHandle handle,
            PickManager pickManager)
    {
        FragmentShaderProperties shaderProps = geom.getRenderProperties().getShaderProperties();
        if (isTileProjectionColorOn())
        {
            rc.getShaderRendererUtilities().enableShaderByName(rc.getGL(),
                    ShaderRendererUtilitiesGLSL.TileShader.DEBUG_PROJECTION_COLOR, shaderProps, imgTexCoords);
        }
        else if (isTileBordersOn())
        {
            rc.getShaderRendererUtilities().enableShaderByName(rc.getGL(), ShaderRendererUtilitiesGLSL.TileShader.DEBUG,
                    shaderProps, imgTexCoords);
        }
        else
        {
            if (RenderToTexture.getActiveInstance() == null)
            {
                rc.getShaderRendererUtilities().enableShaderByName(rc.getGL(), ShaderRendererUtilitiesGLSL.TileShader.DRAW,
                        shaderProps, imgTexCoords);
            }
            else
            {
                rc.getShaderRendererUtilities().enableShaderByName(rc.getGL(),
                        ShaderRendererUtilitiesGLSL.TileShader.DRAW_NO_BLEND, shaderProps, imgTexCoords);
            }
        }
        GL2Utilities.glColor(rc, pickManager, geom);
        rc.getGL().glBindTexture(GL.GL_TEXTURE_2D, isTileTexturesOff() ? 0 : handle.getTextureId());
    }

    /**
     * Set up the geometry to be rendered in pick mode.
     *
     * @param rc The render context.
     * @param geom The geometry to render.
     * @param imgTexCoords The texture coordinates for this tile.
     * @param handle The texture handle for this tile.
     * @param pickManager The pick manager.
     */
    protected void preparePick(RenderContext rc, final TileGeometry geom, TextureCoords imgTexCoords, TextureHandle handle,
            PickManager pickManager)
    {
        FragmentShaderProperties shaderProps = geom.getRenderProperties().getShaderProperties();
        if (pickManager == null)
        {
            // Use the draw shader, since we are actually drawing the pick
            // texture.
            rc.getShaderRendererUtilities().enableShaderByName(rc.getGL(), ShaderRendererUtilitiesGLSL.TileShader.DRAW,
                    shaderProps, imgTexCoords);
            rc.glColorARGB(0xff000000);
            rc.getGL().glBindTexture(GL.GL_TEXTURE_2D, handle.getTextureId());
        }
        else
        {
            if (handle == null)
            {
                rc.getShaderRendererUtilities().enableShaderByName(rc.getGL(), ShaderRendererUtilitiesGLSL.TileShader.PICK_ONLY,
                        shaderProps, (TextureCoords)null);
            }
            else
            {
                rc.getShaderRendererUtilities().enableShaderByName(rc.getGL(), ShaderRendererUtilitiesGLSL.TileShader.PICK,
                        shaderProps, imgTexCoords);
                // Use the draw texture for picking this tile. This will
                // allow areas of the texture where the alpha is 0 to be
                // non-pickable.
                rc.getGL().glBindTexture(GL.GL_TEXTURE_2D, handle.getTextureId());
            }
            pickManager.glColor(rc.getGL(), geom);
        }
    }

    /**
     * Setup the background texture for custom blending in the shader.
     *
     * @param rc The render context.
     * @param geom The tile which is about to be rendered.
     */
    protected void setupTextureBackground(RenderContext rc, TileGeometry geom)
    {
        // To get the second glTexCoordPointer() call to bind to the second
        // texture unit, you must use glClientActiveTexture instead of
        // glActiveTexture.
        rc.getGL2().glClientActiveTexture(GL.GL_TEXTURE1);
        rc.getGL().glActiveTexture(GL.GL_TEXTURE1);
        RenderToTexture texRend = RenderToTexture.getActiveInstance();
        TextureHandle handle = getCache().getCacheAssociation(texRend.getTexture(), TextureHandle.class);
        if (handle != null)
        {
            rc.getGL().glBindTexture(GL.GL_TEXTURE_2D, handle.getTextureId());

            Quadrilateral<?> bbox = geom.getBounds();
            // TODO what if this is a screen quad?
            if (bbox instanceof ScreenBoundingBox)
            {
                FloatBuffer texCoords = texRend.getTexCoords((ScreenBoundingBox)bbox);

                myBackgroundTextureCoordinatesBufferObject = new TextureCoordinateBufferObject(texCoords);
                myBackgroundTextureCoordinatesBufferObject.bind(rc);
            }
        }

        rc.getGL().glActiveTexture(GL.GL_TEXTURE0);
        rc.getGL2().glClientActiveTexture(GL.GL_TEXTURE0);
    }

    /**
     * Clean up after a render pass.
     *
     * @param rc The render context.
     */
    private void cleanup(RenderContext rc)
    {
        if (rc.isMultiTextureAvailable())
        {
            rc.getGL().glActiveTexture(GL.GL_TEXTURE0);
            rc.getGL().glBindTexture(GL.GL_TEXTURE_2D, 0);
            rc.getGL().glActiveTexture(GL.GL_TEXTURE1);
            rc.getGL().glBindTexture(GL.GL_TEXTURE_2D, 0);
        }
        rc.getShaderRendererUtilities().cleanupShaders(rc.getGL());
        rc.popAttributes();
    }

    /**
     * Get the texture from the texture group for the specified render mode.
     *
     * @param renderMode The render mode.
     * @param textureGroup The texture group.
     * @return The texture.
     */
    private Object getTextureHandleKeyForMode(RenderMode renderMode, TextureGroup textureGroup)
    {
        Object textureHandleKey;
        if (renderMode == RenderMode.DRAW)
        {
            textureHandleKey = textureGroup.getTextureMap().get(RenderMode.DRAW);
        }
        else if (renderMode == RenderMode.PICK)
        {
            textureHandleKey = textureGroup.getTextureMap().get(RenderMode.PICK);
            if (textureHandleKey == null)
            {
                textureHandleKey = textureGroup.getTextureMap().get(RenderMode.DRAW);
            }
        }
        else
        {
            throw new UnexpectedEnumException(renderMode);
        }
        return textureHandleKey;
    }

    /**
     * Get the buffered wrapper and texture for the tile or create them if
     * necessary. This should typically happen during pre-rendering, but can
     * happen during render when necessary.
     *
     * @param geom The tile geometry for which the wrapper and image is desired.
     * @param dataRetriever The model data retriever.
     * @param tilePair Any existing part of the pair which may not need to be
     *            regenerated.
     * @param projectionSnapshot The projection snapshot used for the current
     *            render pass.
     * @param timeBudget The time budget for the request.
     * @return The buffered wrapper for the tile paired with the image.
     */
    private Pair<TileDataBuffered, TextureGroup> getWrapperAndTexture(TileGeometry geom,
            ModelDataRetriever<TileGeometry> dataRetriever, Pair<TileDataBuffered, TextureGroup> tilePair,
            Projection projectionSnapshot, TimeBudget timeBudget)
    {
        // If we already have the buffers, prevent getModelData() from creating
        // a TileData that we don't need.
        TileData td;
        if (tilePair == null || tilePair.getFirstObject() == null)
        {
            td = null;
        }
        else
        {
            td = new TileData(tilePair.getFirstObject().getImageTextureCoords(), Collections.<TileMeshData>emptyList(), -1);
        }
        TextureGroup texture = tilePair == null ? null : tilePair.getSecondObject();
        TextureModelData modelData = tilePair == null ? null : new TextureModelData(td, texture);
        modelData = (TextureModelData)dataRetriever.getModelData(geom, projectionSnapshot, modelData, timeBudget);
        if (modelData != null)
        {
            td = (TileData)modelData.getModelData();
            texture = modelData.getTextureGroup();
        }
        // If we already have the buffers, do not try to create them.
        TileDataBuffered buf = tilePair == null ? null : tilePair.getFirstObject();
        if (buf == null && td != null)
        {
            buf = getCache().getCacheAssociation(td, TileDataBuffered.class);
            if (buf == null)
            {
                buf = new TileDataBuffered(td);
            }
        }

        return new Pair<TileDataBuffered, TextureGroup>(buf, texture);
    }

    /**
     * When using a debug feature which requires a special color for the tile,
     * set the gl_Color as appropriate.
     *
     * @param rc The current render context.
     * @param tile The tile which is being rendered.
     */
    private void setDebugTileColor(RenderContext rc, TileDataBuffered tile)
    {
        if (isTileProjectionColorOn())
        {
            rc.glColorARGB(tile.getSourceProjectionHash());
        }
    }

    /**
     * Set up the necessary GL parameters.
     *
     * @param rc The render context.
     */
    private void setupGL(RenderContext rc)
    {
        boolean linesOn = isTessellationLinesOn();

        GL gl = rc.getGL();

        gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);

        if (RenderToTexture.getActiveInstance() != null)
        {
            gl.glDisable(GL.GL_BLEND);
        }

        if (rc.getRenderMode() == AbstractGeometry.RenderMode.DRAW)
        {
            if (linesOn)
            {
                gl.getGL2().glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
            }
            else
            {
                gl.getGL2().glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
            }
        }
        else
        {
            gl.getGL2().glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
        }

        gl.glEnable(GL.GL_CULL_FACE);
    }

    /** A factory for creating this renderer. */
    public static class Factory extends AbstractRenderer.Factory<TileGeometry>
    {
        @Override
        public GeometryRenderer<TileGeometry> createRenderer()
        {
            return new TileRendererBuffered(getCache());
        }

        @Override
        public Set<? extends String> getCapabilities()
        {
            Set<String> result = New.set(super.getCapabilities());
            result.add(RenderingCapabilities.TILE_SHADER);
            return result;
        }

        @Override
        public Collection<? extends BufferDisposalHelper<?>> getDisposalHelpers()
        {
            return Collections.singleton(BufferDisposalHelper.create(TileDataBuffered.class, getCache()));
        }

        @Override
        public Class<? extends Geometry> getType()
        {
            return TileGeometry.class;
        }

        @Override
        public boolean isViable(RenderContext rc, Collection<String> warnings)
        {
            return rc.is15Available(getClass().getEnclosingClass().getSimpleName() + " is not viable", warnings);
        }
    }

    /** Data used for rendering. */
    private static class TileRenderData extends RenderData
    {
        /** The map of geometries to pairs of data. */
        private final Map<TileGeometry, Pair<TileDataBuffered, TextureGroup>> myData;

        /**
         * Constructor.
         *
         * @param data The map of geometries to pairs of render data.
         * @param projection The projection used to generate this data.
         */
        public TileRenderData(Map<TileGeometry, Pair<TileDataBuffered, TextureGroup>> data, Projection projection)
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
        public Map<TileGeometry, Pair<TileDataBuffered, TextureGroup>> getData()
        {
            return myData;
        }
    }
}
