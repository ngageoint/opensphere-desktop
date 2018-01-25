package io.opensphere.core.pipeline.renderer.buffered;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonMeshGeometry;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.processor.PolygonMeshData;
import io.opensphere.core.pipeline.processor.TextureModelData;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
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
import io.opensphere.core.util.lang.UnexpectedEnumException;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * Polygon mesh renderer that uses server-side buffering.
 */
public class PolygonMeshRendererBuffered extends AbstractRenderer<PolygonMeshGeometry> implements Comparator<PolygonMeshGeometry>
{
    /** Bits used for {@link GL2#glPushAttrib(int)}. */
    private static final int ATTRIB_BITS = GL2.GL_CURRENT_BIT | GL2.GL_POLYGON_BIT | GL.GL_COLOR_BUFFER_BIT | GL2.GL_ENABLE_BIT
            | GL2.GL_LIGHTING_BIT | GL.GL_DEPTH_BUFFER_BIT;

    /** Bits used for {@link GL2#glPushClientAttrib(int)}. */
    private static final int CLIENT_ATTRIB_BITS = GL2.GL_CLIENT_VERTEX_ARRAY_BIT;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PolygonMeshRendererBuffered.class);

    /**
     * Construct the renderer.
     *
     * @param cache The geometry cache.
     */
    public PolygonMeshRendererBuffered(CacheProvider cache)
    {
        super(cache);
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
                    ScreenPosition.class.isAssignableFrom(input.iterator().next().getPositionType()),
                    isDebugFeatureOn("TessellationLines"));
            rc.getGL().glDisable(GL.GL_CULL_FACE);

            LightingModelConfigGL lastLight = null;
            List<? extends PolygonMeshGeometry> toRender = New.list(input);
            toRender.sort(this);
            for (final PolygonMeshGeometry geom : toRender)
            {
                try
                {
                    Pair<PolygonMeshDataBuffered, TextureGroup> meshAndTexture = getMesh(rc, dataRetriever, renderData, geom);
                    PolygonMeshDataBuffered mesh = meshAndTexture.getFirstObject();
                    TextureGroup textureGroup = meshAndTexture.getSecondObject();
                    if (mesh != null)
                    {
                        if (rc.getRenderMode() == AbstractGeometry.RenderMode.DRAW)
                        {
                            lastLight = GL2LightHandler.setLight(rc, lastLight, geom.getRenderProperties().getLighting());
                        }
                        rc.glDepthMask(geom.getRenderProperties().isObscurant());

                        if (textureGroup != null)
                        {
                            rc.glColorARGB(Color.black.getRGB());
                        }
                        else
                        {
                            GL2Utilities.glColor(rc, pickManager, geom);
                        }

                        Object textureHandleKey = null;
                        TextureHandle handle = null;
                        if (geom.getTextureCoords() != null)
                        {
                            textureHandleKey = getTextureHandleKeyForMode(rc.getRenderMode(), textureGroup);
                            handle = textureHandleKey == null ? null
                                    : getCache().getCacheAssociation(textureHandleKey, TextureHandle.class);

                            if (handle != null)
                            {
                                rc.getGL().glEnable(GL.GL_TEXTURE_2D);
                                rc.getGL().getGL2().glTexEnvfv(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_COLOR,
                                        new float[] { 1f, 1f, 1f, 1f }, 0);
                                rc.getGL().getGL2().glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE, GL.GL_BLEND);
                                rc.getGL().glBindTexture(GL.GL_TEXTURE_2D, handle.getTextureId());
                                rc.getGL().glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
                                rc.getGL().glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
                            }
                        }

                        GL2Utilities.renderWithTransform(rc, geom.getRenderProperties().getTransform(), () -> mesh.draw(rc));

                        PolygonMeshData modelData = mesh.get(0).getPolygonMeshData();
                        // If the buffered data is not already in the cache,
                        // add
                        // it
                        // to enable the GPU memory to be cleaned up.
                        getCache().putCacheAssociation(modelData, mesh, PolygonMeshDataBuffered.class, 0L, mesh.getSizeGPU());
                    }
                    else
                    {
                        rejected.add(geom);
                    }
                }
                finally
                {
                    rc.getGL().getGL2().glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
                    rc.getGL().getGL2().glTexEnvfv(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_COLOR,
                            new float[] { 0f, 0f, 0f, 0f }, 0);
                    rc.getGL().glDisable(GL.GL_TEXTURE_2D);
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
        return PolygonMeshGeometry.class;
    }

    @Override
    public void preRender(Collection<? extends PolygonMeshGeometry> input, Collection<? extends PolygonMeshGeometry> drawable,
            Collection<? extends PolygonMeshGeometry> pickable, PickManager pickManager,
            ModelDataRetriever<PolygonMeshGeometry> dataRetriever, Projection projectionSnapshot)
    {
        super.preRender(input, drawable, pickable, pickManager, dataRetriever, projectionSnapshot);

        Map<PolygonMeshGeometry, Pair<PolygonMeshDataBuffered, TextureGroup>> renderDataMap = New.weakMap(input.size());
        for (PolygonMeshGeometry geom : input)
        {
            Pair<PolygonMeshDataBuffered, TextureGroup> meshAndTexture = getPolygonMeshBuffered(geom, dataRetriever,
                    projectionSnapshot, TimeBudget.ZERO);

            if (meshAndTexture.getFirstObject() != null)
            {
                renderDataMap.put(geom, meshAndTexture);
            }
        }

        setRenderData(new PolygonMeshRenderData(renderDataMap, projectionSnapshot));
    }

    @Override
    protected Logger getLogger()
    {
        return LOGGER;
    }

    /**
     * Get the mesh from the render data or request it from the data retriever
     * if it's missing.
     *
     * @param rc The render context.
     * @param dataRetriever The data retriever.
     * @param renderData The render data.
     * @param geom The geometry.
     * @return The mesh.
     */
    private Pair<PolygonMeshDataBuffered, TextureGroup> getMesh(RenderContext rc,
            ModelDataRetriever<PolygonMeshGeometry> dataRetriever, PolygonMeshRenderData renderData,
            final PolygonMeshGeometry geom)
    {
        Pair<PolygonMeshDataBuffered, TextureGroup> meshAndTexture = renderData.getData().get(geom);
        if (meshAndTexture == null || meshAndTexture.getFirstObject() == null)
        {
            meshAndTexture = getPolygonMeshBuffered(geom, dataRetriever, renderData.getProjection(), rc.getTimeBudget());
            if (meshAndTexture.getFirstObject() != null)
            {
                renderData.getData().put(geom, meshAndTexture);
            }
        }

        return meshAndTexture;
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
     * @return The buffer data.
     */
    private Pair<PolygonMeshDataBuffered, TextureGroup> getPolygonMeshBuffered(PolygonMeshGeometry geom,
            ModelDataRetriever<PolygonMeshGeometry> dataRetriever, Projection projectionSnapshot, TimeBudget timeBudget)
    {
        PolygonMeshDataBuffered bufferedData = null;
        TextureModelData textureModel = (TextureModelData)dataRetriever.getModelData(geom, projectionSnapshot, null,
                TimeBudget.ZERO);
        TextureGroup textureGroup = null;
        if (textureModel != null)
        {
            textureGroup = textureModel.getTextureGroup();
            PolygonMeshData modelData = (PolygonMeshData)textureModel.getModelData();
            if (modelData != null)
            {
                bufferedData = getCache().getCacheAssociation(modelData, PolygonMeshDataBuffered.class);
                if (bufferedData == null)
                {
                    bufferedData = new PolygonMeshDataBuffered(modelData);
                }
            }
        }
        return new Pair<>(bufferedData, textureGroup);
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
     * A factory for creating this renderer.
     */
    public static class Factory extends AbstractRenderer.Factory<PolygonMeshGeometry>
    {
        @Override
        public GeometryRenderer<PolygonMeshGeometry> createRenderer()
        {
            return new PolygonMeshRendererBuffered(getCache());
        }

        @Override
        public Collection<? extends BufferDisposalHelper<?>> getDisposalHelpers()
        {
            return Collections.singleton(BufferDisposalHelper.create(PolygonMeshDataBuffered.class, getCache()));
        }

        @Override
        public Class<? extends Geometry> getType()
        {
            return PolygonMeshGeometry.class;
        }

        @Override
        public boolean isViable(RenderContext rc, Collection<String> warnings)
        {
            return rc.is11Available(getClass().getEnclosingClass().getSimpleName() + " is not viable", warnings);
        }
    }

    /** Data used for rendering. */
    private static class PolygonMeshRenderData extends RenderData
    {
        /** The map of geometries to data. */
        private final Map<PolygonMeshGeometry, Pair<PolygonMeshDataBuffered, TextureGroup>> myData;

        /**
         * Constructor.
         *
         * @param data The map of geometries to data.
         * @param projection The projection snapshot used to generate this data.
         */
        public PolygonMeshRenderData(Map<PolygonMeshGeometry, Pair<PolygonMeshDataBuffered, TextureGroup>> data,
                Projection projection)
        {
            super(projection);
            myData = data;
        }

        /**
         * Get the data.
         *
         * @return the data
         */
        public Map<PolygonMeshGeometry, Pair<PolygonMeshDataBuffered, TextureGroup>> getData()
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
