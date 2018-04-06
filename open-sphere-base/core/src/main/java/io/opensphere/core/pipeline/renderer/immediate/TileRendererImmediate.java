package io.opensphere.core.pipeline.renderer.immediate;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES1;
import com.jogamp.opengl.GL2GL3;

import org.apache.log4j.Logger;

import gnu.trove.list.TIntList;
import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.processor.TextureModelData;
import io.opensphere.core.pipeline.processor.TileData;
import io.opensphere.core.pipeline.processor.TileData.TileMeshData;
import io.opensphere.core.pipeline.renderer.AbstractTileRenderer;
import io.opensphere.core.pipeline.util.GL2Utilities;
import io.opensphere.core.pipeline.util.GLUtilities;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.pipeline.util.TextureGroup;
import io.opensphere.core.pipeline.util.TextureHandle;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.UnexpectedEnumException;
import io.opensphere.core.viewer.impl.MapContext;
import io.opensphere.core.viewer.impl.PositionConverter;

/**
 * Immediate mode GL tile renderer.
 */
@SuppressWarnings("PMD.GodClass")
public class TileRendererImmediate extends AbstractTileRenderer implements GeometryRendererImmediate<TileGeometry>
{
    /** Bits used for {@link GL2#glPushAttrib(int)}. */
    private static final int ATTRIB_BITS = GL.GL_COLOR_BUFFER_BIT | GL2.GL_CURRENT_BIT | GL.GL_DEPTH_BUFFER_BIT
            | GL2.GL_TEXTURE_BIT | GL2.GL_POLYGON_BIT;

    /** Bits used for {@link GL2#glPushClientAttrib(int)}. */
    private static final int CLIENT_ATTRIB_BITS = -1;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(TileRendererImmediate.class);

    /** Alpha component for outlines. */
    private static final float OUTLINE_ALPHA = .5f;

    /** Blue component for outlines. */
    private static final float OUTLINE_BLUE = .3f;

    /** Green component for outlines. */
    private static final float OUTLINE_GREEN = 0f;

    /** Red component for outlines. */
    private static final float OUTLINE_RED = .6f;

    /**
     * Construct the renderer.
     *
     * @param cache the cache holding the model coordinates for the geometries
     */
    protected TileRendererImmediate(CacheProvider cache)
    {
        super(cache);
    }

    @Override
    public void cleanupShaders(RenderContext rc, Collection<? extends TileGeometry> input)
    {
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

        if (rc.getRenderMode() == AbstractGeometry.RenderMode.DRAW)
        {
            render(rc, input, rejected, pickManager, dataRetriever, renderData);

            if (isAnyDebugFeatureOn())
            {
                if (isTileEllipsoidsOn())
                {
                    drawEllipsoids(rc, input, mapContext);
                }
                if (isTileEllipsoidAxesOn())
                {
                    drawEllipsoidAxes(rc, input, mapContext);
                }
                if (isTileBordersOn())
                {
                    drawTileBorders(rc, input, mapContext);
                }
                if (isTileLabelsOn())
                {
                    drawTileLabels(rc, input, mapContext);
                }
                if (isTessellationLinesOn())
                {
                    drawTessellationLines(rc, input, dataRetriever, renderData, rc.getTimeBudget());
                }
            }
        }
        else if (rc.getRenderMode() == AbstractGeometry.RenderMode.PICK)
        {
            renderPick(rc, input, rejected, pickManager, dataRetriever, renderData);
        }
        else
        {
            throw new UnexpectedEnumException(rc.getRenderMode());
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
    public void initializeShaders(RenderContext rc, Collection<? extends TileGeometry> input)
    {
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
        Map<TileGeometry, TextureModelData> renderMap = New.weakMap(input.size());

        for (TileGeometry geom : input)
        {
            TextureModelData modelData = (TextureModelData)dataRetriever.getModelData(geom, projection, null, TimeBudget.ZERO);
            if (modelData != null)
            {
                renderMap.put(geom, modelData);
            }
        }

        setRenderData(new TileRenderData(renderMap, projection));
    }

    /**
     * Helper method that sends the texture coordinates and vertex commands to
     * GL. This assumes that the texture has already been bound.
     *
     * @param rc The render context.
     * @param geom The geometry being drawn.
     * @param tc The tile data.
     */
    protected void draw(RenderContext rc, final TileGeometry geom, TileData tc)
    {
        rc.glDepthMask(geom.getRenderProperties().isObscurant());
        // These settings must happen after the texture is bound.
        if (rc.isClampToBorderSupported())
        {
            rc.getGL().glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL2GL3.GL_CLAMP_TO_BORDER);
            rc.getGL().glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL2GL3.GL_CLAMP_TO_BORDER);
        }
        if (!geom.isTranslucent())
        {
            rc.getGL().glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
            rc.getGL().glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        }

        for (TileMeshData mesh : tc.getMeshes())
        {
            List<? extends Vector2d> textureCoords = mesh.getTextureCoords();
            List<? extends Vector3d> modelCoords = mesh.getMeshData().getVertexData().getModelCoords();
            TIntList modelIndices = mesh.getMeshData().getModelIndices();

            int numVertices = modelIndices == null ? textureCoords.size() : modelIndices.size();

            int drawMode;
            switch (mesh.getMeshData().getTesseraVertexCount())
            {
                case TRIANGLE_VERTEX_COUNT:
                    drawMode = GL.GL_TRIANGLES;
                    break;
                case QUAD_VERTEX_COUNT:
                    drawMode = GL2.GL_QUADS;
                    break;
                default:
                    drawMode = GL2.GL_POLYGON;
                    break;
            }

            rc.getGL().getGL2().glBegin(drawMode);
            try
            {
                if (modelIndices == null)
                {
                    for (int i = 0; i < numVertices; ++i)
                    {
                        Vector2d texCoord = textureCoords.get(i);
                        Vector3d modelCoord = modelCoords.get(i);
                        rc.getGL().getGL2().glTexCoord2f((float)texCoord.getX(), (float)texCoord.getY());
                        rc.getGL().getGL2().glVertex3d((float)modelCoord.getX(), (float)modelCoord.getY(),
                                (float)modelCoord.getZ());
                    }
                }
                else
                {
                    for (int i = 0; i < numVertices; ++i)
                    {
                        int index = modelIndices.get(i);
                        Vector3d modelCoord = modelCoords.get(index);
                        Vector2d texCoord = textureCoords.get(index);
                        rc.getGL().getGL2().glTexCoord2f((float)texCoord.getX(), (float)texCoord.getY());
                        rc.getGL().getGL2().glVertex3d((float)modelCoord.getX(), (float)modelCoord.getY(),
                                (float)modelCoord.getZ());
                    }
                }
            }
            finally
            {
                rc.getGL().getGL2().glEnd();
            }
        }
    }

