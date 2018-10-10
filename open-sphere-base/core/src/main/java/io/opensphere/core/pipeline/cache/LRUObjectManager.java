package io.opensphere.core.pipeline.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.metrics.MetricsRegistry;
import io.opensphere.core.metrics.impl.DefaultNumberMetricsProvider;
import io.opensphere.core.metrics.impl.DefaultPercentageMetricsProvider;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.TinySet;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Class responsible for keeping track of a set of cached objects in
 * least-recently-used order.
 */
@SuppressWarnings("PMD.GodClass")
abstract class LRUObjectManager
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(LRUObjectManager.class);

    /** The video card cache information. */
    private final CacheStatus myCacheStatusGPU;

    /** The VM cache information. */
    private final CacheStatus myCacheStatusVM;

    /** Lock that ensures that the cleanup method is not reentrant. */
    private final Lock myCleanupLock = new ReentrantLock();

    /** Optional metric tracking the GPU cache size. */
    private DefaultNumberMetricsProvider myGPUCacheMetric;

    /** Optional metric tracking the GPU cache percentage. */
    private DefaultPercentageMetricsProvider myGPUPctCacheMetric;

    /** The map that keeps track of which node set was last referenced. */
    private final Map<Object, LRUNodeSet> myLRUMap = new LinkedHashMap<>(16, .75f, true);

    /** Metric for the max GPU size. */
    private DefaultNumberMetricsProvider myMaxGPUmetric;

    /** Metric for the max VM size. */
    private DefaultNumberMetricsProvider myMaxVMmetric;

    /** Optional metric tracking the VM cache size. */
    private DefaultNumberMetricsProvider myVMCacheMetric;

    /** Optional metric tracking the VM cache percentage. */
    private DefaultPercentageMetricsProvider myVMPctCacheMetric;

    /**
     * Construct the LRU object manager.
     *
     * @param metricsRegistry The optional metrics registry.
     */
    public LRUObjectManager(MetricsRegistry metricsRegistry)
    {
        myCacheStatusVM = new CacheStatus(Long.MAX_VALUE, Long.MAX_VALUE);
        myCacheStatusGPU = new CacheStatus(Long.MAX_VALUE, Long.MAX_VALUE);

        if (metricsRegistry == null)
        {
            myVMCacheMetric = null;
            myGPUCacheMetric = null;
        }
        else
        {
            String topic = "Pipeline";
            String vmSubTopic = "Cache";
            myVMCacheMetric = new DefaultNumberMetricsProvider(1, topic, vmSubTopic, "Current");
            myVMCacheMetric.setPostfix(" MB");
            myVMCacheMetric.setNumberValueFormatter("%.3f");
            myMaxVMmetric = new DefaultNumberMetricsProvider(2, topic, vmSubTopic, "Max");
            myMaxVMmetric.setValue(Long.valueOf(Long.MAX_VALUE / Constants.BYTES_PER_MEGABYTE));
            myMaxVMmetric.setNumberValueFormatter("%d");
            myMaxVMmetric.setPostfix(" MB");
            myVMPctCacheMetric = new DefaultPercentageMetricsProvider(3, topic, vmSubTopic, "Usage");

            String gpuSubTopic = "GPU Memory";
            myGPUCacheMetric = new DefaultNumberMetricsProvider(1, topic, gpuSubTopic, "Current");
            myGPUCacheMetric.setPostfix(" MB");
            myGPUCacheMetric.setNumberValueFormatter("%.3f");
            myMaxGPUmetric = new DefaultNumberMetricsProvider(2, topic, gpuSubTopic, "Max");
            myMaxGPUmetric.setValue(Long.valueOf(Long.MAX_VALUE / Constants.BYTES_PER_MEGABYTE));
            myMaxGPUmetric.setNumberValueFormatter("%d");
            myMaxGPUmetric.setPostfix(" MB");
            myGPUPctCacheMetric = new DefaultPercentageMetricsProvider(3, topic, gpuSubTopic, "Usage");

            metricsRegistry.addMetricsProvider(myVMCacheMetric);
            metricsRegistry.addMetricsProvider(myMaxVMmetric);
            metricsRegistry.addMetricsProvider(myVMPctCacheMetric);
            metricsRegistry.addMetricsProvider(myGPUCacheMetric);
            metricsRegistry.addMetricsProvider(myMaxGPUmetric);
            metricsRegistry.addMetricsProvider(myGPUPctCacheMetric);
        }
    }

    /**
     * Add the node to the LRU map. Add the max size of the node to the cache
     * size.
     *
     * @param node the new node
     */
    public void add(CacheNode node)
    {
        Object object = node.getObject();
        long sizeVM = node.getSizeVM();
        long sizeGPU = node.getSizeGPU();

        synchronized (myLRUMap)
        {
            LRUNodeSet lruNodeSet = myLRUMap.get(object);
            if (lruNodeSet == null)
            {
                lruNodeSet = new LRUNodeSet();
                myLRUMap.put(object, lruNodeSet);
                myCacheStatusVM.addToCurrent(sizeVM);
                myCacheStatusGPU.addToCurrent(sizeGPU);
            }
            else
            {
                long maxSizeVM = lruNodeSet.getMaxNodeSizeVM();
                if (maxSizeVM < sizeVM)
                {
                    myCacheStatusVM.addToCurrent(sizeVM - maxSizeVM);
                }

                long maxSizeGPU = lruNodeSet.getMaxNodeSizeGPU();
                if (maxSizeGPU < sizeGPU)
                {
                    myCacheStatusGPU.addToCurrent(sizeGPU - maxSizeGPU);
                }
            }
            lruNodeSet.addNode(node);
        }
    }

    /**
     * Cleaning routine. This should be called periodically. When called, check
     * the cache size. If the size is too high compared with the JRE maximum
     * memory footprint, clear some portion of the least-recently-used cached
     * objects.
     */
    public void cleanup()
    {
        if (myCleanupLock.tryLock())
        {
            long t0 = System.nanoTime();
            try
            {
                int sizeBefore;
                long sizeBeforeGPU;
                long sizeBeforeVM;
                synchronized (myLRUMap)
                {
                    sizeBefore = myLRUMap.size();
                    sizeBeforeGPU = myCacheStatusGPU.getCurrentSizeBytes();
                    sizeBeforeVM = myCacheStatusVM.getCurrentSizeBytes();
                }

                boolean cleaned = cleanup(myCacheStatusGPU, true) || cleanup(myCacheStatusVM, false);

                if (cleaned && LOGGER.isDebugEnabled())
                {
                    int sizeAfter;
                    synchronized (myLRUMap)
                    {
                        sizeAfter = myLRUMap.size();
                    }
                    long t1 = System.nanoTime();
                    String msg = new StringBuilder(192).append("Cleaned cache (count before/after: ").append(sizeBefore)
                            .append('/').append(sizeAfter).append(", GPU size before/after: ").append(sizeBeforeGPU).append('/')
                            .append(myCacheStatusGPU.getCurrentSizeBytes()).append(" B, VM size before/after: ")
                            .append(sizeBeforeVM).append('/').append(myCacheStatusVM.getCurrentSizeBytes()).append(" B) in ")
                            .toString();
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug(StringUtilities.formatTimingMessage(msg, t1 - t0));
                    }
                    if (LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace(getDetailedCacheStatusString());
                    }
                }
            }
            finally
            {
                myCleanupLock.unlock();
            }
        }
        if (myVMCacheMetric != null)
        {
            myVMCacheMetric
            .setValue(Double.valueOf((double)myCacheStatusVM.getCurrentSizeBytes() / Constants.BYTES_PER_MEGABYTE));
        }
        if (myGPUCacheMetric != null)
        {
            myGPUCacheMetric
            .setValue(Double.valueOf((double)myCacheStatusGPU.getCurrentSizeBytes() / Constants.BYTES_PER_MEGABYTE));
        }
        if (myVMPctCacheMetric != null)
        {
            myVMPctCacheMetric
            .setValue(Double.valueOf((double)myCacheStatusVM.getCurrentSizeBytes() / myCacheStatusVM.getMaxSizeBytes()));
        }
        if (myGPUPctCacheMetric != null)
        {
            myGPUPctCacheMetric.setValue(
                    Double.valueOf((double)myCacheStatusGPU.getCurrentSizeBytes() / myCacheStatusGPU.getMaxSizeBytes()));
        }
    }

    /**
     * Remove all nodes from the LRU map.
     */
    public void clear()
    {
        Map<Object, Set<CacheNode>> toBeRemovedMap;
        synchronized (myLRUMap)
        {
            toBeRemovedMap = new HashMap<>(myLRUMap.size());
            for (Entry<Object, LRUNodeSet> entry : myLRUMap.entrySet())
            {
                toBeRemovedMap.put(entry.getKey(), new HashSet<>(entry.getValue().getNodes()));
            }
        }

        doRemoveFromCache(toBeRemovedMap);
    }

    /**
     * Get a string containing the types of objects in the cache and how much
     * memory they are using.
     *
     * @return The string.
     */
    public String getDetailedCacheStatusString()
    {
        Map<Class<?>, long[]> map = New.map();
        synchronized (myLRUMap)
        {
            for (Entry<Object, LRUNodeSet> entry : myLRUMap.entrySet())
            {
                long[] arr = map.get(entry.getKey().getClass());
                if (arr == null)
                {
                    arr = new long[2];
                    map.put(entry.getKey().getClass(), arr);
                }
                arr[0] += entry.getValue().getMaxNodeSizeVM();
                arr[1] += entry.getValue().getMaxNodeSizeGPU();
            }
        }

        final StringBuilder sb = new StringBuilder(32);
        for (Entry<Class<?>, long[]> entry : map.entrySet())
        {
            sb.append(entry.getKey().getName()).append(" VM bytes: ").append(entry.getValue()[0]).append(" GPU bytes: ")
            .append(entry.getValue()[1]).append(StringUtilities.LINE_SEP);
        }

        return sb.toString();
    }

    /**
     * Record use of objects.
     *
     * @param objects The objects accessed.
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public void recordUse(Collection<Object> objects)
    {
        synchronized (myLRUMap)
        {
            for (Object object : objects)
            {
                // It's possible that the LRU map no longer contains the object
                // if the object was cleared in a different thread.
                myLRUMap.get(object);
            }
        }
    }

    /**
     * Record use of an object.
     *
     * @param object The object accessed.
     */
    public void recordUse(Object object)
    {
        synchronized (myLRUMap)
        {
            // It's possible that the LRU map no longer contains the object
            // if the object was cleared in a different thread.
            myLRUMap.get(object);
        }
    }

    /**
     * Remove a node from the LRU map. Subtract the maximum size of the node's
     * objects from the overall cache size.
     *
     * @param node the node being removed
     */
    public void remove(CacheNode node)
    {
        removeFromLRUMap(Collections.singletonMap(node.getObject(), Collections.singleton(node)));
    }

    /**
     * Remove a collection of nodes from the LRU cache.
     *
     * @param nodes The nodes to be removed.
     */
    public void remove(Collection<CacheNode> nodes)
    {
        Map<Object, Set<CacheNode>> map = new HashMap<>(nodes.size());
        for (CacheNode node : nodes)
        {
            map.put(node.getObject(), Collections.singleton(node));
        }
        removeFromLRUMap(map);
    }

    /**
     * Set the size that the cache will be shrunk to when it reaches the high
     * water mark.
     *
     * @param lowWaterBytesGPU The low water bytes.
     */
    public void setLowWaterBytesGPU(long lowWaterBytesGPU)
    {
        myCacheStatusGPU.setLowWaterBytes(lowWaterBytesGPU);
    }

    /**
     * Set the size that the cache will be shrunk to when it reaches the high
     * water mark.
     *
     * @param lowWaterBytesVM The low water bytes.
     */
    public void setLowWaterBytesVM(long lowWaterBytesVM)
    {
        myCacheStatusVM.setLowWaterBytes(lowWaterBytesVM);
    }

    /**
     * Set the size at which cache cleaning will be triggered.
     *
     * @param maxSizeBytesGPU The max size bytes.
     */
    public void setMaxSizeBytesGPU(long maxSizeBytesGPU)
    {
        LOGGER.info("Setting GPU geometry cache size to " + maxSizeBytesGPU / Constants.BYTES_PER_MEGABYTE + "MB");
        myCacheStatusGPU.setMaxSizeBytes(maxSizeBytesGPU);
        if (myMaxGPUmetric != null)
        {
            myMaxGPUmetric.setValue(Long.valueOf(maxSizeBytesGPU / Constants.BYTES_PER_MEGABYTE));
        }
    }

    /**
     * Set the size at which cache cleaning will be triggered.
     *
     * @param maxSizeBytesVM The max size bytes.
     */
    public void setMaxSizeBytesVM(long maxSizeBytesVM)
    {
        LOGGER.info("Setting VM geometry cache size to " + maxSizeBytesVM / Constants.BYTES_PER_MEGABYTE + "MB");
        myCacheStatusVM.setMaxSizeBytes(maxSizeBytesVM);
        if (myMaxVMmetric != null)
        {
            myMaxVMmetric.setValue(Long.valueOf(maxSizeBytesVM / Constants.BYTES_PER_MEGABYTE));
        }
    }

    /**
     * Callback mechanism that must be implemented by the instance managing the
     * cache to remove objects from the cache. This is called by the
     * {@link #cleanup()} routine.
     *
     * @param nodes The nodes to be removed from the cache.
     * @param type The type of object cached by the nodes.
     */
    protected abstract void removeFromCache(Set<CacheNode> nodes, Class<? extends Object> type);

    /**
     * If necessary, clean the VM cache or GL cache.
     *
     * @param cacheStatus The information for the cache which is being cleaned.
     * @param gpuCache true when the GL cache is being cleaned. In this case
     *            check to make sure that only nodes which affect the GL cache
     *            footprint are removed.
     * @return If the cache was cleaned.
     */
    private boolean cleanup(CacheStatus cacheStatus, boolean gpuCache)
    {
        long needToRemoveBytes;
        if (cacheStatus.getCurrentSizeBytes() > cacheStatus.getMaxSizeBytes())
        {
            needToRemoveBytes = cacheStatus.getCurrentSizeBytes() - cacheStatus.getLowWaterBytes();
        }
        else
        {
            return false;
        }

        Map<Object, Set<CacheNode>> toBeRemovedMap = new HashMap<>();
        synchronized (myLRUMap)
        {
            long toBeRemovedBytes = 0;
            for (Iterator<Entry<Object, LRUNodeSet>> lruIter = myLRUMap.entrySet().iterator(); lruIter.hasNext()
                    && needToRemoveBytes > toBeRemovedBytes;)
            {
                Entry<Object, LRUNodeSet> entry = lruIter.next();
                Object key = entry.getKey();
                Set<CacheNode> entryNodes = entry.getValue().getNodes();

                // Only remove nodes if they will reduce the cache footprint for
                // the cache type we are checking.
                int checkedCacheSize = 0;
                for (CacheNode node : entryNodes)
                {
                    if (gpuCache)
                    {
                        checkedCacheSize += node.getSizeGPU();
                    }
                    else
                    {
                        checkedCacheSize += node.getSizeVM();
                    }
                }
                if (checkedCacheSize > 0)
                {
                    toBeRemovedBytes += checkedCacheSize;
                    toBeRemovedMap.put(key, new HashSet<>(entryNodes));
                }
            }
        }
        if (LOGGER.isTraceEnabled() && !toBeRemovedMap.isEmpty())
        {
            LOGGER.trace(" Removing classes from the LRU cache");
            Map<Class<?>, Set<CacheNode>> classCounts = new HashMap<>();
            for (Entry<Object, Set<CacheNode>> entry : toBeRemovedMap.entrySet())
            {
                Set<CacheNode> nodes = classCounts.get(entry.getKey().getClass());
                if (nodes == null)
                {
                    nodes = new HashSet<>();
                    classCounts.put(entry.getKey().getClass(), nodes);
                }
                nodes.addAll(entry.getValue());
            }

            for (Entry<Class<?>, Set<CacheNode>> entry : classCounts.entrySet())
            {
                LOGGER.trace((gpuCache ? "GPU Cache - " : "VM Cache - ") + entry.getValue().size() + " : " + entry.getKey());
            }
        }
        doRemoveFromCache(toBeRemovedMap);

        return true;
    }

    /**
     * Remove the nodes from the LRU map and from the cache.
     *
     * @param toBeRemovedMap The map of nodes to remove.
     */
    private void doRemoveFromCache(Map<Object, Set<CacheNode>> toBeRemovedMap)
    {
        // Remove from the LRU map first to be sure that there can
        // never be an object in the cache that is not also in the
        // LRU map. If the LRU remove happens after the cache
        // remove, another thread could add it back to the cache in
        // between the calls, and then the LRU remove would remove
        // an object that remains in the cache.
        removeFromLRUMap(toBeRemovedMap);

        Map<Class<? extends Object>, Set<CacheNode>> typeToNodeMap = new HashMap<>();
        for (Entry<Object, Set<CacheNode>> toBeRemoved : toBeRemovedMap.entrySet())
        {
            Class<? extends Object> keyType = toBeRemoved.getKey().getClass();
            Set<CacheNode> nodes = typeToNodeMap.get(keyType);
            if (nodes == null)
            {
                nodes = new HashSet<>();
                typeToNodeMap.put(keyType, nodes);
            }
            nodes.addAll(toBeRemoved.getValue());
        }

        for (Entry<Class<? extends Object>, Set<CacheNode>> entry : typeToNodeMap.entrySet())
        {
            removeFromCache(entry.getValue(), entry.getKey());
        }
    }

    /**
     * Remove nodes from the LRU map. Adjust the current cache size.
     *
     * @param nodeMap a map of cache objects to their nodes
     */
    private void removeFromLRUMap(Map<Object, Set<CacheNode>> nodeMap)
    {
        synchronized (myLRUMap)
        {
            Set<Entry<Object, Set<CacheNode>>> entrySet = nodeMap.entrySet();
            for (Entry<Object, Set<CacheNode>> entry : entrySet)
            {
                Object object = entry.getKey();
                LRUNodeSet lruSet = myLRUMap.get(object);
                if (lruSet != null)
                {
                    Set<CacheNode> nodes = entry.getValue();
                    long maxSizeRemovedVM = lruSet.getMaxNodeSizeVM();
                    long maxSizeRemovedGL = lruSet.getMaxNodeSizeGPU();
                    lruSet.removeAll(nodes);
                    if (lruSet.isEmpty())
                    {
                        myLRUMap.remove(object);
                        myCacheStatusVM.addToCurrent(-maxSizeRemovedVM);
                        myCacheStatusGPU.addToCurrent(-maxSizeRemovedGL);
                    }
                    else
                    {
                        long maxSizeRemainingVM = lruSet.getMaxNodeSizeVM();
                        if (maxSizeRemainingVM < maxSizeRemovedVM)
                        {
                            myCacheStatusVM.addToCurrent(maxSizeRemainingVM - maxSizeRemovedVM);
                        }

                        long maxSizeRemainingGL = lruSet.getMaxNodeSizeGPU();
                        if (maxSizeRemainingGL < maxSizeRemovedGL)
                        {
                            myCacheStatusGPU.addToCurrent(maxSizeRemainingGL - maxSizeRemovedGL);
                        }
                    }
                }
            }
        }
    }

    /** Encapsulates the information associated with a set of cache settings. */
    private static class CacheStatus
    {
        /** The overall cache size. */
        private volatile long myCurrentSizeBytes;

        /**
         * The size to which to reduce the cache when the max bytes is exceeded.
         */
        private volatile long myLowWaterBytes;

        /** The max size used in the ratio calculation. */
        private volatile long myMaxSizeBytes;

        /**
         * Constructor.
         *
         * @param maxSizeBytes The maximum size of the cache before nodes will
         *            be removed.
         * @param lowWaterBytes The maximum size of the cache which will remain
         *            in use after removing nodes.
         */
        public CacheStatus(long maxSizeBytes, long lowWaterBytes)
        {
            myMaxSizeBytes = maxSizeBytes;
            myLowWaterBytes = lowWaterBytes;
        }

        /**
         * Add the given amount to the current size (used amount) of the cache.
         *
         * @param sizeBytes The amount in bytes to add to the cache size.
         */
        public void addToCurrent(long sizeBytes)
        {
            myCurrentSizeBytes += sizeBytes;
        }

        /**
         * Get the currentSizeBytes.
         *
         * @return the currentSizeBytes
         */
        public long getCurrentSizeBytes()
        {
            return myCurrentSizeBytes;
        }

        /**
         * Get the lowWaterBytes.
         *
         * @return the lowWaterBytes
         */
        public long getLowWaterBytes()
        {
            return myLowWaterBytes;
        }

        /**
         * Get the maxSizeBytes.
         *
         * @return the maxSizeBytes
         */
        public long getMaxSizeBytes()
        {
            return myMaxSizeBytes;
        }

        /**
         * Set the size to which to reduce the cache when the max bytes is
         * exceeded.
         *
         * @param lowWaterBytes The lowWaterBytes to set.
         */
        public void setLowWaterBytes(long lowWaterBytes)
        {
            myLowWaterBytes = lowWaterBytes;
        }

        /**
         * Set the size at which the cache will be cleaned.
         *
         * @param maxSizeBytes The maxSizeBytes to set.
         */
        public void setMaxSizeBytes(long maxSizeBytes)
        {
            myMaxSizeBytes = maxSizeBytes;
        }
    }

    /**
     * A class used to manage the objects cached with a single key. As nodes are
     * added to the set, the set keeps track of the largest node added. This
     * size is then subtracted from the overall cache size if the set is removed
     * from the cache.
     */
    private static class LRUNodeSet
    {
        /** The maximum video card size of the nodes in this set. */
        private long myMaxNodeSizeGPU;

        /** The maximum VM size of the nodes in this set. */
        private long myMaxNodeSizeVM;

        /** The nodes in this set. */
        private Set<CacheNode> myNodes = Collections.emptySet();

        /**
         * Add a node to the set.
         *
         * @param node the node added
         */
        public void addNode(CacheNode node)
        {
            myNodes = TinySet.add(myNodes, node);
            myMaxNodeSizeVM = Math.max(myMaxNodeSizeVM, node.getSizeVM());
            myMaxNodeSizeGPU = Math.max(myMaxNodeSizeGPU, node.getSizeGPU());
        }

        /**
         * Get the size of my largest node (for video card usage).
         *
         * @return the max size of my nodes
         */
        public long getMaxNodeSizeGPU()
        {
            return myMaxNodeSizeGPU;
        }

        /**
         * Get the size of my largest node (for VM usage).
         *
         * @return the max size of my nodes
         */
        public long getMaxNodeSizeVM()
        {
            return myMaxNodeSizeVM;
        }

        /**
         * Get my nodes.
         *
         * @return the nodes
         */
        public Set<CacheNode> getNodes()
        {
            return myNodes;
        }

        /**
         * Determine if this node set is empty.
         *
         * @return <code>true</code> if empty
         */
        public boolean isEmpty()
        {
            return myNodes.isEmpty();
        }

        /**
         * Remove nodes from this node set.
         *
         * @param nodes the nodes to be removed
         */
        public void removeAll(Set<CacheNode> nodes)
        {
            int oldSize = myNodes.size();
            myNodes = TinySet.removeAll(myNodes, nodes);
            boolean changed = oldSize != myNodes.size();

            if (changed)
            {
                determineMaxNodeSize(nodes);
            }
        }

        /**
         * Determine the max node size. This must be called if some nodes are
         * removed.
         *
         * @param nodesRemoved The nodes that were removed.
         */
        private void determineMaxNodeSize(Collection<? extends CacheNode> nodesRemoved)
        {
            if (myMaxNodeSizeVM > 0L || myMaxNodeSizeGPU > 0L)
            {
                // If the largest of the removed nodes is smaller than
                // myMaxNodeSize, then the sizes do not need to be checked.
                boolean checkSize = true;
                if (nodesRemoved.size() < myNodes.size())
                {
                    long maxRemovedSizeVM = 0L;
                    long maxRemovedSizeGPU = 0L;
                    for (CacheNode node : nodesRemoved)
                    {
                        long sizeVM = node.getSizeVM();
                        if (sizeVM > maxRemovedSizeVM)
                        {
                            maxRemovedSizeVM = sizeVM;
                        }

                        long sizeGL = node.getSizeGPU();
                        if (sizeGL > maxRemovedSizeGPU)
                        {
                            maxRemovedSizeGPU = sizeGL;
                        }
                    }
                    if (maxRemovedSizeVM < myMaxNodeSizeVM && maxRemovedSizeGPU < myMaxNodeSizeGPU)
                    {
                        checkSize = false;
                    }
                }
                if (checkSize)
                {
                    myMaxNodeSizeVM = 0L;
                    myMaxNodeSizeGPU = 0L;
                    for (CacheNode node : myNodes)
                    {
                        long sizeVM = node.getSizeVM();
                        if (sizeVM > myMaxNodeSizeVM)
                        {
                            myMaxNodeSizeVM = sizeVM;
                        }

                        long sizeGL = node.getSizeGPU();
                        if (sizeGL > myMaxNodeSizeGPU)
                        {
                            myMaxNodeSizeGPU = sizeGL;
                        }
                    }
                }
            }
        }
    }
}
