package io.opensphere.core.pipeline.renderer.buffered;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.jogamp.opengl.GL;

import io.opensphere.core.pipeline.cache.CacheContentListener;
import io.opensphere.core.pipeline.cache.CacheContentListener.ContentChangeType;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.util.DisposalHelper;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.ref.Reference;
import io.opensphere.core.util.ref.ReferenceQueue;
import io.opensphere.core.util.ref.TransparentEqualsWeakReference;
import io.opensphere.core.util.ref.WeakReference;

/**
 * Helper for on-card memory disposal handling.
 *
 * @param <E> The type of buffer objects to be disposed.
 */
public class BufferDisposalHelper<E extends BufferObjectList<?>> implements DisposalHelper
{
    /**
     * The executor service used to run the dispose code.
     */
    private static final ExecutorService ourExecutorService = Executors.newFixedThreadPool(1,
            new NamedThreadFactory("BufferDisposalHelper"));

    /** The type of buffered object to clean up. */
    private final Class<E> myBufferedObjectType;

    /** Listener for insertion of buffers into the cache. */
    private final CacheContentListener<BufferObjectList<?>> myBufferInsertionListener;

    /**
     * Reference queue for the buffer data. At the time the buffer data is
     * weakly reachable (or some time after) the reference will be enqueued.
     */
    private final ReferenceQueue<BufferObjectList<?>> myBufferReferenceQueue = new ReferenceQueue<>();

    /** Cache used by the renderer. */
    private final CacheProvider myCache;

    /** A lock to synchronize disposal map writes. */
    private final Lock myDisposalLock = new ReentrantLock();

    /** Map of references to the associated data which must be disposed. */
    private final Map<WeakReference<BufferObjectList<?>>, List<? extends BufferObject>> myDisposalMap = New.map();

    /**
     * Convenience factory method.
     *
     * @param <T> The type of buffered object to clean up.
     * @param type The type of buffered object to clean up.
     * @param cache The cache to watch for added objects.
     * @return The helper.
     */
    public static <T extends BufferObjectList<?>> BufferDisposalHelper<T> create(Class<T> type, CacheProvider cache)
    {
        return new BufferDisposalHelper<T>(type, cache);
    }

    /**
     * Constructor.
     *
     * @param type The type of buffered object to clean up.
     * @param cache the cache to watch for added objects.
     */
    public BufferDisposalHelper(Class<E> type, CacheProvider cache)
    {
        myBufferedObjectType = Utilities.checkNull(type, "type");
        myCache = Utilities.checkNull(cache, "cache");

        myBufferInsertionListener = new CacheContentListener<BufferObjectList<?>>()
        {
            @Override
            public void handleCacheContentChange(final CacheContentEvent<BufferObjectList<?>> event)
            {
                ourExecutorService.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        myDisposalLock.lock();
                        try
                        {
                            for (BufferObjectList<?> bufferObjectList : event.getChangedItems())
                            {
                                // Use TransparentEqualsWeakReference so that we
                                // can use regular map lookup and only add this
                                // reference to the map if there is no reference
                                // for this object already.
                                WeakReference<BufferObjectList<?>> ref = new TransparentEqualsWeakReference<BufferObjectList<?>>(
                                        bufferObjectList, myBufferReferenceQueue);
                                myDisposalMap.put(ref, bufferObjectList.getBufferObjects());
                            }
                        }
                        finally
                        {
                            myDisposalLock.unlock();
                        }
                    }
                });
            }
        };
    }

    @Override
    public void cleanOncardMemory(GL gl)
    {
        Reference<? extends BufferObjectList<?>> ref = null;
        myDisposalLock.lock();
        try
        {
            while ((ref = myBufferReferenceQueue.poll()) != null)
            {
                List<? extends BufferObject> buffers = myDisposalMap.remove(ref);
                if (buffers != null)
                {
                    disposeBuffers(gl, buffers);
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
        getCache().deregisterContentListener(myBufferInsertionListener, myBufferedObjectType);
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
        BufferDisposalHelper<?> other = (BufferDisposalHelper<?>)obj;
        if (!Objects.equals(myBufferedObjectType, other.myBufferedObjectType))
        {
            return false;
        }
        return Objects.equals(myCache, other.myCache);
    }

    @Override
    public void forceDispose(GL gl)
    {
        myDisposalLock.lock();
        try
        {
            for (List<? extends BufferObject> blocks : myDisposalMap.values())
            {
                disposeBuffers(gl, blocks);
            }
            myDisposalMap.clear();
        }
        finally
        {
            myDisposalLock.unlock();
        }
    }

    /**
     * Get the cache.
     *
     * @return the cache
     */
    public CacheProvider getCache()
    {
        return myCache;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myBufferedObjectType == null ? 0 : myBufferedObjectType.hashCode());
        result = prime * result + (myCache == null ? 0 : myCache.hashCode());
        return result;
    }

    @Override
    public void open()
    {
        getCache().registerContentListener(myBufferInsertionListener, ContentChangeType.INSERTION, myBufferedObjectType);
    }

    /**
     * Dispose of some buffers.
     *
     * @param gl The GL context.
     * @param buffers The buffers.
     */
    private void disposeBuffers(GL gl, List<? extends BufferObject> buffers)
    {
        for (BufferObject buffer : buffers)
        {
            buffer.dispose(gl);
        }
    }
}