    /**
     * Debugging method that will draw the outline of the tiles.
     * <p>
     * NOTE: calling this method in a display list may cause rendering problems
     * because it pushes/pops GL attributes.
     *
     * @param rc The render context.
     * @param onscreen The tiles to draw.
     * @param dataRetriever The model data retriever.
     * @param renderData The render data gathered during pre-render.
     * @param timeBudget The time budget.
     */
    protected void drawTessellationLines(RenderContext rc, Collection<? extends TileGeometry> onscreen,
            ModelDataRetriever<TileGeometry> dataRetriever, TileRenderData renderData, TimeBudget timeBudget)
    {
        rc.getGL().getGL2().glPushAttrib(ATTRIB_BITS ^ GL2.GL_TEXTURE_BIT);
        try
        {
            rc.getGL().glDisable(GL.GL_DEPTH_TEST);
            rc.getGL().getGL2().glPolygonMode(GL.GL_FRONT, GL2GL3.GL_LINE);
            rc.getGL().glEnable(GL.GL_CULL_FACE);
            rc.getGL().glEnable(GL.GL_BLEND);
            rc.getGL().glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            rc.getGL().glCullFace(GL.GL_BACK);
            rc.getGL().getGL2().glColor4f(OUTLINE_RED, OUTLINE_GREEN, OUTLINE_BLUE, OUTLINE_ALPHA);
            for (final TileGeometry geom : onscreen)
            {
                TileData tc = (TileData)((TextureModelData)dataRetriever.getModelData(geom, renderData.getProjection(), null,
                        timeBudget)).getModelData();
                if (tc != null)
                {
                    draw(rc, geom, tc);
                }
            }
        }
        finally
        {
            rc.getGL().getGL2().glPopAttrib();
        }
    }

