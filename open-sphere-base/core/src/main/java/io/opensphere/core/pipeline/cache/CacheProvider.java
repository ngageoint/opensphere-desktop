package io.opensphere.core.pipeline.cache;

import java.util.Collection;
import java.util.Set;

import io.opensphere.core.pipeline.cache.CacheContentListener.ContentChangeType;
import io.opensphere.core.util.collections.LazyCollectionProvider;
import io.opensphere.core.util.lang.Pair;

/**
 * Provides a facility that associates keys having a common supertype with
 * values of arbitrary types. When a cache association is made, a type is
 * provided that can be used to look up the association at a later time. The
 * type can be the concrete type of the cached object, or it can be a supertype.
 */
public interface CacheProvider
{
    /**
     * Clear the cache associations for a key that have a specific type.
     *
     * @param <T> The type of association.
     * @param key The key whose associations are being cleared.
     * @param type The type of association being cleared.
     * @return The old association.
     */
    <T> T clearCacheAssociation(Object key, Class<T> type);

    /**
     * Clear all cache associations stored under a particular set of types.
     *
     * @param type The association type.
     */
    void clearCacheAssociations(Class<?>... type);

    /**
     * Clear all cache associations stored under a particular set of types.
     *
     * @param <T> The type of association.
     * @param type The association type.
     * @param clearedCollectionProvider Optional provider for a collection to
     *            contain the cleared associations.
     */
    <T> void clearCacheAssociations(Class<T> type, LazyCollectionProvider<? super T> clearedCollectionProvider);

    /**
     * Clear the cache associations of specific types for a collection of keys.
     *
     * @param keys The keys whose associations are being cleared.
     * @param type The type of association being cleared.
     */
    void clearCacheAssociations(Collection<? extends Object> keys, Class<?>... type);

    /**
     * Clear the cache associations of a specific type for a collection of keys.
     *
     * @param <T> The type of association.
     * @param keys The keys whose associations are being cleared.
     * @param type The type of association being cleared.
     * @param clearedCollectionProvider Optional provider for a collection to
     *            contain the cleared associations.
     */
    <T> void clearCacheAssociations(Collection<? extends Object> keys, Class<T> type,
            LazyCollectionProvider<? super T> clearedCollectionProvider);

    /**
     * Free up any resources that the cache is using and shut down.
     */
    void close();

    /**
     * De-register for notification of content change of items of the given
     * type.
     *
     * @param <T> The type of the items.
     * @param listener Listener to de-register.
     * @param type The class type for which to no longer receive events.
     */
    <T> void deregisterContentListener(CacheContentListener<? super T> listener, Class<T> type);

    /**
     * Retrieve the cache association for a key that was stored with a certain
     * type.
     *
     * @param <T> The type of association.
     * @param key The key.
     * @param type The type used when the association was stored.
     * @return The stored association, or <code>null</code> if it was not stored
     *         or has been cleared.
     */
    <T> T getCacheAssociation(Object key, Class<T> type);

    /**
     * Get all cache associations stored with a certain type, regardless of key.
     * This method also provides the keys that go with the associations.
     *
     * @param <T> The type of the associations.
     * @param type The type of the associations.
     * @return Any associations stored with given types.
     */
    <T> Set<Pair<Object, T>> getCacheAssociationEntries(Class<T> type);

    /**
     * Get all cache associations stored with a certain type, regardless of key.
     *
     * @param <T> The type of the associations.
     * @param type The type of the associations.
     * @return Any associations stored with given types.
     */
    <T> Set<T> getCacheAssociations(Class<T> type);

    /**
     * Insert a cache association.
     *
     * @param <T> The type of the object being cached as it's passed in.
     * @param key The key that the object is associated with.
     * @param object The object being cached.
     * @param type The type that will be used when the cached object is
     *            retrieved.
     * @param sizeVM the amount of VM memory used by the cached object in bytes.
     * @param sizeGPU the amount of video card memory used by the cached object
     *            in bytes
     * @return The old value.
     */
    <T> T putCacheAssociation(Object key, T object, Class<? super T> type, long sizeVM, long sizeGPU);

    /**
     * Register a listener for removal or insertion of a particular type.
     *
     * @param <T> The type of the items.
     * @param listener Listener for content change events.
     * @param changeType Type of content change the listener wants.
     * @param type Class type for which to be notified.
     */
    <T> void registerContentListener(CacheContentListener<? super T> listener, ContentChangeType changeType, Class<T> type);

    /**
     * Set the size that the cache will be shrunk to when it reaches the high
     * water mark.
     *
     * @param lowWaterBytesGPU The low water bytes.
     */
    void setLowWaterBytesGPU(long lowWaterBytesGPU);

    /**
     * Set the size that the cache will be shrunk to when it reaches the high
     * water mark.
     *
     * @param lowWaterBytesVM The low water bytes.
     */
    void setLowWaterBytesVM(long lowWaterBytesVM);

    /**
     * Set the size at which cache cleaning will be triggered.
     *
     * @param maxSizeBytesGPU The max size bytes.
     */
    void setMaxSizeBytesGPU(long maxSizeBytesGPU);

    /**
     * Set the size at which cache cleaning will be triggered.
     *
     * @param maxSizeBytesVM The max size bytes.
     */
    void setMaxSizeBytesVM(long maxSizeBytesVM);
}
