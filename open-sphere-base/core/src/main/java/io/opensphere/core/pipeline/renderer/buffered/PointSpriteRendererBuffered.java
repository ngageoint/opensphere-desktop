package io.opensphere.core.pipeline.renderer.buffered;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES1;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PointSpriteGeometry;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.processor.PointSpriteProcessor.SpriteModelCoordinates;
import io.opensphere.core.pipeline.processor.TextureModelData;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.pipeline.util.TextureGroup;
import io.opensphere.core.pipeline.util.TextureHandle;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.core.util.lang.Pair;

/**
 * Point sprite renderer that uses server-side buffering.
 *
 * @param <T> The geometry type.
 */
@SuppressWarnings("PMD.GodClass")
public class PointSpriteRendererBuffered<T extends PointSpriteGeometry> extends PointRendererBuffered<T>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PointSpriteRendererBuffered.class);

    /**
     * In order to keep the buffered data in the cache so that it is correctly
     * accounted for for GPU memory usage we need a key for the buffer which is
     * unique.
     */
    private final List<Pair<PointSpriteRendererBuffered<T>, PointSpriteRenderKey>> myBufferKeys = New.list();

    /**
     * Construct the renderer.
     *
     * @param cache The geometry cache.
     */
    public PointSpriteRendererBuffered(CacheProvider cache)
    {
        super(cache);
    }

    @Override
    public Class<?> getType()
    {
        return PointSpriteGeometry.class;
    }

    @Override
    public void preRender(Collection<? extends T> input, Collection<? extends T> drawable, Collection<? extends T> pickable,
            PickManager pickManager, final ModelDataRetriever<T> dataRetriever, Projection projection)
    {
        if (isLoggingEnabled(Level.TRACE))
        {
            log(Level.TRACE, toString() + " is pre-rendering " + input.size() + " geometries.");
        }

        ModelDataRetriever<T> specialDataRetriever = createSpecialDataRetriever(dataRetriever);

        cleanBuffersFromCache();

        Map<PointSpriteRenderKey, PointDataBuffered> buffers = New.map();
        List<PointSpriteGeometry> rejects = New.list();
        Map<PointSpriteRenderKey, List<T>> sorted = groupByTextureAndSize(input, rejects, dataRetriever, false);
        TimeSpan groupTimeSpan = getGroupInterval();
        for (Entry<PointSpriteRenderKey, List<T>> entry : sorted.entrySet())
        {
            List<T> list = entry.getValue();
            PointDataBuffered pointData = new PointDataBuffered(list, false, pickManager, specialDataRetriever, projection,
                    groupTimeSpan);
            buffers.put(entry.getKey(), pointData);
        }
        setRenderData(new PointSpriteRenderData<>(buffers, rejects, projection, groupTimeSpan));
    }

    @Override
    protected void doRenderPoints(RenderContext rc, Collection<? extends T> input, Collection<? super T> rejects,
            PickManager pickManager, ModelDataRetriever<T> dataRetriever, TimeRenderData renderData)
    {
        try
        {
            @SuppressWarnings("unchecked")
            PointSpriteRenderData<T> pointRenderData = (PointSpriteRenderData<T>)renderData;
            for (Entry<PointSpriteRenderKey, PointDataBuffered> entry : pointRenderData.getBuffers().entrySet())
            {
                PointDataBuffered pointData = entry.getValue();
                PointSpriteRenderKey key = entry.getKey();

                TextureHandle handle = getTextureHandle(rc, key.getTextureGroup(), rc.getRenderMode());
                if (handle != null)
                {
                    rc.getGL().getGL2().glPointSize(key.getSize());
                    rc.glDepthMask(key.isObscurant());
                    rc.getGL().glBindTexture(GL.GL_TEXTURE_2D, handle.getTextureId());
                    pointData.draw(rc, GL.GL_POINTS);

                    Pair<PointSpriteRendererBuffered<T>, PointSpriteRenderKey> bufferKey = Pair.create(this, key);
                    if (getCache().getCacheAssociation(bufferKey, PointDataBuffered.class) == null)
                    {
                        synchronized (myBufferKeys)
                        {
                            myBufferKeys.add(bufferKey);
                            getCache().putCacheAssociation(bufferKey, pointData, PointDataBuffered.class, 0L,
                                    pointData.getSizeGPU());
                        }
                    }
                }
            }

            if (!pointRenderData.getRejects().isEmpty())
            {
                rejects.addAll(pointRenderData.getRejects());
            }
        }
        finally
        {
            rc.popAttributes();
        }

        if (!pickManager.getPickedGeometries().isEmpty())
        {
            renderHighlights(rc, input, pickManager, dataRetriever, renderData);
        }
    }

    @Override
    protected Logger getLogger()
    {
        return LOGGER;
    }

    @Override
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
    }

    @Override
    protected void renderHighlights(RenderContext rc, Collection<? extends T> input, PickManager pickManager,
            ModelDataRetriever<T> dataRetriever, TimeRenderData renderData)
    {
        Collection<T> pickedGeomsToRender = getPickedGeometries(input, pickManager);
        if (!pickedGeomsToRender.isEmpty())
        {
            Map<PointSpriteRenderKey, List<T>> sizeSorted = groupByTextureAndSize(pickedGeomsToRender, New.list(), dataRetriever,
                    true);
            AbstractRenderer.ModelDataRetriever<T> specialDataRetriever = createSpecialDataRetriever(dataRetriever);
            for (Entry<PointSpriteRenderKey, List<T>> entry : sizeSorted.entrySet())
            {
                List<T> pointData = entry.getValue();
                PointSpriteRenderKey key = entry.getKey();

                PointDataBuffered highlightData = new PointDataBuffered(pointData, true, pickManager, specialDataRetriever,
                        renderData.getProjection(), renderData.getGroupTimeSpan());
                if (rc.getRenderMode() == RenderMode.DRAW)
                {
                    rc.getGL().getGL2().glPointSize(key.getSize());
                    rc.getGL().glDisable(GL.GL_DEPTH_TEST);
                }
                else
                {
                    // When rendering the pick color, render in the
                    // regular size so that nearby points are not
                    // occluded.
                    rc.getGL().getGL2().glPointSize(key.getSize());
                }
                TextureHandle handle = getTextureHandle(rc, key.getTextureGroup(), rc.getRenderMode());
                if (handle != null)
                {
                    rc.getGL().glBindTexture(GL.GL_TEXTURE_2D, handle.getTextureId());
                    highlightData.draw(rc, GL.GL_POINTS);
                    highlightData.dispose(rc.getGL());
                }
            }
        }
    }

    /**
     * Remove the buffered data from the cache so that the GPU memory can be
     * freed.
     */
    @Override
    protected void cleanBuffersFromCache()
    {
        synchronized (myBufferKeys)
        {
            if (!myBufferKeys.isEmpty())
            {
                CacheProvider cache = getCache();
                for (Pair<PointSpriteRendererBuffered<T>, PointSpriteRenderKey> key : myBufferKeys)
                {
                    cache.clearCacheAssociation(key, PointDataBuffered.class);
                }
                myBufferKeys.clear();
            }
        }
    }

    /**
     * Create a special data retriever that just returns the model data portion
     * of the {@link TextureModelData} object from the processor.
     *
     * @param dataRetriever The data retriever that returns a
     *            {@link TextureModelData}.
     * @return A data retriever that returns a
     *         {@link io.opensphere.core.pipeline.processor.PointProcessor.ModelCoordinates}
     *         .
     */
    private ModelDataRetriever<T> createSpecialDataRetriever(final ModelDataRetriever<T> dataRetriever)
    {
        ModelDataRetriever<T> specialDataRetriever = (geom, proj, override, timeBudget) ->
        {
            TextureModelData modelData = (TextureModelData)dataRetriever.getModelData(geom, proj, override, timeBudget);
            return modelData.getModelData();
        };
        return specialDataRetriever;
    }

    /**
     * Get the texture handle associated with the key for a render mode.
     *
     * @param rc The render context.
     * @param textureGroup The texture group.
     * @param renderMode The render mode.
     * @return The texture handle, or {@code null} if one could not be found.
     */
    private TextureHandle getTextureHandle(RenderContext rc, TextureGroup textureGroup, RenderMode renderMode)
    {
        Object texture;
        if (textureGroup == null)
        {
            texture = null;
        }
        else
        {
            texture = textureGroup.getTextureMap().get(renderMode);
            boolean pickEffect;
            if (texture == null && rc.getRenderMode() == RenderMode.PICK)
            {
                texture = textureGroup.getTextureMap().get(RenderMode.DRAW);
                pickEffect = true;
            }
            else
            {
                pickEffect = false;
            }
            getFadedRenderingHelper().initIntervalFilter(rc, pickEffect, textureGroup.getImageTexCoords());
        }
        return texture == null ? null : getCache().getCacheAssociation(texture, TextureHandle.class);
    }

    /**
     * Group points based on their textures and point sizes.
     *
     * @param input Geometries to be grouped.
     * @param rejects Geometries that could not be rendered and need
     *            reprocessing.
     * @param dataRetriever The data retriever for the texture groups.
     * @param isHighlighted Whether to use the highlighted size (false means use
     *            the size)
     * @return Grouped geometries.
     */
    private Map<PointSpriteRenderKey, List<T>> groupByTextureAndSize(Collection<? extends T> input, List<? super T> rejects,
            ModelDataRetriever<T> dataRetriever, boolean isHighlighted)
    {
        Map<PointSpriteRenderKey, List<T>> sizeSorted = New.map();

        for (T geom : input)
        {
            TextureModelData modelData = (TextureModelData)dataRetriever.getModelData(geom, null, null, TimeBudget.ZERO);
            if (modelData.getTextureGroup() != null)
            {
                SpriteModelCoordinates coords = (SpriteModelCoordinates)modelData.getModelData();
                int size = isHighlighted ? coords.getHighlightSize() : coords.getSize();
                if (size > 0)
                {
                    PointSpriteRenderKey key = new PointSpriteRenderKey(size, modelData.getTextureGroup(),
                            geom.getRenderProperties().isObscurant());
                    CollectionUtilities.multiMapAdd(sizeSorted, key, geom, false);
                }
            }
            else
            {
                rejects.add(geom);
            }
        }

        return sizeSorted;
    }

    /** A factory for creating this renderer. */
    public static class Factory extends AbstractRenderer.Factory<PointSpriteGeometry>
    {
        @Override
        public GeometryRenderer<PointSpriteGeometry> createRenderer()
        {
            return new PointSpriteRendererBuffered<>(getCache());
        }

        @Override
        public Collection<? extends BufferDisposalHelper<?>> getDisposalHelpers()
        {
            return Collections.singleton(BufferDisposalHelper.create(PointDataBuffered.class, getCache()));
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

    /**
     * Grouped sets of buffers for rendering during a single rendering pass.
     *
     * @param <T> The type of geometry.
     */
    private static class PointSpriteRenderData<T extends PointSpriteGeometry> extends TimeRenderData
    {
        /** Buffers with their associated size properties. */
        private final Map<PointSpriteRenderKey, PointDataBuffered> myBuffers;

        /**
         * Any geometries that we could not render and need reprocessing.
         */
        private final List<? extends T> myRejects;

        /**
         * Constructor.
         *
         * @param buffers Buffers with their associated size properties.
         * @param rejects Any geometries that we could not render and need
         *            reprocessing.
         * @param projection The projection used to generate this data.
         * @param groupTimeSpan The time span associated with this render data.
         */
        public PointSpriteRenderData(Map<PointSpriteRenderKey, PointDataBuffered> buffers, List<? extends T> rejects,
                Projection projection, TimeSpan groupTimeSpan)
        {
            super(projection, groupTimeSpan);
            myBuffers = buffers;
            myRejects = rejects;
        }

        /**
         * Gets the geometries that need reprocessing.
         *
         * @return The geometries needing reprocessing.
         */
        public List<? extends T> getRejects()
        {
            return myRejects;
        }

        /**
         * Get the buffers.
         *
         * @return the buffers
         */
        public Map<PointSpriteRenderKey, PointDataBuffered> getBuffers()
        {
            return myBuffers;
        }
    }

    /**
     * Encapsulation of a point size property and a texture group.
     */
    private static class PointSpriteRenderKey
    {
        /** The point size property. */
        private final int mySize;

        /** The texture group. */
        private final TextureGroup myTextureGroup;

        /**
         * When true, the geometries may obscure other geometries based on depth
         * from the viewer, otherwise depth is ignored.
         */
        private final boolean myObscurant;

        /**
         * Constructor.
         *
         * @param size The size.
         * @param textureGroup The texture group.
         * @param obscurant True when the geometries are obscurant
         */
        public PointSpriteRenderKey(int size, TextureGroup textureGroup, boolean obscurant)
        {
            Utilities.checkNull(textureGroup, "textureGroup");
            mySize = size;
            myTextureGroup = textureGroup;
            myObscurant = obscurant;
        }

        /**
         * Accessor for the size.
         *
         * @return The size.
         */
        public int getSize()
        {
            return mySize;
        }

        /**
         * Accessor for the textureGroup.
         *
         * @return The textureGroup.
         */
        public TextureGroup getTextureGroup()
        {
            return myTextureGroup;
        }

        /**
         * Get whether the geometries are obscurant.
         *
         * @return true when the geometries are obscurant
         */
        public boolean isObscurant()
        {
            return myObscurant;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + HashCodeHelper.getHashCode(mySize);
            result = prime * result + HashCodeHelper.getHashCode(myObscurant);
            result = prime * result + HashCodeHelper.getHashCode(myTextureGroup);
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null || getClass() != obj.getClass())
            {
                return false;
            }
            PointSpriteRenderKey other = (PointSpriteRenderKey)obj;
            return mySize == other.mySize && myObscurant == other.myObscurant
                    && EqualsHelper.equals(myTextureGroup, other.myTextureGroup);
        }
    }
}