    /**
     * Debugging method that will draw the borders of the tiles.
     * <p>
     * NOTE: calling this method in a display list may cause rendering problems
     * because it pushes/pops GL attributes.
     *
     * @param rc The render context.
     * @param onscreen The tiles to draw.
     * @param mapContext The map context.
     */
    protected void drawTileBorders(RenderContext rc, Collection<? extends TileGeometry> onscreen, MapContext<?> mapContext)
    {
        GL gl = rc.getGL();
        gl.getGL2().glPushAttrib(GL2.GL_CURRENT_BIT | GL2.GL_POLYGON_BIT);
        try
        {
            gl.getGL2().glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
            Vector3d modelCenter = rc.getCurrentModelCenter();
            PositionConverter positionConverter = new PositionConverter(mapContext);

            gl.getGL2().glColor4f(OUTLINE_RED, OUTLINE_GREEN, OUTLINE_BLUE, OUTLINE_ALPHA);
            final float lineWidth = 3f;
            gl.glLineWidth(lineWidth);
            for (TileGeometry tileGeometry : onscreen)
            {
                Class<? extends Position> positionType = tileGeometry.getPositionType();
                List<Vector3d> vecs = positionConverter.convertLinesToModel(tileGeometry.getBounds().getVertices(), positionType,
                        LineType.STRAIGHT_LINE, null, modelCenter);
                gl.getGL2().glBegin(GL.GL_LINE_STRIP);
                try
                {
                    for (Vector3d modelCoord : vecs)
                    {
                        gl.getGL2().glVertex3d((float)modelCoord.getX(), (float)modelCoord.getY(), (float)modelCoord.getZ());
                    }
                }
                finally
                {
                    gl.getGL2().glEnd();
                }
            }
        }
        finally
        {
            gl.getGL2().glPopAttrib();
        }
    }

    @Override
    protected Logger getLogger()
    {
        return LOGGER;
    }

