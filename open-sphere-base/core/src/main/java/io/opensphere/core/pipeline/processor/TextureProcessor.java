package io.opensphere.core.pipeline.processor;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

import org.apache.log4j.Logger;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.TextureData;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.ImageGroup;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.ImageProvidingGeometry;
import io.opensphere.core.geometry.PointSpriteGeometry;
import io.opensphere.core.geometry.RenderableGeometry;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.image.Image;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.AbstractRenderer.ModelData;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.pipeline.util.GLUtilities;
import io.opensphere.core.pipeline.util.PreloadedTextureImage;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.pipeline.util.TextureDataGroup;
import io.opensphere.core.pipeline.util.TextureGroup;
import io.opensphere.core.pipeline.util.TextureHandle;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.BatchingBlockingQueue;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ThreadedStateMachine;
import io.opensphere.core.util.concurrent.ThreadedStateMachine.StateChangeHandler;
import io.opensphere.core.util.concurrent.ThreadedStateMachine.StateController;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.lang.UnexpectedEnumException;

/**
 * Middle management for geometries with associated textures.
 *
 * @param <E> The geometry class.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class TextureProcessor<E extends ImageProvidingGeometry<E>> extends AbstractProcessor<E>
{
    /** Texture group used for blank tiles. */
    protected static final TextureGroup BLANK_TEXTURE_GROUP;

    /**
     * Dummy texture data group that indicates that the geometry has been
     * deferred.
     */
    protected static final TextureDataGroup DEFERRED_TEXTURE_DATA_GROUP;

    /** Texture coordinates representing the full quad. */
    protected static final TextureCoords FULL_QUAD = new TextureCoords(0, 1, 1, 0);

    /** How long the geometry queue waits before releasing a batch. */
    private static final long GEOMETRY_QUEUE_DELAY_MILLISECONDS = 300L;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(TextureProcessor.class);

    /**
     * Lock used to synchronize cache changes. All manipulation of
     * {@link TextureDataGroup} caches should be done within this lock.
     */
    private static final ReentrantLock ourCacheLock = new ReentrantLock();

    /** Observer to be notified when a geometry has new data available. */
    private final ImageProvidingGeometry.Observer<E> myGeometryObserver = new ImageProvidingGeometry.Observer<>()
    {
        @Override
        public void dataReady(E geom)
        {
            if (!isClosed() && geom.getImageManager().getCachedImageData() != null)
            {
                handleImageReady(geom);
            }
        }
    };

    /** Queue of geometries ready to be reprocessed. */
    private final BatchingBlockingQueue<E> myGeometryQueue;

    /** Executor for activities which are required to be on the GL thread. */
    private final Executor myGLExecutor;

    /**
     * A map of image managers to geometries. This is used so that if a geometry
     * shares the same image we load and create only one image and texture for
     * the group of geometries.
     */
    private final Map<ImageManager, List<E>> myImageManagersToGeoms = Collections.synchronizedMap(New.map());

    /** The state change handler for the geometry states. */
    private final StateChangeHandler<E> myProcessingHandler = new StateChangeHandler<>()
    {
        @Override
        public void handleStateChanged(List<? extends E> objects, ThreadedStateMachine.State newState,
                StateController<E> controller)
        {
            if (TextureState.class.isInstance(newState))
            {
                switch (TextureState.class.cast(newState))
                {
                    case IMAGE_LOADED:
                        processImageLoaded(objects, controller);
                        break;
                    case AWAITING_IMAGE:
                        processAwaitingImage(objects, controller);
                        break;
                    case TEXTURE_DATA_LOADED:
                        processTextureDataLoaded(objects, controller);
                        break;
                    case TEXTURE_LOADED:
                        ThreadUtilities.runCpu(() -> processTextureLoaded(objects, controller));
                        break;
                    default:
                        throw new UnexpectedEnumException(TextureState.class.cast(newState));
                }
            }
        }
    };

    /** The texture capabilities of the GL engine. */
    private final TextureCapabilities myTextureCapabilities = new TextureCapabilities();

    static
    {
        BLANK_TEXTURE_GROUP = new TextureGroup(Collections.<AbstractGeometry.RenderMode, Object>emptyMap(), FULL_QUAD);
        DEFERRED_TEXTURE_DATA_GROUP = new TextureDataGroup();
    }

    /**
     * If the geometry has a pre-loaded texture, return it.
     *
     * @param geom The geometry.
     * @return The pre-loaded texture, or {@code null}.
     */
    protected static TextureGroup getPreloadedTexture(ImageProvidingGeometry<?> geom)
    {
        TextureGroup texture;
        ImageGroup imageData = geom.getImageManager() == null ? null : geom.getImageManager().getCachedImageData();
        if (imageData == null)
        {
            texture = null;
        }
        else
        {
            Map<AbstractGeometry.RenderMode, ? extends Image> imageMap = imageData.getImageMap();
            if (imageMap.get(AbstractGeometry.RenderMode.DRAW) instanceof PreloadedTextureImage)
            {
                @SuppressWarnings("unchecked")
                Map<AbstractGeometry.RenderMode, PreloadedTextureImage> preloadedImageMap = (Map<AbstractGeometry.RenderMode, PreloadedTextureImage>)imageMap;
                texture = new TextureGroup(preloadedImageMap);
            }
            else
            {
                texture = null;
            }
        }
        return texture;
    }

    /**
     * Construct the processor.
     *
     * @param geometryType The concrete type of geometry handled by this
     *            processor.
     * @param builder The builder for the processor.
     * @param renderer The renderer for the geometries handled by this
     *            processor.
     * @param executorObjectCountThreshold The number of geometries in a state
     *            transition that cause the high volume executor to be engaged.
     */
    public TextureProcessor(Class<?> geometryType, ProcessorBuilder builder, GeometryRenderer<E> renderer,
            int executorObjectCountThreshold)
    {
        super(geometryType, builder, renderer, executorObjectCountThreshold);

        Executor glExecutor = builder.getGLExecutor();
        myGLExecutor = glExecutor;
        if (glExecutor == null)
        {
            throw new IllegalArgumentException("glExecutor is null");
        }
        getStateMachine().registerStateChangeHandler(EnumSet.of(TextureState.IMAGE_LOADED), myProcessingHandler,
                builder.getExecutorService(), null, 0);
        getStateMachine().registerStateChangeHandler(EnumSet.of(TextureState.TEXTURE_DATA_LOADED), myProcessingHandler,
                glExecutor, null, 0);
        getStateMachine().registerStateChangeHandler(
                EnumSet.complementOf(EnumSet.of(TextureState.TEXTURE_DATA_LOADED, TextureState.IMAGE_LOADED)),
                myProcessingHandler, builder.getExecutorService(), builder.getLoadSensitiveExecutor(), 10);

        myGeometryQueue = new BatchingBlockingQueue<>(getNonGLScheduledExecutor(), GEOMETRY_QUEUE_DELAY_MILLISECONDS);

        // Observer that gets called whenever a geometry has new image data.
        myGeometryQueue.addObserver(new BatchingBlockingQueue.Observer()
        {
            @Override
            public void objectsAdded()
            {
                List<E> objs;
                if (myGeometryQueue.size() == 1)
                {
                    E obj = myGeometryQueue.poll();
                    objs = obj == null ? Collections.<E>emptyList() : Collections.singletonList(obj);
                }
                else
                {
                    objs = New.list();
                    myGeometryQueue.drainTo(objs);
                }

                if (!isClosed() && !objs.isEmpty())
                {
                    processImageUpdates(objs);
                }
            }
        });

        myGLExecutor.execute(() -> myTextureCapabilities.setCompressedTexturesSupported(
                RenderContext.getCurrent().isExtensionAvailable("GL_EXT_texture_compression_s3tc")));
    }

    @Override
    public boolean allGeometriesReady()
    {
        Collection<? extends E> allGeoms = getStateMachine().getAllObjects();
        Collection<E> deferred = getStateMachine().getObjectsInState(State.DEFERRED);
        Collection<E> ready = getReadyGeometries();

        for (E geom : allGeoms)
        {
            // TODO also check to see if the image is missing
            if (geom instanceof RenderableGeometry && ((RenderableGeometry)geom).getRenderProperties().isHidden()
                    || deferred.contains(geom) || ready.contains(geom))
            {
                continue;
            }

            if (isImageBlank(geom))
            {
                continue;
            }

            if (isOnScreen(geom, true))
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public AbstractRenderer.ModelData getModelData(E geom, Projection projectionSnapshot, AbstractRenderer.ModelData override,
            TimeBudget timeBudget)
    {
        Lock readLock = getProjectionChangeLock().readLock();
        readLock.lock();
        try
        {
            TextureModelData cachedData = getCachedData(geom, override);
            TextureModelData data = cachedData;

            // If any data is missing, try to generate it.
            if (data == null || data.getModelData() == null || data.getTextureGroup() == null)
            {
                data = processGeometry(geom, projectionSnapshot, data, timeBudget);
                if (data != null && data.getModelData() != null && cachedData.getModelData() == null)
                {
                    cacheData(geom, data);
                    setOnscreenDirty();
                }
            }

            return data;
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public boolean isViable(RenderContext rc, Collection<String> warnings)
    {
        return super.isViable(rc, warnings) && rc.is11Available("Texture processing is not available", warnings);
    }

    /**
     * Determine if any of the textures in the texture group are missing texture
     * handles.
     *
     * @param textureGroup The texture group.
     * @return <code>true</code> if handles are missing.
     */
    protected boolean checkForMissingTextureHandles(TextureGroup textureGroup)
    {
        boolean missingTexture = false;
        for (Object handleKey : textureGroup.getTextureMap().values())
        {
            TextureHandle handle = getCache().getCacheAssociation(handleKey, TextureHandle.class);
            if (handle == null)
            {
                missingTexture = true;
                break;
            }
        }
        return missingTexture;
    }

    /**
     * Create the texture from the texture data. This also caches the newly
     * created texture. This must be called on the GL thread.
     *
     * @param geom The geometry for which to create the new texture.
     * @param textureData The texture data to be used.
     * @param textureGroup Optional textureGroup to use when creating the new
     *            texture group.
     * @return The newly created texture.
     */
    protected TextureGroup createTexture(E geom, TextureDataGroup textureData, TextureGroup textureGroup)
    {
        EnumMap<AbstractGeometry.RenderMode, Object> map = textureGroup == null ? new EnumMap<>(AbstractGeometry.RenderMode.class)
                : null;
        TextureCoords texCoords = null;
        GL gl = GLContext.getCurrentGL();
        for (Map.Entry<AbstractGeometry.RenderMode, TextureData> entry : textureData.getTextureDataMap().entrySet())
        {
            TextureData data = entry.getValue();
            /* Don't use TextureIO, since it will decide which texture target to
             * use and that target may not be the desired GL_TEXTURE_2D
             * target. */

            // com.jogamp.opengl.util.texture.Texture joglTex =
            // TextureIO.newTexture(data);

            Texture joglTex = new Texture(GL.GL_TEXTURE_2D);
            joglTex.updateImage(gl, data, GL.GL_TEXTURE_2D);
            texCoords = joglTex.getImageTexCoords();
            Object textureHandleKey;
            if (textureGroup == null)
            {
                textureHandleKey = new Object();
                if (map != null)
                {
                    map.put(entry.getKey(), textureHandleKey);
                }
            }
            else
            {
                textureHandleKey = textureGroup.getTextureMap().get(entry.getKey());
            }
            TextureHandle handle = new TextureHandle(joglTex, gl);
            getCache().putCacheAssociation(textureHandleKey, handle, TextureHandle.class, 0L, handle.getSizeGPU());
        }

        setOnscreenDirty();

        if (textureGroup == null && texCoords != null)
        {
            TextureGroup tg = new TextureGroup(map, texCoords);
            getCache().putCacheAssociation(geom.getImageManager(), tg, TextureGroup.class, tg.getSizeBytes(), 0L);
            return tg;
        }
        return textureGroup;
    }

    /**
     * Determine the target state for a geometry based on what cached data are
     * available.
     *
     * @param geom The geometry.
     * @return The target state.
     */
    protected Enum<? extends ThreadedStateMachine.State> determineState(E geom)
    {
        CacheProvider cache = getCache();
        Enum<? extends ThreadedStateMachine.State> state;

        // Check for a model data object associated with the geometry.
        TextureModelData cachedData = getCachedData(geom, null);
        AbstractRenderer.ModelData modelData = cachedData.getModelData();
        TextureGroup textureGroup = cachedData.getTextureGroup();
        if (textureGroup == null || checkForMissingTextureHandles(textureGroup))
        {
            if (geom.getImageManager() == null)
            {
                // This is a pick-only geometry; skip straight to
                // TEXTURE_LOADED.
                state = TextureState.TEXTURE_LOADED;
            }
            else
            {
                // check for texture data.
                TextureDataGroup textureData = cache.getCacheAssociation(geom.getImageManager(), TextureDataGroup.class);
                if (textureData == null || textureData.getTextureDataMap().isEmpty())
                {
                    // No texture data; check to see what state the image
                    // processing has reached.
                    ImageGroup imageData = geom.getImageManager().getCachedImageData();
                    if (imageData == null)
                    {
                        // No image data; wait for image.
                        state = TextureState.AWAITING_IMAGE;
                    }
                    else
                    {
                        // Got image data, but no texture data; need to load
                        // texture data.
                        state = TextureState.IMAGE_LOADED;

                        // Make sure the geometry doesn't have the
                        // DEFERRED_TEXTURE_DATA_GROUP.
                        ourCacheLock.lock();
                        try
                        {
                            if (Utilities.sameInstance(DEFERRED_TEXTURE_DATA_GROUP,
                                    cache.getCacheAssociation(geom.getImageManager(), TextureDataGroup.class)))
                            {
                                cache.clearCacheAssociation(geom.getImageManager(), TextureDataGroup.class);
                            }
                        }
                        finally
                        {
                            ourCacheLock.unlock();
                        }
                    }
                }
                else
                {
                    // Texture data is loaded; need to create the
                    // texture.
                    state = TextureState.TEXTURE_DATA_LOADED;
                }
            }
        }
        else if (Utilities.sameInstance(textureGroup, BLANK_TEXTURE_GROUP))
        {
            // Send blank tiles to IMAGE_LOADED, where they will stay.
            state = TextureState.IMAGE_LOADED;
        }
        else if (modelData == null)
        {
            state = TextureState.TEXTURE_LOADED;
        }
        else
        {
            state = State.READY;
        }

        return state;
    }

    /**
     * Determine the state of the input geometries and place them in the
     * appropriate return collection.
     *
     * @param input The input geometries.
     * @param imageLoaded Return collection of geometries that have image data.
     * @param awaitingImage Return collection of geometries that are still
     *            waiting for image data.
     * @param textureDataLoaded Return collection of geometries that have
     *            texture data.
     * @param textureLoaded Return collection of geometries that have textures.
     * @param ready Return collection of geometries that have all necessary
     *            data.
     */
    protected void determineStates(Collection<? extends E> input, Collection<? super E> imageLoaded,
            Collection<? super E> awaitingImage, Collection<? super E> textureDataLoaded, Collection<? super E> textureLoaded,
            Collection<? super E> ready)
    {
        for (final E geom : input)
        {
            Enum<? extends ThreadedStateMachine.State> state = determineState(geom);

            if (state instanceof TextureState)
            {
                switch ((TextureState)state)
                {
                    case IMAGE_LOADED:
                        imageLoaded.add(geom);
                        break;
                    case AWAITING_IMAGE:
                        awaitingImage.add(geom);
                        break;
                    case TEXTURE_LOADED:
                        textureLoaded.add(geom);
                        break;
                    case TEXTURE_DATA_LOADED:
                        textureDataLoaded.add(geom);
                        break;
                    default:
                        throw new UnexpectedEnumException(state);
                }
            }
            else if (state == State.READY)
            {
                ready.add(geom);
            }
            else
            {
                throw new UnexpectedEnumException(state);
            }
        }
    }

    /**
     * Indicates if the loaded textures should also have a mipmap automatically
     * generated for it.
     *
     * @return True if a mipmap should be autogenerated for every texture, false
     *         if not.
     */
    protected boolean enableAutoMipMap()
    {
        return false;
    }

    @Override
    protected TextureModelData getCachedData(E geom, AbstractRenderer.ModelData override)
    {
        TextureGroup cachedTextureGroup = getCachedTextureGroup(geom, override);
        ModelData cachedModelData;
        if (cachedTextureGroup == null)
        {
            if (geom.getImageManager() == null)
            {
                cachedModelData = getCachedModelData(geom, FULL_QUAD, override);
            }
            else
            {
                cachedModelData = null;
            }
        }
        else
        {
            TextureCoords imageTexCoords = cachedTextureGroup.getImageTexCoords();
            cachedModelData = getCachedModelData(geom, imageTexCoords, override);
        }
        return new TextureModelData(cachedModelData, cachedTextureGroup);
    }

    /**
     * Get the cached model data, if any, for a geometry. An override may be
     * provided that may be used by the processor to compose the return model
     * data, to avoid multiple cache hits if the model data has multiple parts
     * and some of the parts are already known.
     *
     * @param geom The geometry.
     * @param imageTexCoords The image texture coordinates if they're available.
     * @param override The override data.
     * @return The model data.
     */
    protected abstract AbstractRenderer.ModelData getCachedModelData(E geom, TextureCoords imageTexCoords,
            AbstractRenderer.ModelData override);

    /**
     * Get the cached texture group, if any, for a geometry. An override may be
     * provided that may be used by the processor to compose the return model
     * data, to avoid multiple cache hits if the model data has multiple parts
     * and some of the parts are already known.
     *
     * @param geom The geometry.
     * @param override The override data.
     * @return The texture group.
     */
    protected TextureGroup getCachedTextureGroup(E geom, AbstractRenderer.ModelData override)
    {
        TextureGroup texture = override == null ? null : ((TextureModelData)override).getTextureGroup();
        if (texture == null && geom.getImageManager() != null)
        {
            texture = getCache().getCacheAssociation(geom.getImageManager(), TextureGroup.class);
        }
        return texture;
    }

    /**
     * Gets the map of image managers to geometries.
     *
     * @return the map of image managers to geometries
     */
    public Map<ImageManager, List<E>> getImageManagersToGeoms()
    {
        return myImageManagersToGeoms;
    }

    /**
     * Get the comparator to be used to determine processing priority.
     *
     * @return The comparator.
     */
    protected abstract Comparator<? super E> getPriorityComparator();

    /**
     * Perform any necessary handling when it is determined that an image is
     * blank.
     *
     * @param geom The geometry whose image is blank.
     */
    protected abstract void handleBlankImage(E geom);

    /**
     * Handle replacing an image for a geometry.
     *
     * @param geom The geometry.
     */
    protected void handleImageReady(E geom)
    {
        if (geom.isRapidUpdate())
        {
            processImageUpdates(Collections.singletonList(geom));
        }
        else
        {
            myGeometryQueue.offer(geom);
        }
    }

    /**
     * Get if the most recent image for a geometry was blank.
     *
     * @param geom The geometry.
     * @return {@code true} if the image was blank.
     */
    protected boolean isImageBlank(ImageProvidingGeometry<?> geom)
    {
        return Utilities.sameInstance(BLANK_TEXTURE_GROUP,
                getCache().getCacheAssociation(geom.getImageManager(), TextureGroup.class));
    }

    /**
     * Create the texture data for a geometry and cache it.
     *
     * @param geom The geometry.
     * @param imageData The image data.
     * @return The texture data group or {@code null} if not successful.
     */
    protected TextureDataGroup loadTextureData(E geom, ImageGroup imageData)
    {
        // Get a write lock on the cache to ensure that only one textureDataMap
        // is created per source image.
        TextureDataGroup textureData;
        ourCacheLock.lock();
        try
        {
            textureData = getCache().getCacheAssociation(geom.getImageManager(), TextureDataGroup.class);
            if (textureData == null)
            {
                textureData = new TextureDataGroup();
                getCache().putCacheAssociation(geom.getImageManager(), textureData, TextureDataGroup.class,
                        textureData.getEstimatedMemorySize(), 0L);
            }
            else if (Utilities.sameInstance(DEFERRED_TEXTURE_DATA_GROUP, textureData))
            {
                // Processing is cancelled, so skip populating the texture data
                // group.
                return DEFERRED_TEXTURE_DATA_GROUP;
            }
        }
        finally
        {
            ourCacheLock.unlock();
        }

        boolean compressedTexturesSupported = myTextureCapabilities.isCompressedTexturesSupported();

        // Do this outside the lock to allow for parallel texture data creation.
        boolean success;
        long newSize;
        synchronized (textureData)
        {
            success = textureData.populateTextureDataGroup(geom, imageData, compressedTexturesSupported, enableAutoMipMap());
            newSize = textureData.getEstimatedMemorySize();
        }

        if (success)
        {
            // Ensure that the geometry is not removed as the TextureDataGroup
            // is cached.
            synchronized (getGeometrySet())
            {
                ourCacheLock.lock();
                try
                {
                    TextureDataGroup cached = getCache().getCacheAssociation(geom.getImageManager(), TextureDataGroup.class);
                    if (Utilities.sameInstance(cached, textureData) && hasGeometry(geom))
                    {
                        // Re-cache the texture data with the correct size.
                        getCache().putCacheAssociation(geom.getImageManager(), textureData, TextureDataGroup.class, newSize, 0L);
                    }
                    else
                    {
                        textureData.flush();
                    }
                    return cached;
                }
                finally
                {
                    ourCacheLock.unlock();
                }
            }
        }
        textureData.flush();

        // If the texture data could not be loaded, clear it from the cache.
        getCache().clearCacheAssociation(geom.getImageManager(), TextureDataGroup.class);
        return null;
    }

    /**
     * Check the cache to see if there's a texture data for a geometry. If there
     * is, clear it from the cache and return it.
     * <p>
     * The texture data is cleared from the cache once it is retrieved since its
     * data will be transfered to a texture by the caller of this method. It is
     * cleared here rather than after the texture is loaded so that it can be
     * cleared within the same cache lock that it is retrieved, to be sure that
     * the correct texture data is cleared.
     *
     * @param geom The geometry for which to get the texture data.
     * @return The texture data if it was in the cache, or {@code null}.
     */
    protected final TextureDataGroup pollTextureDataGroupCache(E geom)
    {
        ourCacheLock.lock();
        try
        {
            TextureDataGroup cachedTextureData = getCache().getCacheAssociation(geom.getImageManager(), TextureDataGroup.class);
            if (cachedTextureData != null)
            {
                synchronized (cachedTextureData)
                {
                    if (!cachedTextureData.getTextureDataMap().isEmpty())
                    {
                        // Set flushing disabled so that the cache listener does
                        // not flush the texture group when it is removed from
                        // the cache.
                        cachedTextureData.setFlushingDisabled(true);
                        getCache().clearCacheAssociation(geom.getImageManager(), TextureDataGroup.class);
                        cachedTextureData.setFlushingDisabled(false);
                        return cachedTextureData;
                    }
                }
            }
            return null;
        }
        finally
        {
            ourCacheLock.unlock();
        }
    }

    /**
     * Callback from the state machine for geometries in the
     * {@link TextureState#AWAITING_IMAGE} state.
     *
     * @param objects The objects.
     * @param controller A controller used to initiate further state changes for
     *            the geometries.
     */
    protected void processAwaitingImage(Collection<? extends E> objects, StateController<E> controller)
    {
        List<E> sharesImage = New.list();
        List<E> uniqueImage = New.list();

        for (E geom : objects)
        {
            if (geom.sharesImage())
            {
                sharesImage.add(geom);
            }
            else
            {
                uniqueImage.add(geom);
            }
        }

        if (!sharesImage.isEmpty())
        {
            processAwaitingImageSharesImage(sharesImage);
        }

        if (!uniqueImage.isEmpty())
        {
            processAwaitingImageUniqueImage(uniqueImage);
        }
    }

    @Override
    protected void processDeferred(Collection<? extends E> deferred, StateController<E> controller)
    {
        super.processDeferred(deferred, controller);

        // Clear any cached images since we may never process them.
        for (E geom : deferred)
        {
            removeObserverFromGeometry(geom);

            ourCacheLock.lock();
            try
            {
                TextureDataGroup textureData = pollTextureDataGroupCache(geom);
                if (textureData != null)
                {
                    textureData.flush();
                }

                // Put in a marker texture data group to prevent another thread
                // from putting a texture data group in the cache that will not
                // be processed.
                getCache().putCacheAssociation(geom.getImageManager(), DEFERRED_TEXTURE_DATA_GROUP, TextureDataGroup.class, 0L,
                        0L);
            }
            finally
            {
                ourCacheLock.unlock();
            }
        }

        notifyReadyObservers();
    }

    @Override
    protected void processGeometries(Collection<? extends E> unprocessed, Collection<? super E> ready,
            StateController<E> controller)
    {
        Collection<E> imageLoaded = New.collection(0);
        Collection<E> awaitingImage = New.collection(0);
        Collection<E> textureDataLoaded = New.collection(0);
        Collection<E> textureLoaded = New.collection(0);

        determineStates(unprocessed, imageLoaded, awaitingImage, textureDataLoaded, textureLoaded, ready);

        if (!imageLoaded.isEmpty())
        {
            controller.changeState(imageLoaded, TextureState.IMAGE_LOADED);
        }
        if (!awaitingImage.isEmpty())
        {
            controller.changeState(awaitingImage, TextureState.AWAITING_IMAGE);
        }
        if (!textureLoaded.isEmpty())
        {
            controller.changeState(textureLoaded, TextureState.TEXTURE_LOADED);

            boolean rapid = false;
            for (E geom : textureLoaded)
            {
                if (geom.getImageManager() != null && !geom.getImageManager().getDirtyRegions().isEmpty())
                {
                    rapid |= geom.isRapidUpdate();
                    myGeometryQueue.offer(geom);
                }
            }
            if (rapid)
            {
                myGeometryQueue.notifyObservers();
            }
        }
        if (!textureDataLoaded.isEmpty())
        {
            controller.changeState(textureDataLoaded, TextureState.TEXTURE_DATA_LOADED);
        }
    }

    @Override
    protected abstract TextureModelData processGeometry(E geo, Projection projectionSnapshot, AbstractRenderer.ModelData override,
            TimeBudget timeBudget);

    /**
     * Callback from the state machine for geometries in the
     * {@link TextureState#IMAGE_LOADED} state. Create texture data for
     * geometries.
     *
     * @param objects The objects.
     * @param controller A controller used to initiate further state changes for
     *            the geometries.
     */
    protected void processImageLoaded(Collection<? extends E> objects, StateController<E> controller)
    {
        Collection<E> textureDataLoaded = New.collection(objects.size());
        Collection<E> textureLoaded = New.collection(0);
        Collection<E> reload = null;
        Collection<E> retFailed = null;

        for (E geom : objects)
        {
            handleBlankImage(geom);
            ImageGroup imageData = geom.getImageManager().pollCachedImageData();

            if (imageData == null)
            {
                reload = handleMissingImageData(geom, textureLoaded, reload);
            }
            else
            {
                // Ignore failed here because there may be another thread trying
                // to process the image. If failed is instance of
                // PointSpriteGeometry, reprocess.

                retFailed = handleImageLoaded(textureDataLoaded, textureLoaded, (Collection<E>)null, geom, imageData);
                if (retFailed != null)
                {
                    retFailed.stream().filter(geometry -> geometry instanceof PointSpriteGeometry)
                            .forEach(geometry -> handleImageLoaded(textureDataLoaded, textureLoaded, (Collection<E>)null,
                                    geometry, imageData));
                }
            }
        }

        // If no geometries progress, do a determineOnScreen to make sure that
        // magnified tiles are removed for the blank geometries.
        boolean determineOnScreen = true;

        if (reload != null)
        {
            controller.changeState(reload, State.UNPROCESSED);
        }
        if (!textureDataLoaded.isEmpty())
        {
            controller.changeState(textureDataLoaded, TextureState.TEXTURE_DATA_LOADED);
            determineOnScreen = false;
        }
        if (!textureLoaded.isEmpty())
        {
            controller.changeState(textureLoaded, TextureState.TEXTURE_LOADED);
            determineOnScreen = false;
        }
        if (determineOnScreen)
        {
            determineOnscreen();
        }
        notifyReadyObservers();
    }

    /**
     * Process geometries for image updates. This determines if the geometries
     * have textures that can be reused or if new textures must be generated.
     *
     * @param geoms The geometries.
     */
    protected void processImageUpdates(List<? extends E> geoms)
    {
        List<E> resets = null;
        List<E> replaces = null;
        List<E> loaded = null;
        List<TextureHandle> handles = null;
        for (int index = 0; index < geoms.size();)
        {
            E geom = geoms.get(index++);

            ImageGroup imageData = geom.getImageManager().getCachedImageData();
            if (imageData != null)
            {
                TextureHandle handle = getCachedTextureHandle(geom, AbstractGeometry.RenderMode.DRAW);

                if (isTextureUsable(imageData, handle, AbstractGeometry.RenderMode.DRAW))
                {
                    if (geom.sharesImage())
                    {
                        loaded = CollectionUtilities.lazyAdd(geom, loaded);
                    }
                    else
                    {
                        replaces = CollectionUtilities.lazyAdd(geom, replaces);
                        handles = CollectionUtilities.lazyAdd(handle, handles);
                    }
                }
                else
                {
                    resets = CollectionUtilities.lazyAdd(geom, resets, New.<E>linkedListFactory());
                }
            }
        }

        if (resets != null)
        {
            resetDueToImageUpdate(resets, TextureState.IMAGE_LOADED);
        }

        if (loaded != null)
        {
            renderOtherGeometries(loaded, true);
        }

        if (replaces != null)
        {
            myGLExecutor.execute(new TextureReplacer(replaces, handles));
        }
    }

    @Override
    protected void processReady(Collection<? extends E> ready, StateController<E> controller)
    {
        super.processReady(ready, controller);

        // Once the geometries are ready, add the observer to them to receive
        // any updates to their images.
        for (E geom : ready)
        {
            addObserverToGeometry(geom);
        }
    }

    @Override
    protected void processRemoves(Collection<? extends Geometry> removes)
    {
        if (removes != null && !removes.isEmpty())
        {
            super.processRemoves(removes);

            Set<ImageManager> imageManagers = New.set(removes.size());
            for (Geometry geom : removes)
            {
                removeObserverFromGeometry(geom);
                if (geom instanceof ImageProvidingGeometry && ((ImageProvidingGeometry<?>)geom).getImageManager() != null)
                {
                    imageManagers.add(((ImageProvidingGeometry<?>)geom).getImageManager());
                }
            }

            if (!imageManagers.isEmpty())
            {
                ourCacheLock.lock();
                try
                {
                    getCache().clearCacheAssociations(imageManagers, TextureDataGroup.class);
                }
                finally
                {
                    ourCacheLock.unlock();
                }
            }
        }
    }

    /**
     * Callback from the state machine for geometries in the
     * {@link TextureState#TEXTURE_DATA_LOADED} state. Retrieve the
     * {@link TextureData} objects and create {@link TextureGroup} objects from
     * them. This must happen on the thread with the active GL context.
     *
     * @param textureDataLoaded The objects.
     * @param controller A controller used to initiate further state changes for
     *            the geometries.
     */
    protected void processTextureDataLoaded(Collection<? extends E> textureDataLoaded, StateController<E> controller)
    {
        if (textureDataLoaded.isEmpty())
        {
            return;
        }
        Collection<E> textureLoaded = New.collection(textureDataLoaded.size());
        Collection<E> reloads = null;
        CacheProvider cache = getCache();
        for (E geom : textureDataLoaded)
        {
            TextureGroup textureGroup = cache.getCacheAssociation(geom.getImageManager(), TextureGroup.class);
            if (!((BaseRenderProperties)geom.getRenderProperties()).isDrawable()
                    || textureGroup != null && !checkForMissingTextureHandles(textureGroup))
            {
                // This is a pick-only geometry or there's already a texture
                // cached for it.
                textureLoaded.add(geom);
            }
            else
            {
                TextureDataGroup textureData = pollTextureDataGroupCache(geom);
                try
                {
                    if (textureData == null || createTexture(geom, textureData, (TextureGroup)null) == null)
                    {
                        // No texture data and no texture; need to reload.
                        reloads = CollectionUtilities.lazyAdd(geom, reloads);
                    }
                    else
                    {
                        textureLoaded.add(geom);
                    }
                }
                finally
                {
                    if (textureData != null)
                    {
                        textureData.flush();
                    }
                }
            }
        }
        if (!textureLoaded.isEmpty())
        {
            renderOtherGeometries(textureLoaded, false);
            controller.changeState(textureLoaded, TextureState.TEXTURE_LOADED);
        }
        if (reloads != null)
        {
            reloadGeometries(reloads);
        }
    }

    /**
     * When geometries share the same image manager we group them so that one
     * image and texture is retrieved and created. So one geometry goes through
     * the appropriate states and the other geometries don't. We need to send
     * the other geometries to the TEXTURE_LOADED state so they are renedered as
     * well.
     *
     * @param textureLoaded The geometries who got their textures loaded.
     * @param renderTextureLoaded True if you want the geometries within
     *            textureLoaded to have their states set to TEXTURE_LOADED.
     */
    private void renderOtherGeometries(Collection<E> textureLoaded, boolean renderTextureLoaded)
    {
        List<E> toAdd = New.list();
        for (E geom : textureLoaded)
        {
            List<E> geoms = myImageManagersToGeoms.remove(geom.getImageManager());
            if (geoms != null)
            {
                if (!renderTextureLoaded)
                {
                    geoms.remove(geom);
                }
                if (!geoms.isEmpty())
                {
                    toAdd.addAll(geoms);
                }
            }
        }

        if (!toAdd.isEmpty())
        {
            getStateMachine().removeFromState(toAdd);
            resetState(toAdd, TextureState.TEXTURE_LOADED);
        }
    }

    /**
     * Get a texture group for a geometry, generating it on-the-fly if necessary
     * and possible.
     *
     * @param geom The geometry.
     * @param override Optional override that may be used to supply part of the
     *            model data.
     * @param timeBudget The time budget for getting the model data. This may be
     *            ignored, at the discretion of the implementation.
     * @return The texture group, or {@code null} if one could not be
     *         found/generated.
     */
    protected TextureGroup processTextureForGeometry(E geom, AbstractRenderer.ModelData override, TimeBudget timeBudget)
    {
        TextureGroup textureGroup = override == null ? null : ((TextureModelData)override).getTextureGroup();
        boolean preloaded = false;
        if (textureGroup == null)
        {
            // Check if this is a pre-loaded texture.
            textureGroup = getPreloadedTexture(geom);
            if (textureGroup != null)
            {
                preloaded = true;
            }
        }

        boolean missingHandles = textureGroup == null || checkForMissingTextureHandles(textureGroup);
        // If no pre-loaded texture and this is the GL thread, generate the
        // texture.
        if (geom.getImageManager() != null && missingHandles && !preloaded && GLUtilities.isGLThread())
        {
            // Check to see if there is texture data.
            TextureDataGroup textureData = pollTextureDataGroupCache(geom);
            if (textureData == null)
            {
                ImageGroup imageData = geom.getImageManager().pollCachedImageData();
                if (imageData != null)
                {
                    textureData = new TextureDataGroup();
                    try
                    {
                        if (textureData.populateTextureDataGroup(geom, imageData,
                                myTextureCapabilities.isCompressedTexturesSupported(), enableAutoMipMap()))
                        {
                            textureGroup = createTexture(geom, textureData, textureGroup);
                        }
                    }
                    finally
                    {
                        textureData.flush();
                    }

                    // Reset the state to ensure that another thread doesn't
                    // strand the geometry in AWAITING_IMAGE.
                    resetState(Collections.singletonList(geom), State.UNPROCESSED);
                }
            }
            else
            {
                try
                {
                    textureGroup = createTexture(geom, textureData, textureGroup);
                }
                finally
                {
                    textureData.flush();
                }
            }
        }
        return textureGroup;
    }

    /**
     * Callback from the state machine for geometries in the
     * {@link TextureState#TEXTURE_LOADED} state.
     *
     * @param textureLoaded The objects.
     * @param controller A controller used to initiate further state changes for
     *            the geometries.
     */
    protected abstract void processTextureLoaded(Collection<? extends E> textureLoaded, StateController<E> controller);

    /**
     * Remove the observer from a geometry.
     *
     * @param geom The geometry.
     */
    protected final void removeObserverFromGeometry(Geometry geom)
    {
        if (geom instanceof ImageProvidingGeometry)
        {
            @SuppressWarnings("unchecked")
            ImageProvidingGeometry<E> cast = (ImageProvidingGeometry<E>)geom;
            if (cast.getImageManager() != null)
            {
                cast.removeObserver(myGeometryObserver);
            }
        }
    }

    /**
     * Reset geometries because their images have been changed. This should only
     * happen if the geometries' textures cannot be reused.
     *
     * @param resets The geometries to reset. This should be a linked list for
     *            best performance.
     * @param state The state to reset the geometries to.
     */
    protected void resetDueToImageUpdate(List<? extends E> resets, ThreadedStateMachine.State state)
    {
        // Make sure the geometries aren't removed from the
        // processor while this is happening.
        Collection<E> processorGeometries = getGeometrySet();
        synchronized (processorGeometries)
        {
            Collection<ImageManager> imageManagers = New.collection(resets.size());
            for (Iterator<? extends E> iter = resets.iterator(); iter.hasNext();)
            {
                E geom = iter.next();
                if (!hasGeometry(geom))
                {
                    iter.remove();
                }
                else
                {
                    imageManagers.add(geom.getImageManager());
                }
            }
            if (!resets.isEmpty())
            {
                // Clear the cache to force a reload.
                // Lock the cache lock to ensure that another thread does not
                // create a new texture from the old texture data and cache it
                // after this thread clears the textures.
                ourCacheLock.lock();
                try
                {
                    getCache().clearCacheAssociations(imageManagers, TextureGroup.class, TextureDataGroup.class);
                }
                finally
                {
                    ourCacheLock.unlock();
                }

                resetState(resets, state);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Overridden to only reset to TEXTURE_LOADED and only for on-screen tiles.
     */
    @Override
    protected void resetStateDueToProjectionChange()
    {
        resetState(getReadyGeometries(), TextureState.TEXTURE_LOADED);
    }

    /**
     * Add an observer to be notified when the geometry has a new image
     * available.
     *
     * @param geom The geometry.
     */
    private void addObserverToGeometry(final E geom)
    {
        if (geom.getImageManager() != null)
        {
            // Make sure we don't add an observer to the geometry as the
            // geometry is being removed from the processor. Synchronize around
            // the addObserver() call so that receiveObjects() can't complete in
            // between the hasGeometry() call and the addObserver() call.
            synchronized (getGeometrySet())
            {
                if (hasGeometry(geom))
                {
                    geom.addObserver(myGeometryObserver);
                }
            }

            // Clear the dirty regions to force a full image update. The
            // image may have changed prior to the observer being added to the
            // geometry, so the dirty regions may be for an image that hasn't
            // been loaded to the texture yet.
            geom.getImageManager().clearDirtyRegions();

            // Go ahead and call the observer just in case data arrived right
            // before adding the observer to the geometry. There will be no
            // effect if the data isn't there.
            myGeometryObserver.dataReady(geom);
        }
    }

    /**
     * Get the cached texture handle for a geometry.
     *
     * @param geom The geometry.
     * @param renderMode The render mode.
     * @return The cached handle, or {@code null}.
     */
    private TextureHandle getCachedTextureHandle(E geom, AbstractGeometry.RenderMode renderMode)
    {
        TextureGroup textureGroup = getCachedTextureGroup(geom, (ModelData)null);
        Object textureHandleKey = textureGroup == null ? null : textureGroup.getTextureMap().get(renderMode);
        return textureHandleKey == null ? null : getCache().getCacheAssociation(textureHandleKey, TextureHandle.class);
    }

    /**
     * Handle when an image as reached the {@link TextureState#IMAGE_LOADED}
     * state for a single geometry.
     *
     * @param textureDataLoaded The geometry may be added to this collection of
     *            the texture data is loaded.
     * @param textureLoaded The geometry may be added to this collection of the
     *            texture is loaded.
     * @param failed The geometry may be added to this collection of the texture
     *            data cannot be loaded.
     * @param geom The geometry whose image is loaded.
     * @param imageData The loaded image data.
     * @return the failed geometries. This may be null if the given failed
     *         collection is null.
     */
    private Collection<E> handleImageLoaded(Collection<E> textureDataLoaded, Collection<E> textureLoaded, Collection<E> failed,
            E geom, ImageGroup imageData)
    {
        Collection<E> retFailed = null;
        Map<AbstractGeometry.RenderMode, ? extends Image> imageMap = imageData.getImageMap();
        if (imageMap.get(AbstractGeometry.RenderMode.DRAW) instanceof PreloadedTextureImage)
        {
            // If the image data is pre-loaded we should set the state
            // to TEXTURE_LOADED.
            // @formatter:off
            @SuppressWarnings("unchecked")
            Map<AbstractGeometry.RenderMode, PreloadedTextureImage> preloadedImageMap =
                (Map<AbstractGeometry.RenderMode, PreloadedTextureImage>)imageMap;
            // @formatter:on
            TextureGroup texture = new TextureGroup(preloadedImageMap);
            getCache().putCacheAssociation(geom.getImageManager(), texture, TextureGroup.class, texture.getSizeBytes(), 0L);
            textureLoaded.add(geom);
        }
        else
        {
            TextureDataGroup textureData = loadTextureData(geom, imageData);
            if (textureData == null)
            {
                retFailed = CollectionUtilities.lazyAdd(geom, failed);
            }

            // If the geometry has been deferred, ignore it here.
            else if (!Utilities.sameInstance(DEFERRED_TEXTURE_DATA_GROUP, textureData))
            {
                // If the texture data map is empty, it means the DRAW
                // image was blank and either there was no PICK image or
                // the PICK image was blank as well. With no image to
                // render, ignore this geometry.
                if (textureData.getTextureDataMap().isEmpty())
                {
                    getCache().putCacheAssociation(geom.getImageManager(), BLANK_TEXTURE_GROUP, TextureGroup.class, 0L, 0L);
                }
                else
                {
                    textureDataLoaded.add(geom);
                }
            }
        }

        if (textureLoaded.contains(geom) || textureDataLoaded.contains(geom))
        {
            // Remove the observer here so that the geometry is allowed to
            // get to the ready state before processing another image update.
            // This must be done after the image is polled because removing the
            // observer will clear the cached image.
            removeObserverFromGeometry(geom);
        }

        return retFailed;
    }

    /**
     * Figure out what to do with a geometry in the
     * {@link TextureState#IMAGE_LOADED} state but missing image data.
     *
     * @param geom The geometry.
     * @param textureLoaded Collection to put the geometry in if it should go to
     *            {@link TextureState#TEXTURE_LOADED}.
     * @param reload Collection of geometries to be reloaded. This may be
     *            {@code null}.
     * @return Collection of geometries to be reloaded.
     */
    @Nullable
    private Collection<E> handleMissingImageData(E geom, Collection<E> textureLoaded, @Nullable Collection<E> reload)
    {
        if (((BaseRenderProperties)geom.getRenderProperties()).isDrawable())
        {
            // If the image is already known to be blank, ignore this
            // geometry.
            if (isImageBlank(geom))
            {
                return reload;
            }
            // If the state has been set IMAGE_LOADED, but we cannot
            // find the image, restart processing for the geometry.
            return CollectionUtilities.lazyAdd(geom, reload);
        }
        // Not drawable; go straight to texture loaded.
        textureLoaded.add(geom);
        return reload;
    }

    /**
     * Determine if a texture handle can be used for an image.
     *
     * @param imageData The image data.
     * @param handle The texture handle (may be {@code null}).
     * @param renderMode The render mode.
     * @return {@code true} if the handle can be used.
     */
    private boolean isTextureUsable(ImageGroup imageData, TextureHandle handle, AbstractGeometry.RenderMode renderMode)
    {
        boolean usable;
        if (handle == null)
        {
            usable = false;
        }
        else
        {
            Image image = imageData.getImageMap().get(renderMode);
            try
            {
                usable = !(image instanceof PreloadedTextureImage) && image.getHeight() == handle.getHeight()
                        && image.getWidth() == handle.getWidth();
            }
            catch (IllegalStateException e)
            {
                // IllegalStateException may be thrown if the image has been
                // disposed by another thread.
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace(e, e);
                }
                usable = false;
            }
        }
        return usable;
    }

    /**
     * Processes awaiting images for geometries that share the same image, such
     * as point sprite geometries.
     *
     * @param objects The geometries to process.
     */
    private void processAwaitingImageSharesImage(List<E> objects)
    {
        Map<ImageManager, List<E>> imageManagersToGeoms = New.map();
        for (E geom : objects)
        {
            synchronized (myImageManagersToGeoms)
            {
                if (!myImageManagersToGeoms.containsKey(geom.getImageManager()))
                {
                    List<E> geoms = imageManagersToGeoms.computeIfAbsent(geom.getImageManager(),
                            k -> Collections.synchronizedList(New.list()));
                    geoms.add(geom);
                }
                else
                {
                    myImageManagersToGeoms.get(geom.getImageManager()).add(geom);
                }
            }
        }

        myImageManagersToGeoms.putAll(imageManagersToGeoms);

        for (Entry<ImageManager, List<E>> entry : imageManagersToGeoms.entrySet())
        {
            E geom = entry.getValue().get(0);
            addObserverToGeometry(geom);

            // This must be called after the observer is added or else the
            // request will be ignored.
            geom.requestImageData(getPriorityComparator(), TimeBudget.ZERO);
        }
    }

    /**
     * Processes awaiting image for geometries that have a unique image to load
     * just for them, such as tiles.
     *
     * @param objects The geometries to process.
     */
    private void processAwaitingImageUniqueImage(List<E> objects)
    {
        for (E geom : objects)
        {
            addObserverToGeometry(geom);

            // This must be called after the observer is added or else the
            // request will be ignored.
            geom.requestImageData(getPriorityComparator(), TimeBudget.ZERO);
        }
    }

    /** The states for the state machine. */
    protected enum TextureState implements ThreadedStateMachine.State
    {
        /** State for geometries that have image data. */
        IMAGE_LOADED(State.DEFERRED.getStateOrder() + 10),

        /** State for geometries that are waiting for image data. */
        AWAITING_IMAGE(IMAGE_LOADED.getStateOrder() + 10),

        /**
         * State for geometries that have their {@link TextureData} objects
         * loaded.
         */
        TEXTURE_DATA_LOADED(AWAITING_IMAGE.getStateOrder() + 10),

        /**
         * State for geometries that have their {@link Texture} objects created.
         */
        TEXTURE_LOADED(TEXTURE_DATA_LOADED.getStateOrder() + 10),

        ;

        /** The order of the state. */
        private final int myStateOrder;

        /**
         * Construct a state.
         *
         * @param order The order of the state.
         */
        TextureState(int order)
        {
            myStateOrder = order;
        }

        @Override
        public int getStateOrder()
        {
            return myStateOrder;
        }
    }

    /** Texture capabilities of the GL engine. */
    private static final class TextureCapabilities
    {
        /** Indicates if compressed textures are supported. */
        private boolean myCompressedTexturesSupported;

        /** Flag indicating if the capabilities have been set. */
        private boolean mySet;

        /**
         * Get if compressed textures are supported.
         *
         * @return {@code true} if compressed textures are supported.
         */
        public synchronized boolean isCompressedTexturesSupported()
        {
            if (!mySet)
            {
                try
                {
                    final long timeout = 10000L;
                    wait(timeout);

                    if (!mySet)
                    {
                        LOGGER.warn("Timed out waiting for texture capabilities.");
                    }
                }
                catch (InterruptedException e)
                {
                    LOGGER.warn("Interrupted waiting for texture capabilities.");
                }
            }
            return myCompressedTexturesSupported;
        }

        /**
         * Set if compressed textures are supported.
         *
         * @param flag If compressed textures are supported.
         */
        public synchronized void setCompressedTexturesSupported(boolean flag)
        {
            if (mySet)
            {
                throw new IllegalStateException("Texture capabilities may only be set once.");
            }
            myCompressedTexturesSupported = flag;
            mySet = true;
            notifyAll();
        }
    }
}
