package io.opensphere.core.pipeline.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import edu.umd.cs.findbugs.annotations.Nullable;
import net.jcip.annotations.GuardedBy;

import io.opensphere.core.metrics.MetricsRegistry;
import io.opensphere.core.pipeline.cache.CacheContentListener.CacheContentEvent;
import io.opensphere.core.pipeline.cache.CacheContentListener.ContentChangeType;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.LazyCollectionProvider;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;

/**
 * This is a facility that allows objects to be associated with keys by object
 * type. This cache implementation is fully in-memory, and tracks its memory
 * usage. Once the cache contents reach the configured limit, blocks of the
 * least-recently-used references are cleared.
 * <p>
 * In order to handle cache entries that have memory impacts that the JRE is not
 * aware of, the size of objects placed in the cache may be specified. The total
 * size of the cache is monitored to determine when cleaning should occur.
 */
@SuppressWarnings("PMD.GodClass")
public class LRUMemoryCache implements CacheProvider
{
    /**
     * The map of cached object types to maps of keys to cached object nodes.
     */
    @GuardedBy("myCacheMap")
    private final Map<Class<?>, Map<Object, CacheNode>> myCacheMap = new HashMap<>(32);

    /** The timer for the cleanup monitor. */
    @GuardedBy("this")
    private Timer myCleanupTimer;

    /** Listeners for insertion of items into the cache. */
    @GuardedBy("myInsertionListeners")
    private final Map<Class<?>, List<CacheContentListener<?>>> myInsertionListeners = New.weakMap();

    /**
     * Instance that helps with cache cleaning. This is synchronized with the
     * values of myCacheMap to prevent them getting out of sync.
     */
    private final LRUObjectManager myLRUObjectManager;

    /** The optional metrics registry. */
    @Nullable
    private final MetricsRegistry myMetricsRegistry;

    /** Listeners for removal of items from the cache. */
    @GuardedBy("myRemovalListeners")
    private final Map<Class<?>, List<CacheContentListener<?>>> myRemovalListeners = New.weakMap();

    /**
     * An optional metrics registry to provide metrics to.
     *
     * @param metricsRegistry The metrics registry.
     */
    public LRUMemoryCache(MetricsRegistry metricsRegistry)
    {
        myMetricsRegistry = metricsRegistry;
        myLRUObjectManager = new LRUObjectManager(myMetricsRegistry)
        {
            @Override
            public void removeFromCache(Set<CacheNode> nodes, Class<? extends Object> type)
            {
                LRUMemoryCache.this.removeFromCache(nodes, type);
            }
        };
    }

