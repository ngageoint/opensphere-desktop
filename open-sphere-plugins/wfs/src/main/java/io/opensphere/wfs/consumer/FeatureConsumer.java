package io.opensphere.wfs.consumer;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.element.MapDataElement;

/**
 * Base class for Consumers that receive Feature data.
 */
public abstract class FeatureConsumer
{
    /** Lock to restrict thread access to Feature List. */
    private final Lock myFeatureLock = new ReentrantLock();

    /** Current features that need to be dispositioned. */
    private final List<MapDataElement> myFeatures = New.list();

    /** The number of records to queue before sending to cache. */
    private final int myFlushSize;

    /**
     * Instantiates a new feature consumer.
     *
     * @param flushSize The number of features to accumulate before flushing to
     *            the data element cache.
     */
    public FeatureConsumer(int flushSize)
    {
        myFlushSize = flushSize;
    }

    /**
     * Add a feature for the consumer.
     *
     * @param feature the feature to add
     */
    public void addFeature(MapDataElement feature)
    {
        myFeatureLock.lock();
        try
        {
            myFeatures.add(feature);
            checkQueue();
        }
        finally
        {
            myFeatureLock.unlock();
        }
    }

    /**
     * Add a collection of features for the consumer.
     *
     * @param features the collection of features to add
     * @return the number of features added
     */
    public int addFeatures(Collection<MapDataElement> features)
    {
        myFeatureLock.lock();
        try
        {
            if (CollectionUtilities.hasContent(features))
            {
                myFeatures.addAll(features);
                checkQueue();
                return features.size();
            }
        }
        finally
        {
            myFeatureLock.unlock();
        }
        return 0;
    }

    /**
     * Force a flush of the features to the consumer. This should also be called
     * once a stream of features is finished to force a final push.
     */
    public abstract void flush();

    /**
     * Clean up after this consumer.
     */
    protected void cleanup()
    {
        myFeatureLock.lock();
        try
        {
            myFeatures.clear();
        }
        finally
        {
            myFeatureLock.unlock();
        }
    }

    /**
     * Return all of the cached features in a new collection and clear my
     * feature cache.
     *
     * @return the cached features.
     */
    protected List<MapDataElement> consumeFeatures()
    {
        myFeatureLock.lock();
        try
        {
            List<MapDataElement> features = New.list(myFeatures);
            myFeatures.clear();
            return features;
        }
        finally
        {
            myFeatureLock.unlock();
        }
    }

    /**
     * Check the current queue of features and determine whether they should be
     * sent to the consumer.
     */
    private void checkQueue()
    {
        if (CollectionUtilities.hasContent(myFeatures) && myFeatures.size() >= myFlushSize)
        {
            flush();
        }
    }
}
