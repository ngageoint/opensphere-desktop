package io.opensphere.core.pipeline.renderer.immediate;

import java.awt.Color;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GL2GL3;

import org.apache.log4j.Logger;

import gnu.trove.list.TIntList;
import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonMeshGeometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.processor.PolygonMeshData;
import io.opensphere.core.pipeline.processor.TextureModelData;
import io.opensphere.core.pipeline.processor.TileData;
import io.opensphere.core.pipeline.processor.TileData.TileMeshData;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.util.PolygonRenderUtil;
import io.opensphere.core.pipeline.util.GL2LightHandler;
import io.opensphere.core.pipeline.util.GL2Utilities;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.pipeline.util.TextureGroup;
import io.opensphere.core.pipeline.util.TextureHandle;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * Immediate mode GL tile renderer.
 */
public class PolygonMeshRendererImmediate extends AbstractRenderer<PolygonMeshGeometry>
        implements GeometryRendererImmediate<PolygonMeshGeometry>, Comparator<PolygonMeshGeometry>
{
    /** Bits used for {@link GL2#glPushAttrib(int)}. */
    private static final int ATTRIB_BITS = GL2.GL_CURRENT_BIT | GL2.GL_POLYGON_BIT | GL.GL_COLOR_BUFFER_BIT | GL2.GL_ENABLE_BIT
            | GL2.GL_LIGHTING_BIT | GL.GL_DEPTH_BUFFER_BIT;

    /** Bits used for {@link GL2#glPushClientAttrib(int)}. */
    private static final int CLIENT_ATTRIB_BITS = -1;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PolygonMeshRendererImmediate.class);

    /**
     * Construct the renderer.
     *
     * @param cache the cache holding the model coordinates for the geometries
     */
    protected PolygonMeshRendererImmediate(CacheProvider cache)
    {
        super(cache);
    }

    @Override
    public void cleanupShaders(RenderContext rc, Collection<? extends PolygonMeshGeometry> input)
    {
    }

    @Override
    public void doRender(RenderContext rc, Collection<? extends PolygonMeshGeometry> input,
            Collection<? super PolygonMeshGeometry> rejected, PickManager pickManager, MapContext<?> mapContext,
            ModelDataRetriever<PolygonMeshGeometry> dataRetriever)
    {
        PolygonMeshRenderData renderData = (PolygonMeshRenderData)getRenderData();
        if (renderData == null)
        {
            return;
        }

        try
        {
            rc.getGL().glEnable(GL.GL_POLYGON_OFFSET_FILL);

            rc.getGL().glEnable(GL.GL_BLEND);

            rc.getGL().glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

            PolygonRenderUtil.setupGL(rc.getGL(), rc.getRenderMode(),
                    false, false);
            rc.getGL().glDisable(GL.GL_CULL_FACE);

            LightingModelConfigGL lastLight = null;
            List<? extends PolygonMeshGeometry> toRender = New.list(input);
            toRender.sort(this);
            for (final PolygonMeshGeometry geom : toRender)
            {
                if (rc.getRenderMode() == AbstractGeometry.RenderMode.DRAW)
                {
                    lastLight = GL2LightHandler.setLight(rc, lastLight, geom.getRenderProperties().getLighting());
                }

                Pair<PolygonMeshData, TextureGroup> modelData = getModelData(rc, dataRetriever, renderData, geom);

                try
                {
                    if (modelData == null)
                    {
                        rejected.add(geom);
                    }
                    else
                    {
                        TextureGroup textureGroup = modelData.getSecondObject();
                        Object textureHandleKey = textureGroup == null ? null
                                : textureGroup.getTextureMap().get(rc.getRenderMode());
                        TextureHandle handle = textureHandleKey == null ? null
                                : getCache().getCacheAssociation(textureHandleKey, TextureHandle.class);

                        rc.glDepthMask(geom.getRenderProperties().isObscurant());

                        if (handle != null)
                        {
                            rc.getGL().glEnable(GL.GL_TEXTURE_2D);
                            rc.getGL().getGL2().glTexEnvfv(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_COLOR,
                                    new float[] { 1f, 1f, 1f, 1f }, 0);
                            rc.getGL().getGL2().glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE, GL.GL_BLEND);
                            rc.getGL().glBindTexture(GL.GL_TEXTURE_2D, handle.getTextureId());
                            rc.getGL().glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
                            rc.getGL().glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
                            rc.glColorARGB(Color.black.getRGB());
                        }
                        else
                        {
                            GL2Utilities.glColor(rc, pickManager, geom);
                        }

                        GL2Utilities.renderWithTransform(rc, geom.getRenderProperties().getTransform(),
                            () -> PolygonRenderUtil.drawPolygonMesh(rc.getGL(), modelData.getFirstObject()));
                    }
                }
                finally
                {
                    rc.getGL().glDisable(GL.GL_TEXTURE_2D);
                }
            }
        }
        finally
        {
            rc.popAttributes();
        }
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
                    drawMode = GL2GL3.GL_QUADS;
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
        return PolygonMeshGeometry.class;
    }

    @Override
    public void initializeShaders(RenderContext rc, Collection<? extends PolygonMeshGeometry> input)
    {
    }

    @Override
    public void preRender(Collection<? extends PolygonMeshGeometry> input, Collection<? extends PolygonMeshGeometry> drawable,
            Collection<? extends PolygonMeshGeometry> pickable, PickManager pickManager,
            ModelDataRetriever<PolygonMeshGeometry> dataRetriever, Projection projection)
    {
        super.preRender(input, drawable, pickable, pickManager, dataRetriever, projection);
        Map<PolygonMeshGeometry, Pair<PolygonMeshData, TextureGroup>> renderDataMap = New.weakMap(input.size());
        for (PolygonMeshGeometry geom : input)
        {
            TextureModelData modelData = (TextureModelData)dataRetriever.getModelData(geom, projection, null, TimeBudget.ZERO);

            if (modelData.getModelData() != null)
            {
                Pair<PolygonMeshData, TextureGroup> pair = new Pair<>((PolygonMeshData)modelData.getModelData(),
                        modelData.getTextureGroup());
                renderDataMap.put(geom, pair);
            }
        }

        setRenderData(new PolygonMeshRenderData(renderDataMap, projection));
    }

    @Override
    protected Logger getLogger()
    {
        return LOGGER;
    }

    /**
     * Get the model data from the render data or request it from the data
     * retriever if it's missing.
     *
     * @param rc The rc.
     * @param dataRetriever The data retriever.
     * @param renderData The render data.
     * @param geom The geom.
     * @return The model data.
     */
    private Pair<PolygonMeshData, TextureGroup> getModelData(RenderContext rc,
            ModelDataRetriever<PolygonMeshGeometry> dataRetriever, PolygonMeshRenderData renderData,
            final PolygonMeshGeometry geom)
    {
        Pair<PolygonMeshData, TextureGroup> modelData = renderData.getData().get(geom);
        if (modelData == null)
        {
            TextureModelData textureModelData = (TextureModelData)dataRetriever.getModelData(geom, renderData.getProjection(),
                    null, TimeBudget.ZERO);
            if (textureModelData != null)
            {
                modelData = new Pair<>((PolygonMeshData)textureModelData.getModelData(), textureModelData.getTextureGroup());
                renderData.getData().put(geom, modelData);
            }
        }
        return modelData;
    }

    /**
     * A factory for creating this renderer.
     */
    public static class Factory extends GeometryRendererImmediate.Factory<PolygonMeshGeometry>
    {
        @Override
        public GeometryRendererImmediate<PolygonMeshGeometry> createRenderer()
        {
            return new PolygonMeshRendererImmediate(getCache());
        }

        @Override
        public Class<? extends Geometry> getType()
        {
            return PolygonMeshGeometry.class;
        }

        @Override
        public boolean isViable(RenderContext rc, Collection<String> warnings)
        {
            return true;
        }
    }

    /** Data used for rendering. */
    private static class PolygonMeshRenderData extends RenderData
    {
        /** The map of geometries to pairs of data. */
        private final Map<PolygonMeshGeometry, Pair<PolygonMeshData, TextureGroup>> myData;

        /**
         * Constructor.
         *
         * @param data The map of geometries to pairs of render data.
         * @param projection The projection snapshot used to generate this data.
         */
        public PolygonMeshRenderData(Map<PolygonMeshGeometry, Pair<PolygonMeshData, TextureGroup>> data, Projection projection)
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
        public Map<PolygonMeshGeometry, Pair<PolygonMeshData, TextureGroup>> getData()
        {
            return myData;
        }
    }

    @Override
    public int compare(PolygonMeshGeometry o1, PolygonMeshGeometry o2)
    {
        int compare = 0;

        if (o1.getTextureCoords() == null)
        {
            compare = -1;
        }
        else if (o2.getTextureCoords() == null)
        {
            compare = 1;
        }

        return compare;
    }
}
