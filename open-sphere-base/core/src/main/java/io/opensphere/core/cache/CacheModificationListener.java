package io.opensphere.core.cache;

/**
 * Interface for objects interested in cache modifications.
 */
@FunctionalInterface
public interface CacheModificationListener
{
    /**
     * Method called when the cache is modified.
     *
     * @param cacheModificationReport The cache modification report.
     */
    void cacheModified(CacheModificationReport cacheModificationReport);
}