    /**
     * Helper method to handle normal rendering.
     *
     * @param rc The render context.
     * @param input The input geometries.
     * @param rejected The output rejected geometries.
     * @param pickManager Determines pick colors for the tiles.
     * @param dataRetriever The model data retriever.
     * @param renderData The render data gathered during pre-render.
     */
    protected void render(RenderContext rc, Collection<? extends TileGeometry> input, Collection<? super TileGeometry> rejected,
            PickManager pickManager, ModelDataRetriever<TileGeometry> dataRetriever, TileRenderData renderData)
    {
        try
        {
            // TODO - need to adjust this so that multiple layers can be
            // rendered with transparent tiles.
            // gl.glDisable(GL.GL_DEPTH_TEST);
            rc.getGL().glEnable(GL.GL_POLYGON_OFFSET_FILL);
            rc.getGL().glEnable(GL.GL_CULL_FACE);
            rc.getGL().glEnable(GL.GL_TEXTURE_2D);
            rc.getGL().glEnable(GL.GL_BLEND);
            rc.getGL().getGL2().glTexEnvfv(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_COLOR, new float[] { 1f, 1f, 1f, 1f }, 0);
            rc.getGL().getGL2().glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE, GL.GL_BLEND);
            rc.getGL().glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

            for (final TileGeometry geom : input)
            {
                @SuppressWarnings("unchecked")
                Class<? extends BoundingBox<?>> positionType = (Class<? extends BoundingBox<?>>)geom.getBounds().getClass();
                // Depth testing should be on for tiles with terrain, so that
                // the triangles appear correctly. For screen tiles depth
                // testing should be off so that they always render regardless
                // of their location in model coordinates.
                if (ScreenBoundingBox.class.isAssignableFrom(positionType))
                {
                    rc.getGL().glDisable(GL.GL_DEPTH_TEST);
                }
                TextureModelData modelData = renderData.getData().get(geom);
                if (modelData == null || modelData.getModelData() == null || modelData.getTextureGroup() == null)
                {
                    modelData = (TextureModelData)dataRetriever.getModelData(geom, renderData.getProjection(), modelData,
                            TimeBudget.ZERO);
                }
                TileData tc = (TileData)modelData.getModelData();
                TextureGroup textureGroup = modelData.getTextureGroup();
                Object textureHandleKey = textureGroup == null ? null : textureGroup.getTextureMap().get(rc.getRenderMode());
                TextureHandle handle = textureHandleKey == null ? null
                        : getCache().getCacheAssociation(textureHandleKey, TextureHandle.class);
                if (tc == null || textureGroup == null || handle == null)
                {
                    rejected.add(geom);
                }
                else
                {
                    if (!isTileTexturesOff())
                    {
                        GL2Utilities.glColor(rc, pickManager, geom);

                        rc.getGL().glBindTexture(GL.GL_TEXTURE_2D, handle.getTextureId());
                        draw(rc, geom, tc);

                        GLUtilities.checkGLErrors(rc.getGL(), LOGGER, "after tile render");
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
     * Helper method to handle pick rendering. This draws filled polygons using
     * pick colors instead of rendering the textures.
     *
     * @param rc The render context.
     * @param input The input geometries.
     * @param rejected The output rejected geometries.
     * @param pickManager Determines pick colors for the tiles.
     * @param dataRetriever The model data retriever.
     * @param renderData The render data gathered during pre-render.
     */
    protected void renderPick(RenderContext rc, Collection<? extends TileGeometry> input,
            Collection<? super TileGeometry> rejected, PickManager pickManager, ModelDataRetriever<TileGeometry> dataRetriever,
            TileRenderData renderData)
    {
        try
        {
            rc.getGL().getGL2().glPolygonMode(GL.GL_FRONT, GL2GL3.GL_FILL);
            rc.getGL().glEnable(GL.GL_POLYGON_OFFSET_FILL);
            rc.getGL().glEnable(GL.GL_CULL_FACE);
            rc.getGL().glEnable(GL.GL_TEXTURE_2D);
            rc.getGL().glEnable(GL.GL_BLEND);
            rc.getGL().glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

            if (rc.is13Available())
            {
                rc.getGL().getGL2().glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_COMBINE_RGB, GL.GL_REPLACE);
                rc.getGL().getGL2().glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2.GL_SOURCE0_RGB, GL2ES1.GL_PRIMARY_COLOR);
                if (rc.isMultiTextureAvailable())
                {
                    rc.getGL().getGL2().glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2.GL_SOURCE0_ALPHA, GL.GL_TEXTURE0);
                }
                else
                {
                    rc.getGL().getGL2().glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2.GL_SOURCE0_ALPHA, GL.GL_TEXTURE);
                }
            }
            rc.getGL().getGL2().glTexEnvfv(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_COLOR, new float[] { 1f, 1f, 1f, 1f }, 0);

            TextureHandle whiteTextureHandle = null;

            for (final TileGeometry geom : input)
            {
                TextureModelData modelData = renderData.getData().get(geom);
                if (modelData == null)
                {
                    modelData = (TextureModelData)dataRetriever.getModelData(geom, renderData.getProjection(), null,
                            rc.getTimeBudget());
                }
                TileData tc = (TileData)modelData.getModelData();
                TextureGroup texture = modelData.getTextureGroup();
                if (tc == null)
                {
                    rejected.add(geom);
                }
                else
                {
                    if (texture != null && texture.getTextureMap().get(AbstractGeometry.RenderMode.PICK) != null)
                    {
                        rc.getGL().getGL2().glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE, GL.GL_BLEND);
                        rc.getGL().getGL2().glColor4d(0., 0., 0., 1.);
                        Object textureHandleKey = texture.getTextureMap().get(AbstractGeometry.RenderMode.PICK);
                        TextureHandle handle = getCache().getCacheAssociation(textureHandleKey, TextureHandle.class);
                        if (handle != null)
                        {
                            rc.getGL().glBindTexture(GL.GL_TEXTURE_2D, handle.getTextureId());
                            draw(rc, geom, tc);
                        }
                    }
                    else
                    {
                        if (rc.is13Available())
                        {
                            rc.getGL().getGL2().glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE, GL2ES1.GL_COMBINE);
                        }
                        pickManager.glColor(rc.getGL(), geom);

                        Object textureHandleKey = texture == null ? null
                                : texture.getTextureMap().get(AbstractGeometry.RenderMode.DRAW);
                        if (textureHandleKey == null)
                        {
                            whiteTextureHandle = GLUtilities.bindSolidWhiteTexture(rc, getCache(), whiteTextureHandle);
                            draw(rc, geom, tc);
                        }
                        else
                        {
                            TextureHandle handle = getCache().getCacheAssociation(textureHandleKey, TextureHandle.class);
                            if (handle != null)
                            {
                                rc.getGL().glBindTexture(GL.GL_TEXTURE_2D, handle.getTextureId());
                                draw(rc, geom, tc);
                            }
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

    /** A factory for creating this renderer. */
    public static class Factory extends GeometryRendererImmediate.Factory<TileGeometry>
    {
        @Override
        public GeometryRendererImmediate<TileGeometry> createRenderer()
        {
            return new TileRendererImmediate(getCache());
        }

        @Override
        public Class<? extends Geometry> getType()
        {
            return TileGeometry.class;
        }

        @Override
        public boolean isViable(RenderContext rc, Collection<String> warnings)
        {
            return true;
        }
    }

    /** Data used for rendering. */
    private static class TileRenderData extends RenderData
    {
        /** The map of geometries to pairs of data. */
        private final Map<TileGeometry, TextureModelData> myData;

        /**
         * Constructor.
         *
         * @param data The map of geometries to pairs of render data.
         * @param projection The projection snapshot used to generate this data.
         */
        public TileRenderData(Map<TileGeometry, TextureModelData> data, Projection projection)
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
        public Map<TileGeometry, TextureModelData> getData()
        {
            return myData;
        }
    }
}