    @Override
    public <T> T clearCacheAssociation(Object key, Class<T> type)
    {
        Map<Object, CacheNode> map;
        synchronized (myCacheMap)
        {
            map = myCacheMap.get(type);
        }
        CacheNode node;
        if (map == null)
        {
            node = null;
        }
        else
        {
            Collection<CacheNode> removes;
            synchronized (map)
            {
                node = map.remove(key);
                if (node == null)
                {
                    removes = Collections.emptyList();
                }
                else
                {
                    removes = Collections.singletonList(node);
                    removeFromLRUOM(node);
                }
            }
            notifyContentsChanged(removes, ContentChangeType.REMOVAL, type);
        }
        @SuppressWarnings("unchecked")
        T cast = node == null ? null : (T)node.getObject();
        return cast;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void clearCacheAssociations(Class<T> type, LazyCollectionProvider<? super T> clearedCollectionProvider)
    {
        Map<Object, CacheNode> map;
        synchronized (myCacheMap)
        {
            map = myCacheMap.remove(type);
        }
        if (map != null)
        {
            List<CacheNode> removes;
            synchronized (map)
            {
                if (map.isEmpty())
                {
                    removes = null;
                }
                else
                {
                    removes = new ArrayList<>(map.values().size());
                    for (CacheNode node : map.values())
                    {
                        if (node.getSizeGPU() != 0L || node.getSizeVM() != 0L)
                        {
                            removes.add(node);
                        }
                        if (clearedCollectionProvider != null)
                        {
                            clearedCollectionProvider.get().add((T)node.getObject());
                        }
                    }
                    if (!removes.isEmpty())
                    {
                        myLRUObjectManager.remove(removes);
                    }
                    map.clear();
                }
            }
            if (removes != null)
            {
                notifyContentsChanged(removes, ContentChangeType.REMOVAL, type);
            }
        }
    }

    @Override
    public void clearCacheAssociations(Class<?>... type)
    {
        for (Class<?> cl : type)
        {
            clearCacheAssociations(cl, (LazyCollectionProvider<Object>)null);
        }
    }

    @Override
    public void clearCacheAssociations(Collection<? extends Object> keys, Class<?>... type)
    {
        for (Class<?> cl : type)
        {
            clearCacheAssociations(keys, cl, (LazyCollectionProvider<Object>)null);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void clearCacheAssociations(Collection<? extends Object> keys, Class<T> type,
            LazyCollectionProvider<? super T> clearedCollectionProvider)
    {
        Map<Object, CacheNode> map;
        synchronized (myCacheMap)
        {
            map = myCacheMap.get(type);
        }
        if (map != null)
        {
            CacheNode node;
            List<CacheNode> removedNodes = null;
            synchronized (map)
            {
                if (!map.isEmpty())
                {
                    for (Object key : keys)
                    {
                        node = map.remove(key);
                        if (node != null)
                        {
                            if (removedNodes == null)
                            {
                                removedNodes = new ArrayList<>(keys.size());
                            }
                            removedNodes.add(node);
                            removeFromLRUOM(node);

                            if (clearedCollectionProvider != null)
                            {
                                clearedCollectionProvider.get().add((T)node.getObject());
                            }
                        }
                    }
                }
            }
            if (removedNodes != null && !removedNodes.isEmpty())
            {
                notifyContentsChanged(removedNodes, ContentChangeType.REMOVAL, type);
            }
        }
    }

    @Override
    public void close()
    {
        myLRUObjectManager.clear();
    }

    @Override
    public <T> void deregisterContentListener(CacheContentListener<? super T> listener, Class<T> type)
    {
        synchronized (myRemovalListeners)
        {
            List<CacheContentListener<?>> listeners = myRemovalListeners.get(type);
            if (listeners != null)
            {
                listeners.remove(listener);
                if (listeners.isEmpty())
                {
                    myRemovalListeners.remove(type);
                }
            }
        }

        synchronized (myInsertionListeners)
        {
            List<CacheContentListener<?>> listeners = myInsertionListeners.get(type);
            if (listeners != null)
            {
                listeners.remove(listener);
                if (listeners.isEmpty())
                {
                    myInsertionListeners.remove(type);
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCacheAssociation(Object key, Class<T> type)
    {
        T object;
        Map<Object, CacheNode> map;
        synchronized (myCacheMap)
        {
            map = myCacheMap.get(type);
        }
        if (map == null)
        {
            object = null;
        }
        else
        {
            synchronized (map)
            {
                CacheNode node = map.get(key);
                if (node == null)
                {
                    object = null;
                }
                else
                {
                    object = (T)node.getObject();
                    if (object != null)
                    {
                        myLRUObjectManager.recordUse(object);
                    }
                }
            }
        }
        return object;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Set<Pair<Object, T>> getCacheAssociationEntries(Class<T> type)
    {
        Set<Pair<Object, T>> results = New.set();
        Set<T> used = new HashSet<>();
        Map<Object, CacheNode> map;
        synchronized (myCacheMap)
        {
            map = myCacheMap.get(type);
        }
        if (map != null)
        {
            synchronized (map)
            {
                Collection<Entry<Object, CacheNode>> values = map.entrySet();
                for (Entry<Object, CacheNode> node : values)
                {
                    results.add(new Pair<Object, T>(node.getKey(), (T)node.getValue().getObject()));
                }
                myLRUObjectManager.recordUse((Set<Object>)used);
            }
        }
        return results;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Set<T> getCacheAssociations(Class<T> type)
    {
        Set<T> results = new HashSet<>();
        Map<Object, CacheNode> map;
        synchronized (myCacheMap)
        {
            map = myCacheMap.get(type);
        }
        if (map != null)
        {
            synchronized (map)
            {
                Collection<CacheNode> values = map.values();
                for (CacheNode node : values)
                {
                    results.add((T)node.getObject());
                }
                myLRUObjectManager.recordUse((Set<Object>)results);
            }
        }
        return results;
    }

    /**
     * Initialize the memory sensitive cache.
     */
    public synchronized void initialize()
    {
        synchronized (myCacheMap)
        {
            if (!myCacheMap.isEmpty())
            {
                clearCacheAssociations(New.array(myCacheMap.keySet(), Class.class));
            }
        }

        if (myCleanupTimer == null)
        {
            TimerTask task = new TimerTask()
            {
                @Override
                public void run()
                {
                    myLRUObjectManager.cleanup();
                }
            };
            final long milliseconds = 1000L;
            myCleanupTimer = new Timer("Cache-monitor", true);
            myCleanupTimer.scheduleAtFixedRate(task, 0L, milliseconds);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T putCacheAssociation(Object key, T object, Class<? super T> type, long sizeVM, long sizeGPU)
    {
        Map<Object, CacheNode> map;
        CacheNode node = new CacheNode(object, sizeVM, sizeGPU);
        synchronized (myCacheMap)
        {
            map = myCacheMap.get(type);
            if (map == null)
            {
                map = New.weakMap(1024);
                myCacheMap.put(type, map);
            }
        }
        CacheNode old;
        synchronized (map)
        {
            old = map.put(key, node);
            if (old == null || !Utilities.sameInstance(old.getObject(), object) || old.getSizeGPU() != sizeGPU
                    || old.getSizeVM() != sizeVM)
            {
                addToLRUOM(node);
                if (old != null && (old.getSizeGPU() | old.getSizeVM()) != 0L)
                {
                    removeFromLRUOM(old);
                }
            }
        }
        if (old == null || !Utilities.sameInstance(old.getObject(), object))
        {
            if (old != null)
            {
                notifyContentsChanged(Collections.singleton(old), ContentChangeType.REMOVAL, type);
            }
            notifyContentsChanged(Collections.singleton(node), ContentChangeType.INSERTION, type);
        }
        return old == null ? null : (T)old.getObject();
    }

    @Override
    public <T> void registerContentListener(CacheContentListener<? super T> listener, ContentChangeType changeType, Class<T> type)
    {
        if (changeType == ContentChangeType.REMOVAL || changeType == ContentChangeType.ALL)
        {
            synchronized (myRemovalListeners)
            {
                List<CacheContentListener<?>> listeners = myRemovalListeners.get(type);
                if (listeners == null)
                {
                    listeners = new ArrayList<>();
                    myRemovalListeners.put(type, listeners);
                }
                listeners.add(listener);
            }
        }

        if (changeType == ContentChangeType.INSERTION || changeType == ContentChangeType.ALL)
        {
            synchronized (myInsertionListeners)
            {
                List<CacheContentListener<?>> listeners = myInsertionListeners.get(type);
                if (listeners == null)
                {
                    listeners = new ArrayList<>();
                    myInsertionListeners.put(type, listeners);
                }
                listeners.add(listener);
            }
        }
    }

    @Override
    public void setLowWaterBytesGPU(long lowWaterBytesGPU)
    {
        myLRUObjectManager.setLowWaterBytesGPU(lowWaterBytesGPU);
    }

    @Override
    public void setLowWaterBytesVM(long lowWaterBytesVM)
    {
        myLRUObjectManager.setLowWaterBytesVM(lowWaterBytesVM);
    }

    @Override
    public void setMaxSizeBytesGPU(long maxSizeBytesGPU)
    {
        myLRUObjectManager.setMaxSizeBytesGPU(maxSizeBytesGPU);
    }

    @Override
    public void setMaxSizeBytesVM(long maxSizeBytesVM)
    {
        myLRUObjectManager.setMaxSizeBytesVM(maxSizeBytesVM);
    }

    @Override
    protected synchronized void finalize() throws Throwable
    {
        if (myCleanupTimer != null)
        {
            myCleanupTimer.cancel();
        }
        super.finalize();
    }

    /**
     * Add a node to the LRU Object Manager (if it has any size).
     *
     * @param node The node.
     */
    private void addToLRUOM(CacheNode node)
    {
        if (node.getSizeGPU() != 0L || node.getSizeVM() != 0L)
        {
            myLRUObjectManager.add(node);
        }
    }

    /**
     * Notify listener that items have been inserted or removed from the cache.
     *
     * @param changedItems The items which have been inserted or removed from
     *            the cache.
     * @param changeType The type of contents change which has occurred (insert
     *            or remove).
     * @param type The type of the items that were changed.
     */
    private void notifyContentsChanged(Collection<CacheNode> changedItems, ContentChangeType changeType, Class<?> type)
    {
        Map<Class<?>, List<CacheContentListener<?>>> listeners;
        if (changeType == ContentChangeType.REMOVAL)
        {
            listeners = myRemovalListeners;
        }
        else
        {
            listeners = myInsertionListeners;
        }

        Collection<CacheContentListener<?>> listenersForType;
        synchronized (listeners)
        {
            if (listeners.isEmpty())
            {
                return;
            }
            listenersForType = listeners.get(type);
            if (listenersForType == null || listenersForType.isEmpty())
            {
                return;
            }
            else
            {
                listenersForType = New.<CacheContentListener<?>>collection(listenersForType);
            }
        }
        Collection<Object> items = New.collection(changedItems.size());
        for (CacheNode item : changedItems)
        {
            items.add(item.getObject());
        }
        CacheContentEvent<Object> event = new CacheContentEvent<Object>(items, changeType);
        for (CacheContentListener<?> cacheContentListener : listenersForType)
        {
            @SuppressWarnings("unchecked")
            final CacheContentListener<Object> cast = (CacheContentListener<Object>)cacheContentListener;
            cast.handleCacheContentChange(event);
        }
    }

    /**
     * Helper method that removes nodes from the cache map. This is only called
     * from the cache cleaning thread, so it's less important to be fast and
     * more important to not block operations occurring on other threads.
     *
     * @param nodes The nodes to be removed.
     * @param objectType The type of object referenced by the nodes, used as a
     *            filter so that the whole cache doesn't have to be searched.
     */
    private void removeFromCache(Set<CacheNode> nodes, Class<? extends Object> objectType)
    {
        Iterable<Class<?>> cacheTypes;
        synchronized (myCacheMap)
        {
            cacheTypes = new ArrayList<>(myCacheMap.keySet());
        }

        for (Class<?> type : cacheTypes)
        {
            if (type.isAssignableFrom(objectType))
            {
                Map<Object, CacheNode> map;
                synchronized (myCacheMap)
                {
                    map = myCacheMap.get(type);
                }
                if (map != null)
                {
                    synchronized (map)
                    {
                        for (Iterator<CacheNode> nodeIter = map.values().iterator(); nodeIter.hasNext();)
                        {
                            CacheNode node = nodeIter.next();
                            if (nodes.contains(node))
                            {
                                nodeIter.remove();
                            }
                        }
                    }
                    if (map.isEmpty())
                    {
                        synchronized (myCacheMap)
                        {
                            myCacheMap.remove(type);
                        }
                    }
                }
                notifyContentsChanged(nodes, ContentChangeType.REMOVAL, type);
            }
        }
    }

    /**
     * Remove a node from the LRU Object Manager.
     *
     * @param node The node.
     */
    private void removeFromLRUOM(CacheNode node)
    {
        // If the node has no size, it will not be in the object manager.
        if (node.getSizeGPU() != 0L || node.getSizeVM() != 0L)
        {
            myLRUObjectManager.remove(node);
        }
    }
}
