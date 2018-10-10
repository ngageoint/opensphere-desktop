package io.opensphere.core.pipeline.processor;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL;

import io.opensphere.core.pipeline.cache.CacheContentListener;
import io.opensphere.core.pipeline.cache.CacheContentListener.ContentChangeType;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.util.DisposalHelper;
import io.opensphere.core.pipeline.util.TextureHandle;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.ref.Reference;
import io.opensphere.core.util.ref.ReferenceQueue;
import io.opensphere.core.util.ref.TransparentEqualsWeakReference;
import io.opensphere.core.util.ref.WeakReference;

/** Helper for disposing textures from GPU memory. */
public class TextureDisposalHelper implements DisposalHelper
{
    /** Cache used by the renderer. */
    private final CacheProvider myCache;

    /** A lock to synchronize disposal map writes. */
    private final Lock myDisposalLock = new ReentrantLock();

    /**
     * A map to store references to textures which will require disposal at some
     * later time.
     */
    private final Map<WeakReference<TextureHandle>, Integer> myTextureDisposalMap = New.map();

    /** Listener for insertion of textures into the cache. */
    private final CacheContentListener<TextureHandle> myTextureHandleInsertionListener;

    /**
     * Reference queue for the texture handles. At the time the texture handle
     * is weakly reachable (or some time after) the reference will be enqueued.
     */
    private final ReferenceQueue<TextureHandle> myTextureHandleReferenceQueue = new ReferenceQueue<>();

    /**
     * Constructor.
     *
     * @param cache Cache used by the renderer.
     */
    public TextureDisposalHelper(CacheProvider cache)
    {
        myCache = cache;

        myTextureHandleInsertionListener = getTileTextureGroupContentChangeListener();
    }

    @Override
    public void cleanOncardMemory(GL gl)
    {
        Reference<? extends TextureHandle> ref = null;
        myDisposalLock.lock();
        try
        {
            while ((ref = myTextureHandleReferenceQueue.poll()) != null)
            {
                Integer textureId = myTextureDisposalMap.remove(ref);
                if (textureId != null)
                {
                    disposeTexture(gl, textureId);
                }
            }
        }
        finally
        {
            myDisposalLock.unlock();
        }
    }

    @Override
    public void close()
    {
        myCache.deregisterContentListener(myTextureHandleInsertionListener, TextureHandle.class);
    }

    @Override
    public void forceDispose(GL gl)
    {
        myDisposalLock.lock();
        try
        {
            Collection<Integer> values = myTextureDisposalMap.values();
            for (Integer textureId : values)
            {
                disposeTexture(gl, textureId);
            }
            myTextureDisposalMap.clear();
        }
        finally
        {
            myDisposalLock.unlock();
        }
    }

    @Override
    public void open()
    {
        myCache.registerContentListener(myTextureHandleInsertionListener, ContentChangeType.INSERTION, TextureHandle.class);
    }

    /**
     * Dispose a texture.
     *
     * @param gl The GL context.
     * @param textureId The texture id.
     */
    private void disposeTexture(GL gl, Integer textureId)
    {
        new TextureHandle(textureId.intValue(), 0, 0, 0).dispose(gl);
    }

    /**
     * Create the cache content change listener for tile data.
     *
     * @return the tile date cache content change listener.
     */
    private CacheContentListener<TextureHandle> getTileTextureGroupContentChangeListener()
    {
        return event ->
        {
            final Collection<? extends TextureHandle> textureHandles = event.getChangedItems();

            ThreadUtilities.runBackground(() ->
            {
                myDisposalLock.lock();
                try
                {
                    for (TextureHandle handle : textureHandles)
                    {
                        if (handle != null)
                        {
                            // Use TransparentEqualsWeakReference so
                            // that we can use regular map lookup and
                            // only add this reference to the map if
                            // there is no reference for this object
                            // already.
                            WeakReference<TextureHandle> ref = new TransparentEqualsWeakReference<>(handle,
                                    myTextureHandleReferenceQueue);
                            myTextureDisposalMap.put(ref, Integer.valueOf(handle.getTextureId()));
                        }
                    }
                }
                finally
                {
                    myDisposalLock.unlock();
                }
            });
        };
    }
}
